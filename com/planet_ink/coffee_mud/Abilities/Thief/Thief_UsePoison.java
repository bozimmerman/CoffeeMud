package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_UsePoison extends ThiefSkill
{
	public String ID() { return "Thief_UsePoison"; }
	public String name(){ return "Use Poison";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"POISON"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_UsePoison();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<2)
		{
			mob.tell("What would you like to poison, and which poison would you use?");
			return false;
		}
		Item target=mob.fetchCarried(null,(String)commands.elementAt(0));
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("You don't see '"+((String)commands.elementAt(0))+"' here.");
			return false;
		}
		Item poison=mob.fetchCarried(null,Util.combine(commands,1));
		if((poison==null)||((poison!=null)&&(!Sense.canBeSeenBy(poison,mob))))
		{
			mob.tell("You don't see '"+Util.combine(commands,1)+"' here.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_THIEF_ACT,"<S-NAME> poison(s) <T-NAMESELF>.");
		if(mob.location().okAffect(mob,msg))
		{
			mob.location().send(mob,msg);
		}
		return success;
	}

}