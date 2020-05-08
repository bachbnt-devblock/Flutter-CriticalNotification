package com.example.criticalalert

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
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity(),MethodChannel.MethodCallHandler {
    val TAG="MainActivity"
    val CHANNEL="crossingthestreams.io/resourceResolver"
    private var nextAction=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nextAction= intent?.getStringExtra("next_action")?:""
    }

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine);
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener(OnCompleteListener { task ->
            if(!task.isSuccessful){
                Log.i(TAG,"failed: ${task.exception}")
            }

            val token=task.result?.token
            Log.i(TAG,"*** token: ${token}")
        })
        FirebaseMessaging.getInstance().isAutoInitEnabled=true
        MethodChannel(flutterEngine.dartExecutor, CHANNEL).setMethodCallHandler(this::onMethodCall)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        if (call.method=="getServiceData") {
            var data=nextAction
            result.success(data)
        }
    }
}
