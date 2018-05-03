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
public class Prop_SpellReflecting extends Property implements TriggeredAffect
{
	@Override
	public String ID()
	{
		return "Prop_SpellReflecting";
	}

	@Override
	public String name()
	{
		return "Spell reflecting property";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS|Ability.CAN_ITEMS;
	}

	protected int minLevel=1;
	protected int maxLevel=30;
	protected int chance=100;
	protected int remaining=100;
	protected int fade=1;
	protected int uses=100;
	protected long lastFade=0;

	@Override
	public int abilityCode()
	{
		return uses;
	}

	@Override
	public void setAbilityCode(int newCode)
	{
		uses=newCode;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_IMMUNER;
	}

	@Override
	public int triggerMask()
	{
		return TriggeredAffect.TRIGGER_BEING_HIT;
	}

	@Override
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		minLevel=CMParms.getParmInt(newText,"min",minLevel);
		maxLevel=CMParms.getParmInt(newText,"max",maxLevel);
		chance=CMParms.getParmInt(newText,"chance",chance);
		fade=CMParms.getParmInt(newText,"fade",fade);
		remaining=CMParms.getParmInt(newText,"remain",remaining);
		setAbilityCode(remaining);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(affected==null)
			return true;
		if((fade<=0)&&(abilityCode()<remaining))
		{
			if(lastFade==0)
				lastFade=System.currentTimeMillis();
			final long time=System.currentTimeMillis()-lastFade;
			if(time>5*60000)
			{
				final double div=CMath.div(time,(long)5*60000);
				if(div>1.0)
				{
					setAbilityCode(abilityCode()+(int)Math.round(div));
					if(abilityCode()>remaining)
						setAbilityCode(remaining);
					lastFade=System.currentTimeMillis();
				}
			}
		}

		if((CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS))
		&&(msg.targetMinor()==CMMsg.TYP_CAST_SPELL)
		&&(msg.tool() instanceof Ability)
		&&(CMLib.dice().rollPercentage()<=chance)
		&&(abilityCode()>0)
		&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL))
		{
			MOB target=null;
			if(affected instanceof MOB)
				target=(MOB)affected;
			else
			if((affected instanceof Item)
			&&(!((Item)affected).amWearingAt(Wearable.IN_INVENTORY))
			&&(((Item)affected).owner()!=null)
			&&(((Item)affected).owner() instanceof MOB))
				target=(MOB)((Item)affected).owner();
			else
				return true;

			if(!msg.amITarget(target))
				return true;
			if(msg.amISource(target))
				return true;
			if(target.location()==null)
				return true;

			int lvl=CMLib.ableMapper().qualifyingLevel(msg.source(),((Ability)msg.tool()));
			if(lvl<=0)
				lvl=CMLib.ableMapper().lowestQualifyingLevel(((Ability)msg.tool()).ID());
			if(lvl<=0)
				lvl=1;
			if((lvl<minLevel)||(lvl>maxLevel))
				return true;

			target.location().show(target,affected,CMMsg.MSG_OK_VISUAL,L("The field around <T-NAMESELF> reflects the spell!"));
			final Ability A=(Ability)msg.tool();
			A.invoke(target,msg.source(),true,msg.source().phyStats().level());
			setAbilityCode(abilityCode()-lvl);
			if(abilityCode()<=0)
			{
				if(affected instanceof MOB)
				{
					target.location().show(target,target,CMMsg.MSG_OK_VISUAL,L("The field around <T-NAMESELF> fades."));
					if(fade>0)
						target.delEffect(this);
				}
				else
				if(affected instanceof Item)
				{
					if(fade>0)
					{
						target.location().show(target,affected,CMMsg.MSG_OK_VISUAL,L("<T-NAMESELF> vanishes!"));
						((Item)affected).destroy();
						target.location().recoverRoomStats();
					}
					else
						target.location().show(target,affected,CMMsg.MSG_OK_VISUAL,L("The field around <T-NAMESELF> fades."));
				}
			}
			return false;
		}
		return super.okMessage(myHost,msg);
	}

}
