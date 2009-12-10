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
public class Spell_Clone extends Spell
{
	public String ID() { return "Spell_Clone"; }
	public String name(){return "Clone";}
	public String displayText(){return "(Clone)";}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int overrideMana(){return 200;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_TRANSMUTATION;}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)&&(affected instanceof MOB))
		{
			if((msg.amISource((MOB)affected)||msg.amISource(((MOB)affected).amFollowing()))
			&&(msg.sourceMinor()==CMMsg.TYP_QUIT))
			{
				unInvoke();
				if(msg.source().playerStats()!=null) msg.source().playerStats().setLastUpdated(0);
			}
			else
			if(msg.amISource((MOB)affected))
			{
				if(msg.sourceMinor()==CMMsg.TYP_DEATH)
				{
					unInvoke();
				}
			}
		}
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

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,mob,auto),auto?"":"^S<S-NAME> incant(s), feeling <S-HIS-HER> body split in two.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				MOB myMonster = determineMonster(mob);
				Behavior B=CMClass.getBehavior("CombatAbilities");
				myMonster.addBehavior(B);
				B.startBehavior(myMonster);
				if(CMLib.dice().rollPercentage()<50)
				{
					if(CMLib.flags().isGood(mob))
					{
						B=CMClass.getBehavior("MobileGoodGuardian");
						myMonster.addBehavior(B);
						B.startBehavior(myMonster);
						myMonster.copyFactions(mob);
					}
					else
                    if(CMLib.flags().isEvil(mob))
					{
						B=CMClass.getBehavior("MobileAggressive");
						myMonster.addBehavior(B);
						B.startBehavior(myMonster);
						myMonster.copyFactions(mob);
					}
                    else
                    {
                        B=CMClass.getBehavior("Mobile");
                        myMonster.addBehavior(B);
                        B.startBehavior(myMonster);
                        B=CMClass.getBehavior("Guard");
                        myMonster.addBehavior(B);
                        B.startBehavior(myMonster);
						myMonster.copyFactions(mob);
                    }
					myMonster.setVictim(mob);
				}
				else
				{
					B=CMClass.getBehavior("Mobile");
					myMonster.addBehavior(B);
					B.startBehavior(myMonster);
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
			return beneficialWordsFizzle(mob,null,"<S-NAME> attempt(s) to clone <S-HIM-HERSELF>, but fails.");

		// return whether it worked
		return success;
	}
	public MOB determineMonster(MOB caster)
	{
		MOB newMOB=(MOB)caster.copyOf();
		for(int i=0;i<newMOB.inventorySize();i++)
		{
			Item I=newMOB.fetchInventory(i);
			while(I.numEffects()>0)
				I.delEffect(I.fetchEffect(0));
			I.baseEnvStats().setAbility(0);
			if(I instanceof Potion)
				((Potion)I).setSpellList("");
			else
			if(I instanceof Pill)
				((Pill)I).setSpellList("");
			else
			if(I instanceof Wand)
			{
				((Wand)I).setMaxUses(0);
				((Wand)I).setUsesRemaining(0);
			}
			I.recoverEnvStats();
			I.text();
		}
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
		CMLib.beanCounter().clearZeroMoney(newMOB,null);
		newMOB.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,"<S-NAME> appears!");
		caster.location().recoverRoomStats();
		newMOB.setStartRoom(null);
		return(newMOB);
	}
}
