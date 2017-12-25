package br.unesp.amoraes.dbvis.logic.impl;

import java.io.PrintWriter;
import java.net.Socket;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class PositionUpdaterSocket extends AsyncTask<Integer, Long, Long> {
	
	
	
	private Context context;
	public PositionUpdaterSocket(Context context, String deviceName, String token, Socket socket){
		this.context = context;
		this.deviceName = deviceName;
		this.token = token;
		this.socket = socket;
	}
	
	private static final String TAG = "DBVis::PositionUpdaterSocket";
	
	private String deviceName;
	private String token;
	private Socket socket;
	private PrintWriter printWriter;
	
	
	@Override
	protected void onPostExecute(Long p) {
		Log.i(TAG, "Position updated");	
	}

	@Override
	protected Long doInBackground(Integer... params) {
		int x   = params[0];
		int y    = params[1];
		int width  = params[2];
		int height = params[3];
		try{
			this.printWriter = new PrintWriter(socket.getOutputStream());
			printWriter.write("positionUpdate#"+deviceName+"#"+token+"#"+x+"#"+y+"#"+width+"#"+height+"\n");
			this.printWriter.flush();		
		}catch(Exception e){
			Log.e(TAG, e.getMessage(), e);
		}
		return 1L;
	}

}
