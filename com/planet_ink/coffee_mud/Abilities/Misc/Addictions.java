package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DisFlag;
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

/*
   Copyright 2005-2023 Bo Zimmerman

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
public class Addictions extends StdAbility
{
	@Override
	public String ID()
	{
		return "Addictions";
	}

	private final Map<String,Item>	puffCredit	= new Hashtable<String,Item>();
	private final static long	CRAVE_TIME		= TimeManager.MILI_HOUR;
	private final static long	WITHDRAW_TIME	= TimeManager.MILI_DAY;
	private Map<String, long[]>	lastFix			= new Hashtable<String, long[]>();

	private final static String	localizedName	= CMLib.lang().L("Addictions");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		final StringBuilder str = new StringBuilder("");
		final long now=System.currentTimeMillis();
		for(final String addictedStr : lastFix.keySet())
		{
			final long lf = lastFix.get(addictedStr)[0];
			final long delta = now-lf;
			if(delta>CRAVE_TIME)
			{
				if(str.length()>0)
					str.append(",");
				str.append(CMStrings.capitalizeAndLower(addictedStr));
			}
		}
		if(str.length()==0)
			return "";
		return L("(Addiction to @x1)", str.toString());
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PROPERTY;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}


	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
		lastFix = new Hashtable<String, long[]>();
		for(final String str : CMParms.parseSemicolons(newMiscText.toLowerCase(),true))
			lastFix.put(str, new long[] { 0 });
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		final long now=System.currentTimeMillis();
		for(final String addictedStr : lastFix.keySet())
		{
			final long lf = lastFix.get(addictedStr)[0];
			final long delta = now-lf;
			if(delta>CRAVE_TIME)
			{
				if((CMLib.dice().rollPercentage()<=(delta/TimeManager.MILI_HOUR))
				&&(ticking instanceof MOB))
				{
					final MOB mob=(MOB)ticking;
					final String name;
					if(addictedStr.startsWith("effect:"))
					{
						final Ability A=CMClass.getAbilityPrototype(addictedStr.substring(7));
						if(A!=null)
							name = A.name().toLowerCase();
						else
							name = addictedStr;
					}
					else
						name = addictedStr;
					if(delta>WITHDRAW_TIME)
					{
						mob.tell(L("You've managed to kick your addiction to @x1.",name));
						//TODO: incorrect_>
						canBeUninvoked=true;
						unInvoke();
						mob.delEffect(this);
						return false;
					}
					final Item puffCreditI = puffCredit.get(addictedStr);
					if((puffCreditI!=null)
					&&(puffCreditI.amDestroyed()
						||puffCreditI.amWearingAt(Wearable.IN_INVENTORY)
						||puffCreditI.owner()!=(MOB)affected))
							this.puffCredit.remove(addictedStr);
					if((!CMSecurity.isDisabled(DisFlag.FATIGUE))
					&&(!mob.charStats().getMyRace().infatigueable()))
						mob.curState().adjFatigue(CMProps.getTickMillis(), mob.maxState());
					switch(CMLib.dice().roll(1,7,0))
					{
					case 1:
						mob.tell(L("Man, you could sure use some @x1.", name));
						break;
					case 2:
						mob.tell(L("Wouldn't some @x1 be great right about now?", name));
						break;
					case 3:
						mob.tell(L("You are seriously craving @x1.", name));
						break;
					case 4:
						mob.tell(L("There's got to be some @x1 around here somewhere.", name));
						break;
					case 5:
						mob.tell(L("You REALLY want some @x1.", name));
						break;
					case 6:
						mob.tell(L("You NEED some @x1, NOW!", name));
						break;
					case 7:
						mob.tell(L("Some @x1 would be lovely.", name));
						break;
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean okMessage(final Environmental host, final CMMsg msg)
	{
		if(affected instanceof MOB)
		{
			if((msg.source()==affected)
			&&(msg.targetMinor()==CMMsg.TYP_WEAR)
			&&(msg.target() instanceof Light)
			&&(msg.target() instanceof Container)
			&&(CMath.bset(((Item)msg.target()).rawProperLocationBitmap(),Wearable.WORN_MOUTH)))
			{
				final List<Item> contents=((Container)msg.target()).getContents();
				if(contents.size()>0)
				{
					final Environmental content=contents.get(0);
					for(final String addictedStr : lastFix.keySet())
					{
						if(addictedStr.startsWith("effect:"))
						{
							if(content instanceof Physical)
							{
								if(((Physical)content).fetchEffect(addictedStr.substring(7))!=null)
									puffCredit.put(addictedStr, (Item)msg.target());
							}
						}
						else
						if(CMLib.english().containsString(content.Name(),addictedStr))
							puffCredit.put(addictedStr, (Item)msg.target());
					}
				}
			}
		}
		return true;
	}

	public void maybeFixFix(final Environmental E)
	{
		final String name = E.name();
		for(final String addictedStr : lastFix.keySet())
		{
			if(addictedStr.startsWith("effect:"))
			{
				if(E instanceof Physical)
				{
					if(((Physical)E).fetchEffect(addictedStr.substring(7))!=null)
						lastFix.get(addictedStr)[0] = System.currentTimeMillis();
				}
			}
			else
			if(CMLib.english().containsString(name,addictedStr))
				lastFix.get(addictedStr)[0] = System.currentTimeMillis();
			else
			if((E instanceof Item) && addictedStr.equalsIgnoreCase(RawMaterial.CODES.NAME(((Item)E).material())))
				lastFix.get(addictedStr)[0] = System.currentTimeMillis();
			else
			if(this.puffCredit.containsKey(addictedStr))
				lastFix.get(addictedStr)[0] = System.currentTimeMillis();
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(affected instanceof MOB)
		{
			if(msg.source()==affected)
			{
				if(((msg.targetMinor()==CMMsg.TYP_EAT)
					||(msg.targetMinor()==CMMsg.TYP_DRINK))
				&&((msg.target() instanceof Food)
					||(msg.target() instanceof Drink)
					||(msg.target() instanceof Pill)
					||(msg.target() instanceof Potion))
				&&(msg.target() instanceof Item))
					maybeFixFix(msg.target());

				if((msg.targetMinor()==CMMsg.TYP_SNIFF)
				&&(msg.target() instanceof MagicDust))
					maybeFixFix(msg.target());

				if((msg.amISource((MOB)affected))
				&&(msg.targetMinor()==CMMsg.TYP_HANDS)
				&&(msg.target() instanceof Light)
				&&(msg.tool() instanceof Light)
				&&(msg.target()==msg.tool())
				&&(((Light)msg.target()).amWearingAt(Wearable.WORN_MOUTH))
				&&(((Light)msg.target()).isLit()))
					maybeFixFix(msg.target());
			}
		}
		super.executeMsg(myHost,msg);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		Physical target=givenTarget;
		if((target==null)&&(text().length()==0))
			return false;
		else
		if((target==mob)&&(text().length()>0))
			target=null;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			String addiction;
			if(target != null)
			{
				addiction=target.Name().toUpperCase();
				if(addiction.toUpperCase().startsWith("A POUND OF "))
					addiction=addiction.substring(11);
				if(addiction.toUpperCase().startsWith("A "))
					addiction=addiction.substring(2);
				if(addiction.toUpperCase().startsWith("AN "))
					addiction=addiction.substring(3);
				if(addiction.toUpperCase().startsWith("SOME "))
					addiction=addiction.substring(5);
			}
			else
				addiction=text();
			if(commands.size()>0)
			{
				String cmdAdd = CMParms.combine(commands,0);
				if((!cmdAdd.toLowerCase().startsWith("effect:"))
				&&(CMClass.getAbilityPrototype(cmdAdd)!=null))
					cmdAdd = "effect:"+CMClass.getAbilityPrototype(cmdAdd).ID();
				if(addiction.length()>0)
					addiction += ";" + cmdAdd;
				else
					addiction = cmdAdd;
			}
			final Ability oldA=mob.fetchEffect(ID());
			if(oldA!=null)
			{
				if(oldA.text().indexOf(addiction)<0)
					oldA.setMiscText(oldA.text()+";"+addiction);
				return false;
			}
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_OK_VISUAL,"");
			if(mob.location()!=null)
			{
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					final Ability A=(Ability)copyOf();
					A.setMiscText(addiction.trim());
					mob.addNonUninvokableEffect(A);
				}
			}
			else
			{
				final Ability A=(Ability)copyOf();
				A.setMiscText(addiction.trim());
				mob.addNonUninvokableEffect(A);
			}
		}
		return success;
	}
}
