package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_PlainsTactics extends Fighter_FieldTactics
{
	public String ID() { return "Fighter_PlainsTactics"; }
	public String name(){ return "Plains Tactics";}
	private static final Integer[] landClasses = {new Integer(Room.DOMAIN_OUTDOORS_PLAINS)};
	public Integer[] landClasses(){return landClasses;}
}
