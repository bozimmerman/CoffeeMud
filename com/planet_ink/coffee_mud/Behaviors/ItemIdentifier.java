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
   Copyright 2002-2018 Bo Zimmerman

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
public class ItemIdentifier extends StdBehavior
{
	@Override
	public String ID()
	{
		return "ItemIdentifier";
	}

	private CMath.CompiledFormula costFormula = null;

	@Override
	public String accountForYourself()
	{
		return "item identifying for a price";
	}

	protected double cost(Item item)
	{
		if(costFormula != null)
		{
			final double[] vars = {item.phyStats().level(), item.value(), item.usesRemaining(), CMLib.flags().isABonusItems(item)?1.0:0.0,item.basePhyStats().level(), item.baseGoldValue(),0,0,0,0,0};
			return CMath.parseMathExpression(costFormula, vars, 0.0);
		}
		else
			return 500+(item.phyStats().level()*20);
	}

	@Override
	public void setParms(String parms)
	{
		super.setParms(parms);
		final String formulaString = CMParms.getParmStr(parms,"COST","500 + (@x1 * 20)");
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
	public boolean okMessage(Environmental affecting, CMMsg msg)
	{
		if(!super.okMessage(affecting,msg))
			return false;
		final MOB source=msg.source();
		if(!canFreelyBehaveNormal(affecting))
			return true;
		final MOB observer=(MOB)affecting;
		if((source!=observer)
		&&(msg.amITarget(observer))
		&&(msg.targetMinor()==CMMsg.TYP_GIVE)
		&&(!CMSecurity.isAllowed(source,source.location(),CMSecurity.SecFlag.CMDROOMS))
		&&(!(msg.tool() instanceof Coins))
		&&(msg.tool() instanceof Item))
		{
			final double cost=cost((Item)msg.tool());
			if(CMLib.beanCounter().getTotalAbsoluteShopKeepersValue(msg.source(),observer)<(cost))
			{
				final String costStr=CMLib.beanCounter().nameCurrencyShort(observer,cost);
				CMLib.commands().postSay(observer,source,L("You'll need @x1 for me to identify that.",costStr),true,false);
				return false;
			}
			return true;
		}
		return true;
	}

	@Override
	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting,msg);
		if(!canFreelyBehaveNormal(affecting))
			return;
		final MOB observer=(MOB)affecting;
		final MOB source=msg.source();

		if((source!=observer)
		&&(msg.amITarget(observer))
		&&(msg.targetMinor()==CMMsg.TYP_GIVE)
		&&(!CMSecurity.isAllowed(source,source.location(),CMSecurity.SecFlag.CMDROOMS))
		&&(!(msg.tool() instanceof Coins))
		&&(msg.tool() instanceof Item))
		{
			final Item I = (Item)msg.tool();
			final double cost=cost(I);
			CMLib.beanCounter().subtractMoney(source,CMLib.beanCounter().getCurrency(observer),cost);
			final String costStr=CMLib.beanCounter().nameCurrencyLong(observer,cost);
			source.recoverPhyStats();
			CMMsg newMsg=CMClass.getMsg(msg.source(),observer,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> give(s) @x1 to <T-NAMESELF>.",costStr));
			msg.addTrailerMsg(newMsg);
			newMsg=CMClass.getMsg(observer,I,null,CMMsg.MSG_EXAMINE,L("<S-NAME> examine(s) <T-NAME> very closely."));
			msg.addTrailerMsg(newMsg);
			final StringBuffer up=new StringBuffer(I.name(observer)+" is made of "+RawMaterial.CODES.NAME(I.material()).toLowerCase()+".\n\r");
			if((I instanceof Armor)&&(((Armor)I).phyStats().height()>0))
				up.append("It is a size "+((Armor)I).phyStats().height()+".\n\r");
			final int weight=I.phyStats().weight();
			if((weight!=I.basePhyStats().weight())&&(I instanceof Container))
				up.append("It weighs "+I.basePhyStats().weight()+" pounds empty and "+weight+" pounds right now.\n\r");
			else
				up.append("It weighs "+weight+" pounds.\n\r");
			if(I instanceof Weapon)
			{
				final Weapon w=(Weapon)I;
				up.append("It is a "+Weapon.CLASS_DESCS[w.weaponClassification()].toLowerCase()+" weapon.\n\r");
				up.append("It does "+Weapon.TYPE_DESCS[w.weaponDamageType()].toLowerCase()+" damage.\n\r");
			}
			if((CMLib.flags().domainAffects(I,Ability.DOMAIN_CURSING).size()>0)||(!CMLib.flags().isRemovable(I)))
				up.append("It is cursed.\n\r");
			up.append(I.secretIdentity());
			newMsg=CMClass.getMsg(observer,null,null,CMMsg.MSG_SPEAK,L("^T<S-NAME> say(s) '@x1'^?.",up.toString()));
			msg.addTrailerMsg(newMsg);
			newMsg=CMClass.getMsg(observer,source,I,CMMsg.MSG_GIVE,L("<S-NAME> give(s) <O-NAME> to <T-NAMESELF>."));
			msg.addTrailerMsg(newMsg);
			newMsg=CMClass.getMsg(observer,I,null,CMMsg.MSG_DROP,null);
			msg.addTrailerMsg(newMsg);
		}
	}
}
