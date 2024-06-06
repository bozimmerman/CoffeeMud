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
public class Skill_HardToStarboard extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_HardToStarboard";
	}

	private final static String	localizedName	= CMLib.lang().L("Hard to Starboard");

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

	private static final String[]	triggerStrings	= I(new String[] { "HARDTOSTARBOARD" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_SEATRAVEL;
	}

	@Override
	protected int overrideMana()
	{
		return 100;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if(affected instanceof NavigableItem)
		{
			final NavigableItem ship=(NavigableItem)affected;
			if((msg.target()==ship)
			&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
			&&(msg.value()>0)
			&&(CMLib.dice().rollPercentage()<=(25+super.getXLEVELLevel(invoker()))))
			{
				msg.setValue(0);
			}
		}
		return true;
	}

	protected volatile long lastUse = 0;

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
		if((!(R.getArea() instanceof Boardable))
		||(!(((Boardable)R.getArea()).getBoardableItem() instanceof NavigableItem))
		||(((NavigableItem)(((Boardable)R.getArea()).getBoardableItem())).navBasis() != Rideable.Basis.WATER_BASED))
		{
			mob.tell(L("You must be on a sailing ship."));
			return false;
		}
		final Boardable myShip=(Boardable)R.getArea();
		final NavigableItem myShipItem=(NavigableItem)myShip.getBoardableItem();
		final Area myShipArea=myShip.getArea();
		final Room myShipRoom = CMLib.map().roomLocation(myShipItem);
		if((myShipItem==null)
		||(myShipArea==null)
		||(myShipRoom==null)
		||(!(myShipItem.owner() instanceof Room)))
		{
			mob.tell(L("You must be on your sailing ship."));
			return false;
		}

		if((R.domainType()&Room.INDOORS)!=0)
		{
			mob.tell(L("You must be on the deck of a ship."));
			return false;
		}

		if((!CMLib.law().doesHavePriviledgesHere(mob, R))
		&&(!CMSecurity.isAllowed(mob, R, CMSecurity.SecFlag.CMDMOBS)))
		{
			mob.tell(L("You must be on the deck of a ship that you have privileges on."));
			return false;
		}
		if(myShipItem.getCombatant()==null)
		{
			mob.tell(L("You must be in ship combat to use this skill."));
			return false;
		}

		if(((System.currentTimeMillis()-lastUse)<(3 * 60 * 1000))
		||(myShipItem.fetchEffect("Skill_HardToPort")!=null)
		||(myShipItem.fetchEffect("Skill_HardToStern")!=null))
		{
			mob.tell(L("You can't put your ship through another hard turn to starboard attempt right now.  Wait a bit."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final String str=L("<S-NAME> grab(s) the ship rigging.");
			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_DELICATE_HANDS_ACT,str);
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				CMLib.commands().forceStandardCommand(mob, "Yell", new XVector<String>("YELL",L("HARD TO STARBOARD!")));
				{
					int newDirection = -1;
					switch(myShipItem.getDirectionFacing())
					{
					case Directions.NORTH:
						newDirection=Directions.EAST;
						break;
					case Directions.SOUTH:
						newDirection=Directions.WEST;
						break;
					case Directions.EAST:
						newDirection=Directions.SOUTH;
						break;
					case Directions.WEST:
						newDirection=Directions.NORTH;
						break;
					case Directions.UP:
						break;
					case Directions.DOWN:
						break;
					case Directions.NORTHEAST:
						newDirection=Directions.SOUTHEAST;
						break;
					case Directions.NORTHWEST:
						newDirection=Directions.NORTHEAST;
						break;
					case Directions.SOUTHEAST:
						newDirection=Directions.SOUTHWEST;
						break;
					case Directions.SOUTHWEST:
						newDirection=Directions.NORTHWEST;
						break;
					}
					if(newDirection >= 0)
					{
						final Room thisRoom=myShipRoom;
						final MOB smob = CMClass.getFactoryMOB(myShipItem.name(),myShipItem.phyStats().level(),thisRoom);
						try
						{
							smob.setRiding(myShipItem);
							if((myShipItem instanceof PrivateProperty)
							&&(((PrivateProperty)myShipItem).isProperlyOwned()))
							{
								final Clan clan = CMLib.clans().fetchClanAnyHost(((PrivateProperty)myShipItem).getOwnerName());
								if(clan != null)
									smob.setClan(clan.name(), clan.getAutoPosition());
							}
							smob.basePhyStats().setDisposition(smob.basePhyStats().disposition()|PhyStats.IS_SWIMMING);
							smob.phyStats().setDisposition(smob.phyStats().disposition()|PhyStats.IS_SWIMMING);
							final String directionName = CMLib.directions().getDirectionName(newDirection);
							final String finalDirectionName = CMLib.directions().getDirectionName(newDirection);
							final CMMsg maneuverMsg=CMClass.getMsg(smob, thisRoom, null,
																	CMMsg.MSG_ADVANCE,directionName,
																	CMMsg.MSG_ADVANCE,finalDirectionName,
																	CMMsg.MSG_ADVANCE,L("<S-NAME> change(s) course, turning hard @x1.",directionName));
							if(thisRoom.okMessage(smob, maneuverMsg))
							{
								thisRoom.send(smob, maneuverMsg);
								myShipItem.setDirectionFacing(newDirection);
								this.lastUse=System.currentTimeMillis();
								super.beneficialAffect(mob, myShipItem, asLevel, 6);
							}
						}
						finally
						{
							smob.destroy();
						}
					}
				}
			}
		}
		else
		{
			this.lastUse+=CMProps.getTickMillis();
			return beneficialVisualFizzle(mob,null,L("<S-NAME> can't seem to control the rigging."));
		}

		return success;
	}

}
