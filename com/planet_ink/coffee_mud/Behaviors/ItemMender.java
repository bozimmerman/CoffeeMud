package com.planet_ink.coffee_mud.Behaviors;
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
public class ItemMender extends StdBehavior
{
	public String ID(){return "ItemMender";}

	private LinkedList<CMath.CompiledOperation> costFormula = null;
	
	protected double cost(Item item)
	{
		if(costFormula != null)
		{
			double[] vars = {item.envStats().level(), item.value(), item.usesRemaining(), CMLib.flags().isABonusItems(item)?1.0:0.0,item.baseEnvStats().level(), item.baseGoldValue(),0,0,0,0,0};
			return CMath.parseMathExpression(costFormula, vars, 0.0);
		}
		else
		{
			int cost=(100-item.usesRemaining())+item.envStats().level();
			if(CMLib.flags().isABonusItems(item))
				cost+=item.envStats().level();
			return cost;
		}
	}

	public void setParms(String parms)
	{
		super.setParms(parms);
		String formulaString = CMParms.getParmStr(parms,"COST","(100-@x3)+@x1+(@x4*@x1)");
		costFormula = null;
		if(formulaString.trim().length()>0)
		{
			try
			{
				costFormula = CMath.compileMathExpression(formulaString);
			}
			catch(Exception e)
			{
				Log.errOut(ID(),"Error compiling formula: " + formulaString);
			}
		}
	}
	
	
	public boolean okMessage(Environmental affecting, CMMsg msg)
	{
		if(!super.okMessage(affecting,msg))
			return false;
		MOB source=msg.source();
		if(!canFreelyBehaveNormal(affecting))
			return true;
		MOB observer=(MOB)affecting;
		if((source!=observer)
		&&(msg.amITarget(observer))
		&&(msg.targetMinor()==CMMsg.TYP_GIVE)
		&&(!CMSecurity.isAllowed(source,source.location(),"CMDROOMS"))
		&&(!(msg.tool() instanceof Coins))
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Item))
		{
			double cost=cost((Item)msg.tool());
			Item tool=(Item)msg.tool();
			if(!tool.subjectToWearAndTear())
			{
				CMLib.commands().postSay(observer,source,"I'm sorry, I can't work on these.",true,false);
				return false;
			}
			else
			if(tool.usesRemaining()>100)
			{
				CMLib.commands().postSay(observer,source,"Take this thing away from me.  It's so perfect, it's scary.",true,false);
				return false;
			}
			else
			if(tool.usesRemaining()==100)
			{
				CMLib.commands().postSay(observer,source,tool.name()+" doesn't require repair.",true,false);
				return false;
			}
			if(CMLib.beanCounter().getTotalAbsoluteShopKeepersValue(msg.source(),observer)<((double)cost))
			{
			    String costStr=CMLib.beanCounter().nameCurrencyShort(observer,(double)cost);
				CMLib.commands().postSay(observer,source,"You'll need "+costStr+" for me to repair that.",true,false);
				return false;
			}
			return true;
		}
		return true;
	}

	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting,msg);
		MOB source=msg.source();
		if(!canFreelyBehaveNormal(affecting))
			return;
		MOB observer=(MOB)affecting;

		if((source!=observer)
		&&(msg.amITarget(observer))
		&&(msg.targetMinor()==CMMsg.TYP_GIVE)
		&&(!CMSecurity.isAllowed(source,source.location(),"CMDROOMS"))
		&&(!(msg.tool() instanceof Coins))
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Item))
		{
			double cost=cost((Item)msg.tool());
			CMLib.beanCounter().subtractMoney(source,CMLib.beanCounter().getCurrency(observer),(double)cost);
			String costStr=CMLib.beanCounter().nameCurrencyLong(observer,(double)cost);
			source.recoverEnvStats();
			((Item)msg.tool()).setUsesRemaining(100);
			CMMsg newMsg=CMClass.getMsg(observer,source,msg.tool(),CMMsg.MSG_GIVE,"<S-NAME> give(s) <O-NAME> to <T-NAMESELF> and charges <T-NAMESELF> "+costStr+".");
			msg.addTrailerMsg(newMsg);
			newMsg=CMClass.getMsg(observer,source,null,CMMsg.MSG_SPEAK,"^T<S-NAME> say(s) 'There she is, good as new!  Thanks for your business' to <T-NAMESELF>.^?");
			msg.addTrailerMsg(newMsg);
			newMsg=CMClass.getMsg(observer,msg.tool(),null,CMMsg.MSG_DROP,null);
			msg.addTrailerMsg(newMsg);
		}
	}
}
