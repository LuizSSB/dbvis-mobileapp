package br.unesp.amoraes.dbvis.utils;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

//SAX parser to parse the entity 
public class BasicXMLParser extends DefaultHandler
{         
         List<GenericValuesModel> list=null;        
         // string builder acts as a buffer
         StringBuilder builder;
         GenericValuesModel values=null;
         boolean insideData = false;
         // Initialize the arraylist
         // @throws SAXException
         @Override
         public void startDocument() throws SAXException {
             /******* Create ArrayList To Store XmlValuesModel object ******/
             list = new ArrayList<GenericValuesModel>();
         }    // Initialize the temp XmlValuesModel object which will hold the parsed info
         // and the string builder that will store the read characters
         // @param uri
         // @param localName ( Parsed Node name will come in localName  )
         // @param qName
         // @param attributes
         // @throws SAXException
         @Override
         public void startElement(String uri, String localName, String qName,
                 Attributes attributes) throws SAXException {
              
             /****  When New XML Node initiating to parse this function called *****/
              
             // Create StringBuilder object to store xml node value
             builder=new StringBuilder();
              
             if(localName.equalsIgnoreCase("Node")){
                 /********** Create Model Object  *********/
                 values = new GenericValuesModel();
             }else if(localName.equalsIgnoreCase("data")){
            	 insideData = true;
             }
         }
         // Finished reading the login tag, add it to arraylist
         // @param uri
         // @param localName
         // @param qName
         // @throws SAXException
         @Override
         public void endElement(String uri, String localName, String qName)
                 throws SAXException {
             if(localName.equals("Node")){
                 list.add( values );
             }
             else  if(localName.equalsIgnoreCase("id")){  
                 values.setId(builder.toString());
             }
             else if(localName.equalsIgnoreCase("label")){
            	 values.setLabel(builder.toString());
             }
             else if(localName.equalsIgnoreCase("data")){
            	 insideData = false;
             }
             else if(insideData){
            	 values.getValues().put(localName.substring(1), builder.toString());
             }
         }
         // Read the value of each xml NODE
         // @param ch
         // @param start
         // @param length
         // @throws SAXException         
         @Override
         public void characters(char[] ch, int start, int length)
                 throws SAXException {
			/****** Read the characters and append them to the buffer ******/
			String tempString = new String(ch, start, length);
			builder.append(tempString);
         }
}