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
		
		int[] types={Affect.TYP_FIRE,
					 Affect.TYP_COLD,
					 Affect.TYP_ACID,
					 Affect.TYP_WATER,
					 Affect.TYP_ELECTRIC,
					 Affect.TYP_GAS};
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
				FullMsg msg=new FullMsg(mob,target,this,affectType(auto),(i==0)?((auto?"A magic missle appears hurling full speed at <T-NAME>!":"^S<S-NAME> point(s) at <T-NAMESELF>, evoking an elemental storm!^?")+CommonStrings.msp("spelldam1.wav",40)):null);
				if(mob.location().okAffect(mob,msg))
				{
					mob.location().send(mob,msg);
					if(!msg.wasModified())
					{
						int damage = 0;
						damage += Dice.roll(1,3,0);
						if(target.location()==mob.location())
							ExternalPlay.postDamage(mob,target,this,damage,Affect.MASK_GENERAL|types[i],dames[i],"^S"+ds[i]+" <DAMAGE> <T-NAME>!^?");
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
