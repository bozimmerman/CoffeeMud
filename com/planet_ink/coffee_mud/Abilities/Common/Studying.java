package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Attrib;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
/*
   Copyright 2017-2017 Bo Zimmerman

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
public class Studying extends CommonSkill
{
	@Override
	public String ID()
	{
		return "Studying";
	}

	private final static String	localizedName	= CMLib.lang().L("Studying");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return !isAnAutoEffect;
	}

/*
 * 	TODO:

When the command is initiated, it looks like a reverse TEACH.  It will provide the teaching character 
with a y/n dialogue option if they want to train the scholar, and it will tell them about how long to 
train the scholar.  Training time should be (Skill’s qualifying level by the teaching character in minutes 
minus 10 seconds per level the teacher has over that, minus 15 seconds per expertise the scholar has, with 
a minimum of 1 minute).  

We could also make this 6 different abilities – Common Skill Studying, Skills 
Studying, Songs Studying, Chants Studying, Spells Studying, and Prayers Studying if you would prefer…
granting each ability at the lowest level above (1,2,3,4,5,6).

EDUCATING expertise provides reduced study time, increased proficiency (unless at 100%), and I would 
suggest one additional skill per ability type.

Can’t study things until your class level is higher than the lowestqualifyinglevel overall.

When level, look ahead to see if a skill you taught is coming, and if so, untaught it.
 */
	
	protected Ability teachingA = null;
	protected volatile boolean distributed = false;
	protected List<Ability> skillList = new LinkedList<Ability>();
	
	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
		distributed = false;
	}
	
	@Override
	public String displayText()
	{
		if(this.isNowAnAutoEffect())
			return "";
		final MOB invoker=this.invoker;
		final Ability teachingA = this.teachingA;
		final Physical affected = this.affected;
		if((invoker != null)
		&&(teachingA != null)
		&&(affected instanceof MOB))
			return L("You are teaching @x1 @x2.",invoker.name((MOB)affected),teachingA.name());
		return L("You are teaching someone something somehow!");
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		return super.okMessage(myHost,msg);
	}
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(!canBeUninvoked())
		{
			final MOB mob=(MOB)affected;
			if((!distributed)
			&&(affected instanceof MOB)
			&&(isNowAnAutoEffect()))
			{
				boolean doWorkOn = false;
				synchronized(skillList)
				{
					if(!distributed)
					{
						distributed=true;
						doWorkOn=true;
					}
				}
				if(doWorkOn)
				{
					if(skillList.size() > 0)
					{
						for(Ability a : skillList)
						{
							final Ability A=mob.fetchAbility(a.ID());
							if((A!=null)&&(!A.isSavable()))
							{
								mob.delAbility(A);
								final Ability fA=mob.fetchEffect(A.ID());
								fA.unInvoke();
								mob.delEffect(fA);
							}
						}
						skillList.clear();
					}
					for(final String as : CMParms.parseSemicolons(text(), false))
					{
						final List<String> idProf = CMParms.parseCommas(as, true);
						if(idProf.size()>1)
						{
							final Ability A=CMClass.getAbility(idProf.get(0));
							if(A!=null)
							{
								A.setSavable(false);
								A.setProficiency(CMath.s_int(idProf.get(1)));
								mob.addAbility(A);
								skillList.add(A);
							}
						}
					}
				}
			}
		}
		else
		if((affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			final MOB invoker=this.invoker();
			if((invoker == null)
			||(invoker == mob)
			||(invoker.location()!=mob.location())
			||(invoker.isInCombat())
			||(invoker.location()!=activityRoom)
			||(!CMLib.flags().isAliveAwakeMobileUnbound(invoker,true)))
			{
				aborted=true;
				unInvoke();
				return false;
			}
		}
		return true;
	}
	@Override
	public void unInvoke()
	{
		if(canBeUninvoked() 
		&& (!super.unInvoked) 
		&& (affected instanceof MOB)
		&& (!aborted))
		{
			final MOB mob=(MOB)affected;
			final MOB invoker=this.invoker;
			final Ability teachingA=this.teachingA;
			if((mob!=null)
			&&(invoker !=null)
			&&(teachingA != null)
			&&(mob.location()!=null)
			&&(invoker.location()==mob.location()))
			{
				final Ability A=invoker.fetchAbility(ID());
				final Ability fA=invoker.fetchEffect(ID());
				final Ability mTeachingA=mob.fetchAbility(teachingA.ID());
				if((A==null)||(fA==null)||(mTeachingA==null)||(!A.isSavable())||(!fA.isNowAnAutoEffect()))
					aborted=true;
				else
				{
					final StringBuilder str=new StringBuilder(A.text());
					if(str.length()>0)
						str.append(';');
					final int prof = mTeachingA.proficiency() + (5 * super.expertise(mob, mTeachingA, ExpertiseLibrary.Flag.LEVEL));
					str.append(mTeachingA.ID()).append(',').append(prof);
					fA.setMiscText(str.toString()); // and this should do it.
					A.setMiscText(str.toString()); // and this should be savable
				}
				// let super announce it
			}
		}
		this.teachingA=null;
		super.unInvoke();
	}

	protected static enum perLevelLimits 
	{
		COMMON(1,6,1, ACODE_COMMON_SKILL, ACODE_LANGUAGE),
		SKILL(1,6,2,ACODE_SKILL, ACODE_THIEF_SKILL),
		SONG(1,6,3,ACODE_SONG, -1),
		SPELL(1,6,4,ACODE_SPELL, -1),
		CHANT(1,6,5,ACODE_CHANT, -1),
		PRAYER(1,6,6,ACODE_PRAYER, -1)
		;
		private int type1 = -1;
		private int type2 = -1;
		private int num = 1;
		private int perLevels = 1;
		private int aboveLevel = 1;
		private perLevelLimits(int num, int perLevels, int aboveLevel, int type1, int type2)
		{
			this.num=num;
			this.perLevels=perLevels;
			this.aboveLevel=aboveLevel;
			this.type1=type1;
			this.type2=type2;
		}
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()==0)
		{
			mob.tell(L("You've been taught: "));
			final List<List<String>> taughts = CMParms.parseDoubleDelimited(text(), ';', ',');
			StringBuilder str=new StringBuilder("");
			for(final List<String> l : taughts)
			{
				if(l.size()>1)
				{
					final Ability A=mob.fetchAbility(l.get(0));
					final Ability eA=mob.fetchEffect(l.get(0));
					final int prof=CMath.s_int(l.get(1));
					if((A!=null)&&(!A.isSavable()))
					{
						A.setProficiency(prof);
						if(eA!=null)
						{
							eA.unInvoke();
							mob.delEffect(eA);
						}
						if(str.length()>0)
							str.append(", ");
						str.append(A.Name());
					}
				}
			}
			mob.tell(str.toString());
		}
		if(commands.size()<2)
		{
			mob.tell(L("Have who teach you what?"));
			return false;
		}
		List<String> name = new XVector<String>(commands.remove(0));
		final MOB target=super.getTarget(mob, name, givenTarget);
		if(target==null)
			return false;
		if(target == mob)
		{
			mob.tell(L("You can't teach yourself."));
			return false;
		}
		if((target.isAttributeSet(MOB.Attrib.NOTEACH))
		&&((!target.isMonster())||(!target.willFollowOrdersOf(mob))))
		{
			mob.tell(L("@x1 is not accepting students right now.",target.name(mob)));
			return false;
		}
		final String skillName = CMParms.combine(commands);
		final Ability A=CMClass.findAbility(skillName, mob);
		if(A==null)
		{
			mob.tell(L("@x1 doesn't know '@x2'.",target.name(mob),skillName));
			return false;
		}
		final int lowestQualifyingLevel = CMLib.ableMapper().lowestQualifyingLevel(A.ID());
		int level = CMLib.ableMapper().qualifyingClassLevel(mob, this);
		if((level >=0)
		&&(!auto)
		&&(level < lowestQualifyingLevel))
		{
			mob.tell(L("You aren't qualified to be taught @x1.",A.Name()));
			return false;
		}
		
		if(!super.invoke(mob, commands, givenTarget, auto, asLevel))
			return false;
		return true;
	}
}
