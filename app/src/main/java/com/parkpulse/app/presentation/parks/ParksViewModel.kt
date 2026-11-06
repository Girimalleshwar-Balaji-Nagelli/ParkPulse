package com.parkpulse.app.presentation.parks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parkpulse.app.domain.usecase.ObserveParksUseCase
import com.parkpulse.app.domain.usecase.SeedParksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ParksViewModel @Inject constructor(
    private val observeParks: ObserveParksUseCase,
    private val seedParks: SeedParksUseCase
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(StatusFilter.ALL)
    private val reload = MutableStateFlow(0)

    val uiState: StateFlow<ParksUiState> = reload
        .flatMapLatest {
            flow {
                seedParks()
                emitAll(
                    combine(observeParks(), query, filter) { all, q, f ->
                        val needle = q.trim()
                        ParksUiState(
                            isLoading = false,
                            parks = all.filter {
                                f.matches(it.status) && it.name.contains(needle, ignoreCase = true)
                            },
                            totalCount = all.size,
                            query = q,
                            filter = f
                        )
                    }
                )
            }.catch { e ->
                emit(ParksUiState(isLoading = false, error = e.message ?: "Something went wrong"))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ParksUiState())

    fun onQueryChange(value: String) { query.value = value }
    fun onFilterChange(value: StatusFilter) { filter.value = value }
    fun retry() { reload.value++ }
    fun clearFilters() {
        query.value = ""
        filter.value = StatusFilter.ALL
    }
}