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
   Copyright 2005-2025 Bo Zimmerman

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
 * Collection manager for all the socials in the game, with utility
 * functions for dealing with external socials as well.
 *
 * A social set is a set of social object with the same base name,
 * so SMILE, SMILE T-NAME, SMILE ALL, etc. would all be part of the
 * same social set of the base name "SMILE".
 *
 * @author Bo Zimmerman
 *
 */
public interface SocialsList extends CMLibrary
{
	/**
	 * Removes the entire social set of the given name, including
	 * all of its variations.
	 *
	 * @see SocialsList#addSocial(Social)
	 *
	 * @param name the full or partial/base social name
	 */
	public void delSocial(String name);

	/**
	 * Adds a single social target to its cached set in this
	 * collection/manager.
	 *
	 * @see SocialsList#delSocial(String)
	 *
	 * @param S the social object
	 */
	public void addSocial(Social S);

	/**
	 * Provides the entire command line editor for creating or
	 * modifying a social set.
	 *
	 * @param mob the mob editor
	 * @param socials the social id or base name
	 * @param name the social name
	 * @param rest any extra terms used from command line
	 * @return true if something was done
	 * @throws IOException any i/o errors that occured, usually disconnect
	 */
	public boolean modifySocialInterface(MOB mob, List<Social> socials, String name, String rest)
		throws IOException;

	/**
	 * Given a full social id, this will return the Social object, either
	 * matching exactly, or doing a softer search.
	 *
	 * @see SocialsList#fetchSocial(List, boolean, boolean)
	 * @see SocialsList#fetchSocial(String, Environmental, String, boolean)
	 *
	 * @param name the full social id, with base name and target
	 * @param exactOnly true for exact match, or false for search
	 * @return null, or the Social object
	 */
	public Social fetchSocial(String name, boolean exactOnly);

	/**
	 * Given a potential social name, a target object, and a possible trailing arg, this
	 * will find and return the social that matches.
	 *
	 * @see SocialsList#fetchSocial(List, boolean, boolean)
	 * @see SocialsList#fetchSocial(String, boolean)
	 *
	 * @param baseName the hopeful social base name
	 * @param targetE the target representing the social target
	 * @param arg a remaining argument, or ""
	 * @param exactOnly true for exact social match, or false for looser
	 * @return the found social object, or null
	 */
	public Social fetchSocial(String baseName, Environmental targetE, String arg, boolean exactOnly);

	/**
	 * Given a parsed user command line entry, this will search the socials for a likely social
	 * object match, and return it.
	 *
	 * @see SocialsList#fetchSocial(String, Environmental, String, boolean)
	 * @see SocialsList#fetchSocial(String, boolean)
	 *
	 * @param commands the parsed user command line
	 * @param exactOnly true for exact base name match only
	 * @param checkItemTargets true to consider I-NAME target socials
	 * @return null, or the found social.
	 */
	public Social fetchSocial(List<String> commands, boolean exactOnly, boolean checkItemTargets);

	/**
	 * Given a social name to match, it will search for and return the base name.
	 *
	 * @param named the whole or partial or full social name of it
	 * @param exactOnly true for exact base name match
	 * @return null, or the base social name
	 */
	public String findSocialBaseName(String named, boolean exactOnly);

	/**
	 * Given an external map of base names to social sets, and a parsed command line from a
	 * user, this will attempt to find if one of the socials appears in the map, and return
	 * it.
	 *
	 * @see SocialsList#putSocialsInHash(Map, List)
	 *
	 * @param socialsMap the map of base names to social sets
	 * @param commands the parsed user command line
	 * @param exactOnly true for only perfect exact matches
	 * @param checkItemTargets true to check I-NAME type targets
	 * @return null, or the matching social
	 */
	public Social fetchSocialFromSet(final Map<String,List<Social>> socialsMap, List<String> commands, boolean exactOnly, boolean checkItemTargets);

	/**
	 * Given a private map of base social names to social sets, this will populate that map from a
	 * list of parseable social lines, where the format is:
	 * 12\tID\tYouSee\tOthersSee\tTargetSees\tNoTargetSees\tMSP filename\tZappermask\t
	 * 1 = source code, 2 = others/target code
	 * @return the number of socials added to hash
	 *
	 * @see SocialsList#fetchSocialFromSet(Map, List, boolean, boolean)
	 *
	 * @param socialsMap the map to put the socials into
	 * @param lines the lines to parse
	 */
	public int putSocialsInHash(final Map<String,List<Social>> socialsMap, final List<String> lines);

	/**
	 * Returns the social set of the given base name.
	 *
	 * @param named the social base name
	 * @return null, or the social set
	 */
	public List<Social> getSocialsSet(String named);

	/**
	 * Returns an enumeration of every social object, of
	 * every base name, and every target.  All of the
	 * cached and managed socials.
	 *
	 * @return a complete enumeration of every social object.
	 */
	public Enumeration<Social> getAllSocials();

	/**
	 * On behalf of the given mob, saves the cached socials to
	 * the default file in /resources/socials.txt
	 *
	 * @param whom the saver
	 */
	public void save(MOB whom);

	/**
	 * Returns a cached list of all social base names.
	 *
	 * @return a cached list of all social base names.
	 */
	public List<String> getSocialsBaseList();

	/**
	 * Gets the full details on every target variation
	 * of the social set with the given name, for the
	 * given mob viewer.
	 *
	 * @param mob the viewer mob, or null
	 * @param named search base name
	 * @return null, or full info about social set
	 */
	public String getSocialsHelp(MOB mob, String named);

	/**
	 * Creates 4 column list of all social base names.
	 * Keeps a resource cache as well.
	 *
	 * @return the list of social base names.
	 */
	public String getSocialsBaseTable();

	/**
	 * Create a default social object of the given base name,
	 * and the given target type, such as ALL, S-NAME, SELF,
	 * etc. This represents only one social target total.
	 *
	 * @param name baseName of the social to create
	 * @param type the target type, or ""
	 * @return the social object
	 */
	public Social makeDefaultSocial(String name, String type);

	/**
	 * Unloads all the internally hashed socials for reloading later.
	 */
	public void unloadSocials();
}
