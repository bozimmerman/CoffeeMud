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
public class GenMob extends StdMOB
{
	public String ID(){return "GenMob";}
	public GenMob()
	{
		super();
		Username="a generic mob";
		setDescription("");
		setDisplayText("A generic mob stands here.");

		baseEnvStats().setAbility(11); // his only off-default

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
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

	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		CoffeeMaker.resetGenMOB(this,newText);
	}
	public String getStat(String code)
	{ return CoffeeMaker.getGenMobStat(this,code);}
	public void setStat(String code, String val)
	{ CoffeeMaker.setGenMobStat(this,code,val);}
	public String[] getStatCodes(){return CoffeeMaker.GENMOBCODES;}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenMob)) return false;
		String[] theCodes=getStatCodes();
		for(int i=0;i<theCodes.length;i++)
			if(!E.getStat(theCodes[i]).equals(getStat(theCodes[i])))
				return false;
		return true;
	}
}
