package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Mold extends StdRace
{
	public String ID(){	return "Mold"; }
	public String name(){ return "Mold"; }
	public int shortestMale(){return 1;}
	public int shortestFemale(){return 1;}
	public int heightVariance(){return 1;}
	public int lightestWeight(){return 5;}
	public int weightVariance(){return 1;}
	public long forbiddenWornBits(){return Integer.MAX_VALUE;}
	public String racialCategory(){return "Vegetation";}
	public boolean fertile(){return false;}
	public boolean uncharmable(){return true;}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,0 ,0 ,0 ,0 ,0 ,0 ,1 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 };
	public int[] bodyMask(){return parts;}

	private int[] agingChart={0,0,0,0,0,Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE};
	public int[] getAgingChart(){return agingChart;}
	
	protected static Vector resources=new Vector();
	public int availabilityCode(){return Area.THEME_FANTASY|Area.THEME_SKILLONLYMASK;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_GOLEM);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SPEAK|EnvStats.CAN_NOT_TASTE);
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(2*affected.envStats().level()));
		affectableStats.setDamage(affectableStats.damage()+(affected.envStats().level()/2));
	}
	public void affectCharState(MOB affectedMOB, CharState affectableState)
	{
		affectableState.setHitPoints(affectableState.getHitPoints()*4);
		affectableState.setHunger(999999);
		affectedMOB.curState().setHunger(affectableState.getHunger());
		affectedMOB.curState().setMana(0);
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		affectableStats.setStat(CharStats.GENDER,'N');
		affectableStats.setPermaStat(CharStats.INTELLIGENCE,1);
		affectableStats.setPermaStat(CharStats.WISDOM,1);
		affectableStats.setPermaStat(CharStats.CHARISMA,1);
		affectableStats.setPermaStat(CharStats.STRENGTH,1);
		affectableStats.setStat(CharStats.SAVE_POISON,affectableStats.getStat(CharStats.SAVE_POISON)+100);
		affectableStats.setStat(CharStats.SAVE_COLD,affectableStats.getStat(CharStats.SAVE_COLD)-100);
		affectableStats.setStat(CharStats.SAVE_MIND,affectableStats.getStat(CharStats.SAVE_MIND)+100);
		affectableStats.setStat(CharStats.SAVE_GAS,affectableStats.getStat(CharStats.SAVE_GAS)+100);
		affectableStats.setStat(CharStats.SAVE_PARALYSIS,affectableStats.getStat(CharStats.SAVE_PARALYSIS)+100);
		affectableStats.setStat(CharStats.SAVE_UNDEAD,affectableStats.getStat(CharStats.SAVE_UNDEAD)+100);
		affectableStats.setStat(CharStats.SAVE_DISEASE,affectableStats.getStat(CharStats.SAVE_DISEASE)+100);
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
			naturalWeapon.setName("a moldy surface");
			naturalWeapon.setRanges(0,5);
			naturalWeapon.setWeaponType(Weapon.TYPE_MELTING);
		}
		return naturalWeapon;
	}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((myHost!=null)
		&&(myHost instanceof MOB))
		{
			if(msg.amISource((MOB)myHost))
			{
				if(((msg.targetMinor()==CMMsg.TYP_LEAVE)
					||(msg.sourceMinor()==CMMsg.TYP_ADVANCE)
					||(msg.sourceMinor()==CMMsg.TYP_RETREAT)))
				{
					msg.source().tell("You can't really go anywhere -- you are a mold!");
					return false;
				}
			}
			else
			if(msg.amITarget(myHost)&&(msg.targetMinor()==CMMsg.TYP_DAMAGE))
			{
				switch(msg.targetMinor())
				{
				case CMMsg.TYP_FIRE:
					{
						((MOB)myHost).curState().setHitPoints(((MOB)myHost).curState().getHitPoints()+msg.value());
						msg.setValue(1);
					}
					break;
				case CMMsg.TYP_WEAPONATTACK:
					if((msg.tool()!=null)
				   &&(msg.tool() instanceof Weapon)
				   &&((((Weapon)msg.tool()).weaponClassification()==Weapon.TYPE_SLASHING)
					||(((Weapon)msg.tool()).weaponClassification()==Weapon.TYPE_PIERCING)
					||(((Weapon)msg.tool()).weaponClassification()==Weapon.TYPE_BASHING)))
						msg.setValue(1);
					break;
				case CMMsg.TYP_COLD:
					msg.setValue(msg.value()*2);
					break;
				}
			}
		}
		return super.okMessage(myHost,msg);
	}

	public String healthText(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name() + "^r is near destruction!^N";
		else
		if(pct<.20)
			return "^r" + mob.name() + "^r is massively scrapped and damaged.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is extremeley scrapped and damaged.^N";
		else
		if(pct<.40)
			return "^y" + mob.name() + "^y is very scrapped and damaged.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y is scrapped and damaged.^N";
		else
		if(pct<.60)
			return "^p" + mob.name() + "^p is scrapped and slightly damaged.^N";
		else
		if(pct<.70)
			return "^p" + mob.name() + "^p is showing numerous scrapes.^N";
		else
		if(pct<.80)
			return "^g" + mob.name() + "^g is showing some scrapes.^N";
		else
		if(pct<.90)
			return "^g" + mob.name() + "^g is showing small scrapes.^N";
		else
		if(pct<.99)
			return "^g" + mob.name() + "^g is no longer in perfect condition.^N";
		else
			return "^c" + mob.name() + "^c is in perfect condition.^N";
	}
}

