package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.Abilities.Traps.Trap_Trap;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_MinorTrap extends Thief_Trap
{
	public String ID() { return "Thief_MinorTrap"; }
	public String name(){ return "Lay Minor Traps";}
	private static final String[] triggerStrings = {"MTRAP","MINORTRAP"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}

	protected int maxLevel(){return 3;}
}
