package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skeleton extends Undead
{
	public String ID(){	return "Skeleton"; }
	public String name(){ return "Skeleton"; }

	protected static Vector resources=new Vector();

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(myHost instanceof MOB)
		{
			MOB mob=(MOB)myHost;
			if((msg.amITarget(mob))
			&&(Util.bset(msg.targetCode(),CMMsg.MASK_HURT))
			&&(msg.tool()!=null)
			&&(msg.tool() instanceof Weapon)
			&&((((Weapon)msg.tool()).weaponType()==Weapon.TYPE_PIERCING)
				||(((Weapon)msg.tool()).weaponType()==Weapon.TYPE_SLASHING))
			&&(!mob.amDead()))
			{
				int recovery=(int)Math.round(Util.div((msg.targetCode()-CMMsg.MASK_HURT),2.0));
				SaucerSupport.adjustDamageMessage(msg,recovery*-1);
			}
		}
		return super.okMessage(myHost,msg);
	}

	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
					("knuckle bone",EnvResource.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
