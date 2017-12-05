/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.uamp.playback

import android.content.res.Resources
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.test.mock.MockResources
import com.example.android.uamp.TestSetupHelper
import com.example.android.uamp.model.MusicProvider
import com.example.android.uamp.utils.MediaIDHelper
import com.example.android.uamp.utils.QueueHelper
import com.example.android.uamp.utils.SimpleMusicProviderSource
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Android instrumentation unit tests for [QueueManager] and related classes.
 */

class QueueManagerTest {

    private var provider: MusicProvider? = null

    @Before
    @Throws(Exception::class)
    fun setupMusicProvider() {
        val source = SimpleMusicProviderSource()
        populateMusicSource(source)
        provider = TestSetupHelper.setupMusicProvider(source)
    }

    private fun populateMusicSource(source: SimpleMusicProviderSource) {
        source.add("Music 1", "Album 1", "Smith Singer", "Genre 1",
                "https://examplemusic.com/music1.mp3", null!!, 1, 3, 3200)
        source.add("Music 2", "Album 1", "Joe Singer", "Genre 1",
                "https://examplemusic.com/music2.mp3", null!!, 2, 3, 3300)
        source.add("Music 3", "Album 1", "John Singer", "Genre 1",
                "https://examplemusic.com/music3.mp3", null!!, 3, 3, 3400)
        source.add("Romantic Song 1", "Album 2", "Joe Singer", "Genre 2",
                "https://examplemusic.com/music4.mp3", null!!, 1, 2, 4200)
        source.add("Romantic Song 2", "Album 2", "Joe Singer", "Genre 2",
                "https://examplemusic.com/music5.mp3", null!!, 2, 2, 4200)
    }

    private fun createQueueManagerWithValidation(latch: CountDownLatch?,
                                                 expectedQueueIndex: Int, expectedNewQueue: List<MediaSessionCompat.QueueItem>?): QueueManager {
        val resources = object : MockResources() {
            @Throws(Resources.NotFoundException::class)
            override fun getString(id: Int): String {
                return ""
            }

            @Throws(Resources.NotFoundException::class)
            override fun getString(id: Int, vararg formatArgs: Any): String {
                return ""
            }
        }
        return QueueManager(provider!!, resources,
                object : QueueManager.MetadataUpdateListener {
                    override fun onMetadataChanged(metadata: MediaMetadataCompat) {}

                    override fun onMetadataRetrieveError() {}

                    override fun onCurrentQueueIndexUpdated(queueIndex: Int) {
                        if (expectedQueueIndex >= 0) {
                            assertEquals(expectedQueueIndex.toLong(), queueIndex.toLong())
                        }
                        latch?.countDown()
                    }

                    override fun onQueueUpdated(title: String, newQueue: List<MediaSessionCompat.QueueItem>) {
                        if (expectedNewQueue != null) {
                            assertEquals(expectedNewQueue, newQueue)
                        }
                        latch?.countDown()
                    }
                })
    }

    @Test
    @Throws(Exception::class)
    fun testIsSameBrowsingCategory() {
        val queueManager = createQueueManagerWithValidation(null, -1, null)

        val genres = provider!!.genres.iterator()
        val genre1 = genres.next()
        val genre2 = genres.next()
        val queueGenre1 = QueueHelper.getPlayingQueue(
                MediaIDHelper.createMediaID(null, MediaIDHelper.MEDIA_ID_MUSICS_BY_GENRE, genre1),
                provider)
        val queueGenre2 = QueueHelper.getPlayingQueue(
                MediaIDHelper.createMediaID(null, MediaIDHelper.MEDIA_ID_MUSICS_BY_GENRE, genre2),
                provider)

        // set the current queue
        queueManager.setCurrentQueue("Queue genre 1", queueGenre1)

        // the current music cannot be of same browsing category as one with genre 2
        assertFalse(queueManager.isSameBrowsingCategory(queueGenre2!![0].description.mediaId!!))

        // the current music needs to be of same browsing category as one with genre 1
        assertTrue(queueManager.isSameBrowsingCategory(queueGenre1!![0].description.mediaId!!))
    }

    @Test
    @Throws(Exception::class)
    fun testSetValidQueueItem() {
        // Get a queue that contains songs with space on their title (all in our test dataset)
        val queue = QueueHelper.getPlayingQueueFromSearch(
                " ", null, provider)

        val expectedItemIndex = queue.size - 1
        val expectedItem = queue[expectedItemIndex]
        // Latch for 3 tests
        val latch = CountDownLatch(3)
        val queueManager = createQueueManagerWithValidation(latch, expectedItemIndex,
                queue)

        // test 1: set the current queue
        queueManager.setCurrentQueue("Queue 1", queue)

        // test 2: set queue index to the expectedItem using its queueId
        assertTrue(queueManager.setCurrentQueueItem(expectedItem.queueId))

        // test 3: set queue index to the expectedItem using its mediaId
        assertTrue(queueManager.setCurrentQueueItem(expectedItem.description.mediaId))

        latch.await(5, TimeUnit.SECONDS)
    }

