package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import java.util.*;


public class Chant_FindOre extends Chant_FindPlant
{
	public String ID() { return "Chant_FindOre"; }
	public String name(){ return "Find Ore";}
	public String displayText(){return "(Finding "+lookingFor+")";}
	public long flags(){return Ability.FLAG_TRACKING;}
	protected String lookingFor="ore";

	private int[] myMats={EnvResource.MATERIAL_ROCK,
						  EnvResource.MATERIAL_METAL};
	protected int[] okMaterials(){	return myMats;}
	protected int[] okResources(){	return null;}
}
