package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Insect extends StdRace
{
	public String ID(){	return "Insect"; }
	public String name(){ return "Insect"; }
	protected int shortestMale(){return 2;}
	protected int shortestFemale(){return 2;}
	protected int heightVariance(){return 0;}
	protected int lightestWeight(){return 1;}
	protected int weightVariance(){return 0;}
	protected long forbiddenWornBits(){return Integer.MAX_VALUE;}
	public String racialCategory(){return "Insect";}
	
	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={2 ,2 ,0 ,1 ,1 ,0 ,0 ,1 ,2 ,2 ,0 ,0 ,1 ,0 ,0 ,0 };
	public int[] bodyMask(){return parts;}
	
	protected static Vector resources=new Vector();
	public boolean playerSelectable(){return false;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SNEAKING);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_GOLEM);
	}
	public void affect(Environmental myHost, Affect msg)
	{
		MOB mob=(MOB)myHost;
		if(msg.amISource(mob)
		&&(!msg.amITarget(mob))
		&&(Util.bset(msg.targetCode(),Affect.MASK_HURT))
		&&(msg.target()!=null)
		&&(msg.target() instanceof MOB)
		&&(mob.fetchWieldedItem()==null)
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Weapon)
		&&(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_NATURAL)
		&&(!((MOB)msg.target()).isMonster())
		&&(((msg.targetCode()-Affect.MASK_HURT)>(((MOB)msg.target()).maxState().getHitPoints()/20))))
		{
			Ability A=CMClass.getAbility("Disease_Lyme");
			if((A!=null)&&(msg.target().fetchAffect(A.ID())==null))
				A.invoke(mob,(MOB)msg.target(),true);
		}
		super.affect(myHost,msg);
	}
	
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STRENGTH,3);
		affectableStats.setStat(CharStats.DEXTERITY,3);
		affectableStats.setStat(CharStats.INTELLIGENCE,1);
		affectableStats.setStat(CharStats.SAVE_POISON,affectableStats.getStat(CharStats.SAVE_POISON)+100);
	}
	public String arriveStr()
	{
		return "creeps in";
	}
	public String leaveStr()
	{
		return "creeps";
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("a nasty maw");
			naturalWeapon.setWeaponType(Weapon.TYPE_NATURAL);
		}
		return naturalWeapon;
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" guts",EnvResource.RESOURCE_MEAT));
			}
		}
		return resources;
	}
}