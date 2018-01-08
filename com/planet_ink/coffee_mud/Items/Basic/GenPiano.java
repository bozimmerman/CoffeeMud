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
import com.planet_ink.coffee_mud.Items.interfaces.MusicalInstrument.InstrumentType;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

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
public class GenPiano extends GenRideable implements MusicalInstrument
{
	@Override
	public String ID()
	{
		return "GenPiano";
	}

	private InstrumentType type = InstrumentType.PIANOS;
	
	public GenPiano()
	{
		super();
		setName("a generic piano");
		setDisplayText("a generic piano sits here.");
		setDescription("");
		baseGoldValue = 1015;
		basePhyStats().setLevel(1);
		recoverPhyStats();
		basePhyStats().setWeight(2000);
		rideBasis = Rideable.RIDEABLE_SIT;
		riderCapacity = 2;
		setMaterial(RawMaterial.RESOURCE_OAK);
	}

	@Override
	public void recoverPhyStats()
	{
		CMLib.flags().setReadable(this, false);
		super.recoverPhyStats();
	}

	@Override
	public InstrumentType getInstrumentType()
	{
		return type;
	}

	@Override
	public String getInstrumentTypeName()
	{
		return type.name();
	}

	@Override
	public void setReadableText(String text)
	{
		super.setReadableText(text);
		if(CMath.isInteger(text))
			setInstrumentType(CMath.s_int(text));
	}

	@Override
	public void setInstrumentType(int typeOrdinal)
	{
		if(typeOrdinal < InstrumentType.values().length)
			type = InstrumentType.values()[typeOrdinal];
		readableText = ("" + type.ordinal());
	}

	@Override
	public void setInstrumentType(InstrumentType newType)
	{
		if(newType != null)
			type = newType;
		readableText = ("" + type.ordinal());
	}

	@Override
	public void setInstrumentType(String newType)
	{
		if(newType != null)
		{
			final InstrumentType typeEnum = (InstrumentType)CMath.s_valueOf(InstrumentType.class, newType.toUpperCase().trim());
			if(typeEnum != null)
				type = typeEnum;
		}
		readableText = ("" + type.ordinal());
	}
	
}
