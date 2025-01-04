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
   Copyright 2003-2025 Bo Zimmerman

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
public class AnimalTraining extends CommonSkill
{
	@Override
	public String ID()
	{
		return "AnimalTraining";
	}

	private final static String localizedName = CMLib.lang().L("Animal Training");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"ANIMALTRAINING"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_ANIMALAFFINITY;
	}

	protected Physical	 trainingM		= null;
	protected Trainables skillto	= null;
	protected Modifiable skill		= null;
	protected boolean	 messedUp	= false;

	private enum Trainables
	{
		WANDER, //0
		HUNT, //1
		ASSAULT, //2
		DOORGUARD, //3
		KILL,
		SIT,
		SLEEP,
		STAND,
		FLEE,
		GET,
		FOLLOW,
		NOFOLLOW,
		TALK,
		HUSH
	}

	public AnimalTraining()
	{
		super();
		displayText=L("You are taming...");
		verb=L("taming");
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			if((trainingM==null)||(mob.location()==null))
			{
				messedUp=true;
				unInvoke();
			}
			if((trainingM instanceof MOB)&&(!mob.location().isInhabitant((MOB)trainingM)))
			{
				messedUp=true;
				unInvoke();
			}
			if((trainingM instanceof Item)&&(!mob.location().isContent((Item)trainingM)))
			{
				messedUp=true;
				unInvoke();
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
				if((trainingM!=null)&&(!aborted))
				{
					MOB animal=null;
					if(trainingM instanceof MOB)
						animal=(MOB)trainingM;
					else
					if((trainingM!=null)&&(trainingM instanceof CagedAnimal))
						animal=((CagedAnimal)trainingM).unCageMe();
					if((messedUp)||(animal==null)||(skill==null)||(skillto==null))
						commonTelL(mob,"You've failed to train @x1!",trainingM.name());
					else
					{
						final String s=" to "+skillto.name().toLowerCase();
						mob.location().show(mob,null,getActivityMessageType(),L("<S-NAME> manage(s) to train @x1 @x2.",animal.name(),s));
						if(skillto == Trainables.HUSH)
							animal.delBehavior(animal.fetchBehavior(skill.ID()));
						else
						if(skill instanceof Behavior)
							animal.addBehavior((Behavior)skill);
						else
						if(skill instanceof Ability)
						{
							if(skill.ID().equalsIgnoreCase("Prop_LangTranslator"))
							{
								Language lang = CMLib.utensils().getLanguageSpoken(mob);
								if(lang == null)
									lang = (Language)CMClass.getAbility("Common");
								if(animal.fetchEffect(skill.ID())==null)
								{
									animal.addNonUninvokableEffect((Ability)skill);
									((Ability)skill).setMiscText("NOTRANSLATE `^"+skillto+" "+lang.ID());
								}
								else
									skill.setStat("+"+lang.ID(), "`^"+skillto);
								skill.setStat("+TRUSTED", "*");
								skill.setStat("+NOTRANSLATE", "");
							}
							else
							{
								((Ability)skill).setProficiency(100);
								animal.addAbility((Ability)skill);
							}
						}
						animal.recoverCharStats();
						animal.recoverPhyStats();
						animal.recoverMaxState();
						if(trainingM instanceof CagedAnimal)
						{
							animal.text();
							((CagedAnimal)trainingM).cageMe(animal);
						}
					}
				}
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;

		verb=L("training");
		trainingM=null;
		Item cage=null;
		String valid="Skills include:";
		for (final String skill2 : CMParms.toStringArray(Trainables.values()))
			valid+=" "+skill2.toLowerCase();
		valid+=".";
		if(commands.size()<2)
		{
			commonTelL(mob,"Train whom to do what? @x1",valid);
			return false;
		}
		skill=null;
		final String what=commands.get(commands.size()-1);
		commands.remove(commands.size()-1);
		for(final Trainables T : Trainables.values())
		{
			if(T.name().startsWith(what.toUpperCase()))
			{
				skillto=T;
				switch(T)
				{
				case WANDER:
					skill=CMClass.getBehavior("Mobile");
					break;
				case HUNT:
					skill=CMClass.getAbility("Hunting");
					break;
				case ASSAULT:
					skill=CMClass.getBehavior("Aggressive");
					break;
				case DOORGUARD:
					skill=CMClass.getBehavior("DoorwayGuardian");
					break;
				case TALK:
				case HUSH:
					skill=CMClass.getBehavior("MudChat");
					break;
				default:
					skill=CMClass.getAbility("Prop_LangTranslator");
					break;
				}
				break;
			}
		}
		if(skill==null)
		{
			commonTelL(mob,"Train whom to do what? @x1",valid);
			return false;
		}

		final String str=CMParms.combine(commands,0);
		MOB M=getVisibleRoomTarget(mob,str);
		trainingM=null;
		if(M!=null)
		{
			if(!CMLib.flags().canBeSeenBy(M,mob))
			{
				commonTelL(mob,"You don't see anyone called '@x1' here.",str);
				return false;
			}
			if((!M.isMonster())||(!CMLib.flags().isAnAnimal(M)))
			{
				commonTelL(mob,"You can't train @x1.",M.name(mob));
				return false;
			}
			if((CMLib.flags().canMove(M))
			&&(!CMLib.flags().isBoundOrHeld(M)))
			{
				commonTelL(mob,"@x1 doesn't seem willing to cooperate.",M.name(mob));
				return false;
			}
			trainingM=M;
		}
		else
		if(mob.location()!=null)
		{
			for(int i=0;i<mob.location().numItems();i++)
			{
				final Item I=mob.location().getItem(i);
				if((I!=null)
				&&(I instanceof Container)
				&&((((Container)I).containTypes()&Container.CONTAIN_CAGED)==Container.CONTAIN_CAGED))
				{
					cage=I;
					break;
				}
			}
			if(commands.size()>0)
			{
				final String last=commands.get(commands.size()-1);
				final Item I=mob.location().findItem(null,last);
				if((I!=null)
				&&(I instanceof Container)
				&&((((Container)I).containTypes()&Container.CONTAIN_CAGED)==Container.CONTAIN_CAGED))
				{
					cage=I;
					commands.remove(last);
				}
			}
			if(cage==null)
			{
				commonTelL(mob,"You don't see anyone called '@x1' here.",str);
				return false;
			}
			trainingM=mob.location().findItem(cage,CMParms.combine(commands,0));
			if((trainingM==null)||(!CMLib.flags().canBeSeenBy(trainingM,mob))||(!(trainingM instanceof CagedAnimal)))
			{
				commonTelL(mob,"You don't see any creatures in @x1 called '@x2'.",cage.name(),CMParms.combine(commands,0));
				return false;
			}
			M=((CagedAnimal)trainingM).unCageMe();
		}
		else
			return false;

		Language lang = CMLib.utensils().getLanguageSpoken(mob);
		if(lang == null)
			lang = (Language)CMClass.getAbility("Common");
		if(skill.ID().equalsIgnoreCase("Prop_LangTranslator"))
		{
			if(M.fetchEffect(skill.ID())!=null)
				skill=M.fetchEffect(skill.ID());
			if(CMath.s_bool(skill.getStat("EXISTS:"+lang.ID()+" `^"+skillto.name().toLowerCase()))
			)//&&CMath.s_bool(skill.getStat("EXISTS:#"+mob.Name())))
			{
				commonTelL(mob,"@x1 already knows how to do that for you.",M.name(mob));
				return false;
			}
		}
		else
		if(((skill instanceof Behavior)
			&&(M.fetchBehavior(((Behavior)skill).ID())!=null)
			)
		||(skill instanceof Ability)&&(M.fetchAbility(((Ability)skill).ID())!=null))
		{
			commonTelL(mob,"@x1 already knows how to do that.",M.name(mob));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		messedUp=!proficiencyCheck(mob,-trainingM.phyStats().level()+(2*getXLEVELLevel(mob)),auto);
		final int duration=getDuration(35,mob,trainingM.phyStats().level(),10);
		verb=L("training @x1",M.name());
		final CMMsg msg=CMClass.getMsg(mob,null,this,getActivityMessageType(),L("<S-NAME> start(s) training @x1.",M.name()));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
