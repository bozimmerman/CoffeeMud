package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;



import java.util.*;

/*
   Copyright 2000-2010 Bo Zimmerman

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

@SuppressWarnings("unchecked")
public class Chant_SummonRockGolem extends Chant
{
	public String ID() { return "Chant_SummonRockGolem"; }
	public String name(){ return "Summon Rock Golem";}
	public String displayText(){return "(Summon Rock Golem)";}
    public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_ROCKCONTROL;}
	public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
	public int enchantQuality(){return Ability.QUALITY_INDIFFERENT;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public long flags(){return Ability.FLAG_SUMMONING;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
		{
			if((affected!=null)&&(affected instanceof MOB)&&(invoker!=null))
			{
				MOB mob=(MOB)affected;
				if(((mob.amFollowing()==null)
				||(mob.amDead())
				||(mob.location()!=invoker.location())))
				{
					mob.delEffect(this);
					if(mob.amDead()) mob.setLocation(null);
					mob.destroy();
				}
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
		{
			unInvoke();
			if(msg.source().playerStats()!=null) msg.source().playerStats().setLastUpdated(0);
		}
	}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            Room R=mob.location();
            if(R!=null)
            {
                if(R.domainType()!=Room.DOMAIN_INDOORS_CAVE)
                    return Ability.QUALITY_INDIFFERENT;
        		if(CMLib.flags().hasAControlledFollower(mob, this))
                    return Ability.QUALITY_INDIFFERENT;
            }
        }
        return super.castingQuality(mob,target);
    }
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(CMLib.flags().hasAControlledFollower(mob, this))
		{
			mob.tell("You can only control one golem.");
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if((!auto)
		&&(mob.location().domainType()!=Room.DOMAIN_INDOORS_CAVE))
		{
			mob.tell("You can not summon a rock golem here.");
			return false;
		}

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":"^S<S-NAME> chant(s) and summon(s) help from the earth.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				MOB target = determineMonster(mob, mob.envStats().level()+(2*super.getXLEVELLevel(mob)));
				target.addNonUninvokableEffect((Ability)this.copyOf());
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s), but nothing happens.");

		// return whether it worked
		return success;
	}
	public MOB determineMonster(MOB caster, int level)
	{
		MOB newMOB=CMClass.getMOB("GenMOB");
		newMOB.baseEnvStats().setLevel(adjustedLevel(caster,0));
		newMOB.setName("an golem of stone");
		newMOB.setDisplayText("an stone golem lumbers around here.");
		newMOB.setDescription("A large beast, made of rock and stone, with a hard stare.");
		CMLib.factions().setAlignment(newMOB,Faction.ALIGN_NEUTRAL);
		newMOB.baseCharStats().setMyRace(CMClass.getRace("StoneGolem"));
		newMOB.recoverEnvStats();
		newMOB.recoverCharStats();
		newMOB.baseEnvStats().setAbility(newMOB.baseEnvStats().ability()*2);
		newMOB.baseEnvStats().setArmor(CMLib.leveler().getLevelMOBArmor(newMOB)+100);
		newMOB.baseEnvStats().setAttackAdjustment(CMLib.leveler().getLevelAttack(newMOB)+100);
		newMOB.baseEnvStats().setSpeed(CMLib.leveler().getLevelMOBSpeed(newMOB)/2.0);
		newMOB.baseEnvStats().setDamage(CMLib.leveler().getLevelMOBDamage(newMOB)+20);
		newMOB.baseEnvStats().setSensesMask(newMOB.baseEnvStats().sensesMask()|EnvStats.CAN_SEE_DARK);
		newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
		Ability P=CMClass.getAbility("Prop_StatTrainer");
		if(P!=null)
		{
			P.setMiscText("NOTEACH STR=20 INT=1 WIS=10 CON=15 DEX=3 CHA=10");
			newMOB.addNonUninvokableEffect(P);
		}
		newMOB.addAbility(CMClass.getAbility("Fighter_Rescue"));
		newMOB.addAbility(CMClass.getAbility("Fighter_Kick"));
		newMOB.addBehavior(CMClass.getBehavior("CombatAbilities"));
		newMOB.setLocation(caster.location());
		newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
		newMOB.setMiscText(newMOB.text());
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.bringToLife(caster.location(),true);
		CMLib.beanCounter().clearZeroMoney(newMOB,null);
		newMOB.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,"<S-NAME> appears out of the cave walls!");
        MOB victim=caster.getVictim();
		newMOB.setStartRoom(null); // keep before postFollow for Conquest
        CMLib.commands().postFollow(newMOB,caster,true);
        if(newMOB.amFollowing()!=caster)
            caster.tell(newMOB.name()+" seems unwilling to follow you.");
        else
        if(victim!=null)
        {
            if(newMOB.getVictim()!=victim) newMOB.setVictim(victim);
            newMOB.location().showOthers(newMOB,victim,CMMsg.MSG_OK_ACTION,"<S-NAME> start(s) attacking <T-NAMESELF>!");
        }
		newMOB.addNonUninvokableEffect(this);
		return(newMOB);
	}
}
