package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Undead_LifeDrain extends StdAbility
{
	public Undead_LifeDrain()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Drain Life";
		displayText="(in the unholy dominion of the undead)";
		miscText="";
		triggerStrings.addElement("DRAIN");

		quality=Ability.MALICIOUS;

		canBeUninvoked=true;
		isAutoinvoked=false;
	}

	public int classificationCode()
	{
		return Ability.SKILL;
	}

	public Environmental newInstance()
	{
		return new Undead_LifeDrain();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);

		if(target==null) return false;
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);


		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			int much=mob.envStats().level();
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_UNDEAD|(auto?Affect.ACT_GENERAL:0),auto?"":"<S-NAME> clutch(es) <T-NAMESELF>, and drain(s) <T-HIS-HER> life!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.curState().adjMana(-much,mob.maxState());
				if(msg.wasModified())
					much = (int)Math.round(Util.div(much,2.0));
				mob.location().show(target,null,Affect.MSG_OK_VISUAL,"The drain "+ExternalPlay.hitWord(Weapon.TYPE_BURNING,much)+" <S-NAME>!");
				mob.curState().adjHitPoints(much,mob.maxState());
				ExternalPlay.postDamage(mob,target,this,much);
				mob.tell("You feel a little better!");
			}
		}
		else
			maliciousFizzle(mob,target,"<S-NAME> attempt(s) to drain life from <T-NAMESELF>, but fail(s).");

		// return whether it worked
		return success;
	}
}