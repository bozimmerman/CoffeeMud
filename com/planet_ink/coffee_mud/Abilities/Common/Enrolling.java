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
   Copyright 2003-2018 Bo Zimmerman

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

public class Enrolling extends CommonSkill
{
	@Override
	public String ID()
	{
		return "Enrolling";
	}

	private final static String localizedName = CMLib.lang().L("Enrolling");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"ENROLLING","ENROLL"});
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

	protected MOB		enrollingM	= null;
	protected CharClass	enrolledInC	= null;
	protected boolean	messedUp	= false;

	public Enrolling()
	{
		super();
		displayText=L("You are enrolling...");
		verb=L("enrolling");
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			if((enrollingM==null)||(mob.location()==null))
			{
				messedUp=true;
				unInvoke();
			}
			if(!mob.location().isInhabitant(enrollingM))
			{
				messedUp=true;
				unInvoke();
			}
			final Room R=mob.location();
			if((CMLib.dice().rollPercentage()<20)
			&&(R!=null))
			{
				final MOB randM=enrollingM;
				final String lectureName = enrolledInC.name();
				switch(CMLib.dice().roll(1, 10, -1))
				{
				case 0:
					R.show(mob, null, CMMsg.MSG_NOISE, L("<S-NAME> say(s) really interesting things about @x1.",lectureName));
					break;
				case 1:
					R.show(mob, null, CMMsg.MSG_NOISE, L("<S-NAME> tell(s) @x2 to practice @x1 now.",lectureName,randM.name()));
					break;
				case 2:
					R.show(mob, null, CMMsg.MSG_NOISE, L("<S-NAME> instructs(s) @x2 on @x1.",lectureName,randM.name()));
					break;
				case 3:
					R.show(mob, null, CMMsg.MSG_NOISE, L("<S-NAME> shows(s) @x2 some improved methods of @x1.",lectureName,randM.name()));
					break;
				case 4:
					R.show(randM, null, CMMsg.MSG_NOISE, L("<S-NAME> ask(s) a stupid question about @x1.",lectureName));
					break;
				case 5:
					R.show(randM, null, CMMsg.MSG_NOISE, L("<S-NAME> flub(s) an attempt at a @x1 technique.",lectureName));
					break;
				case 6:
					R.show(mob, null, CMMsg.MSG_NOISE, L("<S-NAME> point(s) out some of the finer points of @x1.",lectureName));
					break;
				case 7:
					R.show(mob, null, CMMsg.MSG_NOISE, L("<S-NAME> ask(s) @x2 a question about @x1.",lectureName,randM.name()));
					break;
				case 8:
					R.show(mob, null, CMMsg.MSG_NOISE, L("<S-NAME> correct(s) @x2s @x1 technique.",lectureName,randM.Name()));
					break;
				case 9:
					R.show(mob, null, CMMsg.MSG_NOISE, L("<S-NAME> draw(s) a diagram to illustrate something about @x1.",lectureName,randM.Name()));
					break;
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
				if((enrollingM!=null)&&(!aborted))
				{
					MOB M=enrollingM;
					if((messedUp)||(M==null))
					{
						commonTell(mob,L("You've failed to enroll @x1!",enrollingM.name()));
						CMLib.beanCounter().giveSomeoneMoney(mob, this.enrollCost(mob, M, 0));
						mob.tell(L("You recover @x1 in fees from the guild.",CMLib.beanCounter().abbreviatedPrice(mob, this.enrollCost(mob, M, 0))));
					}
					else
					{
						if(M.charStats().getCurrentClass() != CMClass.getCharClass("StdCharClass"))
							commonTell(mob,L("@x1 is already enrolled.",enrollingM.name()));
						else
						{
							mob.location().show(mob,null,getActivityMessageType(),L("<S-NAME> manage(s) to enroll @x1 in @x2.",M.name(),enrolledInC.name()));
							Ability A=M.fetchEffect("Prop_Adjuster");
							String txt="";
							if(A!=null)
								txt=A.text();
							else
							{
								A=CMClass.getAbility("Prop_Adjuster");
								A.setSavable(true);
								M.addNonUninvokableEffect(A);
							}
							txt += " class="+enrolledInC.ID()+" clstart="+M.basePhyStats().level();
							txt = txt.trim();
							A.setMiscText(txt);
							M.recoverCharStats();
							M.recoverMaxState();
							M.recoverPhyStats();
						}
					}
				}
			}
		}
		super.unInvoke();
	}

	protected int enrollCost(final MOB teacherM, final MOB M, final int asLevel)
	{
		int cost=(M.phyStats().level() * M.phyStats().level() * 100)
			-  (10 * M.phyStats().level() * adjustedLevel(teacherM,asLevel))
			-  (100* M.phyStats().level() * super.getX1Level(teacherM));
		if(cost < 500)
			cost=500;
		return 500;
	}

	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		verb=L("enrolling");
		enrollingM=null;
		enrolledInC=null;
		if(commands.size()<2)
		{
			commonTell(mob,L("Enroll whom in what class?"));
			return false;
		}
		String s1=commands.get(commands.size()-1);
		enrolledInC=CMClass.getCharClass(s1);
		if(enrolledInC==null)
			enrolledInC=CMClass.findCharClass(s1);
		if(enrolledInC==null)
		{
			commonTell(mob,L("You don't know of a class called @x1.",s1));
			return false;
		}
		else
			commands.remove(commands.size()-1);
		
		if((!(mob.charStats().getCharClasses().contains(enrolledInC)))
		&&((mob.playerStats()==null)||(mob.playerStats().getActiveTitle().toLowerCase().indexOf(enrolledInC.name().toLowerCase())<0)))
		{
			commonTell(mob,L("You need to either be a @x1, or an honorary @x1 to enroll anyone in that.",enrolledInC.name()));
			return false;
		}
		
		final String str=CMParms.combine(commands,0);
		MOB M=mob.location().fetchInhabitant(str);
		enrollingM=null;
		if(M!=null)
		{
			if(!CMLib.flags().canBeSeenBy(M,mob))
			{
				commonTell(mob,L("You don't see anyone called '@x1' here.",str));
				return false;
			}
			if(!M.isMonster())
			{
				commonTell(mob,L("You can't enroll @x1.",M.name(mob)));
				return false;
			}
			if(M.charStats().getCurrentClass()!=CMClass.getCharClass("StdCharClass"))
			{
				commonTell(mob,L("@x1 already has a career in @x2.",M.name(),M.charStats().getCurrentClass().name()));
				return false;
			}
			if((!mob.getGroupMembers(new HashSet<MOB>()).contains(M))
			&&(CMLib.flags().canMove(M))
			&&(!CMLib.flags().isBoundOrHeld(M)))
			{
				commonTell(mob,L("@x1 doesn't seem willing to cooperate.",M.name(mob)));
				return false;
			}
			enrollingM=M;
		}
		else
			return false;

		if(!enrolledInC.qualifiesForThisClass(enrollingM, true))
		{
			commonTell(mob,L("@x1 does not qualify to become a @x2.",enrollingM.Name(),enrolledInC.name()));
			return false;
		}
		
		int cost=enrollCost(mob,enrollingM,asLevel);
		if(CMLib.beanCounter().getTotalAbsoluteNativeValue(mob) < cost)
		{
			commonTell(mob,L("You don't have the @x1 to pay the guild fees.",CMLib.beanCounter().abbreviatedPrice(mob, cost)));
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		messedUp=!proficiencyCheck(mob,0,auto);
		long baseDuration=60 * 60;
		final Room R=mob.location();
		final Area areA=(R!=null)?R.getArea():null;
		if(areA!=null)
			baseDuration = CMProps.getTicksPerMudHour() * areA.getTimeObj().getHoursInDay();
			
		final int duration=getDuration((int)baseDuration,mob,enrollingM.phyStats().level(),10);
		verb=L("enrolling @x1 into a @x2 career",M.name(),enrolledInC.name());
		
		final CMMsg msg=CMClass.getMsg(mob,null,this,getActivityMessageType(),L("<S-NAME> start(s) enrolling @x1 into a @x2 career.",M.name(),enrolledInC.name()));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			if(beneficialAffect(mob,mob,asLevel,duration)!=null)
			{
				CMLib.beanCounter().subtractMoney(mob, cost);
				mob.tell(L("The guild fees came to @x1.",CMLib.beanCounter().abbreviatedPrice(mob, cost)));
			}
		}
		return true;
	}
}
