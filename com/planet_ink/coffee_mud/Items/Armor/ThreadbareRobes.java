package com.planet_ink.coffee_mud.Items.Armor;
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

public class ThreadbareRobes extends StdArmor
{
	@Override
	public String ID()
	{
		return "ThreadbareRobes";
	}

	public ThreadbareRobes()
	{
		super();
		setName("a set of worn robes");
		setDisplayText("a set of worn robes");
		setDescription("These robes are patched, yet still gape with ragged holes. Evidently having seen years of use, they are vitualy worthless");
		properWornBitmap=Wearable.WORN_TORSO | Wearable.WORN_ARMS | Wearable.WORN_LEGS;
		wornLogicalAnd=true;
		basePhyStats().setArmor(5);
		basePhyStats().setWeight(2);
		basePhyStats().setAbility(0);
		baseGoldValue=1;
		material=RawMaterial.RESOURCE_COTTON;
		recoverPhyStats();
	}

}
