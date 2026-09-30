package moe.https.syncthing.viewmodel

import kotlin.test.Test
import kotlin.test.assertEquals
import moe.https.syncthing.storage.AppSettingPrivateStorage
import moe.https.syncthing.ui.model.AppPage
import moe.https.syncthing.ui.model.MainUiState

class MainViewModelTest {
    @Test
    fun missingConfigurationLoadsDefaultPages() {
        val state = MainViewModel(InMemoryAppSettingsStorage()).uiState.value

        assertEquals(MainUiState.DEFAULT_BOTTOM_BAR_PAGES.toList(), state.bottomBarPages.toList())
        assertEquals(AppPage.CORE, state.defaultBottomBarPage)
    }

    @Test
    fun savedPagesKeepTheirOrderAndSelectedDefault() {
        val storage = configuredStorage(
            pageNames = setOf(AppPage.SETTINGS.name, AppPage.DEVICES.name, AppPage.CORE.name),
            defaultPageName = AppPage.SETTINGS.name,
        )

        val state = MainViewModel(storage).uiState.value

        assertEquals(listOf(AppPage.DEVICES, AppPage.CORE, AppPage.SETTINGS), state.bottomBarPages.toList())
        assertEquals(AppPage.SETTINGS, state.defaultBottomBarPage)
    }

    @Test
    fun emptyConfigurationFallsBackToCore() {
        val state = MainViewModel(configuredStorage(emptySet(), AppPage.SETTINGS.name)).uiState.value

        assertEquals(setOf(AppPage.CORE), state.bottomBarPages)
        assertEquals(AppPage.CORE, state.defaultBottomBarPage)
    }

    @Test
    fun unknownSavedPagesFallBackToCore() {
        val state = MainViewModel(configuredStorage(setOf("REMOVED_PAGE"), "REMOVED_PAGE")).uiState.value

        assertEquals(setOf(AppPage.CORE), state.bottomBarPages)
        assertEquals(AppPage.CORE, state.defaultBottomBarPage)
    }

    @Test
    fun unavailableDefaultFallsBackToFirstSelectedPageWhenCoreIsAbsent() {
        val storage = configuredStorage(
            pageNames = setOf(AppPage.SETTINGS.name, AppPage.WEBUI.name, "REMOVED_PAGE"),
            defaultPageName = AppPage.CORE.name,
        )

        val state = MainViewModel(storage).uiState.value

        assertEquals(listOf(AppPage.WEBUI, AppPage.SETTINGS), state.bottomBarPages.toList())
        assertEquals(AppPage.WEBUI, state.defaultBottomBarPage)
    }

    @Test
    fun savedPagesRespectThePageLimitAndFallBackToCoreForATrimmedDefault() {
        val storage = configuredStorage(
            pageNames = AppPage.entries.mapTo(mutableSetOf(), AppPage::name),
            defaultPageName = AppPage.SETTINGS.name,
        )

        val state = MainViewModel(storage).uiState.value

        assertEquals(
            listOf(AppPage.DEVICES, AppPage.FOLDERS, AppPage.CORE, AppPage.WEBUI, AppPage.RECENT_CHANGES),
            state.bottomBarPages.toList(),
        )
        assertEquals(AppPage.CORE, state.defaultBottomBarPage)
    }

    @Test
    fun togglingAPagePreservesOtherPagesAndPersistsTheResult() {
        val storage = configuredStorage(
            pageNames = setOf(AppPage.CORE.name, AppPage.SETTINGS.name),
            defaultPageName = AppPage.SETTINGS.name,
        )
        val viewModel = MainViewModel(storage)

        viewModel.onBottomBarPageToggled(AppPage.DEVICES)

        assertEquals(
            listOf(AppPage.DEVICES, AppPage.CORE, AppPage.SETTINGS),
            viewModel.uiState.value.bottomBarPages.toList(),
        )
        assertEquals(AppPage.SETTINGS, viewModel.uiState.value.defaultBottomBarPage)
        assertEquals(
            setOf(AppPage.DEVICES.name, AppPage.CORE.name, AppPage.SETTINGS.name),
            storage.getStringSet(AppSettingPrivateStorage.KEY_BOTTOM_BAR_PAGES),
        )
        assertEquals(AppPage.SETTINGS.name, storage.getString(AppSettingPrivateStorage.KEY_BOTTOM_BAR_DEFAULT_PAGE))
    }

    private fun configuredStorage(
        pageNames: Set<String>,
        defaultPageName: String,
    ): InMemoryAppSettingsStorage = InMemoryAppSettingsStorage().apply {
        putStringSet(AppSettingPrivateStorage.KEY_BOTTOM_BAR_PAGES, pageNames)
        putString(AppSettingPrivateStorage.KEY_BOTTOM_BAR_DEFAULT_PAGE, defaultPageName)
    }

    private class InMemoryAppSettingsStorage : AppSettingPrivateStorage {
        private val values = mutableMapOf<String, Any>()

        override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
            values[key] as? Boolean ?: defaultValue

        override fun putBoolean(key: String, value: Boolean) {
            values[key] = value
        }

        override fun getInt(key: String, defaultValue: Int): Int = values[key] as? Int ?: defaultValue

        override fun putInt(key: String, value: Int) {
            values[key] = value
        }

        override fun getLong(key: String, defaultValue: Long): Long = values[key] as? Long ?: defaultValue

        override fun putLong(key: String, value: Long) {
            values[key] = value
        }

        override fun getFloat(key: String, defaultValue: Float): Float = values[key] as? Float ?: defaultValue

        override fun putFloat(key: String, value: Float) {
            values[key] = value
        }

        override fun getString(key: String, defaultValue: String?): String? =
            values[key] as? String ?: defaultValue

        override fun putString(key: String, value: String?) {
            if (value == null) values.remove(key) else values[key] = value
        }

        @Suppress("UNCHECKED_CAST")
        override fun getStringSet(key: String, defaultValue: Set<String>?): Set<String>? =
            values[key] as? Set<String> ?: defaultValue

        override fun putStringSet(key: String, value: Set<String>?) {
            if (value == null) values.remove(key) else values[key] = value.toSet()
        }

        override fun contains(key: String): Boolean = key in values

        override fun remove(key: String) {
            values.remove(key)
        }
    }
}
