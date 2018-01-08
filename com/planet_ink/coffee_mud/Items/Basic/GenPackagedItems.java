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
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;

/*
   Copyright 2005-2018 Bo Zimmerman

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
public class GenPackagedItems extends GenItem implements PackagedItems
{
	@Override
	public String ID()
	{
		return "GenPackagedItems";
	}

	public GenPackagedItems()
	{
		super();
		setName("item");
		basePhyStats.setWeight(150);
		setDisplayText("");
		setDescription("");
		baseGoldValue = 5;
		basePhyStats().setLevel(1);
		setMaterial(RawMaterial.RESOURCE_MEAT);
		recoverPhyStats();
	}

	@Override
	protected boolean abilityImbuesMagic()
	{
		return false;
	}

	@Override
	public String name()
	{
		return L("a package of @x1 @x2(s)",""+numberOfItemsInPackage(), Name().trim());
	}

	@Override
	public String displayText()
	{
		return L("a package of @x1 @x2(s) sit here.",""+numberOfItemsInPackage(), Name().trim());
	}

	@Override
	public int numberOfItemsInPackage()
	{
		return basePhyStats().ability();
	}

	@Override
	public void setNumberOfItemsInPackage(int number)
	{
		basePhyStats().setAbility(number);
		phyStats().setAbility(number);
	}

	protected byte[]	readableText	= null;

	@Override
	public String readableText()
	{
		return readableText == null ? "" : CMLib.encoder().decompressString(readableText);
	}

	@Override
	public void setReadableText(String text)
	{
		readableText = (text.trim().length() == 0) ? null : CMLib.encoder().compressString(text);
	}

	@Override
	public boolean packageMe(Item I, int number)
	{
		if ((I == null) 
		|| (!CMLib.utensils().disInvokeEffects(I)) 
		|| (I.amDestroyed()))
			return false;
		name = CMLib.english().cleanArticles(I.Name());
		displayText = "";
		if(I.description().trim().length()==0)
			setDescription(L("The contents of the stack appears as follows: ") + I.name());
		else
			setDescription(L("The contents of the stack appears as follows: ") + "\n\r"+I.description());
		basePhyStats().setLevel(I.basePhyStats().level());
		basePhyStats().setWeight(I.basePhyStats().weight() * number);
		basePhyStats().setHeight(I.basePhyStats().height());
		setMaterial(I.material());
		setBaseValue(I.baseGoldValue() * number);
		final StringBuffer itemstr = new StringBuffer("");
		itemstr.append("<PAKITEM>");
		itemstr.append(CMLib.xml().convertXMLtoTag("PICLASS", CMClass.classID(I)));
		itemstr.append(CMLib.xml().convertXMLtoTag("PIDATA", CMLib.coffeeMaker().getPropertiesStr(I, true)));
		itemstr.append("</PAKITEM>");
		setPackageText(itemstr.toString());
		setNumberOfItemsInPackage(number);
		recoverPhyStats();
		return true;
	}

	@Override
	public boolean isPackagable(List<Item> V)
	{
		if (V == null)
			return false;
		if (V.size() == 0)
			return false;
		for (int v1 = 0; v1 < V.size(); v1++)
		{
			final Item I = V.get(v1);
			for (int v2 = v1 + 1; v2 < V.size(); v2++)
			{
				if (!V.get(v2).sameAs(I))
					return false;
			}
		}
		return true;
	}

	@Override
	public boolean areAllItemsTheSame()
	{
		return true;
	}

	@Override
	public Item peekFirstItem()
	{
		if (packageText().length() == 0)
			return null;
		final List<XMLLibrary.XMLTag> buf = CMLib.xml().parseAllXML(packageText());
		if (buf == null)
		{
			Log.errOut("Packaged", "Error parsing 'PAKITEM'.");
			return null;
		}
		final XMLTag iblk = CMLib.xml().getPieceFromPieces(buf, "PAKITEM");
		if ((iblk == null) || (iblk.contents() == null))
		{
			Log.errOut("Packaged", "Error parsing 'PAKITEM'.");
			return null;
		}
		final String itemi = iblk.getValFromPieces( "PICLASS");
		final Environmental newOne = CMClass.getItem(itemi);
		final List<XMLLibrary.XMLTag> idat = iblk.getContentsFromPieces( "PIDATA");
		if ((idat == null) || (newOne == null) || (!(newOne instanceof Item)))
		{
			Log.errOut("Packaged", "Error parsing 'PAKITEM' data.");
			return null;
		}
		CMLib.coffeeMaker().setPropertiesStr(newOne, idat, true);
		return (Item) newOne;
	}

	@Override
	public List<Item> unPackage(int number)
	{
		final List<Item> V = new Vector<Item>();
		if (number >= numberOfItemsInPackage())
			number = numberOfItemsInPackage();
		if (number <= 0)
			return V;
		final int itemWeight = basePhyStats().weight() / numberOfItemsInPackage();
		final int itemValue = baseGoldValue() / numberOfItemsInPackage();
		final Item I = peekFirstItem();
		if (I == null)
			return V;
		I.recoverPhyStats();
		for (int i = 0; i < number; i++)
			V.add((Item) I.copyOf());
		setNumberOfItemsInPackage(numberOfItemsInPackage() - number);
		if (numberOfItemsInPackage() <= 0)
		{
			destroy();
			return V;
		}
		basePhyStats().setWeight(itemWeight * number);
		setBaseValue(itemValue * number);
		recoverPhyStats();
		return V;
	}

	@Override
	public String packageText()
	{
		return CMLib.xml().restoreAngleBrackets(readableText());
	}

	@Override
	public void setPackageText(String text)
	{
		setReadableText(CMLib.xml().parseOutAngleBrackets(text));
		CMLib.flags().setReadable(this, false);
	}

	@Override
	public int getPackageFlagsBitmap()
	{
		return 0;
	}

	@Override
	public void setPackageFlagsBitmap(int bitmap)
	{
	}
}
