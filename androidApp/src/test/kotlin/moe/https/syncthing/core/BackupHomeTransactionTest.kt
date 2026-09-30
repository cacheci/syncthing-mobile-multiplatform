package moe.https.syncthing.core

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class BackupHomeTransactionTest {
    @Test
    fun originalMoveFailureLeavesOriginalUntouched() = withFixture { fixture ->
        fixture.failedMoves += fixture.home to fixture.previous

        val error = assertFailsWith<IOException> { fixture.apply() }

        fixture.assertOriginal(fixture.home)
        assertFalse(fixture.previous.exists())
        assertTrue(fixture.staged.exists())
        assertTrue(fixture.deletedDirectories.isEmpty())
        assertEquals(0, fixture.applyCalls)
        assertEquals(0, fixture.rollbackCalls)
        assertTrue(error.message.orEmpty().contains("原配置目录未被替换"))
    }

    @Test
    fun installationFailureRestoresOriginalWithoutChangingSettings() = withFixture { fixture ->
        fixture.failedMoves += fixture.staged to fixture.home

        assertFailsWith<IOException> { fixture.apply() }

        fixture.assertOriginal(fixture.home)
        assertFalse(fixture.previous.exists())
        assertTrue(fixture.staged.exists())
        assertTrue(fixture.deletedDirectories.isEmpty())
        assertEquals(0, fixture.applyCalls)
        assertEquals(0, fixture.rollbackCalls)
    }

    @Test
    fun settingsFailureRestoresDirectoryAndSettings() = withFixture { fixture ->
        val failure = IOException("settings write failed")
        val error = assertFailsWith<IOException> {
            fixture.apply(applyChanges = {
                fixture.settings = "imported"
                throw failure
            })
        }

        fixture.assertOriginal(fixture.home)
        assertFalse(fixture.previous.exists())
        assertEquals(listOf(fixture.home), fixture.deletedDirectories)
        assertEquals("original", fixture.settings)
        assertEquals(1, fixture.rollbackCalls)
        assertSame(failure, error.cause)
        assertTrue(error.message.orEmpty().contains("已恢复原配置"))
    }

    @Test
    fun cleanupFailureKeepsOriginalBackup() = withFixture { fixture ->
        fixture.failDeletion = true

        val error = assertFailsWith<IOException> {
            fixture.apply(applyChanges = { throw IOException("reconcile failed") })
        }

        fixture.assertOriginal(fixture.previous)
        assertEquals("imported", File(fixture.home, "config.xml").readText())
        assertEquals(0, fixture.rollbackCalls)
        assertEquals(1, error.suppressed.size)
        assertTrue(error.message.orEmpty().contains("原配置保留在 ${fixture.previous.path}"))
    }

    @Test
    fun cleanupReportingSuccessWithDirectoryRemainingDoesNotMoveOriginal() = withFixture { fixture ->
        val transaction = BackupHomeTransaction(
            fixture.home,
            fixture.staged,
            fixture.previous,
            moveDirectory = fixture::move,
            deleteDirectory = { true },
        )

        val error = assertFailsWith<IOException> {
            transaction.apply(
                applyChanges = { throw IOException("reconcile failed") },
                rollbackChanges = { fixture.rollbackCalls++ },
            )
        }

        fixture.assertOriginal(fixture.previous)
        assertEquals("imported", File(fixture.home, "config.xml").readText())
        assertEquals(0, fixture.rollbackCalls)
        assertEquals(1, error.suppressed.size)
    }

    @Test
    fun restorationMoveFailureKeepsOriginalBackup() = withFixture { fixture ->
        fixture.failedMoves += fixture.staged to fixture.home
        fixture.failedMoves += fixture.previous to fixture.home

        val error = assertFailsWith<IOException> { fixture.apply() }

        fixture.assertOriginal(fixture.previous)
        assertFalse(fixture.home.exists())
        assertTrue(fixture.staged.exists())
        assertTrue(fixture.deletedDirectories.isEmpty())
        assertEquals(1, error.suppressed.size)
        assertTrue(error.message.orEmpty().contains("原配置保留在 ${fixture.previous.path}"))
    }

    @Test
    fun settingsRollbackFailureReportsRestoredHomeLocation() = withFixture { fixture ->
        val rollbackFailure = IOException("settings rollback failed")
        val error = assertFailsWith<IOException> {
            fixture.apply(
                applyChanges = { throw IOException("reconcile failed") },
                rollbackChanges = { throw rollbackFailure },
            )
        }

        fixture.assertOriginal(fixture.home)
        assertFalse(fixture.previous.exists())
        assertSame(rollbackFailure, error.suppressed.single())
        assertTrue(error.message.orEmpty().contains("原配置目录已恢复到 ${fixture.home.path}"))
        assertFalse(error.message.orEmpty().contains("原配置保留在 ${fixture.previous.path}"))
    }

    @Test
    fun failedImportWithoutOriginalRemovesOnlyInstalledHome() = withFixture { fixture ->
        fixture.home.deleteRecursively()

        val error = assertFailsWith<IOException> {
            fixture.apply(applyChanges = { throw IOException("reconcile failed") })
        }

        assertFalse(fixture.home.exists())
        assertFalse(fixture.previous.exists())
        assertEquals(listOf(fixture.home), fixture.deletedDirectories)
        assertEquals(1, fixture.rollbackCalls)
        assertTrue(error.message.orEmpty().contains("导入前没有原配置目录"))
    }

    @Test
    fun failedInstallationWithoutOriginalDoesNotDeleteAnything() = withFixture { fixture ->
        fixture.home.deleteRecursively()
        fixture.failedMoves += fixture.staged to fixture.home

        assertFailsWith<IOException> { fixture.apply() }

        assertFalse(fixture.home.exists())
        assertTrue(fixture.staged.exists())
        assertTrue(fixture.deletedDirectories.isEmpty())
        assertEquals(0, fixture.rollbackCalls)
    }

    @Test
    fun successfulImportKeepsNewHomeAndRemovesPreviousHome() = withFixture { fixture ->
        fixture.apply(applyChanges = { fixture.settings = "imported" })

        assertEquals("imported", File(fixture.home, "config.xml").readText())
        assertFalse(fixture.previous.exists())
        assertFalse(fixture.staged.exists())
        assertEquals("imported", fixture.settings)
        assertEquals(1, fixture.applyCalls)
        assertEquals(0, fixture.rollbackCalls)
    }

    private fun withFixture(block: (Fixture) -> Unit) {
        val root = Files.createTempDirectory("backup-home-test-").toFile()
        try {
            block(Fixture(root))
        } finally {
            root.deleteRecursively()
        }
    }

    private class Fixture(root: File) {
        val home = File(root, "home").apply { mkdirs() }
        val staged = File(root, "staged").apply { mkdirs() }
        val previous = File(root, "previous")
        val failedMoves = mutableSetOf<Pair<File, File>>()
        val deletedDirectories = mutableListOf<File>()
        var failDeletion = false
        var applyCalls = 0
        var rollbackCalls = 0
        var settings = "original"

        init {
            File(home, "config.xml").writeText("original")
            File(home, "key.pem").writeText("original-key")
            File(staged, "config.xml").writeText("imported")
        }

        fun move(source: File, target: File) {
            if (source to target in failedMoves) throw IOException("injected atomic move failure")
            Files.move(source.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE)
        }

        fun apply(
            applyChanges: () -> Unit = {},
            rollbackChanges: () -> Unit = { settings = "original" },
        ) {
            BackupHomeTransaction(
                home,
                staged,
                previous,
                moveDirectory = ::move,
                deleteDirectory = { directory ->
                    deletedDirectories += directory
                    if (failDeletion) false else directory.deleteRecursively()
                },
            ).apply(
                applyChanges = {
                    applyCalls++
                    applyChanges()
                },
                rollbackChanges = {
                    rollbackCalls++
                    rollbackChanges()
                },
            )
        }

        fun assertOriginal(directory: File) {
            assertEquals("original", File(directory, "config.xml").readText())
            assertEquals("original-key", File(directory, "key.pem").readText())
        }
    }
}
