package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2025 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "Dance_Swords";
	}

	private final static String localizedName = CMLib.lang().L("Swords");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS|CAN_ITEMS;
	}

	@Override
	protected String danceOf()
	{
		return L("@x1 Dance",name());
	}

	@Override
	protected boolean skipStandardDanceInvoke()
	{
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((!super.okMessage(myHost,msg))
		||(affected==null))
		{
			if(affected instanceof MOB)
				unDanceMe((MOB)affected,null, false);
			else
				unInvoke();
			return false;
		}
		else
		if(msg.amITarget(affected))
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GET:
			case CMMsg.TYP_PUSH:
			case CMMsg.TYP_PULL:
			case CMMsg.TYP_REMOVE:
				if(affected instanceof MOB)
					unDanceMe((MOB)affected,null, false);
				else
					unInvoke();
				break;
			}
		return true;
	}

	@Override
	public void unInvoke()
	{
		final Environmental E=affected;
		super.unInvoke();
		if((E!=null)
		&&(E instanceof Item)
		&&(((Item)E).owner()!=null)
		&&(((Item)E).owner() instanceof Room)
		&&(canBeUninvoked()))
		{
			((Room)((Item)E).owner()).showHappens(CMMsg.MSG_OK_ACTION,L("@x1 stops dancing!",E.name()));
		}
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected instanceof Item)
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_FLYING);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((affected instanceof MOB)&&(!super.tick(ticking,tickID)))
			return false;
		final MOB M=invoker();
		if(affected==M)
		{
			Weapon sword=null;
			int num=0;
			for(int i=0;i<M.location().numItems();i++)
			{
				final Item I=M.location().getItem(i);
				if((I!=null)
				&&(I instanceof Weapon)
				&&(((Weapon)I).weaponClassification()==Weapon.CLASS_SWORD))
				{
					if((!CMLib.flags().isFlying(I))&&(I.fetchEffect(ID())==null))
					{
						sword=(Weapon)I;
						break;
					}
					else
					{
						num++;
					}
				}
			}
			if(sword==null)
				return true;
			final int max=3+(super.getXLEVELLevel(invoker())/2);
			if(num>max)
				return true;
			final Dance newOne=(Dance)this.copyOf();
			newOne.invokerManaCost=-1;
			newOne.startTickDown(M,sword,99999);
			return true;
		}
		else
		if((affected instanceof Weapon)
		&&(((Weapon)affected).owner() instanceof Room)
		&&(M!=null)
		&&(M.location().isContent((Weapon)affected))
		&&(M.fetchEffect(ID())!=null)
		&&(CMLib.flags().isAliveAwakeMobile(M,true)))
		{
			final MOB victiM=M.getVictim();
			if(M.isInCombat())
			{
				final boolean isHit=(CMLib.combat().rollToHit(CMLib.combat().adjustedAttackBonus(M,victiM)+((Weapon)affected).phyStats().attackAdjustment(), CMLib.combat().adjustedArmor(victiM), 0));
				if((!isHit)||(!(affected instanceof Weapon)))
					M.location().show(M,victiM,affected,CMMsg.MSG_OK_ACTION,L("<O-NAME> attacks <T-NAME> and misses!"));
				else
				{
					final double pct = super.statBonusPct();
					final int dmg = (int)Math.round(CMath.mul(5,pct));
					final int bonusDamage=(affected.phyStats().damage()+dmg+getXLEVELLevel(M))-M.phyStats().damage();
					final int damage=CMLib.combat().adjustedDamage(M, (Weapon)affected, victiM, bonusDamage,false, false);
					CMLib.combat().postDamage(M,victiM,affected,damage,CMMsg.MASK_ALWAYS|CMMsg.MASK_MALICIOUS|CMMsg.TYP_WEAPONATTACK,
											((Weapon)affected).weaponDamageType(),L("@x1 attacks and <DAMAGE> <T-NAME>!",affected.name()));
				}
			}
			else
			if(CMLib.dice().rollPercentage()>75)
			switch(CMLib.dice().roll(1,5,0))
			{
			case 1:
				M.location().showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 twiches a bit.",affected.name()));
				break;
			case 2:
				M.location().showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 is looking for trouble.",affected.name()));
				break;
			case 3:
				M.location().showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 practices its moves.",affected.name()));
				break;
			case 4:
				M.location().showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 makes a few fake attacks.",affected.name()));
				break;
			case 5:
				M.location().showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 dances around.",affected.name()));
				break;
			}
		}
		else
		{
			if(affected instanceof MOB)
				unDanceMe((MOB)affected,null, false);
			else
				unInvoke();
			return false;
		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		timeOut=0;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if((!auto)&&(!CMLib.flags().isAliveAwakeMobile(mob,false)))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		final int newDepth = this.calculateNewSongDepth(mob);
		final boolean redance = mob.fetchEffect(ID())!=null;
		unDanceAll(mob,null,false,false); // because ALWAYS removing myself, depth needs pre-calculating.
		if(success)
		{
			invoker=mob;
			originRoom=mob.location();
			final int oldDepth = this.danceDepth;
			commonRoomSet=getInvokerScopeRoomSet(newDepth);
			this.danceDepth = newDepth;
			String str=auto?L("^SThe @x1 begins!^?",danceOf()):L("^S<S-NAME> begin(s) to dance the @x1.^?",danceOf());
			if((!auto)&&(redance))
			{
				if(newDepth > oldDepth)
					str=L("^S<S-NAME> extend(s) the @x1`s range.^?",danceOf());
				else
					str=L("^S<S-NAME> start(s) the @x1 over again.^?",danceOf());
			}

			for(int v=0;v<commonRoomSet.size();v++)
			{
				final Room R=commonRoomSet.get(v);
				final String msgStr=getCorrectMsgString(R,str,v);
				final CMMsg msg=CMClass.getMsg(mob,null,this,somaticCastCode(mob,null,auto),msgStr);
				if(R.okMessage(mob,msg))
				{
					if(originRoom==R)
						R.send(mob,msg);
					else
						R.sendOthers(mob,msg);
					invoker=mob;
					final Dance newOne=(Dance)this.copyOf();
					newOne.invokerManaCost=-1;

					final MOB follower=mob;

					// malicious dances must not affect the invoker!
					int affectType=CMMsg.MSG_CAST_SOMANTIC_SPELL;
					if(auto)
						affectType=affectType|CMMsg.MASK_ALWAYS;

					final Dance effectD = (Dance)follower.fetchEffect(this.ID());
					if(effectD!=null)
						effectD.danceDepth = this.danceDepth;
					else
					if(CMLib.flags().canBeSeenBy(invoker,follower))
					{
						final CMMsg msg2=CMClass.getMsg(mob,follower,this,affectType,null);
						if(R.okMessage(mob,msg2))
						{
							follower.location().send(follower,msg2);
							if(msg2.value()<=0)
								follower.addEffect(newOne);
						}
					}
					R.recoverRoomStats();
				}
			}
		}
		else
			mob.location().show(mob,null,CMMsg.MSG_NOISE,L("<S-NAME> make(s) a false step."));

		return success;
	}
}
