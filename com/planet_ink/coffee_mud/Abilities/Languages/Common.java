package com.planet_ink.coffee_mud.Abilities.Languages;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Common extends Language
{
	public Common()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Common";
		CMAble.addCharAbilityMapping("All",1,ID(),100,true);
	}
	public Environmental newInstance()
	{
		return new Common();
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		for(int a=0;a<mob.numAffects();a++)
		{
			Ability A=mob.fetchAffect(a);
			if((A!=null)&&(A instanceof Language))
				((Language)A).setBeingSpoken(false);
		}
		isAnAutoEffect=false;
		mob.tell("You are now speaking "+name()+".");
		return true;
	}
}
