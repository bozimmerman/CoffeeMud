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
	protected int overrideMana(){return Integer.MAX_VALUE;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Environmental target=getAnyTarget(mob,commands,givenTarget,Item.WORN_REQ_ANY);
		if(target==null) return false;

		if((mob.baseState().getMana()<100)||(mob.maxState().getMana()<100))
		{
			mob.tell("You aren't powerful enough to cast this.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> incant(s) to <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				StdAbility theOne=null;
				for(int a=target.numEffects()-1;a>=0;a--)
				{
					Ability A=target.fetchEffect(a);
					if((A.invoker()==mob)
					 &&(!A.isAutoInvoked())
					 &&(A.canBeUninvoked())
					 &&(A instanceof StdAbility)
					 &&((A.classificationCode()&Ability.ALL_CODES)==Ability.SPELL))
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
					theOne.makeNonUninvokable();
					theOne.setBorrowed(target,false);
					mob.baseState().setMana(mob.baseState().getMana()-100);
					mob.maxState().setMana(mob.maxState().getMana()-100);
					target.text();
					if((target instanceof Room)
					&&(CoffeeUtensils.doesOwnThisProperty(mob,(Room)target)))
						CMClass.DBEngine().DBUpdateRoom((Room)target);
					else
					if(target instanceof Exit)
					{
						Room R=mob.location();
						Room R2=null;
						for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
							if(R.getExitInDir(d)==target)
							{ R2=R.getRoomInDir(d); break;}
						if((CoffeeUtensils.doesOwnThisProperty(mob,R))
						||((R2!=null)&&(CoffeeUtensils.doesOwnThisProperty(mob,R2))))
							CMClass.DBEngine().DBUpdateExits(R);
					}
					mob.location().show(mob,target,null,CMMsg.MSG_OK_VISUAL,"The quality of "+theOne.name()+" inside <T-NAME> glows!");
				}
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> incant(s) to <T-NAMESELF>, but loses patience.");


		// return whether it worked
		return success;
	}
}