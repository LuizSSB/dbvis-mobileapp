package br.unesp.amoraes.dbvis.charts;

import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.graphics.Color;
import android.widget.LinearLayout;
import br.unesp.amoraes.dbvis.IBasicChartActivity;
import br.unesp.amoraes.dbvis.NewChartActivity;
import br.unesp.amoraes.dbvis.utils.FunctionsHelper;
import br.unesp.amoraes.dbvis.utils.GenericValuesModel;

public class BarChart {
		public BarChart(List<GenericValuesModel> listData, String xColumn, String yColumn, String zColumn,
				LinearLayout linearLayout, IBasicChartActivity activity) {
			int numSeries = 1;
			XYSeries seriesX = new XYSeries(xColumn);
			XYSeries seriesY = null;
			XYSeries seriesZ = null;
			if(!yColumn.equals(NewChartActivity.SPINNER_EMPTY)){
				numSeries++;
				seriesY = new XYSeries(yColumn);
			}
			if(!zColumn.equals(NewChartActivity.SPINNER_EMPTY)){
				numSeries++;
				seriesZ = new XYSeries(zColumn);
			}
			for(int i = 0; i < listData.size(); i++){
				GenericValuesModel data = listData.get(i);
				seriesX.add(i,Double.parseDouble(data.getValues().get(xColumn)));
				if(seriesY != null){
					seriesY.add(i,Double.parseDouble(data.getValues().get(yColumn)));	
				}
				if(seriesZ != null){
					seriesZ.add(i,Double.parseDouble(data.getValues().get(zColumn)));	
				}		
			}
			XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
			dataset.addSeries(seriesX);
			if(seriesY != null){ dataset.addSeries(seriesY); }
			if(seriesZ != null){ dataset.addSeries(seriesZ); }
			
			XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
			multiRenderer.setGridColor(Color.GRAY);
			multiRenderer.setShowGrid(true);
			multiRenderer.setXLabels(0);
			for(int i = 0; i < listData.size(); i++){
				multiRenderer.addXTextLabel(i, listData.get(i).getLabel());
			}
			
			XYSeries serieToRender = seriesX;
			int[] colors = FunctionsHelper.createColorCodeTable(numSeries);
			XYSeries[] seriesToRender = new XYSeries[]{ seriesX, seriesY, seriesZ };
			for(int s = 0; s < seriesToRender.length; s++){
				if(seriesToRender[s] != null){
					XYSeriesRenderer renderer = new XYSeriesRenderer();
					renderer.setColor(colors[s]);
					renderer.setFillPoints(true);
					renderer.setLineWidth(2);
					renderer.setDisplayChartValues(false);
					multiRenderer.addSeriesRenderer(renderer);
				}
			}
			
			GraphicalView mChart = ChartFactory.getBarChartView(activity.getContext(), dataset, multiRenderer , Type.DEFAULT);
			mChart.setBackgroundColor(Color.DKGRAY);
			linearLayout.addView(mChart);
			activity.setChartView(mChart);
			
		}
		
		
	}
