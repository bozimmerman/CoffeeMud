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
	public int usageType(){return USAGE_MOVEMENT;}

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

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((msg.amITarget(mob))
		   &&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		   &&((msg.value())>0))
		{
			if((msg.tool()!=null)
			&&(!mob.amDead())
			&&(msg.tool() instanceof Weapon))
			{
				msg.modify(msg.source(),msg.target(),msg.tool(),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
				msg.addTrailerMsg(new FullMsg((MOB)msg.target(),msg.source(),CMMsg.MSG_OK_VISUAL,"<S-NAME> tumble(s) around the attack from <T-NAME>."));
				if((++hits)>=2)
					unInvoke();
			}
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.fetchEffect(this.ID())!=null)
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
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_QUIETMOVEMENT,auto?"<T-NAME> begin(s) tumbling around!":"<S-NAME> tumble(s) around!");
			if(mob.location().okMessage(mob,msg))
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