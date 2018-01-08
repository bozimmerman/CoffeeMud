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
import com.planet_ink.coffee_mud.Libraries.Socials;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
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
public interface SocialsList extends CMLibrary
{
	public final String filename=Resources.buildResourcePath("")+"socials.txt";

	public boolean isLoaded();

	public void put(String name, Social S);
	public void remove(String name);
	public void addSocial(Social S);

	public void modifySocialOthersCode(MOB mob, Social me, int showNumber, int showFlag)
		throws IOException;
	public void modifySocialTargetCode(MOB mob, Social me, int showNumber, int showFlag)
		throws IOException;
	public void modifySocialSourceCode(MOB mob, Social me, int showNumber, int showFlag)
		throws IOException;
	public boolean modifySocialInterface(MOB mob, String socialString)
		throws IOException;

	public Social fetchSocial(String name, boolean exactOnly);
	public Social fetchSocial(String baseName, Environmental targetE, boolean exactOnly);
	public Social fetchSocial(List<String> C, boolean exactOnly, boolean checkItemTargets);
	public Social fetchSocial(List<Social> set, String name, boolean exactOnly);
	public String findSocialName(String named, boolean exactOnly);
	public Social fetchSocialFromSet(final Map<String,List<Social>> soc, List<String> C, boolean exactOnly, boolean checkItemTargets);

	public void putSocialsInHash(final Map<String,List<Social>> soc, final List<String> lines);

	public List<Social> getSocialsSet(String named);
	public int numSocialSets();
	public List<Social> enumSocialSet(int index);

	public void save(MOB whom);
	public List<String> getSocialsList();
	public String getSocialsHelp(MOB mob, String named, boolean exact);
	public String getSocialsTable();
	public Social makeDefaultSocial(String name, String type);

	public void unloadSocials();
}
