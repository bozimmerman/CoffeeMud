package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.Basic.GenItem;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class GenWand extends StdWand
{
	public String ID(){	return "GenWand";}
	private String secretWord=StdWand.words[Dice.roll(1,StdWand.words.length,0)-1];
	protected String readableText="";
	
	public GenWand()
	{
		super();

		setName("a wand");
		setDisplayText("a simple wand is here.");
		setDescription("A wand made out of wood.");
		secretIdentity=null;
		setUsesRemaining(0);
		baseGoldValue=20000;
		baseEnvStats().setLevel(12);
		Sense.setReadable(this,false);
		material=EnvResource.RESOURCE_OAK;
		recoverEnvStats();
	}

	public boolean isGeneric(){return true;}

	public void setSpell(Ability theSpell)
	{
		readableText="";
		if(theSpell!=null)
			readableText=theSpell.ID();
		secretWord=StdWand.getWandWord(readableText);
	}
	public Ability getSpell()
	{
		return CMClass.getAbility(readableText);
	}

	public void setReadableText(String text)
	{
		super.setReadableText(text);
		secretWord=StdWand.getWandWord(readableText);
	}

	public String text()
	{
		return CoffeeMaker.getPropertiesStr(this,false);
	}

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
		if(!(E instanceof GenWand)) return false;
		for(int i=0;i<getStatCodes().length;i++)
			if(!E.getStat(getStatCodes()[i]).equals(getStat(getStatCodes()[i])))
				return false;
		return true;
	}
}
