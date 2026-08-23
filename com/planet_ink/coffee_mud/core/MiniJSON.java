package com.planet_ink.coffee_mud.core;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import java.util.Stack;
import java.util.Vector;

/*
   Copyright 2013-2026 Bo Zimmerman

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
 * A JSON parser. Not much to say. It can take a valid json string and generate
 * a standing object that represents the document string, and also generate a
 * string from such an object.
 *
 * @author Bo Zimmerman
 *
 */
public class MiniJSON
{
	/**
	 * Maximum depth of parsing arrays and objects, to prevent stack overflows.
	 */
	private static final int MAX_DEPTH = 500;

	/**
	 * JSON object-level state machine states.
	 */
	private enum ObjectParseState {
		/**
		 * Waiting for the first { character.
		 */
		INITIAL,
		/**
		 * Waiting for the opening quotation mark.
		 */
		NEEDKEY,
		/**
		 * Waiting for the colon between key and value.
		 */
		GOTKEY,
		/**
		 * Waiting for the value to begin.
		 */
		NEEDOBJECT,
		/**
		 * Got the value, waiting for a comma or closing } character.
		 */
		GOTOBJECT,
		/**
		 * Waiting for the next opening quotation mark.
		 */
		NEEDNEWKEY
	}

	/**
	 * Numeric value state machine states.
	 */
	private enum NumberParseState {
		/**
		 * Waiting for first numeric character.
		 */
		INITIAL,
		/**
		 * Got a dash, so waiting for a non-dash first character.
		 */
		NEEDNODASH,
		/**
		 * Waiting for a digit, or dot, or e.
		 */
		HAVEDIGIT,
		/**
		 * Got a leading 0, so need a dot.
		 */
		NEEDDOT,
		/**
		 * Got a dot, so need a non-dot digit, or e.
		 */
		NEEDDOTDIGIT,
		/**
		 * Waiting for more dot digits, or e.
		 */
		HAVEDOTDIGIT,
		/**
		 * Got an e, so waiting for first e digit.
		 */
		HAVEE,
		/**
		 * Got e digit, so waiting for the end.
		 */
		HAVEEDIGIT
	}

	/**
	 * Array parsing state machine states.
	 */
	private enum ArrayParseState {
		/**
		 * Waiting for the opening bracket.
		 */
		INITIAL,
		/**
		 * Got the opening bracket, so waiting for an object or ] char.
		 */
		EXPECTOBJECT,
		/**
		 * Got a comma so expect only another object.
		 */
		NEEDOBJECT,
		/**
		 * Got an object, so expect ] char or comma.
		 */
		GOTOBJECT
	}

	/**
	 * Parse state for an unknown document or element
	 * 
	 * @author BZ
	 *
	 */
	private static enum ParseState
	{
		/**
		 * Any element will do
		 */
		ELEMENT,
		/**
		 * Parsing an array
		 */
		ARRAY,
		/**
		 * Parsing a JSON object
		 */
		OBJECT,
		/**
		 * Parsing a JSON object key string
		 */
		KEY,
		/**
		 * Parsing a string element
		 */
		STRING,
		/**
		 * Parsing a number (int, long, double)
		 */
		NUMBER,
		/**
		 * Parsing a literal true
		 */
		LITERAL_TRUE,
		/**
		 * Parsing a literal false
		 */
		LITERAL_FALSE,
		/**
		 * Parsing a literal null
		 */
		LITERAL_NULL
	}
	
	/**
	 * String key or element parsing state
	 * @author BZ
	 *
	 */
	private static enum StringParseState
	{
		/**
		 * At the first character after the opening quotation mark
		 */
		DEFAULT,
		/**
		 * At the first char after an escape char
		 */
		ESCAPE,
		/**
		 * Escape u hex parsing, at first digit
		 */
		HEX1,
		/**
		 * Escape u hex parsing, at second digit
		 */
		HEX2,
		/**
		 * Escape u hex parsing, at third digit
		 */
		HEX3,
		/**
		 * Escape u hex parsing, at last digit
		 */
		HEX4
	}

	/**
	 * Literal definition for NULL.
	 */
	protected static final String NULL_STR = "null";
	/**
	 * Literal definition for TRUE.
	 */
	protected static final String TRUE_STR = "true";
	/**
	 * Literal definition for FALSE.
	 */
	protected static final String FALSE_STR = "false";
	/**
	 * Literal definition for four zeroes.
	 */
	private static final String ZEROES = "0000";
	/**
	 * Length of literal definition for ZEROES.
	 */
	private static final int ZEROES_LEN = ZEROES.length();


	/**
	 * The official definition of "null" for a JSON object.
	 */
	public static final Object NULL = new Object();

	/**
	 * An official MiniJSON parsing exception. It means the document being
	 * parsed was malformed in some way.
	 *
	 * @author Bo Zimmerman
	 */
	public static class MJSONException extends Exception
	{
		private static final long serialVersionUID = -2651922052891126260L;

		/**
		 * Constructs a new exception with the given parse error.
		 *
		 * @param string the parse error
		 */
		public MJSONException(final String string)
		{
			super(string);
		}

		/**
		 * Constructs a new exception with the given parse error, and underlying
		 * cause.
		 *
		 * @param string the parse error
		 * @param e an underlying cause of the parse error
		 */
		public MJSONException(final String string, final Exception e)
		{
			super(string, e);
		}
	}


	/**
	 * An official MiniJSON parsing exception. It means the document being
	 * parsed was malformed due to being incomplete.
	 *
	 * @author Bo Zimmerman
	 */
	public static final class MJSONIncompleteException extends MJSONException
	{
		private static final long serialVersionUID = -2651922052891126260L;

		/**
		 * Constructs a new exception with the given parse error.
		 *
		 * @param string the parse error
		 */
		public MJSONIncompleteException(final String string)
		{
			super(string);
		}

		/**
		 * Constructs a new exception with the given parse error, and underlying
		 * cause.
		 *
		 * @param string the parse error
		 * @param e an underlying cause of the parse error
		 */
		public MJSONIncompleteException(final String string, final Exception e)
		{
			super(string, e);
		}
	}

	/**
	 * Returns whether the given character is in the range of standard
	 * 7-bit ascii that anyone can see and read.
	 *
	 * @param c the character to check
	 * @return true if it is a viewable ascii character
	 */
	public static boolean isASCIIViewable(final char c) {
		final int asciiLow = 31;
		final int asciiHigh = 127;
		return ((c > asciiLow) && (c < asciiHigh));
	}

	/**
	 * Given a normal string, this method will return a JSON-Safe string, which
	 * means escaped cr-lf, escaped tabs and backslashes, etc.
	 *
	 * @param value the unsafe string
	 * @return the JSON safe string
	 */
	public static String toJSONString(final String value)
	{
		return toJSONString(value.getBytes(StandardCharsets.UTF_8));
	}


