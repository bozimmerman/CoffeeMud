package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Delude extends Spell
{
	public String ID() { return "Spell_Delude"; }
	public String name(){return "Delude";}
	public String displayText(){return "(Delude spell)";}
	public int quality(){ return OK_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	int previousAlignment=500;
	public Environmental newInstance(){	return new Spell_Delude();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_TRANSMUTATION;}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
		{
			mob.setAlignment(previousAlignment);
			mob.tell("Your attitude returns to normal.");
		}
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already deluding others.");
			return false;
		}
		boolean success=profficiencyCheck(mob,0,auto);


		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> incant(s) and meditate(s).^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					int alignment = mob.getAlignment();
					previousAlignment=alignment;

					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> undergo(es) a change of attitude");
					success=beneficialAffect(mob,target,0);
					if(success)
					{
						if(alignment < 350)
						{
							mob.setAlignment(1000);
							return true;
						}
						else
						if(alignment > 650)
						{
							mob.setAlignment(0);
							return true;
						}
						else
						{
							if(Dice.rollPercentage()>50)
								mob.setAlignment(1000);
							else
								mob.setAlignment(0);
						}

					}
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> incant(s) and meditate(s), but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}
