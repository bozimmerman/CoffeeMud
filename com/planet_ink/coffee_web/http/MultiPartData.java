package com.planet_ink.coffee_web.http;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/*
   Copyright 2012-2018 Bo Zimmerman

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

/**
 * This class is a pojo that represents one part of a multi-part request submitted
 * to the web server.  Each part can consist of multiple parts itself, have its
 * own headers, content-type, and random variable definitions.
 * 
 * It is created and populated by HTTPRequest as part of its multi-part request
 * parsing.
 * @author Bo Zimmerman
 *
 */
public class MultiPartData
{
	private final Map<String,String> 	headers		= new HashMap<String,String>(); // headers pertaining only to this part
	private String 						contentType = MIMEType.All.bin.getType();// content type for the buffer of this part
	private byte[] 						data		= new byte[0];  				// data buffer for this part
	private final List<MultiPartData> 	subParts	= new LinkedList<MultiPartData>(); // any sub-parts to this part
	private String						disposition = "form-data";  				// content-disposition for this part
	private final Map<String,String>  	variables   = new HashMap<String,String>(); // content-disposition variables definition map

	/**
	 * If any headers were found for this multi-part OTHER than content-type
	 * and content-disposition, they will have been mapped here.
	 * @return extra headers that pertain only to this part.
	 */
	public Map<String, String> getHeaders()
	{
		return headers;
	}
	/**
	 * If any extraneous variables were defined by the content-disposition,
	 * this will return the map of them for your reading pleasure
	 * The variables names are, as always, normalized to lowercase.
	 * @return content-disposition variables map
	 */
	public Map<String, String> getVariables()
	{
		return variables;
	}
	/**
	 * Returns any sub-multi-parts to this part, 
	 * if any are found.  Otherwise, an empty list.
	 * @return sub-multi-parts
	 */
	public List<MultiPartData> getSubParts()
	{
		return subParts;
	}
	/**
	 * Get the content type for this part
	 * @return the content-type for the buffer here
	 */
	public String getContentType()
	{
		return contentType;
	}
	/**
	 * Get the content-disposition for this part.
	 * Any extraneous variables have already been parsed
	 * off and are found in {@link #getVariables()}
	 * @return the content-disposition type
	 */
	public String getDisposition()
	{
		return disposition;
	}
	/**
	 * Set the content-type for this part's buffer
	 * @param contentType the content-type
	 */
	public void setContentType(String contentType)
	{
		this.contentType = contentType;
	}
	
	/**
	 * Return the data buffer for this part
	 * @return the data buffer
	 */
	public byte[] getData()
	{
		return data;
	}
	
	/**
	 * Set a new data buffer for this "part" 
	 * @param data the new data buffer
	 */
	public void setData(byte[] data)
	{
		this.data = data;
	}
	
	/**
	 * The disposition is special in that it often contains other
	 * informational key/value pairs that are vital to understanding
	 * the nature of the request.  As such, we pull off the "simple"
	 * disposition, and then each variable def key/pair in turn.
	 * @param dispositionStr the unparsed dispositionString defintion
	 */
	public void setDisposition(String dispositionStr)
	{
		final String[] parts=dispositionStr.split(";");
		if(parts.length>0)
		{
			disposition=parts[0].trim();
			for(int i=1;i<parts.length;i++)
			{
				final String[] equates=parts[i].split("=",2);
				if(equates.length==2)
				{
					String value=equates[1].trim();
					if(value.startsWith("\"")&&(value.endsWith("\"")))
						value=value.substring(1, value.length()-1);
					variables.put(equates[0].toLowerCase().trim(), value);
				}
			}
		}
	}
}
