package com.aldajo92.stopwatch

import android.Manifest.permission.POST_NOTIFICATIONS
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private val viewModel: StopwatchViewModel by viewModels()

    // TODO: move to viewModel
    private lateinit var statusReceiver: BroadcastReceiver
    private lateinit var timeReceiver: BroadcastReceiver

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(POST_NOTIFICATIONS),
            1
        )

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                StopwatchScreen()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Moving the service to background when the app is visible
//        moveToBackground()
    }

    override fun onResume() {
        super.onResume()

        setupStopwatchReceiver()

    }

    private fun setupStopwatchReceiver() {
        // getStopwatchStatus()
        // Receiving stopwatch status from service
        val statusFilter = IntentFilter()
        statusFilter.addAction(StopwatchService.STOPWATCH_STATUS)
        statusReceiver = object : BroadcastReceiver() {
            @SuppressLint("SetTextI18n")
            override fun onReceive(p0: Context?, p1: Intent?) {
                val isRunning =
                    p1?.getBooleanExtra(StopwatchService.IS_STOPWATCH_RUNNING, false) ?: false
                val timeElapsed = p1?.getIntExtra(StopwatchService.TIME_ELAPSED, 0) ?: 0

                updateLayout(isRunning)
                updateStopwatchValue(timeElapsed)
            }
        }
        registerReceiver(statusReceiver, statusFilter, Context.RECEIVER_EXPORTED)

        // Receiving time values from service
        val timeFilter = IntentFilter()
        timeFilter.addAction(StopwatchService.STOPWATCH_TICK)
        timeReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                val timeElapsed = p1?.getIntExtra(StopwatchService.TIME_ELAPSED, 0)!!
                updateStopwatchValue(timeElapsed)
            }
        }
        registerReceiver(timeReceiver, timeFilter, Context.RECEIVER_EXPORTED)
    }

    override fun onPause() {
        super.onPause()

        unregisterReceiver(statusReceiver)
        unregisterReceiver(timeReceiver)

        // Moving the service to foreground when the app is in background / not visible
        if (viewModel.isRunning.value) {
            moveToForeground()
        }
    }

    private fun updateStopwatchValue(timeElapsed: Int) {
        val hours: Int = (timeElapsed / 60) / 60
        val minutes: Int = timeElapsed / 60
        val seconds: Int = timeElapsed % 60

        viewModel.updateStopwatchValue(
            "${"%02d".format(hours)}:${"%02d".format(minutes)}:${"%02d".format(seconds)}"
        )
    }

    private fun updateLayout(isStopwatchRunning: Boolean) {
        viewModel.setIsRunning(isStopwatchRunning)
    }

    private fun getStopwatchStatus() {
        Log.i("MainActivity", "getStopwatchStatus: ${viewModel.isRunning.value}")
        val stopwatchService = Intent(this@MainActivity, StopwatchService::class.java)
        stopwatchService.putExtra(StopwatchService.STOPWATCH_ACTION, StopwatchService.GET_STATUS)
        ContextCompat.startForegroundService(this@MainActivity, stopwatchService)
    }

    private fun moveToForeground() {
        Log.i("MainActivity", "moveToForeground: ${viewModel.isRunning.value}")
        val stopwatchService = Intent(this@MainActivity, StopwatchService::class.java)
        stopwatchService.putExtra(
            StopwatchService.STOPWATCH_ACTION,
            StopwatchService.MOVE_TO_FOREGROUND
        )
        ContextCompat.startForegroundService(this@MainActivity, stopwatchService)
    }

    private fun moveToBackground() {
        Log.i("MainActivity", "moveToBackground: ${viewModel.isRunning.value}")
        val stopwatchService = Intent(this@MainActivity, StopwatchService::class.java)
        stopwatchService.putExtra(
            StopwatchService.STOPWATCH_ACTION,
            StopwatchService.MOVE_TO_BACKGROUND
        )
        ContextCompat.startForegroundService(this@MainActivity, stopwatchService)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @Composable
    fun StopwatchScreen() {
        val context = LocalContext.current
        val timeElapsedText by viewModel.timeElapsedText.collectAsState()
        val isRunning by viewModel.isRunning.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = R.string.stopwatch),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Start)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth(0.75f)
                    .clip(CircleShape)
                    .background(Color(0xFFBEAEE2)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeElapsedText,
                        fontSize = 36.sp
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_reset),
                        contentDescription = stringResource(id = R.string.reset_stopwatch),
                        modifier = Modifier
                            .size(36.dp)
                            .clickable {
                                viewModel.resetStopwatch(context)
                            }
                    )
                }
            }

            IconButton(
                onClick = {
                    if (isRunning)
                        viewModel.pauseStopwatch(context)
                    else
                        viewModel.startStopwatch(context)
                },
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.1f))
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isRunning) R.drawable.ic_pause else R.drawable.ic_play
                    ),
                    contentDescription = stringResource(id = R.string.toggle_stopwatch),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }

}