	/**
	 * Given a byte buffer, this method will return a JSON-Safe string, which
	 * means escaped cr-lf, escaped tabs and backslashes, etc.
	 *
	 * @param value the unsafe bytes
	 * @return the JSON safe string
	 */
	public static String toJSONString(final byte[] value)
	{
		final StringBuilder strBldr=new StringBuilder("");
		for(final byte c : value)
		{
			switch((char)c)
			{
			case '\"':
			case '\\':
				strBldr.append('\\').append((char)c);
				break;
			case '\b':
				strBldr.append('\\').append('b');
				break;
			case '\f':
				strBldr.append('\\').append('f');
				break;
			case '\n':
				strBldr.append('\\').append('n');
				break;
			case '\r':
				strBldr.append('\\').append('r');
				break;
			case '\t':
				strBldr.append('\\').append('t');
				break;
			default:
				if (isASCIIViewable((char)c))
					strBldr.append((char)c);
				else
				{
					final int sixteenBits = 0xffff;
					String hex = ZEROES + Integer.toHexString(c & sixteenBits);
					hex = hex.substring(hex.length() - ZEROES_LEN);
					strBldr.append("\\u"+hex.toLowerCase());
				}
				break;
			}
		}
		return strBldr.toString();
	}

	/**
	 * An official JSON object. Implemented as a Map, this class has numerous
	 * methods for accessing the internal keys and their mapped values in
	 * different ways, both raw, and checked.
	 *
	 * @author Bo Zimmerman
	 */
	public static class JSONObject extends LinkedHashMap<String,Object>
	{
		private static final long serialVersionUID = 8390676973120915175L;

		/**
		 * Internal method that returns a raw value object, or throws an
		 * exception if the key is not found.
		 *
		 * @param key the key to look for
		 * @return the raw Object the key is mapped to
		 * @throws MJSONException the key was not found
		 */
		private Object getCheckedObject(final String key) throws MJSONException
		{
			if(!containsKey(key))
				throw new MJSONException("Key '"+key+"' not found");
			return get(key);
		}

		/**
		 * Returns a JSONObject mapped in THIS object by the given key. Throws
		 * an exception if anything goes wrong.
		 *
		 * @param key the key of the object
		 * @return the JSON Object mapped to by that key
		 * @throws MJSONException a missing key, or not a JSON Object
		 */
		public JSONObject getCheckedJSONObject(final String key) throws MJSONException
		{
			final Object o = getCheckedObject(key);
			if(!(o instanceof JSONObject))
			{
				throw new MJSONException("Key '" + key
						+ "' is not a JSON object");
			}
			return (JSONObject)o;
		}

		/**
		 * Returns a JSON Array mapped in this object by the given key. Throws
		 * an exception if anything goes wrong.
		 *
		 * @param key the key of the Array
		 * @return the JSON Array mapped to by that key
		 * @throws MJSONException a missing key, or not a JSON Array
		 */
		public Object[] getCheckedArray(final String key) throws MJSONException
		{
			final Object o = getCheckedObject(key);
			if(!(o instanceof Object[]))
				throw new MJSONException("Key '"+key+"' is not an array");
			return (Object[])o;
		}

		/**
		 * Returns a String mapped in this object by the given key. Throws an
		 * exception if anything goes wrong.
		 *
		 * @param key the key of the String
		 * @return the String mapped to by that key
		 * @throws MJSONException a missing key, or not a String
		 */
		public String getCheckedString(final String key) throws MJSONException
		{
			final Object o = getCheckedObject(key);
			if(!(o instanceof String))
				throw new MJSONException("Key '"+key+"' is not a String");
			return (String)o;
		}

		/**
		 * Returns a Long mapped in this object by the given key. Throws an
		 * exception if anything goes wrong.
		 *
		 * @param key the key of the Long
		 * @return the Long mapped to by that key
		 * @throws MJSONException a missing key, or not a Long
		 */
		public Long getCheckedLong(final String key) throws MJSONException
		{
			final Object o = getCheckedObject(key);
			if(!(o instanceof Long))
				throw new MJSONException("Key '"+key+"' is not a long");
			return (Long)o;
		}

		/**
		 * Returns a Double mapped in this object by the given key. Throws an
		 * exception if anything goes wrong.
		 *
		 * @param key the key of the Long
		 * @return the Double mapped to by that key
		 * @throws MJSONException a missing key, or not a Double
		 */
		public Double getCheckedDouble(final String key) throws MJSONException
		{
			final Object o = getCheckedObject(key);
			if(!(o instanceof Double))
				throw new MJSONException("Key '"+key+"' is not a double");
			return (Double)o;
		}

		/**
		 * Returns a Boolean mapped in this object by the given key. Throws an
		 * exception if anything goes wrong.
		 *
		 * @param key the key of the Long
		 * @return the Boolean mapped to by that key
		 * @throws MJSONException a missing key, or not a Boolean
		 */
		public Boolean getCheckedBoolean(final String key) throws MJSONException
		{
			final Object o = getCheckedObject(key);
			if(!(o instanceof Boolean))
				throw new MJSONException("Key '"+key+"' is not a boolean");
			return (Boolean)o;
		}

		/**
		 * Returns a numeric value mapped in this object by the given key.
		 * Throws an exception if anything goes wrong.
		 *
		 * @param key the key of the Long
		 * @return the double value of the number mapped to by that key
		 * @throws MJSONException a missing key, or not a numeric value
		 */
		public double getCheckedNumber(final String key) throws MJSONException
		{
			final Object o = getCheckedObject(key);
			if(o instanceof Double)
				return ((Double)o).doubleValue();
			if(o instanceof Long)
				return ((Long)o).doubleValue();
			throw new MJSONException("Key '"+key+"' is not a number");
		}

		/**
		 * Checks this object for the given key, and checks if it is an official
		 * NULL or not. Throws an exception if the key is missing.
		 *
		 * @param key the key of the possible null
		 * @return true if the key maps to NULL or false otherwise
		 * @throws MJSONException the key was missing
		 */
		public boolean isCheckedNULL(final String key) throws MJSONException
		{
			final Object o = getCheckedObject(key);
			return o == NULL;
		}

		/**
		 * Correctly appends the given thing to the given stringbuffer which is
		 * assumed to be in the middle of a JSON object definition, right after
		 * the key and the colon.
		 *
		 * @param value the StringBuffer to append a value to
		 * @param obj the value to append -- a string, null, array, or number
		 */
		public void appendJSONValue(final StringBuilder value, final Object obj)
		{
			if(obj instanceof String)
			{
				value.append("\"").append(toJSONString((String)obj)).append("\"");
			}
			else
			if(obj instanceof byte[])
			{
				value.append("\"").append(toJSONString((byte[])obj)).append("\"");
			}
			else
			if(obj == NULL)
			{
				value.append(NULL_STR);
			}
			else
			if(obj instanceof Object[])
			{
				value.append("[");
				final Object[] array=(Object[])obj;
				for(int i=0; i<array.length; i++)
				{
					if(i>0)
						value.append(",");
					appendJSONValue(value, array[i]);
				}
				value.append("]");
			}
			else
			if(obj != null)
			{
				value.append(obj.toString());
			}
		}

