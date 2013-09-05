package com.example.fragment_assignment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

public class AsyncDownloader extends AsyncTaskLoader<Void> {

	public static final String TAG = "AsyncDownloader";

	private AtomicBoolean cancelled = new AtomicBoolean(false);

	public AsyncDownloader(Context context) {
		super(context);
		instance = this;
	}

	public static AsyncDownloader instance;

	static AsyncDownloader getInstance() {
		return instance;
	}

	public static final String FLICKR_URL = "http://api.flickr.com/services/feeds/groups_pool.gne?id=1614613@N24&lang=en-us&format=json&jsoncallback=?";
	public static final int UPDATE_PROGRESS = 8344;
	private ResultReceiver resultReceiver;

	public void setResultReceiver(ResultReceiver resultReceiver) {
		this.resultReceiver = resultReceiver;
	}

	@Override
	public Void loadInBackground() {
		int total = 0;
		try {
			InputStream is = getJsonResponse(FLICKR_URL);
			String jsonString = getJsonString(is);
			ArrayList<String> mediaUrls = getMediaUrls(jsonString);

			int index = 1;
			total = mediaUrls.size();
			for (String mediaUrl : mediaUrls) {
				synchronized (this) {
					if (cancelled.get()) {
						Log.v(TAG, "" + cancelled.get());
						break;
					}

					File file = getFile(mediaUrl);
					if (!file.exists()) {
						URL url = new URL(mediaUrl);
						URLConnection connection = url.openConnection();
						connection.connect();

						// download the file
						InputStream input = new BufferedInputStream(url.openStream());
						OutputStream output = new FileOutputStream(file);

						byte data[] = new byte[1024];
						int count;
						while ((count = input.read(data)) != -1) {
							output.write(data, 0, count);
						}

						output.flush();
						output.close();
						input.close();

						Log.v(TAG, "downloading image# " + index);
					}
					// publishing the progress....
					Bundle resultData = new Bundle();
					resultData.putInt("progress", (int) (index++ * 100 / total));
					resultReceiver.send(UPDATE_PROGRESS, resultData);

					wait(500);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		 Bundle resultData = new Bundle();
		 resultData.putInt("progress", 100);
		 resultReceiver.send(UPDATE_PROGRESS, resultData);

		return null;
	}

	private String getJsonString(InputStream is) throws IOException {
		String jsonString = Utils.convertStreamToString(is);
		return jsonString.substring(jsonString.indexOf('(') + 1, jsonString.length() - 1);
	}

	private ArrayList<String> getMediaUrls(String jsonString) throws IOException, JSONException {
		ArrayList<String> mediaUrls = new ArrayList<String>();
		JSONArray jsonArray = null;
		JSONObject jsonObject = null;

		jsonObject = new JSONObject(jsonString);
		jsonArray = jsonObject.getJSONArray("items");
		for (int i = 0; i < jsonArray.length(); i++) {
			String mediaUrl = jsonArray.getJSONObject(i).getJSONObject("media").getString("m");
			Log.v(TAG, "url " + i + ": " + mediaUrl);
			mediaUrls.add(mediaUrl);
		}
		return mediaUrls;
	}

	private InputStream getJsonResponse(String urlString) throws IOException {
		InputStream is = null;
		try {
			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(15000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			// Starts the query
			conn.connect();
			// int response = conn.getResponseCode();
			return conn.getInputStream();

			// Makes sure that the InputStream is closed after the app is
			// finished using it.
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	private File getFile(String mediaUrl) {
		String root = Environment.getExternalStorageDirectory().toString();
		File myDir = new File(root + "/saved_images");
		myDir.mkdirs();
		Random generator = new Random();
		int n = 10000;
		n = generator.nextInt(n);
		String fname = mediaUrl.substring(mediaUrl.lastIndexOf("/") + 1, mediaUrl.length());
		return new File(myDir, fname);
	}

	public synchronized void cancelLoading() {
		cancelLoad();
		cancelled.set(true);
		notifyAll();
		Log.v(TAG, "cancelled");
	}

}
