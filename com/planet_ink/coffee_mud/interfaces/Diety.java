package com.planet_ink.coffee_mud.interfaces;

public interface Diety extends MOB
{
	public String getClericRequirements();
	public void setClericRequirements(String reqs);
	public String getWorshipRequirements();
	public void setWorshipRequirements(String reqs);
	public String getClericRitual();
	public void setClericRitual(String ritual);
	public String getWorshipRitual();
	public void setWorshipRitual(String ritual);
	
	public String getClericRequirementsDesc();
	public String getClericTriggerDesc();
	public String getWorshipRequirementsDesc();
	public String getWorshipTriggerDesc();
	
	/** Manipulation of blessing objects, which includes spells, traits, skills, etc.*/
	public void addBlessing(Ability to);
	public void delBlessing(Ability to);
	public int numBlessings();
	public Ability fetchBlessing(int index);
	public Ability fetchBlessing(String ID);
}
