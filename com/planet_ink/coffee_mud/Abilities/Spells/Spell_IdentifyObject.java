package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.Spells.interfaces.*;
import java.util.*;

public class Spell_IdentifyObject extends Spell
	implements DivinationDevotion
{
	public Spell_IdentifyObject()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Identify Object";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(8);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_IdentifyObject();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"<S-NAME> study(s) <T-NAMESELF> very closely.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				String identity=((Item)target).secretIdentity();
				mob.tell(identity);
			}

		}
		else
			beneficialVisualFizzle(mob,target,"<S-NAME> study(s) <T-NAMESELF>, looking more frustrated every second.");


		// return whether it worked
		return success;
	}
}
