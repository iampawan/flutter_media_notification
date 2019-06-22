import 'dart:async';

import 'package:flutter/services.dart';
import 'package:meta/meta.dart';

class MediaNotification {
  static const MethodChannel _channel = const MethodChannel('media_notification');
  static Map<String, Function> _listeners = new Map();

  static Future<dynamic> _myUtilsHandler(MethodCall methodCall) async {
    _listeners.forEach((event, callback) {
      if (methodCall.method == event) {
        callback();
        return true;
      }
    });
  }

  static Future show({@required title, @required author, play = true, beint = false}) async {
    final Map<String, dynamic> params = <String, dynamic>{
      'title': title,
      'author': author,
      'play': play,
      'beint': beint
    };
    await _channel.invokeMethod('show', params);

    _channel.setMethodCallHandler(_myUtilsHandler);
  }

  static Future hide() async {
    await _channel.invokeMethod('hide');
  }

  static setListener(String event, Function callback) {
    _listeners.addAll({event: callback});
  }

  static stopSound() async {
    await _channel.invokeMethod('stopSound');
  }

  static getWifiLock() async {
    await _channel.invokeMethod('getWifiLock');
  }

  static releaseWifiLock() async {
    await _channel.invokeMethod('releaseWifiLock');
  }

}
