package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_Berzerk extends StdAbility
{
	public String ID() { return "Fighter_Berzerk"; }
	public String name(){ return "Berzerk";}
	public String displayText(){ return "(Berzerk)";}
	private static final String[] triggerStrings = {"BERZERK"};
	public int quality(){return Ability.BENEFICIAL_SELF;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Fighter_Berzerk();}
	public int classificationCode(){ return Ability.SKILL;}

	public int hpAdjustment=0;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((invoker==null)&&(affected instanceof MOB))
		   invoker=(MOB)affected;
		if(invoker!=null)
		{
			affectableStats.setDamage(affectableStats.damage()+(int)Math.round(Util.div(affectableStats.damage(),4.0)));
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(int)Math.round(Util.div(affectableStats.attackAdjustment(),4.0)));
			affectableStats.setArmor(affectableStats.armor()+20);
		}
	}

	public void affectCharState(MOB affectedMOB, CharState affectedMaxState)
	{
		super.affectCharState(affectedMOB,affectedMaxState);
		if(affectedMOB!=null)
			affectedMaxState.setHitPoints(affectedMaxState.getHitPoints()+hpAdjustment);
	}

	public void unInvoke()
	{
		if(affecting() instanceof MOB)
		{
			MOB mob=(MOB)affecting();

			super.unInvoke();

			if(canBeUninvoked())
			{
				if(mob.curState().getHitPoints()<=hpAdjustment)
					mob.curState().setHitPoints(1);
				else
					mob.curState().adjHitPoints(-hpAdjustment,mob.maxState());
				mob.tell("You fell calmer.");
				mob.recoverMaxState();
			}
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(this.ID())!=null)
		{
			target.tell("You are already berzerk.");
			return false;
		}

		if((!auto)&&(!mob.isInCombat()))
		{
			mob.tell("You aren't in combat!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> get(s) a wild look in <S-HIS-HER> eyes!");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				hpAdjustment=(int)Math.round(Util.div(target.maxState().getHitPoints(),5.0));
				beneficialAffect(mob,target,0);
				target.curState().setHitPoints(target.curState().getHitPoints()+hpAdjustment);
				target.recoverMaxState();
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> huff(s) and grunt(s), but can't get angry.");
		return success;
	}
}
