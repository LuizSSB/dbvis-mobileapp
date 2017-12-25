package br.unesp.amoraes.dbvis.utils;

import java.util.HashMap;

public class GenericValuesModel {
	private String id;
	private String label;
	private HashMap<String, String> values = new HashMap<String, String>();
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public HashMap<String, String> getValues() {
		return values;
	}
	public void setValues(HashMap<String, String> values) {
		this.values = values;
	}
	
}
