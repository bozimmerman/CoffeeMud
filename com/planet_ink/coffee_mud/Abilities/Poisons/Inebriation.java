package com.planet_ink.coffee_mud.Abilities.Poisons;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import java.util.*;


public class Inebriation extends Poison_Alcohol
{
	public String ID() { return "Inebriation"; }
	public String name(){ return "Inebriation";}
	private static final String[] triggerStrings = {"INEBRIATE"};
	public String[] triggerStrings(){return triggerStrings;}
}