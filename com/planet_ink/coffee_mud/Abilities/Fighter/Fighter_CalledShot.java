package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_CalledShot extends Fighter_CalledStrike
{
	public String ID() { return "Fighter_CalledShot"; }
	public String name(){ return "Called Shot";}
	private static final String[] triggerStrings = {"CALLEDSHOT"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Fighter_CalledShot();}

	protected boolean prereqs(MOB mob)
	{
		if(mob.isInCombat()&&(mob.rangeToTarget()==0))
		{
			mob.tell("You are too close to perform a called shot!");
			return false;
		}
		
		Item w=mob.fetchWieldedItem();
		if((w==null)||(!(w instanceof Weapon)))
		{
			mob.tell("You need a weapon to perform a called shot!");
			return false;
		}
		Weapon wp=(Weapon)w;
		if((wp.weaponClassification()!=Weapon.CLASS_RANGED)&&(wp.weaponClassification()!=Weapon.CLASS_THROWN))
		{
			mob.tell("You cannot shoot with "+wp.name()+"!");
			return false;
		}
		return true;
	}
}
