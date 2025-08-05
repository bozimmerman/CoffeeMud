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
public class EditorFoodDrink extends AbilityParmEditorImpl
{
	public EditorFoodDrink()
	{
		super("FOOD_DRINK",CMLib.lang().L("ETyp"),ParmType.CHOICES);
	}

	@Override
	public void createChoices()
	{
		createChoices(new String[]
		{
			"", "FOOD", "DRINK", "SOAP",
			"GenPerfume", "GenPowder", "GenCigar",
			"GenFoodResource", "GenLiquidResource", "GenResource"
		});
	}

	@Override
	public String defaultValue()
	{
		return "";
	}

	@Override
	public String convertFromItem(final ItemCraftor A, final Item I)
	{
		final String str=(I.name()+" "+I.displayText()+" "+I.description()).toUpperCase();
		if(str.startsWith("SOAP ") || str.endsWith(" SOAP") || (str.indexOf("SOAP")>0))
			return "SOAP";
		if(I instanceof Perfume)
			return "GenPerfume";
		if(I instanceof MagicDust)
			return "GenPowder";
		if((I instanceof Container)&&(I instanceof Light))
			return "GenCigar";
		if((I instanceof Food)&&(I instanceof RawMaterial))
			return "GenFoodResource";
		if(I instanceof Food)
			return "FOOD";
		if((I instanceof Drink)&&(I instanceof RawMaterial))
			return "GenLiquidResource";
		if(I instanceof Drink)
			return "DRINK";
		if(I instanceof RawMaterial)
			return "GenResource";
		return "";
	}
}
