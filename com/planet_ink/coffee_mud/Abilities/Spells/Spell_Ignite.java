package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Ignite extends Spell
{
	public String ID() { return "Spell_Ignite"; }
	public String name(){return "Ignite";}
	public String displayText(){return "Ignite";}
	public int quality(){return MALICIOUS;};
	protected int canTargetCode(){return CAN_ITEMS|CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_Ignite();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_EVOCATION;}

	public void ignite(MOB mob, Item I)
	{
		int durationOfBurn=5;
		switch(I.material()&EnvResource.MATERIAL_MASK)
		{
		case EnvResource.MATERIAL_LEATHER:
			durationOfBurn=20+I.envStats().weight();
			break;
		case EnvResource.MATERIAL_CLOTH:
		case EnvResource.MATERIAL_PAPER:
			durationOfBurn=5+I.envStats().weight();
			break;
		case EnvResource.MATERIAL_WOODEN:
			durationOfBurn=40+(I.envStats().weight()*2);
			break;
		default:
			return;
		}
		mob.location().showHappens(Affect.MSG_OK_VISUAL,I.name()+" ignites!");
		Ability B=CMClass.getAbility("Burning");
		B.setProfficiency(durationOfBurn);
		B.invoke(mob,I,true);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Environmental target=getAnyTarget(mob,commands,givenTarget,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;
		if((!(target instanceof MOB))
		&&(!(target instanceof Item)))
		{
			mob.tell("You can't ignite '"+target.name()+"'!");
			return false;
		}


		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),"^S<S-NAME> evoke(s) a spell upon <T-NAMESELF>.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					if(target instanceof Item)
						ignite(mob,(Item)target);
					else
					if(target instanceof MOB)
					{
						MOB mob2=(MOB)target;
						for(int i=0;i<mob2.inventorySize();i++)
						{
							Item I=mob2.fetchInventory(i);
							if((I!=null)&&(I.container()==null))
								ignite(mob2,I);
						}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> evoke(s) at <T-NAMESELF>, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}