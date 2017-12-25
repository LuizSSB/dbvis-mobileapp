package br.unesp.amoraes.dbvis;

import java.io.File;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import br.unesp.amoraes.dbvis.exception.AlreadyConnectedException;
import br.unesp.amoraes.dbvis.exception.InvalidPasswordException;
import br.unesp.amoraes.dbvis.exception.NotConnectedException;
import br.unesp.amoraes.dbvis.logic.ServerConnection;
import br.unesp.amoraes.dbvis.logic.ServerConnectionFactory;
import br.unesp.amoraes.dbvis.logic.Visualization;

public class MainActivity extends Activity {

	private static final String TAG = "DBVis::MainActivity";
	private EditText txtServer;
	private EditText txtPassword;
	private EditText txtDeviceName;
	private Button btnConnect;
	private SharedPreferences preferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.activity_main);
		
		String[] files = fileList();
		for(String f : files){
			if(f.indexOf(KGlobal.USER_CHARTS_FILE_PREFIX) != -1 && f.indexOf(KGlobal.USER_CHARTS_FILE_SUFIX) != -1){
				File file = new File(f);
				file.delete();
			}
		}

		//get the components
		txtServer = (EditText) findViewById(R.id.txtServer);
		txtPassword = (EditText) findViewById(R.id.txtPassword);
		txtDeviceName = (EditText) findViewById(R.id.txtDeviceName);
		btnConnect = (Button) findViewById(R.id.btnConnect);
		btnConnect.setOnClickListener(btnConnectListener);
		preferences = getSharedPreferences(KGlobal.PREFS_GENERAL_FILE, Context.MODE_PRIVATE);
		
		//get the last connection details
		String server = preferences.getString(KGlobal.PREFS_LAST_SERVER, "200.145.150.161");
		String password = preferences.getString(KGlobal.PREFS_LAST_PASSWORD, "123");
		String deviceName = preferences.getString(KGlobal.PREFS_LAST_DEVICE_NAME, "");
		if(deviceName == null || deviceName.equals("")){
			//try get bluetooth name to display on device name
			BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if(bluetoothAdapter != null){
				deviceName = bluetoothAdapter.getName();
			}
		}
		txtServer.setText(server);
		txtPassword.setText(password);
		txtDeviceName.setText(deviceName);
		
		//focus the server field
		txtServer.requestFocus();
		
		//allow network connection on main thread
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.activity_main, menu);
//		return true;
//	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_calibrate:     
            	Intent intent = new Intent(MainActivity.this, null);
				startActivity(intent);
                break;
        }
        return true;
    } 

	private OnClickListener btnConnectListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Visualization visualization = Visualization.getInstance();
			if (visualization.getServerConnection() != null
					&& visualization.getServerConnection().isConnected()) {
				Toast.makeText(MainActivity.this, R.string.msg_disconnecting,Toast.LENGTH_SHORT).show();
				try {
					visualization.getServerConnection().disconnect();
				} catch (NotConnectedException e) {
					Log.e(TAG, "Not connected!");
				}
				Toast.makeText(MainActivity.this, R.string.msg_disconnected,Toast.LENGTH_SHORT).show();
				
			}
			ServerConnection serverConnection = ServerConnectionFactory.create();
			
			try {
				String server = txtServer.getText().toString();
				String password = txtPassword.getText().toString();
				String deviceName = txtDeviceName.getText().toString();
						
				boolean connected = serverConnection.connect(server,password,deviceName);
				if(connected){
					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
					saveLastConnectionOnPreferences(server,password,deviceName);
					Toast.makeText(MainActivity.this, R.string.msg_connected,Toast.LENGTH_SHORT).show();
					visualization.setServerConnection(serverConnection);
					Intent intent = new Intent(MainActivity.this,ExplorerActivity.class);
					startActivity(intent);
				}else{
					Toast.makeText(MainActivity.this, R.string.error_cannot_connect,Toast.LENGTH_LONG).show();
				}
			} catch (AlreadyConnectedException e) {
				Log.e(TAG, "Already connected!");
			} catch (InvalidPasswordException e) {
				Toast.makeText(MainActivity.this, R.string.error_invalid_password,Toast.LENGTH_SHORT).show();
			}

		}

		private void saveLastConnectionOnPreferences(String server,
				String password, String deviceName) {
			SharedPreferences.Editor editor = preferences.edit();
			editor.putString(KGlobal.PREFS_LAST_SERVER, server);
			editor.putString(KGlobal.PREFS_LAST_PASSWORD, password);
			editor.putString(KGlobal.PREFS_LAST_DEVICE_NAME, deviceName);
			editor.commit();
		}
	};

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	
}
