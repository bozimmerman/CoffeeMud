package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Archon extends StdCharClass
{
	public String ID(){return "Archon";}
	public String name(){return "Archon";}
	public String baseClass(){return ID();}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	
	public Archon()
	{
		super();
		for(int i=0;i<=5;i++)
			maxStat[i]=25;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Resistance",true);
		}
	}

	public boolean playerSelectable()
	{
		return false;
	}

	public String statQualifications(){return "Must be granted by another Archon.";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(!quiet)
			mob.tell("This class cannot be learned.");
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
		if(verifyOnly)
			grantAbilities(mob,true);
	}
	
	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		for(int a=0;a<CMClass.abilities.size();a++)
		{
			Ability A=(Ability)CMClass.abilities.elementAt(a);
			if((CMAble.getQualifyingLevel(ID(),A.ID())>0)
			&&(CMAble.getQualifyingLevel(ID(),A.ID())<=mob.charStats().getClassLevel(this)))
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
}
