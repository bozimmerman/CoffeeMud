package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Qualifier extends StdCharClass
{
	public String ID(){return "Qualifier";}
	public String name(){return "Qualifier";}
	public String baseClass(){return ID();}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	
	public Qualifier()
	{
		super();
		for(int i=0;i<CharStats.NUM_BASE_STATS;i++)
			maxStatAdj[i]=7;
	}

	public boolean playerSelectable()
	{
		return false;
	}

	public String statQualifications(){return "Must be granted by an Archon.";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(!quiet)
			mob.tell("This class cannot be learned.");
		return false;
	}
	
	public void startCharacter(MOB mob, boolean isBorrowedClass, boolean verifyOnly)
	{
		if((verifyOnly)&&(!loaded()))
		{
			setLoaded(true);
			for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
			{
				Ability A=(Ability)a.nextElement();
				int lvl=CMAble.lowestQualifyingLevel(A.ID());
				if(lvl>0)
					CMAble.addCharAbilityMapping(ID(),lvl,A.ID(),false);
			}
		}
		super.startCharacter(mob, false, verifyOnly);
	}
	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		super.grantAbilities(mob,isBorrowedClass);
		if(mob.isMonster())
		{
			Vector V=CMAble.getUpToLevelListings(ID(),
												mob.charStats().getClassLevel(ID()),
												false,
												false);
			for(Enumeration a=V.elements();a.hasMoreElements();)
			{
				Ability A=CMClass.getAbility((String)a.nextElement());
				if((A!=null)
				&&((A.classificationCode()&Ability.ALL_CODES)!=Ability.COMMON_SKILL)
				&&(!CMAble.getDefaultGain(ID(),true,A.ID())))
					giveMobAbility(mob,A,CMAble.getDefaultProfficiency(ID(),true,A.ID()),CMAble.getDefaultParm(ID(),true,A.ID()),isBorrowedClass);
			}
		}
	}

}
