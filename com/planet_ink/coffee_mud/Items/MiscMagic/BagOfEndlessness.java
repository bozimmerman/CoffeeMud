package com.planet_ink.coffee_mud.Items.MiscMagic;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.List;
import java.util.Vector;

/*
   Copyright 2003-2018 Bo Zimmerman

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

public class BagOfEndlessness extends BagOfHolding implements ArchonOnly
{
	@Override
	public String ID()
	{
		return "BagOfEndlessness";
	}

	public BagOfEndlessness()
	{
		super();

		setName("a small sack");
		setDisplayText("a small black sack is crumpled up here.");
		setDescription("A nice silk sack to put your things in.");
		secretIdentity="The Bag of Endless Stuff";
		basePhyStats().setLevel(1);
		capacity=Integer.MAX_VALUE-1000;

		baseGoldValue=10000;
		basePhyStats().setDisposition(basePhyStats().disposition()|PhyStats.IS_BONUS);
		recoverPhyStats();
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if(msg.amITarget(this)&&(msg.tool() instanceof Item))
		{
			final Item newitem=(Item)msg.tool();
			if((newitem.container()==this)&&(newitem.owner() !=null))
			{
				if((!CMSecurity.isAllowedAnywhere(msg.source(), CMSecurity.SecFlag.COPYITEMS))
				&&(!CMSecurity.isAllowedAnywhere(msg.source(), CMSecurity.SecFlag.CMDITEMS)))
				{
					msg.source().tell(L("You aren't allowed to do that."));
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(this)&&(msg.tool() instanceof Item))
		{
			final Item newitem=(Item)msg.tool();
			if((newitem.container()==this)
			&&(newitem.owner() !=null))
			{
				Item neweritem=(Item)newitem.copyOf();
				final Vector<Item> allStuff=new Vector<Item>();
				allStuff.addElement(neweritem);
				if(newitem instanceof Container)
				{
					final List<Item> V=((Container)newitem).getDeepContents();
					for(int v=0;v<V.size();v++)
					{
						final Item I=(Item)V.get(v).copyOf();
						I.setContainer((Container)neweritem);
						allStuff.addElement(I);
					}
				}
				neweritem.setContainer(this);
				for(int i=0;i<allStuff.size();i++)
				{
					neweritem=allStuff.elementAt(i);
					if(newitem.owner() instanceof MOB)
						((MOB)newitem.owner()).addItem(neweritem);
					else
					if(newitem.owner() instanceof Room)
					{
						((Room)newitem.owner()).addItem(neweritem);
						neweritem.setExpirationDate(expirationDate());
					}
					neweritem.recoverPhyStats();
				}
			}
		}
		super.executeMsg(myHost,msg);
	}
}
