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
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
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
public class Prop_LimitedContents extends Property
{
	@Override
	public String ID()
	{
		return "Prop_LimitedContents";
	}

	@Override
	public String name()
	{
		return "Limited Content Types";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS|Ability.CAN_AREAS|Ability.CAN_ROOMS;
	}

	protected final PairVector<Integer, CompiledZMask> limits = new PairVector<Integer, CompiledZMask>();
	protected String msgStr = "";
	protected boolean actual=true;

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
		limits.clear();
		this.msgStr="";
		this.actual=true;
		if(newMiscText.length()>0)
		{
			for(String part : newMiscText.split(";"))
			{
				part=part.trim();
				final int x=part.indexOf(' ');
				final String amtStr;
				String mask="";
				if(x>0)
				{
					amtStr=part.substring(0,x);
					mask=part.substring(x+1).trim();
				}
				else
					amtStr=part;
				if(CMath.isInteger(amtStr))
					limits.add(Integer.valueOf(CMath.s_int(amtStr)), CMLib.masking().getPreCompiledMask(mask));
				else
				{
					this.msgStr=CMParms.getParmStr(part,"MSG",this.msgStr);
					this.actual=CMParms.getParmBool(part,"ACTUAL",this.actual);
				}
			}
		}
	}

	protected int itemCount(final Item item)
	{
		if(item instanceof RawMaterial)
			return ((RawMaterial)item).phyStats().weight();
		else
		if(item instanceof PackagedItems)
			return ((PackagedItems)item).numberOfItemsInPackage();
		return 1;
	}

	protected Enumeration<Item> getExistingItems(final Physical locatableP)
	{
		if(affected instanceof Container)
			return new IteratorEnumeration<Item>(((Container)affected).getContents().iterator());
		if(affected instanceof Room)
			return ((Room)affected).items();
		final Room R=CMLib.map().roomLocation(locatableP);
		if(R!=null)
			return R.items();
		return new Vector<Item>().elements();
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;

		final Item item;
		if(msg.targetMinor()==CMMsg.TYP_DROP)
		{
			if(affected instanceof Room)
			{
				if(msg.target()!=affected)
					return true;
			}
			else
			if(!(affected instanceof Area))
				return true;
			if(!(msg.tool() instanceof Item))
				return true;
			item=(Item)msg.tool();
		}
		else
		if(msg.targetMinor()==CMMsg.TYP_PUT)
		{
			if((!(affected instanceof Item))
			||(msg.target() != affected))
				return true;
			item=(Item)msg.tool();
		}
		else
			return true;

		final MaskingLibrary maskLib=CMLib.masking();
		for(final Pair<Integer,CompiledZMask> p : this.limits)
		{
			if(maskLib.maskCheck(p.second, item, this.actual))
			{
				final int maxLimit = p.first.intValue();
				final int numAdded=itemCount(item);
				if(numAdded > maxLimit)
				{
					if(msgStr.length()==0)
						msg.source().tell(L("You can only put @x1 of those in there.",""+maxLimit));
					else
						msg.source().tell(L(msgStr,""+maxLimit));
					return false;
				}
				int existingCount=0;
				for(final Enumeration<Item> i=this.getExistingItems(msg.source());i.hasMoreElements();)
				{
					final Item I=i.nextElement();
					if(maskLib.maskCheck(p.second, item, this.actual))
						existingCount += itemCount(I);
				}
				if(existingCount + numAdded > maxLimit)
				{
					if(msgStr.length()==0)
						msg.source().tell(L("You can't can only put @x1 of those in there.",""+maxLimit));
					else
						msg.source().tell(L(msgStr,""+maxLimit));
					return false;
				}
				break;
			}
		}
		return true;
	}
}
