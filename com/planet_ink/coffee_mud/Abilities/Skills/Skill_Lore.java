package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Lore extends StdAbility
{
	public Skill_Lore()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Lore";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="";

		quality=Ability.MALICIOUS;

		baseEnvStats().setLevel(1);

		canBeUninvoked=true;
		isAutoinvoked=false;

		triggerStrings.addElement("LORE");

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Skill_Lore();
	}

	public int classificationCode()
	{
		return Ability.SKILL;
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
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_EXAMINESOMETHING,auto?"":"<S-NAME> study(s) <T-NAMESELF> and consider(s) for a moment.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				String identity=((Item)target).secretIdentity();
				mob.tell(identity);
				
			}

		}
		else
			beneficialVisualFizzle(mob,target,"<S-NAME> study(s) <T-NAMESELF>, but can't remember a thing.");
		return success;
	}
}