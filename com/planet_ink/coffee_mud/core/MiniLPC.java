package com.planet_ink.coffee_mud.core;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.lang.reflect.*;

/*
   Copyright 2024-2024 Bo Zimmerman

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
 * A LPC parser.
 * Not much to say.  It can take a valid json string and generate a standing
 * object that represents the document string, and also generate a string
 * from such an object.
 * @author Bo Zimmerman
 *
 */
public class MiniLPC
{
	/**
	 * LPC object-level state machine states.
	 */
	private enum ObjectParseState {
		/**
		 * Waiting for the first ( character.
		 */
		INITIAL,
		/**
		 * Wait for the [ or { character
		 */
		INITIAL2,
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
		 * Got ], need the }.
		 */
		NEEDEND
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
		 * Got a dot, so need a non-dot digit, or e
		 */
		NEEDDOTDIGIT,
		/**
		 * Waiting for more dot digits, or e
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
		 * Waiting for the opening (.
		 */
		INITIAL,
		/**
		 * Wait for the [
		 */
		INITIAL2,
		/**
		 * Got a comma so expect only another object.
		 */
		NEEDOBJECT,
		/**
		 * Got an object, so expect comma.
		 */
		GOTOBJECT,
		/**
		 * Expect ]
		 */
		EXPECTEND
	}

	/**
	 * The official definition of "null" for a LPC object
	 */
	public static final Object NULL = new Object();

	/**
	 * An official MiniLPC parsing exception. It means the document being parsed was malformed in some way.
	 * @author Bo Zimmerman
	 */
	public static class MLPCException extends Exception
	{
		private static final long serialVersionUID = -2651922052891126260L;

		/**
		 * Constructs a new exception with the given parse error
		 * @param string the parse error
		 */
		public MLPCException(final String string)
		{
			super(string);
		}

		/**
		 * Constructs a new exception with the given parse error, and underlying cause
		 * @param string the parse error
		 * @param e an underlying cause of the parse error
		 */
		public MLPCException(final String string, final Exception e)
		{
			super(string, e);
		}
	}

	/**
	 * Given a normal string, this method will return a LPC-Safe
	 * string, which means escaped crlf, escaped tabs and backslashes, etc.
	 * @param value the unsafe string
	 * @return the LPC safe string
	 */
	public static String toLPCString(final String value)
	{
		final StringBuilder strBldr=new StringBuilder("");
		for(final char c : value.toCharArray())
		{
			switch(c)
			{
			case '\"':
			case '\\':
				strBldr.append('\\').append(c);
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
				strBldr.append(c);
				break;
			}
		}
		return strBldr.toString();
	}

	/**
	 * An official LPC object.  Implemented as a Map, this class
	 * has numerous methods for accessing the internal keys and
	 * their mapped values in different ways, both raw, and checked.
	 * @author Bo Zimmerman
	 */
	public static class LPCObject extends TreeMap<String,Object>
	{
		private static final long serialVersionUID = 8390676973120915175L;

		/**
		 * Internal method that returns a raw value object, or throws
		 * an exception if the key is not found
		 * @param key the key to look for
		 * @return the raw Object the key is mapped to
		 * @throws MLPCException the key was not found
		 */
		private Object getCheckedObject(final String key) throws MLPCException
		{
			if(!containsKey(key))
				throw new MLPCException("Key '"+key+"' not found");
			return get(key);
		}

		/**
		 * Returns a LPCObject mapped in THIS object by the given key.
		 * Throws an exception if anything goes wrong.
		 * @param key the key of the object
		 * @return the LPC Object mapped to by that key
		 * @throws MLPCException a missing key, or not a LPC Object
		 */
		public LPCObject getCheckedLPCObject(final String key) throws MLPCException
		{
			final Object o = getCheckedObject(key);
			if(!(o instanceof LPCObject))
				throw new MLPCException("Key '"+key+"' is not a LPC object");
			return (LPCObject)o;
		}

		/**
		 * Returns a LPC Array mapped in this object by the given key.
		 * Throws an exception if anything goes wrong.
		 * @param key the key of the Array
		 * @return the LPC Array mapped to by that key
		 * @throws MLPCException a missing key, or not a LPC Array
		 */
		public Object[] getCheckedArray(final String key) throws MLPCException
		{
			final Object o = getCheckedObject(key);
			if(!(o instanceof Object[]))
				throw new MLPCException("Key '"+key+"' is not an array");
			return (Object[])o;
		}

		/**
		 * Returns a String mapped in this object by the given key.
		 * Throws an exception if anything goes wrong.
		 * @param key the key of the String
		 * @return the String mapped to by that key
		 * @throws MLPCException a missing key, or not a String
		 */
		public String getCheckedString(final String key) throws MLPCException
		{
			final Object o = getCheckedObject(key);
			if(!(o instanceof String))
				throw new MLPCException("Key '"+key+"' is not a String");
			return (String)o;
		}

		/**
		 * Returns a Long mapped in this object by the given key.
		 * Throws an exception if anything goes wrong.
		 * @param key the key of the Long
		 * @return the Long mapped to by that key
		 * @throws MLPCException a missing key, or not a Long
		 */
		public Long getCheckedLong(final String key) throws MLPCException
		{
			final Object o = getCheckedObject(key);
			if(!(o instanceof Long))
				throw new MLPCException("Key '"+key+"' is not a long");
			return (Long)o;
		}

		/**
		 * Returns a Double mapped in this object by the given key.
		 * Throws an exception if anything goes wrong.
		 * @param key the key of the Long
		 * @return the Double mapped to by that key
		 * @throws MLPCException a missing key, or not a Double
		 */
		public Double getCheckedDouble(final String key) throws MLPCException
		{
			final Object o = getCheckedObject(key);
			if(!(o instanceof Double))
				throw new MLPCException("Key '"+key+"' is not a double");
			return (Double)o;
		}

		/**
		 * Returns a Boolean mapped in this object by the given key.
		 * Throws an exception if anything goes wrong.
		 * @param key the key of the Long
		 * @return the Boolean mapped to by that key
		 * @throws MLPCException a missing key, or not a Boolean
		 */
		public Boolean getCheckedBoolean(final String key) throws MLPCException
		{
			final Object o = getCheckedObject(key);
			if(!(o instanceof Boolean))
				throw new MLPCException("Key '"+key+"' is not a boolean");
			return (Boolean)o;
		}

		/**
		 * Returns a numeric value mapped in this object by the given key.
		 * Throws an exception if anything goes wrong.
		 * @param key the key of the Long
		 * @return the double value of the number mapped to by that key
		 * @throws MLPCException a missing key, or not a numeric value
		 */
		public double getCheckedNumber(final String key) throws MLPCException
		{
			final Object o = getCheckedObject(key);
			if(o instanceof Double)
				return ((Double)o).doubleValue();
			if(o instanceof Long)
				return ((Long)o).doubleValue();
			throw new MLPCException("Key '"+key+"' is not a number");
		}

		/**
		 * Checks this object for the given key, and checks if it
		 * is an official NULL or not.
		 * Throws an exception if the key is missing.
		 * @param key the key of the possible null
		 * @return true if the key maps to NULL or false otherwise
		 * @throws MLPCException the key was missing
		 */
		public boolean isCheckedNULL(final String key) throws MLPCException
		{
			final Object o = getCheckedObject(key);
			return o == NULL;
		}

		/**
		 * Correctly appends the given thing to the given stringbuffer which
		 * is assumed to be in the middle of a LPC obect definition, right
		 * after the key and the colon:
		 * @param value the StringBuffer to append a value to
		 * @param obj the value to append -- a string, null, array, or number
		 */
		public static void appendLPCValue(final StringBuilder value, final Object obj)
		{
			if(obj instanceof String)
			{
				value.append("\"").append(toLPCString((String)obj)).append("\"");
			}
			else
			if(obj == NULL)
			{
				value.append("null");
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
					appendLPCValue(value, array[i]);
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
		 * Returns a full LPC document representation of this LPC object
		 */
		@Override
		public String toString()
		{
			final StringBuilder value = new StringBuilder("");
			value.append("{");
			for(final Iterator<String> k = keySet().iterator(); k.hasNext();)
			{
				final String keyVar = k.next();
				value.append("\"").append(toLPCString(keyVar)).append("\":");
				final Object obj = get(keyVar);
				appendLPCValue(value, obj);
				if(k.hasNext())
				{
					value.append(",");
				}
			}
			value.append("}");
			return value.toString();
		}

		public Object lpcDeepCopy(final Object obj)
		{
			if(obj instanceof LPCObject)
				return ((LPCObject)obj).copyOf();
			else
			if(obj.getClass().isArray())
			{
				final Object[] newArray = Arrays.copyOf((Object[])obj, ((Object[])obj).length);
				for(int i=0;i<newArray.length;i++)
					newArray[i] = lpcDeepCopy(newArray[i]);
				return newArray;
			}
			else
				return obj;
		}

		public LPCObject copyOf()
		{
			final LPCObject newObj = new LPCObject();
			for(final String key : this.keySet())
				newObj.put(key, lpcDeepCopy(this.get(key)));
			return newObj;
		}

		public void putString(final String key, final String value)
		{
			if(value == null)
				this.put(key, NULL);
			else
			{
				final String fixedValue=MiniLPC.toLPCString(value);
				this.put(key, fixedValue);
			}
		}
	}

	/**
	 * Parse either an Long, or Double object from the doc buffer
	 * @param doc the full LPC document
	 * @param index one dimensional array containing current index into the doc
	 * @return either an Long or a Double
	 * @throws MLPCException any parsing errors
	 */
	private Object parseNumber(final char[] doc, final int[] index) throws MLPCException
	{
		final int numStart = index[0];
		NumberParseState state = NumberParseState.INITIAL;
		while(index[0] < doc.length)
		{
			final char c=doc[index[0]];
			switch(state)
			{
			case INITIAL:
				if(c=='0')
					state=NumberParseState.NEEDDOT;
				else
				if(c=='-')
					state=NumberParseState.NEEDNODASH;
				else
				if(Character.isDigit(c))
					state=NumberParseState.HAVEDIGIT;
				else
					throw new MLPCException("Expected digit at "+index[0]);
				break;
			case NEEDNODASH:
				if(c=='-')
					throw new MLPCException("Expected digit at "+index[0]);
				else
				if(c=='0')
					state=NumberParseState.NEEDDOT;
				else
				if(Character.isDigit(c))
					state=NumberParseState.HAVEDIGIT;
				else
					throw new MLPCException("Expected digit at "+index[0]);
				break;
			case HAVEDIGIT:
				if(c=='.')
					state=NumberParseState.NEEDDOTDIGIT;
				else
				if((c=='E')||(c=='e'))
					state=NumberParseState.HAVEE;
				else
				if(Character.isDigit(c))
					state=NumberParseState.HAVEDIGIT;
				else
				{
					index[0]--;
					return Long.valueOf(new String(doc,numStart,index[0]-numStart+1));
				}
				break;
			case NEEDDOT:
				if(c=='.')
					state=NumberParseState.NEEDDOTDIGIT;
				else
				{
					index[0]--;
					return Long.valueOf(new String(doc,numStart,index[0]-numStart+1));
				}
				break;
			case NEEDDOTDIGIT:
				if(Character.isDigit(c))
					state=NumberParseState.HAVEDOTDIGIT;
				else
					throw new MLPCException("Expected digit at "+index[0]);
				break;
			case HAVEDOTDIGIT:
				if(Character.isDigit(c))
					state=NumberParseState.HAVEDOTDIGIT;
				else
				if((c=='e')||(c=='E'))
					state=NumberParseState.HAVEE;
				else
				{
					index[0]--;
					return Double.valueOf(new String(doc,numStart,index[0]-numStart+1));
				}
				break;
			case HAVEE:
				if(c=='0')
					throw new MLPCException("Expected non-zero digit at "+index[0]);
				else
				if(Character.isDigit(c)||(c=='+')||(c=='-'))
					state=NumberParseState.HAVEEDIGIT;
				else
					throw new MLPCException("Expected +- or non-zero digit at "+index[0]);
				break;
			case HAVEEDIGIT:
				if(!Character.isDigit(c))
				{
					index[0]--;
					return Double.valueOf(new BigDecimal(new String(doc,numStart,index[0]-numStart+1)).doubleValue());
				}
				break;
			}
			index[0]++;
		}
		throw new MLPCException("Unexpected end of number at"+index[0]);
	}

	/**
	 * Given a char array, and index into it, returns the byte value of the 1 hex
	 * digits at the indexed point of the char array.
	 * @param doc the json doc containing a hex number
	 * @param index the index into that json doc where the hex number begins
	 * @return the byte value of the 1 digit hex number
	 * @throws MLPCException a parse error meaning it wasn't a hex number at all
	 */
	private byte getByteFromHex(final char[] doc, final int index) throws MLPCException
	{
		final char c = doc[index];
		if((c >= '0') && (c <= '9'))
			return (byte)(c-'0');
		if((c >= 'a') && (c <= 'f'))
			return (byte)(10 + (c-'a'));
		if((c >= 'A') && (c <= 'F'))
			return (byte)(10 + (c-'A'));
		throw new MLPCException("Illegal hex digit at "+index);
	}

	/**
	 * Given a json document char array, and an index into it, parses a string at
	 * the indexed point of the char array and returns its value.
	 * @param doc the json doc containing the string
	 * @param index the index into that json doc where the string begins
	 * @return the value of the found string
	 * @throws MLPCException a parse exception, meaning no string was there
	 */
	private String parseString(final char[] doc, final int[] index) throws MLPCException
	{
		final StringBuilder value=new StringBuilder("");
		if(doc[index[0]]!='\"')
		{
			throw new MLPCException("Expected quote at: "+doc[index[0]]);
		}
		index[0]++;
		while(index[0] < doc.length)
		{
			final char c=doc[index[0]];
			if(c=='\"')
				return value.toString();
			else
			if(c=='\\')
			{
				if(index[0] == doc.length-1)
					throw new MLPCException("Unfinished escape");
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
						throw new MLPCException("Unfinished unicode escape at "+index[0]);
					final byte[] hexBuf=new byte[4];
					hexBuf[0] = getByteFromHex(doc,++index[0]);
					hexBuf[1] = getByteFromHex(doc,++index[0]);
					hexBuf[2] = getByteFromHex(doc,++index[0]);
					hexBuf[3] = getByteFromHex(doc,++index[0]);
					try
					{
						value.append(new String(hexBuf, "Cp1251"));
					}
					catch (final UnsupportedEncodingException e)
					{
						throw new MLPCException("Illegal character at"+index[0],e);
					}
					break;
				}
				default:
					throw new MLPCException("Illegal escape character: "+doc[index[0]]);
				}
			}
			else
				value.append(c);
			index[0]++;
		}
		throw new MLPCException("Unfinished string at "+index[0]);
	}

	/**
	 * Given a json document char array, and an index into it, parses an array at
	 * the indexed point of the char array and returns its value object.
	 * @param doc the json doc containing the array
	 * @param index the index into that json doc where the array begins
	 * @return the value object of the found array
	 * @throws MLPCException a parse exception, meaning no array was there
	 */
	private Object[] parseArray(final char[] doc, final int[] index) throws MLPCException
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
					if(c=='(')
						state = ArrayParseState.INITIAL2;
					else
						throw new MLPCException("Expected ( at "+index[0]);
					break;
				case INITIAL2:
					if(c=='{')
						state = ArrayParseState.NEEDOBJECT;
					else
						throw new MLPCException("Expected { at "+index[0]);
					break;
				case EXPECTEND:
					if(c==')')
						return finalSet.toArray(new Object[0]);
					else
						throw new MLPCException("Expected { at "+index[0]);
				case NEEDOBJECT:
					if(c=='}')
						state = ArrayParseState.EXPECTEND;
					else
					{
						finalSet.add(parseElement(doc,index));
						state = ArrayParseState.GOTOBJECT;
					}
					break;
				case GOTOBJECT:
					if(c==',')
						state = ArrayParseState.NEEDOBJECT;
					else
						throw new MLPCException("Expected , at "+index[0]);
					break;
				}
			}
			index[0]++;
		}
		throw new MLPCException("Expected ] at "+index[0]);
	}

	/**
	 * Given a json document char array, and an index into it, parses a value object at
	 * the indexed point of the char array and returns its value object.  A value object
	 * may be anything from a string, array, a LPC object, boolean, null, or a number.
	 * @param doc the json doc containing the value
	 * @param index the index into that json doc where the value begins
	 * @return the value object of the found value
	 * @throws MLPCException a parse exception, meaning no recognized value was there
	 */
	private Object parseElement(final char[] doc, final int[] index) throws MLPCException
	{
		switch(doc[index[0]])
		{
		case '\"':
			return parseString(doc,index);
		case '(':
		{
			int dex = index[0];
			while(++dex < doc.length)
			{
				if(!Character.isWhitespace(doc[dex]))
				{
					if(doc[dex]=='{')
						return parseArray(doc,index);
					else
					if(doc[dex]=='[')
						return parseObject(doc,index);
					else
						throw new MLPCException("Invalid character "+dex);
				}
			}
			throw new MLPCException("Unopened ( "+doc[dex]);
		}
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
			if((index[0] < doc.length-5) && (new String(doc,index[0],4).equals("true")))
			{
				index[0]+=3;
				return Boolean.TRUE;
			}
			throw new MLPCException("Invalid true at "+index[0]);
		case 'f':
			if((index[0] < doc.length-6) && (new String(doc,index[0],5).equals("false")))
			{
				index[0]+=4;
				return Boolean.FALSE;
			}
			throw new MLPCException("Invalid false at "+index[0]);
		case 'n':
			if((index[0] < doc.length-5) && (new String(doc,index[0],4).equals("null")))
			{
				index[0]+=3;
				return NULL;
			}
			throw new MLPCException("Invalid null at "+index[0]);
		default:
			throw new MLPCException("Unknown character at "+index[0]);
		}
	}

	/**
	 * Given a json document char array, and an index into it, parses a LPC object at
	 * the indexed point of the char array and returns it as a mapped LPC object.
	 * @param doc the json doc containing the LPC object
	 * @param index the index into that json doc where the LPC object begins
	 * @return the value object of the found LPC object
	 * @throws MLPCException a parse exception, meaning no LPC object was there
	 */
	private LPCObject parseObject(final char[] doc, final int[] index) throws MLPCException
	{
		final LPCObject map = new LPCObject();
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
					if(c=='(')
						state = ObjectParseState.INITIAL2;
					else
						throw new MLPCException("Expected ( at "+index[0]);
					break;
				case INITIAL2:
					if(c=='[')
						state = ObjectParseState.NEEDKEY;
					else
						throw new MLPCException("Expected [ at "+index[0]);
					break;
				case NEEDEND:
					if(c==')')
						return map;
					else
						throw new MLPCException("Expected ) at "+index[0]);
				case NEEDKEY:
					if(c=='\"')
					{
						key = parseString(doc,index);
						state = ObjectParseState.GOTKEY;
					}
					else
					if(c==']')
						state = ObjectParseState.NEEDEND;
					else
						throw new MLPCException("Expected Key/String at "+index[0]);
					break;
				case GOTKEY:
					if(c==':')
						state = ObjectParseState.NEEDOBJECT;
					else
						throw new MLPCException("Expected Colon at "+index[0]);
					break;
				case NEEDOBJECT:
					map.put(key, parseElement(doc,index));
					state = ObjectParseState.GOTOBJECT;
					break;
				case GOTOBJECT:
					if(c==',')
						state = ObjectParseState.NEEDKEY;
					else
						throw new MLPCException("Expected , at "+index[0]);
					break;
				}
			}
			index[0]++;
		}
		throw new MLPCException("Expected } at "+index[0]);
	}

	/**
	 * Given a string containing a LPC object, this method will parse it
	 * into a mapped LPCObject object recursively.
	 * @param doc the LPC document that contains a top-level LPC object
	 * @return the LPC object at the top level
	 * @throws MLPCException the parse error
	 */
	public LPCObject parseObject(final String doc) throws MLPCException
	{
		try
		{
			return parseObject(doc.toCharArray(), new int[]{0});
		}
		catch (final MLPCException e)
		{
			throw e;
		}
		catch (final Exception e)
		{
			throw new MLPCException("Internal error",e);
		}
	}

	/**
	 * Given a string containing a LPC something, this method will parse it
	 * into an object recursively.
	 * @param doc the LPC document that contains a top-level LPC object
	 * @return the LPC object at the top level
	 * @throws MLPCException the parse error
	 */
	public Object parseLPC(final String doc) throws MLPCException
	{
		try
		{
			return parseElement(doc.toCharArray(), new int[]{0});
		}
		catch (final MLPCException e)
		{
			throw e;
		}
		catch (final Exception e)
		{
			throw new MLPCException("Internal error",e);
		}
	}

	/**
	 * Converts a pojo field to a LPC value.
	 * @param type the class type
	 * @param val the value
	 * @return the json value
	 */
	public String fromPOJOFieldtoLPC(final Class<?> type, final Object val)
	{
		final StringBuilder str=new StringBuilder("");
		if(val==null)
			str.append("null");
		else
		if(type.isArray())
		{
			str.append("({");
			final int length = Array.getLength(val);
			for (int i=0; i<length; i++)
			{
				final Object e = Array.get(val, i);
				str.append(fromPOJOFieldtoLPC(type.getComponentType(),e));
				str.append(",");
			}
			str.append("})");
		}
		else
		if(type == String.class)
			str.append("\"").append(toLPCString(val.toString())).append("\"");
		else
		if(type.isPrimitive())
			str.append(val.toString());
		else
		if((type == Float.class)||(type==Integer.class)||(type==Double.class)||(type==Boolean.class)
		 ||(type==Long.class)||(type==Short.class)||(type==Byte.class))
			str.append(val.toString());
		else
			str.append(fromPOJOtoLPC(val));
		return str.toString();
	}

	/**
	 * Converts a pojo object to a LPC document.
	 * @param o the object to convert
	 * @return the json document
	 */
	public String fromPOJOtoLPC(final Object o)
	{
		final StringBuilder str=new StringBuilder("");
		str.append("([");
		final Field[] fields = o.getClass().getDeclaredFields();
		for(final Field field : fields)
		{
			try
			{
				field.setAccessible(true);
				if(field.isAccessible())
				{
					str.append("\"").append(field.getName()).append("\":");
					str.append(fromPOJOFieldtoLPC(field.getType(),field.get(o)));
					str.append(",");
				}
			}
			catch (final IllegalArgumentException e)
			{
			}
			catch (final IllegalAccessException e)
			{
			}
		}
		str.append("])");
		return str.toString();
	}

	/**
	 * Converts a LPC document to a pojo object.
	 * @param json the json document
	 * @param o the object to convert
	 * @throws MLPCException a parse exception
	 */
	public void fromLPCtoPOJO(final String json, final Object o) throws MLPCException
	{
		fromLPCtoPOJO(parseObject(json),o);
	}
	/**
	 * Converts a json object to a pojo object.
	 * @param jsonObj the json object
	 * @param o the object to convert
	 * @throws MLPCException a parse exception
	 */
	public void fromLPCtoPOJO(final MiniLPC.LPCObject jsonObj, final Object o) throws MLPCException
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
					if((jo == null) || (jo == MiniLPC.NULL))
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
							if(objs[i] instanceof LPCObject)
							{
								Object newObj;
								try
								{
									newObj = cType.getDeclaredConstructor().newInstance();
									fromLPCtoPOJO((LPCObject)objs[i], newObj);
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
					if(jo instanceof LPCObject)
					{
						final Object newObj = field.getType().getDeclaredConstructor().newInstance();
						fromLPCtoPOJO((LPCObject)jo, newObj);
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
				throw new MLPCException(e.getMessage(),e);
			}
			catch (final IllegalAccessException e)
			{
				throw new MLPCException(e.getMessage(),e);
			}
			catch (final InstantiationException e)
			{
				throw new MLPCException(e.getMessage(),e);
			}
			catch (final InvocationTargetException e)
			{
				throw new MLPCException(e.getMessage(),e);
			}
			catch (final NoSuchMethodException e)
			{
				throw new MLPCException(e.getMessage(),e);
			}
			catch (final SecurityException e)
			{
				throw new MLPCException(e.getMessage(),e);
			}
		}
	}
}
