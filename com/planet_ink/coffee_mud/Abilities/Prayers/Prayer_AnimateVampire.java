package com.planet_ink.coffee_mud.Abilities.Prayers;
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
public class Prayer_AnimateVampire extends Prayer
{
	public String ID() { return "Prayer_AnimateVampire"; }
	public String name(){ return "Animate Vampire";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_DEATHLORE;}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	public int enchantQuality(){return Ability.QUALITY_INDIFFERENT;}
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
		Environmental target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_UNWORNONLY);
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

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> "+prayForWord(mob)+" to animate <T-NAMESELF> as a vampire.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				MOB newMOB=CMClass.getMOB("GenUndead");
				newMOB.setName("a vampire");
				newMOB.setDescription(description);
				newMOB.setDisplayText("a vampire is here");
				newMOB.baseEnvStats().setLevel(19+(super.getX1Level(mob)*2)+super.getXLEVELLevel(mob));
				newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,body.charStats().getStat(CharStats.STAT_GENDER));
				newMOB.baseCharStats().setMyRace(CMClass.getRace("Undead"));
                newMOB.baseCharStats().setBodyPartsFromStringAfterRace(body.charStats().getBodyPartsAsString());
				Ability P=CMClass.getAbility("Prop_StatTrainer");
				if(P!=null)
				{
					P.setMiscText("NOTEACH STR=22 INT=15 WIS=15 CON=10 DEX=22 CHA=20");
					newMOB.addNonUninvokableEffect(P);
				}
				newMOB.baseEnvStats().setDisposition(EnvStats.IS_FLYING);
				newMOB.baseEnvStats().setSensesMask(EnvStats.CAN_SEE_DARK|EnvStats.CAN_SEE_INVISIBLE);
				newMOB.recoverCharStats();
				newMOB.baseEnvStats().setAttackAdjustment(CMLib.leveler().getLevelAttack(newMOB));
				newMOB.baseEnvStats().setDamage(CMLib.leveler().getLevelMOBDamage(newMOB));
				CMLib.factions().setAlignment(newMOB,Faction.ALIGN_EVIL);
				newMOB.baseState().setHitPoints(30*newMOB.baseEnvStats().level());
				newMOB.baseState().setMovement(CMLib.leveler().getLevelMove(newMOB));
				newMOB.baseEnvStats().setArmor(CMLib.leveler().getLevelMOBArmor(newMOB));
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
				CMLib.beanCounter().clearZeroMoney(newMOB,null);
				newMOB.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,"<S-NAME> appears!");
				int it=0;
				while(it<newMOB.location().numItems())
				{
					Item item=newMOB.location().fetchItem(it);
					if((item!=null)&&(item.container()==body))
					{
						CMMsg msg2=CMClass.getMsg(newMOB,body,item,CMMsg.MSG_GET,null);
						newMOB.location().send(newMOB,msg2);
						CMMsg msg4=CMClass.getMsg(newMOB,item,null,CMMsg.MSG_GET,null);
						newMOB.location().send(newMOB,msg4);
						CMMsg msg3=CMClass.getMsg(newMOB,item,null,CMMsg.MSG_WEAR,null);
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
				newMOB.setStartRoom(null);
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