    @Test
    @Throws(Exception::class)
    fun testSetInvalidQueueItem() {
        // Get a queue that contains songs with space on their title (all in our test dataset)
        val queue = QueueHelper.getPlayingQueueFromSearch(
                " ", null, provider)

        val expectedItemIndex = queue.size - 1

        // Latch for 1 test, because queueItem setters will fail and not trigger the validation
        // listener
        val latch = CountDownLatch(1)
        val queueManager = createQueueManagerWithValidation(latch, expectedItemIndex,
                queue)

        // test 1: set the current queue
        queueManager.setCurrentQueue("Queue 1", queue)

        // test 2: set queue index to an invalid queueId (we assume MAX_VALUE is invalid, because
        // queueIds are, in uAmp, defined as the item's index, and no queue is big enough to have
        // a MAX_VALUE index)
        assertFalse(queueManager.setCurrentQueueItem(Integer.MAX_VALUE.toLong()))

        // test 3: set queue index to an invalid negative queueId
        assertFalse(queueManager.setCurrentQueueItem(-1))

        // test 3: set queue index to the expectedItem using its mediaId
        assertFalse(queueManager.setCurrentQueueItem("INVALID_MEDIA_ID"))

        latch.await(5, TimeUnit.SECONDS)
    }

    @Test
    @Throws(Exception::class)
    fun testSkip() {
        // Get a queue that contains songs with space on their title (all in our test dataset)
        val queue = QueueHelper.getPlayingQueueFromSearch(
                " ", null, provider)
        assertTrue(queue.size > 3)

        val queueManager = createQueueManagerWithValidation(null, -1, queue)
        queueManager.setCurrentQueue("Queue 1", queue)

        // start on index 3
        var expectedQueueId = queue[3].queueId
        assertTrue(queueManager.setCurrentQueueItem(expectedQueueId))
        assertEquals(expectedQueueId, queueManager.currentMusic!!.queueId)

        // skip to previous (expected: index 2)
        expectedQueueId = queue[2].queueId
        assertTrue(queueManager.skipQueuePosition(-1))
        assertEquals(expectedQueueId, queueManager.currentMusic!!.queueId)

        // skip twice to previous (expected: index 0)
        expectedQueueId = queue[0].queueId
        assertTrue(queueManager.skipQueuePosition(-2))
        assertEquals(expectedQueueId, queueManager.currentMusic!!.queueId)

        // skip to previous (expected: index 0, by definition)
        expectedQueueId = queue[0].queueId
        assertTrue(queueManager.skipQueuePosition(-1))
        assertEquals(expectedQueueId, queueManager.currentMusic!!.queueId)

        // skip to 2 past the last index (expected: index 1, because
        // newindex = (index + skip) % size, by definition)
        expectedQueueId = queue[1].queueId
        assertTrue(queueManager.skipQueuePosition(queue.size + 1))
        assertEquals(expectedQueueId, queueManager.currentMusic!!.queueId)
    }

    @Test
    @Throws(Exception::class)
    fun testSetQueueFromSearch() {
        val queueManager = createQueueManagerWithValidation(null, -1, null)
        // set a queue from a free search
        queueManager.setQueueFromSearch("Romantic", null)
        // confirm that the search results have the expected size of 2 (because we know the dataset)
        assertEquals(2, queueManager.currentQueueSize.toLong())

        // for each result, check if it contains the search term in its title
        for (i in 0 until queueManager.currentQueueSize) {
            val item = queueManager.currentMusic
            assertTrue(item!!.description.title!!.toString().contains("Romantic"))
            queueManager.skipQueuePosition(1)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testSetQueueFromMusic() {
        val queueManager = createQueueManagerWithValidation(null, -1, null)
        // get the first music of the first genre and build a hierarchy-aware version of its
        // mediaId
        val genre = provider!!.genres.iterator().next()
        val metadata = provider!!.getMusicsByGenre(genre).iterator().next()
        val hierarchyAwareMediaID = MediaIDHelper.createMediaID(
                metadata.description.mediaId, MediaIDHelper.MEDIA_ID_MUSICS_BY_GENRE,
                genre)

        // set a queue from the hierarchyAwareMediaID. It should contain all music with the same
        // genre
        queueManager.setQueueFromMusic(hierarchyAwareMediaID)

        // count all songs with the same genre
        var count = 0
        for (m in provider!!.getMusicsByGenre(genre)) {
            count++
        }

        // check if size matches
        assertEquals(count.toLong(), queueManager.currentQueueSize.toLong())

        // Now check if all songs in current queue have the expected genre:
        for (i in 0 until queueManager.currentQueueSize) {
            val item = queueManager.currentMusic
            val musicId = MediaIDHelper.extractMusicIDFromMediaID(
                    item!!.description.mediaId!!)
            val itemGenre = provider!!.getMusic(musicId)!!.getString(
                    MediaMetadataCompat.METADATA_KEY_GENRE)
            assertEquals(genre, itemGenre)
            queueManager.skipQueuePosition(1)
        }
    }
}