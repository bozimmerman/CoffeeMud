package com.planet_ink.coffee_mud.MOBS;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ShopKeeper.ViewType;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.CoffeeShop.ShelfProduct;
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.Libraries.interfaces.TimeManager;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.Librarian.CheckedOutRecord;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

import javax.swing.text.html.HTML.Tag;

/*
   Copyright 2024-2024 Bo Zimmerman

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
public class StdCraftBroker extends StdShopKeeper implements CraftBroker
{
	@Override
	public String ID()
	{
		return "StdCraftBroker";
	}

	protected static final Map<String,Long> lastCheckTimes=new Hashtable<String,Long>();

	protected String		currency			= "";
	protected int			maxDays				= 900;
	protected int			maxListings			= 5;

	public StdCraftBroker()
	{
		super();
		_name="a craft broker";
		setDescription("He wants to help you make a deal!");
		setDisplayText("A craft broker is here.");
		CMLib.factions().setAlignment(this,Faction.Align.GOOD);
		setMoney(0);
		basePhyStats.setWeight(150);
		setWimpHitPoint(0);

		baseCharStats().setStat(CharStats.STAT_WISDOM,16);
		baseCharStats().setStat(CharStats.STAT_CHARISMA,8);

		basePhyStats().setArmor(0);

		baseState.setHitPoints(1000);

		recoverMaxState();
		resetToMaxState();
		recoverPhyStats();
		recoverCharStats();
	}

	@Override
	public String brokerChain()
	{
		return text();
	}

	@Override
	public void setBrokerChain(final String name)
	{
		setMiscText(name);
	}

	private List<Quad<String,String,Integer,Double>> loadRequests()
	{
		final String shopKey = "BROKER_REQ_"+brokerChain().toUpperCase().replace(' ', '_');
		final List<PlayerData> data = CMLib.database().DBReadPlayerSectionData(shopKey);
		final List<Quad<String,String,Integer,Double>> list = new ArrayList<Quad<String,String,Integer,Double>>();
		final XMLLibrary xmlLib = CMLib.xml();
		for(final PlayerData dat : data)
		{
			if((dat.xml().length()>0)&&(dat.xml().startsWith("<")))
			{
				final List<XMLLibrary.XMLTag> xml = xmlLib.parseAllXML(dat.xml());
				for(final XMLLibrary.XMLTag tag : xml)
				{
					if(tag.tag().equals("REQUEST"))
					{
						final String author = tag.getParmValue("NAME");
						final String req = xmlLib.restoreAngleBrackets(tag.value());
						final int num = CMath.s_int(tag.getParmValue("NUM"));
						final double price = CMath.s_double(tag.getParmValue("PRICE"));
						list.add(new Quad<String,String,Integer,Double>(author+"/"+dat.key(),req,Integer.valueOf(num),Double.valueOf(price)));
					}
				}
			}
		}
		return list;
	}

	private void addRequest(final MOB mob, final String request, final int number, final double price)
	{
		final String shopKey = "BROKER_REQ_"+brokerChain().toUpperCase().replace(' ', '_');
		final String key = shopKey+mob.Name()+(shopKey+mob.Name()).hashCode();
		final StringBuilder xml = new StringBuilder("");
		xml.append("<REQUEST NAME=\""+mob.Name()+"\" NUM="+number+" PRICE="+price+">");
		xml.append(CMLib.xml().parseOutAngleBrackets(request));
		xml.append("</REQUEST>");
		CMLib.database().DBCreatePlayerData(mob.Name(), shopKey, key, xml.toString());
	}

	private CoffeeShop loadRequestShop()
	{
		final CoffeeShop shop = ((CoffeeShop)CMClass.getCommon("DefaultCoffeeShop")).build(this);
		for(final Quad<String,String,Integer,Double> q : loadRequests())
		{
			final ExtendableAbility A = (ExtendableAbility)CMClass.getAbility("ExtAbility");
			A.setName(q.second);
			A.setMiscText(q.first);
			shop.addStoreInventory(A, q.third.intValue(), q.fourth.intValue());
		}
		return shop;
	}

	private void removeRequestShop(final Ability A)
	{
		final int x =A.text().indexOf('/');
		if(x>0)
			return;
		final String shopKey = "BROKER_REQ_"+brokerChain().toUpperCase().replace(' ', '_');
		final String author = A.text().substring(0,x);
		final String key = A.text().substring(x+1);
		CMLib.database().DBDeletePlayerData(author, shopKey, key);
	}

	private void updateRequestShop(final CoffeeShop shop, final Ability A)
	{
		final int x =A.text().indexOf('/');
		if(x>0)
			return;
		final String shopKey = "BROKER_REQ_"+brokerChain().toUpperCase().replace(' ', '_');
		final String author = A.text().substring(0,x);
		final String key = A.text().substring(x+1);
		final int num = shop.numberInStock(A);
		final int price = shop.stockPrice(A);
		final StringBuilder xml = new StringBuilder("");
		xml.append("<REQUEST NAME=\""+author+"\" NUM="+num+" PRICE="+price+">");
		xml.append(CMLib.xml().parseOutAngleBrackets(A.name()));
		xml.append("</REQUEST>");
		CMLib.database().DBReCreatePlayerData(author, shopKey, key, xml.toString());
	}

	private CoffeeShop loadResults(final MOB mob)
	{
		final String shopKey = "BROKER_SHOP_"+brokerChain().toUpperCase().replace(' ', '_');
		final CoffeeShop shop = ((CoffeeShop)CMClass.getCommon("DoubleCoffeeShop")).build(this);
		final List<PlayerData> data = CMLib.database().DBReadPlayerData(mob.Name(), shopKey);
		for(final PlayerData dat : data)
		{
			if((dat.xml().length()>0)&&(dat.xml().startsWith("<")))
				shop.buildShopFromXML(dat.xml());
		}
		return shop;
	}

	private void saveResults(final MOB mob, final CoffeeShop shop)
	{
		final String shopKey = "BROKER_SHOP_"+brokerChain().toUpperCase().replace(' ', '_');
		String key = shopKey+mob.Name()+(shopKey+mob.Name()).hashCode();
		if(key.length()>=100)
			key=key.substring(key.length()-99);
		CMLib.database().DBReCreatePlayerData(mob.Name(), shopKey, key, shop.makeXML());
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return true;
		synchronized(CMClass.getSync(("BROKER_CHAIN_"+brokerChain().toUpperCase().trim())))
		{
			final Long lastTime=StdCraftBroker.lastCheckTimes.get(brokerChain().toUpperCase().trim());
			if((lastTime==null)||(System.currentTimeMillis()-lastTime.longValue())>(CMProps.getMillisPerMudHour()-5))
			{
				if(!CMLib.flags().isInTheGame(this,true))
					return true;
				StdCraftBroker.lastCheckTimes.remove(brokerChain().toUpperCase().trim());
				final long thisTime=System.currentTimeMillis();
				StdCraftBroker.lastCheckTimes.put(brokerChain().toUpperCase().trim(),Long.valueOf(thisTime));
			}
		}
		return true;
	}

	protected void autoGive(final MOB src, final MOB tgt, final Item I)
	{
		CMMsg msg2=CMClass.getMsg(src,I,null,CMMsg.MSG_DROP|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_DROP|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_DROP|CMMsg.MASK_INTERMSG,null);
		location().send(this,msg2);
		msg2=CMClass.getMsg(tgt,I,null,CMMsg.MSG_GET|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_GET|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_GET|CMMsg.MASK_INTERMSG,null);
		location().send(this,msg2);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		final MOB mob = msg.source();
		if (msg.amITarget(this))
		{
			switch (msg.targetMinor())
			{
			case CMMsg.TYP_GIVE:
			case CMMsg.TYP_DEPOSIT:
				if (CMLib.flags().isAliveAwakeMobileUnbound(mob, true))
				{
					if ((CMSecurity.isAllowed(msg.source(), location(), CMSecurity.SecFlag.ORDER)
					|| (CMLib.law().doesHavePriviledgesHere(msg.source(), getStartRoom()))
					|| (CMSecurity.isAllowed(msg.source(), location(), CMSecurity.SecFlag.CMDMOBS) && (isMonster()))
					|| (CMSecurity.isAllowed(msg.source(), location(), CMSecurity.SecFlag.CMDROOMS) && (isMonster()))))
						return;
					super.executeMsg(myHost, msg);
				}
				return;
			case CMMsg.TYP_BORROW:
			case CMMsg.TYP_WITHDRAW:
				if (CMLib.flags().isAliveAwakeMobileUnbound(mob, true))
				{
				}
				super.executeMsg(myHost, msg);
				return;
			case CMMsg.TYP_VALUE:
			case CMMsg.TYP_SELL:
			case CMMsg.TYP_VIEW:
				super.executeMsg(myHost, msg);
				return;
			case CMMsg.TYP_BUY:
				super.executeMsg(myHost, msg);
				return;
			case CMMsg.TYP_SPEAK:
			{
				super.executeMsg(myHost, msg);
				CMStrings.getSayFromMessage(msg.targetMessage());
				return;
			}
			case CMMsg.TYP_LIST:
			{
				super.executeMsg(myHost, msg);
				if (CMLib.flags().isAliveAwakeMobileUnbound(mob, true))
				{
					final StringBuilder str = new StringBuilder("");
					if (str.length() > 2)
					{
						mob.tell("");
						CMLib.commands().postSay(this, mob, str.toString().substring(0, str.length() - 2), true, false);
					}
					return;
				}
				break;
			}
			default:
				break;
			}
		}
		super.executeMsg(myHost, msg);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		final MOB mob = msg.source();
		if ((msg.targetMinor() == CMMsg.TYP_EXPIRE) && (msg.target() == location()) && (CMLib.flags().isInTheGame(this, true)))
			return false;
		else
		if (msg.amITarget(this))
		{
			switch (msg.targetMinor())
			{
			case CMMsg.TYP_GIVE:
			case CMMsg.TYP_DEPOSIT:
				{
					if (!CMLib.coffeeShops().ignoreIfNecessary(msg.source(), getFinalIgnoreMask(), this))
						return false;
					if (msg.tool() == null)
						return false;
					if (!(msg.tool() instanceof Item))
					{
						mob.tell(L("@x1 doesn't look interested.", mob.charStats().HeShe()));
						return false;
					}
					if (CMLib.flags().isEnspelled((Item) msg.tool()) || CMLib.flags().isOnFire((Item) msg.tool()))
					{
						mob.tell(this, msg.tool(), null, L("<S-HE-SHE> refuses to accept <T-NAME>."));
						return false;
					}
					return super.okMessage(myHost, msg);
				}
			case CMMsg.TYP_WITHDRAW:
			case CMMsg.TYP_BORROW:
				{
					if (!CMLib.coffeeShops().ignoreIfNecessary(msg.source(), getFinalIgnoreMask(), this))
						return false;
					if ((msg.tool() == null) || (!(msg.tool() instanceof Item)) || (msg.tool() instanceof Coins))
					{
						CMLib.commands().postSay(this, mob, L("What do you want? I'm busy! Also, SHHHH!!!!"), true, false);
						return false;
					}
					if ((msg.tool() != null) && (!msg.tool().okMessage(myHost, msg)))
						return false;
					if (!this.getShop().doIHaveThisInStock(msg.tool().Name(), null))
					{
						CMLib.commands().postSay(this, mob, L("We don't stock anything like that."), true, false);
						return false;
					}
				}
				return super.okMessage(myHost, msg);
			case CMMsg.TYP_SELL:
			case CMMsg.TYP_VALUE:
				return super.okMessage(myHost, msg);
			case CMMsg.TYP_VIEW:
				return super.okMessage(myHost, msg);
			case CMMsg.TYP_BUY:
				CMLib.commands().postSay(this, mob, L("I'm sorry, but nothing here is for sale."), true, false);
				return super.okMessage(myHost, msg);
			case CMMsg.TYP_LIST:
			{
				if (!CMLib.coffeeShops().ignoreIfNecessary(msg.source(), getFinalIgnoreMask(), this))
					return false;
				return super.stdMOBokMessage(myHost, msg);
			}
			default:
				break;
			}
		}
		return super.okMessage(myHost, msg);
	}

	@Override
	public int maxTimedListingDays()
	{
		return maxDays;
	}

	@Override
	public void setMaxTimedListingDays(final int d)
	{
		maxDays=d;
	}

	@Override
	public int maxListings()
	{
		return maxListings;
	}

	@Override
	public void setMaxListings(final int d)
	{
		maxListings=d;
	}
}
