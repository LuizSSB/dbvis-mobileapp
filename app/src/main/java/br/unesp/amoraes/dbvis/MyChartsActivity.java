package br.unesp.amoraes.dbvis;

import java.util.ArrayList;
import java.util.List;

import org.achartengine.GraphicalView;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import br.unesp.amoraes.dbvis.charts.BarChart;
import br.unesp.amoraes.dbvis.charts.RoundChart;
import br.unesp.amoraes.dbvis.charts.ScatterChart;
import br.unesp.amoraes.dbvis.components.GalleryImageAdapter;
import br.unesp.amoraes.dbvis.logic.Visualization;
import br.unesp.amoraes.dbvis.userdata.UserChart;
import br.unesp.amoraes.dbvis.utils.FunctionsHelper;

public class MyChartsActivity extends Activity implements IBasicChartActivity{

	private ImageView leftArrowImageView;

	private ImageView rightArrowImageView;

	private Gallery gallery;

	private int selectedImagePosition = 0;

	private List<Drawable> drawables;
	private List<UserChart> userCharts;

	private GalleryImageAdapter galImageAdapter;

	private LinearLayout chartLayout;

	private GraphicalView chartView;

	private TextView chartText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_charts);
		getListOfPreviewsAndCharts();
		setupUI();
	}

	private void setupUI() {

		chartText = (TextView)findViewById(R.id.textViewMyChart);
		chartLayout = (LinearLayout)findViewById(R.id.linearLayoutMyChart);
		leftArrowImageView = (ImageView) findViewById(R.id.left_arrow_imageview);
		rightArrowImageView = (ImageView) findViewById(R.id.right_arrow_imageview);
		gallery = (Gallery) findViewById(R.id.gallery);

		leftArrowImageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (selectedImagePosition > 0) {
					--selectedImagePosition;

				}

				gallery.setSelection(selectedImagePosition, false);
				
			}
		});
			

		rightArrowImageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				if (selectedImagePosition < drawables.size() - 1) {
					++selectedImagePosition;

				}

				gallery.setSelection(selectedImagePosition, false);

			}
		});

		gallery.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {

				selectedImagePosition = pos;

				if (selectedImagePosition > 0
						&& selectedImagePosition < drawables.size() - 1) {

					leftArrowImageView.setImageDrawable(getResources()
							.getDrawable(R.drawable.arrow_left_enabled));
					rightArrowImageView.setImageDrawable(getResources()
							.getDrawable(R.drawable.arrow_right_enabled));

				} else if (selectedImagePosition == 0) {

					leftArrowImageView.setImageDrawable(getResources()
							.getDrawable(R.drawable.arrow_left_disabled));

				} else if (selectedImagePosition == drawables.size() - 1) {

					rightArrowImageView.setImageDrawable(getResources()
							.getDrawable(R.drawable.arrow_right_disabled));
				}

				changeBorderForSelectedImage(selectedImagePosition);
				openChart(selectedImagePosition);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}

		});

		galImageAdapter = new GalleryImageAdapter(this, drawables);

		gallery.setAdapter(galImageAdapter);

		if (drawables.size() > 0) {

			gallery.setSelection(selectedImagePosition, false);

		}

		if (drawables.size() == 1) {

			rightArrowImageView.setImageDrawable(getResources().getDrawable(
					R.drawable.arrow_right_disabled));
		}

	}

	private void changeBorderForSelectedImage(int selectedItemPos) {

		int count = gallery.getChildCount();

		for (int i = 0; i < count; i++) {

			ImageView imageView = (ImageView) gallery.getChildAt(i);
			imageView.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.image_border));
			imageView.setPadding(3, 3, 3, 3);

		}

		ImageView imageView = (ImageView) gallery.getSelectedView();
		imageView.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.selected_image_border));
		imageView.setPadding(3, 3, 3, 3);
	}

	private void getListOfPreviewsAndCharts() {

		drawables = new ArrayList<Drawable>();
		userCharts = new ArrayList<UserChart>();
		for(Integer k : Visualization.getInstance().getUserContents().getCharts().keySet()){
			UserChart uc = Visualization.getInstance().getUserContents().getCharts().get(k);
			if(uc.isDeleted() == false){
				drawables.add(new BitmapDrawable(uc.getImage()));
				userCharts.add(uc);
			}
		}	

	}

	private void openChart(int selectedPosition) {
		chartLayout.removeAllViews();
		UserChart uc = userCharts.get(selectedPosition);
		if(uc.getType() == KGlobal.CHART_TYPE_BAR){
			new BarChart(FunctionsHelper.parseXML(uc.getNodesInfo()),
					(!uc.getAxisX().equals("")?uc.getAxisX():null), 
					(!uc.getAxisY().equals("")?uc.getAxisY():null), 
					(!uc.getAxisZ().equals("")?uc.getAxisZ():null), 
					chartLayout, this);
		}else if(uc.getType() == KGlobal.CHART_TYPE_PIZZA){
			new RoundChart(FunctionsHelper.parseXML(uc.getNodesInfo()),
					(!uc.getAxisX().equals("")?uc.getAxisX():null), 
					chartLayout, this);
			
		}else if(uc.getType() == KGlobal.CHART_TYPE_POINT){
			new ScatterChart(FunctionsHelper.parseXML(uc.getNodesInfo()),
					(!uc.getAxisX().equals("")?uc.getAxisX():null), 
					(!uc.getAxisY().equals("")?uc.getAxisY():null),
					chartLayout, this);
			
		}
		chartText.setText(uc.getText());
		

	}

	@Override
	protected void onResume() {
		super.onResume();
		getListOfPreviewsAndCharts();
		setupUI();
	}

	@Override
	public void setChartView(GraphicalView gv) {
		this.chartView = gv;
	}

	@Override
	public Context getContext() {
		return this;
	}
	
	
}
