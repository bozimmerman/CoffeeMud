package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skeleton extends StdRace
{
	public String ID(){	return "Skeleton"; }
	public String name(){ return "Skeleton"; }
	public int shortestMale(){return 64;}
	public int shortestFemale(){return 60;}
	public int heightVariance(){return 12;}
	public int lightestWeight(){return 100;}
	public int weightVariance(){return 100;}
	public long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Undead";}
	public boolean fertile(){return false;}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,0 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,0 ,0 };
	public int[] bodyMask(){return parts;}

	protected static Vector resources=new Vector();
	public boolean playerSelectable(){return false;}

	public boolean okAffect(Environmental myHost, Affect msg)
	{
		if(!(myHost instanceof MOB)) return super.okAffect(myHost,msg);
		MOB myChar=(MOB)myHost;
		MOB mob=(MOB)myChar;
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
		else
		if((msg.amITarget(mob))
	    &&(Util.bset(msg.targetCode(),Affect.MASK_HURT))
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Weapon)
		&&((((Weapon)msg.tool()).weaponType()==Weapon.TYPE_PIERCING)
			||(((Weapon)msg.tool()).weaponType()==Weapon.TYPE_SLASHING))
		&&(!mob.amDead()))
		{
			int recovery=(int)Math.round(Util.div((msg.targetCode()-Affect.MASK_HURT),2.0));
			msg.modify(msg.source(),msg.target(),msg.tool(),msg.sourceCode(),msg.sourceMessage(),msg.targetCode()-recovery,msg.targetMessage(),msg.othersCode(),msg.othersMessage());
		}
		return super.okAffect(myChar,msg);
	}


	public void affectCharState(MOB affectedMOB, CharState affectableState)
	{
		super.affectCharState(affectedMOB, affectableState);
		affectableState.setHunger(999999);
		affectedMOB.curState().setHunger(affectableState.getHunger());
		affectableState.setThirst(999999);
		affectedMOB.curState().setThirst(affectableState.getThirst());
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
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_GOLEM);
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
					("knuckle bone",EnvResource.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
