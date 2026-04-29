package com.posite.my_alarm.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

inline fun ViewModel.onMain(
    crossinline body: suspend CoroutineScope.() -> Unit
) = viewModelScope.launch {
    body(this)
}

inline fun ViewModel.onIO(
    crossinline body: suspend CoroutineScope.() -> Unit
) = viewModelScope.launch(Dispatchers.IO) {
    body(this)
}

inline fun ViewModel.onAsyncIO(
    crossinline body: suspend CoroutineScope.() -> Unit
) = viewModelScope.async(Dispatchers.IO) {
    body(this)
}