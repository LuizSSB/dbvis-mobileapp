package br.unesp.amoraes.dbvis;

import br.unesp.amoraes.dbvis.logic.Visualization;
import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class ChartActivity extends TabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chart);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		TabHost tabHost = getTabHost();
		TabSpec tab1 = tabHost.newTabSpec("Tag Chart New");
		TabSpec tab2 = tabHost.newTabSpec("Tag Chart Saved");
		TabSpec tab3 = tabHost.newTabSpec("Tag Chart Shared");
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		
		getWindow().setLayout(new Double(bundle.getInt(KGlobal.PARAM_WINDOW_WIDTH)*0.9).intValue(), new Double(bundle.getInt(KGlobal.PARAM_WINDOW_HEIGHT)*0.9).intValue());
		
		tab1.setIndicator(this.getResources().getString(R.string.chart_tab1));
		Intent intent1 = new Intent(this, NewChartActivity.class);
		intent1.putExtra(KGlobal.PARAM_GLOBAL_X, bundle.getFloat(KGlobal.PARAM_GLOBAL_X));
		intent1.putExtra(KGlobal.PARAM_GLOBAL_Y, bundle.getFloat(KGlobal.PARAM_GLOBAL_Y));
		intent1.putExtra(KGlobal.PARAM_GLOBAL_WIDTH, bundle.getFloat(KGlobal.PARAM_GLOBAL_WIDTH));
		intent1.putExtra(KGlobal.PARAM_GLOBAL_HEIGHT, bundle.getFloat(KGlobal.PARAM_GLOBAL_HEIGHT));
		tab1.setContent(intent1);
		
		tab2.setIndicator(this.getResources().getString(R.string.chart_tab2));
		Intent intent2 = new Intent(this, MyChartsActivity.class);
		tab2.setContent(intent2);
		
		tab3.setIndicator(this.getResources().getString(R.string.chart_tab3));
		Intent intent3 = new Intent(this, SharedChartsActivity.class);
		tab3.setContent(intent3);
		tabHost.setup();
		tabHost.addTab(tab1);
		tabHost.addTab(tab2);
		tabHost.addTab(tab3);
		
		//change text of tabs
		for(int i = 0; i < tabHost.getTabWidget().getChildCount(); i++){
			TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
			tv.setTextSize(20);
			
		}
		
		if(Visualization.getInstance().getServerConnection().hasNewOhterUsersCharts()){
			tabHost.setCurrentTab(2);
			Visualization.getInstance().getServerConnection().setHasNewOtherUsersCharts(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_chart, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
