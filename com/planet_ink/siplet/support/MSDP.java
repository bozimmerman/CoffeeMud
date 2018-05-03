package com.planet_ink.siplet.support;

import java.applet.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;

import com.planet_ink.coffee_mud.Common.interfaces.Session;
import com.planet_ink.siplet.applet.Siplet;
import com.planet_ink.siplet.support.MiniJSON.JSONObject;
import com.planet_ink.siplet.support.MiniJSON.MJSONException;

/*
   Copyright 2000-2018 Bo Zimmerman

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
@SuppressWarnings({ "unchecked", "rawtypes" })
public class MSDP
{

	public MSDP()
	{
		super();
	}

	public int process(StringBuffer buf, int i, Siplet applet, boolean useExternal)
	{
		return 0;
	}

	protected Object msdpStringify(Object o)
	{
		if (o instanceof StringBuilder)
			return ((StringBuilder) o).toString();
		else 
		if (o instanceof Map)
		{
			final Map<String, Object> newO = new HashMap<String, Object>();
			for (final Object key : ((Map) o).keySet())
			{
				if (key instanceof StringBuilder)
					newO.put(((StringBuilder) key).toString().toUpperCase(), msdpStringify(((Map) o).get(key)));
			}
			return newO;
		}
		else 
		if (o instanceof List)
		{
			final List<Object> newO = new LinkedList<Object>();
			for (final Object subO : (List) o)
				newO.add(msdpStringify(subO));
			return newO;
		}
		else
			return o;
	}

	protected Map<String, Object> buildMsdpMap(byte[] data, int dataSize)
	{
		final Stack<Object> stack = new Stack<Object>();
		stack.push(new HashMap<StringBuilder, Object>());
		StringBuilder str = null;
		StringBuilder var = null;
		StringBuilder valVar = null;
		int x = -1;
		while (++x < dataSize)
		{
			switch (data[x])
			{
			case Session.MSDP_VAR: // start a string
				str = new StringBuilder("");
				var = str;
				if (stack.peek() instanceof Map)
					((Map) stack.peek()).put(str, "");
				else 
				if (stack.peek() instanceof List)
					((List) stack.peek()).add(str);
				break;
			case Session.MSDP_VAL:
			{
				valVar = var;
				var = null;
				str = new StringBuilder("");
				break;
			}
			case Session.MSDP_TABLE_OPEN: // open a table
			{
				final Map<StringBuilder, Object> M = new HashMap<StringBuilder, Object>();
				if ((stack.peek() instanceof Map) && (valVar != null))
					((Map) stack.peek()).put(valVar, M);
				else 
				if (stack.peek() instanceof List)
					((List) stack.peek()).add(M);
				valVar = null;
				stack.push(M);
				break;
			}
			case Session.MSDP_TABLE_CLOSE: // done with table
				if ((stack.size() > 1) && (stack.peek() instanceof Map))
					stack.pop();
				break;
			case Session.MSDP_ARRAY_OPEN: // open an array
			{
				final List<Object> M = new LinkedList<Object>();
				if ((stack.peek() instanceof Map) && (valVar != null))
					((Map) stack.peek()).put(valVar, M);
				else 
				if (stack.peek() instanceof List)
					((List) stack.peek()).add(M);
				valVar = null;
				stack.push(M);
				break;
			}
			case Session.MSDP_ARRAY_CLOSE: // close an array
				if ((stack.size() > 1) && (stack.peek() instanceof List))
					stack.pop();
				break;
			default:
				if ((stack.peek() instanceof Map) && (valVar != null))
					((Map) stack.peek()).put(valVar, str);
				else 
				if ((stack.peek() instanceof List) && (!((List) stack.peek()).contains(str)))
					((List) stack.peek()).add(str);
				valVar = null;
				if (str != null)
					str.append((char) data[x]);
				break;
			}
		}
		return (Map<String, Object>) msdpStringify(stack.firstElement());
	}

	public String msdpOutput(Object o, int indentions)
	{
		final String spaces = new String(new char[indentions * 2]).replace('\0', ' ');
		if (o instanceof String)
			return "\"" + o.toString() + "\"";
		else 
		if (o instanceof List)
		{
			final StringBuilder json = new StringBuilder("[\n");
			for (final Object o2 : ((List) o))
				json.append(msdpOutput(o2, indentions + 1)).append(", ");
			json.append("\n").append(spaces).append("]");
			return json.toString();
		}
		else 
		if (o instanceof Map)
		{
			final StringBuilder json = new StringBuilder("{\n");
			for (final Entry<String, Object> e : ((Map<String, Object>) o).entrySet())
			{
				json.append(spaces).append("\"").append(e.getKey()).append("\": ");
				json.append(msdpOutput(e.getValue(), indentions + 1)).append(", \n");
			}
			json.append("\n").append(spaces).append("}");
			return json.toString();
		}
		return "";
	}

	public String msdpReceive(byte[] buffer)
	{
		final Map<String, Object> map = buildMsdpMap(buffer, buffer.length);
		return msdpOutput(map, 0);
	}

	private byte[] getMsdpFromJsonObject(JSONObject obj) throws IOException
	{
		final ByteArrayOutputStream bout = new ByteArrayOutputStream();
		for (final String key : obj.keySet())
		{
			final Object o = obj.get(key);
			bout.write((byte) 1); // var
			bout.write(key.getBytes(Charset.forName("US-ASCII")));
			bout.write((byte) 2); // val
			bout.write(getMsdpFromJsonSomething(o));
		}
		return bout.toByteArray();
	}

	private byte[] getMsdpFromJsonArray(Object[] obj) throws IOException
	{
		final ByteArrayOutputStream bout = new ByteArrayOutputStream();
		for (final Object o : obj)
		{
			bout.write((byte) 2); // val
			bout.write(getMsdpFromJsonSomething(o));
		}
		return bout.toByteArray();
	}

	private byte[] getMsdpFromJsonSomething(Object obj) throws IOException
	{
		final ByteArrayOutputStream bout = new ByteArrayOutputStream();
		if (obj instanceof String)
			bout.write(((String) obj).getBytes(Charset.forName("US-ASCII")));
		else 
		if (obj instanceof JSONObject)
		{
			bout.write((byte) 3); // table open
			bout.write(getMsdpFromJsonObject((JSONObject) obj));
			bout.write((byte) 4); // table close
		}
		else 
		if (obj instanceof Object[])
		{
			bout.write((byte) 5); // array open
			bout.write(getMsdpFromJsonArray((Object[]) obj));
			bout.write((byte) 6); // array close
		}
		return bout.toByteArray();
	}

	public byte[] convertStringToMsdp(String data) throws MJSONException
	{
		try
		{
			final ByteArrayOutputStream bout = new ByteArrayOutputStream();
			bout.write(TelnetFilter.IAC_);
			bout.write(TelnetFilter.IAC_SB);
			bout.write(TelnetFilter.IAC_MSDP);
			final MiniJSON jsonParser = new MiniJSON();
			final JSONObject obj = jsonParser.parseObject(data);
			bout.write(getMsdpFromJsonObject(obj));
			bout.write(TelnetFilter.IAC_);
			bout.write(TelnetFilter.IAC_SE);
			return bout.toByteArray();
		}
		catch (final IOException e)
		{
			return new byte[0];
		}
	}
}
