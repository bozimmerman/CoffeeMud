package com.planet_ink.coffee_mud.Abilities.Prayers;

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

public class Prayer_AnimateDead extends Prayer
{
	public String ID() { return "Prayer_AnimateDead"; }
	public String name(){ return "Animate Dead";}
	public int quality(){ return INDIFFERENT;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	protected int canTargetCode(){return CAN_ITEMS;}

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
		String realName=body.mobName();
		String description=body.mobDescription();
		if(description.trim().length()==0)
			description="It looks dead.";
		else
			description+="\n\rIt also looks dead.";

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" for dark powers over <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				MOB newMOB=CMClass.getMOB("GenUndead");
				newMOB.setName(realName+" zombie");
				newMOB.setDescription(description);
				newMOB.setDisplayText("");
				newMOB.baseEnvStats().setLevel(body.envStats().level());
				newMOB.baseCharStats().setStat(CharStats.GENDER,body.charStats().getStat(CharStats.GENDER));
				newMOB.baseCharStats().setMyRace(CMClass.getRace("Undead"));
				newMOB.baseCharStats().setBodyPartStrAfterRace(body.charStats().getBodyPartStr());
				Ability P=CMClass.getAbility("Prop_StatTrainer");
				if(P!=null)
				{
					P.setMiscText("NOTEACH STR=20 INT=10 WIS=10 CON=10 DEX=3 CHA=2");
					newMOB.addNonUninvokableEffect(P);
				}
				newMOB.baseEnvStats().setSensesMask(EnvStats.CAN_SEE_DARK);
				newMOB.recoverCharStats();
				newMOB.baseEnvStats().setAttackAdjustment(newMOB.baseCharStats().getCurrentClass().getLevelAttack(newMOB));
				newMOB.baseEnvStats().setDamage(newMOB.baseCharStats().getCurrentClass().getLevelDamage(newMOB));
				Factions.setAlignment(newMOB,Faction.ALIGN_EVIL);
				newMOB.baseState().setHitPoints(15*newMOB.baseEnvStats().level());
				newMOB.baseState().setMovement(30);
				newMOB.baseEnvStats().setArmor(newMOB.baseCharStats().getCurrentClass().getLevelArmor(newMOB));
				newMOB.baseState().setMana(0);
				Behavior B=CMClass.getBehavior("Aggressive");
				if(B!=null){ B.setParms("+NAMES \"-"+mob.Name()+"\""); newMOB.addBehavior(B);}
				newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
				newMOB.recoverCharStats();
				newMOB.recoverEnvStats();
				newMOB.recoverMaxState();
				newMOB.resetToMaxState();
				newMOB.text();
				newMOB.bringToLife(mob.location(),true);
				BeanCounter.clearZeroMoney(newMOB,null);
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
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for dark powers, but fail(s) miserably.");


		// return whether it worked
		return success;
	}
}
