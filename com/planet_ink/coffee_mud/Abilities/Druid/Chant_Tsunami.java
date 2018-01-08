package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
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

public class Chant_Tsunami extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_Tsunami";
	}

	private final static String	localizedName	= CMLib.lang().L("Tsunami");

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

	protected MOB gatherHighestLevels(final MOB mob, final Room R, final Set<MOB> grp, final int[] highestLevels, final MOB[] highestLevelM)
	{
		if(R!=null && (R.numInhabitants() >0))
		{
			for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
			{
				final MOB M=m.nextElement();
				if((M!=null)&&(!grp.contains(M))&&(mob.mayIFight(M)))
				{
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

	protected MOB gatherHighestLevels(final MOB mob, final Area A, final Set<MOB> grp, final int[] highestLevels, final MOB[] highestLevelM)
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
	
	protected MOB getHighestLevel(MOB casterM, List<Room> targets)
	{
		// pc=0, npc=1
		final int[] highestLevels=new int[]{0,0};
		final MOB[] highestLevelM=new MOB[]{null, null};
		final Set<MOB> grp = casterM.getGroupMembers(new HashSet<MOB>());
		for(Room R : targets)
		{
			gatherHighestLevels(casterM,R,grp,highestLevels,highestLevelM);
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

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room mobR=mob.location();
		if(mobR==null)
			return false;
		if(((mobR.domainType()&Room.INDOORS)>0)||(mobR.getArea() instanceof BoardableShip))
		{
			mob.tell(L("You must be on or near the shore for this chant to work."));
			return false;
		}
		
		List<Room> targetRooms = new ArrayList<Room>();
		int waterDir = this.getWaterRoomDir(mobR);
		if(waterDir < 0)
		{
			mob.tell(L("You must be on or near the shore for this chant to work."));
			return false;
		}
		String fromDir = CMLib.directions().getFromCompassDirectionName(waterDir);
		
		//targetRooms.add(mobR);
		TrackingLibrary.TrackingFlags flags=CMLib.tracking().newFlags().plus(TrackingFlag.NOAIR).plus(TrackingFlag.OPENONLY).plus(TrackingFlag.NOWATER);
		targetRooms.addAll(CMLib.tracking().getRadiantRooms(mobR, flags, 2));
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			MOB M=this.getHighestLevel(mob, targetRooms);
			if(M!=null)
			{
				int chanceToFail = M.charStats().getSave(CharStats.STAT_SAVE_JUSTICE);
				if (chanceToFail > Integer.MIN_VALUE)
				{
					final int diff = (M.phyStats().level() - mob.phyStats().level());
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
		
		Physical target=mobR;
		
		if(success)
		{
			int newAtmosphere = RawMaterial.RESOURCE_SALTWATER;
			if((mobR.getAtmosphere()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID)
				newAtmosphere = mobR.getAtmosphere();
			for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
			{
				Room R=mobR.getRoomInDir(d);
				if(R!=null)
				{
					if((R.getAtmosphere()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID)
					{
						newAtmosphere =R.getAtmosphere();
						break;
					}
					R=R.getRoomInDir(d);
					if(R!=null)
					{
						if((R.getAtmosphere()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID)
						{
							newAtmosphere =R.getAtmosphere();
							break;
						}
					}
				}
			}
			
			final Set<MOB> casterGroup=mob.getGroupMembers(new HashSet<MOB>());
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L(""):L("^S<S-NAME> chant(s), calling in a tsunami from @x1.^?",fromDir));
			if(mobR.okMessage(mob,msg))
			{
				mobR.send(mob, msg);
				
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
					R2.showHappens(CMMsg.MSG_OK_ACTION, L("^HA massive Tsunami rushes in, flooding the whole area!"));
					for(final Enumeration<MOB> m=R2.inhabitants();m.hasMoreElements();)
					{
						final MOB M=m.nextElement();
						if((M!=null) && mob.mayIFight(M) && (!casterGroup.contains(M)))
						{
							msg2.setTarget(M);
							msg2.setValue(0);
							msg3.setTarget(M);
							msg3.setValue(0);
							if(((R2==mobR)||(R2.okMessage(mob,msg2)))
							&&((R2.okMessage(mob,msg3))))
							{
								if(R2!=mobR)
									R2.send(mob,msg2);
								R2.send(mob,msg3);
								if((msg2.value()<=0)&&(msg3.value()<=0))
								{
									final int harming=CMLib.dice().roll(1,adjustedLevel(mob,asLevel)/numEnemies,numEnemies);
									String msgStr = L("^SA tsunami from @x1 pummels <T-NAME>.^?",fromDir);
									CMLib.combat().postDamage(mob,M,this,harming,CMMsg.MASK_ALWAYS|CMMsg.TYP_WATER,Weapon.TYPE_BURSTING,msgStr);
								}
							}
						}
					}
					Chant A=(Chant)CMClass.getAbility("Chant_Flood");
					if(A!=null)
					{
						if(R2.fetchEffect(A.ID())==null)
						{
							int oldAtmo=R2.getAtmosphereCode();
							Chant A1=(Chant)A.maliciousAffect(mob,R2,asLevel,0,-1);
							if(A1!=null)
							{
								A1.setTickDownRemaining(A1.getTickDownRemaining()/2);
								A1.setMiscText("ATMOSPHERE="+oldAtmo);
								R2.setAtmosphere(newAtmosphere);
							}
						}
					}
				}
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s) towards the waves, but nothing happens."));

		// return whether it worked
		return success;
	}
}
