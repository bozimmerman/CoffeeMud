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
a minimum of 1 minute).  The scholar gets the skill as a level 0 skill learned through studying at the 
teaching player’s proficiency, plus 5% per level of expertise.  (We could just grant the scholar 100% 
proficiency if it makes it easier on identifying the skills he can’t use, but my rationale was to encourage 
the scholar to study from someone who actually knows the skill…or we could require that player to have 
it with at least 75% proficiency, or even at 100% proficiency).  
Should there be a limit on the number of abilities a scholar has learned? (I think 1 per level, or maybe 
1 common skill per 6 levels above level 1 (so 1 at 1, 2 at 7, 3 at 13, etc), 1 skill (fighter, thief, 
normal, etc) per 6 levels above 2 (1 at 2, 2 at 8, 3 at 14, etc), 1 song per 6 levels above 3, 1 spell 
per 6 levels above 4, 1 chant per 6 levels above 5 and 1 prayer per 6 levels above 6 (so 1 at 6, 2 at 
12, 3 at 18, etc).)  We could also make this 6 different abilities – Common Skill Studying, Skills 
Studying, Songs Studying, Chants Studying, Spells Studying, and Prayers Studying if you would prefer…
granting each ability at the lowest level above (1,2,3,4,5,6).
EDUCATING expertise provides reduced study time, increased proficiency (unless at 100%), and I would 
suggest one additional skill per ability type.
Can’t study things until your class level is higher than the lowestqualifyinglevel overall.
 */
	
	protected Ability teachingA = null;
	protected volatile boolean distributed = false;
	protected List<String> skillList = new LinkedList<String>();
	
	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
		distributed = false;
	}
	
	@Override
	public String displayText()
	{
		if(this.isAnAutoEffect)
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
			&&(this.isNowAnAutoEffect()))
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
						for(String a : skillList)
						{
							final Ability A=mob.fetchAbility(a);
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
					for(String a : CMParms.parseSemicolons(text(), false))
					{
						final Ability A=CMClass.getAbility(a);
						if(A!=null)
						{
							A.setSavable(false);
							A.setProficiency(0);
							mob.addAbility(A);
							skillList.add(A.ID());
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
				final Ability fA=mob.fetchAbility(ID());
				if((A==null)||(fA==null)||(!A.isSavable())||(!fA.isNowAnAutoEffect()))
					aborted=true;
				else
				{
					final List<String> list = CMParms.parseSemicolons(A.text(), true);
					if(!list.contains(teachingA.ID()))
						list.add(teachingA.ID());
					fA.setMiscText(CMParms.combineWith(list, ';')); // and this should do it.
					A.setMiscText(CMParms.combineWith(list, ';')); // and this should be savable
				}
				// let super announce it
			}
		}
		this.teachingA=null;
		super.unInvoke();
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob, commands, givenTarget, auto, asLevel))
			return false;
		return true;
	}
}
