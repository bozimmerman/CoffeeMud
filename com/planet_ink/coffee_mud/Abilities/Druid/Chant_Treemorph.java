package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_Treemorph extends Chant
{
	public String ID() { return "Chant_Treemorph"; }
	public String name(){ return "Treemorph";}
	public String displayText(){return "(Treemorph)";}
	public int quality(){return Ability.MALICIOUS;}
	Item tree=null;
	Race treeForm=null;
	public Environmental newInstance(){	return new Chant_Treemorph();}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Host.TICK_MOB)
		&&(affected!=null)
		&&(tree!=null)
		&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if((tree.owner()!=null)&&(tree.owner()!=mob.location()))
			{
				Room room=null;
				if(tree.owner() instanceof MOB)
					room=((MOB)tree.owner()).location();
				else
				if(tree.owner() instanceof Room)
					room=(Room)tree.owner();
				if((room!=null)&&(room!=mob.location()))
					room.bringMobHere(mob,false);
			}
		}
		return super.tick(ticking,tickID);
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(treeForm!=null)
			affectableStats.setMyRace(treeForm);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if(msg.source().getVictim()==mob)
				msg.source().setVictim(null);
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
			if(msg.amISource(mob))
			{
				if((!Util.bset(msg.sourceMajor(),CMMsg.MASK_GENERAL))
				&&(msg.sourceMajor()>0))
				{
					mob.tell("Trees can't do that.");
					return false;
				}
			}
		}
		if(!super.okMessage(myHost,msg))
			return false;

		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if(msg.source().getVictim()==affected)
				msg.source().setVictim(null);
			if(mob.isInCombat())
			{
				if(mob.getVictim()!=null)
					mob.getVictim().makePeace();
				mob.makePeace();
			}
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
		if((treeForm!=null)&&(affected instanceof MOB))
		{
			if(affected.name().indexOf(" ")>0)
				affectableStats.setName("a "+treeForm.name()+" called "+affected.name());
			else
				affectableStats.setName(affected.name()+" the "+treeForm.name());
			treeForm.setHeightWeight(((MOB)affected).baseEnvStats(),'M');

			//affectableStats.setReplacementName("a tree of "+affected.name());
			affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_MOVE);
			affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_HEAR);
			affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SMELL);
			affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SPEAK);
			affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_TASTE);
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_NOT_SEEN);
		}
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
			if(tree!=null) tree.destroy();
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> <S-IS-ARE> no longer a tree.");
			mob.curState().setHitPoints(1);
			mob.curState().setMana(0);
			mob.curState().setMovement(0);
			mob.curState().setHunger(0);
			mob.curState().setThirst(0);
			ExternalPlay.standIfNecessary(mob);
		}
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


		int levelDiff=target.envStats().level()-mob.envStats().level();
		if(levelDiff<0) levelDiff=0;
		boolean success=profficiencyCheck(-(levelDiff*10),auto);
		treeForm=CMClass.getRace("TreeGolem");
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) at <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					int a=0;
					while(a<target.numEffects())
					{
						Ability A=target.fetchEffect(a);
						int s=target.numEffects();
						if(A!=null) A.unInvoke();
						if(target.numEffects()==s)
							a++;
					}
					target.makePeace();
					ExternalPlay.standIfNecessary(target);
					tree=CMClass.getItem("GenItem");
					tree.setName("a oak tree");
					tree.setDisplayText("an oak tree that reminds you of "+target.name()+" is growing here.");
					tree.setDescription("It`s a tall oak tree, which seems to remind you of "+target.name()+".");
					tree.setMaterial(EnvResource.RESOURCE_OAK);
					tree.baseEnvStats().setWeight(5000);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> turn(s) into a tree!!");
					success=maliciousAffect(mob,target,mob.envStats().level()*50,-1);
					Ability A=target.fetchEffect(ID());
					if(success&&(A!=null))
					{
						mob.location().addItem(tree);
						tree.addEffect(A);
						A.setAffectedOne(target);
						tree.recoverEnvStats();
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) at <T-NAMESELF>, but the magic fades.");

		// return whether it worked
		return success;
	}
}
