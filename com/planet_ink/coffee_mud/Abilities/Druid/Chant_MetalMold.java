package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_MetalMold extends Chant
{
	public String ID() { return "Chant_MetalMold"; }
	public String name(){return "Metal Mold";}
	protected int canTargetCode(){return CAN_MOBS|CAN_ITEMS;}
	public Environmental newInstance(){	return new Chant_MetalMold();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB mobTarget=getTarget(mob,commands,givenTarget,true);
		Item target=null;
		if(mobTarget!=null)
		{
			Vector goodPossibilities=new Vector();
			Vector possibilities=new Vector();
			for(int i=0;i<mobTarget.inventorySize();i++)
			{
				Item item=mobTarget.fetchInventory(i);
				if((item!=null)
				   &&((item.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_METAL)
				   &&(item.subjectToWearAndTear()))
				{
					if(item.amWearingAt(Item.INVENTORY))
						possibilities.addElement(item);
					else
						goodPossibilities.addElement(item);
				}
				if(goodPossibilities.size()>0)
					target=(Item)goodPossibilities.elementAt(Dice.roll(1,goodPossibilities.size(),-1));
				else
				if(possibilities.size()>0)
					target=(Item)possibilities.elementAt(Dice.roll(1,possibilities.size(),-1));
			}
		}

		if(target==null)
			target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);

		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> grow(s) moldy!":"^S<S-NAME> chant(s), causing <T-NAMESELF> to get eaten by mold.^?");
			FullMsg msg2=new FullMsg(mob,mobTarget,this,affectType(auto),null);
			if((mob.location().okAffect(mob,msg))&&((mobTarget==null)||(mob.location().okAffect(mob,msg2))))
			{
				mob.location().send(mob,msg);
				if(mobTarget!=null)
					mob.location().send(mob,msg2);
				if(!msg.wasModified())
				{
					int damage=2;
					for(int i=0;i<(mob.envStats().level()/2);i++)
						damage+=Dice.roll(1,2,2);
					if(Sense.isABonusItems(target))
						damage=(int)Math.round(Util.div(damage,2.0));
					if(target.envStats().ability()>0)
						damage=(int)Math.round(Util.div(damage,1+target.envStats().ability()));
					target.setUsesRemaining(target.usesRemaining()-damage);
					if(target.usesRemaining()>0)
						target.recoverEnvStats();
					else
					{
						target.setUsesRemaining(100);
						if(mobTarget==null)
							mob.location().show(mob,target,Affect.MSG_OK_VISUAL,"<T-NAME> is destroyed by mold!");
						else
							mob.location().show(mobTarget,target,Affect.MSG_OK_VISUAL,"<T-NAME>, possessed by <S-NAME>, is destroyed by mold!");
						target.unWear();
						target.destroyThis();
						mob.location().recoverRoomStats();
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> chant(s) for mold, but nothing happens.");


		// return whether it worked
		return success;
	}
}
