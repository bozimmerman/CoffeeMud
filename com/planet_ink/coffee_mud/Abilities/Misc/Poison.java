package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Poison extends StdAbility
{
	int poisonTick=3;

	public Poison()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Poison";
		displayText="(Poisoned)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		quality=Ability.MALICIOUS;

		baseEnvStats().setLevel(10);
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
		
		if((--poisonTick)<=0)
		{
			poisonTick=3;
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"<S-NAME> cringe(s) as the poison courses through <S-HIS-HER> blood.");
			int damage=Dice.roll(envStats().level(),3,1);
			ExternalPlay.postDamage(invoker,mob,this,damage);
		}
		return true;
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		if(affected==null) return;
		affectableStats.setConstitution(affectableStats.getConstitution()-5);
		affectableStats.setStrength(affectableStats.getStrength()-5);
		if(affectableStats.getConstitution()<=0)
			affectableStats.setConstitution(1);
		if(affectableStats.getStrength()<=0)
			affectableStats.setStrength(1);
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		mob.tell(mob,null,"The poison runs its course.");
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
			String str=auto?"":"<S-NAME> attempt(s) to poison <T-NAMESELF>!";
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_POISON|(auto?Affect.ACT_GENERAL:0),str);
			if(target.location().okAffect(msg))
			{
			    target.location().send(target,msg);
				if(!msg.wasModified())
				{
					mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> turn(s) green!");
				    success=maliciousAffect(mob,target,0,-1);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to poison <T-NAMESELF>, but fail(s).");

        return success;

	}

}
