package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Permanency extends Spell
{
	public String ID() { return "Spell_Permanency"; }
	public String name(){return "Permanency";}
	protected int canAffectCode(){return CAN_ITEMS|CAN_MOBS|CAN_EXITS;}
	protected int canTargetCode(){return CAN_ITEMS|CAN_MOBS|CAN_EXITS;}
	public Environmental newInstance(){	return new Spell_Permanency();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Environmental target=getAnyTarget(mob,commands,givenTarget,Item.WORN_REQ_ANY);
		if(target==null) return false;

		if(mob.curState().getMana()<mob.maxState().getMana())
		{
			mob.tell("You need to be at full mana to cast this.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		mob.curState().setMana(0);

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> incant(s) to <T-NAMESELF>.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				StdAbility theOne=null;
				for(int a=target.numAffects()-1;a>=0;a--)
				{
					Ability A=target.fetchAffect(a);
					if((A.invoker()==mob)
					 &&(!A.isAutoInvoked())
					 &&(A.canBeUninvoked())
					 &&(A instanceof StdAbility)
					 &&((A.classificationCode()&Ability.ALL_CODES)!=Ability.PROPERTY))
					{
						theOne=(StdAbility)A;
						break;
					}
				}
				if(theOne==null)
				{
					mob.tell("There does not appear to be any of your spells on "+target.name()+" which can be made permanent.");
					return false;
				}
				else
				{
					int exp=10*CMAble.lowestQualifyingLevel(theOne.ID());
					mob.tell("You lose "+exp+" experience points.");
					mob.charStats().getCurrentClass().loseExperience(mob,exp);
					theOne.makeNonUninvokable();
					mob.location().show(mob,target,null,Affect.MSG_OK_VISUAL,"The quality of "+theOne.name()+" inside <T-NAME> glows!");
				}
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> incant(s) to <T-NAMESELF>, but loses patience.");


		// return whether it worked
		return success;
	}
}