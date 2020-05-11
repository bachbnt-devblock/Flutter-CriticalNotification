package com.example.criticalalert

import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import android.util.Log
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugins.GeneratedPluginRegistrant
import com.google.firebase.messaging.FirebaseMessaging
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugins.firebasemessaging.FirebaseMessagingPlugin
import io.flutter.plugins.firebasemessaging.FlutterFirebaseMessagingService

class MainActivity: FlutterActivity(),MethodChannel.MethodCallHandler {
    val TAG="MainActivity"
    val CHANNEL="crossingthestreams.io/resourceResolver"
//    var nextAction=""
//    lateinit var methodChannel:MethodChannel
//
//    override fun onNewIntent(intent: Intent) {
//        super.onNewIntent(intent)
//        nextAction=intent.getStringExtra("action")
//        Log.i(TAG,"nextAction: $nextAction")
//        methodChannel.invokeMethod("getServiceData",nextAction)
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
                MethodChannel(flutterEngine!!.dartExecutor.binaryMessenger,CHANNEL).setMethodCallHandler { call, result ->
            if(call.method=="callService") {
                Log.i(TAG,"callService")
                try {
                    EmergencyAlarmService.startAlarm(this, call.arguments as HashMap<String, String>)
                } catch (e: Exception) {
                    Log.e("** Exception", e.toString())
                }

            }
        }
    }
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
//        if (call.method=="getServiceData") {
//            var data=nextAction
//            result.success(data)
//        }
    }
}
