package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_AntTrain extends Chant
{
	public String ID() { return "Chant_AntTrain"; }
	public String name(){return "Ant Train";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_ITEMS;}
	public Environmental newInstance(){	return new Chant_AntTrain();}

	boolean wasntMine=false;

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof Item)))
			return;
		if(invoker==null)
			return;

		MOB mob=(MOB)invoker;
		Item item=(Item)affected;
		super.unInvoke();


		if(canBeUninvoked())
		{
			if(item.amWearingAt(Item.FLOATING_NEARBY))
			{
				if(wasntMine)
					mob.location().show(mob,item,CMMsg.MSG_OK_VISUAL,"<T-NAME> floating near <S-NAME>, is left behind by a departing train of ants.");
				else
					mob.location().show(mob,item,CMMsg.MSG_OK_VISUAL,"<T-NAME> floating near <S-NAME>, is carried back into <S-HIS-HER> hands by a departing train of ants.");
				item.unWear();
			}
			if(wasntMine)
				CommonMsgs.drop(mob,item,true,false);
			wasntMine=false;

			item.recoverEnvStats();
			mob.recoverMaxState();
			mob.recoverCharStats();
			mob.recoverEnvStats();
		}
	}


	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setWeight(0);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Environmental target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;
		if((!(target instanceof Item))
		||(!Sense.isGettable(((Item)target))))
		{
			mob.tell("The ants can't carry "+target.name()+"!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			wasntMine=false;
			if(!mob.isMine(target))
			{
				target.addNonUninvokableEffect(this);
				target.recoverEnvStats();
				wasntMine=true;
				if(target.ID().equals("StdCoins"))
				{
					mob.location().delItem((Item)target);
					mob.addInventory((Item)target);
				}
				else
				if(!CommonMsgs.get(mob,null,(Item)target,true))
				{
					target.delEffect(this);
					target.recoverEnvStats();
					return false;
				}
				target.delEffect(this);
				target.recoverEnvStats();
			}
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> begin(s) to float around.":"^S<S-NAME> chants(s), and a train of ants appears to carry <T-NAMESELF> for <S-HIM-HER>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				long properWornCode=((Item)target).rawProperLocationBitmap();
				boolean properWornLogical=((Item)target).rawLogicalAnd();
				((Item)target).setRawLogicalAnd(false);
				((Item)target).setRawProperLocationBitmap(Item.FLOATING_NEARBY);
				((Item)target).wearAt(Item.FLOATING_NEARBY);
				((Item)target).setRawLogicalAnd(properWornLogical);
				((Item)target).setRawProperLocationBitmap(properWornCode);
				((Item)target).recoverEnvStats();
				beneficialAffect(mob,target,mob.envStats().level()*10);
				mob.recoverEnvStats();
				mob.recoverMaxState();
				mob.recoverCharStats();
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) for ants, but fail(s).");



		// return whether it worked
		return success;
	}
}