package br.unesp.amoraes.dbvis.charts;

import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
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

public class ScatterChart {

		public ScatterChart(List<GenericValuesModel> listData, String xColumn, String yColumn,
				LinearLayout linearLayout, IBasicChartActivity activity) {
			int[] colors = FunctionsHelper.createColorCodeTable(listData.size());
			XYMultipleSeriesDataset xyDataSet = new XYMultipleSeriesDataset();
			XYMultipleSeriesRenderer xyRenderer = new XYMultipleSeriesRenderer();
			xyRenderer.setLegendTextSize(xyRenderer.getLegendTextSize()+4);
			xyRenderer.setLabelsTextSize(xyRenderer.getLabelsTextSize()+4);
			xyRenderer.setPointSize(xyRenderer.getPointSize()+2);
			xyRenderer.setGridColor(Color.GRAY);
			xyRenderer.setShowGrid(true);
			xyRenderer.setXTitle(xColumn);
			xyRenderer.setYTitle(yColumn);
			int i = 0;
			int ps = 0;
			PointStyle[] styles = new PointStyle[] { PointStyle.X, PointStyle.DIAMOND, PointStyle.TRIANGLE,
			        PointStyle.SQUARE, PointStyle.CIRCLE };
			for(GenericValuesModel data : listData){
				XYSeries serie = new XYSeries(data.getLabel());
				serie.add(Double.parseDouble(data.getValues().get(xColumn)), Double.parseDouble(data.getValues().get(yColumn)));
				xyDataSet.addSeries(serie);
				XYSeriesRenderer renderer = new XYSeriesRenderer();
				renderer.setColor(colors[i % colors.length]);
				renderer.setPointStyle(styles[ps]);
				xyRenderer.addSeriesRenderer(renderer);
				i++;
				ps++;
				if(ps > 4){ ps = 0; }
			}
			GraphicalView mChart = ChartFactory.getScatterChartView(activity.getContext(), xyDataSet, xyRenderer);
			mChart.setBackgroundColor(Color.DKGRAY);
			linearLayout.addView(mChart);
			activity.setChartView(mChart);
		}
		
	}
