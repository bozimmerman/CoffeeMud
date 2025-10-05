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
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2025-2025 Bo Zimmerman

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
public class CMarket extends StdBehavior
{
	@Override
	public String ID()
	{
		return "CMarket";
	}

	/*
	 * private enum MarketType { STOCK, REGIONAL, BOND, COMMODITY, RACIAL,
	 * PLAYER }
	 *
	 * private MarketType marketType = MarketType.STOCK;
	 */
	private int				updateDays				= 24;
	private int				waitDaysAfterBankruptcy	= 10;
	private int				maxStocks				= 10;
	private boolean			allowsClans				= true;
	private boolean			groupShopkeepers		= false;
	private boolean			groupShopkeepersByType	= false;
	private String			nameMask				= "The Stock";
	private CompiledZMask	shopkeeperMask			= null;
	private CompiledZMask	areaMask				= null;
	private PhysicalAgent	host					= null;

	private volatile TimeClock nextUpdate = null;

	private boolean isApplicableArea(final Environmental E)
	{
		final Area A = CMLib.map().areaLocation(E);
		if (A == null)
			return false;
		return areaMask == null || (CMLib.masking().maskCheck(areaMask, A, true));
	}

	private boolean isApplicableShopKeeper(final Environmental E)
	{
		final ShopKeeper SK = CMLib.coffeeShops().getShopKeeper(E);
		if (SK == null)
			return false;
		return shopkeeperMask == null || (CMLib.masking().maskCheck(shopkeeperMask, E, true));
	}

	@Override
	protected int canImproveCode()
	{
		return Behavior.CAN_AREAS;
	}

	@Override
	public void startBehavior(final PhysicalAgent forMe)
	{
		if (forMe != null)
			host = forMe;
	}

	@Override
	public void endBehavior(final PhysicalAgent forMe)
	{
	}

	@Override
	public void setParms(final String parameters)
	{
		super.setParms(parameters);
		// final String marketTypeStr = CMParms.getParmStr(parameters,
		// "MARKETTYPE", MarketType.STOCK.name()).toUpperCase().trim();
		// final MarketType mt = (MarketType)CMath.s_valueOf(MarketType.class,
		// marketTypeStr);
		// marketType = (mt != null) ? mt : MarketType.STOCK;
		this.updateDays = CMParms.getParmInt(parameters, "UPDATEDAYS", 24);
		this.waitDaysAfterBankruptcy = CMParms.getParmInt(parameters, "WAITDAYAB", 10);
		this.maxStocks = CMParms.getParmInt(parameters, "MAXSTOCKS", 10);
		this.nameMask = CMParms.getParmStr(parameters, "NAME", "The Stock");
		this.allowsClans = CMParms.getParmBool(parameters, "ALLOWCLANS", true);
		final String shopMask = CMParms.getParmStr(parameters, "SHOPMASK", "");
		this.shopkeeperMask = (shopMask.trim().length() == 0) ? null : CMLib.masking().getPreCompiledMask(shopMask);
		final String areaMask = CMParms.getParmStr(parameters, "AREAMASK", "");
		this.areaMask = (areaMask.trim().length() == 0) ? null : CMLib.masking().getPreCompiledMask(areaMask);
		this.groupShopkeepers = CMParms.getParmBool(parameters, "GROUPSHOPS", true);
		this.groupShopkeepersByType = CMParms.getParmBool(parameters, "GROUPTYPES", true);
	}
}
