package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_FloatingDisc extends Spell
{
	public String ID() { return "Spell_FloatingDisc"; }
	public String name(){return "Floating Disc";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_ITEMS;}
	public Environmental newInstance(){	return new Spell_FloatingDisc();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_EVOCATION;}

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
				mob.location().show(mob,item,CMMsg.MSG_OK_VISUAL,"<T-NAME> floating near <S-NAME> now floats back "+((wasntMine)?"down to the ground":"into <S-HIS-HER> hands."));
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
		||(!((Item)target).isGettable()))
		{
			mob.tell("You cannot float "+target.name()+"!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> begin(s) to float around.":"^S<S-NAME> invoke(s) a floating disc underneath <T-NAMESELF>.^?");
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
				beneficialAffect(mob,target,mob.envStats().level()*30);
				mob.recoverEnvStats();
				mob.recoverMaxState();
				mob.recoverCharStats();
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke a floating disc, but fail(s).");



		// return whether it worked
		return success;
	}
}