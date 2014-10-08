package com.planet_ink.coffee_mud.Libraries.interfaces;
import java.util.List;
import java.util.Map;
import com.planet_ink.coffee_mud.core.collections.XHashtable;
import com.planet_ink.coffee_mud.core.collections.XVector;

/*
   Copyright 2005-2014 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public interface XMLLibrary extends CMLibrary
{
	public static final String FILE_XML_BOUNDARY="<?xml version=\"1.0\"?>";

	/**
	 * Return the outer wrapper and contents of an XML tag <TNAME>Data</TNAME>
	 *
	 * <br><br><b>Usage:</b> Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
	 * @param TName Tag name to use
	 * @param Data the data
	 * @return String Information corresponding to the tname
	 */
	public String convertXMLtoTag(String TName, String Data);
	/**
	 * Return the outer wrapper and contents of an XML tag <TNAME>Data</TNAME>
	 *
	 * <br><br><b>Usage:</b> Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
	 * @param TName Tag name to use
	 * @param Data the data
	 * @return String Information corresponding to the tname
	 */
	public String convertXMLtoTag(String TName, int Data);
	/**
	 * Return the outer wrapper and contents of an XML tag <TNAME>Data</TNAME>
	 *
	 * <br><br><b>Usage:</b> Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
	 * @param TName Tag name to use
	 * @param Data the data
	 * @return String Information corresponding to the tname
	 */
	public String convertXMLtoTag(String TName, short Data);
	/**
	 * Return the outer wrapper and contents of an XML tag <TNAME>Data</TNAME>
	 *
	 * <br><br><b>Usage:</b> Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
	 * @param TName Tag name to use
	 * @param Data the data
	 * @return String Information corresponding to the tname
	 */
	public String convertXMLtoTag(String TName, boolean Data);
	/**
	 * Return the outer wrapper and contents of an XML tag <TNAME>Data</TNAME>
	 *
	 * <br><br><b>Usage:</b> Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
	 * @param TName Tag name to use
	 * @param Data the data
	 * @return String Information corresponding to the tname
	 */
	public String convertXMLtoTag(String TName, long Data);
	/**
	 * Return the contents of an XML tag, given the tag to search for
	 *
	 * <br><br><b>Usage:</b> String XML=returnXMLBlock(Response,"PDIModelErrors");
	 * @param Blob String to searh
	 * @param Tag Tag name to search for
	 * @return String Information corresponding to the tname
	 */
	public String returnXMLBlock(String Blob, String Tag);
	/**
	 * Returns the contents of a container tag, searched for in
	 * another container tags contents
	 * @param V the container tags contents
	 * @param tag the tag to look for
	 * @return the tags contained in tag, or null
	 */
	public List<XMLpiece> getContentsFromPieces(List<XMLpiece> V, String tag);
	/**
	 * Returns all tags inside the gives set that match this tag name
	 * @param V the container tags contents
	 * @param tag the tag to look for
	 * @return all the tags contained in tag
	 */
	public List<XMLpiece> getPiecesFromPieces(List<XMLpiece> V, String tag);
	/**
	 * Returns the xml tag node for the given tag name, if found in the
	 * given tag container contents
	 * @param V the tag container contents
	 * @param tag the tag name
	 * @return the xml tag node for the given tag name
	 */
	public XMLpiece getPieceFromPieces(List<XMLpiece> V, String tag);
	/**
	 * Returns the value of the tag, if it exists in the given
	 * tag collection
	 * @param V the tag collection (container tag)
	 * @param tag the tag to look for
	 * @return its value, or null
	 */
	public String getValFromPieces(List<XMLpiece> V, String tag);
	/**
	 * Return the data value within a given XML block
	 * <TAG>Data</TAG>
	 *
	 * <br><br><b>Usage:</b> String ThisColHead=getBoolFromPieces(ThisRow,"TD");
	 * @param V Pieces to search
	 * @param tag Tag to search for
	 * @return boolean Information from XML block
	 */
	public boolean getBoolFromPieces(List<XMLpiece> V, String tag);

	/**
	 * Return the data value within a given XML block
	 * <TAG>Data</TAG>
	 *
	 * <br><br><b>Usage:</b> String ThisColHead=getShortFromPieces(ThisRow,"TD");
	 * @param V Pieces to search
	 * @param tag Tag to search for
	 * @return short Information from XML block
	 */
	public short getShortFromPieces(List<XMLpiece> V, String tag);

	/**
	 * Return the data value within a given XML block
	 * <TAG>Data</TAG>
	 *
	 * <br><br><b>Usage:</b> String ThisColHead=getIntFromPieces(ThisRow,"TD");
	 * @param V Pieces to search
	 * @param tag Tag to search for
	 * @return int Information from XML block
	 */
	public int getIntFromPieces(List<XMLpiece> V, String tag);

	/**
	 * Return where the value is within a given XML block
	 * <TAG>Data</TAG>
	 *
	 * <br><br><b>Usage:</b> boolean ThisColHead=isTagInPieces(ThisRow,"TD");
	 * @param V Pieces to search
	 * @param tag Tag to search for
	 * @return boolean Information from XML block
	 */
	public boolean isTagInPieces(List<XMLpiece> V, String tag);

	/**
	 * Return the data value within a given XML block
	 * <TAG>Data</TAG>
	 *
	 * <br><br><b>Usage:</b> String ThisColHead=getLongFromPieces(ThisRow,"TD");
	 * @param V Pieces to search
	 * @param tag Tag to search for
	 * @return long Information from XML block
	 */
	public long getLongFromPieces(List<XMLpiece> V, String tag);

	/**
	 * Return the data value within a given XML block
	 * <TAG>Data</TAG>
	 *
	 * <br><br><b>Usage:</b> String ThisColHead=getDoubleFromPieces(ThisRow,"TD");
	 * @param V Pieces to search
	 * @param tag Tag to search for
	 * @return double Information from XML block
	 */
	public double getDoubleFromPieces(List<XMLpiece> V, String tag);

	/**
	 * Returns the value of the tag, if it exists in the given
	 * tag collection
	 * @param V the tag collection (container tag)
	 * @param tag the tag to look for
	 * @param defValue the value to return if the tag doesn't exist
	 * @return the tags value, or defValue
	 */
	public String getValFromPieces(List<XMLpiece> V, String tag, String defValue);
	/**
	 * Return the data value within a given XML block
	 * <TAG>Data</TAG>
	 *
	 * <br><br><b>Usage:</b> String ThisColHead=getBoolFromPieces(ThisRow,"TD");
	 * @param V Pieces to search
	 * @param tag Tag to search for
	 * @param defValue the value to return if the tag doesn't exist
	 * @return the tags value, or defValue
	 */
	public boolean getBoolFromPieces(List<XMLpiece> V, String tag, boolean defValue);

	/**
	 * Return the data value within a given XML block
	 * <TAG>Data</TAG>
	 *
	 * <br><br><b>Usage:</b> String ThisColHead=getShortFromPieces(ThisRow,"TD");
	 * @param V Pieces to search
	 * @param tag Tag to search for
	 * @param defVal the value to return if the tag doesn't exist
	 * @return the tags value, or defValue
	 */
	public short getShortFromPieces(List<XMLpiece> V, String tag, short defVal);

	/**
	 * Return the data value within a given XML block
	 * <TAG>Data</TAG>
	 *
	 * <br><br><b>Usage:</b> String ThisColHead=getIntFromPieces(ThisRow,"TD");
	 * @param V Pieces to search
	 * @param tag Tag to search for
	 * @param defVal the value to return if the tag doesn't exist
	 * @return the tags value, or defValue
	 */
	public int getIntFromPieces(List<XMLpiece> V, String tag, int defVal);

	/**
	 * Return the data value within a given XML block
	 * <TAG>Data</TAG>
	 *
	 * <br><br><b>Usage:</b> String ThisColHead=getLongFromPieces(ThisRow,"TD");
	 * @param V Pieces to search
	 * @param tag Tag to search for
	 * @param defVal the value to return if the tag doesn't exist
	 * @return the tags value, or defValue
	 */
	public long getLongFromPieces(List<XMLpiece> V, String tag, long defVal);

	/**
	 * Return the data value within a given XML block
	 * <TAG>Data</TAG>
	 *
	 * <br><br><b>Usage:</b> String ThisColHead=getDoubleFromPieces(ThisRow,"TD");
	 * @param V Pieces to search
	 * @param tag Tag to search for
	 * @param defVal the value to return if the tag doesn't exist
	 * @return the tags value, or defValue
	 */
	public double getDoubleFromPieces(List<XMLpiece> V, String tag, double defVal);
	/**
	 * Parses all xml inside the given string buffer and returns
	 * the root tags as a container collection.
	 * @param buf the string to parse
	 * @return the parsed xml
	 */
	public List<XMLpiece> parseAllXML(String buf);

	/**
	 * Parses all xml inside the given stringbuffer and returns
	 * the root tags as a container collection.
	 * @param buf the string to parse
	 * @return the parsed xml
	 */
	public List<XMLpiece> parseAllXML(StringBuffer buf);

	/**
	 * Parses a list of single-level xml tags, together in string.
	 * This method assumes that the given string is a series of
	 * top level tags, with no child tags, and no attributes. It
	 * will parse the tags, and return their values (only) as
	 * a list of strings.  Any deconversions for compatibility
	 * are also performed
	 * @param numberedList the top level xml tags
	 * @return the list of strings with the values of those tags
	 */
	public List<String> parseXMLList(String numberedList);

	/**
	 * Converts the given list of strings into a set of top-level
	 * xml tags called simply <X>
	 * @param V the list of strings
	 * @return the top level xml tags
	 */
	public String getXMLList(List<String> V);

	/**
	 * Return the data value within the first XML block
	 * <TAG>Data</TAG>
	 *
	 * <br><br><b>Usage:</b> String ThisColHead=returnXMLValue(ThisRow);
	 * @param Blob String to searh
	 * @return String Information from first XML block
	 */
	public String returnXMLValue(String Blob);

	/**
	 * Return the data value within a given XML block
	 * <TAG>Data</TAG>
	 *
	 * <br><br><b>Usage:</b> String ThisColHead=returnXMLValue(ThisRow,"TD");
	 * @param Blob String to search
	 * @param Tag Tag to search for
	 * @return String Information from XML block
	 */
	public String returnXMLValue(String Blob, String Tag);

	/**
	 * Return the data value within a given XML block
	 * <TAG>Data</TAG>
	 *
	 * <br><br><b>Usage:</b> String ThisColHead=returnXMLValue(ThisRow,"TD");
	 * @param Blob String to search
	 * @param Tag Tag to search for
	 * @return String Information from XML block
	 */
	public boolean returnXMLBoolean(String Blob, String Tag);

	/**
	 * Return a parameter value within an XML tag
	 * <TAG Parameter="VALUE">
	 *
	 * <br><br><b>Usage:</b> String ThisColHead=getParmValue(parmSet,"TD");
	 * @param parmSet set of parms to search
	 * @param Tag Tag to search for
	 * @return String Parameter value
	 */
	public String getParmValue(Map<String, String> parmSet, String Tag);

	/**
	 * parse a tag value for safety
	 *
	 * <br><br><b>Usage:</b> String val=parseOutAngleBrackets(ThisValue);
	 * @param s String to parse
	 * @return String parsed value
	 */
	public String parseOutAngleBrackets(String s);

	/**
	 * parse a tag value for safety
	 *
	 * <br><br><b>Usage:</b> String val=parseOutAngleBracketsAndQuotes(ThisValue);
	 * @param s String to parse
	 * @return String parsed value
	 */
	public String parseOutAngleBracketsAndQuotes(String s);
	/**
	 * restore a tag value parsed for safety
	 *
	 * <br><br><b>Usage:</b> String val=restoreAngleBrackets(ThisValue);
	 * @param s String to parse
	 * @return String unparsed value
	 */
	public String restoreAngleBrackets(String s);

	/**
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static class XMLpiece implements Cloneable
	{
		public String tag="";
		public String value="";
		public List<XMLpiece> contents=new XVector<XMLpiece>();
		public Map<String,String> parms=new XHashtable<String,String>();
		public XMLpiece parent=null;
		public int outerStart=-1;
		public int innerStart=-1;
		public int innerEnd=-1;
		public int outerEnd=-1;

		public XMLpiece()
		{
			
		}
		
		public XMLpiece(String tag, String value)
		{
			this.tag=tag.toUpperCase().trim();
			this.value=value;
		}
		
		public XMLpiece copyOf()
		{
			try
			{
				final XMLpiece piece2=(XMLpiece)this.clone();
				piece2.contents=new XVector<XMLpiece>(contents);
				piece2.parms=new XHashtable<String,String>(parms);
				return piece2;
			}
			catch(final Exception e)
			{
				return this;
			}
		}

		public void addContent(XMLpiece x)
		{
			if (x == null) return;
			if (contents == null) 
				contents = new XVector<XMLpiece>();
			x.parent=this;
			contents.add(x);
		}

		@Override
		public String toString()
		{
			final StringBuilder str=new StringBuilder("");
			str.append("<").append(tag);
			for(final String parm : parms.keySet())
				str.append(" ").append(parm).append("=\"").append(parms.get(parm)).append("\"");
			str.append(">").append(value).append("</").append(tag).append(">");
			return str.toString();
		}
	}
}
