package com.planet_ink.coffee_mud.core;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/*
   Copyright 2013-2025 Bo Zimmerman

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
public final class MiniJSON
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
     * Literal definition for NULL.
     */
    private static final String NULL_STR = "null";
    /**
     * Literal definition for TRUE.
     */
    private static final String TRUE_STR = "true";
    /**
     * Literal definition for FALSE.
     */
    private static final String FALSE_STR = "false";
    /**
     * Literal definition for four zeroes.
     */
    private static final String ZEROES = "0000";
    /**
     * Length of literal definition for ZEROES.
     */
    private static final int ZEROES_LEN = ZEROES.length();


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
	 * The official definition of "null" for a JSON object.
	 */
	public static final Object NULL = new Object();

	/**
	 * An official MiniJSON parsing exception. It means the document being
	 * parsed was malformed in some way.
	 *
	 * @author Bo Zimmerman
	 */
	public static final class MJSONException extends Exception
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
		public static void appendJSONValue(final StringBuilder value, final Object obj)
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
			if(obj instanceof JSONObject)
				return ((JSONObject)obj).copyOf();
			else
			if(obj.getClass().isArray())
			{
				final Object[] newArray = Arrays.copyOf((Object[])obj, ((Object[])obj).length);
				for(int i=0;i<newArray.length;i++)
					newArray[i] = jsonDeepCopy(newArray[i]);
				return newArray;
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

        /**
         * Adds a string:string key pair to this JSONObject.
         * @param key the key
         * @param value the value, which is fixed by this method.
         */
 		public void putString(final String key, final String value)
		{
			if(value == null)
				this.put(key, NULL);
			else
			{
				final String fixedValue=MiniJSON.toJSONString(value);
				this.put(key, fixedValue);
			}
		}

        /**
         * Adds a string:string key pair to this JSONObject.
         * @param key the key
         * @param value the value, which is fixed by this method.
         */
 		public void putString(final String key, final byte[] value)
		{
			if(value == null)
				this.put(key, NULL);
			else
			{
				final String fixedValue=MiniJSON.toJSONString(value);
				this.put(key, fixedValue);
			}
		}
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
		final int numStart = index[0];
		NumberParseState state = NumberParseState.INITIAL;
		while(index[0] <= doc.length)
		{
            final char c = (index[0] < doc.length) ? doc[index[0]] : '\0';
			switch(state)
			{
			case INITIAL:
				if (c == '0')
					state=NumberParseState.NEEDDOT;
				else
				if (c == '-')
					state=NumberParseState.NEEDNODASH;
				else
				if (Character.isDigit(c))
					state=NumberParseState.HAVEDIGIT;
				else
					throw new MJSONException("Expected digit at "+index[0]);
				break;
			case NEEDNODASH:
				if (c == '-')
					throw new MJSONException("Expected digit at "+index[0]);
				else
				if (c == '0')
					state=NumberParseState.NEEDDOT;
				else
				if (Character.isDigit(c))
					state=NumberParseState.HAVEDIGIT;
				else
					throw new MJSONException("Expected digit at "+index[0]);
				break;
			case HAVEDIGIT:
				if (c == '.')
					state=NumberParseState.NEEDDOTDIGIT;
				else
				if ((c == 'E') || (c == 'e'))
					state=NumberParseState.HAVEE;
				else
				if(Character.isDigit(c))
					state=NumberParseState.HAVEDIGIT;
				else
				{
					index[0]--;
                    final String numStr = new String(doc, numStart, index[0] - numStart + 1);
                    try {
                        return Long.valueOf(numStr);
                    } catch (final NumberFormatException nxe) {
                        throw new MJSONException("Number Format Exception (" + numStr + ")", nxe);
                    }
				}
				break;
			case NEEDDOT:
				if (c == '.')
					state=NumberParseState.NEEDDOTDIGIT;
				else
				if ((c == 'E') || (c == 'e'))
                    state = NumberParseState.HAVEE;
				else
				{
					index[0]--;
                    final String numStr = new String(doc, numStart, index[0] - numStart + 1);
                    try {
                        return Long.valueOf(numStr);
                    } catch (final NumberFormatException nxe) {
                        throw new MJSONException("Number Format Exception (" + numStr + ")", nxe);
                    }
				}
				break;
			case NEEDDOTDIGIT:
				if (Character.isDigit(c))
					state=NumberParseState.HAVEDOTDIGIT;
				else
					throw new MJSONException("Expected digit at "+index[0]);
				break;
			case HAVEDOTDIGIT:
				if (Character.isDigit(c))
					state=NumberParseState.HAVEDOTDIGIT;
				else
				if ((c == 'e') || (c == 'E'))
					state=NumberParseState.HAVEE;
				else
				{
					index[0]--;
                    final String numStr = new String(doc, numStart, index[0] - numStart + 1);
                    try {
                        return Double.valueOf(numStr);
                    } catch (final NumberFormatException nxe) {
                        throw new MJSONException("Number Format Exception (" + numStr + ")", nxe);
                    }
				}
				break;
			case HAVEE:
				if(c == '0')
					throw new MJSONException("Expected non-zero digit at "+index[0]);
				else
				if (Character.isDigit(c) || (c == '+') || (c == '-'))
					state=NumberParseState.HAVEEDIGIT;
				else
					throw new MJSONException("Expected +- or non-zero digit at "+index[0]);
				break;
			case HAVEEDIGIT:
				if(!Character.isDigit(c))
				{
					index[0]--;
                    final String numStr = new String(doc, numStart, index[0] - numStart + 1);
                    try {
                        return Double.valueOf(new BigDecimal(numStr).doubleValue());
                    } catch (final NumberFormatException nxe) {
                        throw new MJSONException("Number Format Exception (" + numStr + ")", nxe);
                    }
				}
				break;
			}
			index[0]++;
		}
		throw new MJSONException("Unexpected end of number at"+index[0]);
	}

	/**
     * Given a char array, and index into it, returns the nybble value of the 1
     * hex digits at the indexed point of the char array.
	 *
	 * @param doc the json doc containing a hex number
	 * @param index the index into that json doc where the hex number begins
	 * @return the byte value of the 1 digit hex nybble
	 * @throws MJSONException a parse error meaning it wasn't a hex number at all
	 */
	private byte getHexNybble(final char[] doc, final int index) throws MJSONException
	{
		final char c = doc[index];
		if((c >= '0') && (c <= '9'))
			return (byte)(c-'0');
		if((c >= 'a') && (c <= 'f'))
			return (byte)(10 + (c-'a'));
		if((c >= 'A') && (c <= 'F'))
			return (byte)(10 + (c-'A'));
		throw new MJSONException("Illegal hex digit at "+index);
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
		index[0]++;
		while(index[0] < doc.length)
		{
			final char c=doc[index[0]];
			if (c == '\"')
				return value.toString();
			else
			if (c == '\\')
			{
				if(index[0] == doc.length-1)
					throw new MJSONException("Unfinished escape");
				index[0]++;
				switch(doc[index[0]])
				{
				case '\"':
				case '\\':
				case '/':
					value.append(doc[index[0]]);
					break;
				case 'b':
					value.append('\b');
					break;
				case 'f':
					value.append('\f');
					break;
				case 'n':
					value.append('\n');
					break;
				case 'r':
					value.append('\r');
					break;
				case 't':
					value.append('\t');
					break;
				case 'u':
				{
					if(index[0] >= doc.length-5)
						throw new MJSONException("Unfinished unicode escape at "+index[0]);
					final byte[] hexBuf=new byte[] {
						(byte)((getHexNybble(doc,++index[0]) << 4) | getHexNybble(doc,++index[0])),
						(byte)((getHexNybble(doc,++index[0]) << 4) | getHexNybble(doc,++index[0]))
					};
					value.append(new String(hexBuf, StandardCharsets.UTF_16));
					break;
				}
				default:
					throw new MJSONException("Illegal escape character: "+doc[index[0]]);
				}
			}
			else
				value.append(c);
			index[0]++;
		}
		throw new MJSONException("Unfinished string at "+index[0]);
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
			if(!Character.isWhitespace(c))
			{
				switch(state)
				{
				case INITIAL:
					if (c == '[')
						state = ArrayParseState.NEEDOBJECT;
					else
						throw new MJSONException("Expected String at "+index[0]);
					break;
				case EXPECTOBJECT:
					finalSet.add(parseElement(doc,index,depth));
					state = ArrayParseState.GOTOBJECT;
					break;
				case NEEDOBJECT:
					if (c == ']')
						return finalSet.toArray(new Object[0]);
					else
					{
						finalSet.add(parseElement(doc,index,depth));
						state = ArrayParseState.GOTOBJECT;
					}
					break;
				case GOTOBJECT:
					if (c == ']')
						return finalSet.toArray(new Object[0]);
					else
					if (c == ',')
						state = ArrayParseState.EXPECTOBJECT;
					else
						throw new MJSONException("Expected ] or , at "+index[0]);
					break;
				}
			}
			index[0]++;
		}
		throw new MJSONException("Expected ] at "+index[0]);
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
	private Object parseElement(final char[] doc, final int[] index, final int depth) throws MJSONException
	{
        while (index[0] < doc.length && Character.isWhitespace(doc[index[0]])) {
            index[0]++ ;
        }
        if (index[0] >= doc.length) {
            throw new MiniJSON.MJSONException("Unexpected end of document @"+index[0]);
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
			if((index[0] < doc.length-5) && (new String(doc,index[0],4).equals(TRUE_STR)))
			{
				index[0]+=3;
				return Boolean.TRUE;
			}
			throw new MJSONException("Invalid true at "+index[0]);
		case 'f':
			if((index[0] < doc.length-6) && (new String(doc,index[0],5).equals(FALSE_STR)))
			{
				index[0]+=4;
				return Boolean.FALSE;
			}
			throw new MJSONException("Invalid false at "+index[0]);
		case 'n':
			if((index[0] < doc.length-5) && (new String(doc,index[0],4).equals(NULL_STR)))
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
		final JSONObject map = new JSONObject();
		String key = null;
		ObjectParseState state = ObjectParseState.INITIAL;
		while(index[0] < doc.length)
		{
			final char c=doc[index[0]];
			if(!Character.isWhitespace(c))
			{
				switch(state)
				{
				case INITIAL:
					if (c == '{')
						state = ObjectParseState.NEEDKEY;
					else
						throw new MJSONException("Expected Key/String at "+index[0]);
					break;
				case NEEDKEY:
                    case NEEDNEWKEY:
					if(c=='\"')
					{
						key = parseString(doc,index);
						state = ObjectParseState.GOTKEY;
					}
					else
                    if ((c == '}') && (state == ObjectParseState.NEEDKEY))
						return map;
					else
						throw new MJSONException("Expected Key/String at "+index[0]);
					break;
				case GOTKEY:
					if (c == ':')
						state = ObjectParseState.NEEDOBJECT;
					else
						throw new MJSONException("Expected Colon at "+index[0]);
					break;
				case NEEDOBJECT:
					map.put(key, parseElement(doc,index,depth));
					state = ObjectParseState.GOTOBJECT;
					break;
				case GOTOBJECT:
					if (c == ',')
						state = ObjectParseState.NEEDKEY;
					else
					if (c == '}')
						return map;
					else
						throw new MJSONException("Expected } or , at "+index[0]);
					break;
				}
			}
			index[0]++;
		}
		throw new MJSONException("Expected } at "+index[0]);
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
		final StringBuilder str=new StringBuilder("");
		str.append("{");
		final Field[] fields = o.getClass().getDeclaredFields();
		boolean firstField=true;
		for(final Field field : fields)
		{
			try
			{
				field.setAccessible(true);
				if(field.isAccessible())
				{
					if(!firstField)
						str.append(",");
					else
						firstField=false;
					str.append("\"").append(field.getName()).append("\":");
					str.append(fromPOJOFieldtoJSON(field.getType(),field.get(o)));
				}
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
				field.setAccessible(true);
				if(field.isAccessible() && jsonObj.containsKey(field.getName()))
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
									Array.setBoolean(tgt, i, Boolean.valueOf(objs[i].toString()).booleanValue());
								else
								if(cType == int.class)
									Array.setInt(tgt, i, Long.valueOf(objs[i].toString()).intValue());
								else
								if(cType == short.class)
									Array.setShort(tgt, i, Long.valueOf(objs[i].toString()).shortValue());
								else
								if(cType == byte.class)
									Array.setByte(tgt, i, Long.valueOf(objs[i].toString()).byteValue());
								else
								if(cType == long.class)
									Array.setLong(tgt, i, Long.valueOf(objs[i].toString()).longValue());
								else
								if(cType == float.class)
									Array.setFloat(tgt, i, Double.valueOf(objs[i].toString()).floatValue());
								else
								if(cType == double.class)
									Array.setDouble(tgt, i, Double.valueOf(objs[i].toString()).doubleValue());
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
							field.setBoolean(o, Boolean.valueOf(jo.toString()).booleanValue());
						else
						if(cType == int.class)
							field.setInt(o, Long.valueOf(jo.toString()).intValue());
						else
						if(cType == short.class)
							field.setShort(o, Long.valueOf(jo.toString()).shortValue());
						else
						if(cType == byte.class)
							field.setByte(o, Long.valueOf(jo.toString()).byteValue());
						else
						if(cType == long.class)
							field.setLong(o, Long.valueOf(jo.toString()).longValue());
						else
						if(cType == float.class)
							field.setFloat(o, Double.valueOf(jo.toString()).floatValue());
						else
						if(cType == double.class)
							field.setDouble(o, Double.valueOf(jo.toString()).doubleValue());
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
				throw new MJSONException(e.getMessage(),e);
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
