package com.planet_ink.coffee_mud.core.threads;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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



import java.util.*;
import com.planet_ink.coffee_mud.core.exceptions.*;

/* 
   Copyright 2000-2006 Bo Zimmerman

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
public class SaveThread extends Thread
{
	public static boolean started=false;
	private static boolean shutDown=false;
	public long lastStart=0;
	public long lastStop=0;
	public static long milliTotal=0;
	public static long tickTotal=0;
	public static String status="";

	public SaveThread()
	{
		super("SaveThread");
		setName("SaveThread");
        setDaemon(true);
	}

	public void titleSweep()
	{
		status="title sweeping";
		try
		{
			for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				LandTitle T=CMLib.utensils().getLandTitle(R);
				if(T!=null)
				{
					status="updating title in "+R.roomID();
					T.updateLot();
					status="title sweeping";
				}
			}
	    }catch(NoSuchElementException nse){}
	}
    
    public void commandJournalSweep()
    {
        status="command journal sweeping";
        try
        {
            for(int j=0;j<CMLib.journals().getNumCommandJournals();j++)
            {
                String num=(String)CMLib.journals().getCommandJournalFlags(j).get("EXPIRE=");
                if((num!=null)&&(CMath.isNumber(num)))
                {
                    status="updating journal "+CMLib.journals().getCommandJournalName(j);
                    Vector items=CMLib.database().DBReadJournal("SYSTEM_"+CMLib.journals().getCommandJournalName(j)+"S");
                    if(items!=null)
                    for(int i=items.size()-1;i>=0;i--)
                    {
                        Vector entry=(Vector)items.elementAt(i);
                        long compdate=CMath.s_long((String)entry.elementAt(6));
                        compdate=compdate+Math.round(CMath.mul(TimeManager.MILI_DAY,CMath.s_double(num)));
                        if(System.currentTimeMillis()>compdate)
                        {
                            String from=(String)entry.elementAt(1);
                            String message=(String)entry.elementAt(5);
                            Log.sysOut("SaveThread","Expired "+CMLib.journals().getCommandJournalName(j)+" from "+from+": "+message);
                            CMLib.database().DBDeleteJournal("SYSTEM_"+CMLib.journals().getCommandJournalName(j)+"S",i);
                        }
                    }
                    status="command journal sweeping";
                }
            }
        }catch(NoSuchElementException nse){}
    }
    
	public void shutdown()
	{
		shutDown=true;
		CMLib.killThread(this,500,30);
	}

	public int savePlayers()
	{
		int processed=0;
		for(Enumeration p=CMLib.map().players();p.hasMoreElements();)
		{
			MOB mob=(MOB)p.nextElement();
			if(!mob.isMonster())
			{
				status="just saving "+mob.Name();
				CMLib.database().DBUpdatePlayerStatsOnly(mob);
				if((mob.Name().length()==0)||(mob.playerStats()==null))
					continue;
				status="saving "+mob.Name()+", "+mob.inventorySize()+"items";
                CMLib.database().DBUpdatePlayerItems(mob);
				status="saving "+mob.Name()+", "+mob.numLearnedAbilities()+"abilities";
                CMLib.database().DBUpdatePlayerAbilities(mob);
				status="saving "+mob.numFollowers()+" followers of "+mob.Name();
                CMLib.database().DBUpdateFollowers(mob);
				mob.playerStats().setUpdated(System.currentTimeMillis());
				processed++;
			}
			else
			if((mob.playerStats()!=null)
			&&((mob.playerStats().lastUpdated()==0)
			   ||(mob.playerStats().lastUpdated()<mob.playerStats().lastDateTime())))
			{
				status="just saving "+mob.Name();
                CMLib.database().DBUpdatePlayerStatsOnly(mob);
				if((mob.Name().length()==0)||(mob.playerStats()==null))
					continue;
				status="just saving "+mob.Name()+", "+mob.inventorySize()+" items";
                CMLib.database().DBUpdatePlayerItems(mob);
				status="just saving "+mob.Name()+", "+mob.numLearnedAbilities()+" abilities";
                CMLib.database().DBUpdatePlayerAbilities(mob);
				mob.playerStats().setUpdated(System.currentTimeMillis());
				processed++;
			}
		}
		return processed;
	}

	public boolean autoPurge()
	{
		long[] levels=new long[2001];
		long[] prePurgeLevels=new long[2001];
		for(int i=0;i<levels.length;i++) levels[i]=0;
		for(int i=0;i<prePurgeLevels.length;i++) prePurgeLevels[i]=0;
		String mask=CMProps.getVar(CMProps.SYSTEM_AUTOPURGE);
		Vector maskV=CMParms.parseCommas(mask.trim(),false);
		long purgePoint=0;
		for(int mv=0;mv<maskV.size();mv++)
		{
			Vector V=CMParms.parse(((String)maskV.elementAt(mv)).trim());
			if(V.size()<2) continue;
			long val=CMath.s_long((String)V.elementAt(1));
			if(val<=0) continue;
			long prepurge=0;
			if(V.size()>2)
			    prepurge=CMath.s_long((String)V.elementAt(2));
			String cond=((String)V.firstElement()).trim();
			int start=0;
			int finish=levels.length-1;
			if(cond.startsWith("<="))
				finish=CMath.s_int(cond.substring(2).trim());
			else
			if(cond.startsWith(">="))
				start=CMath.s_int(cond.substring(2).trim());
			else
			if(cond.startsWith("=="))
			{
				start=CMath.s_int(cond.substring(2).trim());
				finish=start;
			}
			else
			if(cond.startsWith("="))
			{
				start=CMath.s_int(cond.substring(1).trim());
				finish=start;
			}
			else
			if(cond.startsWith(">"))
				start=CMath.s_int(cond.substring(1).trim())+1;
			else
			if(cond.startsWith("<"))
				finish=CMath.s_int(cond.substring(1).trim())-1;

			if((start>=0)&&(finish<levels.length)&&(start<=finish))
			{
				long realVal=System.currentTimeMillis()-(val*TimeManager.MILI_DAY);
				purgePoint=realVal+(prepurge*TimeManager.MILI_DAY);
				for(int s=start;s<=finish;s++)
				{
					if(levels[s]==0) levels[s]=realVal;
					if(prePurgeLevels[s]==0) prePurgeLevels[s]=purgePoint;
				}
			}
		}
		status="autopurge process";
		Vector allUsers=CMLib.database().getUserList();
		Vector protectedOnes=Resources.getFileLineVector(Resources.getFileResource("protectedplayers.ini",false));
		if(protectedOnes==null) protectedOnes=new Vector();

		for(int u=0;u<allUsers.size();u++)
		{
			Vector user=(Vector)allUsers.elementAt(u);
			String name=(String)user.elementAt(0);
			int level=CMath.s_int((String)user.elementAt(3));
			long last=CMath.s_long((String)user.elementAt(5));
			long when=Long.MAX_VALUE;
			long warn=Long.MAX_VALUE;
			if(level>levels.length) 
            {
				when=levels[levels.length-1];
				warn=prePurgeLevels[prePurgeLevels.length-1];
			}
			else
			if(level>=0) 
            {
				when=levels[level];
				warn=prePurgeLevels[level];
			}
			else
				continue;
            if(CMSecurity.isDebugging("AUTOPURGE"))
                Log.debugOut("SaveThread",name+" last on "+CMLib.time().date2String(last)+" will be warned on "+CMLib.time().date2String(warn)+" and purged on "+CMLib.time().date2String(when));
	        if((last>when)&&(last<warn))
			{
				boolean protectedOne=false;
				for(int p=0;p<protectedOnes.size();p++)
				{
					String P=(String)protectedOnes.elementAt(p);
					if(P.equalsIgnoreCase(name))
					{
						protectedOne=true;
						break;
					}
				}
				if(!protectedOne)
				{
					Vector warnedOnes=Resources.getFileLineVector(Resources.getFileResource("warnedplayers.ini",false));
					long foundWarning=-1;
					StringBuffer warnStr=new StringBuffer("");
					if((warnedOnes!=null)&&(warnedOnes.size()>0))
						for(int b=0;b<warnedOnes.size();b++)
						{
							String B=((String)warnedOnes.elementAt(b)).trim();
							if(B.trim().length()>0)
							{
								if(B.toUpperCase().startsWith(name.toUpperCase()+" "))
								{
									int lastSpace=B.lastIndexOf(" ");
									foundWarning=CMath.s_long(B.substring(lastSpace+1).trim());
								}
								warnStr.append(B+"\n");
							}
						}
					if((foundWarning<0)||(foundWarning<when))
					{
						MOB M=CMLib.map().getLoadPlayer(name);
						if((M!=null)&&(M.playerStats()!=null))
						{
							warnStr.append(M.name()+" "+M.playerStats().getEmail()+" "+System.currentTimeMillis()+"\n");
							Resources.updateResource("warnedplayers.ini",warnStr);
							Resources.saveFileResource("warnedplayers.ini");
                            if(CMSecurity.isDebugging("AUTOPURGE"))
                                Log.debugOut("SaveThread",name+" is now warned.");
							warnPrePurge(M,warn-when);
						}
					}
                    else
                    if(CMSecurity.isDebugging("AUTOPURGE"))
                        Log.debugOut("SaveThread",name+" has already been warned on "+CMLib.time().date2String(foundWarning));
				}
                else
                if(CMSecurity.isDebugging("AUTOPURGE"))
                    Log.debugOut("SaveThread",name+" is protected from purge warnings.");
			}

			if(last<when)
			{
                boolean protectedOne=false;
                for(int p=0;p<protectedOnes.size();p++)
                {
                    String P=(String)protectedOnes.elementAt(p);
                    if(P.equalsIgnoreCase(name))
                    { protectedOne=true; break; }
                }
				if(!protectedOne)
				{
					MOB M=CMLib.map().getLoadPlayer(name);
					if(M!=null)
					{
						CMLib.map().obliteratePlayer(M,true);
						Log.sysOut("SaveThread","AutoPurged user "+name+". Last logged in "+(CMLib.time().date2String(last))+".");
					}
				}
                else
                if(CMSecurity.isDebugging("AUTOPURGE"))
                    Log.debugOut("SaveThread",name+" is protected from purging.");
			}
		}
		return true;
	}

	private void warnPrePurge(MOB mob, long timeLeft)
	{
		// check for valid recipient
		if(mob==null) return;

		if((mob.playerStats()==null)
		||(mob.playerStats().getEmail().length()==0)) // no email addy to forward TO
			return;

		//  timeLeft is in millis
		String from="AutoPurgeWarning";
		String to=mob.Name();
		String subj=CMProps.SYSTEM_MUDNAME+" Autopurge Warning: "+to;
		String textTimeLeft="";
		if(timeLeft>(1000*60*60*24*2))
		{
			int days=new Double(CMath.div((double)timeLeft,1000*60*60*24)).intValue();
			textTimeLeft = days + " days";
		}
		else
		{
			int hours=new Double(CMath.div((double)timeLeft,1000*60*60)).intValue();
			textTimeLeft = hours + " hours";
		}
		String msg="Your character, "+to+", is going to be autopurged by the system in "+textTimeLeft+".  If you would like to keep this character active, please re-login.  This is an automated message, please do not reply.";

		SMTPLibrary.SMTPClient SC=null;
		try
		{
		    if(CMProps.getVar(CMProps.SYSTEM_SMTPSERVERNAME).length()>0)
				SC=CMLib.smtp().getClient(CMProps.getVar(CMProps.SYSTEM_SMTPSERVERNAME),SMTPLibrary.DEFAULT_PORT);
		    else
				SC=CMLib.smtp().getClient(mob.playerStats().getEmail());
		}
		catch(BadEmailAddressException be)
		{
			Log.errOut("SaveThread","Unable to notify "+to+" of impending autopurge.  Invalid email address.");
			return;
		}
		catch(java.io.IOException ioe)
		{
			return;
		}

		String replyTo="AutoPurge";
		String domain=CMProps.getVar(CMProps.SYSTEM_MUDDOMAIN).toLowerCase();
		try
		{
			SC.sendMessage(from+"@"+domain,
						   replyTo+"@"+domain,
						   mob.playerStats().getEmail(),
						   mob.playerStats().getEmail(),
						   subj,
						   CMLib.coffeeFilter().simpleOutFilter(msg));
		}
		catch(java.io.IOException ioe)
		{
			Log.errOut("SaveThread","Unable to notify "+to+" of impending autopurge.");
		}
	}

	public void run()
	{
		lastStart=System.currentTimeMillis();
		if(started)
		{
            Log.errOut("SaveThread","DUPLICATE SAVETHREAD RUNNING!!");
			return;
		}
		started=true;
        shutDown=false;

		while(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
			try{Thread.sleep(1000);}catch(Exception e){}
		while(true)
		{
			try
			{
                while(CMLib.threads().isAllSuspended())
                    try{Thread.sleep(2000);}catch(Exception e){}
				if(!CMSecurity.isDisabled("SAVETHREAD"))
				{
					status="checking database health";
					String ok=CMLib.database().errorStatus();
					if((ok.length()!=0)&&(!ok.startsWith("OK")))
						Log.errOut("Save Thread","DB: "+ok);
					else
					{
						titleSweep();
                        commandJournalSweep();
						autoPurge();
						CMLib.coffeeTables().bump(null,CoffeeTableRow.STAT_SPECIAL_NUMONLINE);
						CMLib.coffeeTables().update();
						lastStop=System.currentTimeMillis();
						milliTotal+=(lastStop-lastStart);
						tickTotal++;
						status="sleeping";
						Thread.sleep(MudHost.TIME_SAVETHREAD_SLEEP);
						lastStart=System.currentTimeMillis();
						if(!CMSecurity.isSaveFlag("NOPLAYERS"))
							savePlayers();
						status="not saving players";
						//if(processed>0)
						//	Log.sysOut("SaveThread","Saved "+processed+" mobs.");
					}
				}
				else
				{
					status="sleeping";
					Thread.sleep(MudHost.TIME_SAVETHREAD_SLEEP);
				}
			}
			catch(InterruptedException ioe)
			{
				Log.sysOut("SaveThread","Interrupted!");
				if(shutDown)
				{
					shutDown=false;
					started=false;
					break;
				}
			}
			catch(Exception e)
			{
				Log.errOut("SaveThread",e);
			}
		}

		Log.sysOut("SaveThread","Shutdown complete.");
	}
}
