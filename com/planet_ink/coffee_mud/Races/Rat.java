package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.Vector;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Rat extends Rodent
{
	public String ID(){	return "Rat"; }
	public String name(){ return "Rat"; }
	public int shortestMale(){return 6;}
	public int shortestFemale(){return 6;}
	public int heightVariance(){return 6;}
	public int lightestWeight(){return 10;}
	public int weightVariance(){return 10;}
	public String racialCategory(){return "Rodent";}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,0 ,0 ,1 ,4 ,4 ,1 ,0 ,1 ,1 ,1 ,0 };
	public int[] bodyMask(){return parts;}

	protected static Vector resources=new Vector();
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setPermaStat(CharStats.STRENGTH,4);
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
					("some "+name().toLowerCase()+" hair",EnvResource.RESOURCE_FUR));
				resources.addElement(makeResource
					("a pair of "+name().toLowerCase()+" teeth",EnvResource.RESOURCE_BONE));
				resources.addElement(makeResource
					("some "+name().toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
			}
		}
		Vector rsc=(Vector)resources.clone();
		Item meat=makeResource
		("some "+name().toLowerCase()+" flesh",EnvResource.RESOURCE_MEAT);
		if(Dice.rollPercentage()<10)
		{
			Ability A=CMClass.getAbility("Disease_SARS");
			if(A!=null)	meat.addNonUninvokableEffect(A);
		}
		rsc.addElement(meat);
		return rsc;
	}
}
