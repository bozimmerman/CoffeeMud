package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.Items.Armor.*;
import com.planet_ink.coffee_mud.Items.*;
import java.util.*;

public class StdRace implements Race
{
	protected String myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	protected String name="";
	protected int practicesAtFirstLevel=0;
	protected int trainsAtFirstLevel=0;	  
	public String ID()
	{
		return myID;
	}
	public String name()
	{
		return name;
	}
	public boolean playerSelectable(){return false;}
	
	/** some general statistics about such an item
	 * see class "Stats" for more information. */
	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		
	}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{
		
	}
	
	public boolean okAffect(Affect affect)
	{
		return true;
	}
	
	public void affect(Affect affect)
	{
	}
	
	public void level(MOB mob)
	{
	}
	public void newCharacter(MOB mob)
	{
		mob.setPractices(mob.getPractices()+practicesAtFirstLevel);
		mob.setTrains(mob.getTrains()+trainsAtFirstLevel);

	}
	public void setWeight(MOB mob)
	{
		if(mob.baseEnvStats().weight()>5) return;
		
		Random randomizer = new Random(System.currentTimeMillis());
		char gender = mob.baseCharStats().getGender();

		int weightModifier = Math.abs(randomizer.nextInt() % 10) + Math.abs(randomizer.nextInt() % 10) + Math.abs(randomizer.nextInt() % 10) + Math.abs(randomizer.nextInt() % 10) + 4; 		
		if (gender == 'M')
			mob.baseEnvStats().setWeight(130+weightModifier);
 		else
			mob.baseEnvStats().setWeight(105+weightModifier);
	}
}
