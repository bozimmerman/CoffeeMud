package com.planet_ink.coffee_mud.Items.Basic;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
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
public class GenLightSource extends LightSource
{
	public String ID(){	return "GenLightSource";}
	protected String	readableText="";

	public GenLightSource()
	{
		super();

		setName("a generic lightable thing");
		setDisplayText("a generic lightable thing sits here.");
		setDescription("");
		destroyedWhenBurnedOut=true;
		setMaterial(EnvResource.RESOURCE_OAK);
		setDuration(200);
	}


	public void setDuration(int duration){readableText=""+duration;}
	public int getDuration(){return Util.s_int(readableText);}
	
	public boolean isGeneric(){return true;}

	public String text()
	{
		return CoffeeMaker.getPropertiesStr(this,false);
	}

	public String readableText(){return readableText;}
	public void setReadableText(String text){readableText=text;}
	public void setMiscText(String newText)
	{
		miscText="";
		CoffeeMaker.setPropertiesStr(this,newText,false);
		recoverEnvStats();
	}

	public String getStat(String code)
	{ return CoffeeMaker.getGenItemStat(this,code);}
	public void setStat(String code, String val)
	{ CoffeeMaker.setGenItemStat(this,code,val);}
	public String[] getStatCodes(){return CoffeeMaker.GENITEMCODES;}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenLightSource)) return false;
		for(int i=0;i<getStatCodes().length;i++)
			if(!E.getStat(getStatCodes()[i]).equals(getStat(getStatCodes()[i])))
				return false;
		return true;
	}
}
