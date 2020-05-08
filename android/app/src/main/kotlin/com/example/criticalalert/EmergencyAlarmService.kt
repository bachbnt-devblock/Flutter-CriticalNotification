package com.example.criticalalert

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

const val EMERGENCY_FOREGROUND_SERVICE_CHANNEL = "EMERGENCY_FOREGROUND_SERVICE_CHANNEL"
const val EMERGENCY_FOREGROUND_SERVICE_NOTIFICATION_ID = -1

const val EMERGENCY_DEFAULT_CHANNEL = "EMERGENCY_DEFAULT_CHANNEL"

const val ACTION_START_ALARM = "ACTION_START_ALARM"

class EmergencyAlarmService : Service() {
  companion object {
    fun startAlarm(context: Context, data: Map<String, String>) {
      val intent = Intent(context, EmergencyAlarmService::class.java).apply {
        action = ACTION_START_ALARM
        for ((key, value) in data) {
          putExtra(key, value)
        }
      }
      context.startService(intent)
    }
  }

  private val vibrationPattern = longArrayOf(0, 400, 200, 400)

  private val notificationManager: NotificationManager
    get() = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

  private val audioManager: AudioManager
    get() = getSystemService(Context.AUDIO_SERVICE) as AudioManager

  private lateinit var alarmPlayer: MediaPlayer

  override fun onBind(p0: Intent?): IBinder? = null

  override fun onCreate() {
    super.onCreate()

    Log.e("*****", "onCreate")

    createForegroundChannel()

    initAlarmMediaPlayer()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    Log.e("*****", "onStartCommand $alarmPlayer")

    startInForeground()

    val reverseVolume = maximizeNotificationVolume()

    val reverseDisableDoNotDisturb = disableDoNotDisturb()

    if (intent != null) {
      displayEmergencyNotification(intent)
    }

    alarmPlayer.setOnCompletionListener {
      alarmPlayer.stop()
      reverseVolume.run()
      reverseDisableDoNotDisturb.run()
      stopSelf()
    }

    if (!alarmPlayer.isPlaying) {
      alarmPlayer.start()
    }

    return START_NOT_STICKY
  }

  override fun onDestroy() {
    Log.e("*****", "onDestroy")
    super.onDestroy()
  }

  private fun createForegroundChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
          EMERGENCY_FOREGROUND_SERVICE_CHANNEL,
          EMERGENCY_FOREGROUND_SERVICE_CHANNEL,
          NotificationManager.IMPORTANCE_NONE
      )

      notificationManager.createNotificationChannel(channel)
    }
  }

  private fun initAlarmMediaPlayer() {
    //    alarmPlayer = MediaPlayer.create(this, R.raw.music_box)
    val uri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    alarmPlayer = MediaPlayer.create(this, uri)
  }

  private fun startInForeground() {
    val builder = NotificationCompat.Builder(this, EMERGENCY_DEFAULT_CHANNEL)
        .setContentTitle(EMERGENCY_DEFAULT_CHANNEL)
        .setContentText(EMERGENCY_DEFAULT_CHANNEL)
        .setSmallIcon(R.drawable.launch_background)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      builder.priority = NotificationManager.IMPORTANCE_NONE
    }

    val foregroundNotification = builder.build()

    startForeground(EMERGENCY_FOREGROUND_SERVICE_NOTIFICATION_ID, foregroundNotification)
  }

  private fun maximizeNotificationVolume(): Runnable {
    val volume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
    audioManager.setStreamVolume(
        AudioManager.STREAM_NOTIFICATION,
        audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION),
        4
    )

    return Runnable {
      audioManager.setStreamVolume(
          AudioManager.STREAM_NOTIFICATION,
          volume,
          4
      )
    }
  }

  private fun disableDoNotDisturb(): Runnable {
    var currentInterruptionFilter: Int? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      currentInterruptionFilter = notificationManager.currentInterruptionFilter
      notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
    }

    return Runnable {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && currentInterruptionFilter != null) {
        notificationManager.setInterruptionFilter(currentInterruptionFilter)
      }
    }
  }

  private fun displayEmergencyNotification(intent: Intent) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(EMERGENCY_DEFAULT_CHANNEL,
          EMERGENCY_DEFAULT_CHANNEL,
          NotificationManager.IMPORTANCE_HIGH
      ).apply {
        vibrationPattern = vibrationPattern
      }

      notificationManager.createNotificationChannel(channel)
    }

    val openActivityIntent = Intent(this, MainActivity::class.java).apply {
      action = intent.getStringExtra("action")

      for (key in intent.extras!!.keySet()) {
        putExtra(key, intent.getStringExtra(key))
        Log.e("************ putExtra", "$key ${intent.getStringExtra(key)}")
      }
    }

    val pendingIntent = PendingIntent.getActivity(this, 0, openActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT)

    val emergencyNotification: Notification =
        NotificationCompat.Builder(this, EMERGENCY_DEFAULT_CHANNEL)
            .setContentTitle(intent.getStringExtra("title"))
            .setContentText(intent.getStringExtra("body"))
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setSmallIcon(R.drawable.launch_background)
            .setVibrate(vibrationPattern)
            .build()

    notificationManager.notify((Math.random() * 1000000).toInt(), emergencyNotification)
  }

}