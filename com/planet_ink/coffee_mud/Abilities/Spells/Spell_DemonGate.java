package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Spell_DemonGate extends Spell
{
	public String ID() { return "Spell_DemonGate"; }
	public String name(){return "Demon Gate";}
	public String displayText(){return "(Demon Gate)";}
	public int quality(){return BENEFICIAL_SELF;};
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	protected int overrideMana(){return 100;}
	MOB myTarget=null;
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_CONJURATION;}
	public long flags(){return Ability.FLAG_SUMMONING;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==MudHost.TICK_MOB)
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if(myTarget==null)
					myTarget=mob.getVictim();
				else
				if(myTarget!=mob.getVictim())
                    unInvoke();
			}
		}
		return super.tick(ticking,tickID);
	}

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
            if(mob.amFollowing()!=null)
                mob.location().showOthers(mob,mob.amFollowing(),CMMsg.MSG_OK_ACTION,"^F^<FIGHT^><S-NAME> uses the fight to wrest itself from out of <T-YOUPOSS> control!^?%0DTo <T-YOUPOSS> great relief, it disappears back into its home plane.^</FIGHT^>^?");
            else
                mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> disappears back into it's home plane.");
			if(mob.amDead()) mob.setLocation(null);
			mob.destroy();
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

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> open(s) the gates of the abyss, incanting angrily.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				MOB myMonster = determineMonster(mob, mob.envStats().level());
				if(Dice.rollPercentage()<10)
                { 
					myMonster.location().showOthers(myMonster,mob,CMMsg.MSG_OK_ACTION,"^F^<FIGHT^><S-NAME> wrests itself from out of <T-YOUPOSS> control!^</FIGHT^>^?");
                    myMonster.setVictim(mob);
                }
				else
				{
					myMonster.setVictim(mob.getVictim());
					CommonMsgs.follow(myMonster,mob,true);
					if(myMonster.amFollowing()!=mob)
						mob.tell(myMonster.name()+" seems unwilling to follow you.");
				}
				invoker=mob;
				beneficialAffect(mob,myMonster,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> attempt(s) to open the gates of the abyss, but fails.");

		// return whether it worked
		return success;
	}
	public MOB determineMonster(MOB caster, int level)
	{
		MOB newMOB=CMClass.getMOB("GenRideable");
		Rideable ride=(Rideable)newMOB;
		newMOB.baseEnvStats().setAbility(43);
		newMOB.baseEnvStats().setLevel(level+10);
		newMOB.setAlignment(0);
		newMOB.baseEnvStats().setWeight(850);
		newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
		newMOB.baseCharStats().setStat(CharStats.STRENGTH,25);
		newMOB.baseCharStats().setStat(CharStats.DEXTERITY,25);
		newMOB.baseCharStats().setStat(CharStats.CONSTITUTION,25);
		newMOB.baseCharStats().setMyRace(CMClass.getRace("Demon"));
		newMOB.baseCharStats().getMyRace().startRacing(newMOB,false);
		newMOB.baseCharStats().setStat(CharStats.GENDER,'M');
		newMOB.recoverEnvStats();
		newMOB.recoverCharStats();
		newMOB.baseEnvStats().setArmor(newMOB.baseCharStats().getCurrentClass().getLevelArmor(newMOB));
		newMOB.baseEnvStats().setAttackAdjustment(newMOB.baseCharStats().getCurrentClass().getLevelAttack(newMOB));
		newMOB.baseEnvStats().setDamage(newMOB.baseCharStats().getCurrentClass().getLevelDamage(newMOB));
		newMOB.baseEnvStats().setSpeed(newMOB.baseCharStats().getCurrentClass().getLevelSpeed(newMOB));
		newMOB.setName("the great demonbeast");
		newMOB.setDisplayText("a horrendous demonbeast is stalking around here");
		newMOB.setDescription("Blood red skin with massive horns, and of course muscles in places you didn`t know existed.");
		newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
		ride.setRiderCapacity(2);
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.text();
		newMOB.bringToLife(caster.location(),true);
		BeanCounter.clearZeroMoney(newMOB,null);
        newMOB.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,"<S-NAME> tears through the fabric of reality and steps into this world!");
		caster.location().recoverRoomStats();
		newMOB.setStartRoom(null);
		return(newMOB);
	}
}
