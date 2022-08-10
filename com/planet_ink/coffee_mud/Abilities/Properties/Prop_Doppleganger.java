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
   Copyright 2003-2022 Bo Zimmerman

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
	protected Physical	lastOwner					= null;
	private int			maxLevel					= Integer.MAX_VALUE;
	private int			minLevel					= Integer.MIN_VALUE;
	protected int		lastLevel					= Integer.MIN_VALUE;
	protected int		levelAdd					= 0;
	protected double	levelPct					= 1.0;
	protected int		levelCode					= 0;
	protected boolean	diffGrp						= false;
	protected int		diffAdd						= 0;
	protected double	savePct						= 0;
	protected double	diffPct						= 1.0;
	protected int		saveAdd						= 0;
	protected boolean	groupMultiplier				= false;
	protected boolean	matchPlayersOnly			= false;
	protected boolean	matchPlayersFollowersOnly	= false;
	protected int		asMaterial					= -1;

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
	public void setMiscText(final String text)
	{
		super.setMiscText(text);
		levelAdd=0;
		diffAdd=0;
		levelPct=1.0;
		diffPct=1.0;
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
			levelCode=CMParms.getParmInt(text, "LEVELCODE", 0);
			levelAdd=CMParms.getParmInt(text, "LEVELADD", 0);
			levelPct=CMath.s_pct(CMParms.getParmStr(text, "LEVELPCT", "100"));
			diffAdd=CMParms.getParmInt(text, "DIFFLADD", 0);
			savePct=CMath.s_pct(CMParms.getParmStr(text, "SAVEPCT", "0"));
			diffGrp=CMParms.getParmBool(text, "DIFFGRP", false);
			diffPct=CMParms.getParmInt(text, "DIFFPCT", 100)/100.0;
			matchPlayersFollowersOnly=CMParms.getParmBool(text, "PLAYERSNFOLS", false);
			matchPlayersOnly=CMParms.getParmBool(text, "PLAYERSONLY", false);
			final String asMat = CMParms.getParmStr(text, "ASMATERIAL", "");
			if((asMat!=null)&&(asMat.trim().length()>0))
				asMaterial = RawMaterial.CODES.FIND_IgnoreCase(asMat);
		}
	}

	protected void doppleGangItem(final Item I)
	{
		final ItemPossessor owner=I.owner();
		if(owner!=null)
		{
			lastOwner=owner;
			lastLevel=owner.phyStats().level();
			int level=(int)Math.round(CMath.mul(((MOB)owner).phyStats().level(),levelPct))+levelAdd;
			if(level<minLevel)
				level=minLevel;
			if(level>maxLevel)
				level=maxLevel;
			final int difflevel=(int)Math.round(CMath.mul(level,diffPct))+diffAdd;
			I.basePhyStats().setLevel(difflevel);
			I.phyStats().setLevel(difflevel);
			final int oldMaterial=I.material();
			if(asMaterial != -1)
				I.setMaterial(asMaterial);
			CMLib.itemBuilder().balanceItemByLevel(I);
			I.basePhyStats().setLevel(level);
			I.phyStats().setLevel(level);
			I.setMaterial(oldMaterial);
			level=((MOB)owner).phyStats().level();
			if(level<minLevel)
				level=minLevel;
			if(level>maxLevel)
				level=maxLevel;
			I.basePhyStats().setLevel(level);
			I.phyStats().setLevel(level);
			owner.recoverPhyStats();
			if(owner instanceof Room)
				((Room)owner).recoverRoomStats();
			else
			if(owner instanceof MOB)
			{
				final MOB M=(MOB)owner;
				M.recoverCharStats();
				M.recoverMaxState();
				M.recoverPhyStats();
			}
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((affected instanceof Item)
		&&((((Item)affected).owner()!=lastOwner)||((lastOwner!=null)&&(lastOwner.phyStats().level()!=lastLevel)))
		&&(((Item)affected).owner() instanceof MOB))
			doppleGangItem((Item)affected);
		super.executeMsg(myHost,msg);
	}

	protected void addSingleOrGroup(final MOB M, final Set<MOB> all)
	{
		if(M!=null)
		{
			if(this.diffGrp)
				M.getGroupMembers(all);
			else
				all.add(M);
		}
	}

	protected void doppleGangMob(final MOB entrantM, final MOB doppleM, final Room R)
	{
		if((R!=null)
		&&(CMLib.flags().isAliveAwakeMobile(doppleM,true))
		&&(doppleM.curState().getHitPoints()>=doppleM.maxState().getHitPoints()))
		{
			final MOB doppleVictim=doppleM.getVictim();
			final Set<MOB> all = new HashSet<MOB>();
			if(canMimic(doppleVictim,R))
				addSingleOrGroup(doppleVictim,all);
			if(canMimic(entrantM,R))
				addSingleOrGroup(entrantM,all);
			for(int i=0;i<R.numInhabitants();i++)
			{
				final MOB M=R.fetchInhabitant(i);
				if((M!=null)
				&&(M!=doppleM)
				&&(!all.contains(doppleM))
				&&((M.getVictim()==doppleM)||(doppleVictim==null))
				&&((M!=doppleVictim)&&(M!=entrantM))
				&&(canMimic(M,R)))
					addSingleOrGroup(M,all);
			}
			for(final Iterator<MOB> m = all.iterator();m.hasNext();)
			{
				if(!canMimic(m.next(),R))
					m.remove();
			}
			int level=levelCode>=0?0:CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL);
			int total=0;
			int num=0;
			for(final MOB M : all)
			{
				switch(levelCode)
				{
				case 1:
					if(M.phyStats().level()>level)
						level=M.phyStats().level();
					break;
				case 0:
					total += M.phyStats().level();
					break;
				case -1:
					if(M.phyStats().level()<level)
						level=M.phyStats().level();
					break;
				}
				num++;
			}
			if(num>0)
			{
				int levelAdd = this.levelAdd;
				double levelPct = this.levelPct;
				double savePct = this.savePct;
				if(diffGrp)
				{
					levelAdd *= num;
					levelPct = CMath.mul(levelPct,num);
					savePct = CMath.mul(savePct,num);
				}
				if(levelCode == 0)
					level = (int)Math.round(CMath.div(total,num));
				level=(int)Math.round(CMath.mul(level,levelPct))+levelAdd;
				if(level<minLevel)
					level=minLevel;
				if(level>maxLevel)
					level=maxLevel;
				final int difflevel=(int)Math.round(CMath.mul(level,diffPct))+diffAdd;
				if(level!=doppleM.basePhyStats().level())
				{
					doppleM.basePhyStats().setLevel(difflevel);
					doppleM.phyStats().setLevel(difflevel);
					doppleM.basePhyStats().setArmor(CMLib.leveler().getLevelMOBArmor(doppleM));
					doppleM.basePhyStats().setAttackAdjustment(CMLib.leveler().getLevelAttack(doppleM));
					doppleM.basePhyStats().setDamage(CMLib.leveler().getLevelMOBDamage(doppleM));
					doppleM.basePhyStats().setSpeed(1.0+CMath.div(level,100)*4.0);
					doppleM.baseState().setHitPoints(CMLib.leveler().getPlayerHitPoints(doppleM));
					doppleM.baseState().setMana(CMLib.leveler().getLevelMana(doppleM));
					doppleM.baseState().setMovement(CMLib.leveler().getLevelMove(doppleM));
					if(savePct > 0.0)
					{
						final int saveSaveAmt = (int)Math.round(CMath.mul(difflevel,savePct));
						for(final int cd : CharStats.CODES.SAVING_THROWS())
							doppleM.baseCharStats().setStat(cd, doppleM.baseCharStats().getStat(cd) + saveSaveAmt);
					}
					doppleM.basePhyStats().setLevel(level);
					doppleM.phyStats().setLevel(level);
					doppleM.recoverPhyStats();
					doppleM.recoverCharStats();
					doppleM.recoverMaxState();
					doppleM.resetToMaxState();
				}
			}
		}
	}

	public boolean canMimic(final MOB mob, final Room R)
	{
		if((mob==affected)
		||(mob==null))
			return false;
		if((affected instanceof Room)
		&&(mob.isMonster())
		&&(mob.getStartRoom()!=null)
		&&(mob.getStartRoom().getArea()==((Room)affected).getArea()))
			return false;
		if((affected instanceof Area)
		&&(mob.isMonster())
		&&(mob.getStartRoom()!=null)
		&&(mob.getStartRoom().getArea()==(Area)affected))
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
		if((((msg.target() instanceof Room)&&(msg.sourceMinor()==CMMsg.TYP_ENTER))
		   ||(msg.sourceMinor()==CMMsg.TYP_LIFE))
		&&(!(affected instanceof Item)))
		//&&(lastLevelChangers))
		{
			//lastLevelChangers=false;
			if(msg.source() == affected)
			{
				final Room R=(msg.target() instanceof Room)?((Room)msg.target()):msg.source().location();
				MOB highestM = null;
				for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
				{
					final MOB M=m.nextElement();
					if((M != msg.source())
					&&((highestM==null)||(M.phyStats().level()>highestM.phyStats().level())))
						highestM = M;
				}
				if(highestM != null)
					doppleGangMob(highestM, msg.source(), R);
			}
			else
			if(affected instanceof MOB)
			{
				final Room R=(msg.target() instanceof Room)?((Room)msg.target()):msg.source().location();
				doppleGangMob(msg.source(), (MOB)affected, R);
			}
			else
			if(affected instanceof Room)
			{
				final Room R=(Room)affected;
				for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
				{
					final MOB M=m.nextElement();
					if((M.isMonster())
					&&(M.getStartRoom()!=null)
					&&(M.amFollowing()==null)
					&&(M.getStartRoom().getArea()==R.getArea()))
						doppleGangMob(msg.source(), M, R);
				}
			}
			else
			if(affected instanceof Area)
			{
				final Room R=(msg.target() instanceof Room)?((Room)msg.target()):msg.source().location();
				for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
				{
					final MOB M=m.nextElement();
					if((M.isMonster())
					&&(M.getStartRoom()!=null)
					&&(M.amFollowing()==null)
					&&(M.getStartRoom().getArea()==affected))
						doppleGangMob(msg.source(), M, R);
				}
			}
		}
		return super.okMessage(myHost,msg);
	}
}
