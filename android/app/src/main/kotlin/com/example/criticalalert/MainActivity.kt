package com.example.criticalalert

import android.media.RingtoneManager
import android.util.Log
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugins.GeneratedPluginRegistrant
import com.google.firebase.messaging.FirebaseMessaging
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {
    val TAG="MainActivity"
    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine);
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener(OnCompleteListener { task ->
            if(!task.isSuccessful){
                Log.i(TAG,"failed: ${task.exception}")
            }

            val token=task.result?.token
            Log.i(TAG,"token: ${token}")
        })
        FirebaseMessaging.getInstance().isAutoInitEnabled=true
        MethodChannel(flutterEngine.dartExecutor, "crossingthestreams.io/resourceResolver").setMethodCallHandler { call: MethodCall, result: MethodChannel.Result ->
            if ("getServiceData" == call.method) {
                var data="This is service data"
                result.success(data)
            }
            if ("getNotificationUri" == call.method) {
                result.success(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString())
            }
        }
    }
}
