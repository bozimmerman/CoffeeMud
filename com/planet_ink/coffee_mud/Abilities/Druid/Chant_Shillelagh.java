package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_Shillelagh extends Chant
{
	public String ID() { return "Chant_Shillelagh"; }
	public String name(){ return "Shillelagh";}
	public String displayText(){return "(Blessed)";}
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_ITEMS;}
	public Environmental newInstance(){	return new Chant_Shillelagh();}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_BONUS);
		if(affected instanceof Item)
			affectableStats.setAbility(affectableStats.ability()+4);
	}



	public void unInvoke()
	{
		// undo the affects of this spell
		if(canBeUninvoked())
		{
			if(((affected!=null)&&(affected instanceof Item))
			&&((((Item)affected).owner()!=null)
			&&(((Item)affected).owner() instanceof MOB)))
				((MOB)((Item)affected).owner()).tell("The enchantment on "+((Item)affected).displayName()+" fades.");
		}
		super.unInvoke();
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null) return false;

		if(!(target instanceof Weapon))
		{
			mob.tell("You can only enchant weapons.");
			return false;
		}
		if(((((Weapon)target).material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_WOODEN)
		&&((((Weapon)target).material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_VEGETATION))
		{
			mob.tell("You cannot enchant this foreign material.");
			return false;
		}
		if(((Weapon)target).fetchAffect(this.ID())!=null)
		{
			mob.tell(target.displayName()+" is already enchanted.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> appear(s) enchanted!":"^S<S-NAME> chant(s) to <T-NAMESELF>.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
				mob.location().show(mob,target,Affect.MSG_OK_VISUAL,"<T-NAME> glow(s)!");
				target.recoverEnvStats();
				mob.recoverEnvStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but nothing happens.");
		// return whether it worked
		return success;
	}
}