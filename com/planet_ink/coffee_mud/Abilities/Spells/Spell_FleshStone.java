package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_FleshStone extends Spell
{
	public String ID() { return "Spell_FleshStone"; }
	public String name(){return "Flesh Stone";}
	public String displayText(){return "(Flesh to Stone)";}
	public int quality(){return MALICIOUS;};
	public Environmental newInstance(){	return new Spell_FleshStone();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_TRANSMUTATION;}

	public Item statue=null;
	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==MudHost.TICK_MOB)
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
					mob.tell("Statues can't do that.");
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
			if(statue!=null) statue.destroy();
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> flesh returns to normal.");
			mob.curState().setHitPoints(1);
			mob.curState().setMana(0);
			mob.curState().setMovement(0);
			mob.curState().setHunger(0);
			mob.curState().setThirst(0);
			CommonMsgs.stand(mob,true);
		}
	}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// if they can't hear the sleep spell, it
		// won't happen
		if((!auto)&&(!Sense.canBeHeardBy(mob,target)))
		{
			mob.tell(target.charStats().HeShe()+" can't hear your words.");
			return false;
		}


		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;


		int levelDiff=target.envStats().level()-mob.envStats().level();
		if(levelDiff<0) levelDiff=0;
		boolean success=profficiencyCheck(mob,-(levelDiff*5),auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> incant(s) at <T-NAMESELF>.^?");
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
					CommonMsgs.stand(target,true);
					statue=CMClass.getItem("GenItem");
					String name=target.name();
					if(name.startsWith("A ")) name="a "+name.substring(2);
					if(name.startsWith("An ")) name="an "+name.substring(3);
					if(name.startsWith("The ")) name="the "+name.substring(4);
					statue.setName("a statue of "+name);
					statue.setDisplayText("a statue of "+name+" stands here.");
					statue.setDescription("It`s a hard granite statue, which looks exactly like "+name+".");
					statue.setMaterial(EnvResource.RESOURCE_GRANITE);
					statue.baseEnvStats().setWeight(2000);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> turn(s) into stone!!");
					success=maliciousAffect(mob,target,mob.envStats().level()*25,-1);
					Ability A=target.fetchEffect(ID());
					if(success&&(A!=null))
					{
						mob.location().addItem(statue);
						statue.addEffect(A);
						A.setAffectedOne(target);
						statue.recoverEnvStats();
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> incant(s) at <T-NAMESELF>, but the spell fades.");

		// return whether it worked
		return success;
	}
}
