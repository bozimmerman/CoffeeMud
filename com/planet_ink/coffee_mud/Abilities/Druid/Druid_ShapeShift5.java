package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Druid_ShapeShift5 extends Druid_ShapeShift
{
	public String ID() { return "Druid_ShapeShift5"; }
	public String name(){ return "Fifth Totem";}
	public int quality(){return Ability.OK_SELF;}
	public String[] triggerStrings(){return empty;}

	public Environmental newInstance(){	return new Druid_ShapeShift5();}
}