		/**
		 * Returns a full JSON document representation of this JSON object.
		 *
		 * @return JSON doc
		 */
		@Override
		public String toString()
		{
			final StringBuilder value = new StringBuilder("");
			value.append("{");
			for(final Iterator<String> k = keySet().iterator(); k.hasNext();)
			{
				final String keyVar = k.next();
				value.append("\"").append(toJSONString(keyVar)).append("\":");
				final Object obj = get(keyVar);
				appendJSONValue(value, obj);
				if(k.hasNext())
				{
					value.append(",");
				}
			}
			value.append("}");
			return value.toString();
		}

		/**
		 * Makes a deep true copy of a json object, such as JSONObject,
		 * Array, etc.  Immutable objects are simply returned.
		 *
		 * @param obj the MiniJSON object to copy
		 * @return the copy
		 */
		public Object jsonDeepCopy(final Object obj)
		{
			if(obj == null)
				return null;
			if(obj instanceof JSONObject)
				return ((JSONObject)obj).copyOf();
			else
			if(obj.getClass().isArray())
			{
				if(obj.getClass().getComponentType().isPrimitive())
				{
					final Class<?> componentType = obj.getClass().getComponentType();
					final int length = Array.getLength(obj);
					final Object newArray = Array.newInstance(componentType, length);
					System.arraycopy(obj, 0, newArray, 0, length);
					return newArray;
				}
				else
				{
					final Object[] newArray = Arrays.copyOf((Object[])obj, ((Object[])obj).length);
					for(int i=0;i<newArray.length;i++)
						newArray[i] = jsonDeepCopy(newArray[i]);
					return newArray;
				}
			}
			else
				return obj;
		}

		/**
		 * Makes a deep copy of this JSONObject and returns it.
		 * @return a deep copy of this JSONObject.
		 */
 		public JSONObject copyOf()
		{
			final JSONObject newObj = new JSONObject();
			for(final String key : this.keySet())
				newObj.put(key, jsonDeepCopy(this.get(key)));
			return newObj;
		}
	}

	/**
	 * Generates a new JSON Object for your jsoning pleasure.
	 * @return a new JSON Object
	 */
	public JSONObject createJSONObject()
	{
		return new JSONObject();
	}

	/**
	 * Parses a character in a number stream, starting with the first digit. 
	 * Returns either the Number object, the next State, or an exception.
	 * @param c the next char from the stream
	 * @param index the 1 byte array denoting the current buffer index, for debugging purposes
	 * @param parseState the current parse state
	 * @param str the stringbuilder for the temporary number string
	 * @param subObj a 1 element object array for internal use by this parser
	 * @return either the next state, or a Integer, Double, Long, etc
	 * @throws MJSONException any parse errors that occur
	 */
	private Object parseNumberStream(final char c, final int[] index, final NumberParseState state, final StringBuilder str) throws MJSONException
	{
		str.append(c);
		switch(state)
		{
		case INITIAL:
			if (c == '0')
				return NumberParseState.NEEDDOT;
			else
			if (c == '-')
				return NumberParseState.NEEDNODASH;
			else
			if (Character.isDigit(c))
				return NumberParseState.HAVEDIGIT;
			else
				throw new MJSONException("Expected digit at "+index[0]);
		case NEEDNODASH:
			if (c == '-')
				throw new MJSONException("Expected digit at "+index[0]);
			else
			if (c == '0')
				return NumberParseState.NEEDDOT;
			else
			if (Character.isDigit(c))
				return NumberParseState.HAVEDIGIT;
			else
				throw new MJSONException("Expected digit at "+index[0]);
		case HAVEDIGIT:
			if (c == '.')
				return NumberParseState.NEEDDOTDIGIT;
			else
			if ((c == 'E') || (c == 'e'))
				return NumberParseState.HAVEE;
			else
			if(Character.isDigit(c))
				return NumberParseState.HAVEDIGIT;
			else
			{
				index[0]--;
				str.deleteCharAt(str.length()-1);
				final String numStr = str.toString();
				try {
					return Long.valueOf(numStr);
				} catch (final NumberFormatException nxe) {
					throw new MJSONException("Number Format Exception (" + numStr + ")", nxe);
				}
			}
		case NEEDDOT:
			if (c == '.')
				return NumberParseState.NEEDDOTDIGIT;
			else
			if ((c == 'E') || (c == 'e'))
				return NumberParseState.HAVEE;
			else
			{
				index[0]--;
				str.deleteCharAt(str.length()-1);
				final String numStr = str.toString();
				try {
					return Long.valueOf(numStr);
				} catch (final NumberFormatException nxe) {
					throw new MJSONException("Number Format Exception (" + numStr + ")", nxe);
				}
			}
		case NEEDDOTDIGIT:
			if (Character.isDigit(c))
				return NumberParseState.HAVEDOTDIGIT;
			else
				throw new MJSONException("Expected digit at "+index[0]);
		case HAVEDOTDIGIT:
			if (Character.isDigit(c))
				return NumberParseState.HAVEDOTDIGIT;
			else
			if ((c == 'e') || (c == 'E'))
				return NumberParseState.HAVEE;
			else
			{
				index[0]--;
				str.deleteCharAt(str.length()-1);
				final String numStr = str.toString();
				try {
					return Double.valueOf(numStr);
				} catch (final NumberFormatException nxe) {
					throw new MJSONException("Number Format Exception (" + numStr + ")", nxe);
				}
			}
		case HAVEE:
			if(c == '0')
				throw new MJSONException("Expected non-zero digit at "+index[0]);
			else
			if (Character.isDigit(c) || (c == '+') || (c == '-'))
				return NumberParseState.HAVEEDIGIT;
			else
				throw new MJSONException("Expected +- or non-zero digit at "+index[0]);
		case HAVEEDIGIT:
			if(!Character.isDigit(c))
			{
				index[0]--;
				str.deleteCharAt(str.length()-1);
				final String numStr = str.toString();
				try {
					return Double.valueOf(numStr);
				} catch (final NumberFormatException nxe) {
					throw new MJSONException("Number Format Exception (" + numStr + ")", nxe);
				}
			}
			break;
		}
		return state;
	}

	/**
	 * Parse either an Long, or Double object from the doc buffer.
	 *
 	 * @param doc the full JSON document
	 * @param index one dimensional array containing current index into the doc
	 * @return either an Long or a Double
	 * @throws MJSONException any parsing errors
	 */
	private Object parseNumber(final char[] doc, final int[] index) throws MJSONException
	{
		NumberParseState state = NumberParseState.INITIAL;
		StringBuilder str = new StringBuilder("");
		while(index[0] <= doc.length)
		{
			final char c = (index[0] < doc.length) ? doc[index[0]] : '\0';
			final Object res = this.parseNumberStream(c, index, state, str);
			if(res instanceof NumberParseState)
				state = (NumberParseState)res;
			else
				return res;
			index[0]++;
		}
		// technically unreachable, since you don't really know when digits end
		throw new MJSONIncompleteException("Unexpected end of number at"+index[0]);
	}

