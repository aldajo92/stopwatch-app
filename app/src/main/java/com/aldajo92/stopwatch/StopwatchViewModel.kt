package com.aldajo92.stopwatch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StopwatchViewModel() : ViewModel() {

    private var statusReceiver: BroadcastReceiver? = null
    private var timeReceiver: BroadcastReceiver? = null

    private val _timeElapsedText = MutableStateFlow("00:00:00")
    val timeElapsedText: StateFlow<String> = _timeElapsedText

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    fun initReceivers(
        statusReceiver: BroadcastReceiver,
        timeReceiver: BroadcastReceiver
    ) {
        this.statusReceiver = statusReceiver
        this.timeReceiver = timeReceiver
    }

    fun updateStopwatchValue(time: String) {
        _timeElapsedText.value = time
    }

    fun setIsRunning(isRunning: Boolean) {
        _isRunning.value = isRunning
    }

    fun startStopwatch(context: Context) {
        val stopwatchService = Intent(context, StopwatchService::class.java)
        stopwatchService.putExtra(StopwatchService.STOPWATCH_ACTION, StopwatchService.START)
        ContextCompat.startForegroundService(context, stopwatchService)
    }

    fun pauseStopwatch(context: Context) {
        val stopwatchService = Intent(context, StopwatchService::class.java)
        stopwatchService.putExtra(StopwatchService.STOPWATCH_ACTION, StopwatchService.PAUSE)
        ContextCompat.startForegroundService(context, stopwatchService)
    }

    fun resetStopwatch(context: Context) {
        val stopwatchService = Intent(context, StopwatchService::class.java)
        stopwatchService.putExtra(StopwatchService.STOPWATCH_ACTION, StopwatchService.RESET)
        ContextCompat.startForegroundService(context, stopwatchService)
    }

}
