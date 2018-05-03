package com.planet_ink.coffee_mud.Items.BasicTech;
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
import com.planet_ink.coffee_mud.Items.interfaces.Armor.SizeDeviation;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

import java.util.*;

/*
   Copyright 2013-2018 Bo Zimmerman

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
public class GenTickerShield extends StdElecItem implements Armor
{

	@Override
	public String ID()
	{
		return "GenTickerShield";
	}

	protected short	layer				= 0;
	protected short	layerAttributes		= 0;
	protected String	readableText	= "";

	public GenTickerShield()
	{
		super();
		setName("a personal field generator");
		basePhyStats.setWeight(2);
		setDisplayText("a personal field generator sits here.");
		setDescription("");
		baseGoldValue=2500;
		basePhyStats().setLevel(1);
		recoverPhyStats();
		setMaterial(RawMaterial.RESOURCE_STEEL);
		super.activate(true);
		super.setRawProperLocationBitmap(Wearable.WORN_ABOUT_BODY);
		super.setPowerCapacity(100);
		super.setPowerRemaining(100);
	}

	protected String fieldOnStr(MOB viewerM)
	{
		return L("A field surrounds <O-NAME>.");
	}

	protected String fieldDeadStr(MOB viewerM)
	{
		return L("The around <S-NAME> flickers and dies out as <S-HE-SHE> fade(s) back into view.");
	}

	@Override
	public TechType getTechType()
	{
		return TechType.PERSONAL_SHIELD;
	}

	@Override
	public void setOwner(ItemPossessor owner)
	{
		final ItemPossessor prevOwner=super.owner;
		super.setOwner(owner);
		if((prevOwner != owner)&&(owner!=null))
		{
			if(!CMLib.threads().isTicking(this, Tickable.TICKID_ELECTRONICS))
				CMLib.threads().startTickDown(this, Tickable.TICKID_ELECTRONICS, 1);
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(activated() && (tickID==Tickable.TICKID_ELECTRONICS))
		{
			if(!amWearingAt(Wearable.IN_INVENTORY))
			setPowerRemaining(powerRemaining()-1);
			if(powerRemaining()<=0)
			{
				setPowerRemaining(0);
				if(owner() instanceof MOB)
				{
					final MOB mob=(MOB)owner();
					final CMMsg msg=CMClass.getMsg(mob, this, null,CMMsg.MSG_OK_VISUAL,CMMsg.TYP_DEACTIVATE|CMMsg.MASK_ALWAYS,CMMsg.MSG_OK_VISUAL,fieldDeadStr(mob));
					if(mob.location()!=null)
						mob.location().send(mob, msg);
				}
				else
					activate(false);
			}
		}
		return !amDestroyed();
	}

	@Override
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if(msg.amITarget(owner()) && (owner() instanceof MOB))
		{
			final MOB mob=(MOB)owner();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_REMOVE:
			case CMMsg.TYP_DROP:
				if(activated())
					msg.addTrailerMsg(CMClass.getMsg(mob, this, null,CMMsg.MSG_OK_VISUAL,CMMsg.TYP_DEACTIVATE|CMMsg.MASK_ALWAYS,CMMsg.MSG_OK_VISUAL,fieldDeadStr(msg.source())));
				break;
			}
		}
		return true;
	}

	@Override
	public SizeDeviation getSizingDeviation(MOB mob)
	{
		return SizeDeviation.FITS;
	}
	
	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_LOOK:
				super.executeMsg(host, msg);
				if(CMLib.flags().canBeSeenBy(this, msg.source()))
				{
					msg.source().tell(L("@x1 is currently @x2 and is at @x3% power.",name(),(activated()?"activated":"deactivated"),""+Math.round(CMath.div(powerRemaining(),powerCapacity())*100.0)));
				}
				return;
			case CMMsg.TYP_ACTIVATE:
			{
				if((msg.source().location()!=null)&&(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG)))
					msg.source().location().show(msg.source(), this, owner(), CMMsg.MSG_OK_VISUAL, fieldOnStr(null));
				this.activate(true);
				final Physical P=owner();
				if(P!=null)
				{
					P.recoverPhyStats();
					if(P instanceof MOB)
					{
						((MOB)P).recoverCharStats();
						((MOB)P).recoverMaxState();
					}
				}
				break;
			}
			case CMMsg.TYP_DEACTIVATE:
			{
				if((msg.source().location()!=null)&&(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG)))
					msg.source().location().show(msg.source(), this, owner(), CMMsg.MSG_OK_VISUAL, fieldDeadStr(null));
				this.activate(false);
				final Physical P=owner();
				if(P!=null)
				{
					P.recoverPhyStats();
					if(P instanceof MOB)
					{
						((MOB)P).recoverCharStats();
						((MOB)P).recoverMaxState();
					}
				}
				break;
			}
			}
		}
		else if(msg.amITarget(owner()) && (owner() instanceof MOB) && (!amWearingAt(Wearable.IN_INVENTORY)))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_LOOK:
				super.executeMsg(host, msg);
				if(CMLib.flags().canBeSeenBy(owner(), msg.source()) &&(activated())&&(powerRemaining()>0))
					msg.source().tell(msg.source(),this,owner(),fieldOnStr(msg.source()));
				return;
			}
		}
		super.executeMsg(host, msg);
	}

	@Override
	public short getClothingLayer()
	{
		return layer;
	}

	@Override
	public void setClothingLayer(short newLayer)
	{
		layer = newLayer;
	}

	@Override
	public short getLayerAttributes()
	{
		return layerAttributes;
	}

	@Override
	public void setLayerAttributes(short newAttributes)
	{
		layerAttributes = newAttributes;
	}

	@Override
	public boolean canWear(MOB mob, long where)
	{
		if(where==0)
			return (whereCantWear(mob)==0);
		if((rawProperLocationBitmap()&where)!=where)
			return false;
		return mob.freeWearPositions(where,getClothingLayer(),getLayerAttributes())>0;
	}

	@Override
	public boolean isGeneric()
	{
		return true;
	}

	@Override
	public String text()
	{
		return CMLib.coffeeMaker().getPropertiesStr(this, false);
	}

	@Override
	public String readableText()
	{
		return readableText;
	}

	@Override
	public void setReadableText(String text)
	{
		readableText = text;
	}

	@Override
	public void setMiscText(String newText)
	{
		miscText="";
		CMLib.coffeeMaker().setPropertiesStr(this,newText,false);
		recoverPhyStats();
	}

	private final static String[] MYCODES={"POWERCAP","ACTIVATED","POWERREM","MANUFACTURER","LAYER","LAYERATTRIB","TECHLEVEL"};
	@Override
	public String getStat(String code)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			return CMLib.coffeeMaker().getGenItemStat(this,code);
		switch(getCodeNum(code))
		{
		case 0:
			return "" + powerCapacity();
		case 1:
			return "" + activated();
		case 2:
			return "" + powerRemaining();
		case 3:
			return "" + getManufacturerName();
		case 4:
			return "" + getClothingLayer();
		case 5:
			return "" + getLayerAttributes();
		case 6:
			return "" + techLevel();
		default:
			return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}

	@Override
	public void setStat(String code, String val)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			CMLib.coffeeMaker().setGenItemStat(this,code,val);
		else
		switch(getCodeNum(code))
		{
		case 0:
			setPowerCapacity(CMath.s_parseLongExpression(val));
			break;
		case 1:
			activate(CMath.s_bool(val));
			break;
		case 2:
			setPowerRemaining(CMath.s_parseLongExpression(val));
			break;
		case 3:
			setManufacturerName(val);
			break;
		case 4:
			setClothingLayer((short) CMath.s_parseIntExpression(val));
			break;
		case 5:
			setLayerAttributes((short) CMath.s_parseListLongExpression(Armor.LAYERMASK_DESCS, val));
			break;
		case 6:
			setTechLevel(CMath.s_parseIntExpression(val));
			break;
		default:
			CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
			break;
		}
	}

	@Override
	protected int getCodeNum(String code)
	{
		for(int i=0;i<MYCODES.length;i++)
		{
			if(code.equalsIgnoreCase(MYCODES[i]))
				return i;
		}
		return -1;
	}

	private static String[] codes=null;

	@Override
	public String[] getStatCodes()
	{
		if(codes!=null)
			return codes;
		final String[] MYCODES=CMProps.getStatCodesList(GenTickerShield.MYCODES,this);
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
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenTickerShield))
			return false;
		final String[] theCodes=getStatCodes();
		for(int i=0;i<theCodes.length;i++)
		{
			if(!E.getStat(theCodes[i]).equals(getStat(theCodes[i])))
				return false;
		}
		return true;
	}
}
