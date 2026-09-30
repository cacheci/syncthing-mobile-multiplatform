import com.android.build.api.variant.FilterConfiguration
import com.android.build.api.variant.impl.VariantOutputImpl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

/** 提交总数，用作 versionCode（等价于 `git rev-list --count HEAD`）。 */
fun Project.getGitVersionCode(): Int =
    providers.exec {
        commandLine("git", "rev-list", "--count", "HEAD")
    }.standardOutput.asText.get().trim().toInt()

abstract class BuildBuiltInSyncthingTask : Exec() {
    @get:OutputDirectory
    abstract val jniLibsDirectory: DirectoryProperty
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.aboutLibraries)
}

aboutLibraries {
    collect {
        configPath = file("aboutlibraries")
    }
    export {
        outputFile = file("../shared/src/commonMain/composeResources/files/aboutlibraries.json")
        variant = "release"
        prettyPrint = true
    }
    license {
        additionalLicenses.addAll("Apache-2.0", "MIT", "MPL-2.0")
    }
    library {
        mergePlatformArtifacts = true
    }
}

val syncthingVersion = libs.versions.syncthing.version.get()
val syncthingCommit = libs.versions.syncthing.commit.get()
val syncthingPreviousVersion = libs.versions.syncthing.previous.version.get()
val syncthingPreviousCommit = libs.versions.syncthing.previous.commit.get()
val syncthingRcVersion = libs.versions.syncthing.rc.version.get()
val syncthingRcCommit = libs.versions.syncthing.rc.commit.get()
val androidAbis = listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
val generatedSyncthingJniLibs = layout.buildDirectory.dir("generated/syncthing/jniLibs")
val generatedSyncthingPreviousJniLibs = layout.buildDirectory.dir("generated/syncthing/previous/jniLibs")
val generatedSyncthingPreviousSource = layout.buildDirectory.dir("generated/syncthing/previous/source")
val generatedSyncthingRcJniLibs = layout.buildDirectory.dir("generated/syncthing/rc/jniLibs")
val generatedSyncthingRcSource = layout.buildDirectory.dir("generated/syncthing/rc/source")
val buildSyncthingScript = layout.projectDirectory.file("build-syncthing.py")
val localProperties = Properties().apply {
    runCatching {
        rootProject.file("local.properties").reader(Charsets.UTF_8).use(::load)
    }
}
val keystorePath: String? = localProperties.getProperty("KEYSTORE_PATH") ?: System.getenv("KEYSTORE_PATH")
val keystorePassword: String? = localProperties.getProperty("KEYSTORE_PASS") ?: System.getenv("KEYSTORE_PASS")
val keyAliasName: String? = localProperties.getProperty("KEY_ALIAS") ?: System.getenv("KEY_ALIAS")
val keyPasswordValue: String? = localProperties.getProperty("KEY_PASSWORD") ?: System.getenv("KEY_PASSWORD")

