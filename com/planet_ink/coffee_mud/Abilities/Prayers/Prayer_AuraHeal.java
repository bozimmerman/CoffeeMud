package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_AuraHeal extends Prayer
{
	public String ID() { return "Prayer_AuraHeal"; }
	public String name(){ return "Aura of Healing";}
	public String displayText(){ return "(Heal Aura)";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	public int quality(){ return MALICIOUS;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_HEALING;;}
	public Environmental newInstance(){	return new Prayer_AuraHeal();	}
	private int tickDown=4;

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof Room)))
			return;
		Room R=(Room)affected;

		super.unInvoke();

		if(canBeUninvoked())
			R.showHappens(CMMsg.MSG_OK_VISUAL,"The healing aura around you fades.");
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof Room)))
			return super.tick(ticking,tickID);

		if((--tickDown)>=0) return super.tick(ticking,tickID);
		tickDown=4;
		
		Hashtable H=null;
		if((invoker()!=null)&&(invoker().location()==affected))
		{
			H=new Hashtable();
			invoker().getGroupMembers(H);
		}
		Room R=(Room)affected;
		for(int i=0;i<R.numInhabitants();i++)
		{
			MOB M=R.fetchInhabitant(i);
			if((M!=null)
			   &&(M.curState().getHitPoints()<M.maxState().getHitPoints())
			   &&((H==null)
				||(M.getVictim()==null)
				||(!H.containsKey(M.getVictim()))))
			{
				if(invoker()!=null)
				{
					int healing=Dice.roll(2,adjustedLevel(invoker()),4);
					MUDFight.postHealing(invoker(),M,this,CMMsg.MASK_GENERAL|CMMsg.TYP_CAST_SPELL,healing,null);
				}
				else
				{
					int healing=Dice.roll(2,CMAble.lowestQualifyingLevel(ID()),4);
					MUDFight.postHealing(M,M,this,CMMsg.MASK_GENERAL|CMMsg.TYP_CAST_SPELL,healing,null);
				}
				M.tell("You feel a little better!");
			}
		}
		return super.tick(ticking,tickID);
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Room target=mob.location();
		if(target==null) return false;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell("The aura of healing is already here.");
			return false;
		}
		if(target.fetchEffect("Prayer_AuraHarm")!=null)
		{
			target.fetchEffect("Prayer_AuraHarm").unInvoke();
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" for all to feel better.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"A healing aura descends over the area!");
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for an aura of healing, but <S-HIS-HER> plea is not answered.");


		// return whether it worked
		return success;
	}
}