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
public class AnimalTraining extends CommonSkill
{
	public String ID() { return "AnimalTraining"; }
	public String name(){ return "Animal Training";}
	private static final String[] triggerStrings = {"ANIMALTRAINING"};
	public String[] triggerStrings(){return triggerStrings;}
    public int classificationCode() {   return Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_ANIMALAFFINITY; }

	protected Environmental taming=null;
	protected String skillto="";
    protected Object skill=null;
	protected boolean messedUp=false;
	public AnimalTraining()
	{
		super();
		displayText="You are taming...";
		verb="taming";
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(tickID==Tickable.TICKID_MOB))
		{
			MOB mob=(MOB)affected;
			if((taming==null)||(mob.location()==null))
			{
				messedUp=true;
				unInvoke();
			}
			if((taming instanceof MOB)&&(!mob.location().isInhabitant((MOB)taming)))
			{
				messedUp=true;
				unInvoke();
			}
			if((taming instanceof Item)&&(!mob.location().isContent((Item)taming)))
			{
				messedUp=true;
				unInvoke();
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
				if((taming!=null)&&(!aborted))
				{
					MOB animal=null;
					if(taming instanceof MOB)
						animal=(MOB)taming;
					else
					if((taming!=null)&&(taming instanceof CagedAnimal))
						animal=((CagedAnimal)taming).unCageMe();
					if((messedUp)||(animal==null)||(skill==null))
						commonTell(mob,"You've failed to train "+taming.name()+"!");
					else
					{
						if(animal.numBehaviors()==0)
							commonTell(mob,taming.name()+" is already tame.");
						else
						{
							String s=" to "+skillto;
							mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to train "+animal.name()+" "+s+".");
							if(skill instanceof Behavior)
								animal.addBehavior((Behavior)skill);
							else
							if(skill instanceof Ability)
							{
								((Ability)skill).setProficiency(100);
								animal.addAbility((Ability)skill);
							}
							animal.recoverCharStats();
							animal.recoverEnvStats();
							animal.recoverMaxState();
							if(taming instanceof CagedAnimal)
							{
								animal.text();
								((CagedAnimal)taming).cageMe(animal);
							}
						}
					}
				}
			}
		}
		super.unInvoke();
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		verb="training";
		taming=null;
		Item cage=null;
		String[] skills={"WANDER", //0
						 "HUNT", //1
						 "KILL", //2
						 "DOORGUARD" //3
		};
		String valid="Skills include:";
		for(int i=0;i<skills.length;i++)
			valid+=" "+skills[i];
		valid+=".";
		if(commands.size()<2)
		{
			commonTell(mob,"Train whom to do what? "+valid);
			return false;
		}
		skill=null;
		String what=(String)commands.lastElement();
		commands.removeElementAt(commands.size()-1);
		for(int i=0;i<skills.length;i++)
		{
			if(skills[i].startsWith(what.toUpperCase()))
			{
				switch(i)
				{
				case 0:
					skill=CMClass.getBehavior("Mobile");
					break;
				case 1:
					skill=CMClass.getAbility("Hunt");
					break;
				case 2:
					skill=CMClass.getBehavior("Aggressive");
					break;
				case 3:
					skill=CMClass.getBehavior("DoorwayGuardian");
					break;
				}
				if(skill!=null)
					skillto=skills[i].toLowerCase();
			}
		}
		if(skill==null)
		{
			commonTell(mob,"Train whom to do what? "+valid);
			return false;
		}

		String str=CMParms.combine(commands,0);
		MOB M=mob.location().fetchInhabitant(str);
		taming=null;
		if(M!=null)
		{
			if(!CMLib.flags().canBeSeenBy(M,mob))
			{
				commonTell(mob,"You don't see anyone called '"+str+"' here.");
				return false;
			}
			if((!M.isMonster())||(!CMLib.flags().isAnimalIntelligence(M)))
			{
				commonTell(mob,"You can't train "+M.name()+".");
				return false;
			}
			if((CMLib.flags().canMove(M))&&(!CMLib.flags().isBoundOrHeld(M)))
			{
				commonTell(mob,M.name()+" doesn't seem willing to cooperate.");
				return false;
			}
			taming=M;
		}
		else
		if(mob.location()!=null)
		{
			for(int i=0;i<mob.location().numItems();i++)
			{
				Item I=mob.location().fetchItem(i);
				if((I!=null)
				&&(I instanceof Container)
				&&((((Container)I).containTypes()&Container.CONTAIN_CAGED)==Container.CONTAIN_CAGED))
				{ cage=I; break;}
			}
			if(commands.size()>0)
			{
				String last=(String)commands.lastElement();
				Item I=mob.location().fetchItem(null,last);
				if((I!=null)
				&&(I instanceof Container)
				&&((((Container)I).containTypes()&Container.CONTAIN_CAGED)==Container.CONTAIN_CAGED))
				{
					cage=I;
					commands.removeElement(last);
				}
			}
			if(cage==null)
			{
				commonTell(mob,"You don't see anyone called '"+str+"' here.");
				return false;
			}
			taming=mob.location().fetchItem(cage,CMParms.combine(commands,0));
			if((taming==null)||(!CMLib.flags().canBeSeenBy(taming,mob))||(!(taming instanceof CagedAnimal)))
			{
				commonTell(mob,"You don't see any creatures in "+cage.name()+" called '"+CMParms.combine(commands,0)+"'.");
				return false;
			}
			M=((CagedAnimal)taming).unCageMe();
		}
		else
			return false;

		if(((skill instanceof Behavior)&&(M.fetchBehavior(((Behavior)skill).ID())!=null))
		||(skill instanceof Ability)&&(M.fetchAbility(((Ability)skill).ID())!=null))
		{
			commonTell(mob,M.name()+" already knows how to do that.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		messedUp=!proficiencyCheck(mob,-taming.envStats().level()+(2*getXLEVELLevel(mob)),auto);
		int duration=getDuration(35,mob,taming.envStats().level(),10);
		verb="training "+M.name();
		CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> start(s) training "+M.name()+".");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
