package com.planet_ink.coffee_mud.Libraries.interfaces;
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
import com.planet_ink.coffee_mud.Libraries.Sense;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;
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
public interface TelnetFilter extends CMLibrary
{
	public final static String hexStr="0123456789ABCDEF";

	public enum Pronoun
	{
		HISHER("-HIS-HER","-h"),
		HIMHER("-HIM-HER","-m"),
		NAME("-NAME",null),
		NAMESELF("-NAMESELF","-s"),
		HESHE("-HE-SHE","-e"),
		ISARE("-IS-ARE",null),
		HASHAVE("-HAS-HAVE",null),
		YOUPOSS("-YOUPOSS","`s"),
		HIMHERSELF("-HIM-HERSELF","-ms"),
		HISHERSELF("-HIS-HERSELF","-hs"),
		SIRMADAM("-SIRMADAM",null),
		ISARE2("IS-ARE",null),
		NAMENOART("-NAMENOART",null),
		ACCOUNTNAME("-ACCOUNTNAME",null);
		public final String suffix;
		public final String emoteSuffix;
		private Pronoun(String suffix, String emoteSuffix)
		{
			this.suffix=suffix;
			this.emoteSuffix=emoteSuffix;
		}
	}

	public Map<String, Pronoun> getTagTable();
	public String simpleOutFilter(String msg);
	// no word-wrapping, text filtering or ('\','n') -> '\n' translations
	// (it's not a member of the interface either so probably shouldn't be public)
	public String colorOnlyFilter(String msg, Session S);
	public String mxpSafetyFilter(String msg, Session S);
	public String[] wrapOnlyFilter(String msg, int wrap);
	public String getLastWord(StringBuffer buf, int lastSp, int lastSpace);
	public String fullOutFilter(Session S, MOB mob, Physical source, Environmental target, Environmental tool, String msg, boolean wrapOnly);
	public String simpleInFilter(StringBuilder input, boolean permitMXPTags);
	public String simpleInFilter(StringBuilder input);
	public String fullInFilter(String input);
	public String safetyFilter(String s);
}
