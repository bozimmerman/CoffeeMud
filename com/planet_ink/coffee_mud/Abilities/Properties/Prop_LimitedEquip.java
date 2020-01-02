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

import java.lang.ref.WeakReference;
import java.util.*;

/*
   Copyright 2019-2020 Bo Zimmerman

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
public class Prop_LimitedEquip extends Property
{
	@Override
	public String ID()
	{
		return "Prop_LimitedEquip";
	}

	@Override
	public String name()
	{
		return "Limited Equips Item";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS;
	}

	protected int		maxEquips	= 1;
	protected String	id			= null;
	protected String	msgStr		= "";

	@Override
	public String accountForYourself()
	{
		if(CMath.s_int(text())<=0)
			return "Only 1 may be equipped";
		final int x=text().indexOf(';');
		final String numStr;
		if(x<0)
			numStr = text();
		else
			numStr=text().substring(0,x);
		return L("Only @x1 may be equipped.",numStr);
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
		maxEquips=1;
		this.id=null;
		this.msgStr="";
		if(newMiscText.length()>0)
		{
			final int x=newMiscText.indexOf(';');
			final String numStr;
			if(x<0)
				numStr = newMiscText;
			else
			{
				final String parms = newMiscText.substring(x+1).trim();
				final String newId = CMParms.getParmStr(parms, "ID", "");
				if(newId.trim().length()>0)
					this.id=newId;
				numStr=newMiscText.substring(0,x);
				final String newMsgStr = CMParms.getParmStr(parms, "MESSAGE", "");
				if(newMsgStr.trim().length()>0)
					this.msgStr=newMsgStr.trim();
			}
			if(CMath.isInteger(numStr))
				maxEquips = CMath.s_int(numStr);
		}
	}

	protected String getId(final Physical I)
	{
		if(I == affected)
		{
			if(this.id != null)
				return this.id;
			return I.Name();
		}
		else
		if(I != null)
		{
			final Prop_LimitedEquip liA = (Prop_LimitedEquip)I.fetchEffect(ID());
			if((liA==null) || (liA.id == null))
				return I.Name();
			return liA.id;
		}
		return "N/A";
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((msg.targetMinor()==CMMsg.TYP_WEAR)
		&&(msg.target()==affected)
		&&(CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
		&&(affected instanceof Item))
		{
			final Item affI=(Item)msg.target();
			final ItemPossessor owner=affI.owner();
			if(owner instanceof MOB)
			{
				final MOB M=(MOB)owner;
				final String affId = getId(affI);
				int alreadyWornCount = 0;
				for(final Enumeration<Item> i=M.items();i.hasMoreElements();)
				{
					final Item I=i.nextElement();
					if((I != affI)
					&&(I.amBeingWornProperly())
					&&(affI.ID().equals(I.ID()))
					&&(affId.equals(getId(I))))
						alreadyWornCount++;
				}
				if(alreadyWornCount >= this.maxEquips)
				{
					if((msgStr==null)
					||(msgStr.trim().length()==0))
						msg.source().tell(L("You may not wear any more of those."));
					else
						msg.source().tell(msgStr);
					return false;
				}
			}
		}
		return true;
	}
}
