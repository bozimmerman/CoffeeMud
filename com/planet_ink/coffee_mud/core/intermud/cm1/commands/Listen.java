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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.io.*;
import java.util.concurrent.atomic.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
public class Listen extends CM1Command
{
	public String getCommandWord(){ return "LISTEN";}
	
	protected void ListenTo(Object o)
	{
		
	}
	
	// dont forget to do a security check on what
	// they want to listen to
	/*
	public boolean passesSecurityCheck(MOB user, PhysicalAgent target)
	{
		if(user==null) return false;
		if(target instanceof MOB)
		{
			if(CMLib.players().playerExists(target.Name()))
				return CMSecurity.isAllowed(user,user.location(),"CMDPLAYERS");
			return CMSecurity.isAllowed(user,user.location(),"CMDMOBS");
		}
		else
			return false;
	}
	 */
	public void run()
	{
		try
		{
			PhysicalAgent P=getTarget(parameters);
			if(P!=null)
			{
				req.setTarget(P);
				req.sendMsg("[OK]");
				return;
			}
			req.sendMsg("[FAIL]");
		}
		catch(Exception ioe)
		{
			Log.errOut(className,ioe);
			req.close();
		}
	}
	// depends on what you want to listen to
	public boolean passesSecurityCheck(MOB user, PhysicalAgent target){return true;}
	public String getHelp(MOB user)
	{
		return "USAGE: LISTEN CHANNEL <NAME>, "
				+"SOURCEOBJECT <TARGET>, SOURCECODE <CODE>, SOURCETEXT <TEXT>, "
				+"TARGETOBJECT <TARGET>, TARGETCODE <CODE>, TARGETTEXT <TEXT>, "
				+"TOOLOBJECT <TARGET>, TOOLCODE <CODE>, TOOLTEXT <TEXT>, "
				+"@ROOM <TARGET>, @TARGET, + <OTHERS>, - <OTHERS>";
	}
}
