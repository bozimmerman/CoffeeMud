package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Undead extends StdRace
{
	public String ID(){	return "Undead"; }
	public String name(){ return "Undead"; }
	public int shortestMale(){return 64;}
	public int shortestFemale(){return 60;}
	public int heightVariance(){return 12;}
	public int lightestWeight(){return 100;}
	public int weightVariance(){return 100;}
	public long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Undead";}
	public boolean fertile(){return false;}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,0 ,0 };
	public int[] bodyMask(){return parts;}

	protected static Vector resources=new Vector();
	
	public boolean playerSelectable(){return false;}

	public void affectCharState(MOB affectedMOB, CharState affectableState)
	{
		super.affectCharState(affectedMOB, affectableState);
		affectableState.setHunger(999999);
		affectedMOB.curState().setHunger(affectableState.getHunger());
		affectableState.setThirst(999999);
		affectedMOB.curState().setThirst(affectableState.getThirst());
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_GOLEM);
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
			else
			if((msg.amITarget(mob))
			&&(Util.bset(msg.targetCode(),Affect.MASK_MALICIOUS)
				||Util.bset(msg.targetCode(),Affect.MASK_HURT))
			&&((msg.targetMinor()==Affect.TYP_DISEASE)
				||(msg.targetMinor()==Affect.TYP_GAS)
				||(msg.targetMinor()==Affect.TYP_MIND)
				||(msg.targetMinor()==Affect.TYP_PARALYZE)
				||(msg.targetMinor()==Affect.TYP_POISON)
				||(msg.targetMinor()==Affect.TYP_UNDEAD))
			&&(!mob.amDead()))
			{
				String immunityName="certain";
				if(msg.tool()!=null)
					immunityName=msg.tool().name();
				if(mob!=msg.source())
					mob.location().show(mob,msg.source(),Affect.MSG_OK_VISUAL,"<S-NAME> seems immune to "+immunityName+" attacks from <T-NAME>.");
				else
					mob.location().show(mob,msg.source(),Affect.MSG_OK_VISUAL,"<S-NAME> seems immune to "+immunityName+".");
				return false;
			}
		}
		return super.okAffect(myHost,msg);
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
	public String healthText(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name() + "^r is near destruction!^N";
		else
		if(pct<.20)
			return "^r" + mob.name() + "^r is massively broken and damaged.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is very damaged.^N";
		else
		if(pct<.40)
			return "^y" + mob.name() + "^y is somewhat damaged.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y is very weak and slightly damaged.^N";
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
				("some "+name().toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
			}
		}
		return resources;
	}
}

