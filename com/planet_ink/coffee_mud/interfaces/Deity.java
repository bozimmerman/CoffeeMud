package com.planet_ink.coffee_mud.interfaces;

public interface Deity extends MOB
{
	public String getClericRequirements();
	public void setClericRequirements(String reqs);
	public String getClericRequirementsDesc();
	
	public String getWorshipRequirements();
	public void setWorshipRequirements(String reqs);
	public String getWorshipRequirementsDesc();
	
	/** Manipulation of blessing objects, which includes spells, traits, skills, etc.*/
	public void addBlessing(Ability to);
	public void delBlessing(Ability to);
	public int numBlessings();
	public Ability fetchBlessing(int index);
	public Ability fetchBlessing(String ID);
	
	public String getClericRitual();
	public void setClericRitual(String ritual);
	public String getClericTriggerDesc();
	
	public String getWorshipRitual();
	public void setWorshipRitual(String ritual);
	public String getWorshipTriggerDesc();
	
	/** Manipulation of curse objects, which includes spells, traits, skills, etc.*/
	public void addCurse(Ability to);
	public void delCurse(Ability to);
	public int numCurses();
	public Ability fetchCurse(int index);
	public Ability fetchCurse(String ID);
	
	public String getClericSin();
	public void setClericSin(String ritual);
	public String getClericSinDesc();
	
	public String getWorshipSin();
	public void setWorshipSin(String ritual);
	public String getWorshipSinDesc();
	
	/** Manipulation of granted clerical powers, which includes spells, traits, skills, etc.*/
	/** Make sure that none of these can really be qualified for by the cleric!*/
	public void addPower(Ability to);
	public void delPower(Ability to);
	public int numPowers();
	public Ability fetchPower(int index);
	public Ability fetchPower(String ID);
	
	public String getClericPowerup();
	public void setClericPowerup(String ritual);
	public String getClericPowerupDesc();
	
}
