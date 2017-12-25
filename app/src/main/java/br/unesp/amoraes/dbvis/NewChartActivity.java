package br.unesp.amoraes.dbvis;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import br.unesp.amoraes.dbvis.charts.BarChart;
import br.unesp.amoraes.dbvis.charts.RoundChart;
import br.unesp.amoraes.dbvis.charts.ScatterChart;
import br.unesp.amoraes.dbvis.exception.NotConnectedException;
import br.unesp.amoraes.dbvis.logic.Metadata;
import br.unesp.amoraes.dbvis.logic.ServerConnection;
import br.unesp.amoraes.dbvis.logic.ServerConnectionFactory;
import br.unesp.amoraes.dbvis.logic.Visualization;
import br.unesp.amoraes.dbvis.userdata.UserChart;
import br.unesp.amoraes.dbvis.userdata.UserText;
import br.unesp.amoraes.dbvis.utils.FunctionsHelper;
import br.unesp.amoraes.dbvis.utils.GenericValuesModel;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class NewChartActivity extends Activity implements IBasicChartActivity{

	private ToggleButton toggleChartTypePizza;
	private ToggleButton toggleChartTypePoint;
	private ToggleButton toggleChartTypeBar;
	private Spinner spinnerChartX;
	private Spinner spinnerChartY;
	private Spinner spinnerChartZ;
	private TextView textViewX;
	private TextView textViewY;
	private TextView textViewZ;
	private Button btnChartGenerate;
	private Button btnChartShare;
	private LinearLayout linearLayoutChartButtons;
	private GraphicalView chartView;
	private LinearLayout chartLayout;
	private UserChart currentChart;
	
	
	
	public static final String SPINNER_EMPTY = " ";
	private int selectedChartType = -1;
	
	protected float globalX;
	protected float globalY;
	protected float globalWidth;
	protected float globalHeight;
	
	
	protected static final String TAG = "DBVis::ChartActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chart_new);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		globalX = bundle.getFloat(KGlobal.PARAM_GLOBAL_X);
		globalY = bundle.getFloat(KGlobal.PARAM_GLOBAL_Y);
		globalWidth = bundle.getFloat(KGlobal.PARAM_GLOBAL_WIDTH);
		globalHeight = bundle.getFloat(KGlobal.PARAM_GLOBAL_HEIGHT);
		
		
		
		toggleChartTypePizza = (ToggleButton)findViewById(R.id.toggleChartTypePizza);
		toggleChartTypePoint = (ToggleButton)findViewById(R.id.toggleChartTypePoint);
		toggleChartTypeBar = (ToggleButton)findViewById(R.id.toggleChartTypeBar);
		linearLayoutChartButtons = (LinearLayout)findViewById(R.id.linearLayoutChartButtons);
		
		chartLayout = (LinearLayout)findViewById(R.id.linearLayoutChart);
		
		toggleChartTypePizza.setOnCheckedChangeListener(new ToggleChartTypeListener(KGlobal.CHART_TYPE_PIZZA, this));
		toggleChartTypePoint.setOnCheckedChangeListener(new ToggleChartTypeListener(KGlobal.CHART_TYPE_POINT, this));
		toggleChartTypeBar.setOnCheckedChangeListener(new ToggleChartTypeListener(KGlobal.CHART_TYPE_BAR, this));
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_chart, menu);
		return true;
	}

	public int getSelectedChartType() {
		return selectedChartType;
	}
	
	public Spinner getSpinnerChartX() {
		return spinnerChartX;
	}

	public Spinner getSpinnerChartY() {
		return spinnerChartY;
	}
	
	public Spinner getSpinnerChartZ() {
		return spinnerChartZ;
	}
	
	public Button getBtnChartGenerate() {
		return btnChartGenerate;
	}

	public Button getBtnChartShare() {
		return btnChartShare;
	}

	public GraphicalView getChartView(){
		return chartView;
	}
	public void setChartView(GraphicalView v){
		chartView = v;
	}

	public void changeChartType(int selectedChartType) {
		this.selectedChartType = selectedChartType;
		linearLayoutChartButtons.removeAllViews();
		spinnerChartX = null;
		spinnerChartY = null;
		btnChartGenerate = null;
		if(this.selectedChartType == KGlobal.CHART_TYPE_PIZZA){
			toggleChartTypeBar.setChecked(false);
			toggleChartTypePoint.setChecked(false);
		}else if(this.selectedChartType == KGlobal.CHART_TYPE_POINT){
			toggleChartTypeBar.setChecked(false);
			toggleChartTypePizza.setChecked(false);
		}else if(this.selectedChartType == KGlobal.CHART_TYPE_BAR){
			toggleChartTypePizza.setChecked(false);
			toggleChartTypePoint.setChecked(false);
		}
		if(this.selectedChartType != -1){
			textViewX = new TextView(this);
			linearLayoutChartButtons.addView(textViewX);
			spinnerChartX = new Spinner(this);
			linearLayoutChartButtons.addView(spinnerChartX);
			if(this.selectedChartType == KGlobal.CHART_TYPE_POINT){
				textViewY = new TextView(this);
				linearLayoutChartButtons.addView(textViewY);
				spinnerChartY = new Spinner(this);
				linearLayoutChartButtons.addView(spinnerChartY);
				textViewX.setText(R.string.chart_x);
				textViewY.setText(R.string.chart_y);
				populateSpinner(spinnerChartX, true, false);
				populateSpinner(spinnerChartY, true, false);
			}
			if(this.selectedChartType == KGlobal.CHART_TYPE_BAR){
				textViewY = new TextView(this);				
				linearLayoutChartButtons.addView(textViewY);
				spinnerChartY = new Spinner(this);
				linearLayoutChartButtons.addView(spinnerChartY);
				
				textViewZ = new TextView(this);
				linearLayoutChartButtons.addView(textViewZ);
				spinnerChartZ = new Spinner(this);
				linearLayoutChartButtons.addView(spinnerChartZ);
				
				textViewX.setText(R.string.chart_serie_1);
				textViewY.setText(R.string.chart_serie_2);
				textViewZ.setText(R.string.chart_serie_3);
				populateSpinner(spinnerChartX, true, false);
				populateSpinner(spinnerChartY, true, true);
				populateSpinner(spinnerChartZ, true, true);
			}
			if(this.selectedChartType == KGlobal.CHART_TYPE_PIZZA){
				textViewX.setText(R.string.chart_values);
				populateSpinner(spinnerChartX, true, false);
			}
			
			
			
			btnChartGenerate = new Button(this);
			btnChartGenerate.setText(R.string.btn_generate);
			btnChartGenerate.setOnClickListener(new GenerateChartOnClickListener(chartLayout, this));
			linearLayoutChartButtons.addView(btnChartGenerate);
			btnChartShare = new Button(this);
			btnChartShare.setText(R.string.btn_share);
			btnChartShare.setOnClickListener(new ShareChartOnClickListener(this));
			if(chartView == null){
				btnChartShare.setEnabled(false);
			}else{
				btnChartShare.setEnabled(true);
			}
			linearLayoutChartButtons.addView(btnChartShare);
			
		}
	}

	
	private void populateSpinner(Spinner spinner, boolean onlyNumeric, boolean showEmpty) {
		Metadata metadata = Metadata.getInstance();
		ArrayList<String> list = new ArrayList<String>();
		if(showEmpty){
			list.add(SPINNER_EMPTY);
		}
		for(String column : metadata.getColumns().keySet()){
			String klass = metadata.getColumns().get(column);
			if(FunctionsHelper.isNumberClass(klass) || onlyNumeric == false){
				list.add(column);
			}
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_spinner_item,list);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);				
	}


	@Override
	public Context getContext() {
		return this;
	}

	public UserChart getCurrentChart() {
		return currentChart;
	}

	public void setCurrentChart(UserChart currentChart) {
		this.currentChart = currentChart;
	}

}

