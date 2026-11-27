package dev.aladins.focustube.feature.search

import dev.aladins.focustube.core.common.result.AppError
import dev.aladins.focustube.domain.model.Persona
import dev.aladins.focustube.domain.model.QuotaStatus
import dev.aladins.focustube.domain.model.RecentSearch
import dev.aladins.focustube.domain.model.Region
import dev.aladins.focustube.domain.model.SavedSearch
import dev.aladins.focustube.domain.model.ShortsFilter
import dev.aladins.focustube.domain.model.Video
import dev.aladins.focustube.domain.model.WatchProgress

/** Everything Classic search (12) shows. */
data class SearchUiState(
    val persona: Persona? = null,
    /** The persona's regions (the chip and the region sheet). */
    val regions: List<Region> = emptyList(),
    val defaultRegions: List<Region> = Region.Defaults,
    val filter: ShortsFilter = ShortsFilter.All,
    /** Null until a search is made (then Recent shows instead of results). */
    val submittedQuery: String? = null,
    val saved: SavedSearch? = null,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    /** The last request's error; saved results stay visible alongside it. */
    val error: AppError? = null,
    val quota: QuotaStatus? = null,
    /** Saved searches with the current regions, most recently used first. */
    val recent: List<RecentSearch> = emptyList(),
    /** Ids in the persona's Watch later (filled bookmarks). */
    val savedIds: Set<String> = emptySet(),
    /** Watch progress by video id (thin progress bars on rows). */
    val progress: Map<String, WatchProgress> = emptyMap()
) {
    /** Saved results after the All / Videos / Shorts filter (filtering costs nothing). */
    val visibleVideos: List<Video> get() = saved?.let { filter.apply(it.videos) }.orEmpty()

    /** YouTube's daily limit is reached: show the banner, disable Refresh and More results. */
    val isLimitReached: Boolean get() = quota?.isExhausted == true || error == AppError.QuotaExceeded

    val canRefresh: Boolean get() = saved != null && !isLoading && !isLimitReached

    val canLoadMore: Boolean get() = saved?.hasMore == true && !isLoadingMore && !isLimitReached
}
