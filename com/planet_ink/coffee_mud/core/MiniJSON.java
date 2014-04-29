package com.planet_ink.coffee_mud.core;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
/*
Copyright 2000-2014 Bo Zimmerman

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
public class MiniJSON
{
	private enum ObjectParseState { INITIAL, NEEDKEY, GOTKEY, NEEDOBJECT, GOTOBJECT }
	private enum NumberParseState { INITIAL, NEEDN0DIGIT, HAVEDIGIT , NEEDDOT, NEEDDOTDIGIT, HAVEDOTDIGIT, HAVEE, HAVEEDIGIT }
	private enum ArrayParseState { INITIAL, EXPECTOBJECT, NEEDOBJECT, GOTOBJECT }

	public static final Object NULL = new Object();

	public static class MJSONException extends Exception
	{
		private static final long serialVersionUID = -2651922052891126260L;
		public MJSONException(String string)
		{
			super(string);
		}

		public MJSONException(String string, Exception e)
		{
			super(string, e);
		}
	}

	public static String toJSONString(final String str)
	{
		StringBuilder strBldr=new StringBuilder("");
		for(char c : str.toCharArray())
		{
			switch(c)
			{
			case '\"':
			case '\\':
			case '/':
				strBldr.append('\\').append(c);
				break;
			case '\b': strBldr.append('\\').append('b'); break;
			case '\f': strBldr.append('\\').append('f'); break;
			case '\n': strBldr.append('\\').append('n'); break;
			case '\r': strBldr.append('\\').append('r'); break;
			case '\t': strBldr.append('\\').append('t'); break;
			default: strBldr.append(c); break;
			}
		}
		return strBldr.toString();
	}

	public static class JSONObject extends TreeMap<String,Object>
	{
		private static final long serialVersionUID = 8390676973120915175L;

		private Object getCheckedObject(String key) throws MJSONException
		{
			if(!containsKey(key))
				throw new MJSONException("Key '"+key+"' not found");
			return get(key);
		}

		public JSONObject getCheckedJSONObject(String key) throws MJSONException
		{
			final Object o = getCheckedObject(key);
			if(!(o instanceof JSONObject))
				throw new MJSONException("Key '"+key+"' is not a JSON object");
			return (JSONObject)o;
		}

		public Object[] getCheckedArray(String key) throws MJSONException
		{
			final Object o = getCheckedObject(key);
			if(!(o instanceof Object[]))
				throw new MJSONException("Key '"+key+"' is not an array");
			return (Object[])o;
		}

		public String getCheckedString(String key) throws MJSONException
		{
			final Object o = getCheckedObject(key);
			if(!(o instanceof String))
				throw new MJSONException("Key '"+key+"' is not a String");
			return (String)o;
		}

		public Long getCheckedLong(String key) throws MJSONException
		{
			final Object o = getCheckedObject(key);
			if(!(o instanceof Long))
				throw new MJSONException("Key '"+key+"' is not a long");
			return (Long)o;
		}

		public Double getCheckedDouble(String key) throws MJSONException
		{
			final Object o = getCheckedObject(key);
			if(!(o instanceof Double))
				throw new MJSONException("Key '"+key+"' is not a double");
			return (Double)o;
		}

		public Boolean getCheckedBoolean(String key) throws MJSONException
		{
			final Object o = getCheckedObject(key);
			if(!(o instanceof Boolean))
				throw new MJSONException("Key '"+key+"' is not a boolean");
			return (Boolean)o;
		}

		public double getCheckedNumber(String key) throws MJSONException
		{
			final Object o = getCheckedObject(key);
			if(o instanceof Double)
				return ((Double)o).doubleValue();
			if(o instanceof Long)
				return ((Long)o).doubleValue();
			throw new MJSONException("Key '"+key+"' is not a number");
		}

		public boolean isCheckedNULL(String key) throws MJSONException
		{
			final Object o = getCheckedObject(key);
			return o == NULL;
		}

		public void appendJSONString(final StringBuilder str, Object obj)
		{
			if(obj instanceof String)
			{
				str.append("\"").append(toJSONString((String)obj)).append("\"");
			}
			else
			if(obj == NULL)
			{
				str.append("null");
			}
			else
			if(obj instanceof Object[])
			{
				str.append("[");
				Object[] array=(Object[])obj;
				for(int i=0; i<array.length; i++)
				{
					if(i>0)
						str.append(",");
					appendJSONString(str, array[i]);
				}
				str.append("]");
			}
			else
			if(obj != null)
			{
				str.append(obj.toString());
			}
		}

		@Override
		public String toString()
		{
			StringBuilder str = new StringBuilder("");
			str.append("{");
			for(Iterator<String> k = keySet().iterator(); k.hasNext();)
			{
				final String keyVar = k.next();
				str.append("\"").append(toJSONString(keyVar)).append("\":");
				final Object obj = get(keyVar);
				appendJSONString(str, obj);
				if(k.hasNext())
				{
					str.append(",");
				}
			}
			str.append("}");
			return str.toString();
		}

	}

	/**
	 * Parse either an Long, or Double object from the doc buffer
	 * @param doc the full JSON document
	 * @param index one dimensional array containing current index into the doc
	 * @return either an Long or a Double
	 * @throws MJSONException any parsing errors
	 */
	private Object parseNumber(char[] doc, int[] index) throws MJSONException
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
					state=NumberParseState.NEEDN0DIGIT;
				else
				if(Character.isDigit(c))
					state=NumberParseState.HAVEDIGIT;
				else
					throw new MJSONException("Expected digit at "+index[0]);
				break;
			case NEEDN0DIGIT:
				if(c=='0')
					throw new MJSONException("Expected digit at "+index[0]);
				else
				if(Character.isDigit(c))
					state=NumberParseState.HAVEDIGIT;
				else
					throw new MJSONException("Expected digit at "+index[0]);
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
					throw new MJSONException("Expected digit at "+index[0]);
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
					throw new MJSONException("Expected non-zero digit at "+index[0]);
				else
				if(Character.isDigit(c)||(c=='+')||(c=='-'))
					state=NumberParseState.HAVEEDIGIT;
				else
					throw new MJSONException("Expected +- or non-zero digit at "+index[0]);
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
		throw new MJSONException("Unexpected end of number at"+index[0]);
	}

	private byte getByteFromHex(char[] doc, int index) throws MJSONException
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

	private String parseString(char[] doc, int[] index) throws MJSONException
	{
		StringBuilder str=new StringBuilder("");
		if(doc[index[0]]!='\"')
		{
			throw new MJSONException("Expectged quote at: "+doc[index[0]]);
		}
		index[0]++;
		while(index[0] < doc.length)
		{
			final char c=doc[index[0]];
			if(c=='\"')
				return str.toString();
			else
			if(c=='\\')
			{
				if(index[0] == doc.length-1)
					throw new MJSONException("Unfinished escape");
				index[0]++;
				switch(doc[index[0]])
				{
				case '\"':
				case '\\':
				case '/':
					str.append(doc[index[0]]);
					break;
				case 'b': str.append('\b'); break;
				case 'f': str.append('\f'); break;
				case 'n': str.append('\n'); break;
				case 'r': str.append('\r'); break;
				case 't': str.append('\t'); break;
				case 'u':
				{
					if(index[0] >= doc.length-5)
						throw new MJSONException("Unfinished unicode escape at "+index[0]);
					byte[] hexBuf=new byte[4];
					hexBuf[0] = getByteFromHex(doc,++index[0]);
					hexBuf[1] = getByteFromHex(doc,++index[0]);
					hexBuf[2] = getByteFromHex(doc,++index[0]);
					hexBuf[3] = getByteFromHex(doc,++index[0]);
					try
					{
						str.append(new String(hexBuf, "Cp1251"));
					}
					catch (UnsupportedEncodingException e)
					{
						throw new MJSONException("Illegal character at"+index[0],e);
					}
					break;
				}
				default:
					throw new MJSONException("Illegal escape character: "+doc[index[0]]);
				}
			}
			else
				str.append(c);
			index[0]++;
		}
		throw new MJSONException("Unfinished string at "+index[0]);
	}

	private Object[] parseArray(char[] doc, int[] index) throws MJSONException
	{
		ArrayParseState state=ArrayParseState.INITIAL;
		List<Object> finalSet=new ArrayList<Object>();
		while(index[0] < doc.length)
		{
			final char c=doc[index[0]];
			if(!Character.isWhitespace(c))
			{
				switch(state)
				{
				case INITIAL:
					if(c=='[')
						state = ArrayParseState.NEEDOBJECT;
					else
						throw new MJSONException("Expected String at "+index[0]);
					break;
				case EXPECTOBJECT:
					finalSet.add(parseElement(doc,index));
					state = ArrayParseState.GOTOBJECT;
					break;
				case NEEDOBJECT:
					if(c==']')
						return finalSet.toArray(new Object[0]);
					else
					{
						finalSet.add(parseElement(doc,index));
						state = ArrayParseState.GOTOBJECT;
					}
					break;
				case GOTOBJECT:
					if(c==']')
						return finalSet.toArray(new Object[0]);
					else
					if(c==',')
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

	private Object parseElement(char[] doc, int[] index) throws MJSONException
	{
		switch(doc[index[0]])
		{
		case '\"':
			return parseString(doc,index);
		case '[':
			return parseArray(doc,index);
		case '{':
			return parseObject(doc,index);
		case '-': case '0': case '1': case '2': case '3': case '4':
		case '5': case '6': case '7': case '8': case '9':
			return parseNumber(doc,index);
		case 't':
			if((index[0] < doc.length-5) && (new String(doc,index[0],4).equals("true")))
			{
				index[0]+=3;
				return Boolean.TRUE;
			}
			throw new MJSONException("Invalid true at "+index[0]);
		case 'f':
			if((index[0] < doc.length-6) && (new String(doc,index[0],5).equals("false")))
			{
				index[0]+=4;
				return Boolean.FALSE;
			}
			throw new MJSONException("Invalid false at "+index[0]);
		case 'n':
			if((index[0] < doc.length-5) && (new String(doc,index[0],4).equals("null")))
			{
				index[0]+=3;
				return NULL;
			}
			throw new MJSONException("Invalid null at "+index[0]);
		default:
			throw new MJSONException("Unknown character at "+index[0]);
		}
	}

	private JSONObject parseObject(char[] doc, int[] index) throws MJSONException
	{
		JSONObject map = new JSONObject();
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
					if(c=='{')
						state = ObjectParseState.NEEDKEY;
					else
						throw new MJSONException("Expected Key/String at "+index[0]);
					break;
				case NEEDKEY:
					if(c=='\"')
					{
						key = parseString(doc,index);
						state = ObjectParseState.GOTKEY;
					}
					else
					if(c=='}')
						return map;
					else
						throw new MJSONException("Expected Key/String at "+index[0]);
					break;
				case GOTKEY:
					if(c==':')
						state = ObjectParseState.NEEDOBJECT;
					else
						throw new MJSONException("Expected Colon at "+index[0]);
					break;
				case NEEDOBJECT:
					map.put(key, parseElement(doc,index));
					state = ObjectParseState.GOTOBJECT;
					break;
				case GOTOBJECT:
					if(c==',')
						state = ObjectParseState.NEEDKEY;
					else
					if(c=='}')
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

	public JSONObject parseObject(String doc) throws MJSONException
	{
		try
		{
			return parseObject(doc.toCharArray(), new int[]{0});
		}
		catch (MJSONException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new MJSONException("Internal error",e);
		}
	}
}
