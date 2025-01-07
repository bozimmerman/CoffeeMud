package com.planet_ink.coffee_mud.core;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.threads.CMFactoryThread;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2005-2024 Bo Zimmerman

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
 * CMLib is a semi-singleton central repository for all the various code libraries
 * implemented in CoffeeMud.  Generally it provides accessor methods for all the
 * java classes in com.planet_ink.coffee_mud.Libraries.  Like many other CoffeeMud
 * classes, it also supports the thread-group-code-character accessor method, so
 * that it can provide unique instances of some of the libraries based on the
 * first character of the name of the current thread group.  For completeness, you'll
 * also find accessors for other core singletons.  Lastly, CMLib is a container
 * class for all MudHost objects running in this process.
 * @see com.planet_ink.coffee_mud.core.interfaces.MudHost
 * @author Bo Zimmerman
 *
 */
public class CMLib
{
	private static final SVector<MudHost> mudThreads=new SVector<MudHost>();

	private static final CMLib[] libs=new CMLib[256];

	/**
	 * Constructs a new CMLib object for the current thread group.
	 */
	public CMLib()
	{
		super();
		final char c=Thread.currentThread().getThreadGroup().getName().charAt(0);
		if(libs[c]==null)
			libs[c]=this;
	}

	/**
	 * Returns the log object for the current threadgroup, or null if unassigned.
	 * @return the Log object, or null
	 */
	private static final CMLib l()
	{
		return libs[Thread.currentThread().getThreadGroup().getName().charAt(0)];
	}

	/**
	 * Returns a CMLib object for the current thread group.  If one is not assigned,
	 * it will be instantiated, thus guaranteeing that a CMLib object always returns
	 * from this method.
	 * @return a CMLib object
	 */
	public static final CMLib initialize()
	{
		final CMLib l=l();
		return (l==null)?new CMLib():l;
	}

	private final CMLibrary[] 	libraries=new CMLibrary[Library.values().length];
	private final boolean[]		registered=new boolean[Library.values().length];

	/**
	 * Collection of all the different official CoffeeMud libraries
	 * @author Bo Zimmerman
	 */
	public static enum Library
	{
		DATABASE(DatabaseEngine.class),
		THREADS(ThreadEngine.class),
		INTERMUD(I3Interface.class),
		WEBMACS(WebMacroLibrary.class),
		LISTER(ListingLibrary.class),
		MONEY(MoneyLibrary.class),
		SHOPS(ShoppingLibrary.class),
		COMBAT(CombatLibrary.class),
		HELP(HelpLibrary.class),
		TRACKING(TrackingLibrary.class),
		MASKING(MaskingLibrary.class),
		CHANNELS(ChannelsLibrary.class),
		COMMANDS(CommonCommands.class),
		ENGLISH(EnglishParsing.class),
		SLAVERY(SlaveryLibrary.class),
		JOURNALS(JournalsLibrary.class),
		FLAGS(CMFlagLibrary.class),
		OBJBUILDERS(GenericBuilder.class),
		SESSIONS(SessionsList.class),
		TELNET(TelnetFilter.class),
		XML(XMLLibrary.class),
		SOCIALS(SocialsList.class),
		UTENSILS(CMMiscUtils.class),
		STATS(StatisticsLibrary.class),
		MAP(WorldMap.class),
		QUEST(QuestManager.class),
		ABLEMAP(AbilityMapper.class),
		ENCODER(TextEncoders.class),
		SMTP(SMTPLibrary.class),
		DICE(DiceLibrary.class),
		FACTIONS(FactionManager.class),
		CLANS(ClanManager.class),
		POLLS(PollManager.class),
		TIME(TimeManager.class),
		COLOR(ColorLibrary.class),
		LOGIN(CharCreationLibrary.class),
		TIMS(ItemBalanceLibrary.class),
		LEVELS(ExpLevelLibrary.class),
		EXPERTISES(ExpertiseLibrary.class),
		MATERIALS(MaterialLibrary.class),
		LEGAL(LegalLibrary.class),
		LANGUAGE(LanguageLibrary.class),
		CATALOG(CatalogLibrary.class),
		PLAYERS(PlayerLibrary.class),
		AUTOAWARDS(AutoAwardsLibrary.class),
		ABLEPARMS(AbilityParameters.class),
		GENEDITOR(GenericEditor.class),
		AREAGEN(AreaGenerationLibrary.class),
		TECH(TechLibrary.class),
		PROTOCOL(ProtocolLibrary.class),
		ACHIEVEMENTS(AchievementLibrary.class),
		ABLECOMP(AbilityComponents.class),
		GALACTIC(GalacticMap.class),
		CITY(CityMap.class),
		WORLDHUNT(WorldHuntLibrary.class)
		;

		public final Class<?> ancestor;
		private Library(final Class<?> ancestorC1)
		{
			this.ancestor=ancestorC1;
		}
	}

	/**
	 * Returns reference to the math utility class.
	 * @see com.planet_ink.coffee_mud.core.CMath
	 * @return reference to the math utility class.
	 */
	public static final CMath math()
	{
		return CMath.instance();
	}

	/**
	 * Returns reference to the string parameter utility class.
	 * @see com.planet_ink.coffee_mud.core.CMParms
	 * @return reference to the string parameter utility class.
	 */
	public static final CMParms parms()
	{
		return CMParms.instance();
	}

	/**
	 * Returns reference to the string utility class.
	 * @see com.planet_ink.coffee_mud.core.CMStrings
	 * @return reference to the string utility class.
	 */
	public static final CMStrings strings()
	{
		return CMStrings.instance();
	}

	/**
	 * Returns reference to the class loader.
	 * @see com.planet_ink.coffee_mud.core.CMClass
	 * @return reference to the class loader.
	 */
	public static final CMClass classes()
	{
		return CMClass.instance();
	}

	/**
	 * Returns reference to the security class.
	 * @see com.planet_ink.coffee_mud.core.CMSecurity
	 * @return reference to the security class.
	 */
	public static final CMSecurity security()
	{
		return CMSecurity.instance();
	}

	/**
	 * Returns reference to the directions class.
	 * @see com.planet_ink.coffee_mud.core.Directions
	 * @return reference to the directions class.
	 */
	public static final Directions directions()
	{
		return Directions.instance();
	}

