package com.aldajo92.stopwatch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StopwatchViewModel() : ViewModel() {

    private val _timeElapsedText = MutableStateFlow("00:00:00")
    val timeElapsedText: StateFlow<String> = _timeElapsedText

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private var isReceiverRegistered = false
    private var statusReceiver: BroadcastReceiver? = null
    private var tickReceiver: BroadcastReceiver? = null

    fun registerReceivers(context: Context) {
        if (isReceiverRegistered) return

        // Stopwatch status updates
        statusReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, intent: Intent?) {
                val isRunning =
                    intent?.getBooleanExtra(StopwatchService.IS_STOPWATCH_RUNNING, false) ?: false
                val timeElapsed = intent?.getIntExtra(StopwatchService.TIME_ELAPSED, 0) ?: 0
                _isRunning.value = isRunning
                updateTimeText(timeElapsed)
            }
        }

        // Tick updates
        tickReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, intent: Intent?) {
                val timeElapsed = intent?.getIntExtra(StopwatchService.TIME_ELAPSED, 0) ?: 0
                updateTimeText(timeElapsed)
            }
        }

        context.registerReceiver(
            statusReceiver,
            IntentFilter(StopwatchService.STOPWATCH_STATUS),
            Context.RECEIVER_EXPORTED
        )

        context.registerReceiver(
            tickReceiver,
            IntentFilter(StopwatchService.STOPWATCH_TICK),
            Context.RECEIVER_EXPORTED
        )

        isReceiverRegistered = true

        // Trigger a status fetch
        sendCommand(context, StopwatchService.GET_STATUS)
    }

    private fun updateTimeText(timeElapsed: Int) {
        val hours: Int = timeElapsed / 3600
        val minutes: Int = (timeElapsed % 3600) / 60
        val seconds: Int = timeElapsed % 60
        _timeElapsedText.value = "%02d:%02d:%02d".format(hours, minutes, seconds)
    }

    fun unregisterReceivers(context: Context) {
        statusReceiver?.let { context.unregisterReceiver(it) }
        tickReceiver?.let { context.unregisterReceiver(it) }
        isReceiverRegistered = false
    }

    private fun sendCommand(context: Context, command: String) {
        val intent = Intent(context, StopwatchService::class.java).apply {
            putExtra(StopwatchService.STOPWATCH_ACTION, command)
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun startStopwatch(context: Context) = sendCommand(context, StopwatchService.START)
    fun pauseStopwatch(context: Context) = sendCommand(context, StopwatchService.PAUSE)
    fun resetStopwatch(context: Context) = sendCommand(context, StopwatchService.RESET)

}
