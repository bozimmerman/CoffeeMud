package com.planet_ink.coffee_mud.Abilities.Thief;
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
   Copyright 2016-2016 Bo Zimmerman

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

public class Thief_MerchantFlag extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_MerchantFlag";
	}

	private final static String	localizedName	= CMLib.lang().L("Merchant Flag");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Merchant Flag)");

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

	private static final String[]	triggerStrings	= I(new String[] { "MERCHANTFLAG" });

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

	protected TimeClock lastFlag = null;
	
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
		if(affected instanceof Item)
		{
			final Item I=(Item)affected;
			if(I.subjectToWearAndTear())
			{
				final String currentVictim = I.getStat("COMBATTARGET");
				if(currentVictim.length()==0)
				{
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
		
		final Item target;
		if((R.getArea() instanceof BoardableShip)
		&&(((BoardableShip)R.getArea()).getShipItem() instanceof BoardableShip))
		{
			target=((BoardableShip)R.getArea()).getShipItem();
		}
		else
		{
			mob.tell(L("You must be on a ship to raise the merchant flag!"));
			return false;
		}
		
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(L("Your ship is already flying the merchant flag!"));
			return false;
		}
		
		Room shipR=CMLib.map().roomLocation(target);
		if((shipR==null)||(!CMLib.flags().isWaterySurfaceRoom(shipR))||(!target.subjectToWearAndTear()))
		{
			mob.tell(L("You must be on a sailing ship to raise the merchant flag!"));
			return false;
		}
		
		BoardableShip ship = (BoardableShip)target;
		final String currentVictim = ship.getStat("COMBATTARGET");
		if(currentVictim.length()>0)
		{
			mob.tell(L("Your ship must not be in combat to raise the merchant flag!"));
			return false;
		}

		final TimeClock now = CMLib.time().localClock(shipR);
		if(lastFlag!=null)
		{
			long mudHoursDiff = now.deriveMillisAfter(lastFlag);
			long hoursPerMonth = now.getDaysInMonth() * now.getHoursInDay();
			if(mudHoursDiff < hoursPerMonth)
			{
				mob.tell(L("Your ship is too notorious to fly a merchant flag at this time!"));
				return false;
			}
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MALICIOUS|CMMsg.MSG_NOISYMOVEMENT,auto?L("<T-NAME> is protected from attack!"):L("<S-NAME> raise(s) an innocent countries merchant flag above <T-NAME>!"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Ability A=beneficialAffect(mob, target, asLevel, 0);
				if(A!=null)
				{
					lastFlag = (TimeClock)now.copyOf();
					A.setAbilityCode(adjustedLevel(mob,asLevel));
					A.makeLongLasting();
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to raise a merchant flag, but mess(es) it up."));
		return success;
	}
}