	/**
	 * Given a char returns the nybble value of the hex digit.
	 *
	 * @param c the char to evaluate
	 * @return the byte value of the 1 digit hex nybble
	 * @throws MJSONException a parse error meaning it wasn't a hex number at all
	 */
	private byte getHexNybble(final char c, int index) throws MJSONException
	{
		if((c >= '0') && (c <= '9'))
			return (byte)(c-'0');
		if((c >= 'a') && (c <= 'f'))
			return (byte)(10 + (c-'a'));
		if((c >= 'A') && (c <= 'F'))
			return (byte)(10 + (c-'A'));
		throw new MJSONException("Illegal hex digit at "+index);
	}

	/**
	 * Parses a character in a string stream, starting with the first character after the 
	 * opening quote.  Returns either the completed String, the next State, or an exception.
	 * @param c the next char from the stream
	 * @param index the 1 byte array denoting the current buffer index, for debugging purposes
	 * @param parseState the current parse state
	 * @param str the stringbuilder for the temporary string
	 * @param subObj a 1 element object array for internal use by this parser
	 * @return either the next state, or a String
	 * @throws MJSONException any parse errors that occur
	 */
	private Object parseStringStream(final char c, final int[] index, final StringParseState parseState, 
			final StringBuilder str, final Object[]	subObj) throws MJSONException
	{
		switch(parseState)
		{
		case DEFAULT:
			switch(c)
			{
			case '\"':
				return str.toString();
			case '\\':
				return StringParseState.ESCAPE;
			default:
				str.append(c);
				break;
			}
			break;
		case ESCAPE:
			switch(c)
			{
			case '\"':
			case '\\':
			case '/':
				str.append(c);
				return StringParseState.DEFAULT;
			case 'b':
				str.append('\b');
				return StringParseState.DEFAULT;
			case 'f':
				str.append('\f');
				return StringParseState.DEFAULT;
			case 'n':
				str.append('\n');
				return StringParseState.DEFAULT;
			case 'r':
				str.append('\r');
				return StringParseState.DEFAULT;
			case 't':
				str.append('\t');
				return StringParseState.DEFAULT;
			case 'u':
				return StringParseState.HEX1;
			default:
				throw new MJSONException("Illegal escape character: "+c);
			}
		case HEX1:
			subObj[0] = new byte[3];
			((byte[])subObj[0])[0] = getHexNybble(c,index[0]);
			return StringParseState.HEX2;
		case HEX2:
			((byte[])subObj[0])[1] = getHexNybble(c,index[0]);
			return StringParseState.HEX3;
		case HEX3:
			((byte[])subObj[0])[2] = getHexNybble(c,index[0]);
			return StringParseState.HEX4;
		case HEX4:
		{
			byte b = getHexNybble(c,index[0]);
			final byte[] hexBuf=new byte[] {
				(byte)((((byte[])subObj[0])[0] << 4) | ((byte[])subObj[0])[1]),
				(byte)((((byte[])subObj[0])[2] << 4) | b)
			};
			str.append(new String(hexBuf, StandardCharsets.UTF_16));
			return StringParseState.DEFAULT;
		}
		}
		return parseState;
	}

	/**
	 * Given a JSON document char array, and an index into it, parses a string
	 * at the indexed point of the char array and returns its value.
	 *
	 * @param doc the json doc containing the string
	 * @param index the index into that json doc where the string begins
	 * @return the value of the found string
	 * @throws MJSONException a parse exception, meaning no string was there
	 */
	private String parseString(final char[] doc, final int[] index) throws MJSONException
	{
		final StringBuilder value=new StringBuilder("");
		if(doc[index[0]] != '\"')
		{
			throw new MJSONException("Expected quote at: "+doc[index[0]]);
		}
		StringParseState state = StringParseState.DEFAULT;
		Object[] hexBits = new Object[1];
		while(++index[0] < doc.length)
		{
			final char c=doc[index[0]];
			Object res = parseStringStream(c,index,state,value,hexBits);
			if(res instanceof StringParseState)
				state = (StringParseState)res;
			else
				return res.toString();
		}
		throw new MJSONIncompleteException("Unfinished string at "+index[0]);
	}

	/**
	 * Parses the next character in a json array character stream.  Returns either the next state, or the final array.
	 * If the next state is gotobject, you will need to immediately fill the given list with the next object.
	 * 
	 * @param c the next char in the stream
	 * @param index the 1 dim int[] array holding the index into the doc, for debugging purposes
	 * @param state the current parse state for the array
	 * @param list the list build in-progress
	 * @return the next state, or the object array
	 * @throws MJSONException any parse errors that occur
	 */
	private Object parseArrayStream(final char c, final int[] index, final ArrayParseState state, final List<Object> list) throws MJSONException
	{
		if(!Character.isWhitespace(c))
		{
			switch(state)
			{
			case INITIAL:
				if (c == '[')
					return ArrayParseState.NEEDOBJECT;
				else
					throw new MJSONException("Expected String at "+index[0]);
			case EXPECTOBJECT:
				return ArrayParseState.GOTOBJECT;
			case NEEDOBJECT:
				if (c == ']')
					return list.toArray(new Object[0]);
				else
					return ArrayParseState.GOTOBJECT;
			case GOTOBJECT:
				if (c == ']')
					return list.toArray(new Object[0]);
				else
				if (c == ',')
					return ArrayParseState.EXPECTOBJECT;
				else
					throw new MJSONException("Expected ] or , at "+index[0]);
			}
		}
		return state;
	}
	
	/**
	 * Given a JSON document char array, and an index into it, parses an array
	 * at the indexed point of the char array and returns its value object.
	 *
	 * @param doc the JSON doc containing the array
	 * @param index the index into that JSON doc where the array begins
	 * @param depth the current parsing depth, to prevent stack overflows
	 * @return the value object of the found array
	 * @throws MJSONException a parse exception, meaning no array was there
	 */
	private Object[] parseArray(final char[] doc, final int[] index, final int depth) throws MJSONException
	{
		ArrayParseState state=ArrayParseState.INITIAL;
		final List<Object> finalSet=new ArrayList<Object>();
		while(index[0] < doc.length)
		{
			final char c=doc[index[0]];
			Object res = parseArrayStream(c,index,state,finalSet);
			if(res instanceof ArrayParseState)
			{
				if((res == ArrayParseState.GOTOBJECT) && (state != ArrayParseState.GOTOBJECT))
					finalSet.add(parseElement(doc,index,depth));
				state = (ArrayParseState)res;
			}
			else
				return (Object[])res;
			index[0]++;
		}
		throw new MJSONIncompleteException("Expected ] at "+index[0]);
	}

