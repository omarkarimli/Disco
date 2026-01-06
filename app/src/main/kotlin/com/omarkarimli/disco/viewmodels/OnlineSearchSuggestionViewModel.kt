package com.omarkarimli.disco.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omarkarimli.innertube.YouTube
import com.omarkarimli.innertube.models.YTItem
import com.omarkarimli.innertube.models.filterExplicit
import com.omarkarimli.disco.constants.HideExplicitKey
import com.omarkarimli.disco.db.MusicDatabase
import com.omarkarimli.disco.db.entities.SearchHistory
import com.omarkarimli.disco.utils.dataStore
import com.omarkarimli.disco.utils.get
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class OnlineSearchSuggestionViewModel
@Inject
constructor(
    @ApplicationContext val context: Context,
    database: MusicDatabase,
) : ViewModel() {
    val query = MutableStateFlow("")
    private val _viewState = MutableStateFlow(SearchSuggestionViewState())
    val viewState = _viewState.asStateFlow()

    init {
        viewModelScope.launch {
            query
                .flatMapLatest { query ->
                    if (query.isEmpty()) {
                        database.searchHistory().map { history ->
                            SearchSuggestionViewState(
                                history = history,
                            )
                        }
                    } else {
                        val result = YouTube.searchSuggestions(query).getOrNull()
                        val hideExplicit = context.dataStore.get(HideExplicitKey, false)

                        database
                            .searchHistory(query)
                            .map { it.take(3) }
                            .map { history ->
                                SearchSuggestionViewState(
                                    history = history,
                                    suggestions =
                                    result
                                        ?.queries
                                        ?.filter { suggestionQuery ->
                                            history.none { it.query == suggestionQuery }
                                        }.orEmpty(),
                                    items =
                                    result
                                        ?.recommendedItems
                                        ?.distinctBy { it.id }
                                        ?.filterExplicit(hideExplicit)
                                        .orEmpty(),
                                )
                            }
                    }
                }.collect {
                    _viewState.value = it
                }
        }
    }
}

data class SearchSuggestionViewState(
    val history: List<SearchHistory> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val items: List<YTItem> = emptyList(),
)
