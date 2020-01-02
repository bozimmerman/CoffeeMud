package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Move;
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
   Copyright 2018-2020 Bo Zimmerman

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
public class Skill_AbandonShip extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_AbandonShip";
	}

	private final static String	localizedName	= CMLib.lang().L("Abandon Ship");

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

	private static final String[]	triggerStrings	= I(new String[] { "ABANDONSHIP" });

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

	public int hasOverboardExit(final Room R)
	{
		if((R.domainType()&Room.INDOORS)==0)
		{
			for(int i=0;i<Directions.NUM_DIRECTIONS();i++)
			{
				final Exit E2=R.getExitInDir(i);
				final Room R2=R.getRoomInDir(i);
				if((R2!=null)
				&&(R2.getArea()!=R.getArea())
				&&(E2!=null)
				&&(E2.isOpen()))
					return i;
			}
		}
		return -1;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		Room R=mob.location();
		if(R==null)
			return false;
		if((!(R.getArea() instanceof BoardableShip))
		||(!(((BoardableShip)R.getArea()).getShipItem() instanceof SailingShip)))
		{
			mob.tell(L("You must be on a sailing ship."));
			return false;
		}
		final BoardableShip myShip=(BoardableShip)R.getArea();
		final SailingShip myShipItem=(SailingShip)myShip.getShipItem();
		final Area myShipArea=myShip.getShipArea();
		final Room myShipRoom = CMLib.map().roomLocation(myShipItem);
		if((myShipItem==null)
		||(myShipArea==null)
		||(myShipRoom==null)
		||(!(myShipItem.owner() instanceof Room)))
		{
			mob.tell(L("You must be on your sailing ship."));
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

		if(CMLib.flags().isUnderWateryRoom(myShipRoom))
		{
			mob.tell(L("It's a little late to abandon ship .. it's already sunk."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final String str=L("<S-NAME> prepare(s) to abandon the ship.");
			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_DELICATE_HANDS_ACT,str);
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				int overboardDir = hasOverboardExit(R);
				if(overboardDir<0)
				{
					for(final Enumeration<Room> r=myShipArea.getProperMap();r.hasMoreElements();)
					{
						final Room R1=r.nextElement();
						if(R1==null)
							continue;
						overboardDir = hasOverboardExit(R1);
						if(overboardDir >= 0)
						{
							R=R1;
							CMLib.tracking().wanderFromTo(mob, R1, false);
							break;
						}
					}
				}
				CMLib.commands().forceStandardCommand(mob, "Yell", new XVector<String>("YELL",L("ABANDON SHIP!")));
				{
					final int maxCrew = adjustedLevel(mob,asLevel) /2;
					final int maxPaddle = adjustedLevel(mob, asLevel) /3;
					final List<MOB> crew=new LinkedList<MOB>();
					final List<Item> items=new LinkedList<Item>();
					final List<Item> boats=new LinkedList<Item>();
					crew.add(mob);
					for(final Enumeration<Room> r=myShipArea.getProperMap();r.hasMoreElements();)
					{
						final Room R1=r.nextElement();
						if(R1==null)
							continue;
						for(final Enumeration<Item> i=R1.items();i.hasMoreElements();)
						{
							final Item I=i.nextElement();
							if((I instanceof Rideable)
							&&(((Rideable)I).rideBasis()==Rideable.RIDEABLE_WATER)
							&&(((Rideable)I).mobileRideBasis())
							&&(CMLib.flags().isGettable(I)))
								boats.add(I);
						}
					}
					for(final Enumeration<Room> r=myShipArea.getProperMap();r.hasMoreElements();)
					{
						final Room R1=r.nextElement();
						if(R1==null)
							continue;
						for(final Enumeration<MOB> m=R1.inhabitants();m.hasMoreElements();)
						{
							final MOB M=m.nextElement();
							if(M==null)
								continue;
							if(CMLib.flags().isAliveAwakeMobileUnbound(M, true))
								crew.add(mob);
						}
						for(final Enumeration<Item> i=R1.items();i.hasMoreElements();)
						{
							final Item I=i.nextElement();
							if(I == null)
								continue;
							if((CMLib.flags().isGettable(I))
							&&(!boats.contains(I)))
								items.add(I);
						}
					}
					crew.remove(mob);
					Collections.sort(crew, new Comparator<MOB>()
					{
						@Override
						public int compare(final MOB arg0, final MOB arg1)
						{
							if(arg0.isPlayer())
								return arg1.isPlayer()?0:-1;
							return arg1.isPlayer()?1:0;
						}
					});
					Collections.sort(items, new Comparator<Item>()
					{
						@Override
						public int compare(final Item arg0, final Item arg1)
						{
							if(arg0.phyStats().weight()==arg1.phyStats().weight())
								return 0;
							else
							if(arg0.phyStats().weight()<arg1.phyStats().weight())
								return -1;
							else
								return 1;
						}
					});
					if(boats.size()==0)
					{
						for(int i=0;i<maxCrew;i++)
						{
							if(crew.size()==0)
								break;
							final MOB M=crew.remove(0);
							if(M.location()!=R)
								CMLib.tracking().wanderFromTo(M, R, false);
							if(R.show(mob, M, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> throw(s) <T-NAME> overboard.")))
							{
								if(overboardDir < 0)
									CMLib.tracking().wanderFromTo(M, myShipRoom, false);
								else
									CMLib.tracking().walk(M, overboardDir, M.isInCombat(), false);
							}
						}
						if(R.show(mob, null, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> jump(s) overboard.")))
						{
							if(overboardDir < 0)
								CMLib.tracking().wanderFromTo(mob, myShipRoom, false);
							else
								CMLib.tracking().walk(mob, overboardDir, mob.isInCombat(), false);
						}
					}
					else
					{
						for(int i=0;i<boats.size();i++)
						{
							final Item boat=boats.get(i);
							final Room bR=CMLib.map().roomLocation(boat);
							if(bR!= R)
							{
								if(R.show(mob, boat, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> retrieve(s) <T-NAME>.")))
									R.moveItemTo(boats.get(i));
							}
							if(boat instanceof Container)
							{
								final Container boatC=(Container)boat;
								if(boatC.phyStats().weight()<boatC.capacity())
								{
									while(items.size()>0)
									{
										final Item I=items.get(0);
										if(boatC.phyStats().weight() + I.phyStats().weight() > boatC.capacity())
											break;
										items.remove(0);
										if(R.show(mob, I, boatC, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> load(s) <T-NAME> into <O-NAME>.")))
										{
											R.moveItemTo(I);
											I.setContainer(boatC);
											boatC.recoverPhyStats();
											R.recoverRoomStats();
										}
									}
								}
							}
						}
						for(int i=0;i<maxCrew;i++)
						{
							if(crew.size()==0)
								break;
							Rideable boatC = null;
							for(int b=0;b<boats.size();b++)
							{
								final Rideable bI=(Rideable)boats.get(b);
								if((b==0) && (bI.riderCapacity() > bI.numRiders()+1))
								{
									boatC=bI;
									break;
								}
							}
							if(boatC == null)
								break;
							final MOB M=crew.remove(0);
							if(M.location()!=R)
								CMLib.tracking().wanderFromTo(M, R, false);
							if(R.show(mob, M, boatC, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> load(s) <T-NAME> into <O-NAME>.")))
							{
								R.show(M, boatC, CMMsg.MSG_MOUNT,null);
							}
						}
						for(int b=boats.size()-1;b>0;b--)
						{
							final Rideable boatC=(Rideable)boats.get(b);
							if(R.show(mob, boatC, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> lower(s) <T-NAME>.")))
								myShipRoom.moveItemTo((Item)boatC,Expire.Player_Drop,Move.Followers);
						}
						final Rideable boatC=(Rideable)boats.get(0);
						if(R.show(mob, boatC, boatC, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> jump(s) into <T-NAME>.")))
						{
							R.show(mob, boatC, CMMsg.MSG_MOUNT, null);
						}
						if(R.show(mob, boatC, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> launch(es) <T-NAME>.")))
						{
							myShipRoom.moveItemTo((Item)boatC,Expire.Player_Drop,Move.Followers);
						}
						final TrackingLibrary.TrackingFlags rflags = CMLib.tracking().newFlags()
								.plus(TrackingLibrary.TrackingFlag.NOAIR)
								.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
								.plus(TrackingLibrary.TrackingFlag.WATERSURFACEORSHOREONLY);
						final List<Room> sailingRooms = CMLib.tracking().getRadiantRooms(myShipRoom, rflags, maxPaddle);
						Room curRoom = myShipRoom;
						for(int i=0;i<maxPaddle;i++)
						{
							final int dex=sailingRooms.indexOf(curRoom);
							if(dex < 0)
								break;
							int goDir = -1;
							Room newR = null;
							for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
							{
								if(curRoom == null)
									continue;
								final Room R1=curRoom.getRoomInDir(d);
								final Exit E1=curRoom.getExitInDir(d);
								if((R1!=null)
								&&(E1!=null)
								&&(E1.isOpen()))
								{
									final int xx=sailingRooms.indexOf(R1);
									if(xx > dex)
									{
										goDir = d;
										newR = R1;
										break;
									}
								}
							}
							if(goDir < 0)
								break;
							if(curRoom == null)
								continue;
							curRoom.show(mob, null, CMMsg.MSG_NOISE, L("<S-NAME> order(s) the boats @x1.",CMLib.directions().getDirectionName(goDir)));
							boolean didOne = false;
							for(int b=0;b<boats.size();b++)
							{
								final Rideable boat=(Rideable)boats.get(b);
								if(boat.numRiders()>0)
								{
									MOB M=null;
									for(int r=0;r<boat.numRiders();r++)
									{
										final Rider iR=boat.fetchRider(r);
										if((iR instanceof MOB)
										&&(CMLib.flags().isAliveAwakeMobileUnbound((MOB)iR,true)))
											M=(MOB)iR;
									}
									if(M==null)
										continue;
									CMLib.tracking().walk(M, goDir, M.isInCombat(), false, false);
									if(M.location() == newR)
										didOne = true;
								}
							}
							if(!didOne)
								break;
							curRoom = newR;
						}
						mob.curState().setMovement(0);
					}
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> can't seem to control this dire situation."));

		return success;
	}

}
