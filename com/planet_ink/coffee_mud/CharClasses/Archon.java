package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Archon extends StdCharClass
{
	public String ID(){return "Archon";}
	public String name(){return "Archon";}
	private static boolean abilitiesLoaded=false;
	
	public Archon()
	{
		super();
		for(int i=0;i<=5;i++)
			maxStat[i]=25;
		if(!abilitiesLoaded)
		{
			abilitiesLoaded=true;
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Resistance",true);
		}
	}

	public boolean playerSelectable()
	{
		return false;
	}

	public String statQualifications(){return "Must be granted by another Archon.";}
	public boolean qualifiesForThisClass(MOB mob)
	{
		return false;
	}
	public void outfit(MOB mob)
	{
		Weapon w=(Weapon)CMClass.getWeapon("ArchonStaff");
		if(mob.fetchInventory(w.ID())==null)
		{
			mob.addInventory(w);
			if(!mob.amWearingSomethingHere(Item.WIELD))
				w.wearAt(Item.WIELD);
		}
	}
	public void startCharacter(MOB mob, boolean isBorrowedClass, boolean verifyOnly)
	{
		// archons ALWAYS use borrowed abilities
		super.startCharacter(mob, true, verifyOnly);
		for(int a=0;a<CMClass.abilities.size();a++)
		{
			Ability A=(Ability)CMClass.abilities.elementAt(a);
			if(A.qualifyingLevel(mob)>0)
			{
				Ability mine=mob.fetchAbility(A.ID());
				if(mine!=null)
				{
					mine.setProfficiency(100);
					mine.setBorrowed(mob,true);
					if(mob.fetchAffect(A.ID())!=null)
						mob.fetchAffect(A.ID()).setProfficiency(100);
					else
						mine.autoInvocation(mob);
				}
				else
					giveMobAbility(mob,A,100,"",true,false);
			}
		}
	}

	public void level(MOB mob)
	{
		mob.tell("^HYou leveled... not that it matters.^N");
		levelAdjuster(mob,1);

		int practiceGain=(int)Math.floor(Util.div(mob.charStats().getStat(CharStats.WISDOM),4.0))+getBonusPracLevel();
		if(practiceGain<=0)practiceGain=1;
		mob.setPractices(mob.getPractices()+practiceGain);

		int trainGain=1;
		if(trainGain<=0)trainGain=1;
		mob.setTrains(mob.getTrains()+trainGain);
		// wrap it all up
		mob.recoverEnvStats();
		mob.recoverCharStats();
		mob.recoverMaxState();
	}
}
