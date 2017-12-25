package br.unesp.amoraes.dbvis.logic.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import br.unesp.amoraes.dbvis.ExplorerActivity;
import br.unesp.amoraes.dbvis.KGlobal;
import br.unesp.amoraes.dbvis.exception.AlreadyConnectedException;
import br.unesp.amoraes.dbvis.exception.InvalidPasswordException;
import br.unesp.amoraes.dbvis.exception.NotConnectedException;
import br.unesp.amoraes.dbvis.logic.Metadata;
import br.unesp.amoraes.dbvis.logic.ServerConnection;
import br.unesp.amoraes.dbvis.logic.Visualization;
import br.unesp.amoraes.dbvis.userdata.CurrentImage;
import br.unesp.amoraes.dbvis.userdata.UserChart;
import br.unesp.amoraes.dbvis.userdata.UserContents;
import br.unesp.amoraes.dbvis.userdata.UserText;

public class ServerConnectionRest extends ServerConnection{

	private static final String TAG = "DBVis::ServerConnectionRest";
	
	private static ServerConnectionRest instance;
	
	private String server;
	private String deviceName;
	private String token;
	private boolean hasNewOtherUsersCharts = false;
	
	//nice constants
	public static final String PROTOCOL = "http://";
	public static final String PORT = "8081";
	private final int SOCKET_PORT = 8082;
	private final String URL_DEVICES = "/devices";
	private final String URL_NODES_POINT = "/nodes/point";
	private final String URL_NODES_AREA = "/nodes/area";
	private final String URL_SELECTIONS_POINT = "/selections/point";
	private final String URL_SELECTIONS_AREA = "/selections/area";
	private final String URL_USER_CONTENTS_TIMESTAMP = "/userContents/timestamp";
	private final String URL_METADATA = "/metadata";
	private final String URL_IMAGES_PREFIX = "/images";
	private final String URL_USER_CONTENTS_TEXTS = "/userContents/texts";
	private final String URL_USER_CONTENTS_CHARTS = "/userContents/charts";
	public static final String URL_DOWNLOAD_SUFIX = "/download";
	private final String PARAM_DEVICE_NAME = "deviceName";
	private final String PARAM_PASSWORD = "password";
	private final String PARAM_TOKEN = "token";
	private final String PARAM_X = "x";
	private final String PARAM_Y = "y";
	private final String PARAM_WIDTH = "width";
	private final String PARAM_HEIGHT = "height";
	private final String PARAM_TIMESTAMP = "timestamp";
	private final String PARAM_TEXT = "text";
	private final String PARAM_FILE = "file";
	private final String PARAM_NODES_INFO = "nodesInfo";
	private final String PARAM_AXIS_X = "axisX";
	private final String PARAM_AXIS_Y = "axisY";
	private final String PARAM_AXIS_Z = "axisZ";
	private final String PARAM_TYPE = "type";
	private final String TEXT_XML = "text/xml";
	private final String TEXT_PLAIN = "text/plain";
	private final String MULTIPART_FORM_DATA = "multipart/form-data";
	private final String ACCEPT = "Accept";
	
	
	
	
	
	private Bitmap mBitmapZoom;
	private long currentImageTimestamp;
	
	private Socket socket;
	
	private ServerConnectionRest(){
		
	}
	public static ServerConnectionRest getInstance(){	
		if(instance == null)
			instance = new ServerConnectionRest();
		return instance;
	}
	
	
	private boolean connected = false;

