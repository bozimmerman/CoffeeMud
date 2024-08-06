package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine.MPContext;
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.CoffeeTime.TimeDelta;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AutoAwardsLibrary.AutoProperties;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMaskEntry;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2024-2024 Bo Zimmerman

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
public class ScriptLater extends StdAbility
{
	@Override
	public String ID()
	{
		return "ScriptLater";
	}

	@Override
	public String displayText()
	{
		return "";
	}

	private final static String localizedName = CMLib.lang().L("ScriptLater");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PROPERTY;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	protected boolean initialized = false;
	protected TimeClock homeClock = null;
	protected PairList<Object, String> schedule = new PairVector<Object,String>();

	@Override
	public String text()
	{
		if(!initialized)
			return super.miscText;
		final StringBuilder xml = new StringBuilder("<SCRIPTS>");
		final String invokerName = (invoker()==null)?"":invoker().Name();
		for(final Pair<Object,String> p : schedule)
		{
			xml.append("<SCRIPT INVOKER=\""+invokerName+"\" ");
			if(p.first instanceof TimeClock)
				xml.append("CLOCK=\""+((TimeClock)p.first).toTimePeriodCodeString()+"\">");
			else
			if(p.first instanceof int[])
				xml.append("TICKS=\""+((int[])p.first)[0]+"\">");
			else
			if(p.first instanceof Long)
				xml.append("TIME=\""+((Long)p.first).toString()+"\">");
			xml.append(CMLib.xml().parseOutAngleBrackets(p.second));
			xml.append("</SCRIPT>");
		}
		return xml.toString()+"</SCRIPTS>";
	}

	protected boolean parseXML(final String xmlStr)
	{
		this.schedule.clear();
		if(xmlStr.trim().startsWith("<SCRIPTS>"))
		{
			final List<XMLTag> xml = CMLib.xml().parseAllXML(xmlStr);
			if(xml.size()==1)
			{
				for(final XMLTag tag : xml.get(0).contents())
				{
					final String clockStr = tag.getParmValue("CLOCK");
					final String ticksStr = tag.getParmValue("TICKS");
					final String timeStr = tag.getParmValue("TIME");
					final String invokerName = tag.getParmValue("INVOKER");
					final String script = CMLib.xml().restoreAngleBrackets(tag.value());
					if((((clockStr!=null)&&(clockStr.length()>0))
						||((ticksStr!=null)&&(ticksStr.length()>0))
						||((timeStr!=null)&&(timeStr.length()>0)))
					&&(script.trim().length()>0))
					{
						final Object O;
						if(((clockStr!=null)&&(clockStr.length()>0)))
							O = CMLib.time().homeClock(affected).fromTimePeriodCodeString(clockStr).copyOf();
						else
						if((timeStr!=null)&&(timeStr.length()>0))
							O = Long.valueOf(CMath.s_long(timeStr));
						else
							O = new int[] { CMath.s_int(ticksStr) };
						if((invokerName!=null)&&(invokerName.trim().length()>0))
						{
							final MOB M = CMLib.players().getPlayer(invokerName);
							if(M != null)
								setInvoker(M);
						}
						this.schedule.add(O, script);
					}
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
		initialized = false;
		homeClock=null;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(super.tick(ticking, tickID))
		{
			if(!initialized)
				initialized = this.parseXML(super.miscText);
			if(initialized) // did the load work?
			{
				synchronized(schedule)
				{
					for(int i=schedule.size()-1;i>=0;i--)
					{
						final Pair<Object, String> event = schedule.get(i);
						if(homeClock == null)
							homeClock = CMLib.time().homeClock(affected);
						if(((event.first instanceof TimeClock)&&(homeClock.isAfter((TimeClock)event.first)))
						||((event.first instanceof Long)&&(System.currentTimeMillis()>((Long)event.first).longValue()))
						||((event.first instanceof int[])&&(--((int[])event.first)[0]<=0)))
						{
							final ScriptingEngine predictE = (ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
							predictE.setScript(event.second);
							schedule.remove(i);
							if(affected instanceof MOB)
								((MOB)affected).addScript(predictE);
						}
					}
				}
			}
			if(schedule.size()==0)
			{
				this.canBeUninvoked=true;
				affected.delEffect(this);
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=super.getTarget(mob, commands, givenTarget, true, false);
		if(target==null)
			return false;

		if(commands.size()>2)
		{
			final String script = commands.get(2);
			final String tm = commands.get(1);
			Object C;
			if(CMath.isNumber(tm))
			{
				final long num = CMath.s_long(tm);
				if(num > System.currentTimeMillis()-30000L)
					C=Long.valueOf(num);
				else
					C=new int[] { (int)num };
			}
			else
				C = CMLib.time().homeClock(target).fromTimePeriodCodeString(tm).copyOf();
			ScriptLater slA = (ScriptLater)target.fetchEffect(ID());
			if(slA != null)
			{
				if(!slA.initialized)
				{
					slA.tick(target, Tickable.TICKID_MOB); // ensure initialized
					slA = (ScriptLater)givenTarget.fetchEffect(ID());
				}
			}
			if(slA == null)
			{
				slA = new ScriptLater();
				slA.setSavable(true);
				slA.setInvoker(mob);
				target.addNonUninvokableEffect(slA);
				slA.initialized=true;
			}
			else
			{
				synchronized(slA.schedule)
				{
					for(final Pair<Object,String> p : slA.schedule)
					{
						if(p.second.equalsIgnoreCase(script))
							return false;
					}
				}
			}
			synchronized(slA.schedule)
			{
				slA.schedule.add(C,script);
			}
			return true;
		}
		else
		{
			mob.tell(L("Args: mob, timeclock, script"));
		}
		return false;
	}
}