	/**
	 * Represents a single element being parsed in a json document
	 * @author BZ
	 */
	private static class ParseFrame
	{
		/**
		 * This frames element type being parsed
		 */
		protected ParseState	state;
		/**
		 * The sub-state inside this element type.
		 */
		protected Enum<?>		subState	= null;
		/**
		 * The element construction object, typically a list, map, or stringbuilder
		 */
		protected Object		obj			= null;
		/**
		 * For maps, the previous key parsed
		 */
		protected String		lastKey		= null;
		/**
		 * Any extraneous sub-element state data required
		 */
		protected Object[]		subObj		= new Object[1];
	}
	
	/**
	 * Top level streaming parse object
	 * @author BZ
	 */
	private static class ParseContext
	{
		/**
		 * The initial top level parse state, typically ELEMENT
		 */
		protected ParseState		  state;
		/**
		 * The last top-level element parsed
		 */
		protected List<Object>		  parsedElements = new Vector<Object>();
		/**
		 * The document being parsed
		 */
		protected  final StringBuffer doc;
		/**
		 * The index into the given document
		 */
		protected  final int[]		  index;
		/**
		 * The stack of different element depths being parsed
		 */
		protected Stack<ParseFrame>	  frames	= new Stack<ParseFrame>();

		/**
		 * Simple constructor
		 * @param doc the document to parse
		 */
		protected ParseContext(final StringBuffer doc)
		{
			this.doc = doc;
			this.index=new int[] { 0 };
			state = ParseState.ELEMENT;
		}
		
		/**
		 * When a new deep element needs parsing, this constructs it and pushes it onto the stack
		 * 
		 * @param newState the new high-level parse state
		 * @param subState the initial element parse state
		 * @param obj the main partial construction object
		 */
		protected void pushFrame(final ParseState newState, final Enum<?> subState, final Object obj)
		{
			ParseFrame frame = new ParseFrame();
			frame.state = state; // remember the prior state
			frame.subState = subState;
			frame.obj = obj;
			this.frames.push(frame);
			this.state = newState;
		}

		/**
		 * When an element is finished being constructed, this pops the frame off the stack and applies
		 * the element to its parent.
		 * 
		 * @param answer the final element
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		protected void popFrame(final Object answer)
		{
			if(frames.size()==0)
			{
				parsedElements.add(answer);
				state = ParseState.ELEMENT;
				return;
			}
			ParseFrame myFrame = frames.pop();
			if(frames.size()==0)
			{
				parsedElements.add(answer);
				state = myFrame.state;
				return;
			}
			ParseFrame parent = frames.peek();
			if(parent.obj == null)
			{
				if(frames.size()==1)
				{
					parent.obj=answer;
					state = ParseState.ELEMENT;
					return;
				}
				frames.pop();
				if(frames.size()==0)
				{
					parsedElements.add(answer);
					state = ParseState.ELEMENT;
					return;
				}
				parent = frames.peek();
			}
			if(parent.obj instanceof List)
				((List)parent.obj).add(answer);
			else
			if(parent.obj instanceof Map)
			{
				if(myFrame.state == ParseState.OBJECT)
					parent.lastKey = answer.toString();
				else
					((Map)parent.obj).put(parent.lastKey,answer);
			}
			if(parent.obj instanceof List)
				state = ParseState.ARRAY;
			else
			if(parent.obj instanceof Map)
				state = ParseState.OBJECT;
			else
				state = parent.state;
		}
	}

	/**
	 * Constructs a runnable that is capable of partial JSON parsing.  The run() method
	 * should be called after adding more bytes to the given StringBuffer, and any parsed
	 * elements are added to the given results array.  The results array is cleared
	 * on every run() call, and the buffer may be modified or trimmed by the parser.
	 * 
	 * The run() method will throw a java.lang.Error on any fatal parse errors.
	 * 
	 * @param buf the buffer to hold existing and future json document string chars
	 * @param results after every run call, this will hold any completed parsed objects
	 * @return the runnable to run()
	 * 
	 */
	public Runnable getJSONParser(final StringBuffer buf, final List<Object> results)
	{
		final ParseContext context=new ParseContext(buf);
		final MiniJSON jsoner = this;
		return new Runnable()
		{
			@Override
			public void run()
			{
				results.clear();
				if(context.index[0] > buf.length())
					context.index[0] = buf.length();
				try
				{
					jsoner.parseStream(context);
					results.addAll(context.parsedElements);
				}
				catch(MJSONException e)
				{
					throw new java.lang.Error("Json parsing error", e);
				}
				finally
				{
					context.parsedElements.clear();
					int trim = buf.length()-10;
					if(context.index[0]>10 && buf.length()>20) // trim the buffer
					{
						context.index[0] -= trim;
						buf.delete(0, trim);
					}
				}
			}
		};
	}
	