	/**
	 * Returns reference to the logger.
	 * @see com.planet_ink.coffee_mud.core.Log
	 * @return reference to the logger.
	 */
	public static final Log log()
	{
		return Log.instance();
	}

	/**
	 * Returns reference to the resources storage class.
	 * @see com.planet_ink.coffee_mud.core.Resources
	 * @return reference to the resources storage class.
	 */
	public static final Resources resources()
	{
		return Resources.instance();
	}

	/**
	 * Returns reference to the properties ini file class.
	 * @see com.planet_ink.coffee_mud.core.CMProps
	 * @return reference to the properties ini file class.
	 */
	public static final CMProps props()
	{
		return CMProps.instance();
	}

	/**
	 * Returns a list of all the registered mud hosts running.
	 * @see com.planet_ink.coffee_mud.application.MUD
	 * @return list of the registered mud hosts running.
	 */
	public static final List<MudHost> hosts()
	{
		return mudThreads;
	}

	/**
	 * Returns the MudHost associated with the callers
	 * thread group.
	 * @param threadId the threadId of the host to get
	 * @return the appropriate mud host.
	 */
	public static final MudHost host(final char threadId)
	{
		if(mudThreads.size()==0)
			return null;
		for(int i=0;i<mudThreads.size();i++)
		{
			final MudHost host = mudThreads.get(i);
			if(host instanceof Thread)
			{
				if(((Thread)host).getThreadGroup().getName().charAt(0) == threadId)
					return host;
			}
		}
		return mudThreads.get(0);
	}

	/**
	 * Returns the MudHost associated with the callers
	 * thread group.
	 * @return the appropriate mud host.
	 */
	public static final MudHost host()
	{
		return host(Thread.currentThread().getThreadGroup().getName().charAt(0));
	}

	/**
	 * Returns the mud running on the given port, or null
	 * if none is found.
	 * @see com.planet_ink.coffee_mud.core.interfaces.MudHost
	 * @param port port to search for
	 * @return the mudhost running on that port
	 */
	public static final MudHost mud(final int port)
	{
		if(mudThreads.size()==0)
			return null;
		else
		if(port<=0)
			return mudThreads.firstElement();
		else
		for(int i=0;i<mudThreads.size();i++)
		{
			if(mudThreads.elementAt(i).getPort()==port)
				return mudThreads.elementAt(i);
		}
		return null;
	}

	/**
	 * Returns an enumeration of basic code libraries registered
	 * with the system thus far.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.CMLibrary
	 * @return an enumeration of basic code libraries registered
	 */
	public static final Enumeration<CMLibrary> libraries()
	{
		final Vector<CMLibrary> V=new Vector<CMLibrary>();
		for(final Library lbry : Library.values())
		{
			if(l().libraries[lbry.ordinal()]!=null)
				V.add(l().libraries[lbry.ordinal()]);
		}
		return V.elements();
	}

	/**
	 * Returns the CMLib instance associated either with
	 * the given session, or the current thread.
	 * @param session the session
	 * @return the cmlib
	 */
	public static final CMLib get(final Session session)
	{
		if(session != null)
			return get(session.getGroupID());
		return l();
	}

	/**
	 * Returns the CMLib instance associated with
	 * the given thread id, or the current thread.
	 * @param id the id
	 * @return the cmlib
	 */
	public static final CMLib get(final int id)
	{
		if((id>=0)&&(id<libs.length))
		{
			final CMLib lib=libs[id];
			if(lib != null)
				return lib;
		}
		return l();
	}

	/**
	 * Returns a reference to this threads database engine library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine
	 * @return a reference to this threads database engine library.
	 */
	public static final DatabaseEngine database()
	{
		return (DatabaseEngine)l().libraries[Library.DATABASE.ordinal()];
	}

	/**
	 * Returns a reference to this threads Thread access library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.ThreadEngine
	 * @return a reference to this threads Thread access library.
	 */
	public static final ThreadEngine threads()
	{
		return (ThreadEngine)l().libraries[Library.THREADS.ordinal()];
	}

	/**
	 * Returns a reference to this threads Intermud3 access library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.I3Interface
	 * @return a reference to this threads Intermud3 access library.
	 */
	public static final I3Interface intermud()
	{
		return (I3Interface)l().libraries[Library.INTERMUD.ordinal()];
	}

	/**
	 * Returns a reference to this threads item balancing library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.ItemBalanceLibrary
	 * @return a reference to this threads item balancing library.
	 */
	public static final ItemBalanceLibrary itemBuilder()
	{
		return (ItemBalanceLibrary)l().libraries[Library.TIMS.ordinal()];
	}

	/**
	 * Returns a reference to this threads web macro filtering library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.WebMacroLibrary
	 * @return a reference to this threads web macro filtering library.
	 */
	public static final WebMacroLibrary webMacroFilter()
	{
		return (WebMacroLibrary)l().libraries[Library.WEBMACS.ordinal()];
	}

	/**
	 * Returns a reference to this threads string/item/object listing library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary
	 * @return a reference to this threads string/item/object listing library.
	 */
	public static final ListingLibrary lister()
	{
		return (ListingLibrary)l().libraries[Library.LISTER.ordinal()];
	}

	/**
	 * Returns a reference to this threads money handling library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MoneyLibrary
	 * @return a reference to this threads money handling library.
	 */
	public static final MoneyLibrary beanCounter()
	{
		return (MoneyLibrary)l().libraries[Library.MONEY.ordinal()];
	}

	/**
	 * Returns a reference to this threads store front/shopping library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.ShoppingLibrary
	 * @return a reference to this threads store front/shopping library.
	 */
	public static final ShoppingLibrary coffeeShops()
	{
		return (ShoppingLibrary)l().libraries[Library.SHOPS.ordinal()];
	}

	/**
	 * Returns a reference to this threads raw resource/material item library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaterialLibrary
	 * @return a reference to this threads raw resource/material item library.
	 */
	public static final MaterialLibrary materials()
	{
		return (MaterialLibrary)l().libraries[Library.MATERIALS.ordinal()];
	}

	/**
	 * Returns a reference to this threads combat library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.CombatLibrary
	 * @return a reference to this threads combat library.
	 */
	public static final CombatLibrary combat()
	{
		return (CombatLibrary)l().libraries[Library.COMBAT.ordinal()];
	}

