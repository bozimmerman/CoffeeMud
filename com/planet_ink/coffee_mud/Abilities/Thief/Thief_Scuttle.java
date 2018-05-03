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

public class Thief_Scuttle extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_Scuttle";
	}

	private final static String	localizedName	= CMLib.lang().L("Scuttle");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "SCUTTLE" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_SEATRAVEL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	public int usageType()
	{
		return USAGE_MANA;
	}

	@Override
	public boolean invoke(final MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
		final Room boatRoom;
		final Rideable boat;
		if((R.getArea() instanceof BoardableShip)
		&&(R.domainType()!=Room.DOMAIN_OUTDOORS_AIR)
		&&(((BoardableShip)R.getArea()).getShipItem() instanceof Rideable))
		{
			boat = (Rideable)((BoardableShip)R.getArea()).getShipItem();
			boatRoom=CMLib.map().roomLocation(((BoardableShip)R.getArea()).getShipItem());
		}
		else
		if((mob.riding() != null)&&(mob.riding().rideBasis()==Rideable.RIDEABLE_WATER))
		{
			boat=mob.riding();
			boatRoom=mob.location();
		}
		else
		{
			mob.tell(L("You must be on a ship or boat to scuttle it."));
			return false;
		}
		
		if(boat == null)
		{
			mob.tell(L("You want to scuttle what now?"));
			return false;
		}
		
		if(((!CMLib.combat().mayIAttackThisVessel(mob, boat))
			&&(!CMLib.law().doesHavePriviledgesHere(mob, R))))
		{
			mob.tell(L("You are not permitted to scuttle this boat."));
			return false;
		}
		
		if((boatRoom==null)
		||(!CMLib.flags().isWaterySurfaceRoom(boatRoom)))
		{
			mob.tell(L("The boat must be on the waves to scuttle it."));
			return false;
		}

		int numRiders=0;
		if(R.getArea() instanceof BoardableShip)
		{
			for(Enumeration<Room> r=R.getArea().getProperMap();r.hasMoreElements();)
			{
				final Room R2=r.nextElement();
				if(R2!=null)
				{
					numRiders += R2.numInhabitants();
				}
			}
		}
		else
		if((mob.riding() != null)&&(mob.riding().rideBasis()==Rideable.RIDEABLE_WATER))
			numRiders=mob.riding().numRiders();
		if(numRiders > 1)
		{
			mob.tell(L("You must be the last person aboard to scuttle a ship"));
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		Physical target=boat;
		final int adjustment=target.phyStats().level()-((mob.phyStats().level()+super.getXLEVELLevel(mob))/2);
		boolean success=proficiencyCheck(mob,-adjustment,auto);
		if(success)
		{
			String str=auto?"":L("^S<S-NAME> scuttle(s) <T-NAME>!^?");
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_THIEF_ACT,str,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.MSG_THIEF_ACT|(auto?CMMsg.MASK_ALWAYS:0),str,CMMsg.MSG_NOISYMOVEMENT,str);
			if(boatRoom.okMessage(mob,msg))
			{
				boatRoom.send(mob,msg);
				if((boatRoom != R)
				&&((R.domainType()&Room.INDOORS)>0))
				{
					msg.setSourceCode(CMMsg.NO_EFFECT);
					msg.setSourceMessage(null);
					R.send(mob,msg);
				}
				if((boat instanceof Item)
				&&(((Item)boat).subjectToWearAndTear()))
					((Item)boat).setUsesRemaining(0);
				CMLib.threads().scheduleRunnable(new Runnable(){
					@Override
					public void run()
					{
						if((mob.riding() != null)&&(mob.riding().rideBasis()==Rideable.RIDEABLE_WATER))
							mob.riding().setRideBasis(Rideable.RIDEABLE_SIT);
						CMLib.tracking().makeSink(boat, boatRoom, false);
						final String sinkString = L("<T-NAME> start(s) sinking!");
						boatRoom.show(mob, boatRoom, CMMsg.MSG_OK_ACTION, sinkString);
						CMLib.tracking().walkForced(mob, R, boatRoom, false, true, L("<S-NAME> jump(s) off @x1 and go(es) kersplash!",boat.name()));
					}
				}, 1000);
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to scuttle <T-NAME>, but fails."));

		// return whether it worked
		return success;
	}
}
