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

import org.junit.Test

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail

/**
 * Unit tests for the [MediaIDHelper] class. Exercises the helper methods that
 * do MediaID to MusicID conversion and hierarchy (categories) extraction.
 */
class MediaIDHelperTest {

    @Test
    @Throws(Exception::class)
    fun testNormalMediaIDStructure() {
        val mediaID = MediaIDHelper.createMediaID("784343", "BY_GENRE", "Classic 70's")
        assertEquals("Classic 70's", MediaIDHelper.extractBrowseCategoryValueFromMediaID(mediaID))
        assertEquals("784343", MediaIDHelper.extractMusicIDFromMediaID(mediaID))
    }

    @Test
    @Throws(Exception::class)
    fun testSpecialSymbolsMediaIDStructure() {
        val mediaID = MediaIDHelper.createMediaID("78A_88|X/3", "BY_GENRE", "Classic 70's")
        assertEquals("Classic 70's", MediaIDHelper.extractBrowseCategoryValueFromMediaID(mediaID))
        assertEquals("78A_88|X/3", MediaIDHelper.extractMusicIDFromMediaID(mediaID))
    }

    @Test
    @Throws(Exception::class)
    fun testNullMediaIDStructure() {
        val mediaID = MediaIDHelper.createMediaID(null, "BY_GENRE", "Classic 70's")
        assertEquals("Classic 70's", MediaIDHelper.extractBrowseCategoryValueFromMediaID(mediaID))
        assertNull(MediaIDHelper.extractMusicIDFromMediaID(mediaID))
    }

    @Test(expected = IllegalArgumentException::class)
    @Throws(Exception::class)
    fun testInvalidSymbolsInMediaIDStructure() {
        fail(MediaIDHelper.createMediaID(null, "BY|GENRE/2", "Classic 70's"))
    }

    @Test
    @Throws(Exception::class)
    fun testCreateBrowseCategoryMediaID() {
        val browseMediaID = MediaIDHelper.createMediaID(null, "BY_GENRE", "Rock & Roll")
        assertEquals("Rock & Roll", MediaIDHelper.extractBrowseCategoryValueFromMediaID(browseMediaID))
        val categories = MediaIDHelper.getHierarchy(browseMediaID)
        assertArrayEquals(categories, arrayOf("BY_GENRE", "Rock & Roll"))
    }

    @Test
    @Throws(Exception::class)
    fun testGetParentOfPlayableMediaID() {
        val mediaID = MediaIDHelper.createMediaID("23423423", "BY_GENRE", "Rock & Roll")
        val expectedParentID = MediaIDHelper.createMediaID(null, "BY_GENRE", "Rock & Roll")
        assertEquals(expectedParentID, MediaIDHelper.getParentMediaID(mediaID))
    }

    @Test
    @Throws(Exception::class)
    fun testGetParentOfBrowsableMediaID() {
        val mediaID = MediaIDHelper.createMediaID(null, "BY_GENRE", "Rock & Roll")
        val expectedParentID = MediaIDHelper.createMediaID(null, "BY_GENRE")
        assertEquals(expectedParentID, MediaIDHelper.getParentMediaID(mediaID))
    }

    @Test
    @Throws(Exception::class)
    fun testGetParentOfCategoryMediaID() {
        assertEquals(
                MediaIDHelper.MEDIA_ID_ROOT,
                MediaIDHelper.getParentMediaID(MediaIDHelper.createMediaID(null, "BY_GENRE")))
    }

    @Test
    @Throws(Exception::class)
    fun testGetParentOfRoot() {
        assertEquals(
                MediaIDHelper.MEDIA_ID_ROOT,
                MediaIDHelper.getParentMediaID(MediaIDHelper.MEDIA_ID_ROOT))
    }

    @Test(expected = NullPointerException::class)
    @Throws(Exception::class)
    fun testGetParentOfNull() {

        fail(MediaIDHelper.getParentMediaID(null!!))
    }

}