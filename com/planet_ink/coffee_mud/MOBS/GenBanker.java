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
public class GenBanker extends StdBanker
{
	public String ID(){return "GenBanker";}
	private String PrejudiceFactors="";
	private String bankChain="GenBank";

	public GenBanker()
	{
		super();
		Username="a generic banker";
		setDescription("He looks like he wants your money.");
		setDisplayText("A generic banker stands here.");
		baseEnvStats().setAbility(11); // his only off-default
	}

	public boolean isGeneric(){return true;}

	public String text()
	{
		if(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MOBCOMPRESS))
			miscText=Util.compressString(CoffeeMaker.getPropertiesStr(this,false));
		else
			miscText=CoffeeMaker.getPropertiesStr(this,false).getBytes();
		return super.text();
	}

	public String prejudiceFactors(){return PrejudiceFactors;}
	public void setPrejudiceFactors(String factors){PrejudiceFactors=factors;}
	public String bankChain(){return bankChain;}
	public void setBankChain(String name){bankChain=name;}

	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		CoffeeMaker.resetGenMOB(this,newText);
	}
	private static String[] MYCODES={"WHATISELL","PREJUDICE","BANKCHAIN","COININT","ITEMINT"};
	public String getStat(String code)
	{
		if(CoffeeMaker.getGenMobCodeNum(code)>=0)
			return CoffeeMaker.getGenMobStat(this,code);
		else
		switch(getCodeNum(code))
		{
		case 0: return ""+whatIsSold();
		case 1: return prejudiceFactors();
		case 2: return bankChain();
		case 3: return ""+getCoinInterest();
		case 4: return ""+getItemInterest();
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
		case 2: setBankChain(val); break;
		case 3: setCoinInterest(Util.s_double(val)); break;
		case 4: setItemInterest(Util.s_double(val)); break;
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
		if(!(E instanceof GenBanker)) return false;
		String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		return true;
	}
}
