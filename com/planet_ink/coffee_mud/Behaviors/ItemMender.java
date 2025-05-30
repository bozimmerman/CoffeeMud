package com.planet_ink.coffee_mud.Behaviors;
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
   Copyright 2002-2025 Bo Zimmerman

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
public class ItemMender extends StdBehavior
{
	@Override
	public String ID()
	{
		return "ItemMender";
	}

	private CMath.CompiledFormula costFormula = null;

	@Override
	public String accountForYourself()
	{
		return "item mending for a price";
	}

	protected double cost(final Item item)
	{
		if(costFormula != null)
		{
			final double[] vars = {item.phyStats().level(), item.value(), item.usesRemaining(), CMLib.flags().isABonusItems(item)?1.0:0.0,item.basePhyStats().level(), item.baseGoldValue(),0,0,0,0,0};
			return CMath.parseMathExpression(costFormula, vars, 0.0);
		}
		else
		{
			int cost=(100-item.usesRemaining())+item.phyStats().level();
			if(CMLib.flags().isABonusItems(item))
				cost+=item.phyStats().level();
			return cost;
		}
	}

	@Override
	public void setParms(final String parms)
	{
		super.setParms(parms);
		final String formulaString = CMParms.getParmStr(parms,"COST","(100-@x3)+@x1+(@x4*@x1)");
		costFormula = null;
		if(formulaString.trim().length()>0)
		{
			try
			{
				costFormula = CMath.compileMathExpression(formulaString);
			}
			catch(final Exception e)
			{
				Log.errOut(ID(),"Error compiling formula: " + formulaString);
			}
		}
	}

	@Override
	public boolean okMessage(final Environmental host, final CMMsg msg)
	{
		if(!super.okMessage(host,msg))
			return false;
		final MOB source=msg.source();
		if(!canFreelyBehaveNormal(host))
			return true;
		final MOB observer=(MOB)host;
		if((source!=observer)
		&&(msg.amITarget(observer))
		&&(msg.targetMinor()==CMMsg.TYP_GIVE)
		&&(!CMSecurity.isAllowed(source,source.location(),CMSecurity.SecFlag.CMDROOMS))
		&&(!(msg.tool() instanceof Coins))
		&&(msg.tool() instanceof Item))
		{
			final double cost=cost((Item)msg.tool());
			final Item tool=(Item)msg.tool();
			if(!tool.subjectToWearAndTear())
			{
				CMLib.commands().postSay(observer,source,L("I'm sorry, I can't work on these."),true,false);
				return false;
			}
			else
			if(tool.usesRemaining()>100)
			{
				CMLib.commands().postSay(observer,source,L("Take this thing away from me.  It's so perfect, it's scary."),true,false);
				return false;
			}
			else
			if(tool.usesRemaining()==100)
			{
				CMLib.commands().postSay(observer,source,L("@x1 doesn't require repair.",tool.name()),true,false);
				return false;
			}
			if(CMLib.beanCounter().getTotalAbsoluteShopKeepersValue(msg.source(),observer)<(cost))
			{
				final String costStr=CMLib.beanCounter().nameCurrencyShort(observer,cost);
				CMLib.commands().postSay(observer,source,L("You'll need @x1 for me to repair that.",costStr),true,false);
				return false;
			}
			return true;
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental affecting, final CMMsg msg)
	{
		super.executeMsg(affecting,msg);
		final MOB source=msg.source();
		if(!canFreelyBehaveNormal(affecting))
			return;
		final MOB observer=(MOB)affecting;

		if((source!=observer)
		&&(msg.amITarget(observer))
		&&(msg.targetMinor()==CMMsg.TYP_GIVE)
		&&(!CMSecurity.isAllowed(source,source.location(),CMSecurity.SecFlag.CMDROOMS))
		&&(!(msg.tool() instanceof Coins))
		&&(msg.tool() instanceof Item))
		{
			final double cost=cost((Item)msg.tool());
			CMLib.beanCounter().subtractMoney(source,CMLib.beanCounter().getCurrency(observer),cost);
			final String costStr=CMLib.beanCounter().nameCurrencyLong(observer,cost);
			source.recoverPhyStats();
			((Item)msg.tool()).setUsesRemaining(100);
			CMMsg newMsg=CMClass.getMsg(observer,source,msg.tool(),CMMsg.MSG_GIVE,L("<S-NAME> give(s) <O-NAME> to <T-NAMESELF> and charges <T-NAMESELF> @x1.",costStr));
			msg.addTrailerMsg(newMsg);
			newMsg=CMClass.getMsg(observer,source,null,CMMsg.MSG_SPEAK,L("^T<S-NAME> say(s) 'There she is, good as new!  Thanks for your business' to <T-NAMESELF>.^?"));
			msg.addTrailerMsg(newMsg);
			newMsg=CMClass.getMsg(observer,msg.tool(),null,CMMsg.MSG_DROP,null);
			msg.addTrailerMsg(newMsg);
		}
	}
}
