package com.planet_ink.coffee_mud.Abilities.Archon;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Archon_Multiwatch extends ArchonSkill
{
	public String ID() { return "Archon_Multiwatch"; }
	public String name(){ return "Multiwatch";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"MULTIWATCH"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public Environmental newInstance(){	return new Archon_Multiwatch();}
	public int usageType(){return USAGE_MOVEMENT;}
	
	public static 

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		
		return success;
	}

}