	/**
	 * Continues parsing a json document one character at a time.
	 * The top level object parsed, as it is found, is in ctx.lastObj
	 * 
	 * @param ctx the parsing context with all state information
	 * @throws MJSONException
	 */
	protected void parseStream(ParseContext ctx) throws MJSONException
	{
		while(ctx.index[0] < ctx.doc.length())
		{
			char c = ctx.doc.charAt(ctx.index[0]);
			switch(ctx.state)
			{
			case NUMBER:
			{
				ParseFrame frame = ctx.frames.peek();
				final Object res = parseNumberStream(c,ctx.index,(NumberParseState)frame.subState,(StringBuilder)frame.obj);
				if(res instanceof NumberParseState)
					frame.subState = (NumberParseState)res;
				else
					ctx.popFrame(res);
				break;
			}
			case KEY:
			case STRING:
			{
				ParseFrame frame = ctx.frames.peek();
				final Object res = parseStringStream(c,ctx.index,(StringParseState)frame.subState,(StringBuilder)frame.obj,frame.subObj);
				if(res instanceof StringParseState)
					frame.subState = (StringParseState)res;
				else
					ctx.popFrame(res);
				break;
			}
			case LITERAL_TRUE:
			{
				ParseFrame frame = ctx.frames.peek();
				if(parseLiteral(c,ctx.index,(StringBuilder)frame.obj,TRUE_STR))
					ctx.popFrame(Boolean.TRUE);
				break;
			}
			case LITERAL_FALSE:
			{
				ParseFrame frame = ctx.frames.peek();
				if(parseLiteral(c,ctx.index,(StringBuilder)frame.obj,FALSE_STR))
					ctx.popFrame(Boolean.FALSE);
				break;
			}
			case LITERAL_NULL:
			{
				ParseFrame frame = ctx.frames.peek();
				if(parseLiteral(c,ctx.index,(StringBuilder)frame.obj,NULL_STR))
					ctx.popFrame(NULL);
				break;
			}
			case ELEMENT:
				if(!Character.isWhitespace(c))
				{
					switch(c)
					{
					case '\"':
						ctx.pushFrame(ParseState.STRING,StringParseState.DEFAULT,new StringBuilder());
						break;
					case '[':
						if (ctx.frames.size() >= MAX_DEPTH)
							throw new MiniJSON.MJSONException("Maximum depth reached @" + ctx.index[0]);
						ctx.pushFrame(ParseState.ARRAY,ArrayParseState.INITIAL,new ArrayList<Object>());
						ctx.index[0]--; // allow first char to be reparsed
						break;
					case '{':
						if (ctx.frames.size() >= MAX_DEPTH)
							throw new MiniJSON.MJSONException("Maximum depth reached @" + ctx.index[0]);
						ctx.pushFrame(ParseState.OBJECT,ObjectParseState.NEEDKEY,new JSONObject());
						break;
					case '-':
					case '0':
					case '1':
					case '2':
					case '3':
					case '4':
					case '5':
					case '6':
					case '7':
					case '8':
					case '9':
						ctx.index[0]--; // number parser needs first char
						ctx.pushFrame(ParseState.NUMBER,NumberParseState.INITIAL,new StringBuilder());
						break;
					case 't':
						ctx.pushFrame(ParseState.LITERAL_TRUE,null,new StringBuilder().append(c));
						break;
					case 'f':
						ctx.pushFrame(ParseState.LITERAL_FALSE,null,new StringBuilder().append(c));
						break;
					case 'n':
						ctx.pushFrame(ParseState.LITERAL_NULL,null,new StringBuilder().append(c));
						break;
					default:
						throw new MJSONException("Unknown character at " + ctx.index[0]
								+ "(" + Integer.toHexString(c) + ")");
					}
				}
				break;
			case OBJECT:
			{
				final ParseFrame frame = ctx.frames.peek();
				@SuppressWarnings("unchecked")
				final Map<String, Object> map = (Map<String, Object>)frame.obj;
				Object res = parseObjectStream(c, ctx.index, (ObjectParseState)frame.subState, frame.lastKey, map);
				if(res instanceof ObjectParseState)
				{
					ObjectParseState nextState = (ObjectParseState)res;
					if((nextState == ObjectParseState.GOTKEY)&&(frame.subState != ObjectParseState.GOTKEY))
						ctx.pushFrame(ParseState.KEY,StringParseState.DEFAULT,new StringBuilder());
					else
					if((nextState == ObjectParseState.GOTOBJECT)&&(frame.subState != ObjectParseState.GOTOBJECT))
					{
						ctx.index[0]--;
						ctx.pushFrame(ParseState.ELEMENT,null,null);
					}
					frame.subState = nextState;
				}
				else
					ctx.popFrame(map);
				break;
			}
			case ARRAY:
			{
				final ParseFrame frame = ctx.frames.peek();
				@SuppressWarnings("unchecked")
				final List<Object> list = (List<Object>)frame.obj;
				Object res = parseArrayStream(c,ctx.index,(ArrayParseState)frame.subState,list);
				if(res instanceof ArrayParseState)
				{
					if((res == ArrayParseState.GOTOBJECT) && (frame.subState != ArrayParseState.GOTOBJECT))
					{
						ctx.index[0]--;
						ctx.pushFrame(ParseState.ELEMENT,null,null);
					}
					frame.subState = (ArrayParseState)res;
				}
				else
					ctx.popFrame(list.toArray(new Object[0]));
				break;
			}
			}
			ctx.index[0]++;
		}
	}
	
	/**
	 * Handles stream parsing of literals by comparing the next char against what was received so far.
	 * @param c the next character to evaluate
	 * @param index the document index, for debugging purposes
	 * @param str the partial string
	 * @param target the completed string
	 * @return true if the literal was parsed out completely, false if not, or throws an exception
	 */
	private boolean parseLiteral(final char c, final int[] index, final StringBuilder str, final String target) throws MJSONException
	{
		if(str.length()>=target.length())
			throw new MJSONException("Unknown literal at "+index[0]);
		if(c != target.charAt(str.length()))
			throw new MJSONException("Unknown literal at "+index[0]);
		str.append(c);
		return (str.length()==target.length());
	}

	/**
	 * Given a JSON document char array, and an index into it, parses a value
	 * object at the indexed point of the char array and returns its value
	 * object. A value object may be anything from a string, array, a JSON
	 * object, boolean, null, or a number.
	 *
	 * @param doc the JSON doc containing the value
	 * @param index the index into that JSON doc where the value begins
	 * @param depth the current parsing depth, to prevent stack overflows
	 * @return the value object of the found value
	 * @throws MJSONException a parse exception, meaning no recognized value was there
	 */
	protected Object parseElement(final char[] doc, final int[] index, final int depth) throws MJSONException
	{
		while (index[0] < doc.length && Character.isWhitespace(doc[index[0]])) {
			index[0]++ ;
		}
		if (index[0] >= doc.length) {
			throw new MiniJSON.MJSONIncompleteException("Unexpected end of document @"+index[0]);
		}
		switch(doc[index[0]])
		{
		case '\"':
			return parseString(doc,index);
		case '[':
			if (depth >= MAX_DEPTH)
				throw new MiniJSON.MJSONException("Maximum depth reached @" + index[0]);
			return parseArray(doc,index,depth+1);
		case '{':
			if (depth >= MAX_DEPTH)
				throw new MiniJSON.MJSONException("Maximum depth reached @" + index[0]);
			return parseObject(doc,index,depth+1);
		case '-':
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
			return parseNumber(doc,index);
		case 't':
			if((index[0] < doc.length-3) && (new String(doc,index[0],4).equals(TRUE_STR)))
			{
				index[0]+=3;
				return Boolean.TRUE;
			}
			throw new MJSONException("Invalid true at "+index[0]);
		case 'f':
			if((index[0] < doc.length-4) && (new String(doc,index[0],5).equals(FALSE_STR)))
			{
				index[0]+=4;
				return Boolean.FALSE;
			}
			throw new MJSONException("Invalid false at "+index[0]);
		case 'n':
			if((index[0] < doc.length-3) && (new String(doc,index[0],4).equals(NULL_STR)))
			{
				index[0]+=3;
				return NULL;
			}
			throw new MJSONException("Invalid null at "+index[0]);
		default:
			throw new MJSONException("Unknown character at " + index[0]
					+ "(" + Integer.toHexString(doc[index[0]]) + ")");
		}
	}

	/**
	 * Given a JSON document string, this parses and returns its value.
	 * A value object may be anything from a string, array, a JSON
	 * object, boolean, null, or a number.
	 *
	 * @param doc the JSON doc containing the value
	 * @return the value object of the found value
	 * @throws MJSONException a parse exception, meaning no recognized value was
	 * there
	 */
	public Object parse(final String doc) throws MJSONException
	{
		final int[] index = new int[] { 0 };
		final Object obj = parseElement(doc.toCharArray(), index, 0);
		for (++index[0]; index[0] < doc.length(); index[0]++)
		{
			if (!Character.isWhitespace(doc.charAt(index[0])))
				throw new MJSONException("Extra characters found (" + doc.charAt(index[0]) + ")");
		}
		return obj;
	}

