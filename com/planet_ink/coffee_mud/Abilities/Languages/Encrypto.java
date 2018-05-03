package com.planet_ink.coffee_mud.Abilities.Languages;
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

import java.util.*;

/*
   Copyright 2017-2018 Bo Zimmerman

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
public class Encrypto extends StdLanguage
{
	@Override
	public String ID()
	{
		return "Encrypto";
	}

	private final static String localizedName = CMLib.lang().L("Encrypto");
	protected int letterRot = CMLib.dice().roll(1, 13, 0);
	protected int wordRot = CMLib.dice().roll(1, 3, -1);
	protected int level = 0;

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abilityCode()
	{
		return level;
	}

	@Override
	public void setAbilityCode(int newCode)
	{
		if(newCode != level)
		{
			level=newCode;
			final String txt=super.text();
			if(txt.toUpperCase().indexOf("LVL=")<0)
				setMiscText("LVL="+level+" "+txt);
		}
	}

	@Override
	public String translate(String language, String word)
	{
		final char[] w=word.toCharArray();
		final char[] nw=Arrays.copyOf(w,w.length);
		for(int i=0;i<w.length;i++)
			w[i]=nw[(i+wordRot)%w.length];
		for(int i=0;i<w.length;i++)
		{
			if(Character.isLetter(w[i]))
			{
				if(Character.isUpperCase(w[i]))
				{
					int val=(w[i])-65;
					val += letterRot;
					val = val % 26;
					w[i]=(char)(val+65);
				}
				else
				if(Character.isLowerCase(w[i]))
				{
					int val=(w[i])-97;
					val += letterRot;
					val = val % 26;
					w[i]=(char)(val+97);
				}
			}
		}
		return new String(w);
	}
	
	@Override
	public void setMiscText(String newMiscText)
	{
		if(newMiscText.length()>0)
		{
			letterRot = CMParms.getParmInt(newMiscText, "LROT", letterRot);
			wordRot = CMParms.getParmInt(newMiscText, "WROT", wordRot);
			level = CMParms.getParmInt(newMiscText, "LVL", level);
		}
		if(newMiscText.toUpperCase().indexOf("LROT=")<0)
			newMiscText="LROT="+letterRot+" "+newMiscText;
		if(newMiscText.toUpperCase().indexOf("WROT=")<0)
			newMiscText="WROT="+wordRot+" "+newMiscText;
		super.setMiscText(newMiscText);
	}
}
