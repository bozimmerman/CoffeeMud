package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.Vector;

public class Rat extends Rodent
{
	public String ID(){	return "Rat"; }
	public String name(){ return "Rat"; }
	protected int shortestMale(){return 6;}
	protected int shortestFemale(){return 6;}
	protected int heightVariance(){return 6;}
	protected int lightestWeight(){return 10;}
	protected int weightVariance(){return 10;}
	public String racialCategory(){return "Rodent";}
	
	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,0 ,0 ,1 ,4 ,4 ,1 ,0 ,1 ,1 ,1 ,0 };
	public int[] bodyMask(){return parts;}
	
	protected static Vector resources=new Vector();
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STRENGTH,4);
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
			if(A!=null)	meat.addNonUninvokableAffect(A);
		}
		rsc.addElement(meat);
		return rsc;
	}
}
