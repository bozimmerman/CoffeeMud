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
public class EditorPoseDescription extends AbilityParmEditorImpl
{
	public EditorPoseDescription()
	{
		super("POSE_DESCRIPTION",CMLib.lang().L("Pose Description"),ParmType.STRING);
	}

	@Override
	public void createChoices()
	{
	}

	@Override
	public String defaultValue()
	{
		return "<S-NAME> is standing here.";
	}

	@Override
	public String convertFromItem(final ItemCraftor A, final Item I)
	{
		if(!(I instanceof DeadBody))
			return "";
		String pose=I.displayText();
		pose=CMStrings.replaceAll(pose,I.name(),"<S-NAME>");
		pose=CMStrings.replaceWord(pose,"himself"," <S-HIM-HERSELF>");
		pose=CMStrings.replaceWord(pose,"herself"," <S-HIM-HERSELF>");
		pose=CMStrings.replaceWord(pose,"his"," <S-HIS-HER>");
		pose=CMStrings.replaceWord(pose,"her"," <S-HIS-HER>");
		pose=CMStrings.replaceWord(pose,"him"," <S-HIM-HER>");
		pose=CMStrings.replaceWord(pose,"her"," <S-HIM-HER>");
		return pose;
	}
}
