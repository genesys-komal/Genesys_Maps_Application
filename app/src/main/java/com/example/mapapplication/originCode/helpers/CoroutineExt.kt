package com.example.mapapplication.originCode.helpers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun ioThread(start: suspend () -> Unit) = CoroutineScope(Dispatchers.IO).launch {
    start()
}