package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMLib.Library;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2008-2024 Bo Zimmerman

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
public class AutoAwards extends StdLibrary implements AutoAwardsLibrary
{
	@Override
	public String ID()
	{
		return "AutoAwards";
	}

	private Map<String, AutoTitle>	autoTitles		= null;
	private Integer					autoPropHash	= null;
	private Vector<AutoProperties>	autoProperties	= null;

	private static final String titleFilename = "titles.ini";
	private static final String propsFilename = "awards.txt";

	@Override
	public String getAutoTitleFilename()
	{
		CMFile F = new CMFile(Resources.makeFileResourceName(titleFilename),null);
		if(F.exists() && (F.canRead()))
			return titleFilename;
		final String oldFilename = titleFilename.substring(0,titleFilename.length()-4)+".txt";
		F = new CMFile(Resources.makeFileResourceName(oldFilename),null);
		if(F.exists() && (F.canRead()))
			return oldFilename;
		return titleFilename;
	}

	@Override
	public String getAutoPropsFilename()
	{
		return propsFilename;
	}

	@Override
	public String evaluateAutoTitle(final String row, final boolean addIfPossible)
	{
		if(row.trim().startsWith("#")||row.trim().startsWith(";")||(row.trim().length()==0))
			return null;
		int x=row.indexOf('=');
		while((x>=1)&&(row.charAt(x-1)=='\\'))
			x=row.indexOf('=',x+1);
		if(x<0)
			return "Error: Invalid line! Not comment, whitespace, and does not contain an = sign!";
		final String title=row.substring(0,x).trim();
		final String partialmask=row.substring(x+1).trim();
		final int colon=partialmask.indexOf(':');
		final int max;
		final String mask;
		if((colon>0)&&(CMath.isInteger(partialmask.substring(0,colon).trim())))
		{
			max=CMath.s_int(partialmask.substring(0,colon).trim());
			mask=partialmask.substring(colon+1);
		}
		else
		{
			mask=partialmask;
			max=0;
		}
		if(title.length()==0)
			return "Error: Blank title: "+title+"="+mask+"!";
		if(mask.length()==0)
			return "Error: Blank mask: "+title+"="+mask+"!";
		if(addIfPossible)
		{
			if(autoTitles==null)
				reloadAutoTitles();
			for(final String ID : autoTitles.keySet())
			{
				if(ID.equalsIgnoreCase(title))
					return "Error: Duplicate title: "+title+"="+mask+"!";
			}
			final AutoTitle A = new AutoTitle()
			{
				private final String		t	= title;
				private final String		m	= mask;
				private final int			x	= max;
				private final CompiledZMask	M	= CMLib.masking().maskCompile(mask);
				private volatile int		ctr	= 0;

				@Override
				public String getTitle()
				{
					return t;
				}

				@Override
				public String getMaskStr()
				{
					return m;
				}

				@Override
				public CompiledZMask getMask()
				{
					return M;
				}

				@Override
				public int getMax()
				{
					return x;
				}

				@Override
				public int bumpCounter(final int amt)
				{
					ctr += amt;
					return ctr;
				}
			};
			autoTitles.put(title,A);
			if(A.getMax() > 0)
				updateTitleMax(A);
		}
		return null;
	}

	@Override
	public boolean isExistingAutoTitle(String title)
	{
		if(autoTitles==null)
			reloadAutoTitles();
		title=title.trim();
		if(autoTitles.containsKey(title))
			return true;
		for(final String ID : autoTitles.keySet())
		{
			if(ID.equalsIgnoreCase(title))
				return true;
		}
		return false;
	}

	@Override
	public Enumeration<String> autoTitles()
	{
		if(autoTitles==null)
			reloadAutoTitles();
		return new IteratorEnumeration<String>(autoTitles.keySet().iterator());
	}

	@Override
	public String getAutoTitleMask(final String title)
	{
		if(autoTitles==null)
			reloadAutoTitles();
		if(autoTitles.containsKey(title))
			return autoTitles.get(title).getMaskStr();
		for(final String ID : autoTitles.keySet())
		{
			if(ID.equalsIgnoreCase(title))
				return autoTitles.get(ID).getMaskStr();
		}
		return "";
	}

