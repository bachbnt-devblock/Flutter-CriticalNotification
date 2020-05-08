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
import androidx.core.app.NotificationCompat

const val EMERGENCY_CHANNEL = "EMERGENCY_CHANNEL"

const val ACTION_START_ALARM = "ACTION_START_ALARM"

class EmergencyAlarmService : Service() {
  companion object {
    fun startAlarm(context: Context, data: String?) {
      val intent = Intent(context, EmergencyAlarmService::class.java).apply {
        action = ACTION_START_ALARM
        putExtra("next_action", data)
      }
      context.startService(intent)
    }
  }

  private val notificationManager: NotificationManager
    get() = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

  private val audioManager: AudioManager
    get() = getSystemService(Context.AUDIO_SERVICE) as AudioManager

//    private val vibrationService: Vibrator
//        get() = getSystemService(VIBRATOR_SERVICE) as Vibrator

  private lateinit var alarmPlayer: MediaPlayer

  override fun onBind(p0: Intent?): IBinder? = null

  override fun onCreate() {
    super.onCreate()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
          EMERGENCY_CHANNEL,
          EMERGENCY_CHANNEL,
          NotificationManager.IMPORTANCE_HIGH
      )

      notificationManager.createNotificationChannel(channel)
    }

//    alarmPlayer = MediaPlayer.create(this, R.raw.music_box)
    val uri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    alarmPlayer = MediaPlayer.create(this, uri)
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
    audioManager.setStreamVolume(
        AudioManager.STREAM_NOTIFICATION,
        audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION),
        4
    )

    var currentInterruptionFilter: Int? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      currentInterruptionFilter = notificationManager.currentInterruptionFilter
      notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
    }

    val openActivityIntent = Intent(this, MainActivity::class.java).apply {
      action = intent?.getStringExtra("next_action")
    }
    val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, openActivityIntent, 0)

    val notification: Notification =
        NotificationCompat.Builder(this, EMERGENCY_CHANNEL)
            .setContentTitle("SPD Emergency")
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentText("Save the world!!!!")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.launch_background)
            .build()

    startForeground(-1, notification)

    alarmPlayer.setOnCompletionListener {
      alarmPlayer.stop()
      audioManager.setStreamVolume(
          AudioManager.STREAM_NOTIFICATION,
          currentVol,
          4
      )

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && currentInterruptionFilter != null) {
        notificationManager.setInterruptionFilter(currentInterruptionFilter)
      }

      stopSelf()
    }

    if (!alarmPlayer.isPlaying) {
      alarmPlayer.start()
    }

    return START_NOT_STICKY
  }

}