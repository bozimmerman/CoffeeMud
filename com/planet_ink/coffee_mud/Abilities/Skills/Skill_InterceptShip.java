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

public class Skill_InterceptShip extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_InterceptShip";
	}

	private final static String	localizedName	= CMLib.lang().L("Intercept Ship");

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

	private static final String[]	triggerStrings	= I(new String[] { "INTERCEPT","INTERCEPTSHIP" });

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
	public int usageType()
	{
		return USAGE_MOVEMENT | USAGE_MANA;
	}

	protected volatile Room targetRoom = null;
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if((invoker!=null)&&(invoker.isInCombat()))
		{
			unInvoke();
			return false;
		}
		final Room R=CMLib.map().roomLocation(affected);
		if(R == targetRoom)
		{
			unInvoke();
			return false;
		}
		if((tickID==Tickable.TICKID_MOB)
		&&(affected instanceof MOB))
		{
			final MOB mob=(MOB)affected;
			if((mob.riding() !=null) && (mob.riding().rideBasis() == Rideable.RIDEABLE_WATER))
			{
				if(mob.isInCombat())
				{
					unInvoke();
					return false;
				}
				
			}
			else
			{
				unInvoke();
				return false;
			}
		}
		else
		if(affected instanceof SailingShip)
		{
			final SailingShip ship=(SailingShip)affected;
			if(ship.isInCombat())
			{
				unInvoke();
				return false;
			}
		}
		return true;
	}

	@Override
	protected int overrideMana()
	{
		return Ability.COST_PCT+50;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
		int bonus=super.getXLEVELLevel(invoker) / 3;
		if(this.affected instanceof Item)
		{
			affectableStats.setAbility(affectableStats.ability()+1+bonus);
		}
		else
		if(this.affected instanceof MOB)
		{
			affectableStats.setSpeed(affectableStats.speed()+1.0+bonus);
		}
	}

	@Override
	public void unInvoke()
	{
		super.unInvoke();
	}
	
	@Override
	public boolean invoke(final MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
		Room currentR=null;
		Rideable myShip=null;
		if((R.getArea() instanceof BoardableShip)
		&&(((BoardableShip)R.getArea()).getShipItem() instanceof SailingShip))
		{
			SailingShip sailShip=(SailingShip)((BoardableShip)R.getArea()).getShipItem();
			myShip=sailShip;
			currentR=CMLib.map().roomLocation(myShip);
			if(currentR!=null)
			{
				if(sailShip.isInCombat())
				{
					mob.tell(L("Your ship must not be in combat to move to intercept speeds!"));
					return false;
				}
				
				if(sailShip.isAnchorDown())
				{
					mob.tell(L("You should probably raise anchor first."));
					return false;
				}
			}
		}
		else
		if((mob.riding() !=null) && (mob.riding().rideBasis() == Rideable.RIDEABLE_WATER))
		{
			myShip=mob.riding();
			if(CMLib.flags().isWaterySurfaceRoom(mob.location()))
				currentR=mob.location();
		}
		else
		{
			mob.tell(L("This skill only works on board a ship or boat."));
			return false;
		}

		if(currentR==null)
		{
			mob.tell(L("You can't seem to figure out how to get there from here."));
			return false;
		}
		
		final String parm=CMParms.combine(commands).trim();
		
		Room targetR = null;
		List<Room> trail = null;
		TrackingFlags flags=CMLib.tracking().newFlags().plus(TrackingFlag.NOAIR)
														.plus(TrackingFlag.WATERSURFACEORSHOREONLY);
		final PhysicalAgent[] targetShipI=new PhysicalAgent[1];
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
				case Room.DOMAIN_INDOORS_WATERSURFACE:
				case Room.DOMAIN_OUTDOORS_WATERSURFACE:
				{
					final Item I=R.findItem(null,parm);
					if((I instanceof Rideable)
					&&((I instanceof BoardableShip)
						||(((Rideable)I).rideBasis()==Rideable.RIDEABLE_WATER))
					&&(!CMLib.flags().isHidden(I)))
					{
						targetShipI[0]=I;
						return false;
					}
					return true;
				}
				default:
					return true;
				}
			}
		};
		trail = CMLib.tracking().findTrailToAnyRoom(currentR, destFilter, flags, 100);
		if((trail!=null)&&(trail.size()>0))
			targetR=trail.get(0);
		
		if((targetR==null)||(trail==null)||(trail.size()==0))
		{
			mob.tell(L("Your contacts and charts tell you nothing about where '@x1' might be .",parm));
			return false;
		}
		
		if(!mob.mayIFight(targetShipI[0]))
		{
			mob.tell(L("You may only intercept a potential enemy ship, which '@x1' is not .",targetShipI[0].Name()));
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		
		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final String str=L("<S-NAME> consult(s) <S-HIS-HER> sea charts and rig(s) @x1 to intercept <T-NAME>.",myShip.Name());
			final CMMsg msg=CMClass.getMsg(mob,targetShipI[0],this,CMMsg.MSG_DELICATE_HANDS_ACT,str);
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				StringBuilder dirs=new StringBuilder("");
				StringBuilder courseStr=new StringBuilder("");
				Room room=trail.get(trail.size()-1);
				List<String> cmds=new XVector<String>("GO");
				for(int i=trail.size()-2;i>=0;i--)
				{
					Room nextRoom=trail.get(i);
					int dir=CMLib.map().getRoomDir(room, nextRoom);
					if(dir >= 0)
					{
						dirs.append(CMLib.directions().getDirectionName(dir));
						courseStr.append(CMLib.directions().getDirectionName(dir));
						cmds.add(CMLib.directions().getDirectionName(dir));
						if(i>0)
						{
							dirs.append(", ");
							courseStr.append(" ");
						}
					}
					room=nextRoom;
				}
				final String msgStr=L("Your charts say the way there is: @x1",dirs.toString());
				if(myShip instanceof BoardableShip)
				{
					String courseMsgStr="COURSE "+courseStr.toString();
					final CMMsg huhMsg=CMClass.getMsg(mob,null,null,CMMsg.MSG_HUH,msgStr,courseMsgStr,null);
					if(R.okMessage(mob,huhMsg))
						R.send(mob,huhMsg);
					final Skill_InterceptShip A = (Skill_InterceptShip)beneficialAffect(mob,myShip,asLevel,trail.size()/2);
					A.targetRoom = targetR;
				}
				else
				if((mob.riding() !=null) && (mob.riding().rideBasis() == Rideable.RIDEABLE_WATER))
				{
					final Skill_InterceptShip A = (Skill_InterceptShip)beneficialAffect(mob,mob,asLevel,trail.size()/2);
					A.targetRoom = targetR;
					mob.enqueCommand(commands, MUDCmdProcessor.METAFLAG_FORCED, 0);
					//mob.tell(msgStr);
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,targetShipI[0],L("<S-NAME> consult(s) <S-HIS-HER> sea charts, but can't seem to figure out how to find <T-NAME>."));

		return success;
	}

}
