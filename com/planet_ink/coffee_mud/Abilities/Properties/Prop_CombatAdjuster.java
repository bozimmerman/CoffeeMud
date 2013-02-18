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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2013 Bo Zimmerman

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
	public String ID() { return "Prop_CombatAdjuster"; }
	public String name(){ return "Adjust combat stats";}
	protected int canAffectCode(){return 0;}
	// attack, damage, armor, hp, mana, move
	protected double[] alladj={1.0,1.0,1.0,1.0,1.0,1.0};
	public String accountForYourself()
	{ return "Adjusted combat stats";	}

	public long flags(){return Ability.FLAG_ADJUSTER;}

	public int triggerMask()
	{ 
		return TriggeredAffect.TRIGGER_ALWAYS;
	}

	public void affectPhyStats(Physical affectedMOB, PhyStats affectableStats)
	{
		super.affectPhyStats(affectedMOB,affectableStats);
		if(alladj[2]!=1.0)
			affectableStats.setArmor((int)Math.round(CMath.mul(affectableStats.armor()-100,alladj[2]))+100);
		if(alladj[0]!=1.0)
			affectableStats.setAttackAdjustment((int)Math.round(CMath.mul(affectableStats.attackAdjustment(),alladj[0])));
		if(alladj[1]!=1.0)
			affectableStats.setDamage((int)Math.round(CMath.mul(affectableStats.damage(),alladj[1])));
	}
	public void affectCharState(MOB mob, CharState maxState)
	{
		super.affectCharState(mob,maxState);
		if(alladj[3]!=1.0)
			maxState.setHitPoints((int)Math.round(CMath.mul(maxState.getHitPoints(),alladj[3])));
		if(alladj[4]!=1.0)
			maxState.setMana((int)Math.round(CMath.mul(maxState.getMana(),alladj[4])));
		if(alladj[5]!=1.0)
			maxState.setMovement((int)Math.round(CMath.mul(maxState.getMovement(),alladj[5])));
	}
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
		}
	}

}
