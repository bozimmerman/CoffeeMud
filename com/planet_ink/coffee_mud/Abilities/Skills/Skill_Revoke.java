package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
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

		addQualifyingClass("Ranger",5);
		addQualifyingClass("Paladin",5);
		addQualifyingClass("Cleric",1);
		addQualifyingClass("Mage",1);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Skill_Revoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		String whatToRevoke=Util.combine(commands,0);

		Environmental target=mob.location().fetchFromRoomFavorMOBs(null,whatToRevoke);

		if((whatToRevoke.length()==0)
		&&(mob.location().numAffects()>0))
			target=mob.location();


		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("Revoke from what?  You don't see '"+whatToRevoke+"' here.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
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
			if(target instanceof Room)
				mob.tell(mob,null,"Revoke a spell from what?");
			else
				mob.tell(mob,target,"<T-NAME> does not appear to be affected by anything you can revoke.");
			return false;
		}

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_NOISYMOVEMENT,"<S-NAME> revoke(s) "+revokeThis.name()+" from <T-NAMESELF>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				revokeThis.unInvoke();
			}
		}
		else
			beneficialVisualFizzle(mob,target,"<S-NAME> attempt(s) to revoke "+revokeThis.name()+" from <T-NAMESELF>, but flub(s) it.");
		return success;
	}

}
