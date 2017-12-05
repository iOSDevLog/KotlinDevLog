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

import android.support.v4.media.session.MediaSessionCompat

internal open class SimplePlayback : Playback {
    override fun start() {

    }

    override fun stop(notifyListeners: Boolean) {

    }

    override fun setState(state: Int) {

    }

    override fun getState(): Int {
        return 0
    }

    override fun isConnected(): Boolean {
        return false
    }

    override fun isPlaying(): Boolean {
        return false
    }

    override fun getCurrentStreamPosition(): Long {
        return 0
    }

    override fun updateLastKnownStreamPosition() {

    }

    override fun play(item: MediaSessionCompat.QueueItem) {

    }

    override fun pause() {

    }

    override fun seekTo(position: Long) {

    }

    override fun setCurrentMediaId(mediaId: String) {

    }

    override fun getCurrentMediaId(): String? {
        return null
    }

    override fun setCallback(callback: Playback.Callback) {

    }
}
