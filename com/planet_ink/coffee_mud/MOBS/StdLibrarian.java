package com.planet_ink.coffee_mud.MOBS;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.MiniJSON.MJSONException;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2017-2017 Bo Zimmerman

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

public class StdLibrarian extends StdShopKeeper implements Librarian
{
	@Override
	public String ID()
	{
		return "StdLibrarian";
	}

	protected double	overdueCharge			= DEFAULT_MIN_OVERDUE_CHARGE;
	protected double	overdueChargePct		= DEFAULT_PCT_OVERDUE_CHARGE;
	protected double	dailyOverdueCharge		= DEFAULT_MIN_OVERDUE_DAILY;
	protected double	dailyOverdueChargePct	= DEFAULT_PCT_OVERDUE_DAILY;
	protected int		minOverdueDays			= DEFAULT_MIN_OVERDUE_DAYS;
	protected int		maxOverdueDays			= DEFAULT_MAX_OVERDUE_DAYS;
	protected int		maxBorrowedItems		= DEFAULT_MAX_BORROWED;
	protected String	contributorMask			= "";
	
	public StdLibrarian()
	{
		super();
		username="a librarian";
		setDescription("She\\`s just waiting for you to say something so she can shush you!");
		setDisplayText("The librarian is ready to help.");
		CMLib.factions().setAlignment(this,Faction.Align.GOOD);
		setMoney(0);
		whatIsSoldMask=ShopKeeper.DEAL_POSTMAN;
		basePhyStats.setWeight(150);
		setWimpHitPoint(0);

		baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,18);
		baseCharStats().setStat(CharStats.STAT_CHARISMA,7);

		basePhyStats().setArmor(0);

		baseState.setHitPoints(1000);

