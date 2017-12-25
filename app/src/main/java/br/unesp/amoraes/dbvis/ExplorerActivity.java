package br.unesp.amoraes.dbvis;

import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.TooManyListenersException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources.Theme;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.sax.TextElementListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import br.unesp.amoraes.dbvis.components.ZoomImageView;
import br.unesp.amoraes.dbvis.exception.NotConnectedException;
import br.unesp.amoraes.dbvis.logic.ServerConnection;
import br.unesp.amoraes.dbvis.logic.ServerConnectionFactory;
import br.unesp.amoraes.dbvis.logic.Visualization;
import br.unesp.amoraes.dbvis.userdata.UserText;
import br.unesp.amoraes.dbvis.utils.FunctionsHelper;
import br.unesp.amoraes.dbvis.utils.GenericValuesModel;


public class ExplorerActivity extends Activity  implements Observer {

	static final String TAG = "DBVis::ExplorerActivity";
	private ZoomImageView graphZoomImageView;
	private ToggleButton toggleNavigation;
	private ToggleButton toggleInformation;
	private ToggleButton toggleSelection;
	private ToggleButton toggleText;
	private ToggleButton togglePreferences;
	private ToggleButton toggleChart;
	
	public static final int MODE_NAVIGATION = 0;
	public static final int MODE_INFORMATION = 1;
	public static final int MODE_SELECTION = 2;
	public static final int MODE_TEXT = 3;
	public static final int MODE_CHART = 4;
	public static final int MODE_PREFERENCES = 5;
	public static final int TOTAL_WIDTH = 20;
	public static final int VIEW_WIDTH = 19;
	
	private int oldMode = -1;
	
