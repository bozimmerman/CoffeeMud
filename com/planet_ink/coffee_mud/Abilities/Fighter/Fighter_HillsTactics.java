package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_HillsTactics extends Fighter_FieldTactics
{
	public String ID() { return "Fighter_HillsTactics"; }
	public String name(){ return "Hills Tactics";}
	private static final Integer[] landClasses = {new Integer(Room.DOMAIN_OUTDOORS_HILLS)};
	public Integer[] landClasses(){return landClasses;}
}
