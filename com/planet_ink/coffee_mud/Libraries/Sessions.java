package com.planet_ink.coffee_mud.Libraries;
import java.util.Enumeration;
import java.util.Vector;
import com.planet_ink.coffee_mud.Common.interfaces.Session;
import com.planet_ink.coffee_mud.Libraries.interfaces.SessionsList;
import com.planet_ink.coffee_mud.Libraries.interfaces.ThreadEngine;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMSecurity;
import com.planet_ink.coffee_mud.core.DVector;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.interfaces.MudHost;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Sessions extends StdLibrary implements SessionsList
{
    public String ID(){return "Sessions";}
    private ThreadEngine.SupportThread thread=null;
    public Vector all=new Vector();
    
    public ThreadEngine.SupportThread getSupportThread() { return thread;}
    
	public Session elementAt(int x)
	{
		return (Session)all.elementAt(x);
	}
	public int size()
	{
		return all.size();
	}
	public void addElement(Session S)
	{
		all.addElement(S);
	}
	public void removeElementAt(int x)
	{
		all.removeElementAt(x);
	}
	public void removeElement(Session S)
	{
		all.removeElement(S);
	}
    public Enumeration sessions() { return ((Vector)all.clone()).elements();}
    
    public void stopSessionAtAllCosts(Session S)
    {
        if(S==null) return;
        S.kill(true,true,false);
        try{Thread.sleep(1000);}catch(Exception e){}
        int tries=100;
        while((S.getStatus()!=Session.STATUS_LOGOUTFINAL)
        &&((--tries)>=0))
        {
            S.kill(true,true,true);
            try{Thread.sleep(100);}catch(Exception e){}
        }
        removeElement(S);
    }
    public boolean activate() {
        if(thread==null)
            thread=new ThreadEngine.SupportThread("THSessions"+Thread.currentThread().getThreadGroup().getName().charAt(0), 
                    MudHost.TIME_UTILTHREAD_SLEEP, this, CMSecurity.isDebugging("UTILITHREAD"));
        if(!thread.started)
            thread.start();
        return true;
    }
    
    public boolean shutdown() {
        thread.shutdown();
        return true;
    }
    
    public Session findPlayerOnline(String srchStr, boolean exactOnly)
    {
        // then look for players
		for(int s=0;s<size();s++)
		{
			Session thisSession=elementAt(s);
			if((thisSession.mob()!=null) && (!thisSession.killFlag())
			&&(thisSession.mob().location()!=null)
			&&(thisSession.mob().name().equalsIgnoreCase(srchStr)))
				return thisSession;
		}
		// keep looking for players
		if(!exactOnly)
			for(int s=0;s<size();s++)
			{
				Session thisSession=elementAt(s);
				if((thisSession.mob()!=null)&&(!thisSession.killFlag())
				&&(thisSession.mob().location()!=null)
				&&(CMLib.english().containsString(thisSession.mob().name(),srchStr)))
					return thisSession;
			}
		return null;
    }
    
    public void run()
    {
        if((CMSecurity.isDisabled("UTILITHREAD"))
        ||(CMSecurity.isDisabled("SESSIONTHREAD")))
            return;
        thread.status("checking player sessions.");
        for(int s=size()-1;s>=0;s--)
        {
            Session S=elementAt(s);
            if(S==null) continue;
            long time=System.currentTimeMillis()-S.lastLoopTime();
            if(time>0)
            {
                if((S.mob()!=null)||(S.getStatus()==Session.STATUS_ACCOUNTMENU))
                {
                    long check=60000;

                    if((S.previousCMD()!=null)
                    &&(S.previousCMD().size()>0)
                    &&(((String)S.previousCMD().firstElement()).equalsIgnoreCase("IMPORT")
                       ||((String)S.previousCMD().firstElement()).equalsIgnoreCase("EXPORT")
                       ||((String)S.previousCMD().firstElement()).equalsIgnoreCase("CHARGEN")
                       ||((String)S.previousCMD().firstElement()).equalsIgnoreCase("MERGE")))
                        check=check*600;
                    else
                    if((S.mob()!=null)&&(CMSecurity.isAllowed(S.mob(),S.mob().location(),"CMDROOMS")))
                        check=check*15;
                    else
                    if(S.getStatus()==Session.STATUS_LOGIN)
                        check=check*5;

                    if(time>(check*10))
                    {
                        String roomID=S.mob()!=null?CMLib.map().getExtendedRoomID(S.mob().location()):"";
                        if((S.previousCMD()==null)||(S.previousCMD().size()==0)||(S.getStatus()==Session.STATUS_LOGIN)||(S.getStatus()==Session.STATUS_ACCOUNTMENU))
                            Log.errOut(thread.getName(),"Kicking out: "+((S.mob()==null)?"Unknown":S.mob().Name())+" who has spent "+time+" millis out-game.");
                        else
                        {
                            Log.errOut(thread.getName(),"KILLING DEAD Session: "+((S.mob()==null)?"Unknown":S.mob().Name())+" ("+roomID+"), out for "+time);
                            Log.errOut(thread.getName(),"STATUS  was :"+S.getStatus()+", LASTCMD was :"+((S.previousCMD()!=null)?S.previousCMD().toString():""));
                            if(S instanceof Thread)
                                thread.debugDumpStack((Thread)S);
                        }
                        thread.status("killing session ");
                        stopSessionAtAllCosts(S);
                        thread.status("checking player sessions.");
                    }
                    else
                    if(time>check)
                    {
                        if((S.mob()==null)||(S.mob().Name()==null)||(S.mob().Name().length()==0))
                            stopSessionAtAllCosts(S);
                        else
                        if((S.previousCMD()!=null)&&(S.previousCMD().size()>0))
                        {
                            String roomID=S.mob()!=null?CMLib.map().getExtendedRoomID(S.mob().location()):"";
                            if((S.isLockedUpWriting())
                            &&(CMLib.flags().isInTheGame(S.mob(),true)))
                            {
                                Log.errOut(thread.getName(),"LOGGED OFF Session: "+((S.mob()==null)?"Unknown":S.mob().Name())+" ("+roomID+"), out for "+time+": "+S.isLockedUpWriting());
                                stopSessionAtAllCosts(S);
                            }
                            else
                                Log.errOut(thread.getName(),"Suspect Session: "+((S.mob()==null)?"Unknown":S.mob().Name())+" ("+roomID+"), out for "+time);
                            if((S.getStatus()!=1)||((S.previousCMD()!=null)&&(S.previousCMD().size()>0)))
                                Log.errOut(thread.getName(),"STATUS  is :"+S.getStatus()+", LASTCMD was :"+((S.previousCMD()!=null)?S.previousCMD().toString():""));
                            else
                                Log.errOut(thread.getName(),"STATUS  is :"+S.getStatus()+", no last command available.");
                        }
                    }
                }
                else
                if(time>(60000))
                {
                    String roomID=S.mob()!=null?CMLib.map().getExtendedRoomID(S.mob().location()):"";
                    if(S.getStatus()==Session.STATUS_LOGIN)
                        Log.errOut(thread.getName(),"Kicking out login session after "+time+" millis.");
                    else
                    {
	                    Log.errOut(thread.getName(),"KILLING DEAD Session: "+((S.mob()==null)?"Unknown":S.mob().Name())+" ("+roomID+"), out for "+time);
	                    if(S instanceof Thread)
	                        thread.debugDumpStack((Thread)S);
                    }
                    if((S.getStatus()!=1)||((S.previousCMD()!=null)&&(S.previousCMD().size()>0)))
                    	Log.errOut(thread.getName(),"STATUS  was :"+S.getStatus()+", LASTCMD was :"+((S.previousCMD()!=null)?S.previousCMD().toString():""));
                    thread.status("killing session ");
                    stopSessionAtAllCosts(S);
                    thread.status("checking player sessions");
                }
            }
        }
        
    }
}
