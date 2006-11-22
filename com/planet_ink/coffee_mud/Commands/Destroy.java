package com.planet_ink.coffee_mud.Commands;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

import java.util.*;
import java.io.IOException;

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
public class Destroy extends BaseItemParser
{
	public Destroy(){}

	private String[] access={getScr("Destroy","cmd1")};
	public String[] getAccessWords(){return access;}

	public boolean errorOut(MOB mob)
	{
		mob.tell(getScr("Destroy","noallowed"));
		return false;
	}
	
	public boolean mobs(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell(getScr("Destroy","baddesmob"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubspowspell"));
			return false;
		}

		String mobID=CMParms.combine(commands,2);
		boolean allFlag=((String)commands.elementAt(2)).equalsIgnoreCase(getScr("Destroy","all"));
		if(mobID.toUpperCase().startsWith(getScr("Destroy","alldot"))){ allFlag=true; mobID=getScr("Destroy","allup")+mobID.substring(4);}
		if(mobID.toUpperCase().endsWith(getScr("Destroy","dotall"))){ allFlag=true; mobID=getScr("Destroy","allup")+mobID.substring(0,mobID.length()-4);}
		MOB deadMOB=mob.location().fetchInhabitant(mobID);
		boolean doneSomething=false;
		while(deadMOB!=null)
		{
			if(!deadMOB.isMonster())
			{
				mob.tell(deadMOB.name()+getScr("Destroy","isplayer"));
				if(!doneSomething)
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubspowspell"));
				return false;
			}
			doneSomething=true;
			mob.location().showHappens(CMMsg.MSG_OK_VISUAL,deadMOB.name()+getScr("Destroy","vanishes"));
			Log.sysOut("Mobs",mob.Name()+getScr("Destroy","destroyedmob")+deadMOB.Name()+".");
            deadMOB.destroy();
            mob.location().delInhabitant(deadMOB);
			deadMOB=mob.location().fetchInhabitant(mobID);
			if(!allFlag) break;
		}
		if(!doneSomething)
		{
			mob.tell(getScr("Destroy","nosee",mobID));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubspowspell"));
			return false;
		}
		return true;
	}


	public static boolean players(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(getScr("Destroy","baddesuser"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubspowspell"));
			return false;
		}

		MOB deadMOB=CMClass.getMOB("StdMOB");
		boolean found=CMLib.database().DBUserSearch(deadMOB,CMParms.combine(commands,2));

		if(!found)
		{
			mob.tell(getScr("Destroy","nouser",CMParms.combine(commands,2)));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubspowspell"));
            deadMOB.destroy();
			return false;
		}

		if(mob.session().confirm(getScr("Destroy","oblituser",deadMOB.Name()),"N"))
		{
			CMLib.map().obliteratePlayer(deadMOB,false);
			mob.tell(getScr("Destroy","useredad",CMParms.combine(commands,2)));
			Log.sysOut("Mobs",mob.Name()+getScr("Destroy","destroyeduser")+deadMOB.Name()+".");
            deadMOB.destroy();
			return true;
		}
        deadMOB.destroy();
		return true;
	}
    
    
    public Thread findThreadGroup(String threadName,ThreadGroup tGroup)
    {
        int ac = tGroup.activeCount();
        int agc = tGroup.activeGroupCount();
        Thread tArray[] = new Thread [ac+1];
        ThreadGroup tgArray[] = new ThreadGroup [agc+1];

        tGroup.enumerate(tArray,false);
        tGroup.enumerate(tgArray,false);

        for (int i = 0; i<ac; ++i)
        {
            if (tArray[i] != null)
            {
                if((tArray[i] instanceof TickableGroup)
                &&(((TickableGroup)tArray[i]).lastTicked()!=null)
                &&(((TickableGroup)tArray[i]).lastTicked().getTickStatus()==0))
                    continue;
                if((tArray[i] instanceof Tickable)
                &&(((Tickable)tArray[i]).getTickStatus()==0))
                    continue;
                
                if(tArray[i].getName().equalsIgnoreCase(threadName))
                    return tArray[i];
            }
        }

        if (agc > 0)
        {
            for (int i = 0; i<agc; ++i)
            {
                if (tgArray[i] != null)
                {
                    Thread t=findThreadGroup(threadName,tgArray[i]);
                    if(t!=null) return t;
                }
            }
        }
        return null;
    }


    public Thread findThread(String threadName)
    {
        Thread t=null;
        try
        {
            ThreadGroup topTG = Thread.currentThread().getThreadGroup();
            while (topTG != null && topTG.getParent() != null)
                topTG = topTG.getParent();
            if (topTG != null)
                t=findThreadGroup(threadName,topTG);

        }
        catch (Exception e)
        {
        }
        return t;

    }

	public void rooms(MOB mob, Vector commands)
		throws IOException
	{
		String thecmd=((String)commands.elementAt(0)).toLowerCase();
		if(commands.size()<3)
		{
			if(thecmd.equalsIgnoreCase(getScr("Destroy","cmdunlink")))
				mob.tell(getScr("Destroy","instunlink"));
			else
				mob.tell(getScr("Destroy","baddestroom"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubspowspell"));
			return;
		}
		boolean confirmed=false;
		if((commands.size()>3))
		{
			if(((String)commands.lastElement()).equalsIgnoreCase(getScr("Destroy","cmdconfirmed")))
			{
				commands.removeElementAt(commands.size()-1);
				confirmed=true;
			}
		}
		String roomdir=CMParms.combine(commands,2);
		int direction=Directions.getGoodDirectionCode(roomdir);
		Room deadRoom=null;
		if(!thecmd.equalsIgnoreCase(getScr("Destroy","cmdunlink")))
			deadRoom=CMLib.map().getRoom(roomdir);
		if((deadRoom==null)&&(direction<0))
		{
			if(thecmd.equalsIgnoreCase(getScr("Destroy","cmdunlink")))
				mob.tell(getScr("Destroy","baddir")+Directions.DIRECTIONS_DESC+").\n\r");
			else
				mob.tell(getScr("Destroy","badroomid")+Directions.DIRECTIONS_DESC+").\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubspowspell"));
			return;
		}
		else
		if(mob.isMonster())
		{
			mob.tell(getScr("Destroy","sorry"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubspowspell"));
			return;

		}
		if(deadRoom!=null)
		{
			if(!CMSecurity.isAllowed(mob,deadRoom,"CMDROOMS"))
			{
				mob.tell(getScr("Destroy","sorryroom"));
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubspowspell"));
				return;
			}
			if(mob.location()==deadRoom)
			{
				mob.tell(getScr("Destroy","leaveroomnow"));
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubspowspell"));
				return;
			}

			if(!confirmed)
				if(!mob.session().confirm(getScr("Destroy","confirmdesroom",deadRoom.roomID()),"N")) return;
			CMLib.map().obliterateRoom(deadRoom);
			mob.tell(getScr("Destroy","soundofdestruction"));
			mob.location().showOthers(mob,null,CMMsg.MSG_NOISE,getScr("Destroy","soundofdestruction"));
			Log.sysOut("Rooms",mob.Name()+getScr("Destroy","destroyedroom")+deadRoom.roomID()+".");
		}
		else
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDEXITS"))
			{
				errorOut(mob);
				return;
			}
			Room unRoom=mob.location().rawDoors()[direction];
			if((unRoom!=null)&&(unRoom.getGridParent()!=null))
				unRoom=unRoom.getGridParent();
			if((mob.location().getGridParent()!=null)
			&&(!(mob.location() instanceof GridLocale)))
			{
				GridLocale GL=mob.location().getGridParent();
				Vector outer=GL.outerExits();
				int myX=GL.getGridChildX(mob.location());
				int myY=GL.getGridChildY(mob.location());
				for(int v=0;v<outer.size();v++)
				{
					WorldMap.CrossExit CE=(WorldMap.CrossExit)outer.elementAt(v);
					if((CE.out)
					&&(CE.x==myX)
					&&(CE.y==myY)
					&&(CE.dir==direction))
					   GL.delOuterExit(CE);
				}
				CMLib.database().DBUpdateExits(GL);
				mob.location().rawDoors()[direction]=null;
				mob.location().rawExits()[direction]=null;
			}
			else
			{
				mob.location().rawDoors()[direction]=null;
				mob.location().rawExits()[direction]=null;
				CMLib.database().DBUpdateExits(mob.location());
			}
			if(unRoom instanceof GridLocale)
			{
				GridLocale GL=(GridLocale)unRoom;
				Vector outer=GL.outerExits();
				for(int v=0;v<outer.size();v++)
				{
					WorldMap.CrossExit CE=(WorldMap.CrossExit)outer.elementAt(v);
					if((!CE.out)
					&&(CE.dir==direction)
					&&(CE.destRoomID.equalsIgnoreCase(CMLib.map().getExtendedRoomID(mob.location()))))
					   GL.delOuterExit(CE);
				}
				CMLib.database().DBUpdateExits(GL);
			}
			mob.location().getArea().fillInAreaRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Destroy","wallfalls")+Directions.getInDirectionName(direction)+".");
			Log.sysOut("Rooms",mob.Name()+getScr("Destroy","unlinkeddir",Directions.getDirectionName(direction))+mob.location().roomID()+".");
		}
	}

	public void exits(MOB mob, Vector commands)
	{
		if(mob.location().roomID().equals(""))
		{
			mob.tell(getScr("Destroy","notingridchild"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubspowspell"));
			return;
		}
		if(commands.size()<3)
		{
			mob.tell(getScr("Destroy","baddesexit"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubsspell"));
			return;
		}

		int direction=Directions.getGoodDirectionCode(((String)commands.elementAt(2)));
		if(direction<0)
		{
			mob.tell(getScr("Destroy","baddir2")+Directions.DIRECTIONS_DESC+".\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubsspell"));
			return;
		}
		if(mob.isMonster())
		{
			mob.tell(getScr("Destroy","sorry"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubsspell"));
			return;

		}
		mob.location().rawExits()[direction]=null;
		CMLib.database().DBUpdateExits(mob.location());
		mob.location().getArea().fillInAreaRoom(mob.location());
		if(mob.location() instanceof GridLocale)
			((GridLocale)mob.location()).buildGrid();
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Destroy","wallfalls")+Directions.getInDirectionName(direction)+".");
		Log.sysOut("Exits",mob.location().roomID()+getScr("Destroy","exitsdestroyed")+mob.Name()+".");
	}

	public boolean items(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell(getScr("Destroy","baddesitem"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubsspell"));
			return false;
		}
		
		String itemID=CMParms.combine(commands,2);
		MOB srchMob=mob;
		Room srchRoom=mob.location();
		int x=itemID.indexOf("@");
		if(x>0)
		{
			String rest=itemID.substring(x+1).trim();
			itemID=itemID.substring(0,x).trim();
			if(rest.equalsIgnoreCase(getScr("Destroy","cmdroom")))
				srchMob=null;
			else
			if(rest.length()>0)
			{
				MOB M=srchRoom.fetchInhabitant(rest);
				if(M==null)
				{
					mob.tell(getScr("Destroy","mobnotfound",rest));
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubsspell"));
					return false;
				}
				srchMob=M;
				srchRoom=null;
			}
		}
		
		boolean allFlag=((String)commands.elementAt(2)).equalsIgnoreCase(getScr("Destroy","all"));
		if(itemID.toUpperCase().startsWith(getScr("Destroy","alldot"))){ allFlag=true; itemID=getScr("Destroy","allup")+itemID.substring(4);}
		if(itemID.toUpperCase().endsWith(getScr("Destroy","dotall"))){ allFlag=true; itemID=getScr("Destroy","allup")+itemID.substring(0,itemID.length()-4);}
		boolean doneSomething=false;
		Item deadItem=null;
		if(deadItem==null) deadItem=(srchRoom==null)?null:srchRoom.fetchItem(null,itemID);
		if((!allFlag)&&(deadItem==null)) deadItem=(srchMob==null)?null:srchMob.fetchInventory(null,itemID);
		while(deadItem!=null)
		{
			mob.location().recoverRoomStats();
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,deadItem.name()+getScr("Destroy","disintegrates"));
			doneSomething=true;
			Log.sysOut("Items",mob.Name()+getScr("Destroy","destroyeditem")+deadItem.name()+".");
            deadItem.destroy();
            mob.location().delItem(deadItem);
			deadItem=null;
			if(!allFlag) deadItem=(srchMob==null)?null:srchMob.fetchInventory(null,itemID);
			if(deadItem==null) deadItem=(srchRoom==null)?null:srchRoom.fetchItem(null,itemID);
			if(!allFlag) break;
		}
		if(!doneSomething)
		{
			mob.tell(getScr("Destroy","nosee",itemID));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubsspell"));
			return false;
		}
		return true;
	}


	public void areas(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(getScr("Destroy","baddesarea"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubsthunderspell"));
			return;
		}
		boolean confirmed=false;
		if((commands.size()>3))
		{
			if(((String)commands.lastElement()).equalsIgnoreCase(getScr("Destroy","cmdconfirmed")))
			{
				commands.removeElementAt(commands.size()-1);
				confirmed=true;
			}
		}

		String areaName=CMParms.combine(commands,2);
		if(CMLib.map().getArea(areaName)==null)
		{
			mob.tell(getScr("Destroy","nosucharea")+areaName+"'");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubsthunderspell"));
			return;
		}
		Area A=CMLib.map().getArea(areaName);
		Room R=A.getRandomProperRoom();
		if((R!=null)&&(!CMSecurity.isAllowed(mob,R,"CMDAREAS")))
		{
			errorOut(mob);
			return;
		}
			
		if(!confirmed);
		if(mob.session().confirm(getScr("Destroy","obliarea",areaName),"N"))
		{
			if(mob.location().getArea().Name().equalsIgnoreCase(areaName))
			{
				mob.tell(getScr("Destroy","leavearea"));
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubsthunderspell"));
				return;
			}
			confirmed=true;
		}
		CMLib.map().obliterateArea(areaName);
		if(confirmed)
		{
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Destroy","boomheard"));
			Log.sysOut("Rooms",mob.Name()+getScr("Destroy","destroyedarea")+areaName+".");
		}
	}

	public boolean races(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell(getScr("Destroy","baddesrace"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubsspell"));
			return false;
		}

		String raceID=CMParms.combine(commands,2);
		Race R=CMClass.getRace(raceID);
		if(R==null)
		{
			mob.tell("'"+raceID+getScr("Destroy","invalidrace"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubsspell"));
			return false;
		}
		if(!(R.isGeneric()))
		{
			mob.tell("'"+R.ID()+getScr("Destroy","notgenrace"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubsspell"));
			return false;
		}
		try
		{
			for(Enumeration e=CMLib.map().rooms();e.hasMoreElements();)
			{
				Room room=(Room)e.nextElement();
				for(int i=0;i<room.numInhabitants();i++)
				{
					MOB M=room.fetchInhabitant(i);
					if(M.baseCharStats().getMyRace()==R)
					{
						mob.tell(getScr("Destroy","mobfound",M.Name(),room.roomID()));
						mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubsspell"));
						return false;
					}
				}
			}
	    }catch(NoSuchElementException e){}
		CMClass.delRace(R);
		CMLib.database().DBDeleteRace(R.ID());
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Destroy","diversitydown"));
		return true;
	}

    protected boolean findRemoveFromFile(CMFile F, String match)
    {
        boolean removed=false;
        StringBuffer text=F.textUnformatted();
        int x=text.toString().toUpperCase().indexOf(match.toUpperCase());
        while(x>=0)
        {
            if(((x==0)||(!Character.isLetterOrDigit(text.charAt(x-1))))
            &&(text.substring(x+match.length()).trim().startsWith("=")))
            {
                int zb1=text.lastIndexOf("\n",x);
                int zb2=text.lastIndexOf("\r",x);
                int zb=(zb2>zb1)?zb2:zb1;
                if(zb<0) zb=0; else zb++;
                int ze1=text.indexOf("\n",x);
                int ze2=text.indexOf("\r",x);
                int ze=ze2+1;
                if((ze1>zb)&&(ze1==ze2+1)) ze=ze1+1; 
                else
                if((ze2<0)&&(ze1>0)) ze=ze1+1;
                if(ze<=0) ze=text.length();
                if(!text.substring(zb).trim().startsWith("#"))
                {
                    text.delete(zb,ze);
                    x=-1;
                    removed=true;
                }
            }
            x=text.toString().toUpperCase().indexOf(match.toUpperCase(),x+1);
        }
        if(removed)
            F.saveRaw(text);
        return removed;
    }
    
	public boolean components(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell(getScr("Destroy","baddescomp"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubsspell"));
			return false;
		}

		String classID=CMParms.combine(commands,2);
		if(CMLib.ableMapper().getAbilityComponentMap().get(classID.toUpperCase())==null)
		{
			mob.tell("'"+classID+getScr("Destroy","nocomp"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubsspell"));
			return false;
		}
		CMFile F=new CMFile(Resources.makeFileResourceName("skills/components.txt"),null,true);
		if(F!=null)
		{
            boolean removed=this.findRemoveFromFile(F,classID);
			if(removed)
			{
				CMLib.ableMapper().getAbilityComponentMap().remove(classID.toUpperCase());
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Destroy","skilldown"));
			}
		}
		return true;
	}
    
    public boolean expertises(MOB mob, Vector commands)
    {
        if(commands.size()<3)
        {
            mob.tell(getScr("Destroy","baddesexper"));
            mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubsspell"));
            return false;
        }

        String classID=CMParms.combine(commands,2);
        CMFile F=new CMFile(Resources.makeFileResourceName("skills/expertises.txt"),null,true);
        if(F!=null)
        {
            boolean removed=this.findRemoveFromFile(F,classID);
            if(removed)
            {
                Resources.removeResource("skills/expertises.txt");
                mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Destroy","powerdown"));
                CMLib.expertises().recompileExpertises();
            }
        }
        return true;
    }
    
    public boolean titles(MOB mob, Vector commands)
    {
        mob.tell(getScr("Destroy","instdesttitle"));
        mob.tell(getScr("Destroy","instdesttitle2") +
                 getScr("Destroy","instdesttitle3"));
        if(commands.size()<3)
        {
            mob.tell(getScr("Destroy","baddestitle"));
            mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubsspell"));
            return false;
        }

        String classID=CMParms.combine(commands,2);
        if(!CMLib.login().isExistingAutoTitle(classID))
        {
            mob.tell("'"+classID+getScr("Destroy","notitle"));
            mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubsspell"));
            return false;
        }
        for(Enumeration e=CMLib.map().players();e.hasMoreElements();)
        {
            MOB M=(MOB)e.nextElement();
            if((M.playerStats()!=null)&&(M.playerStats().getTitles().contains(classID)))
            {
                M.playerStats().getTitles().remove(classID);
                if(!CMLib.flags().isInTheGame(M,true))
                    CMLib.database().DBUpdatePlayerStatsOnly(M);
            }
        }
        CMFile F=new CMFile(Resources.makeFileResourceName("titles.txt"),null,true);
        if(F!=null)
        {
            boolean removed=findRemoveFromFile(F,classID);
            if(removed)
            {
                mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Destroy","presitgedown"));
                Resources.removeResource("titles.txt");
                CMLib.login().reloadAutoTitles();
            }
        }
        return true;
    }
    
	public boolean classes(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell(getScr("Destroy","baddesclass"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubsspell"));
			return false;
		}

		String classID=CMParms.combine(commands,2);
		CharClass C=CMClass.getCharClass(classID);
		if(C==null)
		{
			mob.tell("'"+classID+getScr("Destroy","invalidclass"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubsspell"));
			return false;
		}
		if(!(C.isGeneric()))
		{
			mob.tell("'"+C.ID()+getScr("Destroy","notgenrace"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubsspell"));
			return false;
		}
		CMClass.delCharClass(C);
		CMLib.database().DBDeleteClass(C.ID());
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Destroy","employdown"));
		return true;
	}

	public boolean abilities(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell(getScr("Destroy","baddesable"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubsspell"));
			return false;
		}

		String classID=CMParms.combine(commands,2);
		Ability A=CMClass.getAbility(classID);
		if(A==null)
		{
			mob.tell("'"+classID+getScr("Destroy","invalidable"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubsspell"));
			return false;
		}
		if(!(A.isGeneric()))
		{
			mob.tell("'"+A.ID()+getScr("Destroy","notgenrace"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubsspell"));
			return false;
		}
        Object O=CMClass.getClass(A.ID());
        if(!(O instanceof Ability))
        {
            mob.tell("'"+classID+getScr("Destroy","nodelclass")+CMClass.rawClassName(O)+".");
            mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubsspell"));
            return false;
        }
		CMClass.delClass(O);
		CMLib.database().DBDeleteAbility(A.ID());
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Destroy","worldskilldown"));
		return true;
	}

	public void socials(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.session().rawPrintln(getScr("Destroy","baddessocial"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubspowspell"));
			return;
		}
		else
		if(commands.size()>3)
		{
			String therest=CMParms.combine(commands,3);
			if(!((therest.equalsIgnoreCase("<T-NAME>")
                    ||therest.equalsIgnoreCase("SELF")
                    ||therest.equalsIgnoreCase("ALL"))))
			{
				mob.session().rawPrintln(getScr("Destroy","baddessocial2"));
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubspowspell"));
				return;
			}
		}

		Social soc2=CMLib.socials().FetchSocial(CMParms.combine(commands,2).toUpperCase(),true);
		if(soc2==null)
		{
			mob.tell(getScr("Destroy","nosocexist"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubspowspell"));
			return;
		}
		if(mob.session().confirm(getScr("Destroy","delsocialyn"),"N"))
		{
			CMLib.socials().remove(soc2.name());
			Resources.removeResource("SOCIALS LIST");
			CMLib.socials().save(mob);
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Destroy","happydown"));
            Log.sysOut("SysopSocials",mob.Name()+getScr("Destroy","destroyedsocial")+soc2.name()+".");
		}
		else
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,getScr("Destroy","happyup"));
	}
	
	public static boolean destroyItem(MOB mob, Environmental dropThis, boolean quiet, boolean optimize)
	{
		String msgstr=null;
		int material=(dropThis instanceof Item)?((Item)dropThis).material():-1;
		if(!quiet)
		switch(material&RawMaterial.MATERIAL_MASK)
		{
		case RawMaterial.MATERIAL_LIQUID:
			msgstr=getScr("Destroy","poursout");
			break;
		case RawMaterial.MATERIAL_PAPER:
			msgstr=getScr("Destroy","tearsup");
			break;
		case RawMaterial.MATERIAL_GLASS:
			msgstr=getScr("Destroy","smashes");
			break;
		default:
			return false;
		}
		CMMsg msg=CMClass.getMsg(mob,dropThis,null,CMMsg.MSG_NOISYMOVEMENT,(optimize?CMMsg.MASK_OPTIMIZE:0)|CMMsg.MASK_ALWAYS|CMMsg.MSG_DEATH,CMMsg.MSG_NOISYMOVEMENT,msgstr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			return true;
		}
		if(dropThis instanceof Coins)
		    ((Coins)dropThis).putCoinsBack();
        if(dropThis instanceof RawMaterial)
        	((RawMaterial)dropThis).rebundle();
		return false;
	}

	
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if((!CMSecurity.isAllowedStartsWith(mob,"CMD"))
		&&(!CMSecurity.isAllowedStartsWith(mob,mob.location(),"KILL"))
		&&(!CMSecurity.isAllowed(mob,mob.location(),"BAN"))
		&&(!CMSecurity.isAllowed(mob,mob.location(),"NOPURGE")))
		{
			commands.removeElementAt(0);
			if(commands.size()==0)
			{
				mob.tell(getScr("Destroy","errmsg"));
				return false;
			}
			if(mob.location().fetchInhabitant(CMParms.combine(commands,0))!=null)
			{
				Command C=CMClass.getCommand("Kill");
				commands.insertElementAt(getScr("Destroy","cmdkill"),0);
				if(C!=null) C.execute(mob,commands);
				return false;
			}

			Vector V=new Vector();
			int maxToDrop=Integer.MAX_VALUE;
			
			if((commands.size()>1)
			&&(CMath.s_int((String)commands.firstElement())>0))
			{
				maxToDrop=CMath.s_int((String)commands.firstElement());
				commands.setElementAt(getScr("Destroy","all"),0);
			}

			String whatToDrop=CMParms.combine(commands,0);
			boolean allFlag=(commands.size()>0)?((String)commands.elementAt(0)).equalsIgnoreCase(getScr("Destroy","all")):false;
			if(whatToDrop.toUpperCase().startsWith(getScr("Destroy","alldot"))){ allFlag=true; whatToDrop=getScr("Destroy","allup")+whatToDrop.substring(4);}
			if(whatToDrop.toUpperCase().endsWith(getScr("Destroy","dotall"))){ allFlag=true; whatToDrop=getScr("Destroy","allup")+whatToDrop.substring(0,whatToDrop.length()-4);}
			int addendum=1;
			String addendumStr="";
			do
			{
				Item dropThis=mob.fetchCarried(null,whatToDrop+addendumStr);
				if((dropThis==null)
				&&(V.size()==0)
				&&(addendumStr.length()==0)
				&&(!allFlag))
				{
					dropThis=mob.fetchWornItem(whatToDrop);
					if(dropThis!=null)
					{
						int matType=dropThis.material()&RawMaterial.MATERIAL_MASK;
						if((matType!=RawMaterial.MATERIAL_GLASS)
						&&(matType!=RawMaterial.MATERIAL_LIQUID)
						&&(matType!=RawMaterial.MATERIAL_PAPER))
						{
							mob.tell(dropThis.Name()+getScr("Destroy","notdestroyed"));
							return false;
						}
						else	
						if((!dropThis.amWearingAt(Item.WORN_HELD))&&(!dropThis.amWearingAt(Item.WORN_WIELD)))
						{
							mob.tell(getScr("Destroy","remove"));
							return false;
						}
						else
						{
							CMMsg newMsg=CMClass.getMsg(mob,dropThis,null,CMMsg.MSG_REMOVE,null);
							if(mob.location().okMessage(mob,newMsg))
								mob.location().send(mob,newMsg);
							else
								return false;
						}
					}
				}
				if(dropThis==null) break;
				if((CMLib.flags().canBeSeenBy(dropThis,mob))
				&&(!V.contains(dropThis)))
					V.addElement(dropThis);
				addendumStr="."+(++addendum);
			}
			while((allFlag)&&(addendum<=maxToDrop));

			boolean didAnything=false;
			for(int i=0;i<V.size();i++)
			{
				if(destroyItem(mob,(Item)V.elementAt(i),false,true))
					didAnything=true;
				else
				if(V.elementAt(i) instanceof Coins)
					((Coins)V.elementAt(i)).putCoinsBack();
				else
				if(V.elementAt(i) instanceof RawMaterial)
					((RawMaterial)V.elementAt(i)).rebundle();
			}
			if(!didAnything)
			{
				if(V.size()==0)
					mob.tell(getScr("Destroy","nocarried"));
				else
					mob.tell(getScr("Destroy","nodestroy"));
			}
			mob.location().recoverRoomStats();
			mob.location().recoverRoomStats();
			return false;
		}
		
		String commandType="";

		if(commands.size()>1)
		{
			commandType=((String)commands.elementAt(1)).toUpperCase();
		}
        for(int i=0;i<CMLib.journals().getNumCommandJournals();i++)
        {
            if((CMLib.journals().getCommandJournalName(i).equals(commandType))
            &&(CMSecurity.isAllowed(mob,mob.location(),CMLib.journals().getCommandJournalName(i))
                ||CMSecurity.isAllowed(mob,mob.location(),"KILL"+CMLib.journals().getCommandJournalName(i)+"S")))
            {
                String nam=CMLib.journals().getCommandJournalName(i);
                int which=-1;
                if(commands.size()>2)
                    which=CMath.s_int((String)commands.elementAt(2));
                if(which<=0)
                    mob.tell(getScr("Destroy","invalidnumber",nam.toLowerCase(),nam));
                else
                {
                    CMLib.database().DBDeleteJournal("SYSTEM_"+nam+"S",which-1);
                    mob.tell(nam.toLowerCase()+getScr("Destroy","deletedone"));
                    
                }
                return true;
            }
        }
		if(commandType.equals(getScr("Destroy","cmdexit")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDEXITS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Destroy","wavesarms"));
			exits(mob,commands);
		}
		else
		if(commandType.equals(getScr("Destroy","cmditem")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDITEMS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Destroy","wavesarms"));
			items(mob,commands);
		}
		else
		if(commandType.equals(getScr("Destroy","cmdarea")))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Destroy","wavesarms"));
			areas(mob,commands);
		}
		else
		if(commandType.equals(getScr("Destroy","cmdroom2")))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Destroy","wavesarms"));
			rooms(mob,commands);
		}
		else
		if(commandType.equals(getScr("Destroy","cmdrace")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDRACES")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Destroy","wavesarms"));
			races(mob,commands);
		}
		else
		if(commandType.equals(getScr("Destroy","cmdclass")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDCLASSES")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Destroy","wavesarms"));
			classes(mob,commands);
		}
		else
		if(commandType.equals(getScr("Destroy","cmdable")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDABILITIES")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Destroy","wavesarms"));
			abilities(mob,commands);
		}
		else
		if(commandType.equals(getScr("Destroy","cmdcompo")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"COMPONENTS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Destroy","wavesarms"));
			components(mob,commands);
		}
        else
        if(commandType.equals(getScr("Destroy","cmdexper")))
        {
            if(!CMSecurity.isAllowed(mob,mob.location(),"EXPERTISES")) return errorOut(mob);
            mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Destroy","wavesarms"));
            expertises(mob,commands);
        }
        else
        if(commandType.equals(getScr("Destroy","cmdtitle")))
        {
            if(!CMSecurity.isAllowed(mob,mob.location(),"TITLES")) return errorOut(mob);
            mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Destroy","wavesarms"));
            titles(mob,commands);
        }
		else
		if(commandType.equals(getScr("Destroy","cmduser")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDPLAYERS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Destroy","wavesarms"));
			players(mob,commands);
		}
		else
		if(commandType.equals(getScr("Destroy","cmdsocial")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDSOCIALS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Destroy","wavesarms"));
			socials(mob,commands);
		}
		else
		if(commandType.equals(getScr("Destroy","cmdnopurge")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"NOPURGE")) return errorOut(mob);
			int which=-1;
			if(commands.size()>2)
				which=CMath.s_int((String)commands.elementAt(2));
			if(which<=0)
				mob.tell(getScr("Destroy","badplayer"));
			else
			{
				StringBuffer newNoPurge=new StringBuffer("");
				Vector protectedOnes=Resources.getFileLineVector(Resources.getFileResource("protectedplayers.ini",false));
				if((protectedOnes!=null)&&(protectedOnes.size()>0))
					for(int b=0;b<protectedOnes.size();b++)
					{
						String B=(String)protectedOnes.elementAt(b);
						if(((b+1)!=which)&&(B.trim().length()>0))
							newNoPurge.append(B+"\n");
					}
				Resources.updateResource("protectedplayers.ini",newNoPurge);
				Resources.saveFileResource("protectedplayers.ini");
				mob.tell(getScr("Destroy","ok"));
			}
		}
		else
		if(commandType.equals(getScr("Destroy","cmdban")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"BAN")) return errorOut(mob);
			int which=-1;
			if(commands.size()>2)
				which=CMath.s_int((String)commands.elementAt(2));
			if(which<=0)
				mob.tell(getScr("Destroy","badbad"));
			else
			{
                CMSecurity.unban(which);
				mob.tell(getScr("Destroy","ok"));
			}
		}
        else
        if(commandType.equals(getScr("Destroy","cmdthread")))
        {
            if(!CMSecurity.isASysOp(mob)) return errorOut(mob);
            String which=CMParms.combine(commands,2);
            Thread whichT=null;
            if(which.length()>0)
                whichT=findThread(which);
            if(whichT==null)
                mob.tell(getScr("Destroy","badthread"));
            else
            {
                CMLib.killThread(whichT,500,1);
                Log.sysOut("CreateEdit",mob.Name()+getScr("Destroy","destroyedthread")+whichT.getName()+".");
                mob.tell(getScr("Destroy","ok"));
            }
        }
        else
        if(commandType.startsWith(getScr("Destroy","cmdsession")))
        {
            if(!CMSecurity.isAllowed(mob,mob.location(),"BOOT")) return errorOut(mob);
            int which=-1;
            if(commands.size()>2)
                which=CMath.s_int((String)commands.elementAt(2));
            if((which<0)||(which>=CMLib.sessions().size()))
                mob.tell(getScr("Destroy","badsession"));
            else
            {
                Session S=CMLib.sessions().elementAt(which);
                CMLib.sessions().stopSessionAtAllCosts(S);
                if(S.getStatus()==Session.STATUS_LOGOUTFINAL)
                    mob.tell(getScr("Destroy","ok"));
                else
                    mob.tell(getScr("Destroy","noshutdown",Session.STATUS_STR[S.getStatus()]));
            }
        }
        else
        if(commandType.equals(getScr("Destroy","cmdjournal")))
        {
            if(!CMSecurity.isAllowed(mob,mob.location(),"JOURNALS")) return errorOut(mob);
            if(commands.size()<3)
            {
                mob.tell(getScr("Destroy","badjournalerr"));
                return errorOut(mob);
            }
            Vector V=CMLib.database().DBReadJournal(null);
            String name=CMParms.combine(commands,2);
            int which=-1;
            for(int v=0;v<V.size();v++)
                if(((String)V.elementAt(v)).equalsIgnoreCase(name))
                {
                    name=(String)V.elementAt(v);
                    which=v;
                    break;
                }
            if(which<0)
            for(int v=0;v<V.size();v++)
                if(((String)V.elementAt(v)).startsWith(name))
                {
                    name=(String)V.elementAt(v);
                    which=v;
                    break;
                }
            if(which<0)
                mob.tell(getScr("Destroy","invalidjournal"));
            else
            if(mob.session().confirm(getScr("Destroy","confirmmsgs",""+CMLib.database().DBCountJournal(name,null,null)),"N"))
            {
                CMLib.database().DBDeleteJournal(name,Integer.MAX_VALUE);
                mob.tell(getScr("Destroy","done"));
            }
        }
        else
        if(commandType.equals(getScr("Destroy","cmdfaction")))
        {
            if(!CMSecurity.isAllowed(mob,mob.location(),"CMDFACTIONS")) return errorOut(mob);
            mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Destroy","wavesarms"));
            if(commands.size()<3)
                mob.tell(getScr("Destroy","badfaction"));
            else
            {
                String name=CMParms.combine(commands,2);
                Faction F=CMLib.factions().getFaction(name);
                if(F==null) F=CMLib.factions().getFactionByName(name);
                if(F==null)
                    mob.tell(getScr("Destroy","unknownfac",name));
                else
                if((!mob.isMonster())&&(mob.session().confirm(getScr("Destroy","delfac",F.factionID()),"N")))
                {
                    try
                    {
                        java.io.File F2=new java.io.File("resources/"+F.factionID());
                        if(F2.exists()) F2.delete();
                        Log.sysOut("CreateEdit",mob.Name()+getScr("Destroy","destroyedfaction")+F.name()+" ("+F.factionID()+").");
                        mob.tell(getScr("Destroy","factiondeled",F.factionID()));
                        Resources.removeResource(F.factionID());
                    }
                    catch(Exception e)
                    {
                        Log.errOut("CreateEdit",e);
                        mob.tell(getScr("Destroy","nofacdeled",F.factionID()));
                    }
                }
            }
        }
        else
		if(commandType.equals(getScr("Destroy","cmdmob")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDMOBS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Destroy","wavesarms"));
			mobs(mob,commands);
		}
        else
        if(commandType.equals(getScr("Destroy","cmdpoll")))
        {
            if(!CMSecurity.isAllowed(mob,mob.location(),"POLLS")) return errorOut(mob);
            String name=CMParms.combine(commands,2);
            Poll P=null;
            if(CMath.isInteger(name))
                P=CMLib.polls().getPoll(CMath.s_int(name)-1);
            else
            if(name.length()>0)
                P=CMLib.polls().getPoll(name);
            if(P==null)
            {
                mob.tell(getScr("Destroy","badpoll",name));
                mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubsspell"));
                return false;
            }
            mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Destroy","wavesarms"));
            if((mob.session()!=null)&&(mob.session().confirm(getScr("Destroy","despoll",P.getName()),"Y")))
            {
                P.dbdelete();
                mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Destroy","uncertainup"));
                Log.sysOut("CreateEdit",mob.Name()+getScr("Destroy","modifiedpoll")+P.getName()+".");
            }
            else
                mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Destroy","flubsspell"));
        }
		else
		if(commandType.equals(getScr("Destroy","cmdquest")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDQUESTS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Destroy","wavesarms"));
			if(commands.size()<3)
				mob.tell(getScr("Destroy","desquest"));
			else
			{
				String name=CMParms.combine(commands,2);
                Quest Q=null;
                if(CMath.isInteger(name))
                {
                    Q=CMLib.quests().fetchQuest(CMath.s_int(name)-1);
                    if(Q!=null) name=Q.name();
                }
                if(Q==null) Q=CMLib.quests().fetchQuest(name);
				if(Q==null)
					mob.tell(getScr("Destroy","unknownquest",name));
				else
				{
                    if(Q.running()&&(!Q.stopping())) Q.stopQuest();
					mob.tell(getScr("Destroy","questdestroyed",Q.name()));
					CMLib.quests().delQuest(Q);
				}
			}
		}
		else
		if(commandType.equals(getScr("Destroy","cmdclan")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDCLANS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Destroy","wavesarms"));
			if(commands.size()<3)
				mob.tell(getScr("Destroy","whichclan"));
			else
			{
				String name=CMParms.combine(commands,2);
				Clan C=CMLib.clans().findClan(name);
				if(C==null)
					mob.tell(getScr("Destroy","badclan",name));
				else
				{
					mob.tell(getScr("Destroy","clandes",C.name()));
					C.destroyClan();
					Log.sysOut("CreateEdit",getScr("Destroy","clandestroyed",C.name())+mob.name()+".");
				}
			}
		}
		else
		{
			String allWord=CMParms.combine(commands,1);
			Environmental thang=mob.location().fetchFromRoomFavorItems(null,allWord,Item.WORNREQ_ANY);
			if(thang==null)
			    thang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,allWord,Item.WORNREQ_ANY);
			if((thang!=null)&&(thang instanceof Item))
			{
				commands.insertElementAt(getScr("Destroy","cmditem"),1);
				execute(mob,commands);
			}
			else
			if((thang!=null)&&(thang instanceof MOB))
			{
				if(((MOB)thang).isMonster())
					commands.insertElementAt(getScr("Destroy","cmdmob"),1);
				else
					commands.insertElementAt(getScr("Destroy","cmduser"),1);
				execute(mob,commands);
			}
			else
			{
				Room theRoom=null;
				if(allWord.length()>0)
				{
				    try
				    {
						for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
						{
							Room room=(Room)r.nextElement();
							if(room.roomID().equalsIgnoreCase(allWord))
							{
								theRoom=room;
								break;
							}
						}
				    }catch(NoSuchElementException e){}
				}
				if(theRoom!=null)
				{
					commands=new Vector();
					commands.addElement(getScr("Destroy","cmd1"));
					commands.addElement(getScr("Destroy","cmdroom2"));
					commands.addElement(theRoom.roomID());
					execute(mob,commands);
				}
				else
				{
					if(Directions.getGoodDirectionCode(allWord)>=0)
					{
						commands=new Vector();
						commands.addElement(getScr("Destroy","cmd1"));
						commands.addElement(getScr("Destroy","cmdroom2"));
						commands.addElement(allWord);
						execute(mob,commands);

						commands=new Vector();
						commands.addElement(getScr("Destroy","cmd1"));
						commands.addElement(getScr("Destroy","cmdexit"));
						commands.addElement(allWord);
						execute(mob,commands);
					}
					else
					if(CMLib.socials().FetchSocial(allWord,true)!=null)
					{
						commands.insertElementAt(getScr("Destroy","cmdsocial"),1);
						execute(mob,commands);
					}
					else
					mob.tell(
						getScr("Destroy","cannotdes")+commandType+"'. "
						+getScr("Destroy","tryinstead")
						+getScr("Destroy","deslist"));
				}
			}
		}
		return false;
	}
    public double combatActionsCost(){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),100.0);}
    public double actionsCost(){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);}
	public boolean canBeOrdered(){return false;}

	
}
