package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_GrowOak extends Chant_SummonPlants
{
	public String ID() { return "Chant_GrowOak"; }
	public String name(){ return "Grow Oak";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Chant_GrowOak();}
	protected int overrideMana(){return Integer.MAX_VALUE;}
	protected int hpRemaining=0;
	protected int lastHp=-1;

	public Item buildMyPlant(MOB mob, Room room)
	{
		int material=EnvResource.RESOURCE_OAK;
		int code=material&EnvResource.RESOURCE_MASK;
		Item newItem=CMClass.getStdItem("GenItem");
		String name=Util.startWithAorAn(EnvResource.RESOURCE_DESCS[code].toLowerCase()+" tree");
		newItem.setName(name);
		newItem.setDisplayText(newItem.name()+" grows here.");
		newItem.setDescription("");
		newItem.baseEnvStats().setWeight(10000);
		newItem.setGettable(false);
		newItem.setMaterial(material);
		newItem.setSecretIdentity(mob.Name());
		newItem.setMiscText(newItem.text());
		room.addItem(newItem);
		newItem.setDispossessionTime(0);
		room.showHappens(CMMsg.MSG_OK_ACTION,"a tall, healthy "+EnvResource.RESOURCE_DESCS[code].toLowerCase()+" tree sprouts up.");
		room.recoverEnvStats();
		Chant_GrowOak newChant=new Chant_GrowOak();
		newChant.PlantsLocation=room;
		newChant.hpRemaining=100*mob.envStats().level();
		newChant.littlePlants=newItem;
		newChant.beneficialAffect(mob,newItem,(newChant.adjustedLevel(mob)*240)+450);
		room.recoverEnvStats();
		return newItem;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if((PlantsLocation==null)||(littlePlants==null)) return false;
		if(invoker!=null)
		{
			if((lastHp>invoker.curState().getHitPoints())&&(lastHp>0))
			{
				int dmg=lastHp-invoker.curState().getHitPoints();
				if(invoker.location()!=PlantsLocation)
					dmg=dmg/2;
				if(dmg>0)
				{
					if(ExternalPlay.postHealing(invoker,invoker,this,CMMsg.MASK_GENERAL|CMMsg.TYP_CAST_SPELL,dmg,null))
						invoker.tell("Your oak absorbs "+dmg+" points of your damage!");
				}
				hpRemaining-=dmg;
				if(hpRemaining<0)
					unInvoke();
			}
			lastHp=invoker.curState().getHitPoints();
		}
		for(int i=0;i<PlantsLocation.numInhabitants();i++)
		{
			MOB M=PlantsLocation.fetchInhabitant(i);
			if(M.fetchEffect("Chopping")!=null)
			{
				int dmg=Dice.roll(1,50,50);
				hpRemaining-=dmg;
				if(invoker!=null) invoker.tell("Your oak is being chopped down!");
				ExternalPlay.postDamage(invoker,invoker,null,dmg/2,CMMsg.MSG_OK_ACTION,Weapon.TYPE_SLASHING,"The chopping on your oak <DAMAGE> you!");
				if(hpRemaining<0)
				{
					if(invoker!=null)
						ExternalPlay.postDeath(invoker,null,null);
					unInvoke();
				}
			}
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
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
		if(super.invoke(mob,commands,givenTarget,auto))
			return true;
		return false;
	}
}