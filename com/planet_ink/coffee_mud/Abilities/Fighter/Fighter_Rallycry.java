package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_Rallycry extends StdAbility
{
	public String ID() { return "Fighter_Rallycry"; }
	public String name(){ return "Rally Cry";}
	public String displayText(){return "(Rally Cry)";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public Environmental newInstance(){	return new Fighter_Rallycry();}
	private static final String[] triggerStrings = {"RALLYCRY"};
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int classificationCode(){return Ability.SKILL;}
	private int timesTicking=0;
	private int hpUp=0;

	public void affectCharState(MOB affected, CharState affectableStats)
	{
		super.affectCharState(affected,affectableStats);
		if(invoker==null) return;
		affectableStats.setHitPoints(affectableStats.getHitPoints()+hpUp);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((affected==null)||(invoker==null)||(!(affected instanceof MOB)))
			return false;
		if((!((MOB)affected).isInCombat())&&(++timesTicking>5))
			unInvoke();
		return true;
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
		{
			mob.tell("You feel less rallyed.");
			if(mob.curState().getHitPoints()>mob.baseState().getHitPoints())
				mob.curState().setHitPoints(mob.baseState().getHitPoints());
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,Affect.MSG_SPEAK,auto?"":"^S<S-NAME> scream(s) a mighty RALLYING CRY!!^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				Hashtable h=properTargets(mob,givenTarget,auto);
				if(h==null) return false;
				for(Enumeration e=h.elements();e.hasMoreElements();)
				{
					MOB target=(MOB)e.nextElement();
					target.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> seem(s) rallyed!");
					timesTicking=0;
					hpUp=mob.envStats().level();
					beneficialAffect(mob,target,0);
					target.recoverMaxState();
					if(target.fetchAffect(ID())!=null)
						mob.curState().adjHitPoints(hpUp,mob.maxState());
				}
			}
		}
		else
			beneficialWordsFizzle(mob,null,auto?"":"<S-NAME> mumble(s) a weak rally cry.");

		// return whether it worked
		return success;
	}
}