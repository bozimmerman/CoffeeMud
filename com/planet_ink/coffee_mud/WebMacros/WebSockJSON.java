package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.http.HTTPException;
import com.planet_ink.coffee_web.http.HTTPHeader;
import com.planet_ink.coffee_web.http.HTTPStatus;
import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_web.util.CWDataBuffers;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CoffeeIOPipe.CoffeeIOPipes;
import com.planet_ink.coffee_mud.core.CoffeeIOPipe.CoffeePipeSocket;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.*;
import java.util.*;

/*
   Copyright 2025-2025 Bo Zimmerman

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
public class WebSockJSON extends WebSock
{
	@Override
	public String ID()
	{
		return "WebSockJSON";
	}

	@Override
	public String name()
	{
		return "WebSockJSON";
	}

	public WebSockJSON()
	{
		super();
	}

	public WebSockJSON(final HTTPRequest httpReq) throws IOException
	{
		super(httpReq);
	}

	private enum WJSONType
	{
		INPUT,
		TEXT
	}

	@Override
	protected Pair<byte[],WSPType> processPolledBytes(final byte[] data)
	{
		final MiniJSON.JSONObject json = new MiniJSON.JSONObject();
		json.putString("type", WJSONType.TEXT.name().toLowerCase());
		json.put("text", data);
		return new Pair<byte[],WSPType>(json.toString().getBytes(), WSPType.TEXT);
	}

	@Override
	protected void processTextInput(final String input) throws IOException
	{
		if(mudOut == null)
			return;
		try
		{
			final MiniJSON.JSONObject obj = new MiniJSON().parseObject(input);
			if(!obj.containsKey("type"))
				return;
			final WJSONType type = (WJSONType)CMath.s_valueOf(WJSONType.class,obj.getCheckedString("type").toUpperCase());
			if((type == WJSONType.INPUT)
			&& (obj.containsKey("data")))
			{
				final Object o = obj.get("data");
				if (o instanceof String)
				{
					if (mudOut != null)
						mudOut.write(((String)o).getBytes(CMProps.getVar(CMProps.Str.CHARSETINPUT)));
				}
				else
				if (o instanceof Object[])
				{
					final Object[] inputs = (Object[])o;
					final List<String> inputV = new ArrayList<String>();
					for(final Object o2 : inputs)
					{
						if(o2 instanceof String)
							inputV.add((String)o2);
					}
					final String finalInput = CMParms.combineQuoted(inputV,  0).trim() + "\n";
					if (mudOut != null)
						mudOut.write(finalInput.getBytes(CMProps.getVar(CMProps.Str.CHARSETINPUT)));
				}
			}
		}
		catch(final MiniJSON.MJSONException x)
		{
			// ignore
		}
	}

	@Override
	protected void processBinaryInput(final byte[] input) throws IOException
	{
		// ignore
	}

}
