package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_Goodberry extends Chant
{

	public Chant_Goodberry()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Goodberry";

		canBeUninvoked=true;
		isAutoinvoked=false;
		quality=Ability.INDIFFERENT;

		baseEnvStats().setLevel(2);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Chant_Goodberry();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;

		Environmental owner=target.myOwner();
		if(owner==null) return false;
		
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if((!(target instanceof Food))||(((Food)target).material()!=EnvResource.RESOURCE_BERRIES))
		{
			mob.tell(target.name()+" is not berries.");
			return false;
		}
		
		if(success)
		{
			int numAffected=Dice.roll(1,adjustedLevel(mob)/7,1);
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"<S-NAME> chant(s) to <T-NAMESELF>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,Affect.MSG_OK_ACTION,"<T-NAME> begin to glow!");
				for(int i=0;i<mob.inventorySize();i++)
				{
					Item newTarget=mob.fetchInventory(i);
					if((newTarget!=null)
					&&(newTarget instanceof Food)
					&&(!(newTarget instanceof Pill))
					&&(((Food)newTarget).material()==EnvResource.RESOURCE_BERRIES)
					&&(newTarget.location()==target.location())
					&&(newTarget.name().equals(target.name())))
					{
						Pill newItem=(Pill)CMClass.getItem("GenPill");
						newItem.setName(newTarget.name());
						newItem.setDisplayText(newTarget.displayText());
						newItem.setDescription(newTarget.description());
						newItem.setMaterial(EnvResource.RESOURCE_BERRIES);
						newItem.baseEnvStats().setDisposition(EnvStats.IS_LIGHT);
						newItem.setSpellList(";Prayer_CureLight;");
						newItem.recoverEnvStats();
						newItem.setMiscText(newItem.text());
						Item location=newTarget.location();
						newTarget.destroyThis();
						if(owner instanceof MOB)
							((MOB)owner).addInventory(newItem);
						else
						if(owner instanceof Room)
							((Room)owner).addItem(newItem);
						newItem.setLocation(location);
						if((--numAffected)==0)
							break;
						i=-1;
					}
				}
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}