package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.Vector;

public class Lich extends Skeleton
{
	public String ID(){	return "Lich"; }
	public String name(){ return "Lich"; }
	public int shortestMale(){return 64;}
	public int shortestFemale(){return 60;}
	public int heightVariance(){return 12;}
	public int lightestWeight(){return 100;}
	public int weightVariance(){return 100;}
	public long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Undead";}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,0 ,0 };
	public int[] bodyMask(){return parts;}

	protected static Vector resources=new Vector();
	public boolean playerSelectable(){return false;}

	private int tickDown=10;

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
		if((tickID==Host.MOB_TICK)&&((--tickDown<=0)))
		{
			tickDown=10;
			Ability A=CMClass.getAbility("Spell_Fear");
			if(A!=null)
			{
				A.setMiscText("WEAK");
				A.invoke(myChar,null,true);
			}
		}
	}
}
