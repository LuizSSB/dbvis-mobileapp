package br.unesp.amoraes.dbvis.charts;

import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import android.graphics.Color;
import android.widget.LinearLayout;
import br.unesp.amoraes.dbvis.IBasicChartActivity;
import br.unesp.amoraes.dbvis.NewChartActivity;
import br.unesp.amoraes.dbvis.utils.FunctionsHelper;
import br.unesp.amoraes.dbvis.utils.GenericValuesModel;

public class RoundChart {

	public RoundChart(List<GenericValuesModel> listData, String valueColumn,
			LinearLayout linearLayout, IBasicChartActivity activity) {
		int[] colors = FunctionsHelper.createColorCodeTable(listData.size());
		CategorySeries mSeries = new CategorySeries("");
		DefaultRenderer mRenderer = new DefaultRenderer();
		mRenderer.setStartAngle(180);
		mRenderer.setDisplayValues(true);
		mRenderer.setLegendTextSize(mRenderer.getLegendTextSize() + 4);
		mRenderer.setLabelsTextSize(mRenderer.getLabelsTextSize() + 4);
		mRenderer.setBackgroundColor(Color.DKGRAY);
		int i = 0;
		for (GenericValuesModel data : listData) {
			mSeries.add(data.getLabel(),
					Double.parseDouble(data.getValues().get(valueColumn)));
			SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
			renderer.setColor(colors[i % colors.length]);
			mRenderer.addSeriesRenderer(renderer);
			i++;
		}
		GraphicalView mChart = ChartFactory.getPieChartView(activity.getContext(), mSeries,
				mRenderer);
		linearLayout.addView(mChart);
		activity.setChartView(mChart);
	}

}
