package com.planet_ink.coffee_mud.Abilities.Songs;

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
public class Dance_Swords extends Dance
{
	public String ID() { return "Dance_Swords"; }
	public String name(){ return "Swords";}
	public int quality(){ return INDIFFERENT;}
	protected int canAffectCode(){return CAN_MOBS|CAN_ITEMS;}
	protected String danceOf(){return name()+" Dance";}
	protected boolean skipStandardDanceInvoke(){return true;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((!super.okMessage(myHost,msg))
		||(affected==null))
		{
			if(affected instanceof MOB)
				undance((MOB)affected,null,false);
			else
				unInvoke();
			return false;
		}
		else
		if(msg.amITarget(affected))
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GET:
			case CMMsg.TYP_REMOVE:
				if(affected instanceof MOB)
					undance((MOB)affected,null,false);
				else
					unInvoke();
				break;
			}
		return true;
	}

	public void unInvoke()
	{
		Environmental E=affected;
		super.unInvoke();
		if((E!=null)
		&&(E instanceof Item)
		&&(((Item)E).owner()!=null)
		&&(((Item)E).owner() instanceof Room))
		{
			((Room)((Item)E).owner()).showHappens(CMMsg.MSG_OK_ACTION,E.name()+" vanishes!");
			((Item)E).destroy();
		}
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected instanceof Item)
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_FLYING);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(affected==invoker())
		{
			MOB M=invoker();
			Weapon sword=null;
			for(int i=0;i<M.location().numItems();i++)
			{
				Item I=M.location().fetchItem(i);
				if((I!=null)
				&&(I instanceof Weapon)
				&&(((Weapon)I).weaponClassification()==Weapon.CLASS_SWORD)
				&&(I.fetchEffect(ID())==null))
				{
					sword=(Weapon)I;
					break;
				}
			}
			if(sword==null) return true;
			Dance newOne=(Dance)this.copyOf();
			newOne.invokerManaCost=-1;
			newOne.startTickDown(invoker(),sword,99999);
			return true;
		}
		else
		if((affected!=null)
		&&(affected instanceof Item)
		&&(((Item)affected).owner()!=null)
		&&(((Item)affected).owner() instanceof Room)
		&&(invoker()!=null)
		&&(invoker().location().isContent((Item)affected))
		&&(invoker().fetchEffect(ID())!=null)
		&&(Sense.aliveAwakeMobile(invoker(),true)))
		{
			if(invoker().isInCombat())
			{
				boolean isHit=(Dice.normalizeAndRollLess(invoker().adjustedAttackBonus(invoker().getVictim())
																   +((Item)affected).envStats().attackAdjustment()
																   +invoker().getVictim().adjustedArmor()));
				if((!isHit)||(!(affected instanceof Weapon)))
					invoker().location().show(invoker(),invoker().getVictim(),affected,CMMsg.MSG_OK_ACTION,"<O-NAME> attacks <T-NAME> and misses!");
				else
					MUDFight.postDamage(invoker(),invoker().getVictim(),(Item)affected,
											Dice.roll(1,affected.envStats().damage(),5),
											CMMsg.MASK_GENERAL|CMMsg.TYP_WEAPONATTACK,
											((Weapon)affected).weaponType(),affected.name()+" attacks and <DAMAGE> <T-NAME>!");
			}
			else
			if(Dice.rollPercentage()>75)
			switch(Dice.roll(1,5,0))
			{
			case 1:
				invoker().location().showHappens(CMMsg.MSG_OK_VISUAL,affected.name()+" twiches a bit.");
				break;
			case 2:
				invoker().location().showHappens(CMMsg.MSG_OK_VISUAL,affected.name()+" is looking for trouble.");
				break;
			case 3:
				invoker().location().showHappens(CMMsg.MSG_OK_VISUAL,affected.name()+" practices its moves.");
				break;
			case 4:
				invoker().location().showHappens(CMMsg.MSG_OK_VISUAL,affected.name()+" makes a few fake attacks.");
				break;
			case 5:
				invoker().location().showHappens(CMMsg.MSG_OK_VISUAL,affected.name()+" dances around.");
				break;
			}
		}
		else
		{
			if(affected instanceof MOB)
				undance((MOB)affected,null,false);
			else
				unInvoke();
			return false;
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if((!auto)&&(!Sense.aliveAwakeMobile(mob,false)))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		undance(mob,null,true);
		if(success)
		{
			String str=auto?"^SThe "+danceOf()+" begins!^?":"^S<S-NAME> begin(s) to dance the "+danceOf()+".^?";
			if((!auto)&&(mob.fetchEffect(this.ID())!=null))
				str="^S<S-NAME> start(s) the "+danceOf()+" over again.^?";

			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),str);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				Dance newOne=(Dance)this.copyOf();
				newOne.invokerManaCost=-1;

				MOB follower=mob;

				// malicious dances must not affect the invoker!
				int affectType=CMMsg.MSG_CAST_SOMANTIC_SPELL;
				if(auto) affectType=affectType|CMMsg.MASK_GENERAL;

				if((Sense.canBeSeenBy(invoker,follower)&&(follower.fetchEffect(this.ID())==null)))
				{
					FullMsg msg2=new FullMsg(mob,follower,this,affectType,null);
					if(mob.location().okMessage(mob,msg2))
					{
						follower.location().send(follower,msg2);
						if((msg2.value()<=0)&&(follower.fetchEffect(newOne.ID())==null))
							follower.addEffect(newOne);
					}
				}
				mob.location().recoverRoomStats();
			}
		}
		else
			mob.location().show(mob,null,CMMsg.MSG_NOISE,"<S-NAME> make(s) a false step.");

		return success;
	}
}
