package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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

public class Prayer_AnimateVampire extends Prayer
{
	public String ID() { return "Prayer_AnimateVampire"; }
	public String name(){ return "Animate Vampire";}
	public int quality(){ return INDIFFERENT;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	protected int canTargetCode(){return CAN_ITEMS;}


	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if(msg.amISource(mob)
			&&(msg.sourceMinor()==CMMsg.TYP_DEATH))
			{
				Ability A=CMClass.getAbility("Disease_Vampirism");
				if((A!=null)&&(mob.fetchEffect(A.ID())==null))
					A.invoke(mob,mob,true,0);
			}
		}
		super.executeMsg(myHost,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Environmental target=getAnyTarget(mob,commands,givenTarget,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;

		if(target==mob)
		{
			mob.tell(target.name()+" doesn't look dead yet.");
			return false;
		}
		if(!(target instanceof DeadBody))
		{
			mob.tell("You can't animate that.");
			return false;
		}

		DeadBody body=(DeadBody)target;
		if(body.playerCorpse()||(body.mobName().length()==0))
		{
			mob.tell("You can't animate that.");
			return false;
		}
		String description=body.mobDescription();
		if(description.trim().length()==0)
			description="It looks dead.";
		else
			description+="\n\rIt also looks dead.";

		if(body.baseEnvStats().level()<25)
		{
			mob.tell("This creature is too weak to create a vampire from.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> "+prayForWord(mob)+" to animate <T-NAMESELF> as a vampire.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				MOB newMOB=(MOB)CMClass.getMOB("GenUndead");
				newMOB.setName("a vampire");
				newMOB.setDescription(description);
				newMOB.setDisplayText("a vampire is here");
				newMOB.baseEnvStats().setLevel(19);
				newMOB.baseCharStats().setStat(CharStats.GENDER,body.charStats().getStat(CharStats.GENDER));
				newMOB.baseCharStats().setMyRace(CMClass.getRace("Undead"));
				newMOB.baseCharStats().setBodyPartStrAfterRace(body.charStats().getBodyPartStr());
				Ability P=CMClass.getAbility("Prop_StatTrainer");
				if(P!=null)
				{
					P.setMiscText("NOTEACH STR=22 INT=15 WIS=15 CON=10 DEX=22 CHA=20");
					newMOB.addNonUninvokableEffect(P);
				}
				newMOB.baseEnvStats().setDisposition(EnvStats.IS_FLYING);
				newMOB.baseEnvStats().setSensesMask(EnvStats.CAN_SEE_DARK|EnvStats.CAN_SEE_INVISIBLE);
				newMOB.recoverCharStats();
				newMOB.baseEnvStats().setAttackAdjustment(newMOB.baseCharStats().getCurrentClass().getLevelAttack(newMOB));
				newMOB.baseEnvStats().setDamage(newMOB.baseCharStats().getCurrentClass().getLevelDamage(newMOB));
				newMOB.setAlignment(0);
				newMOB.baseState().setHitPoints(30*newMOB.baseEnvStats().level());
				newMOB.baseState().setMovement(newMOB.baseCharStats().getCurrentClass().getLevelMove(newMOB));
				newMOB.baseEnvStats().setArmor(newMOB.baseCharStats().getCurrentClass().getLevelArmor(newMOB));
				newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
				newMOB.baseState().setMana(100);
				Behavior B=CMClass.getBehavior("Aggressive");
				if(B!=null){ B.setParms("+NAMES \"-"+mob.Name()+"\""); newMOB.addBehavior(B);}
				newMOB.recoverCharStats();
				newMOB.recoverEnvStats();
				newMOB.recoverMaxState();
				newMOB.resetToMaxState();
				Ability A=CMClass.getAbility("Immunities");
				if(A!=null){
					A.setMiscText("all");
					newMOB.addNonUninvokableEffect(A);
				}
				newMOB.addAbility(CMClass.getAbility("Undead_EnergyDrain"));
				B=CMClass.getBehavior("CombatAbilities");
				newMOB.addBehavior(B);
				newMOB.text();
				newMOB.bringToLife(mob.location(),true);
				newMOB.setMoney(0);
				newMOB.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,"<S-NAME> appears!");
				int it=0;
				while(it<newMOB.location().numItems())
				{
					Item item=newMOB.location().fetchItem(it);
					if((item!=null)&&(item.container()==body))
					{
						FullMsg msg2=new FullMsg(newMOB,body,item,CMMsg.MSG_GET,null);
						newMOB.location().send(newMOB,msg2);
						FullMsg msg4=new FullMsg(newMOB,item,null,CMMsg.MSG_GET,null);
						newMOB.location().send(newMOB,msg4);
						FullMsg msg3=new FullMsg(newMOB,item,null,CMMsg.MSG_WEAR,null);
						newMOB.location().send(newMOB,msg3);
						if(!newMOB.isMine(item))
							it++;
						else
							it=0;
					}
					else
						it++;
				}
				body.destroy();
				mob.location().show(newMOB,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> begin(s) to rise!");
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayForWord(mob)+" to animate <T-NAMESELF>, but fail(s) miserably.");

		// return whether it worked
		return success;
	}
}
