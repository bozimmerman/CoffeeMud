package com.planet_ink.coffee_mud.Abilities.Fighter;
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

public class Fighter_FieldTactics extends FighterSkill
{
	public String ID() { return "Fighter_FieldTactics"; }
	public String name(){ return "Field Tactics";}
	public String displayText(){ return hidden?"(Hidden)":"";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
    public int classificationCode(){ return Ability.ACODE_SKILL|Ability.DOMAIN_NATURELORE;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	private static final Integer[] landClasses = {Integer.valueOf(-1)};
	public Integer[] landClasses(){return landClasses;}
	protected boolean activated=false;
	protected boolean hidden=false;
	protected long sitTime=0;

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if((affected!=null)&&(affected instanceof MOB)&&(activated))
		{
			if(hiding(affected))
			{
				if(!hidden)
				{
					hidden=true;
					sitTime=System.currentTimeMillis();
					affected.recoverEnvStats();
				}
			}
			else
			if(hidden)
			{
				hidden=false;
				sitTime=System.currentTimeMillis();
				affected.recoverEnvStats();
			}
		}
		return true;
	}

	public boolean hiding(Environmental mob)
	{
		if(!(mob instanceof MOB)) return false;
		return CMLib.flags().isSitting(mob)&&(((MOB)mob).riding()==null);
	}
	public boolean hiding(MOB mob)
	{
		return CMLib.flags().isSitting(mob)&&(mob.riding()==null);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected instanceof MOB)
		&&(activated)
		&&(msg.amISource((MOB)affected))
		&&(!msg.amITarget(affected))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Weapon)
		&&(msg.value()>0))
		{
			if(CMLib.dice().rollPercentage()<5) helpProficiency((MOB)affected);
			msg.setValue(msg.value()+(int)Math.round(CMath.mul(msg.value(),CMath.div(proficiency(),400.0-(10.0*getXLEVELLevel(msg.source()))))));
		}
		else
		if((hidden)&&(!hiding(affected)))
		{
			hidden=false;
			sitTime=System.currentTimeMillis();
			affected.recoverEnvStats();
		}
		else
		if((msg.source()==affected)
		&&(hidden)
		&&((CMath.bset(msg.sourceMajor(),CMMsg.MASK_SOUND)
			 ||(msg.sourceMinor()==CMMsg.TYP_SPEAK)
			 ||(msg.sourceMinor()==CMMsg.TYP_ENTER)
			 ||(msg.sourceMinor()==CMMsg.TYP_LEAVE)
			 ||(msg.sourceMinor()==CMMsg.TYP_RECALL)))
		 &&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS))
		 &&(msg.sourceMinor()!=CMMsg.TYP_LOOK)
         &&(msg.sourceMinor()!=CMMsg.TYP_EXAMINE)
		 &&(msg.sourceMajor()>0))
		{
			hidden=false;
			sitTime=System.currentTimeMillis();
			affected.recoverEnvStats();
		}
		return super.okMessage(myHost,msg);
	}

	public boolean oneOf(int dom)
	{
		for(int i=0;i<landClasses().length;i++)
			if((dom==landClasses()[i].intValue())
			||(landClasses()[i].intValue()<0))
				return true;
		return false;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((affected instanceof MOB)
		&&(((MOB)affected).location()!=null)
		&&(oneOf(((MOB)affected).location().domainType())))
		{
			if((hidden)&&((System.currentTimeMillis()-sitTime)>(60*2*1000)))
				affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_HIDDEN);
			activated=true;
			float xlvl=(float)getXLEVELLevel(invoker());
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(int)Math.round((15.0+xlvl)*(CMath.div(proficiency(),100.0))));
			affectableStats.setArmor(affectableStats.armor()-(int)Math.round((15.0+xlvl)*(CMath.div(proficiency(),100.0))));
		}
		else
		{
			activated=false;
			hidden=false;
		}
	}
}