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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2023-2023 Bo Zimmerman

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
public class Skill_MountedLeap extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_MountedLeap";
	}

	private final static String localizedName = CMLib.lang().L("Mounted Leap");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
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

	private static final String[] triggerStrings = I(new String[] { "MLEAP" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_ANIMALAFFINITY;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_FLYING);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		Room R=mob.location();
		if(R==null)
			return false;
		int dirCode=-1;
		Physical target=givenTarget;
		if(commands.size()==0)
		{
			if(mob.isMonster())
			{
				for(int i=0;i<20;i++)
				{
					final int dir = CMLib.dice().roll(1, Directions.NUM_DIRECTIONS(), -1);
					final Room nR = R.getRoomInDir(dir);
					final Exit nE = R.getExitInDir(dir);
					if((nR!=null)&&(nE!=null)&&(nE.isOpen()))
					{
						commands.add(CMLib.directions().getDirectionName(dir));
						break;
					}
				}
			}
			else
			{
				mob.tell(L("Leap what direction?"));
				return false;
			}
		}
		if(mob.isInCombat())
		{
			mob.tell(L("Not while you are fighting!"));
			return false;
		}
		if(target==null)
			dirCode = CMLib.directions().getGoodDirectionCode(CMParms.combine(commands,0));
		final String targetName;
		if(dirCode<0)
		{
			if(target == null)
				target=R.fetchFromRoomFavorExits(CMParms.combine(commands,0));
			if(target instanceof Exit)
				dirCode = CMLib.map().getExitDir(R, (Exit)target);
			if((dirCode<0)&&(target != null))
			{
				if(target instanceof Rideable)
				{
					if(target instanceof Exit) // it's a portal .. so we just assume you can jump "in" it
					{
						targetName=target.name();
					}
					else
					{
						mob.tell(L("You can not jump in '@x1'.",target.name(mob)));
						return false;
					}
				}
				else
				{
					mob.tell(L("You can not jump in '@x1'.",target.name(mob)));
					return false;
				}
			}
			else
			{
				target = null; // it's an ordinary exit
				targetName = CMLib.directions().getDirectionName(dirCode);
			}
		}
		else
			targetName = CMLib.directions().getDirectionName(dirCode);

		if(dirCode<0)
		{
			mob.tell(L("Leap where?"));
			return false;
		}
		else
		{
			final Room nR = R.getRoomInDir(dirCode);
			final Exit nE = R.getExitInDir(dirCode);
			if((nR==null)||(nE==null))
			{
				mob.tell(L("You can't leap that way."));
				return false;
			}
			if(!nE.isOpen())
			{
				mob.tell(L("That way looks blocked."));
				return false;
			}
			if((dirCode==Directions.UP)
			&&(nR.domainType()==Room.DOMAIN_OUTDOORS_AIR)||(nR.domainType()==Room.DOMAIN_INDOORS_AIR))
			{
				mob.tell(L("You can't leap that high."));
				return false;
			}
		}

		final MOB riddenM;
		if((mob instanceof Rideable)
		&&(((Rideable)mob).isMobileRideBasis())
		&&(mob.riding()==null))
		{
			riddenM=null;
			if(CMLib.flags().isSitting(mob)
			||CMLib.flags().isSleeping(mob))
			{
				mob.tell(L("You need to stand up first!"));
				return false;
			}
			if((mob.charStats().getBodyPart(Race.BODY_LEG)==0)
			||(mob.charStats().getBodyPart(Race.BODY_FOOT)==0)
			||CMLib.flags().isFalling(mob))
			{
				mob.tell(L("You can't seem to jump right now."));
				return false;
			}
		}
		else
		if((!CMLib.flags().isMobileMounted(mob))
		||(!(mob.riding() instanceof MOB)))
		{
			mob.tell(L("You need to be mounted to do that."));
			return false;
		}
		else
		{
			riddenM = (MOB)mob.riding();
			if(CMLib.flags().isSitting(riddenM)
			||CMLib.flags().isSleeping(riddenM))
			{
				mob.tell(L("Your mount needs to stand up first!"));
				return false;
			}
			if((riddenM.charStats().getBodyPart(Race.BODY_LEG)==0)
			||(riddenM.charStats().getBodyPart(Race.BODY_FOOT)==0)
			||CMLib.flags().isFalling(riddenM))
			{
				mob.tell(L("Your mount can't seem to leap right now."));
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> leap(s) @x1!",targetName));
		int jumps=1;
		if((target==null)&&(dirCode>=0))
			jumps+=(int)Math.round(super.getXLEVELLevel(mob)/2.5);

		for(int j=0;j<jumps;j++)
		{
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				success=proficiencyCheck(mob,0,auto);

				if((mob.fetchEffect(ID())==null)
				&&(riddenM.fetchEffect(ID())==null))
				{
					final Ability A=(Ability)this.copyOf();
					A.setSavable(false);
					final Ability rA=(Ability)this.copyOf();
					rA.setSavable(false);
					try
					{
						mob.addEffect(A);
						mob.recoverPhyStats();
						if(riddenM != null)
						{
							riddenM.addEffect(rA);
							riddenM.recoverPhyStats();
						}
						if(dirCode>=0)
							CMLib.tracking().walk(mob,dirCode,false,false);
						else
						if(target instanceof Rideable)
							CMLib.commands().forceStandardCommand(mob, "Enter", new XVector<String>("ENTER",R.getContextName(target)));
					}
					finally
					{
						mob.delEffect(A);
						if(riddenM != null)
							riddenM.delEffect(rA);
					}
				}
				else
				{
					if(dirCode>=0)
						CMLib.tracking().walk(mob,dirCode,false,false);
					else
					if(target instanceof Rideable)
						CMLib.commands().forceStandardCommand(mob, "Enter", new XVector<String>("ENTER",R.getContextName(target)));
				}
			}
			if((j<jumps-1)&&(dirCode>=0))
			{
				if((mob.location()==R)
				||(!CMLib.flags().isAiryRoom(mob.location())))
					break;
				R=mob.location();
				if((R.getRoomInDir(dirCode)==null)
				||(R.getExitInDir(dirCode)==null))
					break;
				msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> fl(ys) @x1!",targetName));
			}
		}
		if(riddenM != null)
		{
			riddenM.recoverPhyStats();
			mob.location().executeMsg(riddenM,CMClass.getMsg(riddenM,mob.location(),CMMsg.MASK_MOVE|CMMsg.TYP_GENERAL,null));
		}
		mob.recoverPhyStats();
		mob.location().executeMsg(mob,CMClass.getMsg(mob,mob.location(),CMMsg.MASK_MOVE|CMMsg.TYP_GENERAL,null));
		return success;
	}

}
