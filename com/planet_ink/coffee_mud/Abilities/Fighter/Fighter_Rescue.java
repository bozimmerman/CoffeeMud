package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_Rescue extends StdAbility
{
	public Fighter_Rescue()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Rescue";
		displayText="(Rescued)";
		miscText="";

		triggerStrings.addElement("RESCUE");
		triggerStrings.addElement("RES");

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(5);

		addQualifyingClass("Fighter",5);
		addQualifyingClass("Paladin",2);
		addQualifyingClass("Ranger",2);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Fighter_Rescue();
	}

	public int classificationCode()
	{
		return Ability.SKILL;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if((target.amDead())||(!target.isInCombat()))
		{
			mob.tell(target.charStats().HeShe()+" isn't fighting anyone!");
			return false;
		}

		MOB monster=target.getVictim();

		if(monster.getVictim()==mob)
		{
			mob.tell("You are already taking the blows from "+mob.getVictim().name()+".");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		String str=null;
		if(success)
		{
			str="<S-NAME> rescue(s) <T-NAMESELF>!";
			FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_NOISYMOVEMENT,str);
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				monster.setVictim(mob);
			}
		}
		else
		{
			str="<S-NAME> attempt(s) to rescue <T-NAMESELF>, but fail(s).";
			FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_NOISYMOVEMENT,str);
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}

		return success;
	}

}
