package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_KnowBliss extends Spell
{
	public String ID() { return "Spell_KnowBliss"; }
	public String name(){return "Know Bliss";}
	public String displayText(){return "(Know Bliss)";}
	public int quality(){ return MALICIOUS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int maxRange(){return 5;}
	public int hpAdjustment=0;
	public Environmental newInstance(){	return new Spell_KnowBliss();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();
		if(canBeUninvoked())
		{
			mob.tell("You feel less blissful.");
			if((mob.isMonster())
			   &&(!mob.amDead())
			   &&(mob.location()!=null)
			   &&(mob.location()!=mob.getStartRoom()))
			{
				CoffeeUtensils.wanderAway(mob,true);
				mob.getStartRoom().bringMobHere(mob,false);
			}
		}
	}

	public void affect(Environmental myHost, Affect msg)
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(msg.amITarget(mob)
		&&((msg.targetCode()&Affect.MASK_HURT)>0)
		&&((msg.targetCode()-Affect.MASK_HURT)>0))
			unInvoke();
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Host.MOB_TICK)
		{
			// undo the affects of this spell
			if((affected==null)||(!(affected instanceof MOB)))
				return super.tick(ticking,tickID);
			MOB mob=(MOB)affected;
			CoffeeUtensils.wanderAway(mob,false);
		}
		return super.tick(ticking,tickID);
	}
	
	public boolean okAffect(Environmental myHost, Affect msg)
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okAffect(myHost,msg);
		MOB mob=(MOB)affected;
		if(msg.amISource(mob)
		&&(msg.targetCode()&Affect.MASK_MALICIOUS)>0)
		{
			mob.tell("Nah, you feel too happy to do that.");
			mob.setVictim(null);
			return false;
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> encant(s) happily at <T-NAMESELF>!^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				if(target.location()==mob.location())
				{
					target.location().show(target,null,Affect.MSG_OK_ACTION,"<S-NAME> smile(s) most peculiarly!");
					maliciousAffect(mob,target,0,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_MIND|(auto?Affect.MASK_GENERAL:0));
					target.makePeace();
					if(mob.getVictim()==target)
						mob.makePeace();
					for(int m=0;m<target.location().numInhabitants();m++)
					{
						MOB M=target.location().fetchInhabitant(m);
						if((M!=null)&&(M.getVictim()==target))
							M.makePeace();
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> encant(s) happily at <T-NAMESELF>, but nothing more happens.");

		// return whether it worked
		return success;
	}
}
