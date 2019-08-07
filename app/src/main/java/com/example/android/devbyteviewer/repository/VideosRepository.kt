/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.example.android.devbyteviewer.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.android.devbyteviewer.database.VideosDatabase
import com.example.android.devbyteviewer.database.asDomainModel
import com.example.android.devbyteviewer.domain.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.android.devbyteviewer.network.Network
import com.example.android.devbyteviewer.network.asDatabaseModel

/*
A Repository is just a regular class that has one (or more) methods that load data without specifying
the data source as part of the main API. Because it's just a regular class, there's no need for an
annotation to define a repository. The repository hides the complexity of managing the interactions
between the database and the networking code.*/

//This is dependency injetion, pass db as constructor param, don't need to keep a reference to android context


class VideosRepository(private val database: VideosDatabase) {

    /**
     * A playlist of videos that can be shown on the screen.
     */
    //loads the videos from the offline cache
    //asDomainModel referenced in Network, DataTransferObjects
    val videos: LiveData<List<Video>> =
            Transformations.map(database.videoDao.getVideos()) {
                it.asDomainModel()
            }

    /**
     * Refresh the videos stored in the offline cache.
     *
     * This function uses the IO dispatcher to ensure the database insert database operation
     * happens on the IO dispatcher. By switching to the IO dispatcher using `withContext` this
     * function is now safe to call from any thread including the Main thread.
     *
     * To actually load the videos for use, observe [videos]
     */
//refreshes the videos
    //just updates the offline cache
    //asDatabaseModel referenced in DataTransferObjects
    //called from a coroutine so use suspend
    suspend fun refreshVideos() {
        withContext(Dispatchers.IO) {
            val playlist = Network.devbytes.getPlaylist().await()
            database.videoDao.insertAll(*playlist.asDatabaseModel())
        }
    }
}

