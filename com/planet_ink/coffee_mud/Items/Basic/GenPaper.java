package com.planet_ink.coffee_mud.Items.Basic;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/*
   Copyright 2017-2017 Bo Zimmerman

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
public class GenPaper extends StdItem implements Book
{
	@Override
	public String ID()
	{
		return "GenPaper";
	}

	protected String readableText="";
	public GenPaper()
	{
		super();
		setDisplayText("a piece of paper sits here.");
		setDescription("You should try reading it. ");
		basePhyStats().setSensesMask(PhyStats.SENSE_ITEMREADABLE);
		recoverPhyStats();
		setMaterial(RawMaterial.RESOURCE_PAPER);
	}

	@Override
	public boolean isGeneric()
	{
		return true;
	}

	@Override
	public String text()
	{
		return CMLib.coffeeMaker().getPropertiesStr(this,false);
	}

	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
	}

	@Override
	public String readableText()
	{
		return readableText;
	}

	@Override
	public void setReadableText(String text)
	{
		readableText=text;
	}

	@Override
	public void setMiscText(String newText)
	{
		miscText="";
		CMLib.coffeeMaker().setPropertiesStr(this,newText,false);
		recoverPhyStats();
	}

	@Override
	public String getStat(String code)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			return CMLib.coffeeMaker().getGenItemStat(this,code);
		return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
	}

	@Override
	public void setStat(String code, String val)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			CMLib.coffeeMaker().setGenItemStat(this,code,val);
		CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
	}

	private static String[] codes=null;

	@Override
	public String[] getStatCodes()
	{
		if(codes==null)
			codes=CMProps.getStatCodesList(CMParms.toStringArray(GenericBuilder.GenItemCode.values()),this);
		return codes;
	}

	@Override
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenPaper))
			return false;
		for(int i=0;i<getStatCodes().length;i++)
		{
			if(!E.getStat(getStatCodes()[i]).equals(getStat(getStatCodes()[i])))
				return false;
		}
		return true;
	}
	
	@Override
	public int getUsedPages()
	{
		return readableText().length()>0 ? 1 : 0;
	}
	
	@Override
	public int getMaxPages()
	{
		return 1;
	}
	
	@Override
	public void setMaxPages(int max)
	{
	}
	
	@Override
	public String getRawContent(int page)
	{
		if(page == 1)
			return readableText();
		return "";
	}
	
	@Override
	public String getContent(int page)
	{
		if(page == 1)
			return readableText();
		return "";
	}
	
	@Override
	public void addRawContent(String authorName, String content)
	{
		if(content.startsWith("::")&&(content.length()>2)&&(content.charAt(2)!=':'))
		{
			int x=content.indexOf("::",2);
			if(x>2)
				this.setReadableText(this.readableText()+L("\n\rSubject: ")+content.substring(2,x)+"\n\r"+content.substring(x+2));
			else
				this.setReadableText(this.readableText()+content);
		}
		else
			this.setReadableText(this.readableText()+content);
	}
	
	@Override
	public boolean isJournal()
	{
		return false;
	}
}
