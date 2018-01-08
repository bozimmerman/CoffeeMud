package com.planet_ink.coffee_mud.Libraries;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.planet_ink.coffee_mud.Areas.interfaces.Area;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.CatalogLibrary.CataData;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.interfaces.*;

/*
   Copyright 2001-2018 Bo Zimmerman

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
public class Sessions extends StdLibrary implements SessionsList
{
	@Override
	public String ID()
	{
		return "Sessions";
	}

	private volatile long nextSweepTime = System.currentTimeMillis();

	public final SLinkedList<Session> all=new SLinkedList<Session>();

	private final static Filterer<Session> localOnlineFilter=new Filterer<Session>()
	{
		@Override
		public boolean passesFilter(Session obj) {
			if((obj!=null) && (!obj.isStopped()) && (((obj.getStatus())==Session.SessionStatus.MAINLOOP)))
			{
				final MOB M=obj.mob();
				return ((M!=null)&&M.amActive()&&(CMLib.flags().isInTheGame(M,true)));
			}
			return false;
		}
	};

	@Override
	public Iterator<Session> all()
	{
		return all.iterator();
	}

	@Override
	public Iterable<Session> allIterable()
	{
		return all;
	}

	@Override
	public Iterator<Session> localOnline()
	{
		return new FilteredIterator<Session>(all.iterator(),localOnlineFilter);
	}

	@Override
	public Iterable<Session> localOnlineIterable()
	{
		return new FilteredIterable<Session>(all,localOnlineFilter);
	}

	@Override
	public int getCountAll()
	{
		return getCount(all());
	}

	@Override
	public int getCountLocalOnline()
	{
		return getCount(localOnline());
	}

	protected int getCount(Iterator<Session> i)
	{
		int xt=0;
		for(;i.hasNext();)
		{
			i.next();
			xt++;
		}
		return xt;
	}

	@Override
	public Session getAllSessionAt(int index)
	{
		return getAllSessionAt(all(),index);
	}

	protected Session getAllSessionAt(Iterator<Session> i, int index)
	{
		int xt=0;
		Session S;
		for(;i.hasNext();)
		{
			S=i.next();
			if(xt==index)
				return S;
			xt++;
		}
		return null;
	}

	@Override
	public synchronized void add(Session s)
	{
		if(!all.contains(s))
			all.add(s);
	}

	@Override
	public synchronized void remove(Session s)
	{
		all.remove(s);
	}

	@Override
	public synchronized boolean isSession(Session s)
	{
		return all.contains(s);
	}
	
	@Override
	public void stopSessionAtAllCosts(Session S)
	{
		if(S==null)
			return;
		S.stopSession(true,true,false);
		CMLib.s_sleep(100);
		if(all.contains(S))
		{
			CMLib.s_sleep(100);
			S.run();
			CMLib.s_sleep(100);
			S.stopSession(true,true,false);
			CMLib.s_sleep(100);
			if(all.contains(S))
			{
				S.stopSession(true,true,true);
				remove(S);
			}
		}
	}

	protected void sessionCheck()
	{
		setThreadStatus(serviceClient,"checking player sessions.");
		for(final Session S : all)
		{
			final long time=System.currentTimeMillis()-S.getInputLoopTime();
			if(time>0)
			{
				if((S.mob()!=null)||((S.getStatus())==Session.SessionStatus.ACCOUNT_MENU)||((S.getStatus())==Session.SessionStatus.CHARCREATE))
				{
					long check=60000;

					if((S.getPreviousCMD()!=null)
					&&(S.getPreviousCMD().size()>0)
					&&(S.getPreviousCMD().get(0).equalsIgnoreCase("IMPORT")
					   ||S.getPreviousCMD().get(0).equalsIgnoreCase("EXPORT")
					   ||S.getPreviousCMD().get(0).equalsIgnoreCase("CHARGEN")
					   ||S.getPreviousCMD().get(0).equalsIgnoreCase("MERGE")))
						check=check*600;
					else
					if((S.mob()!=null)&&(CMSecurity.isAllowed(S.mob(),S.mob().location(),CMSecurity.SecFlag.CMDROOMS)))
						check=check*15;
					else
					if((S.getStatus())==Session.SessionStatus.LOGIN)
						check=check*5;

					if(time>(check*10))
					{
						final String roomID=S.mob()!=null?CMLib.map().getExtendedRoomID(S.mob().location()):"";
						if((S.getPreviousCMD()==null)
						||(S.getPreviousCMD().size()==0)
						||(S.getStatus()==Session.SessionStatus.LOGIN)
						||(S.getStatus()==Session.SessionStatus.ACCOUNT_MENU)
						||(S.getStatus()==Session.SessionStatus.HANDSHAKE_MCCP)
						||(S.getStatus()==Session.SessionStatus.HANDSHAKE_OPEN)
						||(S.getStatus()==Session.SessionStatus.CHARCREATE))
							Log.sysOut(serviceClient.getName(),"Kicking out: "+((S.mob()==null)?"Unknown":S.mob().Name())+", idle: "+CMLib.time().date2EllapsedTime(time, TimeUnit.MILLISECONDS, true)+", status: "+S.getStatus());
						else
						{
							Log.errOut(serviceClient.getName(),"KILLING DEAD Session: "+((S.mob()==null)?"Unknown":S.mob().Name())+" ("+roomID+"), out for "+CMLib.time().date2EllapsedTime(time, TimeUnit.MILLISECONDS, true));
							Log.errOut(serviceClient.getName(),"STATUS  was :"+S.getStatus()+", LASTCMD was :"+((S.getPreviousCMD()!=null)?S.getPreviousCMD().toString():""));
							if(S instanceof Thread)
								CMLib.threads().debugDumpStack("Sessions",(Thread)S);
						}
						setThreadStatus(serviceClient,"killing session ");
						stopSessionAtAllCosts(S);
						setThreadStatus(serviceClient,"checking player sessions.");
					}
					else
					if(time>check)
					{
						if((S.mob()==null)||(S.mob().Name()==null)||(S.mob().Name().length()==0))
							stopSessionAtAllCosts(S);
						else
						if((S.getPreviousCMD()!=null)&&(S.getPreviousCMD().size()>0))
						{
							final String roomID=S.mob()!=null?CMLib.map().getExtendedRoomID(S.mob().location()):"";
							final String statusMsg;
							if(((S.getStatus())!=Session.SessionStatus.LOGIN)
							||((S.getPreviousCMD()!=null)&&(S.getPreviousCMD().size()>0)))
								statusMsg = "STATUS  is :"+S.getStatus()+", LASTCMD was :"+((S.getPreviousCMD()!=null)?S.getPreviousCMD().toString():"");
							else
								statusMsg = "STATUS  is :"+S.getStatus()+", no last command available.";
							if((S.isLockedUpWriting())
							&&(CMLib.flags().isInTheGame(S.mob(),true)))
							{
								Log.errOut(serviceClient.getName(),"LOGGED OFF Session: "+((S.mob()==null)?"Unknown":S.mob().Name())+" ("+roomID+"), out for "+CMLib.time().date2EllapsedTime(time, TimeUnit.MILLISECONDS, true)+": "+S.isLockedUpWriting()+"\r\n"+statusMsg);
								stopSessionAtAllCosts(S);
							}
							else
								Log.warnOut(serviceClient.getName(),"Suspect Session: "+((S.mob()==null)?"Unknown":S.mob().Name())+" ("+roomID+"), out for "+CMLib.time().date2EllapsedTime(time, TimeUnit.MILLISECONDS, true)+"\r\n"+statusMsg);
						}
					}
				}
				else
				if(time>(300000))
				{
					final String roomID=S.mob()!=null?CMLib.map().getExtendedRoomID(S.mob().location()):"";
					if((S.getStatus()==Session.SessionStatus.LOGIN)
					||(S.getStatus()==Session.SessionStatus.HANDSHAKE_MCCP)
					||(S.getStatus()==Session.SessionStatus.HANDSHAKE_OPEN))
						Log.sysOut(serviceClient.getName(),"Kicking out login session after "+CMLib.time().date2EllapsedTime(time, TimeUnit.MILLISECONDS, true)+".");
					else
					{
						Log.errOut(serviceClient.getName(),"KILLING DEAD Session: "+((S.mob()==null)?"Unknown":S.mob().Name())+" ("+roomID+"), out for "+CMLib.time().date2EllapsedTime(time, TimeUnit.MILLISECONDS, true));
						if(S instanceof Thread)
							CMLib.threads().debugDumpStack("Sessions",(Thread)S);
					}
					if(S.getStatus()!=Session.SessionStatus.HANDSHAKE_MCCP)
					{
						if(((S.getStatus())!=Session.SessionStatus.LOGIN)
						||((S.getPreviousCMD()!=null)&&(S.getPreviousCMD().size()>0)))
							Log.errOut(serviceClient.getName(),"STATUS was :"+S.getStatus()+", LASTCMD was :"+((S.getPreviousCMD()!=null)?S.getPreviousCMD().toString():""));
					}
					setThreadStatus(serviceClient,"killing session ");
					stopSessionAtAllCosts(S);
					setThreadStatus(serviceClient,"checking player sessions");
				}
			}
		}
	}

	@Override
	public boolean activate()
	{
		nextSweepTime = System.currentTimeMillis()+MudHost.TIME_UTILTHREAD_SLEEP;
		if(serviceClient==null)
		{
			name="THSessions"+Thread.currentThread().getThreadGroup().getName().charAt(0);
			serviceClient=CMLib.threads().startTickDown(this, Tickable.TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK, 100, 1);
		}
		return true;
	}

	@Override public boolean tick(Tickable ticking, int tickID)
	{
		tickStatus=Tickable.STATUS_ALIVE;
		try
		{
			final double numThreads=all.size();
			if(numThreads>0.0)
			{
				for(final Session S : all)
				{
					if(!S.isRunning())
					{
						CMLib.threads().executeRunnable(S.getGroupName(), S);
					}
				}
			}
			if(System.currentTimeMillis() >= nextSweepTime)
			{
				nextSweepTime = System.currentTimeMillis()+MudHost.TIME_UTILTHREAD_SLEEP;
				if((!CMSecurity.isDisabled(CMSecurity.DisFlag.UTILITHREAD))
				&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.SESSIONTHREAD)))
				{
					isDebugging=CMSecurity.isDebugging(DbgFlag.UTILITHREAD);
					sessionCheck();
				}
			}
		}
		finally
		{
			tickStatus=Tickable.STATUS_NOT;
			setThreadStatus(serviceClient,"sleeping");
		}
		return true;
	}

	@Override
	public boolean shutdown()
	{
		if(CMLib.threads().isTicking(this, TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK))
		{
			CMLib.threads().deleteTick(this, TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK);
			serviceClient=null;
		}
		return true;
	}

	@Override
	public MOB findPlayerOnline(String srchStr, boolean exactOnly)
	{
		final Session S=findPlayerSessionOnline(srchStr, exactOnly);
		if(S==null)
			return null;
		return S.mob();
	}

	@Override
	public Session findPlayerSessionOnline(String srchStr, boolean exactOnly)
	{
		// then look for players
		for(final Session S : localOnlineIterable())
		{
			if(S.mob().Name().equalsIgnoreCase(srchStr))
				return S;
		}
		for(final Session S : localOnlineIterable())
		{
			if(S.mob().name().equalsIgnoreCase(srchStr))
				return S;
		}
		// keep looking for players
		if(!exactOnly)
		{
			for(final Session S : localOnlineIterable())
			{
				if(CMLib.english().containsString(S.mob().Name(),srchStr))
					return S;
			}
			for(final Session S : localOnlineIterable())
				if(CMLib.english().containsString(S.mob().name(),srchStr))
					return S;
		}
		return null;
	}
	
	@Override
	public void moveSessionToCorrectThreadGroup(final Session session, int theme)
	{
		final int themeDex=CMath.firstBitSetIndex(theme);
		if((themeDex>=0)&&(themeDex<Area.THEME_BIT_NAMES.length))
		{
			final ThreadGroup privateGroup=CMProps.getPrivateOwner(Area.THEME_BIT_NAMES[themeDex]+"PLAYERS");
			if((privateGroup!=null)
			&&(privateGroup.getName().length()>0)
			&&(!privateGroup.getName().equals(session.getGroupName())))
			{
				if(session.getGroupName().length()>0)
				{
					if(CMLib.library(session.getGroupName().charAt(0), CMLib.Library.SESSIONS)
					!= CMLib.library(privateGroup.getName().charAt(0), CMLib.Library.SESSIONS))
					{
						((Sessions)CMLib.library(session.getGroupName().charAt(0), CMLib.Library.SESSIONS)).remove(session);
						((Sessions)CMLib.library(privateGroup.getName().charAt(0), CMLib.Library.SESSIONS)).add(session);
					}
				}
				session.setGroupName(privateGroup.getName());
			}
		}
	}

}
