package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_CurseItem extends Prayer
{
	public String ID() { return "Prayer_CurseItem"; }
	public String name(){ return "Curse Item";}
	public String displayText(){ return "(Cursed)";}
	public int quality(){ return MALICIOUS;}
	public long flags(){return Ability.FLAG_UNHOLY|Ability.FLAG_CURSE;}
	protected int canAffectCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS;}
	protected int canTargetCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS;}
	public Environmental newInstance(){	return new Prayer_CurseItem();}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_EVIL);
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			affectableStats.setArmor(affectableStats.armor()+10+mob.envStats().level());
		}
		else
		if(affected instanceof Item)
			affectableStats.setAbility(affectableStats.ability()-1);
	}

	public void unInvoke()
	{


		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
		{
			if(canBeUninvoked())
			if((affected instanceof Item)&&(((Item)affected).owner()!=null)&&(((Item)affected).owner() instanceof MOB))
				((MOB)((Item)affected).owner()).tell("The curse on "+((Item)affected).name()+" is lifted.");
			super.unInvoke();
			return;
		}
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell("The curse is lifted.");
		super.unInvoke();
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if(affected==null) return true;
		if(!(affected instanceof Item)) return true;

		Item item=(Item)affected;

		MOB mob=msg.source();
		if(!msg.amITarget(item))
			return true;
		else
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_REMOVE:
			if(!item.amWearingAt(Item.INVENTORY))
			{
				if(item.amWearingAt(Item.WIELD)||item.amWearingAt(Item.HELD))
				{
					mob.tell("You can't seem to let go of "+item.name()+".");
					return false;
				}
				mob.tell("You can't seem to remove "+item.name()+".");
				return false;
			}
			break;
		case CMMsg.TYP_DROP:
		case CMMsg.TYP_THROW:
			mob.tell("You can't seem to get rid of "+item.name()+".");
			return false;
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB mobTarget=getTarget(mob,commands,givenTarget,true);
		Item target=null;
		if(mobTarget!=null)
			target=Prayer_Curse.getSomething(mobTarget,true);
		if((target==null)&&(mobTarget!=null))
			target=Prayer_Curse.getSomething(mobTarget,false);
		if(target==null)
			target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);

		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> is cursed!":"^S<S-NAME> curse(s) <T-NAMESELF>.^?");
			FullMsg msg2=new FullMsg(mob,mobTarget,this,affectType(auto),null);
			if((mob.location().okMessage(mob,msg))&&((mobTarget==null)||(mob.location().okMessage(mob,msg2))))
			{
				mob.location().send(mob,msg);
				if(mobTarget!=null)
					mob.location().send(mob,msg2);
				if(msg.value()<=0)
				{
					Prayer_Curse.endLowerBlessings(target,CMAble.lowestQualifyingLevel(ID()));
					success=maliciousAffect(mob,target,0,-1);
					target.recoverEnvStats();
					mob.recoverEnvStats();
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to curse <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
