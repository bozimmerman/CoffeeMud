package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import java.util.*;


public class Chant_SenseOres extends Chant_SensePlants
{
	public String ID() { return "Chant_SenseOres"; }
	public String name(){ return "Sense Ores";}
	public String displayText(){return "(Sensing Ores)";}
	public long flags(){return Ability.FLAG_TRACKING;}
	protected String word(){return "ores";};

	private int[] myMats={EnvResource.MATERIAL_ROCK,
						  EnvResource.MATERIAL_METAL};
	protected int[] okMaterials(){	return myMats;}
	protected int[] okResources(){	return null;}
}
