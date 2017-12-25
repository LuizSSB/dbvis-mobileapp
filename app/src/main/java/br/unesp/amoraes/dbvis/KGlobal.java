package br.unesp.amoraes.dbvis;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * My nice "constants" file
 * @author Alessandro Moraes
 *
 */
public class KGlobal {
	public static final String PREFS_GENERAL_FILE = "DBVisPreferences";
	public static final String PREFS_LAST_SERVER = "PREFS_LAST_SERVER";
	public static final String PREFS_LAST_PASSWORD = "PREFS_LAST_PASSWORD";
	public static final String PREFS_LAST_DEVICE_NAME = "PREFS_LAST_DEVICE_NAME";
	public static final String PREFS_CALIB_LIMIT_RIGHT = "PREFS_CALIB_LIMIT_RIGHT";
	public static final String PREFS_CALIB_LIMIT_LEFT = "PREFS_CALIB_LIMIT_LEFT";
	public static final String PREFS_CALIB_LIMIT_TOP = "PREFS_CALIB_LIMIT_TOP";
	public static final String PREFS_CALIB_LIMIT_BOTTOM = "PREFS_CALIB_LIMIT_DOWN";
	
	public static final String PARAM_GLOBAL_X = "PARAM_GLOBAL_X";
	public static final String PARAM_GLOBAL_Y = "PARAM_GLOBAL_Y";
	public static final String PARAM_GLOBAL_WIDTH = "PARAM_GLOBAL_WIDTH";
	public static final String PARAM_GLOBAL_HEIGHT = "PARAM_GLOBAL_HEIGHT";
	
	public static final String USER_CHARTS_FILE_PREFIX = "userCharts_";
	public static final String USER_CHARTS_FILE_SUFIX = ".png";
	
	public static final int CHART_TYPE_PIZZA = 0;
	public static final int CHART_TYPE_BAR = 1;
	public static final int CHART_TYPE_POINT = 2;
	
	public static final String[] NUMBER_CLASSES = new String[]{BigDecimal.class.getName()
        , BigInteger.class.getName()
        , Byte.class.getName()
        , Double.class.getName()
        , Float.class.getName()
        , Integer.class.getName()
        , Long.class.getName()
        , Short.class.getName()
        , Boolean.class.getName()};
	public static final String PARAM_WINDOW_WIDTH = "PARAM_WINDOW_WIDTH";
	public static final String PARAM_WINDOW_HEIGHT = "PARAM_WINDOW_HEIGHT";
	public static final long USER_CONTENT_UPDATE_INTERVAL = 2000;
	public static final long USER_POSITION_UPDATE_INTERVAL = 33;
	public static final long CURRENT_IMAGE_UPDATE_INTERVAL = 10000;
	
}
