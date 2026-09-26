package moe.https.syncthing.ui.model

data class MainUiState(
    val bottomBarPages: Set<AppPage> = DEFAULT_BOTTOM_BAR_PAGES,
    val defaultBottomBarPage: AppPage = AppPage.CORE,
    val floatingBottomBar: Boolean = false,
    val topBarBlurEnabled: Boolean = true,
    val bottomBarBlurEnabled: Boolean = true,
    val highContrastMode: Boolean = false,
) {
    val canSelectMoreBottomBarPages: Boolean
        get() = bottomBarPages.size < MAX_BOTTOM_BAR_PAGES

    companion object {
        const val MAX_BOTTOM_BAR_PAGES = 5

        val DEFAULT_BOTTOM_BAR_PAGES: Set<AppPage> = setOf(
            AppPage.DEVICES,
            AppPage.FOLDERS,
            AppPage.CORE,
            AppPage.RECENT_CHANGES,
            AppPage.SETTINGS,
        )
    }
}

enum class AppPage {
    DEVICES,
    FOLDERS,
    CORE,
    WEBUI,
    RECENT_CHANGES,
    SETTINGS,
}