class ShareChartOnClickListener implements OnClickListener {

	private NewChartActivity chartActivity;
	public ShareChartOnClickListener(NewChartActivity a){
		this.chartActivity = a;
	}
	@Override
	public void onClick(View v) {
		AlertDialog.Builder chartTextPopup = new AlertDialog.Builder(chartActivity);
		chartTextPopup.setTitle(chartActivity.getResources().getString(R.string.lbl_note));
		chartTextPopup.setMessage("");
		EditText editTextNote = new EditText(chartActivity);
		chartTextPopup.setView(editTextNote);
		chartTextPopup.setPositiveButton(R.string.btn_save, new ChartTextPopupSaveListener(editTextNote, chartActivity));
		chartTextPopup.show();
	}
}

class ChartTextPopupSaveListener implements DialogInterface.OnClickListener {
	private EditText editTextNote;
	private UserChart userChart;
	private NewChartActivity activity;
	public ChartTextPopupSaveListener(EditText editTextNote, NewChartActivity activity){
		this.editTextNote = editTextNote;
		this.userChart = new UserChart();
		this.activity = activity;
	}
	@Override
	public void onClick(DialogInterface dialog, int whichButton) {
		userChart.setDate(new Date());
		userChart.setText(editTextNote.getText().toString());
		userChart.setDeviceName(Visualization.getInstance().getServerConnection().getDeviceName());
		Bitmap image = Bitmap.createBitmap(activity.getChartView().getWidth(), activity.getChartView().getHeight(), Bitmap.Config.ARGB_8888);
		activity.getChartView().draw(new Canvas(image));
		userChart.setImage(Bitmap.createScaledBitmap(image, image.getWidth()/4, image.getHeight()/4, true));
		userChart.setNodesInfo(activity.getCurrentChart().getNodesInfo());
		userChart.setAxisX(activity.getCurrentChart().getAxisX());
		userChart.setAxisY(activity.getCurrentChart().getAxisY());
		userChart.setAxisZ(activity.getCurrentChart().getAxisZ());
		userChart.setType(activity.getCurrentChart().getType());
		ServerConnection connection = ServerConnectionFactory.create();
		try {
			connection.addUserChart(userChart);
			Visualization.getInstance().getUserContents().getCharts().put(userChart.getId(), userChart);
		} catch (NotConnectedException e) {
			Log.e(ExplorerActivity.TAG, "Cannot save text on server");
		}
		try {
			FileOutputStream fos = activity.openFileOutput(KGlobal.USER_CHARTS_FILE_PREFIX+userChart.getId(), Context.MODE_PRIVATE);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			userChart.getImage().compress(Bitmap.CompressFormat.PNG, 100, stream);
			byte[] byteArray = stream.toByteArray();
			fos.write(byteArray);
			fos.close();
		} catch (FileNotFoundException e) {
			Log.e(ExplorerActivity.TAG, null, e);
		} catch (IOException e) {
			Log.e(ExplorerActivity.TAG, null, e);
		}
		Log.d(ExplorerActivity.TAG, "Saving chart: "+editTextNote.getText());
		dialog.dismiss();
	}
}

