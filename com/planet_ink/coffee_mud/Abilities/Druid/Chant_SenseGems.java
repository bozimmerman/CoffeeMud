package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import java.util.*;


public class Chant_SenseGems extends Chant_SensePlants
{
	public String ID() { return "Chant_SenseGems"; }
	public String name(){ return "Sense Gems";}
	public String displayText(){return "(Sensing Gems)";}
	public long flags(){return Ability.FLAG_TRACKING;}
	protected String word(){return "gems";};
	public Environmental newInstance(){	return new Chant_SenseGems();}
	
	private int[] myMats={EnvResource.MATERIAL_PRECIOUS,
						  EnvResource.MATERIAL_GLASS};
	protected int[] okMaterials(){	return myMats;}
	protected int[] okResources(){	return null;}
}
