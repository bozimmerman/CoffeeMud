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
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

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
			if((msg.sourceMinor()==CMMsg.TYP_ENTER)||(msg.sourceMinor()==CMMsg.TYP_LEAVE))
				unInvoke();
			else
			if((!Util.bset(msg.sourceMajor(),CMMsg.MASK_GENERAL))
			&&(msg.sourceMajor()>0))
			{
				mob.tell("Trees can't do that.");
				return false;
			}
		}
		if(msg.amITarget(mob))
		{
			if(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
			{
				msg.source().tell("Attack a tree?!");
				msg.source().setVictim(null);
				mob.setVictim(null);
				return false;
			}
			else
			{
				Item item=CMClass.getItem("GenResource");
				item.setName(mob.Name());
				item.setDescription(mob.description());
				item.setDisplayText(mob.displayText());
				item.setMaterial(EnvResource.RESOURCE_WOOD);
				Sense.setGettable(item,false);
				item.envStats().setWeight(2000);
				FullMsg msg2=new FullMsg(msg.source(),item,msg.targetCode(),null);
				if(!okMessage(msg.source(),msg2))
					return false;
			}
		}
		if(!super.okMessage(myHost,msg))
			return false;

		if(msg.source().getVictim()==mob)
			msg.source().setVictim(null);
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
		affectableStats.setName("a tree that reminds you of "+affected.name());
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
		if(canBeUninvoked())
		{
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> body is no longer treeish.");
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
			CommonMsgs.stand(mob,true);
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
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already a tree.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;


		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					target.makePeace();
					CommonMsgs.stand(target,true);
					oldState=target.curState().cloneCharState();
					success=beneficialAffect(mob,target,mob.envStats().level()*50);
					if(success)
					{
						mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> transform(s) into a tree!!");
						target.tell("To return to your flesh body, try to leave this area.");
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