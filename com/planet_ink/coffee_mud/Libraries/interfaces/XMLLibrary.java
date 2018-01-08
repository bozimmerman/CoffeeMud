package com.planet_ink.coffee_mud.Libraries.interfaces;
import java.util.List;
import java.util.Map;

import com.planet_ink.coffee_mud.core.collections.XHashtable;
import com.planet_ink.coffee_mud.core.collections.XVector;

/*
   Copyright 2005-2018 Bo Zimmerman

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
	 * Return the outer wrapper and contents of an XML tag &lt;TNAME&gt;Data&lt;/TNAME&gt;
	 *
	 * Usage: Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
	 * @param TName Tag name to use
	 * @param Data the data
	 * @return String Information corresponding to the tname
	 */
	public String convertXMLtoTag(String TName, String Data);
	/**
	 * Return the outer wrapper and contents of an XML tag &lt;TNAME&gt;Data&lt;/TNAME&gt;
	 *
	 * Usage: Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
	 * @param TName Tag name to use
	 * @param Data the data
	 * @return String Information corresponding to the tname
	 */
	public String convertXMLtoTag(String TName, int Data);
	/**
	 * Return the outer wrapper and contents of an XML tag &lt;TNAME&gt;Data&lt;/TNAME&gt;
	 *
	 * Usage: Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
	 * @param TName Tag name to use
	 * @param Data the data
	 * @return String Information corresponding to the tname
	 */
	public String convertXMLtoTag(String TName, short Data);
	/**
	 * Return the outer wrapper and contents of an XML tag &lt;TNAME&gt;Data&lt;/TNAME&gt;
	 *
	 * Usage: Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
	 * @param TName Tag name to use
	 * @param Data the data
	 * @return String Information corresponding to the tname
	 */
	public String convertXMLtoTag(String TName, boolean Data);
	/**
	 * Return the outer wrapper and contents of an XML tag &lt;TNAME&gt;Data&lt;/TNAME&gt;
	 *
	 * Usage: Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
	 * @param TName Tag name to use
	 * @param Data the data
	 * @return String Information corresponding to the tname
	 */
	public String convertXMLtoTag(String TName, long Data);
	/**
	 * Return the contents of an XML tag, given the tag to search for
	 *
	 * Usage: String XML=returnXMLBlock(Response,"PDIModelErrors");
	 * @param Blob String to searh
	 * @param Tag Tag name to search for
	 * @return String Information corresponding to the tname
	 */
	public String returnXMLBlock(String Blob, String Tag);
	
	/**
	 * Parses all xml inside the given string buffer and returns
	 * the root tags as a container collection.
	 * @param buf the string to parse
	 * @return the parsed xml
	 */
	public List<XMLTag> parseAllXML(String buf);

	/**
	 * Parses all xml inside the given stringbuffer and returns
	 * the root tags as a container collection.
	 * @param buf the string to parse
	 * @return the parsed xml
	 */
	public List<XMLTag> parseAllXML(StringBuffer buf);

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
	 * xml tags called simply &lt;X&gt;
	 * @param V the list of strings
	 * @return the top level xml tags
	 */
	public String getXMLList(List<String> V);

	/**
	 * Return the data value within the first XML block
	 * &lt;TAG&gt;Data&lt;/TAG&gt;
	 *
	 * Usage: String ThisColHead=returnXMLValue(ThisRow);
	 * @param Blob String to searh
	 * @return String Information from first XML block
	 */
	public String returnXMLValue(String Blob);

	/**
	 * Return the data value within a given XML block
	 * &lt;TAG&gt;Data&lt;/TAG&gt;
	 *
	 * Usage: String ThisColHead=returnXMLValue(ThisRow,"TD");
	 * @param Blob String to search
	 * @param Tag Tag to search for
	 * @return String Information from XML block
	 */
	public String returnXMLValue(String Blob, String Tag);

	/**
	 * Return the data value within a given XML block
	 * &lt;TAG&gt;Data&lt;/TAG&gt;
	 *
	 * Usage: String ThisColHead=returnXMLValue(ThisRow,"TD");
	 * @param Blob String to search
	 * @param Tag Tag to search for
	 * @return String Information from XML block
	 */
	public boolean returnXMLBoolean(String Blob, String Tag);

	/**
	 * parse a tag value for safety
	 *
	 * Usage: String val=parseOutAngleBrackets(ThisValue);
	 * @param s String to parse
	 * @return String parsed value
	 */
	public String parseOutAngleBrackets(String s);

	/**
	 * parse a tag value for safety
	 *
	 * Usage: String val=parseOutAngleBracketsAndQuotes(ThisValue);
	 * @param s String to parse
	 * @return String parsed value
	 */
	public String parseOutAngleBracketsAndQuotes(String s);
	/**
	 * restore a tag value parsed for safety
	 *
	 * Usage: String val=restoreAngleBrackets(ThisValue);
	 * @param s String to parse
	 * @return String unparsed value
	 */
	public String restoreAngleBrackets(String s);

	/**
	 * Create a new tag, just like the XML Manager does.
	 * @param key the tag name/key
	 * @param value the value of the inside of the tag, if any.
	 * @return the new tag object
	 */
	public XMLTag createNewTag(String key, String value);
	
	/**
	 * Returns the contents of a container tag, searched for in
	 * another container tags contents
	 * @param V the container tags contents
	 * @param tag the tag to look for
	 * @return the tags contained in tag, or null
	 */
	public List<XMLTag> getContentsFromPieces(List<XMLTag> V, String tag);
	/**
	 * Returns all tags inside the gives set that match this tag name
	 * @param V the container tags contents
	 * @param tag the tag to look for
	 * @return all the tags contained in tag
	 */
	public List<XMLTag> getPiecesFromPieces(List<XMLTag> V, String tag);
	/**
	 * Returns the xml tag node for the given tag name, if found in the
	 * given tag container contents
	 * @param V the tag container contents
	 * @param tag the tag name
	 * @return the xml tag node for the given tag name
	 */
	public XMLTag getPieceFromPieces(List<XMLTag> V, String tag);
	/**
	 * Returns the value of the tag, if it exists in the given
	 * tag collection
	 * @param V the tag collection (container tag)
	 * @param tag the tag to look for
	 * @return its value, or null
	 */
	public String getValFromPieces(List<XMLTag> V, String tag);
	/**
	 * Return the data value within a given XML block
	 * &lt;TAG&gt;Data&lt;/TAG&gt;
	 *
	 * Usage: String ThisColHead=getBoolFromPieces(ThisRow,"TD");
	 * @param V Pieces to search
	 * @param tag Tag to search for
	 * @return boolean Information from XML block
	 */
	public boolean getBoolFromPieces(List<XMLTag> V, String tag);

	/**
	 * Return the data value within a given XML block
	 * &lt;TAG&gt;Data&lt;/TAG&gt;
	 *
	 * Usage: String ThisColHead=getShortFromPieces(ThisRow,"TD");
	 * @param V Pieces to search
	 * @param tag Tag to search for
	 * @return short Information from XML block
	 */
	public short getShortFromPieces(List<XMLTag> V, String tag);

	/**
	 * Return the data value within a given XML block
	 * &lt;TAG&gt;Data&lt;/TAG&gt;
	 *
	 * Usage: String ThisColHead=getIntFromPieces(ThisRow,"TD");
	 * @param V Pieces to search
	 * @param tag Tag to search for
	 * @return int Information from XML block
	 */
	public int getIntFromPieces(List<XMLTag> V, String tag);

	/**
	 * Return where the value is within a given XML block
	 * &lt;TAG&gt;Data&lt;/TAG&gt;
	 *
	 * Usage: boolean ThisColHead=isTagInPieces(ThisRow,"TD");
	 * @param V Pieces to search
	 * @param tag Tag to search for
	 * @return boolean Information from XML block
	 */
	public boolean isTagInPieces(List<XMLTag> V, String tag);

	/**
	 * Return the data value within a given XML block
	 * &lt;TAG&gt;Data&lt;/TAG&gt;
	 *
	 * Usage: String ThisColHead=getLongFromPieces(ThisRow,"TD");
	 * @param V Pieces to search
	 * @param tag Tag to search for
	 * @return long Information from XML block
	 */
	public long getLongFromPieces(List<XMLTag> V, String tag);

	/**
	 * Return the data value within a given XML block
	 * &lt;TAG&gt;Data&lt;/TAG&gt;
	 *
	 * Usage: String ThisColHead=getDoubleFromPieces(ThisRow,"TD");
	 * @param V Pieces to search
	 * @param tag Tag to search for
	 * @return double Information from XML block
	 */
	public double getDoubleFromPieces(List<XMLTag> V, String tag);

	/**
	 * Returns the value of the tag, if it exists in the given
	 * tag collection
	 * @param V the tag collection (container tag)
	 * @param tag the tag to look for
	 * @param defValue the value to return if the tag doesn't exist
	 * @return the tags value, or defValue
	 */
	public String getValFromPieces(List<XMLTag> V, String tag, String defValue);
	/**
	 * Return the data value within a given XML block
	 * &lt;TAG&gt;Data&lt;/TAG&gt;
	 *
	 * Usage: String ThisColHead=getBoolFromPieces(ThisRow,"TD");
	 * @param V Pieces to search
	 * @param tag Tag to search for
	 * @param defValue the value to return if the tag doesn't exist
	 * @return the tags value, or defValue
	 */
	public boolean getBoolFromPieces(List<XMLTag> V, String tag, boolean defValue);

	/**
	 * Return the data value within a given XML block
	 * &lt;TAG&gt;Data&lt;/TAG&gt;
	 *
	 * Usage: String ThisColHead=getShortFromPieces(ThisRow,"TD");
	 * @param V Pieces to search
	 * @param tag Tag to search for
	 * @param defVal the value to return if the tag doesn't exist
	 * @return the tags value, or defValue
	 */
	public short getShortFromPieces(List<XMLTag> V, String tag, short defVal);

	/**
	 * Return the data value within a given XML block
	 * &lt;TAG&gt;Data&lt;/TAG&gt;
	 *
	 * Usage: String ThisColHead=getIntFromPieces(ThisRow,"TD");
	 * @param V Pieces to search
	 * @param tag Tag to search for
	 * @param defVal the value to return if the tag doesn't exist
	 * @return the tags value, or defValue
	 */
	public int getIntFromPieces(List<XMLTag> V, String tag, int defVal);

	/**
	 * Return the data value within a given XML block
	 * &lt;TAG&gt;Data&lt;/TAG&gt;
	 *
	 * Usage: String ThisColHead=getLongFromPieces(ThisRow,"TD");
	 * @param V Pieces to search
	 * @param tag Tag to search for
	 * @param defVal the value to return if the tag doesn't exist
	 * @return the tags value, or defValue
	 */
	public long getLongFromPieces(List<XMLTag> V, String tag, long defVal);

	/**
	 * Return the data value within a given XML block
	 * &lt;TAG&gt;Data&lt;/TAG&gt;
	 *
	 * Usage: String ThisColHead=getDoubleFromPieces(ThisRow,"TD");
	 * @param V Pieces to search
	 * @param tag Tag to search for
	 * @param defVal the value to return if the tag doesn't exist
	 * @return the tags value, or defValue
	 */
	public double getDoubleFromPieces(List<XMLTag> V, String tag, double defVal);
	
	/**
	 * Converts a pojo object to a XML document.
	 * @param o the object to convert
	 * @return the XML document
	 */
	public String fromPOJOtoXML(Object o);
	
	/**
	 * Converts a xml document to a XML object.
	 * @param XML the XML document
	 * @param o the object to convert
	 */
	public void fromXMLtoPOJO(String XML, Object o);

	/**
	 * Converts a xml object to a pojo object.
	 * @param xmlObj the xml object
	 * @param o the object to convert
	 */
	public void fromXMLtoPOJO(List<XMLTag> xmlObj, Object o);
	
	public interface XMLTag
	{
		/* (non-Javadoc)
		 * @see com.planet_ink.coffee_mud.Libraries.interfaces.XMLTag#copyOf()
		 */
		public XMLTag copyOf();

		/* (non-Javadoc)
		 * @see com.planet_ink.coffee_mud.Libraries.interfaces.XMLTag#addContent(com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag)
		 */
		public void addContent(XMLTag x);

		@Override
		public String toString();

		/**
		 * @return the tag
		 */
		public String tag();

		/**
		 * @return the value
		 */
		public String value();

		/**
		 * @return the contents
		 */
		public List<XMLTag> contents();

		/**
		 * @return the parms
		 */
		public Map<String, String> parms();

		/**
		 * @return the parent
		 */
		public XMLTag parent();

		/**
		 * @return the outerStart
		 */
		public int outerStartIndex();

		/**
		 * @return the innerStart
		 */
		public int innerStartIndex();

		/**
		 * @return the innerEnd
		 */
		public int innerEndIndex();

		/**
		 * @return the outerEnd
		 */
		public int outerEndIndex();
		
		/**
		 * Return a parameter value within an XML tag
		 * &lt;TAG Parameter="VALUE"&gt;
		 *
		 * Usage: String ThisColHead=getParmValue(parmSet,"TD");
		 * @param Tag Tag to search for
		 * @return String Parameter value
		 */
		public String getParmValue(String Tag);
		
		/**
		 * Return the data value within a given XML block
		 * &lt;TAG&gt;Data&lt;/TAG&gt;
		 *
		 * Usage: String ThisColHead=getDoubleFromPieces(ThisRow,"TD");
		 * @param tag Tag to search for
		 * @param defVal the value to return if the tag doesn't exist
		 * @return the tags value, or defValue
		 */
		public double getDoubleFromPieces(String tag, double defVal);
		
		/**
		 * Returns the xml tag node for the given tag name, if found in the
		 * given tag container contents
		 * @param tag the tag name
		 * @return the xml tag node for the given tag name
		 */
		public XMLTag getPieceFromPieces(String tag);
		
		/**
		 * Return the data value within a given XML block
		 * &lt;TAG&gt;Data&lt;/TAG&gt;
		 *
		 * Usage: String ThisColHead=getBoolFromPieces(ThisRow,"TD");
		 * @param tag Tag to search for
		 * @param defValue the value to return if the tag doesn't exist
		 * @return the tags value, or defValue
		 */
		public boolean getBoolFromPieces(String tag, boolean defValue);

		/**
		 * Return the data value within a given XML block
		 * &lt;TAG&gt;Data&lt;/TAG&gt;
		 *
		 * Usage: String ThisColHead=getShortFromPieces(ThisRow,"TD");
		 * @param tag Tag to search for
		 * @param defVal the value to return if the tag doesn't exist
		 * @return the tags value, or defValue
		 */
		public short getShortFromPieces(String tag, short defVal);

		/**
		 * Return the data value within a given XML block
		 * &lt;TAG&gt;Data&lt;/TAG&gt;
		 *
		 * Usage: String ThisColHead=getIntFromPieces(ThisRow,"TD");
		 * @param tag Tag to search for
		 * @param defVal the value to return if the tag doesn't exist
		 * @return the tags value, or defValue
		 */
		public int getIntFromPieces(String tag, int defVal);

		/**
		 * Return the data value within a given XML block
		 * &lt;TAG&gt;Data&lt;/TAG&gt;
		 *
		 * Usage: String ThisColHead=getLongFromPieces(ThisRow,"TD");
		 * @param tag Tag to search for
		 * @param defVal the value to return if the tag doesn't exist
		 * @return the tags value, or defValue
		 */
		public long getLongFromPieces(String tag, long defVal);
		
		/**
		 * Returns the value of the tag, if it exists in the given
		 * tag collection
		 * @param tag the tag to look for
		 * @return its value, or null
		 */
		public String getValFromPieces(String tag);
		
		/**
		 * Returns the value of the tag, if it exists in the given
		 * tag collection
		 * @param tag the tag to look for
		 * @param defValue the value to return if the tag doesn't exist
		 * @return the tags value, or defValue
		 */
		public String getValFromPieces(String tag, String defValue);
		
		/**
		 * Returns the contents of a container tag, searched for in
		 * another container tags contents
		 * @param tag the tag to look for
		 * @return the tags contained in tag, or null
		 */
		public List<XMLTag> getContentsFromPieces(String tag);
		/**
		 * Returns all tags inside the gives set that match this tag name
		 * @param tag the tag to look for
		 * @return all the tags contained in tag
		 */
		public List<XMLTag> getPiecesFromPieces(String tag);
		/**
		 * Return the data value within a given XML block
		 * &lt;TAG&gt;Data&lt;/TAG&gt;
		 *
		 * Usage: String ThisColHead=getBoolFromPieces(ThisRow,"TD");
		 * @param tag Tag to search for
		 * @return boolean Information from XML block
		 */
		public boolean getBoolFromPieces(String tag);

		/**
		 * Return the data value within a given XML block
		 * &lt;TAG&gt;Data&lt;/TAG&gt;
		 *
		 * Usage: String ThisColHead=getShortFromPieces(ThisRow,"TD");
		 * @param tag Tag to search for
		 * @return short Information from XML block
		 */
		public short getShortFromPieces(String tag);

		/**
		 * Return the data value within a given XML block
		 * &lt;TAG&gt;Data&lt;/TAG&gt;
		 *
		 * Usage: String ThisColHead=getIntFromPieces(ThisRow,"TD");
		 * @param tag Tag to search for
		 * @return int Information from XML block
		 */
		public int getIntFromPieces(String tag);

		/**
		 * Return where the value is within a given XML block
		 * &lt;TAG&gt;Data&lt;/TAG&gt;
		 *
		 * Usage: boolean ThisColHead=isTagInPieces(ThisRow,"TD");
		 * @param tag Tag to search for
		 * @return boolean Information from XML block
		 */
		public boolean isTagInPieces(String tag);

		/**
		 * Return the data value within a given XML block
		 * &lt;TAG&gt;Data&lt;/TAG&gt;
		 *
		 * Usage: String ThisColHead=getLongFromPieces(ThisRow,"TD");
		 * @param tag Tag to search for
		 * @return long Information from XML block
		 */
		public long getLongFromPieces(String tag);

		/**
		 * Return the data value within a given XML block
		 * &lt;TAG&gt;Data&lt;/TAG&gt;
		 *
		 * Usage: String ThisColHead=getDoubleFromPieces(ThisRow,"TD");
		 * @param tag Tag to search for
		 * @return double Information from XML block
		 */
		public double getDoubleFromPieces(String tag);
	}
}