	@Override
	public boolean evaluateAutoTitles(final MOB mob)
	{
		if(mob==null)
			return false;
		final PlayerStats P=mob.playerStats();
		if(P==null)
			return false;
		if(autoTitles==null)
			reloadAutoTitles();
		int pdex=0;
		final List<String> ptV=P.getTitles();
		boolean somethingDone=false;
		synchronized(P)
		{
			for(final String title : autoTitles.keySet())
			{
				final AutoTitle A = autoTitles.get(title);
				final CompiledZMask mask = A.getMask();
				final String fixedTitle = title.replace('\'', '`');
				pdex=getTitleIndex(ptV, title, fixedTitle);
				if(CMLib.masking().maskCheck(mask,mob,true))
				{
					if(pdex<0)
					{
						P.addTitle(title);
						P.addTitle(title); // put it on TOP!
						A.bumpCounter(1);
						if((A.getMax()>0)&&(A.bumpCounter(0)>A.getMax()))
							dispossessOldTimers(A,mob);
						somethingDone=true;
					}
				}
				else
				if(pdex>=0)
				{
					if(P.delTitle(title)||P.delTitle(fixedTitle))
					{
						somethingDone=true;
						A.bumpCounter(-1);
					}
				}
			}
		}
		return somethingDone;
	}

	protected int getTitleIndex(final List<String> ptV, final String title, final String fixedTitle)
	{
		int pdex=ptV.indexOf(title);
		if(pdex<0)
		{
			for(int p=ptV.size()-1;p>=0;p--)
			{
				try
				{
					final String tit=ptV.get(p).replace('\'', '`');
					if(tit.equalsIgnoreCase(fixedTitle))
					{
						pdex=p;
						break;
					}
				}
				catch(final IndexOutOfBoundsException ioe)
				{
				}
			}
		}
		return pdex;
	}

	protected void updateTitleMax(final AutoTitle A)
	{
		final String title = A.getTitle();
		final String fixedTitle = title.replace('\'', '`');
		final PairList<String,Long> players = CMLib.database().DBSearchPFIL("<TITLE>"+title+"</TITLE>");
		for(final Pair<String, Long> player : players)
		{
			final String playerName = player.first;
			final MOB chkM=CMLib.players().getPlayer(playerName);
			final PlayerStats pStats;
			if(chkM != null)
				pStats = chkM.playerStats();
			else
				pStats = CMLib.database().DBLoadPlayerStats(playerName);
			if(pStats != null)
			{
				final List<String> ptV=pStats.getTitles();
				synchronized(pStats)
				{
					final int pdex=getTitleIndex(ptV,title,fixedTitle);
					if(pdex>=0)
						A.bumpCounter(1);
				}
			}
		}
	}

	protected void dispossessOldTimers(final AutoTitle A, final MOB mob)
	{
		final String fixedTitle = A.getTitle().replace('\'', '`');
		final PairList<String,Long> players = CMLib.database().DBSearchPFIL("<TITLE>"+A.getTitle()+"</TITLE>");
		for(final Pair<String, Long> player : players)
		{
			final MOB M=CMLib.players().getPlayerAllHosts(player.first);
			if((M!=null)
			&&(M.playerStats()!=null))
				player.second=Long.valueOf(M.playerStats().getLastDateTime());
		}
		Collections.sort(players, new Comparator<Pair<String,Long>>()
		{
			@Override
			public int compare(final Pair<String, Long> o1, final Pair<String, Long> o2)
			{
				return o1.second.compareTo(o2.second);
			}
		});
		for(final Pair<String, Long> player : players)
		{
			if(A.bumpCounter(0)<=A.getMax())
				break;
			if(player.first.equalsIgnoreCase(mob.Name()))
				continue;
			final MOB chkM=CMLib.players().getPlayer(player.first);
			final PlayerStats pStats;
			if(chkM != null)
				pStats = chkM.playerStats();
			else
				pStats = CMLib.database().DBLoadPlayerStats(player.first);
			if(pStats != null)
			{
				final List<String> ptV=pStats.getTitles();
				synchronized(pStats)
				{
					final int pdex=getTitleIndex(ptV,A.getTitle(),fixedTitle);
					if(pdex>=0)
					{
						final MOB M=CMLib.players().getLoadPlayer(player.first);
						if((M!=null)
						&&(M.playerStats()!=null))
						{
							if(M.playerStats().delTitle(A.getTitle())
							||M.playerStats().delTitle(fixedTitle))
							{
								A.bumpCounter(-1);
								CMLib.database().DBUpdatePlayerPlayerStats(M);
							}
						}
					}
				}
			}
		}
	}

