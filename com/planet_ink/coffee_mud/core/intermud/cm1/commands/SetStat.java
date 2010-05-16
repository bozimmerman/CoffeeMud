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
import com.planet_ink.coffee_mud.Libraries.interfaces.GenericBuilder;
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
public class SetStat extends GetStat
{
	public String getCommandWord(){ return "SETSTAT";}
	public SetStat(RequestHandler req, String parameters) {
		super(req, parameters);
	}
	
	public void run()
	{
		try
		{
			Physical P=null;
			if(req.getTarget() instanceof Physical)
				P=(Physical)req.getTarget();
			if(P==null)
			{
				req.sendMsg("[FAIL BAD TARGET]");
				return;
			}
			String rest = "";
			String type = parameters.toUpperCase().trim();
			int x=parameters.indexOf(' ');
			if(x>0)
			{
				type=parameters.substring(0,x).toUpperCase().trim();
				rest=parameters.substring(x+1).toUpperCase().trim();
			}
			Modifiable mod=getModifiable(type,P);
			if(mod==null)
			{
				req.sendMsg("[FAIL "+getHelp(req.getUser(), P, "")+"]");
				return;
			}
			String value="";
			String stat=rest;
			x=rest.indexOf(' ');
			if(x>0)
			{
				stat=rest.substring(0,x).toUpperCase().trim();
				value=rest.substring(x+1).toUpperCase().trim();
			}
			if((stat.length()==0)||(!isAStat(P,mod,stat)))
			{
				req.sendMsg("[FAIL USAGE: SETSTAT "+type+" "+CMParms.toStringList(getStatCodes(P,mod))+"]");
				return;
			}
			if(!UseGenBuilder(P,mod))
				mod.setStat(stat, value);
			else
			{
				String[] codes = this.getStatCodes(P, mod);
		        for(int i=0;i<codes.length;i++)
		            if(codes[i].equalsIgnoreCase(stat))
		    			if(P instanceof MOB)
			            	CMLib.coffeeMaker().setGenMobStat((MOB)P, stat, value);
		    			else
		    			if(P instanceof Item)
			            	CMLib.coffeeMaker().setGenItemStat((Item)P, stat, value);
		        		
			}
			req.sendMsg("[OK]");
		}
		catch(Exception ioe)
		{
			Log.errOut(className,ioe);
			req.close();
		}
	}
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
	public String getHelp(MOB user, Physical target, String rest)
	{
		return "USAGE: SETSTAT "+CMParms.toStringList(getApplicableStatCodes(target));
	}
}
