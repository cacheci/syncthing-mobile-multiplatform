package moe.https.syncthing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import moe.https.syncthing.core.RecentChangesController
import moe.https.syncthing.core.localizedUserMessageOrNull
import moe.https.syncthing.generated.resources.*
import moe.https.syncthing.ui.model.RecentChangesUiState
import org.jetbrains.compose.resources.getString

class RecentChangesViewModel(
    private val controller: RecentChangesController,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(RecentChangesUiState())
    val uiState: StateFlow<RecentChangesUiState> = mutableUiState.asStateFlow()
    private val refreshMutex = Mutex()

    fun refresh() = refresh(showLoading = true)

    fun refreshInBackground() = refresh(showLoading = false)

    private fun refresh(showLoading: Boolean) {
        viewModelScope.launch {
            if (showLoading) {
                refreshMutex.lock()
            } else if (!refreshMutex.tryLock()) {
                return@launch
            }
            try {
                if (mutableUiState.value.isLoading) return@launch

                mutableUiState.update {
                    it.copy(isLoading = showLoading || !it.hasLoaded, errorMessage = null)
                }
                try {
                    val changes = controller.loadRecentChanges()
                    mutableUiState.update {
                        it.copy(
                            changes = changes,
                            isLoading = false,
                            hasLoaded = true,
                        )
                    }
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    val errorMessage = error.localizedUserMessageOrNull()
                        ?: error::class.simpleName?.takeIf(String::isNotBlank)
                        ?: getString(Res.string.common_unknown)
                    mutableUiState.update {
                        it.copy(
                            isLoading = false,
                            hasLoaded = true,
                            errorMessage = errorMessage,
                        )
                    }
                }
            } finally {
                refreshMutex.unlock()
            }
        }
    }

    companion object {
        fun factory(
            controller: RecentChangesController,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                RecentChangesViewModel(controller)
            }
        }
    }
}
