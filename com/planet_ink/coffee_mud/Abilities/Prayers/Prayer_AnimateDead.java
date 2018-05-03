package com.planet_ink.coffee_mud.Abilities.Prayers;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2002-2018 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "Prayer_AnimateDead";
	}

	private final static String	localizedName	= CMLib.lang().L("Animate Dead");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER | Ability.DOMAIN_DEATHLORE;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int enchantQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_UNHOLY;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS;
	}

	private final static String	localizedDiplayText	= CMLib.lang().L("Newly animate dead");

	@Override
	public String displayText()
	{
		return localizedDiplayText;
	}
	
	@Override
	public void unInvoke()
	{
		final Physical P=affected;
		super.unInvoke();
		if((P instanceof MOB)&&(this.canBeUninvoked)&&(this.unInvoked))
		{
			if((!P.amDestroyed())&&(((MOB)P).amFollowing()==null))
			{
				final Room R=CMLib.map().roomLocation(P);
				if(!CMLib.law().doesHavePriviledgesHere(invoker(), R))
				{
					if((R!=null)&&(!((MOB)P).amDead()))
						R.showHappens(CMMsg.MSG_OK_ACTION, P,L("<S-NAME> wander(s) off."));
					P.destroy();
				}
			}
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		int tickSet = super.tickDown;
		if(!super.tick(ticking, tickID))
			return false;
		if(ticking instanceof MOB)
		{
			final MOB mob=(MOB)ticking;
			if(mob.amFollowing() != null)
				super.tickDown = tickSet;
		}
		return true;
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Physical target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_UNWORNONLY);
		if(target==null)
			return false;

		if(target==mob)
		{
			mob.tell(L("@x1 doesn't look dead yet.",target.name(mob)));
			return false;
		}
		if(!(target instanceof DeadBody))
		{
			mob.tell(L("You can't animate that."));
			return false;
		}

		final DeadBody body=(DeadBody)target;
		if(body.isPlayerCorpse()||(body.getMobName().length()==0)
		||((body.charStats()!=null)&&(body.charStats().getMyRace()!=null)&&(body.charStats().getMyRace().racialCategory().equalsIgnoreCase("Undead"))))
		{
			mob.tell(L("You can't animate that."));
			return false;
		}
		final String realName=body.getMobName();
		String description=body.getMobDescription();
		if(description.trim().length()==0)
			description="It looks dead.";
		else
			description+="\n\rIt also looks dead.";

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> @x1 for dark powers over <T-NAMESELF>.^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final MOB newMOB=CMClass.getMOB("GenUndead");
				newMOB.setName(L("@x1 zombie",realName));
				newMOB.setDescription(description);
				newMOB.setDisplayText("");
				newMOB.basePhyStats().setLevel(body.phyStats().level()+((super.getX1Level(mob)+super.getXLEVELLevel(mob))/2));
				newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,body.charStats().getStat(CharStats.STAT_GENDER));
				newMOB.baseCharStats().setMyRace(CMClass.getRace("Undead"));
				newMOB.baseCharStats().setBodyPartsFromStringAfterRace(body.charStats().getBodyPartsAsString());
				final Ability P=CMClass.getAbility("Prop_StatTrainer");
				if(P!=null)
				{
					P.setMiscText("NOTEACH STR=20 INT=10 WIS=10 CON=10 DEX=3 CHA=2");
					newMOB.addNonUninvokableEffect(P);
				}
				newMOB.basePhyStats().setSensesMask(PhyStats.CAN_SEE_DARK);
				newMOB.recoverCharStats();
				newMOB.basePhyStats().setAttackAdjustment(CMLib.leveler().getLevelAttack(newMOB));
				newMOB.basePhyStats().setDamage(CMLib.leveler().getLevelMOBDamage(newMOB));
				CMLib.factions().setAlignment(newMOB,Faction.Align.EVIL);
				newMOB.baseState().setHitPoints(15*newMOB.basePhyStats().level());
				newMOB.baseState().setMovement(30);
				newMOB.basePhyStats().setArmor(CMLib.leveler().getLevelMOBArmor(newMOB));
				newMOB.baseState().setMana(0);
				final Behavior B=CMClass.getBehavior("Aggressive");
				if(B!=null)
				{ 
					B.setParms("+NAMES \"-"+mob.Name()+"\" -LEVEL +>"+newMOB.basePhyStats().level()); 
					newMOB.addBehavior(B);
				}
				newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
				newMOB.recoverCharStats();
				newMOB.recoverPhyStats();
				newMOB.recoverMaxState();
				newMOB.resetToMaxState();
				newMOB.text();
				newMOB.bringToLife(mob.location(),true);
				CMLib.beanCounter().clearZeroMoney(newMOB,null);
				newMOB.setMoneyVariation(0);
				//newMOB.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> appears!"));
				int it=0;
				while(it<newMOB.location().numItems())
				{
					final Item item=newMOB.location().getItem(it);
					if((item!=null)&&(item.container()==body))
					{
						final CMMsg msg2=CMClass.getMsg(newMOB,body,item,CMMsg.MSG_GET,null);
						newMOB.location().send(newMOB,msg2);
						final CMMsg msg4=CMClass.getMsg(newMOB,item,null,CMMsg.MSG_GET,null);
						newMOB.location().send(newMOB,msg4);
						final CMMsg msg3=CMClass.getMsg(newMOB,item,null,CMMsg.MSG_WEAR,null);
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
				beneficialAffect(mob,newMOB,0,0);
				mob.location().show(newMOB,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> begin(s) to rise!"));
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> @x1 for dark powers, but fail(s) miserably.",prayWord(mob)));

		// return whether it worked
		return success;
	}
}