		recoverMaxState();
		resetToMaxState();
		recoverPhyStats();
		recoverCharStats();
	}

	public static class CheckedOutRecord
	{
		public String	playerName		= "";
		public String	itemName		= "";
		public long		mudDueDate		= 0;
		public double	charges			= 0.0;
		public long		mudReclaimDate	= 0;
	}

	protected String getLibraryChainKey()
	{
		return "LIBRARY_RECORDS_"+this.libraryChain().toUpperCase().replace(' ','_');
	}
	
	protected List<CheckedOutRecord> getCheckedOutRecords()
	{
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<CheckedOutRecord> records = (List)Resources.getResource(this.getLibraryChainKey());
		if(records == null)
		{
			records=new Vector<CheckedOutRecord>();
			Resources.submitResource(this.getLibraryChainKey(), records);
			final XMLLibrary xml = CMLib.xml();
			synchronized(records)
			{
				final List<PlayerData> pData = CMLib.database().DBReadPlayerDataEntry(this.getLibraryChainKey());
				for(final PlayerData data : pData)
				{
					try
					{
						for(XMLLibrary.XMLTag tag : xml.parseAllXML(data.xml()))
						{
							if(tag.tag().equalsIgnoreCase("OBJECT"))
							{
								CheckedOutRecord r = new CheckedOutRecord();
								xml.fromXMLtoPOJO(tag.contents(),r);
								records.add(r);
							}
						}
					}
					catch (IllegalArgumentException e)
					{
						Log.errOut(getLibraryChainKey(),e);
					}
				}
			}
		}
		return records;
	}

	protected void updateCheckedOutRecords()
	{
		List<CheckedOutRecord> records = this.getCheckedOutRecords();
		final StringBuilder json = new StringBuilder("");
		final XMLLibrary xml = CMLib.xml();
		for(int r=0;r<records.size();r++)
		{
			try
			{
				final CheckedOutRecord record = records.get(r);
				final String subXML = xml.fromPOJOtoXML(record);
				json.append("<OBJECT>");
				json.append(subXML);
				json.append("</OBJECT>");
			}
			catch(Exception e)
			{
				Log.errOut(getLibraryChainKey(),e);
			}
		}
		CMLib.database().DBReCreatePlayerData(getLibraryChainKey(), "LIBRARY_RECORDS", getLibraryChainKey(), json.toString());
	}

	@Override
	public String libraryChain()
	{
		return text();
	}

	@Override
	public void setLibraryChain(String name)
	{
		setMiscText(name);
	}

	@Override
	public double getOverdueCharge()
	{
		return overdueCharge;
	}

	@Override
	public void setOverdueCharge(double charge)
	{
		overdueCharge=charge;
	}

	@Override
	public double getDailyOverdueCharge()
	{
		return dailyOverdueCharge;
	}

	@Override
	public void setDailyOverdueCharge(double charge)
	{
		dailyOverdueCharge=charge;
	}

	@Override
	public double getOverdueChargePct()
	{
		return overdueChargePct;
	}

	@Override
	public void setOverdueChargePct(double pct)
	{
		overdueChargePct=pct;
	}

	@Override
	public double getDailyOverdueChargePct()
	{
		return dailyOverdueChargePct;
	}

	@Override
	public void setDailyOverdueChargePct(double pct)
	{
		dailyOverdueChargePct=pct;
	}

	@Override
	public int getMinOverdueDays()
	{
		return minOverdueDays;
	}

	@Override
	public void setMinOverdueDays(int days)
	{
		minOverdueDays=days;
	}

	@Override
	public int getMaxOverdueDays()
	{
		return maxOverdueDays;
	}

	@Override
	public void setMaxOverdueDays(int days)
	{
		maxOverdueDays=days;
	}

	@Override
	public String contributorMask()
	{
		return contributorMask;
	}

	@Override
	public void setContributorMask(String mask)
	{
		contributorMask=mask;
	}

	@Override
	protected void cloneFix(MOB E)
	{
		super.cloneFix(E);
	}

	@Override
	public CoffeeShop getShop()
	{
		//TODO: !!!!
		return shop;
	}

	@Override
	public void destroy()
	{
		super.destroy();
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return true;

		if((tickID==Tickable.TICKID_MOB)&&(getStartRoom()!=null))
		{
		}
		return true;
	}

	public void autoGive(MOB src, MOB tgt, Item I)
	{
		CMMsg msg2=CMClass.getMsg(src,I,null,CMMsg.MSG_DROP|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_DROP|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_DROP|CMMsg.MASK_INTERMSG,null);
		location().send(this,msg2);
		msg2=CMClass.getMsg(tgt,I,null,CMMsg.MSG_GET|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_GET|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_GET|CMMsg.MASK_INTERMSG,null);
		location().send(this,msg2);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		final MOB mob=msg.source();
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GIVE:
			case CMMsg.TYP_DEPOSIT:
				if(CMLib.flags().isAliveAwakeMobileUnbound(mob,true))
				{
					if(msg.tool() instanceof Container)
						((Container)msg.tool()).emptyPlease(true);
					final Session S=msg.source().session();
					if((!msg.source().isMonster())&&(S!=null)&&(msg.tool() instanceof Item))
					{
						autoGive(msg.source(),this,(Item)msg.tool());
						if(isMine(msg.tool()))
						{
							
						}
					}
				}
				return;
			case CMMsg.TYP_BORROW:
			case CMMsg.TYP_WITHDRAW:
				if(CMLib.flags().isAliveAwakeMobileUnbound(mob,true))
				{
					Item I=null;
					CMLib.commands().postSay(this,mob,L("There ya go!"),true,false);
					if(location()!=null)
						location().addItem(I,ItemPossessor.Expire.Player_Drop);
					final CMMsg msg2=CMClass.getMsg(mob,I,this,CMMsg.MSG_GET,null);
					if(location().okMessage(mob,msg2))
						location().send(mob,msg2);
				}
				return;
			case CMMsg.TYP_VALUE:
			case CMMsg.TYP_SELL:
			case CMMsg.TYP_VIEW:
				super.executeMsg(myHost,msg);
				return;
			case CMMsg.TYP_BUY:
				super.executeMsg(myHost,msg);
				return;
			case CMMsg.TYP_SPEAK:
			{
				super.executeMsg(myHost,msg);
				CMStrings.getSayFromMessage(msg.targetMessage());
				return;
			}
			case CMMsg.TYP_LIST:
			{
				super.executeMsg(myHost,msg);
				if(CMLib.flags().isAliveAwakeMobileUnbound(mob,true))
				{
					return;
				}
				break;
			}
			default:
				break;
			}
		}
		super.executeMsg(myHost,msg);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		final MOB mob=msg.source();
		if((msg.targetMinor()==CMMsg.TYP_EXPIRE)
		&&(msg.target()==location())
		&&(CMLib.flags().isInTheGame(this,true)))
			return false;
		else
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GIVE:
			case CMMsg.TYP_DEPOSIT:
				{
					if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),finalIgnoreMask(),this))
						return false;
					if(msg.tool()==null)
						return false;
					if(!(msg.tool() instanceof Item))
					{
						mob.tell(L("@x1 doesn't look interested.",mob.charStats().HeShe()));
						return false;
					}
					if(CMLib.flags().isEnspelled((Item)msg.tool()) || CMLib.flags().isOnFire((Item)msg.tool()))
					{
						mob.tell(this,msg.tool(),null,L("<S-HE-SHE> refuses to accept <T-NAME> for delivery."));
						return false;
					}
				}
				return true;
			case CMMsg.TYP_WITHDRAW:
				{
					if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),finalIgnoreMask(),this))
						return false;
					if((msg.tool()==null)||(!(msg.tool() instanceof Item)))
					{
						CMLib.commands().postSay(this,mob,L("What do you want? I'm busy!"),true,false);
						return false;
					}
					if((msg.tool()!=null)&&(!msg.tool().okMessage(myHost,msg)))
						return false;
				}
				return true;
			case CMMsg.TYP_VALUE:
			case CMMsg.TYP_SELL:
			case CMMsg.TYP_VIEW:
				return super.okMessage(myHost,msg);
			case CMMsg.TYP_BUY:
				return super.okMessage(myHost,msg);
			case CMMsg.TYP_LIST:
			{
				if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),finalIgnoreMask(),this))
					return false;
				return true;
			}
			default:
				break;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public int getMaxBorrowed()
	{
		return maxBorrowedItems;
	}

	@Override
	public void setMaxBorrowed(int items)
	{
		maxBorrowedItems=items;
	}
}
