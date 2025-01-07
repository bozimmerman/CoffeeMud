package com.planet_ink.coffee_mud.Behaviors;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.SecretFlag;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary.ExpertiseDefinition;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.util.Map.Entry;

/*
   Copyright 2001-2024 Bo Zimmerman

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

public class MOBTeacher extends CombatAbilities
{
	@Override
	public String ID()
	{
		return "MOBTeacher";
	}

	protected MOB		myMOB				= null;
	protected boolean	teachEverything		= true;
	protected boolean	noCommon			= false;
	protected boolean	noExpertises		= false;  // doubles as a "done ticking" flag
	protected boolean	noHLExpertises		= false;
	protected int		tickDownToKnowledge	= 4;

	protected List<ExpertiseDefinition> trainableExpertises = null;

	@Override
	public String accountForYourself()
	{
		return "skill teaching";
	}

	@Override
	public void startBehavior(final PhysicalAgent forMe)
	{
		if(forMe instanceof MOB)
			myMOB=(MOB)forMe;
		setParms(parms);
	}

	protected void setTheCharClass(final MOB mob, final CharClass C)
	{
		if((mob.baseCharStats().numClasses()==1)
		&&(mob.baseCharStats().getMyClass(0).ID().equals("StdCharClass"))
		&&(!C.ID().equals("StdCharClass")))
		{
			mob.baseCharStats().setAllClassInfo(C.ID(), ""+mob.phyStats().level());
			mob.recoverCharStats();
			return;
		}
		for(int i=0;i<mob.baseCharStats().numClasses();i++)
		{
			final CharClass C1=mob.baseCharStats().getMyClass(i);
			if((C1!=null)
			&&(mob.baseCharStats().getClassLevel(C1)>0))
				mob.baseCharStats().setClassLevel(C1,1);
		}
		mob.baseCharStats().setCurrentClass(C);
		mob.baseCharStats().setClassLevel(C,mob.phyStats().level());
		mob.recoverCharStats();
	}

	protected void classAbles(final MOB mob, final Map<String,Ability> myAbles, final int pct)
	{
		final boolean stdCharClass=mob.charStats().getCurrentClass().ID().equals("StdCharClass");
		final String className=mob.charStats().getCurrentClass().ID();
		Ability A=null;
		final AbilityMapper ableMap = CMLib.ableMapper();
		for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
		{
			A=a.nextElement();
			if(A==null)
				continue;
			if((noCommon)
			&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL))
				continue;
			if(stdCharClass)
			{
				if(!ableMap.availableToTheme(A.ID(),Area.THEME_FANTASY,true))
					continue;
				if((ableMap.lowestQualifyingLevel(A.ID())<0)
				||(A instanceof ArchonOnly))
					continue;
				final SecretFlag secret = ableMap.getSecretSkill(A.ID());
				if(secret != SecretFlag.PUBLIC)
					continue;
			}
			else
			{
				if(!ableMap.qualifiesByLevel(mob, A))
					continue;
				final SecretFlag secret = ableMap.getSecretSkill(className,true,A.ID());
				if((secret==SecretFlag.SECRET)
				||((secret==SecretFlag.MASKED)
					&&(!CMLib.masking().maskCheck(CMLib.ableMapper().getExtraMask(className, true, A.ID()), mob, true))))
					continue;
			}
			addAbility(mob,A,pct,myAbles);
		}
		for(final ClanGovernment G : CMLib.clans().getStockGovernments())
		{
			G.getClanLevelAbilities(null,null,Integer.valueOf(Integer.MAX_VALUE));
			for(final Enumeration<AbilityMapping> m= CMLib.ableMapper().getClassAbles(G.getName(), false);m.hasMoreElements();)
			{
				final AbilityMapping M=m.nextElement();
				final Ability A2=CMClass.getAbility(M.abilityID());
				addAbility(mob,A2,pct,myAbles);
			}
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((tickID==Tickable.TICKID_MOB)
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.MOBTEACHER))
		&&((--tickDownToKnowledge)==0)
		&&(ticking instanceof MOB))
		{
			if(!noExpertises)
			{
				noExpertises=true;
				final MOB mob=(MOB)ticking;
				if(teachEverything)
				{
					for(final Enumeration<ExpertiseLibrary.ExpertiseDefinition> e=CMLib.expertises().definitions();e.hasMoreElements();)
						mob.addExpertise(e.nextElement().ID());
					trainableExpertises=null;
				}
				else
				{
					boolean someNew=true;
					final CharStats oldBase=(CharStats)mob.baseCharStats().copyOf();
					for(final int i: CharStats.CODES.BASECODES())
						mob.baseCharStats().setStat(i,100);
					for(int i=0;i<mob.baseCharStats().numClasses();i++)
						mob.baseCharStats().setClassLevel(mob.baseCharStats().getMyClass(i),100);
					mob.recoverCharStats();
					while(someNew)
					{
						someNew=false;
						final List<ExpertiseDefinition> V=CMLib.expertises().myQualifiedExpertises(mob);
						ExpertiseLibrary.ExpertiseDefinition def=null;
						for(int v=0;v<V.size();v++)
						{
							def=V.get(v);
							if(mob.fetchExpertise(def.ID())==null)
							{
								mob.addExpertise(def.ID());
								someNew=true;
							}
						}
						if(someNew)
							trainableExpertises=null;
					}
					mob.setBaseCharStats(oldBase);
					mob.recoverCharStats();
				}
			}

		}
		return super.tick(ticking,tickID);
	}

	public void addAbility(final MOB mob, Ability A, final int pct, final Map<String,Ability> myAbles)
	{
		if(CMLib.dice().rollPercentage()<=pct)
		{
			final Ability A2=myAbles.get(A.ID());
			if(A2==null)
			{
				A=(Ability)A.copyOf();
				A.setSavable(false);
				A.setProficiency(CMLib.ableMapper().getMaxProficiency(mob,true,A.ID()));
				myAbles.put(A.ID(),A);
				mob.addAbility(A);
			}
			else
				A2.setProficiency(CMLib.ableMapper().getMaxProficiency(mob,true,A2.ID()));
		}
	}

	protected void ensureCharClass()
	{
		myMOB.baseCharStats().setAllClassInfo("StdCharClass", ""+myMOB.phyStats().level());
		myMOB.recoverCharStats();

		final Map<String,Ability> myAbles=new HashMap<String,Ability>();
		Ability A=null;
		for(final Enumeration<Ability> a=myMOB.allAbilities();a.hasMoreElements();)
		{
			A=a.nextElement();
			if(A!=null)
				myAbles.put(A.ID(),A);
		}
		myMOB.baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,19);
		myMOB.baseCharStats().setStat(CharStats.STAT_WISDOM,19);

		int pct=100;
		List<String> V=null;
		A=CMClass.getAbility(getParms());
		if(A!=null)
		{
			addAbility(myMOB,A,pct,myAbles);
			teachEverything=false;
		}
		else
			V=CMParms.parse(getParms());

		if(V!=null)
		{
			for(int v=V.size()-1;v>=0;v--)
			{
				final String s=V.get(v);
				if(s.equalsIgnoreCase("NOCOMMON"))
				{
					noCommon=true;
					V.remove(v);
				}
				if(s.equalsIgnoreCase("NOEXPS")||s.equalsIgnoreCase("NOEXP"))
				{
					noExpertises=true;
					V.remove(v);
				}
				if(s.equalsIgnoreCase("NOHLEXPS")||s.equalsIgnoreCase("NOHLEXP"))
				{
					noHLExpertises=true;
					V.remove(v);
				}
			}
		}

		if(V!=null)
		{
			for(int v=0;v<V.size();v++)
			{
				final String s=V.get(v);
				if(s.endsWith("%"))
				{
					pct=CMath.s_int(s.substring(0,s.length()-1));
					continue;
				}

				A=CMClass.getAbility(s);
				final CharClass C=CMClass.findCharClass(s);
				if((C!=null)&&(C.availabilityCode()!=0))
				{
					teachEverything=false;
					setTheCharClass(myMOB,C);
					classAbles(myMOB,myAbles,pct);
					myMOB.recoverCharStats();
				}
				else
				if(A!=null)
				{
					addAbility(myMOB,A,pct,myAbles);
					teachEverything=false;
				}
				else
				{
					final ExpertiseLibrary.ExpertiseDefinition def=CMLib.expertises().getDefinition(s);
					if(def!=null)
					{
						myMOB.addExpertise(def.ID());
						teachEverything=false;
					}
				}
			}
		}
		myMOB.recoverCharStats();
		if((myMOB.charStats().getCurrentClass().ID().equals("StdCharClass"))
		&&(teachEverything))
			classAbles(myMOB,myAbles,pct);
		int lvl=myMOB.phyStats().level()/myMOB.baseCharStats().numClasses();
		if(lvl<1)
			lvl=1;
		for(int i=0;i<myMOB.baseCharStats().numClasses();i++)
		{
			final CharClass C=myMOB.baseCharStats().getMyClass(i);
			if((C!=null)&&(myMOB.baseCharStats().getClassLevel(C)>=0))
				myMOB.baseCharStats().setClassLevel(C,lvl);
		}
		myMOB.recoverCharStats();
	}

	@Override
	public void setParms(final String newParms)
	{
		super.setParms(newParms);
		if(myMOB==null)
			return;
		teachEverything=true;
		noCommon=false;
		noExpertises=false;
		tickDownToKnowledge=4;
		trainableExpertises=null;
		ensureCharClass();
	}

	@Override
	public boolean okMessage(final Environmental host, final CMMsg msg)
	{
		if(host instanceof MOB)
		{
			if(((MOB)host).isAttributeSet(MOB.Attrib.NOTEACH))
				((MOB)host).setAttribute(MOB.Attrib.NOTEACH,false);
		}
		return super.okMessage(host,msg);
	}

	@Override
	public void executeMsg(final Environmental affecting, final CMMsg msg)
	{
		if(myMOB==null)
			return;
		super.executeMsg(affecting,msg);
		if(!canFreelyBehaveNormal(affecting))
			return;
		final MOB monster=myMOB;
		final MOB student=msg.source();

		if((!msg.amISource(monster))
		&&(!student.isMonster())
		&&(msg.sourceMessage()!=null)
		&&((msg.target()==null)||msg.amITarget(monster))
		&&(msg.targetMinor()==CMMsg.TYP_SPEAK)
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.MOBTEACHER)))
		{
			String sayMsg=CMStrings.getSayFromMessage(msg.sourceMessage());
			if(sayMsg==null)
			{
				final int start=msg.sourceMessage().indexOf('\'');
				if(start>0)
				{
					sayMsg=msg.sourceMessage().substring(start+1);
					final int x=sayMsg.lastIndexOf("\'");
					if(x>0)
						sayMsg=sayMsg.substring(0,x);
				}
				else
					sayMsg=msg.sourceMessage();
			}
			final int x1=sayMsg.toUpperCase().indexOf("TEACH");
			final int x2=sayMsg.toUpperCase().indexOf("GAIN ");
			int x=x1;
			if((x1<0)||((x2<x)&&(x2>=0)))
				x=x2;
			if(x>=0)
			{
				boolean giveABonus=false;
				String s=sayMsg.substring(x+5).trim();
				if(s.startsWith("\""))
					s=s.substring(1).trim();
				if(s.endsWith("\""))
					s=s.substring(0,s.length()-1);
				if(s.toUpperCase().endsWith("PLEASE"))
					s=s.substring(0,s.length()-6).trim();
				if(s.startsWith("\""))
					s=s.substring(1).trim();
				if(s.endsWith("\""))
					s=s.substring(0,s.length()-1);
				if(s.toUpperCase().startsWith("PLEASE "))
				{
					giveABonus=true;
					s=s.substring(6).trim();
				}
				if(s.startsWith("\""))
					s=s.substring(1).trim();
				if(s.endsWith("\""))
					s=s.substring(0,s.length()-1);
				if(s.toUpperCase().startsWith("ME "))
					s=s.substring(3).trim();
				if(s.startsWith("\""))
					s=s.substring(1).trim();
				if(s.endsWith("\""))
					s=s.substring(0,s.length()-1);
				if(s.toUpperCase().startsWith("PLEASE "))
				{
					giveABonus=true;
					s=s.substring(6).trim();
				}
				if(s.toUpperCase().startsWith("ME "))
					s=s.substring(3).trim();
				if(s.startsWith("\""))
					s=s.substring(1).trim();
				if(s.endsWith("\""))
					s=s.substring(0,s.length()-1);
				if(s.trim().equalsIgnoreCase("LIST"))
				{
					CMLib.commands().postSay(monster,student,L("Try the QUALIFY command."),true,false);
					return;
				}
				if(s.trim().toUpperCase().equals("ALL"))
				{
					CMLib.commands().postSay(monster,student,L("I can't teach you everything at once. Try the QUALIFY command."),true,false);
					return;
				}
				if(!CMLib.flags().canBeSeenBy(student,monster))
				{
					CMLib.commands().postSay(monster,student,L("I can't see you, so I can't teach you."),true,false);
					return;
				}
				if(!CMLib.flags().canBeSeenBy(monster,student))
				{
					CMLib.commands().postSay(monster,student,L("You can't see me, so I can't teach you."),true,false);
					return;
				}
				Ability possA=null;
				final String calledThis = s.trim().toUpperCase();
				final AbilityMapper ableMapper = CMLib.ableMapper();
				final List<Ability> possAs=new LinkedList<Ability>();
				for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
				{
					final Ability A1=a.nextElement();
					final boolean qualifies = ableMapper.qualifiesByLevel(student,A1);
					if(qualifies  && (student.fetchAbility(A1.ID())==null))
						possAs.add(A1);
					if((A1.name().equalsIgnoreCase(calledThis)||A1.ID().equalsIgnoreCase(calledThis))
					&&(qualifies || (possA == null))
					&&(qualifies || ableMapper.qualifiesByAnyCharClassOrRace(A1.ID())))
						possA=A1;
				}
				if(possA==null)
					possA=(Ability)CMLib.english().fetchEnvironmental(possAs,calledThis,true);
				if(possA==null)
					possA=(Ability)CMLib.english().fetchEnvironmental(possAs,calledThis,false);
				Ability teachA=null;
				if(possA != null)
					teachA=monster.fetchAbility(possA.ID());
				if(teachA == null)
					teachA=CMClass.findAbility(calledThis,monster);
				if(teachA==null)
				{
					ExpertiseLibrary.ExpertiseDefinition theExpertise=null;
					if(trainableExpertises==null)
					{
						trainableExpertises=new LinkedList<ExpertiseLibrary.ExpertiseDefinition>();
						trainableExpertises.addAll(CMLib.expertises().myListableExpertises(monster));
						for(final Enumeration<String> exi=monster.expertises();exi.hasMoreElements();)
						{
							final Pair<String,Integer> EXI=monster.fetchExpertise(exi.nextElement());
							if(EXI.getValue()==null)
							{
								final ExpertiseLibrary.ExpertiseDefinition def=CMLib.expertises().getDefinition(EXI.getKey());
								if((def != null) && (!trainableExpertises.contains(def)))
									trainableExpertises.add(def);
							}
							else
							{
								final List<String> childrenIDs=CMLib.expertises().getStageCodes(EXI.getKey());
								for(final String experID : childrenIDs)
								{
									final ExpertiseLibrary.ExpertiseDefinition def=CMLib.expertises().getDefinition(experID);
									if((def != null) && (!trainableExpertises.contains(def)))
										trainableExpertises.add(def);
								}
							}
						}
					}
					for(final ExpertiseLibrary.ExpertiseDefinition def : trainableExpertises)
					{
						if((def.name().equalsIgnoreCase(s))
						&&(theExpertise==null))
							theExpertise=def;
					}
					if(theExpertise==null)
					{
						for(final ExpertiseLibrary.ExpertiseDefinition def : trainableExpertises)
						{
							if((CMLib.english().containsString(def.name(),s)
							&&(theExpertise==null)))
								theExpertise=def;
						}
					}
					if(theExpertise!=null)
					{
						if(!CMLib.expertises().postTeach(monster,student,theExpertise))
							return;
					}
					else
					if((CMClass.findCharClass(s.trim())!=null))
						CMLib.commands().postSay(monster,student,L("I've heard of @x1, but that's an class-- try TRAINing  for it.",s),true,false);
					else
					{
						for(final Enumeration<ExpertiseLibrary.ExpertiseDefinition> e=CMLib.expertises().definitions(); e.hasMoreElements();)
						{
							final ExpertiseLibrary.ExpertiseDefinition def=e.nextElement();
							if(def.name().equalsIgnoreCase(s))
							{
								theExpertise=def;
								break;
							}
						}
						if(theExpertise==null)
							CMLib.commands().postSay(monster,student,L("I'm sorry, but I've never heard of @x1",s),true,false);
						else
							CMLib.commands().postSay(monster,student,L("I'm sorry, but I do not know @x1.",theExpertise.name()));
					}
					return;
				}
				if(giveABonus)
				{
					monster.baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,25);
					monster.baseCharStats().setStat(CharStats.STAT_WISDOM,25);
					monster.recoverCharStats();
				}

				teachA = (Ability)teachA.copyOf();
				final double max75 =CMath.div(CMProps.getIntVar(CMProps.Int.PRACMAXPCT), 100.0);
				final int prof75=(int)Math.round(CMath.mul(CMLib.ableMapper().getMaxProficiency(student,true,teachA.ID()),max75));
				teachA.setProficiency(prof75/2);
				CMLib.expertises().postTeach(monster,student,teachA);
				monster.baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,19);
				monster.baseCharStats().setStat(CharStats.STAT_WISDOM,19);
				monster.recoverCharStats();
			}
		}

	}
}
