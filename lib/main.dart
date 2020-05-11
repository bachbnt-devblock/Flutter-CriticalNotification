import 'dart:io';
import 'dart:typed_data';

import 'package:after_layout/after_layout.dart';
import 'package:criticalalert/screen_one.dart';
import 'package:criticalalert/screen_two.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_dnd/flutter_dnd.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:volume/volume.dart';

void main() => runApp(App());

class App extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Home(),
    );
  }
}

class Home extends StatefulWidget {
  @override
  _HomeState createState() => _HomeState();
}

class _HomeState extends State<Home> with AfterLayoutMixin<Home> {
  static FlutterLocalNotificationsPlugin flutterLocalNotificationsPlugin =
      FlutterLocalNotificationsPlugin();
  FirebaseMessaging firebaseMessaging = FirebaseMessaging();
  static MethodChannel platform =
      MethodChannel('crossingthestreams.io/resourceResolver');
  bool isNotificationPolicyAccessGranted = false;

  @override
  void initState() {
    // TODO: implement initState
    super.initState();
    Future.delayed(Duration(seconds: 1), () {
      firebaseMessaging.configure(
          onMessage: (Map<String, dynamic> msg) async {
            print("onMessage: $msg");
            await pushNotificationWithSound(msg["data"]['title'],
                msg['data']['body'], msg['data']['action']);
            await platform.invokeMethod("callService", msg['data']);
          },
          onBackgroundMessage: Platform.isIOS ? null : handleBackgroundMessage,
          onLaunch: (Map<String, dynamic> msg) async {
            print("onLaunch: ");
          },
          onResume: (Map<String, dynamic> msg) async {
            print("onResume: ");
          });
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        width: MediaQuery.of(context).size.width,
        child: Column(
          mainAxisSize: MainAxisSize.max,
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            RaisedButton(
              child: Text("Request permission"),
              onPressed: requestPermission,
            ),
//            RaisedButton(
//              child: Text("Turn off DND"),
//              onPressed: turnOffDND,
//            ),
//            RaisedButton(
//              child: Text("Turn on DND"),
//              onPressed: turnOnDND,
//            ),
//            RaisedButton(
//              child: Text("Push notification without sound"),
//              onPressed: pushNotificationWithoutSound,
//            ),
//            RaisedButton(
//              child: Text("Push notification with sound"),
//              onPressed: pushNotificationWithSound,
//            )
          ],
        ),
      ),
    );
  }

  void requestPermission() {
    FlutterDnd.gotoPolicySettings();
  }

  static Future<dynamic> handleBackgroundMessage(
      Map<String, dynamic> msg) async {
    print("onBackground: $msg");
    await pushNotificationWithSound(
        msg['data']['title'], msg['data']['body'], msg['data']['action']);
    await platform.invokeMethod("callService", msg['data']);
  }

//  Future<void> turnOffDND() async {
//    if (await FlutterDnd.isNotificationPolicyAccessGranted)
//      await FlutterDnd.setInterruptionFilter(
//          FlutterDnd.INTERRUPTION_FILTER_ALL);
//  }

//
//  Future<void> turnOnDND() async {
//    if (await FlutterDnd.isNotificationPolicyAccessGranted)
//      await FlutterDnd.setInterruptionFilter(
//          FlutterDnd.INTERRUPTION_FILTER_NONE);
//  }
//
  Future<void> selectNotification(String payload) async {
    if (payload == "action1")
      await Navigator.push(
          context, MaterialPageRoute(builder: (context) => ScreenOne()));
    else if (payload == "action2")
      await Navigator.push(
          context, MaterialPageRoute(builder: (context) => ScreenTwo()));
  }

//
//  Future<void> initAudioStreamType() async {
//    await Volume.controlVolume(AudioManager.STREAM_NOTIFICATION);
//  }
//
//  Future<void> setMaxVol() async {
//    int maxVol = await Volume.getMaxVol;
//    await Volume.setVol(maxVol, showVolumeUI: ShowVolumeUI.HIDE);
//  }

//
//  Future<void> setMinVol()async{
//    await Volume.setVol(0, showVolumeUI: ShowVolumeUI.HIDE);
//  }
//
//  Future<void> pushNotificationWithoutSound() async {
//    var androidPlatformChannelSpecifics = AndroidNotificationDetails(
//        'channel id', 'channel name', 'channel description',
//        importance: Importance.Max,
//        priority: Priority.Max,
//        enableVibration: true,
//        vibrationPattern: Int64List.fromList([200, 100, 200, 100]),
//        visibility: NotificationVisibility.Public,
//        playSound: true);
//    var iOSPlatformChannelSpecifics = IOSNotificationDetails();
//    var platformChannelSpecifics = NotificationDetails(
//        androidPlatformChannelSpecifics, iOSPlatformChannelSpecifics);
//    await flutterLocalNotificationsPlugin.show(
//        0, "title", "body", platformChannelSpecifics);
//  }
//
  static Future<void> pushNotificationWithSound(
      String title, String body, String payload) async {
//    String alarmUri = await platform.invokeMethod('getNotificationUri');
//    final x = UriAndroidNotificationSound(alarmUri);
    var androidPlatformChannelSpecifics = AndroidNotificationDetails(
        'channel id', 'channel name', 'channel description',
        priority: Priority.Max,
        importance: Importance.High,
//        enableVibration: true,
//        vibrationPattern: Int64List.fromList([1000, 2000, 1000, 2000]),
//        sound: x,
//        playSound: true,
        styleInformation: DefaultStyleInformation(true, true));
    var iOSPlatformChannelSpecifics =
        IOSNotificationDetails(presentSound: false);
    var platformChannelSpecifics = NotificationDetails(
        androidPlatformChannelSpecifics, iOSPlatformChannelSpecifics);
    await flutterLocalNotificationsPlugin
        .show(0, title, body, platformChannelSpecifics, payload: payload);
  }

  @override
  void afterFirstLayout(BuildContext context) async {
    var initializationSettingsAndroid =
        AndroidInitializationSettings("app_icon");
    var initializationSettingsIOS = IOSInitializationSettings(
        onDidReceiveLocalNotification: (a, b, c, d) async {});
    var initializationSettings = InitializationSettings(
        initializationSettingsAndroid, initializationSettingsIOS);
    await flutterLocalNotificationsPlugin.initialize(initializationSettings,
        onSelectNotification: selectNotification);
//    await initAudioStreamType();
//    platform.setMethodCallHandler(handleMethod);
    String token = await firebaseMessaging.getToken();
    print("flutter token: $token");
  }
//  Future<void> handleMethod(MethodCall call) async {
//    String nextAction=call.arguments;
//    try{
//      if(nextAction=="action1"){
//        await Navigator.push(
//            context, MaterialPageRoute(builder: (context) => ScreenOne()));
//      }else if(nextAction=="action2"){
//        await Navigator.push(
//            context, MaterialPageRoute(builder: (context) => ScreenTwo()));
//      }
//    }catch(e){
//
//    }
//  }
}
