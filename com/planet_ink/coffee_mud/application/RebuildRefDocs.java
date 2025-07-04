	package com.planet_ink.coffee_mud.application;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_web.interfaces.HTTPRequest;
import com.planet_ink.coffee_web.interfaces.HTTPResponse;
import com.planet_ink.coffee_web.server.WebServer;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMFile.CMVFSDir;
import com.planet_ink.coffee_mud.core.CMFile.CMVFSFile;
import com.planet_ink.coffee_mud.core.Log.Type;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.database.DBConnector;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.core.exceptions.HTTPServerException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.MemberRecord;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.JournalMetaData;
import com.planet_ink.coffee_mud.Libraries.interfaces.LanguageLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.ThinPlayer;
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.ThinnerPlayer;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.WebMacros.interfaces.WebMacro;
import com.planet_ink.coffee_web.util.CWThread;

import java.net.*;
import java.util.*;
import java.sql.*;
import java.io.*;

	/*
	Copyright 2000-2025 Bo Zimmerman

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
public class RebuildRefDocs
{
	public static void main(final String[] args)
	{
		MUD.skipMapLoads = true;
		new Thread(new ThreadGroup("0-MUD"),new Runnable() {
			@Override
			public void run()
			{
				MUD.main(args);
			}
		}).start();
		while((CMProps.instance('0')==null)
		||(!CMProps.instance('0')._isState(CMProps.HostState.RUNNING)))
		{
			try
			{
				Thread.sleep(1000);
			}
			catch (final InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		WebServer server = null;
		for(final WebServer W : MUD.webServers)
			if(CMath.s_bool(W.getConfig().getMiscProp("ADMIN")))
			{
				server=W;
				break;
			}
		if(server == null)
		{
			Log.errOut("Missing admin web server");
			MUD.globalShutdown(null, true, null);
		}
		else
		new CWThread(new ThreadGroup("0-MUD"),server.getConfig(), new Runnable() {
			@Override
			public void run()
			{
				Log.errOut("Running Rebuild Ref Docs");
				final HTTPResponse resp = new HTTPResponse()
				{
					@Override
					public void setStatusCode(final int httpStatusCode) { }
					@Override
					public void setHeader(final String name, final String value) { }
					@Override
					public void setCookie(final String name, final String value) { }
					@Override
					public void setMimeType(final String mimeType) { }
				};
				final Map<String, String>	params	= new HashMap<String, String>();
				final Map<String, Object>	objects	= new HashMap<String, Object>();
				final WebMacro W = CMClass.getWebMacro("RebuildReferenceDocs".toUpperCase());
				CMSecurity.setSysOp("-LEVEL +>=0");
				final MOB M = CMClass.getFactoryMOB();
				objects.put("AUTHENTICATED_USER", M);
				final HTTPRequest req = CMLib.webMacroFilter().createFakeRequest(params, objects);
				try
				{
					W.runMacro(req, "", resp);
				}
				catch (final HTTPServerException e)
				{
					e.printStackTrace();
				}

				Log.errOut("Shutting Down");
				MUD.globalShutdown(null, true, null);
			}
		},"RBRefDocs").start();
	}
}
