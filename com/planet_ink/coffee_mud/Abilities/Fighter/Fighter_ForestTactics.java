package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_ForestTactics extends Fighter_FieldTactics
{
	public String ID() { return "Fighter_ForestTactics"; }
	public String name(){ return "Forest Tactics";}
	public Environmental newInstance(){	return new Fighter_ForestTactics();}
	private static final Integer[] landClasses = {new Integer(Room.DOMAIN_OUTDOORS_WOODS)};
	public Integer[] landClasses(){return landClasses;}
}
