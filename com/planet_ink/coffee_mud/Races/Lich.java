package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.Vector;

public class Lich extends Skeleton
{
	public String ID(){	return "Lich"; }
	public String name(){ return "Lich"; }
	protected int shortestMale(){return 64;}
	protected int shortestFemale(){return 60;}
	protected int heightVariance(){return 12;}
	protected int lightestWeight(){return 100;}
	protected int weightVariance(){return 100;}
	protected long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Undead";}
	
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
