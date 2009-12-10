package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;


/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Dance_Swords extends Dance
{
	public String ID() { return "Dance_Swords"; }
	public String name(){ return "Swords";}
	public int abstractQuality(){ return Ability.QUALITY_OK_OTHERS;}
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
			((Room)((Item)E).owner()).showHappens(CMMsg.MSG_OK_ACTION,E.name()+" stops dancing!");
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
		if((affected instanceof MOB)&&(!super.tick(ticking,tickID)))
			return false;
		MOB M=invoker();
		if(affected==M)
		{
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
			newOne.startTickDown(M,sword,99999);
			return true;
		}
		else
		if((affected!=null)
		&&(affected instanceof Item)
		&&(((Item)affected).owner()!=null)
		&&(((Item)affected).owner() instanceof Room)
		&&(M!=null)
		&&(M.location().isContent((Item)affected))
		&&(M.fetchEffect(ID())!=null)
		&&(CMLib.flags().aliveAwakeMobile(M,true)))
		{
			MOB victiM=M.getVictim();
			if(M.isInCombat())
			{
				boolean isHit=(CMLib.combat().rollToHit(CMLib.combat().adjustedAttackBonus(M,victiM)+((Item)affected).envStats().attackAdjustment(), CMLib.combat().adjustedArmor(victiM), 0));
				if((!isHit)||(!(affected instanceof Weapon)))
					M.location().show(M,victiM,affected,CMMsg.MSG_OK_ACTION,"<O-NAME> attacks <T-NAME> and misses!");
				else
					CMLib.combat().postDamage(M,victiM,affected,
											CMLib.dice().roll(1,affected.envStats().damage(),5+getXLEVELLevel(M)),
											CMMsg.MASK_ALWAYS|CMMsg.TYP_WEAPONATTACK,
											((Weapon)affected).weaponType(),affected.name()+" attacks and <DAMAGE> <T-NAME>!");
			}
			else
			if(CMLib.dice().rollPercentage()>75)
			switch(CMLib.dice().roll(1,5,0))
			{
			case 1:
				M.location().showHappens(CMMsg.MSG_OK_VISUAL,affected.name()+" twiches a bit.");
				break;
			case 2:
				M.location().showHappens(CMMsg.MSG_OK_VISUAL,affected.name()+" is looking for trouble.");
				break;
			case 3:
				M.location().showHappens(CMMsg.MSG_OK_VISUAL,affected.name()+" practices its moves.");
				break;
			case 4:
				M.location().showHappens(CMMsg.MSG_OK_VISUAL,affected.name()+" makes a few fake attacks.");
				break;
			case 5:
				M.location().showHappens(CMMsg.MSG_OK_VISUAL,affected.name()+" dances around.");
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

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		steadyDown=-1;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if((!auto)&&(!CMLib.flags().aliveAwakeMobile(mob,false)))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		undance(mob,null,true);
		if(success)
		{
			invoker=mob;
			originRoom=mob.location();
			commonRoomSet=getInvokerScopeRoomSet(null);
			String str=auto?"^SThe "+danceOf()+" begins!^?":"^S<S-NAME> begin(s) to dance the "+danceOf()+".^?";
			if((!auto)&&(mob.fetchEffect(this.ID())!=null))
				str="^S<S-NAME> start(s) the "+danceOf()+" over again.^?";

			for(int v=0;v<commonRoomSet.size();v++)
			{
				Room R=(Room)commonRoomSet.elementAt(v);
				String msgStr=getCorrectMsgString(R,str,v);
				CMMsg msg=CMClass.getMsg(mob,null,this,somanticCastCode(mob,null,auto),msgStr);
				if(R.okMessage(mob,msg))
				{
					if(originRoom==R)
						R.send(mob,msg);
					else
						R.sendOthers(mob,msg);
					invoker=mob;
					Dance newOne=(Dance)this.copyOf();
					newOne.invokerManaCost=-1;
	
					MOB follower=mob;
	
					// malicious dances must not affect the invoker!
					int affectType=CMMsg.MSG_CAST_SOMANTIC_SPELL;
					if(auto) affectType=affectType|CMMsg.MASK_ALWAYS;
	
					if((CMLib.flags().canBeSeenBy(invoker,follower)&&(follower.fetchEffect(this.ID())==null)))
					{
						CMMsg msg2=CMClass.getMsg(mob,follower,this,affectType,null);
						if(R.okMessage(mob,msg2))
						{
							follower.location().send(follower,msg2);
							if((msg2.value()<=0)&&(follower.fetchEffect(newOne.ID())==null))
								follower.addEffect(newOne);
						}
					}
					R.recoverRoomStats();
				}
			}
		}
		else
			mob.location().show(mob,null,CMMsg.MSG_NOISE,"<S-NAME> make(s) a false step.");

		return success;
	}
}
