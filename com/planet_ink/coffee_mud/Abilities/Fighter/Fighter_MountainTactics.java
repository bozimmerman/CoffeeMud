package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_MountainTactics extends Fighter_FieldTactics
{
	public String ID() { return "Fighter_MountainTactics"; }
	public String name(){ return "Mountain Tactics";}
	public Environmental newInstance(){	return new Fighter_MountainTactics();}
	private static final Integer[] landClasses = {new Integer(Room.DOMAIN_OUTDOORS_MOUNTAINS)};
	public Integer[] landClasses(){return landClasses;}
}
