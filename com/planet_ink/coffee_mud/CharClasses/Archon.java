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
			
			CMAble.addCharAbilityMapping(ID(),1,"Chant_AlterTime",true);
			CMAble.addCharAbilityMapping(ID(),1,"Chant_MoveSky",true);
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
		// the most efficient way of doing this -- just hash em!
		Hashtable alreadyAble=new Hashtable();
		Hashtable alreadyAff=new Hashtable();
		for(int a=0;a<mob.numAffects();a++)
		{
			Ability A=mob.fetchAffect(a);
			if(A!=null) alreadyAff.put(A.ID(),A);
		}
		for(int a=0;a<mob.numAbilities();a++)
		{
			Ability A=mob.fetchAbility(a);
			if(A!=null)
			{
				A.setProfficiency(100);
				A.setBorrowed(mob,true);
				Ability A2=(Ability)alreadyAff.get(A.ID());
				if(A2!=null)
					A2.setProfficiency(100);
				else
					A.autoInvocation(mob);
				alreadyAble.put(A.ID(),A);
			}
		}
		int classLevel=mob.charStats().getClassLevel(this);
		for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
		{
			Ability A=(Ability)a.nextElement();
			int lvl=CMAble.getQualifyingLevel(ID(),A.ID());
			if((lvl>0)
			&&(lvl<=classLevel)
			&&(!alreadyAble.containsKey(A.ID())))
				giveMobAbility(mob,A,100,"",true,false);
		}
		alreadyAble.clear();
		alreadyAff.clear();
	}
}
