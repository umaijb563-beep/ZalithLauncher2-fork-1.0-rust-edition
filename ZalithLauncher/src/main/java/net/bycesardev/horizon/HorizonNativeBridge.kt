/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package net.bycesardev.horizon

import android.content.res.AssetManager
import android.view.Surface
import androidx.annotation.Keep

/**
 * JNI declarations matching the Android entry points exported by Horizon's
 * `crates/horizon-android` library. Keep this class and its method names in
 * sync with that crate's `Java_net_bycesardev_horizon_MainActivity_*` symbols.
 *
 * This is deliberately not an Activity: the launcher owns the game Activity
 * and its surface. The actual engine library must be packaged in the launcher
 * APK before these methods can be used.
 */
@Keep
class MainActivity private constructor() {
    companion object {
        @JvmStatic
        external fun nativeSetAssetManager(assetManager: AssetManager)

        @JvmStatic
        external fun nativeInit(internalDataDir: String, resourcesDir: String): Boolean

        @JvmStatic
        external fun nativeSurfaceCreated(surface: Surface)

        @JvmStatic
        external fun nativeSurfaceChanged(width: Int, height: Int)

        @JvmStatic
        external fun nativeSurfaceDestroyed()

        @JvmStatic
        external fun nativeOnTouch(pointerId: Int, action: Int, x: Float, y: Float)

        @JvmStatic
        external fun nativeOnPause()

        @JvmStatic
        external fun nativeOnResume()

        @JvmStatic
        external fun nativeOnDestroy()
    }
}

/** Guarded JNI access for the optional Horizon Android backend. */
object HorizonNativeBridge {
    private const val LIBRARY_NAME = "horizon_android"

    /** True only when the Horizon native library was packaged and loaded. */
    val isAvailable: Boolean

    init {
        isAvailable = runCatching { System.loadLibrary(LIBRARY_NAME) }.isSuccess
    }

    /** Initializes the native engine and gives it access to APK assets. */
    fun initialize(
        assetManager: AssetManager,
        internalDataDir: String,
        resourcesDir: String,
    ): Boolean = runCatching {
        check(isAvailable) { "Horizon native library is not packaged in this build" }
        MainActivity.nativeSetAssetManager(assetManager)
        MainActivity.nativeInit(internalDataDir, resourcesDir)
    }.getOrDefault(false)

    fun onSurfaceCreated(surface: Surface): Boolean = invoke { MainActivity.nativeSurfaceCreated(surface) }

    fun onSurfaceChanged(width: Int, height: Int): Boolean = invoke {
        require(width > 0 && height > 0) { "Surface size must be positive" }
        MainActivity.nativeSurfaceChanged(width, height)
    }

    fun onSurfaceDestroyed(): Boolean = invoke { MainActivity.nativeSurfaceDestroyed() }

    /** Horizon uses 0=down, 1=up, 2=move, 3=cancel. */
    fun onTouch(pointerId: Int, action: Int, x: Float, y: Float): Boolean = invoke {
        require(action in 0..3) { "Unsupported Horizon touch action: $action" }
        MainActivity.nativeOnTouch(pointerId, action, x, y)
    }

    fun onPause(): Boolean = invoke { MainActivity.nativeOnPause() }

    fun onResume(): Boolean = invoke { MainActivity.nativeOnResume() }

    fun onDestroy(): Boolean = invoke { MainActivity.nativeOnDestroy() }

    private inline fun invoke(block: () -> Unit): Boolean = runCatching {
        check(isAvailable) { "Horizon native library is not packaged in this build" }
        block()
    }.isSuccess
}
