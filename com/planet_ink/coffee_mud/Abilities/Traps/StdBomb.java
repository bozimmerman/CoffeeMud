package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdBomb extends StdTrap
{
	public String ID() { return "StdBomb"; }
	public String name(){ return "a bomb";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public String requiresToSet(){return "";}
	public boolean isABomb(){return true;}
	public int baseRejuvTime(int level){ return 5;}

}
