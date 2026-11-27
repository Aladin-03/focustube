package dev.aladins.focustube.data.local

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.aladins.focustube.data.local.database.FocusTubeDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Data must survive every database upgrade. Each new version adds a case here: create the old
 * version from its committed schema JSON, insert rows, open with the current code, check the rows.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val dbName = "migration-test.db"

    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), FocusTubeDatabase::class.java)

    @Test
    fun version1SchemaFileMatchesCode_andKeepsPersonas() = runTest {
        helper.createDatabase(dbName, 1).apply {
            execSQL(
                "INSERT INTO persona (id, name, goal, regions, created_at, archived_at) VALUES " +
                    "(1, 'Guitar', 'Learn', 'US,GB', 100, NULL), (2, 'Cooking', NULL, 'NL', 200, 500)"
            )
            close()
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.databaseBuilder(context, FocusTubeDatabase::class.java, dbName).build()
        try {
            val dao = database.personaDao()
            assertEquals(listOf("Guitar"), dao.observeActive().first().map { it.name })
            assertEquals(listOf("Cooking"), dao.observeArchived().first().map { it.name })
        } finally {
            database.close()
            context.deleteDatabase(dbName)
        }
    }

    @Test
    fun version1To2KeepsPersonas_andAddsTheSearchTables() = runTest {
        helper.createDatabase(dbName, 1).apply {
            execSQL(
                "INSERT INTO persona (id, name, goal, regions, created_at, archived_at) VALUES (1, 'Guitar', 'Learn', 'US,GB', 100, NULL)"
            )
            close()
        }
        helper.runMigrationsAndValidate(dbName, 2, true).apply {
            query(
                "SELECT name FROM sqlite_master WHERE type = 'table' AND name IN ('video', 'search_cache', 'search_result', 'quota_log')"
            ).use {
                assertEquals(4, it.count)
            }
            close()
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.databaseBuilder(context, FocusTubeDatabase::class.java, dbName).build()
        try {
            val guitar = database.personaDao().observeActive().first().single()
            assertEquals("Guitar", guitar.name)
            assertEquals("Learn", guitar.goal)
            assertEquals("US,GB", guitar.regions)
            assertEquals(0L, database.quotaDao().unitsBlocking("2026-11-27"))
        } finally {
            database.close()
            context.deleteDatabase(dbName)
        }
    }

    @Test
    fun version2To3KeepsEverything_andAddsTheLibraryTables() = runTest {
        helper.createDatabase(dbName, 2).apply {
            execSQL(
                "INSERT INTO persona (id, name, goal, regions, created_at, archived_at) VALUES (1, 'Guitar', 'Learn', 'US', 100, NULL)"
            )
            execSQL(
                "INSERT INTO video (youtube_id, title, channel_id, channel_title, duration_sec, published_at, " +
                    "view_count, is_short, description, thumbnail_url, fetched_at) " +
                    "VALUES ('v1', 'Video', 'c1', 'Fred', 664, 0, 10, 0, '', NULL, 5)"
            )
            execSQL(
                "INSERT INTO search_cache (id, persona_id, query, query_normalized, mode, regions, region_order, " +
                    "pages_loaded, page_tokens, created_at, last_used_at) " +
                    "VALUES (1, 1, 'guitar', 'guitar', 'CLASSIC', 'US', 'US', 1, '{}', 5, 5)"
            )
            execSQL(
                "INSERT INTO search_result (cache_id, video_id, rank, reason, level) VALUES (1, 'v1', 0, NULL, NULL)"
            )
            execSQL("INSERT INTO quota_log (pacific_date, units_used) VALUES ('2026-11-27', 505)")
            close()
        }
        helper.runMigrationsAndValidate(dbName, 3, true).apply {
            query("SELECT COUNT(*) FROM watch_later").use {
                it.moveToFirst()
                assertEquals(0, it.getInt(0))
            }
            query("SELECT COUNT(*) FROM history").use {
                it.moveToFirst()
                assertEquals(0, it.getInt(0))
            }
            close()
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.databaseBuilder(context, FocusTubeDatabase::class.java, dbName).build()
        try {
            assertEquals("Guitar", database.personaDao().observeActive().first().single().name)
            assertEquals("Video", database.videoDao().get("v1")?.title)
            assertEquals(listOf("v1"), database.searchDao().resultVideoIds(1))
            assertEquals(505L, database.quotaDao().unitsBlocking("2026-11-27"))
        } finally {
            database.close()
            context.deleteDatabase(dbName)
        }
    }
}
