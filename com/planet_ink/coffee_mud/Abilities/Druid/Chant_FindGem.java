package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import java.util.*;


public class Chant_FindGem extends Chant_FindPlant
{
	public String ID() { return "Chant_FindGem"; }
	public String name(){ return "Find Gem";}
	public String displayText(){return "(Finding "+lookingFor+")";}
	public long flags(){return Ability.FLAG_TRACKING;}
	protected String lookingFor="gem";
	public Environmental newInstance(){	return new Chant_FindGem();}
	
	private int[] myMats={EnvResource.MATERIAL_PRECIOUS,
						  EnvResource.MATERIAL_GLASS};
	protected int[] okMaterials(){	return myMats;}
	protected int[] okResources(){	return null;}
}