	protected void dispossesTitle(final String title)
	{
		final String fixedTitle = title.replace('\'', '`');
		final PairList<String,Long> players = CMLib.database().DBSearchPFIL("<TITLE>"+title+"</TITLE>");
		for(final Pair<String, Long> player : players)
		{
			final String playerName = player.first;
			final MOB chkM=CMLib.players().getPlayer(playerName);
			final PlayerStats pStats;
			if(chkM != null)
				pStats = chkM.playerStats();
			else
				pStats = CMLib.database().DBLoadPlayerStats(playerName);
			if(pStats != null)
			{
				final List<String> ptV=pStats.getTitles();
				synchronized(pStats)
				{
					final int pdex=getTitleIndex(ptV,title,fixedTitle);
					if(pdex>=0)
					{
						final MOB M=CMLib.players().getLoadPlayer(playerName);
						if((M!=null)
						&&(M.playerStats()!=null))
						{
							if(M.playerStats().delTitle(title)
							||M.playerStats().delTitle(fixedTitle))
								CMLib.database().DBUpdatePlayerPlayerStats(M);
						}
					}
				}
			}
		}
	}

	@Override
	public AutoTitle getAutoTitle(String title)
	{
		if(autoTitles==null)
			reloadAutoTitles();
		title=title.trim();
		if(autoTitles.containsKey(title))
			return autoTitles.get(title);
		for(final String ID : autoTitles.keySet())
		{
			if(ID.equalsIgnoreCase(title))
				return autoTitles.get(ID);
		}
		return null;
	}

	@Override
	public void appendAutoTitle(final String text)
	{
		Resources.removeResource(getAutoTitleFilename());
		final CMFile F=new CMFile(Resources.makeFileResourceName(titleFilename),null,CMFile.FLAG_LOGERRORS);
		F.saveText(text,true);
		reloadAutoTitles();
	}

	@Override
	public String deleteTitleAndResave(final String title)
	{
		dispossesTitle(title);
		final Set<CMLibrary> playerLibSets = CMLib.getLibrariesSharedWith(Library.PLAYERS, this);
		for(final CMLibrary playerLib : playerLibSets)
		{
			if(playerLib != CMLib.players())
			{
				final char otherHostThreadId = CMLib.getLibraryThreadID(Library.PLAYERS, playerLib);
				CMLib.threads().executeRunnable(otherHostThreadId, new Runnable() {
					@Override
					public void run()
					{
						if(CMLib.awards() instanceof AutoAwards)
							((AutoAwards)CMLib.awards()).dispossesTitle(title);
					}
				});
			}
		}
		final CMFile F=new CMFile(Resources.makeFileResourceName(titleFilename),null,CMFile.FLAG_LOGERRORS);
		if(F.exists())
		{
			final boolean removed=Resources.findRemoveProperty(F, title);
			if(removed)
			{
				Resources.removeResource(titleFilename);
				reloadAutoTitles();
				return null;
			}
			return "Unable to delete title!";
		}
		return "Unable to open "+titleFilename+"!";
	}

	@Override
	public void reloadAutoTitles()
	{
		autoTitles=new STreeMap<String,AutoTitle>();
		final Iterator<String> k=Resources.findResourceKeys(getAutoTitleFilename());
		while(k.hasNext())
			Resources.removeResource(k.next());
		final List<String> V=Resources.getFileLineVector(Resources.getFileResource(getAutoTitleFilename(),true));
		String WKID=null;
		for(int v=0;v<V.size();v++)
		{
			final String row=V.get(v);
			WKID=evaluateAutoTitle(row,true);
			if(WKID==null)
				continue;
			if(WKID.startsWith("Error: "))
				Log.errOut("CharCreation",WKID);
		}
		for(final Enumeration<MOB> e=CMLib.players().players();e.hasMoreElements();)
		{
			final MOB M=e.nextElement();
			if(M.playerStats()!=null)
			{
				if(evaluateAutoTitles(M))
					CMLib.database().DBUpdatePlayerPlayerStats(M);
			}
		}
	}

