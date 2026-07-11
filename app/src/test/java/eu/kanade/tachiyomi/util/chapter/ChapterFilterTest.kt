package eu.kanade.tachiyomi.util.chapter

import eu.kanade.tachiyomi.data.database.models.Chapter
import eu.kanade.tachiyomi.data.database.models.ChapterImpl
import eu.kanade.tachiyomi.data.database.models.MangaImpl
import eu.kanade.tachiyomi.data.download.DownloadManager
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.domain.manga.models.Manga
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ChapterFilterTest {

    private lateinit var preferences: PreferencesHelper
    private lateinit var downloadManager: DownloadManager
    private lateinit var chapterFilter: ChapterFilter
    private val manga = MangaImpl()

    @BeforeEach
    fun setup() {
        preferences = mockk()
        downloadManager = mockk()
        every { downloadManager.isChapterDownloaded(any(), any()) } returns false
        chapterFilter = ChapterFilter(preferences, downloadManager)
    }

    private fun chapter(
        url: String,
        number: Float,
        order: Int,
        read: Boolean = false,
        bookmark: Boolean = false,
    ) = ChapterImpl().apply {
        this.url = url
        this.name = url
        this.chapter_number = number
        this.source_order = order
        this.read = read
        this.bookmark = bookmark
    }

    @Test
    fun `keeps the first listed copy of a duplicated chapter number`() {
        val official = chapter("/official/12", 12f, 0)
        val fan = chapter("/fan/12", 12f, 1)
        val previous = chapter("/fan/11", 11f, 2)

        val result = chapterFilter.filterDuplicates(listOf(official, fan, previous), manga)

        assertEquals(listOf<Chapter>(official, previous), result)
    }

    @Test
    fun `prefers the downloaded copy over the first listed one`() {
        val official = chapter("/official/12", 12f, 0)
        val fan = chapter("/fan/12", 12f, 1)
        every { downloadManager.isChapterDownloaded(fan, manga) } returns true

        val result = chapterFilter.filterDuplicates(listOf(official, fan), manga)

        assertEquals(listOf<Chapter>(fan), result)
    }

    @Test
    fun `prefers the read copy when none is downloaded`() {
        val official = chapter("/official/12", 12f, 0)
        val fan = chapter("/fan/12", 12f, 1, read = true)

        val result = chapterFilter.filterDuplicates(listOf(official, fan), manga)

        assertEquals(listOf<Chapter>(fan), result)
    }

    @Test
    fun `prefers the bookmarked copy when none is downloaded or read`() {
        val official = chapter("/official/12", 12f, 0)
        val fan = chapter("/fan/12", 12f, 1, bookmark = true)

        val result = chapterFilter.filterDuplicates(listOf(official, fan), manga)

        assertEquals(listOf<Chapter>(fan), result)
    }

    @Test
    fun `never removes chapters with an unknown chapter number`() {
        val unknown1 = chapter("/special/1", -1f, 0)
        val unknown2 = chapter("/special/2", -1f, 1)

        val result = chapterFilter.filterDuplicates(listOf(unknown1, unknown2), manga)

        assertEquals(listOf<Chapter>(unknown1, unknown2), result)
    }

    @Test
    fun `keeps list order and unique chapters untouched`() {
        val ch3 = chapter("/fan/3", 3f, 0)
        val ch2a = chapter("/official/2", 2f, 1)
        val ch2b = chapter("/fan/2", 2f, 2)
        val ch1 = chapter("/fan/1", 1f, 3)

        val result = chapterFilter.filterDuplicates(listOf(ch3, ch2a, ch2b, ch1), manga)

        assertEquals(listOf<Chapter>(ch3, ch2a, ch1), result)
    }

    @Test
    fun `duplicates filter flag round trips through chapter flags`() {
        val manga = MangaImpl()
        manga.duplicatesFilter = Manga.CHAPTER_SHOW_NOT_DUPLICATES
        manga.readFilter = Manga.CHAPTER_SHOW_UNREAD

        assertEquals(Manga.CHAPTER_SHOW_NOT_DUPLICATES, manga.duplicatesFilter)
        assertEquals(Manga.CHAPTER_SHOW_UNREAD, manga.readFilter)

        manga.duplicatesFilter = Manga.SHOW_ALL
        assertEquals(Manga.SHOW_ALL, manga.duplicatesFilter)
        assertEquals(Manga.CHAPTER_SHOW_UNREAD, manga.readFilter)
    }
}
