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
   Copyright 2017-2018 Bo Zimmerman

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

public class AttributeTraining extends CommonSkill
{
	@Override
	public String ID()
	{
		return "AttributeTraining";
	}

	private final static String localizedName = CMLib.lang().L("Attribute Training");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"ATTRIBUTETRAINING","ATTRIBTRAIN","ATRAIN"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_EDUCATIONLORE;
	}

	protected final static int costMultiplier=2;
	protected final static int costAdder=0;
	
	protected Physical trained=null;
	protected int attribute=-1;
	protected boolean messedUp=false;
	
	public AttributeTraining()
	{
		super();
		displayText=L("You are training...");
		verb=L("training");
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			if((trained==null)||(mob.location()==null))
			{
				messedUp=true;
				unInvoke();
			}
			if((trained instanceof MOB)&&(!mob.location().isInhabitant((MOB)trained)))
			{
				messedUp=true;
				unInvoke();
			}
			if((trained instanceof Item)&&(!mob.location().isContent((Item)trained)))
			{
				messedUp=true;
				unInvoke();
			}
			final String attribName=CMStrings.capitalizeAndLower(CharStats.CODES.DESC(attribute));

			if(CMLib.dice().rollPercentage()<25)
				verb=L("@x1 training with @x2",attribName,trained.name());
			else
			switch(attribute)
			{
			case CharStats.STAT_CHARISMA:
				verb=L("praticing charm, decorum, and grooming with @x2",attribName,trained.name());
				break;
			case CharStats.STAT_CONSTITUTION:
				verb=L("doing cardio-vascular training with @x2",attribName,trained.name());
				break;
			case CharStats.STAT_DEXTERITY:
				verb=L("doing eye-hand coordination exercises with @x2",attribName,trained.name());
				break;
			case CharStats.STAT_STRENGTH:
				verb=L("doing weight repetitions with @x2",attribName,trained.name());
				break;
			case CharStats.STAT_INTELLIGENCE:
				verb=L("studying ancient knowledge with @x2",attribName,trained.name());
				break;
			case CharStats.STAT_WISDOM:
				verb=L("praticing wit and awareness with @x2",attribName,trained.name());
				break;
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
				if((trained!=null)&&(!aborted))
				{
					MOB follower=null;
					if(trained instanceof MOB)
						follower=(MOB)trained;
					if((messedUp)||(follower==null)||(attribute<0))
						commonTell(mob,L("You've failed to train @x1!",trained.name()));
					else
					{
						final String s=CMStrings.capitalizeAndLower(CharStats.CODES.DESC(attribute));
						mob.location().show(mob,null,getActivityMessageType(),L("<S-NAME> manage(s) to complete @x2 training with @x1.",follower.name(),s));
						final String attribName=CMStrings.capitalizeAndLower(CharStats.CODES.DESC(attribute));
						if(follower.isMonster())
						{
							Ability adjA=follower.fetchEffect("Prop_StatAdjuster");
							if(adjA == null)
							{
								adjA=CMClass.getAbility("Prop_StatAdjuster");
								follower.addNonUninvokableEffect(adjA);
							}
							final int oldVal=CMParms.getParmInt(adjA.text(), CMStrings.limit(CharStats.CODES.NAME(attribute),3), 0);
							int trainsRequired=CMLib.login().getTrainingCost(follower, attribute, false)*costMultiplier;
							if(trainsRequired>=0)
								trainsRequired+=costAdder;
							commonTell(mob,L("The training cost @x1 @x2 training points.",follower.name(),""+trainsRequired));
							if(trainsRequired > follower.getTrains())
								follower.setTrains(0);
							else
								follower.setTrains(follower.getTrains()-trainsRequired);
							String oldStr=adjA.text();
							oldStr = CMParms.delParmLong(oldStr, CMStrings.limit(CharStats.CODES.NAME(attribute),3));
							oldStr = CMStrings.replaceAll(oldStr, "  ", " ");
							adjA.setMiscText(oldStr.trim()+" "+CMStrings.limit(CharStats.CODES.NAME(attribute),3)+"="+(oldVal+1));
						}
						else
						{
							CMLib.commands().forceStandardCommand(follower, "Train", new XVector<String>(attribName));
						}
						follower.recoverCharStats();
						follower.recoverPhyStats();
						follower.recoverMaxState();
						if(trained instanceof CagedAnimal)
						{
							follower.text();
							((CagedAnimal)trained).cageMe(follower);
						}
					}
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

		verb=L("training");
		trained=null;
		String valid="Attributes include:";
		final StringBuffer thingsToTrainFor=new StringBuffer("");
		for(final int i: CharStats.CODES.BASECODES())
			thingsToTrainFor.append(CharStats.CODES.DESC(i)+", ");
		valid+=thingsToTrainFor.toString()+".";
		if(commands.size()<2)
		{
			commonTell(mob,L("Train whom in what attribute? @x1",valid));
			return false;
		}
		attribute=-1;
		String attribName="";
		final String what=commands.get(commands.size()-1);
		commands.remove(commands.size()-1);
		for(final int i: CharStats.CODES.BASECODES())
		{
			if(CharStats.CODES.DESC(i).startsWith(what.toUpperCase()))
			{
				attribute=i;
				attribName=CMStrings.capitalizeAndLower(CharStats.CODES.DESC(i));
			}
		}
		if(attribute<0)
		{
			commonTell(mob,L("Train whom in what attribute? @x1",valid));
			return false;
		}

		final String str=CMParms.combine(commands,0);
		MOB M=super.getTarget(mob, commands, givenTarget);
		trained=M;
		if(M!=null)
		{
			if(!CMLib.flags().canBeSeenBy(M,mob))
			{
				commonTell(mob,L("You don't see anyone called '@x1' here.",str));
				return false;
			}
			if(!M.isMonster())
			{
				commonTell(mob,L("@x1 is perfectly capable of training on their own.",M.name(mob)));
				return false;
			}
			if(CMLib.flags().isAnimalIntelligence(M))
			{
				commonTell(mob,L("You can't train with @x1.",M.name(mob)));
				return false;
			}
			if((!CMLib.flags().canMove(M))&&(CMLib.flags().isBoundOrHeld(M)))
			{
				commonTell(mob,L("@x1 doesn't seem able to train.",M.name(mob)));
				return false;
			}
			if(!mob.getGroupMembers(new HashSet<MOB>()).contains(M))
			{
				commonTell(mob,L("@x1 doesn't seem willing to train with you.",M.name(mob)));
				return false;
			}
			int curStat=M.baseCharStats().getRacialStat(M, attribute);
			int trainsRequired=CMLib.login().getTrainingCost(M, attribute, false)*costMultiplier;
			if(trainsRequired>=0)
				trainsRequired+=costAdder;
			if(trainsRequired<0)
				return false;
			final int teachStat=mob.charStats().getStat(attribute);
			if(curStat>=teachStat)
			{
				commonTell(mob,L("@x1 can only train with someone whose score is higher than their own.",M.name(mob)));
				return false;
			}
			if(M.getTrains()<trainsRequired)
			{
				if(trainsRequired>1)
				{
					commonTell(mob,L("@x1 requires @x1 training points to do that.",""+trainsRequired));
					return false;
				}
				else
				if(trainsRequired==1)
				{
					commonTell(mob,L("@x1 requires @x1 training point to do that.",""+trainsRequired));
					return false;
				}
			}
			trained=M;
		}
		else
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		
		messedUp=!proficiencyCheck(mob,-trained.phyStats().level()+(2*getXLEVELLevel(mob)),auto);
		final int duration=getDuration(45,mob,trained.phyStats().level(),10);
		verb=L("@x1 training with @x2",attribName,M.name());
		final CMMsg msg=CMClass.getMsg(mob,null,this,getActivityMessageType(),L("<S-NAME> start(s) @x2 training with @x1.",M.name(),attribName.toLowerCase()));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
