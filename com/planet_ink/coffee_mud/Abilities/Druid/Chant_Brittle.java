package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_Brittle extends Chant
{
	public String ID() { return "Chant_Brittle"; }
	public String name(){return "Brittle";}
	protected int canTargetCode(){return CAN_MOBS|CAN_ITEMS;}
	protected int canAffectCode(){return CAN_ITEMS;}
	public int quality(){return Ability.MALICIOUS;}
	public Environmental newInstance(){	return new Chant_Brittle();}
	private int oldCondition=-1;
	private boolean noRecurse=true;
	public void affectEnvStats(Environmental E, EnvStats stats)
	{
		super.affectEnvStats(E,stats);
		if((E instanceof Item)&&(!noRecurse)&&(((Item)E).subjectToWearAndTear()))
		{
			noRecurse=true;
			if(oldCondition==-1) 
				oldCondition=((Item)E).usesRemaining();
			if(((Item)E).usesRemaining()<oldCondition)
			{
				Room R=CoffeeUtensils.roomLocation(E);
				if(R!=null)
					R.showHappens(CMMsg.MSG_OK_ACTION,E.name()+" is destroyed!");
				((Item)E).destroy();
			}
			else
				((Item)E).setUsesRemaining(oldCondition);
			noRecurse=false;
		}
	}
	
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
			if(target==null)
				return maliciousFizzle(mob,mobTarget,"<S-NAME> chant(s) at <T-NAMESELF>, but nothing happens.");
		}

		if(target==null)
			target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);

		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		oldCondition=-1;
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> starts vibrating!":"^S<S-NAME> chant(s), causing <T-NAMESELF> to grow brittle!^?");
			FullMsg msg2=new FullMsg(mob,mobTarget,this,affectType(auto),null);
			if((mob.location().okMessage(mob,msg))&&((mobTarget==null)||(mob.location().okMessage(mob,msg2))))
			{
				mob.location().send(mob,msg);
				if(mobTarget!=null)
					mob.location().send(mob,msg2);
				if((msg.value()<=0)&&(msg2.value()<=0))
				{
					if(target.subjectToWearAndTear())
						oldCondition=target.usesRemaining();
					maliciousAffect(mob,target,0,-1);
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> chant(s), but nothing happens.");


		// return whether it worked
		return success;
	}
}
