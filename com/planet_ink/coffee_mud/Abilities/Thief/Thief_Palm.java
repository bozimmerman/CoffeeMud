package com.planet_ink.coffee_mud.Abilities.Thief;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Palm extends ThiefSkill
{
	public String ID() { return "Thief_Palm"; }
	public String name(){ return "Palm";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"PALM"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_Palm();}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	public int combatCastingTime(){return 0;}
	public int castingTime(){return 0;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		boolean success=profficiencyCheck(mob,0,auto);
		if(!success)
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to palm something and fail(s).");
		else
		{
			if((commands.size()>0)&&(!((String)commands.lastElement()).equalsIgnoreCase("UNOBTRUSIVELY")))
			   commands.addElement("UNOBTRUSIVELY");
			try
			{
				Command C=CMClass.getCommand("Get");
				if(C!=null) C.execute(mob,commands);
			}
			catch(Exception e)
			{}
		}
		return success;
	}
}