package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_SummonTree extends Chant_SummonPlants
{
	public String ID() { return "Chant_SummonTree"; }
	public String name(){ return "Summon Tree";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Chant_SummonTree();}
	private int material=0;
	private int oldMaterial=-1;

	public Item buildMyPlant(MOB mob, Room room)
	{
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
		room.showHappens(Affect.MSG_OK_ACTION,"a tall, healthy "+EnvResource.RESOURCE_DESCS[code].toLowerCase()+" tree sprouts up.");
		room.recoverEnvStats();
		Chant_SummonTree newChant=new Chant_SummonTree();
		newChant.PlantsLocation=room;
		newChant.littlePlants=newItem;
		newChant.beneficialAffect(mob,newItem,(newChant.adjustedLevel(mob)*240)+450);
		room.recoverEnvStats();
		return newItem;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if((PlantsLocation==null)||(littlePlants==null)) return false;
		if(PlantsLocation.myResource()!=littlePlants.material())
		{
			oldMaterial=PlantsLocation.myResource();
			PlantsLocation.setResource(littlePlants.material());
		}
		for(int i=0;i<PlantsLocation.numInhabitants();i++)
		{
			MOB M=PlantsLocation.fetchInhabitant(i);
			if(M.fetchAffect("Chopping")!=null)
			{
				unInvoke();
				break;
			}
		}
		return true;
	}

	public void unInvoke()
	{
		if((canBeUninvoked())&&(PlantsLocation!=null)&&(oldMaterial>=0))
			PlantsLocation.setResource(oldMaterial);
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		material=EnvResource.RESOURCE_OAK;
		if((mob.location().myResource()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_WOODEN)
			material=mob.location().myResource();
		else
		{
			Vector V=mob.location().resourceChoices();
			Vector V2=new Vector();
			if(V!=null)
			for(int v=0;v<V.size();v++)
			{
				if(((((Integer)V.elementAt(v)).intValue()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_WOODEN)
				&&((((Integer)V.elementAt(v)).intValue())!=EnvResource.RESOURCE_WOOD))
					V2.addElement(V.elementAt(v));
			}
			if(V2.size()>0)
				material=((Integer)V2.elementAt(Dice.roll(1,V2.size(),-1))).intValue();
		}

		return super.invoke(mob,commands,givenTarget,auto);
	}
}
