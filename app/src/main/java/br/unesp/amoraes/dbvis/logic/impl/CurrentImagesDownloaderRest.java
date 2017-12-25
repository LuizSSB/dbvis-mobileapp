package br.unesp.amoraes.dbvis.logic.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import br.unesp.amoraes.dbvis.KGlobal;
import br.unesp.amoraes.dbvis.logic.Visualization;
import br.unesp.amoraes.dbvis.userdata.CurrentImage;
import br.unesp.amoraes.dbvis.userdata.UserChart;
import br.unesp.amoraes.dbvis.userdata.UserContents;
import br.unesp.amoraes.dbvis.userdata.UserText;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class CurrentImagesDownloaderRest extends AsyncTask<String, Long, Long> {
	private Context context;
	
private static final String TAG = "DBVis::UserContentDownloaderRest";
	
	private final String URL_DOWNLOAD_PREFIX = "/userContents/charts/";
	private final String URL_DOWNLOAD_SUFIX  = "/download";
	private final String PARAM_DEVICE_NAME = "deviceName";
	private final String PARAM_TOKEN = "token";
	private final String URL_USER_CONTENTS_TEXTS = "/userContents/texts";
	private final String URL_USER_CONTENTS_CHARTS = "/userContents/charts";
	
	private String deviceName;
	private String token;
	private String server;
	private static long timestamp = 0;
	
	public CurrentImagesDownloaderRest(Context context, String server, String deviceName, String token){
		this.context = context;
		this.deviceName = deviceName;
		this.token = token;
		
	}
	
	@Override
	protected void onPostExecute(Long p) {
		Log.i(TAG, "Current images verification completed");
	}

	@Override
	protected Long doInBackground(String... params) {
		
		try {
			long t = ServerConnectionRest.getInstance().getTimestamp();
			String xml = ServerConnectionRest.getInstance().getCurrentImages();
			
			//LER O XML E ATUALIZAR OS DADOS NO VISUALIZATION.GETUSERCONTENTS
			XmlPullParserFactory pullParserFactory;
			try{
				pullParserFactory = XmlPullParserFactory.newInstance();
				XmlPullParser parser = pullParserFactory.newPullParser();
				InputStream in_s = new ByteArrayInputStream(xml.getBytes());
				parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
				parser.setInput(in_s, null);
				parseXMLCurrentImages(parser);
				if(Visualization.getInstance().getCurrentImages().get(0).getTime() > ServerConnectionRest.getInstance().getCurrentImageTimestamp()){
					ServerConnectionRest.getInstance().getCurrentImages();
					ServerConnectionRest.getInstance().setCurrentImageTimestamp(ServerConnectionRest.getInstance().getTimestamp());
				}
			}catch(XmlPullParserException e){
				Log.e(TAG, e.getMessage());
			}catch(Exception e){
				Log.e(TAG, e.getMessage());
			}
			timestamp = t;
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
		return 1L;
	}
	
	private void parseXMLCurrentImages(XmlPullParser parser) throws XmlPullParserException,IOException
	{
		ArrayList<CurrentImage> images = null;
        int eventType = parser.getEventType();
        CurrentImage currentImage = null;

        while (eventType != XmlPullParser.END_DOCUMENT){
            String name = null;
            switch (eventType){
                case XmlPullParser.START_DOCUMENT:
                	images = new ArrayList();
                    break;
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    if (name.equals("image")){
                        currentImage = new CurrentImage();
                    } else if (currentImage != null){
                        if (name.equals("id")){
                            currentImage.setId(parser.nextText());
                        } else if (name.equals("url")){
                        	currentImage.setUrl(parser.nextText());
                        } else if (name.equals("time")){
                        	currentImage.setTime(Long.parseLong(parser.nextText()));
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    if (name.equalsIgnoreCase("image") && currentImage != null){
                    	images.add(currentImage);
                    } 
            }
            eventType = parser.next();
        }
        
        if(images.size()>0){
        	Visualization.getInstance().getCurrentImages().clear();
        	for(CurrentImage ci : images){
        		Visualization.getInstance().getCurrentImages().add(ci);
        	}
        }
    	Log.i(TAG, "Current images updated");
        
	}

}
