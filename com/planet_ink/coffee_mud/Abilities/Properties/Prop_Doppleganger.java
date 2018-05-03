package com.planet_ink.coffee_mud.Abilities.Properties;
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
   Copyright 2003-2018 Bo Zimmerman

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
public class Prop_Doppleganger extends Property
{
	@Override
	public String ID()
	{
		return "Prop_Doppleganger";
	}

	@Override
	public String name()
	{
		return "Doppleganger";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS|Ability.CAN_ITEMS;
	}
	//protected boolean lastLevelChangers=true;
	protected Physical lastOwner=null;
	private int maxLevel=Integer.MAX_VALUE;
	private int minLevel=Integer.MIN_VALUE;
	protected int lastLevel=Integer.MIN_VALUE;
	protected int levelAdd=0;
	protected double levelPct=1.0;
	protected boolean matchPlayersOnly=false;
	protected boolean matchPlayersFollowersOnly=false;
	protected int asMaterial=-1;

	@Override
	public long flags()
	{
		return Ability.FLAG_ADJUSTER;
	}

	@Override
	public String accountForYourself()
	{
		return "Level Changer";
	}

	@Override
	public void setMiscText(String text)
	{
		super.setMiscText(text);
		levelAdd=0;
		levelPct=1.0;
		asMaterial=-1;
		maxLevel=Integer.MAX_VALUE;
		minLevel=Integer.MIN_VALUE;
		if(CMath.isInteger(text))
			levelAdd=CMath.s_int(text);
		else
		if(CMath.isPct(text))
			levelPct=CMath.s_pct(text);
		else
		{
			maxLevel=CMParms.getParmInt(text,"MAX",Integer.MAX_VALUE);
			minLevel=CMParms.getParmInt(text,"MIN",Integer.MIN_VALUE);
			levelAdd=CMParms.getParmInt(text, "LEVELADD", 0);
			levelPct=CMParms.getParmInt(text, "LEVELPCT", 100)/100.0;
			matchPlayersFollowersOnly=CMParms.getParmBool(text, "PLAYERSNFOLS", false);
			matchPlayersOnly=CMParms.getParmBool(text, "PLAYERSONLY", false);
			final String asMat = CMParms.getParmStr(text, "ASMATERIAL", "");
			if((asMat!=null)&&(asMat.trim().length()>0))
				asMaterial = RawMaterial.CODES.FIND_IgnoreCase(asMat);
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((affected instanceof Item)
		&&((((Item)affected).owner()!=lastOwner)||((lastOwner!=null)&&(lastOwner.phyStats().level()!=lastLevel)))
		&&(((Item)affected).owner() instanceof MOB))
		{
			final Item I=(Item)affected;
			lastOwner=I.owner();
			lastLevel=lastOwner.phyStats().level();
			int level=(int)Math.round(CMath.mul(((MOB)lastOwner).phyStats().level(),levelPct))+levelAdd;
			if(level<minLevel)
				level=minLevel;
			if(level>maxLevel)
				level=maxLevel;
			I.basePhyStats().setLevel(level);
			I.phyStats().setLevel(level);
			int oldMaterial=I.material();
			if(asMaterial != -1)
				I.setMaterial(asMaterial);
			CMLib.itemBuilder().balanceItemByLevel(I);
			I.setMaterial(oldMaterial);
			level=((MOB)lastOwner).phyStats().level();
			if(level<minLevel)
				level=minLevel;
			if(level>maxLevel)
				level=maxLevel;
			I.basePhyStats().setLevel(level);
			I.phyStats().setLevel(level);
			lastOwner.recoverPhyStats();
			final Room R=((MOB)lastOwner).location();
			if(R!=null)
				R.recoverRoomStats();
		}
		super.executeMsg(myHost,msg);
	}

	public boolean qualifies(MOB mob, Room R)
	{
		if((mob==affected)||(mob==null))
			return false;
		if(mob.fetchEffect(ID())!=null)
			return false;
		if(mob.isMonster())
		{
			if(matchPlayersFollowersOnly)
			{
				final MOB folM=mob.amUltimatelyFollowing();
				return (folM!=null)&& (!folM.isMonster());
			}
			return (!matchPlayersOnly);
		}

		if((!CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.CMDMOBS))
		&&(!CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.CMDROOMS))
		&&(!CMLib.flags().isUnattackable(mob)))
			return true;
		return false;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected instanceof MOB)
		&&(((msg.target() instanceof Room)&&(msg.sourceMinor()==CMMsg.TYP_ENTER))
		   ||(msg.sourceMinor()==CMMsg.TYP_LIFE)))
		//&&(lastLevelChangers))
		{
			//lastLevelChangers=false;
			final MOB mob=(MOB)affected;
			final Room R=(msg.target() instanceof Room)?((Room)msg.target()):msg.source().location();
			if((R!=null)
			&&(CMLib.flags().isAliveAwakeMobile(mob,true))
			&&(mob.curState().getHitPoints()>=mob.maxState().getHitPoints()))
			{
				int total=0;
				int num=0;
				final MOB victim=mob.getVictim();
				if(qualifies(victim,R))
				{
					total+=victim.phyStats().level();
					num++;
				}
				final MOB entrant=msg.source();
				if(qualifies(entrant,R))
				{
					total+=entrant.phyStats().level();
					num++;
				}
				for(int i=0;i<R.numInhabitants();i++)
				{
					final MOB M=R.fetchInhabitant(i);
					if((M!=null)
					&&(M!=mob)
					&&((M.getVictim()==mob)||(victim==null))
					&&((M!=victim)&&(M!=entrant))
					&&(qualifies(M,R)))
					{
						total+=M.phyStats().level();
						num++;
					}
				}
				if(num>0)
				{
					int level=(int)Math.round(CMath.mul(CMath.div(total,num),levelPct))+levelAdd;
					if(level<minLevel)
						level=minLevel;
					if(level>maxLevel)
						level=maxLevel;
					if(level!=mob.basePhyStats().level())
					{
						mob.basePhyStats().setLevel(level);
						mob.basePhyStats().setArmor(CMLib.leveler().getLevelMOBArmor(mob));
						mob.basePhyStats().setAttackAdjustment(CMLib.leveler().getLevelAttack(mob));
						mob.basePhyStats().setDamage(CMLib.leveler().getLevelMOBDamage(mob));
						mob.basePhyStats().setSpeed(1.0+(CMath.div(level,100)*4.0));
						mob.baseState().setHitPoints(CMLib.leveler().getPlayerHitPoints(mob));
						mob.baseState().setMana(CMLib.leveler().getLevelMana(mob));
						mob.baseState().setMovement(CMLib.leveler().getLevelMove(mob));
						mob.recoverPhyStats();
						mob.recoverCharStats();
						mob.recoverMaxState();
						mob.resetToMaxState();
					}
				}
			}
		}
		return super.okMessage(myHost,msg);
	}
}
