package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_DemonGate extends Spell
{
	MOB myMonster=null;
	MOB myTarget=null;
	public Spell_DemonGate()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Demon Gate";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Demon Gate)";

		quality=Ability.BENEFICIAL_SELF;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(7);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_DemonGate();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_CONJURATION;
	}

	public boolean tick(int tickID)
	{
		if(tickID==Host.MOB_TICK)
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if(myTarget==null)
					myTarget=mob.getVictim();
				else
				if(myTarget!=mob.getVictim())
					((MOB)affected).destroy();
			}
		}
		return super.tick(tickID);
	}
	
	public void unInvoke()
	{
		super.unInvoke();
		if((myMonster!=null)&&(invoker!=null))
		{
			MOB targ=myMonster;
			myMonster=null;
			targ.destroy();
		}
	}

	
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDamage(affectableStats.damage()+20);
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+100);
		affectableStats.setArmor(affectableStats.armor()+20);
		affectableStats.setSpeed(affectableStats.speed()+2);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			invoker=mob;
			FullMsg msg=new FullMsg(mob,null,this,affectType,auto?"":"<S-NAME> open(s) the gates of the abyss, chanting angrilly.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				myMonster = determineMonster(mob, mob.envStats().level());
				if(Dice.rollPercentage()<25)
					myMonster.setVictim(mob);
				else
				{
					myMonster.setVictim(mob.getVictim());
					ExternalPlay.follow(myMonster,mob,true);
				}
				invoker=mob;
				beneficialAffect(mob,myMonster,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> attempt(s) to open the gates of the abyss, but fails.");

		// return whether it worked
		return success;
	}
	public MOB determineMonster(MOB caster, int level)
	{
		MOB newMOB=(MOB)CMClass.getMOB("GenRideable");
		Rideable ride=(Rideable)newMOB;
		newMOB.baseEnvStats().setAbility(43);
		newMOB.baseEnvStats().setLevel(level+10);
		newMOB.baseEnvStats().setWeight(850);
		newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
		newMOB.baseCharStats().setStat(CharStats.STRENGTH,25);
		newMOB.baseCharStats().setStat(CharStats.DEXTERITY,25);
		newMOB.baseCharStats().setStat(CharStats.CONSTITUTION,25);
		newMOB.baseCharStats().setMyRace(CMClass.getRace("Unique"));
		newMOB.baseCharStats().getMyRace().setHeightWeight(newMOB.baseEnvStats(),'M');
		newMOB.baseCharStats().setStat(CharStats.GENDER,(int)'M');
		newMOB.setName("the great demonbeast");
		newMOB.setDisplayText("a horrendous demonbeast is stalking around here");
		newMOB.setDescription("Blood red skin with massive horns, and of course muscles in places you didn`t know existed.");
		ride.setMobCapacity(2);
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.text();
		newMOB.bringToLife(caster.location());
		caster.location().recoverRoomStats();
		newMOB.setStartRoom(null);
		return(newMOB);


	}
}
