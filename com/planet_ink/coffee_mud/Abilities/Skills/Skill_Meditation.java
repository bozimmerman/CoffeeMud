package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.system.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Meditation extends StdAbility
{
	public Skill_Meditation()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Meditation";
		displayText="";
		miscText="";

		triggerStrings.addElement("MEDITATE");
		canBeUninvoked=true;
		isAutoinvoked=false;
		quality=Ability.INDIFFERENT;

		canTargetCode=0;
		canAffectCode=Ability.CAN_MOBS;
		
		baseEnvStats().setLevel(19);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Skill_Meditation();
	}


	public int classificationCode()
	{
		return Ability.SKILL;
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();
		if(canBeUninvoked)
		{
			if(!mob.amDead())
			{
				if(mob.location()!=null)
					mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> end(s) <S-HIS-HER> meditation.");
				else
					mob.tell("Your meditation ends.");
			}
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
		if(Util.bset(affect.othersCode(),Affect.ACT_SOUND)
		   &&(Sense.canBeHeardBy(affect.source(),mob)))
		{
			if(!affect.amISource(mob))
				affect.addTrailerMsg(new FullMsg(mob,null,null,Affect.TYP_GENERAL|Affect.ACT_HANDS,Affect.NO_EFFECT,Affect.NO_EFFECT,null));
			else
				affect.addTrailerMsg(new FullMsg(mob,null,null,Affect.TYP_GENERAL|Affect.ACT_HANDS,Affect.NO_EFFECT,Affect.NO_EFFECT,"Your meditation is interrupted by the noise."));
		}
		return;
	}
	
	public boolean tick(int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(tickID);

		MOB mob=(MOB)affected;

		if(tickID!=Host.MOB_TICK) return true;
		if(!profficiencyCheck(0,false)) return true;
		
		if((!mob.isInCombat())
		&&(mob.curState().getHunger()>0)
		&&(mob.curState().getThirst()>0)
		&&(Sense.isSitting(mob)))
		{
			double man=new Integer((mob.charStats().getStat(CharStats.INTELLIGENCE)+mob.charStats().getStat(CharStats.WISDOM))).doubleValue();
			mob.curState().adjMana((int)Math.round((man*.1)+mob.envStats().level()),mob.maxState());
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
		MOB target=mob;
		if(mob.isInCombat())
		{
			mob.tell("You can't meditate while in combat!");
			return false;
		}
		if(!Sense.isSitting(mob))
		{
			mob.tell("You must be in a sitting, restful position to meditate.");
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
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_OK_VISUAL|(auto?Affect.ACT_GENERAL:0),auto?"":"<S-NAME> begin(s) to meditate...");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,Integer.MAX_VALUE-1000);
				helpProfficiency(mob);
			}
		}
		else
			return beneficialVisualFizzle(mob,target,"<S-NAME> attempt(s) to meditate, but loses concentration.");

		// return whether it worked
		return success;
	}
}