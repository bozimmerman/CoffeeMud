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
public class Chant_GrowOak extends Chant_SummonPlants
{
	public String ID() { return "Chant_GrowOak"; }
	public String name(){ return "Grow Oak";}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_PLANTGROWTH;}
    public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	protected int overrideMana(){return Integer.MAX_VALUE;}
	protected int hpRemaining=0;
	protected int lastHp=-1;

	protected Item buildMyPlant(MOB mob, Room room)
	{
		int material=RawMaterial.RESOURCE_OAK;
		int code=material&RawMaterial.RESOURCE_MASK;
		Item newItem=CMClass.getBasicItem("GenItem");
		String name=CMLib.english().startWithAorAn(RawMaterial.CODES.NAME(code).toLowerCase()+" tree");
		newItem.setName(name);
		newItem.setDisplayText(newItem.name()+" grows here.");
		newItem.setDescription("");
		Chant_GrowOak newChant=new Chant_GrowOak();
		newItem.baseEnvStats().setLevel(10+newChant.getX1Level(mob));
		newItem.baseEnvStats().setWeight(10000);
		CMLib.flags().setGettable(newItem,false);
		newItem.setMaterial(material);
		newItem.setSecretIdentity(mob.Name());
		newItem.setMiscText(newItem.text());
		room.addItem(newItem);
		newItem.setExpirationDate(0);
		room.showHappens(CMMsg.MSG_OK_ACTION,"a tall, healthy "+RawMaterial.CODES.NAME(code).toLowerCase()+" tree sprouts up.");
		room.recoverEnvStats();
		newChant.PlantsLocation=room;
		newChant.hpRemaining=100*(mob.envStats().level()+(2*newChant.getXLEVELLevel(mob))+(10*newChant.getX1Level(mob)));
		newChant.littlePlants=newItem;
		newChant.beneficialAffect(mob,newItem,0,(newChant.adjustedLevel(mob,0)*240)+450);
		room.recoverEnvStats();
		return newItem;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		Room plantsLocation = PlantsLocation;
		MOB invoker = invoker();
		if((plantsLocation==null)||(littlePlants==null)) return false;
		if(invoker!=null)
		{
			if((lastHp>invoker.curState().getHitPoints())&&(lastHp>0))
			{
				int dmg=lastHp-invoker.curState().getHitPoints();
				if(invoker.location()!=plantsLocation)
					dmg=dmg/2;
				if(dmg>0)
				{
					if(CMLib.combat().postHealing(invoker,invoker,this,CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,dmg,null))
						invoker.tell("Your oak absorbs "+dmg+" points of your damage!");
				}
				hpRemaining-=dmg;
				if(hpRemaining<0)
					unInvoke();
			}
			lastHp=invoker.curState().getHitPoints();
		}
		for(int i=0;i<plantsLocation.numInhabitants();i++)
		{
			MOB M=plantsLocation.fetchInhabitant(i);
			if(M.fetchEffect("Chopping")!=null)
			{
				int dmg=CMLib.dice().roll(1,50,50);
				hpRemaining-=dmg;
				if(invoker!=null) invoker.tell("Your oak is being chopped down!");
				CMLib.combat().postDamage(invoker,invoker,null,dmg/2,CMMsg.MASK_ALWAYS|CMMsg.TYP_UNDEAD,Weapon.TYPE_SLASHING,"The chopping on your oak <DAMAGE> you!");
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

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Vector V=Druid_MyPlants.myPlantRooms(mob);
		for(int v=0;v<V.size();v++)
		{
			Room R=(Room)V.elementAt(v);
			for(int i=0;i<R.numItems();i++)
			{
				Item I=R.fetchItem(i);
				if((I!=null)
				   &&(I.secretIdentity().equals(mob.Name()))
				   &&(I.fetchEffect(ID())!=null))
				{
					mob.tell("Each druid is allowed but one oak at a time.");
					return false;
				}
			}
		}
		if(super.invoke(mob,commands,givenTarget,auto,asLevel))
			return true;
		return false;
	}
}
