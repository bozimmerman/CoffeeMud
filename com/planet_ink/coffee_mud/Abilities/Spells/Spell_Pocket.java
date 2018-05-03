package com.planet_ink.coffee_mud.Abilities.Spells;
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
   Copyright 2014-2018 Bo Zimmerman

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
public class Spell_Pocket extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Pocket";
	}

	private final static String	localizedName	= CMLib.lang().L("Pocket");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS | CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL | Ability.DOMAIN_EVOCATION;
	}

	@Override
	public int overrideMana()
	{
		return 200;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<2)
		{
			mob.tell(L("Pocket who or what, into what?"));
			return false;
		}
		final Vector<String> targetV=new XVector<String>(commands.get(0).toString());
		final Vector<String> containerV=new XVector<String>(commands);
		containerV.remove(0);
		final Physical target=getAnyTarget(mob,targetV,givenTarget,Wearable.FILTER_ANY);
		if(target==null)
			return false;
		final Item containerI=super.getTarget(mob, null, null, containerV, Wearable.FILTER_UNWORNONLY);
		if(containerI==null)
			return false;
		if((!(containerI instanceof Container))||(((Container)containerI).capacity()<=0))
		{
			mob.tell(L("@x1 is not a container.",containerI.name()));
			return false;
		}
		if((target instanceof MOB)&&(((MOB)target).isPlayer()))
		{
			mob.tell(L("You can't pocket @x1.",target.name(mob)));
			return false;
		}
		final List<DeadBody> DBs=CMLib.utensils().getDeadBodies(target);
		for(int v=0;v<DBs.size();v++)
		{
			final DeadBody DB=DBs.get(v);
			if(DB.isPlayerCorpse()
			&&(!DB.getMobName().equals(mob.Name())))
			{
				mob.tell(L("You are not allowed to pocket a player corpse."));
				return false;
			}
		}
		
		if((target instanceof Item) && (!CMLib.utensils().canBePlayerDestroyed(mob,(Item)target,true)))
		{
			mob.tell(L("You can't pocket @x1.",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=false;
		int affectType=CMMsg.MSG_CAST_VERBAL_SPELL;
		if(!(target instanceof Item))
		{
			if(!auto)
				affectType=affectType|CMMsg.MASK_MALICIOUS;
		}
		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+(getXLEVELLevel(mob)/2));
		if(target instanceof MOB)
			levelDiff+=6;
		if(levelDiff<0)
			levelDiff=0;
		success=proficiencyCheck(mob,-(levelDiff*15),auto);

		if(auto)
			affectType=affectType|CMMsg.MASK_ALWAYS;

		if(success)
		{
			final Room R=mob.location();
			final CMMsg msg=CMClass.getMsg(mob,target,this,affectType,L(auto?"":"^S<S-NAME> point(s) at <T-NAMESELF> and evoke(s) a little spell!^?")+CMLib.protocol().msp("spelldam2.wav",40));
			if((R!=null)&&(R.okMessage(mob,msg)))
			{
				R.send(mob,msg);
				if(msg.value()<=0)
				{
					Item pocketItem;
					int level=target.phyStats().level();
					String realName=target.name();
					String name=CMLib.english().cleanArticles(target.name());
					if(target instanceof Item)
					{
						final PackagedItems packageItem=(PackagedItems)CMClass.getItem("GenPackagedVariety");
						final List<Item> items=CMLib.utensils().deepCopyOf((Item)target);
						for(Item I : items)
							packageItem.packageMe(I,1);
						((Item)target).destroy();
						pocketItem=packageItem;
					}
					else
					if(target instanceof MOB)
					{
						final CagedAnimal pocketCage = (CagedAnimal)CMClass.getItem("GenCaged");
						pocketCage.cageMe((MOB)target);
						((MOB) target).killMeDead(false);
						pocketItem=pocketCage;
					}
					else
						return false;
					pocketItem.setName(L("a tiny ceramic @x1",name));
					pocketItem.setDisplayText(L("a tiny ceramic @x1 has been left here",name));
					pocketItem.setDescription(L("It`s so cute!"));
					pocketItem.basePhyStats().setLevel(level);
					pocketItem.basePhyStats().setWeight(1);
					pocketItem.recoverPhyStats();
					pocketItem.setMaterial(RawMaterial.RESOURCE_CLAY);
					mob.addItem(pocketItem);
					pocketItem.setContainer((Container)containerI);
					R.recoverRoomStats();
					R.showHappens(CMMsg.MSG_OK_VISUAL, L("@x1 turns into @x2!",realName,pocketItem.Name()));
				}
			}
		}
		else
			maliciousFizzle(mob,target,L("<S-NAME> point(s) at <T-NAMESELF> and utter(s) a little fizzled spell!"));

		// return whether it worked
		return success;
	}
}
