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
public class GenAmmunition extends StdItem implements Ammunition
{
	public String ID(){	return "GenAmmunition";}
	protected String	readableText="";
	public GenAmmunition()
	{
		super();

		setName("a batch of arrows");
		setDisplayText("a generic batch of arrows sits here.");
		setUsesRemaining(100);
		setAmmunitionType("arrows");
		setDescription("");
		recoverEnvStats();
	}

	public boolean isGeneric(){return true;}

	public String text()
	{
		return CoffeeMaker.getPropertiesStr(this,false);
	}
	public String readableText(){return readableText;}
	public void setReadableText(String text)
	{
		if(Sense.isReadable(this)) Sense.setReadable(this,false);
		readableText=text;
	}
	public String ammunitionType(){return readableText;}
	public void setAmmunitionType(String text){readableText=text;}

	public void setMiscText(String newText)
	{
		miscText="";
		CoffeeMaker.setPropertiesStr(this,newText,false);
		recoverEnvStats();
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		MOB mob=msg.source();
		if(!msg.amITarget(this))
			return super.okMessage(myHost,msg);
		else
		if(msg.targetCode()==CMMsg.NO_EFFECT)
			return super.okMessage(myHost,msg);
		else
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_HOLD:
			mob.tell("You can't hold "+name()+".");
			return false;
		case CMMsg.TYP_WEAR:
			mob.tell("You can't wear "+name()+".");
			return false;
		case CMMsg.TYP_WIELD:
			mob.tell("You can't wield "+name()+" as a weapon.");
			return false;
		}
		return super.okMessage(myHost,msg);
	}
}
