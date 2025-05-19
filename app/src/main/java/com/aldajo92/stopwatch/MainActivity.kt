package com.aldajo92.stopwatch

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                    StopwatchScreen()
            }
        }
    }
}

@Composable
fun StopwatchScreen() {
    var timeElapsed by remember { mutableIntStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }

    val hours = (timeElapsed / 3600)
    val minutes = (timeElapsed % 3600) / 60
    val seconds = timeElapsed % 60

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(id = R.string.stopwatch),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
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
                    text = "%02d:%02d:%02d".format(hours, minutes, seconds),
                    fontSize = 36.sp
                )
                Spacer(modifier = Modifier.height(32.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_reset),
                    contentDescription = stringResource(id = R.string.reset_stopwatch),
                    modifier = Modifier
                        .size(36.dp)
                        .clickable {
                            timeElapsed = 0
                            isRunning = false
                        }
                )
            }
        }

        IconButton(
            onClick = {
                isRunning = !isRunning
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

@Composable
fun sendStopwatchCommand(context: Context, action: String) {
    val intent = Intent(context, StopwatchService::class.java).apply {
        putExtra(StopwatchService.STOPWATCH_ACTION, action)
    }
    ContextCompat.startForegroundService(context, intent)
}

//
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityMainBinding
//
//    private var isStopwatchRunning = false
//
//    private lateinit var statusReceiver: BroadcastReceiver
//    private lateinit var timeReceiver: BroadcastReceiver
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        binding.toggleButton.setOnClickListener {
//            if (isStopwatchRunning) pauseStopwatch() else startStopwatch()
//        }
//
//        binding.resetImageView.setOnClickListener {
//            resetStopwatch()
//        }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(POST_NOTIFICATIONS),
//                1
//            )
//        } else {
//            // old way
//        }
//    }
//
//
//    override fun onStart() {
//        super.onStart()
//
//        // Moving the service to background when the app is visible
//        moveToBackground()
//    }
//
//    override fun onResume() {
//        super.onResume()
//
//        getStopwatchStatus()
//
//        // Receiving stopwatch status from service
//        val statusFilter = IntentFilter()
//        statusFilter.addAction(StopwatchService.STOPWATCH_STATUS)
//        statusReceiver = object : BroadcastReceiver() {
//            @SuppressLint("SetTextI18n")
//            override fun onReceive(p0: Context?, p1: Intent?) {
//                val isRunning =
//                    p1?.getBooleanExtra(StopwatchService.IS_STOPWATCH_RUNNING, false) ?: false
//                isStopwatchRunning = isRunning
//                val timeElapsed = p1?.getIntExtra(StopwatchService.TIME_ELAPSED, 0) ?: 0
//
//                updateLayout(isStopwatchRunning)
//                updateStopwatchValue(timeElapsed)
//            }
//        }
//        registerReceiver(statusReceiver, statusFilter, Context.RECEIVER_EXPORTED)
//
//        // Receiving time values from service
//        val timeFilter = IntentFilter()
//        timeFilter.addAction(StopwatchService.STOPWATCH_TICK)
//        timeReceiver = object : BroadcastReceiver() {
//            override fun onReceive(p0: Context?, p1: Intent?) {
//                val timeElapsed = p1?.getIntExtra(StopwatchService.TIME_ELAPSED, 0)!!
//                updateStopwatchValue(timeElapsed)
//            }
//        }
//        registerReceiver(timeReceiver, timeFilter, Context.RECEIVER_EXPORTED)
//    }
//
//    override fun onPause() {
//        super.onPause()
//
//        unregisterReceiver(statusReceiver)
//        unregisterReceiver(timeReceiver)
//
//        // Moving the service to foreground when the app is in background / not visible
//        moveToForeground()
//    }
//
//    @SuppressLint("SetTextI18n")
//    private fun updateStopwatchValue(timeElapsed: Int) {
//        val hours: Int = (timeElapsed / 60) / 60
//        val minutes: Int = timeElapsed / 60
//        val seconds: Int = timeElapsed % 60
//        binding.stopwatchValueTextView.text =
//            "${"%02d".format(hours)}:${"%02d".format(minutes)}:${"%02d".format(seconds)}"
//    }
//
//    private fun updateLayout(isStopwatchRunning: Boolean) {
//        if (isStopwatchRunning) {
//            binding.toggleButton.icon =
//                ContextCompat.getDrawable(this, R.drawable.ic_pause)
//            binding.resetImageView.visibility = View.INVISIBLE
//        } else {
//            binding.toggleButton.icon =
//                ContextCompat.getDrawable(this, R.drawable.ic_play)
//            binding.resetImageView.visibility = View.VISIBLE
//        }
//    }
//
//    private fun getStopwatchStatus() {
//        val stopwatchService = Intent(this, StopwatchService::class.java)
//        stopwatchService.putExtra(StopwatchService.STOPWATCH_ACTION, StopwatchService.GET_STATUS)
//        ContextCompat.startForegroundService(this, stopwatchService)
//    }
//
//    private fun startStopwatch() {
//        val stopwatchService = Intent(this, StopwatchService::class.java)
//        stopwatchService.putExtra(StopwatchService.STOPWATCH_ACTION, StopwatchService.START)
//        ContextCompat.startForegroundService(this, stopwatchService)
//    }
//
//    private fun pauseStopwatch() {
//        val stopwatchService = Intent(this, StopwatchService::class.java)
//        stopwatchService.putExtra(StopwatchService.STOPWATCH_ACTION, StopwatchService.PAUSE)
//        ContextCompat.startForegroundService(this, stopwatchService)
//    }
//
//    private fun resetStopwatch() {
//        val stopwatchService = Intent(this, StopwatchService::class.java)
//        stopwatchService.putExtra(StopwatchService.STOPWATCH_ACTION, StopwatchService.RESET)
//        ContextCompat.startForegroundService(this, stopwatchService)
//    }
//
//    private fun moveToForeground() {
//        val stopwatchService = Intent(this, StopwatchService::class.java)
//        stopwatchService.putExtra(
//            StopwatchService.STOPWATCH_ACTION,
//            StopwatchService.MOVE_TO_FOREGROUND
//        )
//        ContextCompat.startForegroundService(this, stopwatchService)
//    }
//
//    private fun moveToBackground() {
//        val stopwatchService = Intent(this, StopwatchService::class.java)
//        stopwatchService.putExtra(
//            StopwatchService.STOPWATCH_ACTION,
//            StopwatchService.MOVE_TO_BACKGROUND
//        )
//        ContextCompat.startForegroundService(this, stopwatchService)
//    }
//}