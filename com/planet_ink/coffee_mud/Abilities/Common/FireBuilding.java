package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class FireBuilding extends CommonSkill
{
	public Item lighting=null;
	
	public FireBuilding()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Fire Building";

		displayText="You building a fire";
		verb="building a fire";
		miscText="";
		triggerStrings.addElement("LIGHT");
		quality=Ability.INDIFFERENT;

		recoverEnvStats();
		CMAble.addCharAbilityMapping("All",1,ID(),false);
	}
	
	public Environmental newInstance()
	{
		return new FireBuilding();
	}
	
	public void unInvoke()
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if(aborted)
				mob.tell("You stop "+verb);
			else
				mob.tell("You are done "+verb);
			
		}
		super.unInvoke();
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()==0)
		{
			mob.tell("Light what?  Try light fire, or light torch...");
			return false;
		}
		String name=Util.combine(commands,0);
		if(name.equalsIgnoreCase("fire"))
		{
		}
		else
		{
			lighting=getTarget(mob,mob.location(),givenTarget,commands);
			if(lighting==null) return;
			
		}
		verb="building a fire";
		found=null;
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		int duration=40-mob.envStats().level();
		if(duration<15) duration=15;
		beneficialAffect(mob,mob,duration);
		return true;
	}

}