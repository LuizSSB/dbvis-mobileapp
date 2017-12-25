package br.unesp.amoraes.dbvis.logic.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class PositionUpdaterRest extends AsyncTask<Integer, Long, Long> {
	private Context context;
	public PositionUpdaterRest(Context context, String server, String deviceName, String token){
		this.context = context;
		this.deviceName = deviceName;
		this.token = token;
		this.server = server;
	}
	
	private static final String TAG = "DBVis::PositionUpdaterRest";
	
	private final String URL_UPDATE_POSITION = "/devices/";
	private final String PARAM_DEVICE_NAME = "deviceName";
	private final String PARAM_TOKEN = "token";
	
	private String deviceName;
	private String token;
	private String server;
	
	
	@Override
	protected void onPostExecute(Long p) {
		Log.i(TAG, "Position updated");	
	}

	@Override
	protected Long doInBackground(Integer... params) {
		int left   = params[0];
		int top    = params[1];
		int right  = params[2];
		int bottom = params[3];
		
		DefaultHttpClient client = new DefaultHttpClient();
		try{
			String url = ServerConnectionRest.PROTOCOL+server+":"+ServerConnectionRest.PORT
					+URL_UPDATE_POSITION
					+"?"+PARAM_DEVICE_NAME+"="+deviceName+"&"+PARAM_TOKEN+"="+token;
			HttpPut req = new HttpPut(url);      
			req.addHeader("Accept", "text/plain");
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("left", left+""));
	        nameValuePairs.add(new BasicNameValuePair("top", top+""));
	        nameValuePairs.add(new BasicNameValuePair("right", right+""));
	        nameValuePairs.add(new BasicNameValuePair("bottom", bottom+""));
	        req.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			client.execute(req);
		}catch(Exception e){
			Log.e(TAG, e.getMessage(), e);
		}
		return 1L;
	}

}
