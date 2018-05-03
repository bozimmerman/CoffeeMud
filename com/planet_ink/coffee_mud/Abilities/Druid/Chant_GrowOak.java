package com.planet_ink.coffee_mud.Abilities.Druid;
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
   Copyright 2003-2018 Bo Zimmerman

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

public class Chant_GrowOak extends Chant_SummonPlants
{
	@Override
	public String ID()
	{
		return "Chant_GrowOak";
	}

	private final static String localizedName = CMLib.lang().L("Grow Oak");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_PLANTGROWTH;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	protected int overrideMana()
	{
		return Ability.COST_ALL;
	}

	protected int	hpRemaining	= 0;
	protected int	lastHp		= -1;

	@Override
	protected Item buildMyPlant(MOB mob, Room room)
	{
		final int material=RawMaterial.RESOURCE_OAK;
		final int code=material&RawMaterial.RESOURCE_MASK;
		final Item newItem=CMClass.getBasicItem("GenItem");
		final String name=CMLib.english().startWithAorAn(RawMaterial.CODES.NAME(code).toLowerCase()+" tree");
		newItem.setName(name);
		newItem.setDisplayText(L("@x1 grows here.",newItem.name()));
		newItem.setDescription("");
		final Chant_GrowOak newChant=new Chant_GrowOak();
		newItem.basePhyStats().setDisposition(newItem.basePhyStats().disposition()|PhyStats.IS_BONUS);
		newItem.basePhyStats().setLevel(10+newChant.getX1Level(mob));
		newItem.basePhyStats().setWeight(10000);
		CMLib.flags().setGettable(newItem,false);
		newItem.setMaterial(material);
		newItem.setSecretIdentity(mob.Name());
		newItem.setMiscText(newItem.text());
		room.addItem(newItem);
		Druid_MyPlants.addNewPlant(mob, newItem);
		newItem.setExpirationDate(0);
		room.showHappens(CMMsg.MSG_OK_ACTION,L("a tall, healthy @x1 tree sprouts up.",RawMaterial.CODES.NAME(code).toLowerCase()));
		room.recoverPhyStats();
		newChant.plantsLocationR=room;
		newChant.hpRemaining=100*(mob.phyStats().level()+(2*newChant.getXLEVELLevel(mob))+(10*newChant.getX1Level(mob)));
		newChant.littlePlantsI=newItem;
		newChant.beneficialAffect(mob,newItem,0,(newChant.adjustedLevel(mob,0)*240)+450);
		room.recoverPhyStats();
		return newItem;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		final Room plantsLocation = plantsLocationR;
		final MOB invoker = invoker();
		if((plantsLocation==null)||(littlePlantsI==null))
			return false;
		if(invoker!=null)
		{
			if((lastHp>invoker.curState().getHitPoints())&&(lastHp>0))
			{
				int dmg=lastHp-invoker.curState().getHitPoints();
				if(invoker.location()!=plantsLocation)
					dmg=dmg/2;
				if(dmg>0)
				{
					if(CMLib.combat().postHealing(invoker,invoker,this,dmg,CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,null))
						invoker.tell(L("Your oak absorbs @x1 points of your damage!",""+dmg));
				}
				hpRemaining-=dmg;
				if(hpRemaining<0)
					unInvoke();
			}
			lastHp=invoker.curState().getHitPoints();
		}
		for(int i=0;i<plantsLocation.numInhabitants();i++)
		{
			final MOB M=plantsLocation.fetchInhabitant(i);
			if(M.fetchEffect("Chopping")!=null)
			{
				final int dmg=CMLib.dice().roll(1,50,50);
				hpRemaining-=dmg;
				if(invoker!=null)
					invoker.tell(L("Your oak is being chopped down!"));
				CMLib.combat().postDamage(invoker,invoker,null,dmg/2,CMMsg.MASK_ALWAYS|CMMsg.TYP_UNDEAD,Weapon.TYPE_SLASHING,L("The chopping on <S-YOUPOSS> oak <DAMAGE> <S-NAME>!"));
				if(hpRemaining<0)
				{
					if(invoker!=null)
						CMLib.combat().postDeath(invoker,null,null);
					unInvoke();
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final List<Room> V=Druid_MyPlants.myPlantRooms(mob);
		for(int v=0;v<V.size();v++)
		{
			final Room R=V.get(v);
			for(int i=0;i<R.numItems();i++)
			{
				final Item I=R.getItem(i);
				if((I!=null)
				   &&(I.secretIdentity().equals(mob.Name()))
				   &&(I.fetchEffect(ID())!=null))
				{
					mob.tell(L("Each druid is allowed but one oak at a time."));
					return false;
				}
			}
		}
		if(super.invoke(mob,commands,givenTarget,auto,asLevel))
			return true;
		return false;
	}
}