android {
    namespace = "moe.https.syncthing"
    compileSdk {
        version = release(37) {
            minorApiLevel = 2
        }
    }
    ndkVersion = libs.versions.ndk.get()

    defaultConfig {
        applicationId = "moe.https.syncthing"
        minSdk = 25
        targetSdk = 28
        versionCode = getGitVersionCode()
        versionName = "0.1.0"
        buildConfigField("String", "SYNCTHING_VERSION", "\"$syncthingVersion\"")
        buildConfigField("String", "SYNCTHING_COMMIT", "\"$syncthingCommit\"")
        buildConfigField("boolean", "HAS_SYNCTHING_PREVIOUS_STABLE", "false")
        buildConfigField("String", "SYNCTHING_PREVIOUS_VERSION", "\"\"")
        buildConfigField("String", "SYNCTHING_PREVIOUS_COMMIT", "\"\"")
        buildConfigField("boolean", "HAS_SYNCTHING_RC", "false")
        buildConfigField("String", "SYNCTHING_RC_VERSION", "\"\"")
        buildConfigField("String", "SYNCTHING_RC_COMMIT", "\"\"")

        ndk {
            //noinspection ChromeOsAbiSupport
            abiFilters += androidAbis
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include(*androidAbis.toTypedArray())
            isUniversalApk = true
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
        jniLibs.useLegacyPackaging = true
    }

    lint {
        disable += "ExpiredTargetSdkVersion"
    }

    if (keystorePath != null) {
        signingConfigs {
            create("shared") {
                storeFile = file(keystorePath)
                storePassword = keystorePassword
                keyAlias = keyAliasName
                keyPassword = keyPasswordValue
                enableV3Signing = true
                enableV4Signing = true
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )

            signingConfig = signingConfigs.getByName(if (keystorePath != null) "shared" else "debug")
        }
        getByName("debug") {
            if (keystorePath != null) {
                signingConfig = signingConfigs.getByName("shared")
            }
        }
        create("releaseStable") {
            initWith(getByName("release"))
            matchingFallbacks += "release"
            buildConfigField("boolean", "HAS_SYNCTHING_PREVIOUS_STABLE", "true")
            buildConfigField("String", "SYNCTHING_PREVIOUS_VERSION", "\"$syncthingPreviousVersion\"")
            buildConfigField("String", "SYNCTHING_PREVIOUS_COMMIT", "\"$syncthingPreviousCommit\"")
        }
        create("releaseBeta") {
            initWith(getByName("release"))
            matchingFallbacks += "release"
            buildConfigField("boolean", "HAS_SYNCTHING_RC", "true")
            buildConfigField("String", "SYNCTHING_RC_VERSION", "\"$syncthingRcVersion\"")
            buildConfigField("String", "SYNCTHING_RC_COMMIT", "\"$syncthingRcCommit\"")
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(project(":shared"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ui)
    implementation(libs.bcrypt)
    implementation(libs.quickie.bundled)
    implementation(libs.zip4j)
    debugImplementation(libs.ui.tooling)
    testImplementation(kotlin("test-junit", libs.versions.kotlin.get()))
}

val syncthingSource = rootProject.layout.projectDirectory.dir("third_party/syncthing")
val configuredPython = providers.gradleProperty("syncthing.pythonExecutable")
    .orElse(providers.environmentVariable("PYTHON"))
    .orNull
val inheritedPath = providers.environmentVariable("PATH").orNull.orEmpty()
val pythonExecutable = sequenceOf(configuredPython)
    .filterNotNull()
    .plus(
        inheritedPath.split(File.pathSeparatorChar)
            .asSequence()
            .filter(String::isNotBlank)
            .flatMap { directory ->
                sequenceOf("python3", "python", "python.exe")
                    .map { executable -> File(directory, executable) }
            }
            .filter(File::isFile)
            .map(File::getAbsolutePath),
    )
    .plus(
        sequenceOf(
            "/usr/bin/python3",
            "/opt/homebrew/bin/python3",
            "/usr/local/bin/python3",
        ).filter { candidate -> File(candidate).isFile },
    )
    .firstOrNull()
    ?: "python3"

val buildBuiltInSyncthing = tasks.register<BuildBuiltInSyncthingTask>("buildBuiltInSyncthing") {
    group = "build"
    description = "Compile Syncthing Core"
    jniLibsDirectory.set(generatedSyncthingJniLibs)
    inputs.file(buildSyncthingScript)
    inputs.dir(syncthingSource)
    inputs.property("syncthingVersion", syncthingVersion)
    inputs.property("syncthingCommit", syncthingCommit)
    inputs.property("ndkVersion", libs.versions.ndk)
    inputs.property("goVersion", libs.versions.go)
    inputs.property("minSdk", 25)
    inputs.property("androidAbis", androidAbis)
    workingDir(rootProject.layout.projectDirectory)
    environment("PYTHONDONTWRITEBYTECODE", "1")
    environment("PYTHONUNBUFFERED", "1")
    commandLine(
        pythonExecutable,
        buildSyncthingScript.asFile.absolutePath,
        "--project-dir", rootProject.layout.projectDirectory.asFile.absolutePath,
        "--source-dir", syncthingSource.asFile.absolutePath,
        "--output-dir", jniLibsDirectory.get().asFile.absolutePath,
    )
}

val buildBuiltInSyncthingPreviousStable = tasks.register<BuildBuiltInSyncthingTask>("buildBuiltInSyncthingPreviousStable") {
    group = "build"
    description = "Compile the previous bundled Syncthing stable Core"
    // Share the Go installation prepared by the current stable build.
    dependsOn(buildBuiltInSyncthing)
    jniLibsDirectory.set(generatedSyncthingPreviousJniLibs)
    inputs.file(buildSyncthingScript)
    inputs.property("syncthingVersion", syncthingPreviousVersion)
    inputs.property("syncthingCommit", syncthingPreviousCommit)
    inputs.property("ndkVersion", libs.versions.ndk)
    inputs.property("goVersion", libs.versions.go)
    inputs.property("minSdk", 25)
    inputs.property("androidAbis", androidAbis)
    workingDir(rootProject.layout.projectDirectory)
    environment("PYTHONDONTWRITEBYTECODE", "1")
    environment("PYTHONUNBUFFERED", "1")
    commandLine(
        pythonExecutable,
        buildSyncthingScript.asFile.absolutePath,
        "--project-dir", rootProject.layout.projectDirectory.asFile.absolutePath,
        "--source-dir", generatedSyncthingPreviousSource.get().asFile.absolutePath,
        "--output-dir", jniLibsDirectory.get().asFile.absolutePath,
        "--channel", "previous-stable",
    )
}

val buildBuiltInSyncthingRc = tasks.register<BuildBuiltInSyncthingTask>("buildBuiltInSyncthingRc") {
    group = "build"
    description = "Compile the bundled Syncthing RC Core"
    // Both builds share the Go installation; prepare it once before the RC build.
    dependsOn(buildBuiltInSyncthing)
    jniLibsDirectory.set(generatedSyncthingRcJniLibs)
    inputs.file(buildSyncthingScript)
    inputs.property("syncthingVersion", syncthingRcVersion)
    inputs.property("syncthingCommit", syncthingRcCommit)
    inputs.property("ndkVersion", libs.versions.ndk)
    inputs.property("goVersion", libs.versions.go)
    inputs.property("minSdk", 25)
    inputs.property("androidAbis", androidAbis)
    workingDir(rootProject.layout.projectDirectory)
    environment("PYTHONDONTWRITEBYTECODE", "1")
    environment("PYTHONUNBUFFERED", "1")
    commandLine(
        pythonExecutable,
        buildSyncthingScript.asFile.absolutePath,
        "--project-dir", rootProject.layout.projectDirectory.asFile.absolutePath,
        "--source-dir", generatedSyncthingRcSource.get().asFile.absolutePath,
        "--output-dir", jniLibsDirectory.get().asFile.absolutePath,
        "--channel", "rc",
    )
}

androidComponents {
    onVariants { variant ->
        variant.sources.jniLibs?.addGeneratedSourceDirectory(
            buildBuiltInSyncthing,
            BuildBuiltInSyncthingTask::jniLibsDirectory,
        )
        if (variant.buildType == "releaseStable") {
            variant.sources.jniLibs?.addGeneratedSourceDirectory(
                buildBuiltInSyncthingPreviousStable,
                BuildBuiltInSyncthingTask::jniLibsDirectory,
            )
        }
        if (variant.buildType == "releaseBeta") {
            variant.sources.jniLibs?.addGeneratedSourceDirectory(
                buildBuiltInSyncthingRc,
                BuildBuiltInSyncthingTask::jniLibsDirectory,
            )
        }
        if (variant.buildType in setOf("release", "releaseStable", "releaseBeta")) {
            val channelSuffix = when (variant.buildType) {
                "releaseStable" -> "_stable"
                "releaseBeta" -> "_beta"
                else -> ""
            }
            variant.outputs.forEach { output ->
                val abi = output.filters
                    .firstOrNull { it.filterType == FilterConfiguration.FilterType.ABI }
                    ?.identifier
                    ?: "universal"
                (output as? VariantOutputImpl)?.outputFileName?.set(
                    output.versionName.zip(output.versionCode) { versionName, versionCode ->
                        "syncthing-neo_android-${abi}_${versionName}_${versionCode}${channelSuffix}.apk"
                    },
                )
            }
        }
    }
}
