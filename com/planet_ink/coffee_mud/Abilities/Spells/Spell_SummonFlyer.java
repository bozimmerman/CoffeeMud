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
public class Spell_SummonFlyer extends Spell
{
	public String ID() { return "Spell_SummonFlyer"; }
	public String name(){return "Summon Flyer";}
	public String displayText(){return "(Summon Flyer)";}
	public int quality(){ return OK_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_CONJURATION;}
	public long flags(){return Ability.FLAG_SUMMONING;}
	protected int overrideMana(){return 50;}

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

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected)||msg.amISource(((MOB)affected).amFollowing()))
		&&(msg.sourceMinor()==CMMsg.TYP_QUIT))
			unInvoke();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==MudHost.TICK_MOB)
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if(!mob.isInCombat())
				if(((mob.amFollowing()==null)
				||(mob.location()==null)
				||(mob.amDead())
				||(invoker==null)
				||(invoker.location()==null)
				||((invoker!=null)&&(mob.location()!=invoker.location())&&(invoker.riding()!=affected))))
				{
					mob.delEffect(this);
					if(mob.amDead()) mob.setLocation(null);
					mob.destroy();
				}
                if((mob.amFollowing()==null)&&(mob.curState().getHitPoints()<((mob.maxState().getHitPoints()/10)*3)))
                {
                    mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> flees.");
                    mob.delEffect(this);
                    if(mob.amDead()) mob.setLocation(null);
                    mob.destroy();
                }
			}
		}
		return super.tick(ticking,tickID);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			invoker=mob;
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> magically call(s) for a loyal steed.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				MOB target = determineMonster(mob, mob.envStats().level());
                MOB squabble = checkPack(target, mob);
                target.addNonUninvokableEffect( (Ability) copyOf());
                if(squabble==null)
				{
                    if (target.isInCombat()) target.makePeace();
					CommonMsgs.follow(target,mob,true);
                    invoker=mob;
                    if (target.amFollowing() != mob)
                        mob.tell(target.name() + " seems unwilling to follow you.");
                }
                else
                {
                    squabble.location().showOthers(squabble,target,CMMsg.MSG_OK_ACTION,"^F^<FIGHT^><S-NAME> bares its teeth at <T-NAME> and begins to attack!^</FIGHT^>^?");
                    target.setVictim(squabble);
                }
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> call(s) for a steed, but choke(s) on the words.");

		// return whether it worked
		return success;
	}
	public MOB determineMonster(MOB caster, int level)
	{

		MOB newMOB=CMClass.getMOB("GenRideable");
		Rideable ride=(Rideable)newMOB;
		newMOB.baseEnvStats().setAbility(11);
		newMOB.baseEnvStats().setDisposition(newMOB.baseEnvStats().disposition()|EnvStats.IS_FLYING);
		newMOB.baseEnvStats().setLevel(level);
		newMOB.baseEnvStats().setWeight(500);
		newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
		newMOB.baseCharStats().setMyRace(CMClass.getRace("Horse"));
		newMOB.baseCharStats().setStat(CharStats.GENDER,'M');
		newMOB.baseCharStats().getMyRace().startRacing(newMOB,false);
		newMOB.recoverEnvStats();
		newMOB.recoverCharStats();
		newMOB.baseEnvStats().setArmor(newMOB.baseCharStats().getCurrentClass().getLevelArmor(newMOB));
		newMOB.baseEnvStats().setAttackAdjustment(newMOB.baseCharStats().getCurrentClass().getLevelAttack(newMOB));
		newMOB.baseEnvStats().setDamage(newMOB.baseCharStats().getCurrentClass().getLevelDamage(newMOB));
		newMOB.baseEnvStats().setSpeed(newMOB.baseCharStats().getCurrentClass().getLevelSpeed(newMOB));
		newMOB.setName("a flying warhorse");
		newMOB.setDisplayText("a warhorse with broad powerful wings stands here");
		newMOB.setDescription("A ferocious, fleet of foot, flying friend.");
		ride.setRideBasis(Rideable.RIDEABLE_AIR);
		ride.setRiderCapacity(2);
		newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		newMOB.recoverMaxState();
		newMOB.setAlignment(500);
		newMOB.resetToMaxState();
		newMOB.text();
		newMOB.bringToLife(caster.location(),true);
		newMOB.setMoney(0);
		newMOB.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,"<S-NAME> appears!");
		caster.location().recoverRoomStats();
		newMOB.setStartRoom(null);
		return(newMOB);
	}

    public MOB checkPack(MOB newPackmate, MOB mob)
	{
        for(int i=0;i<mob.numFollowers();i++)
		{
            MOB possibleBitch = mob.fetchFollower(i);
            if(newPackmate.Name().equalsIgnoreCase(possibleBitch.Name())
            && (Dice.rollPercentage()-mob.charStats().getStat(CharStats.CHARISMA)+newPackmate.envStats().level() > 75))
                return possibleBitch;
        }
        return null;
    }
}
