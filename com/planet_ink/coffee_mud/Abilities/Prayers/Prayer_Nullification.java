package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Nullification extends Prayer
{
	public String ID() { return "Prayer_Nullification"; }
	public String name(){ return "Nullification";}
	public int quality(){ return OK_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Prayer_Nullification();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			MOB target=(MOB)mob.location().fetchInhabitant(i);
			if((target!=null)&&(success))
			{
				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> become(s) nullified.":"^S<S-NAME> sweep(s) <S-HIS-HER> hands over <T-NAMESELF>.^?");
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					Ability revokeThis=null;
					boolean foundSomethingAtLeast=false;
					for(int a=0;a<target.numEffects();a++)
					{
						Ability A=(Ability)target.fetchEffect(a);
						if((A!=null)&&(A.canBeUninvoked())&&(!A.isAutoInvoked())
						&&(((A.classificationCode()&Ability.ALL_CODES)==Ability.SPELL)
						   ||((A.classificationCode()&Ability.ALL_CODES)==Ability.PRAYER)
						   ||((A.classificationCode()&Ability.ALL_CODES)==Ability.CHANT)
						   ||((A.classificationCode()&Ability.ALL_CODES)==Ability.SONG)))
						{
							foundSomethingAtLeast=true;
							if((A.invoker()!=null)&&((A.invoker().envStats().level()<mob.envStats().level())))
								revokeThis=A;
						}
					}

					if(revokeThis==null)
					{
						if(foundSomethingAtLeast)
							mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,"The magic on <T-NAME> appears too powerful to be nullified.");
						else
						if(auto)
							mob.tell(mob,target,null,"Nothing seems to be happening to <T-NAME>.");
					}
					else
						revokeThis.unInvoke();
				}
			}
			else
				beneficialWordsFizzle(mob,target,auto?"":"<S-NAME> sweep(s) <S-HIS-HER> hands over <T-NAMESELF>, but "+hisHerDiety(mob)+" does not heed.");
		}

		// return whether it worked
		return success;
	}
}