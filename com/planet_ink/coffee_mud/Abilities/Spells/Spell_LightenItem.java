package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_LightenItem extends Spell
{
	public String ID() { return "Spell_LightenItem"; }
	public String name(){return "Lighten Item";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_ITEMS;}
	public Environmental newInstance(){	return new Spell_LightenItem();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setWeight(0);
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		if((affected!=null)&&(affected instanceof Item))
		{
			Item item=(Item)affected;
			if((item.owner()!=null)
			&&(item.owner() instanceof MOB)
			&&(((MOB)item.owner()).isMine(item)))
			{
				MOB mob=(MOB)item.owner();
				mob.tell(item.name()+" grows heavy again.");
				if((mob.envStats().weight()+item.baseEnvStats().weight())>mob.maxCarry())
				{
					if(!item.amWearingAt(Item.INVENTORY))
						ExternalPlay.remove(mob,item,false);
					if(item.amWearingAt(Item.INVENTORY))
						ExternalPlay.drop(mob,item,false);
				}
			}
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null)
		{
			String str=Util.combine(commands,0).toUpperCase();
			if(str.equals("MONEY")||str.equals("GOLD")||str.equals("COINS"))
				mob.tell("You can't cast this spell on your own coins.");
			return false;
		}

		if(target.fetchAffect(this.ID())!=null)
		{
			mob.tell(target.name()+" is already light!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, encanting.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,Affect.MSG_OK_ACTION,"<T-NAME> grow(s) much lighter.");
				beneficialAffect(mob,target,100);
				target.recoverEnvStats();
				mob.recoverEnvStats();
				mob.location().recoverRoomStats();
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, encanting, but nothing happens.");


		// return whether it worked
		return success;
	}
}