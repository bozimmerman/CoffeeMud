package com.planet_ink.coffee_mud.Abilities.Languages;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Common extends Language
{
	public String ID() { return "Common"; }
	public String name(){ return "Common";}
	public boolean isAutoInvoked(){return false;}
	public boolean canBeUninvoked(){return canBeUninvoked;}
	private static boolean mapped=false;
	public Common()
	{
		super();
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",1,ID(),100,true);}
		profficiency=100;
	}
	public Environmental newInstance(){	return new Common();}
	public int profficiency(){return 100;}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		for(int a=0;a<mob.numAffects();a++)
		{
			Ability A=mob.fetchAffect(a);
			if((A!=null)&&(A instanceof Language))
				((Language)A).setBeingSpoken(false);
				
		}
		isAnAutoEffect=false;
		if(!auto)
			mob.tell("You are now speaking "+displayName()+".");
		return true;
	}
}
