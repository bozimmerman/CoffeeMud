package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class GenShopkeeper extends StdShopKeeper
{
	public String ID(){return "GenShopkeeper";}
	private String PrejudiceFactors="";
    private String IgnoreMask="";

	public GenShopkeeper()
	{
		super();
		Username="a generic shopkeeper";
		setDescription("He looks like he wants to sell something to you.");
		setDisplayText("A generic shopkeeper stands here.");
		baseEnvStats().setAbility(11); // his only off-default
	}

	public boolean isGeneric(){return true;}

	public String prejudiceFactors(){return PrejudiceFactors;}
	public void setPrejudiceFactors(String factors){PrejudiceFactors=factors;}
    public String ignoreMask(){return IgnoreMask;}
    public void setIgnoreMask(String factors){IgnoreMask=factors;}
	public String text()
	{
		if(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MOBCOMPRESS))
			miscText=CMEncoder.compressString(CoffeeMaker.getPropertiesStr(this,false));
		else
			miscText=CoffeeMaker.getPropertiesStr(this,false).getBytes();
		return super.text();
	}

	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		CoffeeMaker.resetGenMOB(this,newText);
	}
	private static String[] MYCODES={"WHATISELL","PREJUDICE","BUDGET","DEVALRATE","INVRESETRATE","IGNOREMASK"};
	public String getStat(String code)
	{
		if(CoffeeMaker.getGenMobCodeNum(code)>=0)
			return CoffeeMaker.getGenMobStat(this,code);
		switch(getCodeNum(code))
		{
		case 0: return ""+whatIsSold();
		case 1: return prejudiceFactors();
		case 2: return budget();
		case 3: return devalueRate();
		case 4: return ""+invResetRate();
        case 5: return ignoreMask();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		if(CoffeeMaker.getGenMobCodeNum(code)>=0)
			CoffeeMaker.setGenMobStat(this,code,val);
		else
		switch(getCodeNum(code))
		{
		case 0: setWhatIsSold(Util.s_int(val)); break;
		case 1: setPrejudiceFactors(val); break;
		case 2: setBudget(val); break;
		case 3: setDevalueRate(val); break;
		case 4: setInvResetRate(Util.s_int(val)); break;
        case 5: setIgnoreMask(val); break;
		}
	}
	protected int getCodeNum(String code){
		for(int i=0;i<MYCODES.length;i++)
			if(code.equalsIgnoreCase(MYCODES[i])) return i;
		return -1;
	}
	private static String[] codes=null;
	public String[] getStatCodes()
	{
		if(codes!=null) return codes;
		String[] superCodes=CoffeeMaker.GENMOBCODES;
		codes=new String[superCodes.length+MYCODES.length];
		int i=0;
		for(;i<superCodes.length;i++)
			codes[i]=superCodes[i];
		for(int x=0;x<MYCODES.length;i++,x++)
			codes[i]=MYCODES[x];
		return codes;
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenShopkeeper)) return false;
		String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		return true;
	}
}
