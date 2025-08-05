package com.planet_ink.coffee_mud.Libraries.editors;
import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityParameters.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.RawMaterial.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
   Copyright 2008-2025 Bo Zimmerman

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
public class EditorItemClassId extends AbilityParmEditorImpl
{
	public EditorItemClassId()
	{
		super("ITEM_CLASS_ID",CMLib.lang().L("Class ID"),ParmType.CHOICES);
	}

	@Override
	public void createChoices()
	{
		final List<Item> V  = new ArrayList<Item>();
		V.addAll(new XVector<ClanItem>(CMClass.clanItems()));
		V.addAll(new XVector<Armor>(CMClass.armor()));
		V.addAll(new XVector<Item>(CMClass.basicItems()));
		V.addAll(new XVector<MiscMagic>(CMClass.miscMagic()));
		V.addAll(new XVector<Technical>(CMClass.tech()));
		V.addAll(new XVector<Weapon>(CMClass.weapons()));
		final List<Item> V2=new ArrayList<Item>();
		Item I;
		for(final Iterator<Item> e=V.iterator();e.hasNext();)
		{
			I=e.next();
			if(I.isGeneric() || I.ID().equalsIgnoreCase("StdDeckOfCards"))
				V2.add(I);
		}
		for(int i=V.size()-1;i<=0;i--)
		{
			if(V.get(i) instanceof CMObjectWrapper)
				V.remove(i);
		}
		createChoices(V2);
	}

	@Override
	public String convertFromItem(final ItemCraftor A, final Item I)
	{
		if(I.isGeneric())
			return I.ID();
		if(I instanceof Weapon)
			return "GenWeapon";
		if(I instanceof Armor)
		{
			if(I instanceof Container)
				return "GenArmor";
			else
				return "GenWearable";
		}
		if(I instanceof Rideable)
			return "GenRideable";
		return "GenItem";
	}

	@Override
	public String defaultValue()
	{
		return "GenItem";
	}
}
