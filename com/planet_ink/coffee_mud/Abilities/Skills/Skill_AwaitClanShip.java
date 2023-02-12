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
   Copyright 2002-2023 Bo Zimmerman

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
public class Skill_AwaitClanShip extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_AwaitClanShip";
	}

	private final static String	localizedName	= CMLib.lang().L("Await Clan Ship");

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

	private static final String[]	triggerStrings	= I(new String[] { "AWAITCLANSHIP" });

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
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
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
			mob.tell(L("You can only wait for your clan ship at the shore."));
			return false;
		}

		// now see if it worked
		if(!super.invoke(mob, commands, givenTarget, auto, asLevel))
			return false;

		NavigableItem targetShip=null;
		final boolean[] possShipI=new boolean[] { false };
		boolean success=proficiencyCheck(mob,0,auto);
		Room targetR = null;
		List<Room> trail = null;
		if(success)
		{
			final TrackingFlags flags=CMLib.tracking().newFlags()
											.plus(TrackingFlag.NOAIR)
											.plus(TrackingFlag.PASSABLE)
											.plus(TrackingFlag.WATERSURFACEORSHOREONLY);
			final NavigableItem[] targetShipI=new NavigableItem[1];
			final TrackingLibrary.RFilter destFilter = new TrackingLibrary.RFilter()
			{
				@Override
				public boolean isFilteredOut(final Room hostR, final Room R, final Exit E, final int dir)
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
						for(final Enumeration<Item> i=R.items();i.hasMoreElements();)
						{
							final Item I=i.nextElement();
							if((I instanceof NavigableItem)
							&&(((NavigableItem)I).navBasis()==Rideable.Basis.WATER_BASED)
							&&(I instanceof PrivateProperty)
							&&(((PrivateProperty)I).isProperlyOwned())
							&&(CMLib.clans().getClan(((PrivateProperty)I).getOwnerName())!=null)
							&&(CMLib.law().doesHavePrivilegesWith(mob, (PrivateProperty)I)))
							{
								final NavigableItem shipI = (NavigableItem)I;
								possShipI[0]=true;
								for(final Enumeration<Room> r = shipI.getArea().getFilledProperMap();r.hasMoreElements();)
								{
									final Room R1=r.nextElement();
									if((R1==null)||(R1.numInhabitants()==0))
										continue;
									for(final Enumeration<MOB> m = R1.inhabitants();m.hasMoreElements();)
									{
										final MOB M = m.nextElement();
										if(M.isPlayer() && shipI.securityCheck(M))
											return true;
									}
								}
								targetShipI[0]= shipI;
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

		if(success && (trail!=null))
		{
			if(targetShip != null)
			{
				invoker=mob;
				final CMMsg msg=CMClass.getMsg(mob,targetShip,this,CMMsg.MSG_QUIETMOVEMENT|(auto?CMMsg.MASK_ALWAYS:0),auto?"":L("<S-NAME> wait(s) for the scheduled arrival of <T-NAME>."));
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					final List<Integer> newCourse=new ArrayList<Integer>();
					Room room=trail.get(trail.size()-1);
					for(int i=0;i<trail.size();i++)
					{
						final Room nextRoom=trail.get(i);
						final int dir=CMLib.map().getRoomDir(room, nextRoom);
						if(dir >= 0)
							newCourse.add(Integer.valueOf(dir));
						room=nextRoom;
					}
					targetShip.setAnchorDown(false);
					targetShip.setCurrentCourse(newCourse);
				}
			}
			else
			if(possShipI[0])
				return beneficialVisualFizzle(mob,targetShip,L("<S-NAME> wait(s) for <S-HIS-HER> clan ship, but that's the captains work to do."));
			else
				return beneficialVisualFizzle(mob,targetShip,L("<S-NAME> await(s) <S-HIS-HER> clan ship, but it never arrives."));
		}
		else
			return beneficialVisualFizzle(mob,targetShip,L("<S-NAME> wait(s) for <S-HIS-HER> clan ship to come in, but it never does."));

		// return whether it worked
		return success;
	}
}
