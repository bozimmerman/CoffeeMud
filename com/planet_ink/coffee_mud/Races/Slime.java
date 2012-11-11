package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2012 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Slime extends StdRace
{
	public String ID(){	return "Slime"; }
	public String name(){ return "Slime"; }
	public int shortestMale(){return 24;}
	public int shortestFemale(){return 24;}
	public int heightVariance(){return 12;}
	public int lightestWeight(){return 80;}
	public int weightVariance(){return 80;}
	public long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Slime";}
	public boolean fertile(){return false;}

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
	public int[] bodyMask(){return parts;}

	private int[] agingChart={0,0,0,0,0,YEARS_AGE_LIVES_FOREVER,YEARS_AGE_LIVES_FOREVER,YEARS_AGE_LIVES_FOREVER,YEARS_AGE_LIVES_FOREVER};
	public int[] getAgingChart(){return agingChart;}
	
	protected static Vector<RawMaterial> resources=new Vector<RawMaterial>();
	public int availabilityCode(){return Area.THEME_FANTASY|Area.THEME_SKILLONLYMASK;}

	public String healthText(MOB viewer, MOB mob)
	{
		double pct=(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.displayName(viewer) + "^r is unstable and almost disintegrated!^N";
		else
		if(pct<.20)
			return "^r" + mob.displayName(viewer) + "^r is nearing disintegration.^N";
		else
		if(pct<.30)
			return "^r" + mob.displayName(viewer) + "^r is noticeably disintegrating.^N";
		else
		if(pct<.40)
			return "^y" + mob.displayName(viewer) + "^y is very damaged and slightly disintegrated.^N";
		else
		if(pct<.50)
			return "^y" + mob.displayName(viewer) + "^y is very damaged.^N";
		else
		if(pct<.60)
			return "^p" + mob.displayName(viewer) + "^p is starting to show major damage.^N";
		else
		if(pct<.70)
			return "^p" + mob.displayName(viewer) + "^p is definitely damaged.^N";
		else
		if(pct<.80)
			return "^g" + mob.displayName(viewer) + "^g is disheveled and mildly damaged.^N";
		else
		if(pct<.90)
			return "^g" + mob.displayName(viewer) + "^g is noticeably disheveled.^N";
		else
		if(pct<.99)
			return "^g" + mob.displayName(viewer) + "^g is slightly disheveled.^N";
		else
			return "^c" + mob.displayName(viewer) + "^c is in perfect condition.^N";
	}
	
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(myHost instanceof MOB)
		{
			if((msg.amITarget(myHost))
			&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
			&&(msg.tool()!=null)
			&&(msg.tool() instanceof Weapon)
			&&(msg.source().getVictim()==myHost)
			&&(msg.source().rangeToTarget()==0)
			&&((((Weapon)msg.tool()).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL)
			&&(((Weapon)msg.tool()).subjectToWearAndTear())
			&&(CMLib.dice().rollPercentage()<20)
			&&(!((MOB)myHost).amDead()))
				CMLib.combat().postItemDamage(msg.source(), (Item)msg.tool(), null, 10, CMMsg.TYP_ACID,"<T-NAME> sizzle(s)!");
		}
	}

	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("a "+name().toLowerCase()+" bit",RawMaterial.RESOURCE_SLIME));
			}
		}
		return resources;
	}
}