	/**
	 * Returns a reference to this threads help file library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.HelpLibrary
	 * @return a reference to this threads help file library.
	 */
	public static final HelpLibrary help()
	{
		return (HelpLibrary)l().libraries[Library.HELP.ordinal()];
	}

	/**
	 * Returns a reference to this threads mob tracking/movement library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary
	 * @return a reference to this threads mob tracking/movement library.
	 */
	public static final TrackingLibrary tracking()
	{
		return (TrackingLibrary)l().libraries[Library.TRACKING.ordinal()];
	}

	/**
	 * Returns a reference to this threads legal and property library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.LegalLibrary
	 * @return a reference to this threads legal and property library.
	 */
	public static final LegalLibrary law()
	{
		return (LegalLibrary)l().libraries[Library.LEGAL.ordinal()];
	}

	/**
	 * Returns a reference to this threads object masking/filtering library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
	 * @return a reference to this threads object masking/filtering library.
	 */
	public static final MaskingLibrary masking()
	{
		return (MaskingLibrary)l().libraries[Library.MASKING.ordinal()];
	}

	/**
	 * Returns a reference to this threads chat channel library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary
	 * @return a reference to this threads chat channel library.
	 */
	public static final ChannelsLibrary channels()
	{
		return (ChannelsLibrary)l().libraries[Library.CHANNELS.ordinal()];
	}

	/**
	 * Returns a reference to this threads command shortcut and common event handler library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.CommonCommands
	 * @return a reference to this threads command shortcut and common event handler library.
	 */
	public static final CommonCommands commands()
	{
		return (CommonCommands)l().libraries[Library.COMMANDS.ordinal()];
	}

	/**
	 * Returns a reference to this threads achievement system library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary
	 * @return a reference to this threads achievement library.
	 */
	public static final AchievementLibrary achievements()
	{
		return (AchievementLibrary)l().libraries[Library.ACHIEVEMENTS.ordinal()];
	}

	/**
	 * Returns a reference to this threads english grammar and input utility library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.EnglishParsing
	 * @return a reference to this threads english grammar and input utility library.
	 */
	public static final EnglishParsing english()
	{
		return (EnglishParsing)l().libraries[Library.ENGLISH.ordinal()];
	}

	/**
	 * Returns a reference to this threads slavery and geas library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.SlaveryLibrary
	 * @return a reference to this threads slavery and geas library.
	 */
	public static final SlaveryLibrary slavery()
	{
		return (SlaveryLibrary)l().libraries[Library.SLAVERY.ordinal()];
	}

	/**
	 * Returns a reference to this threads space mapping library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.GalacticMap
	 * @return a reference to this threads space mapping library.
	 */
	public static final GalacticMap space()
	{
		return (GalacticMap)l().libraries[Library.GALACTIC.ordinal()];
	}

	/**
	 * Returns a reference to this threads city objects library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.CityMap
	 * @return a reference to this threads city objects library.
	 */
	public static final CityMap city()
	{
		return (CityMap)l().libraries[Library.CITY.ordinal()];
	}

	/**
	 * Returns a reference to this threads message board and journal library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary
	 * @return a reference to this threads message board and journal library.
	 */
	public static final JournalsLibrary journals()
	{
		return (JournalsLibrary)l().libraries[Library.JOURNALS.ordinal()];
	}

	/**
	 * Returns a reference to this threads telnet input/output filtering library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.TelnetFilter
	 * @return a reference to this threads telnet input/output filtering library.
	 */
	public static final TelnetFilter coffeeFilter()
	{
		return (TelnetFilter)l().libraries[Library.TELNET.ordinal()];
	}

	/**
	 * Returns a reference to this threads GenObject low level construction library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.GenericBuilder
	 * @return a reference to this threads GenObject low level construction library.
	 */
	public static final GenericBuilder coffeeMaker()
	{
		return (GenericBuilder)l().libraries[Library.OBJBUILDERS.ordinal()];
	}

	/**
	 * Returns a reference to this threads telnet session management library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.SessionsList
	 * @return a reference to this threads telnet session management library.
	 */
	public static final SessionsList sessions()
	{
		return (SessionsList)l().libraries[Library.SESSIONS.ordinal()];
	}

	/**
	 * Returns a reference to this threads flag checking shortcut library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.CMFlagLibrary
	 * @return a reference to this threads flag checking shortcut library.
	 */
	public static final CMFlagLibrary flags()
	{
		return (CMFlagLibrary)l().libraries[Library.FLAGS.ordinal()];
	}

	/**
	 * Returns a reference to this threads xml parsing library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary
	 * @return a reference to this threads xml parsing library.
	 */
	public static final XMLLibrary xml()
	{
		return (XMLLibrary)l().libraries[Library.XML.ordinal()];
	}

	/**
	 * Returns a reference to this threads social command collection/management library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.SocialsList
	 * @return a reference to this threads social command collection/management library.
	 */
	public static final SocialsList socials()
	{
		return (SocialsList)l().libraries[Library.SOCIALS.ordinal()];
	}

	/**
	 * Returns a reference to this threads random world utilities library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.CMMiscUtils
	 * @return a reference to this threads random world utilities library.
	 */
	public static final CMMiscUtils utensils()
	{
		return (CMMiscUtils)l().libraries[Library.UTENSILS.ordinal()];
	}

	/**
	 * Returns a reference to this threads statistics library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.StatisticsLibrary
	 * @return a reference to this threads statistics library.
	 */
	public static final StatisticsLibrary coffeeTables()
	{
		return (StatisticsLibrary)l().libraries[Library.STATS.ordinal()];
	}

	/**
	 * Returns a reference to this threads leveling and experience gaining library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.ExpLevelLibrary
	 * @return a reference to this threads leveling and experience gaining library.
	 */
	public static final ExpLevelLibrary leveler()
	{
		return (ExpLevelLibrary)l().libraries[Library.LEVELS.ordinal()];
	}

	/**
	 * Returns a reference to this threads areas and rooms access/management library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.WorldMap
	 * @return a reference to this threads areas and rooms access/management library.
	 */
	public static final WorldMap map()
	{
		return (WorldMap)l().libraries[Library.MAP.ordinal()];
	}

