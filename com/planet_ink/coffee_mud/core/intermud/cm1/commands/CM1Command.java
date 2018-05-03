package com.planet_ink.coffee_mud.core.intermud.cm1.commands;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.intermud.cm1.RequestHandler;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;
import java.lang.reflect.Constructor;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.io.*;
import java.util.concurrent.atomic.*;

/*
   Copyright 2010-2018 Bo Zimmerman

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
public abstract class CM1Command implements Runnable, Cloneable
{
	protected final String			className	= "CM1" + getClass().getName().substring(getClass().getName().lastIndexOf('.'));
	protected final String			parameters;
	protected final RequestHandler	req;

	public CM1Command()
	{
		super();
		req = null;
		parameters = "";
	}

	public CM1Command(final RequestHandler req, final String parameters)
	{
		super();
		this.parameters = parameters;
		this.req = req;
	}

	public static CM1Command newInstance(Class<? extends CM1Command> cls, RequestHandler req, String parms)
	{
		try
		{
			if (cls == null)
				return null;
			return cls.getConstructor(RequestHandler.class, String.class).newInstance(req, parms);
		}
		catch (final Exception e)
		{
			Log.errOut("CM1Command", e);
			return null;
		}
	}

	public PhysicalAgent getTarget(String parameters)
	{
		if (parameters.equalsIgnoreCase("USER"))
			return req.getUser();
		final int x = parameters.indexOf('@');
		String who = parameters;
		String where = "";
		PhysicalAgent P = req.getTarget();
		if (x > 0)
		{
			who = parameters.substring(0, x);
			where = parameters.substring(x + 1);
			Room R = CMLib.map().getRoom(where);
			if (R == null)
			{
				final Area A = CMLib.map().getArea(where);
				if (A != null)
					R = A.getRandomMetroRoom();
			}
			if (who.length() == 0)
				P = R;
		}
		else
		{
			final MOB M = CMLib.players().getLoadPlayer(who);
			if (M != null)
				return M;
		}
		final Room R = CMLib.map().roomLocation(P);
		if (R == null)
			CMLib.map().roomLocation(req.getTarget());
		if (R == null)
			return null;
		P = R.fetchFromRoomFavorMOBs(null, who);
		if ((P == null) && (req.getTarget() instanceof MOB))
			P = R.fetchFromRoomFavorMOBs(null, who);
		return P;
	}

	public abstract String getCommandWord();

	public abstract boolean passesSecurityCheck(MOB user, PhysicalAgent target);

	public abstract String getHelp(MOB user, PhysicalAgent target, String rest);
}
