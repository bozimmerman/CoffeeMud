package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_Treeform extends Chant
{
	public String ID() { return "Chant_Treeform"; }
	public String name(){ return "Treeform";}
	public String displayText(){return "(Treeform)";}
	public int quality(){return Ability.INDIFFERENT;}
	public int maxRange(){return 3;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	private CharState oldState=null;
	public Environmental newInstance(){	return new Chant_Treeform();}
	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if(affect.source().getVictim()==mob)
			affect.source().setVictim(null);
		if(mob.isInCombat())
		{
			if(mob.getVictim()!=null)
				mob.getVictim().makePeace();
			mob.makePeace();
		}
		mob.recoverMaxState();
		mob.resetToMaxState();
		mob.curState().setHunger(1000);
		mob.curState().setThirst(1000);
		mob.recoverCharStats();
		mob.recoverEnvStats();

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if(affect.amISource(mob))
		{
			if((affect.sourceMinor()==Affect.TYP_ENTER)||(affect.sourceMinor()==Affect.TYP_LEAVE))
				unInvoke();
			else
			if((!Util.bset(affect.sourceMajor(),Affect.ACT_GENERAL))
			&&(affect.sourceMajor()>0))
			{
				mob.tell("Trees can't do that.");
				return false;
			}
		}
		if(affect.amITarget(mob))
		{
			if(affect.targetMinor()==Affect.TYP_WEAPONATTACK)
			{
				affect.source().tell("Attack a tree?!");
				affect.source().setVictim(null);
				mob.setVictim(null);
				return false;
			}
			else
			{
				Item item=CMClass.getItem("GenResource");
				item.setName(mob.name());
				item.setDescription(mob.description());
				item.setDisplayText(mob.displayText());
				item.setMaterial(EnvResource.RESOURCE_WOOD);
				item.setGettable(false);
				item.envStats().setWeight(2000);
				FullMsg msg=new FullMsg(affect.source(),item,affect.targetCode(),null);
				if(!okAffect(msg))
					return false;
			}
		}
		if(!super.okAffect(affect))
			return false;
		
		if(affect.source().getVictim()==mob)
			affect.source().setVictim(null);
		if(mob.isInCombat())
		{
			if(mob.getVictim()!=null)
				mob.getVictim().makePeace();
			mob.makePeace();
		}
		return true;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setReplacementName("a tree that reminds you of "+affected.name());
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_MOVE);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_HEAR);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SMELL);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SPEAK);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_TASTE);
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked)
		{
			mob.tell("Your body returns to normal!");
			if(oldState!=null)
			{
				mob.curState().setHitPoints(oldState.getHitPoints());
				mob.curState().setHunger(oldState.getHunger());
				mob.curState().setMana(oldState.getMana());
				mob.curState().setMovement(oldState.getMovement());
				mob.curState().setThirst(oldState.getThirst());
			}
			else
			{
				mob.curState().setHitPoints(1);
				mob.curState().setMana(0);
				mob.curState().setMovement(0);
				mob.curState().setHunger(0);
				mob.curState().setThirst(0);
			}
			ExternalPlay.standIfNecessary(mob);
		}
	}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell("You must be outdoors to try this.");
			return false;
		}
		
		MOB target=mob;
		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You are already a tree.");
			return false;
		}

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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					int a=0;
					while(a<target.numAffects())
					{
						Ability A=target.fetchAffect(a);
						int s=target.numAffects();
						if(A!=null) A.unInvoke();
						if(target.numAffects()==s)
							a++;
					}
					target.makePeace();
					ExternalPlay.standIfNecessary(target);
					oldState=target.curState().cloneCharState();
					success=beneficialAffect(mob,target,mob.envStats().level()*50);
					if(success)
					{
						mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> transform(s) into a tree!!");
						mob.tell("To return to your flesh body, try to leave this area.");
					}
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but the magic fades.");

		// return whether it worked
		return success;
	}
}