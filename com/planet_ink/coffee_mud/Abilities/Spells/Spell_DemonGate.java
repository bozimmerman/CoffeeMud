package com.planet_ink.coffee_mud.Abilities.Spells;
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
public class Spell_DemonGate extends Spell
{
	public String ID() { return "Spell_DemonGate"; }
	public String name(){return "Demon Gate";}
	public String displayText(){return "(Demon Gate)";}
	public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
	public int enchantQuality(){return Ability.QUALITY_INDIFFERENT;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	protected int overrideMana(){return 100;}
	MOB myTarget=null;
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;}
	public long flags(){return Ability.FLAG_SUMMONING;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
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
		{
			unInvoke();
			if(msg.source().playerStats()!=null) msg.source().playerStats().setLastUpdated(0);
		}
	}

	public void unInvoke()
	{
		MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(mob!=null))
		{
			Room R=mob.location();
			if(R!=null)
			{
	            if(mob.amFollowing()!=null)
	                R.showOthers(mob,mob.amFollowing(),CMMsg.MSG_OK_ACTION,"^F^<FIGHT^><S-NAME> uses the fight to wrest itself from out of <T-YOUPOSS> control!^?%0DTo <T-YOUPOSS> great relief, it disappears back into its home plane.^</FIGHT^>^?");
	            else
	                R.showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> disappears back into its home plane.");
			}
			if(mob.amDead()) mob.setLocation(null);
			mob.destroy();
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":"^S<S-NAME> open(s) the gates of the abyss, incanting angrily.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
                MOB otherMonster=mob.location().fetchInhabitant("the great demonbeast$");
				MOB myMonster = determineMonster(mob, mob.envStats().level()+(getXLEVELLevel(mob)+(2*getX1Level(mob))));
                if(otherMonster!=null)
                {
                    myMonster.location().showOthers(myMonster,mob,CMMsg.MSG_OK_ACTION,"^F^<FIGHT^><S-NAME> wrests itself from out of <T-YOUPOSS> control!^</FIGHT^>^?");
                    myMonster.setVictim(otherMonster);
                }
                else
				if(CMLib.dice().rollPercentage()<10)
                {
					myMonster.location().showOthers(myMonster,mob,CMMsg.MSG_OK_ACTION,"^F^<FIGHT^><S-NAME> wrests itself from out of <T-YOUPOSS> control!^</FIGHT^>^?");
                    myMonster.setVictim(mob);
                }
				else
				{
					myMonster.setVictim(mob.getVictim());
					CMLib.commands().postFollow(myMonster,mob,true);
					if(myMonster.amFollowing()!=mob)
						mob.tell(myMonster.name()+" seems unwilling to follow you.");
				}
				invoker=mob;
				beneficialAffect(mob,myMonster,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> attempt(s) to open the gates of the abyss, but fail(s).");

		// return whether it worked
		return success;
	}
	public MOB determineMonster(MOB caster, int level)
	{
		MOB newMOB=CMClass.getMOB("GenRideable");
		Rideable ride=(Rideable)newMOB;
		newMOB.baseEnvStats().setAbility(22 + super.getXLEVELLevel(caster));
		newMOB.baseEnvStats().setLevel(level+ 2 + super.getXLEVELLevel(caster));
		CMLib.factions().setAlignment(newMOB,Faction.ALIGN_EVIL);
		newMOB.baseEnvStats().setWeight(850);
		newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
		newMOB.baseCharStats().setStat(CharStats.STAT_STRENGTH,18);
		newMOB.baseCharStats().setStat(CharStats.STAT_DEXTERITY,18);
		newMOB.baseCharStats().setStat(CharStats.STAT_CONSTITUTION,18);
		newMOB.baseCharStats().setMyRace(CMClass.getRace("Demon"));
		newMOB.baseCharStats().getMyRace().startRacing(newMOB,false);
		newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'M');
		newMOB.recoverEnvStats();
		newMOB.recoverCharStats();
		newMOB.baseEnvStats().setArmor(CMLib.leveler().getLevelMOBArmor(newMOB));
		newMOB.baseEnvStats().setAttackAdjustment(CMLib.leveler().getLevelAttack(newMOB));
		newMOB.baseEnvStats().setDamage(CMLib.leveler().getLevelMOBDamage(newMOB));
		newMOB.baseEnvStats().setSpeed(CMLib.leveler().getLevelMOBSpeed(newMOB));
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
		CMLib.beanCounter().clearZeroMoney(newMOB,null);
        newMOB.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,"<S-NAME> tears through the fabric of reality and steps into this world!");
		caster.location().recoverRoomStats();
		newMOB.setStartRoom(null);
		return(newMOB);
	}
}
