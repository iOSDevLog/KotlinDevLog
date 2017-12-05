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
package com.example.android.uamp.utils

import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import com.example.android.uamp.TestSetupHelper
import com.example.android.uamp.model.MusicProvider

import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import java.util.*

class QueueHelperTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

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
                "https://examplemusic.com/music1.mp3", "https://icons.com/album1.png", 1, 3, 3200)
        source.add("Music 2", "Album 1", "Joe Singer", "Genre 1",
                "https://examplemusic.com/music2.mp3", "https://icons.com/album1.png", 2, 3, 3300)
        source.add("Music 3", "Album 1", "John Singer", "Genre 1",
                "https://examplemusic.com/music3.mp3", "https://icons.com/album1.png", 3, 3, 3400)
        source.add("Romantic Song 1", "Album 2", "Joe Singer", "Genre 2",
                "https://examplemusic.com/music4.mp3", "https://icons.com/album2.png", 1, 2, 4200)
        source.add("Romantic Song 2", "Album 2", "Joe Singer", "Genre 2",
                "https://examplemusic.com/music5.mp3", "https://icons.com/album2.png", 2, 2, 4200)
    }

    @Test
    @Throws(Exception::class)
    fun testGetPlayingQueueForSelectedPlayableMedia() {
        val selectedMusic = provider!!.getMusicsByGenre("Genre 1").iterator().next()
        val selectedGenre = selectedMusic.getString(MediaMetadataCompat.METADATA_KEY_GENRE)

        assertEquals("Genre 1", selectedGenre)

        val mediaId = MediaIDHelper.createMediaID(
                selectedMusic.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID),
                MediaIDHelper.MEDIA_ID_MUSICS_BY_GENRE, selectedGenre)

        val queue = QueueHelper.getPlayingQueue(mediaId, provider)
        assertNotNull(queue)
        assertFalse(queue!!.isEmpty())

        // sort by music title to simplify assertions below
        Collections.sort<MediaSessionCompat.QueueItem>(queue) { lhs, rhs ->
            lhs.description.title.toString().compareTo(
                    rhs.description.title.toString())
        }

        // assert they are all of the expected genre
        for (item in queue) {
            val musicId = MediaIDHelper.extractMusicIDFromMediaID(item.description.mediaId!!)
            val metadata = provider!!.getMusic(musicId)
            assertEquals(selectedGenre, metadata!!.getString(MediaMetadataCompat.METADATA_KEY_GENRE))
        }

        // assert that all the tracks are what we expect
        assertEquals("Music 1", queue[0].description.title)
        assertEquals("Music 2", queue[1].description.title)
        assertEquals("Music 3", queue[2].description.title)
    }

    @Test
    @Throws(Exception::class)
    fun testGetPlayingQueueFromUnstructuredSearch() {
        val queue = QueueHelper.getPlayingQueueFromSearch(
                "Romantic", null, provider)
        assertNotNull(queue)
        assertFalse(queue.isEmpty())

        // assert they all contain "Romantic" in the title
        for (item in queue) {
            val musicId = MediaIDHelper.extractMusicIDFromMediaID(item.description.mediaId!!)
            val metadata = provider!!.getMusic(musicId)
            assertTrue(metadata!!.getString(MediaMetadataCompat.METADATA_KEY_TITLE).contains("Romantic"))
        }
    }

    @Test
    @Throws(Exception::class)
    fun testGetPlayingQueueFromArtistSearch() {
        val extras = Bundle()
        extras.putString(MediaStore.EXTRA_MEDIA_FOCUS, MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE)
        extras.putString(MediaStore.EXTRA_MEDIA_ARTIST, "Joe")
        val queue = QueueHelper.getPlayingQueueFromSearch(
                "Joe", extras, provider)
        assertNotNull(queue)
        assertFalse(queue.isEmpty())

        // assert they all contain "Joe" in the artist
        for (item in queue) {
            val musicId = MediaIDHelper.extractMusicIDFromMediaID(item.description.mediaId!!)
            val metadata = provider!!.getMusic(musicId)
            assertTrue(metadata!!.getString(MediaMetadataCompat.METADATA_KEY_ARTIST).contains("Joe"))
        }
    }

    @Test
    @Throws(Exception::class)
    fun testGetMusicIndexOnQueue() {
        // get a queue with all songs with "c" in their title
        val queue = QueueHelper.getPlayingQueueFromSearch("c", null, provider)

        assertNotNull(queue)
        assertFalse(queue.isEmpty())

        for (i in queue.indices) {
            val item = queue[i]
            assertEquals(i.toLong(), QueueHelper.getMusicIndexOnQueue(queue, item.description.mediaId).toLong())
            assertEquals(i.toLong(), QueueHelper.getMusicIndexOnQueue(queue, item.queueId).toLong())
        }
    }

    @Test
    @Throws(Exception::class)
    fun testGetRandomQueue() {
        val queue = QueueHelper.getRandomQueue(provider)
        assertNotNull(queue)
        assertFalse(queue.isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun testIsIndexPlayable() {
        // get a queue with all songs with "c" on its title
        val queue = QueueHelper.getPlayingQueueFromSearch("c", null, provider)

        assertFalse(QueueHelper.isIndexPlayable(-1, queue))
        assertFalse(QueueHelper.isIndexPlayable(queue.size, queue))
        assertFalse(QueueHelper.isIndexPlayable(Integer.MAX_VALUE, queue))

        if (!queue.isEmpty()) {
            assertTrue(QueueHelper.isIndexPlayable(queue.size - 1, queue))
            assertTrue(QueueHelper.isIndexPlayable(0, queue))
        }
    }
}