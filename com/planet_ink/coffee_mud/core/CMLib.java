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
import java.lang.reflect.Modifier;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;


/*
   Copyright 2000-2014 Bo Zimmerman

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
public class CMLib
{
	public final String getClassName(){return "CMLib";}
	private static final SVector<MudHost> mudThreads=new SVector<MudHost>();
	private static final CMLib[] libs=new CMLib[256];
	public CMLib()
	{
		super();
		final char c=Thread.currentThread().getThreadGroup().getName().charAt(0);
		if(libs[c]==null) libs[c]=this;
	}
	private static final CMLib l(){ return libs[Thread.currentThread().getThreadGroup().getName().charAt(0)];}
	public static final CMLib initialize(){ return new CMLib(); }
	public static final CMLib l(final char c){return libs[c];}
	public static final CMLib instance(){return l();}
	
	private final CMLibrary[] 	libraries=new CMLibrary[Library.values().length];
	private final boolean[]		registered=new boolean[Library.values().length];

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
		TITLES(AutoTitlesLibrary.class),
		ABLEPARMS(AbilityParameters.class), 
		GENEDITOR(GenericEditor.class), 
		AREAGEN(AreaGenerationLibrary.class), 
		TECH(TechLibrary.class), 
		PROTOCOL(ProtocolLibrary.class);
		
		public final Class<?> ancestor;
		private Library(Class<?> ancestorC1)
		{
			this.ancestor=ancestorC1;
		}
	}

	public static final CMath math(){return CMath.instance();}
	public static final CMParms parms(){return CMParms.instance();}
	public static final CMStrings strings(){return CMStrings.instance();}
	public static final CMClass classes(){return CMClass.instance();}
	public static final CMSecurity security(){return CMSecurity.instance();}
	public static final Directions directions(){return Directions.instance();}
	public static final Log log(){return Log.instance();}
	public static final List<MudHost> hosts(){return mudThreads;}
	
	public static final MudHost mud(int port)
	{
		if(mudThreads.size()==0)
			return null;
		else
		if(port<=0)
			return mudThreads.firstElement();
		else
		for(int i=0;i<mudThreads.size();i++)
			if(mudThreads.elementAt(i).getPort()==port)
				return mudThreads.elementAt(i);
		return null;
	}
	public static final Resources resources(){return Resources.instance();}
	public static final CMProps props(){return CMProps.instance();}
	public static final Enumeration<CMLibrary> libraries()
	{
		final Vector<CMLibrary> V=new Vector<CMLibrary>();
		for(Library lbry : Library.values())
			if(l().libraries[lbry.ordinal()]!=null)
				V.add(l().libraries[lbry.ordinal()]);
		return V.elements();
	}

	public static final DatabaseEngine database(){return (DatabaseEngine)l().libraries[Library.DATABASE.ordinal()];}
	public static final ThreadEngine threads(){return (ThreadEngine)l().libraries[Library.THREADS.ordinal()];}
	public static final I3Interface intermud(){return (I3Interface)l().libraries[Library.INTERMUD.ordinal()];}
	public static final ItemBalanceLibrary itemBuilder(){return (ItemBalanceLibrary)l().libraries[Library.TIMS.ordinal()];}
	public static final WebMacroLibrary webMacroFilter(){return (WebMacroLibrary)l().libraries[Library.WEBMACS.ordinal()];}
	public static final ListingLibrary lister(){return (ListingLibrary)l().libraries[Library.LISTER.ordinal()];}
	public static final MoneyLibrary beanCounter(){return (MoneyLibrary)l().libraries[Library.MONEY.ordinal()];}
	public static final ShoppingLibrary coffeeShops(){return (ShoppingLibrary)l().libraries[Library.SHOPS.ordinal()];}
	public static final MaterialLibrary materials(){return (MaterialLibrary)l().libraries[Library.MATERIALS.ordinal()];}
	public static final CombatLibrary combat(){return (CombatLibrary)l().libraries[Library.COMBAT.ordinal()];}
	public static final HelpLibrary help(){return (HelpLibrary)l().libraries[Library.HELP.ordinal()];}
	public static final TrackingLibrary tracking(){return (TrackingLibrary)l().libraries[Library.TRACKING.ordinal()];}
	public static final LegalLibrary law(){return (LegalLibrary)l().libraries[Library.LEGAL.ordinal()];}
	public static final MaskingLibrary masking(){return (MaskingLibrary)l().libraries[Library.MASKING.ordinal()];}
	public static final ChannelsLibrary channels(){return (ChannelsLibrary)l().libraries[Library.CHANNELS.ordinal()];}
	public static final CommonCommands commands(){return (CommonCommands)l().libraries[Library.COMMANDS.ordinal()];}
	public static final EnglishParsing english(){return (EnglishParsing)l().libraries[Library.ENGLISH.ordinal()];}
	public static final SlaveryLibrary slavery(){return (SlaveryLibrary)l().libraries[Library.SLAVERY.ordinal()];}
	public static final JournalsLibrary journals(){return (JournalsLibrary)l().libraries[Library.JOURNALS.ordinal()];}
	public static final TelnetFilter coffeeFilter(){return (TelnetFilter)l().libraries[Library.TELNET.ordinal()];}
	public static final GenericBuilder coffeeMaker(){return (GenericBuilder)l().libraries[Library.OBJBUILDERS.ordinal()];}
	public static final SessionsList sessions(){return (SessionsList)l().libraries[Library.SESSIONS.ordinal()];}
	public static final CMFlagLibrary flags(){return (CMFlagLibrary)l().libraries[Library.FLAGS.ordinal()];}
	public static final XMLLibrary xml(){return (XMLLibrary)l().libraries[Library.XML.ordinal()];}
	public static final SocialsList socials(){return (SocialsList)l().libraries[Library.SOCIALS.ordinal()];}
	public static final CMMiscUtils utensils(){return (CMMiscUtils)l().libraries[Library.UTENSILS.ordinal()];}
	public static final StatisticsLibrary coffeeTables(){return (StatisticsLibrary)l().libraries[Library.STATS.ordinal()];}
	public static final ExpLevelLibrary leveler(){return (ExpLevelLibrary)l().libraries[Library.LEVELS.ordinal()];}
	public static final WorldMap map(){return (WorldMap)l().libraries[Library.MAP.ordinal()];}
	public static final QuestManager quests(){return (QuestManager)l().libraries[Library.QUEST.ordinal()];}
	public static final AreaGenerationLibrary percolator(){return (AreaGenerationLibrary)l().libraries[Library.AREAGEN.ordinal()];}
	public static final AbilityMapper ableMapper(){return (AbilityMapper)l().libraries[Library.ABLEMAP.ordinal()];}
	public static final TextEncoders encoder(){return (TextEncoders)l().libraries[Library.ENCODER.ordinal()];}
	public static final SMTPLibrary smtp(){return (SMTPLibrary)l().libraries[Library.SMTP.ordinal()];}
	public static final LanguageLibrary lang(){return (LanguageLibrary)l().libraries[Library.LANGUAGE.ordinal()];}
	public static final DiceLibrary dice(){return (DiceLibrary)l().libraries[Library.DICE.ordinal()];}
	public static final FactionManager factions(){return (FactionManager)l().libraries[Library.FACTIONS.ordinal()];}
	public static final ClanManager clans(){return (ClanManager)l().libraries[Library.CLANS.ordinal()];}
	public static final PollManager polls(){return (PollManager)l().libraries[Library.POLLS.ordinal()];}
	public static final TimeManager time(){return (TimeManager)l().libraries[Library.TIME.ordinal()];}
	public static final ColorLibrary color(){return (ColorLibrary)l().libraries[Library.COLOR.ordinal()];}
	public static final CharCreationLibrary login(){return (CharCreationLibrary)l().libraries[Library.LOGIN.ordinal()];}
	public static final ExpertiseLibrary expertises(){return (ExpertiseLibrary)l().libraries[Library.EXPERTISES.ordinal()];}
	public static final PlayerLibrary players(){return (PlayerLibrary)l().libraries[Library.PLAYERS.ordinal()];}
	public static final CatalogLibrary catalog(){return (CatalogLibrary)l().libraries[Library.CATALOG.ordinal()];}
	public static final AutoTitlesLibrary titles(){return (AutoTitlesLibrary)l().libraries[Library.TITLES.ordinal()];}
	public static final AbilityParameters ableParms(){return (AbilityParameters)l().libraries[Library.ABLEPARMS.ordinal()];}
	public static final GenericEditor genEd(){return (GenericEditor)l().libraries[Library.GENEDITOR.ordinal()];}
	public static final TechLibrary tech(){ return (TechLibrary)l().libraries[Library.TECH.ordinal()];}
	public static final ProtocolLibrary protocol() { return (ProtocolLibrary)l().libraries[Library.PROTOCOL.ordinal()];}

	public static final Library convertToLibraryCode(final Object O)
	{
		if(O==null)
			return null;
		for(Library lbry : Library.values())
			if(CMClass.checkAncestry(O.getClass(),lbry.ancestor))
				return lbry;
		
		return null;
	}

	public static final void registerLibrary(final CMLibrary O)
	{
		final Library lbry=convertToLibraryCode(O);
		if(lbry!=null)
		{
			final int code=lbry.ordinal();
			if(l()==null) CMLib.initialize();
			if((!CMProps.isPrivateToMe(lbry.toString())
			&&(libs[MudHost.MAIN_HOST]!=l())))
			{
				if(libs[MudHost.MAIN_HOST].libraries[code]==null)
					libs[MudHost.MAIN_HOST].libraries[code]=O;
				else
					l().libraries[code]=libs[MudHost.MAIN_HOST].libraries[code];
			}
			else
				l().libraries[code]=O;
			l().registered[code]=true;
		}
	}
	
	public static final void killThread(final Thread t, final long sleepTime, final int attempts)
	{
		if(t==null) return;
		if(t==Thread.currentThread())
			throw new java.lang.ThreadDeath();
		try
		{
			
			boolean stillAlive=false;
			if(t instanceof CMFactoryThread)
			{
				Runnable r=CMLib.threads().findRunnableByThread(t);
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
				try{Thread.sleep(sleepTime);}catch(Exception e){}
				int att=0;
				while((att++<attempts)&&t.isAlive())
				{
					try { Thread.sleep(sleepTime); }catch(Exception e){}
					try { t.interrupt(); }catch(Exception e){}
				}
				stillAlive=t.isAlive();
			}
			try
			{
				if(stillAlive) 
				{ 
					java.lang.StackTraceElement[] s=t.getStackTrace();
					StringBuffer dump = new StringBuffer("Unable to kill thread "+t.getName()+".  It is still running.\n\r");
					for(int i=0;i<s.length;i++)
						dump.append("\n   "+s[i].getClassName()+": "+s[i].getMethodName()+"("+s[i].getFileName()+": "+s[i].getLineNumber()+")");
					Log.errOut(dump.toString());
				} 
			} catch(java.lang.ThreadDeath td) {}
		}
		catch(Throwable th){}

	}
	
	public static final boolean s_sleep(final long millis)
	{
		try{ Thread.sleep(millis); } catch(java.lang.InterruptedException ex) { return false;}
		return true;
	}

	public static final void propertiesLoaded() 
	{
		final CMLib lib=l();
		for(Library lbry : Library.values())
		{
			if((!CMProps.isPrivateToMe(lbry.toString())&&(libs[MudHost.MAIN_HOST]!=lib)))
			{}
			else
			if(lib.libraries[lbry.ordinal()]==null)
			{}
			else
				lib.libraries[lbry.ordinal()].propertiesLoaded();
		}
		CharStats.CODES.reset();
		RawMaterial.CODES.reset();
		Wearable.CODES.reset();
	}
	
	public static final void activateLibraries() 
	{
		final CMLib lib=l();
		for(Library lbry : Library.values())
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
	
	public final static CMLibrary library(final char tcode, final Library lcode) 
	{
		if(libs[tcode]!=null)
			return libs[tcode].libraries[lcode.ordinal()];
		return null;
	}
	
	public final static Enumeration<CMLibrary> libraries(final Library code) 
	{
		final Vector<CMLibrary> V=new Vector<CMLibrary>();
		for(int l=0;l<libs.length;l++)
			if((libs[l]!=null)
			&&(libs[l].libraries[code.ordinal()]!=null)
			&&(!V.contains(libs[l].libraries[code.ordinal()])))
				V.addElement(libs[l].libraries[code.ordinal()]);
		return V.elements();
	}
	
	public static final void registerLibraries(final Enumeration<CMLibrary> e)
	{
		for(;e.hasMoreElements();)
			registerLibrary(e.nextElement());
	}
	
	public static final int countRegistered()
	{
		int x=0;
		for(int i=0;i<l().registered.length;i++)
			if(l().registered[i]) x++;
		return x;
	}
	public static final String unregistered()
	{
		final StringBuffer str=new StringBuffer("");
		for(int i=0;i<l().registered.length;i++)
			if(!l().registered[i]) str.append(""+i+", ");
		return str.toString();
	}
}
