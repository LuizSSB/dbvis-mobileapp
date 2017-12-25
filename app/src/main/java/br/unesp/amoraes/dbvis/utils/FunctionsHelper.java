package br.unesp.amoraes.dbvis.utils;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import br.unesp.amoraes.dbvis.KGlobal;

import android.graphics.Color;
import android.util.Log;

public class FunctionsHelper {

	private static final String TAG = "DBVis::FunctionsHelper";

	public static List<GenericValuesModel> parseXML(String xml){
		
		try{
			BufferedReader br = new BufferedReader(new StringReader(xml));
			InputSource is = new InputSource(br);
			BasicXMLParser parser = new BasicXMLParser();
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser sp = factory.newSAXParser();
			XMLReader reader=sp.getXMLReader();
	        reader.setContentHandler(parser);
	        reader.parse(is);
	        List<GenericValuesModel> listModel = parser.list;
	        return listModel;
		}catch (Exception e) {
			Log.e(TAG, "Error parsing XML", e);
		}
		return null;
	}
	
	
	/**
	Generates a series of colors such that the
	distribution of the colors is (fairly) evenly spaced
	throughout the color spectrum. This is especially
	useful for generating unique color codes to be used
	in a legend or on a graph.

	@param numColors the number of colors to generate
	@return an array of Color objects representing the
	colors in the table
	*/
	public static int[] createColorCodeTable(int numColors)
	{
	   int[] table = new int[numColors];

	   if (numColors == 1)
	   {
	      // Special case for only one color
	      table[0] = Color.RED;
	   }
	   else
	   {
	     /* float hueMax = (float) 0.85;
	      float sat = (float) 0.8;

	      for (int i = 0; i < numColors; i++)
	      {
	         float hue = hueMax * i / (numColors-1);

	         // Here we interleave light colors and dark colors
	         // to get a wider distribution of colors.
	         if (i % 2 == 0)
	            table[i] = Color.HSVToColor(new float[]{hue, sat, (float)0.9});
	         else
	            table[i] = Color.HSVToColor(new float[]{hue, sat, (float)0.7});
	      }*/
		  int step = 255;
		  int i = 0;
		  while(true){
			  table[i] = Color.rgb(0, 0, step);
			  i++;
			  if(i == numColors) break;
			  table[i] = Color.rgb(0, step, 0);
			  i++;
			  if(i == numColors) break;
			  table[i] = Color.rgb(step, 0, 0);
			  i++;
			  if(i == numColors) break;
			  table[i] = Color.rgb(0, step, step);
			  i++;
			  if(i == numColors) break;
			  table[i] = Color.rgb(step, 0 , step);
			  i++;
			  if(i == numColors) break;
			  table[i] = Color.rgb(step, step, 0);
			  i++;
			  if(i == numColors) break;
			  step = step / 2;
		  }
	   }

	   return table;
	}
	
	public static boolean isNumberClass(String className){
		for(String k : KGlobal.NUMBER_CLASSES){
			if(k.equals(className)){
				return true;
			}
		}
		return false;
	}
}
