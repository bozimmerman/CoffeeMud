package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class MOBTeacher extends CombatAbilities
{
	public String ID(){return "MOBTeacher";}
	private MOB myMOB=null;
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

	private void setTheCharClass(MOB mob, CharClass C)
	{
		for(int i=0;i<mob.baseCharStats().numClasses();i++)
		{
			CharClass C1=mob.baseCharStats().getMyClass(i);
			if((C1!=null)&&(mob.baseCharStats().getClassLevel(C1)>0))
				mob.baseCharStats().setClassLevel(C1,1);
		}
		mob.baseCharStats().setCurrentClass(C);
		mob.baseCharStats().setClassLevel(C,mob.envStats().level());
		mob.recoverCharStats();
	}

	private void classAbles(MOB mob, Hashtable myAbles, int pct)
	{
		boolean stdCharClass=mob.charStats().getCurrentClass().ID().equals("StdCharClass");
		String className=mob.charStats().getCurrentClass().ID();
		for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
		{
			Ability A=(Ability)a.nextElement();
			if(((stdCharClass&&(CMAble.lowestQualifyingLevel(A.ID())>0)))
			   ||(CMAble.qualifiesByLevel(mob,A)&&(!CMAble.getSecretSkill(className,A.ID()))))
				addAbility(mob,A,pct,myAbles);
		}
	}

	public void addAbility(MOB mob, Ability A, int pct, Hashtable myAbles)
	{
		if(Dice.rollPercentage()<pct)
		{
			Ability A2=(Ability)myAbles.get(A.ID());
			if(A2==null)
			{
				A=(Ability)A.copyOf();
				A.setBorrowed(myMOB,true);
				A.setProfficiency(100);
				myAbles.put(A.ID(),A);
				mob.addAbility(A);
			}
			else
				A2.setProfficiency(100);
		}
	}
	
	private void ensureCharClass()
	{
		setTheCharClass(myMOB,CMClass.getCharClass("StdCharClass"));
		myMOB.recoverCharStats();
		Hashtable myAbles=new Hashtable();
		Ability A=null;
		for(int a=0;a<myMOB.numAbilities();a++)
		{
			A=myMOB.fetchAbility(a);
			if(A!=null) myAbles.put(A.ID(),A);
		}
		myMOB.baseCharStats().setStat(CharStats.INTELLIGENCE,19);
		myMOB.baseCharStats().setStat(CharStats.WISDOM,19);
		Vector V=Util.parse(getParms());
		
		int pct=100;
		
		A=(Ability)CMClass.getAbility(getParms());
		if(A!=null)
			addAbility(myMOB,A,pct,myAbles);
		else
		for(int v=0;v<V.size();v++)
		{
			String s=(String)V.elementAt(v);
			if(s.endsWith("%"))
			{
				pct=Util.s_int(s.substring(0,s.length()-1));
				continue;
			}
			A=(Ability)CMClass.getAbility(s);
			CharClass C=CMClass.getCharClass(s);
			if(A!=null)
				addAbility(myMOB,A,pct,myAbles);
			else
			if(C!=null)
			{
				setTheCharClass(myMOB,C);
				classAbles(myMOB,myAbles,pct);
				myMOB.recoverCharStats();
			}
		}
		myMOB.recoverCharStats();
		if(myMOB.charStats().getCurrentClass().ID().equals("StdCharClass"))
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
		ensureCharClass();
	}

	public boolean okAffect(Environmental host, Affect msg)
	{
		if(host instanceof MOB)
		{
			if(Util.bset(((MOB)host).getBitmap(),MOB.ATT_NOTEACH))
				((MOB)host).setBitmap(Util.unsetb(((MOB)host).getBitmap(),MOB.ATT_NOTEACH));
		}
		return super.okAffect(host,msg);
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
		&&((affect.target()==null)||affect.amITarget(monster))
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
				Ability myAbility=CMClass.findAbility(s.trim().toUpperCase(),mob.charStats());
				if(myAbility==null)
				{
					ExternalPlay.quickSay(monster,mob,"I'm sorry, I've never heard of "+s,true,false);
					return;
				}
				//ensureCharClass();
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
				if(!monster.location().okAffect(monster,msg))
					return;
				msg=new FullMsg(monster,mob,null,Affect.MSG_OK_ACTION,"<S-NAME> teach(es) <T-NAMESELF> '"+myAbility.name()+"'.");
				if(!monster.location().okAffect(monster,msg))
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