	@Override
	public String getAutoAwardInstructions(final String filename)
	{
		final StringBuffer buf=new CMFile(Resources.makeFileResourceName(filename),null,CMFile.FLAG_LOGERRORS).text();
		final StringBuffer inst=new StringBuffer("");
		List<String> V=new ArrayList<String>();
		if(buf!=null)
			V=Resources.getFileLineVector(buf);
		for(int v=0;v<V.size();v++)
		{
			if(V.get(v).startsWith("#"))
				inst.append(V.get(v).substring(1)+"\n\r");
			else
			if(V.get(v).length()>0)
				break;
		}
		return inst.toString();
	}

	protected final static class AutoPropertiesImpl implements AutoProperties
	{
		public final String					playerMask;
		public final String					dateMask;
		public final CompiledZMask			playerCMask;
		public final CompiledZMask			dateCMask;
		public final Pair<String, String>[]	props;
		public final TimePeriod				period;
		private final int					hashCode;

		public AutoPropertiesImpl(final String pMask, final String dMask, final PairList<String,String> ps)
		{
			playerMask = pMask;
			playerCMask = CMLib.masking().maskCompile(playerMask);
			dateMask = dMask;
			dateCMask = CMLib.masking().maskCompile(dateMask);
			@SuppressWarnings("unchecked")
			final Pair<String,String>[] base = new Pair[ps.size()];
			props = ps.toArray(base);
			TimePeriod per = null;
			final String udmask = dMask.toUpperCase();
			for(final TimePeriod p : TimePeriod.values())
			{
				if(udmask.indexOf(p.name())>=0)
				{
					per=p;
					break;
				}
			}
			period = (per == null) ? TimePeriod.YEAR : per;
			int hc = 0;
			hc = playerCMask.hashCode() ^ dateCMask.hashCode() ^ period.hashCode();
			for(final Pair<String,String> p : props)
				hc = (hc << 8) ^ (p.first.hashCode() ^ p.second.hashCode());
			hashCode = hc;
		}

		@Override
		public int hashCode()
		{
			return hashCode;
		}

		@Override
		public String getPlayerMask()
		{
			return playerMask;
		}

		@Override
		public String getDateMask()
		{
			return dateMask;
		}

		@Override
		public CompiledZMask getPlayerCMask()
		{
			return playerCMask;
		}

		@Override
		public CompiledZMask getDateCMask()
		{
			return dateCMask;
		}

		@Override
		public Pair<String, String>[] getProps()
		{
			return props;
		}

		@Override
		public TimePeriod getPeriod()
		{
			return period;
		}
	}

	@Override
	public Enumeration<AutoProperties> getAutoProperties()
	{
		final Vector<AutoProperties> props = getAllAutoAwards();
		return props.elements();
	}

	@Override
	public int getAutoPropertiesHash()
	{
		Integer hash;
		synchronized(this)
		{
			hash = autoPropHash;
		}
		if(hash != null)
			return hash.intValue();
		int hashh = 0;
		for(final AutoProperties a : getAllAutoAwards())
			hashh = (hashh << 8) ^ a.hashCode();
		autoPropHash = Integer.valueOf(hashh);
		return hashh;
	}

	protected boolean addProp(final PairList<String,String> fprops, final String propID, final String arg, final String s)
	{
		final Ability A=CMClass.getAbility(propID);
		if(A==null)
		{
			final Behavior B = CMClass.getBehavior(propID);
			if(B == null)
			{
				Log.errOut("AutoAwards","Unknown ability/behav id "+propID+" in "+s);
				return false;
			}
			else
				fprops.add(B.ID(),arg);
		}
		else
			fprops.add(A.ID(),arg);
		return true;
	}

