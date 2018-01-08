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
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine;
import com.planet_ink.coffee_mud.Libraries.interfaces.GenericBuilder;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.MsgMkrResolution;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.core.exceptions.HTTPRedirectException;

import java.util.*;
import java.io.IOException;

/*
   Copyright 2006-2018 Bo Zimmerman

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

public class StdPaper extends StdItem implements Book
{
	@Override
	public String ID()
	{
		return "StdPaper";
	}

	public StdPaper()
	{
		super();
		setName("a piece of paper");
		setDisplayText("a piece of paper sits here.");
		setDescription("Perhaps you can READ it. ");
		material=RawMaterial.RESOURCE_PAPER;
		basePhyStats().setSensesMask(PhyStats.SENSE_ITEMREADABLE);
		recoverPhyStats();
	}

	protected int	maxCharsPage	= 2048;	// 0=unlimited

	@Override
	public String readableText()
	{
		return text();
	}

	@Override
	public void setReadableText(String text)
	{
		super.setMiscText(text);
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

	@Override
	public int getMaxCharsPerPage()
	{
		return this.maxCharsPage;
	}

	@Override
	public void setMaxCharsPerPage(int max)
	{
		this.maxCharsPage = max;
	}
	
	@Override
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((msg.target()==this)
		&&(msg.targetMinor()==CMMsg.TYP_WRITE)
		&&(msg.targetMessage()!=null)
		&&(this.getMaxCharsPerPage()>0))
		{
			if((readableText().length() + msg.targetMessage().length()) > this.getMaxCharsPerPage())
			{
				msg.source().tell(L("There isn't enough space left on the paper to write all that.  It looks like you can fit @x1 more characters.",""+this.getMaxCharsPerPage()));
				return false;
			}
		}
		return true;
	}
}
