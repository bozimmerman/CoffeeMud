package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.Basic.GenDrink;
import java.util.*;

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
public class GenPotion extends StdPotion
{
	public String ID(){	return "GenPotion";}
	protected String readableText="";
	public GenPotion()
	{
		super();

		setName("a potion");
		baseEnvStats.setWeight(1);
		setDisplayText("A potion sits here.");
		setDescription("A strange potion with stranger markings.");
		secretIdentity="";
		baseGoldValue=200;
		recoverEnvStats();
		material=EnvResource.RESOURCE_GLASS;
	}



	public boolean isGeneric(){return true;}
	
	public String getSpellList()
	{ return readableText;}
	public void setSpellList(String list){readableText=list;}
	public String readableText(){return readableText;}
	public void setReadableText(String text){
		readableText=text;
		setSpellList(readableText);
	}
	
	public String text()
	{
		return CoffeeMaker.getPropertiesStr(this,false);
	}
	public int liquidType(){
		if((material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_LIQUID)
			return material();
		if(Util.s_int(readableText)==0) return EnvResource.RESOURCE_DRINKABLE;
		return Util.s_int(readableText);
	}
	public void setLiquidType(int newLiquidType){readableText=""+newLiquidType;}

	public void setMiscText(String newText)
	{
		miscText="";
		CoffeeMaker.setPropertiesStr(this,newText,false);
		recoverEnvStats();
	}
	private static String[] MYCODES={"HASLOCK","HASLID","CAPACITY","CONTAINTYPES",
							  "QUENCHED","LIQUIDHELD","LIQUIDTYPE"};
	public String getStat(String code)
	{
		if(CoffeeMaker.getGenItemCodeNum(code)>=0)
			return CoffeeMaker.getGenItemStat(this,code);
		switch(getCodeNum(code))
		{
		case 0: return ""+hasALock();
		case 1: return ""+hasALid();
		case 2: return ""+capacity();
		case 3: return ""+containTypes();
		case 4: return ""+thirstQuenched();
		case 5: return ""+liquidHeld();
		case 6: return ""+liquidType();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		if(CoffeeMaker.getGenItemCodeNum(code)>=0)
			CoffeeMaker.setGenItemStat(this,code,val);
		else
		switch(getCodeNum(code))
		{
		case 0: setLidsNLocks(hasALid(),isOpen(),Util.s_bool(val),false); break;
		case 1: setLidsNLocks(Util.s_bool(val),isOpen(),hasALock(),false); break;
		case 2: setCapacity(Util.s_int(val)); break;
		case 3: setContainTypes(Util.s_long(val)); break;
		case 4: setThirstQuenched(Util.s_int(val)); break;
		case 5: setLiquidHeld(Util.s_int(val)); break;
		case 6: setLiquidType(Util.s_int(val)); break;
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
		String[] superCodes=CoffeeMaker.GENITEMCODES;
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
		if(!(E instanceof GenPotion)) return false;
		String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		return true;
	}
}
