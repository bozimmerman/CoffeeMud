package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Curse extends Prayer
{
	public Prayer_Curse()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Curse";
		displayText="(Cursed)";
		quality=Ability.MALICIOUS;
		baseEnvStats().setLevel(7);
		holyQuality=Prayer.HOLY_EVIL;

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_Curse();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		affectableStats.setDisposition(affectableStats.disposition()|Sense.IS_EVIL);
		if(affected instanceof MOB)
			affectableStats.setArmor(affectableStats.armor()+10);
		else
		if(affected instanceof Item)
			affectableStats.setAbility(affectableStats.ability()-1);
	}

	public void unInvoke()
	{


		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
		{
			if((affected instanceof Item)&&(((Item)affected).myOwner()!=null)&&(((Item)affected).myOwner() instanceof MOB))
				((MOB)((Item)affected).myOwner()).tell("The curse on "+((Item)affected).name()+" is lifted.");
			super.unInvoke();
			return;
		}
		MOB mob=(MOB)affected;
		mob.tell("The curse is lifted.");
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType|Affect.MASK_MALICIOUS,auto?"<T-NAME> is cursed!":"<S-NAME> curse(s) <T-NAMESELF>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					success=maliciousAffect(mob,target,0,-1);
					int a=0;
					while(a<target.numAffects())
					{
						Ability A=target.fetchAffect(a);
						if(A!=null)
						{
							int b=target.numAffects();
							if(A instanceof Prayer_Bless)
								A.unInvoke();
							else
							if(A instanceof Prayer_UnholyWord)
								A.unInvoke();
							else
							if(A instanceof Prayer_GreatCurse)
								A.unInvoke();
							if(b==target.numAffects())
								a++;
						}
						else
							a++;
					}
					target.recoverEnvStats();
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to curse <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
