package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Nullification extends Prayer
{
	public Prayer_Nullification()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Nullification";
		baseEnvStats().setLevel(22);
		quality=Ability.BENEFICIAL_OTHERS;
		holyQuality=Prayer.HOLY_NEUTRAL;

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_Nullification();
	}

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
				FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"<T-NAME> become(s) nullified.":"<S-NAME> sweep(s) <S-HIS-HER> hands over <T-NAMESELF>.");
				if(mob.location().okAffect(msg))
				{
					mob.location().send(mob,msg);
					Ability revokeThis=null;
					boolean foundSomethingAtLeast=false;
					for(int a=0;a<target.numAffects();a++)
					{
						Ability A=(Ability)target.fetchAffect(a);
						if((A!=null)&&(A.canBeUninvoked())&&(!A.isAnAutoEffect())
						&&(((A.classificationCode()&Ability.ALL_CODES)==Ability.SPELL)
						   ||((A.classificationCode()&Ability.ALL_CODES)==Ability.PRAYER)
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
							mob.location().show(mob,target,Affect.MSG_OK_VISUAL,"The magic on <T-NAME> appears too powerful to be nullified.");
						else
						if(auto)
							mob.tell(mob,target,"Nothing seems to be happening to <T-NAME>.");
					}
					else
						revokeThis.unInvoke();
				}
			}
			else
			{
				// it didn't work, but tell everyone you tried.
				FullMsg msg=new FullMsg(mob,target,this,affectType,"<S-NAME> sweep(s) <S-HIS-HER> hands over <T-NAMESELF>, but <S-HIS-HER> god does not heed.");
				if(mob.location().okAffect(msg))
					mob.location().send(mob,msg);
			}
		}

		// return whether it worked
		return success;
	}
}