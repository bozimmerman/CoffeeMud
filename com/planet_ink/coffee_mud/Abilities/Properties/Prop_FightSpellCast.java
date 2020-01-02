package com.planet_ink.coffee_mud.Abilities.Properties;
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

import java.util.*;

/*
   Copyright 2001-2020 Bo Zimmerman

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
public class Prop_FightSpellCast extends Prop_SpellAdder
{
	@Override
	public String ID()
	{
		return "Prop_FightSpellCast";
	}

	@Override
	public String name()
	{
		return "Casting spells when properly used during combat";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	public String accountForYourself()
	{
		return spellAccountingsWithMask("Casts ", " during combat.");
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_CASTER;
	}

	@Override
	public int triggerMask()
	{
		return TriggeredAffect.TRIGGER_HITTING_WITH;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(processing)
			return;

		if(!(affected instanceof Item))
			return;
		try
		{
			processing=true;

			final Item myItem=(Item)affected;

			if((myItem!=null)
			&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
			&&((msg.value())>0))
			{
				if(CMLib.combat().isAShipSiegeWeapon(myItem)
				&&(msg.target() instanceof MOB))
					addMeIfNeccessary(msg.source(),(MOB)msg.target(),false,0,maxTicks);
				else
				if((myItem.amBeingWornProperly())
				&&(myItem.owner() instanceof MOB)
				&&(msg.target() instanceof MOB))
				{
					final MOB mob=(MOB)myItem.owner();
					if((mob.isInCombat())
					&&(mob.location()!=null)
					&&(!mob.amDead()))
					{
						if((myItem instanceof Weapon)
						&&(msg.tool()==myItem)
						&&(myItem.amWearingAt(Wearable.WORN_WIELD))
						&&(msg.amISource(mob)))
							addMeIfNeccessary(msg.source(),(MOB)msg.target(),false,0,maxTicks);
						else
						if((msg.amITarget(mob))
						&&(!myItem.amWearingAt(Wearable.WORN_WIELD))
						&&(!(myItem instanceof Weapon)))
							addMeIfNeccessary(mob,mob,false,0,maxTicks);
					}
				}
				else
				if(CMLib.combat().isAShipSiegeWeapon(myItem)
				&&(msg.target() instanceof Item))
				{
					final Item I=(Item)msg.target();
					if(I instanceof BoardableShip)
					{
						final Area A=((BoardableShip)I).getShipArea();
						if(A!=null)
						{
							final List<Physical> stuff = new ArrayList<Physical>();
							for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
							{
								final Room R=r.nextElement();
								if((R!=null)&&((R.domainType()&Room.INDOORS)==0))
								{
									final Item I2=R.getRandomItem();
									if(I2!=null)
										stuff.add(I2);
									final MOB M=R.fetchRandomInhabitant();
									if(M!=null)
										stuff.add(M);
								}
							}
							if(stuff.size()>0)
							{
								final Physical P=stuff.get(CMLib.dice().roll(1, stuff.size(), -1));
								if(P!=null)
									addMeIfNeccessary(msg.source(),P,true,0,maxTicks);
							}
						}
					}
					else
						addMeIfNeccessary(msg.source(),I,true,0,maxTicks);
				}
			}
		}
		finally
		{
			processing=false;
		}
	}


	@Override
	public String getStat(final String code)
	{
		if(code == null)
			return "";
		if(code.equalsIgnoreCase("STAT-LEVEL"))
		{
			int level = 0;
			for(final Pair<Ability,Integer> p : this.getMySpellsV())
			{
				final Ability A=p.first;
				if(A!=null)
				{
					final int mul=-1;
					//if(A.abstractQuality()==Ability.QUALITY_MALICIOUS)
					//	mul=1;
					level += ((mul*CMLib.ableMapper().lowestQualifyingLevel(A.ID()))/2);
				}
			}
			return ""+level;
		}
		else
		if(code.toUpperCase().startsWith("STAT-"))
			return "";
		return super.getStat(code);
	}

	@Override
	public void setStat(final String code, final String val)
	{
		if(code!=null)
		{
			if(code.equalsIgnoreCase("STAT-LEVEL"))
			{

			}
			else
			if(code.equalsIgnoreCase("TONEDOWN"))
			{
				setStat("TONEDOWN-MISC",val);
			}
			else
			if((code.equalsIgnoreCase("TONEDOWN-ARMOR"))
			||(code.equalsIgnoreCase("TONEDOWN-WEAPON"))
			||(code.equalsIgnoreCase("TONEDOWN-MISC")))
			{
				/*
				final double pct=CMath.s_pct(val);
				final String s=text();
				int plusminus=s.indexOf('+');
				int minus=s.indexOf('-');
				if((minus>=0)&&((plusminus<0)||(minus<plusminus)))
					plusminus=minus;
				while(plusminus>=0)
				{
					minus=s.indexOf('-',plusminus+1);
					plusminus=s.indexOf('+',plusminus+1);
					if((minus>=0)&&((plusminus<0)||(minus<plusminus)))
						plusminus=minus;
				}
				setMiscText(s);
				*/
			}
		}
		else
			super.setStat(code, val);
	}
}
