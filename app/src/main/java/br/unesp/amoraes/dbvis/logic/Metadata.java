package br.unesp.amoraes.dbvis.logic;

import java.util.HashMap;

public class Metadata {

	private static Metadata instance;
	
	private Metadata(){
		
	}
	
	public static Metadata getInstance(){
		if(instance == null)
			instance = new Metadata();
		return instance;
	}
	
	private String idColumn;
	private String labelColumn;
	private HashMap<String, String> columns = new HashMap<String, String>();
	
	public String getIdColumn() {
		return idColumn;
	}
	public void setIdColumn(String idColumn) {
		this.idColumn = idColumn;
	}
	public String getLabelColumn() {
		return labelColumn;
	}
	public void setLabelColumn(String labelColumn) {
		this.labelColumn = labelColumn;
	}
	public HashMap<String, String> getColumns() {
		return columns;
	}
	public void setColumns(HashMap<String, String> columns) {
		this.columns = columns;
	}
	
}
