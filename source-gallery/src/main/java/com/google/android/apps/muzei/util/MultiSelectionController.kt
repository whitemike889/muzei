/*
 * Copyright 2014 Google Inc.
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
 */

package com.google.android.apps.muzei.util

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.savedstate.SavedStateRegistry
import java.util.HashSet

/**
 * Utilities for storing multiple selection information in collection views.
 */
class MultiSelectionController(
        lifecycle: Lifecycle,
        private val bundleSavedStateRegistry: SavedStateRegistry<Bundle>
) : DefaultLifecycleObserver, SavedStateRegistry.SavedStateProvider<Bundle> {

    companion object {
        private const val STATE_SELECTION = "selection"
    }

    val selection = HashSet<Long>()
    var callbacks: Callbacks? = null

    val selectedCount: Int
        get() = selection.size

    init {
        lifecycle.addObserver(this)
        bundleSavedStateRegistry.registerSavedStateProvider(STATE_SELECTION, this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        bundleSavedStateRegistry.consumeRestoredStateForKey(STATE_SELECTION)?.run {
            selection.clear()
            val savedSelection = getLongArray(STATE_SELECTION)
            if (savedSelection?.isNotEmpty() == true) {
                for (item in savedSelection) {
                    selection.add(item)
                }
            }
        }

        callbacks?.onSelectionChanged(true, false)
    }

    override fun saveState() = bundleOf(STATE_SELECTION to selection.toLongArray())

    fun toggle(item: Long, fromUser: Boolean) {
        if (selection.contains(item)) {
            selection.remove(item)
        } else {
            selection.add(item)
        }

        callbacks?.onSelectionChanged(false, fromUser)
    }

    fun reset(fromUser: Boolean) {
        selection.clear()
        callbacks?.onSelectionChanged(false, fromUser)
    }

    fun isSelected(item: Long): Boolean {
        return selection.contains(item)
    }

    interface Callbacks {
        fun onSelectionChanged(restored: Boolean, fromUser: Boolean)
    }
}
