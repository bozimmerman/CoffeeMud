package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_Goodberry extends Chant
{
	public String ID() { return "Chant_Goodberry"; }
	public String name(){ return "Goodberry";}
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_ITEMS;}
	public Environmental newInstance(){	return new Chant_Goodberry();}

	public boolean checkDo(Item newTarget, Item originaltarget, Environmental owner)
	{
		if((newTarget!=null)
		&&(newTarget instanceof Food)
		&&(!(newTarget instanceof Pill))
		&&(((Food)newTarget).material()==EnvResource.RESOURCE_BERRIES)
		&&(newTarget.container()==originaltarget.container())
		&&(newTarget.name().equals(originaltarget.name())))
		{
			Pill newItem=(Pill)CMClass.getItem("GenPill");
			newItem.setName(newTarget.name());
			newItem.setDisplayText(newTarget.displayText());
			newItem.setDescription(newTarget.description());
			newItem.setMaterial(EnvResource.RESOURCE_BERRIES);
			newItem.baseEnvStats().setDisposition(EnvStats.IS_GLOWING);
			newItem.setSpellList(";Prayer_CureLight;");
			newItem.recoverEnvStats();
			newItem.setMiscText(newItem.text());
			Item location=newTarget.container();
			newTarget.destroyThis();
			if(owner instanceof MOB)
				((MOB)owner).addInventory(newItem);
			else
			if(owner instanceof Room)
				((Room)owner).addItemRefuse(newItem,Item.REFUSE_PLAYER_DROP);
			newItem.setContainer(location);
			return true;
		}
		return false;
	}
	
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;

		Environmental owner=target.owner();
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,Affect.MSG_OK_ACTION,"<T-NAME> begin to glow!");
				if(owner instanceof MOB)
					for(int i=0;i<((MOB)owner).inventorySize();i++)
					{
						Item newTarget=((MOB)owner).fetchInventory(i);
						if((newTarget!=null)&&(checkDo(newTarget,target,owner)))
						{
							if((--numAffected)==0)
								break;
							i=-1;
						}
					}
				if(owner instanceof Room)
					for(int i=0;i<((Room)owner).numItems();i++)
					{
						Item newTarget=((Room)owner).fetchItem(i);
						if((newTarget!=null)&&(checkDo(newTarget,target,owner)))
						{
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