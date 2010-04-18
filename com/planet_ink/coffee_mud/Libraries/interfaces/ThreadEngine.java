package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.threads.Tick;
import com.planet_ink.coffee_mud.core.*;
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
public interface ThreadEngine extends CMLibrary, Runnable
{
	// tick related
	public void startTickDown(Tickable E,
							  int tickID,
                              long TICK_TIME,
							  int numTicks);
    public void startTickDown(Tickable E,
                              int tickID,
                              int numTicks);
	public boolean deleteTick(Tickable E, int tickID);
	public void suspendTicking(Tickable E, int tickID);
	public void resumeTicking(Tickable E, int tickID);
	public boolean isSuspended(Tickable E, int tickID);
    public void suspendAll();
    public void resumeAll();
    public boolean isAllSuspended();
	public void clearDebri(Room room, int taskCode);
	public String tickInfo(String which);
	public void tickAllTickers(Room here);
    public void rejuv(Room here, int tickID);
	public String systemReport(String itemCode);
	public boolean isTicking(Tickable E, int tickID);
	public  Iterator<Tick> tickGroups();
    public String getTickStatusSummary(Tickable obj);
    public String getServiceThreadSummary(Thread T);
    public Vector getNamedTickingObjects(String name);
    
    public class SupportThread extends Thread {
        public boolean started=false;
        public boolean shutDown=false;
        public long lastStart=0;
        public long lastStop=0;
        public long milliTotal=0;
        public long tickTotal=0;
        public String status="";
        public boolean debugging=false;
        public long sleepTime=0;
        public Runnable engine=null;
        
        public SupportThread(String name, long sleep, Runnable engine, boolean debugging) {
            this.engine=engine;
            sleepTime=sleep;
            this.debugging=debugging;
            setName(name);
            setDaemon(true);
        }
        
        public void status(String s)
        {
            status=s;
            if(debugging) Log.debugOut(getName(),s);
        }
        
        public boolean shutdown() {
            shutDown=true;
            CMLib.killThread(this,500,30);
            return true;
        }
        
        public void debugDumpStack(Thread theThread)
        {
            // I wish Java had compiler directives.  Would be great to un-comment this for 1.5 JVMs
            java.lang.StackTraceElement[] s=(java.lang.StackTraceElement[])theThread.getStackTrace();
            StringBuffer dump = new StringBuffer("");
            for(int i=0;i<s.length;i++)
            	dump.append("\n   "+s[i].getClassName()+": "+s[i].getMethodName()+"("+s[i].getFileName()+": "+s[i].getLineNumber()+")");
            Log.debugOut("ThreadEngine",dump.toString());
        }
        
        public void run()
        {
            try {
                lastStart=System.currentTimeMillis();
                if(started)
                {
                    Log.errOut(getName(),"DUPLICATE "+getName().toUpperCase()+" RUNNING!!");
                    return;
                }
                started=true;
                shutDown=false;
    
                while(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
                    try{Thread.sleep(1000);}catch(Exception e){}
                lastStart=System.currentTimeMillis();
                while(true)
                {
                    try
                    {
                        while(CMLib.threads().isAllSuspended())
                            try{Thread.sleep(2000);}catch(Exception e){}
                        if(!CMSecurity.isDisabled(getName()))
                        {
                            status("checking database health");
                            String ok=CMLib.database().errorStatus();
                            if((ok.length()!=0)&&(!ok.startsWith("OK")))
                                Log.errOut(getName(),"DB: "+ok);
                            else
                            {
                                lastStop=System.currentTimeMillis();
                                milliTotal+=(lastStop-lastStart);
                                tickTotal++;
                                status("stopped at "+lastStop+", prev was "+(lastStop-lastStart)+"ms");
                                status("sleeping");
                                Thread.sleep(sleepTime);
                                lastStart=System.currentTimeMillis();
                                status("starting run at: "+lastStart);
                                engine.run();
                            }
                        }
                        else
                        {
                            status="sleeping";
                            Thread.sleep(sleepTime);
                        }
                    }
                    catch(InterruptedException ioe)
                    {
                        Log.sysOut(getName(),"Interrupted!");
                        if(shutDown)
                        {
                            shutDown=false;
                            started=false;
                            break;
                        }
                    }
                    catch(Exception e)
                    {
                        Log.errOut(getName(),e);
                    }
                }
            } finally {
                started=false;
            }
            Log.sysOut(getName(),"Shutdown complete.");
        }
    }
}
