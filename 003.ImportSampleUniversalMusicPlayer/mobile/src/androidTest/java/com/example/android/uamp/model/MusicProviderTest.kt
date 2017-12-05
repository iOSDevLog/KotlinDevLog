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

package com.example.android.uamp.model

import android.content.res.Resources
import android.graphics.Bitmap
import android.support.v4.media.MediaMetadataCompat
import android.test.mock.MockResources

import com.example.android.uamp.TestSetupHelper
import com.example.android.uamp.utils.MediaIDHelper
import com.example.android.uamp.utils.SimpleMusicProviderSource

import org.junit.Before
import org.junit.Test

import java.util.ArrayList
import java.util.Arrays
import java.util.Collections

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue

/**
 * Android instrumentation unit tests for [MusicProvider] and related classes.
 */
class MusicProviderTest {

    private var provider: MusicProvider? = null

    @Before
    @Throws(Exception::class)
    fun setupMusicProvider() {
        val source = SimpleMusicProviderSource()
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
        provider = TestSetupHelper.setupMusicProvider(source)
    }

    @Test
    @Throws(Exception::class)
    fun testGetGenres() {
        val genres = provider!!.genres
        val list = ArrayList<String>()
        for (genre in genres) {
            list.add(genre)
        }
        assertEquals(2, list.size.toLong())

        Collections.sort(list)
        assertEquals(Arrays.asList(*arrayOf("Genre 1", "Genre 2")), list)
    }

    @Test
    @Throws(Exception::class)
    fun testGetMusicsByGenre() {
        var count = 0
        for (metadata in provider!!.getMusicsByGenre("Genre 1")) {
            val genre = metadata.getString(MediaMetadataCompat.METADATA_KEY_GENRE)
            assertEquals("Genre 1", genre)
            count++
        }

        assertEquals(3, count.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun testGetMusicsByInvalidGenre() {
        assertFalse(provider!!.getMusicsByGenre("XYZ").iterator().hasNext())
    }

    @Test
    @Throws(Exception::class)
    fun testSearchBySongTitle() {
        var count = 0
        for (metadata in provider!!.searchMusicBySongTitle("Romantic")) {
            val title = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
            assertTrue(title.contains("Romantic"))
            count++
        }

        assertEquals(2, count.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun testSearchByInvalidSongTitle() {
        assertFalse(provider!!.searchMusicBySongTitle("XYZ").iterator().hasNext())
    }

    @Test
    @Throws(Exception::class)
    fun testSearchMusicByAlbum() {
        var count = 0
        for (metadata in provider!!.searchMusicByAlbum("Album")) {
            val title = metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM)
            assertTrue(title.contains("Album"))
            count++
        }

        assertEquals(5, count.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun testSearchMusicByInvalidAlbum() {
        assertFalse(provider!!.searchMusicByAlbum("XYZ").iterator().hasNext())
    }

    @Test
    @Throws(Exception::class)
    fun testSearchMusicByArtist() {
        var count = 0
        for (metadata in provider!!.searchMusicByArtist("Joe")) {
            val title = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
            assertTrue(title.contains("Joe"))
            count++
        }

        assertEquals(3, count.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun testSearchMusicByInvalidArtist() {
        assertFalse(provider!!.searchMusicByArtist("XYZ").iterator().hasNext())
    }

    @Test
    @Throws(Exception::class)
    fun testUpdateMusicArt() {
        val bIcon = Bitmap.createBitmap(2, 2, Bitmap.Config.ALPHA_8)
        val bArt = Bitmap.createBitmap(2, 2, Bitmap.Config.ALPHA_8)

        val metadata = provider!!.shuffledMusic.iterator().next()
        val musicId = metadata.description.mediaId

        assertNotEquals(bArt, metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART))
        assertNotEquals(bIcon, metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON))

        provider!!.updateMusicArt(musicId, bArt, bIcon)
        val newMetadata = provider!!.getMusic(musicId)
        assertEquals(bArt, newMetadata!!.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART))
        assertEquals(bIcon, newMetadata.getBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON))
    }

    @Test
    @Throws(Exception::class)
    fun testFavorite() {
        val metadata = provider!!.shuffledMusic.iterator().next()
        val musicId = metadata.description.mediaId

        assertFalse(provider!!.isFavorite(musicId))
        provider!!.setFavorite(musicId, true)
        assertTrue(provider!!.isFavorite(musicId))
        provider!!.setFavorite(musicId, false)
        assertFalse(provider!!.isFavorite(musicId))
    }

    @Test
    @Throws(Exception::class)
    fun testGetChildren() {
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

        // test an invalid root
        val invalid = provider!!.getChildren(
                "INVALID_MEDIA_ID", resources)
        assertEquals(0, invalid.size.toLong())

        // test level 1 (list of category types - only "by genre" for now)
        val level1 = provider!!.getChildren(
                MediaIDHelper.MEDIA_ID_ROOT, resources)
        assertEquals(1, level1.size.toLong())

        // test level 2 (list of genres)
        var genreCount = 0
        for (ignored in provider!!.genres) {
            genreCount++
        }
        val level2 = provider!!.getChildren(
                level1[0].mediaId, resources)
        assertEquals(genreCount.toLong(), level2.size.toLong())

        // test level 3 (list of music for a given genre)
        val level3 = provider!!.getChildren(
                level2[0].mediaId, resources)
        val genre = MediaIDHelper.extractBrowseCategoryValueFromMediaID(
                level2[0].mediaId!!)
        for (mediaItem in level3) {
            assertTrue(mediaItem.isPlayable)
            assertFalse(mediaItem.isBrowsable)
            val metadata = provider!!.getMusic(
                    MediaIDHelper.extractMusicIDFromMediaID(mediaItem.mediaId!!))
            assertEquals(genre, metadata!!.getString(MediaMetadataCompat.METADATA_KEY_GENRE))
        }

        // test an invalid level 4
        val invalidLevel4 = provider!!.getChildren(
                level3[0].mediaId, resources)
        assertTrue(invalidLevel4.isEmpty())
    }
}