	private Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.activity_explorer);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		graphZoomImageView = (ZoomImageView) findViewById(R.id.graphZoomImageView);
		graphZoomImageView.setMaxZoom(3f);
		graphZoomImageView.setExplorerActivity(this);
		toggleNavigation = (ToggleButton)findViewById(R.id.toggleNavigation);
		toggleInformation = (ToggleButton)findViewById(R.id.toggleInformation);
		toggleSelection = (ToggleButton)findViewById(R.id.toggleSelection);
		toggleText = (ToggleButton)findViewById(R.id.toggleText);
		togglePreferences = (ToggleButton)findViewById(R.id.togglePreferences);
		toggleChart = (ToggleButton)findViewById(R.id.toggleChart);
		
		
		toggleNavigation.setChecked(true);
		toggleInformation.setChecked(false);
		toggleSelection.setChecked(false);
		toggleText.setChecked(false);
		toggleNavigation.setOnCheckedChangeListener(new ToggleChangeListener(MODE_NAVIGATION,this));
		toggleInformation.setOnCheckedChangeListener(new ToggleChangeListener(MODE_INFORMATION,this));
		toggleSelection.setOnCheckedChangeListener(new ToggleChangeListener(MODE_SELECTION, this));
		toggleText.setOnCheckedChangeListener(new ToggleChangeListener(MODE_TEXT, this));
		togglePreferences.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					AlertDialog.Builder preferencesPopup = new AlertDialog.Builder(buttonView.getContext());
					preferencesPopup.setTitle(getResources().getString(R.string.title_preferences));
					preferencesPopup.setMessage("");
					LayoutInflater inflater = getLayoutInflater();
					final View dialogView = inflater.inflate(R.layout.popup_preferences, (ViewGroup)getCurrentFocus());
					final CheckBox chkShowOtherUsersTexts = (CheckBox)dialogView.findViewById(R.id.chkPreferencesShowOtherUsersTexts);
					preferencesPopup.setView(dialogView);
					final int oldMode = getCurrentMode();
					changeToggleButtons(MODE_PREFERENCES);
					preferencesPopup.setPositiveButton(R.string.btn_save, new AlertDialog.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {							
							Visualization.getInstance().getPreferences().setShowOtherUsersTexts(chkShowOtherUsersTexts.isChecked());
							setCurrentMode(oldMode);
							changeToggleButtons(oldMode);
							Log.d(TAG,"Returning to mode "+oldMode);
							
						}
					});
					preferencesPopup.setNeutralButton(R.string.btn_cancel, new AlertDialog.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							setCurrentMode(oldMode);
							changeToggleButtons(oldMode);
						}
					});
					preferencesPopup.setOnCancelListener(new AlertDialog.OnCancelListener() {
						
						@Override
						public void onCancel(DialogInterface dialog) {
							setCurrentMode(oldMode);
							changeToggleButtons(oldMode);
						}
					});
					preferencesPopup.show();
					chkShowOtherUsersTexts.setChecked(Visualization.getInstance().getPreferences().isShowOtherUsersTexts());
				}
				
			}
		});
		toggleChart.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					oldMode = getCurrentMode();
					changeToggleButtons(MODE_CHART);
					Intent intent = new Intent(getApplicationContext(), ChartActivity.class);
					intent.putExtra(KGlobal.PARAM_GLOBAL_X, graphZoomImageView.getGlobalX());
					intent.putExtra(KGlobal.PARAM_GLOBAL_Y, graphZoomImageView.getGlobalY());
					intent.putExtra(KGlobal.PARAM_GLOBAL_WIDTH, graphZoomImageView.getGlobalWidth());
					intent.putExtra(KGlobal.PARAM_GLOBAL_HEIGHT, graphZoomImageView.getGlobalHeight());
					intent.putExtra(KGlobal.PARAM_WINDOW_WIDTH, getWindow().peekDecorView().getWidth());
					intent.putExtra(KGlobal.PARAM_WINDOW_HEIGHT, getWindow().peekDecorView().getHeight());
					toggleChart.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_chart));
					startActivity(intent);
				}				
			}
		});
		
		
		try {
			graphZoomImageView.setImageBitmap(Visualization.getInstance().getServerConnection().getFullDisplayImage());
		} catch (NotConnectedException e) {
			Log.e(TAG, "Not connected!");
		}
		
		Visualization.getInstance().getServerConnection().addObserver(this);
	}

	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_explorer, menu);
		return true;
	}
	
	private void changeChartToggleToNew(){
		this.toggleChart.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_chart_new));
		this.toggleChart.invalidate();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "Resuming Explorer Activity...");
		if(oldMode != -1){
			changeToggleButtons(oldMode);
			oldMode=-1;
		}
	}

	protected void setCurrentMode(int mode){
		graphZoomImageView.setCurrentUserMode(mode);
	}
	
	protected int getCurrentMode(){
		return graphZoomImageView.getCurrentMode();
	}

	public ToggleButton getToggleNavigation() {
		return toggleNavigation;
	}

	public ToggleButton getToggleInformation() {
		return toggleInformation;
	}
	
	public ToggleButton getToggleSelection(){
		return toggleSelection;
	}
	
	public ToggleButton getToggleText(){
		return toggleText;
	}
	
	public ToggleButton getTogglePreferences(){
		return togglePreferences;
	}
	
	public ToggleButton getToggleChart(){
		return toggleChart;
	}
	
	

	public ZoomImageView getGraphZoomImageView() {
		return graphZoomImageView;
	}

	public void showInfoPopup(int x, int y, int nodeX, int nodeY) {
		String nodeInfo;
		try {
			nodeInfo = Visualization.getInstance().getServerConnection().getNodeByPoint(nodeX, nodeY);
			if(nodeInfo.equals("")){
				for(int i = 1; i < 10; i++){
					int step = 5;
					nodeInfo = Visualization.getInstance().getServerConnection().getNodeByArea(nodeX-(step*i), nodeY-(step*i),step*i*2,step*i*2);
					if(!nodeInfo.contains("<collection>\n</collection>")) break;
				}
			}
			if(nodeInfo == null || nodeInfo.equals("") || nodeInfo.contains("<collection>\n</collection>")){
				return;
			}
			String id = "";
			String label = "";
			
			List<GenericValuesModel> listData = FunctionsHelper.parseXML(nodeInfo);
			if(listData == null) return;
			else{
				id = listData.get(0).getId();
				label = listData.get(0).getLabel();
			}
			String title = "#"+id+": "+label;
			if(id.equals(label)){
				title = "#"+id;
			}
			String text = "";
			int lines = 0;
			for(String k : listData.get(0).getValues().keySet()){
				text+="\n"+k+": "+listData.get(0).getValues().get(k);
				lines++;
			}

			int popupWidth = 300;
			int popupHeight = lines * 20 + 100;

			// Inflate the popup_layout.xml
			LinearLayout viewGroup = (LinearLayout) findViewById(R.id.popup_info);
			LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = layoutInflater.inflate(R.layout.popup_info,
					viewGroup);

			// Creating the PopupWindow
			final PopupWindow popup = new PopupWindow(this);
			popup.setContentView(layout);
			popup.setWidth(popupWidth);
			popup.setHeight(popupHeight);
			popup.setFocusable(true);

			// Some offset to align the popup a bit to the right, and a bit
			// down, relative to button's position.
			int OFFSET_X = 30;
			int OFFSET_Y = 30;

			// Clear the default translucent background
			popup.setBackgroundDrawable(new BitmapDrawable());
			

			// Displaying the popup at the specified location, + offsets.
			popup.showAtLocation(layout, Gravity.NO_GRAVITY, x + OFFSET_X,
					y + OFFSET_Y);

			// Getting a reference to Close button, and close the popup when
			// clicked.
			Button close = (Button) layout.findViewById(R.id.btnPopupInfoClose);
			close.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					popup.dismiss();
					
				}
			});
			
			
			TextView textViewTitle = (TextView)layout.findViewById(R.id.txtPopupNodeTitle);
			textViewTitle.setText(title);
			TextView textViewInfo = (TextView)layout.findViewById(R.id.txtPopupNodeInfo);
			textViewInfo.setText(text);
		} catch (NotConnectedException e) {
			Log.e(TAG, e.getMessage(), e);
		}
				
	}

	public void showTextPopup(int x, int y, int visX, int visY) {
		UserText userText = new UserText();
		
		for(UserText ut : Visualization.getInstance().getUserContents().getTexts().values()){
			if(ut.getLastRect() != null && ut.getLastRect().contains(new Float(x), new Float(y))){
				userText = ut;
				visX = ut.getX();
				visY = ut.getY();
				break;
			}
			
		}

		AlertDialog.Builder textNotePopup = new AlertDialog.Builder(this);
		textNotePopup.setTitle(getResources().getString(R.string.lbl_note));
		textNotePopup.setMessage("");
		EditText txtTextNote = new EditText(this);
		if(userText.getText() != null)
			txtTextNote.setText(userText.getText());
		textNotePopup.setView(txtTextNote);
		textNotePopup.setPositiveButton(R.string.btn_save, new TextPopupSaveListener(userText, visX, visY, txtTextNote,this));
		textNotePopup.setNeutralButton(R.string.btn_delete, new TextPopupDeleteListener(userText,this));
		textNotePopup.show();
		
	}

	public void changeToggleButtons(int mode) {
		if(ExplorerActivity.MODE_NAVIGATION == mode){
			this.getToggleNavigation().setChecked(true);
			this.getToggleInformation().setChecked(false);
			this.getToggleSelection().setChecked(false);
			this.getToggleText().setChecked(false);
			this.getToggleChart().setChecked(false);
			this.getTogglePreferences().setChecked(false);
		}else
		if(ExplorerActivity.MODE_INFORMATION == mode){
			this.getToggleInformation().setChecked(true);
			this.getToggleNavigation().setChecked(false);
			this.getToggleSelection().setChecked(false);
			this.getToggleText().setChecked(false);
			this.getToggleChart().setChecked(false);
			this.getTogglePreferences().setChecked(false);
		}else
		if(ExplorerActivity.MODE_SELECTION == mode){
			this.getToggleSelection().setChecked(true);
			this.getToggleNavigation().setChecked(false);
			this.getToggleInformation().setChecked(false);
			this.getToggleText().setChecked(false);
			this.getToggleChart().setChecked(false);
			this.getTogglePreferences().setChecked(false);
		}else
		if(ExplorerActivity.MODE_TEXT == mode){
			this.getToggleText().setChecked(true);
			this.getToggleNavigation().setChecked(false);
			this.getToggleInformation().setChecked(false);
			this.getToggleSelection().setChecked(false);
			this.getToggleChart().setChecked(false);
			this.getTogglePreferences().setChecked(false);
		}else
		if(ExplorerActivity.MODE_CHART == mode){
			this.getToggleChart().setChecked(true);
			this.getToggleNavigation().setChecked(false);
			this.getToggleInformation().setChecked(false);
			this.getToggleSelection().setChecked(false);
			this.getToggleText().setChecked(false);
			this.getTogglePreferences().setChecked(false);
		}else
		if(ExplorerActivity.MODE_PREFERENCES == mode){
			this.getTogglePreferences().setChecked(true);
			this.getToggleNavigation().setChecked(false);
			this.getToggleInformation().setChecked(false);
			this.getToggleSelection().setChecked(false);
			this.getToggleText().setChecked(false);
			this.getToggleChart().setChecked(false);
		}
		if(this.getToggleInformation().isChecked() == false
				&& this.getToggleNavigation().isChecked() == false 
				&& this.getToggleSelection().isChecked() == false
				&& this.getToggleText().isChecked() == false
				&& this.getTogglePreferences().isChecked() == false
				&& this.getToggleChart().isChecked() == false){
			this.setCurrentMode(ExplorerActivity.MODE_NAVIGATION);
			this.getToggleNavigation().setChecked(true);
			
		}
		
	}

	final Handler handler = new Handler(){
		public void handleMessage(Message msg) {
            int newChart = msg.arg1;
            if(newChart == 1){
            	changeChartToggleToNew();
            }
        }
	};
	
	@Override
	public void update(Observable o, Object optionalObject) {
		ServerConnection serverConnection = (ServerConnection)o;
		if(serverConnection.hasNewOhterUsersCharts()){
			Message msg = handler.obtainMessage();
	        msg.arg1 = 1;
	        handler.sendMessage(msg);
		}
	}
	
}

