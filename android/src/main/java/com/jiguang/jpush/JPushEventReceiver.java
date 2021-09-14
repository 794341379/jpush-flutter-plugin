package com.jiguang.jpush;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.JPushMessage;
import cn.jpush.android.service.JPushMessageReceiver;
import io.flutter.plugin.common.MethodChannel.Result;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;

public class JPushEventReceiver extends JPushMessageReceiver {

  private String TAG = "jiguang";
  ///此处接口注册 定义对象
  public static ShowCallBack mShowCallBack;

  public static interface ShowCallBack {
    void onShown(String value);
  }

  public static void setShowCallBack(ShowCallBack c) {
    mShowCallBack = c;
  }

  ///

  @Override
  public void onCommandResult(Context context, JPushMessage cmdMessage) {
    
    //注册失败+三方厂商注册回调
    //  Log.e(TAG,"[onCommandResult] "+cmdMessage);
    //cmd为10000时说明为厂商token回调
    if (
      cmdMessage != null && cmdMessage.cmd == 10000 && cmdMessage.extra != null
    ) {
      String token = cmdMessage.extra.getString("token");
      int platform = cmdMessage.extra.getInt("platform");
      String deviceName = "unkown";
      switch (platform) {
        case 1:
          deviceName = "小米";
          break;
        case 2:
          deviceName = "华为";
          break;
        case 3:
          deviceName = "魅族";
          break;
        case 4:
          deviceName = "OPPO";
          break;
        case 5:
          deviceName = "VIVO";
          break;
        case 6:
          deviceName = "ASUS";
          break;
        case 8:
          deviceName = "FCM";
          break;
      }
      ///此处 接口回调 赋值
      mShowCallBack.onShown(token);
      setShowCallBack(mShowCallBack);
      ///

      Log.e(TAG, "获取到 " + deviceName + " 的token:" + token);
    }
  }

  @Override
  public void onTagOperatorResult(
    Context context,
    final JPushMessage jPushMessage
  ) {
    super.onTagOperatorResult(context, jPushMessage);

    final JSONObject resultJson = new JSONObject();

    final int sequence = jPushMessage.getSequence();
    try {
      resultJson.put("sequence", sequence);
    } catch (JSONException e) {
      e.printStackTrace();
    }

    final Result callback = JPushPlugin.instance.callbackMap.get(sequence); //instance.eventCallbackMap.get(sequence);

    if (callback == null) {
      Log.i("JPushPlugin", "Unexpected error, callback is null!");
      return;
    }

    new Handler(Looper.getMainLooper())
    .post(
        new Runnable() {
          @Override
          public void run() {
            if (jPushMessage.getErrorCode() == 0) { // success
              Set<String> tags = jPushMessage.getTags();
              List<String> tagList = new ArrayList<>(tags);
              Map<String, Object> res = new HashMap<>();
              res.put("tags", tagList);
              callback.success(res);
            } else {
              try {
                resultJson.put("code", jPushMessage.getErrorCode());
              } catch (JSONException e) {
                e.printStackTrace();
              }
              callback.error(
                Integer.toString(jPushMessage.getErrorCode()),
                "",
                ""
              );
            }

            JPushPlugin.instance.callbackMap.remove(sequence);
          }
        }
      );
  }

  @Override
  public void onCheckTagOperatorResult(
    Context context,
    final JPushMessage jPushMessage
  ) {
    super.onCheckTagOperatorResult(context, jPushMessage);

    final int sequence = jPushMessage.getSequence();

    final Result callback = JPushPlugin.instance.callbackMap.get(sequence);

    if (callback == null) {
      Log.i("JPushPlugin", "Unexpected error, callback is null!");
      return;
    }

    new Handler(Looper.getMainLooper())
    .post(
        new Runnable() {
          @Override
          public void run() {
            if (jPushMessage.getErrorCode() == 0) {
              Set<String> tags = jPushMessage.getTags();
              List<String> tagList = new ArrayList<>(tags);
              Map<String, Object> res = new HashMap<>();
              res.put("tags", tagList);
              callback.success(res);
            } else {
              callback.error(
                Integer.toString(jPushMessage.getErrorCode()),
                "",
                ""
              );
            }

            JPushPlugin.instance.callbackMap.remove(sequence);
          }
        }
      );
  }

  @Override
  public void onAliasOperatorResult(
    Context context,
    final JPushMessage jPushMessage
  ) {
    super.onAliasOperatorResult(context, jPushMessage);

    final int sequence = jPushMessage.getSequence();

    final Result callback = JPushPlugin.instance.callbackMap.get(sequence);

    if (callback == null) {
      Log.i("JPushPlugin", "Unexpected error, callback is null!");
      return;
    }

    new Handler(Looper.getMainLooper())
    .post(
        new Runnable() {
          @Override
          public void run() {
            if (jPushMessage.getErrorCode() == 0) { // success
              Map<String, Object> res = new HashMap<>();
              res.put(
                "alias",
                (jPushMessage.getAlias() == null) ? "" : jPushMessage.getAlias()
              );
              callback.success(res);
            } else {
              callback.error(
                Integer.toString(jPushMessage.getErrorCode()),
                "",
                ""
              );
            }

            JPushPlugin.instance.callbackMap.remove(sequence);
          }
        }
      );
  }

  @Override
  public void onNotificationSettingsCheck(
    Context context,
    boolean isOn,
    int source
  ) {
    super.onNotificationSettingsCheck(context, isOn, source);

    HashMap<String, Object> map = new HashMap();
    map.put("isEnabled", isOn);
    JPushPlugin.instance.runMainThread(
      map,
      null,
      "onReceiveNotificationAuthorization"
    );
  }
}
