package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_GrowClub extends Chant
{
	public String ID() { return "Chant_GrowClub"; }
	public String name(){ return "Grow Club";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Chant_GrowClub();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((mob.location().domainType()!=Room.DOMAIN_OUTDOORS_WOODS)
		&&((mob.location().myResource()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_WOODEN)
		&&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_JUNGLE))
		{
			mob.tell("This magic will not work here.");
			return false;
		}
		int material=EnvResource.RESOURCE_OAK;
		if((mob.location().myResource()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_WOODEN)
			material=mob.location().myResource();
		else
		{
			Vector V=mob.location().resourceChoices();
			Vector V2=new Vector();
			for(int v=0;v<V.size();v++)
			{
				if((((Integer)V.elementAt(v)).intValue()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_WOODEN)
					V2.addElement(V.elementAt(v));
			}
			if(V2.size()>0)
				material=((Integer)V2.elementAt(Dice.roll(1,V2.size(),-1))).intValue();
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to the trees.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				Weapon newItem=(Weapon)CMClass.getWeapon("GenWeapon");
				newItem.setName(EnvResource.RESOURCE_DESCS[material&EnvResource.RESOURCE_MASK].toLowerCase()+" club");
				newItem.setName(Util.startWithAorAn(newItem.name()));
				newItem.setDisplayText(newItem.name()+" sits here");
				newItem.setDescription("It looks like the limb of a tree.");
				newItem.setMaterial(material);
				newItem.baseEnvStats().setWeight(10);
				newItem.baseEnvStats().setAttackAdjustment(0);
				newItem.baseEnvStats().setDamage(6);
				newItem.recoverEnvStats();
				newItem.setBaseValue(0);
				newItem.setWeaponClassification(Weapon.CLASS_BLUNT);
				newItem.setWeaponType(Weapon.TYPE_BASHING);
				newItem.setMiscText(newItem.text());
				mob.location().addItemRefuse(newItem,Item.REFUSE_RESOURCE);
				mob.location().showHappens(Affect.MSG_OK_ACTION,"A good looking club grows out of a tree and drops.");
				mob.location().recoverEnvStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) to the trees, but nothing happens.");

		// return whether it worked
		return success;
	}
}
