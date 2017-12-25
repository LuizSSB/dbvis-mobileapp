package br.unesp.amoraes.dbvis.userdata;

import java.util.Date;

import android.graphics.RectF;

public class UserText {
	private int id;
	private int x;
	private int y;
	private String text;
	private Date date;
	private String deviceName;
	private boolean deleted = false;
	private RectF lastRect = null;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
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
	public RectF getLastRect() {
		return lastRect;
	}
	public void setLastRect(RectF lastRect) {
		this.lastRect = lastRect;
	}
	public boolean isDeleted() {
		return deleted;
	}
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
	
}
