package com.planet_ink.coffee_mud.Abilities;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Races.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.db.*;
import java.io.*;
import java.util.*;


public class Archon_CharGen extends ArchonSkill
{
	public Archon_CharGen()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="CharGen";

		triggerStrings.addElement("CHARGEN");

	}

	public Environmental newInstance()
	{
		return new Archon_CharGen();
	}
	private MOB levelMOBup(int level, CharClass C)
	{
		StdMOB mob=new StdMOB();
		mob.setAlignment(500);
		mob.setName("Average Joe");
		mob.baseCharStats().setMyRace(new Human());
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
		mob.maxState().setHitPoints(20);
		mob.maxState().setMovement(100);
		mob.maxState().setMana(100);
		mob.baseCharStats().getMyRace().newCharacter(mob);
		mob.recoverCharStats();
		mob.recoverEnvStats();
		mob.recoverMaxState();
		mob.baseCharStats().getMyClass().newCharacter(mob);
			
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
			int newAttack=mob.baseEnvStats().attackAdjustment()-oldattack;
			mob.baseEnvStats().setArmor(mob.baseEnvStats().armor()-newAttack);
			mob.recoverEnvStats();
			mob.recoverCharStats();
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
		avgMob.maxState().setHitPoints((int)Math.round(Util.div(avgMob.maxState().getHitPoints(),tries)));
		avgMob.maxState().setMovement((int)Math.round(Util.div(avgMob.maxState().getMovement(),tries)));
		avgMob.maxState().setMana((int)Math.round(Util.div(avgMob.maxState().getMana(),tries)));
		avgMob.recoverCharStats();
		avgMob.recoverEnvStats();
		avgMob.recoverMaxState();
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
		avgMob.maxState().setHitPoints(avgMob.maxState().getHitPoints()+mob2.maxState().getHitPoints());
		avgMob.maxState().setMovement(avgMob.maxState().getMovement()+mob2.maxState().getMovement());
		avgMob.maxState().setMana(avgMob.maxState().getMana()+mob2.maxState().getMana());
		avgMob.recoverCharStats();
		avgMob.recoverEnvStats();
		avgMob.recoverMaxState();
	}
	
	public StdMOB AverageClassMOB(MOB mob, int level, CharClass C, int numTries)
	{
		StdMOB avgMob=(StdMOB)levelMOBup(level,C);
		int tries=0;
		for(;tries<numTries;tries++)
		{
			if((tries % 20)==0)
				mob.session().print(".");
			StdMOB mob2=(StdMOB)levelMOBup(level,C);
			addHimIn(avgMob,mob2);
		}
		averageout(avgMob,tries);
		return avgMob;
	}
	
	public StdMOB AverageAllClassMOB(MOB mob, int level, int numTriesClass, int numTriesMOB)
	{
		StdMOB avgMob=null;
		int tries=0;
		int numClasses=0;
		for(;tries<numTriesClass;tries++)
		{
			for(int c=0;c<MUD.charClasses.size();c++)
			{
				CharClass C=(CharClass)MUD.charClasses.elementAt(c);
				if(C.playerSelectable())
				{
					numClasses++;
					StdMOB mob2=AverageClassMOB(mob,level,C,numTriesMOB);
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
	
	public boolean invoke(MOB mob, Vector commands)
	{
		if(mob.isMonster())
			return false;
		CharClass C=null;
		int level=-1;
		String ClassName="";
		if(commands.size()>0)
		{
			ClassName=(String)commands.elementAt(0);
			C=MUD.getCharClass(ClassName);
			level=Util.s_int(CommandProcessor.combine(commands,1));
		}
		
		if((C==null)&&(ClassName.toUpperCase().indexOf("ALL")<0))
		{
			mob.tell("Enter 'ALL' for all classes.");
			ClassName=mob.session().prompt("Enter a class name: ");
		
			C=MUD.getCharClass(ClassName);
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

		StdMOB avgMob=null;
		if(C!=null)
			avgMob=AverageClassMOB(mob, level,C, 300);
		else
			avgMob=AverageAllClassMOB(mob,level, 20, 50);
		
		mob.session().println("\n\r");
		
		if(avgMob!=null)
		{
			avgMob.setSession(mob.session());
			Scoring.score(avgMob);
			avgMob.setSession(null);
		}
		return true;
	}
}
