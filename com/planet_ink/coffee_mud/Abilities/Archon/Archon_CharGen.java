package com.planet_ink.coffee_mud.Abilities.Archon;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.io.*;
import java.util.*;


public class Archon_CharGen extends ArchonSkill
{
	public Archon_CharGen()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="CharGen";


		baseEnvStats().setLevel(1);
		addQualifyingClass("Archon",1);
		triggerStrings.addElement("CHARGEN");

	}

	public Environmental newInstance()
	{
		return new Archon_CharGen();
	}
	private MOB levelMOBup(int level, CharClass C)
	{
		MOB mob=(MOB)CMClass.getMOB("StdMOB").newInstance();
		mob.setAlignment(500);
		mob.setName("Average Joe");
		mob.baseCharStats().setMyRace(CMClass.getRace("Human"));
		mob.baseCharStats().setGender('M');
		mob.baseCharStats().setStrength(1+(int)Math.round(CharStats.AVG_VALUE));
		mob.baseCharStats().setWisdom(1+(int)Math.round(CharStats.AVG_VALUE));
		mob.baseCharStats().setIntelligence((int)Math.round(CharStats.AVG_VALUE));
		mob.baseCharStats().setDexterity(1+(int)Math.round(CharStats.AVG_VALUE));
		mob.baseCharStats().setConstitution((int)Math.round(CharStats.AVG_VALUE));
		mob.baseCharStats().setCharisma((int)Math.round(CharStats.AVG_VALUE));
		mob.baseCharStats().setMyClass(C);
		mob.baseEnvStats().setArmor(50);
		mob.baseEnvStats().setLevel(1);
		mob.baseEnvStats().setSensesMask(0);
		mob.baseState().setHitPoints(20);
		mob.baseState().setMovement(100);
		mob.baseState().setMana(100);
		mob.baseCharStats().getMyRace().newCharacter(mob);
		mob.recoverCharStats();
		mob.recoverEnvStats();
		mob.recoverMaxState();
		mob.resetToMaxState();
		mob.baseCharStats().getMyClass().newCharacter(mob,false);

		for(int lvl=1;lvl<level;lvl++)
		{
			switch(lvl % 6)
			{
			case 0:
				mob.baseCharStats().setStrength(mob.baseCharStats().getStrength()+1);
				break;
			case 1:
				mob.baseCharStats().setDexterity(mob.baseCharStats().getDexterity()+1);
				break;
			case 2:
				mob.baseCharStats().setIntelligence(mob.baseCharStats().getIntelligence()+1);
				break;
			case 3:
				mob.baseCharStats().setConstitution(mob.baseCharStats().getConstitution()+1);
				break;
			case 4:
				mob.baseCharStats().setCharisma(mob.baseCharStats().getCharisma()+1);
				break;
			case 5:
				mob.baseCharStats().setWisdom(mob.baseCharStats().getWisdom()+1);
				break;
			}
			int oldattack=mob.baseEnvStats().attackAdjustment();
			mob.charStats().getMyClass().gainExperience(mob,null,mob.getExpNeededLevel()+1);
			mob.recoverEnvStats();
			mob.recoverCharStats();
			mob.recoverMaxState();
			int newAttack=mob.baseEnvStats().attackAdjustment()-oldattack;
			mob.baseEnvStats().setArmor(mob.baseEnvStats().armor()-newAttack);
			mob.recoverEnvStats();
			mob.recoverCharStats();
			mob.recoverMaxState();
		}
		return mob;
	}

	public void averageout(MOB avgMob, int tries)
	{
		avgMob.baseCharStats().setStrength((int)Math.round(Util.div(avgMob.baseCharStats().getStrength(),tries)));
		avgMob.baseCharStats().setWisdom((int)Math.round(Util.div(avgMob.baseCharStats().getWisdom(),tries)));
		avgMob.baseCharStats().setIntelligence((int)Math.round(Util.div(avgMob.baseCharStats().getIntelligence(),tries)));
		avgMob.baseCharStats().setDexterity((int)Math.round(Util.div(avgMob.baseCharStats().getDexterity(),tries)));
		avgMob.baseCharStats().setConstitution((int)Math.round(Util.div(avgMob.baseCharStats().getConstitution(),tries)));
		avgMob.baseCharStats().setCharisma((int)Math.round(Util.div(avgMob.baseCharStats().getCharisma(),tries)));
		avgMob.baseEnvStats().setArmor((int)Math.round(Util.div(avgMob.baseEnvStats().armor(),tries)));
		avgMob.baseState().setHitPoints((int)Math.round(Util.div(avgMob.baseState().getHitPoints(),tries)));
		avgMob.baseState().setMovement((int)Math.round(Util.div(avgMob.baseState().getMovement(),tries)));
		avgMob.baseState().setMana((int)Math.round(Util.div(avgMob.baseState().getMana(),tries)));
		avgMob.recoverCharStats();
		avgMob.recoverEnvStats();
		avgMob.recoverMaxState();
		avgMob.resetToMaxState();
		avgMob.setTrains(0);
	}

	public void addHimIn(MOB avgMob, MOB mob2)
	{
		avgMob.baseCharStats().setStrength(avgMob.baseCharStats().getStrength()+mob2.baseCharStats().getStrength());
		avgMob.baseCharStats().setWisdom(avgMob.baseCharStats().getWisdom()+mob2.baseCharStats().getWisdom());
		avgMob.baseCharStats().setIntelligence(avgMob.baseCharStats().getIntelligence()+mob2.baseCharStats().getIntelligence());
		avgMob.baseCharStats().setDexterity(avgMob.baseCharStats().getDexterity()+mob2.baseCharStats().getDexterity());
		avgMob.baseCharStats().setConstitution(avgMob.baseCharStats().getConstitution()+mob2.baseCharStats().getConstitution());
		avgMob.baseCharStats().setCharisma(avgMob.baseCharStats().getCharisma()+mob2.baseCharStats().getCharisma());
		avgMob.baseEnvStats().setArmor(avgMob.baseEnvStats().armor()+mob2.baseEnvStats().armor());
		avgMob.baseState().setHitPoints(avgMob.baseState().getHitPoints()+mob2.baseState().getHitPoints());
		avgMob.baseState().setMovement(avgMob.baseState().getMovement()+mob2.baseState().getMovement());
		avgMob.baseState().setMana(avgMob.baseState().getMana()+mob2.baseState().getMana());
		avgMob.recoverCharStats();
		avgMob.recoverEnvStats();
		avgMob.recoverMaxState();
		avgMob.resetToMaxState();
	}

	public MOB AverageClassMOB(MOB mob, int level, CharClass C, int numTries)
	{
		MOB avgMob=(MOB)levelMOBup(level,C);
		int tries=0;
		for(;tries<numTries;tries++)
		{
			if((tries % 20)==0)
				mob.session().print(".");
			MOB mob2=(MOB)levelMOBup(level,C);
			addHimIn(avgMob,mob2);
		}
		averageout(avgMob,tries);
		return avgMob;
	}

	public MOB AverageAllClassMOB(MOB mob, int level, int numTriesClass, int numTriesMOB)
	{
		MOB avgMob=null;
		int tries=0;
		int numClasses=0;
		for(;tries<numTriesClass;tries++)
		{
			for(int c=0;c<CMClass.charClasses.size();c++)
			{
				CharClass C=(CharClass)CMClass.charClasses.elementAt(c);
				if(C.playerSelectable())
				{
					numClasses++;
					MOB mob2=AverageClassMOB(mob,level,C,numTriesMOB);
					if(avgMob==null)
					{
						avgMob=mob2;
						numClasses--;
					}
					else
						addHimIn(avgMob,mob2);
				}
			}
		}
		averageout(avgMob,numClasses);
		return avgMob;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental target, boolean auto)
	{
		if(mob.isMonster())
			return false;
		try
		{
			CharClass C=null;
			int level=-1;
			String ClassName="";
			if(commands.size()>0)
			{
				ClassName=(String)commands.elementAt(0);
				C=CMClass.getCharClass(ClassName);
				level=Util.s_int(Util.combine(commands,1));
			}

			if((C==null)&&(ClassName.toUpperCase().indexOf("ALL")<0))
			{
				mob.tell("Enter 'ALL' for all classes.");
				ClassName=mob.session().prompt("Enter a class name: ");

				C=CMClass.getCharClass(ClassName);
				if((C==null)&&(ClassName.toUpperCase().indexOf("ALL")<0))
					return false;
			}

			if(level<=0)
			{
				level=Util.s_int(mob.session().prompt("Enter a level (1-25): "));
				if(level<=0)
					return false;
			}

			if(C!=null)
				mob.session().print("\n\rAverage "+C.name()+"...");
			else
				mob.session().print("\n\rAverage MOB stats, across all classes...");

			MOB avgMob=null;
			if(C!=null)
				avgMob=AverageClassMOB(mob, level,C, 300);
			else
				avgMob=AverageAllClassMOB(mob,level, 20, 50);

			mob.session().println("\n\r");

			if(avgMob!=null)
			{
				avgMob.setSession(mob.session());
				ExternalPlay.score(avgMob);
				avgMob.setSession(null);
			}
			return true;
		}
		catch(IOException e)
		{
			Log.errOut("CHARGEN",e);
			return false;
		}
	}
}
