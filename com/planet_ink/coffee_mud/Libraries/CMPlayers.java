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
public class CMPlayers extends StdLibrary implements PlayerLibrary
{
    public String ID(){return "CMPlayers";}
    public Vector<MOB> playersList = new Vector<MOB>();
    public Vector<PlayerAccount> accountsList = new Vector<PlayerAccount>();
    
    private ThreadEngine.SupportThread thread=null;
    
    public ThreadEngine.SupportThread getSupportThread() { return thread;}
    
    public int numPlayers() { return playersList.size(); }
    public void addPlayer(MOB newOne)
    {
        synchronized(playersList)
        {
            if(getPlayer(newOne.Name())!=null) return;
            if(playersList.contains(newOne)) return;
            PlayerAccount acct = null;
            if(newOne.playerStats()!=null)
            	acct=newOne.playerStats().getAccount();
            playersList.add(newOne);
            if((acct != null)&&(getAccount(acct.accountName())==null)&&(!accountsList.contains(acct)))
            	accountsList.add(acct);
        }
    }
    public void delPlayer(MOB oneToDel) { synchronized(playersList){playersList.remove(oneToDel);} }
    public PlayerAccount getLoadAccount(String calledThis)
    {
    	PlayerAccount A = getAccount(calledThis);
    	if(A!=null) return A;
        return CMLib.database().DBReadAccount(calledThis);
    }
    
    public PlayerAccount getAccount(String calledThis)
    {
        synchronized(playersList)
        {
        	MOB M=null;
        	for(PlayerAccount A : accountsList)
        		if(A.accountName().equalsIgnoreCase(calledThis))
        			return A;
            for (Enumeration p=players(); p.hasMoreElements();)
            {
                M = (MOB)p.nextElement();
                if((M.playerStats()!=null)
                &&(M.playerStats().getAccount()!=null)
                &&(M.playerStats().getAccount().accountName().equalsIgnoreCase(calledThis)))
                {
                	accountsList.add(M.playerStats().getAccount());
                    return M.playerStats().getAccount();
                }
            }
        }
        return null;
    }
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

