package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_RechargeWand extends Spell
{
	public String ID() { return "Spell_RechargeWand"; }
	public String name(){return "Recharge Wand";}
	protected int canTargetCode(){return CAN_ITEMS;}
	public int overrideMana(){return 100;}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null) return false;

		if(!(target instanceof Wand))
		{
			mob.tell("You can't recharge that.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> incant(s) at <T-NAMESELF> as sweat beads form on <S-HIS-HER> forehead.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if((((Wand)target).usesRemaining()+5) >= ((Wand)target).maxUses()) 
				{
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,"<T-NAME> glow(s) brightly then disintigrates!");
					target.destroy();
				}
                else 
				{
                    boolean willBreak = false;
                    if((((Wand)target).usesRemaining()+10) >= ((Wand)target).maxUses())
                        willBreak = true;
					((Wand)target).setUsesRemaining(((Wand)target).usesRemaining()+5);
                    if(!(willBreak)) 
					{
                        mob.location().show(mob, target, CMMsg.MSG_OK_VISUAL, "<T-NAME> glow(s) brightly!");
                    }
                    else 
					{
                        mob.location().show(mob, target, CMMsg.MSG_OK_VISUAL, "<T-NAME> glow(s) brightly and begins to hum.  It clearly cannot hold more magic.");
                    }
				}
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> incant(s) at <T-NAMESELF>, looking more frustrated every minute.");


		// return whether it worked
		return success;
	}
}
