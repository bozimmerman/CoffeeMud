package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_KiStrike extends StdAbility
{
	public String ID() { return "Fighter_KiStrike"; }
	public String name(){ return "Ki Strike";}
	public String displayText(){return "(Ki Strike)";}
	private static final String[] triggerStrings = {"KISTRIKE","KI"};
	public int quality(){return Ability.OK_SELF;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Fighter_KiStrike();}
	public int classificationCode(){return Ability.SKILL;}
	boolean done=false;

	public boolean okAffect(Environmental myHost, Affect msg)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected))
		&&(!done)
		&&(Util.bset(msg.targetCode(),Affect.MASK_HURT)))
		{
			done=true;
			MOB mob=(MOB)affected;
			if((Sense.aliveAwakeMobile(mob,true))
			&&(mob.location()!=null))
			{
				mob.location().show(mob,null,Affect.MSG_SPEAK,"<S-NAME> yell(s) KIA!");
				unInvoke();
			}

		}
		return super.okAffect(myHost,msg);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(!done)
			affectableStats.setDamage(affectableStats.damage()+(affected.envStats().level()));
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		
		if(!Sense.canSpeak(mob))
		{
			mob.tell("You can't speak!");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_QUIETMOVEMENT,"<S-NAME> concentrate(s) <S-HIS-HER> strength.");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				done=false;
				beneficialAffect(mob,target,2);
			}
		}
		else
			return beneficialVisualFizzle(mob,null,"<S-NAME> lose(s) concentration.");

		// return whether it worked
		return success;
	}
}