package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

import java.util.*;


/*
   Copyright 2021-2022 Bo Zimmerman

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
public class GenBoardable extends StdBoardable
{
	@Override
	public String ID()
	{
		return "GenBoardable";
	}

	@Override
	public boolean isGeneric()
	{
		return true;
	}

	@Override
	public String genericName()
	{
		if(CMLib.english().startsWithAnIndefiniteArticle(name())&&(CMStrings.numWords(name())<4))
			return CMStrings.removeColors(name());
		return L("a thing you can board");
	}

	private final static String[] MYCODES=
	{
		"HASLOCK","HASLID","CAPACITY","CONTAINTYPES", "RESETTIME","RIDEBASIS","MOBSHELD",
		"AREA","OWNER","PRICE","DEFCLOSED","DEFLOCKED", "EXITNAME",
		"PUTSTR","MOUNTSTR","DISMOUNTSTR","STATESTR","STATESUBJSTR","RIDERSTR"
	};

	private int getInternalCodeNum(final String code)
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
		final String[] MYCODES=CMProps.getStatCodesList(GenBoardable.MYCODES,this);
		final String[] superCodes=CMParms.toStringArray(GenericBuilder.GenItemCode.values());
		codes=new String[superCodes.length+MYCODES.length];
		int i=0;
		for(;i<superCodes.length;i++)
			codes[i]=superCodes[i];
		for(int x=0;x<MYCODES.length;i++,x++)
			codes[i]=MYCODES[x];
		return codes;
	}

	protected boolean isLocalStatCode(final String str)
	{
		if(codes == null)
			getStatCodes();
		if((str == null)||(str.length()==0))
			return false;
		return CMParms.contains(codes, str.toUpperCase().trim());
	}

	@Override
	public String getStat(final String code)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			return CMLib.coffeeMaker().getGenItemStat(this,code);
		switch(getInternalCodeNum(code))
		{
		case 0:
			return "" + hasALock();
		case 1:
			return "" + hasADoor();
		case 2:
			return "" + capacity();
		case 3:
			return "" + containTypes();
		case 4:
			return "" + openDelayTicks();
		case 5:
			return "" + rideBasis();
		case 6:
			return "" + riderCapacity();
		case 7:
			return CMLib.coffeeMaker().getAreaObjectXML(getArea(), null, null, null, true).toString();
		case 8:
			return getOwnerName();
		case 9:
			return "" + getPrice();
		case 10:
			return "" + defaultsClosed();
		case 11:
			return "" + defaultsLocked();
		case 12:
			return "" + doorName();
		case 13:
			return this.getPutString();
		case 14:
			return this.getMountString();
		case 15:
			return this.getDismountString();
		case 16:
			return this.getStateString();
		case 17:
			return this.getStateStringSubject();
		case 18:
			return this.getRideString();
		default:
			return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}

	@Override
	public void setStat(final String code, final String val)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			CMLib.coffeeMaker().setGenItemStat(this,code,val);
		else
		switch(getInternalCodeNum(code))
		{
		case 0:
			setDoorsNLocks(hasADoor(), isOpen(), defaultsClosed(), CMath.s_bool(val), false, CMath.s_bool(val) && defaultsLocked());
			break;
		case 1:
			setDoorsNLocks(CMath.s_bool(val), isOpen(), CMath.s_bool(val) && defaultsClosed(), hasALock(), isLocked(), defaultsLocked());
			break;
		case 2:
			setCapacity(CMath.s_parseIntExpression(val));
			break;
		case 3:
			setContainTypes(CMath.s_parseBitLongExpression(Container.CONTAIN_DESCS, val));
			break;
		case 4:
			setOpenDelayTicks(CMath.s_parseIntExpression(val));
			break;
		case 5:
			break;
		case 6:
			break;
		case 7:
			setArea(val);
			break;
		case 8:
			setOwnerName(val);
			break;
		case 9:
			setPrice(CMath.s_int(val));
			break;
		case 10:
			setDoorsNLocks(hasADoor(), isOpen(), CMath.s_bool(val), hasALock(), isLocked(), defaultsLocked());
			break;
		case 11:
			setDoorsNLocks(hasADoor(), isOpen(), defaultsClosed(), hasALock(), isLocked(), CMath.s_bool(val));
			break;
		case 12:
			this.doorName = val;
			break;
		case 13:
			setPutString(val);
			break;
		case 14:
			setMountString(val);
			break;
		case 15:
			setDismountString(val);
			break;
		case 16:
			setStateString(val);
			break;
		case 17:
			setStateStringSubject(val);
			break;
		case 18:
			setRideString(val);
			break;
		default:
			CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
			break;
		}
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof GenBoardable))
			return false;
		final String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
		{
			if((!E.getStat(codes[i]).equals(getStat(codes[i])))
			&&(!codes[i].equals("AREA"))
			&&(!codes[i].equals("ABILITY")))
				return false;
		}
		final Area eA = ((GenBoardable)E).getArea();
		if(eA==null)
			return getArea()==null;
		final Area A = this.getArea();
		if(A==null)
			return false;
		final Enumeration<Room> er = eA.getProperMap();
		final Enumeration<Room> r = A.getProperMap();
		for(;r.hasMoreElements();)
		{
			final Room R=r.nextElement();
			if(!er.hasMoreElements())
				return false;
			final Room eR = er.nextElement();
			if(!R.sameAs(eR))
				return false;
			final Enumeration<Item> i=R.items();
			final Enumeration<Item> ei = eR.items();
			for(;i.hasMoreElements();)
			{
				final Item I=i.nextElement();
				if(!ei.hasMoreElements())
					return false;
				final Item eI=ei.nextElement();
				if(!I.sameAs(eI))
					return false;
			}
			if(ei.hasMoreElements())
				return false;
			final Enumeration<MOB> m=R.inhabitants();
			final Enumeration<MOB> em = eR.inhabitants();
			for(;m.hasMoreElements();)
			{
				final MOB M=m.nextElement();
				if(!em.hasMoreElements())
					return false;
				final MOB eM=em.nextElement();
				if(!M.sameAs(eM))
					return false;
			}
			if(em.hasMoreElements())
				return false;
		}
		if(er.hasMoreElements())
			return false;
		return true;
	}
}
