package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Clone extends Spell
{
	public String ID() { return "Spell_Clone"; }
	public String name(){return "Clone";}
	public String displayText(){return "(Clone)";}
	public int quality(){return INDIFFERENT;};
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int overrideMana(){return 200;}
	public Environmental newInstance(){	return new Spell_Clone();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_TRANSMUTATION;}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected)||msg.amISource(((MOB)affected).amFollowing()))
		&&(msg.sourceMinor()==CMMsg.TYP_QUIT))
			unInvoke();
	}

	public void unInvoke()
	{
		MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(mob!=null))
		{
			if(mob.amDead()) mob.setLocation(null);
			mob.destroy();
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			invoker=mob;
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> incant(s), feeling his body split in two.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				MOB myMonster = determineMonster(mob);
				Behavior B=CMClass.getBehavior("CombatAbilities");
				myMonster.addBehavior(B);
				B.startBehavior(myMonster);
				if(Dice.rollPercentage()<50)
				{
					if(mob.getAlignment()<500)
					{
						B=CMClass.getBehavior("MobileGoodGuardian");
						myMonster.addBehavior(B);
						B.startBehavior(myMonster);
						myMonster.setAlignment(1000);
					}
					else
					{
						B=CMClass.getBehavior("MobileAggressive");
						myMonster.addBehavior(B);
						B.startBehavior(myMonster);
						myMonster.setAlignment(0);
					}
					myMonster.setVictim(mob);
				}
				else
				{
					B=CMClass.getBehavior("Mobile");
					myMonster.addBehavior(B);
					B.startBehavior(myMonster);
					myMonster.setVictim(mob.getVictim());
					CommonMsgs.follow(myMonster,mob,true);
					if(myMonster.amFollowing()!=mob)
						mob.tell(myMonster.name()+" seems unwilling to follow you.");
				}
				invoker=mob;
				beneficialAffect(mob,myMonster,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> attempt(s) to clone himself, but fails.");

		// return whether it worked
		return success;
	}
	public MOB determineMonster(MOB caster)
	{
		MOB newMOB=(MOB)caster.copyOf();
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.text();
		newMOB.setSession(null);
		while(newMOB.numBehaviors()>0)
		{
			Behavior B=newMOB.fetchBehavior(0);
			newMOB.delBehavior(B);
		}
		newMOB.bringToLife(caster.location(),true);
		newMOB.setMoney(0);
		newMOB.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,"<S-NAME> appears!");
		caster.location().recoverRoomStats();
		newMOB.setStartRoom(null);
		return(newMOB);
	}
}
