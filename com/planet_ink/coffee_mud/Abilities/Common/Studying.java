package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.SecFlag;
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
public class Studying extends CommonSkill implements AbilityContainer
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

	private static final String[]	triggerStrings	= I(new String[] { "STUDY", "STUDYING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}
	
	protected static enum perLevelLimits 
	{
		COMMON(1, 6, 1, ACODE_COMMON_SKILL, ACODE_LANGUAGE),
		SKILL(1, 6, 2, ACODE_SKILL, ACODE_THIEF_SKILL),
		SONG(1, 6, 3, ACODE_SONG, -1),
		SPELL(1, 6, 4, ACODE_SPELL, -1),
		CHANT(1, 6, 5, ACODE_CHANT, -1),
		PRAYER(1, 6, 6, ACODE_PRAYER, -1)
		;

		private int	type1		= -1;
		private int	type2		= -1;
		private int	num			= 1;
		private int	perLevels	= 1;
		private int	aboveLevel	= 1;

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

		public boolean doesRuleApplyTo(final int abilityCode)
		{
			return ((abilityCode==type1) || (abilityCode==type2));
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
	We could also make this 6 different abilities, Common Skill Studying, Skills 
	Studying, Songs Studying, Chants Studying, Spells Studying, and Prayers Studying if you would prefer
	granting each ability at the lowest level above (1,2,3,4,5,6).
	 */
	
	protected Physical			teacherP			= null;
	protected Ability			teachingA			= null;
	protected volatile boolean	distributed			= false;
	protected boolean			successfullyTaught	= false;
	protected List<Ability>		skillList			= new LinkedList<Ability>();

	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
		distributed = false;
	}
	
	@Override
	public String displayText()
	{	
		if((teacherP == null)||(teachingA==null))
			return L("(Scholarly)"); // prevents it from being uninvokeable through autoaffects
		else
		{
			final Ability teachingA = this.teachingA;
			if((teachingA != null)
			&&(affected instanceof MOB))
				return L("You are being taught @x2 by @x1.",teacherP.name((MOB)affected),teachingA.name());
			return L("You are being taught something by someone!");
		}
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
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_EDUCATIONLORE;
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
			boolean removed=false;
			for(int i=0;i<strList.size();i++)
			{
				if(strList.get(i).startsWith(abilityID+","))
				{
					strList.remove(i);
					removed = true;
					break;
				}
			}
			if(removed)
			{
				final String text=CMParms.combineWith(strList,';');
				for(final Ability A : effA.skillList)
				{
					if(A.ID().equalsIgnoreCase(abilityID))
						mob.delAbility(A);
				}
				effA.setMiscText(text);
				studA.setMiscText(text);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean autoInvocation(MOB mob, boolean force)
	{
		if(isAutoInvoked())
		{
			if(CMSecurity.isAllowedEverywhere(mob, SecFlag.ALLSKILLS))
				return false;
		}
		return super.autoInvocation(mob, force);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.source()==affected)
		&&(msg.tool() instanceof Ability)
		&&(skillList.contains(msg.tool())))
		{
			msg.source().tell(L("You don't know how to do that in practice."));
			return false;
		}
		else
		if((msg.target()==affected)
		&&(msg.targetMinor()==CMMsg.TYP_TEACH)
		&&(msg.tool() instanceof Ability))
			forget((MOB)msg.target(),msg.tool().ID());
		else
		if((msg.tool() instanceof Ability)
		&&(msg.source()==affected)
		&&(msg.targetMinor()==CMMsg.TYP_WROTE)
		&&(msg.targetMessage().length()>0)
		&&(msg.tool().ID().equals("Skill_Dissertation")))
		{
			forget(msg.source(),msg.targetMessage());
		}
			
		return super.okMessage(myHost,msg);
	}
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected instanceof MOB)
		&&(tickID==Tickable.TICKID_MOB))
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
					distributeSkills(mob);
				}
			}
			if((this.teacherP != null)
			&&(this.teachingA!=null)
			&&(this.affected instanceof MOB))
			{
				final Physical teacher=this.teacherP;
				if((teacher == null)
				||(teacher == affected)
				||(mob.location()!=CMLib.map().roomLocation(teacher))
				||((teacher instanceof MOB)&&(((MOB)teacher).isInCombat()))
				||(mob.isInCombat())
				||(mob.location()!=activityRoom)
				||((teacher instanceof MOB)&&(!CMLib.flags().isAliveAwakeMobileUnbound((MOB)teacher,true)))
				||(!CMLib.flags().isAliveAwakeMobileUnbound(mob, true)))
				{
					aborted=true;
					unInvoke();
					return true;
				}
				if((--tickDown)<=0)
				{
					tickDown=-1;
					unInvoke();
					return true;
				}
				else
					tickUp++;
				super.tick(ticking, tickID);
			}
		}
		return true;
	}

	@Override
	public void unInvoke()
	{
		if((this.teacherP != null)
		&&(this.teachingA!=null)
		&&(this.activityRoom!=null)
		&&(this.affected instanceof MOB))
		{
			final MOB mob=(MOB)this.affected;
			final Ability teachingA=this.teachingA;
			this.teachingA=null;
			this.activityRoom=null;
			this.tickDown=Integer.MAX_VALUE/2;
			final MOB teacherM;
			if(this.teacherP instanceof MOB)
				teacherM=(MOB)this.teacherP;
			else
			{
				teacherM=CMClass.getFactoryMOB(teacherP.name(), 30, mob.location());
				teacherM.setAttribute(Attrib.NOTEACH, false);
				final Ability teacherMA=CMClass.getAbility(teachingA.ID());
				teacherMA.setProficiency(100);
				teacherM.addAbility(teacherMA);
			}
			String teachingAName=(teachingA!=null)?teachingA.name():L("something");
			String doneMsgStr;
			if(aborted)
				doneMsgStr=L("<S-NAME> stop(s) learning @x1 from <T-NAME>.",teachingAName);
			else
				doneMsgStr=L("<S-NAME> <S-IS-ARE> done learning @x1 from <T-NAME>.",teachingAName);
			try
			{
				if((teachingA != null)
				&&(mob.location()!=null)
				&&(!aborted)
				&&(CMLib.map().roomLocation(teacherP)==mob.location()))
				{
					if(this.successfullyTaught)
					{
						final Ability A=mob.fetchAbility(ID());
						final Ability fA=mob.fetchEffect(ID());
						final Ability mTeachingA=teacherM.fetchAbility(teachingA.ID());
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
						mob.location().show(teacherM,mob,getActivityMessageType(),L("<S-NAME> fail(s) to teach <T-NAME> @x1.",teachingA.name()));
					// let super announce it
				}
			}
			finally
			{
				mob.location().show(mob,teacherM,getActivityMessageType(),doneMsgStr);
				if((teacherP instanceof Item)&&(!teacherM.isPlayer()))
					teacherM.destroy();
				this.teacherP=null;
			}
			helping=false;
			helpingAbility=null;
		}
	}
	
	public void distributeSkills(final MOB mob)
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
					if(fA!=null)
					{
						fA.unInvoke();
						mob.delEffect(fA);
					}
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
	
	public void confirmSkills(final MOB mob)
	{
		final Studying studyA=(Studying)mob.fetchEffect(ID());
		if((studyA!=null)
		&&(studyA.distributed))
		{
			boolean broken=false;
			for(final Ability A : skillList)
			{
				if(mob.fetchAbility(A.ID())==null)
					broken=true;
			}
			if(broken)
				studyA.distributeSkills(mob);
		}
	}
	
	@Override
	public boolean invoke(final MOB mob, List<String> commands, Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(commands.size()==0)
		{
			confirmSkills(mob);
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
						str.append(CMStrings.padRight(L(Ability.ACODE_DESCS[A.classificationCode()&Ability.ALL_ACODES]), 12)+": "+A.Name()+"\n\r");
					}
				}
			}
			str.append("\n\rYou may learn ");
			for(int i=0;i<Ability.ACODE_DESCS.length;i++)
			{
				perLevelLimits limitObj = null;
				for(perLevelLimits l : perLevelLimits.values())
				{
					if(l.doesRuleApplyTo(i))
						limitObj = l;
				}
				if(limitObj == null)
					continue;
				if((getSupportedSkillType()!=null) && (getSupportedSkillType()!=limitObj))
					continue;
				final int classLevel = CMLib.ableMapper().qualifyingClassLevel(mob, this);
				int numAllowed = limitObj.numAllowed(classLevel);
				int numHas = 0;
				for(final List<String> l : taughts)
				{
					if(l.size()>0)
					{
						final Ability A1=CMClass.getAbility(l.get(0));
						if(limitObj.doesRuleApplyTo(A1))
							numHas++;
					}
				}
				str.append(numAllowed-numHas).append(" more ").append(CMLib.english().makePlural(Ability.ACODE_DESCS[i].toLowerCase())).append(", ");
			}
			final String fstr=str.toString();
			if(fstr.endsWith(", "))
				mob.tell(fstr.substring(0,fstr.length()-2)+".");
			else
				mob.tell(fstr);
			return true;
		}
		if((commands.size()>0)&&(commands.size()<3))
		{
			String combStr=CMParms.combine(commands);
			final List<List<String>> taughts = CMParms.parseDoubleDelimited(text(), ';', ',');
			for(int i=0;i<Ability.ACODE_DESCS.length;i++)
			{
				perLevelLimits limitObj = null;
				for(perLevelLimits l : perLevelLimits.values())
				{
					if(l.doesRuleApplyTo(i))
						limitObj = l;
				}
				if(limitObj == null)
					continue;
				if((getSupportedSkillType()!=null) && (getSupportedSkillType()!=limitObj))
					continue;
				if(Ability.ACODE_DESCS[i].equalsIgnoreCase(combStr)
				||CMLib.english().makePlural(Ability.ACODE_DESCS[i].toLowerCase()).equalsIgnoreCase(combStr))
				{
					final int classLevel = CMLib.ableMapper().qualifyingClassLevel(mob, this);
					int numAllowed = limitObj.numAllowed(classLevel);
					int numHas = 0;
					for(final List<String> l : taughts)
					{
						if(l.size()>0)
						{
							final Ability A1=CMClass.getAbility(l.get(0));
							if(limitObj.doesRuleApplyTo(A1))
								numHas++;
						}
					}
					StringBuilder str=new StringBuilder("You may learn ");
					str.append(numAllowed-numHas).append(" more ").append(CMLib.english().makePlural(Ability.ACODE_DESCS[i].toLowerCase())).append(". ");
					str.append("\n\rAvailable ").append(CMLib.english().makePlural(Ability.ACODE_DESCS[i].toLowerCase())).append(" include: ");
					List<String> all=new ArrayList<String>(100);
					for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
					{
						final Ability A=a.nextElement();
						if((A!=null)
						&&((A.classificationCode()&Ability.ALL_ACODES)==i))
						{
							final int lowestQualifyingLevel = CMLib.ableMapper().lowestQualifyingLevel(A.ID());
							if((lowestQualifyingLevel <= classLevel)
							&&(lowestQualifyingLevel >= 1)
							&&(mob.fetchAbility(A.ID())==null))
							{
								all.add(A.name());
							}
						}
					}
					str.append(CMLib.english().toEnglishStringList(all));
					mob.tell(str.toString());
					return true;
				}
			}
		}
		if(commands.get(0).equalsIgnoreCase("FORGET") && (commands.size()>1))
		{
			commands.remove(0);
			final String name=CMParms.combine(commands);
			if(name.trim().length()==0)
				mob.tell(L("Forget what?",name));
			else
			{
				final Ability A=CMClass.findAbility(name);
				if(A!=null)
				{
					if(forget(mob,A.ID()))
						mob.tell(L("You have forgotten @x1.",A.name()));
					else
						mob.tell(L("You haven't studied @x1.",A.name()));
				}
			}
			return true;
		}
		if(commands.size()<2)
		{
			mob.tell(L("Have who teach you what?"));
			return false;
		}
		List<String> name = new XVector<String>(commands.remove(0));
		final String skillName = CMParms.combine(commands);
		Physical target = super.getAnyTarget(mob, new XVector<String>(name), givenTarget, Wearable.FILTER_UNWORNONLY,false,true);
		final Ability A;
		final int teacherClassLevel;
		final int teacherQualifyingLevel;
		if(target instanceof Item)
		{
			if(!(target instanceof SpellHolder))
			{
				commonTell(mob,L("You aren't going to learn much from @x1.",target.Name()));
				return false;
			}
			if(target.ID().indexOf("issertation")<0)
			{
				commonTell(mob,L("You don't know how to learn from @x1.",target.Name()));
				return false;
			}
			SpellHolder aC=(SpellHolder)target;
			Ability possA=(Ability)CMLib.english().fetchEnvironmental(aC.getSpells(),skillName,true);
			if(possA==null)
				possA=(Ability)CMLib.english().fetchEnvironmental(aC.getSpells(),skillName,false);
			if(possA==null)
			{
				commonTell(mob,L("@x1 doesn't seem to be about '@x2'.",target.Name(),skillName));
				return false;
			}
			A=possA;
			int lowestQualifyingLevel = CMLib.ableMapper().lowestQualifyingLevel(A.ID());
			teacherClassLevel = (target.phyStats().level() > lowestQualifyingLevel) ? target.phyStats().level() : lowestQualifyingLevel;
			teacherQualifyingLevel = lowestQualifyingLevel;
		}
		else
		if(target instanceof MOB)
		{
			final MOB targetM=(MOB)target;
			if(target == mob)
			{
				mob.tell(L("You can't teach yourself."));
				return false;
			}
			if((targetM.isAttributeSet(MOB.Attrib.NOTEACH))
			&&((!targetM.isMonster())||(!targetM.willFollowOrdersOf(mob))))
			{
				mob.tell(L("@x1 is not accepting students right now.",target.name(mob)));
				return false;
			}
			A=CMClass.findAbility(skillName, targetM);
			if(A==null)
			{
				mob.tell(L("@x1 doesn't know '@x2'.",targetM.name(mob),skillName));
				return false;
			}
			teacherClassLevel = CMLib.ableMapper().qualifyingClassLevel(targetM, A);
			teacherQualifyingLevel = CMLib.ableMapper().qualifyingLevel(targetM, A);
		}
		else
		{
			if(target != null)
				commonTell(mob,L("You can't learn anything from '@x1'.",target.Name()));
			return false;
		}
		if(mob.fetchAbility(A.ID())!=null)
		{
			mob.tell(L("You already know @x1.",A.name()));
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
			mob.tell(L("You may not study any more @x1 at this time.",CMLib.english().makePlural(Ability.ACODE_DESCS[A.classificationCode()&Ability.ALL_ACODES])));
			return false;
		}
		
		final Studying thisOne=(Studying)mob.fetchEffect(ID());
		if((thisOne.teacherP!=null)
		&&(thisOne.teachingA!=null)
		&&(thisOne.activityRoom!=null))
		{
			mob.tell(L("You are already @x1.",thisOne.verb));
			return false;
		}
		thisOne.aborted=false;
		if(!super.invoke(mob, commands, givenTarget, auto, asLevel))
			return false;

		final double quickPct = getXTIMELevel(mob) * 0.05;
		final int teachTicks = (int)(((teacherQualifyingLevel * 60000L) 
							- (10000L * (teacherClassLevel-teacherQualifyingLevel)) 
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
			final Session sess = (target instanceof MOB)?((MOB)target).session() : null;
			if(target instanceof MOB)
				thisOne.verb=L("learning @x2 from @x1",target.name(),A.name());
			else
				thisOne.verb=L("studying @x2 from @x1",target.name(),A.name());
			thisOne.displayText=L("You are @x1",verb);
			final Physical P=target;
			final Room mobroom=mob.location();
			final boolean success=this.successfullyTaught;
			thisOne.activityRoom=mobroom;
			thisOne.teachingA=null;
			thisOne.teacherP=null;
			final Runnable R=new Runnable()
			{
				final MOB		M	= mob;
				final Ability	tA	= A;
				final Physical	tP	= P;
				final Studying	oA	= thisOne;
				final boolean	ss	= success;
				//final Room		mR	= mobroom;
				@Override
				public void run()
				{
					String str=L("<S-NAME> start(s) learning @x1 from <T-NAME>.",tA.Name());
					final CMMsg msg=CMClass.getMsg(M,tP,oA,CMMsg.MSG_NOISYMOVEMENT|(auto?CMMsg.MASK_ALWAYS:0),str);
					final Room R=M.location();
					if(R!=null)
					{
						if(R.okMessage(M,msg))
						{
							R.send(M, msg);
							oA.teachingA=tA;
							oA.teacherP=tP;
							oA.successfullyTaught=ss;
							//oA.activityRoom=mR;
							int ticks=duration;
							if(ticks < 1)
								ticks = 1;
							ticks = getBeneficialTickdownTime(mob,mob,ticks,asLevel);
							oA.tickDown=ticks;
						}
					}
				}
				
			};
			if((!(target instanceof MOB)) || ((MOB)target).isMonster() || (sess==null))
				R.run();
			else
			{
				mob.tell(L("If @x1 agrees to teach you, you will begin studying together.",target.name(mob)));
				sess.prompt(new InputCallback(InputCallback.Type.CONFIRM,"N",0)
				{
					@Override
					public void showPrompt()
					{
						String timeStr;
						if(seconds<=0)
							timeStr = CMLib.lang().L("no time at all",""+seconds);
						else
						if(minutes<2)
							timeStr = CMLib.lang().L("around @x1 seconds",""+seconds);
						else
							timeStr = CMLib.lang().L("around @x1 minutes",""+minutes);
						if(P instanceof MOB)
							sess.promptPrint(L("\n\r@x1 wants you to try to teach @x2 about @x3. It will take @x4.  Is that OK (y/N)? ",
									mob.name((MOB)P), ((MOB)P).charStats().himher(), A.name(), timeStr));
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

	@Override
	public void addAbility(Ability to)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public void delAbility(Ability to)
	{
		if(to==null)
			return;
		final List<String> strList = CMParms.parseSemicolons(text(), true);
		boolean removed=false;
		for(int i=0;i<strList.size();i++)
		{
			if(strList.get(i).startsWith(to.ID()+","))
			{
				strList.remove(i);
				removed = true;
				break;
			}
		}
		if(removed)
		{
			final String text=CMParms.combineWith(strList,';');
			for(final Ability A : skillList)
			{
				if(A.ID().equalsIgnoreCase(A.ID()))
				{
					skillList.remove(A);
				}
			}
			setMiscText(text);
		}
	}

	@Override
	public int numAbilities()
	{
		return skillList.size();
	}

	@Override
	public Ability fetchAbility(int index)
	{
		return skillList.get(index);
	}

	@Override
	public Ability fetchAbility(String ID)
	{
		for(Ability A : skillList)
		{
			if(A.ID().equalsIgnoreCase(ID))
				return A;
		}
		return null;
	}

	@Override
	public Ability fetchRandomAbility()
	{
		if(numAbilities()==0)
			return null;
		return fetchAbility(CMLib.dice().roll(1, numAbilities(), -1));
	}

	@Override
	public Enumeration<Ability> abilities()
	{
		return new IteratorEnumeration<Ability>(skillList.iterator());
	}

	@Override
	public void delAllAbilities()
	{
		XVector<Ability> all=new XVector<Ability>(skillList);
		for(final Ability A : all)
			delAbility(A);
	}

	@Override
	public int numAllAbilities()
	{
		return numAbilities();
	}

	@Override
	public Enumeration<Ability> allAbilities()
	{
		return abilities();
	}
}
