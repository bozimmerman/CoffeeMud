package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_FleshRock extends Prayer
{
	public String ID() { return "Prayer_FleshRock"; }
	public String name(){return "Flesh Rock";}
	public String displayText(){return "(Flesh to Rock)";}
	public int quality(){return MALICIOUS;};
	public int holyQuality(){ return HOLY_EVIL;}
	public Environmental newInstance(){	return new Prayer_FleshRock();}

	public Item statue=null;
	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Host.MOB_TICK)
		&&(affected!=null)
		&&(statue!=null)
		&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if((statue.owner()!=null)&&(statue.owner()!=mob.location()))
			{
				Room room=null;
				if(statue.owner() instanceof MOB)
					room=((MOB)statue.owner()).location();
				else
				if(statue.owner() instanceof Room)
					room=(Room)statue.owner();
				if((room!=null)&&(room!=mob.location()))
					room.bringMobHere(mob,false);
			}
		}
		return super.tick(ticking,tickID);
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(affected instanceof MOB)
		{
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
				if((!Util.bset(affect.sourceMajor(),Affect.MASK_GENERAL))
				&&(affect.sourceMajor()>0))
				{
					mob.tell("Statues can't do that.");
					return false;
				}
			}
		}
		if(!super.okAffect(myHost,affect))
			return false;

		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if(affect.source().getVictim()==affected)
				affect.source().setVictim(null);
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
		if(affected instanceof MOB)
		{
			//affectableStats.setReplacementName("a statue of "+affected.name());
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
			if(statue!=null) statue.destroyThis();
			mob.tell("Your flesh returns to normal!");
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


		boolean success=profficiencyCheck(-(target.envStats().level()*3),auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" and point(s) at <T-NAMESELF>.^?");
			if(mob.location().okAffect(mob,msg))
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
					statue=CMClass.getItem("GenItem");
					String name=target.name();
					if(name.startsWith("A ")) name="a "+name.substring(2);
					if(name.startsWith("An ")) name="an "+name.substring(3);
					if(name.startsWith("The ")) name="the "+name.substring(4);
					statue.setName("a rocky statue of "+name);
					statue.setDisplayText("a rocky statue of "+name+" stands here.");
					statue.setDescription("It`s a hard rocky statue, which looks exactly like "+name+".");
					statue.setMaterial(EnvResource.RESOURCE_GRANITE);
					statue.baseEnvStats().setWeight(2000);
					mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> turn(s) into rock!!");
					success=maliciousAffect(mob,target,mob.envStats().level()*50,-1);
					Ability A=target.fetchAffect(ID());
					if(success&&(A!=null))
					{
						mob.location().addItem(statue);
						statue.addAffect(A);
						A.setAffectedOne(target);
						statue.recoverEnvStats();
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" and point(s) at <T-NAMESELF>, but nothing happens.");

		// return whether it worked
		return success;
	}
}
