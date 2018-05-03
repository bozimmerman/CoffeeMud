package com.planet_ink.coffee_mud.Abilities.Common;
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

public class Hunting extends CommonSkill
{
	@Override
	public String ID()
	{
		return "Hunting";
	}

	private final static String localizedName = CMLib.lang().L("Hunting");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"HUNT","HUNTING"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_GATHERINGSKILL;
	}

	protected MOB found=null;
	protected String foundShortName="";
	public Hunting()
	{
		super();
		displayText=L("You are hunting...");
		verb=L("hunting");
	}

	public Room nearByRoom()
	{
		final Vector<Integer> possibilities=new Vector<Integer>();
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			if(d!=Directions.UP)
			{
				final Room room=activityRoom.getRoomInDir(d);
				final Exit exit=activityRoom.getExitInDir(d);
				if((room!=null)&&(exit!=null)&&(exit.isOpen()))
					possibilities.addElement(Integer.valueOf(d));
			}
		}
		if(possibilities.size()>0)
		{
			final int dir=possibilities.elementAt(CMLib.dice().roll(1,possibilities.size(),-1)).intValue();
			return activityRoom.getRoomInDir(dir);
		}
		return null;
	}

	public void moveFound()
	{
		if(found.location()==null)
			return;

		final Vector<Integer> possibilities=new Vector<Integer>();
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			if(d!=Directions.UP)
			{
				final Room room=found.location().getRoomInDir(d);
				final Exit exit=found.location().getExitInDir(d);
				if((room!=null)&&(exit!=null)&&(exit.isOpen()))
					possibilities.addElement(Integer.valueOf(d));
			}
		}
		if(possibilities.size()>0)
		{
			final int dir=possibilities.elementAt(CMLib.dice().roll(1,possibilities.size(),-1)).intValue();
			CMLib.tracking().walk(found,dir,true,false);
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			activityRoom=mob.location();
			if((found!=null)&&(found.amDead()))
			{
				found.setLocation(null);
				found.destroy();
				unInvoke();
			}
			else
			if((found!=null)
			&&(found.location()!=null)
			&&(CMLib.flags().isAliveAwakeMobile(found,true))
			&&(!found.isInCombat()))
			{
				if(found.location()==mob.location())
				{
					if((mob.isMonster())
					&&(CMLib.flags().isAliveAwakeMobile(mob,true))
					&&(CMLib.flags().canBeSeenBy(found,mob))
					&&(!mob.isInCombat()))
						CMLib.combat().postAttack(mob,found,mob.fetchWieldedItem());
					else
						moveFound();
				}
			}

			if(tickUp==0)
			{
				if(found!=null)
				{
					commonTell(mob,L("You have found some @x1 tracks!",foundShortName));
					commonTell(mob,L("You need to find the @x1 nearby before the trail goes cold!",foundShortName));
					displayText=L("You are hunting for @x1",found.name());
					verb=L("hunting for @x1",found.name());
					found.basePhyStats().setLevel(mob.basePhyStats().level());
					found.recoverPhyStats();
					final Ability A=CMClass.getAbility("Prop_ModExperience");
					A.setMiscText("=20%");
					found.addNonUninvokableEffect(A);
					found.bringToLife(nearByRoom(),true);
					CMLib.beanCounter().clearZeroMoney(found,null);
					found.setMoneyVariation(0);
				}
				else
				{
					final StringBuffer str=new StringBuffer(L("You can't seem to find any game around here.\n\r"));
					final int d=lookingFor(RawMaterial.MATERIAL_FLESH,mob.location());
					if(d<0)
						str.append(L("You might try elsewhere."));
					else
						str.append(L("You might try @x1.",CMLib.directions().getInDirectionName(d)));
					commonTell(mob,str.toString());
					unInvoke();
				}

			}
			else
			if(mob.isMonster()
			&&(CMLib.dice().rollPercentage()>50)
			&&(CMLib.flags().isMobile(mob))
			&&(CMLib.flags().isAliveAwakeMobile(mob,true))
			&&(CMLib.flags().canSenseEnteringLeaving(found,mob)))
			{
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					final Room R=mob.location().getRoomInDir(d);
					if((R!=null)&&(R==found.location()))
					{
						CMLib.tracking().walk(mob,d,false,false);
						break;
					}
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				if((found!=null)&&(!found.amDead())&&(found.location()!=null)&&(!found.isInCombat()))
				{
					if(found.location()==mob.location())
						moveFound();
					found.location().delInhabitant(found);
					found.setLocation(null);
					found.destroy();
					mob.location().show(mob,null,getActivityMessageType(),L("<S-NAME> <S-HAS-HAVE> lost the trail."));
				}
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		verb=L("hunting");
		found=null;
		activityRoom=null;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		final int resourceType=mob.location().myResource();
		if((proficiencyCheck(mob,0,auto))
		&&(nearByRoom()!=null)
		&&(!CMParms.contains(RawMaterial.CODES.FISHES(), resourceType))
		&&(((resourceType&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_FLESH)
		   ||(resourceType==RawMaterial.RESOURCE_BLOOD)
		   ||(resourceType==RawMaterial.RESOURCE_BONE)
		   ||(resourceType==RawMaterial.RESOURCE_EGGS)
		   ||(resourceType==RawMaterial.RESOURCE_FEATHERS)
		   ||(resourceType==RawMaterial.RESOURCE_FUR)
		   ||(resourceType==RawMaterial.RESOURCE_HIDE)
		   ||(resourceType==RawMaterial.RESOURCE_MILK)
		   ||(resourceType==RawMaterial.RESOURCE_SCALES)
		   ||(resourceType==RawMaterial.RESOURCE_WOOL)
		   ||((resourceType&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LEATHER)))
		{
			PhysicalAgent E=CMLib.materials().makeResource(resourceType,Integer.toString(mob.location().domainType()),false,null);
			if(!(E instanceof MOB))
			{
				Log.errOut("Hunting","Failed to convert resource "+resourceType+" to mob.");
				return false;
			}
			found=(MOB)E;
			foundShortName="nothing";
			if(found!=null)
			{
				foundShortName=found.name();
				int x=0;
				if((x=foundShortName.lastIndexOf(' '))>=0)
					foundShortName=foundShortName.substring(x).trim().toLowerCase();
				found.setLocation(null);
			}
		}
		final int duration=10+mob.phyStats().level()+(super.getXTIMELevel(mob)*2);
		final CMMsg msg=CMClass.getMsg(mob,found,this,getActivityMessageType(),L("<S-NAME> start(s) hunting."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			found=(MOB)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
