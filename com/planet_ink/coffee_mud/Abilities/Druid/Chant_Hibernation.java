package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.system.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_Hibernation extends Chant
{
	private CharState oldState=null;
	private int roundsHibernating=0;
	public Chant_Hibernation()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Hibernation";
		displayText="(Hibernating)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;
		quality=Ability.INDIFFERENT;

		baseEnvStats().setLevel(19);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Chant_Hibernation();
	}


	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();
		if(!mob.amDead())
		{
			if(mob.location()!=null)
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> end(s) <S-HIS-HER> hibernation.");
			else
				mob.tell("Your hibernation ends.");
		}
	}

	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public void affect(Affect affect)
	{
		super.affect(affect);
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		if((affect.amISource(mob))&&((Util.bset(affect.sourceCode(),Affect.ACT_MOVE))||(Util.bset(affect.sourceCode(),Affect.ACT_HANDS))||(Util.bset(affect.sourceCode(),Affect.ACT_MOUTH))))
			unInvoke();
		return;
	}
	
	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okAffect(affect);
		MOB mob=(MOB)affected;

		if((affect.amISource(mob)
		&&(!Util.bset(affect.sourceMajor(),Affect.ACT_GENERAL))
		&&(affect.sourceMajor()>0)))
		{
			if(roundsHibernating<10)
			{
				mob.tell("You can't withdraw from hibernation just yet.");
				return false;
			}
			else
				unInvoke();
		}
		return super.okAffect(affect);
	}
	public boolean tick(int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(tickID);

		MOB mob=(MOB)affected;

		if(tickID!=Host.MOB_TICK) return true;
		if(!profficiencyCheck(0,false)) return true;
		
		if((!mob.isInCombat())
		&&(Sense.isSleeping(mob)))
		{
			roundsHibernating++;
			double man=new Integer((mob.charStats().getStat(CharStats.INTELLIGENCE)+mob.charStats().getStat(CharStats.WISDOM))).doubleValue();
			mob.curState().adjMana((int)Math.round((man*.1)+mob.envStats().level()),mob.maxState());
			mob.curState().setHunger(oldState.getHunger());
			mob.curState().setThirst(oldState.getThirst());
			double hp=new Integer(mob.charStats().getStat(CharStats.CONSTITUTION)).doubleValue();
			mob.curState().adjHitPoints((int)Math.round((hp*.1)+mob.envStats().level()),mob.maxState());
		}
		else
		{
			unInvoke();
			return false;
		}
		return super.tick(tickID);
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.isInCombat())
		{
			mob.tell("You can't hibernate while in combat!");
			return false;
		}
		if(!Sense.isSitting(mob))
		{
			mob.tell("You must be in a sitting, restful position to hibernate.");
			return false;
		}
		// now see if it worked
		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,null,this,Affect.MSG_SLEEP|Affect.MASK_MAGIC,"<S-NAME> begin(s) to hibernate...");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				oldState=mob.curState();
				beneficialAffect(mob,mob,Integer.MAX_VALUE-1000);
				helpProfficiency(mob);
			}
		}
		else
			return beneficialVisualFizzle(mob,null,"<S-NAME> chant(s) to hibernate, but loses concentration.");

		// return whether it worked
		return success;
	}
}