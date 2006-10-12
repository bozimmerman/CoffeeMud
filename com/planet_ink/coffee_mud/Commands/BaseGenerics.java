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


import java.util.*;
import java.io.ByteArrayInputStream;
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
public class BaseGenerics extends StdCommand
{
	private static final long maxLength=Long.MAX_VALUE;
	// showNumber should always be a valid number no less than 1
	// showFlag should be a valid number for editing, or -1 for skipping

	static void genName(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","Name",showNumber+"",E.Name()));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt(getScr("BaseGenerics","enternewone"),"");
		if(newName.length()>0)
			E.setName(newName);
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}

	static void genImage(MOB mob, Environmental E, int showNumber, int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","mxpfile",showNumber+"",E.rawImage()));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt(getScr("BaseGenerics","enterfile"),"");
		if(newName.length()>0)
			E.setImage(newName);
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}

	static void genCorpseData(MOB mob, DeadBody E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","corpsedata",showNumber+"",E.mobName(),E.killerName()));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
        mob.tell(getScr("BaseGenerics","deadMobname",E.mobName()));
		String newName=mob.session().prompt(getScr("BaseGenerics","enternewname"),"");
		if(newName.length()>0) E.setMobName(newName);
		else mob.tell(getScr("BaseGenerics","nochange"));
		mob.tell(getScr("BaseGenerics","deadMobd",E.mobDescription()));
		newName=mob.session().prompt(getScr("BaseGenerics","enterd"),"");
		if(newName.length()>0) E.setMobDescription(newName);
		else mob.tell(getScr("BaseGenerics","nochange"));
        mob.tell(getScr("BaseGenerics","deadmobplayercorpse",""+E.playerCorpse()));
        newName=mob.session().prompt(getScr("BaseGenerics","truefalse"),"");
        if((newName.length()>0)&&(newName.equalsIgnoreCase("true")||newName.equalsIgnoreCase("false")))
            E.setPlayerCorpse(Boolean.valueOf(newName.toLowerCase()).booleanValue());
        else mob.tell(getScr("BaseGenerics","nochange"));
        mob.tell(getScr("BaseGenerics","deadmobpkflag",""+E.mobPKFlag()));
        newName=mob.session().prompt(getScr("BaseGenerics","truefalse"),"");
        if((newName.length()>0)&&(newName.equalsIgnoreCase("true")||newName.equalsIgnoreCase("false")))
            E.setMobPKFlag(Boolean.valueOf(newName.toLowerCase()).booleanValue());
        else mob.tell(getScr("BaseGenerics","nochange"));
        genCharStats(mob,E.charStats());
		mob.tell(getScr("BaseGenerics","killersname",E.killerName()));
		newName=mob.session().prompt(getScr("BaseGenerics","enterk"),"");
		if(newName.length()>0) E.setKillerName(newName);
		else mob.tell(getScr("BaseGenerics","nochange"));
        mob.tell(getScr("BaseGenerics","deadmobkillerplayer",""+E.killerPlayer()));
        newName=mob.session().prompt(getScr("BaseGenerics","truefalse"),"");
        if((newName.length()>0)&&(newName.equalsIgnoreCase("true")||newName.equalsIgnoreCase("false")))
            E.setKillerPlayer(Boolean.valueOf(newName.toLowerCase()).booleanValue());
        else mob.tell(getScr("BaseGenerics","nochange"));
        mob.tell(getScr("BaseGenerics","deadmobtod",CMLib.time().date2String(E.timeOfDeath())));
        newName=mob.session().prompt(getScr("BaseGenerics","entvaluenew"),"");
        if(newName.length()>0) E.setTimeOfDeath(CMLib.time().string2Millis(newName));
        else mob.tell(getScr("BaseGenerics","nochange"));
        mob.tell(getScr("BaseGenerics","deadmoblastmsg",E.lastMessage()));
        newName=mob.session().prompt(getScr("BaseGenerics","entvaluenew"),"");
        if(newName.length()>0) E.setLastMessage(newName);
        else mob.tell(getScr("BaseGenerics","nochange"));
	}

	static void genAuthor(MOB mob, Area A, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","author",showNumber+"",A.getAuthorID()));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt(getScr("BaseGenerics","enternewone"),"");
		if(newName.length()>0)
			A.setAuthorID(newName);
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}

