package com.planet_ink.coffee_mud.Abilities.Misc;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import java.util.*;


public class Regeneration extends StdAbility
{
	private static final int maxTickDown=3;
	private int regenTick=maxTickDown;

	public Regeneration()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Regeneration";
		displayText="(Regeneration)";
		miscText="";

		canBeUninvoked=false;
		isAutoinvoked=false;
		quality=Ability.BENEFICIAL_SELF;

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Regeneration();
	}

	public boolean tick(int tickID)
	{
		if(!super.tick(tickID))
			return false;

		if((--regenTick)>0)
			return true;
		regenTick=maxTickDown;
		MOB mob=(MOB)affected;
		if(mob==null) return true;
		if(mob.location()==null) return true;
		if(mob.amDead()) return true;

		boolean doneAnything=false;
		doneAnything=doneAnything||mob.curState().adjHitPoints((int)Math.round(Util.div(mob.envStats().level(),2.0)),mob.maxState());
		doneAnything=doneAnything||mob.curState().adjMana(mob.envStats().level()*2,mob.maxState());
		doneAnything=doneAnything||mob.curState().adjMovement(mob.envStats().level()*3,mob.maxState());
		if(doneAnything)
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"<S-NAME> regenerate(s).");
		return true;
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		mob.tell(mob,null,"You feel less regenerative.");
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
			String str=auto?"":"<S-NAME> lay(s) regenerative magic upon <T-NAMESELF>";
			FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_QUIETMOVEMENT,str);
			if(target.location().okAffect(msg))
			{
			    target.location().send(target,msg);
				success=beneficialAffect(mob,target,0);
			}
		}

        return success;

	}
}