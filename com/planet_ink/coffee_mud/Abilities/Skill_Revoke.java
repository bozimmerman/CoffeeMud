package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;


public class Skill_Revoke extends StdAbility
{
	public Skill_Revoke()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Revoke";
		displayText="(in a the fantastic world of magic)";
		miscText="";

		triggerStrings.addElement("REVOKE");

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(1);

		addQualifyingClass(new Ranger().ID(),5);
		addQualifyingClass(new Paladin().ID(),5);
		addQualifyingClass(new Cleric().ID(),1);
		addQualifyingClass(new Mage().ID(),1);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Skill_Revoke();
	}

	public boolean invoke(MOB mob, Vector commands)
	{

		String whatToRevoke=CommandProcessor.combine(commands,0);

		Environmental target=mob.location().fetchFromRoom(null,whatToRevoke);

		if((whatToRevoke.length()==0)
		&&(mob.location().numAffects()>0))
			target=mob.location();


		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("Revoke from what?  You don't see '"+whatToRevoke+"' here.");
			return false;
		}

		if(!super.invoke(mob,commands))
			return false;

		Ability revokeThis=null;
		for(int a=0;a<target.numAffects();a++)
		{
			Ability A=(Ability)target.fetchAffect(a);
			if((A.invoker()==mob)&&(A.canBeUninvoked()))
				revokeThis=A;
		}

		if(revokeThis==null)
		{
			mob.tell(mob,target,"<T-NAME> does not appear to be affected by anything you can revoke.");
			return false;
		}

		boolean success=profficiencyCheck(0);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.VISUAL_WNOISE,Affect.VISUAL_WNOISE,Affect.VISUAL_WNOISE,"<S-NAME> revoke(s) "+revokeThis.name()+" from <T-NAME>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				revokeThis.unInvoke();
			}
		}
		else
			beneficialFizzle(mob,target,"<S-NAME> attempt(s) to revoke "+revokeThis.name()+" from <T-NAME>, but flub(s) it.");
		return success;
	}

}
