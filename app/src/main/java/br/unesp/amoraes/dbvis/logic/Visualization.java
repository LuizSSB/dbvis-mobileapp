package br.unesp.amoraes.dbvis.logic;

import java.util.ArrayList;

import br.unesp.amoraes.dbvis.userdata.CurrentImage;
import br.unesp.amoraes.dbvis.userdata.UserContents;

/**
 * Singleton that represents the current Visualization
 * @author Alessandro Moraes
 *
 */
public class Visualization {

	private static Visualization instance;
	public static Visualization getInstance(){
		if(instance == null)
			instance = new Visualization();
		return instance;
	}
	
	private ServerConnection serverConnection;
	private UserContents userContents = new UserContents();
	private UserContents otherUsersContents = new UserContents();
	private ArrayList<CurrentImage> currentImages = new ArrayList<CurrentImage>();
	private VisualizationPreferences preferences = new VisualizationPreferences();
	
	public ServerConnection getServerConnection() {
		return serverConnection;
	}

	public void setServerConnection(ServerConnection serverConnection) {
		this.serverConnection = serverConnection;
	}
	
	
	public UserContents getUserContents() {
		return userContents;
	}

	public void setUserContents(UserContents userContents) {
		this.userContents = userContents;
	}

	public UserContents getOtherUsersContents() {
		return otherUsersContents;
	}

	public void setOtherUsersContents(UserContents otherUsersContents) {
		this.otherUsersContents = otherUsersContents;
	}

	public VisualizationPreferences getPreferences() {
		return preferences;
	}

	public void setPreferences(VisualizationPreferences preferences) {
		this.preferences = preferences;
	}

	public ArrayList<CurrentImage> getCurrentImages() {
		return currentImages;
	}

	public void setCurrentImages(ArrayList<CurrentImage> currentImages) {
		this.currentImages = currentImages;
	}
	
	

	
	
	
}
