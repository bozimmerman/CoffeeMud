package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.interfaces.*;

public class Faerie extends SmallElfKin
{
	public String ID(){	return "Faerie"; }
	public String name(){ return "Faerie"; }
	
	
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_FLYING);
	}
}
