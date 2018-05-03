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
public class Lecturing extends CommonSkill
{
	@Override
	public String ID()
	{
		return "Lecturing";
	}

	private final static String	localizedName	= CMLib.lang().L("Lecturing");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "LECTURE", "LECTURING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_EDUCATIONLORE;
	}

	public Lecturing()
	{
		super();
		displayText=L("You are lecturing...");
		verb=L("lecturing");
	}

	protected long lastLecture = 0;
	
	protected boolean success=false;
	protected String lectureName="";
	protected String lectureID="";

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			final Room R=mob.location();
			List<MOB> students = this.getApplicableStudents(mob);
			if(students.size()<3)
			{
				commonTell(mob,L("You don't have enough students to continue the lecture."));
				success=false;
				unInvoke();
				return false;
			}
			
			if((CMLib.dice().rollPercentage()<20)
			&&(R!=null))
			{
				final MOB randM=students.get(CMLib.dice().roll(1, students.size(), -1));
				switch(CMLib.dice().roll(1, 10, -1))
				{
				case 0:
					R.show(mob, null, CMMsg.MSG_NOISE, L("<S-NAME> say(s) really interesting things about @x1.",lectureName));
					break;
				case 1:
					R.show(mob, null, CMMsg.MSG_NOISE, L("<S-NAME> tell(s) the students to practice @x1 now.",lectureName));
					break;
				case 2:
					R.show(mob, null, CMMsg.MSG_NOISE, L("<S-NAME> instructs(s) the class on @x1.",lectureName));
					break;
				case 3:
					R.show(mob, null, CMMsg.MSG_NOISE, L("<S-NAME> shows(s) @x2 some improved methods of @x1.",lectureName,randM.name()));
					break;
				case 4:
					R.show(randM, null, CMMsg.MSG_NOISE, L("<S-NAME> ask(s) a stupid question about @x1.",lectureName));
					break;
				case 5:
					R.show(randM, null, CMMsg.MSG_NOISE, L("<S-NAME> flub(s) an attempt at @x1.",lectureName));
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
			
			if(tickUp==6)
			{
				if(success==false)
				{
					final StringBuffer str=new StringBuffer(L("Noone is paying attention. Your lecture failed.\n\r"));
					commonTell(mob,str.toString());
					unInvoke();
				}

			}
		}
		return super.tick(ticking,tickID);
	}

	protected List<MOB> getApplicableStudents(final MOB mob)
	{
		final ArrayList<MOB> list=new ArrayList<MOB>();
		if(mob==null)
			return list;
		final Room R=mob.location();
		if(R==null)
			return list;
		if((this.lectureID==null)
		||(this.lectureName==null)
		||(this.lectureID.length()==0)
		||(this.lectureName.length()==0))
			return list;
		for(Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
		{
			final MOB M=m.nextElement();
			if((M!=null)
			&&(M!=mob)
			&&(CMLib.flags().canBeHeardSpeakingBy(mob, M))
			&&(M.fetchAbility(lectureID)!=null))
				list.add(M);
		}
		return list;
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				final Room room=mob.location();
				final List<MOB> students=this.getApplicableStudents(mob);
				if((success)
				&&(!aborted)
				&&(room!=null)
				&&(students.size()==3))
				{
					final Ability protoA=mob.fetchAbility(lectureID);
					final int prof = protoA!=null ? protoA.proficiency() : 0;
					for(MOB studentM : students)
					{
						final Ability A=studentM.fetchAbility(lectureID);
						if(A!=null)
						{
							if(!A.appropriateToMyFactions(mob))
								continue;

							final int maxProficiency = CMLib.ableMapper().getMaxProficiency(studentM,true,A.ID());
							if((A.proficiency() > prof)
							||(A.proficiency()==100)
							||(A.proficiency() >= maxProficiency))
								studentM.tell(L("Since you knew more about @x1 than @x2 did, you didn't learn anything.",lectureName,mob.Name()));
							else
							{
								int amt=prof-A.proficiency();
								if(amt < 1)
									amt=1;
								else
								if(amt > 5)
									amt=5;
								if(A.proficiency() + amt > 100)
									amt = 100 - A.proficiency();
								CMLib.leveler().postExperience(mob, null, null, 10 * amt, false);
								A.setProficiency(A.proficiency() + amt);
								studentM.tell(L("You learned @x1% more about @x2 from @x3!",""+amt,lectureName,mob.Name()));
							}
						}
						else
							studentM.tell(L("You had absolutely no idea what @x1 was talking about.",mob.Name()));
					}
					
					final Room R=mob.location();
					final Area areA=(R!=null)?R.getArea():null;
					if(areA!=null)
					{
						final Lecturing lecA=(Lecturing)mob.fetchAbility("Lecturing");
						if(lecA!=null)
							lecA.lastLecture=areA.getTimeObj().toHoursSinceEpoc();
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
		verb=L("lecturing");
		this.lectureID="";
		this.lectureName="";
		success=false;
		
		final Room R=mob.location();
		final Area areA=(R!=null)?R.getArea():null;
		if((this.lastLecture != 0)&&(areA!=null))
		{
			final TimeClock C=areA.getTimeObj();
			TimeClock lastPubC=(TimeClock)CMClass.getCommon("DefaultTimeClock");
			lastPubC.setFromHoursSinceEpoc(this.lastLecture);
			if(C.getYear() == lastPubC.getYear())
			{
				if(C.getMonth() <= lastPubC.getMonth())
				{
					commonTell(mob,L("You've already lectured this month."));
					return false;
				}
			}
			else
			if(C.getYear() < lastPubC.getYear())
			{
				commonTell(mob,L("You last lectured in the year @x1?!!",""+lastPubC.getYear()));
				return false;
			}
		}
		
		if(commands.size()==0)
		{
			commonTell(mob,L("Lecture about what? Try checking yoru SKILLS, SPELLS, PRAYERS, CHANTS, etc.."));
			return false;
		}
		final String calledThis=CMParms.combine(commands,0);
		final Vector<Ability> ableV=new XVector<Ability>(mob.abilities());
		Ability A=(Ability)CMClass.getGlobal(ableV,calledThis);
		if(A==null)
			A=(Ability)CMLib.english().fetchEnvironmental(ableV,calledThis,true);
		if(A==null)
			A=(Ability)CMLib.english().fetchEnvironmental(ableV,calledThis,false);
		if(A==null)
		{
			A=CMClass.findAbility(calledThis);
			if(A!=null)
				commonTell(mob,L("You don't know anything about @x1, and can't lecture about it.",A.Name()));
			else
				commonTell(mob,L("You don't know anything about '@x1', and can't lecture about it.",calledThis));
			return false;
		}
		this.lectureID=A.ID();
		this.lectureName=A.Name();
		if(this.getApplicableStudents(mob).size()<3)
		{
			commonTell(mob,L("You'll need at least three students here who know @x1 to give a lecture.",A.Name()));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		if(proficiencyCheck(mob,0,auto))
			success=true;
		final int duration=getDuration(15 * 60,mob,CMLib.ableMapper().lowestQualifyingLevel(lectureID),1);
		final CMMsg msg=CMClass.getMsg(mob,null,this,getActivityMessageType(),L("<S-NAME> begin(s) lecturing about @x1.",lectureName));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			Lecturing L=(Lecturing)beneficialAffect(mob,mob,asLevel,duration);
			if(L!=null)
			{
				L.lectureID=this.lectureID;
				L.lectureName=this.lectureName;
			}
		}
		return true;
	}
}
