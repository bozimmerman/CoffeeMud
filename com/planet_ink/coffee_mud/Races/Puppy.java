package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.Vector;
public class Puppy extends Dog
{
	protected static Vector resources=new Vector();
	public Puppy()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name=myID;
		// inches
		shortestMale=6;
		shortestFemale=6;
		heightVariance=3;
		// pounds
		lightestWeight=7;
		weightVariance=20;
		forbiddenWornBits=Integer.MAX_VALUE-Item.ON_HEAD-Item.ON_FEET-Item.ON_NECK-Item.ON_EARS-Item.ON_EYES;
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STRENGTH,6);
		affectableStats.setStat(CharStats.DEXTERITY,11);
		affectableStats.setStat(CharStats.INTELLIGENCE,1);
	}
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{
		affectableMaxState.setMovement(affectableMaxState.getMovement()+50);
	}
}
