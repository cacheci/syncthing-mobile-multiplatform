package moe.https.syncthing.core

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.https.syncthing.MainActivity
import moe.https.syncthing.R
import moe.https.syncthing.SyncthingApplication
import moe.https.syncthing.storage.AppSettingPrivateStorage
import moe.https.syncthing.ui.util.AutoStartModeType
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds

class SyncthingCoreService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val preferences by lazy {
        getSharedPreferences(PREFERENCES, MODE_PRIVATE)
    }
    private val runtime: CoreRuntime
        get() = (application as SyncthingApplication).coreRuntime

    private var supervisorJob: Job? = null
    private var stoppingJob: Job? = null
    private var foregroundStarted = false
    private var wakeLock: PowerManager.WakeLock? = null
    private var automaticControlActive = false
    private var destroyed = false
    private lateinit var conditionMonitor: AutoStartConditionMonitor

    override fun onCreate() {
        super.onCreate()
        automaticControlActive = preferences.getBoolean(KEY_AUTOMATIC_CONTROL_ACTIVE, false)
        conditionMonitor = AutoStartConditionMonitor(
            context = this,
            storage = (application as SyncthingApplication).appSettingsStorage,
            onConditionChanged = { conditionsSatisfied ->
                if (automaticControlActive) {
                    requestCoreRunning(conditionsSatisfied)
                }
            },
            onStartTriggered = {
                if (automaticControlActive) {
                    requestCoreRunning(true)
                }
            },
            onStopTriggered = {
                if (automaticControlActive) {
                    requestCoreRunning(false)
                }
            },
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        serviceScope.launch {
            runtime.snapshot.collectLatest { snapshot ->
                if (foregroundStarted) {
                    getSystemService(NotificationManager::class.java)
                        .notify(NOTIFICATION_ID, buildNotification(snapshot))
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> requestManualStop()
            ACTION_START -> requestManualStart()
            ACTION_RESTART -> requestManagedRestart()
            ACTION_REEVALUATE_AUTO_START -> configureAutomaticControl()
            else -> restoreDesiredMode()
        }
        return if (!shouldRemainStarted()) {
            START_NOT_STICKY
        } else {
            START_STICKY
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        destroyed = true
        conditionMonitor.stop()
        releaseWakeLock()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun requestManualStart() {
        requestCoreRunning(true)
    }

    private fun requestManualStop() {
        requestCoreRunning(
            shouldRun = false,
            stopServiceWhenStopped = !automaticControlActive,
        )
    }

    private fun requestManagedRestart() {
        if (!preferences.getBoolean(KEY_DESIRED_RUNNING, false)) return
        val runningSupervisor = supervisorJob
        if (runningSupervisor == null) {
            requestCoreRunning(true)
        } else {
            stopCore(stopServiceWhenStopped = false)
        }
    }

    private fun restoreDesiredMode() {
        when {
            preferences.getBoolean(KEY_AUTOMATIC_CONTROL_ACTIVE, false) ->
                configureAutomaticControl()
            preferences.getBoolean(KEY_DESIRED_RUNNING, false) -> requestManualStart()
            else -> stopSelf()
        }
    }

    private fun configureAutomaticControl() {
        val mode = loadAutoStartMode()
        if (mode == AutoStartModeType.DISABLED) {
            conditionMonitor.stop()
            if (automaticControlActive) {
                setAutomaticControlActive(false)
                requestCoreRunning(shouldRun = false, stopServiceWhenStopped = true)
            } else if (!preferences.getBoolean(KEY_DESIRED_RUNNING, false)) {
                stopSelf()
            }
            return
        }

        setAutomaticControlActive(true)
        ensureForeground()
        when (mode) {
            AutoStartModeType.ENABLED -> {
                conditionMonitor.stop()
                requestCoreRunning(true)
            }
            AutoStartModeType.WITH_CONDITION -> conditionMonitor.start()
        }
    }

    private fun setAutomaticControlActive(active: Boolean) {
        automaticControlActive = active
        preferences.edit { putBoolean(KEY_AUTOMATIC_CONTROL_ACTIVE, active) }
    }

    private fun requestCoreRunning(
        shouldRun: Boolean,
        stopServiceWhenStopped: Boolean = false,
    ) {
        preferences.edit { putBoolean(KEY_DESIRED_RUNNING, shouldRun) }
        if (!shouldRun) {
            stopCore(stopServiceWhenStopped)
            return
        }

        ensureForeground()
        if (stoppingJob?.isActive == true || supervisorJob?.isActive == true) return
        launchCoreSupervisor()
    }

    private fun ensureForeground() {
        if (!foregroundStarted) {
            foregroundStarted = true
            startForeground(NOTIFICATION_ID, buildNotification(runtime.snapshot.value))
        } else {
            getSystemService(NotificationManager::class.java)
                .notify(NOTIFICATION_ID, buildNotification(runtime.snapshot.value))
        }
    }

    private fun launchCoreSupervisor() {
        acquireWakeLock()
        supervisorJob = serviceScope.launch(Dispatchers.IO) {
            var failures = 0
            while (preferences.getBoolean(KEY_DESIRED_RUNNING, false)) {
                val result = try {
                    runtime.runSession()
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    runtime.fail(
                        message = error.message ?: error.javaClass.simpleName,
                        logMessage = "Core supervisor caught an unhandled exception",
                        error = error,
                        includeCoreLogs = true,
                    )
                    CoreRuntime.SessionResult(
                        started = false,
                        runtimeMillis = 0,
                        exitCode = null,
                    )
                }

                if (!preferences.getBoolean(KEY_DESIRED_RUNNING, false)) break

                failures = if (result.runtimeMillis >= STABLE_SESSION_MILLIS) 0 else failures + 1
                if (!result.started || failures >= MAX_RESTART_ATTEMPTS) {
                    preferences.edit { putBoolean(KEY_DESIRED_RUNNING, false) }
                    runtime.fail(
                        message = getString(R.string.core_retries_stopped),
                        logMessage = "Core failed to start repeatedly; automatic retries have stopped",
                        includeCoreLogs = true,
                    )
                    break
                }

                val backoff = min(
                    INITIAL_BACKOFF_MILLIS * (1L shl (failures - 1).coerceAtLeast(0)),
                    MAX_BACKOFF_MILLIS,
                )
                delay(backoff.milliseconds)
            }

            val finishedJob = coroutineContext[Job]
            withContext(NonCancellable + Dispatchers.Main.immediate) {
                if (finishedJob != null && supervisorJob === finishedJob && stoppingJob == null) {
                    supervisorJob = null
                    if (!destroyed) onCoreStopped()
                }
            }
        }
    }

    private fun stopCore(stopServiceWhenStopped: Boolean) {
        val runningSupervisor = supervisorJob
        if (runningSupervisor == null) {
            onCoreStopped(stopServiceWhenStopped)
            return
        }
        if (stoppingJob?.isActive == true) return
        stoppingJob = serviceScope.launch {
            runningSupervisor.cancelAndJoin()
            withContext(Dispatchers.IO) {
                runtime.stop()
            }
            if (supervisorJob === runningSupervisor) supervisorJob = null
            stoppingJob = null
            if (preferences.getBoolean(KEY_DESIRED_RUNNING, false)) {
                launchCoreSupervisor()
            } else {
                onCoreStopped(stopServiceWhenStopped)
            }
        }
    }

    private fun onCoreStopped(stopServiceWhenStopped: Boolean = false) {
        releaseWakeLock()
        if (destroyed) return
        if (automaticControlActive && !stopServiceWhenStopped) {
            ensureForeground()
            return
        }
        if (foregroundStarted) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            foregroundStarted = false
        }
        stopSelf()
    }

    private fun loadAutoStartMode(): AutoStartModeType {
        val storage = (application as SyncthingApplication).appSettingsStorage
        return storage.getString(AppSettingPrivateStorage.KEY_AUTO_START_MODE)
            ?.let { stored -> AutoStartModeType.entries.firstOrNull { it.name == stored } }
            ?: AutoStartModeType.DISABLED
    }

    private fun shouldRemainStarted(): Boolean =
        automaticControlActive || preferences.getBoolean(KEY_DESIRED_RUNNING, false)

    @Suppress("WakelockTimeout")
    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        val powerManager = getSystemService(PowerManager::class.java)
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "$packageName:SyncthingCore",
        ).apply { acquire() }
    }

    private fun releaseWakeLock() {
        wakeLock?.takeIf { it.isHeld }?.release()
        wakeLock = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.core_notification_channel_core),
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = getString(R.string.core_notification_channel_core_description)
                setShowBadge(false)
            },
        )
    }

    private fun buildNotification(snapshot: CoreSnapshot): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, SyncthingCoreService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle(getString(R.string.core_notification_channel_core))
            .setContentText(
                if (automaticControlActive &&
                    !preferences.getBoolean(KEY_DESIRED_RUNNING, false)
                ) {
                    getString(R.string.core_notification_waiting_for_conditions)
                } else {
                    snapshot.notificationText(this)
                },
            )
            .setContentIntent(contentIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .addAction(0, getString(R.string.core_action_stop), stopIntent)
            .build()
    }

    companion object {
        const val ACTION_START = "moe.https.syncthing.action.START_CORE"
        const val ACTION_STOP = "moe.https.syncthing.action.STOP_CORE"
        private const val ACTION_RESTART = "moe.https.syncthing.action.RESTART_CORE"
        const val ACTION_REEVALUATE_AUTO_START =
            "moe.https.syncthing.action.REEVALUATE_AUTO_START"

        private const val PREFERENCES = "core_service"
        private const val KEY_DESIRED_RUNNING = "desired_running"
        private const val KEY_AUTOMATIC_CONTROL_ACTIVE = "automatic_control_active"
        private const val NOTIFICATION_CHANNEL_ID = "syncthing_core"
        private const val NOTIFICATION_ID = 1001
        private const val MAX_RESTART_ATTEMPTS = 3
        private const val INITIAL_BACKOFF_MILLIS = 1_000L
        private const val MAX_BACKOFF_MILLIS = 30_000L
        private const val STABLE_SESSION_MILLIS = 60_000L

        internal fun isDesiredRunning(context: android.content.Context): Boolean =
            context.getSharedPreferences(PREFERENCES, MODE_PRIVATE)
                .getBoolean(KEY_DESIRED_RUNNING, false)

        internal fun requestRestart(context: android.content.Context): Boolean {
            if (!isDesiredRunning(context)) return false
            return runCatching {
                context.startService(
                    Intent(context, SyncthingCoreService::class.java).setAction(ACTION_RESTART),
                )
            }.isSuccess
        }
    }
}

private fun CoreSnapshot.notificationText(context: android.content.Context): String = when (state) {
    CoreState.RUNNING -> rssBytes?.let {
        context.getString(R.string.core_notification_running_memory, it / 1024 / 1024)
    } ?: context.getString(R.string.core_notification_running)
    CoreState.STARTING -> context.getString(R.string.core_notification_starting)
    CoreState.STOPPING -> context.getString(R.string.core_notification_stopping)
    CoreState.FAILED -> context.getString(R.string.core_notification_failed)
    else -> context.getString(R.string.core_notification_waiting_to_start)
}
