package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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

@SuppressWarnings("unchecked")
public class Hunting extends CommonSkill
{
	public String ID() { return "Hunting"; }
	public String name(){ return "Hunting";}
	private static final String[] triggerStrings = {"HUNT","HUNTING"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_GATHERINGSKILL;}

	protected MOB found=null;
	protected String foundShortName="";
	public Hunting()
	{
		super();
		displayText="You are hunting...";
		verb="hunting";
	}

	public Room nearByRoom()
	{
		Vector possibilities=new Vector();
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			if(d!=Directions.UP)
			{
				Room room=activityRoom.getRoomInDir(d);
				Exit exit=activityRoom.getExitInDir(d);
				if((room!=null)&&(exit!=null)&&(exit.isOpen()))
					possibilities.addElement(Integer.valueOf(d));
			}
		}
		if(possibilities.size()>0)
		{
			int dir=((Integer)possibilities.elementAt(CMLib.dice().roll(1,possibilities.size(),-1))).intValue();
			return activityRoom.getRoomInDir(dir);
		}
		return null;
	}

	public void moveFound()
	{
		if(found.location()==null) return;

		Vector possibilities=new Vector();
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			if(d!=Directions.UP)
			{
				Room room=found.location().getRoomInDir(d);
				Exit exit=found.location().getExitInDir(d);
				if((room!=null)&&(exit!=null)&&(exit.isOpen()))
					possibilities.addElement(Integer.valueOf(d));
			}
		}
		if(possibilities.size()>0)
		{
			int dir=((Integer)possibilities.elementAt(CMLib.dice().roll(1,possibilities.size(),-1))).intValue();
			CMLib.tracking().move(found,dir,true,false);
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			MOB mob=(MOB)affected;
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
			&&(CMLib.flags().aliveAwakeMobile(found,true))
			&&(!found.isInCombat()))
			{
				if(found.location()==mob.location())
				{
					if((mob.isMonster())
					&&(CMLib.flags().aliveAwakeMobile(mob,true))
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
					commonTell(mob,"You have found some "+foundShortName+" tracks!");
					commonTell(mob,"You need to find the "+foundShortName+" nearby before the trail goes cold!");
					displayText="You are hunting for "+found.name();
					verb="hunting for "+found.name();
					found.bringToLife(nearByRoom(),true);
					CMLib.beanCounter().clearZeroMoney(found,null);
				}
				else
				{
					StringBuffer str=new StringBuffer("You can't seem to find any game around here.\n\r");
					int d=lookingFor(RawMaterial.MATERIAL_FLESH,mob.location());
					if(d<0)
						str.append("You might try elsewhere.");
					else
						str.append("You might try "+Directions.getInDirectionName(d)+".");
					commonTell(mob,str.toString());
					unInvoke();
				}

			}
			else
			if(mob.isMonster()
			&&(CMLib.dice().rollPercentage()>50)
			&&(CMLib.flags().isMobile(mob))
			&&(CMLib.flags().aliveAwakeMobile(mob,true))
			&&(CMLib.flags().canSenseMoving(found,mob)))
			{
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					Room R=mob.location().getRoomInDir(d);
					if((R!=null)&&(R==found.location()))
					{ CMLib.tracking().move(mob,d,false,false); break;}
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if((found!=null)&&(!found.amDead())&&(found.location()!=null)&&(!found.isInCombat()))
				{
					if(found.location()==mob.location())
						moveFound();
					found.location().delInhabitant(found);
					found.setLocation(null);
					found.destroy();
					mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> <S-HAS-HAVE> lost the trail.");
				}
			}
		}
		super.unInvoke();
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		verb="hunting";
		found=null;
		activityRoom=null;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		int resourceType=mob.location().myResource();
		if((proficiencyCheck(mob,0,auto))
		   &&(nearByRoom()!=null)
		   &&(resourceType!=RawMaterial.RESOURCE_FISH)
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
			found=(MOB)CMLib.materials().makeResource(resourceType,Integer.toString(mob.location().domainType()),false,null);
			foundShortName="nothing";
			if(found!=null)
			{
				foundShortName=found.name();
				int x=0;
				if((x=foundShortName.lastIndexOf(" "))>=0)
					foundShortName=foundShortName.substring(x).trim().toLowerCase();
				found.setLocation(null);
			}
		}
		int duration=10+mob.envStats().level()+(super.getXTIMELevel(mob)*2);
		CMMsg msg=CMClass.getMsg(mob,found,this,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> start(s) hunting.");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			found=(MOB)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