	/**
	 * Returns a reference to this threads areas and rooms searching library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.WorldHuntLibrary
	 * @return a reference to this threads areas and rooms searching library.
	 */
	public static final WorldHuntLibrary hunt()
	{
		return (WorldHuntLibrary)l().libraries[Library.WORLDHUNT.ordinal()];
	}


	/**
	 * Returns a reference to this threads quest collection/management library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.QuestManager
	 * @return a reference to this threads quest collection/management library.
	 */
	public static final QuestManager quests()
	{
		return (QuestManager)l().libraries[Library.QUEST.ordinal()];
	}

	/**
	 * Returns a reference to this threads random map/object generation library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary
	 * @return a reference to this threads random map/object generation library.
	 */
	public static final AreaGenerationLibrary percolator()
	{
		return (AreaGenerationLibrary)l().libraries[Library.AREAGEN.ordinal()];
	}

	/**
	 * Returns a reference to this threads abilities collection/management library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper
	 * @return a reference to this threads abilities collection/management library.
	 */
	public static final AbilityMapper ableMapper()
	{
		return (AbilityMapper)l().libraries[Library.ABLEMAP.ordinal()];
	}

	/**
	 * Returns a reference to this threads abilities components management library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.AbilityComponents
	 * @return a reference to this threads abilities components management library.
	 */
	public static final AbilityComponents ableComponents()
	{
		return (AbilityComponents)l().libraries[Library.ABLECOMP.ordinal()];
	}

	/**
	 * Returns a reference to this threads string hashing and compression library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.TextEncoders
	 * @return a reference to this threads string hashing and compression library.
	 */
	public static final TextEncoders encoder()
	{
		return (TextEncoders)l().libraries[Library.ENCODER.ordinal()];
	}

	/**
	 * Returns a reference to this threads email sending library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.SMTPLibrary
	 * @return a reference to this threads email sending library.
	 */
	public static final SMTPLibrary smtp()
	{
		return (SMTPLibrary)l().libraries[Library.SMTP.ordinal()];
	}

	/**
	 * Returns a reference to this threads localization library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.LanguageLibrary
	 * @return a reference to this threads localization library.
	 */
	public static final LanguageLibrary lang()
	{
		return (LanguageLibrary)l().libraries[Library.LANGUAGE.ordinal()];
	}

	/**
	 * Returns a reference to this threads random dice roll library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.DiceLibrary
	 * @return a reference to this threads random dice roll library.
	 */
	public static final DiceLibrary dice()
	{
		return (DiceLibrary)l().libraries[Library.DICE.ordinal()];
	}

	/**
	 * Returns a reference to this threads faction collection/management library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.FactionManager
	 * @return a reference to this threads faction collection/management library.
	 */
	public static final FactionManager factions()
	{
		return (FactionManager)l().libraries[Library.FACTIONS.ordinal()];
	}

	/**
	 * Returns a reference to this threads clan collection/management library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.ClanManager
	 * @return a reference to this threads clan collection/management library.
	 */
	public static final ClanManager clans()
	{
		return (ClanManager)l().libraries[Library.CLANS.ordinal()];
	}

	/**
	 * Returns a reference to this threads player poll collection/management library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.PollManager
	 * @return a reference to this threads player poll collection/management library.
	 */
	public static final PollManager polls()
	{
		return (PollManager)l().libraries[Library.POLLS.ordinal()];
	}

	/**
	 * Returns a reference to this threads real time utility library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.TimeManager
	 * @return a reference to this threads real time utility library.
	 */
	public static final TimeManager time()
	{
		return (TimeManager)l().libraries[Library.TIME.ordinal()];
	}

	/**
	 * Returns a reference to this threads ansi color library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.ColorLibrary
	 * @return a reference to this threads ansi color library.
	 */
	public static final ColorLibrary color()
	{
		return (ColorLibrary)l().libraries[Library.COLOR.ordinal()];
	}

	/**
	 * Returns a reference to this threads login and char creation library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.CharCreationLibrary
	 * @return a reference to this threads login and char creation library.
	 */
	public static final CharCreationLibrary login()
	{
		return (CharCreationLibrary)l().libraries[Library.LOGIN.ordinal()];
	}

	/**
	 * Returns a reference to this threads expertise collection/management library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary
	 * @return a reference to this threads expertise collection/management library.
	 */
	public static final ExpertiseLibrary expertises()
	{
		return (ExpertiseLibrary)l().libraries[Library.EXPERTISES.ordinal()];
	}

	/**
	 * Returns a reference to this threads player and account collection/management library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary
	 * @return a reference to this threads player and account collection/management library.
	 */
	public static final PlayerLibrary players()
	{
		return (PlayerLibrary)l().libraries[Library.PLAYERS.ordinal()];
	}

	/**
	 * Returns a reference to this threads cataloged mob/item collection/management library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.CatalogLibrary
	 * @return a reference to this threads cataloged mob/item collection/management library.
	 */
	public static final CatalogLibrary catalog()
	{
		return (CatalogLibrary)l().libraries[Library.CATALOG.ordinal()];
	}

	/**
	 * Returns a reference to this threads player awards collection/management library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.AutoAwardsLibrary
	 * @return a reference to this threads player titles collection/management library.
	 */
	public static final AutoAwardsLibrary awards()
	{
		return (AutoAwardsLibrary)l().libraries[Library.AUTOAWARDS.ordinal()];
	}

	/**
	 * Returns a reference to this threads recipe maker and skill parameter library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.AbilityParameters
	 * @return a reference to this threads recipe maker and skill parameter library.
	 */
	public static final AbilityParameters ableParms()
	{
		return (AbilityParameters)l().libraries[Library.ABLEPARMS.ordinal()];
	}

	/**
	 * Returns a reference to this threads generic object builder/editor and prompting library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.GenericEditor
	 * @return a reference to this threads generic object builder/editor and prompting library.
	 */
	public static final GenericEditor genEd()
	{
		return (GenericEditor)l().libraries[Library.GENEDITOR.ordinal()];
	}

	/**
	 * Returns a reference to this threads tech and electricity library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.TechLibrary
	 * @return a reference to this threads tech and electricity library.
	 */
	public static final TechLibrary tech()
	{
		return (TechLibrary)l().libraries[Library.TECH.ordinal()];
	}