class GenerateChartOnClickListener implements OnClickListener {
	
	private LinearLayout linearLayout;
	private NewChartActivity chartActivity;
	public GenerateChartOnClickListener(LinearLayout l, NewChartActivity a){
		this.linearLayout = l;
		this.chartActivity = a;
	}
	
	@Override
	public void onClick(View v) {
		
		String nodesInfo = "";
		try {
			nodesInfo = Visualization.getInstance().getServerConnection().
					getNodeByArea(new Float(chartActivity.globalX).intValue(),
							new Float(chartActivity.globalY).intValue(),
							new Float(chartActivity.globalWidth).intValue(),
							new Float(chartActivity.globalHeight).intValue());
		} catch (NotConnectedException e) {
			Log.e(chartActivity.TAG, e.getMessage());
		}
		if(nodesInfo == null || nodesInfo.equals("") || nodesInfo.contains("<collection>\n</collection>")){
			Toast.makeText(chartActivity, chartActivity.getResources().getText(R.string.error_without_nodes_on_region), Toast.LENGTH_SHORT).show();
			return;
		}
		List<GenericValuesModel> listData = FunctionsHelper.parseXML(nodesInfo);
		if(listData == null) return;
		
		//save the current chart
		chartActivity.setCurrentChart(new UserChart());
		chartActivity.getCurrentChart().setNodesInfo(nodesInfo);
		chartActivity.getCurrentChart().setType(chartActivity.getSelectedChartType());
		
		if(chartActivity.getSelectedChartType() == KGlobal.CHART_TYPE_PIZZA){
			linearLayout.removeAllViews();
			new RoundChart(listData, 
					chartActivity.getSpinnerChartX().getSelectedItem().toString(), 
					linearLayout, chartActivity);
					chartActivity.getCurrentChart().setAxisX(chartActivity.getSpinnerChartX().getSelectedItem().toString());	
		}else if(chartActivity.getSelectedChartType() == KGlobal.CHART_TYPE_POINT){
			linearLayout.removeAllViews();
			new ScatterChart(listData, 
					chartActivity.getSpinnerChartX().getSelectedItem().toString(), 
					chartActivity.getSpinnerChartY().getSelectedItem().toString(), 
					linearLayout, chartActivity);
					chartActivity.getCurrentChart().setAxisX(chartActivity.getSpinnerChartX().getSelectedItem().toString());
					chartActivity.getCurrentChart().setAxisY(chartActivity.getSpinnerChartY().getSelectedItem().toString());
		}else if(chartActivity.getSelectedChartType() == KGlobal.CHART_TYPE_BAR){
			linearLayout.removeAllViews();
			new BarChart(listData, 
					chartActivity.getSpinnerChartX().getSelectedItem().toString(), 
					chartActivity.getSpinnerChartY().getSelectedItem().toString(),
					chartActivity.getSpinnerChartZ().getSelectedItem().toString(),
					linearLayout, chartActivity);
					chartActivity.getCurrentChart().setAxisX(chartActivity.getSpinnerChartX().getSelectedItem().toString());
					chartActivity.getCurrentChart().setAxisY(chartActivity.getSpinnerChartY().getSelectedItem().toString());
					chartActivity.getCurrentChart().setAxisZ(chartActivity.getSpinnerChartZ().getSelectedItem().toString());
		}
		
		if(chartActivity.getChartView() != null){
			chartActivity.getBtnChartShare().setEnabled(true);
		}else{
			chartActivity.getBtnChartShare().setEnabled(false);
		}
		
		
		
	}
	
}

class ToggleChartTypeListener implements OnCheckedChangeListener {
	private int type;
	private NewChartActivity activity;
	public ToggleChartTypeListener(int type, NewChartActivity activity){
		this.type = type;
		this.activity = activity;
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(isChecked){
			activity.changeChartType(type);
		}
	}
	
}






