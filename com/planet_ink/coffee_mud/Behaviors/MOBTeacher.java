package com.planet_ink.coffee_mud.Behaviors;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2006 Bo Zimmerman

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
	public String ID(){return "MOBTeacher";}
	protected MOB myMOB=null;
	protected boolean teachEverything=true;
	protected boolean noCommon=false;
	protected boolean noExpertises=false; // doubles as a "done ticking" flag
	protected int tickDownToKnowledge=4;

	public void startBehavior(Environmental forMe)
	{
		if(forMe instanceof MOB)
			myMOB=(MOB)forMe;
		setParms(parms);
	}

	protected void setTheCharClass(MOB mob, CharClass C)
	{
		if((mob.baseCharStats().numClasses()==1)
		&&(mob.baseCharStats().getMyClass(0).ID().equals("StdCharClass"))
		&&(!C.ID().equals("StdCharClass")))
		{
			mob.baseCharStats().setMyClasses(C.ID());
			mob.baseCharStats().setMyLevels(""+mob.envStats().level());
			mob.recoverCharStats();
			return;
		}
		for(int i=0;i<mob.baseCharStats().numClasses();i++)
		{
			CharClass C1=mob.baseCharStats().getMyClass(i);
			if((C1!=null)
			&&(mob.baseCharStats().getClassLevel(C1)>0))
				mob.baseCharStats().setClassLevel(C1,1);
		}
		mob.baseCharStats().setCurrentClass(C);
		mob.baseCharStats().setClassLevel(C,mob.envStats().level());
		mob.recoverCharStats();
	}

	protected void classAbles(MOB mob, Hashtable myAbles, int pct)
	{
		boolean stdCharClass=mob.charStats().getCurrentClass().ID().equals("StdCharClass");
		String className=mob.charStats().getCurrentClass().ID();
		Ability A=null;
		for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
		{
			A=(Ability)a.nextElement();
			if((((stdCharClass&&(CMLib.ableMapper().lowestQualifyingLevel(A.ID())>0)))
				||(CMLib.ableMapper().qualifiesByLevel(mob,A)&&(!CMLib.ableMapper().getSecretSkill(className,true,A.ID()))))
			&&((!noCommon)||((A.classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_COMMON_SKILL))
			&&((!stdCharClass)||(CMLib.ableMapper().availableToTheme(A.ID(),Area.THEME_FANTASY,true))))
				addAbility(mob,A,pct,myAbles);
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Tickable.TICKID_MOB)
		&&(!noExpertises)
		&&((--tickDownToKnowledge)==0)
		&&(ticking instanceof MOB))
		{
			noExpertises=true;
			MOB mob=(MOB)ticking;
			if(teachEverything)
			{
				for(Enumeration e=CMLib.expertises().definitions();e.hasMoreElements();)
				{
					ExpertiseLibrary.ExpertiseDefinition def=(ExpertiseLibrary.ExpertiseDefinition)e.nextElement();
					if(mob.fetchExpertise(def.ID)==null)
						mob.addExpertise(def.ID);
				}
			}
			else
			{
				boolean someNew=true;
				CharStats oldBase=(CharStats)mob.baseCharStats().copyOf();
				for(int i=0;i<CharStats.NUM_BASE_STATS;i++)
					mob.baseCharStats().setStat(i,100);
				mob.recoverCharStats();
				while(someNew)
				{
					someNew=false;
					Vector V=CMLib.expertises().myQualifiedExpertises(mob);
					ExpertiseLibrary.ExpertiseDefinition def=null;
					for(int v=0;v<V.size();v++)
					{
						def=(ExpertiseLibrary.ExpertiseDefinition)V.elementAt(v);
						if(mob.fetchExpertise(def.ID)==null)
						{
							mob.addExpertise(def.ID);
							someNew=true;
						}
					}
				}
				mob.setBaseCharStats(oldBase);
				mob.recoverCharStats();
			}
		}
		return super.tick(ticking,tickID);
	}
	
	public void addAbility(MOB mob, Ability A, int pct, Hashtable myAbles)
	{
		if(CMLib.dice().rollPercentage()<=pct)
		{
			Ability A2=(Ability)myAbles.get(A.ID());
			if(A2==null)
			{
				A=(Ability)A.copyOf();
				A.setSavable(false);
				A.setProficiency(100);
				myAbles.put(A.ID(),A);
				mob.addAbility(A);
			}
			else
				A2.setProficiency(100);
		}
	}

	protected void ensureCharClass()
	{
		myMOB.baseCharStats().setMyClasses("StdCharClass");
		myMOB.baseCharStats().setMyLevels(""+myMOB.envStats().level());
		myMOB.recoverCharStats();

		Hashtable myAbles=new Hashtable();
		Ability A=null;
		for(int a=0;a<myMOB.numAbilities();a++)
		{
			A=myMOB.fetchAbility(a);
			if(A!=null) myAbles.put(A.ID(),A);
		}
		myMOB.baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,19);
		myMOB.baseCharStats().setStat(CharStats.STAT_WISDOM,19);

		int pct=100;
		Vector V=null;
		A=CMClass.getAbility(getParms());
		if(A!=null)
		{
			addAbility(myMOB,A,pct,myAbles);
			teachEverything=false;
		}
		else
			V=CMParms.parse(getParms());

		if(V!=null)
		for(int v=V.size()-1;v>=0;v--)
		{
			String s=(String)V.elementAt(v);
			if(s.equalsIgnoreCase("NOCOMMON"))
			{
				noCommon=true;
				V.removeElementAt(v);
			}
			if(s.equalsIgnoreCase("NOEXPS")||s.equalsIgnoreCase("NOEXP"))
			{
				noExpertises=true;
				V.removeElementAt(v);
			}
		}

		if(V!=null)
		for(int v=0;v<V.size();v++)
		{
			String s=(String)V.elementAt(v);
			if(s.endsWith("%"))
			{
				pct=CMath.s_int(s.substring(0,s.length()-1));
				continue;
			}

			A=CMClass.getAbility(s);
			CharClass C=CMClass.findCharClass(s);
			if((C!=null)&&(!C.ID().equals("StdCharClass")))
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
				ExpertiseLibrary.ExpertiseDefinition def=CMLib.expertises().getDefinition(s);
				if(def!=null)
				{
					myMOB.addExpertise(def.ID);
					teachEverything=false;
				}
			}
		}
		myMOB.recoverCharStats();
		if((myMOB.charStats().getCurrentClass().ID().equals("StdCharClass"))
		&&(teachEverything))
			classAbles(myMOB,myAbles,pct);
		int lvl=myMOB.envStats().level()/myMOB.baseCharStats().numClasses();
		if(lvl<1) lvl=1;
		for(int i=0;i<myMOB.baseCharStats().numClasses();i++)
		{
			CharClass C=myMOB.baseCharStats().getMyClass(i);
			if((C!=null)&&(myMOB.baseCharStats().getClassLevel(C)>=0))
				myMOB.baseCharStats().setClassLevel(C,lvl);
		}
		myMOB.recoverCharStats();
	}

	public void setParms(String newParms)
	{
		super.setParms(newParms);
		if(myMOB==null) return;
		teachEverything=true;
		noCommon=false;
		noExpertises=false;
		tickDownToKnowledge=4;
		ensureCharClass();
	}

	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(host instanceof MOB)
		{
			if(CMath.bset(((MOB)host).getBitmap(),MOB.ATT_NOTEACH))
				((MOB)host).setBitmap(CMath.unsetb(((MOB)host).getBitmap(),MOB.ATT_NOTEACH));
		}
		return super.okMessage(host,msg);
	}
	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		if(myMOB==null) return;
		super.executeMsg(affecting,msg);
		if(!canFreelyBehaveNormal(affecting))
			return;
		MOB monster=myMOB;
		MOB mob=msg.source();

		if((!msg.amISource(monster))
		&&(!mob.isMonster())
		&&(msg.sourceMessage()!=null)
		&&((msg.target()==null)||msg.amITarget(monster))
		&&(msg.targetMinor()==CMMsg.TYP_SPEAK))
		{
			int x=msg.sourceMessage().toUpperCase().indexOf("TEACH");
			if(x<0)
				x=msg.sourceMessage().toUpperCase().indexOf("GAIN ");
			if(x>=0)
			{
				boolean giveABonus=false;
				String s=msg.sourceMessage().substring(x+5).trim();
				x=s.lastIndexOf("\'");
				if(x>0)
					s=s.substring(0,x);
				else
				{
					x=s.lastIndexOf("`");
					if(x>0) 
						s=s.substring(0,x);
				}

				if(s.startsWith("\"")) s=s.substring(1).trim();
				if(s.endsWith("\"")) s=s.substring(0,s.length()-1);
				if(s.toUpperCase().endsWith("PLEASE"))
					s=s.substring(0,s.length()-6).trim();
				if(s.startsWith("\"")) s=s.substring(1).trim();
				if(s.endsWith("\"")) s=s.substring(0,s.length()-1);
				if(s.toUpperCase().startsWith("PLEASE "))
				{
					giveABonus=true;
					s=s.substring(6).trim();
				}
				if(s.startsWith("\"")) s=s.substring(1).trim();
				if(s.endsWith("\"")) s=s.substring(0,s.length()-1);
				if(s.toUpperCase().startsWith("ME "))
					s=s.substring(3).trim();
				if(s.startsWith("\"")) s=s.substring(1).trim();
				if(s.endsWith("\"")) s=s.substring(0,s.length()-1);
				if(s.toUpperCase().startsWith("PLEASE "))
				{
					giveABonus=true;
					s=s.substring(6).trim();
				}
				if(s.toUpperCase().startsWith("ME "))
					s=s.substring(3).trim();
				if(s.startsWith("\"")) s=s.substring(1).trim();
				if(s.endsWith("\"")) s=s.substring(0,s.length()-1);
				if(s.trim().equalsIgnoreCase("LIST"))
				{
					CMLib.commands().postSay(monster,mob,"Try the QUALIFY command.",true,false);
					return;
				}
				Ability myAbility=CMClass.findAbility(s.trim().toUpperCase(),monster);
				if(myAbility==null)
				{
			    	boolean foundEXP=false;
			    	boolean foundClass=false;
			    	for(Enumeration e=CMLib.expertises().definitions();e.hasMoreElements();)
			    	{
			    		ExpertiseLibrary.ExpertiseDefinition def=(ExpertiseLibrary.ExpertiseDefinition)e.nextElement();
			    		if(CMLib.english().containsString(def.name,s))
			    		{
			    			s=def.name;
			    			foundEXP=true;
			    			break;
			    		}
			    	}
			    	if((!foundEXP)&&(CMClass.findCharClass(s.trim())!=null))
			    		foundClass=true;
			    	if(foundEXP)
						CMLib.commands().postSay(monster,mob,"I've heard of "+s+", but that's an expertise-- try TRAINing it.",true,false);
			    	else
			    	if(foundClass)
						CMLib.commands().postSay(monster,mob,"I've heard of "+s+", but that's an class-- try TRAINing  for it.",true,false);
			    	else
						CMLib.commands().postSay(monster,mob,"I'm sorry, I've never heard of "+s,true,false);
					return;
				}
				if(giveABonus)
				{
					monster.baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,25);
					monster.baseCharStats().setStat(CharStats.STAT_WISDOM,25);
					monster.recoverCharStats();
				}

				if(mob.fetchAbility(myAbility.ID())!=null)
				{
					CMLib.commands().postSay(monster,mob,"But you already know '"+myAbility.name()+"'.",true,false);
					return;
				}
				myAbility.setProficiency(75);
				if(!myAbility.canBeTaughtBy(monster,mob))
					return;
				if(!myAbility.canBeLearnedBy(monster,mob))
					return;
				CMMsg msg2=CMClass.getMsg(monster,mob,null,CMMsg.MSG_SPEAK,null);
				if(!monster.location().okMessage(monster,msg2))
					return;
				msg2=CMClass.getMsg(monster,mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> teach(es) <T-NAMESELF> '"+myAbility.name()+"'.");
				if(!monster.location().okMessage(monster,msg2))
					return;
				myAbility.teach(monster,mob);
				monster.location().send(monster,msg2);
				monster.baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,19);
				monster.baseCharStats().setStat(CharStats.STAT_WISDOM,19);
				monster.recoverCharStats();
			}
		}

	}
}
