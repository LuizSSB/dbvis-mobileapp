package br.unesp.amoraes.dbvis.userdata;

import java.util.Date;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;

public class UserChart {
	private int id;
	private String text;
	private Date date;
	private String deviceName;
	private Bitmap image;
	private String nodesInfo;
	private String axisX;
	private String axisY;
	private String axisZ;
	private boolean deleted = false;
	private int type;
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public Bitmap getImage() {
		return image;
	}
	public void setImage(Bitmap image) {
		this.image = image;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getNodesInfo() {
		return nodesInfo;
	}
	public String getAxisX() {
		return axisX;
	}
	public String getAxisY() {
		return axisY;
	}
	public String getAxisZ() {
		return axisZ;
	}
	public void setNodesInfo(String nodeInfo) {
		this.nodesInfo = nodeInfo;
	}
	public void setAxisX(String axisX) {
		this.axisX = axisX;
	}
	public void setAxisY(String axisY) {
		this.axisY = axisY;
	}
	public void setAxisZ(String axisZ) {
		this.axisZ = axisZ;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public boolean isDeleted() {
		return deleted;
	}
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
}
