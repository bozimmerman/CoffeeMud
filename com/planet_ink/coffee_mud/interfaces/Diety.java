package com.planet_ink.coffee_mud.interfaces;

public interface Diety extends MOB
{
	public String getClericRequirements();
	public void setClericRequirements(String reqs);
	public String getWorshipRequirements();
	public void setWorshipRequirements(String reqs);
}