	/**
	 * Returns a reference to this threads mud protocol mxp/msdp/etc library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.ProtocolLibrary
	 * @return a reference to this threads mud protocol mxp/msdp/etc library.
	 */
	public static final ProtocolLibrary protocol()
	{
		return (ProtocolLibrary)l().libraries[Library.PROTOCOL.ordinal()];
	}


	/**
	 * Returns a reference to this instances database engine library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine
	 * @return a reference to this instances database engine library.
	 */
	public final DatabaseEngine _database()
	{
		return (DatabaseEngine)libraries[Library.DATABASE.ordinal()];
	}

	/**
	 * Returns a reference to this instances Thread access library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.ThreadEngine
	 * @return a reference to this instances Thread access library.
	 */
	public final ThreadEngine _threads()
	{
		return (ThreadEngine)libraries[Library.THREADS.ordinal()];
	}

	/**
	 * Returns a reference to this instances Intermud3 access library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.I3Interface
	 * @return a reference to this instances Intermud3 access library.
	 */
	public final I3Interface _intermud()
	{
		return (I3Interface)libraries[Library.INTERMUD.ordinal()];
	}

	/**
	 * Returns a reference to this instances item balancing library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.ItemBalanceLibrary
	 * @return a reference to this instances item balancing library.
	 */
	public final ItemBalanceLibrary _itemBuilder()
	{
		return (ItemBalanceLibrary)libraries[Library.TIMS.ordinal()];
	}

	/**
	 * Returns a reference to this instances web macro filtering library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.WebMacroLibrary
	 * @return a reference to this instances web macro filtering library.
	 */
	public final WebMacroLibrary _webMacroFilter()
	{
		return (WebMacroLibrary)libraries[Library.WEBMACS.ordinal()];
	}

	/**
	 * Returns a reference to this instances string/item/object listing library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary
	 * @return a reference to this instances string/item/object listing library.
	 */
	public final ListingLibrary _lister()
	{
		return (ListingLibrary)libraries[Library.LISTER.ordinal()];
	}

	/**
	 * Returns a reference to this instances money handling library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MoneyLibrary
	 * @return a reference to this instances money handling library.
	 */
	public final MoneyLibrary _beanCounter()
	{
		return (MoneyLibrary)libraries[Library.MONEY.ordinal()];
	}

	/**
	 * Returns a reference to this instances store front/shopping library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.ShoppingLibrary
	 * @return a reference to this instances store front/shopping library.
	 */
	public final ShoppingLibrary _coffeeShops()
	{
		return (ShoppingLibrary)libraries[Library.SHOPS.ordinal()];
	}

	/**
	 * Returns a reference to this instances raw resource/material item library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaterialLibrary
	 * @return a reference to this instances raw resource/material item library.
	 */
	public final MaterialLibrary _materials()
	{
		return (MaterialLibrary)libraries[Library.MATERIALS.ordinal()];
	}

	/**
	 * Returns a reference to this instances combat library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.CombatLibrary
	 * @return a reference to this instances combat library.
	 */
	public final CombatLibrary _combat()
	{
		return (CombatLibrary)libraries[Library.COMBAT.ordinal()];
	}

	/**
	 * Returns a reference to this instances help file library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.HelpLibrary
	 * @return a reference to this instances help file library.
	 */
	public final HelpLibrary _help()
	{
		return (HelpLibrary)libraries[Library.HELP.ordinal()];
	}

	/**
	 * Returns a reference to this instances mob tracking/movement library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary
	 * @return a reference to this instances mob tracking/movement library.
	 */
	public final TrackingLibrary _tracking()
	{
		return (TrackingLibrary)libraries[Library.TRACKING.ordinal()];
	}

	/**
	 * Returns a reference to this instances legal and property library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.LegalLibrary
	 * @return a reference to this instances legal and property library.
	 */
	public final LegalLibrary _law()
	{
		return (LegalLibrary)libraries[Library.LEGAL.ordinal()];
	}

	/**
	 * Returns a reference to this instances object masking/filtering library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
	 * @return a reference to this instances object masking/filtering library.
	 */
	public final MaskingLibrary _masking()
	{
		return (MaskingLibrary)libraries[Library.MASKING.ordinal()];
	}

	/**
	 * Returns a reference to this instances chat channel library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary
	 * @return a reference to this instances chat channel library.
	 */
	public final ChannelsLibrary _channels()
	{
		return (ChannelsLibrary)libraries[Library.CHANNELS.ordinal()];
	}

	/**
	 * Returns a reference to this instances command shortcut and common event handler library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.CommonCommands
	 * @return a reference to this instances command shortcut and common event handler library.
	 */
	public final CommonCommands _commands()
	{
		return (CommonCommands)libraries[Library.COMMANDS.ordinal()];
	}

	/**
	 * Returns a reference to this instances achievement system library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary
	 * @return a reference to this instances achievement library.
	 */
	public final AchievementLibrary _achievements()
	{
		return (AchievementLibrary)libraries[Library.ACHIEVEMENTS.ordinal()];
	}

	/**
	 * Returns a reference to this instances english grammar and input utility library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.EnglishParsing
	 * @return a reference to this instances english grammar and input utility library.
	 */
	public final EnglishParsing _english()
	{
		return (EnglishParsing)libraries[Library.ENGLISH.ordinal()];
	}

	/**
	 * Returns a reference to this instances slavery and geas library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.SlaveryLibrary
	 * @return a reference to this instances slavery and geas library.
	 */
	public final SlaveryLibrary _slavery()
	{
		return (SlaveryLibrary)libraries[Library.SLAVERY.ordinal()];
	}

	/**
	 * Returns a reference to this instances message board and journal library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary
	 * @return a reference to this instances message board and journal library.
	 */
	public final JournalsLibrary _journals()
	{
		return (JournalsLibrary)libraries[Library.JOURNALS.ordinal()];
	}

	/**
	 * Returns a reference to this instances telnet input/output filtering library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.TelnetFilter
	 * @return a reference to this instances telnet input/output filtering library.
	 */
	public final TelnetFilter _coffeeFilter()
	{
		return (TelnetFilter)libraries[Library.TELNET.ordinal()];
	}

