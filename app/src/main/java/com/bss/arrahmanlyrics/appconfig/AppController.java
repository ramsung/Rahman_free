package com.bss.arrahmanlyrics.appconfig;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bss.arrahmanlyrics.utility.mediaCache;
import com.danikula.videocache.HttpProxyCacheServer;
import com.google.firebase.FirebaseApp;

public class AppController extends Application {

	public static final String TAG = AppController.class.getSimpleName();

	private HttpProxyCacheServer proxy;
	private RequestQueue mRequestQueue;
	private static String APP_LINK = "https://arrahmanlyrics.beyonitysoftwares.in";
	private static AppController mInstance;

	@Override
	public void onCreate() {
		super.onCreate();
		StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
		StrictMode.setVmPolicy(builder.build());
		FirebaseApp.initializeApp(getApplicationContext());
		mInstance = this;
	}

	public static synchronized AppController getInstance() {
		return mInstance;
	}

	public RequestQueue getRequestQueue() {
		if (mRequestQueue == null) {
			mRequestQueue = Volley.newRequestQueue(getApplicationContext());
		}

		return mRequestQueue;
	}

	public <T> void addToRequestQueue(Request<T> req, String tag) {
		req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
		getRequestQueue().add(req);
		Log.d(TAG, "addToRequestQueue: "+req);
	}

	public <T> void addToRequestQueue(Request<T> req) {
		req.setTag(TAG);
		getRequestQueue().add(req);
	}

	public void cancelPendingRequests(Object tag) {
		if (mRequestQueue != null) {
			mRequestQueue.cancelAll(tag);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	public static HttpProxyCacheServer getProxy(Context context) {
		AppController app = (AppController) context.getApplicationContext();
		return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
	}

	private HttpProxyCacheServer newProxy() {
		return new HttpProxyCacheServer.Builder(this)
				.maxCacheFilesCount(50)
				.cacheDirectory(mediaCache.getVideoCacheDir(this))
				.build();
	}

	public static String getAppLink(){
		return APP_LINK;
	}
}