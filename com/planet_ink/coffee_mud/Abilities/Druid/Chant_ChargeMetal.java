package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_ChargeMetal extends Chant
{
	public String ID() { return "Chant_ChargeMetal"; }
	public String name(){return "Charge Metal";}
	public String displayText(){return "(Charged)";}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_ITEMS|CAN_MOBS;}
	public Environmental newInstance(){	return new Chant_ChargeMetal();}

	private Vector affectedItems=new Vector();

	public Item wieldingMetal(MOB mob)
	{
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item item=mob.fetchInventory(i);
			if((item!=null)
			&&(!item.amWearingAt(Item.INVENTORY))
			&&((item.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_METAL)
			&&(item.container()==null)
			&&(!mob.amDead()))
				return item;
		}
		return null;
	}

	public boolean okAffect(Environmental myHost, Affect msg)
	{
		if(!super.okAffect(myHost,msg))
			return false;
		if(affected==null) return true;
		if(!(affected instanceof Item)) return true;
		Item I=(Item)affected;
		if((I.owner()==null)
		||(!(I.owner() instanceof MOB))
		||(I.amWearingAt(Item.INVENTORY)))
			return true;
		MOB mob=(MOB)I.owner();
		if((!msg.amITarget(mob))
		&&(msg.targetMinor()==Affect.TYP_ELECTRIC))
		{
			((MOB)affected).location().show((MOB)mob,null,I,Affect.MSG_OK_VISUAL,"<O-NAME> attracts a charge to <S-NAME>!");
			msg.modify(msg.source(),
					   mob,
					   msg.tool(),
					   msg.sourceCode(),
					   msg.sourceMessage(),
					   msg.targetCode(),
					   msg.targetMessage(),
					   msg.othersCode(),
					   msg.othersMessage());
		}
		return true;
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
		{
			super.unInvoke();
			return;
		}

		if(canBeUninvoked())
		if(affected instanceof MOB)
		{
			for(int i=0;i<affectedItems.size();i++)
			{
				Item I=(Item)affectedItems.elementAt(i);
				Ability A=I.fetchAffect(this.ID());
				while(A!=null)
				{
					I.delAffect(A);
					A=I.fetchAffect(this.ID());
				}

			}
		}
		super.unInvoke();
	}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Environmental target=this.getAnyTarget(mob,commands,givenTarget,Item.WORN_REQ_ANY);
		if(target==null) return false;
		Item I=null;
		if(target instanceof MOB) I=wieldingMetal((MOB)target);

		if((target instanceof Item)
		&&((((Item)target).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_METAL))
			I=(Item)target;
		else
		if(target instanceof Item)
		{
			mob.tell(target.name()+" is not made of metal!");
			return false;
		}
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if((success)&&(I!=null))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),"^S<S-NAME> chant(s) upon <T-NAMESELF>.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
					success=maliciousAffect(mob,I,0,-1);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) at <T-NAMESELF>, but nothing happens.");

		// return whether it worked
		return success;
	}
}