	/**
	 * Returns a reference to this instances GenObject low level construction library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.GenericBuilder
	 * @return a reference to this instances GenObject low level construction library.
	 */
	public final GenericBuilder _coffeeMaker()
	{
		return (GenericBuilder)libraries[Library.OBJBUILDERS.ordinal()];
	}

	/**
	 * Returns a reference to this instances space mapping library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.GalacticMap
	 * @return a reference to this instances space mapping library.
	 */
	public final GalacticMap _space()
	{
		return (GalacticMap)libraries[Library.GALACTIC.ordinal()];
	}

	/**
	 * Returns a reference to this instances city objects library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.CityMap
	 * @return a reference to this instances city objects library.
	 */
	public final CityMap _city()
	{
		return (CityMap)libraries[Library.CITY.ordinal()];
	}

	/**
	 * Returns a reference to this instances telnet session management library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.SessionsList
	 * @return a reference to this instances telnet session management library.
	 */
	public final SessionsList _sessions()
	{
		return (SessionsList)libraries[Library.SESSIONS.ordinal()];
	}

	/**
	 * Returns a reference to this instances flag checking shortcut library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.CMFlagLibrary
	 * @return a reference to this instances flag checking shortcut library.
	 */
	public final CMFlagLibrary _flags()
	{
		return (CMFlagLibrary)libraries[Library.FLAGS.ordinal()];
	}

	/**
	 * Returns a reference to this instances xml parsing library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary
	 * @return a reference to this instances xml parsing library.
	 */
	public final XMLLibrary _xml()
	{
		return (XMLLibrary)libraries[Library.XML.ordinal()];
	}

	/**
	 * Returns a reference to this instances social command collection/management library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.SocialsList
	 * @return a reference to this instances social command collection/management library.
	 */
	public final SocialsList _socials()
	{
		return (SocialsList)libraries[Library.SOCIALS.ordinal()];
	}

	/**
	 * Returns a reference to this instances random world utilities library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.CMMiscUtils
	 * @return a reference to this instances random world utilities library.
	 */
	public final CMMiscUtils _utensils()
	{
		return (CMMiscUtils)libraries[Library.UTENSILS.ordinal()];
	}

	/**
	 * Returns a reference to this instances statistics library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.StatisticsLibrary
	 * @return a reference to this instances statistics library.
	 */
	public final StatisticsLibrary _coffeeTables()
	{
		return (StatisticsLibrary)libraries[Library.STATS.ordinal()];
	}

	/**
	 * Returns a reference to this instances leveling and experience gaining library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.ExpLevelLibrary
	 * @return a reference to this instances leveling and experience gaining library.
	 */
	public final ExpLevelLibrary _leveler()
	{
		return (ExpLevelLibrary)libraries[Library.LEVELS.ordinal()];
	}

	/**
	 * Returns a reference to this instances areas and rooms access/management library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.WorldMap
	 * @return a reference to this instances areas and rooms access/management library.
	 */
	public final WorldMap _map()
	{
		return (WorldMap)libraries[Library.MAP.ordinal()];
	}

	/**
	 * Returns a reference to this instances quest collection/management library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.QuestManager
	 * @return a reference to this instances quest collection/management library.
	 */
	public final QuestManager _quests()
	{
		return (QuestManager)libraries[Library.QUEST.ordinal()];
	}

	/**
	 * Returns a reference to this instances random map/object generation library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary
	 * @return a reference to this instances random map/object generation library.
	 */
	public final AreaGenerationLibrary _percolator()
	{
		return (AreaGenerationLibrary)libraries[Library.AREAGEN.ordinal()];
	}

	/**
	 * Returns a reference to this instances abilities collection/management library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper
	 * @return a reference to this instances abilities collection/management library.
	 */
	public final AbilityMapper _ableMapper()
	{
		return (AbilityMapper)libraries[Library.ABLEMAP.ordinal()];
	}

	/**
	 * Returns a reference to this instances abilities components management library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.AbilityComponents
	 * @return a reference to this instances abilities components management library.
	 */
	public final AbilityComponents _ableComponents()
	{
		return (AbilityComponents)libraries[Library.ABLECOMP.ordinal()];
	}

	/**
	 * Returns a reference to this instances string hashing and compression library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.TextEncoders
	 * @return a reference to this instances string hashing and compression library.
	 */
	public final TextEncoders _encoder()
	{
		return (TextEncoders)libraries[Library.ENCODER.ordinal()];
	}

	/**
	 * Returns a reference to this instances email sending library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.SMTPLibrary
	 * @return a reference to this instances email sending library.
	 */
	public final SMTPLibrary _smtp()
	{
		return (SMTPLibrary)libraries[Library.SMTP.ordinal()];
	}

	/**
	 * Returns a reference to this instances localization library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.LanguageLibrary
	 * @return a reference to this instances localization library.
	 */
	public final LanguageLibrary _lang()
	{
		return (LanguageLibrary)libraries[Library.LANGUAGE.ordinal()];
	}

	/**
	 * Returns a reference to this instances random dice roll library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.DiceLibrary
	 * @return a reference to this instances random dice roll library.
	 */
	public final DiceLibrary _dice()
	{
		return (DiceLibrary)libraries[Library.DICE.ordinal()];
	}

	/**
	 * Returns a reference to this instances faction collection/management library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.FactionManager
	 * @return a reference to this instances faction collection/management library.
	 */
	public final FactionManager _factions()
	{
		return (FactionManager)libraries[Library.FACTIONS.ordinal()];
	}

	/**
	 * Returns a reference to this instances clan collection/management library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.ClanManager
	 * @return a reference to this instances clan collection/management library.
	 */
	public final ClanManager _clans()
	{
		return (ClanManager)libraries[Library.CLANS.ordinal()];
	}

	/**
	 * Returns a reference to this instances player poll collection/management library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.PollManager
	 * @return a reference to this instances player poll collection/management library.
	 */
	public final PollManager _polls()
	{
		return (PollManager)libraries[Library.POLLS.ordinal()];
	}

	/**
	 * Returns a reference to this instances real time utility library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.TimeManager
	 * @return a reference to this instances real time utility library.
	 */
	public final TimeManager _time()
	{
		return (TimeManager)libraries[Library.TIME.ordinal()];
	}

