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

public class Skill_CrowsNest extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_CrowsNest";
	}

	private final static String	localizedName	= CMLib.lang().L("Crow`s Nest");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ROOMS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	private static final String[]	triggerStrings	= I(new String[] { "CROWSNEST"});

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
		if(affected instanceof Item)
		{
		}
		return true;
	}
	
	@Override
	public boolean okMessage(Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if(((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE))
		&&(affected instanceof Room))
		{
			final Room mobR=(Room)affected;
			final Room downR=mobR.getRoomInDir(Directions.DOWN);
			if((downR!=null)
			&&(downR.getArea() instanceof BoardableShip))
			{
				final Item I=((BoardableShip)downR.getArea()).getShipItem();
				final Room shipR=CMLib.map().roomLocation(I);
				if(msg.target() instanceof Exit)
				{
					final Room shipRR=shipR;
					msg.addTrailerRunnable(new Runnable(){
						final Room shipR=shipRR;
						final MOB mob=msg.source();
						final Exit E=(Exit)msg.target();
						final int targetMinor=msg.targetMinor();
						final int targetCode=msg.targetCode();

						@Override
						public void run()
						{
							try
							{
								int dir=CMLib.map().getExitDir(mobR, E);
								if((dir >=0)&&(shipR!=null)&&(shipR.getRoomInDir(dir)!=null))
								{
									Room R=shipR.getRoomInDir(dir);
									if((R!=null)&&(targetMinor==CMMsg.TYP_EXAMINE)&&(R.getRoomInDir(dir)!=null))
										R=R.getRoomInDir(dir);
									if(R!=null)
									{
										final CMMsg msg2=CMClass.getMsg(mob,R,targetCode,null);
										R.executeMsg(mob,msg2);
										if((targetMinor==CMMsg.TYP_EXAMINE)&&(R.getRoomInDir(dir)!=null))
										{
											for(int dir2 : Directions.CODES())
											{
												if(dir2!=Directions.getOpDirectionCode(dir))
												{
													Room R2=R.getRoomInDir(dir);
													if((R2!=null)
													&&((dir2==dir)
														||(!CMLib.flags().isWateryRoom(R2))
														||(R2.numInhabitants()>0)
														||(R2.numItems()>0)))
													{
														msg2.setTarget(R2);
														R2.executeMsg(msg.source(),msg2);
													}
												}
											}
										}
									}
								}
							}
							catch(Throwable t)
							{
								Log.errOut(t);
							}
						}
					});
				}
				else
				if(msg.target() == mobR)
				{
					final Skill_CrowsNest self=this;
					final MOB mob=msg.source();
					final Room shipRR=shipR;
					msg.addTrailerRunnable(new Runnable(){
						final Skill_CrowsNest selfA=self;
						final Room shipR=shipRR;

						@Override
						public void run()
						{
							try
							{
								final CMMsg msg2=CMClass.getMsg(mob,shipR,CMMsg.MSG_LOOK,null);
								shipR.executeMsg(mob,msg2);
								
								final TrackingFlags flags=CMLib.tracking().newFlags().plus(TrackingFlag.WATERSURFACEONLY);
								int maxRadius=1+(selfA.adjustedLevel(mob, 0)/20)+(selfA.getXLEVELLevel(mob)/2)+selfA.getXMAXRANGELevel(mob);
								final List<Room> Rs=CMLib.tracking().getRadiantRooms(shipR, flags, maxRadius);
								for(Room R2 : Rs)
								{
									if(R2==null)
										continue;
									int landHo=-1;
									for(int dir : Directions.CODES())
									{
										final Room R3=R2.getRoomInDir(dir);
										if((R3!=null)&&(!CMLib.flags().isWateryRoom(R3)))
											landHo=dir;
									}
									if((R2.numInhabitants()>0)||(R2.numItems()>0)||(landHo>=0))
									{
										List<String> listOfStuff=new ArrayList<String>(1);
										for(Enumeration<MOB> m=R2.inhabitants();m.hasMoreElements();)
										{
											final MOB M=m.nextElement();
											if((M!=null)&&(CMLib.flags().canBeSeenBy(M, mob)))
												listOfStuff.add(M.name(mob));
										}
										for(Enumeration<Item> i=R2.items();i.hasMoreElements();)
										{
											final Item I=i.nextElement();
											if((I!=null)
											&&(I.container()==null)
											&&(CMLib.flags().canBeSeenBy(I, mob)))
												listOfStuff.add(I.name(mob));
										}
										final List<Room> trail=CMLib.tracking().findTrailToRoom(shipR, R2, flags, maxRadius+1,Rs);
										if((trail.size()==1)&&(landHo>=0))
										{
											mob.tell(L("Directly @x1, you see Land!.",CMLib.directions().getInDirectionName(landHo)));
										}
										else
										if((trail.size()>1)&&(trail.get(trail.size()-1)==shipR)&&(listOfStuff.size()>0))
										{
											StringBuilder str=new StringBuilder("");
											int dir=CMLib.map().getRoomDir(shipR, trail.get(trail.size()-2));
											if(trail.size()<3)
												str.append(L("Directly @x1, you see @x2.",CMLib.directions().getInDirectionName(dir),CMLib.english().toEnglishStringList(listOfStuff)));
											else
											if(trail.size()==3)
												str.append(L("Farther @x1, you see @x2.",CMLib.directions().getInDirectionName(dir),CMLib.english().toEnglishStringList(listOfStuff)));
											else
												str.append(L("Way off @x1, you can barely see @x2.",CMLib.directions().getInDirectionName(dir),CMLib.english().toEnglishStringList(listOfStuff)));
											mob.tell(str.toString());
										}
									}
								}
							}
							catch(Throwable t)
							{
								Log.errOut(t);
							}
						}
					});
				}
			}
		}
		return true;
	}
	
	@Override
	public void unInvoke()
	{
		final Physical affected=this.affected;
		super.unInvoke();
		if((affected instanceof Room)&&(this.unInvoked))
		{
			final Room R=(Room)affected;
			final Room downR=R.getRoomInDir(Directions.DOWN);
			if((downR!=null)
			&&(R.roomID().length()==0)
			&&(downR.roomID().length()>0)
			&&(downR.getRoomInDir(Directions.UP)==R))
			{
				R.showHappens(CMMsg.MSG_OK_VISUAL, L("You climb down from the Crow`s Nest."));
				CMLib.map().emptyRoom(R, downR, true);
				for(int dir : Directions.CODES())
				{
					final Room airRoom=R.getRoomInDir(dir);
					if(airRoom!=null)
					{
						CMLib.map().emptyRoom(airRoom, downR, true);
						airRoom.destroy();
					}
				}
				downR.rawDoors()[Directions.UP]=null;
				downR.setRawExit(Directions.UP, null);
				R.rawDoors()[Directions.DOWN]=null;
				R.setRawExit(Directions.DOWN, null);
				R.destroy();
				downR.giveASky(0);
			}
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
		&&((R.domainType()&Room.INDOORS)==0)
		&&(((BoardableShip)R.getArea()).getShipItem() instanceof SailingShip)
		&&(R.roomID().length()>0))
		{
			ship=(SailingShip)((BoardableShip)R.getArea()).getShipItem();
		}
		else
		{
			mob.tell(L("You must be on the deck of a big sailing ship to climb into the Crow's Nest!"));
			return false;
		}
		
		final Room oldUpR=R.getRoomInDir(Directions.UP);
		if((oldUpR!=null)&&(oldUpR.roomID().length()>0))
		{
			mob.tell(L("You can not build a Crow's Nest here!"));
			return false;
		}
		
		for(Enumeration<Room> r=R.getArea().getProperMap();r.hasMoreElements();)
		{
			final Room R2=r.nextElement();
			if((R2!=null)&&((R2.domainType()&Room.INDOORS)==0)&&(R.numItems()==0))
			{
				if(R2.fetchEffect(ID())!=null)
				{
					mob.tell(L("There is already a Crow's Nest on the ship."));
					return false;
				}
			}
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final Ability consA=CMClass.getAbility("Prop_ReqCapacity");
			consA.setMiscText("people=1");
			final Room crowsR=CMClass.getLocale("WoodenDeck");
			crowsR.setArea(R.getArea());
			crowsR.addNonUninvokableEffect(consA);
			crowsR.setDisplayText(L("Up in the Crow`s Nest"));
			crowsR.setDescription(L("You are atop the mast in the Crow`s Nest, from which you have a wide view of all of your surroundings."));
			for(int dir : Directions.CODES())
			{
				final Room airRoom=CMClass.getLocale("InTheAir");
				airRoom.setDisplayText("In mid-air above the deck");
				airRoom.setArea(R.getArea());
				airRoom.setRawExit(Directions.DOWN, CMClass.getExit("Open"));
				airRoom.rawDoors()[Directions.DOWN]=R;
				crowsR.rawDoors()[dir]=airRoom;
				crowsR.setRawExit(dir, CMClass.getExit("Open"));
			}
			
			final CMMsg msg=CMClass.getMsg(mob,ship,this,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> start(s) climbing up into the Crow`s Nest!"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(oldUpR!=null)
					R.clearSky();
				R.rawDoors()[Directions.UP]=crowsR;
				crowsR.rawDoors()[Directions.DOWN]=R;
				R.setRawExit(Directions.UP, CMClass.getExit("Open"));
				R.getRawExit(Directions.UP).addNonUninvokableEffect(CMClass.getAbility("Prop_Climbable"));
				crowsR.setRawExit(Directions.DOWN, CMClass.getExit("Open"));
				crowsR.getRawExit(Directions.DOWN).addNonUninvokableEffect(CMClass.getAbility("Prop_Climbable"));
				this.beneficialAffect(mob, crowsR, asLevel, 0);
				crowsR.giveASky(0);
				Ability climbA=mob.fetchAbility("Skill_Climb");
				if(climbA!=null)
					climbA.invoke(mob, new XVector<String>("ABOVE"), null, auto, asLevel);
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to climb up into the Crow`s Nest, but slip(s)."));
		return success;
	}
}
