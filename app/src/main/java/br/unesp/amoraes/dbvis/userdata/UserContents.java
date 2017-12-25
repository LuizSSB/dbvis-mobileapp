package br.unesp.amoraes.dbvis.userdata;

import java.util.HashMap;

public class UserContents {
	private HashMap<Integer, UserText> texts = new HashMap<Integer, UserText>();
	private HashMap<Integer, UserChart> charts = new HashMap<Integer, UserChart>();

	public HashMap<Integer, UserText> getTexts() {
		return texts;
	}

	public void setTexts(HashMap<Integer, UserText> texts) {
		this.texts = texts;
	}

	public HashMap<Integer, UserChart> getCharts() {
		return charts;
	}

	public void setCharts(HashMap<Integer, UserChart> charts) {
		this.charts = charts;
	}
	
}
