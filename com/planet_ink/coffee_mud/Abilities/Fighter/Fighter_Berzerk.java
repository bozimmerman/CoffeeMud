package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_Berzerk extends StdAbility
{
	public int hpAdjustment=0;

	public Fighter_Berzerk()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Berzerk";
		displayText="(Berzerk)";
		miscText="";

		triggerStrings.addElement("BERZERK");

		canAffectCode=Ability.CAN_MOBS;
		canTargetCode=0;
		
		canBeUninvoked=true;
		isAutoinvoked=false;
		quality=Ability.BENEFICIAL_SELF;

		baseEnvStats().setLevel(20);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Fighter_Berzerk();
	}

	public int classificationCode()
	{
		return Ability.SKILL;
	}

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

			if(canBeUninvoked)
			{
				if(mob.curState().getHitPoints()<=hpAdjustment)
					mob.curState().setHitPoints(1);
				else
					mob.curState().adjHitPoints(-hpAdjustment,mob.maxState());
				mob.tell(mob,null,"You fell calmer.");
				mob.recoverMaxState();
			}
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You are already berzerk.");
			return false;
		}

		if(!mob.isInCombat())
		{
			mob.tell("You aren't in combat!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> get(s) a wild look in <S-HIS-HER> eyes!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				hpAdjustment=(int)Math.round(Util.div(mob.maxState().getHitPoints(),5.0));
				beneficialAffect(mob,mob,0);
				mob.curState().setHitPoints(mob.curState().getHitPoints()+hpAdjustment);
				mob.recoverMaxState();
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> huff(s) and grunt(s), but can't get angry.");
		return success;
	}
}
