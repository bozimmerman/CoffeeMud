package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Mageness extends CombatAbilities
{
	public String ID(){return "Mageness";}
	public Behavior newInstance()
	{
		return new Mageness();
	}

	protected void getSomeMoreMageAbilities(MOB mob)
	{
		for(int a=0;a<((mob.baseEnvStats().level())+5);a++)
		{
			Ability addThis=null;
			int tries=0;
			while((addThis==null)&&((++tries)<10))
			{
				addThis=(Ability)CMClass.abilities.elementAt(Dice.roll(1,CMClass.abilities.size(),0)-1);
				if((CMAble.qualifyingLevel(mob,addThis)<0)
				||(!CMAble.qualifiesByLevel(mob,addThis))
				||(((addThis.classificationCode()&Ability.ALL_CODES)==Ability.PRAYER)&&(!addThis.appropriateToMyAlignment(mob.getAlignment())))
				||(mob.fetchAbility(addThis.ID())!=null)
				||((addThis.quality()!=Ability.MALICIOUS)
				   &&(addThis.quality()!=Ability.BENEFICIAL_SELF)
				   &&(addThis.quality()!=Ability.BENEFICIAL_OTHERS)))
					addThis=null;
			}
			if(addThis!=null)
			{
				addThis=(Ability)addThis.newInstance();
				addThis.setBorrowed(mob,true);
				mob.addAbility(addThis);
				addThis.autoInvocation(mob);
			}
		}
	}

	public void startBehavior(Environmental forMe)
	{
		super.startBehavior(forMe);
		if(!(forMe instanceof MOB)) return;
		MOB mob=(MOB)forMe;
		if(!mob.baseCharStats().getCurrentClass().ID().equals("Mage"))
		{
			mob.baseCharStats().setCurrentClass("Mage");
			mob.baseCharStats().setClassLevel("Mage",mob.envStats().level());
			mob.recoverCharStats();
		}
		// now equip character...
		newCharacter(mob);
		getSomeMoreMageAbilities(mob);
	}
}