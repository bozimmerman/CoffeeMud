package com.planet_ink.coffee_mud.core.database;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary.CMChannel;
import com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary.ChannelFlag;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/*
   Copyright 2014-2023 Bo Zimmerman

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
public class BackLogLoader
{
	protected DBConnector DB=null;
	protected Map<String,AtomicInteger> counters = new Hashtable<String,AtomicInteger>();

	public BackLogLoader(final DBConnector newDB)
	{
		DB=newDB;
	}

	protected int getCounter(final String channelName, final boolean bump)
	{
		AtomicInteger counter = counters.get(channelName);
		if(counter == null)
		{
			final Object sync = CMClass.getSync("BACKLOG_"+channelName);
			synchronized(sync)
			{
				counter = counters.get(channelName);
				if(counter == null)
				{
					DBConnection D=null;
					try
					{
						D=DB.DBFetch();
						ResultSet R=D.query("SELECT CMDATE FROM CMBKLG WHERE CMNAME='"+channelName+"' AND CMINDX = 0");
						if(R.next())
						{
							final int setCounter = (int)DBConnections.getLongRes(R, "CMDATE");
							int c=setCounter;
							R.close();
							final StringBuilder sql=new StringBuilder("SELECT CMINDX FROM CMBKLG WHERE CMNAME='"+channelName+"'");
							sql.append(" AND CMINDX >"+setCounter);
							R = D.query(sql.toString());
							while(R.next())
							{
								final int i = R.getInt(1);
								if(i>c)
									c=i;
							}
							R.close();
							if(c!=setCounter)
								D.update("UPDATE CMBKLG SET CMDATE="+c+" WHERE CMNAME='"+channelName+"' AND CMINDX=0", 0);
							counters.put(channelName, new AtomicInteger( c ));

						}
						else
						{
							R.close();
							D.update("INSERT INTO CMBKLG (CMNAME,  CMINDX, CMDATE) VALUES ('"+channelName+"', 0, 0)", 0);
							counters.put(channelName, new AtomicInteger (0));
						}
					}
					catch(final Exception sqle)
					{
						Log.errOut("BackLog",sqle);
					}
					finally
					{
						DB.DBDone(D);
					}
					counter = counters.get(channelName);
				}
			}
		}
		if(bump)
		{
			synchronized(counter)
			{
				final int c=counter.incrementAndGet();
				DBConnection D=null;
				try
				{
					D=DB.DBFetch();
					D.update("UPDATE CMBKLG SET CMDATE="+c+" WHERE CMNAME='"+channelName+"' AND CMINDX = 0", 0);
				}
				catch(final Exception sqle)
				{
					Log.errOut("BackLog",sqle);
				}
				finally
				{
					DB.DBDone(D);
				}
				return c;
			}
		}
		return counter.get();
	}

	protected Integer checkSetBacklogTableVersion(final Integer setVersion)
	{
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			Integer existingVersion = null;
			final ResultSet R=D.query("SELECT CMDATE FROM CMBKLG WHERE CMNAME='TABLE_VERSION' AND CMINDX = 0");
			if(R.next())
			{
				final Integer I= Integer.valueOf((int)R.getLong("CMDATE"));
				existingVersion=I;
			}
			R.close();
			if(setVersion == null)
				return existingVersion == null ? Integer.valueOf(0) : existingVersion;
			if(existingVersion == null)
				D.update("INSERT INTO CMBKLG (CMNAME,  CMINDX, CMDATE) VALUES ('TABLE_VERSION', 0, "+setVersion.intValue()+")", 0);
			else
				D.update("UPDATE CMBKLG SET CMDATE="+setVersion.intValue()+" WHERE CMNAME='TABLE_VERSION' AND CMINDX=0;", 0);
			return setVersion;
		}
		catch(final SQLException sqle)
		{
			Log.errOut(sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return setVersion;
	}

	protected void updateBackLogEntry(String channelName, final int index, final long date, final int subNameField, final String entry)
	{
		if((entry == null) || (channelName == null) || (entry.length()==0))
			return;
		channelName = channelName.toUpperCase().trim();
		DBConnection D=null;
		try
		{
			D=DB.DBFetchPrepared("UPDATE CMBKLG SET CMDATE="+date+", CMSNAM="+subNameField+", CMDATA=? WHERE CMNAME='"+channelName+"' AND CMINDX="+index);
			D.setPreparedClobs(new String[]{entry});
			try
			{
				D.update("",0);
			}
			catch(final Exception sqle)
			{
				Log.errOut("Fail: "+sqle.getMessage());
				DB.DBDone(D);
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("BackLog",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
	}

	public void addBackLogEntry(String channelName, final int subNameField, final String entry)
	{
		if((entry == null) || (channelName == null) || (entry.length()==0))
			return;
		channelName = channelName.toUpperCase().trim();
		final int counter = getCounter(channelName, true);
		DBConnection D=null;
		try
		{
			D=DB.DBFetchPrepared("INSERT INTO CMBKLG (CMNAME, CMSNAM, CMINDX, CMDATE, CMDATA) "
					+ "VALUES ('"+channelName+"', "+subNameField+", "+counter+", "+System.currentTimeMillis()+", ?)");
			D.setPreparedClobs(new String[]{entry});
			try
			{
				D.update("",0);
			}
			catch(final Exception sqle)
			{
				// retry for duplicate entries, but how could that even happen?!
				Log.errOut("Retry: "+sqle.getMessage());
				DB.DBDone(D);
				final int counter2 = getCounter(channelName, true);
				D=DB.DBFetchPrepared("INSERT INTO CMBKLG (CMNAME,  CMSNAM, CMINDX, CMDATE, CMDATA) "
						+ "VALUES ('"+channelName+"', "+subNameField+", "+counter2+", "+System.currentTimeMillis()+", ?)");
				D.setPreparedClobs(new String[]{entry});
				D.update("",0);
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("BackLog",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
	}

	public void delBackLogEntry(String channelName, final long timeStamp)
	{
		if(channelName == null)
			return;
		channelName = channelName.toUpperCase().trim();
		DBConnection D=DB.DBFetch();
		String[] updates = new String[0];
		try
		{
			try
			{
				D.update("DELETE FROM CMBKLG WHERE CMNAME='"+channelName+"' AND CMDATE="+timeStamp,0);
			}
			catch(final Exception sqle)
			{
				// retry for duplicate entries, but how could that even happen?!
				Log.errOut("Retry: "+sqle.getMessage());
				DB.DBDone(D);
				D=DB.DBFetch();
				D.update("DELETE FROM CMBKLG WHERE CMNAME='"+channelName+"' AND CMDATE="+timeStamp,0);
			}
			{
				final List<String> updateV = new ArrayList<String>();
				ResultSet R = D.query("SELECT CMDATE, CMINDX FROM CMBKLG WHERE CMNAME='"+channelName+"' AND CMDATE > "+timeStamp+" AND CMINDX>0");
				if(R!=null)
				{
					while(R.next())
					{
						final long ts = R.getLong("CMDATE");
						final int index = R.getInt("CMINDX");
						updateV.add("UPDATE CMBKLG SET CMINDX="+(index-1)+" WHERE CMNAME='"+channelName+"' AND CMINDX="+index+" AND CMDATE="+ts+";");
					}
					R.close();
				}
				R = D.query("SELECT CMDATE FROM CMBKLG WHERE CMNAME='"+channelName+"' AND CMINDX=0");
				if(R!=null)
				{
					if(R.next())
					{
						final long ts = R.getLong("CMDATE");
						updateV.add("UPDATE CMBKLG SET CMDATE="+(ts-1)+" WHERE CMNAME='"+channelName+"' AND CMINDX=0;");
					}
					R.close();
				}
				updates = updateV.toArray(updates);
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("BackLog",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		if((updates != null) && (updates.length>0))
		{
			try
			{
				DB.update(updates);
				synchronized(CMClass.getSync(("BACKLOG_"+channelName)))
				{
					counters.remove(channelName);
				}
			}
			catch(final Exception sqle)
			{
				Log.errOut("BackLog",sqle);
			}
		}
	}

	protected List<Quad<String,Integer,Long,Integer>> enumBackLogEntries(String channelName, final int firstIndex, final int numToReturn)
	{
		final List<Quad<String,Integer,Long,Integer>> list=new Vector<Quad<String,Integer,Long,Integer>>();
		if(channelName == null)
			return list;
		channelName = channelName.toUpperCase().trim();
		final StringBuilder sql=new StringBuilder("SELECT * FROM CMBKLG WHERE CMNAME='"+channelName+"'");
		sql.append(" AND CMINDX >="+firstIndex);
		sql.append(" ORDER BY CMINDX");
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			final ResultSet R = D.query(sql.toString());
			while((R.next())&&(list.size()<numToReturn))
			{
				list.add(new Quad<String,Integer,Long,Integer>(
						DB.getRes(R, "CMDATA"),
						Integer.valueOf((int)DB.getLongRes(R,"CMINDX")),
						Long.valueOf(DB.getLongRes(R, "CMDATE")),
						Integer.valueOf((int)DB.getLongRes(R, "CMSNAM"))));
			}
			R.close();
		}
		catch(final Exception sqle)
		{
			Log.errOut("BackLog",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return list;

	}

	public int getBackLogPageEnd(String channelName, final int subNameField)
	{
		if(channelName == null)
			return -1;
		channelName = channelName.toUpperCase().trim();
		return getCounter(channelName, true);
	}

	public List<Triad<String,Integer,Long>> getBackLogEntries(String channelName, final int subNameField, final int newestToSkip, final int numToReturn)
	{
		final List<Triad<String,Integer,Long>> list=new Vector<Triad<String,Integer,Long>>();
		if(channelName == null)
			return list;
		channelName = channelName.toUpperCase().trim();
		final int counter = getCounter(channelName, false);
		DBConnection D=null;
		try
		{
			final int number = numToReturn + newestToSkip;
			final int oldest = number >= counter ? 1 : (counter - number + 1);
			final int newest = newestToSkip >= counter ? counter : (counter - newestToSkip);
			D=DB.DBFetch();
			final StringBuilder sql=new StringBuilder("SELECT CMDATA,CMINDX,CMDATE FROM CMBKLG WHERE CMNAME='"+channelName+"'");
			sql.append(" AND CMINDX >="+oldest);
			sql.append(" AND CMINDX <="+newest);
			if(subNameField != 0)
				sql.append(" AND CMSNAM = "+subNameField);
			sql.append(" ORDER BY CMINDX");
			final ResultSet R = D.query(sql.toString());
			while((R.next())&&(list.size()<numToReturn))
				list.add(new Triad<String,Integer,Long>(DB.getRes(R, "CMDATA"),Integer.valueOf((int)DB.getLongRes(R,"CMINDX")),Long.valueOf(DB.getLongRes(R, "CMDATE"))));
			R.close();
		}
		catch(final Exception sqle)
		{
			Log.errOut("BackLog",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return list;
	}

	public void trimBackLogEntries(final String[] channels, final int maxMessages, final long oldestTime)
	{
		for(final String channelName : channels)
		{
			final int counter = getCounter(channelName, false);
			DBConnection D=null;
			try
			{
				D=DB.DBFetch();
				if((maxMessages == 0) && (D != null))
				{
					D.update("DELETE FROM CMBKLG WHERE CMNAME='"+channelName+"'",0);
				}
				else
				if((maxMessages < counter) && (D != null))
				{
					final int oldestCounter = counter - maxMessages;
					D.update("DELETE FROM CMBKLG WHERE CMNAME='"+channelName+"' AND CMINDX != 0 AND CMINDX < "+oldestCounter,0);
				}
				if((oldestTime > 0) && (D != null))
				{
					D.update("DELETE FROM CMBKLG WHERE CMNAME='"+channelName+"' AND CMINDX != 0 AND CMDATE < "+oldestTime,0);
				}
			}
			catch(final Exception sqle)
			{
				Log.errOut("BackLog",sqle);
			}
			finally
			{
				DB.DBDone(D);
			}
		}
	}

	public void checkUpgradeBacklogTable(final ChannelsLibrary channels)
	{
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.CHANNELBACKLOGS))
		{
			final Integer tableVer = checkSetBacklogTableVersion(null);
			if((tableVer == null) || (tableVer.intValue() < 1))
			{
				CMLib.threads().scheduleRunnable(new Runnable()
				{
					@Override
					public void run()
					{
						DBConnection D=null;
						try
						{
							D=DB.DBFetch();
							D.update("UPDATE CMBKLG SET CMSNAM=0;", 0);
						}
						catch(final Exception sqle)
						{
							Log.errOut("BackLog",sqle);
						}
						finally
						{
							DB.DBDone(D);
						}
						final List<CMChannel> chansToDo = new LinkedList<CMChannel>();
						for(int f = 0; f<channels.getNumChannels(); f++)
						{
							final CMChannel chan = channels.getChannel(f);
							if((chan.flags().contains(ChannelFlag.CLANALLYONLY))
							||(chan.flags().contains(ChannelFlag.CLANONLY)))
							//||(chan.flags().contains(ChannelFlag.SAMEAREA))) // can't do anything about this
							{
								if(!chan.flags().contains(ChannelsLibrary.ChannelFlag.NOBACKLOG))
									chansToDo.add(chan);
							}
						}
						if(chansToDo.size()>0)
						{
							Log.sysOut("Processing backlog clan table upgrades...");
							final Map<String, Boolean> isPlayerCache=new TreeMap<String, Boolean>();
							final Map<String, MOB> playerCache=new TreeMap<String, MOB>();
							int amountDone = 0;
							int amountSkipped = 0;
							for(final CMChannel chan : chansToDo)
							{
								int firstIndex=1;
								boolean done=false;
								while(!done)
								{
									final List<Quad<String,Integer,Long,Integer>> msgs = enumBackLogEntries(chan.name(), firstIndex, 50);
									if(msgs.size()==0)
										break;
									for(final Quad<String,Integer,Long,Integer> m : msgs)
									{
										int subNameField=0;
										if(m.fourth.intValue()==0)
										{
											final CMMsg msg=CMClass.getMsg();
											msg.parseFlatString(m.first);
											if((msg.source().Name().length()>0)
											&&(!Character.isLetter(msg.source().Name().charAt(0)))
											&&(msg.othersMessage()!=null))
											{
												final int y=msg.othersMessage().indexOf(" has logged o");
												if(y>0)
												{
													final int x=msg.othersMessage().indexOf("] '");
													if((x>0)&&(x<y))
													{
														final String name=msg.othersMessage().substring(x+3,y).trim();
														if((name.length()>0)
														&&(Character.isLetter(name.charAt(0)))
														&&(Character.isUpperCase(name.charAt(0))))
															msg.source().setName(name);
													}
												}
											}
											final String srcName=msg.source().name();
											if(!isPlayerCache.containsKey(srcName))
											{
												final boolean isPlayer = CMLib.players().playerExists(srcName);
												isPlayerCache.put(srcName, Boolean.valueOf(isPlayer));
											}
											if(isPlayerCache.get(srcName).booleanValue())
											{
												if(!playerCache.containsKey(srcName))
												{
													for(final Pair<String, Integer> c : CMLib.database().DBReadPlayerClans(srcName))
														msg.source().setClan(c.first, c.second.intValue());
													playerCache.put(srcName, msg.source());
												}
												else
													msg.source().destroy();
												msg.setSource(playerCache.get(srcName));
											}
											if(!msg.source().clans().iterator().hasNext())
											{
												for(final Enumeration<Clan> c=CMLib.clans().clans();c.hasMoreElements();)
												{
													final Clan C=c.nextElement();
													final String msgStr=msg.othersMessage();
													if((msgStr!=null)&&(msgStr.indexOf(C.name())>=0))
													{
														msg.source().setClan(C.clanID(), C.getTopRankedRoles(Clan.Function.CHANNEL).get(0).intValue());
														break;
													}
												}
											}
											final List<Pair<Clan,Integer>> allClans=new ArrayList<Pair<Clan,Integer>>();
											allClans.addAll(CMLib.clans().findPrivilegedClans(msg.source(), Clan.Function.CHANNEL));
											Collections.sort(allClans,Clan.compareByRole);
											if(allClans.size()>0)
												subNameField=allClans.get(0).first.name().toUpperCase().hashCode();
											if(subNameField != 0)
											{
												amountDone++;
												updateBackLogEntry(chan.name(), m.second.intValue(), m.third.longValue(), subNameField, m.first);
											}
											else
												amountSkipped++;
											if(!playerCache.containsKey(srcName))
												msg.source().destroy();
											if(msg.target()!=null)
												msg.target().destroy();
											if(msg.tool()!=null)
												msg.tool().destroy();
										}
										firstIndex = m.second.intValue()+1;
									}
									done = msgs.size()  < 50;
								}
							}
							Log.sysOut("Backlog clan table upgrades completed. "+amountDone+"/"+(amountDone+amountSkipped)+" messages altered in "+chansToDo.size()+" channels.");
						}
						checkSetBacklogTableVersion(Integer.valueOf(1));
					}
				}, 500);
			}
		}
	}
}
