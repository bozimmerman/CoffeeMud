package com.planet_ink.coffee_mud.Abilities.Misc;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import java.util.*;


public class Inebriation extends StdAbility
{

	public Inebriation()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Inebriation";
		displayText="(Inebriated)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		quality=Ability.MALICIOUS;

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Inebriation();
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected instanceof MOB)
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-(int)Math.round(((MOB)affected).envStats().level()));
	}


	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.DEXTERITY,(int)Math.round(affectableStats.getStat(CharStats.DEXTERITY)-3));
	}

	public boolean tick(int tickID)
	{
		if(!super.tick(tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null) return true;

		if((Dice.rollPercentage()<25)&&(Sense.aliveAwakeMobile(mob,true)))
		{
			if(mob.getAlignment()<350)
				mob.location().show(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> stagger(s) around making ugly faces.");
			else
			if(mob.getAlignment()<650)
				mob.location().show(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> stagger(s) around aimlessly.");
			else
				mob.location().show(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> stagger(s) around trying to hug everyone.");

		}
		return true;
	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;

		if(affect.source()!=affected)
			return true;
		if(affect.source().location()==null)
			return true;
		if((!Util.bset(affect.targetMajor(),Affect.ACT_GENERAL))
		&&(affect.targetMajor()>0))
		{
			if((affect.target() !=null)
				&&(affect.target() instanceof MOB))
					affect.modify(affect.source(),affect.source().location().fetchInhabitant(Dice.roll(1,affect.source().location().numInhabitants(),0)-1),affect.tool(),affect.sourceCode(),affect.sourceMessage(),affect.targetCode(),affect.targetMessage(),affect.othersCode(),affect.othersMessage());
		}
		return true;
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		mob.tell(mob,null,"You feel sober now.");
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			String str=auto?"":"<S-NAME> attempt(s) to inebriate <T-NAMESELF>!";
			FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_NOISYMOVEMENT,str);
			if(target.location().okAffect(msg))
			{
			    target.location().send(target,msg);
				target.location().show(target,null,Affect.MSG_NOISE,"<S-NAME> burp(s)!");
				success=maliciousAffect(mob,target,0,-1);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to inebriate <T-NAMESELF>, but fail(s).");

        return success;

	}
}