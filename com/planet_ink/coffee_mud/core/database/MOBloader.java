package com.planet_ink.coffee_mud.core.database;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.sql.*;
import java.util.*;

/*
 * Copyright 2000-2010 Bo Zimmerman Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
@SuppressWarnings("unchecked")
public class MOBloader
{
    protected DBConnector DB=null;

    public MOBloader(DBConnector newDB)
    {
        DB=newDB;
    }
    protected Room emptyRoom=null;

    public boolean DBReadUserOnly(MOB mob)
    {
        if(mob.Name().length()==0) return false;
        boolean found=false;
        DBConnection D=null;
        try
        {
            D=DB.DBFetch();
            ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMUSERID='"+mob.Name()+"'");
            if(R.next())
            {
                CharStats stats=mob.baseCharStats();
                CharState state=mob.baseState();
                PlayerStats pstats=(PlayerStats)CMClass.getCommon("DefaultPlayerStats");
                mob.setPlayerStats(pstats);
                String username=DBConnections.getRes(R,"CMUSERID");
                String password=DBConnections.getRes(R,"CMPASS");
                mob.setName(username);
                pstats.setPassword(password);
                stats.setMyClasses(DBConnections.getRes(R,"CMCLAS"));
                stats.setStat(CharStats.STAT_STRENGTH,CMath.s_int(DBConnections.getRes(R,"CMSTRE")));
                stats.setMyRace(CMClass.getRace(DBConnections.getRes(R,"CMRACE")));
                stats.setStat(CharStats.STAT_DEXTERITY,CMath.s_int(DBConnections.getRes(R,"CMDEXT")));
                stats.setStat(CharStats.STAT_CONSTITUTION,CMath.s_int(DBConnections.getRes(R,"CMCONS")));
                stats.setStat(CharStats.STAT_GENDER,DBConnections.getRes(R,"CMGEND").charAt(0));
                stats.setStat(CharStats.STAT_WISDOM,CMath.s_int(DBConnections.getRes(R,"CMWISD")));
                stats.setStat(CharStats.STAT_INTELLIGENCE,CMath.s_int(DBConnections.getRes(R,"CMINTE")));
                stats.setStat(CharStats.STAT_CHARISMA,CMath.s_int(DBConnections.getRes(R,"CMCHAR")));
                state.setHitPoints(CMath.s_int(DBConnections.getRes(R,"CMHITP")));
                stats.setMyLevels(DBConnections.getRes(R,"CMLEVL"));
                int level=0;
                for(int i=0;i<mob.baseCharStats().numClasses();i++)
                    level+=stats.getClassLevel(mob.baseCharStats().getMyClass(i));
                mob.baseEnvStats().setLevel(level);
                state.setMana(CMath.s_int(DBConnections.getRes(R,"CMMANA")));
                state.setMovement(CMath.s_int(DBConnections.getRes(R,"CMMOVE")));
                mob.setDescription(DBConnections.getRes(R,"CMDESC"));
                int align=(CMath.s_int(DBConnections.getRes(R,"CMALIG")));
                if((CMLib.factions().getFaction(CMLib.factions().AlignID())!=null)&&(align>=0))
                    CMLib.factions().setAlignmentOldRange(mob,align);
                mob.setExperience(CMath.s_int(DBConnections.getRes(R,"CMEXPE")));
                //mob.setExpNextLevel(CMath.s_int(DBConnections.getRes(R,"CMEXLV")));
                mob.setWorshipCharID(DBConnections.getRes(R,"CMWORS"));
                mob.setPractices(CMath.s_int(DBConnections.getRes(R,"CMPRAC")));
                mob.setTrains(CMath.s_int(DBConnections.getRes(R,"CMTRAI")));
                mob.setAgeHours(CMath.s_long(DBConnections.getRes(R,"CMAGEH")));
                mob.setMoney(CMath.s_int(DBConnections.getRes(R,"CMGOLD")));
                mob.setWimpHitPoint(CMath.s_int(DBConnections.getRes(R,"CMWIMP")));
                mob.setQuestPoint(CMath.s_int(DBConnections.getRes(R,"CMQUES")));
                String roomID=DBConnections.getRes(R,"CMROID");
                if(roomID==null) roomID="";
                int x=roomID.indexOf("||");
                if(x>=0)
                {
                    mob.setLocation(CMLib.map().getRoom(roomID.substring(x+2)));
                    roomID=roomID.substring(0,x);
                }
                mob.setStartRoom(CMLib.map().getRoom(roomID));
                pstats.setLastDateTime(CMath.s_long(DBConnections.getRes(R,"CMDATE")));
                pstats.setChannelMask((int)DBConnections.getLongRes(R,"CMCHAN"));
                mob.baseEnvStats().setAttackAdjustment(CMath.s_int(DBConnections.getRes(R,"CMATTA")));
                mob.baseEnvStats().setArmor(CMath.s_int(DBConnections.getRes(R,"CMAMOR")));
                mob.baseEnvStats().setDamage(CMath.s_int(DBConnections.getRes(R,"CMDAMG")));
                mob.setBitmap(CMath.s_int(DBConnections.getRes(R,"CMBTMP")));
                mob.setLiegeID(DBConnections.getRes(R,"CMLEIG"));
                mob.baseEnvStats().setHeight((int)DBConnections.getLongRes(R,"CMHEIT"));
                mob.baseEnvStats().setWeight((int)DBConnections.getLongRes(R,"CMWEIT"));
                pstats.setPrompt(DBConnections.getRes(R,"CMPRPT"));
                String colorStr=DBConnections.getRes(R,"CMCOLR");
                if((colorStr!=null)&&(colorStr.length()>0)&&(!colorStr.equalsIgnoreCase("NULL"))) pstats.setColorStr(colorStr);
                pstats.setLastIP(DBConnections.getRes(R,"CMLSIP"));
                mob.setClanID(DBConnections.getRes(R,"CMCLAN"));
                mob.setClanRole((int)DBConnections.getLongRes(R,"CMCLRO"));
                pstats.setEmail(DBConnections.getRes(R,"CMEMAL"));
                String buf=DBConnections.getRes(R,"CMPFIL");
                pstats.setXML(buf);
                stats.setNonBaseStatsFromString(DBConnections.getRes(R,"CMSAVE"));
                Vector V9=CMParms.parseSemicolons(CMLib.xml().returnXMLValue(buf,"TATTS"),true);
                while(mob.numTattoos()>0)
                    mob.delTattoo(mob.fetchTattoo(0));
                for(int v=0;v<V9.size();v++)
                    mob.addTattoo((String)V9.elementAt(v));
                V9=CMParms.parseSemicolons(CMLib.xml().returnXMLValue(buf,"EDUS"),true);
                while(mob.numExpertises()>0)
                    mob.delExpertise(mob.fetchExpertise(0));
                for(int v=0;v<V9.size();v++)
                    mob.addExpertise((String)V9.elementAt(v));
                if(pstats.getBirthday()==null)
                    stats.setStat(CharStats.STAT_AGE,
                        pstats.initializeBirthday((int)Math.round(CMath.div(mob.getAgeHours(),60.0)),stats.getMyRace()));
                mob.setImage(CMLib.xml().returnXMLValue(buf,"IMG"));
                Vector CleanXML=CMLib.xml().parseAllXML(DBConnections.getRes(R,"CMMXML"));
                R.close();
                DB.DBDone(D);
                D=null;
                CMLib.coffeeMaker().setFactionFromXML(mob,CleanXML);
                found=true;
            }
        }
        catch(Exception sqle)
        {
            Log.errOut("MOB",sqle);
        }
        finally
        {
        	if(D!=null) 
        		DB.DBDone(D);
        }
        return found;
    }

    public void DBRead(MOB mob)
    {
        if(mob.Name().length()==0) return;
        if(emptyRoom==null) emptyRoom=CMClass.getLocale("StdRoom");
        int oldDisposition=mob.baseEnvStats().disposition();
        mob.baseEnvStats().setDisposition(EnvStats.IS_NOT_SEEN|EnvStats.IS_SNEAKING);
        mob.envStats().setDisposition(EnvStats.IS_NOT_SEEN|EnvStats.IS_SNEAKING);
        CMLib.players().addPlayer(mob);
        DBReadUserOnly(mob);
        Room oldLoc=mob.location();
        boolean inhab=false;
        if(oldLoc!=null) inhab=oldLoc.isInhabitant(mob);
        mob.setLocation(emptyRoom);
        DBConnection D=null;
        // now grab the items
        try
        {
            D=DB.DBFetch();
            ResultSet R=D.query("SELECT * FROM CMCHIT WHERE CMUSERID='"+mob.Name()+"'");
            Hashtable itemNums=new Hashtable();
            Hashtable itemLocs=new Hashtable();
            while(R.next())
            {
                String itemNum=DBConnections.getRes(R,"CMITNM");
                String itemID=DBConnections.getRes(R,"CMITID");
                Item newItem=CMClass.getItem(itemID);
                if(newItem==null)
                    Log.errOut("MOB","Couldn't find item '"+itemID+"'");
                else
                {
                    itemNums.put(itemNum,newItem);
                    newItem.setMiscText(DBConnections.getResQuietly(R,"CMITTX"));
                    String loc=DBConnections.getResQuietly(R,"CMITLO");
                    if(loc.length()>0)
                    {
                        Item container=(Item)itemNums.get(loc);
                        if(container!=null)
                            newItem.setContainer(container);
                        else
                            itemLocs.put(newItem,loc);
                    }
                    newItem.wearAt((int)DBConnections.getLongRes(R,"CMITWO"));
                    newItem.setUsesRemaining((int)DBConnections.getLongRes(R,"CMITUR"));
                    newItem.baseEnvStats().setLevel((int)DBConnections.getLongRes(R,"CMITLV"));
                    newItem.baseEnvStats().setAbility((int)DBConnections.getLongRes(R,"CMITAB"));
                    newItem.baseEnvStats().setHeight((int)DBConnections.getLongRes(R,"CMHEIT"));
                    newItem.recoverEnvStats();
                    mob.addInventory(newItem);
                }
            }
            for(Enumeration e=itemLocs.keys();e.hasMoreElements();)
            {
                Item keyItem=(Item)e.nextElement();
                String location=(String)itemLocs.get(keyItem);
                Item container=(Item)itemNums.get(location);
                if(container!=null)
                {
                    keyItem.setContainer(container);
                    keyItem.recoverEnvStats();
                    container.recoverEnvStats();
                }
            }
        }catch(Exception sqle)
        {
            Log.errOut("MOB",sqle);
        }
        if(D!=null) DB.DBDone(D);
        D=null;
        if(oldLoc!=null)
        {
	        mob.setLocation(oldLoc);
	        if(inhab&&(!oldLoc.isInhabitant(mob))) 
	        	oldLoc.addInhabitant(mob);
        }
        // now grab the abilities
        try
        {
            D=DB.DBFetch();
            ResultSet R=D.query("SELECT * FROM CMCHAB WHERE CMUSERID='"+mob.Name()+"'");
            while(R.next())
            {
                String abilityID=DBConnections.getRes(R,"CMABID");
                int proficiency=(int)DBConnections.getLongRes(R,"CMABPF");
                if((proficiency==Integer.MIN_VALUE)||(proficiency==Integer.MIN_VALUE+1))
                {
                    if(abilityID.equalsIgnoreCase("ScriptingEngine"))
                    {
                        if(CMClass.getCommon("DefaultScriptingEngine")==null)
                            Log.errOut("MOB","Couldn't find scripting engine!");
                        else
                        {

                            String xml=DBConnections.getRes(R,"CMABTX");
                            if(xml.length()>0)
                                CMLib.coffeeMaker().setGenScripts(mob,CMLib.xml().parseAllXML(xml),true);
                        }
                    }
                    else
                    {
                        Behavior newBehavior=CMClass.getBehavior(abilityID);
                        if(newBehavior==null)
                            Log.errOut("MOB","Couldn't find behavior '"+abilityID+"'");
                        else
                        {
                            newBehavior.setParms(DBConnections.getRes(R,"CMABTX"));
                            mob.addBehavior(newBehavior);
                        }
                    }
                }
                else
                {
                    Ability newAbility=CMClass.getAbility(abilityID);
                    if(newAbility==null)
                        Log.errOut("MOB","Couldn't find ability '"+abilityID+"'");
                    else
                    {
                        if((proficiency<0)||(proficiency==Integer.MAX_VALUE))
                        {
                            if(proficiency==Integer.MAX_VALUE)
                            {
                                newAbility.setProficiency(CMLib.ableMapper().getMaxProficiency(newAbility.ID()));
                                mob.addNonUninvokableEffect(newAbility);
                                newAbility.setMiscText(DBConnections.getRes(R,"CMABTX"));
                            }else
                            {
                                proficiency=proficiency+200;
                                newAbility.setProficiency(proficiency);
                                newAbility.setMiscText(DBConnections.getRes(R,"CMABTX"));
                                Ability newAbility2=(Ability)newAbility.copyOf();
                                mob.addNonUninvokableEffect(newAbility);
                                newAbility2.recoverEnvStats();
                                mob.addAbility(newAbility2);
                            }
                        }else
                        {
                            newAbility.setProficiency(proficiency);
                            newAbility.setMiscText(DBConnections.getRes(R,"CMABTX"));
                            newAbility.recoverEnvStats();
                            mob.addAbility(newAbility);
                        }
                    }
                }
            }
        }catch(Exception sqle)
        {
            Log.errOut("MOB",sqle);
        }
        if(D!=null) DB.DBDone(D);
        D=null;
        mob.baseEnvStats().setDisposition(oldDisposition);
        mob.recoverCharStats();
        mob.recoverEnvStats();
        mob.recoverMaxState();
        mob.resetToMaxState();
        if(mob.baseCharStats()!=null)
        {
            mob.baseCharStats().getCurrentClass().startCharacter(mob,false,true);
            int oldWeight=mob.baseEnvStats().weight();
            int oldHeight=mob.baseEnvStats().height();
            mob.baseCharStats().getMyRace().startRacing(mob,true);
            if(oldWeight>0) mob.baseEnvStats().setWeight(oldWeight);
            if(oldHeight>0) mob.baseEnvStats().setHeight(oldHeight);
        }
        mob.recoverCharStats();
        mob.recoverEnvStats();
        mob.recoverMaxState();
        mob.resetToMaxState();
        // wont add if same name already exists
    }

    public List<String> getUserList()
    {
        DBConnection D=null;
        Vector V=new Vector();
        try
        {
            D=DB.DBFetch();
            ResultSet R=D.query("SELECT * FROM CMCHAR");
            if(R!=null) while(R.next())
            {
                String username=DBConnections.getRes(R,"CMUSERID");
                V.addElement(username);
            }
        }catch(Exception sqle)
        {
            Log.errOut("MOB",sqle);
        }
        if(D!=null) DB.DBDone(D);
        return V;
    }

    protected PlayerLibrary.ThinPlayer parseThinUser(ResultSet R)
    {
        try
        {
	    	PlayerLibrary.ThinPlayer thisUser=new PlayerLibrary.ThinPlayer();
	        thisUser.name=DBConnections.getRes(R,"CMUSERID");
	        String cclass=DBConnections.getRes(R,"CMCLAS");
	        int x=cclass.lastIndexOf(";");
	        CharClass C=null;
	        if((x>0)&&(x<cclass.length()-2))
	        {
	            C=CMClass.getCharClass(cclass.substring(x+1));
	            if(C!=null) cclass=C.name();
	        }
	        thisUser.charClass=(cclass);
	        String rrace=DBConnections.getRes(R,"CMRACE");
	        Race R2=CMClass.getRace(rrace);
	        if(R2!=null)
	            thisUser.race=(R2.name());
	        else
	            thisUser.race=rrace;
	        String lvl=DBConnections.getRes(R,"CMLEVL");
	        x=lvl.indexOf(";");
	        int level=0;
	        while(x>=0)
	        {
	            level+=CMath.s_int(lvl.substring(0,x));
	            lvl=lvl.substring(x+1);
	            x=lvl.indexOf(";");
	        }
	        if(lvl.length()>0) level+=CMath.s_int(lvl);
	        thisUser.level=level;
	        thisUser.age=(int)DBConnections.getLongRes(R,"CMAGEH");
	        MOB M=CMLib.players().getPlayer((String)thisUser.name);
	        if((M!=null)&&(M.lastTickedDateTime()>0))
	            thisUser.last=M.lastTickedDateTime();
	        else
	            thisUser.last=DBConnections.getLongRes(R,"CMDATE");
	        String lsIP=DBConnections.getRes(R,"CMLSIP");
	        thisUser.email=DBConnections.getRes(R,"CMEMAL");
	        thisUser.ip=lsIP;
	        return thisUser;
        }catch(Exception e)
        {
            Log.errOut("MOBloader",e);
        }
        return null;
    }
    
    public PlayerLibrary.ThinPlayer getThinUser(String name)
    {
        DBConnection D=null;
    	PlayerLibrary.ThinPlayer thisUser=null;
        try
        {
            D=DB.DBFetch();
            ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMUSERID='"+name+"'");
            if(R!=null) while(R.next())
            	thisUser=parseThinUser(R);
        }catch(Exception sqle)
        {
            Log.errOut("MOB",sqle);
        }
        if(D!=null) DB.DBDone(D);
        return thisUser;
    }
    
    public List<PlayerLibrary.ThinPlayer> getExtendedUserList()
    {
        DBConnection D=null;
        Vector allUsers=new Vector();
        try
        {
            D=DB.DBFetch();
            ResultSet R=D.query("SELECT * FROM CMCHAR");
            if(R!=null) while(R.next())
            {
            	PlayerLibrary.ThinPlayer thisUser=parseThinUser(R);
            	if(thisUser != null)
                    allUsers.addElement(thisUser);
            }
        }catch(Exception sqle)
        {
            Log.errOut("MOB",sqle);
        }
        if(D!=null) DB.DBDone(D);
        return allUsers;
    }

    public void vassals(MOB mob, String liegeID)
    {
        DBConnection D=null;
        try
        {
            D=DB.DBFetch();
            ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMLEIG='"+liegeID+"'");
            StringBuffer head=new StringBuffer("");
            head.append("[");
            head.append(CMStrings.padRight("Race",8)+" ");
            head.append(CMStrings.padRight("Class",10)+" ");
            head.append(CMStrings.padRight("Lvl",4)+" ");
            head.append(CMStrings.padRight("Exp/Lvl",17));
            head.append("] Character name\n\r");
            HashSet done=new HashSet();
            if(R!=null) while(R.next())
            {
                String username=DBConnections.getRes(R,"CMUSERID");
                MOB M=CMLib.players().getPlayer(username);
                if(M==null)
                {
                    done.add(username);
                    String cclass=DBConnections.getRes(R,"CMCLAS");
                    int x=cclass.lastIndexOf(";");
                    if((x>0)&&(x<cclass.length()-2)) cclass=CMClass.getCharClass(cclass.substring(x+1)).name();
                    String race=(CMClass.getRace(DBConnections.getRes(R,"CMRACE"))).name();
                    String lvl=DBConnections.getRes(R,"CMLEVL");
                    x=lvl.indexOf(";");
                    int level=0;
                    while(x>=0)
                    {
                        level+=CMath.s_int(lvl.substring(0,x));
                        lvl=lvl.substring(x+1);
                        x=lvl.indexOf(";");
                    }
                    if(lvl.length()>0) level+=CMath.s_int(lvl);
                    int exp=CMath.s_int(DBConnections.getRes(R,"CMEXPE"));
                    int exlv=CMath.s_int(DBConnections.getRes(R,"CMEXLV"));
                    head.append("[");
                    head.append(CMStrings.padRight(race,8)+" ");
                    head.append(CMStrings.padRight(cclass,10)+" ");
                    head.append(CMStrings.padRight(Integer.toString(level),4)+" ");
                    head.append(CMStrings.padRight(exp+"/"+exlv,17));
                    head.append("] "+CMStrings.padRight(username,15));
                    head.append("\n\r");
                }
            }
            for(Enumeration e=CMLib.players().players();e.hasMoreElements();)
            {
                MOB M=(MOB)e.nextElement();
                if((M.getLiegeID().equals(liegeID))&&(!done.contains(M.Name())))
                {
                    head.append("[");
                    head.append(CMStrings.padRight(M.charStats().getMyRace().name(),8)+" ");
                    head.append(CMStrings.padRight(M.charStats().getCurrentClass().name(M.charStats().getCurrentClassLevel()),10)+" ");
                    head.append(CMStrings.padRight(""+M.envStats().level(),4)+" ");
                    head.append(CMStrings.padRight(M.getExperience()+"/"+M.getExpNextLevel(),17));
                    head.append("] "+CMStrings.padRight(M.name(),15));
                    head.append("\n\r");
                }
            }
            mob.tell(head.toString());
        }catch(Exception sqle)
        {
            Log.errOut("MOB",sqle);
        }
        if(D!=null) DB.DBDone(D);
    }

    public DVector worshippers(String deityID)
    {
        DBConnection D=null;
        DVector DV=new DVector(4);
        try
        {
            D=DB.DBFetch();
            ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMWORS='"+deityID+"'");
            if(R!=null) while(R.next())
            {
                String username=DBConnections.getRes(R,"CMUSERID");
                String cclass=DBConnections.getRes(R,"CMCLAS");
                int x=cclass.lastIndexOf(";");
                if((x>0)&&(x<cclass.length()-2)) cclass=CMClass.getCharClass(cclass.substring(x+1)).name();
                String race=(CMClass.getRace(DBConnections.getRes(R,"CMRACE"))).name();
                String lvl=DBConnections.getRes(R,"CMLEVL");
                x=lvl.indexOf(";");
                int level=0;
                while(x>=0)
                {
                    level+=CMath.s_int(lvl.substring(0,x));
                    lvl=lvl.substring(x+1);
                    x=lvl.indexOf(";");
                }
                if(lvl.length()>0) level+=CMath.s_int(lvl);
                DV.addElement(username,
                              cclass,
                              ""+level,
                              race);
            }
        }catch(Exception sqle)
        {
            Log.errOut("MOB",sqle);
        }
        if(D!=null) DB.DBDone(D);
        return DV;
    }

    public Vector DBScanFollowers(MOB mob)
    {
        DBConnection D=null;
        Vector V=new Vector();
        // now grab the followers
        try
        {
            D=DB.DBFetch();
            ResultSet R=D.query("SELECT * FROM CMCHFO WHERE CMUSERID='"+mob.Name()+"'");
            while(R.next())
            {
                String MOBID=DBConnections.getRes(R,"CMFOID");
                MOB newMOB=CMClass.getMOB(MOBID);
                if(newMOB==null)
                    Log.errOut("MOB","Couldn't find MOB '"+MOBID+"'");
                else
                {
                    newMOB.setMiscText(DBConnections.getResQuietly(R,"CMFOTX"));
                    newMOB.baseEnvStats().setLevel(((int)DBConnections.getLongRes(R,"CMFOLV")));
                    newMOB.baseEnvStats().setAbility((int)DBConnections.getLongRes(R,"CMFOAB"));
                    newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
                    newMOB.recoverEnvStats();
                    newMOB.recoverCharStats();
                    newMOB.recoverMaxState();
                    newMOB.resetToMaxState();
                    V.addElement(newMOB);
                }
            }
        }catch(Exception sqle)
        {
            Log.errOut("MOB",sqle);
        }
        if(D!=null) DB.DBDone(D);
        return V;
    }
    
    public void DBReadFollowers(MOB mob, boolean bringToLife)
    {
        Room location=mob.location();
        if(location==null) location=mob.getStartRoom();
        Vector V=DBScanFollowers(mob);
        for(int v=0;v<V.size();v++) {
            MOB newMOB=(MOB)V.elementAt(v);
            Room room=(location==null)?newMOB.getStartRoom():location;
            newMOB.setStartRoom(room);
            newMOB.setLocation(room);
            newMOB.setFollowing(mob);
            if((newMOB.getStartRoom()!=null)
            &&(CMLib.law().doesHavePriviledgesHere(mob,newMOB.getStartRoom()))
            &&((newMOB.location()==null)
                    ||(!CMLib.law().doesHavePriviledgesHere(mob,newMOB.location()))))
                newMOB.setLocation(newMOB.getStartRoom());
            if(bringToLife)
            {
                newMOB.bringToLife(mob.location(),true);
                mob.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,"<S-NAME> appears!");
            }
        }
    }

    public void DBUpdateEmail(MOB mob)
    {
        PlayerStats pstats=mob.playerStats();
        if(pstats==null) return;
        DB.update("UPDATE CMCHAR SET  CMEMAL='"+pstats.getEmail()+"'  WHERE CMUSERID='"+mob.Name()+"'");
    }

    public Vector<Clan.MemberRecord> DBClanMembers(String clan)
    {
    	Vector<Clan.MemberRecord> members = new Vector<Clan.MemberRecord>();
        DBConnection D=null;
        try
        {
            D=DB.DBFetch();
            ResultSet R=D.query("SELECT * FROM CMCHAR where CMCLAN='"+clan+"'");
            if(R!=null) while(R.next())
            {
                String username=DB.getRes(R,"CMUSERID");
                long lastDateTime=CMath.s_long(DBConnections.getRes(R,"CMDATE"));
                int role=(int)DB.getLongRes(R,"CMCLRO");
                Clan.MemberRecord member = new Clan.MemberRecord(username,role,lastDateTime);
                members.addElement(member);
                MOB M=CMLib.players().getPlayer(username);
                if((M!=null)&&(M.lastTickedDateTime()>0))
                    member.timestamp=M.lastTickedDateTime();
            }
        }
        catch(Exception sqle)
        {
            Log.errOut("MOB",sqle);
        }
        finally
        {
	        if(D!=null) DB.DBDone(D);
        }
        return members;
    }

    public void DBUpdateClan(String name, String clan, int role)
    {
        MOB M=CMLib.players().getPlayer(name);
        if(M!=null)
        {
            M.setClanID(clan);
            M.setClanRole(role);
        }
        DB.update("UPDATE CMCHAR SET  CMCLAN='"+clan+"',  CMCLRO="+role+"  WHERE CMUSERID='"+name+"'");
    }

    public void DBUpdate(MOB mob)
    {
        DBUpdateJustMOB(mob);
        PlayerStats pStats = mob.playerStats();
        if((mob.Name().length()==0)||(pStats==null)) return;
        DBUpdateItems(mob);
        DBUpdateAbilities(mob);
        pStats.setLastUpdated(System.currentTimeMillis());
    	PlayerAccount account = pStats.getAccount();
        if(account != null)
        {
        	DBUpdateAccount(account);
        	account.setLastUpdated(System.currentTimeMillis());
        }
    }

    public void DBUpdatePassword(String name, String password)
    {
    	name=CMStrings.capitalizeAndLower(name);
        DB.update("UPDATE CMCHAR SET CMPASS='"+password+"' WHERE CMUSERID='"+name+"'");
    }

    private String getPlayerStatsXML(MOB mob)
    {
        PlayerStats pstats=mob.playerStats();
        if(pstats==null) return "";
        StringBuffer pfxml=new StringBuffer(pstats.getXML());
        if(mob.numTattoos()>0)
        {
            pfxml.append("<TATTS>");
            for(int i=0;i<mob.numTattoos();i++)
                pfxml.append(mob.fetchTattoo(i)+";");
            pfxml.append("</TATTS>");
        }
        if(mob.numExpertises()>0)
        {
            pfxml.append("<EDUS>");
            for(int i=0;i<mob.numExpertises();i++)
                pfxml.append(mob.fetchExpertise(i)+";");
            pfxml.append("</EDUS>");
        }
        pfxml.append(CMLib.xml().convertXMLtoTag("IMG",mob.rawImage()));
        return pfxml.toString();
    }
    
    public void DBUpdateJustPlayerStats(MOB mob)
    {
        if(mob.Name().length()==0)
        {
            DBCreateCharacter(mob);
            return;
        }
        PlayerStats pstats=mob.playerStats();
        if(pstats==null) return;
        String pfxml=getPlayerStatsXML(mob);
        DB.update("UPDATE CMCHAR SET CMPFIL='"+pfxml.toString()+"' WHERE CMUSERID='"+mob.Name()+"'");
    }
    
    public void DBUpdateJustMOB(MOB mob)
    {
        if(mob.Name().length()==0)
        {
            DBCreateCharacter(mob);
            return;
        }
        PlayerStats pstats=mob.playerStats();
        if(pstats==null) return;
        String strStartRoomID=(mob.getStartRoom()!=null)?CMLib.map().getExtendedRoomID(mob.getStartRoom()):"";
        String strOtherRoomID=(mob.location()!=null)?CMLib.map().getExtendedRoomID(mob.location()):"";
        
        if((mob.location()!=null)
        &&(mob.location().getArea()!=null)
        &&(CMath.bset(mob.location().getArea().flags(),Area.FLAG_INSTANCE_PARENT)
        	||CMath.bset(mob.location().getArea().flags(),Area.FLAG_INSTANCE_CHILD)))
        	strOtherRoomID=strStartRoomID;
        
        String pfxml=getPlayerStatsXML(mob);
        StringBuffer cleanXML=new StringBuffer();
        cleanXML.append(CMLib.coffeeMaker().getFactionXML(mob));
        DB.update("UPDATE CMCHAR SET  CMPASS='"+pstats.password()+"'"
                +", CMCLAS='"+mob.baseCharStats().getMyClassesStr()+"'"
                +", CMSTRE="+mob.baseCharStats().getStat(CharStats.STAT_STRENGTH)
                +", CMRACE='"+mob.baseCharStats().getMyRace().ID()+"'"
                +", CMDEXT="+mob.baseCharStats().getStat(CharStats.STAT_DEXTERITY)
                +", CMCONS="+mob.baseCharStats().getStat(CharStats.STAT_CONSTITUTION)
                +", CMGEND='"+((char)mob.baseCharStats().getStat(CharStats.STAT_GENDER))+"'"
                +", CMWISD="+mob.baseCharStats().getStat(CharStats.STAT_WISDOM)
                +", CMINTE="+mob.baseCharStats().getStat(CharStats.STAT_INTELLIGENCE)
                +", CMCHAR="+mob.baseCharStats().getStat(CharStats.STAT_CHARISMA)
                +", CMHITP="+mob.baseState().getHitPoints()
                +", CMLEVL='"+mob.baseCharStats().getMyLevelsStr()+"'"
                +", CMMANA="+mob.baseState().getMana()
                +", CMMOVE="+mob.baseState().getMovement()
                +", CMALIG=-1"
                +", CMEXPE="+mob.getExperience()
                +", CMEXLV="+mob.getExpNextLevel()
                +", CMWORS='"+mob.getWorshipCharID()+"'"
                +", CMPRAC="+mob.getPractices()
                +", CMTRAI="+mob.getTrains()
                +", CMAGEH="+mob.getAgeHours()
                +", CMGOLD="+mob.getMoney()
                +", CMWIMP="+mob.getWimpHitPoint()
                +", CMQUES="+mob.getQuestPoint()
                +", CMROID='"+strStartRoomID+"||"+strOtherRoomID+"'"
                +", CMDATE='"+pstats.lastDateTime()+"'"
                +", CMCHAN="+pstats.getChannelMask()
                +", CMATTA="+mob.baseEnvStats().attackAdjustment()
                +", CMAMOR="+mob.baseEnvStats().armor()
                +", CMDAMG="+mob.baseEnvStats().damage()
                +", CMBTMP="+mob.getBitmap()
                +", CMLEIG='"+mob.getLiegeID()+"'"
                +", CMHEIT="+mob.baseEnvStats().height()
                +", CMWEIT="+mob.baseEnvStats().weight()
                +", CMPRPT='"+pstats.getPrompt()+"'"
                +", CMCOLR='"+pstats.getColorStr()+"'"
                +", CMCLAN='"+mob.getClanID()+"'"
                +", CMLSIP='"+pstats.lastIP()+"'"
                +", CMCLRO="+mob.getClanRole()
                +", CMEMAL='"+pstats.getEmail()+"'"
                +", CMPFIL='"+pfxml.toString()+"'"
                +", CMSAVE='"+mob.baseCharStats().getNonBaseStatsAsString()+"'"
                +", CMMXML='"+cleanXML.toString()+"'"
                +"  WHERE CMUSERID='"+mob.Name()+"'");
        DB.update("UPDATE CMCHAR SET CMDESC='"+mob.description()+"' WHERE CMUSERID='"+mob.Name()+"'");
    }

    private Vector getDBItemUpdateStrings(MOB mob)
    {
        HashSet done=new HashSet();
        Vector strings=new Vector();
        for(int i=0;i<mob.inventorySize();i++)
        {
            Item thisItem=mob.fetchInventory(i);
            if((thisItem!=null)&&(!done.contains(""+thisItem))&&(thisItem.savable()))
            {
            	CMLib.catalog().updateCatalogIntegrity(thisItem);
                String str="INSERT INTO CMCHIT (CMUSERID, CMITNM, CMITID, CMITTX, CMITLO, CMITWO, "
                +"CMITUR, CMITLV, CMITAB, CMHEIT"
                +") values ('"+mob.Name()+"','"+(thisItem)+"','"+thisItem.ID()+"','"+thisItem.text()+" ','"
                +((thisItem.container()!=null)?(""+thisItem.container()):"")+"',"+thisItem.rawWornCode()+","
                +thisItem.usesRemaining()+","+thisItem.baseEnvStats().level()+","+thisItem.baseEnvStats().ability()+","
                +thisItem.baseEnvStats().height()+")";
                strings.addElement(str);
                done.add(""+thisItem);
            }
        }
        return strings;
    }

    public void DBUpdateItems(MOB mob)
    {
        if(mob.Name().length()==0) return;
        Vector statements=new Vector();
        statements.addElement("DELETE FROM CMCHIT WHERE CMUSERID='"+mob.Name()+"'");
        statements.addAll(getDBItemUpdateStrings(mob));
        DB.update(CMParms.toStringArray(statements));
    }

    // this method is unused, but is a good idea of how to collect riders, followers, carts, etc.
    protected void addFollowerDependent(Environmental E, DVector list, String parent)
    {
        if(E==null) return;
        if(list.contains(E)) return;
        if((E instanceof MOB)
        &&((!((MOB)E).isMonster())||(((MOB)E).isPossessing())))
            return;
    	CMLib.catalog().updateCatalogIntegrity(E);
        String myCode=""+(list.size()-1);
        list.addElement(E,CMClass.classID(E)+"#"+myCode+parent);
        if(E instanceof Rideable)
        {
            Rideable R=(Rideable)E;
            for(int r=0;r<R.numRiders();r++)
                addFollowerDependent(R.fetchRider(r),list,"@"+myCode+"R");
        }
        if(E instanceof Container)
        {
            Container C=(Container)E;
            Vector contents=C.getContents();
            for(int c=0;c<contents.size();c++)
                addFollowerDependent((Environmental)contents.elementAt(c),list,"@"+myCode+"C");
        }

    }

    public void DBUpdateFollowers(MOB mob)
    {
        if((mob==null)||(mob.Name().length()==0)) return;
        Vector statements=new Vector();
        statements.addElement("DELETE FROM CMCHFO WHERE CMUSERID='"+mob.Name()+"'");
        for(int f=0;f<mob.numFollowers();f++)
        {
            MOB thisMOB=mob.fetchFollower(f);
            if((thisMOB!=null)&&(thisMOB.isMonster())&&(!thisMOB.isPossessing()))
            {
            	CMLib.catalog().updateCatalogIntegrity(thisMOB);
                String str="INSERT INTO CMCHFO (CMUSERID, CMFONM, CMFOID, CMFOTX, CMFOLV, CMFOAB"
                +") values ('"+mob.Name()+"',"+f+",'"+CMClass.classID(thisMOB)+"','"+thisMOB.text()+" ',"
                +thisMOB.baseEnvStats().level()+","+thisMOB.baseEnvStats().ability()+")";
                statements.addElement(str);
            }
        }
        DB.update(CMParms.toStringArray(statements));
    }

    public void DBDelete(MOB mob)
    {
        if(mob.Name().length()==0) return;
        Vector channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.PLAYERPURGES);
        for(int i=0;i<channels.size();i++)
            CMLib.commands().postChannel((String)channels.elementAt(i),mob.getClanID(),mob.Name()+" has just been deleted.",true);
        CMLib.coffeeTables().bump(mob,CoffeeTableRow.STAT_PURGES);
        DB.update("DELETE FROM CMCHAR WHERE CMUSERID='"+mob.Name()+"'");
        while(mob.inventorySize()>0)
        {
            Item thisItem=mob.fetchInventory(0);
            if(thisItem!=null)
            {
                thisItem.setContainer(null);
                mob.delInventory(thisItem);
            }
        }
        DBUpdateItems(mob);
        while(mob.numFollowers()>0)
        {
            MOB follower=mob.fetchFollower(0);
            if(follower!=null) follower.setFollowing(null);
        }
        DBUpdateFollowers(mob);
        while(mob.numLearnedAbilities()>0)
        {
            Ability A=mob.fetchAbility(0);
            if(A!=null) mob.delAbility(A);
        }
        DBUpdateAbilities(mob);
        CMLib.database().DBDeletePlayerJournals(mob.Name());
        CMLib.database().DBDeletePlayerData(mob.Name());
        PlayerStats pstats = mob.playerStats();
        if(pstats!=null)
        {
        	PlayerAccount account = pstats.getAccount();
        	if(account != null)
        	{
        		account.delPlayer(mob);
        		DBUpdateAccount(account);
        		account.setLastUpdated(System.currentTimeMillis());
        	}
        }
        for(int q=0;q<CMLib.quests().numQuests();q++)
        {
        	Quest Q=CMLib.quests().fetchQuest(q);
        	if(Q.wasWinner(mob.Name()))
        		Q.declareWinner("-"+mob.Name());
        }
    }

    public void DBUpdateAbilities(MOB mob)
    {
        if(mob.Name().length()==0) return;
        Vector statements=new Vector();
        statements.addElement("DELETE FROM CMCHAB WHERE CMUSERID='"+mob.Name()+"'");
        HashSet H=new HashSet();
        for(int a=0;a<mob.numLearnedAbilities();a++)
        {
            Ability thisAbility=mob.fetchAbility(a);
            if((thisAbility!=null)&&(thisAbility.savable()))
            {
                int proficiency=thisAbility.proficiency();
                Ability effectA=mob.fetchEffect(thisAbility.ID());
                if(effectA!=null)
                {
                    if((effectA.savable())&&(!effectA.canBeUninvoked())&&(!effectA.isAutoInvoked())) proficiency=proficiency-200;
                }
                H.add(thisAbility.ID());
                String str="INSERT INTO CMCHAB (CMUSERID, CMABID, CMABPF,CMABTX"
                +") values ('"+mob.Name()+"','"+thisAbility.ID()+"',"+proficiency+",'"+thisAbility.text()+"')";
                statements.addElement(str);
            }
        }
        for(int a=0;a<mob.numEffects();a++)
        {
            Ability thisAffect=mob.fetchEffect(a);
            if((thisAffect!=null)&&(!H.contains(thisAffect.ID()))&&(thisAffect.savable())&&(!thisAffect.canBeUninvoked()))
            {
                String str="INSERT INTO CMCHAB (CMUSERID, CMABID, CMABPF,CMABTX"
                +") values ('"+mob.Name()+"','"+thisAffect.ID()+"',"+Integer.MAX_VALUE+",'"+thisAffect.text()+"')";
                statements.addElement(str);
            }
        }
        for(int b=0;b<mob.numBehaviors();b++)
        {
            Behavior thisBehavior=mob.fetchBehavior(b);
            if((thisBehavior!=null)&&(thisBehavior.isSavable()))
            {
                String str="INSERT INTO CMCHAB (CMUSERID, CMABID, CMABPF,CMABTX"
                +") values ('"+mob.Name()+"','"+thisBehavior.ID()+"',"+(Integer.MIN_VALUE+1)+",'"+thisBehavior.getParms()+"'"
                +")";
                statements.addElement(str);
            }
        }
        String scriptStuff = CMLib.coffeeMaker().getGenScripts(mob,true);
        if(scriptStuff.length()>0)
        {
            String str="INSERT INTO CMCHAB (CMUSERID, CMABID, CMABPF,CMABTX"
            +") values ('"+mob.Name()+"','ScriptingEngine',"+(Integer.MIN_VALUE+1)+",'"+scriptStuff+"'"
            +")";
            statements.addElement(str);
        }

        DB.update(CMParms.toStringArray(statements));
    }

    public void DBCreateCharacter(MOB mob)
    {
        if(mob.Name().length()==0) return;
        PlayerStats pstats=mob.playerStats();
        if(pstats==null) return;
        DB.update("INSERT INTO CMCHAR (CMUSERID, CMPASS, CMCLAS, CMRACE, CMGEND "
                +") VALUES ('"+mob.Name()+"','"+pstats.password()+"','"+mob.baseCharStats().getMyClassesStr()
                +"','"+mob.baseCharStats().getMyRace().ID()+"','"+((char)mob.baseCharStats().getStat(CharStats.STAT_GENDER))
                +"')");
    	PlayerAccount account = pstats.getAccount();
        if(account != null)
        {
        	account.addNewPlayer(mob);
        	DBUpdateAccount(account);
        	account.setLastUpdated(System.currentTimeMillis());
        }
    }

    public void DBUpdateAccount(PlayerAccount account)
    {
    	if(account == null) return;
    	String characters = CMParms.toSemicolonList(account.getPlayers());
        DB.update("UPDATE CMACCT SET CMPASS='"+account.password()+"',  CMCHRS='"+characters+"',  CMAXML='"+account.getXML()+"'  WHERE CMANAM='"+account.accountName()+"'");
    }

    public void DBDeleteAccount(PlayerAccount account)
    {
    	if(account == null) return;
        DB.update("DELETE FROM CMACCT WHERE CMANAM='"+account.accountName()+"'");
    }

    public void DBCreateAccount(PlayerAccount account)
    {
    	if(account == null) return;
    	account.setAccountName(CMStrings.capitalizeAndLower(account.accountName()));
    	String characters = CMParms.toSemicolonList(account.getPlayers());
        DB.update("INSERT INTO CMACCT (CMANAM, CMPASS, CMCHRS, CMAXML) VALUES ('"+account.accountName()+"','"+account.password()+"','"+characters+"','"+account.getXML()+"')");
    }
    
    public PlayerAccount MakeAccount(String username, ResultSet R) throws SQLException
    {
    	PlayerAccount account = null;
    	account = (PlayerAccount)CMClass.getCommon("DefaultPlayerAccount");
        String password=DB.getRes(R,"CMPASS");
        String chrs=DB.getRes(R,"CMCHRS");
        String xml=DB.getRes(R,"CMAXML");
        Vector<String> names = new Vector<String>();
        if(chrs!=null) names.addAll(CMParms.parseSemicolons(chrs,true));
        account.setAccountName(CMStrings.capitalizeAndLower(username));
        account.setPassword(password);
        account.setPlayerNames(names);
        account.setXML(xml);
        return account;
    }

    public PlayerAccount DBReadAccount(String Login)
    {
        DBConnection D=null;
        PlayerAccount account = null;
        try
        {
        	// why in the hell is this a memory scan?
        	// case insensitivity from databases configured almost
        	// certainly by amateurs is the answer. That, and fakedb 
        	// doesn't understand 'LIKE'
            D=DB.DBFetch();
            ResultSet R=D.query("SELECT * FROM CMACCT WHERE CMANAM='"+CMStrings.capitalizeAndLower(Login)+"'");
            if(R!=null) while(R.next())
            {
                String username=DB.getRes(R,"CMANAM");
                if(Login.equalsIgnoreCase(username))
                	account = MakeAccount(username,R);
            }
        }
        catch(Exception sqle)
        {
            Log.errOut("MOB",sqle);
        }
        if(D!=null) DB.DBDone(D);
        return account;
    }
    
    public Vector<PlayerAccount> DBListAccounts(String mask)
    {
        DBConnection D=null;
    	PlayerAccount account = null;
    	Vector<PlayerAccount> accounts = new Vector<PlayerAccount>();
    	if(mask!=null) mask=mask.toLowerCase();
        try
        {
        	// why in the hell is this a memory scan?
        	// case insensitivity from databases configured almost
        	// certainly by amateurs is the answer. That, and fakedb 
        	// doesn't understand 'LIKE'
            D=DB.DBFetch();
            ResultSet R=D.query("SELECT * FROM CMACCT");
            if(R!=null) while(R.next())
            {
                String username=DB.getRes(R,"CMANAM");
                if((mask==null)||(mask.length()==0)||(username.toLowerCase().indexOf(mask)>=0))
                {
                	account = MakeAccount(username,R);
                	accounts.add(account);
                }
            }
        }
        catch(Exception sqle)
        {
            Log.errOut("MOB",sqle);
        }
        if(D!=null) DB.DBDone(D);
        return accounts;
    }
    
    public PlayerLibrary.ThinnerPlayer DBUserSearch(String Login)
    {
        DBConnection D=null;
        String buf=null;
        PlayerLibrary.ThinnerPlayer thinPlayer = null;
        try
        {
        	// why in the hell is this a memory scan?
        	// case insensitivity from databases configured almost
        	// certainly by amateurs is the answer. That, and fakedb 
        	// doesn't understand 'LIKE'
            D=DB.DBFetch();
            ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMUSERID='"+CMStrings.capitalizeAndLower(Login)+"'");
            if(R!=null) while(R.next())
            {
                String username=DB.getRes(R,"CMUSERID");
                thinPlayer = new PlayerLibrary.ThinnerPlayer();
                String password=DB.getRes(R,"CMPASS");
                String email=DB.getRes(R,"CMEMAL");
                thinPlayer.name=username;
                thinPlayer.password=password;
                thinPlayer.email=email;
                // Acct Exp Code
                buf=DBConnections.getRes(R,"CMPFIL");
            }
        }catch(Exception sqle)
        {
            Log.errOut("MOB",sqle);
        }
        if(D!=null) DB.DBDone(D);
        if((buf!=null)&&(thinPlayer!=null))
        {
        	PlayerAccount acct = null;
        	thinPlayer.accountName = CMLib.xml().returnXMLValue(buf,"ACCOUNT");
        	if((thinPlayer.accountName!=null)&&(thinPlayer.accountName.length()>0))
        		acct = CMLib.players().getLoadAccount(thinPlayer.accountName);
        	if((acct != null)&&(CMProps.getIntVar(CMProps.SYSTEMI_COMMONACCOUNTSYSTEM)>1))
        		thinPlayer.expiration=acct.getAccountExpiration();
        	else
            if(CMLib.xml().returnXMLValue(buf,"ACCTEXP").length()>0)
            	thinPlayer.expiration=CMath.s_long(CMLib.xml().returnXMLValue(buf,"ACCTEXP"));
            else
            {
                Calendar C=Calendar.getInstance();
                C.add(Calendar.DATE,CMProps.getIntVar(CMProps.SYSTEMI_TRIALDAYS));
                thinPlayer.expiration=C.getTimeInMillis();
            }
        }
        return thinPlayer;
    }

    public String[] DBFetchEmailData(String name)
    {
        String[] data=new String[2];
        for(Enumeration e=CMLib.players().players();e.hasMoreElements();)
        {
            MOB M=(MOB)e.nextElement();
            if((M.Name().equalsIgnoreCase(name))&&(M.playerStats()!=null))
            {
                data[0]=M.playerStats().getEmail();
                data[1]=""+((M.getBitmap()&MOB.ATT_AUTOFORWARD)==MOB.ATT_AUTOFORWARD);
                return data;
            }
        }
        DBConnection D=null;
        try
        {
            D=DB.DBFetch();
            ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMUSERID='"+name+"'");
            if(R!=null) while(R.next())
            {
                // String username=DB.getRes(R,"CMUSERID");
                int btmp=CMath.s_int(DB.getRes(R,"CMBTMP"));
                String temail=DB.getRes(R,"CMEMAL");
                DB.DBDone(D);
                data[0]=temail;
                data[1]=""+((btmp&MOB.ATT_AUTOFORWARD)==MOB.ATT_AUTOFORWARD);
                return data;
            }
        }catch(Exception sqle)
        {
            Log.errOut("MOB",sqle);
        }
        if(D!=null) DB.DBDone(D);
        return null;
    }

    public String DBEmailSearch(String email)
    {
        DBConnection D=null;
        for(Enumeration e=CMLib.players().players();e.hasMoreElements();)
        {
            MOB M=(MOB)e.nextElement();
            if((M.playerStats()!=null)&&(M.playerStats().getEmail().equalsIgnoreCase(email))) return M.Name();
        }
        try
        {
            D=DB.DBFetch();
            ResultSet R=D.query("SELECT * FROM CMCHAR");
            if(R!=null) while(R.next())
            {
                String username=DB.getRes(R,"CMUSERID");
                String temail=DB.getRes(R,"CMEMAL");
                if(temail.equalsIgnoreCase(email))
                {
                    DB.DBDone(D);
                    return username;
                }
            }
        }catch(Exception sqle)
        {
            Log.errOut("MOB",sqle);
        }
        if(D!=null) DB.DBDone(D);
        return null;
    }
}
