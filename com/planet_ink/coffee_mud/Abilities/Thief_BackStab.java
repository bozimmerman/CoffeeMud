package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Thief_BackStab extends ThiefSkill
{

	public Thief_BackStab()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Back Stab";
		displayText="(in a dark realm of thievery)";
		miscText="";

		triggerStrings.addElement("BACKSTAB");

		canBeUninvoked=true;
		isAutoinvoked=false;

		malicious=true;

		baseEnvStats().setLevel(7);

		addQualifyingClass(new Thief().ID(),7);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Thief_BackStab();
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		if(commands.size()<1)
		{
			mob.tell("Backstab whom?");
			return false;
		}
		MOB target=this.getTarget(mob,commands);
		if(target==null) return false;

		if(Sense.canBeSeenBy(mob,target))
		{
			mob.tell(target.name()+" is watching you too closely to do that.");
			return false;
		}

		if(!super.invoke(mob,commands))
			return false;

		Item weapon=mob.fetchWieldedItem();
		boolean success=profficiencyCheck(0);

		int factor=(int)Math.round(Util.div(mob.envStats().level(),5.0))+1;
		FullMsg msg=new FullMsg(mob,target,null,Affect.HANDS_DELICATE,Affect.STRIKE_JUSTICE,Affect.VISUAL_WNOISE,"<S-NAME> attempt(s) to stab <T-NAME> in the back!");
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			if(!success)
				mob.location().show(mob,target,Affect.VISUAL_WNOISE,"<T-NAME> spot(s) <S-NAME>!");
			else
				mob.envStats().setDamage(mob.envStats().damage()*factor);
			TheFight.doAttack(mob,target,weapon);
			mob.recoverEnvStats();
		}
		else
			success=false;
		return success;
	}

}
