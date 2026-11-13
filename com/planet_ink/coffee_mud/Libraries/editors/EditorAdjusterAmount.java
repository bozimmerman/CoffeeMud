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
/**
 * The amount half of an adjuster parm, e.g. the "2" in "hit+2".
 *
 * Prop_HaveAdjuster.apply the amount through
 * Prop_HaveAdjuster.addIfPlussed, which accepts anything
 * CMath.isNumber or CMath.isMathExpression accepts -- so
 * "hit+2", "hit+2+2" and "hit+2-" are all applied, but
 * "hit+'2'" and "hit+abc" are rejected with an error.
 * This editor matches that behavior exactly.  Use it
 * instead of NUMBER/DECIMAL in adjuster misc text formats.
 */
public class EditorAdjusterAmount extends AbilityParmEditorImpl
{
	public EditorAdjusterAmount()
	{
		super("ADJUSTER_AMOUNT",CMLib.lang().L("Adjuster Amount"),ParmType.NUMBER);
	}

	@Override
	public void createChoices()
	{
		createChoices(new String[] { "1", "2", "5", "10", "25", "50", "100" });
	}

	@Override
	public boolean confirmValue(final String oldVal, final Map<String,String> args)
	{
		if((oldVal == null) || (oldVal.trim().length() == 0))
			return false;
		final String s = oldVal.trim();
		return CMath.isNumber(s) || CMath.isMathExpression(s);
	}

	@Override
	public String defaultValue()
	{
		return "1";
	}

	@Override
	public String convertFromItem(final ItemCraftor A, final Item I)
	{
		return "";
	}
}
