package com.planet_ink.coffee_mud.Libraries.editors;

import com.planet_ink.coffee_mud.Abilities.interfaces.Ability;
import com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor;
import com.planet_ink.coffee_mud.Abilities.interfaces.PlanarAbility;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityParameters.ParmType;
import com.planet_ink.coffee_mud.Items.interfaces.Item;
import com.planet_ink.coffee_mud.core.CMClass;
import com.planet_ink.coffee_mud.core.CMLib;

/*
   Copyright 2026-2026 Bo Zimmerman

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
public class EditorPlaneName extends AbilityParmEditorImpl
{
	public EditorPlaneName()
	{
		super("PLANE_NAME",CMLib.lang().L("Plane"),ParmType.CHOICES);
	}

	@Override
	public void createChoices()
	{
		final Ability planeA=CMClass.getAbilityPrototype("StdPlanarAbility");
		if(planeA instanceof PlanarAbility)
			super.createChoices(((PlanarAbility)planeA).getAllPlaneKeys());
		else
			super.createChoices(new String[0]);

		if(!choices.containsFirst(""))
			choices.add("",L("Any plane"));
	}

	@Override
	public String defaultValue()
	{
		return "";
	}

	@Override
	public String convertFromItem(final ItemCraftor A, final Item I)
	{
		return "";
	}
}
