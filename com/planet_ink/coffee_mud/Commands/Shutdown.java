package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
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
import java.util.concurrent.TimeUnit;

/* 
   Copyright 2000-2013 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
public class Shutdown extends StdCommand implements Tickable
{
	public Shutdown(){}

	private final String[] access={"SHUTDOWN"};
	public String[] getAccessWords(){return access;}
	protected MOB shuttingDownMob=null;
	protected long shuttingDownNextAnnounce=0;
	protected long shuttingDownCompletes=0;
	protected boolean keepItDown=true;
	protected String externalCommand=null;

	protected void showDisplayableShutdownTimeRemaining()
	{
		long until = shuttingDownCompletes - System.currentTimeMillis();
		String tm = CMLib.time().date2EllapsedTime(until, TimeUnit.SECONDS, false);
		if((tm == null)||(tm.trim().length()==0))
			tm = " now";
		else
			tm=" in "+tm;
		for(Session S : CMLib.sessions().allIterable())
		  S.colorOnlyPrintln("\n\r\n\r^Z"+CMProps.getVar(CMProps.SYSTEM_MUDNAME)+" will be "+(keepItDown?"shutting down":"restarting")+tm+"^.^?\n\r");
	}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(mob.isMonster()) return false;
		boolean noPrompt=false;
		String externalCommand=null;
		boolean keepItDown=true;
		for(int i=commands.size()-1;i>=1;i--)
		{
			String s=(String)commands.elementAt(i);
			if(s.equalsIgnoreCase("RESTART"))
			{ keepItDown=false; commands.removeElementAt(i);}
			else
			if(s.equalsIgnoreCase("NOPROMPT"))
			{ noPrompt=true; commands.removeElementAt(i); }
			else
			if(s.equalsIgnoreCase("CANCEL"))
			{
				if(shuttingDownMob==null)
				{
					mob.tell("Either no shutdown has been scheduled or is already underway and can't be cancelled.");
					return false;
				}
				shuttingDownMob=null;
				CMLib.threads().deleteTick(this, Tickable.TICKID_AREA);
			}
			else
			if((s.equalsIgnoreCase("IN"))&&(i==commands.size()-3))
			{ 
				noPrompt=true; 
				commands.removeElementAt(i);
				long wait=CMath.s_int((String)commands.get(i));
				commands.removeElementAt(i);
				String multiplier=(String)commands.get(i);
				commands.removeElementAt(i);
				long timeMultiplier=CMLib.english().getMillisMultiplierByName(multiplier);
				if((timeMultiplier<0)||(wait<=0))
				{
				   mob.tell("I don't know how to shutdown within the next "+wait+" "+multiplier+"; try `5 minutes` or something similar.");
				   return false;
				}
				if((!mob.session().confirm("Shutdown "+CMProps.getVar(CMProps.SYSTEM_MUDNAME)+" in "+wait+" "+multiplier.toLowerCase()+" (y/N)?","N")))
				   return false;
				shuttingDownCompletes=System.currentTimeMillis()+(wait * timeMultiplier)-1;
				shuttingDownNextAnnounce=System.currentTimeMillis() + ((wait * timeMultiplier)/2)-100;
				shuttingDownMob=mob;
				CMLib.threads().startTickDown(this, Tickable.TICKID_AREA, 1);
				showDisplayableShutdownTimeRemaining();
				return true;
			}
		}
		if((!keepItDown)&&(commands.size()>1))
			externalCommand=CMParms.combine(commands,1);

		if((!noPrompt)
		&&(!mob.session().confirm("Shutdown "+CMProps.getVar(CMProps.SYSTEM_MUDNAME)+" (y/N)?","N")))
			return false;
		shuttingDownMob=null;
		this.externalCommand=externalCommand;
		this.keepItDown=keepItDown;
		
		startShutdown(mob);
		return false;
	}
	
	public boolean canBeOrdered(){return false;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.SHUTDOWN);}
	
	public void startShutdown(final MOB mob)
	{
		new Thread(){
			public void run()
			{
				for(Session S : CMLib.sessions().allIterable())
					S.colorOnlyPrintln("\n\r\n\r^Z"+CMProps.getVar(CMProps.SYSTEM_MUDNAME)+" is now "+(keepItDown?"shutting down":"restarting")+"!^.^?\n\r");
				if(keepItDown)
					Log.errOut("CommandProcessor",mob.Name()+" starts system shutdown...");
				else
				if(externalCommand!=null)
					Log.errOut("CommandProcessor",mob.Name()+" starts system restarting '"+externalCommand+"'...");
				else
					Log.errOut("CommandProcessor",mob.Name()+" starts system restart...");
				mob.tell("Starting "+(keepItDown?"shutdown":"restart")+"...");
				com.planet_ink.coffee_mud.application.MUD.globalShutdown(mob.session(),keepItDown,externalCommand);
			}
		}.start();
	}
	
	public long getTickStatus() { return Tickable.STATUS_ALIVE;}
	public String name() { return super.ID(); }
	public boolean tick(Tickable ticking, int tickID) 
	{
		final MOB mob=shuttingDownMob;
		if(mob==null) return false;
		if(System.currentTimeMillis() > shuttingDownCompletes)
		{
			startShutdown(mob);
			return false;
		}
		else
		if(System.currentTimeMillis() >= shuttingDownNextAnnounce)
		{
			shuttingDownNextAnnounce = System.currentTimeMillis() + ((shuttingDownCompletes - System.currentTimeMillis())/2)-100;
			showDisplayableShutdownTimeRemaining();
		}
		return true;
	}
}
