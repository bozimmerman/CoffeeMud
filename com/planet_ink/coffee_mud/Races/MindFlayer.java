package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class MindFlayer extends Humanoid
{
	public String ID(){	return "MindFlayer"; }
	public String name(){ return "MindFlayer"; }
	protected static Vector resources=new Vector();
	public boolean playerSelectable(){return false;}
	public String racialCatagory(){return "Illithid";}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_DARK);
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.SAVE_MAGIC,affectableStats.getStat(CharStats.SAVE_MAGIC)+50);
		affectableStats.setStat(CharStats.SAVE_MIND,affectableStats.getStat(CharStats.SAVE_MIND)+100);
		affectableStats.setStat(CharStats.STRENGTH,affectableStats.getStat(CharStats.STRENGTH)-5);
		affectableStats.setStat(CharStats.CONSTITUTION,affectableStats.getStat(CharStats.CONSTITUTION)-5);
		affectableStats.setStat(CharStats.INTELLIGENCE,affectableStats.getStat(CharStats.INTELLIGENCE)+5);
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources=super.myResources();
				resources.addElement(makeResource
				("a "+name().toLowerCase()+" tenticle",EnvResource.RESOURCE_MEAT));
			}
		}
		return resources;
	}
}
