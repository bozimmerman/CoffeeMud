package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_Tumble extends StdAbility
{
	public int hits=0;

	public Fighter_Tumble()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Tumble";
		displayText="(Tumbling)";
		miscText="";

		canAffectCode=Ability.CAN_MOBS;
		canTargetCode=0;
		
		triggerStrings.addElement("TUMBLE");

		canBeUninvoked=true;
		isAutoinvoked=false;
		quality=Ability.BENEFICIAL_SELF;

		baseEnvStats().setLevel(20);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Fighter_Tumble();
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
			affectableStats.setDamage(affectableStats.damage()-(int)Math.round(Util.div(affectableStats.damage(),2.0)));
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-(int)Math.round(Util.div(affectableStats.attackAdjustment(),2.0)));
		}
	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((affect.amITarget(mob))&&(Util.bset(affect.targetCode(),Affect.MASK_HURT)))
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
			FullMsg msg=new FullMsg(mob,null,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> tumble(s) around!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				hits=0;
				beneficialAffect(mob,mob,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to tumble, but goof(s) it up.");
		return success;
	}
}