package br.unesp.amoraes.dbvis.logic;

import java.util.Observable;

import android.graphics.Bitmap;
import br.unesp.amoraes.dbvis.exception.AlreadyConnectedException;
import br.unesp.amoraes.dbvis.exception.InvalidPasswordException;
import br.unesp.amoraes.dbvis.exception.NotConnectedException;
import br.unesp.amoraes.dbvis.userdata.UserChart;
import br.unesp.amoraes.dbvis.userdata.UserText;

public abstract class ServerConnection extends Observable {
	abstract public boolean isConnected();

	abstract public boolean connect(String server, String password, String deviceName)
			throws AlreadyConnectedException, InvalidPasswordException;

	abstract public void disconnect() throws NotConnectedException;

	abstract public String getCurrentImages() throws NotConnectedException; 
	
	abstract public Bitmap getFullDisplayImage() throws NotConnectedException;
	
	abstract public void setCurrentImageTimestamp(long timestamp);
	
	abstract public long getCurrentImageTimestamp();

	abstract public void updatePosition(int x, int y, int width, int height) throws NotConnectedException;;

	abstract public String getNodeByPoint(int x, int y) throws NotConnectedException;

	abstract public String getNodeByArea(int x, int y, int width, int height) throws NotConnectedException;
			
	abstract public void selectPoint(int x, int y) throws NotConnectedException;
	
	abstract public void selectArea(int x, int y, int width, int height) throws NotConnectedException;
	
	abstract public String getDeviceName();
	
	abstract public void addUserText(UserText userText) throws NotConnectedException;
	
	abstract public void removeUserText(int id) throws NotConnectedException;
	
	abstract public String getUsersTexts(long timestamp) throws NotConnectedException; 
	
	abstract public void addUserChart(UserChart userChart) throws NotConnectedException;
	
	abstract public Bitmap getUserChartImage(int id) throws NotConnectedException;
	
	abstract public long getTimestamp() throws NotConnectedException;
	
	abstract public String getUsersCharts(long timestamp) throws NotConnectedException;
	
	
	
	abstract public void removeUserChart(int id) throws NotConnectedException;
	
	abstract public boolean hasNewOhterUsersCharts();

	abstract public void setHasNewOtherUsersCharts(boolean b);
	
}
