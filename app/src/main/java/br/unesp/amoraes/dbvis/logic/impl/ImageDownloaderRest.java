package br.unesp.amoraes.dbvis.logic.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class ImageDownloaderRest extends AsyncTask<String, Long, Long> {
	private Context context;
	private long timestamp;
	
	public ImageDownloaderRest(Context context, String server, String deviceName, String token){
		this.context = context;
		this.deviceName = deviceName;
		this.token = token;
		this.server = server;
	}
	
	private static final String TAG = "DBVis::ImageDownloaderRest";
	
	private final String URL_DOWNLOAD_PREFIX = "/images";
	private final String URL_DOWNLOAD_SUFIX  = "/download";
	private final String PARAM_DEVICE_NAME = "deviceName";
	private final String PARAM_TOKEN = "token";
	
	private String deviceName;
	private String token;
	private String server;
	
	private List<Bitmap> bitmaps = new LinkedList<Bitmap>();
	
	@Override
	protected void onPostExecute(Long p) {
		Log.i(TAG, "Download images finished");
		ServerConnectionRest.getInstance().setmBitmapZoom(bitmaps.get(0));
		ServerConnectionRest.getInstance().setCurrentImageTimestamp(timestamp);
	}

	@Override
	protected Long doInBackground(String... params) {
		List<String> images = new ArrayList<String>();
		//full0 and full2 are not in use
		images.add("full1");
		
		DefaultHttpClient client = new DefaultHttpClient();
		for(String imageId : images){
			try{
				String url = ServerConnectionRest.PROTOCOL+server+":"+ServerConnectionRest.PORT
						+URL_DOWNLOAD_PREFIX+"/"+imageId+URL_DOWNLOAD_SUFIX
						+"?"+PARAM_DEVICE_NAME+"="+deviceName+"&"+PARAM_TOKEN+"="+token;
				HttpGet req = new HttpGet(url);      
				req.addHeader("Accept", "image/png");
				HttpResponse res = client.execute(req);
				if(res.getStatusLine().getStatusCode() == 200){
					HttpEntity entity = res.getEntity();
					if (entity != null) {
			            InputStream inputStream = null;
			            try {
			                inputStream = entity.getContent(); 
			                final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
			                bitmaps.add(bitmap);              
			            } finally {
			                if (inputStream != null) {
			                    inputStream.close();  
			                }
			                entity.consumeContent();
			            }
			        }					
				}
				this.timestamp = ServerConnectionRest.getInstance().getTimestamp();
			}catch(Exception e){
				Log.e(TAG, e.getMessage(), e);
			}
		}
		return 1L;
	}

}
