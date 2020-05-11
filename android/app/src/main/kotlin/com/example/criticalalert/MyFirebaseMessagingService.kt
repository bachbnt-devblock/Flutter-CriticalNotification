package com.example.criticalalert

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

const val TAG = "FCM"

class MyFirebaseMessagingService : FirebaseMessagingService() {

  override fun onNewToken(p0: String) {
    Log.i(TAG, "new token: $p0")
  }

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
//    EmergencyAlarmService.startAlarm(this, remoteMessage.data)

    Log.i(TAG, "received")
    try {
    } catch (e: Exception) {
      Log.i(TAG, "error: $e")
    }
  }
}