	/**
	 * Returns a reference to this instances ansi color library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.ColorLibrary
	 * @return a reference to this instances ansi color library.
	 */
	public final ColorLibrary _color()
	{
		return (ColorLibrary)libraries[Library.COLOR.ordinal()];
	}

	/**
	 * Returns a reference to this instances login and char creation library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.CharCreationLibrary
	 * @return a reference to this instances login and char creation library.
	 */
	public final CharCreationLibrary _login()
	{
		return (CharCreationLibrary)libraries[Library.LOGIN.ordinal()];
	}

	/**
	 * Returns a reference to this instances expertise collection/management library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary
	 * @return a reference to this instances expertise collection/management library.
	 */
	public final ExpertiseLibrary _expertises()
	{
		return (ExpertiseLibrary)libraries[Library.EXPERTISES.ordinal()];
	}

	/**
	 * Returns a reference to this instances player and account collection/management library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary
	 * @return a reference to this instances player and account collection/management library.
	 */
	public final PlayerLibrary _players()
	{
		return (PlayerLibrary)libraries[Library.PLAYERS.ordinal()];
	}

	/**
	 * Returns a reference to this instances cataloged mob/item collection/management library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.CatalogLibrary
	 * @return a reference to this instances cataloged mob/item collection/management library.
	 */
	public final CatalogLibrary _catalog()
	{
		return (CatalogLibrary)libraries[Library.CATALOG.ordinal()];
	}

	/**
	 * Returns a reference to this instances player titles collection/management library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.AutoAwardsLibrary
	 * @return a reference to this instances player titles collection/management library.
	 */
	public final AutoAwardsLibrary _titles()
	{
		return (AutoAwardsLibrary)libraries[Library.AUTOAWARDS.ordinal()];
	}

	/**
	 * Returns a reference to this instances recipe maker and skill parameter library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.AbilityParameters
	 * @return a reference to this instances recipe maker and skill parameter library.
	 */
	public final AbilityParameters _ableParms()
	{
		return (AbilityParameters)libraries[Library.ABLEPARMS.ordinal()];
	}

	/**
	 * Returns a reference to this instances generic object builder/editor and prompting library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.GenericEditor
	 * @return a reference to this instances generic object builder/editor and prompting library.
	 */
	public final GenericEditor _genEd()
	{
		return (GenericEditor)libraries[Library.GENEDITOR.ordinal()];
	}

	/**
	 * Returns a reference to this instances tech and electricity library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.TechLibrary
	 * @return a reference to this instances tech and electricity library.
	 */
	public final TechLibrary _tech()
	{
		return (TechLibrary)libraries[Library.TECH.ordinal()];
	}

	/**
	 * Returns a reference to this instances mud protocol mxp/msdp/etc library.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.ProtocolLibrary
	 * @return a reference to this instances mud protocol mxp/msdp/etc library.
	 */
	public final ProtocolLibrary _protocol()
	{
		return (ProtocolLibrary)libraries[Library.PROTOCOL.ordinal()];
	}

	/**
	 * Return the Library Enum entry that represents the ancestor
	 * of the given library object.
	 * @see CMLib.Library
	 * @param O the library object
	 * @return the Library Enum entry
	 */
	public static final Library convertToLibraryCode(final Object O)
	{
		if(O==null)
			return null;
		for(final Library lbry : Library.values())
		{
			if(CMClass.checkAncestry(O.getClass(),lbry.ancestor))
				return lbry;
		}
		return null;
	}

	/**
	 * Register the given library object as belonging to the thread
	 * group that called this method.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.CMLibrary
	 * @param O the library to register
	 */
	public static final void registerLibrary(final CMLibrary O)
	{
		final Library lbry=convertToLibraryCode(O);
		if(lbry!=null)
		{
			final int code=lbry.ordinal();
			if(l()==null)
				CMLib.initialize();
			if((!CMProps.isPrivateToMe(lbry.toString())
			&&(libs[MudHost.MAIN_HOST]!=l())))
			{
				if(libs[MudHost.MAIN_HOST].libraries[code]==null)
					libs[MudHost.MAIN_HOST].libraries[code]=O;
				else
					l().libraries[code]=libs[MudHost.MAIN_HOST].libraries[code];
			}
			else
			{
				l().libraries[code]=O;
			}
			l().registered[code]=true;
		}
	}

	/**
	 * Do your best to shut down the given thread, trying for at most sleepTime ms, and
	 * making as many number attempts as given.
	 * @param t the thread to kill
	 * @param sleepTime ms to wait for the thread to die between attempts
	 * @param attempts the number of attempts to make
	 */
	public static final void killThread(final Thread t, final long sleepTime, final int attempts)
	{
		if(t==null)
			return;
		if(t==Thread.currentThread())
			throw new java.lang.ThreadDeath();
		try
		{

			boolean stillAlive=false;
			if(t instanceof CMFactoryThread)
			{
				final Runnable r=CMLib.threads().findRunnableByThread(t);
				t.interrupt();
				for(int i=0;i<sleepTime;i++)
				{
					Thread.sleep(1);
					if(CMLib.threads().findRunnableByThread(t)!=r)
						return;
				}
				stillAlive=(CMLib.threads().findRunnableByThread(t)==r);
			}
			else
			{
				t.interrupt();
				CMLib.s_sleep(sleepTime);
				int att=0;
				while((att++<attempts)&&t.isAlive())
				{
					try
					{
						 Thread.sleep(sleepTime);
					}
					catch(final Exception e)
					{
					}
					try
					{
						 t.interrupt();
					}
					catch(final Exception e)
					{
					}
				}
				stillAlive=t.isAlive();
			}
			try
			{
				if(stillAlive)
				{
					final java.lang.StackTraceElement[] s=t.getStackTrace();
					final StringBuffer dump = new StringBuffer("Unable to kill thread "+t.getName()+".  It is still running.\n\r");
					for (final StackTraceElement element : s)
						dump.append("\n   "+element.getClassName()+": "+element.getMethodName()+"("+element.getFileName()+": "+element.getLineNumber()+")");
					Log.errOut(dump.toString());
				}
			}
			catch(final java.lang.ThreadDeath td)
			{
			}
		}
		catch(final Throwable th)
		{
		}

	}