	static void genPanelType(MOB mob, ShipComponent.ShipPanel S, int showNumber, int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String componentType=CMStrings.capitalizeAndLower(ShipComponent.ShipPanel.COMPONENT_PANEL_DESC[S.panelType()].toLowerCase());
		mob.tell(getScr("BaseGenerics","paneltype",showNumber+"",componentType));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		boolean continueThis=true;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(continueThis))
		{
		    continueThis=false;
			String newName=mob.session().prompt(getScr("BaseGenerics","enter2"),"");
			if(newName.length()>0)
			{
			    if(newName.equalsIgnoreCase("?"))
			    {
			        mob.tell(getScr("BaseGenerics","comptypes",CMParms.toStringList(ShipComponent.ShipPanel.COMPONENT_PANEL_DESC)));
			        continueThis=true;
			    }
			    else
			    {
			        int newType=-1;
			        for(int i=0;i<ShipComponent.ShipPanel.COMPONENT_PANEL_DESC.length;i++)
			            if(ShipComponent.ShipPanel.COMPONENT_PANEL_DESC[i].equalsIgnoreCase(newName))
			                newType=i;
			        if(newType<0)
			        {
			            mob.tell(getScr("BaseGenerics","newnotrec",newName));
			            continueThis=true;
			        }
			        else
			            S.setPanelType(newType);
			    }
			}
			else
				mob.tell(getScr("BaseGenerics","nochange"));
		}
	}

	static void genCurrency(MOB mob, Area A, int showNumber, int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String currencyName=A.getCurrency().length()==0?"Default":A.getCurrency();
		mob.tell(getScr("BaseGenerics","currencyname",showNumber+"",currencyName));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt(getScr("BaseGenerics","entdef"),"");
		if(newName.length()>0)
		{
		    if(newName.equalsIgnoreCase("default"))
		        A.setCurrency("");
		    else
		    if((newName.indexOf("=")<0)&&(!CMLib.beanCounter().getAllCurrencies().contains(newName.trim().toUpperCase())))
		    {
		        Vector V=CMLib.beanCounter().getAllCurrencies();
		        mob.tell(getScr("BaseGenerics","currencyerr",newName.trim().toUpperCase(),CMParms.toStringList(V)));
		    }
		    else
		    if(newName.indexOf("=")>=0)
		        A.setCurrency(newName.trim());
		    else
				A.setCurrency(newName.toUpperCase().trim());
		}
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}

	static void genTimeClock(MOB mob, Area A, int showNumber, int showFlag)
	throws IOException
	{

		if((showFlag>0)&&(showFlag!=showNumber)) return;
		TimeClock TC=A.getTimeObj();
		StringBuffer report=new StringBuffer("");
		if(TC==CMClass.globalClock())
			report.append(getScr("BaseGenerics","defaultant"));
		else
		{
		    report.append(getScr("BaseGenerics","hrsday",TC.getHoursInDay()+""));
		    report.append(getScr("BaseGenerics","daysmn",TC.getDaysInMonth()+""));
		    report.append(getScr("BaseGenerics","mthyrs",TC.getMonthsInYear()+""));
		}
		mob.tell(getScr("BaseGenerics","calendar",showNumber+"",report.toString()));
		if(TC==CMClass.globalClock()) return;
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName="";
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(newName.length()==0))
		{
			report=new StringBuffer(getScr("BaseGenerics","calset"));
		    report.append(getScr("BaseGenerics","hoursperday",TC.getHoursInDay()+""));
		    report.append(getScr("BaseGenerics","dawnhour",TC.getDawnToDusk()[TimeClock.TIME_DAWN]+""));
		    report.append(getScr("BaseGenerics","dayhour",TC.getDawnToDusk()[TimeClock.TIME_DAY]+""));
		    report.append(getScr("BaseGenerics","duskhour",TC.getDawnToDusk()[TimeClock.TIME_DUSK]+""));
		    report.append(getScr("BaseGenerics","nighthour",TC.getDawnToDusk()[TimeClock.TIME_NIGHT]+""));
		    report.append(getScr("BaseGenerics","weekdays",CMParms.toStringList(TC.getWeekNames())));
		    report.append(getScr("BaseGenerics","months",CMParms.toStringList(TC.getMonthNames())));
		    report.append(getScr("BaseGenerics","yeartitle",CMParms.toStringList(TC.getYearNames())));
		    mob.tell(report.toString());
			newName=mob.session().prompt(getScr("BaseGenerics","entchange"),"");
			if(newName.length()==0) break;
			int which=CMath.s_int(newName);

			if((which<0)||(which>8))
				mob.tell(getScr("BaseGenerics","invalid",which+""));
			else
			if(which<=5)
			{
			    newName="";
			    String newNum=mob.session().prompt(getScr("BaseGenerics","entnumb"),"");
			    int val=CMath.s_int(newNum);
			    if(newNum.length()==0)
			        mob.tell(getScr("BaseGenerics","nochange"));
			    else
				switch(which)
			    {
		        case 1:
		            TC.setHoursInDay(val);
		            break;
		        case 2:
		            TC.getDawnToDusk()[TimeClock.TIME_DAWN]=val;
		            break;
		        case 3:
		            if((val>=0)&&(TC.getDawnToDusk()[TimeClock.TIME_DAWN]>=val))
                        mob.tell(getScr("BaseGenerics","dawnerr"));
		            else
			            TC.getDawnToDusk()[TimeClock.TIME_DAY]=val;
		            break;
		        case 4:
		            if((val>=0)&&(TC.getDawnToDusk()[TimeClock.TIME_DAWN]>=val))
                        mob.tell(getScr("BaseGenerics","dawnerr"));
		            else
		            if((val>=0)&&(TC.getDawnToDusk()[TimeClock.TIME_DAY]>=val))
                        mob.tell(getScr("BaseGenerics","dayerr"));
		            else
			            TC.getDawnToDusk()[TimeClock.TIME_DUSK]=val;
		            break;
		        case 5:
		            if((val>=0)&&(TC.getDawnToDusk()[TimeClock.TIME_DAWN]>=val))
                        mob.tell(getScr("BaseGenerics","dawnerr"));
		            else
		            if((val>=0)&&(TC.getDawnToDusk()[TimeClock.TIME_DAY]>=val))
                        mob.tell(getScr("BaseGenerics","dayerr"));
		            else
		            if((val>=0)&&(TC.getDawnToDusk()[TimeClock.TIME_DUSK]>=val))
                        mob.tell(getScr("BaseGenerics","duskerr"));
		            else
			            TC.getDawnToDusk()[TimeClock.TIME_NIGHT]=val;
		            break;
			    }
			}
			else
			{
			    newName="";
			    String newNum=mob.session().prompt(getScr("BaseGenerics","entlist"),"");
			    if(newNum.length()==0)
			        mob.tell(getScr("BaseGenerics","nochange"));
			    else
			    switch(which)
			    {
		        case 6:
		            TC.setDaysInWeek(CMParms.toStringArray(CMParms.parseCommas(newNum,true)));
		            break;
		        case 7:
		            TC.setMonthsInYear(CMParms.toStringArray(CMParms.parseCommas(newNum,true)));
		            break;
		        case 8:
		            TC.setYearNames(CMParms.toStringArray(CMParms.parseCommas(newNum,true)));
		            break;
			    }
			}
		}
		TC.save();
	}

	static void genClan(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag<=0)||(showFlag==showNumber))
		{
			mob.tell(getScr("BaseGenerics","clanid",showNumber+"",E.getClanID()));
			if((showFlag==showNumber)||(showFlag<=-999))
			{
				String newName=mob.session().prompt(getScr("BaseGenerics","entnull"),"");
				if(newName.equalsIgnoreCase("null"))
					E.setClanID("");
				else
				if(newName.length()>0)
				{
					E.setClanID(newName);
					E.setClanRole(Clan.POS_MEMBER);
				}
				else
					mob.tell(getScr("BaseGenerics","nochange"));
			}
		}
		if(((showFlag<=0)||(showFlag==showNumber))
		   &&(!E.isMonster())
		   &&(E.getClanID().length()>0)
		   &&(CMLib.clans().getClan(E.getClanID())!=null))
		{

			Clan C=CMLib.clans().getClan(E.getClanID());
			mob.tell(getScr("BaseGenerics","clanrole",showNumber+"",CMLib.clans().getRoleName(C.getGovernment(),E.getClanRole(),true,false)));
			if((showFlag==showNumber)||(showFlag<=-999))
			{
				String newName=mob.session().prompt(getScr("BaseGenerics","enternewone"),"");
				if(newName.length()>0)
				{
					int newRole=-1;
					for(int i=0;i<Clan.ROL_DESCS[C.getGovernment()].length;i++)
						if(newName.equalsIgnoreCase(Clan.ROL_DESCS[C.getGovernment()][i]))
						{
							newRole=(int)CMath.pow(2,i-1);
							break;
						}
					if(newRole<0)
						mob.tell(getScr("BaseGenerics","errrole",CMParms.toStringList(Clan.ROL_DESCS[C.getGovernment()])));
					else
						E.setClanRole(newRole);
				}
				else
					mob.tell(getScr("BaseGenerics","nochange"));
			}
		}
	}

	static void genArchivePath(MOB mob, Area E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","arcfilename",showNumber+"",E.getArchivePath()));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt(getScr("BaseGenerics","entnull2"),"");
		if(newName.equalsIgnoreCase("null"))
			E.setArchivePath("");
		else
		if(newName.length()>0)
			E.setArchivePath(newName);
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}

	public static Room changeRoomType(Room R, Room newRoom)
	{
		if((R==null)||(newRoom==null)) return R;
		synchronized(("SYNC"+R.roomID()).intern())
		{
			R=CMLib.map().getRoom(R);
			Room oldR=R;
			R=newRoom;
	        Vector oldBehavsNEffects=new Vector();
			for(int a=oldR.numEffects()-1;a>=0;a--)
			{
				Ability A=oldR.fetchEffect(a);
				if(A!=null)
				{
	                if(!A.canBeUninvoked())
	                {
	                    oldBehavsNEffects.addElement(A);
	                    oldR.delEffect(A);
	                }
	                else
	    				A.unInvoke();
				}
			}
	        for(int b=0;b<oldR.numBehaviors();b++)
	        {
	            Behavior B=oldR.fetchBehavior(b);
	            if(B!=null)
	                oldBehavsNEffects.addElement(B);
	        }
			CMLib.threads().deleteTick(oldR,-1);
			R.setRoomID(oldR.roomID());
			Area A=oldR.getArea();
			if(A!=null) A.delProperRoom(oldR);
			R.setArea(A);
			for(int d=0;d<R.rawDoors().length;d++)
				R.rawDoors()[d]=oldR.rawDoors()[d];
			for(int d=0;d<R.rawExits().length;d++)
				R.rawExits()[d]=oldR.rawExits()[d];
			R.setDisplayText(oldR.displayText());
			R.setDescription(oldR.description());
			if((R instanceof GridLocale)&&(oldR instanceof GridLocale))
			{
				((GridLocale)R).setXGridSize(((GridLocale)oldR).xGridSize());
				((GridLocale)R).setYGridSize(((GridLocale)oldR).yGridSize());
				((GridLocale)R).clearGrid(null);
			}
			Vector allmobs=new Vector();
			int skip=0;
			while(oldR.numInhabitants()>(skip))
			{
				MOB M=oldR.fetchInhabitant(skip);
				if(M.savable())
				{
					if(!allmobs.contains(M))
						allmobs.addElement(M);
					oldR.delInhabitant(M);
				}
				else
				if(oldR!=R)
				{
					oldR.delInhabitant(M);
					R.bringMobHere(M,true);
				}
				else
					skip++;
			}
			Vector allitems=new Vector();
			while(oldR.numItems()>0)
			{
				Item I=oldR.fetchItem(0);
				if(!allitems.contains(I))
					allitems.addElement(I);
				oldR.delItem(I);
			}
	
			for(int i=0;i<allitems.size();i++)
			{
				Item I=(Item)allitems.elementAt(i);
				if(!R.isContent(I))
				{
					if(I.subjectToWearAndTear())
						I.setUsesRemaining(100);
					I.recoverEnvStats();
					R.addItem(I);
					R.recoverRoomStats();
				}
			}
			for(int m=0;m<allmobs.size();m++)
			{
				MOB M=(MOB)allmobs.elementAt(m);
				if(!R.isInhabitant(M))
				{
					MOB M2=(MOB)M.copyOf();
					M2.setStartRoom(R);
					M2.setLocation(R);
	                long rejuv=Tickable.TICKS_PER_RLMIN+Tickable.TICKS_PER_RLMIN+(Tickable.TICKS_PER_RLMIN/2);
	                if(rejuv>(Tickable.TICKS_PER_RLMIN*20)) rejuv=(Tickable.TICKS_PER_RLMIN*20);
					M2.envStats().setRejuv((int)rejuv);
					M2.recoverCharStats();
					M2.recoverEnvStats();
					M2.recoverMaxState();
					M2.resetToMaxState();
					M2.bringToLife(R,true);
					R.recoverRoomStats();
					M.destroy();
				}
			}
	
			try
			{
				for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
				{
					Room R2=(Room)r.nextElement();
					for(int d=0;d<R2.rawDoors().length;d++)
						if(R2.rawDoors()[d]==oldR)
						{
							R2.rawDoors()[d]=R;
							if(R2 instanceof GridLocale)
								((GridLocale)R2).buildGrid();
						}
				}
		    }catch(NoSuchElementException e){}
		    try
		    {
				for(Enumeration e=CMLib.map().players();e.hasMoreElements();)
				{
					MOB M=(MOB)e.nextElement();
					if(M.getStartRoom()==oldR)
						M.setStartRoom(R);
					else
					if(M.location()==oldR)
						M.setLocation(R);
				}
		    }catch(NoSuchElementException e){}
			R.getArea().fillInAreaRoom(R);
	        for(int i=0;i<oldBehavsNEffects.size();i++)
	        {
	            if(oldBehavsNEffects.elementAt(i) instanceof Behavior)
	                R.addBehavior((Behavior)oldBehavsNEffects.elementAt(i));
	            else
	                R.addNonUninvokableEffect((Ability)oldBehavsNEffects.elementAt(i));
	        }
			CMLib.database().DBUpdateRoom(R);
			CMLib.database().DBUpdateMOBs(R);
			CMLib.database().DBUpdateItems(R);
	        oldR.destroy();
	        R.getArea().addProperRoom(R); // necessary because of the destroy
			R.setImage(R.rawImage());
			R.startItemRejuv();
		}
		return R;
	}

	static Room genRoomType(MOB mob, Room R, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return R;
		mob.tell(getScr("BaseGenerics","type",showNumber+"",CMClass.className(R)));
		if((showFlag!=showNumber)&&(showFlag>-999)) return R;
		String newName="";
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(newName.length()==0))
		{
			newName=mob.session().prompt(getScr("BaseGenerics","enter2"),"");
			if(newName.trim().equals("?"))
			{
				mob.tell(CMLib.lister().reallyList2Cols(CMClass.locales(),-1,null).toString()+"\n\r");
				newName="";
			}
			else
			if(newName.length()>0)
			{
				Room newRoom=CMClass.getLocale(newName);
				if(newRoom==null)
					mob.tell(getScr("BaseGenerics","newdontex",newName));
				else
				if(mob.session().confirm(getScr("BaseGenerics","roomcng",R.roomID()),"N"))
					R=changeRoomType(R,newRoom);
				R.recoverRoomStats();
			}
			else
			{
				mob.tell(getScr("BaseGenerics","nochange"));
				break;
			}
		}
		return R;
	}

	static void genDescription(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","description",showNumber+"",E.description()));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt(getScr("BaseGenerics","entnull3"),"");
		if(newName.trim().equalsIgnoreCase("null"))
			E.setDescription("");
		else
		if(newName.length()>0)
			E.setDescription(newName);
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}

	static void genNotes(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","notes",showNumber+"",(E.playerStats()!=null)?E.playerStats().notes():""));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt(getScr("BaseGenerics","some4"),"");
		if((newName.length()>0)&&(E.playerStats()!=null))
			E.playerStats().setNotes(newName);
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}

	static void genPassword(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","pwd",showNumber+""));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt(getScr("BaseGenerics","entreset"),"");
		if((newName.length()>0)&&(E.playerStats()!=null))
		{
			E.playerStats().setPassword(newName);
			CMLib.database().DBUpdatePassword(E);
		}
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}

	static void genEmail(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		if(E.playerStats()!=null)
			mob.tell(getScr("BaseGenerics","email",showNumber+"",E.playerStats().getEmail()));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt(getScr("BaseGenerics","enternewone"),"");
		if((newName.length()>0)&&(E.playerStats()!=null))
			E.playerStats().setEmail(newName);
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}

	public static void genDisplayText(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","display",showNumber+"",E.displayText()));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=null;
		if(E instanceof Item)
			newName=mob.session().prompt(getScr("BaseGenerics","some1"),"");
		else
		if(E instanceof Exit)
			newName=mob.session().prompt(getScr("BaseGenerics","some2"),"");
		else
			newName=mob.session().prompt(getScr("BaseGenerics","some3"),"");
		if(newName.length()>0)
		{
			if(newName.trim().equalsIgnoreCase("null"))
				newName="";
			E.setDisplayText(newName);
		}
		else
			mob.tell(getScr("BaseGenerics","nochange"));
		if((E instanceof Item)&&(E.displayText().length()==0))
			mob.tell(getScr("BaseGenerics","blended"));
	}
	public static void genClosedText(MOB mob, Exit E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		if(E instanceof Item)
			mob.tell(getScr("BaseGenerics","exitclosedtxt",showNumber+"",E.closedText()));
		else
			mob.tell(getScr("BaseGenerics","closedtxt",showNumber+"",E.closedText()));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt(getScr("BaseGenerics","some5"),"");
		if(newName.equals("null"))
			E.setExitParams(E.doorName(),E.closeWord(),E.openWord(),"");
		else
		if(newName.length()>0)
			E.setExitParams(E.doorName(),E.closeWord(),E.openWord(),newName);
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}
	public static void genDoorName(MOB mob, Exit E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		if(E instanceof Item)
			mob.tell(getScr("BaseGenerics","exitdir",showNumber+"",E.doorName()));
		else
			mob.tell(getScr("BaseGenerics","doorn",showNumber+"",E.doorName()));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt(getScr("BaseGenerics","some4"),"");
		if(newName.length()>0)
			E.setExitParams(newName,E.closeWord(),E.openWord(),E.closedText());
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}

	public static void genBurnout(MOB mob, Light E, int showNumber, int showFlag)
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","isdestroyed",showNumber+"",E.destroyedWhenBurnedOut()+""));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.setDestroyedWhenBurntOut(!E.destroyedWhenBurnedOut());
	}

	public static void genOpenWord(MOB mob, Exit E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","openword",showNumber+"",E.openWord()));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt(getScr("BaseGenerics","some4"),"");
		if(newName.length()>0)
			E.setExitParams(E.doorName(),E.closeWord(),newName,E.closedText());
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}

	public static void genSubOps(MOB mob, Area A, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String newName="Q";
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(newName.length()>0))
		{
			mob.tell(getScr("BaseGenerics","aeastaffname",showNumber+"",A.getSubOpList()));
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			newName=mob.session().prompt(getScr("BaseGenerics","addrem"),"");
			if(newName.length()>0)
			{
				if(A.amISubOp(newName))
				{
					A.delSubOp(newName);
					mob.tell(getScr("BaseGenerics","staffrem"));
				}
				else
				if(CMLib.database().DBUserSearch(null,newName))
				{
					A.addSubOp(newName);
					mob.tell(getScr("BaseGenerics","ataffadd"));
				}
				else
					mob.tell(getScr("BaseGenerics","usererr",newName));
			}
		}
	}

    public static void genParentAreas(MOB mob, Area A, int showNumber, int showFlag)
            throws IOException
    {
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String newArea="Q";
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(newArea.length()>0))
		{
		    mob.tell(getScr("BaseGenerics","parentareas",showNumber+"",A.getParentsList()));
		    if((showFlag!=showNumber)&&(showFlag>-999)) return;
		    newArea=mob.session().prompt(getScr("BaseGenerics","arearem"),"");
		    if(newArea.length()>0)
		    {
		        Area lookedUp=CMLib.map().getArea(newArea);
		        if(lookedUp!=null)
		        {
		            if (lookedUp.isChild(A))
					{
						// this new area is already a parent to A,
						// they must want it removed
						A.removeParent(lookedUp);
						lookedUp.removeChild(A);
						mob.tell(getScr("BaseGenerics","arearem", lookedUp.Name()+""));
		            }
		            else
					{
		                if(A.canParent(lookedUp))
						{
		                    A.addParent(lookedUp);
		                    lookedUp.addChild(A);
		                    mob.tell(getScr("BaseGenerics","areaadd",lookedUp.Name()+""));
		                }
		                else
		                {
		                    mob.tell(getScr("BaseGenerics","areaerr",lookedUp.Name()+"" ));
		                }
		            }
		        }
		        else
		            mob.tell(getScr("BaseGenerics","areaerr2",newArea));
		    }
		}
    }

    public static void genChildAreas(MOB mob, Area A, int showNumber, int showFlag)
            throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        String newArea="Q";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(newArea.length()>0))
        {
            mob.tell(getScr("BaseGenerics","areachild",showNumber+"",A.getChildrenList()));
            if((showFlag!=showNumber)&&(showFlag>-999)) return;
            newArea=mob.session().prompt(getScr("BaseGenerics","arearem"),"");
            if(newArea.length()>0)
            {
                Area lookedUp=CMLib.map().getArea(newArea);
                if(lookedUp!=null)
                {
                    if (lookedUp.isParent(A))
					{
                        // this area is already a child to A, they must want it removed
                        A.removeChild(lookedUp);
                        lookedUp.removeParent(A);
                        mob.tell(getScr("BaseGenerics","arearem",lookedUp.Name()+""));
                    }
                    else
					{
                        if(A.canChild(lookedUp))
						{
                            A.addChild(lookedUp);
                            lookedUp.addParent(A);
                            mob.tell(getScr("BaseGenerics","areaadd", lookedUp.Name()+""));
                        }
                        else
                        {
                            mob.tell(getScr("BaseGenerics","areaerr", lookedUp.Name()+"" ));
                        }
                    }
                }
                else
                    mob.tell(getScr("BaseGenerics","areaerr2",newArea));
            }
        }
    }

	public static void genCloseWord(MOB mob, Exit E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","closeword",showNumber+"",E.closeWord()));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt(getScr("BaseGenerics","some4"),"");
		if(newName.length()>0)
			E.setExitParams(E.doorName(),newName,E.openWord(),E.closedText());
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}

	public static void genExitMisc(MOB mob, Exit E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		if(E.hasALock())
		{
			E.setReadable(false);
			mob.tell(getScr("BaseGenerics","asskeyitem",showNumber+"",E.keyName()));
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			String newName=mob.session().prompt(getScr("BaseGenerics","some5"),"");
			if(newName.equalsIgnoreCase("null"))
				E.setKeyName("");
			else
			if(newName.length()>0)
				E.setKeyName(newName);
			else
				mob.tell(getScr("BaseGenerics","nochange"));
		}
		else
		{
			if((showFlag!=showNumber)&&(showFlag>-999))
			{
				if(!E.isReadable())
					mob.tell(getScr("BaseGenerics","dorrnread",showNumber+""));
				else
					mob.tell(getScr("BaseGenerics","dorread",showNumber+"",E.readableText()));
				return;
			}
			else
			if(genGenericPrompt(mob,getScr("BaseGenerics","msgreadble"),E.isReadable()))
			{
				E.setReadable(true);
				mob.tell(getScr("BaseGenerics","readabletxt",E.readableText()));
				String newName=mob.session().prompt(getScr("BaseGenerics","some5"),"");
				if(newName.equalsIgnoreCase("null"))
					E.setReadableText("");
				else
				if(newName.length()>0)
					E.setReadableText(newName);
				else
					mob.tell(getScr("BaseGenerics","nochange"));
			}
			else
				E.setReadable(false);
		}
	}

	public static void genReadable1(MOB mob, Item E, int showNumber, int showFlag)
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;

		if((E instanceof Wand)
		 ||(E instanceof SpellHolder)
		 ||(E instanceof Light)
		 ||(E instanceof Container)
		 ||(E instanceof Ammunition)
		 ||((E instanceof ClanItem)
			 &&((((ClanItem)E).ciType()==ClanItem.CI_GATHERITEM)
				 ||(((ClanItem)E).ciType()==ClanItem.CI_CRAFTITEM)
				 ||(((ClanItem)E).ciType()==ClanItem.CI_SPECIALAPRON)))
		 ||(E instanceof Key))
			CMLib.flags().setReadable(E,false);
		else
		if((CMClass.className(E).endsWith("Readable"))
		||(E instanceof Recipe)
		||(E instanceof com.planet_ink.coffee_mud.Items.interfaces.Map))
			CMLib.flags().setReadable(E,true);
		else
		if((showFlag!=showNumber)&&(showFlag>-999))
			mob.tell(getScr("BaseGenerics","itemread",showNumber+"",CMLib.flags().isReadable(E)+""));
		else
			CMLib.flags().setReadable(E,genGenericPrompt(mob,showNumber+getScr("BaseGenerics","msgitemr"),CMLib.flags().isReadable(E)));
	}

	public static void genReadable2(MOB mob, Item E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;

		if((CMLib.flags().isReadable(E))
		 ||(E instanceof SpellHolder)
		 ||(E instanceof Ammunition)
		 ||(E instanceof Recipe)
		 ||(E instanceof Exit)
		 ||(E instanceof Wand)
		 ||(E instanceof ClanItem)
		 ||(E instanceof Light)
		 ||(E instanceof Key))
		{
			boolean ok=false;
			while((mob.session()!=null)&&(!mob.session().killFlag())&&(!ok))
			{
				if(CMClass.className(E).endsWith("SuperPill"))
				{
					mob.tell(getScr("BaseGenerics","assspellorp",showNumber+"",E.readableText()));
					ok=true;
				}
				else
				if(E instanceof SpellHolder)
					mob.tell(getScr("BaseGenerics","assspell",showNumber+"",E.readableText()));
				else
				if(E instanceof Ammunition)
				{
					mob.tell(getScr("BaseGenerics","ammotype",showNumber+"",E.readableText()));
					ok=true;
				}
				else
				if(E instanceof Exit)
				{
					mob.tell(getScr("BaseGenerics","assroomid",showNumber+"",E.readableText()));
					ok=true;
				}
				else
				if(E instanceof Wand)
					mob.tell(getScr("BaseGenerics","asspellname",showNumber+"",E.readableText()));
				else
				if(E instanceof Key)
				{
					mob.tell(getScr("BaseGenerics","asskeycode",showNumber+"",E.readableText()));
					ok=true;
				}
				else
				if(E instanceof com.planet_ink.coffee_mud.Items.interfaces.Map)
				{
					mob.tell(getScr("BaseGenerics","assmaparea",showNumber+"",E.readableText()));
					ok=true;
				}
				else
				if(E instanceof Light)
				{
					mob.tell(getScr("BaseGenerics","lightdur",showNumber+"",CMath.s_int(E.readableText())+""));
					ok=true;
				}
				else
				{
					mob.tell(getScr("BaseGenerics","assreadtxt",showNumber+"",E.readableText())+"");
					ok=true;
				}

				if((showFlag!=showNumber)&&(showFlag>-999)) return;
				String newName=null;

				if((E instanceof Wand)
				||((E instanceof SpellHolder)&&(!(CMClass.className(E).endsWith("SuperPill")))))
				{
					newName=mob.session().prompt(getScr("BaseGenerics","some6"),"");
					if(newName.length()==0)
						ok=true;
					else
					{
						if(newName.equalsIgnoreCase("?"))
							mob.tell(CMLib.lister().reallyList(CMClass.abilities(),-1).toString());
						else
						if(E instanceof Wand)
						{
							if(CMClass.getAbility(newName)!=null)
								ok=true;
							else
								mob.tell(getScr("BaseGenerics","namenotrec",newName));
						}
						else
						if(E instanceof SpellHolder)
						{
							String oldName=newName;
							if(!newName.endsWith(";")) newName+=";";
							int x=newName.indexOf(";");
							while(x>=0)
							{
								String spellName=newName.substring(0,x).trim();
								if(CMClass.getAbility(spellName)!=null)
									ok=true;
								else
								{
									mob.tell(getScr("BaseGenerics","spellnotrec",spellName));
									break;
								}
								newName=newName.substring(x+1).trim();
								x=newName.indexOf(";");
							}
							newName=oldName;
						}
					}
				}
				else
					newName=mob.session().prompt(getScr("BaseGenerics","some5"),"");

				if(ok)
				{
					if(newName.equalsIgnoreCase("null"))
						E.setReadableText("");
					else
					if(newName.length()>0)
						E.setReadableText(newName);
					else
						mob.tell(getScr("BaseGenerics","nochange"));
				}
			}
		}
		else
		if(E instanceof Drink)
		{
			mob.session().println(showNumber+getScr("BaseGenerics","curliq")+" "+RawMaterial.RESOURCE_DESCS[((Drink)E).liquidType()&RawMaterial.RESOURCE_MASK]);
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			boolean q=false;
			while((mob.session()!=null)&&(!mob.session().killFlag())&&(!q))
			{
				String newType=mob.session().prompt(getScr("BaseGenerics","newtype"),RawMaterial.RESOURCE_DESCS[((Drink)E).liquidType()&RawMaterial.RESOURCE_MASK]);
				if(newType.equals("?"))
				{
					StringBuffer say=new StringBuffer("");
					for(int i=0;i<RawMaterial.RESOURCE_DESCS.length-1;i++)
						if((RawMaterial.RESOURCE_DATA[i][0]&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID)
							say.append(RawMaterial.RESOURCE_DESCS[i]+", ");
					mob.tell(say.toString().substring(0,say.length()-2));
					q=false;
				}
				else
				{
					q=true;
					int newValue=-1;
					for(int i=0;i<RawMaterial.RESOURCE_DESCS.length-1;i++)
						if((RawMaterial.RESOURCE_DATA[i][0]&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID)
							if(newType.equalsIgnoreCase(RawMaterial.RESOURCE_DESCS[i]))
								newValue=RawMaterial.RESOURCE_DATA[i][0];
					if(newValue>=0)
						((Drink)E).setLiquidType(newValue);
					else
						mob.tell(getScr("BaseGenerics","nochange"));
				}
			}
		}
	}

	public static void genRecipe(MOB mob, Recipe E, int showNumber, int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String prompt=getScr("BaseGenerics","recipedata");
		mob.tell(showNumber+". "+prompt+": "+E.getCommonSkillID()+".");
		mob.tell(CMStrings.padRight(" ",(""+showNumber).length()+2+prompt.length())+": "+CMStrings.replaceAll(E.getRecipeCodeLine(),"\t",",")+".");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		while(!mob.session().killFlag())
		{
			String newName=mob.session().prompt(getScr("BaseGenerics","entskillid"),"");
			if(newName.equalsIgnoreCase("?"))
			{
			    StringBuffer str=new StringBuffer("");
			    Ability A=null;
				for(Enumeration e=CMClass.abilities();e.hasMoreElements();)
				{
				 	A=(Ability)e.nextElement();
				 	if(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL)
				 	&&(CMath.bset(A.flags(),Ability.FLAG_CRAFTING)))
				 	    str.append(A.ID()+"\n\r");
				}
				mob.tell(getScr("BaseGenerics","comskill",str.toString()));
			}
			else
			if((newName.length()>0)
			&&(CMClass.getAbility(newName)!=null)
			&&(CMClass.getAbility(newName).classificationCode()==Ability.ACODE_COMMON_SKILL))
			{
			    E.setCommonSkillID(CMClass.getAbility(newName).ID());
			    break;
			}
			else
			if(newName.length()>0)
			    mob.tell(getScr("BaseGenerics","cskillerr",newName));
			else
			{
				mob.tell(getScr("BaseGenerics","nochange"));
				break;
			}
		}
		String newName=mob.session().prompt(getScr("BaseGenerics","entdataline"),"");
		if(newName.length()>0)
			E.setRecipeCodeLine(CMStrings.replaceAll(newName,",","\t"));
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}

	public static void genGettable(MOB mob, Item E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		if(E instanceof Potion)
			((Potion)E).setDrunk(false);

		String c="Q";
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!c.equals("\n")))
		{
			mob.session().println(showNumber+getScr("BaseGenerics","gettable")+" "+(!CMath.bset(E.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNOTGET)));
			mob.session().println("    "+getScr("BaseGenerics","droppable")+" "+(!CMath.bset(E.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNODROP)));
			mob.session().println("    "+getScr("BaseGenerics","removable")+" "+(!CMath.bset(E.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNOREMOVE)));
			mob.session().println("    "+getScr("BaseGenerics","nonlocatable")+" "+(((E.baseEnvStats().sensesMask()&EnvStats.SENSE_UNLOCATABLE)>0)?"true":"false"));
			if(E instanceof Weapon)
				mob.session().println("    "+getScr("BaseGenerics","twohanded")+" "+E.rawLogicalAnd());
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			c=mob.session().choose(getScr("BaseGenerics","msgabcde"),"ABCDE\n","\n").toUpperCase();
			switch(Character.toUpperCase(c.charAt(0)))
			{
			case 'A': CMLib.flags().setGettable(E,(CMath.bset(E.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNOTGET))); break;
			case 'B': CMLib.flags().setDroppable(E,(CMath.bset(E.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNODROP))); break;
			case 'C': CMLib.flags().setRemovable(E,(CMath.bset(E.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNOREMOVE))); break;
			case 'D': if((E.baseEnvStats().sensesMask()&EnvStats.SENSE_UNLOCATABLE)>0)
						  E.baseEnvStats().setSensesMask(E.baseEnvStats().sensesMask()-EnvStats.SENSE_UNLOCATABLE);
					  else
						  E.baseEnvStats().setSensesMask(E.baseEnvStats().sensesMask()|EnvStats.SENSE_UNLOCATABLE);
					  break;
			case 'E': if(E instanceof Weapon)
						  E.setRawLogicalAnd(!E.rawLogicalAnd());
					  break;
			}
		}
	}

	public static void toggleDispositionMask(EnvStats E, int mask)
	{
		int current=E.disposition();
		if((current&mask)==0)
			E.setDisposition(current|mask);
		else
			E.setDisposition(current&((int)(EnvStats.ALLMASK-mask)));
	}

	public static void genDisposition(MOB mob, EnvStats E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		int[] disps={EnvStats.IS_INVISIBLE,
					 EnvStats.IS_HIDDEN,
					 EnvStats.IS_NOT_SEEN,
					 EnvStats.IS_BONUS,
					 EnvStats.IS_GLOWING,
					 EnvStats.IS_LIGHTSOURCE,
					 EnvStats.IS_FLYING,
					 EnvStats.IS_CLIMBING,
					 EnvStats.IS_SNEAKING,
					 EnvStats.IS_SWIMMING,
					 EnvStats.IS_EVIL,
					 EnvStats.IS_GOOD};
		String[] briefs={"invisible",
						 "hide",
						 "unseen",
						 "magical",
						 "glowing",
						 "lightsrc",
						 "fly",
						 "climb",
						 "sneak",
						 "swimmer",
						 "evil",
						 "good"};
		if((showFlag!=showNumber)&&(showFlag>-999))
		{
			StringBuffer buf=new StringBuffer(getScr("BaseGenerics","disptxt",showNumber+""));
			for(int i=0;i<disps.length;i++)
			{
				int mask=disps[i];
				if((E.disposition()&mask)!=0)
					buf.append(briefs[i]+" ");
			}
			mob.tell(buf.toString());
			return;
		}
		String c="Q";
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!c.equals("\n")))
		{
			char letter='A';
			String letters="";
			for(int i=0;i<disps.length;i++)
			{
				int mask=disps[i];
				for(int num=0;num<EnvStats.dispositionsDesc.length;num++)
					if(mask==CMath.pow(2,num))
					{
						mob.session().println("    "+letter+") "+CMStrings.padRight(EnvStats.dispositionsDesc[num],20)+":"+((E.disposition()&mask)!=0));
						letters+=letter;
						break;
					}
				letter++;
			}
			c=mob.session().choose(getScr("BaseGenerics","msgabcde")+" ",letters+"\n","\n").toUpperCase();
			letter='A';
			for(int i=0;i<disps.length;i++)
			{
				int mask=disps[i];
				if(letter==Character.toUpperCase(c.charAt(0)))
				{
					toggleDispositionMask(E,mask);
					break;
				}
				letter++;
			}
		}
	}

	public static boolean genGenericPrompt(MOB mob, String prompt, boolean val)
	{
		try
		{
			prompt=CMStrings.padRight(prompt,35);
			if(val)
				prompt+="(Y/n): ";
			else
				prompt+="(y/N): ";

			return mob.session().confirm(prompt,val?"Y":"N");
		}
		catch(IOException e)
		{
			return val;
		}
	}

	public static void toggleSensesMask(EnvStats E, int mask)
	{
		int current=E.sensesMask();
		if((current&mask)==0)
			E.setSensesMask(current|mask);
		else
			E.setSensesMask(current&((int)(EnvStats.ALLMASK-mask)));
	}

	public static void toggleClimateMask(Area A, int mask)
	{
		int current=A.climateType();
		if((current&mask)==0)
			A.setClimateType(current|mask);
		else
			A.setClimateType(current&((int)(EnvStats.ALLMASK-mask)));
	}



	public static void genClimateType(MOB mob, Area A, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String c="Q";
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!c.equals("\n")))
		{
			mob.session().println(""+showNumber+getScr("BaseGenerics","climate"));
			mob.session().println("    "+getScr("BaseGenerics","wetrainy")+" "+((A.climateType()&Area.CLIMASK_WET)>0));
			mob.session().println("    "+getScr("BaseGenerics","exhot")+" "+((A.climateType()&Area.CLIMASK_HOT)>0));
			mob.session().println("    "+getScr("BaseGenerics","excold")+" "+((A.climateType()&Area.CLIMASK_COLD)>0));
			mob.session().println("    "+getScr("BaseGenerics","verywindy")+" "+((A.climateType()&Area.CLIMATE_WINDY)>0));
			mob.session().println("    "+getScr("BaseGenerics","verydry")+" "+((A.climateType()&Area.CLIMASK_DRY)>0));
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			c=mob.session().choose(getScr("BaseGenerics","msgabcde")+" ","RHCWD\n","\n").toUpperCase();
			switch(c.charAt(0))
			{
			case 'C': toggleClimateMask(A,Area.CLIMASK_COLD); break;
			case 'H': toggleClimateMask(A,Area.CLIMASK_HOT); break;
			case 'R': toggleClimateMask(A,Area.CLIMASK_WET); break;
			case 'W': toggleClimateMask(A,Area.CLIMATE_WINDY); break;
			case 'D': toggleClimateMask(A,Area.CLIMASK_DRY); break;
			}
		}
	}

    public static void genCharStats(MOB mob, CharStats E)
    throws IOException
    {
        String c="Q";
        String commandStr="ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%^&*()=+-";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(!c.equals("\n")))
        {
            for(int i=0;i<CharStats.STAT_DESCS.length;i++)
                if(i!=CharStats.STAT_GENDER)
                    mob.session().println("    "+commandStr.charAt(i)+") "+CMStrings.padRight(CharStats.STAT_DESCS[i],20)+":"+((E.getStat(i))));
            c=mob.session().choose(getScr("BaseGenerics","msgabcde")+" ",commandStr.substring(0,CharStats.STAT_DESCS.length)+"\n","\n").toUpperCase();
            int num=commandStr.indexOf(c);
            if(num>=0)
            {
                String newVal=mob.session().prompt(getScr("BaseGenerics","entnewvalue")+" "+CharStats.STAT_DESCS[num]+" ("+E.getStat(num)+"): ","");
                if(((CMath.s_int(newVal)>0)||(newVal.trim().equals("0")))
                &&(num!=CharStats.STAT_GENDER))
                    E.setStat(num,CMath.s_int(newVal));
                else
                    mob.tell(getScr("BaseGenerics","nochange"));
            }
        }
    }
    
    
	public static void genCharStats(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		if((showFlag!=showNumber)&&(showFlag>-999))
		{
			StringBuffer buf=new StringBuffer(showNumber+getScr("BaseGenerics","stats")+" ");
			for(int i=0;i<CharStats.NUM_BASE_STATS;i++)
				buf.append(CharStats.STAT_ABBR[i]+":"+E.baseCharStats().getStat(i)+" ");
			mob.tell(buf.toString());
			return;
		}
		String c="Q";
		String commandStr="ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%^&*()=+-";
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!c.equals("\n")))
		{
			for(int i=0;i<CharStats.STAT_DESCS.length;i++)
				if(i!=CharStats.STAT_GENDER)
					mob.session().println("    "+commandStr.charAt(i)+") "+CMStrings.padRight(CharStats.STAT_DESCS[i],20)+":"+((E.baseCharStats().getStat(i))));
			c=mob.session().choose(getScr("BaseGenerics","msgabcde")+" ",commandStr.substring(0,CharStats.STAT_DESCS.length)+"\n","\n").toUpperCase();
			int num=commandStr.indexOf(c);
			if(num>=0)
			{
				String newVal=mob.session().prompt(getScr("BaseGenerics","entnewvalue")+" "+CharStats.STAT_DESCS[num]+" ("+E.baseCharStats().getStat(num)+"): ","");
				if(((CMath.s_int(newVal)>0)||(newVal.trim().equals("0")))
				&&(num!=CharStats.STAT_GENDER))
				{
					E.baseCharStats().setStat(num,CMath.s_int(newVal));
					if((num==CharStats.STAT_AGE)&&(E.playerStats()!=null)&&(E.playerStats().getBirthday()!=null))
					    E.playerStats().getBirthday()[2]=CMClass.globalClock().getYear()-CMath.s_int(newVal);
				}
				else
					mob.tell(getScr("BaseGenerics","nochange"));
			}
		}
	}

	public static void genSensesMask(MOB mob, EnvStats E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		int[] senses={EnvStats.CAN_SEE_DARK,
					  EnvStats.CAN_SEE_HIDDEN,
					  EnvStats.CAN_SEE_INVISIBLE,
					  EnvStats.CAN_SEE_SNEAKERS,
					  EnvStats.CAN_SEE_INFRARED,
					  EnvStats.CAN_SEE_GOOD,
					  EnvStats.CAN_SEE_EVIL,
					  EnvStats.CAN_SEE_BONUS,
					  EnvStats.CAN_NOT_SPEAK,
					  EnvStats.CAN_NOT_HEAR,
					  EnvStats.CAN_NOT_SEE};
		String[] briefs={"darkvision",
						 "hidden",
						 "invisible",
						 "sneakers",
						 "infrared",
						 "good",
						 "evil",
						 "magic",
						 "MUTE",
						 "DEAF",
						 "BLIND"};
		if((showFlag!=showNumber)&&(showFlag>-999))
		{
			StringBuffer buf=new StringBuffer(showNumber+getScr("BaseGenerics","senses")+" ");
			for(int i=0;i<senses.length;i++)
			{
				int mask=senses[i];
				if((E.sensesMask()&mask)!=0)
					buf.append(briefs[i]+" ");
			}
			mob.tell(buf.toString());
			return;
		}
		String c="Q";
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!c.equals("\n")))
		{
			char letter='A';
			String letters="";
			for(int i=0;i<senses.length;i++)
			{
				int mask=senses[i];
				for(int num=0;num<EnvStats.sensesDesc.length;num++)
					if(mask==CMath.pow(2,num))
					{
						letters+=letter;
						mob.session().println("    "+letter+") "+CMStrings.padRight(EnvStats.sensesDesc[num],20)+":"+((E.sensesMask()&mask)!=0));
						break;
					}
				letter++;
			}
			c=mob.session().choose(getScr("BaseGenerics","msgabcde")+" ",letters+"\n","\n").toUpperCase();
			letter='A';
			for(int i=0;i<senses.length;i++)
			{
				int mask=senses[i];
				if(letter==Character.toUpperCase(c.charAt(0)))
				{
					toggleSensesMask(E,mask);
					break;
				}
				letter++;
			}
		}
	}

	public static void genDoorsNLocks(MOB mob, Exit E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		boolean HasDoor=E.hasADoor();
		boolean Open=E.isOpen();
		boolean DefaultsClosed=E.defaultsClosed();
		boolean HasLock=E.hasALock();
		boolean Locked=E.isLocked();
		boolean DefaultsLocked=E.defaultsLocked();
		if((showFlag!=showNumber)&&(showFlag>-999)){
			mob.tell(showNumber+getScr("BaseGenerics","hasadoor")+" "+E.hasADoor()
					+getScr("BaseGenerics","hasalock")+" "+E.hasALock()
					+getScr("BaseGenerics","openticks")+" "+E.openDelayTicks());
			return;
		}

		if(genGenericPrompt(mob,getScr("BaseGenerics","hasadoor2"),E.hasADoor()))
		{
			HasDoor=true;
			DefaultsClosed=genGenericPrompt(mob,getScr("BaseGenerics","defclosed"),E.defaultsClosed());
			Open=!DefaultsClosed;
			if(genGenericPrompt(mob,getScr("BaseGenerics","hasalock2"),E.hasALock()))
			{
				HasLock=true;
				DefaultsLocked=genGenericPrompt(mob,getScr("BaseGenerics","deflocked"),E.defaultsLocked());
				Locked=DefaultsLocked;
			}
			else
			{
				HasLock=false;
				Locked=false;
				DefaultsLocked=false;
			}
			mob.tell(getScr("BaseGenerics","resetdticks",E.openDelayTicks()+""));
			int newLevel=CMath.s_int(mob.session().prompt(getScr("BaseGenerics","entdelay"),""));
			if(newLevel>0)
				E.setOpenDelayTicks(newLevel);
			else
				mob.tell(getScr("BaseGenerics","nochange"));
		}
		else
		{
			HasDoor=false;
			Open=true;
			DefaultsClosed=false;
			HasLock=false;
			Locked=false;
			DefaultsLocked=false;
		}
		E.setDoorsNLocks(HasDoor,Open,DefaultsClosed,HasLock,Locked,DefaultsLocked);
	}

	public static String makeContainerTypes(Container E)
	{
		String canContain=", "+Container.CONTAIN_DESCS[0];
		if(E.containTypes()>0)
		{
			canContain="";
			for(int i=0;i<20;i++)
				if(CMath.isSet((int)E.containTypes(),i))
					canContain+=", "+Container.CONTAIN_DESCS[i+1];
		}
		return canContain.substring(2);
	}


	public static void genLidsNLocks(MOB mob, Container E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		if((showFlag!=showNumber)&&(showFlag>-999)){
			mob.tell(showNumber+getScr("BaseGenerics","cancontaini")+" "+makeContainerTypes(E)
					+getScr("BaseGenerics","hasalid")+" "+E.hasALid()
					+getScr("BaseGenerics","hasalock")+" "+E.hasALock()
					+getScr("BaseGenerics","keycode")+" "+E.keyName());
			return;
		}
		String change="NO";
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(change.length()>0))
		{
			mob.tell(getScr("BaseGenerics","cancontain",makeContainerTypes(E)));
			change=mob.session().prompt(getScr("BaseGenerics","typeaddrem"),"");
			if(change.length()==0) break;
			int found=-1;
			if(change.equalsIgnoreCase("?"))
				for(int i=0;i<Container.CONTAIN_DESCS.length;i++)
					mob.tell(Container.CONTAIN_DESCS[i]);
			else
			{
				for(int i=0;i<Container.CONTAIN_DESCS.length;i++)
					if(Container.CONTAIN_DESCS[i].startsWith(change.toUpperCase()))
						found=i;
				if(found<0)
					mob.tell(getScr("BaseGenerics","unktype"));
				else
				if(found==0)
					E.setContainTypes(0);
				else
				if(CMath.isSet((int)E.containTypes(),found-1))
					E.setContainTypes(E.containTypes()-CMath.pow(2,found-1));
				else
					E.setContainTypes(E.containTypes()|CMath.pow(2,found-1));
			}
		}

		if(genGenericPrompt(mob,getScr("BaseGenerics","hasalidmsg")+" " ,E.hasALid()))
		{
			E.setLidsNLocks(true,false,E.hasALock(),E.isLocked());
			if(genGenericPrompt(mob,getScr("BaseGenerics","hasalockmsg"),E.hasALock()))
			{
				E.setLidsNLocks(E.hasALid(),E.isOpen(),true,true);
				mob.tell(getScr("BaseGenerics","textkey",E.keyName()));
				String newName=mob.session().prompt(getScr("BaseGenerics","some4"),"");
				if(newName.length()>0)
					E.setKeyName(newName);
				else
					mob.tell(getScr("BaseGenerics","nochange"));
			}
			else
			{
				E.setKeyName("");
				E.setLidsNLocks(E.hasALid(),E.isOpen(),false,false);
			}
		}
		else
		{
			E.setKeyName("");
			E.setLidsNLocks(false,true,false,false);
		}
	}

	public static void genLevel(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		if(E.baseEnvStats().level()<0)
			E.baseEnvStats().setLevel(1);
		mob.tell(getScr("BaseGenerics","leveltxt",showNumber+"",E.baseEnvStats().level()+""));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.baseEnvStats().setLevel(getNumericData(mob,getScr("BaseGenerics","entlev"),E.baseEnvStats().level()));
	}

	public static void genRejuv(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		if(E instanceof Item)
			mob.tell(getScr("BaseGenerics","rejuv",showNumber+"",E.baseEnvStats().rejuv()+""));
		else
			mob.tell(getScr("BaseGenerics","rejuvticks",showNumber+"",E.baseEnvStats().rejuv()+""));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String rlevel=mob.session().prompt(getScr("BaseGenerics","entamount"),"");
		int newLevel=CMath.s_int(rlevel);
		if((newLevel>0)||(rlevel.trim().equals("0")))
		{
			E.baseEnvStats().setRejuv(newLevel);
			if((E.baseEnvStats().rejuv()==0)&&(E instanceof MOB))
			{
				E.baseEnvStats().setRejuv(Integer.MAX_VALUE);
				mob.tell(getScr("BaseGenerics","neverrej",E.Name()));
			}
		}
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}

	public static void genUses(MOB mob, Item E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","usesrema",showNumber+"",E.usesRemaining()+""));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.setUsesRemaining(getNumericData(mob,getScr("BaseGenerics","entvaluenew"),E.usesRemaining()));
	}

	public static void genMaxUses(MOB mob, Wand E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","maxuses",showNumber+"",E.maxUses()+"" ));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.setMaxUses(getNumericData(mob,getScr("BaseGenerics","entvaluenew"),E.maxUses()));
	}

	public static void genCondition(MOB mob, Item E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","condition",showNumber+"",E.usesRemaining()+""));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.setUsesRemaining(getNumericData(mob,getScr("BaseGenerics","entvaluenew"),E.usesRemaining()));
	}

	public static void genMiscSet(MOB mob, Environmental E)
		throws IOException
	{
		if(E instanceof ShopKeeper)
			modifyGenShopkeeper(mob,(ShopKeeper)E);
		else
		if(E instanceof MOB)
		{
			if(((MOB)E).isMonster())
				modifyGenMOB(mob,(MOB)E);
			else
				modifyPlayer(mob,(MOB)E);
		}
		else
		if((E instanceof Exit)&&(!(E instanceof Item)))
			modifyGenExit(mob,(Exit)E);
		else
		if(E instanceof com.planet_ink.coffee_mud.Items.interfaces.Map)
			modifyGenMap(mob,(com.planet_ink.coffee_mud.Items.interfaces.Map)E);
		else
		if(E instanceof Armor)
			modifyGenArmor(mob,(Armor)E);
		else
		if(E instanceof MusicalInstrument)
			modifyGenInstrument(mob,(MusicalInstrument)E);
		else
		if(E instanceof Food)
			modifyGenFood(mob,(Food)E);
		else
		if((E instanceof Drink)&&(E instanceof Item))
			modifyGenDrink(mob,(Drink)E);
		else
		if(E instanceof Weapon)
			modifyGenWeapon(mob,(Weapon)E);
		else
		if(E instanceof Container)
			modifyGenContainer(mob,(Container)E);
		else
		if(E instanceof Item)
		{
			if(E.ID().equals("GenWallpaper"))
				modifyGenWallpaper(mob,(Item)E);
			else
				modifyGenItem(mob,(Item)E);
		}
	}


	public static int getNumericData(MOB mob, String prompt, int oldValue)
		throws IOException
	{
		String value=mob.session().prompt(prompt,"");
		int numValue=CMath.s_int(value);
		if((numValue==0)&&(!value.trim().equals("0")))
		{
			mob.tell(getScr("BaseGenerics","nochange"));
			return oldValue;
		}
		return numValue;
	}

	public static boolean getBooleanData(MOB mob, String prompt, boolean oldValue)
	throws IOException
	{
		boolean bool=mob.session().confirm(prompt,oldValue?"Y":"N");
		if(bool==oldValue)
			mob.tell(getScr("BaseGenerics","nochange"));
		return bool;
	}
	
	public static long getLongData(MOB mob, String prompt, long oldValue)
	throws IOException
	{
		String value=mob.session().prompt(prompt,"");
		long numValue=CMath.s_long(value);
		if((numValue==0)&&(!value.trim().equals("0")))
		{
			mob.tell(getScr("BaseGenerics","nochange"));
			return oldValue;
		}
		return numValue;
	}

	public static String getTextData(MOB mob, String prompt, String oldValue)
	throws IOException
	{
		String value=mob.session().prompt(prompt,"").trim();
		if(value.length()==0)
		{
			mob.tell(getScr("BaseGenerics","nochange"));
			return oldValue;
		}
		if(value.equalsIgnoreCase("null"))
		    value="";
		return value;
	}

	public static double getDoubleData(MOB mob, String prompt, double oldValue)
		throws IOException
	{
		String value=mob.session().prompt(prompt,"");
		double numValue=CMath.s_double(value);
		if((numValue==0.0)&&(!value.trim().equals("0")))
		{
			mob.tell(getScr("BaseGenerics","nochange"));
			return oldValue;
		}
		return numValue;
	}

	public static void genMiscText(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if(E.isGeneric())
			genMiscSet(mob,E);
		else
		{
			if((showFlag>0)&&(showFlag!=showNumber)) return;
			mob.tell(getScr("BaseGenerics","misctxt",showNumber+"",E.text()));
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			String newText=mob.session().prompt(getScr("BaseGenerics","reent"),"");
			if(newText.equalsIgnoreCase("NULL"))
				E.setMiscText("");
			else
			if(newText.length()>0)
				E.setMiscText(newText);
			else
				mob.tell(getScr("BaseGenerics","nochange"));
		}

	}

	public static void genTitleRoom(MOB mob, LandTitle E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","landplotid",showNumber+"",E.landPropertyID()));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newText="?!?!";
		while((mob.session()!=null)&&(!mob.session().killFlag())
			&&((newText.length()>0)&&(CMLib.map().getRoom(newText)==null)))
		{
			newText=mob.session().prompt(getScr("BaseGenerics","newprpid"),"");
			if((newText.length()==0)
			&&(CMLib.map().getRoom(newText)==null)
			&&(CMLib.map().getArea(newText)==null))
				mob.tell(getScr("BaseGenerics","roomiderror"));
		}
		if(newText.length()>0)
			E.setLandPropertyID(newText);
		else
			mob.tell(getScr("BaseGenerics","nochange"));

	}

	public static void genAbility(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","magicalability",showNumber+"",E.baseEnvStats().ability()+""));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.baseEnvStats().setAbility(getNumericData(mob,getScr("BaseGenerics","zeronomag"),E.baseEnvStats().ability()));
	}

	public static void genCoinStuff(MOB mob, Coins E, int showNumber, int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","moneydata",showNumber+"",E.getNumberOfCoins()+"",CMLib.beanCounter().getDenominationName(E.getCurrency(),E.getDenomination())));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		boolean gocontinue=true;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(gocontinue))
		{
		    gocontinue=false;
		    String oldCurrency=E.getCurrency();
		    if(oldCurrency.length()==0) oldCurrency="Default";
			oldCurrency=mob.session().prompt(getScr("BaseGenerics","entcurcode"),oldCurrency).trim().toUpperCase();
			if(oldCurrency.equalsIgnoreCase("Default"))
			{
			    if(E.getCurrency().length()>0)
				    E.setCurrency("");
			    else
			        mob.tell(getScr("BaseGenerics","nochange"));
			}
			else
			if((oldCurrency.length()==0)||(oldCurrency.equalsIgnoreCase(E.getCurrency())))
			    mob.tell(getScr("BaseGenerics","nochange"));
			else
			if(!CMLib.beanCounter().getAllCurrencies().contains(oldCurrency))
			{
			    Vector V=CMLib.beanCounter().getAllCurrencies();
			    for(int v=0;v<V.size();v++)
			        if(((String)V.elementAt(v)).length()==0)
			            V.setElementAt("Default",v);
			    mob.tell(getScr("BaseGenerics","currencyerr",oldCurrency,CMParms.toStringList(V)));
			    gocontinue=true;
			}
			else
			    E.setCurrency(oldCurrency.toUpperCase().trim());
		}
		gocontinue=true;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(gocontinue))
		{
		    gocontinue=false;
		    String newDenom=mob.session().prompt(getScr("BaseGenerics","entde"),""+E.getDenomination()).trim().toUpperCase();
			DVector DV=CMLib.beanCounter().getCurrencySet(E.getCurrency());
			if((newDenom.length()>0)
			&&(!CMath.isDouble(newDenom))
			&&(!newDenom.equalsIgnoreCase("?")))
			{
			    double denom=CMLib.english().matchAnyDenomination(E.getCurrency(),newDenom);
			    if(denom>0.0) newDenom=""+denom;
			}
		    if((newDenom.length()==0)
		    ||(CMath.isDouble(newDenom)
	            &&(!newDenom.equalsIgnoreCase("?"))
	            &&(CMath.s_double(newDenom)==E.getDenomination())))
			        mob.tell(getScr("BaseGenerics","nochange"));
		    else
			if((!CMath.isDouble(newDenom))
			||(newDenom.equalsIgnoreCase("?"))
			||((DV!=null)&&(!DV.contains(new Double(CMath.s_double(newDenom))))))
			{
			    StringBuffer allDenoms=new StringBuffer("");
			    for(int i=0;i<DV.size();i++)
			        allDenoms.append(((Double)DV.elementAt(i,1)).doubleValue()+"("+((String)DV.elementAt(i,2))+"), ");
			    if(allDenoms.toString().endsWith(", "))
			        allDenoms=new StringBuffer(allDenoms.substring(0,allDenoms.length()-2));
			    mob.tell(getScr("BaseGenerics","denomerr",newDenom,allDenoms.toString()));
			    gocontinue=true;
			}
			else
			    E.setDenomination(CMath.s_double(newDenom));
		}
		E.setNumberOfCoins(getLongData(mob,getScr("BaseGenerics","entstacksize"),E.getNumberOfCoins()));
	}

	public static void genHitPoints(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		if(E.isMonster())
			mob.tell(getScr("BaseGenerics","hitpointsmod",showNumber+"",E.baseEnvStats().ability()+""));
		else
			mob.tell(getScr("BaseGenerics","hitpointsmodp",showNumber+"",E.baseEnvStats().ability()+""));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newLevelStr=mob.session().prompt(getScr("BaseGenerics","entvaluenew"),"");
		int newLevel=CMath.s_int(newLevelStr);
		if((newLevel!=0)||(newLevelStr.equals("0")))
			E.baseEnvStats().setAbility(newLevel);
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}

	public static void genValue(MOB mob, Item E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","basevalue",showNumber+"",E.baseGoldValue()+""));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.setBaseValue(getNumericData(mob,getScr("BaseGenerics","entvaluenew"),E.baseGoldValue()));
	}

	public static void genWeight(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","weight",showNumber+"",E.baseEnvStats().weight()+""));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.baseEnvStats().setWeight(getNumericData(mob,getScr("BaseGenerics","weightmsg"),E.baseEnvStats().weight()));
	}


	public static void genClanItem(MOB mob, ClanItem E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","clanline",showNumber+"",E.clanID(),ClanItem.CI_DESC[E.ciType()]));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String clanID=E.clanID();
		E.setClanID(mob.session().prompt(getScr("BaseGenerics","entclan"),clanID));
		if(E.clanID().equals(clanID))
			mob.tell(getScr("BaseGenerics","nochange"));
		String clanType=ClanItem.CI_DESC[E.ciType()];
		String s="?";
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(s.equals("?")))
		{
			s=mob.session().prompt(getScr("BaseGenerics","newtype"),clanType);
			if(s.equalsIgnoreCase("?"))
				mob.tell(getScr("BaseGenerics","typesclan",CMParms.toStringList(ClanItem.CI_DESC)));
			else
			if(s.equalsIgnoreCase(clanType))
			{
				mob.tell(getScr("BaseGenerics","nochange"));
				break;
			}
			else
			{
				boolean found=false;
				for(int i=0;i<ClanItem.CI_DESC.length;i++)
					if(ClanItem.CI_DESC[i].equalsIgnoreCase(s))
					{ found=true; E.setCIType(i); break;}
				if(!found)
				{
					mob.tell(getScr("BaseGenerics","unknown",s));
					s="?";
				}
			}
		}
	}

	public static void genHeight(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","height",showNumber+"",E.baseEnvStats().height()+""));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.baseEnvStats().setHeight(getNumericData(mob,getScr("BaseGenerics","heightmsg"),E.baseEnvStats().height()));
	}


	public static void genSize(MOB mob, Armor E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","size",showNumber+"",E.baseEnvStats().height()+""));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.baseEnvStats().setHeight(getNumericData(mob,getScr("BaseGenerics","entnewsize"),E.baseEnvStats().height()));
	}


	public static void genLayer(MOB mob, Armor E, int showNumber, int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))  return;
		boolean seeThroughBool=CMath.bset(E.getLayerAttributes(),Armor.LAYERMASK_SEETHROUGH);
		boolean multiWearBool=CMath.bset(E.getLayerAttributes(),Armor.LAYERMASK_MULTIWEAR);
		String seeThroughStr=(!seeThroughBool)?" (opaque)":" (see-through)";
		String multiWearStr=multiWearBool?" (multi)":"";
		mob.tell(getScr("BaseGenerics","layer",showNumber+"",E.getClothingLayer()+"",seeThroughStr,multiWearStr));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.setClothingLayer((short)getNumericData(mob,getScr("BaseGenerics","newlayer"),E.getClothingLayer()));
		boolean newSeeThrough=getBooleanData(mob,getScr("BaseGenerics","newseethrough"),seeThroughBool);
		boolean multiWear=getBooleanData(mob,getScr("BaseGenerics","newmultiwear"),multiWearBool);
		E.setLayerAttributes((short)0);
		E.setLayerAttributes((short)(E.getLayerAttributes()|(newSeeThrough?Armor.LAYERMASK_SEETHROUGH:0)));
		E.setLayerAttributes((short)(E.getLayerAttributes()|(multiWear?Armor.LAYERMASK_MULTIWEAR:0)));
	}
	
	
	public static void genCapacity(MOB mob, Container E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","capacity",showNumber+"",E.capacity()+""));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.setCapacity(getNumericData(mob,getScr("BaseGenerics","entnewcap"),E.capacity()));
	}

	public static void genAttack(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","attackadj",showNumber+"",E.baseEnvStats().attackAdjustment()+""));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.baseEnvStats().setAttackAdjustment(getNumericData(mob,getScr("BaseGenerics","entvaluenew"),E.baseEnvStats().attackAdjustment()));
	}

	public static void genDamage(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","damagehit",showNumber+"",E.baseEnvStats().damage()+""));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.baseEnvStats().setDamage(getNumericData(mob,getScr("BaseGenerics","entvaluenew"),E.baseEnvStats().damage()));
	}

	public static void genBanker1(MOB mob, Banker E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","coininter",showNumber+"",E.getCoinInterest()+""));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.setCoinInterest(getDoubleData(mob,getScr("BaseGenerics","entvaluenew"),E.getCoinInterest()));
	}
	public static void genBanker2(MOB mob, Banker E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","iteminter",showNumber+"",E.getItemInterest()+""));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.setItemInterest(getDoubleData(mob,getScr("BaseGenerics","entvaluenew"),E.getItemInterest()));
	}
	public static void genBanker3(MOB mob, Banker E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","bankchain",showNumber+"",E.bankChain()));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt(getScr("BaseGenerics","entnewchain"),"");
		if(newValue.length()>0)
			E.setBankChain(newValue);
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}
	public static void genBanker4(MOB mob, Banker E, int showNumber, int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","loaninter",showNumber+"",E.getLoanInterest()+""));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.setLoanInterest(getDoubleData(mob,getScr("BaseGenerics","entvaluenew"),E.getLoanInterest()));
	}

	public static void genSpeed(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","attacksperound",showNumber+"",((int)Math.round(E.baseEnvStats().speed())+"")));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.baseEnvStats().setSpeed(getNumericData(mob,getScr("BaseGenerics","entvaluenew"),(int)Math.round(E.baseEnvStats().speed())));
	}

	public static void genArmor(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		if(E instanceof MOB)
			mob.tell(getScr("BaseGenerics","armorlb",showNumber+"",E.baseEnvStats().armor()+""));
		else
			mob.tell(getScr("BaseGenerics","armorhb",showNumber+"",E.baseEnvStats().armor()+""));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.baseEnvStats().setArmor(getNumericData(mob,getScr("BaseGenerics","entvaluenew"),E.baseEnvStats().armor()));
	}

	public static void genMoney(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","moneycounter",showNumber+"",CMLib.beanCounter().getMoney(E)+""));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		CMLib.beanCounter().setMoney(E,getNumericData(mob,getScr("BaseGenerics","entvaluenew"),CMLib.beanCounter().getMoney(E)));
	}

	public static void genWeaponAmmo(MOB mob, Weapon E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String defaultAmmo=(E.requiresAmmunition())?"Y":"N";
		if((showFlag!=showNumber)&&(showFlag>-999))
		{
			mob.tell(getScr("BaseGenerics","ammorequi",showNumber+"",E.requiresAmmunition()+"",E.ammunitionType()+""));
			return;
		}

		if(mob.session().confirm(getScr("BaseGenerics","confammo",defaultAmmo),defaultAmmo))
		{
			mob.tell(getScr("BaseGenerics","ammotype2",E.ammunitionType()));
			String newName=mob.session().prompt(getScr("BaseGenerics","enternewone"),"");
			if(newName.length()>0)
			{
				E.setAmmunitionType(newName);
				mob.tell(getScr("BaseGenerics","remembergenitem",E.ammunitionType()));
			}
			else
				mob.tell(getScr("BaseGenerics","nochange"));
			mob.tell(getScr("BaseGenerics","ammocapacity",E.ammunitionCapacity()+""));
			int newValue=CMath.s_int(mob.session().prompt(getScr("BaseGenerics","entvaluenew"),""));
			if(newValue>0)
				E.setAmmoCapacity(newValue);
			else
				mob.tell(getScr("BaseGenerics","nochange"));
			E.setAmmoRemaining(E.ammunitionCapacity());
		}
		else
		{
			E.setAmmunitionType("");
			E.setAmmoCapacity(0);
		}
	}
	public static void genWeaponRanges(MOB mob, Weapon E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","minmaxranges",showNumber+"",Math.round(E.minRange())+"",Math.round(E.maxRange())+""));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newMinStr=mob.session().prompt(getScr("BaseGenerics","minrangemsg"),"");
		String newMaxStr=mob.session().prompt(getScr("BaseGenerics","maxrangemsg"),"");
		if((newMinStr.length()==0)&&(newMaxStr.length()==0))
			mob.tell(getScr("BaseGenerics","nochange"));
		else
		{
			E.setRanges(CMath.s_int(newMinStr),CMath.s_int(newMaxStr));
			if((E.minRange()>E.maxRange())||(E.minRange()<0)||(E.maxRange()<0))
			{
				mob.tell(getScr("BaseGenerics","defectiveent"));
				E.setRanges(0,0);
			}
		}
	}

	public static void genWeaponType(MOB mob, Weapon E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","weapatttype",showNumber+"",Weapon.typeDescription[E.weaponType()]));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		boolean q=false;
		String sel="NSPBFMR";
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!q))
		{
			String newType=mob.session().choose(getScr("BaseGenerics","entvaluenew"),sel+"?","");
			if(newType.equals("?"))
			{
				for(int i=0;i<sel.length();i++)
					mob.tell(sel.charAt(i)+") "+Weapon.typeDescription[i]);
				q=false;
			}
			else
			{
				q=true;
				int newValue=-1;
				if(newType.length()>0)
					newValue=sel.indexOf(newType.toUpperCase());
				if(newValue>=0)
					E.setWeaponType(newValue);
				else
					mob.tell(getScr("BaseGenerics","nochange"));
			}
		}
	}

	public static void genTechLevel(MOB mob, Area A, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","themesettings",showNumber+"",Area.THEME_DESCS[A.getTechLevel()]));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		boolean q=false;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!q))
		{
			String newType=mob.session().prompt(getScr("BaseGenerics","entnewlev"),Area.THEME_DESCS[A.getTechLevel()]);
			if(newType.equals("?"))
			{
				StringBuffer say=new StringBuffer("");
				for(int i=1;i<Area.THEME_DESCS.length;i++)
					say.append(i+") "+Area.THEME_DESCS[i]+"\n\r");
				mob.tell(say.toString());
				q=false;
			}
			else
			{
				q=true;
				int newValue=-1;
				if(CMath.s_int(newType)>0)
				    newValue=CMath.s_int(newType);
				else
				for(int i=0;i<Area.THEME_DESCS.length;i++)
					if(Area.THEME_DESCS[i].toUpperCase().startsWith(newType.toUpperCase()))
						newValue=i;
				if(newValue>=0)
					A.setTechLevel(newValue);
				else
					mob.tell(getScr("BaseGenerics","nochange"));
			}
		}
	}


	public static void genMaterialCode(MOB mob, Item E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","materialtype",showNumber+"",RawMaterial.RESOURCE_DESCS[E.material()&RawMaterial.RESOURCE_MASK]));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		boolean q=false;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!q))
		{
			String newType=mob.session().prompt(getScr("BaseGenerics","entnewmaterial"),RawMaterial.RESOURCE_DESCS[E.material()&RawMaterial.RESOURCE_MASK]);
			if(newType.equals("?"))
			{
				StringBuffer say=new StringBuffer("");
				for(int i=0;i<RawMaterial.RESOURCE_DESCS.length-1;i++)
					say.append(RawMaterial.RESOURCE_DESCS[i]+", ");
				mob.tell(say.toString().substring(0,say.length()-2));
				q=false;
			}
			else
			{
				q=true;
				int newValue=-1;
				for(int i=0;i<RawMaterial.RESOURCE_DESCS.length-1;i++)
					if(newType.equalsIgnoreCase(RawMaterial.RESOURCE_DESCS[i]))
						newValue=RawMaterial.RESOURCE_DATA[i][0];
				if(newValue>=0)
					E.setMaterial(newValue);
				else
					mob.tell(getScr("BaseGenerics","nochange"));
			}
		}
	}

	public static void genInstrumentType(MOB mob, MusicalInstrument E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","instrumentype",showNumber+"",MusicalInstrument.TYPE_DESC[E.instrumentType()]));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		boolean q=false;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!q))
		{
			String newType=mob.session().prompt(getScr("BaseGenerics","entnewty"),MusicalInstrument.TYPE_DESC[E.instrumentType()]);
			if(newType.equals("?"))
			{
				StringBuffer say=new StringBuffer("");
				for(int i=0;i<MusicalInstrument.TYPE_DESC.length-1;i++)
					say.append(MusicalInstrument.TYPE_DESC[i]+", ");
				mob.tell(say.toString().substring(0,say.length()-2));
				q=false;
			}
			else
			{
				q=true;
				int newValue=-1;
				for(int i=0;i<MusicalInstrument.TYPE_DESC.length-1;i++)
					if(newType.equalsIgnoreCase(MusicalInstrument.TYPE_DESC[i]))
						newValue=i;
				if(newValue>=0)
					E.setInstrumentType(newValue);
				else
					mob.tell(getScr("BaseGenerics","nochange"));
			}
		}
	}

    public static void genSpecialFaction(MOB mob, MOB E, int showNumber, int showFlag, Faction F)
    throws IOException
    {
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		if(F==null) return;
		Faction.FactionRange myFR=CMLib.factions().getRange(F.factionID(),E.fetchFaction(F.factionID()));
		mob.tell(showNumber+". "+F.name()+": "+((myFR!=null)?myFR.name():"UNDEFINED")+" ("+E.fetchFaction(F.factionID())+")");
	    if((showFlag!=showNumber)&&(showFlag>-999)) return;
	    if(F.ranges()!=null)
	    for(int v=0;v<F.ranges().size();v++)
	    {
	        Faction.FactionRange FR=(Faction.FactionRange)F.ranges().elementAt(v);
	        mob.tell(CMStrings.padRight(FR.name(),20)+": "+FR.low()+" - "+FR.high()+")");
	    }
		String newOne=mob.session().prompt(getScr("BaseGenerics","entvaluenew"));
		if(CMath.isInteger(newOne))
		{
		    E.addFaction(F.factionID(),CMath.s_int(newOne));
	        return;
		}
	    for(int v=0;v<F.ranges().size();v++)
	    {
	        Faction.FactionRange FR=(Faction.FactionRange)F.ranges().elementAt(v);
	        if(FR.name().toUpperCase().startsWith(newOne.toUpperCase()))
	        {
	            if(FR.low()==F.lowest())
	                E.addFaction(F.factionID(),FR.low());
	            else
	            if(FR.high()==F.highest())
	                E.addFaction(F.factionID(),FR.high());
	            else
	                E.addFaction(F.factionID(),FR.low()+((FR.high()-FR.low())/2));
	            return;
	        }
	    }
	    mob.tell(getScr("BaseGenerics","nochange"));
    }
    public static void genFaction(MOB mob, MOB E, int showNumber, int showFlag)
    throws IOException
    {
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String newFact="Q";
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(newFact.length()>0))
		{
		    mob.tell(getScr("BaseGenerics","factions",showNumber+"",E.getFactionListing()));
		    if((showFlag!=showNumber)&&(showFlag>-999)) return;
		    newFact=mob.session().prompt(getScr("BaseGenerics","factionmsg"),"");
		    if(newFact.length()>0)
		    {
		        Faction lookedUp=CMLib.factions().getFactionByName(newFact);
		        if(lookedUp==null) lookedUp=CMLib.factions().getFaction(newFact);
		        if(lookedUp!=null)
		        {
		            if (E.fetchFaction(lookedUp.factionID())!=Integer.MAX_VALUE)
		            {
		                // this mob already has this faction, they must want it removed
		                E.removeFaction(lookedUp.factionID());
		                mob.tell(getScr("BaseGenerics","factionrem",lookedUp.name()  ));
		            }
		            else
		            {
						int value =new Integer(mob.session().prompt(getScr("BaseGenerics","howmuchf",lookedUp.findDefault(E)+""),
						           new Integer(lookedUp.findDefault(E)).toString())).intValue();
			            if(value<lookedUp.minimum()) value=lookedUp.minimum();
					    if(value>lookedUp.maximum()) value=lookedUp.maximum();
		                E.addFaction(lookedUp.factionID(),value);
		                mob.tell(getScr("BaseGenerics","factionadd",lookedUp.name() ));
		            }
		         }
		         else
		            mob.tell(getScr("BaseGenerics","factionerr",newFact));
		    }
		}
	}

	public static void genGender(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","gender",showNumber+"",""+Character.toUpperCase((char)E.baseCharStats().getStat(CharStats.STAT_GENDER))));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newType=mob.session().choose(getScr("BaseGenerics","entergemsg"),"MFN","");
		int newValue=-1;
		if(newType.length()>0)
			newValue=("MFN").indexOf(newType.trim().toUpperCase());
		if(newValue>=0)
		{
			switch(newValue)
			{
			case 0:
				E.baseCharStats().setStat(CharStats.STAT_GENDER,'M');
				break;
			case 1:
				E.baseCharStats().setStat(CharStats.STAT_GENDER,'F');
				break;
			case 2:
				E.baseCharStats().setStat(CharStats.STAT_GENDER,'N');
				break;
			}
		}
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}

	public static void genWeaponClassification(MOB mob, Weapon E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","weaponclass",showNumber+"",Weapon.classifictionDescription[E.weaponClassification()]));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		boolean q=false;
		String sel=("ABEFHKPRSDTN");
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!q))
		{
			String newType=mob.session().choose(getScr("BaseGenerics","entnewvalueh"),sel+"?","");
			if(newType.equals("?"))
			{
				for(int i=0;i<sel.length();i++)
					mob.tell(sel.charAt(i)+") "+Weapon.classifictionDescription[i]);
				q=false;
			}
			else
			{
				q=true;
				int newValue=-1;
				if(newType.length()>0)
					newValue=sel.indexOf(newType.toUpperCase());
				if(newValue>=0)
					E.setWeaponClassification(newValue);
				else
					mob.tell(getScr("BaseGenerics","nochange"));
			}
		}
	}

	public static void genSecretIdentity(MOB mob, Item E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","secretid",showNumber+"",E.rawSecretIdentity()));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt(getScr("BaseGenerics","entnewid"),"");
		if(newValue.equalsIgnoreCase("null"))
			E.setSecretIdentity("");
		else
		if(newValue.length()>0)
			E.setSecretIdentity(newValue);
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}

	public static void genNourishment(MOB mob, Food E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","noureat",showNumber+"",E.nourishment()+""));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		int newValue=CMath.s_int(mob.session().prompt(getScr("BaseGenerics","entnewamount"),""));
		if(newValue>0)
			E.setNourishment(newValue);
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}

	public static void genRace(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String raceID="begin!";
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(raceID.length()>0))
		{
			mob.tell(getScr("BaseGenerics","race",showNumber+"",E.baseCharStats().getMyRace().ID()));
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			raceID=mob.session().prompt(getScr("BaseGenerics","entnewrace"),"").trim();
			if(raceID.equalsIgnoreCase("?"))
				mob.tell(CMLib.lister().reallyList(CMClass.races(),-1).toString());
			else
			if(raceID.length()==0)
				mob.tell(getScr("BaseGenerics","nochange"));
			else
			{
				Race R=CMClass.getRace(raceID);
				if(R!=null)
				{
					E.baseCharStats().setMyRace(R);
					E.baseCharStats().getMyRace().startRacing(E,false);
					E.baseCharStats().getMyRace().setHeightWeight(E.baseEnvStats(),(char)E.baseCharStats().getStat(CharStats.STAT_GENDER));
				}
				else
					mob.tell(getScr("BaseGenerics","unknownrace"));
			}
		}
	}

	public static void genCharClass(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String classID="begin!";
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(classID.length()>0))
		{
			StringBuffer str=new StringBuffer("");
			for(int c=0;c<E.baseCharStats().numClasses();c++)
			{
				CharClass C=E.baseCharStats().getMyClass(c);
				str.append(C.ID()+"("+E.baseCharStats().getClassLevel(C)+") ");
			}
			mob.tell(getScr("BaseGenerics","classline",showNumber+"",str.toString()));
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			classID=mob.session().prompt(getScr("BaseGenerics","entclassar"),"").trim();
			if(classID.equalsIgnoreCase("?"))
				mob.tell(CMLib.lister().reallyList(CMClass.charClasses(),-1).toString());
			else
			if(classID.length()==0)
				mob.tell(getScr("BaseGenerics","nochange"));
			else
			{
				CharClass C=CMClass.getCharClass(classID);
				if(C!=null)
				{
					if(E.baseCharStats().getClassLevel(C)>=0)
					{
						if(E.baseCharStats().numClasses()<2)
							mob.tell(getScr("BaseGenerics","finalclassmsg"));
						else
						{
							StringBuffer charClasses=new StringBuffer("");
							StringBuffer classLevels=new StringBuffer("");
							for(int c=0;c<E.baseCharStats().numClasses();c++)
							{
								CharClass C2=E.baseCharStats().getMyClass(c);
								int L2=E.baseCharStats().getClassLevel(C2);
								if(C2!=C)
								{
									charClasses.append(";"+C2.ID());
									classLevels.append(";"+L2);
								}
							}
							E.baseCharStats().setMyClasses(charClasses.toString());
							E.baseCharStats().setMyLevels(classLevels.toString());
						}
					}
					else
					{
						int highLvl=Integer.MIN_VALUE;
						CharClass highestC=null;
						for(int c=0;c<E.baseCharStats().numClasses();c++)
						{
							CharClass C2=E.baseCharStats().getMyClass(c);
							if(E.baseCharStats().getClassLevel(C2)>highLvl)
							{
								highestC=C2;
								highLvl=E.baseCharStats().getClassLevel(C2);
							}
						}
						E.baseCharStats().setCurrentClass(C);
						int levels=E.baseCharStats().combinedSubLevels();
						levels=E.baseEnvStats().level()-levels;
						String lvl=null;
						if(levels>0)
						{
							lvl=mob.session().prompt(getScr("BaseGenerics","levelsclass",levels+""),""+levels).trim();
							int lvl2=CMath.s_int(lvl);
							if(lvl2>levels) lvl2=levels;
							E.baseCharStats().setClassLevel(C,lvl2);
						}
						else
						{
							lvl=mob.session().prompt(getScr("BaseGenerics","siphon",highestC.ID()),""+0).trim();
							int lvl2=CMath.s_int(lvl);
							if(lvl2>highLvl) lvl2=highLvl;
							E.baseCharStats().setClassLevel(highestC,highLvl-lvl2);
							E.baseCharStats().setClassLevel(C,lvl2);
						}

					}
					int levels=E.baseCharStats().combinedSubLevels();
					levels=E.baseEnvStats().level()-levels;
					C=E.baseCharStats().getCurrentClass();
					E.baseCharStats().setClassLevel(C,levels);
				}
				else
					mob.tell(getScr("BaseGenerics","classerr"));
			}
		}
	}

	public static void genTattoos(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String behave="NO";
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(behave.length()>0))
		{
			String behaviorstr="";
			for(int b=0;b<E.numTattoos();b++)
			{
				String B=E.fetchTattoo(b);
				if(B!=null)	behaviorstr+=B+", ";
			}
			if(behaviorstr.length()>0)
				behaviorstr=behaviorstr.substring(0,behaviorstr.length()-2);
			mob.tell(getScr("BaseGenerics","tattoos",showNumber+"",behaviorstr));
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			behave=mob.session().prompt(getScr("BaseGenerics","tattooent"),"");
			if(behave.length()>0)
			{
				String tattoo=behave;
				if((tattoo.length()>0)
				&&(Character.isDigit(tattoo.charAt(0)))
				&&(tattoo.indexOf(" ")>0)
				&&(CMath.isNumber(tattoo.substring(0,tattoo.indexOf(" ")))))
					tattoo=tattoo.substring(tattoo.indexOf(" ")+1).trim();
				if(E.fetchTattoo(tattoo)!=null)
				{
					mob.tell(getScr("BaseGenerics","tattoorem",tattoo.trim().toUpperCase()));
					E.delTattoo(behave);
				}
				else
				{
					mob.tell(getScr("BaseGenerics","tattooadd",behave.trim().toUpperCase()));
					E.addTattoo(behave);
				}
			}
			else
				mob.tell(getScr("BaseGenerics","nochange"));
		}
	}

	public static void genTitles(MOB mob, MOB E, int showNumber, int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		if(E.playerStats()==null) return;
		String behave="NO";
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(behave.length()>0))
		{
			String behaviorstr="";
			for(int b=0;b<E.playerStats().getTitles().size();b++)
			{
				String B=(String)E.playerStats().getTitles().elementAt(b);
				if(B!=null)	behaviorstr+=B+", ";
			}
			if(behaviorstr.length()>0)
				behaviorstr=behaviorstr.substring(0,behaviorstr.length()-2);
			mob.tell(getScr("BaseGenerics","titles",showNumber+"",behaviorstr));
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			behave=mob.session().prompt(getScr("BaseGenerics","titleent"),"");
			if(behave.length()>0)
			{
				String tattoo=behave;
				if((tattoo.length()>0)
				&&(CMath.isInteger(tattoo))
				&&(CMath.s_int(tattoo)>0)
				&&(CMath.s_int(tattoo)<=E.playerStats().getTitles().size()))
					tattoo=(String)E.playerStats().getTitles().elementAt(CMath.s_int(tattoo)-1);
				else
				if((tattoo.length()>0)
				&&(Character.isDigit(tattoo.charAt(0)))
				&&(tattoo.indexOf(" ")>0)
				&&(CMath.isNumber(tattoo.substring(0,tattoo.indexOf(" ")))))
					tattoo=tattoo.substring(tattoo.indexOf(" ")+1).trim();
				if(E.playerStats().getTitles().contains(tattoo))
				{
					mob.tell(getScr("BaseGenerics","tattoorem",tattoo.trim().toUpperCase()));
					E.playerStats().getTitles().remove(tattoo);
				}
				else
				{
					mob.tell(getScr("BaseGenerics","tattooadd",behave.trim().toUpperCase()));
					E.playerStats().getTitles().addElement(tattoo);
				}
			}
			else
				mob.tell(getScr("BaseGenerics","nochange"));
		}
	}

	public static void genExpertises(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String behave="NO";
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(behave.length()>0))
		{
			String behaviorstr="";
			for(int b=0;b<E.numExpertises();b++)
			{
				String B=E.fetchExpertise(b);
				if(B!=null)	behaviorstr+=B+", ";
			}
			if(behaviorstr.length()>0)
				behaviorstr=behaviorstr.substring(0,behaviorstr.length()-2);
			mob.tell(getScr("BaseGenerics","expertises",showNumber+"",behaviorstr));
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			behave=mob.session().prompt(getScr("BaseGenerics","lessonent"),"");
			if(behave.length()>0)
			{
				if(E.fetchExpertise(behave)!=null)
				{
					mob.tell(getScr("BaseGenerics","behaverem",behave));
					E.delExpertise(behave);
				}
				else
				{
					mob.tell(getScr("BaseGenerics","behaveadd",behave));
					E.addExpertise(behave);
				}
			}
			else
				mob.tell(getScr("BaseGenerics","nochange"));
		}
	}

	public static void genSecurity(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		PlayerStats P=E.playerStats();
		if(P==null) return;
		String behave="NO";
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(behave.length()>0))
		{
			String behaviorstr="";
			for(int b=0;b<P.getSecurityGroups().size();b++)
			{
				String B=(String)P.getSecurityGroups().elementAt(b);
				if(B!=null)	behaviorstr+=B+", ";
			}
			if(behaviorstr.length()>0)
				behaviorstr=behaviorstr.substring(0,behaviorstr.length()-2);
			mob.tell(getScr("BaseGenerics","secgroups",showNumber+"",behaviorstr));
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			behave=mob.session().prompt(getScr("BaseGenerics","groupent"),"");
			if(behave.length()>0)
			{
				if(P.getSecurityGroups().contains(behave.trim().toUpperCase()))
				{
					P.getSecurityGroups().remove(behave.trim().toUpperCase());
					mob.tell(getScr("BaseGenerics","behaverem",behave));
				}
				else
                if((behave.trim().toUpperCase().startsWith("AREA "))
                &&(!CMSecurity.isAllowedAnywhere(mob,behave.trim().toUpperCase().substring(5).trim())))
                    mob.tell(getScr("BaseGenerics","behavebad",behave));
                else
                if((!behave.trim().toUpperCase().startsWith("AREA "))
                &&(!CMSecurity.isAllowedEverywhere(mob,behave.trim().toUpperCase())))
                    mob.tell(getScr("BaseGenerics","behavebad",behave));
                else
				{
					P.getSecurityGroups().addElement(behave.trim().toUpperCase());
					mob.tell(getScr("BaseGenerics","behaveadd",behave));
				}
			}
			else
				mob.tell(getScr("BaseGenerics","nochange"));
		}
	}

	public static void genBehaviors(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String behave="NO";
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(behave.length()>0))
		{
			String behaviorstr="";
			for(int b=0;b<E.numBehaviors();b++)
			{
				Behavior B=E.fetchBehavior(b);
				if((B!=null)&&(B.isSavable()))
				{
					behaviorstr+=B.ID();
					if(B.getParms().trim().length()>0)
						behaviorstr+="("+B.getParms().trim()+"), ";
					else
						behaviorstr+=", ";
				}
			}
			if(behaviorstr.length()>0)
				behaviorstr=behaviorstr.substring(0,behaviorstr.length()-2);
			mob.tell(getScr("BaseGenerics","behaviorsline",showNumber+"",behaviorstr));
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			behave=mob.session().prompt(getScr("BaseGenerics","behaent"),"");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("?"))
					mob.tell(CMLib.lister().reallyList(CMClass.behaviors(),-1).toString());
				else
				{
					Behavior chosenOne=null;
					for(int b=0;b<E.numBehaviors();b++)
					{
						Behavior B=E.fetchBehavior(b);
						if((B!=null)&&(B.ID().equalsIgnoreCase(behave)))
							chosenOne=B;
					}
					if(chosenOne!=null)
					{
						mob.tell(getScr("BaseGenerics","cidr",chosenOne.ID()));
						E.delBehavior(chosenOne);
					}
					else
					{
						chosenOne=CMClass.getBehavior(behave);
						if(chosenOne!=null)
						{
							boolean alreadyHasIt=false;
							for(int b=0;b<E.numBehaviors();b++)
							{
								Behavior B=E.fetchBehavior(b);
								if((B!=null)&&(B.ID().equals(chosenOne.ID())))
								{
									alreadyHasIt=true;
									chosenOne=B;
								}
							}
                            String parms="?";
                            while(parms.equals("?"))
                            {
    							parms=chosenOne.getParms();
    							parms=mob.session().prompt(getScr("BaseGenerics","behapar",parms));
                                if(parms.equals("?")){ StringBuffer s2=CMLib.help().getHelpText(chosenOne.ID(),mob,true); if(s2!=null) mob.tell(s2.toString()); else mob.tell("no help!");}
                            }
							chosenOne.setParms(parms.trim());
							if(!alreadyHasIt)
							{
								mob.tell(getScr("BaseGenerics","cida",chosenOne.ID()));
								E.addBehavior(chosenOne);
							}
							else
								mob.tell(getScr("BaseGenerics","cidaa",chosenOne.ID()));
						}
						else
						{
							mob.tell(getScr("BaseGenerics","behaveerr",behave));
						}
					}
				}
			}
			else
				mob.tell(getScr("BaseGenerics","nochange"));
		}
	}

	public static void genAffects(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String behave="NO";
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(behave.length()>0))
		{
			String affectstr="";
			for(int b=0;b<E.numEffects();b++)
			{
				Ability A=E.fetchEffect(b);
				if((A!=null)&&(A.savable()))
				{
					affectstr+=A.ID();
					if(A.text().trim().length()>0)
						affectstr+="("+A.text().trim()+"), ";
					else
						affectstr+=", ";
				}

			}
			if(affectstr.length()>0)
				affectstr=affectstr.substring(0,affectstr.length()-2);
			mob.tell(getScr("BaseGenerics","effects",showNumber+"",affectstr));
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			behave=mob.session().prompt(getScr("BaseGenerics","effectent"),"");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("?"))
					mob.tell(CMLib.lister().reallyList(CMClass.abilities(),-1).toString());
				else
				{
					Ability chosenOne=null;
					for(int a=0;a<E.numEffects();a++)
					{
						Ability A=E.fetchEffect(a);
						if((A!=null)&&(A.ID().equalsIgnoreCase(behave)))
							chosenOne=A;
					}
					if(chosenOne!=null)
					{
						mob.tell(getScr("BaseGenerics","cidr",chosenOne.ID()));
						E.delEffect(chosenOne);
					}
					else
					{
						chosenOne=CMClass.getAbility(behave);
						if(chosenOne!=null)
						{
                            String parms="?";
                            while(parms.equals("?"))
                            {
                                parms=chosenOne.text();
                                parms=mob.session().prompt(getScr("BaseGenerics","effectpar",parms));
                                if(parms.equals("?")){ StringBuffer s2=CMLib.help().getHelpText(chosenOne.ID(),mob,true); if(s2!=null) mob.tell(s2.toString()); else mob.tell("no help!");}
                            }
							chosenOne.setMiscText(parms.trim());
							mob.tell(getScr("BaseGenerics","cida",chosenOne.ID()));
							E.addNonUninvokableEffect(chosenOne);
						}
						else
						{
							mob.tell(getScr("BaseGenerics","behaveerr",behave));
						}
					}
				}
			}
			else
				mob.tell(getScr("BaseGenerics","nochange"));
		}
	}

	public static void genRideable1(MOB mob, Rideable R, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","ridetype",showNumber+"",Rideable.RIDEABLE_DESCS[R.rideBasis()]));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		boolean q=false;
		String sel="LWACBTEDG";
		while(!q)
		{
			String newType=mob.session().choose(getScr("BaseGenerics","entnewvalueh"),sel+"?","");
			if(newType.equals("?"))
			{
				for(int i=0;i<sel.length();i++)
					mob.tell(sel.charAt(i)+") "+Rideable.RIDEABLE_DESCS[i].toLowerCase());
				q=false;
			}
			else
			{
				q=true;
				int newValue=-1;
				if(newType.length()>0)
					newValue=sel.indexOf(newType.toUpperCase());
				if(newValue>=0)
					R.setRideBasis(newValue);
				else
					mob.tell(getScr("BaseGenerics","nochange"));
			}
		}
	}
	public static void genRideable2(MOB mob, Rideable R, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","nomobheld",showNumber+"",R.riderCapacity()+""));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newLevelStr=mob.session().prompt(getScr("BaseGenerics","entvaluenew"),"");
		int newLevel=CMath.s_int(newLevelStr);
		if(newLevel>0)
			R.setRiderCapacity(newLevel);
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}

	public static void genShopkeeper1(MOB mob, ShopKeeper E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","shopktype",showNumber+"",E.storeKeeperString()));
		StringBuffer buf=new StringBuffer("");
		StringBuffer codes=new StringBuffer("");
		String codeStr="0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		if(E instanceof Banker)
		{
			int r=ShopKeeper.DEAL_BANKER;
			char c=codeStr.charAt(r);
			codes.append(c);
			buf.append(c+") "+ShopKeeper.DEAL_DESCS[r]+"\n\r");
			r=ShopKeeper.DEAL_CLANBANKER;
			c=codeStr.charAt(r);
			codes.append(c);
			buf.append(c+") "+ShopKeeper.DEAL_DESCS[r]+"\n\r");
		}
		else
        if(E instanceof PostOffice)
        {
            int r=ShopKeeper.DEAL_POSTMAN;
            char c=codeStr.charAt(r);
            codes.append(c);
            buf.append(c+") "+ShopKeeper.DEAL_DESCS[r]+"\n\r");
            r=ShopKeeper.DEAL_CLANPOSTMAN;
            c=codeStr.charAt(r);
            codes.append(c);
            buf.append(c+") "+ShopKeeper.DEAL_DESCS[r]+"\n\r");
        }
        else
		for(int r=0;r<ShopKeeper.DEAL_DESCS.length;r++)
		{
			if((r!=ShopKeeper.DEAL_CLANBANKER)
            &&(r!=ShopKeeper.DEAL_BANKER)
            &&(r!=ShopKeeper.DEAL_CLANPOSTMAN)
            &&(r!=ShopKeeper.DEAL_POSTMAN))
			{
				char c=codeStr.charAt(r);
				codes.append(c);
				buf.append(c+") "+ShopKeeper.DEAL_DESCS[r]+"\n\r");
			}
		}
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newType=mob.session().choose(getScr("BaseGenerics","entvaluech",buf.toString()),codes.toString(),"");
		int newValue=-1;
		if(newType.length()>0)
			newValue=codeStr.indexOf(newType.toUpperCase());
		if(newValue>=0)
		{
			boolean reexamine=(E.whatIsSold()!=newValue);
			E.setWhatIsSold(newValue);
            if(reexamine)
            {
                Vector V=E.getShop().getStoreInventory();
                for(int b=0;b<V.size();b++)
                    if(!E.doISellThis((Environmental)V.elementAt(b)))
                        E.getShop().delAllStoreInventory((Environmental)V.elementAt(b),E.whatIsSold());
            }
		}
	}

	public static void genShopkeeper2(MOB mob, ShopKeeper E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String itemstr="NO";
		while(itemstr.length()>0)
		{
			String inventorystr="";
			Vector V=E.getShop().getStoreInventory();
			for(int b=0;b<V.size();b++)
			{
				Environmental E2=(Environmental)V.elementAt(b);
				if(E2.isGeneric())
					inventorystr+=E2.name()+" ("+E.getShop().numberInStock(E2)+"), ";
				else
					inventorystr+=CMClass.className(E2)+" ("+E.getShop().numberInStock(E2)+"), ";
			}
			if(inventorystr.length()>0)
				inventorystr=inventorystr.substring(0,inventorystr.length()-2);
			mob.tell(getScr("BaseGenerics","inventoryline",showNumber+"",inventorystr));
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			itemstr=mob.session().prompt(getScr("BaseGenerics","entsometh"),"");
			if(itemstr.length()>0)
			{
				if(itemstr.equalsIgnoreCase("?"))
				{
					mob.tell(CMLib.lister().reallyList(CMClass.abilities(),-1).toString());
					mob.tell(CMLib.lister().reallyList(CMClass.armor(),-1).toString());
					mob.tell(CMLib.lister().reallyList(CMClass.weapons(),-1).toString());
					mob.tell(CMLib.lister().reallyList(CMClass.miscMagic(),-1).toString());
					mob.tell(CMLib.lister().reallyList(CMClass.miscTech(),-1).toString());
					mob.tell(CMLib.lister().reallyList(CMClass.clanItems(),-1).toString());
					mob.tell(CMLib.lister().reallyList(CMClass.basicItems(),-1).toString());
					mob.tell(CMLib.lister().reallyList(CMClass.mobTypes(),-1).toString());
					mob.tell(getScr("BaseGenerics","msgitemground"));
					mob.tell(getScr("BaseGenerics","msgmobsroom"));
				}
				else
				{
					Environmental item=E.getShop().getStock(itemstr,null,E.whatIsSold(),null);
					if(item!=null)
					{
						mob.tell(getScr("BaseGenerics","itemidrem",item.ID()));
						E.getShop().delAllStoreInventory((Environmental)item.copyOf(),E.whatIsSold());
					}
					else
					{
						item=CMClass.getUnknown(itemstr);
						if((item==null)&&(mob.location()!=null))
						{
							Room R=mob.location();
							item=R.fetchItem(null,itemstr);
							if(item==null)
							{
								item=R.fetchInhabitant(itemstr);
								if((item instanceof MOB)&&(!((MOB)item).isMonster()))
									item=null;
							}
						}
						if(item!=null)
						{
							item=(Environmental)item.copyOf();
							item.recoverEnvStats();
							boolean ok=E.doISellThis(item);
							if((item instanceof Ability)
							   &&((E.whatIsSold()==ShopKeeper.DEAL_TRAINER)||(E.whatIsSold()==ShopKeeper.DEAL_CASTER)))
								ok=true;
							else
							if(E.whatIsSold()==ShopKeeper.DEAL_INVENTORYONLY)
								ok=true;
							if(!ok)
							{
								mob.tell(getScr("BaseGenerics","shoperror"));
							}
							else
							{
								boolean alreadyHasIt=false;

								if(E.getShop().doIHaveThisInStock(item.Name(),null,E.whatIsSold(),null))
								   alreadyHasIt=true;

								if(!alreadyHasIt)
								{
									mob.tell(getScr("BaseGenerics","itemidadd",item.ID()));
									int num=1;
									if(!(item instanceof Ability))
										num=CMath.s_int(mob.session().prompt(getScr("BaseGenerics","howman"),""));
									int price=CMath.s_int(mob.session().prompt(getScr("BaseGenerics","atwprice"),""));
									E.getShop().addStoreInventory(item,num,price,E);
								}
							}
						}
						else
						{
							mob.tell(getScr("BaseGenerics","itemerr",itemstr));
						}
					}
				}
			}
			else
				mob.tell(getScr("BaseGenerics","nochange"));
		}
	}
	public static void genShopkeeper3(MOB mob, ShopKeeper E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","prejudice",showNumber+"",E.prejudiceFactors()));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt(getScr("BaseGenerics","anewstri"),"");
		if(newValue.equalsIgnoreCase("null"))
			E.setPrejudiceFactors("");
		else
		if(newValue.length()>0)
			E.setPrejudiceFactors(newValue);
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}

	public static void genShopkeeper4(MOB mob, ShopKeeper E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","budget",showNumber+"",E.budget()));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt(getScr("BaseGenerics","anewstri"),"");
		if(newValue.equalsIgnoreCase("null"))
			E.setBudget("");
		else
		if(newValue.length()>0)
			E.setBudget(newValue);
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}

	public static void genShopkeeper5(MOB mob, ShopKeeper E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","devaluationrate",showNumber+"",E.devalueRate()));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt(getScr("BaseGenerics","anewstri"),"");
		if(newValue.equalsIgnoreCase("null"))
			E.setDevalueRate("");
		else
		if(newValue.length()>0)
			E.setDevalueRate(newValue);
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}
	public static void genShopkeeper6(MOB mob, ShopKeeper E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","inventoryresetrate",showNumber+"",E.invResetRate()+""));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt(getScr("BaseGenerics","anewnum"),"");
		if(newValue.equals("0")||(CMath.s_int(newValue)!=0))
			E.setInvResetRate(CMath.s_int(newValue));
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}

    public static void genShopkeeper7(MOB mob, ShopKeeper E, int showNumber, int showFlag)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.tell(getScr("BaseGenerics","ignoremask",showNumber+"",E.ignoreMask()));
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String newValue=mob.session().prompt(getScr("BaseGenerics","anewsmask"),"");
        if(newValue.equalsIgnoreCase("null"))
            E.setIgnoreMask("");
        else
        if(newValue.length()>0)
            E.setIgnoreMask(newValue);
        else
            mob.tell(getScr("BaseGenerics","nochange"));
    }

	public static void genAbilities(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String behave="NO";
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(behave.length()>0))
		{
			String abilitiestr="";
			for(int a=0;a<E.numLearnedAbilities();a++)
			{
				Ability A=E.fetchAbility(a);
				if((A!=null)&&(A.savable()))
					abilitiestr+=A.ID()+", ";
			}
			if(abilitiestr.length()>0)
				abilitiestr=abilitiestr.substring(0,abilitiestr.length()-2);
			mob.tell(getScr("BaseGenerics","abilities",showNumber+"",abilitiestr));
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			behave=mob.session().prompt(getScr("BaseGenerics","entabaddrem"),"");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("?"))
					mob.tell(CMLib.lister().reallyList(CMClass.abilities(),-1).toString());
				else
				{
					Ability chosenOne=null;
					for(int a=0;a<E.numLearnedAbilities();a++)
					{
						Ability A=E.fetchAbility(a);
						if((A!=null)&&(A.ID().equalsIgnoreCase(behave)))
							chosenOne=A;
					}
					if(chosenOne!=null)
					{
						mob.tell(getScr("BaseGenerics","cidr",chosenOne.ID()));
						E.delAbility(chosenOne);
						if(E.fetchEffect(chosenOne.ID())!=null)
							E.delEffect(E.fetchEffect(chosenOne.ID()));
					}
					else
					{
						chosenOne=CMClass.getAbility(behave);
						if(chosenOne!=null)
						{
							boolean alreadyHasIt=(E.fetchAbility(chosenOne.ID())!=null);
							if(!alreadyHasIt)
								mob.tell(getScr("BaseGenerics","cida",chosenOne.ID()));
							else
								mob.tell(getScr("BaseGenerics","cidaa",chosenOne.ID()));
							if(!alreadyHasIt)
							{
								chosenOne=(Ability)chosenOne.copyOf();
								E.addAbility(chosenOne);
								chosenOne.setProficiency(50);
								chosenOne.autoInvocation(mob);
							}
						}
						else
						{
							mob.tell(getScr("BaseGenerics","behaveerr",behave));
						}
					}
				}
			}
			else
				mob.tell(getScr("BaseGenerics","nochange"));
		}
	}

	public static void genClanMembers(MOB mob, Clan E, int showNumber, int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String behave="NO";
		DVector members=E.getMemberList();
		DVector membersCopy=members.copyOf();
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(behave.length()>0))
		{
			String memberStr="";
			for(int m=0;m<members.size();m++)
				memberStr+=((String)members.elementAt(m,1))+" ("+CMLib.clans().getRoleName(E.getGovernment(),((Integer)members.elementAt(m,2)).intValue(),true,false)+"), ";
			if(memberStr.length()>0)
				memberStr=memberStr.substring(0,memberStr.length()-2);
			mob.tell(getScr("BaseGenerics","clanmembers",showNumber+"",memberStr));
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			behave=mob.session().prompt(getScr("BaseGenerics","clanmemaddrem"),"");
			if(behave.length()>0)
			{
				int chosenOne=-1;
				for(int m=0;m<members.size();m++)
					if(behave.equalsIgnoreCase((String)members.elementAt(m,1)))
						chosenOne=m;
				if(chosenOne>=0)
				{
					mob.tell(getScr("BaseGenerics","cidr",(String)members.elementAt(chosenOne,1)));
					members.removeElementAt(chosenOne);
				}
				else
				{
					MOB M=CMLib.map().getLoadPlayer(behave);
					if(M!=null)
					{
						int oldNum=-1;
						for(int m=0;m<membersCopy.size();m++)
							if(behave.equalsIgnoreCase((String)membersCopy.elementAt(m,1)))
							{
								oldNum=m;
								members.addElement(membersCopy.elementAt(m,1),membersCopy.elementAt(m,2),membersCopy.elementAt(m,3));
								break;
							}
						int index=oldNum;
						if(index<0)
						{
							index=members.size();
							members.addElement(M.Name(),new Integer(Clan.POS_MEMBER),new Long(M.playerStats().lastDateTime()));
						}
						
						int newRole=-1;
						while((mob.session()!=null)&&(!mob.session().killFlag())&&(newRole<0))
						{
							String newRoleStr=mob.session().prompt(getScr("BaseGenerics","clannewrole",CMLib.clans().getRoleName(E.getGovernment(),((Integer)members.elementAt(index,2)).intValue(),true,false)),"");
							StringBuffer roles=new StringBuffer();
							for(int i=0;i<Clan.ROL_DESCS[E.getGovernment()].length;i++)
							{
								roles.append(Clan.ROL_DESCS[E.getGovernment()][i]+", ");
								if(newRoleStr.equalsIgnoreCase(Clan.ROL_DESCS[E.getGovernment()][i]))
									newRole=Clan.POSORDER[i];
							}
							roles=new StringBuffer(roles.substring(0,roles.length()-2));
							if(newRole<0)
								mob.tell(getScr("BaseGenerics","clanrolerr",roles.toString()));
							else
								break;
						}
						if(oldNum<0)
							mob.tell(getScr("BaseGenerics","cida",M.Name()));
						else
							mob.tell(getScr("BaseGenerics","cidaa",M.Name()));
						members.setElementAt(index,2,new Integer(newRole));
					}
					else
					{
						mob.tell(getScr("BaseGenerics","clanmemerr",behave));
					}
				}
				// first add missing ones
				for(int m=0;m<members.size();m++)
				{
					String newName=(String)members.elementAt(m,1);
					if(!membersCopy.contains(newName))
					{
						MOB M=CMLib.map().getLoadPlayer(newName);
						if(M!=null)
						{
							Clan oldC=CMLib.clans().getClan(M.getClanID());
							if((oldC!=null)
							&&(!M.getClanID().equalsIgnoreCase(E.clanID())))
							{
								M.setClanID("");
								M.setClanRole(Clan.POS_APPLICANT);
								oldC.updateClanPrivileges(M);
							}
							Integer role=(Integer)members.elementAt(m,2);
							CMLib.database().DBUpdateClanMembership(M.Name(), E.clanID(), role.intValue());
							M.setClanID(E.clanID());
							M.setClanRole(role.intValue());
							E.updateClanPrivileges(M);
						}
					}
				}
				// now adjust changed roles
				for(int m=0;m<members.size();m++)
				{
					String newName=(String)members.elementAt(m,1);
					if(membersCopy.contains(newName))
					{
						MOB M=CMLib.map().getLoadPlayer(newName);
						int newRole=((Integer)members.elementAt(m,2)).intValue();
						if((M!=null)&&(newRole!=M.getClanRole()))
						{
							CMLib.database().DBUpdateClanMembership(M.Name(), E.clanID(), newRole);
							M.setClanRole(newRole);
							E.updateClanPrivileges(M);
						}
					}
				}
				// now remove old members
				for(int m=0;m<membersCopy.size();m++)
				{
					String newName=(String)membersCopy.elementAt(m,1);
					if(!members.contains(newName))
					{
						MOB M=CMLib.map().getLoadPlayer(newName);
						if(M!=null)
						{
							M.setClanID("");
							M.setClanRole(Clan.POS_APPLICANT);
							E.updateClanPrivileges(M);
						}
					}
				}
			}
			else
				mob.tell(getScr("BaseGenerics","nochange"));
		}
	}
	
	public static void genDeity1(MOB mob, Deity E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","clericreq",showNumber+"",E.getClericRequirements()));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt(getScr("BaseGenerics","newreq"),"");
		if(newValue.length()>0)
			E.setClericRequirements(newValue);
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}
	public static void genDeity2(MOB mob, Deity E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","clericrit",showNumber+"",E.getClericRitual()));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt(getScr("BaseGenerics","newrit"),"");
		if(newValue.length()>0)
			E.setClericRitual(newValue);
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}
	public static void genDeity3(MOB mob, Deity E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","worshipreq",showNumber+"",E.getWorshipRequirements()));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt(getScr("BaseGenerics","newreq"),"");
		if(newValue.length()>0)
			E.setWorshipRequirements(newValue);
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}
	public static void genDeity4(MOB mob, Deity E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","worshiprit",showNumber+"",E.getWorshipRitual()));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt(getScr("BaseGenerics","newrit"),"");
		if(newValue.length()>0)
			E.setWorshipRitual(newValue);
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}
	public static void genDeity5(MOB mob, Deity E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String behave="NO";
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(behave.length()>0))
		{
			String abilitiestr="";
			for(int a=0;a<E.numBlessings();a++)
			{
				Ability A=E.fetchBlessing(a);
				if((A!=null)&&(A.savable()))
					abilitiestr+=A.ID()+", ";
			}
			if(abilitiestr.length()>0)
				abilitiestr=abilitiestr.substring(0,abilitiestr.length()-2);
			mob.tell(getScr("BaseGenerics","blessings",showNumber+"",abilitiestr));
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			behave=mob.session().prompt(getScr("BaseGenerics","entabaddrem"),"");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("?"))
					mob.tell(CMLib.lister().reallyList(CMClass.abilities(),-1).toString());
				else
				{
					Ability chosenOne=null;
					for(int a=0;a<E.numBlessings();a++)
					{
						Ability A=E.fetchBlessing(a);
						if((A!=null)&&(A.ID().equalsIgnoreCase(behave)))
							chosenOne=A;
					}
					if(chosenOne!=null)
					{
						mob.tell(getScr("BaseGenerics","cidr",chosenOne.ID()));
						E.delBlessing(chosenOne);
					}
					else
					{
						chosenOne=CMClass.getAbility(behave);
						if(chosenOne!=null)
						{
							boolean alreadyHasIt=false;
							for(int a=0;a<E.numBlessings();a++)
							{
								Ability A=E.fetchBlessing(a);
								if((A!=null)&&(A.ID().equals(chosenOne.ID())))
									alreadyHasIt=true;
							}
							if(!alreadyHasIt)
								mob.tell(getScr("BaseGenerics","cida",chosenOne.ID()));
							else
								mob.tell(getScr("BaseGenerics","cidaa",chosenOne.ID()));
							if(!alreadyHasIt)
								E.addBlessing((Ability)chosenOne.copyOf());
						}
						else
						{
							mob.tell(getScr("BaseGenerics","behaveerr",behave));
						}
					}
				}
			}
			else
				mob.tell(getScr("BaseGenerics","nochange"));
		}
	}

	public static void genDeity6(MOB mob, Deity E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String behave="NO";
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(behave.length()>0))
		{
			String abilitiestr="";
			for(int a=0;a<E.numCurses();a++)
			{
				Ability A=E.fetchCurse(a);
				if((A!=null)&&(A.savable()))
					abilitiestr+=A.ID()+", ";
			}
			if(abilitiestr.length()>0)
				abilitiestr=abilitiestr.substring(0,abilitiestr.length()-2);
			mob.tell(getScr("BaseGenerics","curses",showNumber+"",abilitiestr));
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			behave=mob.session().prompt(getScr("BaseGenerics","entabaddrem"),"");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("?"))
					mob.tell(CMLib.lister().reallyList(CMClass.abilities(),-1).toString());
				else
				{
					Ability chosenOne=null;
					for(int a=0;a<E.numCurses();a++)
					{
						Ability A=E.fetchCurse(a);
						if((A!=null)&&(A.ID().equalsIgnoreCase(behave)))
							chosenOne=A;
					}
					if(chosenOne!=null)
					{
						mob.tell(getScr("BaseGenerics","cidr",chosenOne.ID()));
						E.delCurse(chosenOne);
					}
					else
					{
						chosenOne=CMClass.getAbility(behave);
						if(chosenOne!=null)
						{
							boolean alreadyHasIt=false;
							for(int a=0;a<E.numCurses();a++)
							{
								Ability A=E.fetchCurse(a);
								if((A!=null)&&(A.ID().equals(chosenOne.ID())))
									alreadyHasIt=true;
							}
							if(!alreadyHasIt)
								mob.tell(getScr("BaseGenerics","cida",chosenOne.ID()));
							else
								mob.tell(getScr("BaseGenerics","cidaa",chosenOne.ID()));
							if(!alreadyHasIt)
								E.addCurse((Ability)chosenOne.copyOf());
						}
						else
						{
							mob.tell(getScr("BaseGenerics","behaveerr",behave));
						}
					}
				}
			}
			else
				mob.tell(getScr("BaseGenerics","nochange"));
		}
	}

	public static void genDeity7(MOB mob, Deity E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String behave="NO";
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(behave.length()>0))
		{
			String abilitiestr="";
			for(int a=0;a<E.numPowers();a++)
			{
				Ability A=E.fetchPower(a);
				if((A!=null)&&(A.savable()))
					abilitiestr+=A.ID()+", ";
			}
			if(abilitiestr.length()>0)
				abilitiestr=abilitiestr.substring(0,abilitiestr.length()-2);
			mob.tell(getScr("BaseGenerics","grantedpowers",showNumber+"",abilitiestr));
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			behave=mob.session().prompt(getScr("BaseGenerics","entabaddrem"),"");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("?"))
					mob.tell(CMLib.lister().reallyList(CMClass.abilities(),-1).toString());
				else
				{
					Ability chosenOne=null;
					for(int a=0;a<E.numPowers();a++)
					{
						Ability A=E.fetchPower(a);
						if((A!=null)&&(A.ID().equalsIgnoreCase(behave)))
							chosenOne=A;
					}
					if(chosenOne!=null)
					{
						mob.tell(getScr("BaseGenerics","cidr",chosenOne.ID()));
						E.delPower(chosenOne);
					}
					else
					{
						chosenOne=CMClass.getAbility(behave);
						if(chosenOne!=null)
						{
							boolean alreadyHasIt=false;
							for(int a=0;a<E.numPowers();a++)
							{
								Ability A=E.fetchPower(a);
								if((A!=null)&&(A.ID().equals(chosenOne.ID())))
									alreadyHasIt=true;
							}
							if(!alreadyHasIt)
								mob.tell(getScr("BaseGenerics","cida",chosenOne.ID()));
							else
								mob.tell(getScr("BaseGenerics","cidaa",chosenOne.ID()));
							if(!alreadyHasIt)
								E.addPower((Ability)chosenOne.copyOf());
						}
						else
						{
							mob.tell(getScr("BaseGenerics","behaveerr",behave));
						}
					}
				}
			}
			else
				mob.tell(getScr("BaseGenerics","nochange"));
		}
	}
	public static void genDeity8(MOB mob, Deity E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","clericsin",showNumber+"",E.getClericSin()));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt(getScr("BaseGenerics","sinritual"),"");
		if(newValue.length()>0)
			E.setClericSin(newValue);
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}
	public static void genDeity9(MOB mob, Deity E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","worshipsin",showNumber+"",E.getWorshipSin()));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt(getScr("BaseGenerics","sinritual"),"");
		if(newValue.length()>0)
			E.setWorshipSin(newValue);
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}
	public static void genDeity0(MOB mob, Deity E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","clericpowrit",showNumber+"",E.getClericPowerup()));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt(getScr("BaseGenerics","newpowritual"),"");
		if(newValue.length()>0)
			E.setClericPowerup(newValue);
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}
    public static void genDeity11(MOB mob, Deity E, int showNumber, int showFlag)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.tell(getScr("BaseGenerics","servicerit",showNumber+"",E.getServiceRitual()));
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String newValue=mob.session().prompt(getScr("BaseGenerics","newrit"),"");
        if(newValue.length()>0)
            E.setServiceRitual(newValue);
        else
            mob.tell(getScr("BaseGenerics","nochange"));
    }
	public static void genGridLocaleX(MOB mob, GridZones E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","sizex",showNumber+"",E.xGridSize()+""));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt(getScr("BaseGenerics","newsize"),"");
		if(CMath.s_int(newValue)>0)
			E.setXGridSize(CMath.s_int(newValue));
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}

	public static void genGridLocaleY(MOB mob, GridZones E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","sizey",showNumber+"",E.yGridSize()+""));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt(getScr("BaseGenerics","newsize"),"");
		if(CMath.s_int(newValue)>0)
			E.setYGridSize(CMath.s_int(newValue));
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}

	public static void genWornLocation(MOB mob, Item E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		if((showFlag!=showNumber)&&(showFlag>-999))
		{
			StringBuffer buf=new StringBuffer(showNumber+". ");
			if(!E.rawLogicalAnd())
				buf.append(getScr("BaseGenerics","wearonany"));
			else
				buf.append(getScr("BaseGenerics","wornonall"));
			for(int l=0;l<Item.WORN_CODES.length;l++)
			{
				long wornCode=1<<l;
				if((CMLib.flags().wornLocation(wornCode).length()>0)
				&&(((E.rawProperLocationBitmap()&wornCode)==wornCode)))
					buf.append(CMLib.flags().wornLocation(wornCode)+" ");
			}
			mob.tell(buf.toString());
			return;
		}
		int codeVal=-1;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(codeVal!=0))
		{
			mob.tell(getScr("BaseGenerics","wearingpar"));
			if(!E.rawLogicalAnd())
				mob.tell(getScr("BaseGenerics","msgworn1"));
			else
				mob.tell(getScr("BaseGenerics","msgworn2"));
			for(int l=0;l<Item.WORN_CODES.length;l++)
			{
				long wornCode=1<<l;
				if(CMLib.flags().wornLocation(wornCode).length()>0)
				{
					String header=(l+2)+": ("+CMLib.flags().wornLocation(wornCode)+") : "+(((E.rawProperLocationBitmap()&wornCode)==wornCode)?"YES":"NO");
					mob.tell(header);
				}
			}
			codeVal=CMath.s_int(mob.session().prompt(getScr("BaseGenerics","togglenumber")));
			if(codeVal>0)
			{
				if(codeVal==1)
					E.setRawLogicalAnd(!E.rawLogicalAnd());
				else
				{
					int wornCode=1<<(codeVal-2);
					if((E.rawProperLocationBitmap()&wornCode)==wornCode)
						E.setRawProperLocationBitmap(E.rawProperLocationBitmap()-wornCode);
					else
						E.setRawProperLocationBitmap(E.rawProperLocationBitmap()|wornCode);
				}
			}
		}
	}

	public static void genThirstQuenched(MOB mob, Drink E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","qdrink",showNumber+"",E.thirstQuenched()+""));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.setThirstQuenched(getNumericData(mob,getScr("BaseGenerics","moumou"),E.thirstQuenched()));
	}

	public static void genDrinkHeld(MOB mob, Drink E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","amountdrinks",showNumber+"",""+E.liquidHeld()));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.setLiquidHeld(getNumericData(mob,getScr("BaseGenerics","moumou"),E.liquidHeld()));
		E.setLiquidRemaining(E.liquidHeld());
	}



	static void genText(MOB mob, Race E, int showNumber, int showFlag, String FieldDisp, String Field)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". "+FieldDisp+": '"+E.getStat(Field)+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt(getScr("BaseGenerics","enternewone"),"");
		if(newName.length()>0)
			E.setStat(Field,newName);
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}
	static void genText(MOB mob, CharClass E, int showNumber, int showFlag, String FieldDisp, String Field)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". "+FieldDisp+": '"+E.getStat(Field)+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt(getScr("BaseGenerics","enternewone"),"");
		if(newName.length()>0)
			E.setStat(Field,newName);
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}
	static void genAttackAttribute(MOB mob, CharClass E, int showNumber, int showFlag, String FieldDisp, String Field)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". "+FieldDisp+": '"+CharStats.STAT_DESCS[CMath.s_int(E.getStat(Field))]+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt(getScr("BaseGenerics","enternewone"),"");
		String newStat="";
		for(int i=0;i<CharStats.NUM_BASE_STATS;i++)
			if(newName.equalsIgnoreCase(CharStats.STAT_DESCS[i]))
				newStat=""+i;
		if(newStat.length()>0)
			E.setStat(Field,newStat);
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}
	static void genArmorCode(MOB mob, CharClass E, int showNumber, int showFlag, String FieldDisp, String Field)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". "+FieldDisp+": '"+CharClass.ARMOR_LONGDESC[CMath.s_int(E.getStat(Field))]+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt(getScr("BaseGenerics","enter",CMParms.toStringList(CharClass.ARMOR_DESCS)),"");
		String newStat="";
		for(int i=0;i<CharClass.ARMOR_DESCS.length;i++)
			if(newName.equalsIgnoreCase(CharClass.ARMOR_DESCS[i]))
				newStat=""+i;
		if(newStat.length()>0)
			E.setStat(Field,newStat);
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}
	static void genQualifications(MOB mob, CharClass E, int showNumber, int showFlag, String FieldDisp, String Field)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". "+FieldDisp+": '"+CMLib.masking().maskDesc(E.getStat(Field))+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName="?";
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(newName.equals("?")))
		{
			newName=mob.session().prompt(getScr("BaseGenerics","entermask"),"");
			if(newName.equals("?"))
				mob.tell(CMLib.masking().maskHelp("\n",getScr("BaseGenerics","allows")));
		}
		if((newName.length()>0)&&(!newName.equals("?")))
			E.setStat(Field,newName);
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}
	static void genClanAccept(MOB mob, Clan E, int showNumber, int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". "+getScr("BaseGenerics","clanaccept")+": '"+CMLib.masking().maskDesc(E.getAcceptanceSettings())+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName="?";
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(newName.equals("?")))
		{
			newName=mob.session().prompt(getScr("BaseGenerics","entermask"),"");
			if(newName.equals("?"))
				mob.tell(CMLib.masking().maskHelp("\n",getScr("BaseGenerics","allows")));
		}
		if((newName.length()>0)&&(!newName.equals("?")))
			E.setAcceptanceSettings(newName);
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}
	static void genWeaponRestr(MOB mob, CharClass E, int showNumber, int showFlag, String FieldDisp, String FieldNum, String Field)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		Vector set=CMParms.parseCommas(E.getStat(Field),true);
		StringBuffer str=new StringBuffer("");
		for(int v=0;v<set.size();v++)
			str.append(" "+Weapon.classifictionDescription[CMath.s_int((String)set.elementAt(v))].toLowerCase());

		mob.tell(showNumber+". "+FieldDisp+": '"+str.toString()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName="?";
		boolean setChanged=false;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(newName.equals("?")))
		{
			newName=mob.session().prompt(getScr("BaseGenerics","weapclass"),"");
			if(newName.equals("?"))
				mob.tell(CMParms.toStringList(Weapon.classifictionDescription));
			else
			if(newName.length()>0)
			{
				int foundCode=-1;
				for(int i=0;i<Weapon.classifictionDescription.length;i++)
					if(Weapon.classifictionDescription[i].equalsIgnoreCase(newName))
						foundCode=i;
				if(foundCode<0)
				{
					mob.tell(getScr("BaseGenerics","namenotrec",newName));
					newName="?";
				}
				else
				{
					int x=set.indexOf(""+foundCode);
					if(x>=0)
					{
						setChanged=true;
						set.removeElementAt(x);
						mob.tell(getScr("BaseGenerics","namerem",newName));
						newName="?";
					}
					else
					{
						set.addElement(""+foundCode);
						setChanged=true;
						mob.tell(getScr("BaseGenerics","nameadd",newName));
						newName="?";
					}
				}
			}
		}
		if(setChanged)
			E.setStat(Field,CMParms.toStringList(set));
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}
	static void genInt(MOB mob, CharClass E, int showNumber, int showFlag, String FieldDisp, String Field)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". "+FieldDisp+": '"+E.getStat(Field)+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt(getScr("BaseGenerics","enternewone"),"");
		if((newName.length()>0)&&((newName.trim().equals("0"))||(CMath.s_int(newName)!=0)))
			E.setStat(Field,""+CMath.s_int(newName));
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}
	static void genInt(MOB mob, Race E, int showNumber, int showFlag, String FieldDisp, String Field)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". "+FieldDisp+": '"+E.getStat(Field)+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt(getScr("BaseGenerics","enternewone"),"");
		if((newName.length()>0)&&((newName.trim().equals("0"))||(CMath.s_int(newName)!=0)))
			E.setStat(Field,""+CMath.s_int(newName));
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}
	static void genBool(MOB mob, Race E, int showNumber, int showFlag, String FieldDisp, String Field)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". "+FieldDisp+": '"+E.getStat(Field)+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt(getScr("BaseGenerics","truefalse"),"");
		if((newName.length()>0)&&(newName.equalsIgnoreCase("true")||newName.equalsIgnoreCase("false")))
			E.setStat(Field,newName.toLowerCase());
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}
	static void genBool(MOB mob, CharClass E, int showNumber, int showFlag, String FieldDisp, String Field)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". "+FieldDisp+": '"+E.getStat(Field)+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt(getScr("BaseGenerics","truefalse"),"");
		if((newName.length()>0)&&(newName.equalsIgnoreCase("true")||newName.equalsIgnoreCase("false")))
			E.setStat(Field,newName.toLowerCase());
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}
	static void genRaceAvailability(MOB mob, Race E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","availab",showNumber+"",Area.THEME_DESCS_EXT[CMath.s_int(E.getStat("AVAIL"))]));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName="?";
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(newName.equals("?")))
		{
			newName=mob.session().prompt(getScr("BaseGenerics","entnewvalueh"),"");
			if(newName.length()==0)
				mob.tell(getScr("BaseGenerics","nochange"));
			else
			if((CMath.isNumber(newName))&&(CMath.s_int(newName)<Area.THEME_DESCS_EXT.length))
				E.setStat("AVAIL",""+CMath.s_int(newName));
			else
			if(newName.equalsIgnoreCase("?"))
			{
			    StringBuffer str=new StringBuffer(getScr("BaseGenerics","validv"));
			    for(int i=0;i<Area.THEME_DESCS_EXT.length;i++)
			        str.append(i+") "+Area.THEME_DESCS_EXT[i]+"\n\r");
			    mob.tell(str.toString());
			}
			else
				mob.tell(getScr("BaseGenerics","nochange"));
		}
	}
    static void genClassAvailability(MOB mob, CharClass E, int showNumber, int showFlag)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.tell(showNumber+". Availability: '"+Area.THEME_DESCS_EXT[CMath.s_int(E.getStat("PLAYER"))]+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String newName="?";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(newName.equals("?")))
        {
            newName=mob.session().prompt(getScr("BaseGenerics","entnewvalueh"),"");
            if(newName.length()==0)
                mob.tell(getScr("BaseGenerics","nochange"));
            else
            if((CMath.isNumber(newName))&&(CMath.s_int(newName)<Area.THEME_DESCS_EXT.length))
                E.setStat("PLAYER",""+CMath.s_int(newName));
            else
            if(newName.equalsIgnoreCase("?"))
            {
                StringBuffer str=new StringBuffer(getScr("BaseGenerics","validval"));
                for(int i=0;i<Area.THEME_DESCS_EXT.length;i++)
                    str.append(i+") "+Area.THEME_DESCS_EXT[i]+"\n\r");
                mob.tell(str.toString());
            }
            else
                mob.tell(getScr("BaseGenerics","nochange"));
        }
    }
    
	static void genCat(MOB mob, Race E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","racialcate",showNumber+"",E.racialCategory()+""));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt(getScr("BaseGenerics","enternewone"),"");
		if(newName.length()>0)
		{
			boolean found=false;
			if(newName.startsWith("new "))
			{
				newName=CMStrings.capitalizeAndLower(newName.substring(4));
				if(newName.length()>0)
					found=true;
			}
			else
			for(Enumeration r=CMClass.races();r.hasMoreElements();)
			{
				Race R=(Race)r.nextElement();
				if(newName.equalsIgnoreCase(R.racialCategory()))
				{
					newName=R.racialCategory();
					found=true;
					break;
				}
			}
			if(!found)
			{
				StringBuffer str=new StringBuffer(getScr("BaseGenerics","cateerr"));
				HashSet H=new HashSet();
				for(Enumeration r=CMClass.races();r.hasMoreElements();)
				{
					Race R=(Race)r.nextElement();
					if(!H.contains(R.racialCategory()))
					{
						H.add(R.racialCategory());
						str.append(R.racialCategory()+", ");
					}
				}
				mob.tell(str.toString().substring(0,str.length()-2)+".");
			}
			else
				E.setStat("CAT",newName);
		}
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}
	static void genHealthBuddy(MOB mob, Race E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","healthrace",showNumber+"",E.getStat("HEALTHRACE")));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt(getScr("BaseGenerics","enternewone"),"");
		if(newName.length()>0)
		{
			Race R2=CMClass.getRace(newName);
			if((R2!=null)&&(R2.isGeneric()))
				R2=null;
			if(R2==null)
			{
				StringBuffer str=new StringBuffer(getScr("BaseGenerics","racenameinv"));
				for(Enumeration r=CMClass.races();r.hasMoreElements();)
				{
					Race R=(Race)r.nextElement();
					if(!R.isGeneric())
						str.append(R.ID()+", ");
				}
				mob.tell(str.toString().substring(0,str.length()-2)+".");
			}
			else
				E.setStat("HEALTHRACE",R2.ID());
		}
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}
	static void genBodyParts(MOB mob, Race E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		StringBuffer parts=new StringBuffer("");
		for(int i=0;i<Race.BODYPARTSTR.length;i++)
			if(E.bodyMask()[i]!=0) parts.append(Race.BODYPARTSTR[i].toLowerCase()+"("+E.bodyMask()[i]+") ");
		mob.tell(getScr("BaseGenerics","bodyparts",showNumber+"",parts.toString()));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt(getScr("BaseGenerics","bodypart"),"");
		if(newName.length()>0)
		{
			int partNum=-1;
			for(int i=0;i<Race.BODYPARTSTR.length;i++)
				if(newName.equalsIgnoreCase(Race.BODYPARTSTR[i]))
				{ partNum=i; break;}
			if(partNum<0)
			{
				StringBuffer str=new StringBuffer(getScr("BaseGenerics","bodyerr"));
				for(int i=0;i<Race.BODYPARTSTR.length;i++)
					str.append(Race.BODYPARTSTR[i]+", ");
				mob.tell(str.toString().substring(0,str.length()-2)+".");
			}
			else
			{
				newName=mob.session().prompt(getScr("BaseGenerics","newnumb",E.bodyMask()[partNum]+""),""+E.bodyMask()[partNum]);
				if(newName.length()>0)
					E.bodyMask()[partNum]=CMath.s_int(newName);
				else
					mob.tell(getScr("BaseGenerics","nochange"));
			}
		}
		else
			mob.tell(getScr("BaseGenerics","nochange"));
	}
	static void genEStats(MOB mob, Race R, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
        EnvStats S=(EnvStats)CMClass.getCommon("DefaultEnvStats");
        S.setAllValues(0);
		CMLib.coffeeMaker().setEnvStats(S,R.getStat("ESTATS"));
		StringBuffer parts=new StringBuffer("");
		for(int i=0;i<S.getCodes().length;i++)
			if(CMath.s_int(S.getStat(S.getCodes()[i]))!=0)
				parts.append(CMStrings.capitalizeAndLower(S.getCodes()[i])+"("+S.getStat(S.getCodes()[i])+") ");
		mob.tell(getScr("BaseGenerics","Estatadj",showNumber+"",parts.toString()));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
        boolean done=false;
        while((!done)&&(mob.session()!=null)&&(!mob.session().killFlag()))
        {
    		String newName=mob.session().prompt(getScr("BaseGenerics","statname"),"");
    		if(newName.length()>0)
    		{
    			String partName=null;
    			for(int i=0;i<S.getCodes().length;i++)
    				if(newName.equalsIgnoreCase(S.getCodes()[i]))
    				{ partName=S.getCodes()[i]; break;}
    			if(partName==null)
    			{
    				StringBuffer str=new StringBuffer(getScr("BaseGenerics","staterr"));
    				for(int i=0;i<S.getCodes().length;i++)
    					str.append(S.getCodes()[i]+", ");
    				mob.tell(str.toString().substring(0,str.length()-2)+".");
    			}
    			else
    			{
    				boolean checkChange=false;
    				if(partName.equals("DISPOSITION"))
    				{
    					genDisposition(mob,S,0,0);
    					checkChange=true;
    				}
    				else
    				if(partName.equals("SENSES"))
    				{
    					genSensesMask(mob,S,0,0);
    					checkChange=true;
    				}
    				else
    				{
    					newName=mob.session().prompt(getScr("BaseGenerics","entvaluep"),"");
    					if(newName.length()>0)
    					{
    						S.setStat(partName,newName);
    						checkChange=true;
    					}
    					else
    						mob.tell(getScr("BaseGenerics","nochange"));
    				}
    				if(checkChange)
    				{
    					boolean zereoed=true;
    					for(int i=0;i<S.getCodes().length;i++)
    					{
    						if(CMath.s_int(S.getStat(S.getCodes()[i]))!=0)
    						{ zereoed=false; break;}
    					}
    					if(zereoed)
    						R.setStat("ESTATS","");
    					else
    						R.setStat("ESTATS",CMLib.coffeeMaker().getEnvStatsStr(S));
    				}
    			}
    		}
    		else
            {
                mob.tell(getScr("BaseGenerics","nochange"));
                done=true;
            }
        }
	}
	static void genAState(MOB mob,
	        			  Race R,
	        			  String field,
	        			  String prompt,
	        			  int showNumber,
	        			  int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		CharState S=(CharState)CMClass.getCommon("DefaultCharState"); S.setAllValues(0);
		CMLib.coffeeMaker().setCharState(S,R.getStat(field));
		StringBuffer parts=new StringBuffer("");
		for(int i=0;i<S.getStatCodes().length;i++)
			if(CMath.s_int(S.getStat(S.getStatCodes()[i]))!=0)
				parts.append(CMStrings.capitalizeAndLower(S.getStatCodes()[i])+"("+S.getStat(S.getStatCodes()[i])+") ");
		mob.tell(showNumber+". "+prompt+": "+parts.toString()+".");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
        boolean done=false;
        while((!done)&&(mob.session()!=null)&&(!mob.session().killFlag()))
        {
    		String newName=mob.session().prompt(getScr("BaseGenerics","statname"),"");
    		if(newName.length()>0)
    		{
    			String partName=null;
    			for(int i=0;i<S.getStatCodes().length;i++)
    				if(newName.equalsIgnoreCase(S.getStatCodes()[i]))
    				{ partName=S.getStatCodes()[i]; break;}
    			if(partName==null)
    			{
    				StringBuffer str=new StringBuffer(getScr("BaseGenerics","staterr"));
    				for(int i=0;i<S.getStatCodes().length;i++)
    					str.append(S.getStatCodes()[i]+", ");
    				mob.tell(str.toString().substring(0,str.length()-2)+".");
    			}
    			else
    			{
    				newName=mob.session().prompt(getScr("BaseGenerics","entvaluep"),"");
    				if(newName.length()>0)
    				{
    					S.setStat(partName,newName);
    					boolean zereoed=true;
    					for(int i=0;i<S.getStatCodes().length;i++)
    					{
    						if(CMath.s_int(S.getStat(S.getStatCodes()[i]))!=0)
    						{ zereoed=false; break;}
    					}
    					if(zereoed)
    						R.setStat(field,"");
    					else
    						R.setStat(field,CMLib.coffeeMaker().getCharStateStr(S));
    				}
    				else
    					mob.tell(getScr("BaseGenerics","nochange"));
    			}
    		}
    		else
            {
                mob.tell(getScr("BaseGenerics","nochange"));
                done=true;
            }
        }
	}
	static void genAStats(MOB mob, Race R, String Field, String FieldName, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
        CharStats S=(CharStats)CMClass.getCommon("DefaultCharStats"); S.setAllValues(0);
		CMLib.coffeeMaker().setCharStats(S,R.getStat(Field));
		StringBuffer parts=new StringBuffer("");
		for(int i=0;i<CharStats.STAT_DESCS.length;i++)
			if(S.getStat(i)!=0)
				parts.append(CMStrings.capitalizeAndLower(CharStats.STAT_DESCS[i])+"("+S.getStat(i)+") ");
		mob.tell(showNumber+". "+FieldName+": "+parts.toString()+".");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
        boolean done=false;
        while((!done)&&(mob.session()!=null)&&(!mob.session().killFlag()))
        {
    		String newName=mob.session().prompt(getScr("BaseGenerics","statname"),"");
    		if(newName.length()>0)
    		{
    			int partNum=-1;
    			for(int i=0;i<CharStats.STAT_DESCS.length;i++)
    				if(newName.equalsIgnoreCase(CharStats.STAT_DESCS[i]))
    				{ partNum=i; break;}
    			if(partNum<0)
    			{
    				StringBuffer str=new StringBuffer(getScr("BaseGenerics","staterr"));
    				for(int i=0;i<CharStats.STAT_DESCS.length;i++)
    					str.append(CharStats.STAT_DESCS[i]+", ");
    				mob.tell(str.toString().substring(0,str.length()-2)+".");
    			}
    			else
    			{
    				newName=mob.session().prompt(getScr("BaseGenerics","entvaluep"),"");
    				if(newName.length()>0)
    				{
    					if(newName.trim().equalsIgnoreCase("0"))
        					S.setStat(partNum,CMath.s_int(newName));
    					else
                        if(partNum==CharStats.STAT_GENDER)
                            S.setStat(partNum,(int)newName.charAt(0));
                        else
        					S.setStat(partNum,CMath.s_int(newName));
    					boolean zereoed=true;
    					for(int i=0;i<CharStats.STAT_DESCS.length;i++)
    					{
    						if(S.getStat(i)!=0)
    						{ zereoed=false; break;}
    					}
    					if(zereoed)
    						R.setStat(Field,"");
    					else
    						R.setStat(Field,CMLib.coffeeMaker().getCharStatsStr(S));
    				}
    				else
    					mob.tell(getScr("BaseGenerics","nochange"));
    			}
    		}
    		else
            {
    			mob.tell(getScr("BaseGenerics","nochange"));
                done=true;
            }
        }
	}

	static void genEStats(MOB mob, CharClass R, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
        EnvStats S=(EnvStats)CMClass.getCommon("DefaultEnvStats");
        S.setAllValues(0);
		CMLib.coffeeMaker().setEnvStats(S,R.getStat("ESTATS"));
		StringBuffer parts=new StringBuffer("");
		for(int i=0;i<S.getCodes().length;i++)
			if(CMath.s_int(S.getStat(S.getCodes()[i]))!=0)
				parts.append(CMStrings.capitalizeAndLower(S.getCodes()[i])+"("+S.getStat(S.getCodes()[i])+") ");
		mob.tell(getScr("BaseGenerics","Estatadjline",showNumber+"",parts.toString()));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
        boolean done=false;
        while((!done)&&(mob.session()!=null)&&(!mob.session().killFlag()))
        {
    		String newName=mob.session().prompt(getScr("BaseGenerics","statname"),"");
    		if(newName.length()>0)
    		{
    			String partName=null;
    			for(int i=0;i<S.getCodes().length;i++)
    				if(newName.equalsIgnoreCase(S.getCodes()[i]))
    				{ partName=S.getCodes()[i]; break;}
    			if(partName==null)
    			{
    				StringBuffer str=new StringBuffer(getScr("BaseGenerics","staterr"));
    				for(int i=0;i<S.getCodes().length;i++)
    					str.append(S.getCodes()[i]+", ");
    				mob.tell(str.toString().substring(0,str.length()-2)+".");
    			}
    			else
    			{
    				boolean checkChange=false;
    				if(partName.equals("DISPOSITION"))
    				{
    					genDisposition(mob,S,0,0);
    					checkChange=true;
    				}
    				else
    				if(partName.equals("SENSES"))
    				{
    					genSensesMask(mob,S,0,0);
    					checkChange=true;
    				}
    				else
    				{
    					newName=mob.session().prompt(getScr("BaseGenerics","entvaluep"),"");
    					if(newName.length()>0)
    					{
    						S.setStat(partName,newName);
    						checkChange=true;
    					}
    					else
    						mob.tell(getScr("BaseGenerics","nochange"));
    				}
    				if(checkChange)
    				{
    					boolean zereoed=true;
    					for(int i=0;i<S.getCodes().length;i++)
    					{
    						if(CMath.s_int(S.getStat(S.getCodes()[i]))!=0)
    						{ zereoed=false; break;}
    					}
    					if(zereoed)
    						R.setStat("ESTATS","");
    					else
    						R.setStat("ESTATS",CMLib.coffeeMaker().getEnvStatsStr(S));
    				}
    			}
    		}
    		else
            {
    			mob.tell(getScr("BaseGenerics","nochange"));
                done=true;
            }
        }
	}
	static void genAState(MOB mob,
	        			  CharClass R,
	        			  String field,
	        			  String prompt,
	        			  int showNumber,
	        			  int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
        CharState S=(CharState)CMClass.getCommon("DefaultCharState"); S.setAllValues(0);
		CMLib.coffeeMaker().setCharState(S,R.getStat(field));
		StringBuffer parts=new StringBuffer("");
		for(int i=0;i<S.getStatCodes().length;i++)
			if(CMath.s_int(S.getStat(S.getStatCodes()[i]))!=0)
				parts.append(CMStrings.capitalizeAndLower(S.getStatCodes()[i])+"("+S.getStat(S.getStatCodes()[i])+") ");
		mob.tell(showNumber+". "+prompt+": "+parts.toString()+".");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
        boolean done=false;
        while((!done)&&(mob.session()!=null)&&(!mob.session().killFlag()))
        {
    		String newName=mob.session().prompt(getScr("BaseGenerics","statname"),"");
    		if(newName.length()>0)
    		{
    			String partName=null;
    			for(int i=0;i<S.getStatCodes().length;i++)
    				if(newName.equalsIgnoreCase(S.getStatCodes()[i]))
    				{ partName=S.getStatCodes()[i]; break;}
    			if(partName==null)
    			{
    				StringBuffer str=new StringBuffer(getScr("BaseGenerics","staterr"));
    				for(int i=0;i<S.getStatCodes().length;i++)
    					str.append(S.getStatCodes()[i]+", ");
    				mob.tell(str.toString().substring(0,str.length()-2)+".");
    			}
    			else
    			{
    				newName=mob.session().prompt(getScr("BaseGenerics","entvaluep"),"");
    				if(newName.length()>0)
    				{
    					S.setStat(partName,newName);
    					boolean zereoed=true;
    					for(int i=0;i<S.getStatCodes().length;i++)
    					{
    						if(CMath.s_int(S.getStat(S.getStatCodes()[i]))!=0)
    						{ zereoed=false; break;}
    					}
    					if(zereoed)
    						R.setStat(field,"");
    					else
    						R.setStat(field,CMLib.coffeeMaker().getCharStateStr(S));
    				}
    				else
    					mob.tell(getScr("BaseGenerics","nochange"));
    			}
    		}
    		else
            {
    			mob.tell(getScr("BaseGenerics","nochange"));
                done=true;
            }
        }
	}
	static void genAStats(MOB mob, CharClass R, String Field, String FieldName, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
        CharStats S=(CharStats)CMClass.getCommon("DefaultCharStats"); S.setAllValues(0);
		CMLib.coffeeMaker().setCharStats(S,R.getStat(Field));
		StringBuffer parts=new StringBuffer("");
		for(int i=0;i<CharStats.STAT_DESCS.length;i++)
			if(S.getStat(i)!=0)
				parts.append(CMStrings.capitalizeAndLower(CharStats.STAT_DESCS[i])+"("+S.getStat(i)+") ");
		mob.tell(showNumber+". "+FieldName+": "+parts.toString()+".");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
        boolean done=false;
        while((!done)&&(mob.session()!=null)&&(!mob.session().killFlag()))
        {
    		String newName=mob.session().prompt(getScr("BaseGenerics","statname"),"");
    		if(newName.length()>0)
    		{
    			int partNum=-1;
    			for(int i=0;i<CharStats.STAT_DESCS.length;i++)
    				if(newName.equalsIgnoreCase(CharStats.STAT_DESCS[i]))
    				{ partNum=i; break;}
    			if(partNum<0)
    			{
    				StringBuffer str=new StringBuffer(getScr("BaseGenerics","staterr"));
    				for(int i=0;i<CharStats.STAT_DESCS.length;i++)
    					str.append(CharStats.STAT_DESCS[i]+", ");
    				mob.tell(str.toString().substring(0,str.length()-2)+".");
    			}
    			else
    			{
    				newName=mob.session().prompt(getScr("BaseGenerics","entvaluep"),"");
    				if(newName.length()>0)
    				{
    					S.setStat(partNum,CMath.s_int(newName));
    					boolean zereoed=true;
    					for(int i=0;i<CharStats.STAT_DESCS.length;i++)
    					{
    						if(S.getStat(i)!=0)
    						{ zereoed=false; break;}
    					}
    					if(zereoed)
    						R.setStat(Field,"");
    					else
    						R.setStat(Field,CMLib.coffeeMaker().getCharStatsStr(S));
    				}
    				else
    					mob.tell(getScr("BaseGenerics","nochange"));
    			}
    		}
    		else
            {
    			mob.tell(getScr("BaseGenerics","nochange"));
                done=true;
            }
        }
	}
	static void genResources(MOB mob, Race E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(true))
		{
			StringBuffer parts=new StringBuffer("");
			int numResources=CMath.s_int(E.getStat("NUMRSC"));
			Vector V=new Vector();
			for(int v=0;v<numResources;v++)
			{
				Item I=CMClass.getItem(E.getStat("GETRSCID"+v));
				if(I!=null)
				{
					I.setMiscText(E.getStat("GETRSCPARM"+v));
					I.recoverEnvStats();
					parts.append(I.name()+", ");
					V.addElement(I);
				}
			}
			if(parts.toString().endsWith(", "))
			{parts.deleteCharAt(parts.length()-1);parts.deleteCharAt(parts.length()-1);}
			mob.tell(getScr("BaseGenerics","resources",showNumber+"",parts.toString()));
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			String newName=mob.session().prompt(getScr("BaseGenerics","resname"),"");
			if(newName.length()>0)
			{
				int partNum=-1;
				for(int i=0;i<V.size();i++)
					if(CMLib.english().containsString(((Item)V.elementAt(i)).name(),newName))
					{ partNum=i; break;}
				boolean updateList=false;
				if(partNum<0)
				{
					if(!newName.toLowerCase().startsWith("new "))
						mob.tell(getScr("BaseGenerics","reserr"));
					else
					{
						Item I=mob.fetchCarried(null,newName.substring(4).trim());
						if(I!=null)
						{
							I=(Item)I.copyOf();
							V.addElement(I);
							mob.tell(getScr("BaseGenerics","inameadd",I.name()));
							updateList=true;
						}

					}
				}
				else
				{
					Item I=(Item)V.elementAt(partNum);
					V.removeElementAt(partNum);
					mob.tell(getScr("BaseGenerics","inamerem",I.name()));
					updateList=true;
				}
				if(updateList)
				{
					E.setStat("NUMRSC","");
					for(int i=0;i<V.size();i++)
						E.setStat("GETRSCID"+i,((Item)V.elementAt(i)).ID());
					for(int i=0;i<V.size();i++)
						E.setStat("GETRSCPARM"+i,((Item)V.elementAt(i)).text());
				}
			}
			else
			{
				mob.tell(getScr("BaseGenerics","nochange"));
				return;
			}
		}
	}
	static void genOutfit(MOB mob, Race E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(true))
		{
			StringBuffer parts=new StringBuffer("");
			int numResources=CMath.s_int(E.getStat("NUMOFT"));
			Vector V=new Vector();
			for(int v=0;v<numResources;v++)
			{
				Item I=CMClass.getItem(E.getStat("GETOFTID"+v));
				if(I!=null)
				{
					I.setMiscText(E.getStat("GETOFTPARM"+v));
					I.recoverEnvStats();
					parts.append(I.name()+", ");
					V.addElement(I);
				}
			}
			if(parts.toString().endsWith(", "))
			{parts.deleteCharAt(parts.length()-1);parts.deleteCharAt(parts.length()-1);}
			mob.tell(getScr("BaseGenerics","outfit",showNumber+"",parts.toString()));
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			String newName=mob.session().prompt(getScr("BaseGenerics","itemname"),"");
			if(newName.length()>0)
			{
				int partNum=-1;
				for(int i=0;i<V.size();i++)
					if(CMLib.english().containsString(((Item)V.elementAt(i)).name(),newName))
					{ partNum=i; break;}
				boolean updateList=false;
				if(partNum<0)
				{
					if(!newName.toLowerCase().startsWith("new "))
						mob.tell(getScr("BaseGenerics","nameitemerr"));
					else
					{
						Item I=mob.fetchCarried(null,newName.substring(4).trim());
						if(I!=null)
						{
							I=(Item)I.copyOf();
							V.addElement(I);
							mob.tell(getScr("BaseGenerics","inameadd",I.name()));
							updateList=true;
						}

					}
				}
				else
				{
					Item I=(Item)V.elementAt(partNum);
					V.removeElementAt(partNum);
					mob.tell(getScr("BaseGenerics","inamerem",I.name()));
					updateList=true;
				}
				if(updateList)
				{
					E.setStat("NUMOFT","");
					for(int i=0;i<V.size();i++)
						E.setStat("GETOFTID"+i,((Item)V.elementAt(i)).ID());
					for(int i=0;i<V.size();i++)
						E.setStat("GETOFTPARM"+i,((Item)V.elementAt(i)).text());
				}
			}
			else
			{
				mob.tell(getScr("BaseGenerics","nochange"));
				return;
			}
		}
	}
	static void genOutfit(MOB mob, CharClass E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(true))
		{
			StringBuffer parts=new StringBuffer("");
			int numResources=CMath.s_int(E.getStat("NUMOFT"));
			Vector V=new Vector();
			for(int v=0;v<numResources;v++)
			{
				Item I=CMClass.getItem(E.getStat("GETOFTID"+v));
				if(I!=null)
				{
					I.setMiscText(E.getStat("GETOFTPARM"+v));
					I.recoverEnvStats();
					parts.append(I.name()+", ");
					V.addElement(I);
				}
			}
			if(parts.toString().endsWith(", "))
			{parts.deleteCharAt(parts.length()-1);parts.deleteCharAt(parts.length()-1);}
			mob.tell(getScr("BaseGenerics","outfitline",showNumber+"",parts.toString()));
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			String newName=mob.session().prompt(getScr("BaseGenerics","itemname"),"");
			if(newName.length()>0)
			{
				int partNum=-1;
				for(int i=0;i<V.size();i++)
					if(CMLib.english().containsString(((Item)V.elementAt(i)).name(),newName))
					{ partNum=i; break;}
				boolean updateList=false;
				if(partNum<0)
				{
					if(!newName.toLowerCase().startsWith("new "))
						mob.tell(getScr("BaseGenerics","nameitemerr"));
					else
					{
						Item I=mob.fetchCarried(null,newName.substring(4).trim());
						if(I!=null)
						{
							I=(Item)I.copyOf();
							V.addElement(I);
							mob.tell(getScr("BaseGenerics","inameadd",I.name()));
							updateList=true;
						}

					}
				}
				else
				{
					Item I=(Item)V.elementAt(partNum);
					V.removeElementAt(partNum);
					mob.tell(getScr("BaseGenerics","inamerem",I.name()));
					updateList=true;
				}
				if(updateList)
				{
					E.setStat("NUMOFT","");
					for(int i=0;i<V.size();i++)
						E.setStat("GETOFTID"+i,((Item)V.elementAt(i)).ID());
					for(int i=0;i<V.size();i++)
						E.setStat("GETOFTPARM"+i,((Item)V.elementAt(i)).text());
				}
			}
			else
			{
				mob.tell(getScr("BaseGenerics","nochange"));
				return;
			}
		}
	}
	static void genWeapon(MOB mob, Race E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		StringBuffer parts=new StringBuffer("");
		Item I=CMClass.getItem(E.getStat("WEAPONCLASS"));
		if(I!=null)
		{
			I.setMiscText(E.getStat("WEAPONXML"));
			I.recoverEnvStats();
			parts.append(I.name());
		}
		mob.tell(getScr("BaseGenerics","naturalweap",showNumber+"",parts.toString()));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt(getScr("BaseGenerics","weapname"),"");
		if(newName.equalsIgnoreCase("null"))
		{
			E.setStat("WEAPONCLASS","");
			mob.tell(getScr("BaseGenerics","humanweapset"));
		}
		else
		if(newName.length()>0)
		{
			I=mob.fetchCarried(null,newName);
			if(I==null)
			{
				mob.tell(getScr("BaseGenerics","errinventory",newName));
				mob.tell(getScr("BaseGenerics","nochange"));
				return;
			}
			I=(Item)I.copyOf();
			E.setStat("WEAPONCLASS",I.ID());
			E.setStat("WEAPONXML",I.text());
		}
		else
		{
			mob.tell(getScr("BaseGenerics","nochange"));
			return;
		}
	}

	static void genAgingChart(MOB mob, Race E, int showNumber, int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;

		mob.tell(getScr("BaseGenerics","agingchart",showNumber+"",CMParms.toStringList(E.getAgingChart())));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(true))
		{
			String newName=mob.session().prompt(getScr("BaseGenerics","comma"),"");
			if(newName.length()==0)
			{
				mob.tell(getScr("BaseGenerics","nochange"));
				return;
			}
			Vector V=CMParms.parseCommas(newName,true);
			if(V.size()==9)
			{
			    int highest=-1;
			    boolean cont=false;
			    for(int i=0;i<V.size();i++)
			    {
			        if(CMath.s_int((String)V.elementAt(i))<highest)
			        {
			            mob.tell(getScr("BaseGenerics","entryout",((String)V.elementAt(i))));
			            cont=true;
			            break;
			        }
			        highest=CMath.s_int((String)V.elementAt(i));
			    }
			    if(cont) continue;
			    E.setStat("AGING",newName);
			    break;
			}
		}
	}

	static void genRaceFlags(MOB mob, Race E, int showNumber, int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
		    return;

		int flags=CMath.s_int(E.getStat("DISFLAGS"));
		StringBuffer sets=new StringBuffer("");
	    if(CMath.bset(flags,Race.GENFLAG_NOCLASS))
		    sets.append(getScr("BaseGenerics","classless"));
	    if(CMath.bset(flags,Race.GENFLAG_NOLEVELS))
		    sets.append(getScr("BaseGenerics","leveless"));
	    if(CMath.bset(flags,Race.GENFLAG_NOEXP))
		    sets.append(getScr("BaseGenerics","expless"));

		mob.tell(getScr("BaseGenerics","extraracflags",showNumber+"",sets.toString()));
		if((showFlag!=showNumber)&&(showFlag>-999))
		    return;
		String newName=mob.session().prompt(getScr("BaseGenerics","esslist"),"");
		switch(CMath.s_int(newName))
		{
		case 1:
		    if(CMath.bset(flags,Race.GENFLAG_NOCLASS))
		        flags=CMath.unsetb(flags,Race.GENFLAG_NOCLASS);
		    else
		        flags=flags|Race.GENFLAG_NOCLASS;
		    break;
		case 2:
		    if(CMath.bset(flags,Race.GENFLAG_NOLEVELS))
		        flags=CMath.unsetb(flags,Race.GENFLAG_NOLEVELS);
		    else
		        flags=flags|Race.GENFLAG_NOLEVELS;
		    break;
		case 3:
		    if(CMath.bset(flags,Race.GENFLAG_NOEXP))
		        flags=CMath.unsetb(flags,Race.GENFLAG_NOEXP);
		    else
		        flags=flags|Race.GENFLAG_NOEXP;
		    break;
		default:
			mob.tell(getScr("BaseGenerics","nochange"));
			break;
		}
		E.setStat("DISFLAGS",""+flags);
	}

	static void genClassFlags(MOB mob, CharClass E, int showNumber, int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
		    return;

		int flags=CMath.s_int(E.getStat("DISFLAGS"));
		StringBuffer sets=new StringBuffer("");
	    if(CMath.bset(flags,CharClass.GENFLAG_NORACE))
		    sets.append(getScr("BaseGenerics","raceless"));
	    if(CMath.bset(flags,CharClass.GENFLAG_NOLEVELS))
		    sets.append(getScr("BaseGenerics","leveless"));
	    if(CMath.bset(flags,CharClass.GENFLAG_NOEXP))
		    sets.append(getScr("BaseGenerics","expless"));

		mob.tell(getScr("BaseGenerics","extracharcfl",showNumber+"",sets.toString()));
		if((showFlag!=showNumber)&&(showFlag>-999))
		    return;
		String newName=mob.session().prompt(getScr("BaseGenerics","esslist"),"");
		switch(CMath.s_int(newName))
		{
		case 1:
		    if(CMath.bset(flags,CharClass.GENFLAG_NORACE))
		        flags=CMath.unsetb(flags,CharClass.GENFLAG_NORACE);
		    else
		        flags=flags|CharClass.GENFLAG_NORACE;
		    break;
		case 2:
		    if(CMath.bset(flags,CharClass.GENFLAG_NOLEVELS))
		        flags=CMath.unsetb(flags,CharClass.GENFLAG_NOLEVELS);
		    else
		        flags=flags|CharClass.GENFLAG_NOLEVELS;
		    break;
		case 3:
		    if(CMath.bset(flags,CharClass.GENFLAG_NOEXP))
		        flags=CMath.unsetb(flags,CharClass.GENFLAG_NOEXP);
		    else
		        flags=flags|CharClass.GENFLAG_NOEXP;
		    break;
		default:
			mob.tell(getScr("BaseGenerics","nochange"));
			break;
		}
		E.setStat("DISFLAGS",""+flags);
	}

	static void genRacialAbilities(MOB mob, Race E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(true))
		{
			StringBuffer parts=new StringBuffer("");
			int numResources=CMath.s_int(E.getStat("NUMRABLE"));
			Vector ables=new Vector();
			Vector data=new Vector();
			for(int v=0;v<numResources;v++)
			{
				Ability A=CMClass.getAbility(E.getStat("GETRABLE"+v));
				if(A!=null)
				{
					parts.append("("+A.ID()+"/"+E.getStat("GETRABLELVL"+v)+"/"+E.getStat("GETRABLEQUAL"+v)+"/"+E.getStat("GETRABLEPROF"+v)+"), ");
					ables.addElement(A);
					data.addElement(A.ID()+";"+E.getStat("GETRABLELVL"+v)+";"+E.getStat("GETRABLEQUAL"+v)+";"+E.getStat("GETRABLEPROF"+v));
				}
			}
			if(parts.toString().endsWith(", "))
			{parts.deleteCharAt(parts.length()-1);parts.deleteCharAt(parts.length()-1);}
			mob.tell(getScr("BaseGenerics","racialab",showNumber+"",parts.toString()));
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			String newName=mob.session().prompt(getScr("BaseGenerics","abname"),"");
			if(newName.equalsIgnoreCase("?"))
				mob.tell(CMLib.lister().reallyList(CMClass.abilities(),-1).toString());
			else
			if(newName.length()>0)
			{
				int partNum=-1;
				for(int i=0;i<ables.size();i++)
					if(CMLib.english().containsString(((Ability)ables.elementAt(i)).ID(),newName))
					{ partNum=i; break;}
				boolean updateList=false;
				if(partNum<0)
				{
					Ability A=CMClass.getAbility(newName);
					if(A==null)
						mob.tell(getScr("BaseGenerics","abilityerr"));
					else
					if(A.isAutoInvoked())
						mob.tell(getScr("BaseGenerics","autoinverr",A.name()));
					else
					if((A.triggerStrings()==null)||(A.triggerStrings().length==0))
						mob.tell(getScr("BaseGenerics","notriggererr",A.name()));
					else
					{
						StringBuffer str=new StringBuffer(A.ID()+";");
						String level=mob.session().prompt(getScr("BaseGenerics","skilllev"),"1");
						str.append((""+CMath.s_int(level))+";");
						if(mob.session().confirm(getScr("BaseGenerics","autaut"),"Y"))
							str.append(getScr("BaseGenerics","falseword"));
						else
							str.append(getScr("BaseGenerics","trueword"));
						String prof=mob.session().prompt(getScr("BaseGenerics","proflev"),"100");
						str.append((""+CMath.s_int(prof)));
						data.addElement(str.toString());
						ables.addElement(A);
						mob.tell(getScr("BaseGenerics","anameadd",A.name()));
						updateList=true;
					}
				}
				else
				{
					Ability A=(Ability)ables.elementAt(partNum);
					ables.removeElementAt(partNum);
					data.removeElementAt(partNum);
					updateList=true;
					mob.tell(getScr("BaseGenerics","anamerem",A.name()));
				}
				if(updateList)
				{
					if(data.size()>0)
						E.setStat("NUMRABLE",""+data.size());
					else
						E.setStat("NUMRABLE","");
					for(int i=0;i<data.size();i++)
					{
						Vector V=CMParms.parseSemicolons((String)data.elementAt(i),false);
						E.setStat("GETRABLE"+i,((String)V.elementAt(0)));
						E.setStat("GETRABLELVL"+i,((String)V.elementAt(1)));
						E.setStat("GETRABLEQUAL"+i,((String)V.elementAt(2)));
						E.setStat("GETRABLEPROF"+i,((String)V.elementAt(3)));
					}
				}
			}
			else
			{
				mob.tell(getScr("BaseGenerics","nochange"));
				return;
			}
		}
	}
	static void genRacialEffects(MOB mob, Race E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(true))
		{
			StringBuffer parts=new StringBuffer("");
			int numResources=CMath.s_int(E.getStat("NUMREFF"));
			Vector ables=new Vector();
			Vector data=new Vector();
			for(int v=0;v<numResources;v++)
			{
				Ability A=CMClass.getAbility(E.getStat("GETREFF"+v));
				if(A!=null)
				{
					parts.append("("+A.ID()+"/"+E.getStat("GETREFFLVL"+v)+"/"+E.getStat("GETREFFPARM"+v)+"), ");
					ables.addElement(A);
					data.addElement(A.ID()+"~"+E.getStat("GETREFFLVL"+v)+"~"+E.getStat("GETREFFPARM"+v));
				}
			}
			if(parts.toString().endsWith(", "))
			{parts.deleteCharAt(parts.length()-1);parts.deleteCharAt(parts.length()-1);}
			mob.tell(getScr("BaseGenerics","racialfx",showNumber+"",parts.toString()));
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			String newName=mob.session().prompt(getScr("BaseGenerics","effaddrem"),"");
			if(newName.equalsIgnoreCase("?"))
				mob.tell(CMLib.lister().reallyList(CMClass.abilities(),-1).toString());
			else
			if(newName.length()>0)
			{
				int partNum=-1;
				for(int i=0;i<ables.size();i++)
					if(CMLib.english().containsString(((Ability)ables.elementAt(i)).ID(),newName))
					{ partNum=i; break;}
				boolean updateList=false;
				if(partNum<0)
				{
					Ability A=CMClass.getAbility(newName);
					if(A==null)
						mob.tell(getScr("BaseGenerics","effecterr"));
					else
					{
						StringBuffer str=new StringBuffer(A.ID()+"~");
						String level=mob.session().prompt(getScr("BaseGenerics","entgaine"),"1");
						str.append((""+CMath.s_int(level))+"~");
						String prof=mob.session().prompt(getScr("BaseGenerics","anypar"),"");
						str.append(""+prof);
						data.addElement(str.toString());
						ables.addElement(A);
						mob.tell(getScr("BaseGenerics","anameadd",A.name()));
						updateList=true;
					}
				}
				else
				{
					Ability A=(Ability)ables.elementAt(partNum);
					ables.removeElementAt(partNum);
					data.removeElementAt(partNum);
					updateList=true;
					mob.tell(getScr("BaseGenerics","anamerem",A.name()));
				}
				if(updateList)
				{
					if(data.size()>0)
						E.setStat("NUMREFF",""+data.size());
					else
						E.setStat("NUMREFF","");
					for(int i=0;i<data.size();i++)
					{
						Vector V=CMParms.parseSquiggleDelimited((String)data.elementAt(i),false);
						E.setStat("GETREFF"+i,((String)V.elementAt(0)));
						E.setStat("GETREFFLVL"+i,((String)V.elementAt(1)));
						E.setStat("GETREFFPARM"+i,((String)V.elementAt(2)));
					}
				}
			}
			else
			{
				mob.tell(getScr("BaseGenerics","nochange"));
				return;
			}
		}
	}
	static void genClassAbilities(MOB mob, CharClass E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(true))
		{
			StringBuffer parts=new StringBuffer("");
			int numResources=CMath.s_int(E.getStat("NUMCABLE"));
			Vector ables=new Vector();
			Vector data=new Vector();
			for(int v=0;v<numResources;v++)
			{
				Ability A=CMClass.getAbility(E.getStat("GETCABLE"+v));
				if(A!=null)
				{
					parts.append("("+A.ID()+"/"+E.getStat("GETCABLELVL"+v)+"/"+E.getStat("GETCABLEGAIN"+v)+"/"+E.getStat("GETCABLEPROF"+v)+"), ");
					ables.addElement(A);
					data.addElement(A.ID()+";"+E.getStat("GETCABLELVL"+v)+";"+E.getStat("GETCABLEPROF"+v)+";"+E.getStat("GETCABLEGAIN"+v)+";"+E.getStat("GETCABLESECR"+v)+";"+E.getStat("GETCABLEPARM"+v));
				}
			}
			if(parts.toString().endsWith(", "))
			{parts.deleteCharAt(parts.length()-1);parts.deleteCharAt(parts.length()-1);}
			mob.tell(getScr("BaseGenerics","classabilities",showNumber+"",parts.toString()));
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			String newName=mob.session().prompt(getScr("BaseGenerics","abname"),"");
			if(newName.equalsIgnoreCase("?"))
				mob.tell(CMLib.lister().reallyList(CMClass.abilities(),-1).toString());
			else
			if(newName.length()>0)
			{
				int partNum=-1;
				for(int i=0;i<ables.size();i++)
					if(CMLib.english().containsString(((Ability)ables.elementAt(i)).ID(),newName))
					{ partNum=i; break;}
				boolean updateList=false;
				if(partNum<0)
				{
					Ability A=CMClass.getAbility(newName);
					if(A==null)
						mob.tell(getScr("BaseGenerics","abilityerr"));
					else
					{
						StringBuffer str=new StringBuffer(A.ID()+";");
						String level=mob.session().prompt(getScr("BaseGenerics","skilllev"),"1");
						str.append((""+CMath.s_int(level))+";");
						String prof=mob.session().prompt(getScr("BaseGenerics","defaultprof"),"0");
						str.append((""+CMath.s_int(prof))+";");
						if(mob.session().confirm(getScr("BaseGenerics","autaut"),"Y"))
							str.append(getScr("BaseGenerics","trueword"));
						else
							str.append(getScr("BaseGenerics","falseword"));
						if(mob.session().confirm(getScr("BaseGenerics","sese"),"N"))
							str.append(getScr("BaseGenerics","trueword"));
						else
							str.append(getScr("BaseGenerics","falseword"));
						String parm=mob.session().prompt(getScr("BaseGenerics","anyprop"),"");
						str.append(parm);
						data.addElement(str.toString());
						ables.addElement(A);
						mob.tell(getScr("BaseGenerics","anameadd",A.name()));
						updateList=true;
					}
				}
				else
				{
					Ability A=(Ability)ables.elementAt(partNum);
					ables.removeElementAt(partNum);
					data.removeElementAt(partNum);
					updateList=true;
					mob.tell(getScr("BaseGenerics","anamerem",A.name()));
				}
				if(updateList)
				{
					if(data.size()>0)
						E.setStat("NUMCABLE",""+data.size());
					else
						E.setStat("NUMCABLE","");
					for(int i=0;i<data.size();i++)
					{
						Vector V=CMParms.parseSemicolons((String)data.elementAt(i),false);
						E.setStat("GETCABLE"+i,((String)V.elementAt(0)));
						E.setStat("GETCABLELVL"+i,((String)V.elementAt(1)));
						E.setStat("GETCABLEPROF"+i,((String)V.elementAt(2)));
						E.setStat("GETCABLEGAIN"+i,((String)V.elementAt(3)));
						E.setStat("GETCABLESECR"+i,((String)V.elementAt(4)));
						E.setStat("GETCABLEPARM"+i,((String)V.elementAt(5)));
					}
				}
			}
			else
			{
				mob.tell(getScr("BaseGenerics","nochange"));
				return;
			}
		}
	}
	static void genCulturalAbilities(MOB mob, Race E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(true))
		{
			StringBuffer parts=new StringBuffer("");
			int numResources=CMath.s_int(E.getStat("NUMCABLE"));
			Vector ables=new Vector();
			Vector data=new Vector();
			for(int v=0;v<numResources;v++)
			{
				Ability A=CMClass.getAbility(E.getStat("GETCABLE"+v));
				if(A!=null)
				{
					parts.append("("+A.ID()+"/"+E.getStat("GETCABLEPROF"+v)+"), ");
					ables.addElement(A);
					data.addElement(A.ID()+";"+E.getStat("GETCABLEPROF"+v));
				}
			}
			if(parts.toString().endsWith(", "))
			{parts.deleteCharAt(parts.length()-1);parts.deleteCharAt(parts.length()-1);}
			mob.tell(getScr("BaseGenerics","culturalab",showNumber+"",parts.toString()));
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			String newName=mob.session().prompt(getScr("BaseGenerics","abname"),"");
			if(newName.equalsIgnoreCase("?"))
				mob.tell(CMLib.lister().reallyList(CMClass.abilities(),-1).toString());
			else
			if(newName.length()>0)
			{
				int partNum=-1;
				for(int i=0;i<ables.size();i++)
					if(CMLib.english().containsString(((Ability)ables.elementAt(i)).ID(),newName))
					{ partNum=i; break;}
				boolean updateList=false;
				if(partNum<0)
				{
					Ability A=CMClass.getAbility(newName);
					if(A==null)
						mob.tell(getScr("BaseGenerics","abilityerr"));
					else
					{
						StringBuffer str=new StringBuffer(A.ID()+";");
						String prof=mob.session().prompt(getScr("BaseGenerics","defproflev"),"100");
						str.append((""+CMath.s_int(prof)));
						data.addElement(str.toString());
						ables.addElement(A);
						mob.tell(getScr("BaseGenerics","anameadd",A.name()));
						updateList=true;
					}
				}
				else
				{
					Ability A=(Ability)ables.elementAt(partNum);
					ables.removeElementAt(partNum);
					data.removeElementAt(partNum);
					updateList=true;
					mob.tell(getScr("BaseGenerics","anamerem",A.name()));
				}
				if(updateList)
				{
					if(data.size()>0)
						E.setStat("NUMCABLE",""+data.size());
					else
						E.setStat("NUMCABLE","");
					for(int i=0;i<data.size();i++)
					{
						Vector V=CMParms.parseSemicolons((String)data.elementAt(i),false);
						E.setStat("GETCABLE"+i,((String)V.elementAt(0)));
						E.setStat("GETCABLEPROF"+i,((String)V.elementAt(1)));
					}
				}
			}
			else
			{
				mob.tell(getScr("BaseGenerics","nochange"));
				return;
			}
		}
	}
	public static void modifyGenClass(MOB mob, CharClass me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		int showFlag=-1;
		if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!ok))
		{
			int showNumber=0;

            genInt(mob,me,++showNumber,showFlag,getScr("BaseGenerics","nunu")+" ","NUMNAME");
            int numNames=CMath.s_int(me.getStat("NUMNAME"));
            if(numNames<=1)
    			genText(mob,me,++showNumber,showFlag,"Name","NAME0");
            else
            for(int i=0;i<numNames;i++)
            {
                genText(mob,me,++showNumber,showFlag,getScr("BaseGenerics","nclvl",i+"")+" ","NAME"+i);
                if(i>0)
                while(!mob.session().killFlag())
                {
                    int oldNameLevel=CMath.s_int(me.getStat("NAMELEVEL"+i));
                    genInt(mob,me,++showNumber,showFlag,"Name #"+i+" class level: ","NAMELEVEL"+i);
                    int previousNameLevel=CMath.s_int(me.getStat("NAMELEVEL"+(i-1)));
                    int newNameLevel=CMath.s_int(me.getStat("NAMELEVEL"+i));
                    if((oldNameLevel!=newNameLevel)&&(newNameLevel<(previousNameLevel+1)))
                    {
                        mob.tell(getScr("BaseGenerics","levelless",(previousNameLevel+1)+""));
                        me.setStat("NAMELEVEL"+i,""+(previousNameLevel+1));
                        showNumber--;
                    }
                    else
                        break;
                }
            }
		    genText(mob,me,++showNumber,showFlag,getScr("BaseGenerics","nana"),"NAME");
			genText(mob,me,++showNumber,showFlag,getScr("BaseGenerics","baba"),"BASE");
            genClassAvailability(mob,me,++showNumber,showFlag);
			genInt(mob,me,++showNumber,showFlag,getScr("BaseGenerics","ipip"),"HPDIV");
			genInt(mob,me,++showNumber,showFlag,getScr("BaseGenerics","didi"),"HPDICE");
			genInt(mob,me,++showNumber,showFlag,getScr("BaseGenerics","hphp"),"HPDIE");
			genInt(mob,me,++showNumber,showFlag,getScr("BaseGenerics","divodivo"),"MANADIV");
			genInt(mob,me,++showNumber,showFlag,getScr("BaseGenerics","dicedice"),"MANADICE");
			genInt(mob,me,++showNumber,showFlag,getScr("BaseGenerics","diedie"),"MANADIE");
			genInt(mob,me,++showNumber,showFlag,getScr("BaseGenerics","pracprac"),"LVLPRAC");
			genInt(mob,me,++showNumber,showFlag,getScr("BaseGenerics","atleatle"),"LVLATT");
			genAttackAttribute(mob,me,++showNumber,showFlag,getScr("BaseGenerics","atat"),"ATTATT");
			genInt(mob,me,++showNumber,showFlag,getScr("BaseGenerics","prapra"),"FSTPRAC");
			genInt(mob,me,++showNumber,showFlag,getScr("BaseGenerics","traintrain"),"FSTTRAN");
			genInt(mob,me,++showNumber,showFlag,getScr("BaseGenerics","dmdm"),"LVLDAM");
			genInt(mob,me,++showNumber,showFlag,getScr("BaseGenerics","movmov"),"LVLMOVE");
			genArmorCode(mob,me,++showNumber,showFlag,getScr("BaseGenerics","armarm"),"ARMOR");
			genText(mob,me,++showNumber,showFlag,getScr("BaseGenerics","limlim"),"STRLMT");
			genText(mob,me,++showNumber,showFlag,getScr("BaseGenerics","bonbon"),"STRBON");
			genQualifications(mob,me,++showNumber,showFlag,getScr("BaseGenerics","qualqual"),"QUAL");
			genEStats(mob,me,++showNumber,showFlag);
			genAStats(mob,me,"ASTATS",getScr("BaseGenerics","charsadj"),++showNumber,showFlag);
			genAStats(mob,me,"CSTATS",getScr("BaseGenerics","chastatsett"),++showNumber,showFlag);
			genAState(mob,me,"ASTATE",getScr("BaseGenerics","chastateadj"),++showNumber,showFlag);
			genAState(mob,me,"STARTASTATE",getScr("BaseGenerics","newplaycs"),++showNumber,showFlag);
			genClassFlags(mob,me,++showNumber,showFlag);
			genWeaponRestr(mob,me,++showNumber,showFlag,getScr("BaseGenerics","weapres"),"NUMWEP","GETWEP");
			genOutfit(mob,me,++showNumber,showFlag);
			genClassAbilities(mob,me,++showNumber,showFlag);
            genInt(mob,me,++showNumber,showFlag,getScr("BaseGenerics","numsecs")+" ","NUMSSET");
            int numGroups=CMath.s_int(me.getStat("NUMSSET"));
            for(int i=0;i<numGroups;i++)
            {
                genText(mob,me,++showNumber,showFlag,getScr("BaseGenerics","seccodes")+i,"SSET"+i);
                while(!mob.session().killFlag())
                {
                    int oldGroupLevel=CMath.s_int(me.getStat("SSETLEVEL"+i));
                    genInt(mob,me,++showNumber,showFlag,getScr("BaseGenerics","cllevsec")+i+": ","SSETLEVEL"+i);
                    int previousGroupLevel=CMath.s_int(me.getStat("SSETLEVEL"+(i-1)));
                    int newGroupLevel=CMath.s_int(me.getStat("SSETLEVEL"+i));
                    if((oldGroupLevel!=newGroupLevel)
                    &&(i>0)
                    &&(newGroupLevel<(previousGroupLevel+1)))
                    {
                        mob.tell(getScr("BaseGenerics","levelless",(previousGroupLevel+1)+""));
                        me.setStat("SSETLEVEL"+i,""+(previousGroupLevel+1));
                        showNumber--;
                    }
                    else
                        break;
                }
            }

			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=CMath.s_int(mob.session().prompt(getScr("BaseGenerics","editwhich"),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
	}

    public static void modifyFaction(MOB mob, Faction me)
    throws IOException
    {
        if(mob.isMonster())
            return;
        boolean ok=false;
        int showFlag=-1;
        if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
            showFlag=-999;
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(!ok))
        {
            int showNumber=0;
            // name
            me.setName(CMLib.english().promptText(mob,me.name(),++showNumber,showFlag,getScr("BaseGenerics","namename")));

            // ranges
            ++showNumber;
            if(me.ranges().size()==0)
                me.ranges().addElement(me.newRange("0;100;Sample Range;SAMPLE;"));
            while((mob.session()!=null)&&(!mob.session().killFlag())&&(!((showFlag>0)&&(showFlag!=showNumber))))
            {
                StringBuffer list=new StringBuffer(getScr("BaseGenerics","faction",showNumber+""));
                list.append(CMStrings.padRight("   Name",21)+CMStrings.padRight("Min",11)+CMStrings.padRight("Max",11)+CMStrings.padRight("Code",16)+CMStrings.padRight("Align",6)+"\n\r");
                for(int r=0;r<me.ranges().size();r++)
                {
                    Faction.FactionRange FR=(Faction.FactionRange)me.ranges().elementAt(r);
                    list.append(CMStrings.padRight("   "+FR.name(),20)+" ");
                    list.append(CMStrings.padRight(""+FR.low(),10)+" ");
                    list.append(CMStrings.padRight(""+FR.high(),10)+" ");
                    list.append(CMStrings.padRight(FR.codeName(),15)+" ");
                    list.append(CMStrings.padRight(Faction.ALIGN_NAMES[FR.alignEquiv()],5)+"\n\r");
                }
                mob.tell(list.toString());
                if((showFlag!=showNumber)&&(showFlag>-999)) break;
                String which=mob.session().prompt(getScr("BaseGenerics","addremmodname"),"");
                if(which.length()==0)
                    break;
                Faction.FactionRange FR=null;
                for(int r=0;r<me.ranges().size();r++)
                {
                    if(((Faction.FactionRange)me.ranges().elementAt(r)).name().equalsIgnoreCase(which))
                        FR=(Faction.FactionRange)me.ranges().elementAt(r);
                }
                if(FR==null)
                {
                    if(mob.session().confirm(getScr("BaseGenerics","cnrange",which)+" ","N"))
                    {
                        FR=me.newRange("0;100;"+which+";CHANGEMYCODENAME;");
                        me.ranges().addElement(FR);
                    }
                }
                else
                if(mob.session().choose(getScr("BaseGenerics","moddelran")+" ","MD","M").toUpperCase().startsWith("D"))
                {
                    me.ranges().remove(FR);
                    mob.tell(getScr("BaseGenerics","rangedeleted"));
                    FR=null;
                }
                if(FR!=null)
                {
                    String newName=mob.session().prompt(getScr("BaseGenerics","entnamefr",FR.name(),FR.name()));
                    boolean error99=false;
                    if(newName.length()==0)
                        error99=true;
                    else
                    for(int r=0;r<me.ranges().size();r++)
                    {
                        Faction.FactionRange FR3=(Faction.FactionRange)me.ranges().elementAt(r);
                        if(FR3.name().equalsIgnoreCase(FR.name())&&(FR3!=FR))
                        { mob.tell(getScr("BaseGenerics","alreadyrange")); error99=true; break;}
                    }
                    if(error99)
                        mob.tell(getScr("BaseGenerics","nochange"));
                    else
                        FR.setName(newName);
                    newName=mob.session().prompt(getScr("BaseGenerics","lowendrange",FR.low()+""),""+FR.low());
                    if(!CMath.isInteger(newName))
                        mob.tell(getScr("BaseGenerics","nochange"));
                    else
                        FR.setLow(CMath.s_int(newName));
                    newName=mob.session().prompt(getScr("BaseGenerics","highendrange",FR.high()+""),""+FR.high());
                    if((!CMath.isInteger(newName))||(CMath.s_int(newName)<FR.low()))
                        mob.tell(getScr("BaseGenerics","nochange"));
                    else
                        FR.setHigh(CMath.s_int(newName));
                    newName=mob.session().prompt(getScr("BaseGenerics","codename",FR.codeName()),""+FR.codeName());
                    if(newName.trim().length()==0)
                        mob.tell(getScr("BaseGenerics","nochange"));
                    else
                    {
                    	Faction FC=CMLib.factions().getFactionByRangeCodeName(newName.toUpperCase().trim());
                    	if((FC!=null)&&(FC!=me))
                            mob.tell(getScr("BaseGenerics","facexists"));
                    	else
	                        FR.setCodeName(newName.toUpperCase().trim());
                    }
                    StringBuffer prompt=new StringBuffer(getScr("BaseGenerics","virtuename"));
                    StringBuffer choices=new StringBuffer("");
                    for(int r=0;r<Faction.ALIGN_NAMES.length;r++)
                    {
                        choices.append(""+r);
                        if(r==Faction.ALIGN_INDIFF)
                            prompt.append(getScr("BaseGenerics","notappmsg",r+""));
                        else
                            prompt.append(r+") "+Faction.ALIGN_NAMES[r].toLowerCase()+"\n\r");
                    }
                    FR.setAlignEquiv(CMath.s_int(mob.session().choose(prompt.toString()+getScr("BaseGenerics","enteralign")+" ",choices.toString(),""+FR.alignEquiv())));
                }
            }


            // show in score
            me.setShowinscore(CMLib.english().promptBool(mob,me.showinscore(),++showNumber,showFlag,getScr("BaseGenerics","shosco")));

            // show in factions
            me.setShowinfactionscommand(CMLib.english().promptBool(mob,me.showinfactionscommand(),++showNumber,showFlag,getScr("BaseGenerics","shofcmd")));

            // show in special reports
            boolean alreadyReporter=false;
            for(Enumeration e=CMLib.factions().factionSet().elements();e.hasMoreElements();)
            {
                Faction F2=(Faction)e.nextElement();
                if(F2.showinspecialreported()) alreadyReporter=true;
            }
            if(!alreadyReporter)
                me.setShowinspecialreported(CMLib.english().promptBool(mob,me.showinspecialreported(),++showNumber,showFlag,getScr("BaseGenerics","shorep")));

            // show in editor
            me.setShowineditor(CMLib.english().promptBool(mob,me.showineditor(),++showNumber,showFlag,getScr("BaseGenerics","shomed")));

            // auto defaults
            boolean error=true;
            me.setAutoDefaults(CMParms.parseSemicolons(CMLib.english().promptText(mob,CMParms.toSemicolonList(me.autoDefaults()),++showNumber,showFlag,getScr("BaseGenerics","zappermasksmsg")),true));

            // non-auto defaults
            error=true;
            if(me.defaults().size()==0)
                me.defaults().addElement("0");
            ++showNumber;
            while(error&&(mob.session()!=null)&&(!mob.session().killFlag()))
            {
                error=false;
                String newDefaults=CMLib.english().promptText(mob,CMParms.toSemicolonList(me.defaults()),showNumber,showFlag,getScr("BaseGenerics","zappermasksmsg2"));
                if((showFlag!=showNumber)&&(showFlag>-999)) break;
                Vector V=CMParms.parseSemicolons(newDefaults,true);
                if(V.size()==0)
                {
                    mob.tell(getScr("BaseGenerics","fielderr"));
                    error=true;
                }
                me.setDefaults(CMParms.parseSemicolons(newDefaults,true));
            }

            // choices and choice intro
            me.setChoices(CMParms.parseSemicolons(CMLib.english().promptText(mob,CMParms.toSemicolonList(me.choices()),++showNumber,showFlag,getScr("BaseGenerics","newplayervchoices")),true));
            if(me.choices().size()>0)
                me.setChoiceIntro(CMLib.english().promptText(mob,me.choiceIntro(),++showNumber,showFlag,getScr("BaseGenerics","introtxt")));

            // rate modifier
            String newModifier=CMLib.english().promptText(mob,Math.round(me.rateModifier()*100.0)+"%",++showNumber,showFlag,getScr("BaseGenerics","ratemod"));
            if(newModifier.endsWith("%"))
                newModifier=newModifier.substring(0,newModifier.length()-1);
            if(CMath.isNumber(newModifier))
                me.setRateModifier(CMath.s_double(newModifier)/100.0);

            // experience flag
            boolean error2=true;
            ++showNumber;
            while(error2&&(mob.session()!=null)&&(!mob.session().killFlag())&&(!((showFlag>0)&&(showFlag!=showNumber))))
            {
                error2=false;
                StringBuffer nextPrompt=new StringBuffer("\n\r");
                int myval=-1;
                for(int i=0;i<Faction.EXPAFFECT_NAMES.length;i++)
                {
                    if(me.experienceFlag().equalsIgnoreCase(Faction.EXPAFFECT_NAMES[i]))
                        myval=i;
                    nextPrompt.append("  "+(i+1)+") "+CMStrings.capitalizeAndLower(Faction.EXPAFFECT_NAMES[i].toLowerCase())+"\n\r");
                }
                if(myval<0){ me.setExperienceFlag("NONE"); myval=0;}
                if((showFlag!=showNumber)&&(showFlag>-999))
                {
                    mob.tell(getScr("BaseGenerics","affectexp",showNumber+"",Faction.EXPAFFECT_NAMES[myval]));
                    break;
                }
                String prompt=getScr("BaseGenerics","afexp")+" "+Faction.EXPAFFECT_NAMES[myval]+nextPrompt.toString()+getScr("BaseGenerics","selvaluen")+" ";
                int mynewval=CMLib.english().promptInteger(mob,myval+1,showNumber,showFlag,prompt);
                if((showFlag!=showNumber)&&(showFlag>-999)) break;
                if((mynewval<=0)||(mynewval>Faction.EXPAFFECT_NAMES.length))
                {
                    mob.tell(getScr("BaseGenerics","valuerr"));
                    error2=true;
                }
                else
                    me.setExperienceFlag(Faction.EXPAFFECT_NAMES[mynewval-1]);
            }

            // factors by mask
            ++showNumber;
            while((mob.session()!=null)&&(!mob.session().killFlag())&&(!((showFlag>0)&&(showFlag!=showNumber))))
            {
                StringBuffer list=new StringBuffer(getScr("BaseGenerics","factionmod",showNumber+""));
                list.append("    #) "+CMStrings.padRight("Zapper Mask",31)+CMStrings.padRight("Gain",6)+CMStrings.padRight("Loss",6)+"\n\r");
                StringBuffer choices=new StringBuffer("");
                for(int r=0;r<me.factors().size();r++)
                {
                    Vector factor=(Vector)me.factors().elementAt(r);
                    if(factor.size()!=3)
                        me.factors().removeElement(factor);
                    else
                    {
                        choices.append(((char)('A'+r)));
                        list.append("    "+(((char)('A'+r))+") "));
                        list.append(CMStrings.padRight((String)factor.elementAt(2),30)+" ");
                        list.append(CMStrings.padRight(""+Math.round(CMath.s_double((String)factor.elementAt(0))*100.0)+"%",5)+" ");
                        list.append(CMStrings.padRight(""+Math.round(CMath.s_double((String)factor.elementAt(1))*100.0)+"%",5)+"\n\r");
                    }
                }
                mob.tell(list.toString());
                if((showFlag!=showNumber)&&(showFlag>-999)) break;
                String which=mob.session().choose(getScr("BaseGenerics","whichstring"),"0"+choices.toString(),"").trim().toUpperCase();
                int factorNum=choices.toString().indexOf(which);
                if((which.length()!=1)
                ||((!which.equalsIgnoreCase("0"))
                    &&((factorNum<0)||(factorNum>=me.factors().size()))))
                    break;
                Vector factor=null;
                if(!which.equalsIgnoreCase("0"))
                {
                    factor=(Vector)me.factors().elementAt(factorNum);
                    if(factor!=null)
                        if(mob.session().choose(getScr("BaseGenerics","moddelran")+" ","MD","M").toUpperCase().startsWith("D"))
                        {
                            me.factors().remove(factor);
                            mob.tell(getScr("BaseGenerics","factordel"));
                            factor=null;
                        }
                }
                else
                {
                    factor=new Vector();
                    factor.addElement("1.0");
                    factor.addElement("1.0");
                    factor.addElement("");
                    me.factors().addElement(factor);
                }
                if(factor!=null)
                {
                    String mask=mob.session().prompt(getScr("BaseGenerics","entzapper",((String)factor.elementAt(2)),((String)factor.elementAt(2))));
                    double newHigh=CMath.s_double((String)factor.elementAt(0));
                    String newName=mob.session().prompt(getScr("BaseGenerics","gainadj",Math.round(newHigh*100)+"",Math.round(newHigh*100)+"".trim()));
                    if(newName.endsWith("%"))
                        newName=newName.substring(0,newName.length()-1);
                    if(!CMath.isNumber(newName))
                        mob.tell(getScr("BaseGenerics","nochange"));
                    else
                        newHigh=CMath.s_double(newName)/100.0;

                    double newLow=CMath.s_double((String)factor.elementAt(1));
                    newName=mob.session().prompt(getScr("BaseGenerics","lossadj",Math.round(newLow*100)+"",Math.round(newLow*100)+"".trim()));
                    if(newName.endsWith("%"))
                        newName=newName.substring(0,newName.length()-1);
                    if(!CMath.isNumber(newName))
                        mob.tell(getScr("BaseGenerics","nochange"));
                    else
                        newLow=CMath.s_double(newName)/100.0;
                    me.factors().removeElement(factor);
                    factor=new Vector();
                    factor.addElement(""+newHigh);
                    factor.addElement(""+newLow);
                    factor.addElement(""+mask);
                    me.factors().addElement(factor);
                }
            }

            // relations between factions
            ++showNumber;
            while((mob.session()!=null)&&(!mob.session().killFlag())&&(!((showFlag>0)&&(showFlag!=showNumber))))
            {
                StringBuffer list=new StringBuffer(getScr("BaseGenerics","crossrelati",showNumber+""));
                list.append(getScr("BaseGenerics","percchange","    ",CMStrings.padRight("",25)));
                for(Enumeration e=me.relations().keys();e.hasMoreElements();)
                {
                    String key=(String)e.nextElement();
                    Double value=(Double)me.relations().get(key);
                    Faction F=CMLib.factions().getFaction(key);
                    if(F!=null)
                    {
                        list.append("    "+CMStrings.padRight(F.name(),31)+" ");
                        long lval=Math.round(value.doubleValue()*100.0);
                        list.append(lval+"%");
                        list.append("\n\r");
                    }
                }
                mob.tell(list.toString());
                if((showFlag!=showNumber)&&(showFlag>-999)) break;
                String which=mob.session().prompt(getScr("BaseGenerics","factionarm"),"");
                if(which.length()==0)
                    break;
                Faction theF=null;
                for(Enumeration e=me.relations().keys();e.hasMoreElements();)
                {
                    String key=(String)e.nextElement();
                    Faction F=CMLib.factions().getFaction(key);
                    if((F!=null)&&(F.name().equalsIgnoreCase(which)))
                        theF=F;
                }
                if(theF==null)
                {
                    Faction possibleF=CMLib.factions().getFaction(which);
                    if(possibleF==null) possibleF=CMLib.factions().getFactionByName(which);
                    if(possibleF==null)
                        mob.tell(getScr("BaseGenerics","errfaction",which));
                    else
                    if(mob.session().confirm(getScr("BaseGenerics","cnf",possibleF.name()),"N"))
                    {
                        theF=possibleF;
                        me.relations().put(theF.factionID(),new Double(1.0));
                    }
                }
                else
                if(mob.session().choose(getScr("BaseGenerics","moddelrel")+" ","MD","M").toUpperCase().startsWith("D"))
                {
                    me.relations().remove(theF.factionID());
                    mob.tell(getScr("BaseGenerics","reldel"));
                    theF=null;
                }
                if(theF!=null)
                {
                    long amount=Math.round(((Double)me.relations().get(theF.factionID())).doubleValue()*100.0);
                    String newName=mob.session().prompt(getScr("BaseGenerics","relamount",amount+""),""+amount+"%");
                    if(newName.endsWith("%")) newName=newName.substring(0,newName.length()-1);
                    if(!CMath.isInteger(newName))
                        mob.tell(getScr("BaseGenerics","nochange"));
                    else
                        amount=CMath.s_long(newName);
                    me.relations().remove(theF.factionID());
                    me.relations().put(theF.factionID(),new Double(amount/100.0));
                }
            }

            // faction change triggers
            ++showNumber;
            while((mob.session()!=null)&&(!mob.session().killFlag())&&(!((showFlag>0)&&(showFlag!=showNumber))))
            {
                StringBuffer list=new StringBuffer(getScr("BaseGenerics","factrigg",showNumber+""));
                list.append("    "+CMStrings.padRight("Type",15)
                        +" "+CMStrings.padRight("Direction",10)
                        +" "+CMStrings.padRight("Factor",10)
                        +" "+CMStrings.padRight("Flags",20)
                        +" Mask\n\r");
                for(Enumeration e=me.Changes().elements();e.hasMoreElements();)
                {
                    Faction.FactionChangeEvent CE=(Faction.FactionChangeEvent)e.nextElement();
                    if(CE!=null)
                    {
                        list.append("    ");
                        list.append(CMStrings.padRight(CE.eventID(),15)+" ");
                        list.append(CMStrings.padRight(Faction.FactionChangeEvent.FACTION_DIRECTIONS[CE.direction()],10)+" ");
                        list.append(CMStrings.padRight(Math.round(CE.factor()*100.0)+"%",10)+" ");
                        list.append(CMStrings.padRight(CE.flagCache(),20)+" ");
                        list.append(CE.zapper()+"\n\r");
                    }
                }
                mob.tell(list.toString());
                if((showFlag!=showNumber)&&(showFlag>-999)) break;
                String which=mob.session().prompt(getScr("BaseGenerics","triggerid"),"");
                which=which.toUpperCase().trim();
                if(which.length()==0) break;
                if(which.equalsIgnoreCase("?"))
                {
                    mob.tell(getScr("BaseGenerics","validtrigg",me.ALL_CHANGE_EVENT_TYPES()));
                    continue;
                }
                Faction.FactionChangeEvent CE=(Faction.FactionChangeEvent)me.Changes().get(which);
                if(CE==null)
                {
                    CE=me.newChangeEvent();
                    if(!CE.setFilterID(which))
                    {
                        mob.tell(getScr("BaseGenerics","iderr"));
                        continue;
                    }
                    else
                    if(!mob.session().confirm(getScr("BaseGenerics","cnt",which)+" ","N"))
                    {
                        CE=null;
                        break;
                    }
                    else
                        me.Changes().put(CE.eventID().toUpperCase(),CE);
                }
                else
                if(mob.session().choose(getScr("BaseGenerics","moddeltrig")+" ","MD","M").toUpperCase().startsWith("D"))
                {
                    me.Changes().remove(CE.eventID());
                    mob.tell(getScr("BaseGenerics","trigrem"));
                    CE=null;
                }

                if(CE!=null)
                {
                    StringBuffer directions=new StringBuffer(getScr("BaseGenerics","validdir"));
                    StringBuffer cmds=new StringBuffer("");
                    for(int i=0;i<Faction.FactionChangeEvent.FACTION_DIRECTIONS.length;i++)
                    {
                        directions.append(((char)('A'+i))+") "+Faction.FactionChangeEvent.FACTION_DIRECTIONS[i]+"\n\r");
                        cmds.append((char)('A'+i));
                    }
                    String str=mob.session().choose(directions+getScr("BaseGenerics","selnewdir")+Faction.FactionChangeEvent.FACTION_DIRECTIONS[CE.direction()]+"): ",cmds.toString()+"\n\r","");
                    if((str.length()==0)||str.equals("\n")||str.equals("\r")||(cmds.toString().indexOf(str.charAt(0))<0))
                        mob.tell(getScr("BaseGenerics","nochange"));
                    else
                        CE.setDirection((cmds.toString().indexOf(str.charAt(0))));
                }
                if(CE!=null)
                {
                    if(CE.factor()==0.0) CE.setFactor(1.0);
                    int amount=(int)Math.round(CE.factor()*100.0);
                    String newName=mob.session().prompt(getScr("BaseGenerics","amountfactor",amount+""),""+amount+"%");
                    if(newName.endsWith("%")) newName=newName.substring(0,newName.length()-1);
                    if(!CMath.isInteger(newName))
                        mob.tell(getScr("BaseGenerics","nochange"));
                    else
                        CE.setFactor(new Double(CMath.s_int(newName)/100.0).doubleValue());
                }
                if(CE!=null)
                {
                    mob.tell(getScr("BaseGenerics","validflags",CMParms.toStringList(Faction.FactionChangeEvent.VALID_FLAGS)));
                    String newFlags=mob.session().prompt(getScr("BaseGenerics","newflags",CE.flagCache(),CE.flagCache()));
                    if((newFlags.length()==0)||(newFlags.equals(CE.flagCache())))
                        mob.tell(getScr("BaseGenerics","nochange"));
                    else
                        CE.setFlags(newFlags);
                }
                if(CE!=null)
                {
                    String newFlags=mob.session().prompt(getScr("BaseGenerics","zappermsg",CE.zapper(),CE.zapper()));
                    if((newFlags.length()==0)||(newFlags.equals(CE.zapper())))
                        mob.tell(getScr("BaseGenerics","nochange"));
                    else
                        CE.setZapper(newFlags);
                }
            }

            // Ability allowances
            ++showNumber;
            while((mob.session()!=null)&&(!mob.session().killFlag())&&(!((showFlag>0)&&(showFlag!=showNumber))))
            {
                if((showFlag>0)&&(showFlag!=showNumber)) break;
                StringBuffer list=new StringBuffer(getScr("BaseGenerics","abiallow",showNumber+""));
                list.append("    #) "
                        +CMStrings.padRight("Ability masks",40)
                        +" "+CMStrings.padRight("Low value",10)
                        +" "+CMStrings.padRight("High value",10)
                        +"\n\r");
                int num=0;
                StringBuffer choices=new StringBuffer("0\n\r");
                for(Enumeration e=me.abilityUsages().elements();e.hasMoreElements();)
                {
                    Faction.FactionAbilityUsage CA=(Faction.FactionAbilityUsage)e.nextElement();
                    if(CA!=null)
                    {
                        list.append("    "+((char)('A'+num)+") "));
                        list.append(CMStrings.padRight(CA.usageID(),40)+" ");
                        list.append(CMStrings.padRight(CA.low()+"",10)+" ");
                        list.append(CMStrings.padRight(CA.high()+"",10)+" ");
                        list.append("\n\r");
                        choices.append((char)('A'+num));
                        num++;
                    }
                }
                mob.tell(list.toString());
                if((showFlag!=showNumber)&&(showFlag>-999)) break;
                String which=mob.session().choose(getScr("BaseGenerics","allmsg"),choices.toString(),"");
                if(which.length()!=1)
                    break;
                which=which.toUpperCase().trim();
                Faction.FactionAbilityUsage CA=null;
                if(!which.equalsIgnoreCase("0"))
                {
                    num=(which.charAt(0)-'A');
                    if((num<0)||(num>=me.abilityUsages().size()))
                        break;
                    CA=(Faction.FactionAbilityUsage)me.abilityUsages().elementAt(num);
                    if(CA==null)
                    {
                        mob.tell(getScr("BaseGenerics","allowancerr"));
                        continue;
                    }
                    if(mob.session().choose(getScr("BaseGenerics","moddelall")+" ","MD","M").toUpperCase().startsWith("D"))
                    {
                        me.abilityUsages().remove(CA);
                        mob.tell(getScr("BaseGenerics","alldel"));
                        CA=null;
                    }
                }
                else
                if(!mob.session().confirm(getScr("BaseGenerics","cna")+" ","N"))
                {
                    CA=null;
                    continue;
                }
                else
                {
                    CA=me.newAbilityUsage();
                    me.abilityUsages().addElement(CA);
                }
                if(CA!=null)
                {
                    boolean cont=false;
                    while((!cont)&&(!mob.session().killFlag()))
                    {
                        String newFlags=mob.session().prompt(getScr("BaseGenerics","abdmasks",CA.usageID(),CA.usageID()));
                        if(newFlags.equalsIgnoreCase("?"))
                        {
                            StringBuffer vals=new StringBuffer(getScr("BaseGenerics","validmasks"));
                            for(int i=0;i<Ability.ACODE_DESCS.length;i++)
                                vals.append(Ability.ACODE_DESCS[i]+", ");
                            for(int i=0;i<Ability.DOMAIN_DESCS.length;i++)
                                vals.append(Ability.DOMAIN_DESCS[i]+", ");
                            for(int i=0;i< Ability.FLAG_DESCS.length;i++)
                                vals.append(Ability.FLAG_DESCS[i]+", ");
                            vals.append(getScr("BaseGenerics","abilityidmsg"));
                            mob.tell(vals.toString());
                            cont=false;
                        }
                        else
                        {
                            cont=true;
                            if((newFlags.length()==0)||(newFlags.equals(CA.usageID())))
                                mob.tell(getScr("BaseGenerics","nochange"));
                            else
                            {
                                Vector unknowns=CA.setAbilityFlag(newFlags);
                                if(unknowns.size()>0)
                                    for(int i=unknowns.size()-1;i>=0;i--)
                                        if(CMClass.getAbility((String)unknowns.elementAt(i))!=null)
                                            unknowns.removeElementAt(i);
                                if(unknowns.size()>0)
                                {
                                    mob.tell(getScr("BaseGenerics","unknownmasks",CMParms.toStringList(unknowns)));
                                    cont=false;
                                }
                            }
                        }
                    }
                    String newName=mob.session().prompt(getScr("BaseGenerics","minvalueab",CA.low()+""),""+CA.low());
                    if((!CMath.isInteger(newName))||(CA.low()==CMath.s_int(newName)))
                        mob.tell(getScr("BaseGenerics","nochange"));
                    else
                        CA.setLow(CMath.s_int(newName));
                    newName=mob.session().prompt(getScr("BaseGenerics","maxvalueab",CA.high()+""),""+CA.high());
                    if((!CMath.isInteger(newName))||(CA.high()==CMath.s_int(newName)))
                        mob.tell(getScr("BaseGenerics","nochange"));
                    else
                        CA.setHigh(CMath.s_int(newName));
                    if(CA.high()<CA.low()) CA.setHigh(CA.low());
                }
            }

            // calculate new max/min
            me.setMinimum(Integer.MAX_VALUE);
            me.setMaximum(Integer.MIN_VALUE);
            for(int r=0;r<me.ranges().size();r++)
            {
                Faction.FactionRange FR=(Faction.FactionRange)me.ranges().elementAt(r);
                if(FR.high()>me.maximum()) me.setMaximum(FR.high());
                if(FR.low()<me.minimum()) me.setMinimum(FR.low());
            }
            if(me.minimum()==Integer.MAX_VALUE) me.setMinimum(Integer.MIN_VALUE);
            if(me.maximum()==Integer.MIN_VALUE) me.setMaximum(Integer.MAX_VALUE);
            if(me.maximum()<me.minimum())
            {
                int oldMin=me.minimum();
                me.setMinimum(me.maximum());
                me.setMaximum(oldMin);
            }
            me.setMiddle(me.minimum()+(int)Math.round(CMath.div(me.maximum()-me.minimum(),2.0)));
            me.setDifference(CMath.abs(me.maximum()-me.minimum()));



            if(showFlag<-900){ ok=true; break;}
            if(showFlag>0){ showFlag=-1; continue;}
            showFlag=CMath.s_int(mob.session().prompt(getScr("BaseGenerics","editwhich"),""));
            if(showFlag<=0)
            {
                showFlag=-1;
                ok=true;
            }
        }
        if((me.factionID().length()>0)&&(CMLib.factions().getFaction(me.factionID())!=null))
        {
            Vector oldV=Resources.getFileLineVector(Resources.getFileResource(me.factionID(),true));
            if(oldV.size()<10)
            {

            }
            boolean[] defined=new boolean[Faction.ALL_TAGS.length];
            for(int i=0;i<defined.length;i++) defined[i]=false;
            for(int v=0;v<oldV.size();v++)
            {
                String s=(String)oldV.elementAt(v);
                if(!(s.trim().startsWith("#")||s.trim().length()==0||(s.indexOf("=")<0)))
                {
                    String tag=s.substring(0,s.indexOf("=")).trim().toUpperCase();
                    int tagRef=CMLib.factions().isFactionTag(tag);
                    if(tagRef>=0) defined[tagRef]=true;
                }
            }
            boolean[] done=new boolean[Faction.ALL_TAGS.length];
            for(int i=0;i<done.length;i++) done[i]=false;
            int lastCommented=-1;
            String CR="\n\r";
            StringBuffer buf=new StringBuffer("");
            for(int v=0;v<oldV.size();v++)
            {
                String s=(String)oldV.elementAt(v);
                if(s.trim().length()==0)
                {
                    if((lastCommented>=0)&&(!done[lastCommented]))
                    {
                        done[lastCommented]=true;
                        buf.append(me.getINIDef(Faction.ALL_TAGS[lastCommented],CR)+CR);
                        lastCommented=-1;
                    }
                }
                else
                if(s.trim().startsWith("#")||(s.indexOf("=")<0))
                {
                    buf.append(s+CR);
                    int x=s.indexOf("=");
                    if(x>=0)
                    {
                        s=s.substring(0,x).trim();
                        int first=s.length()-1;
                        for(;first>=0;first--)
                            if(!Character.isLetterOrDigit(s.charAt(first)))
                                break;
                        first=CMLib.factions().isFactionTag(s.substring(first).trim().toUpperCase());
                        if(first>=0) lastCommented=first;
                    }
                }
                else
                {
                    String tag=s.substring(0,s.indexOf("=")).trim().toUpperCase();
                    int tagRef=CMLib.factions().isFactionTag(tag);
                    if(tagRef<0)
                        buf.append(s+CR);
                    else
                    if(!done[tagRef])
                    {
                        done[tagRef]=true;
                        buf.append(me.getINIDef(tag,CR)+CR);
                    }
                }
            }
            if((lastCommented>=0)&&(!done[lastCommented]))
                buf.append(me.getINIDef(Faction.ALL_TAGS[lastCommented],CR)+CR);
            Resources.removeResource(me.factionID());
            Resources.submitResource(me.factionID(),buf);
            if(!Resources.saveFileResource(me.factionID()))
                mob.tell(getScr("BaseGenerics","factionfilereadonly",me.factionID()));
        }
    }

	public static void modifyGenRace(MOB mob, Race me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		int showFlag=-1;
		if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!ok))
		{
			int showNumber=0;
			genText(mob,me,++showNumber,showFlag,getScr("BaseGenerics","edname"),"NAME");
			genCat(mob,me,++showNumber,showFlag);
			genInt(mob,me,++showNumber,showFlag,getScr("BaseGenerics","edwei"),"BWEIGHT");
			genInt(mob,me,++showNumber,showFlag,getScr("BaseGenerics","edvaria"),"VWEIGHT");
			genInt(mob,me,++showNumber,showFlag,getScr("BaseGenerics","edmhe"),"MHEIGHT");
			genInt(mob,me,++showNumber,showFlag,getScr("BaseGenerics","edfhe"),"FHEIGHT");
			genInt(mob,me,++showNumber,showFlag,getScr("BaseGenerics","edhevar"),"VHEIGHT");
			genRaceAvailability(mob,me,++showNumber,showFlag);
			genText(mob,me,++showNumber,showFlag,getScr("BaseGenerics","edleavtxt"),"LEAVE");
			genText(mob,me,++showNumber,showFlag,getScr("BaseGenerics","edarrtxt"),"ARRIVE");
			genHealthBuddy(mob,me,++showNumber,showFlag);
			genBodyParts(mob,me,++showNumber,showFlag);
			genAgingChart(mob,me,++showNumber,showFlag);
            genBool(mob,me,++showNumber,showFlag,getScr("BaseGenerics","edncc"),"BODYKILL");
			genEStats(mob,me,++showNumber,showFlag);
			genAStats(mob,me,"ASTATS",getScr("BaseGenerics","edcadj"),++showNumber,showFlag);
			genAStats(mob,me,"CSTATS",getScr("BaseGenerics","edcset"),++showNumber,showFlag);
			genAState(mob,me,"ASTATE",getScr("BaseGenerics","edcsta"),++showNumber,showFlag);
			genAState(mob,me,"STARTASTATE",getScr("BaseGenerics","ednpca"),++showNumber,showFlag);
			genRaceFlags(mob,me,++showNumber,showFlag);
			genResources(mob,me,++showNumber,showFlag);
			genOutfit(mob,me,++showNumber,showFlag);
			genWeapon(mob,me,++showNumber,showFlag);
			genRacialAbilities(mob,me,++showNumber,showFlag);
			genCulturalAbilities(mob,me,++showNumber,showFlag);
			//genRacialEffects(mob,me,++showNumber,showFlag);
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=CMath.s_int(mob.session().prompt(getScr("BaseGenerics","editwhich"),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
	}

	public static void modifyGenItem(MOB mob, Item me)
		throws IOException
	{
		boolean ok=false;
		int showFlag=-1;
		if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!ok))
		{
			if(mob.isMonster())	return;
			int showNumber=0;
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			genLevel(mob,me,++showNumber,showFlag);
			genSecretIdentity(mob,me,++showNumber,showFlag);
			genMaterialCode(mob,me,++showNumber,showFlag);
			if(me instanceof ClanItem)
				genClanItem(mob,(ClanItem)me,++showNumber,showFlag);
			if(me instanceof ShipComponent)
			{
			    if(me instanceof ShipComponent.ShipPanel)
				    genPanelType(mob,(ShipComponent.ShipPanel)me,++showNumber,showFlag);
			}
            if(me instanceof PackagedItems)
                ((PackagedItems)me).setNumberOfItemsInPackage(CMLib.english().promptInteger(mob,((PackagedItems)me).numberOfItemsInPackage(),++showNumber,showFlag,getScr("BaseGenerics","numpackaged")));
			genGettable(mob,me,++showNumber,showFlag);
			genReadable1(mob,me,++showNumber,showFlag);
			genReadable2(mob,me,++showNumber,showFlag);
			if(me instanceof Recipe) genRecipe(mob,(Recipe)me,++showNumber,showFlag);
			if(me instanceof Light)	genBurnout(mob,(Light)me,++showNumber,showFlag);
			genRejuv(mob,me,++showNumber,showFlag);
			if(me instanceof Coins)
			    genCoinStuff(mob,(Coins)me,++showNumber,showFlag);
			else
				genAbility(mob,me,++showNumber,showFlag);
			genUses(mob,me,++showNumber,showFlag);
			if(me instanceof Wand)
				genMaxUses(mob,(Wand)me,++showNumber,showFlag);
			genValue(mob,me,++showNumber,showFlag);
			genWeight(mob,me,++showNumber,showFlag);
			genDisposition(mob,me.baseEnvStats(),++showNumber,showFlag);
			genBehaviors(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			if(me instanceof LandTitle)
				genTitleRoom(mob,(LandTitle)me,++showNumber,showFlag);
			genImage(mob,me,++showNumber,showFlag);
            for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
                me.setStat(me.getStatCodes()[x],CMLib.english().promptText(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=CMath.s_int(mob.session().prompt(getScr("BaseGenerics","editwhich"),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				me.recoverEnvStats();
				if(me.text().length()>=maxLength)
				{
					mob.tell(getScr("BaseGenerics","stringlimiterr",maxLength+""));
					ok=false;
				}
			}
		}
	}

	public static void modifyGenFood(MOB mob, Food me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		int showFlag=-1;
		if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!ok))
		{
			int showNumber=0;
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			genSecretIdentity(mob,me,++showNumber,showFlag);
			genLevel(mob,me,++showNumber,showFlag);
			genValue(mob,me,++showNumber,showFlag);
			genRejuv(mob,me,++showNumber,showFlag);
			genWeight(mob,me,++showNumber,showFlag);
			genMaterialCode(mob,me,++showNumber,showFlag);
			genNourishment(mob,me,++showNumber,showFlag);
			genDisposition(mob,me.baseEnvStats(),++showNumber,showFlag);
			genGettable(mob,me,++showNumber,showFlag);
			genReadable1(mob,me,++showNumber,showFlag);
			genReadable2(mob,me,++showNumber,showFlag);
			if(me instanceof Light)	genBurnout(mob,(Light)me,++showNumber,showFlag);
			genBehaviors(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			genImage(mob,me,++showNumber,showFlag);
            for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
                me.setStat(me.getStatCodes()[x],CMLib.english().promptText(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=CMath.s_int(mob.session().prompt(getScr("BaseGenerics","editwhich"),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				me.recoverEnvStats();
				if(me.text().length()>=maxLength)
				{
					mob.tell(getScr("BaseGenerics","stringlimiterr",maxLength+""));
					ok=false;
				}
			}
		}
	}
	public static void modifyGenDrink(MOB mob, Drink me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		int showFlag=-1;
		if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!ok))
		{
			int showNumber=0;
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			genSecretIdentity(mob,(Item)me,++showNumber,showFlag);
			genValue(mob,(Item)me,++showNumber,showFlag);
			genLevel(mob,me,++showNumber,showFlag);
			genWeight(mob,me,++showNumber,showFlag);
			genRejuv(mob,me,++showNumber,showFlag);
			genThirstQuenched(mob,me,++showNumber,showFlag);
			genMaterialCode(mob,(Item)me,++showNumber,showFlag);
			genDrinkHeld(mob,me,++showNumber,showFlag);
			genGettable(mob,(Item)me,++showNumber,showFlag);
			genReadable1(mob,(Item)me,++showNumber,showFlag);
			genReadable2(mob,(Item)me,++showNumber,showFlag);
			if(me instanceof Light)	genBurnout(mob,(Light)me,++showNumber,showFlag);
			genBehaviors(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			genDisposition(mob,me.baseEnvStats(),++showNumber,showFlag);
			if(me instanceof Container)
				genCapacity(mob,(Container)me,++showNumber,showFlag);
			if(me instanceof Perfume)
				((Perfume)me).setSmellList(CMLib.english().promptText(mob,((Perfume)me).getSmellList(),++showNumber,showFlag,getScr("BaseGenerics","smelllist")));
			genImage(mob,me,++showNumber,showFlag);
            for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
                me.setStat(me.getStatCodes()[x],CMLib.english().promptText(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=CMath.s_int(mob.session().prompt(getScr("BaseGenerics","editwhich"),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				me.recoverEnvStats();
				if(me.text().length()>=maxLength)
				{
					mob.tell(getScr("BaseGenerics","stringlimiterr",maxLength+""));
					ok=false;
				}
			}
		}
	}

	public static void modifyGenWallpaper(MOB mob, Item me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		int showFlag=-1;
		if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!ok))
		{
			int showNumber=0;
			genName(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			genReadable1(mob,me,++showNumber,showFlag);
			genReadable2(mob,me,++showNumber,showFlag);
			if(me instanceof Light)	genBurnout(mob,(Light)me,++showNumber,showFlag);
            for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
                me.setStat(me.getStatCodes()[x],CMLib.english().promptText(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=CMath.s_int(mob.session().prompt(getScr("BaseGenerics","editwhich"),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				me.recoverEnvStats();
				if(me.text().length()>=maxLength)
				{
					mob.tell(getScr("BaseGenerics","stringlimiterr",maxLength+""));
					ok=false;
				}
			}
		}
	}

	public static void modifyGenMap(MOB mob, com.planet_ink.coffee_mud.Items.interfaces.Map me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		int showFlag=-1;
		if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!ok))
		{
			int showNumber=0;
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			genLevel(mob,me,++showNumber,showFlag);
			genSecretIdentity(mob,me,++showNumber,showFlag);
			genGettable(mob,me,++showNumber,showFlag);
			genReadable1(mob,me,++showNumber,showFlag);
			genReadable2(mob,me,++showNumber,showFlag);
			genValue(mob,me,++showNumber,showFlag);
			genWeight(mob,me,++showNumber,showFlag);
			genRejuv(mob,me,++showNumber,showFlag);
			genMaterialCode(mob,me,++showNumber,showFlag);
			genDisposition(mob,me.baseEnvStats(),++showNumber,showFlag);
			genBehaviors(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			genImage(mob,me,++showNumber,showFlag);
            for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
                me.setStat(me.getStatCodes()[x],CMLib.english().promptText(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=CMath.s_int(mob.session().prompt(getScr("BaseGenerics","editwhich"),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				me.recoverEnvStats();
				if(me.text().length()>=maxLength)
				{
					mob.tell(getScr("BaseGenerics","stringlimiterr",maxLength+""));
					ok=false;
				}
			}
		}
	}

	public static void modifyGenContainer(MOB mob, Container me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		int showFlag=-1;
		if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!ok))
		{
			int showNumber=0;
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			genLevel(mob,me,++showNumber,showFlag);
			genRejuv(mob,me,++showNumber,showFlag);
			genCapacity(mob,me,++showNumber,showFlag);
			if(me instanceof ShipComponent)
			{
			    if(me instanceof ShipComponent.ShipPanel)
				    genPanelType(mob,(ShipComponent.ShipPanel)me,++showNumber,showFlag);
			}
			genLidsNLocks(mob,me,++showNumber,showFlag);
			genMaterialCode(mob,me,++showNumber,showFlag);
			genSecretIdentity(mob,me,++showNumber,showFlag);
			genValue(mob,me,++showNumber,showFlag);
			genUses(mob,me,++showNumber,showFlag);
			genWeight(mob,me,++showNumber,showFlag);
			genDisposition(mob,me.baseEnvStats(),++showNumber,showFlag);
			if(me instanceof DeadBody)
				genCorpseData(mob,(DeadBody)me,++showNumber,showFlag);
			if(me instanceof ClanItem)
				genClanItem(mob,(ClanItem)me,++showNumber,showFlag);
			genGettable(mob,me,++showNumber,showFlag);
			genReadable1(mob,me,++showNumber,showFlag);
			genReadable2(mob,me,++showNumber,showFlag);
			if(me instanceof Light)	genBurnout(mob,(Light)me,++showNumber,showFlag);
			genBehaviors(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			if(me instanceof Rideable)
			{
				genRideable1(mob,(Rideable)me,++showNumber,showFlag);
				genRideable2(mob,(Rideable)me,++showNumber,showFlag);
			}
			if(me instanceof Exit)
			{
				genDoorName(mob,(Exit)me,++showNumber,showFlag);
				genClosedText(mob,(Exit)me,++showNumber,showFlag);
			}
			genImage(mob,me,++showNumber,showFlag);
            for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
                me.setStat(me.getStatCodes()[x],CMLib.english().promptText(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=CMath.s_int(mob.session().prompt(getScr("BaseGenerics","editwhich"),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				me.recoverEnvStats();
				if(me.text().length()>=maxLength)
				{
					mob.tell(getScr("BaseGenerics","stringlimiterr",maxLength+""));
					ok=false;
				}
			}
		}
	}

	public static void modifyGenWeapon(MOB mob, Weapon me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		int showFlag=-1;
		if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!ok))
		{
			int showNumber=0;
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			genLevel(mob,me,++showNumber,showFlag);
			genAttack(mob,me,++showNumber,showFlag);
			genDamage(mob,me,++showNumber,showFlag);
			genMaterialCode(mob,me,++showNumber,showFlag);
			genWeaponType(mob,me,++showNumber,showFlag);
			genWeaponClassification(mob,me,++showNumber,showFlag);
			genWeaponRanges(mob,me,++showNumber,showFlag);
			if(me instanceof Wand)
			{
				genReadable1(mob,me,++showNumber,showFlag);
				genReadable2(mob,me,++showNumber,showFlag);
				genUses(mob,me,++showNumber,showFlag);
				genMaxUses(mob,(Wand)me,++showNumber,showFlag);
				if(me instanceof Light)	genBurnout(mob,(Light)me,++showNumber,showFlag);
			}
			else
				genWeaponAmmo(mob,me,++showNumber,showFlag);
			genRejuv(mob,me,++showNumber,showFlag);
			if((!me.requiresAmmunition())&&(!(me instanceof Wand)))
				genCondition(mob,me,++showNumber,showFlag);
			genAbility(mob,me,++showNumber,showFlag);
			genSecretIdentity(mob,me,++showNumber,showFlag);
			if(me instanceof ClanItem)
				genClanItem(mob,(ClanItem)me,++showNumber,showFlag);
			genGettable(mob,me,++showNumber,showFlag);
			genValue(mob,me,++showNumber,showFlag);
			genWeight(mob,me,++showNumber,showFlag);
			genDisposition(mob,me.baseEnvStats(),++showNumber,showFlag);
			genBehaviors(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			genImage(mob,me,++showNumber,showFlag);
            for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
                me.setStat(me.getStatCodes()[x],CMLib.english().promptText(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=CMath.s_int(mob.session().prompt(getScr("BaseGenerics","editwhich"),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				me.recoverEnvStats();
				if(me.text().length()>=maxLength)
				{
					mob.tell(getScr("BaseGenerics","stringlimiterr",maxLength+""));
					ok=false;
				}
			}
		}
	}
	public static void modifyGenArmor(MOB mob, Armor me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		int showFlag=-1;
		if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!ok))
		{
			int showNumber=0;
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			genLevel(mob,me,++showNumber,showFlag);
			genMaterialCode(mob,me,++showNumber,showFlag);
			genWornLocation(mob,me,++showNumber,showFlag);
			genLayer(mob,me,++showNumber,showFlag);
			genRejuv(mob,me,++showNumber,showFlag);
			genArmor(mob,me,++showNumber,showFlag);
			genCondition(mob,me,++showNumber,showFlag);
			genAbility(mob,me,++showNumber,showFlag);
			genSecretIdentity(mob,me,++showNumber,showFlag);
			if(me instanceof ClanItem)
				genClanItem(mob,(ClanItem)me,++showNumber,showFlag);
			genGettable(mob,me,++showNumber,showFlag);
			genCapacity(mob,me,++showNumber,showFlag);
			genLidsNLocks(mob,me,++showNumber,showFlag);
			genReadable1(mob,me,++showNumber,showFlag);
			genReadable2(mob,me,++showNumber,showFlag);
			if(me instanceof Light)	genBurnout(mob,(Light)me,++showNumber,showFlag);
			genValue(mob,me,++showNumber,showFlag);
			genWeight(mob,me,++showNumber,showFlag);
			genSize(mob,me,++showNumber,showFlag);
			genDisposition(mob,me.baseEnvStats(),++showNumber,showFlag);
			genBehaviors(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			genImage(mob,me,++showNumber,showFlag);
            for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
                me.setStat(me.getStatCodes()[x],CMLib.english().promptText(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=CMath.s_int(mob.session().prompt(getScr("BaseGenerics","editwhich"),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				me.recoverEnvStats();
				if(me.text().length()>=maxLength)
				{
					mob.tell(getScr("BaseGenerics","stringlimiterr",maxLength+""));
					ok=false;
				}
			}
		}
	}


	public static void modifyGenInstrument(MOB mob, MusicalInstrument me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		int showFlag=-1;
		if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!ok))
		{
			int showNumber=0;
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			genLevel(mob,me,++showNumber,showFlag);
			genMaterialCode(mob,me,++showNumber,showFlag);
			genWornLocation(mob,me,++showNumber,showFlag);
			genRejuv(mob,me,++showNumber,showFlag);
			genAbility(mob,me,++showNumber,showFlag);
			genSecretIdentity(mob,me,++showNumber,showFlag);
			genGettable(mob,me,++showNumber,showFlag);
			genInstrumentType(mob,me,++showNumber,showFlag);
			genValue(mob,me,++showNumber,showFlag);
			genWeight(mob,me,++showNumber,showFlag);
			genDisposition(mob,me.baseEnvStats(),++showNumber,showFlag);
			genBehaviors(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			genImage(mob,me,++showNumber,showFlag);
            for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
                me.setStat(me.getStatCodes()[x],CMLib.english().promptText(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=CMath.s_int(mob.session().prompt(getScr("BaseGenerics","editwhich"),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				me.recoverEnvStats();
				if(me.text().length()>=maxLength)
				{
					mob.tell(getScr("BaseGenerics","stringlimiterr",maxLength+""));
					ok=false;
				}
			}
		}
	}


	public static void modifyGenExit(MOB mob, Exit me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		int showFlag=-1;
		if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!ok))
		{
			int showNumber=0;
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			genLevel(mob,me,++showNumber,showFlag);
			genDoorsNLocks(mob,me,++showNumber,showFlag);
			if(me.hasADoor())
			{
				genClosedText(mob,me,++showNumber,showFlag);
				genDoorName(mob,me,++showNumber,showFlag);
				genOpenWord(mob,me,++showNumber,showFlag);
				genCloseWord(mob,me,++showNumber,showFlag);
			}
			genExitMisc(mob,me,++showNumber,showFlag);
			genDisposition(mob,me.baseEnvStats(),++showNumber,showFlag);
			genBehaviors(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			genImage(mob,me,++showNumber,showFlag);
            for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
                me.setStat(me.getStatCodes()[x],CMLib.english().promptText(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=CMath.s_int(mob.session().prompt(getScr("BaseGenerics","editwhich"),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				me.recoverEnvStats();
				if(me.text().length()>=maxLength)
				{
					mob.tell(getScr("BaseGenerics","stringlimiterr",maxLength+""));
					ok=false;
				}
			}
		}
	}


	public static void modifyGenMOB(MOB mob, MOB me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		int showFlag=-1;
		if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!ok))
		{
			int showNumber=0;
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			int oldLevel=me.baseEnvStats().level();
			genLevel(mob,me,++showNumber,showFlag);
			if((oldLevel<2)&&(me.baseEnvStats().level()>1))
				me.baseCharStats().getCurrentClass().fillOutMOB(me,me.baseEnvStats().level());
			genRejuv(mob,me,++showNumber,showFlag);
			genRace(mob,me,++showNumber,showFlag);
			Faction F=null;
			for(Enumeration e=CMLib.factions().factionSet().elements();e.hasMoreElements();)
			{
			    F=(Faction)e.nextElement();
			    if((!F.hasFaction(me))&&(F.findAutoDefault(me)!=Integer.MAX_VALUE))
			        mob.addFaction(F.factionID(),F.findAutoDefault(me));
			    if(F.showineditor())
				    genSpecialFaction(mob,me,++showNumber,showFlag,F);
			}
			genGender(mob,me,++showNumber,showFlag);
			genHeight(mob,me,++showNumber,showFlag);
			genWeight(mob,me,++showNumber,showFlag);
			genClan(mob,me,++showNumber,showFlag);
			genSpeed(mob,me,++showNumber,showFlag);
			if((oldLevel<2)&&(me.baseEnvStats().level()>1))
				me.baseEnvStats().setDamage((int)Math.round(CMath.div(me.baseEnvStats().damage(),me.baseEnvStats().speed())));
			genAttack(mob,me,++showNumber,showFlag);
			genDamage(mob,me,++showNumber,showFlag);
			genArmor(mob,me,++showNumber,showFlag);
			genHitPoints(mob,me,++showNumber,showFlag);
			genMoney(mob,me,++showNumber,showFlag);
			genAbilities(mob,me,++showNumber,showFlag);
			genBehaviors(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			genDisposition(mob,me.baseEnvStats(),++showNumber,showFlag);
			genSensesMask(mob,me.baseEnvStats(),++showNumber,showFlag);
			if(me instanceof Rideable)
			{
				genRideable1(mob,(Rideable)me,++showNumber,showFlag);
				genRideable2(mob,(Rideable)me,++showNumber,showFlag);
			}
			if(me instanceof Deity)
			{
				genDeity1(mob,(Deity)me,++showNumber,showFlag);
				genDeity2(mob,(Deity)me,++showNumber,showFlag);
				genDeity3(mob,(Deity)me,++showNumber,showFlag);
				genDeity4(mob,(Deity)me,++showNumber,showFlag);
				genDeity5(mob,(Deity)me,++showNumber,showFlag);
				genDeity8(mob,(Deity)me,++showNumber,showFlag);
				genDeity9(mob,(Deity)me,++showNumber,showFlag);
				genDeity6(mob,(Deity)me,++showNumber,showFlag);
				genDeity0(mob,(Deity)me,++showNumber,showFlag);
				genDeity7(mob,(Deity)me,++showNumber,showFlag);
                genDeity11(mob,(Deity)me,++showNumber,showFlag);
			}
			genFaction(mob,me,++showNumber,showFlag);
			genTattoos(mob,me,++showNumber,showFlag);
			genExpertises(mob,me,++showNumber,showFlag);
			genImage(mob,me,++showNumber,showFlag);
            for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
                me.setStat(me.getStatCodes()[x],CMLib.english().promptText(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=CMath.s_int(mob.session().prompt(getScr("BaseGenerics","editwhich"),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				me.recoverCharStats();
				me.recoverMaxState();
				me.recoverEnvStats();
				me.resetToMaxState();
				if(me.text().length()>=maxLength)
				{
					mob.tell(getScr("BaseGenerics","stringlimiterr",maxLength+""));
					ok=false;
				}
				me.setMiscText(me.text());
			}
		}

		mob.tell(getScr("BaseGenerics","equipbeforesave",me.charStats().himher()));
	}

	public static void modifyPlayer(MOB mob, MOB me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		int showFlag=-1;
		if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		String oldName=me.Name();
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!ok))
		{
			int showNumber=0;
			genName(mob,me,++showNumber,showFlag);
			while((!me.Name().equals(oldName))&&(CMLib.database().DBUserSearch(null,me.Name())))
			{
				mob.tell(getScr("BaseGenerics","namealused"));
				genName(mob,me,showNumber,showFlag);
			}
			genPassword(mob,me,++showNumber,showFlag);

			genDescription(mob,me,++showNumber,showFlag);
			genLevel(mob,me,++showNumber,showFlag);
			genRace(mob,me,++showNumber,showFlag);
			genCharClass(mob,me,++showNumber,showFlag);
			genCharStats(mob,me,++showNumber,showFlag);
			Faction F=null;
			for(Enumeration e=CMLib.factions().factionSet().elements();e.hasMoreElements();)
			{
			    F=(Faction)e.nextElement();
			    if((!F.hasFaction(me))&&(F.findAutoDefault(me)!=Integer.MAX_VALUE))
			        mob.addFaction(F.factionID(),F.findAutoDefault(me));
			    if(F.showineditor())
				    genSpecialFaction(mob,me,++showNumber,showFlag,F);
			}
			genGender(mob,me,++showNumber,showFlag);
			genHeight(mob,me,++showNumber,showFlag);
			genWeight(mob,me,++showNumber,showFlag);
			genSpeed(mob,me,++showNumber,showFlag);
			genAttack(mob,me,++showNumber,showFlag);
			genDamage(mob,me,++showNumber,showFlag);
			genArmor(mob,me,++showNumber,showFlag);
			genHitPoints(mob,me,++showNumber,showFlag);
			genMoney(mob,me,++showNumber,showFlag);
            me.setTrains(CMLib.english().promptInteger(mob,me.getTrains(),++showNumber,showFlag,getScr("BaseGenerics","trpoints")));
            me.setPractices(CMLib.english().promptInteger(mob,me.getPractices(),++showNumber,showFlag,getScr("BaseGenerics","practicep")));
            me.setQuestPoint(CMLib.english().promptInteger(mob,me.getQuestPoint(),++showNumber,showFlag,getScr("BaseGenerics","questpoints")));
			genAbilities(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			genBehaviors(mob,me,++showNumber,showFlag);
			genDisposition(mob,me.baseEnvStats(),++showNumber,showFlag);
			genSensesMask(mob,me.baseEnvStats(),++showNumber,showFlag);
			if(me instanceof Rideable)
			{
				genRideable1(mob,(Rideable)me,++showNumber,showFlag);
				genRideable2(mob,(Rideable)me,++showNumber,showFlag);
			}
			genFaction(mob,me,++showNumber,showFlag);
			genTattoos(mob,me,++showNumber,showFlag);
			genExpertises(mob,me,++showNumber,showFlag);
			genTitles(mob,me,++showNumber,showFlag);
			genEmail(mob,me,++showNumber,showFlag);
			genSecurity(mob,me,++showNumber,showFlag);
			genImage(mob,me,++showNumber,showFlag);
			genNotes(mob,me,++showNumber,showFlag);
            for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
                me.setStat(me.getStatCodes()[x],CMLib.english().promptText(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
            if(me.playerStats()!=null)
            for(int x=me.playerStats().getSaveStatIndex();x<me.playerStats().getStatCodes().length;x++)
                me.setStat(me.playerStats().getStatCodes()[x],CMLib.english().promptText(mob,me.playerStats().getStat(me.playerStats().getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.playerStats().getStatCodes()[x])));
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=CMath.s_int(mob.session().prompt(getScr("BaseGenerics","editwhich"),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				me.recoverCharStats();
				me.recoverMaxState();
				me.recoverEnvStats();
				me.resetToMaxState();
				if(!oldName.equals(me.Name()))
				{
					MOB fakeMe=(MOB)me.copyOf();
					fakeMe.setName(oldName);
					CMLib.database().DBDeleteMOB(fakeMe);
					CMLib.database().DBCreateCharacter(me);
				}
				CMLib.database().DBUpdatePlayer(me);
				CMLib.database().DBUpdateFollowers(me);
			}
		}
	}

	
	static void genClanStatus(MOB mob, Clan C, int showNumber, int showFlag)
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","clanstatus",showNumber+"",Clan.CLANSTATUS_DESC[C.getStatus()]));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		switch(C.getStatus())
		{
		case Clan.CLANSTATUS_ACTIVE:
			C.setStatus(Clan.CLANSTATUS_PENDING);
			mob.tell("Clan '"+C.name()+"' has been changed from active to pending!");
			break;
		case Clan.CLANSTATUS_PENDING:
			C.setStatus(Clan.CLANSTATUS_ACTIVE);
			mob.tell("Clan '"+C.name()+"' has been changed from pending to active!");
			break;
		case Clan.CLANSTATUS_FADING:
			C.setStatus(Clan.CLANSTATUS_ACTIVE);
			mob.tell("Clan '"+C.name()+"' has been changed from fading to active!");
			break;
		default:
			mob.tell("Clan '"+C.name()+"' has not been changed!");
			break;
		}
	}
	
	static void genClanGovt(MOB mob, Clan C, int showNumber, int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","clangovt",showNumber+"",C.typeName()));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		while((mob.session()!=null)&&(!mob.session().killFlag()))
		{
			String newName=mob.session().prompt(getScr("BaseGenerics","enter2"),"");
			if(newName.trim().length()==0)
			{
				mob.tell(getScr("BaseGenerics","nochange"));
				return;
			}
			int newGovt=-1;
			StringBuffer gvts=new StringBuffer();
			for(int i=0;i<Clan.GVT_DESCS.length;i++)
			{
				gvts.append(Clan.GVT_DESCS[i]+", ");
				if(newName.equalsIgnoreCase(Clan.GVT_DESCS[i]))
					newGovt=i;
			}
			gvts=new StringBuffer(gvts.substring(0,gvts.length()-2));
			if(newGovt<0)
				mob.tell(getScr("BaseGenerics","clangvterr",gvts.toString()));
			else
			{
				C.setGovernment(newGovt);
				break;
			}
		}
	}
	
	static void genClanRole(MOB mob, Clan C, int showNumber, int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(getScr("BaseGenerics","clanrole",showNumber+"",CMLib.clans().getRoleName(C.getGovernment(),C.getAutoPosition(),true,false)));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		while((mob.session()!=null)&&(!mob.session().killFlag()))
		{
			String newName=mob.session().prompt(getScr("BaseGenerics","enter2"),"");
			if(newName.trim().length()==0)
			{
				mob.tell(getScr("BaseGenerics","nochange"));
				return;
			}
			int newRole=-1;
			StringBuffer roles=new StringBuffer();
			for(int i=0;i<Clan.ROL_DESCS[C.getGovernment()].length;i++)
			{
				roles.append(Clan.ROL_DESCS[C.getGovernment()][i]+", ");
				if(newName.equalsIgnoreCase(Clan.ROL_DESCS[C.getGovernment()][i]))
					newRole=Clan.POSORDER[i];
			}
			roles=new StringBuffer(roles.substring(0,roles.length()-2));
			if(newRole<0)
				mob.tell(getScr("BaseGenerics","clanrolerr",roles.toString()));
			else
			{
				C.setAutoPosition(newRole);
				break;
			}
		}
	}
	
	static void genClanClass(MOB mob, Clan C, int showNumber, int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		CharClass CC=CMClass.getCharClass(C.getClanClass());
		if(CC==null)CC=CMClass.findCharClass(C.getClanClass());
		String clasName=(CC==null)?"NONE":CC.name();
		mob.tell(getScr("BaseGenerics","clanclas",showNumber+"",clasName));
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		while((mob.session()!=null)&&(!mob.session().killFlag()))
		{
			String newName=mob.session().prompt(getScr("BaseGenerics","enter2"),"");
			if(newName.trim().equalsIgnoreCase("none"))
			{
				C.setClanClass("");
				return;
			}
			else
			if(newName.trim().length()==0)
			{
				mob.tell(getScr("BaseGenerics","nochange"));
				return;
			}
			CharClass newC=null;
			StringBuffer clss=new StringBuffer();
			for(Enumeration e=CMClass.charClasses();e.hasMoreElements();)
			{
				CC=(CharClass)e.nextElement();
				clss.append(CC.name()+", ");
				if(newName.equalsIgnoreCase(CC.name())||(newName.equalsIgnoreCase(CC.ID())))
					newC=CC;
			}
			clss=new StringBuffer(clss.substring(0,clss.length()-2));
			if(newC==null)
				mob.tell(getScr("BaseGenerics","clanclaserr",clss.toString()));
			else
			{
				C.setClanClass(newC.ID());
				break;
			}
		}
	}
	
	static String genClanRoom(MOB mob, Clan C, String oldRoomID, String promptCode, int showNumber, int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return oldRoomID;
		mob.tell(getScr("BaseGenerics",promptCode,showNumber+"",oldRoomID));
		if((showFlag!=showNumber)&&(showFlag>-999)) return oldRoomID;
		while((mob.session()!=null)&&(!mob.session().killFlag()))
		{
			String newName=mob.session().prompt(getScr("BaseGenerics","entnull"),"");
			if(newName.trim().equalsIgnoreCase("null"))
				return "";
			else
			if(newName.trim().length()==0)
			{
				mob.tell(getScr("BaseGenerics","nochange"));
				return oldRoomID;
			}
			Room newRoom=CMLib.map().getRoom(newName);
			if((newRoom==null)
			||(CMLib.map().getExtendedRoomID(newRoom).length()==0)
			||(!CMLib.utensils().doesOwnThisProperty(C.clanID(),newRoom)))
				mob.tell(getScr("BaseGenerics","clanroomerr"));
			else
				return CMLib.map().getExtendedRoomID(newRoom);
		}
		return oldRoomID;
	}
    
	public static void modifyClan(MOB mob, Clan C)
	throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		int showFlag=-1;
		if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		String oldName=C.ID();
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!ok))
		{
			mob.tell(getScr("BaseGenerics","Name","*",C.name()));
			int showNumber=0;
			genClanGovt(mob,C,++showNumber,showFlag);
			C.setPremise(CMLib.english().promptText(mob,C.getPremise(),++showNumber,showFlag,getScr("BaseGenerics","clanprem"),true));
			C.setExp(CMLib.english().promptLong(mob,C.getExp(),++showNumber,showFlag,getScr("BaseGenerics","clanexp")));
			C.setTaxes(CMLib.english().promptDouble(mob,C.getTaxes(),++showNumber,showFlag,getScr("BaseGenerics","clantax")));
			C.setMorgue(genClanRoom(mob,C,C.getMorgue(),"clanmorg",++showNumber,showFlag));
			C.setRecall(genClanRoom(mob,C,C.getRecall(),"clanreca",++showNumber,showFlag));
			C.setDonation(genClanRoom(mob,C,C.getDonation(),"clandona",++showNumber,showFlag));
			genClanAccept(mob,C,++showNumber,showFlag);
			genClanClass(mob,C,++showNumber,showFlag);
			genClanRole(mob,C,++showNumber,showFlag);
			genClanStatus(mob,C,++showNumber,showFlag);
			genClanMembers(mob,C,++showNumber,showFlag);
			/*setClanRelations, votes?*/
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=CMath.s_int(mob.session().prompt(getScr("BaseGenerics","editwhich"),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				if(!oldName.equals(C.ID()))
				{
					//cycle through everything changing the name
					CMLib.database().DBDeleteClan(C);
					CMLib.database().DBCreateClan(C);
				}
				C.update();
			}
		}
	}
	
	public static void modifyGenShopkeeper(MOB mob, ShopKeeper me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		if(!(me instanceof MOB))
			return;
		MOB mme=(MOB)me;
		boolean ok=false;
		int showFlag=-1;
		if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!ok))
		{
			int showNumber=0;
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			int oldLevel=me.baseEnvStats().level();
			genLevel(mob,me,++showNumber,showFlag);
			if((oldLevel<2)&&(me.baseEnvStats().level()>1))
				mme.baseCharStats().getCurrentClass().fillOutMOB(mme,me.baseEnvStats().level());
			genRejuv(mob,me,++showNumber,showFlag);
			genRace(mob,mme,++showNumber,showFlag);
			genHeight(mob,me,++showNumber,showFlag);
			genWeight(mob,me,++showNumber,showFlag);
			Faction F=null;
			for(Enumeration e=CMLib.factions().factionSet().elements();e.hasMoreElements();)
			{
			    F=(Faction)e.nextElement();
			    if((!F.hasFaction((MOB)me))&&(F.findAutoDefault((MOB)me)!=Integer.MAX_VALUE))
			        mob.addFaction(F.factionID(),F.findAutoDefault((MOB)me));
			    if(F.showineditor())
				    genSpecialFaction(mob,(MOB)me,++showNumber,showFlag,F);
			}
			genGender(mob,mme,++showNumber,showFlag);
			genClan(mob,mme,++showNumber,showFlag);
			genSpeed(mob,me,++showNumber,showFlag);
			if((oldLevel<2)&&(me.baseEnvStats().level()>1))
				me.baseEnvStats().setDamage((int)Math.round(CMath.div(me.baseEnvStats().damage(),me.baseEnvStats().speed())));
			genAttack(mob,me,++showNumber,showFlag);
			genDamage(mob,me,++showNumber,showFlag);
			genArmor(mob,me,++showNumber,showFlag);
			if(me instanceof MOB)
				genHitPoints(mob,(MOB)me,++showNumber,showFlag);
			genMoney(mob,mme,++showNumber,showFlag);
			genAbilities(mob,mme,++showNumber,showFlag);
			genBehaviors(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			genShopkeeper1(mob,me,++showNumber,showFlag);
			genShopkeeper2(mob,me,++showNumber,showFlag);
			genShopkeeper3(mob,me,++showNumber,showFlag);
            genShopkeeper7(mob,me,++showNumber,showFlag);
			if(me instanceof Banker)
			{
				genBanker1(mob,(Banker)me,++showNumber,showFlag);
				genBanker2(mob,(Banker)me,++showNumber,showFlag);
				genBanker3(mob,(Banker)me,++showNumber,showFlag);
				genBanker4(mob,(Banker)me,++showNumber,showFlag);
			}
			else
            if(me instanceof PostOffice)
            {
                ((PostOffice)me).setPostalChain(CMLib.english().promptText(mob,((PostOffice)me).postalChain(),++showNumber,showFlag,getScr("BaseGenerics","postalchainl")));
                ((PostOffice)me).setFeeForNewBox(CMLib.english().promptDouble(mob,((PostOffice)me).feeForNewBox(),++showNumber,showFlag,getScr("BaseGenerics","feebox")));
                ((PostOffice)me).setMinimumPostage(CMLib.english().promptDouble(mob,((PostOffice)me).minimumPostage(),++showNumber,showFlag,getScr("BaseGenerics","minpostcost")));
                ((PostOffice)me).setPostagePerPound(CMLib.english().promptDouble(mob,((PostOffice)me).postagePerPound(),++showNumber,showFlag,getScr("BaseGenerics","poundcost")));
                ((PostOffice)me).setHoldFeePerPound(CMLib.english().promptDouble(mob,((PostOffice)me).holdFeePerPound(),++showNumber,showFlag,getScr("BaseGenerics","feepound")));
                ((PostOffice)me).setMaxMudMonthsHeld(CMLib.english().promptInteger(mob,((PostOffice)me).maxMudMonthsHeld(),++showNumber,showFlag,getScr("BaseGenerics","monthsmac")));
            }
            else
			{
				genShopkeeper4(mob,me,++showNumber,showFlag);
				genShopkeeper5(mob,me,++showNumber,showFlag);
				genShopkeeper6(mob,me,++showNumber,showFlag);
			}
			genDisposition(mob,me.baseEnvStats(),++showNumber,showFlag);
			genSensesMask(mob,me.baseEnvStats(),++showNumber,showFlag);
			genFaction(mob,mme,++showNumber,showFlag);
			genTattoos(mob,(MOB)me,++showNumber,showFlag);
			genExpertises(mob,(MOB)me,++showNumber,showFlag);
			genImage(mob,me,++showNumber,showFlag);
            for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
                me.setStat(me.getStatCodes()[x],CMLib.english().promptText(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=CMath.s_int(mob.session().prompt(getScr("BaseGenerics","editwhich"),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				mme.recoverCharStats();
				mme.recoverMaxState();
				me.recoverEnvStats();
				mme.resetToMaxState();
				if(me.text().length()>=maxLength)
				{
					mob.tell(getScr("BaseGenerics","stringlimiterr",maxLength+""));
					ok=false;
				}
				me.setMiscText(me.text());
			}
		}
		mob.tell(getScr("BaseGenerics","genmobmsg"));
	}
}
