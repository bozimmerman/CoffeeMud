package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Poison extends StdAbility
{
	int tickDown=3;

	public Poison()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Poison";
		displayText="(Poisoned)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		malicious=true;

		baseEnvStats().setLevel(1);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Poison();
	}

	public boolean tick(int tickID)
	{
		if(!super.tick(tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null) return false;
		if(invoker==null) return false;
		if(mob==invoker) return true;

		if((--tickDown)<=0)
		{
			tickDown=3;
			mob.location().show(mob,null,Affect.VISUAL_ONLY,"<S-NAME> cringe(s) as the poison courses through <S-HIS-HER> blood.");
			int hpLoss=Dice.roll(envStats().level(),3,1);
			TheFight.doDamage(mob,hpLoss);
		}
		return true;
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		if(affected==null) return;
		affectableStats.setConstitution(3);
		affectableStats.setStrength(3);
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		mob.tell(mob,null,"The poison runs its course.");
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		MOB target=this.getTarget(mob,commands);
		if(target==null) return false;

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);
		if(success)
		{
			String str="<S-NAME> attempt(s) to poison <T-NAME>!";
			FullMsg msg=new FullMsg(mob,target,null,Affect.VISUAL_WNOISE,str,Affect.STRIKE_POISON,str,Affect.VISUAL_WNOISE,str);
			if(target.okAffect(msg))
			{
			    if(mob.location().okAffect(msg))
			    {
				    mob.location().send(mob,msg);
					if(!msg.wasModified())
					{
						mob.location().show(mob,target,Affect.VISUAL_WNOISE,"<T-NAME> turn(s) green!");
					    success=maliciousAffect(mob,target,0,-1);
					}
			    }
            }
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to poison <T-NAME>, but fail(s).");

        return success;

	}

}
