package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_Tumble extends StdAbility
{
	public int hits=0;
	public String ID() { return "Fighter_Tumble"; }
	public String name(){ return "Tumble";}
	public String displayText(){ return "(Tumbling)";}
	private static final String[] triggerStrings = {"TUMBLE"};
	public int quality(){return Ability.BENEFICIAL_SELF;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Fighter_Tumble();}
	public int classificationCode(){ return Ability.SKILL; }

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((invoker==null)&&(affected instanceof MOB))
		   invoker=(MOB)affected;
		if(invoker!=null)
		{
			affectableStats.setDamage(affectableStats.damage()-(int)Math.round(Util.div(affectableStats.damage(),2.0)));
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-(int)Math.round(Util.div(affectableStats.attackAdjustment(),2.0)));
		}
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!super.okAffect(myHost,affect))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((affect.amITarget(mob))
		   &&(Util.bset(affect.targetCode(),Affect.MASK_HURT))
		   &&((affect.targetCode()-Affect.MASK_HURT)>0))
		{
			if((affect.tool()!=null)
			&&(!mob.amDead())
			&&(affect.tool() instanceof Weapon))
			{
				affect.modify(affect.source(),affect.target(),affect.tool(),Affect.NO_EFFECT,null,Affect.NO_EFFECT,null,Affect.NO_EFFECT,null);
				affect.addTrailerMsg(new FullMsg((MOB)affect.target(),affect.source(),Affect.MSG_OK_VISUAL,"<S-NAME> tumble(s) around the attack from <T-NAME>."));
				if((++hits)>=2)
					unInvoke();
			}
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You are already tumbling.");
			return false;
		}

		if((!auto)&&(!mob.isInCombat()))
		{
			mob.tell("You aren't in combat!");
			return false;
		}

		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_QUIETMOVEMENT,auto?"<T-NAME> begin(s) tumbling around!":"<S-NAME> tumble(s) around!");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				hits=0;
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to tumble, but goof(s) it up.");
		return success;
	}
}