package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/*
   Copyright 2000-2005 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Spell_Delude extends Spell
{
	public String ID() { return "Spell_Delude"; }
	public String name(){return "Delude";}
	public String displayText(){return "(Delude spell)";}
	public int quality(){ return OK_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	int previousAlignment=500;
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_TRANSMUTATION;}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
		{
			mob.addFaction(Factions.AlignID(),previousAlignment);
			mob.tell("Your attitude returns to normal.");
		}
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already deluding others.");
			return false;
		}
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);


		if((success)&&(Factions.isAlignEnabled()))
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
					previousAlignment=mob.fetchFaction(Factions.AlignID());

					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> undergo(es) a change of attitude");
					success=beneficialAffect(mob,target,asLevel,0);
					if(success)
					{
                        int which=0;
                        if(Sense.isEvil(mob)) 
                            which=1;
                        else 
                        if(Sense.isGood(mob)) 
                            which=2;
                        else
                        if(Dice.rollPercentage()>50) 
                            which=1;
                        else 
                            which=2;
                        switch(which) 
                        {
                            case 1:
                                // find a good range, set them within that
                                int newAlign=0;
                                Vector v=Factions.getRanges(Factions.AlignID());
                                for(int i=0;i<v.size();i++) 
                                {
                                    Faction.FactionRange R=(Faction.FactionRange)v.elementAt(i);
                                    if(R.AlignEquiv==Faction.ALIGN_GOOD) 
                                    {
                                        newAlign = R.random();
                                        break;
                                    }
                                }
                                mob.addFaction(Factions.AlignID(),newAlign);
                                return true;
                            case 2:
                                // find an evil range, set them within that
                                newAlign=0;
                                v=Factions.getRanges(Factions.AlignID());
                                for(int i=0;i<v.size();i++) 
                                {
                                    Faction.FactionRange R=(Faction.FactionRange)v.elementAt(i);
                                    if(R.AlignEquiv==Faction.ALIGN_EVIL) 
                                    {
                                        newAlign = R.random();
                                        break;
                                    }
                                }
                                mob.addFaction(Factions.AlignID(),newAlign);
                                return true;
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
