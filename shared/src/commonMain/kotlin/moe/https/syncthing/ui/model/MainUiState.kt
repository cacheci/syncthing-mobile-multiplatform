package moe.https.syncthing.ui.model

data class MainUiState(
    val bottomBarPages: Set<AppPage> = DEFAULT_BOTTOM_BAR_PAGES,
    val defaultBottomBarPage: AppPage = AppPage.CORE,
    val floatingBottomBar: Boolean = false,
    val topBarBlurEnabled: Boolean = true,
    val bottomBarBlurEnabled: Boolean = true,
    val highContrastMode: Boolean = false,
    val maxBottomBarPages: Int = DEFAULT_BOTTOM_BAR_CAPACITY,
) {
    val canSelectMoreBottomBarPages: Boolean
        get() = bottomBarPages.size < maxBottomBarPages

    companion object {
        const val DEFAULT_BOTTOM_BAR_CAPACITY = 5

        val BOTTOM_BAR_PAGE_PRIORITY: List<AppPage> = listOf(
            AppPage.CORE,
            AppPage.FOLDERS,
            AppPage.DEVICES,
            AppPage.RECENT_CHANGES,
            AppPage.SETTINGS,
            AppPage.WEBUI,
        )

        val DEFAULT_BOTTOM_BAR_PAGES: Set<AppPage> =
            BOTTOM_BAR_PAGE_PRIORITY.take(DEFAULT_BOTTOM_BAR_CAPACITY).toSet()

        fun calculateBottomBarCapacity(windowWidthDp: Int): Int =
            if (windowWidthDp >0)
            (windowWidthDp / BOTTOM_BAR_ITEM_MIN_WIDTH_DP).coerceIn(
                minimumValue = MIN_BOTTOM_BAR_CAPACITY,
                maximumValue = AppPage.entries.size,
            ) else DEFAULT_BOTTOM_BAR_CAPACITY

        fun visibleBottomBarPages(
            selectedPages: Set<AppPage>,
            capacity: Int,
        ): Set<AppPage> {
            val prioritizedPages = BOTTOM_BAR_PAGE_PRIORITY
                .filter(selectedPages::contains)
                .take(capacity.coerceIn(MIN_BOTTOM_BAR_CAPACITY, AppPage.entries.size))
                .toSet()
            return AppPage.entries
                .filter(prioritizedPages::contains)
                .toSet()
        }

        private const val BOTTOM_BAR_ITEM_MIN_WIDTH_DP = 72
        const val MIN_BOTTOM_BAR_CAPACITY = 3
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
