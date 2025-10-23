package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMProps.Bool;
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
import java.util.concurrent.TimeUnit;

/*
   Copyright 2004-2025 Bo Zimmerman

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
public class Shutdown extends StdCommand implements Tickable
{
	public Shutdown()
	{
	}

	private final String[]	access	= I(new String[] { "SHUTDOWN" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	protected MOB		shuttingDownMob				= null;
	protected long		shuttingDownNextAnnounce	= 0;
	protected long		shuttingDownCompletes		= 0;
	protected String	reason						= "";

	protected String getDisplayableShutdownTimeRemaining()
	{
		final long until = shuttingDownCompletes - System.currentTimeMillis();
		String tm = CMLib.time().date2EllapsedTime(until, TimeUnit.SECONDS, false);
		if((tm == null)||(tm.trim().length()==0))
			tm = " now";
		else
			tm=" in "+tm;
		return L("\n\r\n\r^Z@x1 will be @x2@x3@x4^.^?\n\r",CMProps.getVar(CMProps.Str.MUD_NAME),"shutting down",tm,reason);
	}

	protected void showDisplayableShutdownTimeRemaining()
	{
		final String str = getDisplayableShutdownTimeRemaining();
		for(final Session S : CMLib.sessions().allIterable())
		  S.colorOnlyPrintln(str);
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		if(mob.isMonster())
			return false;
		if(CMProps.isState(CMProps.HostState.SHUTTINGDOWN))
		{
			mob.tell(L("A shutdown is already in progress."));
			return false;
		}
		boolean noPrompt=false;
		String reason=null;
		boolean startCountDown=false;
		MOB newShutdownMob=this.shuttingDownMob;
		for(int i=commands.size()-1;i>=1;i--)
		{
			final String s=commands.get(i);
			if(s.equalsIgnoreCase("CHECK"))
			{

			}
			else
			if(s.equalsIgnoreCase("FOR"))
			{
				reason=" "+CMParms.combine(commands,i);
				while(commands.size()>i)
					commands.remove(i);
			}
			else
			if(s.equalsIgnoreCase("NOPROMPT"))
			{
				noPrompt = true;
				commands.remove(i);
			}
			else
			if(s.equalsIgnoreCase("CANCEL")||s.equalsIgnoreCase("STOP"))
			{
				if(shuttingDownMob==null)
				{
					mob.tell(L("Either no shutdown has been scheduled or is already underway and can't be cancelled."));
					return false;
				}
				shuttingDownMob=null;
				if(CMLib.threads().deleteTick(this, Tickable.TICKID_AREA))
					mob.tell(L("The shutdown has been cancelled."));
				else
					mob.tell(L("Either no shutdown has been scheduled or is already underway and can't be cancelled."));
				return false;
			}
			else
			if((s.equalsIgnoreCase("IN"))&&(i<=commands.size()-3))
			{
				noPrompt=true;
				commands.remove(i);
				final long wait=CMath.s_int(commands.get(i));
				commands.remove(i);
				final String multiplier=commands.get(i);
				commands.remove(i);
				final long timeMultiplier=CMLib.english().getMillisMultiplierByName(multiplier);
				if((timeMultiplier<0)||(wait<=0))
				{
					mob.tell(L("I don't know how to shutdown within the next @x1 @x2; try `5 minutes` or something similar.",""+wait,multiplier));
					return false;
				}
				if((!mob.session().confirm(L("Shutdown @x1 in @x2 @x3 (y/N)?",CMProps.getVar(CMProps.Str.MUD_NAME),""+wait,multiplier.toLowerCase()),"N")))
					return false;
				shuttingDownCompletes=System.currentTimeMillis()+(wait * timeMultiplier)-1;
				shuttingDownNextAnnounce=System.currentTimeMillis() + ((wait * timeMultiplier)/2)-100;
				newShutdownMob=mob;
				startCountDown=true;
			}
			else
			if((s.equalsIgnoreCase("AT"))&&(i<=commands.size()-2))
			{
				noPrompt=true;
				commands.remove(i);
				final String atWhat=commands.remove(i);
				final Calendar C=CMLib.time().string2TimeFuture(atWhat);
				if(C==null)
				{
					mob.session().println(L("'@x1' is not a valid time.",atWhat));
					return false;
				}
				if((!mob.session().confirm(L("Shutdown @x1 at @x2 (y/N)?",CMProps.getVar(CMProps.Str.MUD_NAME),CMLib.time().date2String(C)),"N")))
					return false;
				shuttingDownCompletes=C.getTimeInMillis();
				shuttingDownNextAnnounce=System.currentTimeMillis() + ((C.getTimeInMillis()-System.currentTimeMillis())/2)-100;
				newShutdownMob=mob;
				startCountDown=true;
			}
		}
		if(shuttingDownMob != null)
		{
			mob.tell(L("@x1 has already scheduled a shutdown, or one is imminent.  Use SHUTDOWN CANCEL first.",shuttingDownMob.name()));
			return false;
		}
		shuttingDownMob=newShutdownMob;
		if(!startCountDown)
		{
			if((!noPrompt)
			&&(!mob.session().confirm(L("Shutdown @x1 (y/N)?",CMProps.getVar(CMProps.Str.MUD_NAME)),"N")))
				return false;

			shuttingDownMob=null;
		}
		this.reason=(reason==null)?"":reason;

		if(startCountDown)
		{
			CMLib.threads().startTickDown(this, Tickable.TICKID_AREA, 1);
			showDisplayableShutdownTimeRemaining();
			return true;
		}
		else
			startShutdown(mob);
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}

	@Override
	public boolean securityCheck(final MOB mob)
	{
		return CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.SHUTDOWN);
	}

	public void startShutdown(final MOB mob)
	{
		new Thread(Thread.currentThread().getThreadGroup(),"Shutdown"+Thread.currentThread().getThreadGroup().getName().charAt(0))
		{
			@Override
			public void run()
			{
				for(final Session S : CMLib.sessions().allIterable())
					S.colorOnlyPrintln(L("\n\r\n\r^Z@x1 is now shutting down!^.^?\n\r",CMProps.getVar(CMProps.Str.MUD_NAME)));
				Log.errOut("CommandProcessor",mob.Name()+" starts system shutdown...");
				mob.tell(L("Starting @x1...shutdown",CMProps.getVar(CMProps.Str.MUD_NAME)));
				com.planet_ink.coffee_mud.application.MUD.globalShutdown(mob.session(),false,"");
			}
		}.start();
	}

	@Override
	public int getTickStatus()
	{
		return Tickable.STATUS_ALIVE;
	}

	@Override
	public String name()
	{
		return super.ID();
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		final MOB mob=shuttingDownMob;
		if(mob==null)
			return false;
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

	private final static Class<?>[][] internalParameters=new Class<?>[][]{{}, {Boolean.class}, {Long.class}};

	@Override
	public Object executeInternal(final MOB mob, final int metaFlags, final Object... args) throws java.io.IOException
	{
		if(!super.checkArguments(internalParameters, args))
			return "";
		if(args.length==1)
		{
			if(args[0] instanceof Boolean)
			{
				final Boolean upDn = (Boolean)args[0];
				if(CMProps.isState(CMProps.HostState.SHUTTINGDOWN))
					return Boolean.FALSE;
				if(CMLib.threads().isTicking(this, Tickable.TICKID_AREA))
					CMLib.threads().deleteTick(this, Tickable.TICKID_AREA);
				shuttingDownMob=null;
				shuttingDownCompletes=0;
				shuttingDownNextAnnounce=0;
				if(upDn.booleanValue())
					startShutdown(mob);
				return Boolean.TRUE;
			}
			else
			if(args[0] instanceof Long)
			{
				final Long millis = (Long)args[0];
				shuttingDownMob = mob;
				shuttingDownCompletes = millis.longValue();
				shuttingDownNextAnnounce = System.currentTimeMillis() + ((shuttingDownCompletes - System.currentTimeMillis())/2)-100;
				if(!CMLib.threads().isTicking(this, Tickable.TICKID_AREA))
					CMLib.threads().startTickDown(this, Tickable.TICKID_AREA, 1);
				showDisplayableShutdownTimeRemaining();
				return Boolean.TRUE;
			}

		}
		if((shuttingDownMob == null)
		||(shuttingDownCompletes == 0)
		||(shuttingDownNextAnnounce == 0))
			return "";
		return this.getDisplayableShutdownTimeRemaining();
	}

}
