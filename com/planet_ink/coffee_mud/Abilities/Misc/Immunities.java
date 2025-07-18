package com.planet_ink.coffee_mud.Abilities.Misc;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

/*
   Copyright 2003-2025 Bo Zimmerman

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
public class Immunities extends StdAbility
{
	@Override
	public String ID()
	{
		return "Immunities";
	}

	private final static String	localizedName	= CMLib.lang().L("Immunities");

	@Override
	public String name()
	{
		return localizedName;
	}

	protected String	displayText	= "";

	@Override
	public String displayText()
	{
		return displayText;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS | CAN_ROOMS | CAN_AREAS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return canBeUninvoked;
	}

	private boolean				  canBeUninvoked	= false;
	private final HashSet<Integer>immunes			= new HashSet<Integer>();
	private final List<String>	  effects			= new Vector<String>();
	private final List<String>	  behaviors			= new Vector<String>();
	private volatile int		  bct				= 0;
	private volatile int		  fct				= 0;

	public static Map<String,Integer> immunityTypes=new SHashtable<String,Integer>(new Object[][]
	{
		{"ACID",Integer.valueOf(CMMsg.TYP_ACID)},
		{"WATER",Integer.valueOf(CMMsg.TYP_WATER)},
		{"COLD",Integer.valueOf(CMMsg.TYP_COLD)},
		{"DISEASE",Integer.valueOf(CMMsg.TYP_DISEASE)},
		{"ELECTRIC",Integer.valueOf(CMMsg.TYP_ELECTRIC)},
		{"FIRE",Integer.valueOf(CMMsg.TYP_FIRE)},
		{"GAS",Integer.valueOf(CMMsg.TYP_GAS)},
		{"JUSTICE",Integer.valueOf(CMMsg.TYP_JUSTICE)},
		{"MIND",Integer.valueOf(CMMsg.TYP_MIND)},
		{"PARALYZE",Integer.valueOf(CMMsg.TYP_PARALYZE)},
		{"POISON",Integer.valueOf(CMMsg.TYP_POISON)},
		{"UNDEAD",Integer.valueOf(CMMsg.TYP_UNDEAD)},
		{"LEGAL",Integer.valueOf(CMMsg.TYP_LEGALWARRANT)},
		{"LASER",Integer.valueOf(CMMsg.TYP_LASER)},
		{"SONIC",Integer.valueOf(CMMsg.TYP_SONIC)},
	});

	@Override
	public void setMiscText(final String text)
	{
		super.setMiscText(text);
		immunes.clear();
		effects.clear();
		behaviors.clear();
		final Vector<String> immunities=CMParms.parse(text.toUpperCase());
		for(final String v : immunities)
		{
			if(v.equalsIgnoreCase("ALL"))
			{
				for(final String key : immunityTypes.keySet())
					immunes.add(immunityTypes.get(key));
			}
			else
			if(immunityTypes.containsKey(v))
				immunes.add(immunityTypes.get(v));
			else
			{
				final Behavior B = CMClass.getBehavior(v);
				if(B != null)
					behaviors.add(B.ID());
				else
				{
					final Ability A = CMClass.getAbility(v);
					if(A != null)
						effects.add(A.ID());
					else
						Log.errOut("Immunities", "Illegal parm "+v+" in "+text);
				}
			}
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		final Physical P = affected;
		if(P instanceof Behavable)
		{
			if(bct != ((Behavable)P).numBehaviors())
			{
				bct = ((Behavable)P).numBehaviors();
				for(int s=behaviors.size()-1;s>=0;s--)
				{
					final String str=behaviors.get(s);
					final Behavior B = ((Behavable)P).fetchBehavior(str);
					if(B != null)
						((MOB)P).delBehavior(B);
				}
			}
		}
		if(fct != P.numEffects())
		{
			fct = P.numEffects();
			for(int s=effects.size()-1;s>=0;s--)
			{
				final String str=effects.get(s);
				final Ability A = P.fetchEffect(str);
				if(A != null)
				{
					A.unInvoke();
					P.delEffect(A);
				}
			}
		}
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(((!(affected instanceof MOB))||(msg.amITarget(affected)&&(!((MOB)affected).amDead())))
		&&(immunes.contains(Integer.valueOf(msg.targetMinor()))
			|| immunes.contains(Integer.valueOf(msg.sourceMinor())))
		&&(CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS)||(msg.targetMinor()==CMMsg.TYP_DAMAGE)||(msg.targetMinor()==CMMsg.TYP_LEGALWARRANT)))
		{
			if((msg.tool()==msg.source())&&(msg.sourceMinor()==CMMsg.TYP_GAS))
				return false;
			if(!msg.sourceMajor(CMMsg.MASK_CNTRLMSG) && !msg.targetMajor(CMMsg.MASK_CNTRLMSG))
			{
				String immunityName="certain";
				if(msg.tool() instanceof MOB)
					Log.debugOut("Got a weird Immunity: "+msg.toFlatString());
				if(msg.tool()!=null)
					immunityName=msg.tool().name();
				final Room R=CMLib.map().roomLocation(msg.target());
				if(msg.target()!=msg.source())
					R.show(msg.source(),msg.target(),CMMsg.MSG_OK_VISUAL,L("<T-NAME> seem(s) immune to @x1 attacks from <S-NAME>.",immunityName));
				else
					R.show(msg.source(),msg.target(),CMMsg.MSG_OK_VISUAL,L("<T-NAME> seem(s) immune to @x1.",immunityName));
			}
			return false;
		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical target, final boolean auto, final int asLevel)
	{
		final StringBuilder immunes=new StringBuilder("");
		int ticksOverride=0;
		if(commands.size()>0)
		{
			for(final Object o : commands)
			{
				final String s=o.toString().toUpperCase();
				if(s.startsWith("TICKS=")&&(CMath.isInteger(s.substring(6).trim())))
					ticksOverride=CMath.s_int(s.substring(6).trim());
				else
				if(CMath.isInteger(s.trim()))
					ticksOverride=CMath.s_int(s.trim());
				else
				if(immunityTypes.containsKey(s))
					immunes.append(s).append(" ");
			}
		}
		if(!super.invoke(mob, commands, target, auto, asLevel))
			return false;
		if(immunes.length()>0)
		{
			if(beneficialAffect(mob, mob, asLevel, ticksOverride)==null)
				return false;
			final Immunities A=(Immunities)mob.fetchEffect(ID());
			if(A==null)
				return false;
			A.setMiscText(immunes.toString().trim());
			A.canBeUninvoked=true;
		}
		return true;
	}
}

