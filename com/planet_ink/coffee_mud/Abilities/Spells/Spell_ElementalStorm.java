package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_ElementalStorm extends Spell
{
	public String ID() { return "Spell_ElementalStorm"; }
	public String name(){return "Elemental Storm";}
	public String displayText(){return "";}
	public int maxRange(){return 1;}
	public int quality(){return MALICIOUS;};
	public Environmental newInstance(){return new Spell_ElementalStorm();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_EVOCATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		int[] types={CMMsg.TYP_FIRE,
					 CMMsg.TYP_COLD,
					 CMMsg.TYP_ACID,
					 CMMsg.TYP_WATER,
					 CMMsg.TYP_ELECTRIC,
					 CMMsg.TYP_GAS};
		int[] dames={Weapon.TYPE_BURNING,
					 Weapon.TYPE_FROSTING,
					 Weapon.TYPE_MELTING,
					 Weapon.TYPE_BURSTING,
					 Weapon.TYPE_STRIKING,
					 Weapon.TYPE_GASSING};
		String[] ds={"A flame",
					 "Some frost",
					 "Drops of acid",
					 "Stream of water",
					 "A spark",
					 "A puff of gas"};
		if(success)
		{
			int numMissiles=types.length;
			for(int i=0;i<numMissiles;i++)
			{
				FullMsg msg=new FullMsg(mob,target,this,affectType(auto),(i==0)?((auto?"An elemental storm assaults <T-NAME>!":"^S<S-NAME> point(s) at <T-NAMESELF>, evoking an elemental storm!^?")+CommonStrings.msp("spelldam1.wav",40)):null);
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					if(msg.value()<=0)
					{
						int damage = 0;
						damage += Dice.roll(1,3,0);
						if(target.location()==mob.location())
							MUDFight.postDamage(mob,target,this,damage,CMMsg.MASK_GENERAL|types[i],dames[i],"^S"+ds[i]+" <DAMAGE> <T-NAME>!^?");
					}
				}
				if(target.amDead())
				{
					target=this.getTarget(mob,commands,givenTarget,true);
					if(target==null)
						break;
					if(target.amDead())
						break;
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> point(s) at <T-NAMESELF>, but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}
