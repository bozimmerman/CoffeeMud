package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_FakeFood extends Spell
{
	public Spell_FakeFood()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Fake Food";

		canBeUninvoked=true;
		isAutoinvoked=false;

		canAffectCode=0;
		canTargetCode=0;
		
		baseEnvStats().setLevel(13);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_FakeFood();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ILLUSION;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType,auto?"":"<S-NAME> invoke(s) a spell dramatically.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				Food F=(Food)CMClass.getItem("GenFood");
				switch(Dice.roll(1,5,0))
				{
				case 1: F.setName("a shiny apple");
						F.setDisplayText("A shiny red apple sits here.");
						F.setDescription("It looks tasty and crisp!");
						break;
				case 2: F.setName("a nice peach");
						F.setDisplayText("A nice peach sits here.");
						F.setDescription("It looks tasty!");
						break;
				case 3: F.setName("a big pot pie");
						F.setDisplayText("A big pot pie has been left here.");
						F.setDescription("It sure looks good!");
						break;
				case 4: F.setName("a juicy steak");
						F.setDisplayText("A juicy steak has been left here.");
						F.setDescription("It sure looks good!");
						break;
				case 5: F.setName("a bit of food");
						F.setDisplayText("A bit of food has been left here.");
						F.setDescription("It sure looks good!");
						break;
				}
				F.setNourishment(0);
				for(int f=0;f<5;f++)
				{
					Food F2=(Food)F.copyOf();
					F2.recoverEnvStats();
					mob.location().addItemRefuse(F2);
					mob.location().show(mob,null,Affect.MSG_OK_VISUAL,F2.name()+" appears!");
				}
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> dramatically attempt(s) to invoke a spell, but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}
