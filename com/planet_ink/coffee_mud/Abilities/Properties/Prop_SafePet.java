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
   Copyright 2001-2018 Bo Zimmerman

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
public class Prop_SafePet extends Property
{
	@Override
	public String ID()
	{
		return "Prop_SafePet";
	}

	@Override
	public String name()
	{
		return "Unattackable Pets";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	protected boolean	disabled		= false;
	protected String	displayMessage	= "Awww, leave <T-NAME> alone.";

	@Override
	public String accountForYourself()
	{
		return "Unattackable";
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_UNATTACKABLE);
	}

	@Override
	public void setMiscText(String newMiscText)
	{
		super.setMiscText(newMiscText);
		final String newDisplayMsg=CMParms.getParmStr(newMiscText, "MSG", "");
		if(newDisplayMsg.trim().length()>0)
		{
			displayMessage=newDisplayMsg.trim();
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(affected instanceof MOB)
		{
			if((msg.amISource((MOB)affected))&&(msg.sourceMinor()==CMMsg.TYP_DEATH)&&(!CMath.bset(msg.sourceMajor(), CMMsg.MASK_ALWAYS)))
			{
				msg.source().tell(L("You are safe from death."));
				return false;
			}
			else
			if(CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS))
			{
				if((msg.amITarget(affected))
				&&(!disabled))
				{
					if(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS))
						msg.source().tell(msg.source(),affected,null,displayMessage);
					((MOB)affected).makePeace(true);
					return false;
				}
				else
				if(msg.amISource((MOB)affected))
					disabled=true;
			}
			else
			if((msg.targetMinor()==CMMsg.TYP_DAMAGE) && msg.amITarget(affected))
				msg.setValue(0);
			else
			if(!((MOB)affected).isInCombat())
				disabled=false;
		}
		else
		if(CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS) && msg.amITarget(affected))
		{
			if(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS))
				msg.source().tell(msg.source(),affected,null,displayMessage);
			return false;
		}
		return super.okMessage(myHost,msg);
	}
}