            if(playerExists(last))
            {
                M=CMClass.getMOB("StdMOB");
                M.setName(CMStrings.capitalizeAndLower(last));
                CMLib.database().DBReadPlayer(M);
                CMLib.database().DBReadFollowers(M,false);
                if(M.playerStats()!=null)
                    M.playerStats().setLastUpdated(M.playerStats().lastDateTime());
                M.recoverEnvStats();
                M.recoverCharStats();
                Ability A=null;
        		for(int a=0;a<M.numLearnedAbilities();a++)
        		{
        			A=M.fetchAbility(a);
        			if(A!=null) A.autoInvocation(M);
        		}
            }
        }
        return M;
    }

    public boolean accountExists(String name)
    {
    	if(name==null) return false;
    	name=CMStrings.capitalizeAndLower(name);
    	return getLoadAccount(name)!=null;
    }
    
    public boolean playerExists(String name)
    {
    	if(name==null) return false;
    	name=CMStrings.capitalizeAndLower(name);
    	for(Enumeration<MOB> e=players();e.hasMoreElements();)
    		if(e.nextElement().Name().equals(name))
    			return true;
    	return CMLib.database().DBUserSearch(name)!=null;
    }
	public Enumeration players() { return (Enumeration)DVector.s_enum(playersList); }
	public Enumeration accounts() { return (Enumeration)DVector.s_enum(accountsList); }

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
                Resources.updateFileResource("::protectedplayers.ini",newNoPurge);
        }

        CMLib.database().DBDeleteMOB(deadMOB);
        if(deadMOB.session()!=null)
            deadMOB.session().kill(false,false,false);
        Log.sysOut("Scoring",deadMOB.name()+" has been deleted.");
        deadMOB.destroy();
    }
    
    public void obliterateAccountOnly(PlayerAccount deadAccount)
    {
    	deadAccount = getLoadAccount(deadAccount.accountName());
    	if(deadAccount==null) return;
    	accountsList.remove(deadAccount);
        StringBuffer newNoPurge=new StringBuffer("");
        Vector protectedOnes=Resources.getFileLineVector(Resources.getFileResource("protectedplayers.ini",false));
        boolean somethingDone=false;
        if((protectedOnes!=null)&&(protectedOnes.size()>0))
        {
            for(int b=0;b<protectedOnes.size();b++)
            {
                String B=(String)protectedOnes.elementAt(b);
                if(!B.equalsIgnoreCase(deadAccount.accountName()))
                    newNoPurge.append(B+"\n");
                else
                    somethingDone=true;
            }
            if(somethingDone)
                Resources.updateFileResource("::protectedplayers.ini",newNoPurge);
        }

        CMLib.database().DBDeleteAccount(deadAccount);
        Log.sysOut("Scoring",deadAccount.accountName()+" has been deleted.");
    }
    
    public int savePlayers()
    {
        int processed=0;
        for(Enumeration p=players();p.hasMoreElements();)
        {
            MOB mob=(MOB)p.nextElement();
            if(!mob.isMonster())
            {
            	CMLib.factions().updatePlayerFactions(mob,mob.location());
                thread.status("just saving "+mob.Name());
                CMLib.database().DBUpdatePlayerMOBOnly(mob);
                if((mob.Name().length()==0)||(mob.playerStats()==null))
                    continue;
                thread.status("saving "+mob.Name()+", "+mob.inventorySize()+" items");
                CMLib.database().DBUpdatePlayerItems(mob);
                thread.status("saving "+mob.Name()+", "+mob.numLearnedAbilities()+" abilities");
                CMLib.database().DBUpdatePlayerAbilities(mob);
                thread.status("saving "+mob.numFollowers()+" followers of "+mob.Name());
                CMLib.database().DBUpdateFollowers(mob);
                PlayerAccount account = mob.playerStats().getAccount();
                mob.playerStats().setLastUpdated(System.currentTimeMillis());
                if(account!=null)
                {
                    thread.status("saving account "+account.accountName()+" for "+mob.Name());
                    CMLib.database().DBUpdateAccount(account);
                    account.setLastUpdated(System.currentTimeMillis());
                }
                processed++;
            }
            else
            if((mob.playerStats()!=null)
            &&((mob.playerStats().lastUpdated()==0)
               ||(mob.playerStats().lastUpdated()<mob.playerStats().lastDateTime())))
            {
                thread.status("just saving "+mob.Name());
                CMLib.database().DBUpdatePlayerMOBOnly(mob);
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
    
	public String getThinSortValue(ThinPlayer player, int code) 
	{
		switch(code) {
		case 0: return player.name;
		case 1: return player.charClass;
		case 2: return player.race;
		case 3: return Integer.toString(player.level);
		case 4: return Integer.toString(player.age);
		case 5: return Long.toString(player.last);
		case 6: return player.email;
		case 7: return player.ip;
		}
		return player.name;
	}
	
	public String getThinSortValue(PlayerAccount account, int code) 
	{
		switch(code) {
		case 0: return account.accountName();
		case 1: return Long.toString(account.lastDateTime());
		case 2: return account.getEmail();
		case 3: return account.lastIP();
		case 4: return Integer.toString(account.numPlayers());
		}
		return account.accountName();
	}
	
	public int getCharThinSortCode(String codeName, boolean loose) 
	{
		int x=CMParms.indexOf(CHAR_THIN_SORT_CODES,codeName);
		if(x<0)x=CMParms.indexOf(CHAR_THIN_SORT_CODES2,codeName);
		if(!loose) return x;
		if(x<0)
			for(int s=0;s<CHAR_THIN_SORT_CODES.length;s++)
				if(CHAR_THIN_SORT_CODES[s].startsWith(codeName))
					x=s;
		if(x<0)
			for(int s=0;s<CHAR_THIN_SORT_CODES2.length;s++)
				if(CHAR_THIN_SORT_CODES2[s].startsWith(codeName))
					x=s;
		return x;
	}
	
	public int getAccountThinSortCode(String codeName, boolean loose) 
	{
		int x=CMParms.indexOf(ACCOUNT_THIN_SORT_CODES,codeName);
		if(!loose) return x;
		if(x<0)
			for(int s=0;s<ACCOUNT_THIN_SORT_CODES.length;s++)
				if(ACCOUNT_THIN_SORT_CODES[s].startsWith(codeName))
					x=s;
		return x;
	}
	
    public Enumeration<ThinPlayer> thinPlayers(String sort, Hashtable cache)
    {
		List<PlayerLibrary.ThinPlayer> V=(cache==null)?null:(List<PlayerLibrary.ThinPlayer>)cache.get("PLAYERLISTVECTOR"+sort);
		if(V==null)
		{
			V=CMLib.database().getExtendedUserList();
			int code=getCharThinSortCode(sort,false);
			if((sort.length()>0)
			&&(code>=0)
			&&(V.size()>1))
			{
				List<PlayerLibrary.ThinPlayer> unV=V;
				V=new Vector();
				while(unV.size()>0)
				{
					ThinPlayer M=unV.get(0);
					String loweStr=getThinSortValue(M,code);
					ThinPlayer lowestM=M;
					for(int i=1;i<unV.size();i++)
					{
						M=unV.get(i);
						String val=getThinSortValue(M,code);
						if((CMath.isNumber(val)&&CMath.isNumber(loweStr)))
						{
							if(CMath.s_long(val)<CMath.s_long(loweStr))
							{
								loweStr=val;
								lowestM=M;
							}
						}
						else
						if(val.compareTo(loweStr)<0)
						{
							loweStr=val;
							lowestM=M;
						}
					}
					unV.remove(lowestM);
					V.add(lowestM);
				}
			}
			if(cache!=null)
				cache.put("PLAYERLISTVECTOR"+sort,V);
		}
		return DVector.s_enum(V);
    }

    public Enumeration<PlayerAccount> accounts(String sort, Hashtable cache)
    {
		List<PlayerAccount> V=(cache==null)?null:(List<PlayerAccount>)cache.get("ACCOUNTLISTVECTOR"+sort);
		if(V==null)
		{
			V=CMLib.database().DBListAccounts(null);
			int code=getAccountThinSortCode(sort,false);
			if((sort.length()>0)
			&&(code>=0)
			&&(V.size()>1))
			{
				List<PlayerAccount> unV=V;
				V=new Vector();
				while(unV.size()>0)
				{
					PlayerAccount A=unV.get(0);
					String loweStr=getThinSortValue(A,code);
					PlayerAccount lowestA=A;
					for(int i=1;i<unV.size();i++)
					{
						A=unV.get(i);
						String val=getThinSortValue(A,code);
						if((CMath.isNumber(val)&&CMath.isNumber(loweStr)))
						{
							if(CMath.s_long(val)<CMath.s_long(loweStr))
							{
								loweStr=val;
								lowestA=A;
							}
						}
						else
						if(val.compareTo(loweStr)<0)
						{
							loweStr=val;
							lowestA=A;
						}
					}
					unV.remove(lowestA);
					V.add(lowestA);
				}
			}
			if(cache!=null)
				cache.put("ACCOUNTLISTVECTOR"+sort,V);
		}
		return DVector.s_enum(V);
    }

    private boolean autoPurge()
    {
        if(CMSecurity.isDisabled("AUTOPURGE"))
        	return true;
        
        long[] levels=new long[2001];
        long[] prePurgeLevels=new long[2001];
        for(int i=0;i<levels.length;i++) levels[i]=0;
        for(int i=0;i<prePurgeLevels.length;i++) prePurgeLevels[i]=0;
        String mask=CMProps.getVar(CMProps.SYSTEM_AUTOPURGE);
        Vector maskV=CMParms.parseCommas(mask.trim(),false);
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
                long realVal=(val*TimeManager.MILI_DAY);
                long purgePoint=realVal-(prepurge*TimeManager.MILI_DAY);
            	if(val <= 0)
            	{
            		realVal = 0;
            		purgePoint = 0;
            	}
                for(int s=start;s<=finish;s++)
                {
                    if(levels[s]==0) levels[s]=realVal;
                    if(prePurgeLevels[s]==0) prePurgeLevels[s]=purgePoint;
                }
            }
        }
        thread.status("autopurge process");
        List<PlayerLibrary.ThinPlayer> allUsers=CMLib.database().getExtendedUserList();
        Vector protectedOnes=Resources.getFileLineVector(Resources.getFileResource("protectedplayers.ini",false));
        if(protectedOnes==null) protectedOnes=new Vector();

        for(ThinPlayer user : allUsers)
        {
            String name=user.name;
            int level=user.level;
            long userLastLoginDateTime=user.last;
            long purgeDateTime;
            long warnDateTime;
            if(level>levels.length)
            {
            	if(levels[levels.length-1]==0)
            	{
                    if(CMSecurity.isDebugging("AUTOPURGE"))
                        Log.debugOut(thread.getName(),name+" last on "+CMLib.time().date2String(userLastLoginDateTime)+".  Nothing will be done about it.");
            		continue;
            	}
                purgeDateTime=userLastLoginDateTime + levels[levels.length-1];
                warnDateTime=userLastLoginDateTime + prePurgeLevels[prePurgeLevels.length-1];
            }
            else
            if(level>=0)
            {
            	if(levels[level]==0)
            	{
                    if(CMSecurity.isDebugging("AUTOPURGE"))
                        Log.debugOut(thread.getName(),name+" last on "+CMLib.time().date2String(userLastLoginDateTime)+".  Nothing will be done about it.");
            		continue;
            	}
                purgeDateTime=userLastLoginDateTime + levels[level];
                warnDateTime=userLastLoginDateTime + prePurgeLevels[level];
            }
            else
                continue;
            if(CMSecurity.isDebugging("AUTOPURGE"))
                Log.debugOut(thread.getName(),name+" last on "+CMLib.time().date2String(userLastLoginDateTime)+" will be warned on "+CMLib.time().date2String(warnDateTime)+" and purged on "+CMLib.time().date2String(purgeDateTime));
            if((System.currentTimeMillis()>purgeDateTime)||(System.currentTimeMillis()>warnDateTime))
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
                if(protectedOne)
                {
                    if(CMSecurity.isDebugging("AUTOPURGE"))
                        Log.debugOut(thread.getName(),name+" is protected from purging.");
                	continue;
                }
                
                Vector warnedOnes=Resources.getFileLineVector(Resources.getFileResource("warnedplayers.ini",false));
                long foundWarningDateTime=-1;
                StringBuffer warnStr=new StringBuffer("");
                if((warnedOnes!=null)&&(warnedOnes.size()>0))
                    for(int b=0;b<warnedOnes.size();b++)
                    {
                        String B=((String)warnedOnes.elementAt(b)).trim();
                        if((B.trim().length()>0)
                        &&(B.toUpperCase().startsWith(name.toUpperCase()+" ")))
                        {
                            int lastSpace=B.lastIndexOf(" ");
                            foundWarningDateTime=CMath.s_long(B.substring(lastSpace+1).trim());
                            if(System.currentTimeMillis() < purgeDateTime + (10 * TimeManager.MILI_DAY))
	                            warnStr.append(B+"\n");
                        }
                    }
                if((foundWarningDateTime<0)
                &&(System.currentTimeMillis()>warnDateTime))
                {
                    MOB M=getLoadPlayer(name);
                    if((M!=null)&&(M.playerStats()!=null))
                    {
                        warnStr.append(M.name()+" "+M.playerStats().getEmail()+" "+System.currentTimeMillis()+"\n");
                        Resources.updateFileResource("::warnedplayers.ini",warnStr);
                        if(CMSecurity.isDebugging("AUTOPURGE"))
                            Log.debugOut(thread.getName(),name+" is now warned.");
                        warnPrePurge(M,purgeDateTime-System.currentTimeMillis());
                    }
                }
                else
                if((System.currentTimeMillis()>purgeDateTime)
                &&(foundWarningDateTime > 0)
                &&((System.currentTimeMillis()-foundWarningDateTime)>TimeManager.MILI_DAY))
                {
                    MOB M=getLoadPlayer(name);
                    if((M!=null)&&(!CMSecurity.isASysOp(M))&&(!CMSecurity.isAllowedAnywhere(M, "NOPURGE")))
                    {
                        obliteratePlayer(M,true);
                        Log.sysOut(thread.getName(),"AutoPurged user "+name+". Last logged in "+(CMLib.time().date2String(userLastLoginDateTime))+".");
                    }
                }
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
        if(timeLeft<0)
        	timeLeft = 1000*60*60*24;
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
                        CMLib.database().DBUpdatePlayerMOBOnly(M);
                }
            }
            autoPurge();
            if(!CMSecurity.isSaveFlag("NOPLAYERS"))
                savePlayers();
            thread.status("not saving players");
        }
    }
}
