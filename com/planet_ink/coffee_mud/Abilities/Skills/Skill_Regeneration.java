package com.planet_ink.coffee_mud.Abilities.Skills;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import java.util.*;


public class Skill_Regeneration extends StdAbility
{
	public String ID() { return "Skill_Regeneration"; }
	public String name(){ return "Regeneration";}
	public String displayText(){ return "(Regeneration)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	public boolean putInCommandlist(){return false;}
	private static final String[] triggerStrings = {"REGENERATE"};
	public String[] triggerStrings(){return triggerStrings;}
	public boolean canBeUninvoked(){return false;}
	public int classificationCode(){return Ability.SKILL;}

	private static final int maxTickDown=3;
	private int regenDown=maxTickDown;
	public Environmental newInstance(){	return new Skill_Regeneration();}

	public boolean tick(int tickID)
	{
		if(!super.tick(tickID))
			return false;

		if((--regenDown)>0)
			return true;
		regenDown=maxTickDown;
		MOB mob=(MOB)affected;
		if(mob==null) return true;
		if(mob.location()==null) return true;
		if(mob.amDead()) return true;

		boolean doneAnything=false;
		doneAnything=doneAnything||mob.curState().adjHitPoints((int)Math.round(Util.div(mob.envStats().level(),2.0)),mob.maxState());
		doneAnything=doneAnything||mob.curState().adjMana(mob.envStats().level()*2,mob.maxState());
		doneAnything=doneAnything||mob.curState().adjMovement(mob.envStats().level()*3,mob.maxState());
		if(doneAnything)
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"<S-NAME> regenerate(s).");
		return true;
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
			mob.tell(mob,null,"You feel less regenerative.");
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
			FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_NOISYMOVEMENT,auto?"":"<S-NAME> lay(s) a regenerative property to <T-NAME>.");
			if(target.location().okAffect(msg))
			{
			    target.location().send(target,msg);
				success=beneficialAffect(mob,target,0);
			}
		}

        return success;

	}
}