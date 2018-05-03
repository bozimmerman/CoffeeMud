package com.planet_ink.coffee_mud.Abilities.Skills;
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
   Copyright 2016-2018 Bo Zimmerman

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

public class Skill_CombatRepairs extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_CombatRepairs";
	}

	private final static String	localizedName	= CMLib.lang().L("Combat Repairs");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Temporary Patches)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS;
	}

	private static final String[]	triggerStrings	= I(new String[] { "COMBATREPAIR","COMBATREPAIRS" });

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_SEATRAVEL;
	}

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT|USAGE_MANA;
	}

	protected int	code		= 0;
	
	@Override
	public int abilityCode()
	{
		return code;
	}

	@Override
	public void setAbilityCode(int newCode)
	{
		code = newCode;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(affected instanceof SailingShip)
		{
			final SailingShip I=(SailingShip)affected;
			if(I.subjectToWearAndTear())
			{
				final PhysicalAgent currentVictim = I.getCombatant();
				if(currentVictim == null)
				{
					if(I.usesRemaining()<=code)
						unInvoke();
					else
					{
						I.setUsesRemaining(I.usesRemaining()-5);
						Area A=I.getShipArea();
						if(A!=null)
						{
							for(Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
							{
								final Room R=r.nextElement();
								if((R!=null)&&(R.numInhabitants()>0))
								{
									R.showHappens(CMMsg.MSG_OK_ACTION, L("The temporary combat repairs are slowly unraveling."));
								}
							}
						}
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((CMLib.flags().isSitting(mob)||CMLib.flags().isSleeping(mob)))
		{
			mob.tell(L("You are on the floor!"));
			return false;
		}

		if(!CMLib.flags().isAliveAwakeMobileUnbound(mob,false))
			return false;
		
		final Room R=mob.location();
		if(R==null)
			return false;
		
		final SailingShip ship;
		if((R.getArea() instanceof BoardableShip)
		&&(((BoardableShip)R.getArea()).getShipItem() instanceof SailingShip))
		{
			ship=(SailingShip)((BoardableShip)R.getArea()).getShipItem();
		}
		else
		{
			mob.tell(L("You must be on a ship to do combat repairs!"));
			return false;
		}
		
		if(ship.fetchEffect(ID())!=null)
		{
			mob.tell(L("Your ship has already undergone temporary combat repairs!"));
			return false;
		}
		
		Room shipR=CMLib.map().roomLocation(ship);
		if((shipR==null)||(!CMLib.flags().isWaterySurfaceRoom(shipR))||(!ship.subjectToWearAndTear()))
		{
			mob.tell(L("You must be on a sailing ship to do combat repairs!"));
			return false;
		}
		
		if((!ship.isInCombat())||(ship.usesRemaining()<=0))
		{
			mob.tell(L("Your ship must be in combat to do combat repairs!"));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,ship,this,CMMsg.MASK_MALICIOUS|CMMsg.MSG_NOISYMOVEMENT,auto?L("<T-NAME> is suddenly patched up!"):L("<S-NAME> make(s) quick combat repairs to <T-NAME>!"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				int dmg=ship.usesRemaining();
				dmg += 20 + mob.charStats().getStat(CharStats.STAT_DEXTERITY)+(7 * super.getXLEVELLevel(mob));
				if(dmg > 100)
					dmg = 100;
				Ability A=beneficialAffect(mob, ship, asLevel, 0);
				if(A!=null)
				{
					A.setAbilityCode(ship.usesRemaining());
					A.makeLongLasting();
				}
				ship.setUsesRemaining(dmg);
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to do quick ship repairs, but mess(es) it up."));
		return success;
	}
}
