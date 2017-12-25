package br.unesp.amoraes.dbvis;

import org.achartengine.GraphicalView;

import android.content.Context;

public interface IBasicChartActivity {
	public void setChartView(GraphicalView gv);
	public Context getContext();
}
