package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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

import java.util.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
public class GenScroll extends StdScroll
{
	public String ID(){	return "GenScroll";}
	protected String readableText="";

	public GenScroll()
	{
		super();

		setName("a scroll");
		baseEnvStats.setWeight(1);
		setDisplayText("a scroll is rolled up here.");
		setDescription("A rolled up parchment marked with mystical symbols.");
		secretIdentity="";
		baseGoldValue=200;
		recoverEnvStats();
		material=RawMaterial.RESOURCE_PAPER;
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
		return CMLib.coffeeMaker().getPropertiesStr(this,false);
	}

	public void setMiscText(String newText)
	{
		miscText="";
		CMLib.coffeeMaker().setPropertiesStr(this,newText,false);
		recoverEnvStats();
	}

    public String getStat(String code)
    {
        if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
            return CMLib.coffeeMaker().getGenItemStat(this,code);
        return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
    }
    public void setStat(String code, String val)
    {
        if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
            CMLib.coffeeMaker().setGenItemStat(this,code,val);
        CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code,val);
    }
    private static String[] codes=null;
    public String[] getStatCodes()
    {
        if(codes==null)
            codes=CMProps.getStatCodesList(GenericBuilder.GENITEMCODES,this);
        return codes; 
    }
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenScroll)) return false;
		for(int i=0;i<getStatCodes().length;i++)
			if(!E.getStat(getStatCodes()[i]).equals(getStat(getStatCodes()[i])))
				return false;
		return true;
	}
}
