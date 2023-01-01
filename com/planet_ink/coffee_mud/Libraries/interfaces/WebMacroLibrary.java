package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.HTTPRedirectException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_web.interfaces.HTTPOutputConverter;
import com.planet_ink.coffee_web.interfaces.HTTPRequest;

import java.util.*;
/*
   Copyright 2013-2023 Bo Zimmerman

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
public interface WebMacroLibrary extends CMLibrary, HTTPOutputConverter
{
	public byte [] virtualPageFilter(byte [] data) throws HTTPRedirectException;
	public String virtualPageFilter(String s) throws HTTPRedirectException;
	public StringBuffer virtualPageFilter(StringBuffer s) throws HTTPRedirectException;
	public StringBuffer virtualPageFilter(HTTPRequest request, Map<String, Object> objects, long[] processStartTime, String[] lastFoundMacro, StringBuffer s) throws HTTPRedirectException;
	public String clearWebMacros(StringBuffer s);
	public String parseFoundMacro(StringBuffer s, int i, boolean lookOnly);
	public String clearWebMacros(String s);
	
	public String getWebCacheSuffix(final Environmental E);
	public Collection<Item> contributeItemsToWebCache(final Collection<Item> items);
	public boolean isAllNum(final String str);
	public Item findItemMatchInWebCache(final Item I);
	public Item findItemInWebCache(final String MATCHING);
	public Item findItemInAnything(final Object allitems, final String MATCHING);
	public String getAppropriateCode(final PhysicalAgent E, final Physical RorM, final Collection<? extends Physical> classes);
	public Item getItemFromCatalog(final String MATCHING);
	public Item getItemFromWebCache(final Collection<Item> allitems, String code);
	public Item getItemFromWebCache(final String code);
	public Item getItemFromWebCache(final Room R, String code);
	public Item getItemFromWebCache(final MOB M, String code);
	public String findItemWebCacheCode(final MOB M, final Item I);
	public String findItemWebCacheCode(final Collection<Item> allitems, final Item I);
	public String findItemWebCacheCode(final Item I);
	public String findItemWebCacheCode(final Room R, final Item I);
	public boolean isWebCachedItem(final Object I);
	public Iterable<Item> getItemWebCacheIterable();
	
	public Collection<MOB> contributeMOBsToWebCache(final Collection<MOB> inhabs);
	public MOB findMOBMatchInWebCache(final MOB M);
	public MOB getMOBFromAnywhere(final Object allitems, final String MATCHING);
	public MOB getMOBFromCatalog(final String MATCHING);
	public MOB getMOBFromWebCache(final Collection<MOB> allmobs, String code);
	public MOB getMOBFromWebCache(final String code);
	public MOB getMOBFromWebCache(final Room R, String code);
	public String findMOBWebCacheCode(final Collection<MOB> mobs, final MOB M);
	public String findMOBWebCacheCode(final MOB M);
	public String findMOBWebCacheCode(final Room R, final MOB M);
	public boolean isWebCachedMOB(final Object M);
	public Iterable<MOB> getMOBWebCacheIterable();
}
