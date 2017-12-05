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
import android.support.test.runner.AndroidJUnit4
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.test.mock.MockResources

import com.example.android.uamp.TestSetupHelper
import com.example.android.uamp.model.MusicProvider
import com.example.android.uamp.utils.MediaIDHelper
import com.example.android.uamp.utils.SimpleMusicProviderSource

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

import org.junit.Assert.assertEquals

/**
 * Android instrumentation unit tests for [PlaybackManager] and related classes.
 */
class PlaybackManagerTest {

    private var musicProvider: MusicProvider? = null
    private var resources: Resources? = null

    @Before
    @Throws(Exception::class)
    fun setUpMusicProvider() {
        val source = SimpleMusicProviderSource()
        populateMusicSource(source)
        musicProvider = TestSetupHelper.setupMusicProvider(source)

        resources = object : MockResources() {
            @Throws(Resources.NotFoundException::class)
            override fun getString(id: Int): String {
                return ""
            }

            @Throws(Resources.NotFoundException::class)
            override fun getString(id: Int, vararg formatArgs: Any): String {
                return ""
            }
        }
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

    @Test
    @Throws(Exception::class)
    fun testPlay() {
        var mediaId: String? = MediaIDHelper.MEDIA_ID_ROOT
        while (MediaIDHelper.isBrowseable(mediaId!!)) {
            mediaId = musicProvider!!.getChildren(mediaId, resources)[0].mediaId
        }

        // Using a CountDownLatch, we will check if all callbacks are called correctly when
        // a onPlayFromMediaId command is issued.
        val latch = CountDownLatch(5)
        val expectedMediaId = mediaId

        val queueManager = QueueManager(musicProvider!!, resources!!, object : SimpleMetadataUpdateListener() {
            override fun onMetadataChanged(metadata: MediaMetadataCompat) {
                // Latch countdown 1: QueueManager will change appropriately
                assertEquals(MediaIDHelper.extractMusicIDFromMediaID(expectedMediaId),
                        metadata.description.mediaId)
                latch.countDown()
            }
        })

        val serviceCallback = object : SimplePlaybackServiceCallback() {
            override fun onPlaybackStart() {
                // Latch countdown 2: PlaybackService will get a onPlaybackStart call
                latch.countDown()
            }

            override fun onPlaybackStateUpdated(newState: PlaybackStateCompat) {
                if (newState.state == PlaybackStateCompat.STATE_PLAYING) {
                    // Latch countdown 3: PlaybackService will get a state updated call (here we
                    // ignore the unrelated state changes)
                    latch.countDown()
                }
            }

            override fun onNotificationRequired() {
                // Latch countdown 4: PlaybackService will get call to show a media notification
                latch.countDown()
            }
        }

        val playback = object : SimplePlayback() {
            override fun play(item: MediaSessionCompat.QueueItem) {
                // Latch countdown 5: Playback will be called with the correct queueItem
                assertEquals(expectedMediaId, item.description.mediaId)
                latch.countDown()
            }
        }

        val playbackManager = PlaybackManager(serviceCallback, resources,
                musicProvider, queueManager, playback)
        playbackManager.mediaSessionCallback.onPlayFromMediaId(expectedMediaId, null)

        latch.await(5, TimeUnit.SECONDS)

        // Finally, check if the current music in queueManager is as expected
        assertEquals(expectedMediaId, queueManager.currentMusic!!.description.mediaId)
    }


    @Test
    @Throws(Exception::class)
    fun testPlayFromSearch() {
        // Using a CountDownLatch, we will check if all callbacks are called correctly when
        // a onPlayFromMediaId command is issued.
        val latch = CountDownLatch(5)
        val expectedMusicId = musicProvider!!.searchMusicBySongTitle("Music 3")
                .iterator().next().description.mediaId

        val queueManager = QueueManager(musicProvider!!, resources!!, object : SimpleMetadataUpdateListener() {
            override fun onMetadataChanged(metadata: MediaMetadataCompat) {
                // Latch countdown 1: QueueManager will change appropriately
                assertEquals(expectedMusicId, metadata.description.mediaId)
                latch.countDown()
            }
        })

        val serviceCallback = object : SimplePlaybackServiceCallback() {
            override fun onPlaybackStart() {
                // Latch countdown 2: PlaybackService will get a onPlaybackStart call
                latch.countDown()
            }

            override fun onPlaybackStateUpdated(newState: PlaybackStateCompat) {
                if (newState.state == PlaybackStateCompat.STATE_PLAYING) {
                    // Latch countdown 3: PlaybackService will get a state updated call (here we
                    // ignore the unrelated state changes)
                    latch.countDown()
                }
            }

            override fun onNotificationRequired() {
                // Latch countdown 4: PlaybackService will get call to show a media notification
                latch.countDown()
            }
        }

        val playback = object : SimplePlayback() {
            override fun play(item: MediaSessionCompat.QueueItem) {
                // Latch countdown 5: Playback will be called with the correct queueItem
                assertEquals(expectedMusicId, MediaIDHelper.extractMusicIDFromMediaID(
                        item.description.mediaId!!))
                latch.countDown()
            }
        }

        val playbackManager = PlaybackManager(serviceCallback, resources,
                musicProvider, queueManager, playback)
        playbackManager.mediaSessionCallback.onPlayFromSearch("Music 3", null)

        latch.await(5, TimeUnit.SECONDS)

        // Finally, check if the current music in queueManager is as expected
        assertEquals(expectedMusicId, MediaIDHelper.extractMusicIDFromMediaID(
                queueManager.currentMusic!!.description.mediaId!!))
    }


}