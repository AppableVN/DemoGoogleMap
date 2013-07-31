package com.example.mapdemo;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;
import android.util.Log;

public class ReverseGeoCodeTask extends AsyncTask<Void, Void, String>{

	public interface onReverseGeoCodeTaskListener{
		public void onPreLoadListState();
		public void onLoadListStateSuccess(String response);
		public void onLoadListStateFailure();
	}
	
	private onReverseGeoCodeTaskListener listener;
	
	private String lat;
	private String lng;
	
	public ReverseGeoCodeTask(String lat, String lng){
		this.lat = lat;
		this.lng = lng;
	}
	
	public void setOnReverseGeoCodeTaskListener(onReverseGeoCodeTaskListener listener){
		this.listener = listener;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		listener.onPreLoadListState();
	}
	
	@Override
	protected String doInBackground(Void... arg0) {
		String response = "";
		try {
			String url = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + lat + "," + lng + "&sensor=true&types=administrative_area_level_1";
			Log.v("ReverseGeoCodeTask", "URL = " + url);
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(url);
			if (httpClient != null && httpGet != null) {
				HttpResponse httpResponse = httpClient.execute(httpGet);
				HttpEntity entity = httpResponse.getEntity();
				response = EntityUtils.toString(entity);
			}
			httpGet.abort();
			httpGet = null;
			httpClient = null;
		} catch (Exception e) {
			Log.e("ReverseGeoCodeTask", e.toString());
			response = "";
		}
		return response;
	}
	
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		if(listener != null){
			if(result != null && result.compareTo("") != 0){
				listener.onLoadListStateSuccess(result);
			} else {
				listener.onLoadListStateFailure();
			}
		}
		
	}
}
