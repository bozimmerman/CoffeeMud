package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
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

public class Chant_TidalWave extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_TidalWave";
	}

	private final static String	localizedName	= CMLib.lang().L("Tidal Wave");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_WATERCONTROL;
	}

	@Override
	protected int overrideMana()
	{
		return Ability.COST_PCT + 50;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_MOVING;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_ITEMS | Ability.CAN_ROOMS;
	}

	protected MOB gatherHighestLevels(final MOB mob, final Room R, final Set<MOB> grp, final int[] highestLevels, final MOB[] highestLevelM) throws CMException
	{
		if(R!=null && (R.numInhabitants() >0))
		{
			for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
			{
				final MOB M=m.nextElement();
				if((M!=null)&&(!grp.contains(M)))
				{
					if(!mob.mayIFight(M))
						throw new CMException("Not permitted.");
					if(M.isMonster() && (M.phyStats().level() > highestLevels[1]))
					{
						highestLevels[1] = M.phyStats().level();
						highestLevelM[1] = M;
					}
					
					if(M.isPlayer() && (M.phyStats().level() > highestLevels[0]))
					{
						highestLevels[0] = M.phyStats().level();
						highestLevelM[0] = M;
					}
				}
			}
		}
		if(highestLevels[0] > 0)
			return highestLevelM[0];
		return highestLevelM[1];
	}

	protected MOB gatherHighestLevels(final MOB mob, final Area A, final Set<MOB> grp, final int[] highestLevels, final MOB[] highestLevelM) throws CMException
	{
		if(A!=null)
		{
			for(final Enumeration<Room> r=A.getFilledProperMap();r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				gatherHighestLevels(mob,R,grp,highestLevels,highestLevelM);
			}
		}
		if(highestLevels[0] > 0)
			return highestLevelM[0];
		return highestLevelM[1];
	}
	
	protected MOB gatherHighestLevels(final MOB mob, final Rideable I, final Set<MOB> grp, final int[] highestLevels, final MOB[] highestLevelM) throws CMException
	{
		if(I!=null)
		{
			for(final Enumeration<Rider> r=I.riders();r.hasMoreElements();)
			{
				final Rider R=r.nextElement();
				if((R instanceof MOB)&&(!grp.contains(R)))
				{
					final MOB M=(MOB)R;
					if(!mob.mayIFight(M))
						throw new CMException("Not permitted.");
					if(M.isMonster() && (M.phyStats().level() > highestLevels[1]))
					{
						highestLevels[1] = M.phyStats().level();
						highestLevelM[1] = M;
					}
					
					if(M.isPlayer() && (M.phyStats().level() > highestLevels[0]))
					{
						highestLevels[0] = M.phyStats().level();
						highestLevelM[0] = M;
					}
						
				}
			}
		}
		if(highestLevels[0] > 0)
			return highestLevelM[0];
		return highestLevelM[1];
	}
	
	protected MOB getHighestLevel(MOB casterM, Physical target) throws CMException
	{
		// pc=0, npc=1
		final int[] highestLevels=new int[]{0,0};
		final MOB[] highestLevelM=new MOB[]{null, null};
		final Set<MOB> grp = casterM.getGroupMembers(new HashSet<MOB>());
		if(target instanceof Rideable)
		{
			gatherHighestLevels(casterM,(Rideable)target,grp,highestLevels,highestLevelM);
		}
		if(target instanceof BoardableShip)
		{
			final Area A=((BoardableShip)target).getShipArea();
			gatherHighestLevels(casterM,A,grp,highestLevels,highestLevelM);
		}
		if(target instanceof Room)
		{
			gatherHighestLevels(casterM,(Room)target,grp,highestLevels,highestLevelM);
		}
		if(highestLevels[0] > 0)
			return highestLevelM[0];
		return highestLevelM[1];
	}
	
	public int getWaterRoomDir(Room mobR)
	{
		for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
		{
			if((d!=Directions.UP)&&(d!=Directions.DOWN))
			{
				final Room R=mobR.getRoomInDir(d);
				final Exit E=mobR.getExitInDir(d);
				if((R!=null)&&(E!=null)&&(E.isOpen()))
				{
					if(CMLib.flags().isWateryRoom(R))
					{
						return d;
					}
					final Room R2=R.getRoomInDir(d);
					final Exit E2=R.getExitInDir(d);
					if((R2!=null)&&(E2!=null)&&(E2.isOpen()) && (CMLib.flags().isWateryRoom(R2)))
					{
						return d;
					}
				}
			}
		}
		return -1;
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(!(target instanceof BoardableShip))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	protected Room getWashRoom(Physical target, int dir)
	{
		if(target instanceof Room)
		{
			if(CMLib.flags().isWateryRoom((Room)target))
				return (Room)target;
			
		}
		
		if(target instanceof BoardableShip)
		{
			Item I=((BoardableShip)target).getShipItem();
			if(I != null)
				return CMLib.map().roomLocation(target);
		}
		if((target instanceof Item)&&(((Item)target).owner() instanceof Room))
			return (Room)((Item)target).owner();
		return null;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
		if((R.domainType()&Room.INDOORS)>0)
		{
			mob.tell(L("You must be on or near the water for this chant to work."));
			return false;
		}
		if(!CMLib.flags().isWaterySurfaceRoom(R))
		{
			mob.tell(L("This chant does not work here."));
			return false;
		}
		
		List<Room> targetRooms = new ArrayList<Room>();
		Physical target = null;
		Room washRoom = null;
		String washDirection="somewhere";
		List<Integer> possibleWashDirs=null;
		if(commands.size()>0)
		{
			target=getTarget(mob,R,givenTarget,commands,Wearable.FILTER_UNWORNONLY);
			if(target==null)
				return false;
			
			if(target instanceof BoardableShip)
			{ //ok
				if(target instanceof PrivateProperty)
				{
					if(!CMLib.law().canAttackThisProperty(mob, (PrivateProperty)target))
					{
						mob.tell(L("You may not target @x1 with this chant.",target.Name()));
						return false;
					}
				}
				for(Enumeration<Room> r=((BoardableShip)target).getShipArea().getProperMap();r.hasMoreElements();)
				{
					final Room R2=r.nextElement();
					if((R2!=null)&&((R2.domainType()&Room.INDOORS)==0))
						targetRooms.add(R2);
				}
				washRoom = CMLib.map().roomLocation(target);
				washDirection="overboard";
			}
			else
			if((target instanceof Rideable) && (((Rideable)target).rideBasis() == Rideable.RIDEABLE_WATER))
			{ //ok
				targetRooms.add(CMLib.map().roomLocation(target));
				washRoom = CMLib.map().roomLocation(target);
				washDirection="overboard";
			}
			else
			{
				mob.tell(L("That's not a boat!"));
				return false;
			}
			
			if(CMLib.flags().isFlying(target))
			{
				mob.tell(L("This chant would have no effect on @x1!",target.name()));
				return false;
			}
		}
		else
		{
			target = R;
			if(R.getArea() instanceof BoardableShip)
			{ //ok
				if(target instanceof PrivateProperty)
				{
					if(!CMLib.law().canAttackThisProperty(mob, (PrivateProperty)target))
					{
						mob.tell(L("You may not target @x1 with this chant.",target.Name()));
						return false;
					}
				}
				target=((BoardableShip)R.getArea()).getShipItem();
				for(Enumeration<Room> r=((BoardableShip)target).getShipArea().getProperMap();r.hasMoreElements();)
				{
					final Room R2=r.nextElement();
					if((R2!=null)&&((R2.domainType()&Room.INDOORS)==0))
						targetRooms.add(R2);
				}
				washRoom = CMLib.map().roomLocation(target);
				washDirection="overboard";
			}
			else
			{
				targetRooms.add(R);
				possibleWashDirs=new ArrayList<Integer>(4);
				for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
				{
					final Room destRoom=R.getRoomInDir(d);
					final Exit exitRoom=R.getExitInDir(d);
					if((destRoom!=null)
					&&(exitRoom!=null)
					&&(d!=Directions.UP)
					&&(d!=Directions.DOWN)
					&&(exitRoom.isOpen()))
					{
						possibleWashDirs.add(Integer.valueOf(d));
					}
				}
			}
		}
		
		// above will give us a target, and target rooms
		
		// now below will discover where the wave comes from.
		
		String fromDir;
		int waterDir = -1;
		if(CMLib.flags().isWateryRoom(R))
			fromDir="right here";
		else
		{
			if(R.getArea() instanceof BoardableShip)
			{
				if((R.domainType()&Room.INDOORS)==0)
				{
					Item I=((BoardableShip)R.getArea()).getShipItem();
					if((I!=null)&&(I.owner() instanceof Room))
					{
						Room R2=(Room)I.owner();
						if(CMLib.flags().isWateryRoom(R2))
							waterDir = CMLib.dice().roll(1, 4, -1);
						else
							waterDir = getWaterRoomDir(R2);
					}
				}
			}
			else
				waterDir = getWaterRoomDir(R);
			if(waterDir < 0)
			{
				mob.tell(L("There is no water nearby to call in a tidal wave from."));
				return false;
			}
			fromDir=CMLib.directions().getFromCompassDirectionName(waterDir);
		}
		
		if((waterDir >=0)&&(possibleWashDirs!=null))
		{
			possibleWashDirs.remove(Integer.valueOf(waterDir));
			int opWaterDir=Directions.getOpDirectionCode(waterDir);
			if(possibleWashDirs.contains(Integer.valueOf(opWaterDir)))
			{
				washRoom = R.getRoomInDir(opWaterDir);
				washDirection=CMLib.directions().getDirectionName(opWaterDir);
			}
			else
			if(possibleWashDirs.size()>0)
			{
				int washDirDir=possibleWashDirs.get(CMLib.dice().roll(1, possibleWashDirs.size(), -1)).intValue();
				washRoom = R.getRoomInDir(washDirDir);
				washDirection=CMLib.directions().getDirectionName(washDirDir);
			}
		}
		
		MOB highM;
		try
		{
			highM = this.getHighestLevel(mob, target);
		}
		catch (CMException e)
		{
			mob.tell(L("You are not permitted to target @x1!",target.name()));
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			if(highM!=null)
			{
				int chanceToFail = highM.charStats().getSave(CharStats.STAT_SAVE_JUSTICE);
				if (chanceToFail > Integer.MIN_VALUE)
				{
					final int diff = (highM.phyStats().level() - mob.phyStats().level());
					final int diffSign = diff < 0 ? -1 : 1;
					chanceToFail += (diffSign * (diff * diff));
					if (chanceToFail < 5)
						chanceToFail = 5;
					else
					if (chanceToFail > 95)
						chanceToFail = 95;
		
					if (CMLib.dice().rollPercentage() < chanceToFail)
					{
						success = false;
					}
				}
			}
		}
		
		if(success)
		{
			final Set<MOB> casterGroup=mob.getGroupMembers(new HashSet<MOB>());
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L(""):L("^S<S-NAME> chant(s), calling in a tidal wave from @x1.^?",fromDir));
			if(R.okMessage(mob,msg))
			{
				R.send(mob, msg);
				final CMMsg msg2=CMClass.getMsg(mob,null,this,verbalCastCode(mob,target,auto),null);
				final CMMsg msg3=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_WATER|(auto?CMMsg.MASK_ALWAYS:0),null);
				int numEnemies = 0;
				for(final Room R2 : targetRooms)
				{
					for(final Enumeration<MOB> m=R2.inhabitants();m.hasMoreElements();)
					{
						final MOB M=m.nextElement();
						if((M!=null) && mob.mayIFight(M) && (!casterGroup.contains(M)))
							numEnemies++;
					}
				}
				if(numEnemies == 0)
					numEnemies=1;
				for(final Room R2 : targetRooms)
				{
					for(final Enumeration<MOB> m=R2.inhabitants();m.hasMoreElements();)
					{
						final MOB M=m.nextElement();
						if((M!=null) && mob.mayIFight(M) && (!casterGroup.contains(M)))
						{
							msg2.setTarget(M);
							msg2.setValue(0);
							msg3.setTarget(M);
							msg3.setValue(0);
							if(((R2==R)||(R2.okMessage(mob,msg2)))
							&&((R2.okMessage(mob,msg3))))
							{
								if(R2!=R)
									R2.send(mob,msg2);
								R2.send(mob,msg3);
								if((msg2.value()<=0)&&(msg3.value()<=0))
								{
									final int harming=CMLib.dice().roll(1,adjustedLevel(mob,asLevel)/numEnemies,numEnemies);
									String msgStr;
									if(washDirection != null)
										msgStr = L("^SA tidal wave from @x1 pummels <T-NAME>, washing <T-HIM-HER> @x2.^?",fromDir,washDirection);
									else
										msgStr = L("^SA tidal wave from @x1 pummels <T-NAME>.^?",fromDir);
									CMLib.combat().postDamage(mob,M,this,harming,CMMsg.MASK_ALWAYS|CMMsg.TYP_WATER,Weapon.TYPE_BURSTING,msgStr);
									if((washDirection != null)&&(washRoom != null))
									{
										final int chanceToStay=10+(M.charStats().getStat(CharStats.STAT_DEXTERITY)-(2*getXLEVELLevel(mob)));
										final int roll=CMLib.dice().rollPercentage();
										if((roll!=1)&&(roll>chanceToStay))
										{
											int washDir = CMLib.map().getRoomDir(R2, washRoom);
											if(washDir >=0)
												CMLib.tracking().walk(M,washDir,true,false);
											if(!washRoom.isInhabitant(M))
												CMLib.tracking().walkForced(M, R2, washRoom, false, true, L("<S-NAME> washes in."));
											if((!R2.isInhabitant(M))&&(M.isMonster()))
												CMLib.tracking().markToWanderHomeLater(M);
										}
									}
								}
							}
						}
					}
				}
				R.recoverRoomStats();
				R.show(mob,target,CMMsg.MSG_OK_ACTION,L("<T-NAME> momentarily capsizes!"));
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s) at <T-NAMESELF>, but nothing happens."));

		// return whether it worked
		return success;
	}
}
