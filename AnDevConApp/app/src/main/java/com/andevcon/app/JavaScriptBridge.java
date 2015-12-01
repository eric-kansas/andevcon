package com.andevcon.app;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.CalendarContract;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.andevcon.app.util.UserLocation;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.Calendar;


/**
 * Class that defines what java methods will be exposed to the JavsScript
 */
public class JavaScriptBridge implements UserLocation.LocationListener {

    private static final String TAG = "JavaScriptBridge";

    // Name of JavaScript defined function that will recieve our Java callbacks
    private static final String JS_INVOKE_CALL = "window.bridgeFromJava.receiveCallFromJava(";

    // Keys for our serialized JavaScript call
    private static final String JSON_METHOD_KEY = "method";
    private static final String JSON_PARAMETERS_KEY = "parameters";

    Context context;
    WebView webview;

    // Used to find user GPS location
    UserLocation userLocation;

    // Our bridge to C/C++
    CPlusPlusBridge cppBridge;

    /**
     * JavaScript Bridge constructor. Take a webview to invoke JavaScript through
     * @param webview       Used to get context and invoke JavaScript
     */
    public JavaScriptBridge(WebView webview) {
        this.context = webview.getContext();
        this.webview = webview;
        this.userLocation = new UserLocation(context, this);
        this.cppBridge = new CPlusPlusBridge();
    }

    /**
     * Gets the GPS location of the device is possible
     */
    @JavascriptInterface
    public void findUserLocation() {
        Log.i(TAG, "Find GPS location of user");
        userLocation.getLocation();
    }

    /**
     * Prompts user to add event to calendar
     * @param eventProperties
     */
    @JavascriptInterface
    public void createCalendarEvent(String eventProperties) {
        Log.i(TAG, "Create calendar event for user");
        String description = null;
        String location = null;
        String summary = null;

        JSONObject start;
        JSONObject end;

        Calendar beginTime = Calendar.getInstance();
        Calendar endTime = Calendar.getInstance();

        // Parse our event data from JSON object
        try {
            JSONObject propertiesJSON = new JSONObject(eventProperties);
            start = propertiesJSON.getJSONObject("start");
            end = propertiesJSON.optJSONObject("end");

            if(end == null)
                end = start;

            description = propertiesJSON.getString("description");
            location = propertiesJSON.optString("location");
            summary = propertiesJSON.optString("summary");

            beginTime.set(start.getInt("year"), start.getInt("month"), start.getInt("day"),
                    start.getInt("hour"), start.getInt("min"));

            endTime.set(end.getInt("year"), end.getInt("month"), end.getInt("day"),
                    end.getInt("hour"), end.getInt("min"));

            Intent intent = new Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                    .putExtra(CalendarContract.Events.TITLE, summary)
                    .putExtra(CalendarContract.Events.DESCRIPTION, description)
                    .putExtra(CalendarContract.Events.EVENT_LOCATION, location);
            context.startActivity(intent);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Encrypts message by passing it to C library for exception
     */
    @JavascriptInterface
    public void getEncryptedId() {
        Log.i(TAG, "Getting encrypted id from C++ for user");
        String id = cppBridge.getEncryptedID("SomeID");
        invokeJavaScript("onEncryptedId", "\"" + id + "\"");
    }


    /**
     * Unified call for invoking Java methods via Reflection
     * @param serializedJavaCall    method and parameters to call
     */
    @JavascriptInterface
    public void invokeJavaMethod(String serializedJavaCall) {
        JSONObject json;
        String methodName;
        JSONObject parameters;

        try {
            json = new JSONObject(serializedJavaCall);
            methodName = json.getString(JSON_METHOD_KEY);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        parameters = json.optJSONObject(JSON_PARAMETERS_KEY);

        Log.i(TAG, "Calling method: " + methodName + " with parameters: " + parameters);

        // Call method via reflection
        try {
            if (parameters == null) {
                Method m = JavaScriptBridge.class.getMethod(methodName);
                m.invoke(this);
            } else {
                Method m = JavaScriptBridge.class.getMethod(methodName, String.class);
                m.invoke(this, parameters.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Success callback for find user location
     * @param loc       JSON object with longitude and latitude
     */
    @Override
    public void onLocation(String loc) {
        Log.i(TAG, "Found location of user: " + loc);

        // Let JavaScript know that we have successfully found  GPS location

        // Use serialized method call to invoke JavaScript
        invokeJavaScript("onLocation", loc);

        // Could invoke the JavaScript object directly
        //runJavaScriptUIThread("bridgeFromJava.onLocation(" + loc + ")");
    }

    /**
     * Failure callback for finding user location
     */
    @Override
    public void onLocationFailed() {
        Log.i(TAG, "Can not find location of user");

        // Let JavaScript know that we have failed to find GPS location

        // Use serialized method call to invoke JavaScript
        invokeJavaScript("onLocationFailed");

        // Could invoke the JavaScript object directly
        //runJavaScriptUIThread("bridgeFromJava.onLocationFailed()");
    }

    /* Invoking JavaScript */
    public void invokeJavaScript(String method) {
        invokeJavaScript(method, "");
    }

    /**
     * Builds our serialized message to pass to JavaScript.
     * The serialized message contains a method name and an array of parameters.
     * @param method        name of method to invoke in JavaScript
     * @param parameters    list of parameters to use for method invocation
     */
    public void invokeJavaScript(String method, String parameters) {
        String jsCall = JS_INVOKE_CALL + "{ method: \"" + method + "\", parameters: [" + parameters + "] } )";
        Log.e(TAG, jsCall );
        runJavaScriptUIThread(jsCall);
    }

    /**
     * Invokes JavaScript code on UI thread through our WebView by
     * using evaluateJavascript() or loadUrl()
     * @param jsCall
     */
    public void runJavaScriptUIThread(final String jsCall) {
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                // Null check incase the view gets killed before the JS can execute.
                if (webview != null) {
                    // KitKat+ requires evaluateJavascript(...) instead of loadUrl("javascript: ...")
                    // https://developer.android.com/guide/webapps/migrating.html
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        try {
                            webview.evaluateJavascript(jsCall, null);
                        } catch (Exception e) {
                            Log.e(TAG, "Exception in evaluateJavascript. Device not supported. " + e.toString());
                        }
                    } else {
                        try {
                            webview.loadUrl("javascript:" + jsCall);
                        } catch (Exception e) {
                            Log.e(TAG, "Exception in loadUrl. Device not supported. " + e.toString());
                        }
                    }
                }
            }
        });
    }

    /**
     * Runs task on android main UI thread
     * @param task      task to run on main thread
     */
    private void runOnMainThread(Runnable task) {
        // if we're already on main thread, just run immediately, otherwise
        // queue up execution
        if (Looper.myLooper() == Looper.getMainLooper()) {
            task.run();
        } else {
            Handler mainHandler = new Handler(Looper.getMainLooper());
            mainHandler.post(task);
        }
    }
}
