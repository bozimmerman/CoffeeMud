package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.Vector;

public class Lich extends Skeleton
{
	public String ID(){	return "Lich"; }
	public String name(){ return "Lich"; }

	protected static Vector resources=new Vector();

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.CONSTITUTION,affectableStats.getStat(CharStats.CONSTITUTION)-4);
		affectableStats.setStat(CharStats.CHARISMA,affectableStats.getStat(CharStats.CHARISMA)+6);
	}
	public Vector myResources()
	{
		return resources;
	}

	public void tick(MOB myChar, int tickID)
	{
		super.tick(myChar,tickID);
		if((tickID==Host.TICK_MOB)
		&&(Dice.rollPercentage()<10))
		{
			Ability A=CMClass.getAbility("Spell_Fear");
			if(A!=null)
			{
				A.setMiscText("WEAK");
				A.invoke(myChar,null,true);
			}
		}
	}
}