class TextPopupDeleteListener implements DialogInterface.OnClickListener {
	private UserText userText;
	private ExplorerActivity activity;
	
	public TextPopupDeleteListener(UserText userText, ExplorerActivity activity) {
		super();
		this.userText = userText;
		this.activity = activity;
	}

	@Override
	public void onClick(DialogInterface dialog, int whichButton) {
		ServerConnection connection = ServerConnectionFactory.create();
		try {
			connection.removeUserText(userText.getId());
			Visualization.getInstance().getUserContents().getTexts().remove(userText.getId());
			activity.getGraphZoomImageView().invalidate();
		} catch (NotConnectedException e) {
			Log.e(ExplorerActivity.TAG, "Cannot delete text on server");
		}
		
	}
}

class TextPopupSaveListener implements DialogInterface.OnClickListener {
	private UserText userText;
	private int visX;
	private int visY;
	private EditText txtTextNote;
	private ExplorerActivity activity;
	public TextPopupSaveListener(UserText userText, int visX, int visY, EditText txtTextNote, ExplorerActivity activity){
		this.userText = userText;
		this.visX = visX;
		this.visY = visY;
		this.txtTextNote = txtTextNote;
		this.activity = activity;
	}
	@Override
	public void onClick(DialogInterface dialog, int whichButton) {
		userText.setX(visX);
		userText.setY(visY);
		userText.setDate(new Date());
		userText.setText(txtTextNote.getText().toString());
		userText.setDeviceName(Visualization.getInstance().getServerConnection().getDeviceName());
		ServerConnection connection = ServerConnectionFactory.create();
		try {
			connection.addUserText(userText);
			Visualization.getInstance().getUserContents().getTexts().put(userText.getId(), userText);
		} catch (NotConnectedException e) {
			Log.e(ExplorerActivity.TAG, "Cannot save text on server");
		}
		
		Log.d(ExplorerActivity.TAG, "Saving text ("+visX+","+visY+"): "+userText.getText());
		dialog.dismiss();
		activity.getGraphZoomImageView().invalidate();
	}
}

class ToggleChangeListener implements OnCheckedChangeListener {
	
	private int mode;
	private ExplorerActivity activity;
	public ToggleChangeListener(int mode, ExplorerActivity activity){
		this.mode = mode;
		this.activity = activity;
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(isChecked){
			activity.setCurrentMode(mode);
			activity.changeToggleButtons(mode);
		}
					
	}
}
