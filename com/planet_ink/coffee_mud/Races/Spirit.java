package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spirit extends StdRace
{
	public String ID(){	return "Spirit"; }
	public String name(){ return "Spirit"; }
	public int shortestMale(){return 64;}
	public int shortestFemale(){return 60;}
	public int heightVariance(){return 12;}
	public int lightestWeight(){return 1;}
	public int weightVariance(){return 0;}
	public long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Undead";}
	public boolean fertile(){return false;}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,0 ,0 };
	public int[] bodyMask(){return parts;}

	protected static Vector resources=new Vector();
	public boolean playerSelectable(){return false;}

	public Weapon funHumanoidWeapon()
	{
		if(naturalWeaponChoices==null)
		{
			naturalWeaponChoices=new Vector();
			for(int i=1;i<11;i++)
			{
				naturalWeapon=CMClass.getWeapon("StdWeapon");
				switch(i)
				{
					case 1:
					case 2:
					case 3:
					naturalWeapon.setName("an invisible punch");
					naturalWeapon.setWeaponType(Weapon.TYPE_BURSTING);
					break;
					case 4:
					naturalWeapon.setName("an incorporal bite");
					naturalWeapon.setWeaponType(Weapon.TYPE_BURSTING);
					break;
					case 5:
					naturalWeapon.setName("a fading elbow");
					naturalWeapon.setWeaponType(Weapon.TYPE_BURSTING);
					break;
					case 6:
					naturalWeapon.setName("a translucent backhand");
					naturalWeapon.setWeaponType(Weapon.TYPE_BURSTING);
					break;
					case 7:
					naturalWeapon.setName("a strong ghostly jab");
					naturalWeapon.setWeaponType(Weapon.TYPE_BURSTING);
					break;
					case 8:
					naturalWeapon.setName("a ghostly punch");
					naturalWeapon.setWeaponType(Weapon.TYPE_BURSTING);
					break;
					case 9:
					naturalWeapon.setName("a translucent knee");
					naturalWeapon.setWeaponType(Weapon.TYPE_BURSTING);
					break;
				}
				naturalWeaponChoices.addElement(naturalWeapon);
			}
		}
		return (Weapon)naturalWeaponChoices.elementAt(Dice.roll(1,naturalWeaponChoices.size(),-1));
	}
	public Weapon myNaturalWeapon()
	{ return funHumanoidWeapon();	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_GOLEM);
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.SAVE_POISON,affectableStats.getStat(CharStats.SAVE_POISON)+100);
		affectableStats.setStat(CharStats.SAVE_MIND,affectableStats.getStat(CharStats.SAVE_MIND)+100);
		affectableStats.setStat(CharStats.SAVE_GAS,affectableStats.getStat(CharStats.SAVE_GAS)+100);
		affectableStats.setStat(CharStats.SAVE_PARALYSIS,affectableStats.getStat(CharStats.SAVE_PARALYSIS)+100);
		affectableStats.setStat(CharStats.SAVE_UNDEAD,affectableStats.getStat(CharStats.SAVE_UNDEAD)+100);
		affectableStats.setStat(CharStats.SAVE_DISEASE,affectableStats.getStat(CharStats.SAVE_DISEASE)+100);
	}

	public boolean okAffect(Environmental myHost, Affect msg)
	{
		if((myHost!=null)&&(myHost instanceof MOB))
		{
			MOB mob=(MOB)myHost;
			if(msg.amITarget(mob)&&Util.bset(msg.targetCode(),Affect.MASK_HEAL))
			{
				int amount=msg.targetCode()-Affect.MASK_HEAL;
				if((amount>0)
				&&(msg.tool()!=null)
				&&(msg.tool() instanceof Ability)
				&&(Util.bset(((Ability)msg.tool()).flags(),Ability.FLAG_HEALING|Ability.FLAG_HOLY))
				&&(!Util.bset(((Ability)msg.tool()).flags(),Ability.FLAG_UNHOLY)))
				{
					ExternalPlay.postDamage(msg.source(),mob,msg.tool(),amount,Affect.MASK_GENERAL|Affect.TYP_ACID,Weapon.TYPE_BURNING,"The healing magic from <S-NAME> seems to <DAMAGE> <T-NAMESELF>.");
					if((mob.getVictim()==null)&&(mob!=msg.source())&&(mob.isMonster()))
						mob.setVictim(msg.source());
				}
				return false;
			}
			else
			if((msg.amITarget(mob)&&Util.bset(msg.targetCode(),Affect.MASK_HURT))
			&&(msg.tool()!=null)
			&&(msg.tool() instanceof Ability)
			&&(Util.bset(((Ability)msg.tool()).flags(),Ability.FLAG_UNHOLY))
			&&(!Util.bset(((Ability)msg.tool()).flags(),Ability.FLAG_HOLY)))
			{
				int amount=msg.targetCode()-Affect.MASK_HURT;
				if(amount>0)
				{
					ExternalPlay.postHealing(msg.source(),mob,msg.tool(),Affect.MASK_GENERAL|Affect.TYP_CAST_SPELL,amount,"The harming magic heals <T-NAMESELF>.");
					return false;
				}
			}
		}
		return super.okAffect(myHost,msg);
	}
	
	public String healthText(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name() + "^r is near banishment!^N";
		else
		if(pct<.20)
			return "^r" + mob.name() + "^r is massively weak and faded.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is very faded.^N";
		else
		if(pct<.40)
			return "^y" + mob.name() + "^y is somewhat faded.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y is very weak and slightly faded.^N";
		else
		if(pct<.60)
			return "^p" + mob.name() + "^p has lost stability and is weak.^N";
		else
		if(pct<.70)
			return "^p" + mob.name() + "^p is unstable and slightly weak.^N";
		else
		if(pct<.80)
			return "^g" + mob.name() + "^g is unbalanced and unstable.^N";
		else
		if(pct<.90)
			return "^g" + mob.name() + "^g is in somewhat unbalanced.^N";
		else
		if(pct<.99)
			return "^g" + mob.name() + "^g is no longer in perfect condition.^N";
		else
			return "^c" + mob.name() + "^c is in perfect condition.^N";
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" essence",EnvResource.RESOURCE_BLOOD));
			}
		}
		return resources;
	}
}