	/**
	 * Parses the next character in a json object character stream.  Returns either the next state, or the final map.
	 * If the next state is GOTOBJECT, you will need to immediately parse the next element.
	 * If the next state is GOTKEY, you will need to immediately parse the next string as a key.
	 * 
	 * @param c the next char in the stream
	 * @param index the 1 dim int[] array holding the index into the doc, for debugging purposes
	 * @param state the current parse state for the object
	 * @param key the last key encountered
	 * @param map the object build in-progress
	 * @return the next state, or the object
	 * @throws MJSONException any parse errors that occur
	 */
	private Object parseObjectStream(final char c, final int[] index, final ObjectParseState state, final String key, final Map<String,Object> map) throws MJSONException
	{
		if(!Character.isWhitespace(c))
		{
			switch(state)
			{
			case INITIAL:
				if (c == '{')
					return ObjectParseState.NEEDKEY;
				else
					throw new MJSONException("Expected Key/String at "+index[0]);
			case NEEDKEY:
			case NEEDNEWKEY:
				if(c=='\"')
					return ObjectParseState.GOTKEY;
				else
				if ((c == '}') && (state == ObjectParseState.NEEDKEY))
					return map;
				else
					throw new MJSONException("Expected Key/String at "+index[0]);
			case GOTKEY:
				if (c == ':')
					return ObjectParseState.NEEDOBJECT;
				else
					throw new MJSONException("Expected Colon at "+index[0]);
			case NEEDOBJECT:
				return ObjectParseState.GOTOBJECT;
			case GOTOBJECT:
				if (c == ',')
					return ObjectParseState.NEEDKEY;
				else
				if (c == '}')
					return map;
				else
					throw new MJSONException("Expected } or , at "+index[0]);
			}
		}
		return state;
	}

	/**
	 * Given a JSON document char array, and an index into it, parses a JSON
	 * object at the indexed point of the char array and returns it as a mapped
	 * JSON object.
	 *
	 * @param doc the JSON doc containing the JSON object
	 * @param index the index into that JSON doc where the JSON object begins
	 * @param depth the depth of parsing, to prevent stack overflows
	 * @return the value object of the found JSON object
	 * @throws MJSONException a parse exception, meaning no JSON object was there
	 */
	private JSONObject parseObject(final char[] doc, final int[] index, final int depth) throws MJSONException
	{
		final JSONObject map = createJSONObject();
		String key = null;
		ObjectParseState state = ObjectParseState.INITIAL;
		while(index[0] < doc.length)
		{
			final char c=doc[index[0]];
			Object res = parseObjectStream(c, index, state, key, map);
			if(res instanceof ObjectParseState)
			{
				ObjectParseState nextState = (ObjectParseState)res;
				if((nextState == ObjectParseState.GOTKEY)&&(state != ObjectParseState.GOTKEY))
					key = parseString(doc,index);
				else
				if((nextState == ObjectParseState.GOTOBJECT)&&(state != ObjectParseState.GOTOBJECT))
					map.put(key, parseElement(doc,index,depth));
				state = nextState;
			}
			else
				return map;
			index[0]++;
		}
		throw new MJSONIncompleteException("Expected } at "+index[0]);
	}

	/**
	 * Given a string containing a JSON object, this method will parse it into a
	 * mapped JSONObject object recursively.
	 *
	 * @param doc the JSON document that contains a top-level JSON object
	 * @return the JSON object at the top level
	 * @throws MJSONException the parse error
	 */
	public JSONObject parseObject(final String doc) throws MJSONException
	{
		try
		{
			return parseObject(doc.toCharArray(), new int[]{0}, 0);
		}
		catch (final MJSONException e)
		{
			throw e;
		}
		catch (final Exception e)
		{
			throw new MJSONException("Internal error",e);
		}
	}

	/**
	 * Given a string containing a JSON array, this method will parse it into a
	 * mapped JSONObject object[] array recursively.
	 *
	 * @param doc the JSON document that contains a top-level JSON object
	 * @return the JSON object[] array at the top level
	 * @throws MJSONException the parse error
	 */
	public Object[] parseArray(final String doc) throws MJSONException
	{
		try
		{
			return parseArray(doc.toCharArray(), new int[]{0}, 0);
		}
		catch (final MJSONException e)
		{
			throw e;
		}
		catch (final Exception e)
		{
			throw new MJSONException("Internal error", e);
		}
	}

	/**
	 * Converts a pojo field to a JSON value.
	 *
	 * @param type the class type
	 * @param val the value
	 * @return the JSON value
	 */
	public String fromPOJOFieldtoJSON(final Class<?> type, final Object val)
	{
		final StringBuilder str=new StringBuilder("");
		if(val==null)
			str.append(NULL_STR);
		else
		if(type.isArray())
		{
			str.append("[");
			final int length = Array.getLength(val);
			for (int i=0; i<length; i++)
			{
				final Object e = Array.get(val, i);
				if(i>0)
					str.append(",");
				str.append(fromPOJOFieldtoJSON(type.getComponentType(),e));
			}
			str.append("]");
		}
		else
		if(Map.class.isAssignableFrom(type))
		{
			str.append("{");
			@SuppressWarnings("rawtypes")
			final Map map = (Map)val;
			int i = 0;
			@SuppressWarnings("unchecked")
			final Iterator<Object> iter = map.keySet().iterator();
			for (;iter.hasNext();i++)
			{
				final Object e = iter.next();
				if(i>0)
					str.append(",");
				final Object mapVal = map.get(e);
				str.append("\"").append(e.toString()).append("\":");
				str.append(fromPOJOFieldtoJSON(mapVal.getClass().getComponentType(),mapVal));
			}
			str.append("}");
		}
		else
		if(Collection.class.isAssignableFrom(type))
		{
			str.append("[");
			@SuppressWarnings("rawtypes")
			final Collection coll = (Collection)val;
			int i = 0;
			@SuppressWarnings("unchecked")
			final Iterator<Object> iter = coll.iterator();
			for (;iter.hasNext();i++)
			{
				final Object e = iter.next();
				if(i>0)
					str.append(",");
				str.append(fromPOJOFieldtoJSON(type.getComponentType(),e));
			}
			str.append("]");
		}
		else
		if(type == String.class)
			str.append("\"").append(toJSONString(val.toString())).append("\"");
		else
		if(type.isPrimitive())
			str.append(val.toString());
		else
		if((type == Float.class)||(type==Integer.class)||(type==Double.class)||(type==Boolean.class)
		 ||(type==Long.class)||(type==Short.class)||(type==Byte.class))
			str.append(val.toString());
		else
			str.append(fromPOJOtoJSON(val));
		return str.toString();
	}

