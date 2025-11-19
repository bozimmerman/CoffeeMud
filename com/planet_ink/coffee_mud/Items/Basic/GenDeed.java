package com.planet_ink.coffee_mud.Items.Basic;
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

public class GenDeed extends StdItem implements PrivateProperty
{
	@Override
	public String ID()
	{
		return "StdDeed";
	}

	@Override
	public String displayText()
	{
		return "an official looking document sits here";
	}

	protected String readableText="";
	protected String owner = "";
	protected int price = 0;
	protected String deedId = "";

	@Override
	public String genericName()
	{
		if(CMLib.english().startsWithAnIndefiniteArticle(name())&&(CMStrings.numWords(name())<4))
			return CMStrings.removeColors(name());
		return L("a deed");
	}

	@Override
	public int baseGoldValue()
	{
		return getPrice();
	}

	protected static final String	CANCEL_WORD	= "CANCEL";

	@Override
	public int value()
	{
		if(name().indexOf("(Copy)")>=0)
			baseGoldValue=10;
		else
			baseGoldValue=getPrice();
		return baseGoldValue;
	}

	public GenDeed()
	{
		super();
		setName("a standard deed");
		setDescription("Give or Sell this deed to transfer ownership. **DON`T LOSE THIS!**");
		baseGoldValue=10000;
		basePhyStats().setSensesMask(PhyStats.SENSE_ITEMREADABLE);
		setMaterial(RawMaterial.RESOURCE_PAPER);
		recoverPhyStats();
	}

	@Override
	public int getPrice()
	{
		return price;
	}

	@Override
	public void setPrice(final int price)
	{
		this.price=price;
	}

	@Override
	public boolean isProperlyOwned()
	{
		final String owner=getOwnerName();
		if(owner.length()==0)
			return false;
		final Clan C=CMLib.clans().fetchClanAnyHost(owner);
		if(C!=null)
			return true;
		return CMLib.players().playerExistsAllHosts(owner);
	}

	@Override
	public String getOwnerName()
	{
		return owner;
	}

	@Override
	public void setOwnerName(final String owner)
	{
		this.owner = owner;
	}

	@Override
	public String getTitleID()
	{
		return deedId;
	}

