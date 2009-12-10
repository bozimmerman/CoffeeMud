package com.planet_ink.coffee_mud.Abilities.Prayers;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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

@SuppressWarnings("unchecked")
public class Prayer_AuraIntolerance extends Prayer
{
	public String ID() { return "Prayer_AuraIntolerance"; }
	public String name(){ return "Aura of Intolerance";}
	public String displayText(){ return "(Intolerance Aura)";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_COMMUNING;}
	public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_SELF;}
	public long flags(){return Ability.FLAG_HOLY;}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB M=(MOB)affected;

		super.unInvoke();

		if((canBeUninvoked())&&(M!=null)&&(!M.amDead())&&(M.location()!=null))
			M.location().show(M,null,CMMsg.MSG_OK_VISUAL,"The intolerant aura around <S-NAME> fades.");
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		if((msg.source()==affected)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.target() instanceof MOB)
		&&(msg.source().getWorshipCharID().length()>0)
		&&(!((MOB)msg.target()).getWorshipCharID().equals(msg.source().getWorshipCharID())))
		{
			if(((MOB)msg.target()).getWorshipCharID().length()>0)
				msg.setValue(msg.value()*2);
			else
				msg.setValue(msg.value()+(msg.value()/2));
		}
		return true;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(ticking,tickID);

		if(!super.tick(ticking,tickID))
			return false;

		Room R=((MOB)affected).location();
		for(int i=0;i<R.numInhabitants();i++)
		{
			MOB M=R.fetchInhabitant(i);
			if((M!=null)
			&&(M!=((MOB)affected))
			&&((M.getWorshipCharID().length()==0)
				||(((MOB)affected).getWorshipCharID().length()>0)&&(!M.getWorshipCharID().equals(((MOB)affected).getWorshipCharID()))))
			{
				if(M.getWorshipCharID().length()>0)
					CMLib.combat().postDamage(((MOB)affected),M,this,3,CMMsg.MASK_ALWAYS|CMMsg.TYP_UNDEAD,Weapon.TYPE_BURSTING,"The intolerant aura around <S-NAME> <DAMAGES> <T-NAMESELF>!");
				else
					CMLib.combat().postDamage(((MOB)affected),M,this,1,CMMsg.MASK_ALWAYS|CMMsg.TYP_UNDEAD,Weapon.TYPE_BURSTING,"The intolerant aura around <S-NAME> <DAMAGES> <T-NAMESELF>!");
			}
		}
		return true;
	}
	
    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(((mob.getWorshipCharID().length()==0)
            ||(CMLib.map().getDeity(mob.getWorshipCharID())==null)))
                return Ability.QUALITY_INDIFFERENT;
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target,null,null,"The aura of intolerance is already with <S-NAME>.");
			return false;
		}
		if((!auto)&&((mob.getWorshipCharID().length()==0)
					 ||(CMLib.map().getDeity(mob.getWorshipCharID())==null)))
		{
			mob.tell("You must worship a god to be intolerant.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" for the aura of intolerance.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for an aura of intolerance, but <S-HIS-HER> plea is not answered.");


		// return whether it worked
		return success;
	}
}
