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
import br.unesp.amoraes.dbvis.userdata.UserChart;
import br.unesp.amoraes.dbvis.userdata.UserContents;
import br.unesp.amoraes.dbvis.userdata.UserText;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class UserContentDownloaderRest extends AsyncTask<String, Long, Long> {
	private Context context;
	
private static final String TAG = "DBVis::UserContentDownloaderRest";
	
	private final String URL_DOWNLOAD_SUFIX  = "/download";
	private final String PARAM_DEVICE_NAME = "deviceName";
	private final String PARAM_TOKEN = "token";
	private final String URL_USER_CONTENTS_CHARTS = "/userContents/charts";
	
	private String deviceName;
	private String token;
	private String server;
	private boolean firstRun = false;
	private static long timestamp;
	
	public UserContentDownloaderRest(Context context, String server, String deviceName, String token, boolean firstRun){
		this.context = context;
		this.server = server;
		this.deviceName = deviceName;
		this.token = token;
		this.firstRun = firstRun;
		
		
	}
	
	@Override
	protected void onPostExecute(Long p) {
		Log.i(TAG, "New user content verification completed");
	}

	@Override
	protected Long doInBackground(String... params) {
		
		try {
			if(firstRun){
				timestamp = 0;
			}
			String xml = ServerConnectionRest.getInstance().getUsersTexts(timestamp);
			long t = ServerConnectionRest.getInstance().getTimestamp();
			//LER O XML E ATUALIZAR OS DADOS NO VISUALIZATION.GETUSERCONTENTS
			XmlPullParserFactory pullParserFactory;
			try{
				pullParserFactory = XmlPullParserFactory.newInstance();
				XmlPullParser parser = pullParserFactory.newPullParser();
				InputStream in_s = new ByteArrayInputStream(xml.getBytes());
				parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
				parser.setInput(in_s, null);
				parseXMLUserText(parser);
			}catch(XmlPullParserException e){
				Log.e(TAG, e.getMessage());
			}catch(Exception e){
				Log.e(TAG, e.getMessage());
			}
			
			xml = ServerConnectionRest.getInstance().getUsersCharts(timestamp);
			//LER O XML E ATUALIZAR OS DADOS NO VISUALIZATION.GETUSERCONTENTS
			try{
				pullParserFactory = XmlPullParserFactory.newInstance();
				XmlPullParser parser = pullParserFactory.newPullParser();
				InputStream in_s = new ByteArrayInputStream(xml.getBytes());
				parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
				parser.setInput(in_s, null);
				parseXMLUserChart(parser);
			}catch(XmlPullParserException e){
				Log.e(TAG, e.getMessage());
			}catch(Exception e){
				Log.e(TAG, e.getMessage());
			}
			
			try {
				Thread.sleep(KGlobal.USER_CONTENT_UPDATE_INTERVAL);
			} catch (InterruptedException e) {
				Log.e(TAG, e.getMessage());
			}
			timestamp = t;
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
		return 1L;
	}
	
	private void parseXMLUserText(XmlPullParser parser) throws XmlPullParserException,IOException
	{
		ArrayList<UserText> texts = null;
        int eventType = parser.getEventType();
        UserText currentText = null;

        while (eventType != XmlPullParser.END_DOCUMENT){
            String name = null;
            switch (eventType){
                case XmlPullParser.START_DOCUMENT:
                	texts = new ArrayList();
                    break;
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    if (name.equals("userText")){
                        currentText = new UserText();
                    } else if (currentText != null){
                        if (name.equals("x")){
                            currentText.setX(Integer.parseInt(parser.nextText()));
                        } else if (name.equals("y")){
                        	currentText.setY(Integer.parseInt(parser.nextText()));
                        } else if (name.equals("text")){
                        	currentText.setText(parser.nextText());
                        } else if (name.equals("date")){
                        	currentText.setDate(new Date(Long.parseLong(parser.nextText())));
                        } else if (name.equals("deviceName")){
                        	currentText.setDeviceName(parser.nextText());
                        } else if (name.equals("deleted")){
                        	currentText.setDeleted(Boolean.parseBoolean(parser.nextText()));
                        } else if (name.equals("id")){
                        	currentText.setId(Integer.parseInt(parser.nextText()));
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    if (name.equalsIgnoreCase("userText") && currentText != null){
                    	texts.add(currentText);
                    } 
            }
            eventType = parser.next();
        }
        
        if(texts.size()>0){
        	UserContents uc = Visualization.getInstance().getUserContents();
        	UserContents ouc = Visualization.getInstance().getOtherUsersContents();
        	for(UserText ut : texts){
        		if(ut.isDeleted() && !ut.getDeviceName().equals(deviceName)){
        			ouc.getTexts().remove(ut.getId());
        		}else if(!ut.getDeviceName().equals(ServerConnectionRest.getInstance().getDeviceName())){
        			ouc.getTexts().put(ut.getId(), ut);
        		}else if(firstRun && ut.getDeviceName().equals(deviceName) && ut.isDeleted() == false){
        			uc.getTexts().put(ut.getId(), ut);
        		}
        	}
        }
        if(texts.size()>0)
        	Log.i(TAG, "Users texts updated: "+texts.size());
        
	}
	
	private void parseXMLUserChart(XmlPullParser parser) throws XmlPullParserException,IOException
	{
		ArrayList<UserChart> charts = null;
        int eventType = parser.getEventType();
        UserChart currentChart = null;

        while (eventType != XmlPullParser.END_DOCUMENT){
            String name = null;
            switch (eventType){
                case XmlPullParser.START_DOCUMENT:
                	charts = new ArrayList();
                    break;
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    if (name.equals("userChart")){
                        currentChart = new UserChart();
                    } else if (currentChart != null){
                        if (name.equals("axisX")){
                            currentChart.setAxisX(parser.nextText());
                        } else if (name.equals("axisY")){
                        	currentChart.setAxisY(parser.nextText());
                        } else if (name.equals("axisZ")){
                        	currentChart.setAxisZ(parser.nextText());
                        } else if (name.equals("type")){
                        	currentChart.setType(Integer.parseInt(parser.nextText()));
                        } else if (name.equals("text")){
                        	currentChart.setText(parser.nextText());
                        } else if (name.equals("date")){
                        	currentChart.setDate(new Date(Long.parseLong(parser.nextText())));
                        } else if (name.equals("deviceName")){
                        	currentChart.setDeviceName(parser.nextText());
                        } else if (name.equals("deleted")){
                        	currentChart.setDeleted(Boolean.parseBoolean(parser.nextText()));
                        } else if (name.equals("id")){
                        	currentChart.setId(Integer.parseInt(parser.nextText()));
                        } else if (name.equals("nodesInfo")){
                        	currentChart.setNodesInfo(parser.nextText());
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    if (name.equalsIgnoreCase("userChart") && currentChart != null){
                    	charts.add(currentChart);
                    	if(currentChart.isDeleted() == false){
	                    	//download the preview image
	                    	String url = ServerConnectionRest.PROTOCOL+server+":"+ServerConnectionRest.PORT
	        						+URL_USER_CONTENTS_CHARTS+"/"+currentChart.getId()+","+currentChart.getDeviceName()+URL_DOWNLOAD_SUFIX
	        						+"?"+PARAM_DEVICE_NAME+"="+deviceName+"&"+PARAM_TOKEN+"="+token;
	        				HttpGet req = new HttpGet(url);      
	        				req.addHeader("Accept", "image/png");
	        				DefaultHttpClient client = new DefaultHttpClient();
	        				HttpResponse res = client.execute(req);
	        				if(res.getStatusLine().getStatusCode() == 200){
	        					HttpEntity entity = res.getEntity();
	        					if (entity != null) {
	        			            InputStream inputStream = null;
	        			            try {
	        			                inputStream = entity.getContent(); 
	        			                final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
	        			                currentChart.setImage(bitmap);             
	        			            } finally {
	        			                if (inputStream != null) {
	        			                    inputStream.close();  
	        			                }
	        			                entity.consumeContent();
	        			            }
	        			        }					
	        				}
                    	}
                    	
                    } 
            }
            eventType = parser.next();
        }
        boolean newOtherUserChart = false;
        if(charts.size()>0){
        	UserContents uc = Visualization.getInstance().getUserContents();
        	UserContents ouc = Visualization.getInstance().getOtherUsersContents();
        	for(UserChart c : charts){
        		if(c.isDeleted() && !c.getDeviceName().equals(deviceName)){
        			ouc.getCharts().remove(c.getId());
        		}else if(!c.getDeviceName().equals(deviceName)){
        			ouc.getCharts().put(c.getId(), c);
        			newOtherUserChart = true;
        		}else if(firstRun && c.getDeviceName().equals(deviceName) && c.isDeleted() == false){
        			uc.getCharts().put(c.getId(), c);
        		}
        	}
        }
        if(charts.size()>0)
        	Log.i(TAG, "Users charts updated: "+charts.size());
        if(newOtherUserChart){
        	ServerConnectionRest.getInstance().setHasNewOtherUsersCharts(true);
        	
        }
	}
	

}