	int posX;
	int posY;
	int posWidth;
	int posHeight;
	boolean posChanged;
	Thread posThread = null;
	Thread usersContentsThread = null;
	Thread currentImageThread = null;
	
	
	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public boolean connect(String server, String password, String deviceName) throws AlreadyConnectedException, InvalidPasswordException {
		if(connected){
			throw new AlreadyConnectedException();
		}else{
			try {
				this.server = server;
				this.deviceName = deviceName;
				DefaultHttpClient client = new DefaultHttpClient();
				HttpPost req = new HttpPost(PROTOCOL+this.server+":"+PORT+URL_DEVICES);
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		        nameValuePairs.add(new BasicNameValuePair(PARAM_DEVICE_NAME, deviceName));
		        nameValuePairs.add(new BasicNameValuePair(PARAM_PASSWORD, password));
		        req.setEntity(new UrlEncodedFormEntity(nameValuePairs));		        
				req.addHeader(ACCEPT, TEXT_PLAIN);
				HttpResponse res = client.execute(req);
				if(res.getStatusLine().getStatusCode() == 403){
					throw new InvalidPasswordException();
				}else if(res.getStatusLine().getStatusCode() == 200){
					InputStream in = res.getEntity().getContent();
					int i = -1;
			        this.token = "";
			        byte[] buf = new byte[1024];
			        while ((i = in.read(buf)) > -1)
			            this.token += new String(buf, 0, i);
			        Log.i(TAG, "Connected on "+server+". Device name = "+deviceName+", token = "+token);
			        connected = true;
			        downloadCurrentImages(true);
			        currentImageThread = new Thread(new ImageUpdateThread());
			        currentImageThread.start();
			        posThread = new Thread(new PositionUpdateThread());
			        posThread.start();
			        usersContentsThread = new Thread(new UsersContentsUpdateThread());
			        usersContentsThread.start();
			        getMetadata();
			        //open the socket client
			        if(this.socket == null){
						try {
							this.socket = new Socket(server,SOCKET_PORT);
							socket.setKeepAlive(true);
							
						} catch (UnknownHostException e) {
							Log.e(TAG, e.getMessage(), e);
						} catch (IOException e) {
							Log.e(TAG, e.getMessage(), e);
						}
					}
					return connected;
				}
			} catch (InvalidPasswordException e){
				throw e;
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(),e);
			}			
		}
		connected = false;
		return connected;
	}

	public void downloadCurrentImages(boolean waitFinish) {
		ImageDownloaderRest downloader = new ImageDownloaderRest(null,server,deviceName,token);
		downloader.execute(this.server, this.deviceName, this.token);
		if(waitFinish){
			try {
				downloader.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public Bitmap getFullDisplayImage() throws NotConnectedException {
		if(!connected)
			throw new NotConnectedException();
		return mBitmapZoom;
	}
	
	@Override
	public void disconnect() throws NotConnectedException{
		if(!connected)
			throw new NotConnectedException();
		else{
			try{
				DefaultHttpClient client = new DefaultHttpClient();
				HttpDelete req = new HttpDelete(PROTOCOL+this.server+":"+PORT+URL_DEVICES
						+"?"+PARAM_DEVICE_NAME+"="+deviceName+"&"+PARAM_TOKEN+"="+token);
				req.addHeader(ACCEPT, TEXT_PLAIN);
				HttpResponse res = client.execute(req);
				if(res.getStatusLine().getStatusCode() == 200){
					connected = false;
					deviceName = "";
					token = "";
					server = "";
					posThread.interrupt();
					posThread = null;
					usersContentsThread.interrupt();
					usersContentsThread = null;
					currentImageThread.interrupt();
					currentImageThread = null;
				}
				socket.close();
			}catch(Exception e){
				Log.e(TAG, e.getMessage());
			}
		}
	}

	public void setmBitmapZoom(Bitmap mBitmapZoom1) {
		this.mBitmapZoom = mBitmapZoom1;
	}

	@Override
	public void updatePosition(int x, int y, int width, int height) {
		if(x != posX || y != posY || width != posWidth || height != posHeight){
			this.posChanged = true;
			this.posX = x;
			this.posY = y;
			this.posWidth = width;
			this.posHeight = height;
		}else{
			this.posChanged = false;
		}
	}
	@Override
	public String getNodeByPoint(int x, int y) throws NotConnectedException {
		if(!connected)
			throw new NotConnectedException();
		else{
			try{
				DefaultHttpClient client = new DefaultHttpClient();
				HttpGet req = new HttpGet(PROTOCOL+this.server+":"+PORT+URL_NODES_POINT
						+"?"+PARAM_DEVICE_NAME+"="+deviceName+"&"+PARAM_TOKEN+"="+token
						+"&"+PARAM_X+"="+x+"&"+PARAM_Y+"="+y);
				
				req.addHeader(ACCEPT, TEXT_XML);
				HttpResponse res = client.execute(req);
				if(res.getStatusLine().getStatusCode() == 200){
					InputStream in = res.getEntity().getContent();
					int i = -1;
			        String nodeInfo = "";
			        byte[] buf = new byte[1024];
			        while ((i = in.read(buf)) > -1)
			            nodeInfo += new String(buf, 0, i);
			        return nodeInfo;
				}
			}catch(Exception e){
				Log.e(TAG, e.getMessage());
			}
		}
		return null;
	}
	@Override
	public String getNodeByArea(int x, int y, int width, int height)
			throws NotConnectedException {
		if(!connected)
			throw new NotConnectedException();
		else{
			try{
				DefaultHttpClient client = new DefaultHttpClient();
				HttpGet req = new HttpGet(PROTOCOL+this.server+":"+PORT+URL_NODES_AREA
						+"?"+PARAM_DEVICE_NAME+"="+deviceName+"&"+PARAM_TOKEN+"="+token
						+"&"+PARAM_X+"="+x+"&"+PARAM_Y+"="+y
						+"&"+PARAM_WIDTH+"="+width+"&"+PARAM_HEIGHT+"="+height);
				
				req.addHeader(ACCEPT, TEXT_XML);
				HttpResponse res = client.execute(req);
				if(res.getStatusLine().getStatusCode() == 200){
					InputStream in = res.getEntity().getContent();
					int i = -1;
			        String nodeInfo = "";
			        byte[] buf = new byte[1024];
			        while ((i = in.read(buf)) > -1)
			            nodeInfo += new String(buf, 0, i);
			        return nodeInfo;
				}
			}catch(Exception e){
				Log.e(TAG, e.getMessage());
			}
		}
		return null;
	}
	
	class PositionUpdateThread implements Runnable {
		
		@Override
		public void run() {
			while(ServerConnectionRest.getInstance().isConnected()){
				if(ServerConnectionRest.instance.posChanged){
					PositionUpdaterSocket positionUpdater = new PositionUpdaterSocket(null, deviceName, token, socket);
					positionUpdater.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,ServerConnectionRest.instance.posX,ServerConnectionRest.instance.posY,ServerConnectionRest.instance.posWidth,ServerConnectionRest.instance.posHeight);
				}
				try {
					Thread.sleep(KGlobal.USER_POSITION_UPDATE_INTERVAL);
				} catch (InterruptedException e) {
					Log.e(TAG, e.getMessage());
				}
			}			
		}
		
	}
	
	class ImageUpdateThread implements Runnable {
		
		@Override
		public void run() {
			while(ServerConnectionRest.getInstance().isConnected()){
				CurrentImagesDownloaderRest downloader = new CurrentImagesDownloaderRest(null,server,deviceName,token);
				downloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,server, deviceName, token);
								
				try {
					Thread.sleep(KGlobal.USER_CONTENT_UPDATE_INTERVAL);
				} catch (InterruptedException e) {
					Log.e(TAG, e.getMessage());
				}
			}			
		}
		
		
		
	}
	
	class UsersContentsUpdateThread implements Runnable {
		private boolean firstRun = true;
		@Override
		public void run(){
			while(ServerConnectionRest.getInstance().isConnected()){
				UserContentDownloaderRest downloader = new UserContentDownloaderRest(null,server,deviceName,token, firstRun);
				downloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,server, deviceName, token);
				
				firstRun = false;
				try {
					Thread.sleep(KGlobal.USER_CONTENT_UPDATE_INTERVAL);
				} catch (InterruptedException e) {
					Log.e(TAG, e.getMessage());
				}
			}
		}
		
		
	}

	@Override
	public void selectPoint(int x, int y)  throws NotConnectedException{
		if(!connected)
			throw new NotConnectedException();
		else{
			try{
				DefaultHttpClient client = new DefaultHttpClient();
				HttpPost req = new HttpPost(PROTOCOL+this.server+":"+PORT+URL_SELECTIONS_POINT+"?"+PARAM_DEVICE_NAME+"="+deviceName+"&"+PARAM_TOKEN+"="+token);
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		        nameValuePairs.add(new BasicNameValuePair(PARAM_X, ""+x));
		        nameValuePairs.add(new BasicNameValuePair(PARAM_Y, ""+y));
		        req.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				req.addHeader(ACCEPT, TEXT_XML);
				HttpResponse res = client.execute(req);
				if(res.getStatusLine().getStatusCode() == 200){
					return;
				}
			}catch(Exception e){
				Log.e(TAG, e.getMessage());
			}
		}
		
	}
	@Override
	public void selectArea(int x, int y, int width, int height)  throws NotConnectedException{
		if(!connected)
			throw new NotConnectedException();
		else{
			try{
				DefaultHttpClient client = new DefaultHttpClient();
				HttpPost req = new HttpPost(PROTOCOL+this.server+":"+PORT+URL_SELECTIONS_AREA+"?"+PARAM_DEVICE_NAME+"="+deviceName+"&"+PARAM_TOKEN+"="+token);
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		        nameValuePairs.add(new BasicNameValuePair(PARAM_X, ""+x));
		        nameValuePairs.add(new BasicNameValuePair(PARAM_Y, ""+y));
		        nameValuePairs.add(new BasicNameValuePair(PARAM_WIDTH, ""+width));
		        nameValuePairs.add(new BasicNameValuePair(PARAM_HEIGHT, ""+height));
		        req.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				req.addHeader(ACCEPT, TEXT_XML);
				HttpResponse res = client.execute(req);
				if(res.getStatusLine().getStatusCode() == 200){
					return;
				}
			}catch(Exception e){
				Log.e(TAG, e.getMessage());
			}
		}
	}
	@Override
	public String getDeviceName() {
		return deviceName;
	}
	@Override
	public void addUserText(UserText userText) throws NotConnectedException {
		if(!connected)
			throw new NotConnectedException();
		else{
			try{
				DefaultHttpClient client = new DefaultHttpClient();
				HttpPost req = new HttpPost(PROTOCOL+this.server+":"+PORT+URL_USER_CONTENTS_TEXTS+"?"+PARAM_DEVICE_NAME+"="+deviceName+"&"+PARAM_TOKEN+"="+token);
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		        nameValuePairs.add(new BasicNameValuePair(PARAM_X, ""+userText.getX()));
		        nameValuePairs.add(new BasicNameValuePair(PARAM_Y, ""+userText.getY()));
		        nameValuePairs.add(new BasicNameValuePair(PARAM_TEXT, ""+userText.getText()));
		        req.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				req.addHeader(ACCEPT, TEXT_XML);
				HttpResponse res = client.execute(req);
				if(res.getStatusLine().getStatusCode() == 200){
					InputStream in = res.getEntity().getContent();
					int i = -1;
			        String id = "";
			        byte[] buf = new byte[1024];
			        while ((i = in.read(buf)) > -1)
			            id += new String(buf, 0, i);
			        userText.setId(Integer.parseInt(id));
			        return;
				}
			}catch(Exception e){
				Log.e(TAG, e.getMessage());
			}
		}
	}
	@Override
	public void removeUserText(int id) throws NotConnectedException {
		if(!connected)
			throw new NotConnectedException();
		else{
			try{
				DefaultHttpClient client = new DefaultHttpClient();
				HttpDelete req = new HttpDelete(PROTOCOL+this.server+":"+PORT+URL_USER_CONTENTS_TEXTS
						+"/"+id+"?"+PARAM_DEVICE_NAME+"="+deviceName+"&"+PARAM_TOKEN+"="+token);
				req.addHeader(ACCEPT, TEXT_PLAIN);
				HttpResponse res = client.execute(req);
				if(res.getStatusLine().getStatusCode() == 200){
					return;
				}
			}catch(Exception e){
				Log.e(TAG, e.getMessage());
			}
		}
		
	}
	@Override
	public String getUsersTexts(long timestamp) {
		try{
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet req = new HttpGet(PROTOCOL+this.server+":"+PORT+URL_USER_CONTENTS_TEXTS
					+"?"+PARAM_DEVICE_NAME+"="+deviceName+"&"+PARAM_TOKEN+"="+token
					+"&"+PARAM_TIMESTAMP+"="+timestamp);
			
			req.addHeader(ACCEPT, TEXT_XML);
			HttpResponse res = client.execute(req);
			if(res.getStatusLine().getStatusCode() == 200){
				InputStream in = res.getEntity().getContent();
				int i = -1;
		        String usersTexts = "";
		        byte[] buf = new byte[1024];
		        while ((i = in.read(buf)) > -1)
		            usersTexts += new String(buf, 0, i);
		        return usersTexts;
			}
		}catch(Exception e){
			Log.e(TAG, e.getMessage());
		}
		return null;
		
	}
	
	
	private void getMetadata(){
		try{
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet req = new HttpGet(PROTOCOL+this.server+":"+PORT+URL_METADATA
					+"?"+PARAM_DEVICE_NAME+"="+deviceName+"&"+PARAM_TOKEN+"="+token);
			
			req.addHeader(ACCEPT, TEXT_XML);
			HttpResponse res = client.execute(req);
			if(res.getStatusLine().getStatusCode() == 200){
				InputStream in = res.getEntity().getContent();
				int i = -1;
		        String metadata = "";
		        byte[] buf = new byte[1024];
		        while ((i = in.read(buf)) > -1)
		            metadata += new String(buf, 0, i);
		        
		      //LER O XML E ATUALIZAR OS DADOS NO VISUALIZATION.GETUSERCONTENTS
				XmlPullParserFactory pullParserFactory;
				try{
					pullParserFactory = XmlPullParserFactory.newInstance();
					XmlPullParser parser = pullParserFactory.newPullParser();
					InputStream in_s = new ByteArrayInputStream(metadata.getBytes());
					parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
					parser.setInput(in_s, null);
					parseXMLMetadata(parser);
				}catch(XmlPullParserException e){
					Log.e(TAG, e.getMessage());
				}catch(Exception e){
					Log.e(TAG, e.getMessage());
				}
			}
		}catch(Exception e){
			Log.e(TAG, e.getMessage());
		}
	}
	
	private void parseXMLMetadata(XmlPullParser parser) throws XmlPullParserException,IOException
	{
		Metadata m = Metadata.getInstance();
        int eventType = parser.getEventType();
        
        String currentName = "";
        String currentType = "";
        
        while (eventType != XmlPullParser.END_DOCUMENT){
            String name = null;
            switch (eventType){
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    if (name.equals("id")){
                        m.setIdColumn(parser.nextText());
                    } else if (name.equals("label")){
                    	m.setLabelColumn(parser.nextText());
                    } else if (name.equals("name")){
                    	currentName = parser.nextText();
                    } else if (name.equals("type")){
                    	currentType = parser.nextText();
                    }
                    break;
                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    if (name.equalsIgnoreCase("column")){
                    	m.getColumns().put(currentName, currentType);
                    } 
            }
            eventType = parser.next();
        }
        Log.i(TAG, "Metadata updated.");
	}
	@Override
	public void addUserChart(UserChart userChart) throws NotConnectedException {
		if(!connected)
			throw new NotConnectedException();
		else{
			try{
				DefaultHttpClient client = new DefaultHttpClient();
				HttpPost req = new HttpPost(PROTOCOL+this.server+":"+PORT+URL_USER_CONTENTS_CHARTS);
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				userChart.getImage().compress(Bitmap.CompressFormat.PNG, 100, stream);
				byte[] byteArray = stream.toByteArray();
				stream.close();
				MultipartEntityBuilder builder = MultipartEntityBuilder.create();
				builder.addBinaryBody(PARAM_FILE, byteArray, ContentType.create("image/png"), "image.png");
				builder.addTextBody(PARAM_DEVICE_NAME, deviceName);
				builder.addTextBody(PARAM_TOKEN, token);
				builder.addTextBody(PARAM_TEXT, userChart.getText());
				builder.addTextBody(PARAM_NODES_INFO, userChart.getNodesInfo());
				builder.addTextBody(PARAM_AXIS_X, userChart.getAxisX()+"");
				builder.addTextBody(PARAM_AXIS_Y, userChart.getAxisY()+"");
				builder.addTextBody(PARAM_AXIS_Z, userChart.getAxisZ()+"");
				builder.addTextBody(PARAM_TYPE, userChart.getType()+"");
				builder.setBoundary("----WebKitFormBoundaryp7MA4YWxkTrZu0gW");
				builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				req.setEntity(builder.build());
				req.addHeader(HttpHeaders.CACHE_CONTROL, "no-cache");		
				req.addHeader(ACCEPT, MULTIPART_FORM_DATA);
				req.addHeader(HTTP.CONTENT_TYPE, MULTIPART_FORM_DATA+"; boundary=----WebKitFormBoundaryp7MA4YWxkTrZu0gW");
				HttpResponse res = client.execute(req);
				if(res.getStatusLine().getStatusCode() == 200){
					InputStream in = res.getEntity().getContent();
					int i = -1;
			        String id = "";
			        byte[] buf = new byte[1024];
			        while ((i = in.read(buf)) > -1)
			            id += new String(buf, 0, i);
			        userChart.setId(Integer.parseInt(id));
			        return;
				}
			}catch(Exception e){
				Log.e(TAG, e.getMessage());
			}
		}
		
	}
	@Override
	public Bitmap getUserChartImage(int id) throws NotConnectedException {
		ChartImageDownloaderRest downloader = new ChartImageDownloaderRest(null,server,deviceName,token, id);
		downloader.execute(this.server, this.deviceName, this.token);
		try {
			downloader.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return downloader.getBitmap();		
	}
	@Override
	public String getUsersCharts(long timestamp) {
		try{
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet req = new HttpGet(PROTOCOL+this.server+":"+PORT+URL_USER_CONTENTS_CHARTS
					+"?"+PARAM_DEVICE_NAME+"="+deviceName+"&"+PARAM_TOKEN+"="+token
					+"&"+PARAM_TIMESTAMP+"="+timestamp);
			
			req.addHeader(ACCEPT, TEXT_XML);
			HttpResponse res = client.execute(req);
			if(res.getStatusLine().getStatusCode() == 200){
				InputStream in = res.getEntity().getContent();
				int i = -1;
		        String userCharts = "";
		        byte[] buf = new byte[1024];
		        while ((i = in.read(buf)) > -1)
		            userCharts += new String(buf, 0, i);
		        return userCharts;
			}
		}catch(Exception e){
			Log.e(TAG, e.getMessage());
		}
		return null;
	}
	
	@Override
	public String getCurrentImages() {
		try{
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet req = new HttpGet(PROTOCOL+this.server+":"+PORT+URL_IMAGES_PREFIX
					+"?"+PARAM_DEVICE_NAME+"="+deviceName+"&"+PARAM_TOKEN+"="+token);
			
			req.addHeader(ACCEPT, TEXT_XML);
			HttpResponse res = client.execute(req);
			if(res.getStatusLine().getStatusCode() == 200){
				InputStream in = res.getEntity().getContent();
				int i = -1;
		        String currentImages = "";
		        byte[] buf = new byte[1024];
		        while ((i = in.read(buf)) > -1)
		            currentImages += new String(buf, 0, i);
		        return currentImages;
			}
		}catch(Exception e){
			Log.e(TAG, e.getMessage());
		}
		return null;
	}
	
	@Override
	public void removeUserChart(int id) throws NotConnectedException {
		if(!connected)
			throw new NotConnectedException();
		else{
			try{
				DefaultHttpClient client = new DefaultHttpClient();
				HttpDelete req = new HttpDelete(PROTOCOL+this.server+":"+PORT+URL_USER_CONTENTS_CHARTS
						+"/"+id+"?"+PARAM_DEVICE_NAME+"="+deviceName+"&"+PARAM_TOKEN+"="+token);
				req.addHeader(ACCEPT, TEXT_PLAIN);
				HttpResponse res = client.execute(req);
				if(res.getStatusLine().getStatusCode() == 200){
					return;
				}
			}catch(Exception e){
				Log.e(TAG, e.getMessage());
			}
		}
	}
	@Override
	public long getTimestamp() throws NotConnectedException {
		if(!connected)
			throw new NotConnectedException();
		else{
			try{
				DefaultHttpClient client = new DefaultHttpClient();
				HttpGet req = new HttpGet(PROTOCOL+this.server+":"+PORT+URL_USER_CONTENTS_TIMESTAMP
						+"?"+PARAM_DEVICE_NAME+"="+deviceName+"&"+PARAM_TOKEN+"="+token);
				
				req.addHeader(ACCEPT, TEXT_PLAIN);
				HttpResponse res = client.execute(req);
				if(res.getStatusLine().getStatusCode() == 200){
					InputStream in = res.getEntity().getContent();
					int i = -1;
			        String timestamp = "";
			        byte[] buf = new byte[1024];
			        while ((i = in.read(buf)) > -1)
			            timestamp += new String(buf, 0, i);
			        return Long.parseLong(timestamp);
				}
			}catch(Exception e){
				Log.e(TAG, e.getMessage());
			}
		}
		return 0;
	}
	@Override
	public boolean hasNewOhterUsersCharts() {
		return hasNewOtherUsersCharts; 
	}
	
	@Override
	public void setHasNewOtherUsersCharts(boolean b){
		this.hasNewOtherUsersCharts = b;
		setChanged();
		notifyObservers();
	}
	@Override
	public void setCurrentImageTimestamp(long timestamp) {
		this.currentImageTimestamp = timestamp;
		
	}
	@Override
	public long getCurrentImageTimestamp() {
		return this.currentImageTimestamp;
	}
}