	@Override
	public void recoverPhyStats()
	{
		CMLib.flags().setReadable(this, true);
		super.recoverPhyStats();
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(((msg.targetMinor()==CMMsg.TYP_WRITE)||(msg.targetMinor()==CMMsg.TYP_REWRITE))
		&&(msg.amITarget(this)))
		{
			final MOB mob=msg.source();
			mob.tell(L("You shouldn't write on @x1.",name()));
			return false;
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_GET)
		&&(msg.amITarget(this))
		&&(msg.tool() instanceof ShopKeeper))
		{

		}
		else
		if((msg.targetMinor()==CMMsg.TYP_BUY)
		&&(msg.target() instanceof MOB)
		&&(msg.tool()==this))
		{
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_WITHDRAW)
		&&(msg.target() instanceof MOB)
		&&(msg.tool()==this))
		{
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.amITarget(this))
		&&(msg.targetMinor()==CMMsg.TYP_READ))
		{
			if(CMLib.flags().canBeSeenBy(this,msg.source()))
			{
				if((getOwnerName()==null)||(getOwnerName().length()==0))
					msg.source().tell(L("It states that the property herein known as '@x1' is available for ownership.",name()));
				else
					msg.source().tell(L("It states that the property herein known as '@x1' is deeded to @x2.",name(),getOwnerName()));
			}
			else
				msg.source().tell(L("You can't see that!"));
			msg.modify(msg.source(),msg.target(),msg.tool(),msg.sourceCode(),msg.sourceMessage(),msg.targetCode(),CANCEL_WORD,msg.othersCode(),msg.othersMessage());
		}

		super.executeMsg(myHost,msg);

		if((msg.targetMinor()==CMMsg.TYP_SELL)
		&&(msg.tool()==this)
		&&(msg.target() instanceof ShopKeeper))
		{
			setOwnerName("");
			recoverPhyStats();
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_GIVE)
		&&(msg.tool()==this)
		&&(getOwnerName().length()>0)
		&&((msg.source().Name().equals(getOwnerName()))
			||(msg.source().getLiegeID().equals(getOwnerName())&&msg.source().isMarriedToLiege())
			||(CMLib.clans().checkClanPrivilege(msg.source(), getOwnerName(), Clan.Function.PROPERTY_OWNER)))
		&&(msg.target() instanceof MOB)
		&&(!(msg.target() instanceof Banker))
		&&(!(msg.target() instanceof Auctioneer))
		&&(!(msg.target() instanceof Librarian))
		&&(!(msg.target() instanceof PostOffice)))
		{
			final CMMsg buyMsg = CMClass.getMsg((MOB)msg.target(),msg.source(),this,CMMsg.MSG_BUY,CMMsg.MSG_BUY,CMMsg.MSG_BUY,"");
			if(!msg.target().okMessage(msg.target(), buyMsg))
			{
				msg.source().tell(L("@x1 can not seem to accept @x2.",((MOB)msg.target()).name(msg.source()),name()));
				return;
			}
			setOwnerName(msg.target().Name());
			msg.source().tell(L("@x1 is now signed over to @x2.",name(),getOwnerName()));
			recoverPhyStats();
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_GET)
		&&(msg.amITarget(this))
		&&(msg.tool() instanceof ShopKeeper))
		{
			if(getOwnerName().length()==0)
			{
				final String newOwnerName=msg.source().Name();
				setOwnerName(newOwnerName);
				msg.source().tell(L("@x1 is now signed over to @x2.",name(),getOwnerName()));
			}
			recoverPhyStats();
		}
	}

	@Override
	public boolean isGeneric()
	{
		return true;
	}

	@Override
	public String text()
	{
		return CMLib.coffeeMaker().getEnvironmentalMiscTextXML(this, false);
	}

	@Override
	public String readableText()
	{
		final Map<String,Object> m = new TreeMap<String,Object>();
		m.put("PRICE",Integer.valueOf(price));
		m.put("OWNER",owner);
		m.put("ID",deedId);
		return CMParms.toEqListString(m);
	}

	@Override
	public void setReadableText(final String text)
	{
		setPrice(CMParms.getParmInt(text, "PRICE", price));
		this.setOwnerName(CMParms.getParmStr(text, "OWNER", owner));
		this.deedId=CMStrings.deEscape(CMParms.getParmStr(text, "ID", deedId));
	}

	@Override
	public void setMiscText(final String newText)
	{
		miscText="";
		CMLib.coffeeMaker().unpackEnvironmentalMiscTextXML(this,newText,false);
		recoverPhyStats();
	}

	private final static String[] MYCODES={"OWNER","PRICE"};

	private int getInternalCodeNum(final String code)
	{
		for(int i=0;i<MYCODES.length;i++)
		{
			if(code.equalsIgnoreCase(MYCODES[i]))
				return i;
		}
		return -1;
	}

	@Override
	public String getStat(final String code)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			return CMLib.coffeeMaker().getGenItemStat(this,code);
		switch(getInternalCodeNum(code))
		{
		case 0:
			return this.getOwnerName();
		case 1:
			return ""+this.getPrice();
		default:
			return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}

	@Override
	public void setStat(final String code, final String val)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			CMLib.coffeeMaker().setGenItemStat(this,code,val);
		switch(getInternalCodeNum(code))
		{
		case 0:
			this.setOwnerName(val);
			break;
		case 1:
			setPrice(CMath.s_int(val));
			break;
		default:
			CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code,val);
			break;
		}
	}

	private static String[]	codes	= null;

	@Override
	public String[] getStatCodes()
	{
		if(codes!=null)
			return codes;
		final String[] MYCODES=CMProps.getStatCodesList(GenDeed.MYCODES,this);
		final String[] superCodes=CMParms.toStringArray(GenericBuilder.GenItemCode.values());
		codes=new String[superCodes.length+MYCODES.length];
		int i=0;
		for(;i<superCodes.length;i++)
			codes[i]=superCodes[i];
		for(int x=0;x<MYCODES.length;i++,x++)
			codes[i]=MYCODES[x];
		return codes;
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof GenDeed))
			return false;
		for(int i=0;i<getStatCodes().length;i++)
		{
			if(!E.getStat(getStatCodes()[i]).equals(getStat(getStatCodes()[i])))
				return false;
		}
		return true;
	}
}
