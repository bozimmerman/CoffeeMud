package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.exceptions.BadEmailAddressException;
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

import java.io.IOException;
import java.util.*;
/*
   Copyright 2000-2008 Bo Zimmerman

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
public class CMPlayers extends StdLibrary implements PlayerLibrary
{
    public String ID(){return "CMPlayers";}
    public Vector playersList = new Vector();
    
    private ThreadEngine.SupportThread thread=null;
    
    public ThreadEngine.SupportThread getSupportThread() { return thread;}
    
    public int numPlayers() { return playersList.size(); }
    public void addPlayer(MOB newOne)
    {
        synchronized(playersList)
        {
            if(getPlayer(newOne.Name())!=null) return;
            if(playersList.contains(newOne)) return;
            playersList.add(newOne);
        }
    }
    public void delPlayer(MOB oneToDel) { synchronized(playersList){playersList.remove(oneToDel);} }
    public MOB getPlayer(String calledThis)
    {
        MOB M = null;
        synchronized(playersList)
        {
            for (Enumeration p=players(); p.hasMoreElements();)
            {
                M = (MOB)p.nextElement();
                if (M.Name().equalsIgnoreCase(calledThis))
                    return M;
            }
        }
        return null;
    }

    public MOB getLoadPlayer(String last)
    {
        if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
            return null;
        MOB M=null;
        synchronized(playersList)
        {
            M=getPlayer(last);
            if(M!=null) return M;

            for(Enumeration p=players();p.hasMoreElements();)
            {
                MOB mob2=(MOB)p.nextElement();
                if(mob2.Name().equalsIgnoreCase(last))
                { return mob2;}
            }

            MOB TM=CMClass.getMOB("StdMOB");
            if(CMLib.database().DBUserSearch(TM,last))
            {
                M=CMClass.getMOB("StdMOB");
                M.setName(TM.Name());
                CMLib.database().DBReadPlayer(M);
                CMLib.database().DBReadFollowers(M,false);
                if(M.playerStats()!=null)
                    M.playerStats().setLastUpdated(M.playerStats().lastDateTime());
                M.recoverEnvStats();
                M.recoverCharStats();
            }
            TM.destroy();
        }
        return M;
    }

    
	public Enumeration players() { return (Enumeration)DVector.s_enum(playersList); }

    public void obliteratePlayer(MOB deadMOB, boolean quiet)
    {
        if(getPlayer(deadMOB.Name())!=null)
        {
           deadMOB=getPlayer(deadMOB.Name());
           delPlayer(deadMOB);
        }
        for(int s=0;s<CMLib.sessions().size();s++)
        {
            Session S=CMLib.sessions().elementAt(s);
            if((!S.killFlag())&&(S.mob()!=null)&&(S.mob().Name().equals(deadMOB.Name())))
               deadMOB=S.mob();
        }
        CMMsg msg=CMClass.getMsg(deadMOB,null,CMMsg.MSG_RETIRE,(quiet)?null:"A horrible death cry is heard throughout the land.");
        Room deadLoc=deadMOB.location();
        if(deadLoc!=null)
            deadLoc.send(deadMOB,msg);
        try
        {
            for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
            {
                Room R=(Room)r.nextElement();
                if((R!=null)&&(R!=deadLoc))
                {
                    if(R.okMessage(deadMOB,msg))
                        R.sendOthers(deadMOB,msg);
                    else
                    {
                        addPlayer(deadMOB);
                        return;
                    }
                }
            }
        }catch(NoSuchElementException e){}
        StringBuffer newNoPurge=new StringBuffer("");
        Vector protectedOnes=Resources.getFileLineVector(Resources.getFileResource("protectedplayers.ini",false));
        boolean somethingDone=false;
        if((protectedOnes!=null)&&(protectedOnes.size()>0))
        {
            for(int b=0;b<protectedOnes.size();b++)
            {
                String B=(String)protectedOnes.elementAt(b);
                if(!B.equalsIgnoreCase(deadMOB.name()))
                    newNoPurge.append(B+"\n");
                else
                    somethingDone=true;
            }
            if(somethingDone)
            {
                Resources.updateResource("protectedplayers.ini",newNoPurge);
                Resources.saveFileResource("::protectedplayers.ini");
            }
        }

        CMLib.database().DBDeleteMOB(deadMOB);
        if(deadMOB.session()!=null)
            deadMOB.session().logoff(false,false,false);
        Log.sysOut("Scoring",deadMOB.name()+" has been deleted.");
        deadMOB.destroy();
    }
    
    public int savePlayers()
    {
        int processed=0;
        for(Enumeration p=players();p.hasMoreElements();)
        {
            MOB mob=(MOB)p.nextElement();
            if(!mob.isMonster())
            {
                thread.status("just saving "+mob.Name());
                CMLib.database().DBUpdatePlayerStatsOnly(mob);
                if((mob.Name().length()==0)||(mob.playerStats()==null))
                    continue;
                thread.status("saving "+mob.Name()+", "+mob.inventorySize()+"items");
                CMLib.database().DBUpdatePlayerItems(mob);
                thread.status("saving "+mob.Name()+", "+mob.numLearnedAbilities()+"abilities");
                CMLib.database().DBUpdatePlayerAbilities(mob);
                thread.status("saving "+mob.numFollowers()+" followers of "+mob.Name());
                CMLib.database().DBUpdateFollowers(mob);
                mob.playerStats().setLastUpdated(System.currentTimeMillis());
                processed++;
            }
            else
            if((mob.playerStats()!=null)
            &&((mob.playerStats().lastUpdated()==0)
               ||(mob.playerStats().lastUpdated()<mob.playerStats().lastDateTime())))
            {
                thread.status("just saving "+mob.Name());
                CMLib.database().DBUpdatePlayerStatsOnly(mob);
                if((mob.Name().length()==0)||(mob.playerStats()==null))
                    continue;
                thread.status("just saving "+mob.Name()+", "+mob.inventorySize()+" items");
                CMLib.database().DBUpdatePlayerItems(mob);
                thread.status("just saving "+mob.Name()+", "+mob.numLearnedAbilities()+" abilities");
                CMLib.database().DBUpdatePlayerAbilities(mob);
                mob.playerStats().setLastUpdated(System.currentTimeMillis());
                processed++;
            }
        }
        return processed;
    }

    private boolean autoPurge()
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
        thread.status("autopurge process");
        Vector allUsers=CMLib.database().getExtendedUserList();
        Vector protectedOnes=Resources.getFileLineVector(Resources.getFileResource("protectedplayers.ini",false));
        if(protectedOnes==null) protectedOnes=new Vector();

        for(int u=0;u<allUsers.size();u++)
        {
        	DatabaseEngine.ThinPlayer user=(DatabaseEngine.ThinPlayer)allUsers.elementAt(u);
            String name=user.name;
            int level=user.level;
            long last=user.last;
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
                Log.debugOut(thread.getName(),name+" last on "+CMLib.time().date2String(last)+" will be warned on "+CMLib.time().date2String(warn)+" and purged on "+CMLib.time().date2String(when));
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
                        MOB M=getLoadPlayer(name);
                        if((M!=null)&&(M.playerStats()!=null))
                        {
                            warnStr.append(M.name()+" "+M.playerStats().getEmail()+" "+System.currentTimeMillis()+"\n");
                            Resources.updateResource("warnedplayers.ini",warnStr);
                            Resources.saveFileResource("::warnedplayers.ini");
                            if(CMSecurity.isDebugging("AUTOPURGE"))
                                Log.debugOut(thread.getName(),name+" is now warned.");
                            warnPrePurge(M,warn-when);
                        }
                    }
                    else
                    if(CMSecurity.isDebugging("AUTOPURGE"))
                        Log.debugOut(thread.getName(),name+" has already been warned on "+CMLib.time().date2String(foundWarning));
                }
                else
                if(CMSecurity.isDebugging("AUTOPURGE"))
                    Log.debugOut(thread.getName(),name+" is protected from purge warnings.");
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
                    MOB M=getLoadPlayer(name);
                    if(M!=null)
                    {
                        obliteratePlayer(M,true);
                        Log.sysOut(thread.getName(),"AutoPurged user "+name+". Last logged in "+(CMLib.time().date2String(last))+".");
                    }
                }
                else
                if(CMSecurity.isDebugging("AUTOPURGE"))
                    Log.debugOut(thread.getName(),name+" is protected from purging.");
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
            int days=(int)CMath.div((double)timeLeft,1000*60*60*24);
            textTimeLeft = days + " days";
        }
        else
        {
            int hours=(int)CMath.div((double)timeLeft,1000*60*60);
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
            Log.errOut(thread.getName(),"Unable to notify "+to+" of impending autopurge.  Invalid email address.");
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
            Log.errOut(thread.getName(),"Unable to notify "+to+" of impending autopurge.");
        }
    }

    public boolean activate() {
        if(thread==null)
            thread=new ThreadEngine.SupportThread("THPlayers"+Thread.currentThread().getThreadGroup().getName().charAt(0), 
                    MudHost.TIME_SAVETHREAD_SLEEP, this, CMSecurity.isDebugging("SAVETHREAD"));
        if(!thread.started)
            thread.start();
        return true;
    }
    
    public boolean shutdown() {
        playersList.clear();
        thread.shutdown();
        return true;
    }
    
    public void forceTick()
    {
        if(thread.status.equalsIgnoreCase("sleeping"))
        {
            thread.interrupt();
            return;
        }
    }

    public void run()
    {
        if((!CMSecurity.isDisabled("SAVETHREAD"))
        &&(!CMSecurity.isDisabled("PLAYERTHREAD")))
        {
            thread.status("checking player titles.");
            for(Enumeration e=players();e.hasMoreElements();)
            {
                MOB M=(MOB)e.nextElement();
                if(M.playerStats()!=null)
                {
                    if((CMLib.titles().evaluateAutoTitles(M))&&(!CMLib.flags().isInTheGame(M,true)))
                        CMLib.database().DBUpdatePlayerStatsOnly(M);
                }
            }
            autoPurge();
            if(!CMSecurity.isSaveFlag("NOPLAYERS"))
                savePlayers();
            thread.status("not saving players");
        }
    }
}
