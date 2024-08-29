package com.example.mapapplication.common

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.VibrationEffect
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import android.os.Vibrator

fun View.showKeyboard() =
    ViewCompat.getWindowInsetsController(this)?.show(WindowInsetsCompat.Type.ime())

fun View.hideKeyboard() =
    ViewCompat.getWindowInsetsController(this)?.hide(WindowInsetsCompat.Type.ime())

fun Context.showKeyboard() = getActivity()?.showKeyboard()
fun Context.hideKeyboard() = getActivity()?.hideKeyboard()

fun Activity.showKeyboard() =
    WindowCompat.getInsetsController(window, window.decorView).show(WindowInsetsCompat.Type.ime())

fun Activity.hideKeyboard() =
    WindowCompat.getInsetsController(window, window.decorView).hide(WindowInsetsCompat.Type.ime())

fun Context.getActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> this.baseContext.getActivity()
        else -> null
    }
}

fun Context.vibrateDevice() {
    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // For newer versions, you can control the vibration pattern more precisely
        val vibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(vibrationEffect)
    } else {
        // For older versions, just specify the vibration duration
        vibrator.vibrate(500)
    }
}