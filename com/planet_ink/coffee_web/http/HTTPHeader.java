package com.planet_ink.coffee_web.http;

import com.planet_ink.coffee_web.interfaces.HTTPIOHandler;

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
 * Class to help formally manage http headers, both those from the
 * client and those sent to it.  Headers defined herein also have methods
 * for easily constructing the "header line" used to output them.
 * @author Bo Zimmerman
 */
public interface HTTPHeader
{
	/**
	 * Return the default value for this header, if one is defined, or ""
	 * @return the default value of this header
	 */
	public String getDefaultValue();

	/**
	 * Return a lowercase form of this headers name as used in normalized map lookups
	 * @return lowercase name of this header
	 */
	public String lowerCaseName();
	
	/**
	 * Return a header line with the given value
	 * @param value the value to assign to the header
	 * @return the name plus the value
	 */
	public String make(String value);
	
	/**
	 * Return a header line with the given value
	 * and an end-of-line character attached
	 * @param value the value to assign to the header
	 * @return the name plus the value
	 */
	public String makeLine(String value);
	
	/**
	 * Return a header line with the given value
	 * @param value the value to assign to the header
	 * @return the name plus the value
	 */
	public String make(int value);
	
	/**
	 * Return a header line with the given value
	 * @param value the value to assign to the header
	 * @return the name plus the value
	 */
	public String make(long value);
	
	/**
	 * Return a header line with the given value
	 * and an end-of-line character attached
	 * @param value the value to assign to the header
	 * @return the name plus the value
	 */
	public String makeLine(int value);
	
	/**
	 * Return a header line with the given value
	 * and an end-of-line character attached
	 * @param value the value to assign to the header
	 * @return the name plus the value
	 */
	public String makeLine(long value);

	/**
	 * Returns the key name of the header in all uppercase, but with
	 * the - characters replaced with underscores. 
	 * @return the "enum" name of the header.
	 */
	public String name();
	
	/**
	 * Enum of all the publicaly recognized headers.
	 * @author Bo Zimmerman
	 */
	public static enum Common implements HTTPHeader
	{
		ACCEPT("Accept"),
		STATUS("Status"),
		ACCEPT_ENCODING("Accept-Encoding"),
		ACCEPT_RANGES("Accept-Ranges","bytes"),
		ALLOW("Allow",HTTPMethod.getAllowedList()),
		CACHE_CONTROL("Cache-Control"),
		CONNECTION("Connection"),
		CONTENT_DISPOSITION("Content-Disposition"),
		CONTENT_ENCODING("Content-Encoding"),
		CONTENT_LENGTH("Content-Length"),
		CONTENT_RANGE("Content-Range"),
		CONTENT_TYPE("Content-Type"),
		COOKIE("Cookie"),
		DATE("Date"),
		ETAG("ETag"),
		EXPECT("Expect"),
		EXPIRES("Expires"),
		HOST("Host"),
		IF_MODIFIED_SINCE("If-Modified-Since"),
		IF_NONE_MATCH("If-None-Match"),
		KEEP_ALIVE("Keep-Alive"),
		LAST_MODIFIED("Last-Modified"),
		LOCATION("Location"),
		RANGE("Range"),
		SERVER("Server"),
		SET_COOKIE("Set-Cookie"),
		TRANSFER_ENCODING("Transfer-Encoding"),
		X_POWERED_BY("X-Powered-by"),
		X_CONTENT_TYPE_OPTIONS("X-Content-Type-Options"),
		PRAGMA("Pragma"),
		UPGRADE("Upgrade"),
		SEC_WEBSOCKET_ACCEPT("Sec-WebSocket-Accept"),
		SEC_WEBSOCKET_VERSION("Sec-WebSocket-Version"),
		SEC_WEBSOCKET_LOCATION("Sec-WebSocket-Location"),
		X_FRAME_OPTIONS("X-Frame-Options"),
		ORIGIN("Origin")
		;
		public static final String		 		KEEP_ALIVE_FMT	= "timeout=%d, max=%d";
		private static String					keepAliveHeader =KEEP_ALIVE_FMT;
		
		private static final String EOLN = HTTPIOHandler.EOLN;
		
		private final String name;
		private final String defaultValue;
		private final String keyName;

		private Common(String name, String defaultValue)
		{
			this.name=name;
			this.defaultValue=defaultValue;
			this.keyName=name.toLowerCase();
		}

		private Common(String name)
		{
			this(name,"");
		}
		
		/**
		 * Return the right and good outputtable name of this header
		 * @return the disaplayable name
		 */
		@Override
		public String toString()
		{
			return name;
		}
		
		@Override
		public String getDefaultValue()
		{
			return defaultValue;
		}
		
		@Override
		public String lowerCaseName()
		{
			return keyName;
		}
		
		@Override
		public String make(String value)
		{
			return name + ": " + value;
		}
		
		@Override
		public String makeLine(String value)
		{
			return make(value) + EOLN;
		}
		
		@Override
		public String make(int value)
		{
			return name + ": " + value;
		}
		
		@Override
		public String make(long value)
		{
			return name + ": " + value;
		}
		
		@Override
		public String makeLine(int value)
		{
			return make(value) + EOLN;
		}
		
		@Override
		public String makeLine(long value)
		{
			return make(value) + EOLN;
		}
		
		/**
		 * Finds the appropriate HTTPHeader object for the given header
		 * God help the ones that don't exist.
		 * @param str the string to match
		 * @return the HTTPHeader object
		 */
		public static HTTPHeader find(final String str)
		{
			try
			{
				return Common.valueOf(str.toUpperCase().replace('-','_'));
			}
			catch(Exception e) { }
			for(HTTPHeader head : Common.values())
				if(head.lowerCaseName().equalsIgnoreCase(str))
					return head;
			return null;
		}
		
		/**
		 * Set the static keep alive header from your configuration
		 * @param header
		 */
		public static void setKeepAliveHeader(String header)
		{
			Common.keepAliveHeader=header;
		}

		/**
		 * Return the statically defined keep alive header line
		 * @return the keep alive header line, a favorite
		 */
		public static String getKeepAliveHeader()
		{
			return Common.keepAliveHeader;
		}
		
		/**
		 * If an HTTPHeader is something other than the official
		 * ones encased in the CWHTTPHeader class, then this method
		 * will allow you to make a customized one.
		 * @param name the key name of the header to return
		 * @return the HTTPHeader object
		 */
		public static HTTPHeader createNew(final String name)
		{
			final String keyName = name.toLowerCase();
			final String enumName = name.toUpperCase().replace('-', '_');
			return new HTTPHeader()
			{
				@Override
				public String name()
				{
					return enumName;
				}
				
				/**
				 * Return the right and good outputtable name of this header
				 * @return the disaplayable name
				 */
				@Override
				public String toString()
				{
					return name;
				}
				
				@Override
				public String getDefaultValue()
				{
					return "";
				}
				
				@Override
				public String lowerCaseName()
				{
					return keyName;
				}
				
				@Override
				public String make(String value)
				{
					return name + ": " + value;
				}
				
				@Override
				public String makeLine(String value)
				{
					return make(value) + EOLN;
				}
				
				@Override
				public String make(int value)
				{
					return name + ": " + value;
				}
				
				@Override
				public String make(long value)
				{
					return name + ": " + value;
				}
				
				@Override
				public String makeLine(int value)
				{
					return make(value) + EOLN;
				}
				
				@Override
				public String makeLine(long value)
				{
					return make(value) + EOLN;
				}
			};
		}
	}
}
