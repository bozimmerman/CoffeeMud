package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class MOBTeacher extends CombatAbilities
{
	private MOB myMOB=null;

	public MOBTeacher()
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	}

	public Behavior newInstance()
	{
		return new MOBTeacher();
	}

	public void startBehavior(Environmental forMe)
	{
		if(forMe instanceof MOB)
			myMOB=(MOB)forMe;
		setParms(parms);
	}

	private void ensureCharClass()
	{
		myMOB.baseCharStats().setMyClass(CMClass.getCharClass("StdCharClass"));
		myMOB.recoverCharStats();
		Ability A=null;
		
		A=(Ability)CMClass.getAbility(getParms());
		if(A!=null)
		{
			Ability A2=myMOB.fetchAbility(A.ID());
			if(A2==null)
			{
				A=(Ability)A.copyOf();
				A.setBorrowed(myMOB,true);
				A.setProfficiency(100);
				myMOB.addAbility(A);
			}
			else
				A2.setProfficiency(100);
		}
		else
		{
			if(getParms().toUpperCase().indexOf("MAG")>=0)
				myMOB.baseCharStats().setMyClass(CMClass.getCharClass("Mage"));
			if(getParms().toUpperCase().indexOf("THI")>=0)
				myMOB.baseCharStats().setMyClass(CMClass.getCharClass("Thief"));
			if(getParms().toUpperCase().indexOf("FIG")>=0)
				myMOB.baseCharStats().setMyClass(CMClass.getCharClass("Fighter"));
			if(getParms().toUpperCase().indexOf("CLE")>=0)
				myMOB.baseCharStats().setMyClass(CMClass.getCharClass("Cleric"));
			if(getParms().toUpperCase().indexOf("RAN")>=0)
				myMOB.baseCharStats().setMyClass(CMClass.getCharClass("Ranger"));
			if(getParms().toUpperCase().indexOf("PAL")>=0)
				myMOB.baseCharStats().setMyClass(CMClass.getCharClass("Paladin"));
			if(getParms().toUpperCase().indexOf("BAR")>=0)
				myMOB.baseCharStats().setMyClass(CMClass.getCharClass("Bard"));
			if(getParms().toUpperCase().indexOf("DRU")>=0)
				myMOB.baseCharStats().setMyClass(CMClass.getCharClass("Druid"));
			
			myMOB.baseCharStats().setStat(CharStats.INTELLIGENCE,19);
			myMOB.baseCharStats().setStat(CharStats.WISDOM,19);
			myMOB.recoverCharStats();
			for(int i=0;i<CMClass.abilities.size();i++)
			{
				A=(Ability)CMClass.abilities.elementAt(i);
				Ability A2=myMOB.fetchAbility(A.ID());
				if(A2==null)
				{
					if((A.qualifiesByLevel(myMOB))
					||((myMOB.charStats().getMyClass().ID().equals("StdCharClass"))&&(CMAble.lowestQualifyingLevel(A.ID())>=0)))
					{
						A=(Ability)A.copyOf();
						A.setBorrowed(myMOB,true);
						A.setProfficiency(100);
						myMOB.addAbility(A);
					}
				}
				else
					A2.setProfficiency(100);
			}
		}
	}
	
	public void setParms(String newParms)
	{
		super.setParms(newParms);
		if(myMOB==null) return;
		MOB monster=myMOB;
		ensureCharClass();
	}

	public void affect(Environmental affecting, Affect affect)
	{
		if(myMOB==null) return;
		super.affect(affecting,affect);
		if(!canFreelyBehaveNormal(affecting))
			return;
		MOB monster=myMOB;
		MOB mob=affect.source();

		if((!affect.amISource(monster))
		&&(!mob.isMonster())
		&&(affect.sourceMessage()!=null)
		&&(affect.targetMinor()==Affect.TYP_SPEAK))
		{
			int x=affect.sourceMessage().toUpperCase().indexOf("TEACH");
			if(x<0)
				x=affect.sourceMessage().toUpperCase().indexOf("GAIN ");
			if(x>=0)
			{
				boolean giveABonus=false;
				String s=affect.sourceMessage().substring(x+5).trim();
				x=s.lastIndexOf("`");
				if(x>0) s=s.substring(0,x);
				x=s.lastIndexOf("\'");
				if(x>0) s=s.substring(0,x);

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
				Ability myAbility=CMClass.findAbility(s.trim().toUpperCase(),mob.charStats().getMyClass().ID());
				if(myAbility==null)
				{
					ExternalPlay.quickSay(monster,mob,"I'm sorry, I've never heard of "+s,true,false);
					return;
				}
				ensureCharClass();
				myAbility=monster.fetchAbility(myAbility.ID());
				if(myAbility==null)
				{
					ExternalPlay.quickSay(monster,mob,"I'm sorry, I don't know "+s,true,false);
					return;
				}
				if(giveABonus)
				{
					monster.baseCharStats().setStat(CharStats.INTELLIGENCE,25);
					monster.baseCharStats().setStat(CharStats.WISDOM,25);
					monster.recoverCharStats();
				}

				if((!myAbility.qualifiesByLevel(monster))
				&&(!monster.baseCharStats().getMyClass().ID().equals("StdCharClass")))
				{
					ExternalPlay.quickSay(monster,mob,"I'm sorry, I don't know '"+myAbility.name()+"'.",true,false);
					return;
				}
				if(mob.fetchAbility(myAbility.ID())!=null)
				{
					ExternalPlay.quickSay(monster,mob,"But you already know '"+myAbility.name()+"'.",true,false);
					return;
				}
				myAbility.setProfficiency(75);
				if(!myAbility.canBeTaughtBy(monster,mob))
					return;
				if(!myAbility.canBeLearnedBy(monster,mob))
					return;
				FullMsg msg=new FullMsg(monster,mob,null,Affect.MSG_SPEAK,null);
				if(!monster.location().okAffect(msg))
					return;
				msg=new FullMsg(monster,mob,null,Affect.MSG_OK_ACTION,"<S-NAME> teach(es) <T-NAMESELF> '"+myAbility.name()+"'.");
				if(!monster.location().okAffect(msg))
					return;
				myAbility.teach(monster,mob);
				monster.location().send(monster,msg);
				monster.baseCharStats().setStat(CharStats.INTELLIGENCE,19);
				monster.baseCharStats().setStat(CharStats.WISDOM,19);
				monster.recoverCharStats();
			}
		}

	}
}