	/**
	 * Converts a pojo object to a JSON document.
	 *
	 * @param o the object to convert
	 * @return the JSON document
	 */
	public String fromPOJOtoJSON(final Object o)
	{
		if((o==null)||(o.getClass()==null))
			return "null";
		final StringBuilder str=new StringBuilder("");
		str.append("{");
		final Field[] fields = o.getClass().getDeclaredFields();
		if(fields == null)
			return "{}";
		boolean firstField=true;
		for(final Field field : fields)
		{
			try
			{
				try {  field.setAccessible(true);}catch(final Exception e) {}
				if(!firstField)
					str.append(",");
				else
					firstField=false;
				str.append("\"").append(field.getName()).append("\":");
				str.append(fromPOJOFieldtoJSON(field.getType(),field.get(o)));
			}
			catch (final IllegalArgumentException e)
			{
			}
			catch (final IllegalAccessException e)
			{
			}
		}
		str.append("}");
		return str.toString();
	}

	/**
	 * Converts a JSON document to a pojo object.
	 *
	 * @param json the json document
	 * @param o the object to convert
	 * @throws MJSONException a parse exception
	 */
	public void fromJSONtoPOJO(final String json, final Object o) throws MJSONException
	{
		fromJSONtoPOJO(parseObject(json),o);
	}

	/**
	 * Converts a json object to a pojo object.
	 *
	 * @param jsonObj the json object
	 * @param o the object to convert
	 * @throws MJSONException a parse exception
	 */
	public void fromJSONtoPOJO(final MiniJSON.JSONObject jsonObj, final Object o) throws MJSONException
	{
		final Field[] fields = o.getClass().getDeclaredFields();
		for(final Field field : fields)
		{
			try
			{
				try {  field.setAccessible(true);}catch(final Exception e) {}
				if(jsonObj.containsKey(field.getName()))
				{

					final Object jo = jsonObj.get(field.getName());
					if((jo == null) || (jo == MiniJSON.NULL))
						field.set(o, null);
					else
					if(field.getType().isArray() && (jo instanceof Object[]))
					{
						final Object[] objs = (Object[])jo;
						final Object tgt;
						final Class<?> cType = field.getType().getComponentType();
						tgt = Array.newInstance(cType, objs.length);
						for(int i=0;i<objs.length;i++)
						{
							if(objs[i].getClass() == cType)
								Array.set(tgt, i, objs[i]);
							else
							if((cType == Float.class)&&(objs[i] instanceof Double))
								Array.set(tgt, i, Float.valueOf(((Double)objs[i]).floatValue()));
							else
							if((cType == Integer.class)&&(objs[i] instanceof Long))
								Array.set(tgt, i, Integer.valueOf(((Long)objs[i]).intValue()));
							else
							if((cType == Byte.class)&&(objs[i] instanceof Long))
								Array.set(tgt, i, Byte.valueOf(((Long)objs[i]).byteValue()));
							else
							if((cType == Short.class)&&(objs[i] instanceof Long))
								Array.set(tgt, i, Short.valueOf(((Long)objs[i]).shortValue()));
							else
							if(cType.isPrimitive())
							{
								if(cType == boolean.class)
									Array.setBoolean(tgt, i, Boolean.parseBoolean(objs[i].toString()));
								else
								if(cType == int.class)
									Array.setInt(tgt, i, Integer.parseInt(objs[i].toString()));
								else
								if(cType == short.class)
									Array.setShort(tgt, i, Short.parseShort(objs[i].toString()));
								else
								if(cType == byte.class)
									Array.setByte(tgt, i, Long.valueOf(objs[i].toString()).byteValue());
								else
								if(cType == long.class)
									Array.setLong(tgt, i, Long.parseLong(objs[i].toString()));
								else
								if(cType == float.class)
									Array.setFloat(tgt, i, Float.parseFloat(objs[i].toString()));
								else
								if(cType == double.class)
									Array.setDouble(tgt, i, Double.parseDouble(objs[i].toString()));
							}
							else
							if(objs[i] instanceof JSONObject)
							{
								Object newObj;
								try
								{
									newObj = cType.getDeclaredConstructor().newInstance();
									fromJSONtoPOJO((JSONObject)objs[i], newObj);
									Array.set(tgt, i, newObj);
								}
								catch (final Exception e)
								{
									e.printStackTrace();
								}
							}
						}
						field.set(o, tgt);
					}
					else
					if((field.getType() == String.class)&&(jo instanceof String))
						field.set(o, jo);
					else
					if(field.getType().isPrimitive())
					{
						final Class<?> cType=field.getType();
						if(cType == boolean.class)
							field.setBoolean(o, Boolean.parseBoolean(jo.toString()));
						else
						if(cType == int.class)
							field.setInt(o, Integer.parseInt(jo.toString()));
						else
						if(cType == short.class)
							field.setShort(o, Short.parseShort(jo.toString()));
						else
						if(cType == byte.class)
							field.setByte(o, Long.valueOf(jo.toString()).byteValue());
						else
						if(cType == long.class)
							field.setLong(o, Long.parseLong(jo.toString()));
						else
						if(cType == float.class)
							field.setFloat(o, Float.parseFloat(jo.toString()));
						else
						if(cType == double.class)
							field.setDouble(o, Double.parseDouble(jo.toString()));
						else
							field.set(o, jo);
					}
					else
					if(jo instanceof JSONObject)
					{
						final Object newObj = field.getType().getDeclaredConstructor().newInstance();
						fromJSONtoPOJO((JSONObject)jo, newObj);
						field.set(o, newObj);
					}
					else
					if((field.getType() == Integer.class)&&(jo instanceof Long))
						field.set(o, Integer.valueOf(((Long)jo).intValue()));
					else
					if((field.getType() == Short.class)&&(jo instanceof Long))
						field.set(o, Short.valueOf(((Long)jo).shortValue()));
					else
					if((field.getType() == Byte.class)&&(jo instanceof Long))
						field.set(o, Byte.valueOf(((Long)jo).byteValue()));
					else
					if((field.getType() == Long.class)&&(jo instanceof Long))
						field.set(o, Long.valueOf(((Long)jo).longValue()));
					else
					if((field.getType() == Double.class)&&(jo instanceof Double))
						field.set(o, Double.valueOf(((Double)jo).doubleValue()));
					else
					if((field.getType() == Float.class)&&(jo instanceof Double))
						field.set(o, Float.valueOf(((Double)jo).floatValue()));
					else
					if((field.getType() == Boolean.class)&&(jo instanceof Boolean))
						field.set(o, Boolean.valueOf(((Boolean)jo).booleanValue()));
					else
						field.set(o, jo);
				}
			}
			catch (final IllegalArgumentException e)
			{
				throw new MJSONException(e.getMessage(),e);
			}
			catch (final IllegalAccessException e)
			{
				// just continue
			}
			catch (final InstantiationException e)
			{
				throw new MJSONException(e.getMessage(),e);
			}
			catch (final InvocationTargetException e)
			{
				throw new MJSONException(e.getMessage(),e);
			}
			catch (final NoSuchMethodException e)
			{
				throw new MJSONException(e.getMessage(),e);
			}
			catch (final SecurityException e)
			{
				throw new MJSONException(e.getMessage(),e);
			}
		}
	}
}