	protected Vector<AutoProperties> getAllAutoAwards()
	{
		Vector<AutoProperties> astro;
		synchronized(this)
		{
			astro= autoProperties;
		}
		if(astro == null)
		{
			synchronized(CMClass.getSync("AUTO_"+getAutoPropsFilename()))
			{
				astro = autoProperties;
				if(astro != null)
					return astro;
				astro = new Vector<AutoProperties>();
				final List<String> lines = Resources.getFileLineVector(new CMFile(Resources.makeFileResourceName(getAutoPropsFilename()),null).text());
				for(String s : lines)
				{
					s=s.trim();
					if(s.startsWith("#"))
						continue;
					final int x1 = s.indexOf("::");
					if(x1<0)
						continue;
					final int x2 = s.indexOf("::",x1+2);
					if(x2<0)
						continue;
					final String pmask = s.substring(0,x1).trim();
					final String dmask = s.substring(x1+2, x2).trim();
					final String propStr = s.substring(x2+2).trim();
					int state=0;
					final PairList<String,String> fprops = new PairVector<String,String>();
					String propID="";
					final StringBuilder str=new StringBuilder("");
					for(int i=0;i<propStr.length();i++)
					{
						final char c=propStr.charAt(i);
						switch(state)
						{
						case 0: // between things
							if((c=='(')&&(propID.length()>0))
								state=2;
							else
							if(!Character.isWhitespace(c))
							{
								if(propID.length()>0)
								{
									if(!addProp(fprops, propID, "", s))
									{
										i=propStr.length();
										propID="";
										break;
									}
									propID="";
									str.setLength(0);
								}
								str.append(c);
								state=1;
							}
							break;
						case 1: // in-proper-id
							if(Character.isWhitespace(c))
							{
								propID=str.toString();
								state=0;
							}
							else
							if(c=='(')
							{
								propID=str.toString();
								str.setLength(0);
								state=2;
							}
							else
								str.append(c);
							break;
						case 2: // in arg paren
							if((c=='\\')&&(i<propStr.length()-1))
							{
								i++;
								str.append(propStr.charAt(i));
							}
							else
							if(c==')')
							{
								final String args=str.toString();
								str.setLength(0);
								if(!addProp(fprops, propID, args, s))
								{
									i=propStr.length();
									propID="";
									break;
								}
								propID="";
								state=0;
							}
							else
								str.append(c);
							break;
						}
					}
					if(propID.length()>0)
						addProp(fprops, propID, str.toString(), s);
					else
					if(str.length()>0)
						addProp(fprops, str.toString(),"", s);
					final AutoPropertiesImpl entry = new AutoPropertiesImpl(pmask,dmask,fprops);
					astro.add(entry);
				}
				autoProperties = astro;
				autoPropHash = null;
			}
		}
		return astro;
	}

	@Override
	public void giveAutoProperties(final MOB mob, final boolean reset)
	{
		if(CMSecurity.isDisabled(CMSecurity.DisFlag.AUTOAWARDS))
			return;
		if((!mob.isPlayer())&&(CMSecurity.isDisabled(CMSecurity.DisFlag.NPCAUTOAWARDS)))
			return;
		Ability A=mob.fetchEffect("AutoAwards");
		if(A==null)
		{
			A=CMClass.getAbility("AutoAwards");
			if(A!=null)
			{
				mob.addNonUninvokableEffect(A);
				A.setSavable(false);
			}
		}
		else
		if(reset)
			A.setStat("RESET","true");
	}

	@Override
	public boolean modifyAutoProperty(final int lineNum, final String newLine)
	{
		final int num = lineNum;
		if(num<1)
			return false;
		final StringBuffer buf = new StringBuffer("");
		final CMFile F = new CMFile(Resources.makeFileResourceName(getAutoPropsFilename()),null);
		final List<String> lines=Resources.getFileLineVector(F.text());
		boolean found=false;
		int i=1;
		for(String l : lines)
		{
			l=l.trim();
			if(l.startsWith("#") || (l.length()==0))
			{
				buf.append(l).append("\n\r");
				continue;
			}
			if(i!=num)
				buf.append(l).append("\n\r");
			else
			{
				found=true;
				if(newLine != null)
					buf.append(newLine).append("\n\r");
			}
			i++;
		}
		if((num == Integer.MAX_VALUE)
		&&(newLine != null))
		{
			found=true;
			if(newLine != null)
				buf.append(newLine).append("\n\r");
		}
		if(!found)
			return false;
		else
		{
			autoPropHash	= null;
			autoProperties	= null;
			return F.saveText(buf);
		}
	}
}
