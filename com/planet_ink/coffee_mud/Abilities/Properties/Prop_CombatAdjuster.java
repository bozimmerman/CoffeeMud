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
   Copyright 2006-2018 Bo Zimmerman

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
public class Prop_CombatAdjuster extends Property implements TriggeredAffect
{
	@Override
	public String ID()
	{
		return "Prop_CombatAdjuster";
	}

	@Override
	public String name()
	{
		return "Adjust combat stats";
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	// attack, damage, armor, hp, mana, move, speed
	protected double[]	alladj	= { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 };
	// attack, damage, armor, hp, mana, move, speed
	protected int[]		allset	= { -1, -1, -1, -1, -1, -1, -1 };

	@Override
	public String accountForYourself()
	{
		return "Adjusted combat stats";
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_ADJUSTER;
	}

	@Override
	public int triggerMask()
	{
		return TriggeredAffect.TRIGGER_ALWAYS;
	}

	@Override
	public void affectPhyStats(Physical affectedMOB, PhyStats affectableStats)
	{
		super.affectPhyStats(affectedMOB,affectableStats);
		if(alladj[2]!=1.0)
			affectableStats.setArmor(100-(int)Math.round(CMath.mul(100-affectableStats.armor(),alladj[2])));
		else
		if(allset[2]>=0)
			affectableStats.setArmor(allset[2]);
		if(alladj[0]!=1.0)
			affectableStats.setAttackAdjustment((int)Math.round(CMath.mul(affectableStats.attackAdjustment(),alladj[0])));
		else
		if(allset[0]>=0)
			affectableStats.setAttackAdjustment(allset[0]);
		if(alladj[1]!=1.0)
			affectableStats.setDamage((int)Math.round(CMath.mul(affectableStats.damage(),alladj[1])));
		else
		if(allset[1]>=0)
			affectableStats.setDamage(allset[1]);
		if(alladj[6]!=1.0)
			affectableStats.setSpeed((int)Math.round(CMath.mul(affectableStats.speed(),alladj[6])));
		else
		if(allset[6]>=0)
			affectableStats.setSpeed(allset[6]);
	}

	@Override
	public void affectCharState(MOB mob, CharState maxState)
	{
		super.affectCharState(mob,maxState);
		if(alladj[3]!=1.0)
			maxState.setHitPoints((int)Math.round(CMath.mul(maxState.getHitPoints(),alladj[3])));
		else if(allset[3]>=0)
			maxState.setHitPoints(allset[3]);
		if(alladj[4]!=1.0)
			maxState.setMana((int)Math.round(CMath.mul(maxState.getMana(),alladj[4])));
		else 
		if(allset[4]>=0)
			maxState.setMana(allset[4]);
		if(alladj[5]!=1.0)
			maxState.setMovement((int)Math.round(CMath.mul(maxState.getMovement(),alladj[5])));
		else 
		if(allset[5]>=0)
			maxState.setMovement(allset[5]);
	}

	@Override
	public void setMiscText(String newMiscText)
	{
		super.setMiscText(newMiscText);
		if(newMiscText.length()>0)
		{
			alladj[0]=1.0+CMath.div(CMParms.getParmPlus(newMiscText,"ATTACK"),100.0);
			alladj[1]=1.0+CMath.div(CMParms.getParmPlus(newMiscText,"DAMAGE"),100.0);
			alladj[2]=1.0+CMath.div(CMParms.getParmPlus(newMiscText,"ARMOR"),100.0);
			alladj[3]=1.0+CMath.div(CMParms.getParmPlus(newMiscText,"HP"),100.0);
			alladj[4]=1.0+CMath.div(CMParms.getParmPlus(newMiscText,"MANA"),100.0);
			alladj[5]=1.0+CMath.div(CMParms.getParmPlus(newMiscText,"MOVE"),100.0);
			alladj[6]=1.0+CMath.div(CMParms.getParmPlus(newMiscText,"SPEED"),100.0);
			allset[0]=CMParms.getParmInt(newMiscText, "ATTACK", -1);
			allset[1]=CMParms.getParmInt(newMiscText, "DAMAGE", -1);
			allset[2]=CMParms.getParmInt(newMiscText, "ARMOR", -1);
			allset[3]=CMParms.getParmInt(newMiscText, "HP", -1);
			allset[4]=CMParms.getParmInt(newMiscText, "MANA", -1);
			allset[5]=CMParms.getParmInt(newMiscText, "MOVE", -1);
			allset[6]=CMParms.getParmInt(newMiscText, "SPEED", -1);
		}
	}

	@Override
	public String getStat(String code)
	{
		if((code!=null)&&(code.equalsIgnoreCase("LEVEL")))
		{
			int level = 0;
			for(int c=0;c<alladj.length;c+=1)
			{
				if(alladj[c]==1.0)
					continue;
				int amt= (int)Math.round(alladj[c] * 100.0);
				if(amt >= 100)
					amt -= 100;
				else
				if(amt < 100)
					amt = -(100-amt);
				
				switch(c)
				{
				case 0://PhyStats.STAT_ATTACK:
					level+= (amt / 5);
					break;
				case 1://PhyStats.STAT_DAMAGE:
					level+= (amt / 20);
					break;
				case 2://PhyStats.STAT_ARMOR:
					level+= (amt / -5);
					break;
				case 3://CharState.STAT_HITPOINTS:
					level += (amt / 20);
					break;
				case 4://CharState.STAT_MANA:
					level += (amt / 20);
					break;
				case 5://CharState.STAT_MOVE:
					level += (amt / 20);
					break;
				case 6://speed --PhyStats.NUM_STATS:
					level+= (amt / 20);
					break;
				}
			}
			return ""+level;
		}
		return super.getStat(code);
	}

	@Override
	public void setStat(String code, String val)
	{
		if((code!=null)&&(code.equalsIgnoreCase("LEVEL")))
		{
	
		}
		else
			super.setStat(code, val);
	}
}