	/**
	 * Sleep for the given ms without throwing an exception
	 * @param millis the ms to sleep
	 * @return true
	 */
	public static final boolean s_sleep(final long millis)
	{
		try
		{
			Thread.sleep(millis);
		}
		catch(final java.lang.InterruptedException ex)
		{
			return false;
		}
		return true;
	}

	/**
	 * Signify to the library library (this), that the ini file has been loaded,
	 * and that all registered libraries need to be likewise notified.
	 */
	public static final void propertiesLoaded()
	{
		final CMLib lib=l();
		for(final Library lbry : Library.values())
		{
			if((!CMProps.isPrivateToMe(lbry.toString())&&(libs[MudHost.MAIN_HOST]!=lib)))
			{
			}
			else
			if(lib.libraries[lbry.ordinal()]==null)
			{
			}
			else
				lib.libraries[lbry.ordinal()].propertiesLoaded();
		}
		CharStats.CODES.reset();
		RawMaterial.CODES.reset();
		Wearable.CODES.reset();
	}

	/**
	 * Signify to the library library (this) that all of the library classes have
	 * been registered, and that any missing libraries are to share code with the
	 * thread 0 (base) set.
	 */
	public static final void activateLibraries()
	{
		final CMLib lib=l();
		for(final Library lbry : Library.values())
		{
			if((!CMProps.isPrivateToMe(lbry.toString())&&(libs[MudHost.MAIN_HOST]!=lib)))
			{
				if(CMSecurity.isDebugging(DbgFlag.BOOTSTRAPPER))
					Log.debugOut("HOST"+Thread.currentThread().getThreadGroup().getName().charAt(0)+" sharing library "+lbry.toString());
				lib.libraries[lbry.ordinal()]=libs[MudHost.MAIN_HOST].libraries[lbry.ordinal()];
			}
			else
			if(lib.libraries[lbry.ordinal()]==null)
				Log.errOut("Unable to find library "+lbry.toString());
			else
				lib.libraries[lbry.ordinal()].activate();
		}
	}

	/**
	 * Return the library belonging to the given thread group code, and the given
	 * Library Enum
	 * @see CMLib.Library
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.CMLibrary
	 * @param tcode the thread group code
	 * @param lcode the Library Enum
	 * @return the appropriate library belonging to the thread group and code
	 */
	public final static CMLibrary library(final char tcode, final Library lcode)
	{
		if(libs[tcode]!=null)
			return libs[tcode].libraries[lcode.ordinal()];
		return null;
	}

	/**
	 * Returns an enumeration of all library objects of the Library Enum type given
	 * across all thread groups.
	 * @see CMLib.Library
	 * @param code the Library Enum
	 * @return an enumeration of all library objects in all threads of that type
	 */
	public final static Enumeration<CMLibrary> libraries(final Library code)
	{
		final LinkedList<CMLibrary> V=new LinkedList<CMLibrary>();
		for(int l=0;l<libs.length;l++)
		{
			if((libs[l]!=null)
			&&(libs[l].libraries[code.ordinal()]!=null)
			&&(!V.contains(libs[l].libraries[code.ordinal()])))
				V.add(libs[l].libraries[code.ordinal()]);
		}
		return new IteratorEnumeration<CMLibrary>(V.iterator());
	}

	/**
	 * Return the private thread id that belongs to the given library
	 * @param code the library code
	 * @param lib the library to look for
	 * @return that libraries code
	 */
	public final static char getLibraryThreadID(final Library code, final CMLibrary lib)
	{
		if(code != null)
		{
			for(int l=0;l<libs.length;l++)
			{
				if((libs[l]!=null)
				&&(libs[l].libraries[code.ordinal()]==lib))
					return (char)l;
			}
		}
		return '\0';
	}

	/**
	 * Return the private thread id that belongs to the given library
	 * @param lib the library to look for
	 * @return that libraries code
	 */
	public final static char getLibraryThreadID(final CMLibrary lib)
	{
		return getLibraryThreadID(CMLib.convertToLibraryCode(lib), lib);
	}

	/**
	 * Returns the set of CMLibrarys of the *GIVEN* code type that are shared with the
	 * given library instance of any type.  For example, if 3 hosts share a WorldMap, but
	 * each have different database engines, you can return the database for each.
	 *
	 * @param codeTypeToGet the type of libraries to return
	 * @param lib the library instance all hosts must share to return a representative from
	 * @return the list of libraries that share the instance example
	 */
	public final static Set<CMLibrary> getLibrariesSharedWith(final Library codeTypeToGet, final CMLibrary lib)
	{
		final Set<CMLibrary> set = new HashSet<CMLibrary>();
		if(lib == null)
			return set;
		final Library codeTypeSent = CMLib.convertToLibraryCode(lib);
		if(codeTypeSent == null)
			return set;
		for(int l=0;l<libs.length;l++)
		{
			if((libs[l]!=null)
			&&(libs[l].libraries[codeTypeSent.ordinal()]==lib))
			{
				final CMLibrary candidateLib = libs[l].libraries[codeTypeToGet.ordinal()];
				if(!set.contains(candidateLib))
					set.add(candidateLib);
			}
		}
		return set;
	}

	/**
	 * Calls registerLibrary on all the given CMLibrary objects
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.CMLibrary
	 * @see CMLib#registerLibrary(CMLibrary)
	 * @param e an enumeration of CMLibrary objects
	 */
	public static final void registerLibraries(final Enumeration<CMLibrary> e)
	{
		for(;e.hasMoreElements();)
			registerLibrary(e.nextElement());
	}

	/**
	 * Returns how many CMLibrary objects have been registered for this
	 * thread group.
	 * @return a count of CMLibrary objects registered
	 */
	public static final int countRegistered()
	{
		int x=0;
		for (final boolean element : l().registered)
		{
			if(element)
				x++;
		}
		return x;
	}

	/**
	 * Returns a comma-delimited list of the ordinal numbers of those
	 * libraries which have not been registered for this thread group.
	 * @return a string list of unregistered library ordinals
	 */
	public static final String unregistered()
	{
		final StringBuffer str=new StringBuffer("");
		for(int i=0;i<l().registered.length;i++)
		{
			if(!l().registered[i])
				str.append(""+i+", ");
		}
		return str.toString();
	}
}
