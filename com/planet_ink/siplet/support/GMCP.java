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
public class GMCP
{
	public GMCP()
	{
		super();
	}

	public int process(StringBuffer buf, int i, Siplet applet, boolean useExternal)
	{
		return 0;
	}

	public String gmcpReceive(byte[] buffer)
	{
		return new String(buffer, Charset.forName("US-ASCII"));
	}

	public byte[] convertStringToGmcp(String data) throws MJSONException
	{
		try
		{
			data = data.trim();
			String cmd;
			String parms;
			final int x = data.indexOf(' ');
			if (x < 0)
			{
				cmd = data;
				parms = "";
			}
			else
			{
				cmd = data.substring(0, x).trim();
				parms = data.substring(x + 1).trim();
			}
			if (cmd.length() == 0)
				return new byte[0];
			if (parms.length() > 0)
			{
				final MiniJSON jsonParser = new MiniJSON();
				jsonParser.parseObject("{\"root\":" + parms + "}");
				// simple parse test.. should throw an exception
			}
			else
				parms = "{}";
			final ByteArrayOutputStream bout = new ByteArrayOutputStream();
			bout.write(TelnetFilter.IAC_);
			bout.write(TelnetFilter.IAC_SB);
			bout.write(TelnetFilter.IAC_GMCP);
			bout.write((cmd + " " + parms).getBytes("US-ASCII"));
			bout.write(TelnetFilter.IAC_);
			bout.write(TelnetFilter.IAC_SE);
			return bout.toByteArray();
		}
		catch (final Exception e)
		{
			return new byte[0];
		}
	}
}
