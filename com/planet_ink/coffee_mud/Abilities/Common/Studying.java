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
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Attrib;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
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
		public boolean doesRuleApplyTo(final Ability A)
		{
			return (A!=null) 
					&& (((A.classificationCode()&Ability.ALL_ACODES)==type1)
						||((A.classificationCode()&Ability.ALL_ACODES)==type2));
		}
		public int numAllowed(final int classLevel)
		{
			if(classLevel < aboveLevel)
				return 0;
			return num + (num * (int)Math.round(Math.floor((classLevel-aboveLevel) / perLevels)));
		}
	}

	protected perLevelLimits getSupportedSkillType()
	{
		return null;
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
train the scholar.  .  

We could also make this 6 different abilities – Common Skill Studying, Skills 
Studying, Songs Studying, Chants Studying, Spells Studying, and Prayers Studying if you would prefer
granting each ability at the lowest level above (1,2,3,4,5,6).


 */
	
	protected Ability teachingA = null;
	protected volatile boolean distributed = false;
	protected boolean successfullyTaught = false;
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
			return L("(Scholarly)"); // prevents it from being uninvokeable through autoaffects
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
		if((msg.source() == affected)
		&&(!canBeUninvoked())
		&&(msg.tool() instanceof Ability))
		{
			final MOB mob=msg.source();
			if(msg.tool().ID().equals("Spell_Scribe")
			||msg.tool().ID().equals("Spell_EnchantWand")
			||msg.tool().ID().equals("Spell_MagicItem")
			||msg.tool().ID().equals("Spell_StoreSpell")
			||msg.tool().ID().equals("Spell_WardArea"))
			{
				final Ability A=mob.fetchAbility(msg.tool().text());
				if((A!=null)&&(!A.isSavable()))
					forget(mob,A.ID());
			}
			final Ability A=mob.fetchAbility(msg.tool().ID());
			if((A!=null)&&(!A.isSavable())
			&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL))
				forget(mob,A.ID());
		}
	}

	protected boolean forget(final MOB mob, final String abilityID)
	{
		if(mob == null)
			return false;
		final Studying studA=(Studying)mob.fetchAbility(ID());
		final Studying effA=(Studying)mob.fetchEffect(ID());
		if((studA != null) && (effA != null))
		{
			final List<String> strList = CMParms.parseSemicolons(studA.text(), true);
			for(int i=0;i<strList.size();i++)
			{
				if(strList.get(i).startsWith(abilityID+","))
				{
					strList.remove(i);
					break;
				}
			}
			final String text=CMParms.combineWith(strList,';');
			for(final Ability A : effA.skillList)
			{
				if(A.ID().equalsIgnoreCase(abilityID))
				{
					mob.delAbility(A);
					effA.setMiscText(text);
					studA.setMiscText(text);
				}
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!canBeUninvoked())
		{
			if((msg.source()==affected)
			&&(msg.tool() instanceof Ability)
			&&(skillList.contains(msg.tool())))
			{
				msg.source().tell(L("You don't know how to do that."));
				return false;
			}
			else
			if((msg.target()==affected)
			&&(msg.targetMinor()==CMMsg.TYP_TEACH)
			&&(msg.tool() instanceof Ability))
				forget((MOB)msg.target(),msg.tool().ID());
		}
			
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
		if( canBeUninvoked() 
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
				if(this.successfullyTaught)
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
				}
				else
					mob.location().show(mob,invoker,getActivityMessageType(),L("<S-NAME> fail(s) to teach <T-NAME> @x1.",teachingA.name()));
				// let super announce it
			}
		}
		this.teachingA=null;
		super.unInvoke();
	}

	@Override
	public boolean invoke(final MOB mob, List<String> commands, Physical givenTarget, final boolean auto, final int asLevel)
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
						str.append(CMStrings.padRight(L(Ability.ACODE_DESCS[A.abilityCode()&Ability.ALL_ACODES]), 12)+": "+A.Name()+"\n\r");
					}
				}
			}
			mob.tell(str.toString());
		}
		if(commands.get(0).equalsIgnoreCase("FORGET") && (commands.size()>1))
		{
			commands.remove(0);
			final Ability A=CMClass.findAbility(CMParms.combine(commands));
			if(A!=null)
			{
				if(forget(mob,A.ID()))
					mob.tell(L("You have forgotten @x1.",A.name()));
				else
					mob.tell(L("You haven't studied @x1.",A.name()));
			}
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
		final int classLevel = CMLib.ableMapper().qualifyingClassLevel(mob, this);
		if((classLevel >=0)
		&&(!auto)
		&&(classLevel < lowestQualifyingLevel))
		{
			mob.tell(L("You aren't qualified to be taught @x1.",A.Name()));
			return false;
		}
		final int teacherClassLevel = CMLib.ableMapper().qualifyingClassLevel(target, this);
		if((teacherClassLevel <0)
		&&(!auto))
		{
			mob.tell(L("@x1 isn't qualified to teach @x2.",target.name(mob),A.Name()));
			return false;
		}
		perLevelLimits limitObj = null;
		for(perLevelLimits l : perLevelLimits.values())
		{
			if(l.doesRuleApplyTo(A))
				limitObj = l;
		}
		if(limitObj == null)
		{
			mob.tell(L("You can not study that sort of skill."));
			return false;
		}
		if((getSupportedSkillType()!=null) && (getSupportedSkillType()!=limitObj))
		{
			mob.tell(L("You can not study that sort of skill with this one."));
			return false;
		}
		int numAllowed = limitObj.numAllowed(classLevel);
		int numHas = 0;
		final List<List<String>> taughts = CMParms.parseDoubleDelimited(text(), ';', ',');
		for(final List<String> l : taughts)
		{
			if(l.size()>0)
			{
				final Ability A1=CMClass.getAbility(l.get(0));
				if(limitObj.doesRuleApplyTo(A1))
					numHas++;
			}
		}
		if(numHas >= numAllowed)
		{
			mob.tell(L("You may not study any more @x1 at this time.",CMLib.english().makePlural(Ability.ACODE_DESCS[A.abilityCode()&Ability.ALL_ACODES])));
			return false;
		}
		if(!super.invoke(mob, commands, givenTarget, auto, asLevel))
			return false;

		final double quickPct = getXTIMELevel(mob) * 0.05;
		final int teachTicks = (int)(((teacherClassLevel * 60000L) 
							- (10000L * (classLevel-teacherClassLevel)) 
							- (15000L * super.getXLEVELLevel(mob))) / CMProps.getTickMillis());
		final int duration=teachTicks-(int)Math.round(CMath.mul(teachTicks, quickPct));
		final long minutes = (duration * CMProps.getTickMillis() / 60000L);
		final long seconds = (duration * CMProps.getTickMillis() / 1000L);
		
		/*
			Training time should be (Skill's qualifying level by the teaching character in minutes 
			minus 10 seconds per level the teacher has over that, minus 15 seconds per expertise the scholar has, with 
			a minimum of 1 minute)
		*/
		successfullyTaught = super.proficiencyCheck(mob, 0, auto);
		{
			final Session sess = target.session();
			final Ability thisOne=this;
			final Runnable R=new Runnable()
			{
				final MOB		M	= mob;
				final MOB		tM	= target;
				final Ability	tA	= A;
				final Ability	oA	= thisOne;
				
				@Override
				public void run()
				{
					verb=L("teaching @x1 about @x2",M.name(tM),tA.name());
					displayText=L("You are @x1",verb);
					String str=L("<T-NAME> start(s) teaching <S-NAME> about @x1.",tA.Name());
					final CMMsg msg=CMClass.getMsg(mob,target,oA,CMMsg.MSG_NOISYMOVEMENT|(auto?CMMsg.MASK_ALWAYS:0),str);
					final Room R=mob.location();
					if(R!=null)
					{
						if(R.okMessage(mob,msg))
						{
							R.send(mob, msg);
							//final Studying sA = (Studying)
							beneficialAffect(mob,mob,asLevel,duration);
							
						}
					}
				}
				
			};
			if(target.isMonster() || (sess==null))
				R.run();
			else
			{
				sess.prompt(new InputCallback(InputCallback.Type.CONFIRM,"N",0)
				{
					@Override
					public void showPrompt()
					{
						String timeStr;
						if(minutes<2)
							timeStr = CMLib.lang().L("@x1 seconds",""+seconds);
						else
							timeStr = CMLib.lang().L("@x1 minutes",""+minutes);
						sess.promptPrint(L("\n\r@x1 wants you to try to teach @x2 about @x3. It will take around @x4.  Is that OK (y/N)? ",
								mob.name(target), target.charStats().himher(), A.name(), timeStr));
					}

					@Override
					public void timedOut()
					{
					}

					@Override
					public void callBack()
					{
						if (this.input.equals("Y"))
						{
							try
							{
								R.run();
							}
							catch (Exception e)
							{
							}
						}
					}
				});
			}
		}
			
		return true;
	}
}
