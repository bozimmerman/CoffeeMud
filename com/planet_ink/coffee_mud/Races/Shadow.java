package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Shadow extends Undead
{
	public String ID(){	return "Shadow"; }
	public String name(){ return "Shadow"; }
	protected int lightestWeight(){return 0;}
	protected int weightVariance(){return 0;}
	protected long forbiddenWornBits(){return 0;}

	protected static Vector resources=new Vector();
	public boolean playerSelectable(){return false;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if((Sense.isInDark(affected))
		||((affected instanceof MOB)&&(((MOB)affected).location()!=null)&&(Sense.isInDark((((MOB)affected).location())))))
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_INVISIBLE);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_GOLEM);
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" essence",EnvResource.RESOURCE_BLOOD));
			}
		}
		return resources;
	}
}

