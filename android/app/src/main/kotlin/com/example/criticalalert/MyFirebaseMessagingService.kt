package com.example.criticalalert

import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService:FirebaseMessagingService() {
    val TAG="FCM"
    override fun onNewToken(p0: String) {
        Log.i(TAG,"new token: $p0")
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        Log.i(TAG,"receive")
        try {
        }catch(e:Exception){
            Log.i(TAG,"error: $e")
        }
    }
}