package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
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
		affectableStats.setStat(CharStats.SAVE_POISON,affectableStats.getStat(CharStats.SAVE_POISON)+100);
		affectableStats.setStat(CharStats.SAVE_MIND,affectableStats.getStat(CharStats.SAVE_MIND)+100);
		affectableStats.setStat(CharStats.SAVE_GAS,affectableStats.getStat(CharStats.SAVE_GAS)+100);
		affectableStats.setStat(CharStats.SAVE_PARALYSIS,affectableStats.getStat(CharStats.SAVE_PARALYSIS)+100);
		affectableStats.setStat(CharStats.SAVE_UNDEAD,affectableStats.getStat(CharStats.SAVE_UNDEAD)+100);
		affectableStats.setStat(CharStats.SAVE_DISEASE,affectableStats.getStat(CharStats.SAVE_DISEASE)+100);
	}
	public Vector myResources()
	{
		return resources;
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
