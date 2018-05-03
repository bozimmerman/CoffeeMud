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

public class Thief_MerchantFlag extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_MerchantFlag";
	}

	private final static String	localizedName	= CMLib.lang().L("Fly Merchant Flag");

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

	private static final String[]	triggerStrings	= I(new String[] { "FLYMERCHANTFLAG","MERCHANTFLAG" });

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_SEATRAVEL;
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
		if(affected instanceof SailingShip)
		{
			final SailingShip I=(SailingShip)affected;
			if(I.subjectToWearAndTear())
			{
				if(I.isInCombat())
				{
					unInvoke();
				}
			}
		}
		return true;
	}
	
	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if((msg.targetMinor() == CMMsg.TYP_ADVANCE)
		&&((msg.target() instanceof BoardableShip)
			||(msg.target() instanceof Rideable))
		&&(msg.targetMajor(CMMsg.MASK_MALICIOUS))
		&&(msg.source().riding() instanceof BoardableShip)
		&&(msg.source().riding().Name().equals(msg.source().Name())))
		{
			if((msg.source().riding() == affected)
			&&(msg.source().Name().equals(affected.Name())))
			{
				unInvoke();
				return;
			}
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((msg.targetMinor() == CMMsg.TYP_ADVANCE)
		&&((msg.target() instanceof BoardableShip)
			||(msg.target() instanceof Rideable))
		&&(msg.targetMajor(CMMsg.MASK_MALICIOUS))
		&&(msg.source().riding() instanceof BoardableShip)
		&&(msg.source().riding().Name().equals(msg.source().Name())))
		{
			if(msg.source().riding() == affected)
			{
				// execute will end this
				return true;
			}
			if(msg.target() == affected)
			{
				boolean pirateAboard=false;
				final BoardableShip ship=(BoardableShip)msg.source().riding();
				if(ship != null)
				{
					final Area area=ship.getShipArea();
					for(final Enumeration<Room> r=area.getProperMap();r.hasMoreElements();)
					{
						final Room R=r.nextElement();
						if((R!=null)&&(R.numPCInhabitants()>0))
						{
							for(Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
							{
								final MOB M=m.nextElement();
								if((M!=null)
								&&(!M.isMonster())
								&&((M.charStats().getClassLevel("Pirate")>0)
									||(M.fetchAbility(ID())!=null)))
								{
									pirateAboard=true;
									break;
								}
							}
						}
						if(pirateAboard)
							break;
					}
					if(!pirateAboard)
					{
						//TODO: tell the ship why they can't, and then 
						return false;
					}
				}
			}
		}
		return true;
	}

	@Override
	public void unInvoke()
	{
		final Physical P=affected;
		super.unInvoke();
		if(P instanceof BoardableShip)
		{
			final Room R=CMLib.map().roomLocation(P);
			if((R!=null)&&(CMLib.flags().isWaterySurfaceRoom(R))&&(super.canBeUninvoked()))
				R.showHappens(CMMsg.MSG_OK_VISUAL, L("@x1 lower(s) its Merchant Flag.",P.name()));
		}
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
			mob.tell(L("You must be on a ship to raise the merchant flag!"));
			return false;
		}
		
		if(ship.fetchEffect(ID())!=null)
		{
			mob.tell(L("Your ship is already flying the merchant flag!"));
			return false;
		}
		
		Room shipR=CMLib.map().roomLocation(ship);
		if((shipR==null)||(!CMLib.flags().isWaterySurfaceRoom(shipR))||(!ship.subjectToWearAndTear()))
		{
			mob.tell(L("You must be on a sailing ship to raise the merchant flag!"));
			return false;
		}
		
		if(ship.isInCombat())
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
			final CMMsg msg=CMClass.getMsg(mob,ship,this,CMMsg.MASK_MALICIOUS|CMMsg.MSG_NOISYMOVEMENT,auto?L("<T-NAME> is protected from attack!"):L("<S-NAME> raise(s) an innocent countries merchant flag above <T-NAME>!"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Ability A=beneficialAffect(mob, ship, asLevel, 0);
				if(A!=null)
				{
					lastFlag = (TimeClock)now.copyOf();
					A.setAbilityCode(adjustedLevel(mob,asLevel));
					A.makeLongLasting();
					if(CMLib.flags().isWaterySurfaceRoom(shipR))
						R.showHappens(CMMsg.MSG_OK_VISUAL, L("@x1 raise(s) its Merchant Flag.",ship.name()));
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to raise a merchant flag, but mess(es) it up."));
		return success;
	}
}
