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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.TrackingFlag;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.TrackingFlags;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2002-2018 Bo Zimmerman

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

public class Skill_AwaitShip extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_AwaitShip";
	}

	private final static String	localizedName	= CMLib.lang().L("Await Ship");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[]	triggerStrings	= I(new String[] { "AWAITSHIP" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MANA;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_SEATRAVEL;
	}
	
	@Override
	public boolean invoke(final MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Room waterRoom=null;
		final Room R=mob.location();
		if(!CMLib.flags().isWaterySurfaceRoom(R))
		{
			for(int i=0;i<Directions.NUM_DIRECTIONS();i++)
			{
				final Room R2=R.getRoomInDir(i);
				final Exit E2=R.getExitInDir(i);
				if((R2!=null)&&(E2!=null)&&(E2.isOpen())&&(CMLib.flags().isWaterySurfaceRoom(R2)))
				{
					waterRoom=R2;
					break;
				}
			}
		}
		else
			waterRoom=R;
		if(waterRoom == null)
		{
			mob.tell(L("You can only wait for your ship at the shore."));
			return false;
		}
		
		// now see if it worked
		if(!super.invoke(mob, commands, givenTarget, auto, asLevel))
			return false;
		
		SailingShip targetShip=null;
		boolean success=proficiencyCheck(mob,0,auto);
		Room targetR = null;
		List<Room> trail = null;
		if(success)
		{
			TrackingFlags flags=CMLib.tracking().newFlags().plus(TrackingFlag.NOAIR)
															.plus(TrackingFlag.WATERSURFACEORSHOREONLY);
			final SailingShip[] targetShipI=new SailingShip[1];
			TrackingLibrary.RFilter destFilter = new TrackingLibrary.RFilter()
			{
				@Override
				public boolean isFilteredOut(Room hostR, Room R, Exit E, int dir)
				{
					if (R == null)
						return false;
					switch (R.domainType())
					{
					case Room.DOMAIN_INDOORS_UNDERWATER:
					case Room.DOMAIN_OUTDOORS_UNDERWATER:
						return true;
					default:
					{
						for(Enumeration<Item> i=R.items();i.hasMoreElements();)
						{
							final Item I=i.nextElement();
							if((I instanceof SailingShip)
							&&(I instanceof PrivateProperty)
							&&(CMLib.law().doesHavePrivilegesWith(mob, (PrivateProperty)I)))
							{
								targetShipI[0]=(SailingShip)I;
								return false;
							}
						}
						return true;
					}
					}
				}
			};
			trail = CMLib.tracking().findTrailToAnyRoom(R, destFilter, flags, 30+(5*super.getXLEVELLevel(mob)));
			if((trail!=null)&&(trail.size()>0))
				targetR=trail.get(0);
			
			if((targetR==null)||(trail==null)||(trail.size()==0))
			{
				success=false;
			}
			else
			{
				targetShip = targetShipI[0];
				if(targetShip.isInCombat())
				{
					success=false;
				}
			}
			
		}
		
		if(success && (trail!=null) && (targetShip != null))
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,targetShip,this,CMMsg.MSG_QUIETMOVEMENT|(auto?CMMsg.MASK_ALWAYS:0),auto?"":L("<S-NAME> wait(s) for the scheduled arrival of <T-NAME>."));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				List<Integer> newCourse=new ArrayList<Integer>();
				Room room=trail.get(trail.size()-1);
				for(int i=0;i<trail.size();i++)
				{
					Room nextRoom=trail.get(i);
					int dir=CMLib.map().getRoomDir(room, nextRoom);
					if(dir >= 0)
						newCourse.add(Integer.valueOf(dir));
					room=nextRoom;
				}
				targetShip.setAnchorDown(false);
				targetShip.setCurrentCourse(newCourse);
			}
		}
		else
			return beneficialVisualFizzle(mob,targetShip,L("<S-NAME> wait(s) for <S-HIS-HER> ship to come in, but it never does."));

		// return whether it worked
		return success;
	}
}
