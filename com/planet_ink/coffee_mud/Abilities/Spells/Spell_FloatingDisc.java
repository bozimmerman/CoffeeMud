package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_FloatingDisc extends Spell
{
	public Spell_FloatingDisc()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Floating Disc";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(5);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_FloatingDisc();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.SPELL_ILLUSION;
	}

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


		if(item.amWearingAt(Item.FLOATING_NEARBY))
		{
			mob.location().show(mob,item,Affect.MSG_OK_VISUAL,"<T-NAME> floating near <S-NAME> now floats back into <S-HIS-HER> hands.");
			item.remove();
		}
		item.recoverEnvStats();
		mob.recoverMaxState();
		mob.recoverCharStats();
		mob.recoverEnvStats();
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Environmental target=getTarget(mob,mob.location(),givenTarget,commands);
		if(target==null) return false;

		if(!mob.isMine(target))
		{
			mob.tell("You don't have that.");
			return false;
		}
		if(mob.amWearingSomethingHere(Item.FLOATING_NEARBY))
		{
			mob.tell("You are already carrying something on a floating disc.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"<T-NAME> begin(s) to float around.":"<S-NAME> invoke(s) a floating disc underneath <T-NAMESELF>.");
			if(mob.location().okAffect(msg))
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