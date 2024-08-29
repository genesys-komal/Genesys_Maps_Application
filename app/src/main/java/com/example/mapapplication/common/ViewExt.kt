package com.example.mapapplication.common

import android.widget.EditText
import androidx.core.widget.doOnTextChanged
import kotlinx.coroutines.*

fun EditText.doOnTextChangedWithDebounce(
    delayMillis: Long = 500L,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
    onDebouncedTextChanged: (CharSequence?) -> Unit
) {
    var searchJob: Job? = null

    this.doOnTextChanged { text, _, _, _ ->
        if (!hasFocus()) return@doOnTextChanged
        searchJob?.cancel()
        searchJob = coroutineScope.launch {
            delay(delayMillis)
            onDebouncedTextChanged(text)
        }
    }
}

fun EditText.updateText(text: String) {
    val focussed = hasFocus()
    if (focussed) {
        clearFocus()
    }
    setText(text)
    if (focussed) {
        requestFocus()
    }
}