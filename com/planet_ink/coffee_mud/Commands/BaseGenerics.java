package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;


/* 
   Copyright 2000-2005 Bo Zimmerman

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
		mob.tell(showNumber+". Name: '"+E.Name()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a new one\n\r:","");
		if(newName.length()>0)
			E.setName(newName);
		else
			mob.tell("(no change)");
	}

	static void genImage(MOB mob, Environmental E, int showNumber, int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". MXP file: '"+E.image()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a new filename\n\r:","");
		if(newName.length()>0)
			E.setImage(newName);
		else
			mob.tell("(no change)");
	}

	static void genCorpseData(MOB mob, DeadBody E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Corpse Data: '"+E.mobName()+"/"+E.killerName()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a new name\n\r:","");
		if(newName.length()>0)
			E.setMobName(newName);
		else
			mob.tell("(no change)");
		mob.tell("Dead MOB Description: '"+E.mobDescription()+"'.");
		newName=mob.session().prompt("Enter a new description\n\r:","");
		if(newName.length()>0)
			E.setMobDescription(newName);
		else
			mob.tell("(no change)");
		mob.tell("Killers Name: '"+E.killerName()+"'.");
		newName=mob.session().prompt("Enter a new killer\n\r:","");
		if(newName.length()>0)
			E.setKillerName(newName);
		else
			mob.tell("(no change)");
	}

	static void genAuthor(MOB mob, Area A, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Author: '"+A.getAuthorID()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a new one\n\r:","");
		if(newName.length()>0)
			A.setAuthorID(newName);
		else
			mob.tell("(no change)");
	}

	static void genPanelType(MOB mob, ShipComponent.ShipPanel S, int showNumber, int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String componentType=Util.capitalizeAndLower(ShipComponent.ShipPanel.COMPONENT_PANEL_DESC[S.panelType()].toLowerCase());
		mob.tell(showNumber+". Panel Type: '"+componentType+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		boolean continueThis=true;
		while(continueThis)
		{
		    continueThis=false;
			String newName=mob.session().prompt("Enter a new one (?)\n\r:","");
			if(newName.length()>0)
			{
			    if(newName.equalsIgnoreCase("?"))
			    {
			        mob.tell("Component Types: "+Util.toStringList(ShipComponent.ShipPanel.COMPONENT_PANEL_DESC));
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
			            mob.tell("'"+newName+"' is not recognized.  Try '?' for a list.");
			            continueThis=true;
			        }
			        else
			            S.setPanelType(newType);
			    }
			}
			else
				mob.tell("(no change)");
		}
	}
    
	static void genCurrency(MOB mob, Area A, int showNumber, int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String currencyName=A.getCurrency().length()==0?"Default":A.getCurrency();
		mob.tell(showNumber+". Currency: '"+currencyName+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a new one or 'DEFAULT'\n\r:","");
		if(newName.length()>0)
		{
		    if(newName.equalsIgnoreCase("default"))
		        A.setCurrency("");
		    else
		    if((newName.indexOf("=")<0)&&(!BeanCounter.getAllCurrencies().contains(newName.trim().toUpperCase())))
		    {
		        Vector V=BeanCounter.getAllCurrencies();
		        mob.tell("'"+newName.trim().toUpperCase()+"' is not a known currency. Existing currencies include: DEFAULT"+Util.toStringList(V));
		    }
		    else
		    if(newName.indexOf("=")>=0)
		        A.setCurrency(newName.trim());
		    else
				A.setCurrency(newName.toUpperCase().trim());
		}
		else
			mob.tell("(no change)");
	}

	static void genTimeClock(MOB mob, Area A, int showNumber, int showFlag)
	throws IOException
	{

		if((showFlag>0)&&(showFlag!=showNumber)) return;
		TimeClock TC=A.getTimeObj();
		StringBuffer report=new StringBuffer("");
		if(TC==DefaultTimeClock.globalClock)
			report.append("Default -- Can't be changed.");
		else
		{
		    report.append(TC.getHoursInDay()+" hrs-day/");
		    report.append(TC.getDaysInMonth()+" days-mn/");
		    report.append(TC.getMonthsInYear()+" mnths-yr");
		}
		mob.tell(showNumber+". Calendar: '"+report.toString()+"'.");
		if(TC==DefaultTimeClock.globalClock) return;
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName="";
		while(newName.length()==0)
		{
			report=new StringBuffer("\n\rCalendar/Clock settings:\n\r");
		    report.append("1. "+TC.getHoursInDay()+" hours per day\n\r");
		    report.append("2. Dawn Hour: "+TC.getDawnToDusk()[TimeClock.TIME_DAWN]+"\n\r");
		    report.append("3. Day Hour: "+TC.getDawnToDusk()[TimeClock.TIME_DAY]+"\n\r");
		    report.append("4. Dusk Hour: "+TC.getDawnToDusk()[TimeClock.TIME_DUSK]+"\n\r");
		    report.append("5. Night Hour: "+TC.getDawnToDusk()[TimeClock.TIME_NIGHT]+"\n\r");
		    report.append("6. Weekdays: "+Util.toStringList(TC.getWeekNames())+"\n\r");
		    report.append("7. Months: "+Util.toStringList(TC.getMonthNames())+"\n\r");
		    report.append("8. Year Title(s): "+Util.toStringList(TC.getYearNames()));
		    mob.tell(report.toString());
			newName=mob.session().prompt("Enter one to change: ","");
			if(newName.length()==0) break;
			int which=Util.s_int(newName);
			
			if((which<0)||(which>8))
				mob.tell("Invalid: "+which);
			else
			if(which<=5)
			{
			    newName="";
			    String newNum=mob.session().prompt("Enter a new number: ","");
			    int val=Util.s_int(newNum);
			    if(newNum.length()==0)
			        mob.tell("No Change");
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
                        mob.tell("That value is before the dawn!");
		            else
			            TC.getDawnToDusk()[TimeClock.TIME_DAY]=val;
		            break;
		        case 4:
		            if((val>=0)&&(TC.getDawnToDusk()[TimeClock.TIME_DAWN]>=val))
                        mob.tell("That value is before the dawn!");
		            else
		            if((val>=0)&&(TC.getDawnToDusk()[TimeClock.TIME_DAY]>=val))
                        mob.tell("That value is before the day!");
		            else
			            TC.getDawnToDusk()[TimeClock.TIME_DUSK]=val;
		            break;
		        case 5:
		            if((val>=0)&&(TC.getDawnToDusk()[TimeClock.TIME_DAWN]>=val))
                        mob.tell("That value is before the dawn!");
		            else
		            if((val>=0)&&(TC.getDawnToDusk()[TimeClock.TIME_DAY]>=val))
                        mob.tell("That value is before the day!");
		            else
		            if((val>=0)&&(TC.getDawnToDusk()[TimeClock.TIME_DUSK]>=val))
                        mob.tell("That value is before the dusk!");
		            else
			            TC.getDawnToDusk()[TimeClock.TIME_NIGHT]=val;
		            break;
			    }
			}
			else
			{
			    newName="";
			    String newNum=mob.session().prompt("Enter a new list (comma delimited)\n\r: ","");
			    if(newNum.length()==0)
			        mob.tell("No Change");
			    else
			    switch(which)
			    {
		        case 6:
		            TC.setDaysInWeek(Util.toStringArray(Util.parseCommas(newNum,true)));
		            break;
		        case 7:
		            TC.setMonthsInYear(Util.toStringArray(Util.parseCommas(newNum,true)));
		            break;
		        case 8:
		            TC.setYearNames(Util.toStringArray(Util.parseCommas(newNum,true)));
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
			mob.tell(showNumber+". Clan (ID): '"+E.getClanID()+"'.");
			if((showFlag==showNumber)||(showFlag<=-999))
			{
				String newName=mob.session().prompt("Enter a new one (null)\n\r:","");
				if(newName.equalsIgnoreCase("null"))
					E.setClanID("");
				else
				if(newName.length()>0)
				{
					E.setClanID(newName);
					E.setClanRole(Clan.POS_MEMBER);
				}
				else
					mob.tell("(no change)");
			}
		}
		if(((showFlag<=0)||(showFlag==showNumber))
		   &&(!E.isMonster())
		   &&(E.getClanID().length()>0)
		   &&(Clans.getClan(E.getClanID())!=null))
		{

			Clan C=Clans.getClan(E.getClanID());
			mob.tell(showNumber+". Clan (Role): '"+Clans.getRoleName(C.getGovernment(),E.getClanRole(),true,false)+"'.");
			if((showFlag==showNumber)||(showFlag<=-999))
			{
				String newName=mob.session().prompt("Enter a new one\n\r:","");
				if(newName.length()>0)
				{
					int newRole=-1;
					for(int i=0;i<Clan.ROL_DESCS[C.getGovernment()].length;i++)
						if(newName.equalsIgnoreCase(Clan.ROL_DESCS[C.getGovernment()][i]))
						{
							newRole=Util.pow(2,i-1);
							break;
						}
					if(newRole<0)
						mob.tell("That role is invalid. Try: "+Util.toStringList(Clan.ROL_DESCS[C.getGovernment()]));
					else
						E.setClanRole(newRole);
				}
				else
					mob.tell("(no change)");
			}
		}
	}

	static void genArchivePath(MOB mob, Area E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Archive file name: '"+E.getArchivePath()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a new one (null=default)\n\r:","");
		if(newName.equalsIgnoreCase("null"))
			E.setArchivePath("");
		else
		if(newName.length()>0)
			E.setArchivePath(newName);
		else
			mob.tell("(no change)");
	}

	public static Room changeRoomType(Room R, Room newRoom)
	{
		if((R==null)||(newRoom==null)) return R;
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
		CMClass.ThreadEngine().deleteTick(oldR,-1);
		CMMap.delRoom(oldR);
		CMMap.addRoom(R);
		R.setArea(oldR.getArea());
		R.setRoomID(oldR.roomID());
		for(int d=0;d<R.rawDoors().length;d++)
			R.rawDoors()[d]=oldR.rawDoors()[d];
		for(int d=0;d<R.rawExits().length;d++)
			R.rawExits()[d]=oldR.rawExits()[d];
		R.setDisplayText(oldR.displayText());
		R.setDescription(oldR.description());
		if((R instanceof GridLocale)&&(oldR instanceof GridLocale))
		{
			((GridLocale)R).setXSize(((GridLocale)oldR).xSize());
			((GridLocale)R).setYSize(((GridLocale)oldR).ySize());
			((GridLocale)R).clearGrid(null);
		}
		Vector allmobs=new Vector();
		int skip=0;
		while(oldR.numInhabitants()>(skip))
		{
			MOB M=oldR.fetchInhabitant(skip);
			if(M.isEligibleMonster())
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
                long rejuv=MudHost.TICKS_PER_RLMIN+MudHost.TICKS_PER_RLMIN+(MudHost.TICKS_PER_RLMIN/2);
                if(rejuv>(MudHost.TICKS_PER_RLMIN*20)) rejuv=(MudHost.TICKS_PER_RLMIN*20);
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
			for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
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
			for(Enumeration e=CMMap.players();e.hasMoreElements();)
			{
				MOB M=(MOB)e.nextElement();
				if(M.getStartRoom()==oldR)
					M.setStartRoom(R);
				else
				if(M.location()==oldR)
					M.setLocation(R);
			}
	    }catch(NoSuchElementException e){}
		R.getArea().clearMaps();
		R.getArea().fillInAreaRoom(R);
        for(int i=0;i<oldBehavsNEffects.size();i++)
        {
            if(oldBehavsNEffects.elementAt(i) instanceof Behavior)
                R.addBehavior((Behavior)oldBehavsNEffects.elementAt(i));
            else
                R.addNonUninvokableEffect((Ability)oldBehavsNEffects.elementAt(i));
        }
		CMClass.DBEngine().DBUpdateRoom(R);
		CMClass.DBEngine().DBUpdateMOBs(R);
		CMClass.DBEngine().DBUpdateItems(R);
		R.startItemRejuv();
		return R;
	}

	static Room genRoomType(MOB mob, Room R, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return R;
		mob.tell(showNumber+". Type: '"+CMClass.className(R)+"'");
		if((showFlag!=showNumber)&&(showFlag>-999)) return R;
		String newName="";
		while(newName.length()==0)
		{
			newName=mob.session().prompt("Enter a new one (?)\n\r:","");
			if(newName.trim().equals("?"))
			{
				mob.tell(CMLister.reallyList2Cols(CMClass.locales(),-1,null).toString()+"\n\r");
				newName="";
			}
			else
			if(newName.length()>0)
			{
				Room newRoom=CMClass.getLocale(newName);
				if(newRoom==null)
					mob.tell("'"+newName+"' does not exist. No Change.");
				else
				if(mob.session().confirm("This will change the room type of room '"+R.roomID()+"'.  It will automatically save any mobs and items in this room permanently.  Are you absolutely sure (y/N)? ","N"))
					R=changeRoomType(R,newRoom);
				R.recoverRoomStats();
			}
			else
			{
				mob.tell("(no change)");
				break;
			}
		}
		return R;
	}

	static void genDescription(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Description: '"+E.description()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a new one (null = empty)\n\r:","");
		if(newName.trim().equalsIgnoreCase("null"))
			E.setDescription("");
		else
		if(newName.length()>0)
			E.setDescription(newName);
		else
			mob.tell("(no change)");
	}

	static void genPassword(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Password: ********.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a new one to reset\n\r:","");
		if((newName.length()>0)&&(E.playerStats()!=null))
		{
			E.playerStats().setPassword(newName);
			CMClass.DBEngine().DBUpdatePassword(E);
		}
		else
			mob.tell("(no change)");
	}

	static void genEmail(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		if(E.playerStats()!=null)
			mob.tell(showNumber+". Email: "+E.playerStats().getEmail());
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a new one\n\r:","");
		if((newName.length()>0)&&(E.playerStats()!=null))
			E.playerStats().setEmail(newName);
		else
			mob.tell("(no change)");
	}

	public static void genDisplayText(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Display: '"+E.displayText()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=null;
		if(E instanceof Item)
			newName=mob.session().prompt("Enter something new (null == blended)\n\r:","");
		else
		if(E instanceof Exit)
			newName=mob.session().prompt("Enter something new (null == see-through)\n\r:","");
		else
			newName=mob.session().prompt("Enter something new (null = empty)\n\r:","");
		if(newName.length()>0)
		{
			if(newName.trim().equalsIgnoreCase("null"))
				newName="";
			E.setDisplayText(newName);
		}
		else
			mob.tell("(no change)");
		if((E instanceof Item)&&(E.displayText().length()==0))
			mob.tell("(blended)");
	}
	public static void genClosedText(MOB mob, Exit E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		if(E instanceof Item)
			mob.tell(showNumber+". Exit Closed Text: '"+E.closedText()+"'.");
		else
			mob.tell(showNumber+". Closed Text: '"+E.closedText()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter something new\n\r:","");
		if(newName.equals("null"))
			E.setExitParams(E.doorName(),E.closeWord(),E.openWord(),"");
		else
		if(newName.length()>0)
			E.setExitParams(E.doorName(),E.closeWord(),E.openWord(),newName);
		else
			mob.tell("(no change)");
	}
	public static void genDoorName(MOB mob, Exit E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		if(E instanceof Item)
			mob.tell(showNumber+". Exit Direction: '"+E.doorName()+"'.");
		else
			mob.tell(showNumber+". Door Name: '"+E.doorName()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter something new\n\r:","");
		if(newName.length()>0)
			E.setExitParams(newName,E.closeWord(),E.openWord(),E.closedText());
		else
			mob.tell("(no change)");
	}

	public static void genBurnout(MOB mob, Light E, int showNumber, int showFlag)
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Is destroyed after burnout: '"+E.destroyedWhenBurnedOut()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.setDestroyedWhenBurntOut(!E.destroyedWhenBurnedOut());
	}

	public static void genOpenWord(MOB mob, Exit E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Open Word: '"+E.openWord()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter something new\n\r:","");
		if(newName.length()>0)
			E.setExitParams(E.doorName(),E.closeWord(),newName,E.closedText());
		else
			mob.tell("(no change)");
	}

	public static void genSubOps(MOB mob, Area A, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String newName="Q";
		while(newName.length()>0)
		{
			mob.tell(showNumber+". Area staff names: "+A.getSubOpList());
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			newName=mob.session().prompt("Enter a name to add or remove\n\r:","");
			if(newName.length()>0)
			{
				if(A.amISubOp(newName))
				{
					A.delSubOp(newName);
					mob.tell("Staff removed.");
				}
				else
				if(CMClass.DBEngine().DBUserSearch(null,newName))
				{
					A.addSubOp(newName);
					mob.tell("Staff added.");
				}
				else
					mob.tell("'"+newName+"' is not recognized as a valid user name.");
			}
		}
	}

    public static void genParentAreas(MOB mob, Area A, int showNumber, int showFlag)
            throws IOException
    {
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String newArea="Q";
		while(newArea.length()>0)
		{
		    mob.tell(showNumber+". Parent Areas: "+A.getParentsList());
		    if((showFlag!=showNumber)&&(showFlag>-999)) return;
		    newArea=mob.session().prompt("Enter an area name to add or remove\n\r:","");
		    if(newArea.length()>0)
		    {
		        Area lookedUp=CMMap.getArea(newArea);
		        if(lookedUp!=null)
		        {
		            if (lookedUp.isChild(A))
					{
						// this new area is already a parent to A,
						// they must want it removed
						A.removeParent(lookedUp);
						lookedUp.removeChild(A);
						mob.tell("Area '" + lookedUp.Name() + "' removed.");
		            }
		            else
					{
		                if(A.canParent(lookedUp))
						{
		                    A.addParent(lookedUp);
		                    lookedUp.addChild(A);
		                    mob.tell("Area '" + lookedUp.Name() + "' added.");
		                }
		                else
		                {
		                    mob.tell("Area '" + lookedUp.Name() +"' cannot be added because this would create a circular reference.");
		                }
		            }
		        }
		        else
		            mob.tell("'"+newArea+"' is not recognized as a valid area name.");
		    }
		}
    }

    public static void genChildAreas(MOB mob, Area A, int showNumber, int showFlag)
            throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        String newArea="Q";
        while(newArea.length()>0)
        {
            mob.tell(showNumber+". Area Children: "+A.getChildrenList());
            if((showFlag!=showNumber)&&(showFlag>-999)) return;
            newArea=mob.session().prompt("Enter an area name to add or remove\n\r:","");
            if(newArea.length()>0)
            {
                Area lookedUp=CMMap.getArea(newArea);
                if(lookedUp!=null)
                {
                    if (lookedUp.isParent(A))
					{
                        // this area is already a child to A, they must want it removed
                        A.removeChild(lookedUp);
                        lookedUp.removeParent(A);
                        mob.tell("Area '" + lookedUp.Name() + "' removed.");
                    }
                    else
					{
                        if(A.canChild(lookedUp))
						{
                            A.addChild(lookedUp);
                            lookedUp.addParent(A);
                            mob.tell("Area '" + lookedUp.Name() + "' added.");
                        }
                        else
                        {
                            mob.tell("Area '" + lookedUp.Name() +"' cannot be added because this would create a circular reference.");
                        }
                    }
                }
                else
                    mob.tell("'"+newArea+"' is not recognized as a valid area name.");
            }
        }
    }

	public static void genCloseWord(MOB mob, Exit E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Close Word: '"+E.closeWord()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter something new\n\r:","");
		if(newName.length()>0)
			E.setExitParams(E.doorName(),newName,E.openWord(),E.closedText());
		else
			mob.tell("(no change)");
	}

	public static void genExitMisc(MOB mob, Exit E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		if(E.hasALock())
		{
			E.setReadable(false);
			mob.tell(showNumber+". Assigned Key Item: '"+E.keyName()+"'.");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			String newName=mob.session().prompt("Enter something new (null=blank)\n\r:","");
			if(newName.equalsIgnoreCase("null"))
				E.setKeyName("");
			else
			if(newName.length()>0)
				E.setKeyName(newName);
			else
				mob.tell("(no change)");
		}
		else
		{
			if((showFlag!=showNumber)&&(showFlag>-999))
			{
				if(!E.isReadable())
					mob.tell(showNumber+". Door not is readable.");
				else
					mob.tell(showNumber+". Door is readable: "+E.readableText());
				return;
			}
			else
			if(genGenericPrompt(mob,"Is this door readable",E.isReadable()))
			{
				E.setReadable(true);
				mob.tell("\n\rText: '"+E.readableText()+"'.");
				String newName=mob.session().prompt("Enter something new\n\r:","");
				if(newName.length()>0)
					E.setReadableText(newName);
				else
					mob.tell("(no change)");
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
		 ||(E instanceof Key))
			Sense.setReadable(E,false);
		else
		if((CMClass.className(E).endsWith("Readable"))
		||(E instanceof Recipe)
		||(E instanceof com.planet_ink.coffee_mud.interfaces.Map))
			Sense.setReadable(E,true);
		else
		if((showFlag!=showNumber)&&(showFlag>-999))
			mob.tell(showNumber+". Item is readable: "+Sense.isReadable(E));
		else
			Sense.setReadable(E,genGenericPrompt(mob,showNumber+". Is this item readable",Sense.isReadable(E)));
	}

	public static void genReadable2(MOB mob, Item E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;

		if((Sense.isReadable(E))
		 ||(E instanceof SpellHolder)
		 ||(E instanceof Ammunition)
		 ||(E instanceof Recipe)
		 ||(CMClass.className(E).toUpperCase().endsWith("PORTAL"))
		 ||(E instanceof Wand)
		 ||(E instanceof Light)
		 ||(E instanceof Key))
		{
			boolean ok=false;
			while(!ok)
			{
				if(CMClass.className(E).endsWith("SuperPill"))
				{
					mob.tell(showNumber+". Assigned Spell or Parameters: '"+E.readableText()+"'.");
					ok=true;
				}
				else
				if(E instanceof SpellHolder)
					mob.tell(showNumber+". Assigned Spell(s) ( ';' delimited)\n: '"+E.readableText()+"'.");
				else
				if(E instanceof Ammunition)
				{
					mob.tell(showNumber+". Ammunition type: '"+E.readableText()+"'.");
					ok=true;
				}
				else
				if(CMClass.className(E).toUpperCase().endsWith("PORTAL"))
				{
					mob.tell(showNumber+". Assigned Room IDs: '"+E.readableText()+"'.");
					ok=true;
				}
				else
				if(E instanceof Wand)
					mob.tell(showNumber+". Assigned Spell Name: '"+E.readableText()+"'.");
				else
				if(E instanceof Key)
				{
					mob.tell(showNumber+". Assigned Key Code: '"+E.readableText()+"'.");
					ok=true;
				}
				else
				if(E instanceof com.planet_ink.coffee_mud.interfaces.Map)
				{
					mob.tell(showNumber+". Assigned Map Area(s): '"+E.readableText()+"'.");
					ok=true;
				}
				else
				if(E instanceof Light)
				{
					mob.tell(showNumber+". Light duration (before burn out): '"+Util.s_int(E.readableText())+"'.");
					ok=true;
				}
				else
				{
					mob.tell(showNumber+". Assigned Read Text: '"+E.readableText()+"'.");
					ok=true;
				}

				if((showFlag!=showNumber)&&(showFlag>-999)) return;
				String newName=null;

				if((E instanceof Wand)
				||((E instanceof SpellHolder)&&(!(CMClass.className(E).endsWith("SuperPill")))))
				{
					newName=mob.session().prompt("Enter something new (?)\n\r:","");
					if(newName.length()==0)
						ok=true;
					else
					{
						if(newName.equalsIgnoreCase("?"))
							mob.tell(CMLister.reallyList(CMClass.abilities(),-1).toString());
						else
						if(E instanceof Wand)
						{
							Ability chosenOne=chosenOne=CMClass.getAbility(newName);
							if(chosenOne!=null)
								ok=true;
							else
								mob.tell("'"+newName+"' is not recognized.  Try '?'.");
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
								Ability chosenOne=chosenOne=CMClass.getAbility(spellName);
								if(chosenOne!=null)
									ok=true;
								else
								{
									mob.tell("'"+spellName+"' is not recognized.  Try '?'.");
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
					newName=mob.session().prompt("Enter something new\n\r:","");

				if(ok)
				{
					if(newName.length()>0)
						E.setReadableText(newName);
					else
						mob.tell("(no change)");
				}
			}
		}
		else
		if(E instanceof Drink)
		{
			mob.session().println(showNumber+". Current liquid type: "+EnvResource.RESOURCE_DESCS[((Drink)E).liquidType()&EnvResource.RESOURCE_MASK]);
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			boolean q=false;
			while(!q)
			{
				String newType=mob.session().prompt("Enter a new type (?)\n\r:",EnvResource.RESOURCE_DESCS[((Drink)E).liquidType()&EnvResource.RESOURCE_MASK]);
				if(newType.equals("?"))
				{
					StringBuffer say=new StringBuffer("");
					for(int i=0;i<EnvResource.RESOURCE_DESCS.length-1;i++)
						if((EnvResource.RESOURCE_DATA[i][0]&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_LIQUID)
							say.append(EnvResource.RESOURCE_DESCS[i]+", ");
					mob.tell(say.toString().substring(0,say.length()-2));
					q=false;
				}
				else
				{
					q=true;
					int newValue=-1;
					for(int i=0;i<EnvResource.RESOURCE_DESCS.length-1;i++)
						if((EnvResource.RESOURCE_DATA[i][0]&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_LIQUID)
							if(newType.equalsIgnoreCase(EnvResource.RESOURCE_DESCS[i]))
								newValue=EnvResource.RESOURCE_DATA[i][0];
					if(newValue>=0)
						((Drink)E).setLiquidType(newValue);
					else
						mob.tell("(no change)");
				}
			}
		}
	}
	
	public static void genRecipe(MOB mob, Recipe E, int showNumber, int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String prompt="Recipe Data for";
		mob.tell(showNumber+". "+prompt+": "+E.getCommonSkillID()+".");
		mob.tell(Util.padRight(" ",(""+showNumber).length()+2+prompt.length())+": "+Util.replaceAll(E.getRecipeCodeLine(),"\t",",")+".");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		while(!mob.session().killFlag())
		{
			String newName=mob.session().prompt("Enter new skill id (?)\n\r:","");
			if(newName.equalsIgnoreCase("?"))
			{
			    StringBuffer str=new StringBuffer("");
			    Ability A=null;
				for(Enumeration e=CMClass.abilities();e.hasMoreElements();)
				{
				 	A=(Ability)e.nextElement();
				 	if(((A.classificationCode()&Ability.ALL_CODES)==Ability.COMMON_SKILL)
				 	&&(Util.bset(A.flags(),Ability.FLAG_CRAFTING)))
				 	    str.append(A.ID()+"\n\r");
				}
				mob.tell("\n\rCommon Skills:\n\r"+str.toString()+"\n\r");
			}
			else
			if((newName.length()>0)
			&&(CMClass.getAbility(newName)!=null)
			&&(CMClass.getAbility(newName).classificationCode()==Ability.COMMON_SKILL))
			{
			    E.setCommonSkillID(CMClass.getAbility(newName).ID());
			    break;
			}
			else
			if(newName.length()>0)
			    mob.tell("'"+newName+"' is not a valid common skill.  Try ?.");
			else
			{
				mob.tell("(no change)");
				break;
			}
		}
		String newName=mob.session().prompt("Enter new data line\n\r:","");
		if(newName.length()>0)
			E.setRecipeCodeLine(Util.replaceAll(newName,",","\t"));
		else
			mob.tell("(no change)");
	}

	public static void genGettable(MOB mob, Item E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		if(E instanceof Potion)
			((Potion)E).setDrunk(false);

		String c="Q";
		while(!c.equals("\n"))
		{
			mob.session().println(showNumber+". A) Is Gettable   : "+(!Util.bset(E.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNOTGET)));
			mob.session().println("    B) Is Droppable  : "+(!Util.bset(E.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNODROP)));
			mob.session().println("    C) Is Removable  : "+(!Util.bset(E.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNOREMOVE)));
			mob.session().println("    D) Non-Locatable : "+(((E.baseEnvStats().sensesMask()&EnvStats.SENSE_UNLOCATABLE)>0)?"true":"false"));
			if(E instanceof Weapon)
				mob.session().println("    E) Is Two-Handed : "+E.rawLogicalAnd());
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			c=mob.session().choose("Enter one to change, or ENTER when done: ","ABCDE\n","\n").toUpperCase();
			switch(Character.toUpperCase(c.charAt(0)))
			{
			case 'A': Sense.setGettable(E,(Util.bset(E.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNOTGET))); break;
			case 'B': Sense.setDroppable(E,(Util.bset(E.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNODROP))); break;
			case 'C': Sense.setRemovable(E,(Util.bset(E.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNOREMOVE))); break;
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
			StringBuffer buf=new StringBuffer(showNumber+". Dispositions: ");
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
		while(!c.equals("\n"))
		{
			char letter='A';
			String letters="";
			for(int i=0;i<disps.length;i++)
			{
				int mask=disps[i];
				for(int num=0;num<EnvStats.dispositionsDesc.length;num++)
					if(mask==Util.pow(2,num))
					{
						mob.session().println("    "+letter+") "+Util.padRight(EnvStats.dispositionsDesc[num],20)+":"+((E.disposition()&mask)!=0));
						letters+=letter;
						break;
					}
				letter++;
			}
			c=mob.session().choose("Enter one to change, or ENTER when done: ",letters+"\n","\n").toUpperCase();
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
			prompt=Util.padRight(prompt,35);
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
		while(!c.equals("\n"))
		{
			mob.session().println(""+showNumber+". Climate:");
			mob.session().println("    R) Wet and Rainy    : "+((A.climateType()&Area.CLIMASK_WET)>0));
			mob.session().println("    H) Excessively hot  : "+((A.climateType()&Area.CLIMASK_HOT)>0));
			mob.session().println("    C) Excessively cold : "+((A.climateType()&Area.CLIMASK_COLD)>0));
			mob.session().println("    W) Very windy       : "+((A.climateType()&Area.CLIMATE_WINDY)>0));
			mob.session().println("    D) Very dry         : "+((A.climateType()&Area.CLIMASK_DRY)>0));
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			c=mob.session().choose("Enter one to change, or ENTER when done: ","RHCWD\n","\n").toUpperCase();
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

	public static void genCharStats(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		if((showFlag!=showNumber)&&(showFlag>-999))
		{
			StringBuffer buf=new StringBuffer(showNumber+". Stats: ");
			for(int i=0;i<CharStats.NUM_BASE_STATS;i++)
				buf.append(CharStats.TRAITABBR1[i]+":"+E.baseCharStats().getStat(i)+" ");
			mob.tell(buf.toString());
			return;
		}
		String c="Q";
		String commandStr="ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%^&*()=+-";
		while(!c.equals("\n"))
		{
			for(int i=0;i<CharStats.TRAITS.length;i++)
				if(i!=CharStats.GENDER)
					mob.session().println("    "+commandStr.charAt(i)+") "+Util.padRight(CharStats.TRAITS[i],20)+":"+((E.baseCharStats().getStat(i))));
			c=mob.session().choose("Enter one to change, or ENTER when done: ",commandStr.substring(0,CharStats.TRAITS.length)+"\n","\n").toUpperCase();
			int num=commandStr.indexOf(c);
			if(num>=0)
			{
				String newVal=mob.session().prompt("Enter new value for "+CharStats.TRAITS[num]+" ("+E.baseCharStats().getStat(num)+"): ","");
				if(((Util.s_int(newVal)>0)||(newVal.trim().equals("0")))
				&&(num!=CharStats.GENDER))
				{
					E.baseCharStats().setStat(num,Util.s_int(newVal));
					if((num==CharStats.AGE)&&(E.playerStats()!=null)&&(E.playerStats().getBirthday()!=null))
					    E.playerStats().getBirthday()[2]=DefaultTimeClock.globalClock.getYear()-Util.s_int(newVal);
				}
				else
					mob.tell("(no change)");
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
			StringBuffer buf=new StringBuffer(showNumber+". Senses: ");
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
		while(!c.equals("\n"))
		{
			char letter='A';
			String letters="";
			for(int i=0;i<senses.length;i++)
			{
				int mask=senses[i];
				for(int num=0;num<EnvStats.sensesDesc.length;num++)
					if(mask==Util.pow(2,num))
					{
						letters+=letter;
						mob.session().println("    "+letter+") "+Util.padRight(EnvStats.sensesDesc[num],20)+":"+((E.sensesMask()&mask)!=0));
						break;
					}
				letter++;
			}
			c=mob.session().choose("Enter one to change, or ENTER when done: ",letters+"\n","\n").toUpperCase();
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
			mob.tell(showNumber+". Has a door: "+E.hasADoor()
					+"\n\r   Has a lock: "+E.hasALock()
					+"\n\r   Open ticks: "+E.openDelayTicks());
			return;
		}

		if(genGenericPrompt(mob,"Has a door",E.hasADoor()))
		{
			HasDoor=true;
			DefaultsClosed=genGenericPrompt(mob,"Defaults closed",E.defaultsClosed());
			Open=!DefaultsClosed;
			if(genGenericPrompt(mob,"Has a lock",E.hasALock()))
			{
				HasLock=true;
				DefaultsLocked=genGenericPrompt(mob,"Defaults locked",E.defaultsLocked());
				Locked=DefaultsLocked;
			}
			else
			{
				HasLock=false;
				Locked=false;
				DefaultsLocked=false;
			}
			mob.tell("\n\rReset Delay (# ticks): '"+E.openDelayTicks()+"'.");
			int newLevel=Util.s_int(mob.session().prompt("Enter a new delay\n\r:",""));
			if(newLevel>0)
				E.setOpenDelayTicks(newLevel);
			else
				mob.tell("(no change)");
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
				if(Util.isSet((int)E.containTypes(),i))
					canContain+=", "+Container.CONTAIN_DESCS[i+1];
		}
		return canContain.substring(2);
	}


	public static void genLidsNLocks(MOB mob, Container E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		if((showFlag!=showNumber)&&(showFlag>-999)){
			mob.tell(showNumber+". Can contain : "+makeContainerTypes(E)
					+"\n\r   Has a lid   : "+E.hasALid()
					+"\n\r   Has a lock  : "+E.hasALock());
			return;
		}
		String change="NO";
		while(change.length()>0)
		{
			mob.tell("\n\rCan only contain: "+makeContainerTypes(E));
			change=mob.session().prompt("Enter a type to add/remove (?)\n\r:","");
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
					mob.tell("Unknown type.  Try '?'.");
				else
				if(found==0)
					E.setContainTypes(0);
				else
				if(Util.isSet((int)E.containTypes(),found-1))
					E.setContainTypes(E.containTypes()-Util.pow(2,found-1));
				else
					E.setContainTypes(E.containTypes()|Util.pow(2,found-1));
			}
		}

		if(genGenericPrompt(mob,"Has a lid ",E.hasALid()))
		{
			E.setLidsNLocks(true,false,E.hasALock(),E.isLocked());
			if(genGenericPrompt(mob,"Has a lock",E.hasALock()))
			{
				E.setLidsNLocks(E.hasALid(),E.isOpen(),true,true);
				mob.tell("\n\rText: '"+E.keyName()+"'.");
				String newName=mob.session().prompt("Enter something new\n\r:","");
				if(newName.length()>0)
					E.setKeyName(newName);
				else
					mob.tell("(no change)");
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
		mob.tell(showNumber+". Level: '"+E.baseEnvStats().level()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.baseEnvStats().setLevel(getNumericData(mob,"Enter a new level\n\r:",E.baseEnvStats().level()));
	}

	public static void genRejuv(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		if(E instanceof Item)
			mob.tell(showNumber+". Rejuv/Pct: '"+E.baseEnvStats().rejuv()+"' (0=special).");
		else
			mob.tell(showNumber+". Rejuv Ticks: '"+E.baseEnvStats().rejuv()+"' (0=never).");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String rlevel=mob.session().prompt("Enter new amount\n\r:","");
		int newLevel=Util.s_int(rlevel);
		if((newLevel>0)||(rlevel.trim().equals("0")))
		{
			E.baseEnvStats().setRejuv(newLevel);
			if((E.baseEnvStats().rejuv()==0)&&(E instanceof MOB))
			{
				E.baseEnvStats().setRejuv(Integer.MAX_VALUE);
				mob.tell(E.Name()+" will now never rejuvinate.");
			}
		}
		else
			mob.tell("(no change)");
	}

	public static void genUses(MOB mob, Item E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Uses Remaining: '"+E.usesRemaining()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.setUsesRemaining(getNumericData(mob,"Enter a new value\n\r:",E.usesRemaining()));
	}

	public static void genMaxUses(MOB mob, Wand E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Maximum Uses: '"+E.maxUses() +"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.setMaxUses(getNumericData(mob,"Enter a new value\n\r:",E.maxUses()));
	}

	public static void genCondition(MOB mob, Item E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Condition: '"+E.usesRemaining()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.setUsesRemaining(getNumericData(mob,"Enter a new value\n\r:",E.usesRemaining()));
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
		if(E instanceof com.planet_ink.coffee_mud.interfaces.Map)
			modifyGenMap(mob,(com.planet_ink.coffee_mud.interfaces.Map)E);
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
		int numValue=Util.s_int(value);
		if((numValue==0)&&(!value.trim().equals("0")))
		{
			mob.tell("(no change)");
			return oldValue;
		}
		return numValue;
	}

	public static long getLongData(MOB mob, String prompt, long oldValue)
	throws IOException
	{
		String value=mob.session().prompt(prompt,"");
		long numValue=Util.s_long(value);
		if((numValue==0)&&(!value.trim().equals("0")))
		{
			mob.tell("(no change)");
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
			mob.tell("(no change)");
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
		double numValue=Util.s_double(value);
		if((numValue==0.0)&&(!value.trim().equals("0")))
		{
			mob.tell("(no change)");
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
			mob.tell(showNumber+". Misc Text: '"+E.text()+"'.");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			String newText=mob.session().prompt("Re-enter now (null=blank)\n\r:","");
			if(newText.equalsIgnoreCase("NULL"))
				E.setMiscText("");
			else
			if(newText.length()>0)
				E.setMiscText(newText);
			else
				mob.tell("(no change)");
		}

	}

	public static void genTitleRoom(MOB mob, LandTitle E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Land plot ID: '"+E.landPropertyID()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newText="?!?!";
		while((newText.length()>0)&&(CMMap.getRoom(newText)==null))
		{
			newText=mob.session().prompt("New Property ID:","");
			if((newText.length()==0)
			&&(CMMap.getRoom(newText)==null)
			&&(CMMap.getArea(newText)==null))
				mob.tell("That property (room ID) doesn't exist!");
		}
		if(newText.length()>0)
			E.setLandPropertyID(newText);
		else
			mob.tell("(no change)");

	}

	public static void genAbility(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Magical Ability: '"+E.baseEnvStats().ability()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.baseEnvStats().setAbility(getNumericData(mob,"Enter a new value (0=no magic)\n\r:",E.baseEnvStats().ability()));
	}

	public static void genCoinStuff(MOB mob, Coins E, int showNumber, int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Money data: '"+E.getNumberOfCoins()+" x "+BeanCounter.getDenominationName(E.getCurrency(),E.getDenomination())+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		boolean gocontinue=true;
		while(gocontinue)
		{
		    gocontinue=false;
		    String oldCurrency=E.getCurrency();
		    if(oldCurrency.length()==0) oldCurrency="Default";
			oldCurrency=mob.session().prompt("Enter currency code (?):",oldCurrency).trim().toUpperCase();
			if(oldCurrency.equalsIgnoreCase("Default"))
			{
			    if(E.getCurrency().length()>0)
				    E.setCurrency("");
			    else
			        mob.tell("(no change)");
			}
			else
			if((oldCurrency.length()==0)||(oldCurrency.equalsIgnoreCase(E.getCurrency())))
			    mob.tell("(no change)");
			else
			if(!BeanCounter.getAllCurrencies().contains(oldCurrency))
			{
			    Vector V=BeanCounter.getAllCurrencies();
			    for(int v=0;v<V.size();v++)
			        if(((String)V.elementAt(v)).length()==0)
			            V.setElementAt("Default",v);
			    mob.tell("'"+oldCurrency+"' is not recognized.  Try: "+Util.toStringList(V)+".");
			    gocontinue=true;
			}
			else
			    E.setCurrency(oldCurrency.toUpperCase().trim());
		}
		gocontinue=true;
		while(gocontinue)
		{
		    gocontinue=false;
		    String newDenom=mob.session().prompt("Enter denomination (?):",""+E.getDenomination()).trim().toUpperCase();
			DVector DV=BeanCounter.getCurrencySet(E.getCurrency());
			if((newDenom.length()>0)
			&&(!Util.isDouble(newDenom))
			&&(!newDenom.equalsIgnoreCase("?")))
			{
			    double denom=EnglishParser.matchAnyDenomination(E.getCurrency(),newDenom);
			    if(denom>0.0) newDenom=""+denom;
			}
		    if((newDenom.length()==0)
		    ||(Util.isDouble(newDenom)
	            &&(!newDenom.equalsIgnoreCase("?"))
	            &&(Util.s_double(newDenom)==E.getDenomination())))
			        mob.tell("(no change)");
		    else
			if((!Util.isDouble(newDenom))
			||(newDenom.equalsIgnoreCase("?"))
			||((DV!=null)&&(!DV.contains(new Double(Util.s_double(newDenom))))))
			{
			    StringBuffer allDenoms=new StringBuffer("");
			    for(int i=0;i<DV.size();i++)
			        allDenoms.append(((Double)DV.elementAt(i,1)).doubleValue()+"("+((String)DV.elementAt(i,2))+"), ");
			    if(allDenoms.toString().endsWith(", "))
			        allDenoms=new StringBuffer(allDenoms.substring(0,allDenoms.length()-2));
			    mob.tell("'"+newDenom+"' is not a defined denomination. Try one of these: "+allDenoms.toString()+".");
			    gocontinue=true;
			}
			else
			    E.setDenomination(Util.s_double(newDenom));
		}
		E.setNumberOfCoins(getLongData(mob,"Enter stack size\n\r:",E.getNumberOfCoins()));
	}

	public static void genHitPoints(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Hit Points/Level Modifier (hp=((level*level) + (random*level*THIS))) : '"+E.baseEnvStats().ability()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newLevelStr=mob.session().prompt("Enter a new value\n\r:","");
		int newLevel=Util.s_int(newLevelStr);
		if((newLevel!=0)||(newLevelStr.equals("0")))
			E.baseEnvStats().setAbility(newLevel);
		else
			mob.tell("(no change)");
	}

	public static void genValue(MOB mob, Item E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Base Value: '"+E.baseGoldValue()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.setBaseValue(getNumericData(mob,"Enter a new value\n\r:",E.baseGoldValue()));
	}

	public static void genWeight(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Weight: '"+E.baseEnvStats().weight()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.baseEnvStats().setWeight(getNumericData(mob,"Enter a new weight\n\r:",E.baseEnvStats().weight()));
	}


	public static void genClanItem(MOB mob, ClanItem E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Clan: '"+E.clanID()+"', Type: "+ClanItem.CI_DESC[E.ciType()]+".");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String clanID=E.clanID();
		E.setClanID(mob.session().prompt("Enter a new clan\n\r:",clanID));
		if(E.clanID().equals(clanID))
			mob.tell("(no change)");
		String clanType=ClanItem.CI_DESC[E.ciType()];
		String s="?";
		while(s.equals("?"))
		{
			s=mob.session().prompt("Enter a new type (?)\n\r:",clanType);
			if(s.equalsIgnoreCase("?"))
				mob.tell("Types: "+Util.toStringList(ClanItem.CI_DESC));
			else
			if(s.equalsIgnoreCase(clanType))
			{
				mob.tell("(no change)");
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
					mob.tell("'"+s+"' is unknown.  Try '?'");
					s="?";
				}
			}
		}
	}

	public static void genHeight(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Height: '"+E.baseEnvStats().height()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.baseEnvStats().setHeight(getNumericData(mob,"Enter a new height\n\r:",E.baseEnvStats().height()));
	}


	public static void genSize(MOB mob, Armor E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Size: '"+E.baseEnvStats().height()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.baseEnvStats().setHeight(getNumericData(mob,"Enter a new size\n\r:",E.baseEnvStats().height()));
	}


	public static void genCapacity(MOB mob, Container E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Capacity: '"+E.capacity()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.setCapacity(getNumericData(mob,"Enter a new capacity\n\r:",E.capacity()));
	}

	public static void genAttack(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". AttackAdjustment: '"+E.baseEnvStats().attackAdjustment()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.baseEnvStats().setAttackAdjustment(getNumericData(mob,"Enter a new value\n\r:",E.baseEnvStats().attackAdjustment()));
	}

	public static void genDamage(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Damage/Hit: '"+E.baseEnvStats().damage()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.baseEnvStats().setDamage(getNumericData(mob,"Enter a new value\n\r:",E.baseEnvStats().damage()));
	}

	public static void genBanker1(MOB mob, Banker E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Coin Interest: '"+E.getCoinInterest()+"'% per real day.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.setCoinInterest(getDoubleData(mob,"Enter a new value\n\r:",E.getCoinInterest()));
	}
	public static void genBanker2(MOB mob, Banker E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Item Interest: '"+E.getItemInterest()+"'% per real day.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.setItemInterest(getDoubleData(mob,"Enter a new value\n\r:",E.getItemInterest()));
	}
	public static void genBanker3(MOB mob, Banker E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Bank Chain   : '"+E.bankChain()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt("Enter a new chain\n\r:","");
		if(newValue.length()>0)
			E.setBankChain(newValue);
		else
			mob.tell("(no change)");
	}

	public static void genSpeed(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Attacks/Round: '"+((int)Math.round(E.baseEnvStats().speed()))+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.baseEnvStats().setSpeed(getNumericData(mob,"Enter a new value\n\r:",(int)Math.round(E.baseEnvStats().speed())));
	}

	public static void genArmor(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		if(E instanceof MOB)
			mob.tell(showNumber+". Armor (lower-better): '"+E.baseEnvStats().armor()+"'.");
		else
			mob.tell(showNumber+". Armor (higher-better): '"+E.baseEnvStats().armor()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.baseEnvStats().setArmor(getNumericData(mob,"Enter a new value\n\r:",E.baseEnvStats().armor()));
	}

	public static void genMoney(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Money: '"+BeanCounter.getMoney(E)+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		BeanCounter.setMoney(E,getNumericData(mob,"Enter a new value\n\r:",BeanCounter.getMoney(E)));
	}

	public static void genWeaponAmmo(MOB mob, Weapon E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String defaultAmmo=(E.requiresAmmunition())?"Y":"N";
		if((showFlag!=showNumber)&&(showFlag>-999))
		{
			mob.tell(showNumber+". Ammo required: "+(E.requiresAmmunition()?E.ammunitionType():"NO"));
			return;
		}

		if(mob.session().confirm("Does this weapon require ammunition (default="+defaultAmmo+") (Y/N)?",defaultAmmo))
		{
			mob.tell("\n\rAmmo type: '"+E.ammunitionType()+"'.");
			String newName=mob.session().prompt("Enter a new one\n\r:","");
			if(newName.length()>0)
			{
				E.setAmmunitionType(newName);
				mob.tell("(Remember to create a readable GenItem with '"+E.ammunitionType()+"' in the secret identity, and the uses remaining above 0!");
			}
			else
				mob.tell("(no change)");
			mob.tell("\n\rAmmo capacity: '"+E.ammunitionCapacity()+"'.");
			int newValue=Util.s_int(mob.session().prompt("Enter a new value\n\r:",""));
			if(newValue>0)
				E.setAmmoCapacity(newValue);
			else
				mob.tell("(no change)");
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
		mob.tell(showNumber+". Minimum/Maximum Ranges: "+Math.round(E.minRange())+"/"+Math.round(E.maxRange())+".");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newMinStr=mob.session().prompt("Enter a new minimum range\n\r:","");
		String newMaxStr=mob.session().prompt("Enter a new maximum range\n\r:","");
		if((newMinStr.length()==0)&&(newMaxStr.length()==0))
			mob.tell("(no change)");
		else
		{
			E.setRanges(Util.s_int(newMinStr),Util.s_int(newMaxStr));
			if((E.minRange()>E.maxRange())||(E.minRange()<0)||(E.maxRange()<0))
			{
				mob.tell("(defective entries.  resetting.)");
				E.setRanges(0,0);
			}
		}
	}

	public static void genWeaponType(MOB mob, Weapon E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Weapon Attack Type: '"+Weapon.typeDescription[E.weaponType()]+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		boolean q=false;
		String sel="NSPBFMR";
		while(!q)
		{
			String newType=mob.session().choose("Enter a new value (?)\n\r:",sel+"?","");
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
					mob.tell("(no change)");
			}
		}
	}

	public static void genTechLevel(MOB mob, Area A, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Theme setting: '"+Area.THEME_DESCS[A.getTechLevel()]+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		boolean q=false;
		while(!q)
		{
			String newType=mob.session().prompt("Enter a new level (?)\n\r:",Area.THEME_DESCS[A.getTechLevel()]);
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
				if(Util.s_int(newType)>0)
				    newValue=Util.s_int(newType);
				else
				for(int i=0;i<Area.THEME_DESCS.length;i++)
					if(Area.THEME_DESCS[i].toUpperCase().startsWith(newType.toUpperCase()))
						newValue=i;
				if(newValue>=0)
					A.setTechLevel(newValue);
				else
					mob.tell("(no change)");
			}
		}
	}


	public static void genMaterialCode(MOB mob, Item E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Material Type: '"+EnvResource.RESOURCE_DESCS[E.material()&EnvResource.RESOURCE_MASK]+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		boolean q=false;
		while(!q)
		{
			String newType=mob.session().prompt("Enter a new material (?)\n\r:",EnvResource.RESOURCE_DESCS[E.material()&EnvResource.RESOURCE_MASK]);
			if(newType.equals("?"))
			{
				StringBuffer say=new StringBuffer("");
				for(int i=0;i<EnvResource.RESOURCE_DESCS.length-1;i++)
					say.append(EnvResource.RESOURCE_DESCS[i]+", ");
				mob.tell(say.toString().substring(0,say.length()-2));
				q=false;
			}
			else
			{
				q=true;
				int newValue=-1;
				for(int i=0;i<EnvResource.RESOURCE_DESCS.length-1;i++)
					if(newType.equalsIgnoreCase(EnvResource.RESOURCE_DESCS[i]))
						newValue=EnvResource.RESOURCE_DATA[i][0];
				if(newValue>=0)
					E.setMaterial(newValue);
				else
					mob.tell("(no change)");
			}
		}
	}

	public static void genInstrumentType(MOB mob, MusicalInstrument E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Instrument Type: '"+MusicalInstrument.TYPE_DESC[E.instrumentType()]+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		boolean q=false;
		while(!q)
		{
			String newType=mob.session().prompt("Enter a new type (?)\n\r:",MusicalInstrument.TYPE_DESC[E.instrumentType()]);
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
					mob.tell("(no change)");
			}
		}
	}

    public static void genSpecialFaction(MOB mob, MOB E, int showNumber, int showFlag, Faction F)
    throws IOException
    {
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		if(F==null) return;
		Faction.FactionRange myFR=Factions.getRange(F.ID,E.fetchFaction(F.ID));
		mob.tell(showNumber+". "+F.name+": "+((myFR!=null)?myFR.Name:"UNDEFINED")+" ("+E.fetchFaction(F.ID)+")");
	    if((showFlag!=showNumber)&&(showFlag>-999)) return;
	    if(F.ranges!=null)
	    for(int v=0;v<F.ranges.size();v++)
	    {
	        Faction.FactionRange FR=(Faction.FactionRange)F.ranges.elementAt(v);
	        mob.tell(Util.padRight(FR.Name,20)+": "+FR.low+" - "+FR.high+")");
	    }
		String newOne=mob.session().prompt("Enter a new value: ");
		if(Util.isInteger(newOne))
		{
		    E.addFaction(F.ID,Util.s_int(newOne));
	        return;
		}
	    for(int v=0;v<F.ranges.size();v++)
	    {
	        Faction.FactionRange FR=(Faction.FactionRange)F.ranges.elementAt(v);
	        if(FR.Name.toUpperCase().startsWith(newOne.toUpperCase()))
	        {
	            if(FR.low==F.lowest) 
	                E.addFaction(F.ID,FR.low);
	            else
	            if(FR.high==F.highest) 
	                E.addFaction(F.ID,FR.high);
	            else
	                E.addFaction(F.ID,FR.low+((FR.high-FR.low)/2));
	            return;
	        }
	    }
	    mob.tell("(no change)");
    }
    public static void genFaction(MOB mob, MOB E, int showNumber, int showFlag)
    throws IOException
    {
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String newFact="Q";
		while(newFact.length()>0)
		{
		    mob.tell(showNumber+". Factions: "+E.getFactionListing());
		    if((showFlag!=showNumber)&&(showFlag>-999)) return;
		    newFact=mob.session().prompt("Enter a faction name to add or remove\n\r:","");
		    if(newFact.length()>0)
		    {
		        Faction lookedUp=Factions.getFactionByName(newFact);
		        if(lookedUp==null) Factions.getFaction(newFact);
		        if(lookedUp!=null)
		        {
		            if (E.fetchFaction(lookedUp.ID)!=Integer.MAX_VALUE)
		            {
		                // this mob already has this faction, they must want it removed
		                E.removeFaction(lookedUp.ID);
		                mob.tell("Faction '" + lookedUp.name + "' removed.");
		            }
		            else
		            {
						int value =new Integer(mob.session().prompt("How much faction ("+lookedUp.findDefault(E)+")?",
						           new Integer(lookedUp.findDefault(E)).toString())).intValue();
			            if(value<lookedUp.minimum) value=lookedUp.minimum;
					    if(value>lookedUp.maximum) value=lookedUp.maximum;
		                E.addFaction(lookedUp.ID,value);
		                mob.tell("Faction '" + lookedUp.name + "' added.");
		            }
		         }
		         else
		            mob.tell("'"+newFact+"' is not recognized as a valid faction name or file.");
		    }
		}
	}
    
	public static void genGender(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Gender: '"+Character.toUpperCase((char)E.baseCharStats().getStat(CharStats.GENDER))+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newType=mob.session().choose("Enter a new gender (M/F/N)\n\r:","MFN","");
		int newValue=-1;
		if(newType.length()>0)
			newValue=("MFN").indexOf(newType.trim().toUpperCase());
		if(newValue>=0)
		{
			switch(newValue)
			{
			case 0:
				E.baseCharStats().setStat(CharStats.GENDER,'M');
				break;
			case 1:
				E.baseCharStats().setStat(CharStats.GENDER,'F');
				break;
			case 2:
				E.baseCharStats().setStat(CharStats.GENDER,'N');
				break;
			}
		}
		else
			mob.tell("(no change)");
	}

	public static void genWeaponClassification(MOB mob, Weapon E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Weapon Classification: '"+Weapon.classifictionDescription[E.weaponClassification()]+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		boolean q=false;
		String sel=("ABEFHKPRSDTN");
		while(!q)
		{
			String newType=mob.session().choose("Enter a new value (?)\n\r:",sel+"?","");
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
					mob.tell("(no change)");
			}
		}
	}

	public static void genSecretIdentity(MOB mob, Item E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Secret Identity: '"+E.rawSecretIdentity()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt("Enter a new identity (null=blank)\n\r:","");
		if(newValue.equalsIgnoreCase("null"))
			E.setSecretIdentity("");
		else
		if(newValue.length()>0)
			E.setSecretIdentity(newValue);
		else
			mob.tell("(no change)");
	}

	public static void genNourishment(MOB mob, Food E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Nourishment/Eat: '"+E.nourishment()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		int newValue=Util.s_int(mob.session().prompt("Enter a new amount\n\r:",""));
		if(newValue>0)
			E.setNourishment(newValue);
		else
			mob.tell("(no change)");
	}

	public static void genRace(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String raceID="begin!";
		while(raceID.length()>0)
		{
			mob.tell(showNumber+". Race: '"+E.baseCharStats().getMyRace().ID()+"'.");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			raceID=mob.session().prompt("Enter a new race (?)\n\r:","").trim();
			if(raceID.equalsIgnoreCase("?"))
				mob.tell(CMLister.reallyList(CMClass.races(),-1).toString());
			else
			if(raceID.length()==0)
				mob.tell("(no change)");
			else
			{
				Race R=CMClass.getRace(raceID);
				if(R!=null)
				{
					E.baseCharStats().setMyRace(R);
					E.baseCharStats().getMyRace().startRacing(E,false);
					E.baseCharStats().getMyRace().setHeightWeight(E.baseEnvStats(),(char)E.baseCharStats().getStat(CharStats.GENDER));
				}
				else
					mob.tell("Unknown race! Try '?'.");
			}
		}
	}

	public static void genCharClass(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String classID="begin!";
		while(classID.length()>0)
		{
			StringBuffer str=new StringBuffer("");
			for(int c=0;c<E.baseCharStats().numClasses();c++)
			{
				CharClass C=E.baseCharStats().getMyClass(c);
				str.append(C.ID()+"("+E.baseCharStats().getClassLevel(C)+") ");
			}
			mob.tell(showNumber+". Class: '"+str.toString()+"'.");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			classID=mob.session().prompt("Enter a class to add/remove(?)\n\r:","").trim();
			if(classID.equalsIgnoreCase("?"))
				mob.tell(CMLister.reallyList(CMClass.charClasses(),-1).toString());
			else
			if(classID.length()==0)
				mob.tell("(no change)");
			else
			{
				CharClass C=CMClass.getCharClass(classID);
				if(C!=null)
				{
					if(E.baseCharStats().getClassLevel(C)>=0)
					{
						if(E.baseCharStats().numClasses()<2)
							mob.tell("Final class may not be removed.  To change a class, add the new one first.");
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
							lvl=mob.session().prompt("Levels to give this class ("+levels+")\n\r:",""+levels).trim();
							int lvl2=Util.s_int(lvl);
							if(lvl2>levels) lvl2=levels;
							E.baseCharStats().setClassLevel(C,lvl2);
						}
						else
						{
							lvl=mob.session().prompt("Levels to siphon from "+highestC.ID()+" for this class (0)\n\r:",""+0).trim();
							int lvl2=Util.s_int(lvl);
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
					mob.tell("Unknown character class! Try '?'.");
			}
		}
	}

	public static void genTattoos(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String behave="NO";
		while(behave.length()>0)
		{
			String behaviorstr="";
			for(int b=0;b<E.numTattoos();b++)
			{
				String B=E.fetchTattoo(b);
				if(B!=null)	behaviorstr+=B+", ";
			}
			if(behaviorstr.length()>0)
				behaviorstr=behaviorstr.substring(0,behaviorstr.length()-2);
			mob.tell(showNumber+". Tattoos: '"+behaviorstr+"'.");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			behave=mob.session().prompt("Enter a tattoo to add/remove\n\r:","");
			if(behave.length()>0)
			{
				String tattoo=behave;
				if((tattoo.length()>0)
				&&(Character.isDigit(tattoo.charAt(0)))
				&&(tattoo.indexOf(" ")>0)
				&&(Util.isNumber(tattoo.substring(0,tattoo.indexOf(" ")))))
					tattoo=tattoo.substring(tattoo.indexOf(" ")+1).trim();
				if(E.fetchTattoo(tattoo)!=null)
				{
					mob.tell(tattoo.trim().toUpperCase()+" removed.");
					E.delTattoo(behave);
				}
				else
				{
					mob.tell(behave.trim().toUpperCase()+" added.");
					E.addTattoo(behave);
				}
			}
			else
				mob.tell("(no change)");
		}
	}

	public static void genTitles(MOB mob, MOB E, int showNumber, int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		if(E.playerStats()==null) return;
		String behave="NO";
		while(behave.length()>0)
		{
			String behaviorstr="";
			for(int b=0;b<E.playerStats().getTitles().size();b++)
			{
				String B=(String)E.playerStats().getTitles().elementAt(b);
				if(B!=null)	behaviorstr+=B+", ";
			}
			if(behaviorstr.length()>0)
				behaviorstr=behaviorstr.substring(0,behaviorstr.length()-2);
			mob.tell(showNumber+". Titles: '"+behaviorstr+"'.");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			behave=mob.session().prompt("Enter a title to add/remove\n\r:","");
			if(behave.length()>0)
			{
				String tattoo=behave;
				if((tattoo.length()>0)
				&&(Character.isDigit(tattoo.charAt(0)))
				&&(tattoo.indexOf(" ")>0)
				&&(Util.isNumber(tattoo.substring(0,tattoo.indexOf(" ")))))
					tattoo=tattoo.substring(tattoo.indexOf(" ")+1).trim();
				if(E.playerStats().getTitles().contains(tattoo))
				{
					mob.tell(tattoo.trim().toUpperCase()+" removed.");
					E.playerStats().getTitles().remove(tattoo);
				}
				else
				{
					mob.tell(behave.trim().toUpperCase()+" added.");
					E.playerStats().getTitles().addElement(tattoo);
				}
			}
			else
				mob.tell("(no change)");
		}
	}
	
	public static void genEducations(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String behave="NO";
		while(behave.length()>0)
		{
			String behaviorstr="";
			for(int b=0;b<E.numEducations();b++)
			{
				String B=E.fetchEducation(b);
				if(B!=null)	behaviorstr+=B+", ";
			}
			if(behaviorstr.length()>0)
				behaviorstr=behaviorstr.substring(0,behaviorstr.length()-2);
			mob.tell(showNumber+". Educations: '"+behaviorstr+"'.");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			behave=mob.session().prompt("Enter a lesson to add/remove\n\r:","");
			if(behave.length()>0)
			{
				if(E.fetchEducation(behave)!=null)
				{
					mob.tell(behave+" removed.");
					E.delEducation(behave);
				}
				else
				{
					mob.tell(behave+" added.");
					E.addEducation(behave);
				}
			}
			else
				mob.tell("(no change)");
		}
	}

	public static void genSecurity(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		PlayerStats P=E.playerStats();
		if(P==null) return;
		String behave="NO";
		while(behave.length()>0)
		{
			String behaviorstr="";
			for(int b=0;b<P.getSecurityGroups().size();b++)
			{
				String B=(String)P.getSecurityGroups().elementAt(b);
				if(B!=null)	behaviorstr+=B+", ";
			}
			if(behaviorstr.length()>0)
				behaviorstr=behaviorstr.substring(0,behaviorstr.length()-2);
			mob.tell(showNumber+". Security Groups: '"+behaviorstr+"'.");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			behave=mob.session().prompt("Enter a group to add/remove\n\r:","");
			if(behave.length()>0)
			{
				if(P.getSecurityGroups().contains(behave.trim().toUpperCase()))
				{
					P.getSecurityGroups().remove(behave.trim().toUpperCase());
					mob.tell(behave+" removed.");
				}
				else
				{
					P.getSecurityGroups().addElement(behave.trim().toUpperCase());
					mob.tell(behave+" added.");
				}
			}
			else
				mob.tell("(no change)");
		}
	}

	public static void genBehaviors(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String behave="NO";
		while(behave.length()>0)
		{
			String behaviorstr="";
			for(int b=0;b<E.numBehaviors();b++)
			{
				Behavior B=E.fetchBehavior(b);
				if((B!=null)&&(!B.isBorrowed()))
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
			mob.tell(showNumber+". Behaviors: '"+behaviorstr+"'.");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			behave=mob.session().prompt("Enter a behavior to add/remove (?)\n\r:","");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("?"))
					mob.tell(CMLister.reallyList(CMClass.behaviors(),-1).toString());
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
						mob.tell(chosenOne.ID()+" removed.");
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
							String parms=chosenOne.getParms();
							parms=mob.session().prompt("Enter any behavior parameters\n\r:",parms);
							chosenOne.setParms(parms.trim());
							if(!alreadyHasIt)
							{
								mob.tell(chosenOne.ID()+" added.");
								E.addBehavior(chosenOne);
							}
							else
								mob.tell(chosenOne.ID()+" re-added.");
						}
						else
						{
							mob.tell("'"+behave+"' is not recognized.  Try '?'.");
						}
					}
				}
			}
			else
				mob.tell("(no change)");
		}
	}

	public static void genAffects(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String behave="NO";
		while(behave.length()>0)
		{
			String affectstr="";
			for(int b=0;b<E.numEffects();b++)
			{
				Ability A=E.fetchEffect(b);
				if((A!=null)&&(!A.isBorrowed(E)))
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
			mob.tell(showNumber+". Effects: '"+affectstr+"'.");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			behave=mob.session().prompt("Enter an effect to add/remove (?)\n\r:","");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("?"))
					mob.tell(CMLister.reallyList(CMClass.abilities(),-1).toString());
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
						mob.tell(chosenOne.ID()+" removed.");
						E.delEffect(chosenOne);
					}
					else
					{
						chosenOne=CMClass.getAbility(behave);
						if(chosenOne!=null)
						{
							String parms=chosenOne.text();
							parms=mob.session().prompt("Enter any effect parameters (';' delimited!)\n\r:",parms);
							chosenOne.setMiscText(parms.trim());
							mob.tell(chosenOne.ID()+" added.");
							E.addNonUninvokableEffect(chosenOne);
						}
						else
						{
							mob.tell("'"+behave+"' is not recognized.  Try '?'.");
						}
					}
				}
			}
			else
				mob.tell("(no change)");
		}
	}

	public static void genRideable1(MOB mob, Rideable R, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Rideable Type: '"+Rideable.RIDEABLE_DESCS[R.rideBasis()]+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		boolean q=false;
		String sel="LWACBTEDG";
		while(!q)
		{
			String newType=mob.session().choose("Enter a new value (?)\n\r:",sel+"?","");
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
					mob.tell("(no change)");
			}
		}
	}
	public static void genRideable2(MOB mob, Rideable R, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Number of MOBs held: '"+R.riderCapacity()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newLevelStr=mob.session().prompt("Enter a new value: ","");
		int newLevel=Util.s_int(newLevelStr);
		if(newLevel>0)
			R.setRiderCapacity(newLevel);
		else
			mob.tell("(no change)");
	}

	public static void genShopkeeper1(MOB mob, ShopKeeper E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Shopkeeper type: '"+E.storeKeeperString()+"'.");
		StringBuffer buf=new StringBuffer("");
		StringBuffer codes=new StringBuffer("");
		String codeStr="0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		if(E instanceof Banker)
		{
			int r=ShopKeeper.DEAL_BANKER;
			char c=codeStr.charAt(r);
			codes.append(c);
			buf.append(c+") "+ShopKeeper.SOLDCODES[r]+"\n\r");
			r=ShopKeeper.DEAL_CLANBANKER;
			c=codeStr.charAt(r);
			codes.append(c);
			buf.append(c+") "+ShopKeeper.SOLDCODES[r]+"\n\r");
		}
		else
        if(E instanceof PostOffice)
        {
            int r=ShopKeeper.DEAL_POSTMAN;
            char c=codeStr.charAt(r);
            codes.append(c);
            buf.append(c+") "+ShopKeeper.SOLDCODES[r]+"\n\r");
            r=ShopKeeper.DEAL_CLANPOSTMAN;
            c=codeStr.charAt(r);
            codes.append(c);
            buf.append(c+") "+ShopKeeper.SOLDCODES[r]+"\n\r");
        }
        else
		for(int r=0;r<ShopKeeper.SOLDCODES.length;r++)
		{
			if((r!=ShopKeeper.DEAL_CLANBANKER)
            &&(r!=ShopKeeper.DEAL_BANKER)
            &&(r!=ShopKeeper.DEAL_CLANPOSTMAN)
            &&(r!=ShopKeeper.DEAL_POSTMAN))
			{
				char c=codeStr.charAt(r);
				codes.append(c);
				buf.append(c+") "+ShopKeeper.SOLDCODES[r]+"\n\r");
			}
		}
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newType=mob.session().choose(buf.toString()+"Enter a new value\n\r:",codes.toString(),"");
		int newValue=-1;
		if(newType.length()>0)
			newValue=codeStr.indexOf(newType.toUpperCase());
		if(newValue>=0)
		{
			boolean reexamine=(E.whatIsSold()!=newValue);
			E.setWhatIsSold(newValue);
            if(reexamine)
            {
                Vector V=E.getUniqueStoreInventory();
                for(int b=0;b<V.size();b++)
                    if(!E.doISellThis((Environmental)V.elementAt(b)))
                        E.delStoreInventory((Environmental)V.elementAt(b));
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
			Vector V=E.getUniqueStoreInventory();
			for(int b=0;b<V.size();b++)
			{
				Environmental E2=(Environmental)V.elementAt(b);
				if(E2.isGeneric())
					inventorystr+=E2.name()+" ("+E.numberInStock(E2)+"), ";
				else
					inventorystr+=CMClass.className(E2)+" ("+E.numberInStock(E2)+"), ";
			}
			if(inventorystr.length()>0)
				inventorystr=inventorystr.substring(0,inventorystr.length()-2);
			mob.tell(showNumber+". Inventory: '"+inventorystr+"'.");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			itemstr=mob.session().prompt("Enter something to add/remove (?)\n\r:","");
			if(itemstr.length()>0)
			{
				if(itemstr.equalsIgnoreCase("?"))
				{
					mob.tell(CMLister.reallyList(CMClass.abilities(),-1).toString());
					mob.tell(CMLister.reallyList(CMClass.armor(),-1).toString());
					mob.tell(CMLister.reallyList(CMClass.weapons(),-1).toString());
					mob.tell(CMLister.reallyList(CMClass.miscMagic(),-1).toString());
					mob.tell(CMLister.reallyList(CMClass.miscTech(),-1).toString());
					mob.tell(CMLister.reallyList(CMClass.clanItems(),-1).toString());
					mob.tell(CMLister.reallyList(CMClass.items(),-1).toString());
					mob.tell(CMLister.reallyList(CMClass.mobTypes(),-1).toString());
					mob.tell("* Plus! Any items on the ground.");
					mob.tell("* Plus! Any mobs hanging around in the room.");
				}
				else
				{
					Environmental item=E.getStock(itemstr,null);
					if(item!=null)
					{
						mob.tell(item.ID()+" removed.");
						E.delStoreInventory(item.copyOf());
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
							item=item.copyOf();
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
								mob.tell("The shopkeeper does not sell that.");
							}
							else
							{
								boolean alreadyHasIt=false;

								if(E.doIHaveThisInStock(item.Name(),null))
								   alreadyHasIt=true;

								if(!alreadyHasIt)
								{
									mob.tell(item.ID()+" added.");
									int num=1;
									if(!(item instanceof Ability))
										num=Util.s_int(mob.session().prompt("How many? :",""));
									int price=Util.s_int(mob.session().prompt("At what price? :",""));
									E.addStoreInventory(item,num,price);
								}
							}
						}
						else
						{
							mob.tell("'"+itemstr+"' is not recognized.  Try '?'.");
						}
					}
				}
			}
			else
				mob.tell("(no change)");
		}
	}
	public static void genShopkeeper3(MOB mob, ShopKeeper E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Prejudice: '"+E.prejudiceFactors()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt("Enter a new string (null=default)\n\r:","");
		if(newValue.equalsIgnoreCase("null"))
			E.setPrejudiceFactors("");
		else
		if(newValue.length()>0)
			E.setPrejudiceFactors(newValue);
		else
			mob.tell("(no change)");
	}

	public static void genShopkeeper4(MOB mob, ShopKeeper E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Budget: '"+E.budget()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt("Enter a new string (null=default)\n\r:","");
		if(newValue.equalsIgnoreCase("null"))
			E.setBudget("");
		else
		if(newValue.length()>0)
			E.setBudget(newValue);
		else
			mob.tell("(no change)");
	}
	
	public static void genShopkeeper5(MOB mob, ShopKeeper E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Devaluation rate(s): '"+E.devalueRate()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt("Enter a new string (null=default)\n\r:","");
		if(newValue.equalsIgnoreCase("null"))
			E.setDevalueRate("");
		else
		if(newValue.length()>0)
			E.setDevalueRate(newValue);
		else
			mob.tell("(no change)");
	}
	public static void genShopkeeper6(MOB mob, ShopKeeper E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Inventory reset rate: "+E.invResetRate()+" ticks.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt("Enter a new number\n\r:","");
		if(newValue.equals("0")||(Util.s_int(newValue)!=0))
			E.setInvResetRate(Util.s_int(newValue));
		else
			mob.tell("(no change)");
	}
	
    public static void genShopkeeper7(MOB mob, ShopKeeper E, int showNumber, int showFlag)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.tell(showNumber+". Ignore Mask: '"+E.ignoreMask()+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String newValue=mob.session().prompt("Enter a new string (null=no mask)\n\r:","");
        if(newValue.equalsIgnoreCase("null"))
            E.setIgnoreMask("");
        else
        if(newValue.length()>0)
            E.setIgnoreMask(newValue);
        else
            mob.tell("(no change)");
    }

	public static void genAbilities(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String behave="NO";
		while(behave.length()>0)
		{
			String abilitiestr="";
			for(int a=0;a<E.numLearnedAbilities();a++)
			{
				Ability A=E.fetchAbility(a);
				if((A!=null)&&(!A.isBorrowed(E)))
					abilitiestr+=A.ID()+", ";
			}
			if(abilitiestr.length()>0)
				abilitiestr=abilitiestr.substring(0,abilitiestr.length()-2);
			mob.tell(showNumber+". Abilities: '"+abilitiestr+"'.");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			behave=mob.session().prompt("Enter an ability to add/remove (?)\n\r:","");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("?"))
					mob.tell(CMLister.reallyList(CMClass.abilities(),-1).toString());
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
						mob.tell(chosenOne.ID()+" removed.");
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
								mob.tell(chosenOne.ID()+" added.");
							else
								mob.tell(chosenOne.ID()+" re-added.");
							if(!alreadyHasIt)
							{
								chosenOne=(Ability)chosenOne.copyOf();
								E.addAbility(chosenOne);
								chosenOne.setProfficiency(50);
								chosenOne.autoInvocation(mob);
							}
						}
						else
						{
							mob.tell("'"+behave+"' is not recognized.  Try '?'.");
						}
					}
				}
			}
			else
				mob.tell("(no change)");
		}
	}

	public static void genDeity1(MOB mob, Deity E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Cleric Requirements: '"+E.getClericRequirements()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt("Enter new requirements\n\r:","");
		if(newValue.length()>0)
			E.setClericRequirements(newValue);
		else
			mob.tell("(no change)");
	}
	public static void genDeity2(MOB mob, Deity E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Cleric Ritual: '"+E.getClericRitual()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt("Enter new ritual\n\r:","");
		if(newValue.length()>0)
			E.setClericRitual(newValue);
		else
			mob.tell("(no change)");
	}
	public static void genDeity3(MOB mob, Deity E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Worshiper Requirements: '"+E.getWorshipRequirements()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt("Enter new requirements\n\r:","");
		if(newValue.length()>0)
			E.setWorshipRequirements(newValue);
		else
			mob.tell("(no change)");
	}
	public static void genDeity4(MOB mob, Deity E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Worshiper Ritual: '"+E.getWorshipRitual()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt("Enter new ritual\n\r:","");
		if(newValue.length()>0)
			E.setWorshipRitual(newValue);
		else
			mob.tell("(no change)");
	}
	public static void genDeity5(MOB mob, Deity E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String behave="NO";
		while(behave.length()>0)
		{
			String abilitiestr="";
			for(int a=0;a<E.numBlessings();a++)
			{
				Ability A=E.fetchBlessing(a);
				if((A!=null)&&(!A.isBorrowed(E)))
					abilitiestr+=A.ID()+", ";
			}
			if(abilitiestr.length()>0)
				abilitiestr=abilitiestr.substring(0,abilitiestr.length()-2);
			mob.tell(showNumber+". Blessings: '"+abilitiestr+"'.");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			behave=mob.session().prompt("Enter an ability to add/remove (?)\n\r:","");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("?"))
					mob.tell(CMLister.reallyList(CMClass.abilities(),-1).toString());
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
						mob.tell(chosenOne.ID()+" removed.");
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
								mob.tell(chosenOne.ID()+" added.");
							else
								mob.tell(chosenOne.ID()+" re-added.");
							if(!alreadyHasIt)
								E.addBlessing((Ability)chosenOne.copyOf());
						}
						else
						{
							mob.tell("'"+behave+"' is not recognized.  Try '?'.");
						}
					}
				}
			}
			else
				mob.tell("(no change)");
		}
	}

	public static void genDeity6(MOB mob, Deity E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String behave="NO";
		while(behave.length()>0)
		{
			String abilitiestr="";
			for(int a=0;a<E.numCurses();a++)
			{
				Ability A=E.fetchCurse(a);
				if((A!=null)&&(!A.isBorrowed(E)))
					abilitiestr+=A.ID()+", ";
			}
			if(abilitiestr.length()>0)
				abilitiestr=abilitiestr.substring(0,abilitiestr.length()-2);
			mob.tell(showNumber+". Curses: '"+abilitiestr+"'.");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			behave=mob.session().prompt("Enter an ability to add/remove (?)\n\r:","");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("?"))
					mob.tell(CMLister.reallyList(CMClass.abilities(),-1).toString());
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
						mob.tell(chosenOne.ID()+" removed.");
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
								mob.tell(chosenOne.ID()+" added.");
							else
								mob.tell(chosenOne.ID()+" re-added.");
							if(!alreadyHasIt)
								E.addCurse((Ability)chosenOne.copyOf());
						}
						else
						{
							mob.tell("'"+behave+"' is not recognized.  Try '?'.");
						}
					}
				}
			}
			else
				mob.tell("(no change)");
		}
	}

	public static void genDeity7(MOB mob, Deity E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String behave="NO";
		while(behave.length()>0)
		{
			String abilitiestr="";
			for(int a=0;a<E.numPowers();a++)
			{
				Ability A=E.fetchPower(a);
				if((A!=null)&&(!A.isBorrowed(E)))
					abilitiestr+=A.ID()+", ";
			}
			if(abilitiestr.length()>0)
				abilitiestr=abilitiestr.substring(0,abilitiestr.length()-2);
			mob.tell(showNumber+". Granted Powers: '"+abilitiestr+"'.");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			behave=mob.session().prompt("Enter an ability to add/remove (?)\n\r:","");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("?"))
					mob.tell(CMLister.reallyList(CMClass.abilities(),-1).toString());
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
						mob.tell(chosenOne.ID()+" removed.");
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
								mob.tell(chosenOne.ID()+" added.");
							else
								mob.tell(chosenOne.ID()+" re-added.");
							if(!alreadyHasIt)
								E.addPower((Ability)chosenOne.copyOf());
						}
						else
						{
							mob.tell("'"+behave+"' is not recognized.  Try '?'.");
						}
					}
				}
			}
			else
				mob.tell("(no change)");
		}
	}
	public static void genDeity8(MOB mob, Deity E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Cleric Sin: '"+E.getClericSin()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt("Enter new sin ritual\n\r:","");
		if(newValue.length()>0)
			E.setClericSin(newValue);
		else
			mob.tell("(no change)");
	}
	public static void genDeity9(MOB mob, Deity E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Worshiper Sin: '"+E.getWorshipSin()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt("Enter new sin ritual\n\r:","");
		if(newValue.length()>0)
			E.setWorshipSin(newValue);
		else
			mob.tell("(no change)");
	}
	public static void genDeity0(MOB mob, Deity E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Cleric Power Ritual: '"+E.getClericPowerup()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt("Enter new power ritual\n\r:","");
		if(newValue.length()>0)
			E.setClericPowerup(newValue);
		else
			mob.tell("(no change)");
	}
	public static void genGridLocaleX(MOB mob, GridLocale E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Size (X): '"+E.xSize()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt("Enter a new size\n\r:","");
		if(Util.s_int(newValue)>0)
			E.setXSize(Util.s_int(newValue));
		else
			mob.tell("(no change)");
	}

	public static void genGridLocaleY(MOB mob, GridLocale E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Size (Y): '"+E.ySize()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt("Enter a new size\n\r:","");
		if(Util.s_int(newValue)>0)
			E.setYSize(Util.s_int(newValue));
		else
			mob.tell("(no change)");
	}

	public static void genWornLocation(MOB mob, Item E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		if((showFlag!=showNumber)&&(showFlag>-999))
		{
			StringBuffer buf=new StringBuffer(showNumber+". ");
			if(!E.rawLogicalAnd())
				buf.append("Wear on any one of: ");
			else
				buf.append("Worn on all of: ");
			for(int l=0;l<Item.wornCodes.length;l++)
			{
				long wornCode=1<<l;
				if((Sense.wornLocation(wornCode).length()>0)
				&&(((E.rawProperLocationBitmap()&wornCode)==wornCode)))
					buf.append(Sense.wornLocation(wornCode)+" ");
			}
			mob.tell(buf.toString());
			return;
		}
		int codeVal=-1;
		while(codeVal!=0)
		{
			mob.tell("Wearing parameters\n\r0: Done");
			if(!E.rawLogicalAnd())
				mob.tell("1: Able to worn on any ONE of these locations:");
			else
				mob.tell("1: Must be worn on ALL of these locations:");
			for(int l=0;l<Item.wornCodes.length;l++)
			{
				long wornCode=1<<l;
				if(Sense.wornLocation(wornCode).length()>0)
				{
					String header=(l+2)+": ("+Sense.wornLocation(wornCode)+") : "+(((E.rawProperLocationBitmap()&wornCode)==wornCode)?"YES":"NO");
					mob.tell(header);
				}
			}
			codeVal=Util.s_int(mob.session().prompt("Select an option number above to TOGGLE\n\r:"));
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
		mob.tell(showNumber+". Quenched/Drink: '"+E.thirstQuenched()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.setThirstQuenched(getNumericData(mob,"Enter a new amount\n\r:",E.thirstQuenched()));
	}

	public static void genDrinkHeld(MOB mob, Drink E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Amount of Drink Held: '"+E.liquidHeld()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.setLiquidHeld(getNumericData(mob,"Enter a new amount\n\r:",E.liquidHeld()));
		E.setLiquidRemaining(E.liquidHeld());
	}



	static void genText(MOB mob, Race E, int showNumber, int showFlag, String FieldDisp, String Field)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". "+FieldDisp+": '"+E.getStat(Field)+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a new one\n\r:","");
		if(newName.length()>0)
			E.setStat(Field,newName);
		else
			mob.tell("(no change)");
	}
	static void genText(MOB mob, CharClass E, int showNumber, int showFlag, String FieldDisp, String Field)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". "+FieldDisp+": '"+E.getStat(Field)+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a new one\n\r:","");
		if(newName.length()>0)
			E.setStat(Field,newName);
		else
			mob.tell("(no change)");
	}
	static void genAttackAttribute(MOB mob, CharClass E, int showNumber, int showFlag, String FieldDisp, String Field)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". "+FieldDisp+": '"+CharStats.TRAITS[Util.s_int(E.getStat(Field))]+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a new one\n\r:","");
		String newStat="";
		for(int i=0;i<CharStats.NUM_BASE_STATS;i++)
			if(newName.equalsIgnoreCase(CharStats.TRAITS[i]))
				newStat=""+i;
		if(newStat.length()>0)
			E.setStat(Field,newStat);
		else
			mob.tell("(no change)");
	}
	static void genArmorCode(MOB mob, CharClass E, int showNumber, int showFlag, String FieldDisp, String Field)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". "+FieldDisp+": '"+CharClass.ARMOR_LONGDESC[Util.s_int(E.getStat(Field))]+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter ("+Util.toStringList(CharClass.ARMOR_DESCS)+")\n\r:","");
		String newStat="";
		for(int i=0;i<CharClass.ARMOR_DESCS.length;i++)
			if(newName.equalsIgnoreCase(CharClass.ARMOR_DESCS[i]))
				newStat=""+i;
		if(newStat.length()>0)
			E.setStat(Field,newStat);
		else
			mob.tell("(no change)");
	}
	static void genQualifications(MOB mob, CharClass E, int showNumber, int showFlag, String FieldDisp, String Field)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". "+FieldDisp+": '"+MUDZapper.zapperDesc(E.getStat(Field))+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName="?";
		while(newName.equals("?"))
		{
			newName=mob.session().prompt("Enter a new mask (?)\n\r:","");
			if(newName.equals("?"))
				mob.tell(MUDZapper.zapperInstructions("\n","Allows"));
		}
		if((newName.length()>0)&&(!newName.equals("?")))
			E.setStat(Field,newName);
		else
			mob.tell("(no change)");
	}
	static void genWeaponRestr(MOB mob, CharClass E, int showNumber, int showFlag, String FieldDisp, String FieldNum, String Field)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		Vector set=Util.parseCommas(E.getStat(Field),true);
		StringBuffer str=new StringBuffer("");
		for(int v=0;v<set.size();v++)
			str.append(" "+Weapon.classifictionDescription[Util.s_int((String)set.elementAt(v))].toLowerCase());

		mob.tell(showNumber+". "+FieldDisp+": '"+str.toString()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName="?";
		boolean setChanged=false;
		while(newName.equals("?"))
		{
			newName=mob.session().prompt("Enter a weapon class to add/remove (?)\n\r:","");
			if(newName.equals("?"))
				mob.tell(Util.toStringList(Weapon.classifictionDescription));
			else
			if(newName.length()>0)
			{
				int foundCode=-1;
				for(int i=0;i<Weapon.classifictionDescription.length;i++)
					if(Weapon.classifictionDescription[i].equalsIgnoreCase(newName))
						foundCode=i;
				if(foundCode<0)
				{
					mob.tell("'"+newName+"' is not recognized.  Try '?'");
					newName="?";
				}
				else
				{
					int x=set.indexOf(""+foundCode);
					if(x>=0)
					{
						setChanged=true;
						set.removeElementAt(x);
						mob.tell("'"+newName+"' removed.");
						newName="?";
					}
					else
					{
						set.addElement(""+foundCode);
						setChanged=true;
						mob.tell("'"+newName+"' added.");
						newName="?";
					}
				}
			}
		}
		if(setChanged)
			E.setStat(Field,Util.toStringList(set));
		else
			mob.tell("(no change)");
	}
	static void genInt(MOB mob, CharClass E, int showNumber, int showFlag, String FieldDisp, String Field)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". "+FieldDisp+": '"+E.getStat(Field)+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a new one\n\r:","");
		if((newName.length()>0)&&((newName.trim().equals("0"))||(Util.s_int(newName)!=0)))
			E.setStat(Field,""+Util.s_int(newName));
		else
			mob.tell("(no change)");
	}
	static void genInt(MOB mob, Race E, int showNumber, int showFlag, String FieldDisp, String Field)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". "+FieldDisp+": '"+E.getStat(Field)+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a new one\n\r:","");
		if((newName.length()>0)&&((newName.trim().equals("0"))||(Util.s_int(newName)!=0)))
			E.setStat(Field,""+Util.s_int(newName));
		else
			mob.tell("(no change)");
	}
	static void genBool(MOB mob, Race E, int showNumber, int showFlag, String FieldDisp, String Field)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". "+FieldDisp+": '"+E.getStat(Field)+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a new true/false\n\r:","");
		if((newName.length()>0)&&(newName.equalsIgnoreCase("true")||newName.equalsIgnoreCase("false")))
			E.setStat(Field,newName.toLowerCase());
		else
			mob.tell("(no change)");
	}
	static void genBool(MOB mob, CharClass E, int showNumber, int showFlag, String FieldDisp, String Field)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". "+FieldDisp+": '"+E.getStat(Field)+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a new true/false\n\r:","");
		if((newName.length()>0)&&(newName.equalsIgnoreCase("true")||newName.equalsIgnoreCase("false")))
			E.setStat(Field,newName.toLowerCase());
		else
			mob.tell("(no change)");
	}
	static void genRaceAvailability(MOB mob, Race E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Availability: '"+Area.THEME_DESCS_EXT[Util.s_int(E.getStat("AVAIL"))]+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName="?";
		while(newName.equals("?"))
		{
			newName=mob.session().prompt("Enter a new value (?)\n\r:","");
			if(newName.length()==0)
				mob.tell("(no change)");
			else
			if((Util.isNumber(newName))&&(Util.s_int(newName)<Area.THEME_DESCS_EXT.length))
				E.setStat("AVAIL",""+Util.s_int(newName));
			else
			if(newName.equalsIgnoreCase("?"))
			{
			    StringBuffer str=new StringBuffer("Valid values: \n\r");
			    for(int i=0;i<Area.THEME_DESCS_EXT.length;i++)
			        str.append(i+") "+Area.THEME_DESCS_EXT[i]+"\n\r");
			    mob.tell(str.toString());
			}
			else
				mob.tell("(no change)");
		}
	}
	static void genCat(MOB mob, Race E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Racial Category: '"+E.racialCategory()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a new one\n\r:","");
		if(newName.length()>0)
		{
			boolean found=false;
			if(newName.startsWith("new "))
			{
				newName=Util.capitalizeAndLower(newName.substring(4));
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
				StringBuffer str=new StringBuffer("That category does not exist.  Valid categories include: ");
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
			mob.tell("(no change)");
	}
	static void genHealthBuddy(MOB mob, Race E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Health Race: '"+E.getStat("HEALTHRACE")+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a new one\n\r:","");
		if(newName.length()>0)
		{
			Race R2=CMClass.getRace(newName);
			if((R2!=null)&&(R2.isGeneric()))
				R2=null;
			if(R2==null)
			{
				StringBuffer str=new StringBuffer("That race name is invalid.  Valid races include: ");
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
			mob.tell("(no change)");
	}
	static void genBodyParts(MOB mob, Race E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		StringBuffer parts=new StringBuffer("");
		for(int i=0;i<Race.BODYPARTSTR.length;i++)
			if(E.bodyMask()[i]!=0) parts.append(Race.BODYPARTSTR[i].toLowerCase()+"("+E.bodyMask()[i]+") ");
		mob.tell(showNumber+". Body Parts: "+parts.toString()+".");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a body part\n\r:","");
		if(newName.length()>0)
		{
			int partNum=-1;
			for(int i=0;i<Race.BODYPARTSTR.length;i++)
				if(newName.equalsIgnoreCase(Race.BODYPARTSTR[i]))
				{ partNum=i; break;}
			if(partNum<0)
			{
				StringBuffer str=new StringBuffer("That body part is invalid.  Valid parts include: ");
				for(int i=0;i<Race.BODYPARTSTR.length;i++)
					str.append(Race.BODYPARTSTR[i]+", ");
				mob.tell(str.toString().substring(0,str.length()-2)+".");
			}
			else
			{
				newName=mob.session().prompt("Enter new number ("+E.bodyMask()[partNum]+"), 0=none\n\r:",""+E.bodyMask()[partNum]);
				if(newName.length()>0)
					E.bodyMask()[partNum]=Util.s_int(newName);
				else
					mob.tell("(no change)");
			}
		}
		else
			mob.tell("(no change)");
	}
	static void genEStats(MOB mob, Race R, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		EnvStats S=new DefaultEnvStats(0);
		CoffeeMaker.setEnvStats(S,R.getStat("ESTATS"));
		StringBuffer parts=new StringBuffer("");
		for(int i=0;i<S.getCodes().length;i++)
			if(Util.s_int(S.getStat(S.getCodes()[i]))!=0)
				parts.append(Util.capitalizeAndLower(S.getCodes()[i])+"("+S.getStat(S.getCodes()[i])+") ");
		mob.tell(showNumber+". EStat Adjustments: "+parts.toString()+".");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a stat name\n\r:","");
		if(newName.length()>0)
		{
			String partName=null;
			for(int i=0;i<S.getCodes().length;i++)
				if(newName.equalsIgnoreCase(S.getCodes()[i]))
				{ partName=S.getCodes()[i]; break;}
			if(partName==null)
			{
				StringBuffer str=new StringBuffer("That stat is invalid.  Valid stats include: ");
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
					newName=mob.session().prompt("Enter a value\n\r:","");
					if(newName.length()>0)
					{
						S.setStat(partName,newName);
						checkChange=true;
					}
					else
						mob.tell("(no change)");
				}
				if(checkChange)
				{
					boolean zereoed=true;
					for(int i=0;i<S.getCodes().length;i++)
					{
						if(Util.s_int(S.getStat(S.getCodes()[i]))!=0)
						{ zereoed=false; break;}
					}
					if(zereoed)
						R.setStat("ESTATS","");
					else
						R.setStat("ESTATS",CoffeeMaker.getEnvStatsStr(S));
				}
			}
		}
		else
			mob.tell("(no change)");
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
		CharState S=new DefaultCharState(0);
		CoffeeMaker.setCharState(S,R.getStat(field));
		StringBuffer parts=new StringBuffer("");
		for(int i=0;i<S.getCodes().length;i++)
			if(Util.s_int(S.getStat(S.getCodes()[i]))!=0)
				parts.append(Util.capitalizeAndLower(S.getCodes()[i])+"("+S.getStat(S.getCodes()[i])+") ");
		mob.tell(showNumber+". "+prompt+": "+parts.toString()+".");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a stat name\n\r:","");
		if(newName.length()>0)
		{
			String partName=null;
			for(int i=0;i<S.getCodes().length;i++)
				if(newName.equalsIgnoreCase(S.getCodes()[i]))
				{ partName=S.getCodes()[i]; break;}
			if(partName==null)
			{
				StringBuffer str=new StringBuffer("That stat is invalid.  Valid stats include: ");
				for(int i=0;i<S.getCodes().length;i++)
					str.append(S.getCodes()[i]+", ");
				mob.tell(str.toString().substring(0,str.length()-2)+".");
			}
			else
			{
				newName=mob.session().prompt("Enter a value\n\r:","");
				if(newName.length()>0)
				{
					S.setStat(partName,newName);
					boolean zereoed=true;
					for(int i=0;i<S.getCodes().length;i++)
					{
						if(Util.s_int(S.getStat(S.getCodes()[i]))!=0)
						{ zereoed=false; break;}
					}
					if(zereoed)
						R.setStat(field,"");
					else
						R.setStat(field,CoffeeMaker.getCharStateStr(S));
				}
				else
					mob.tell("(no change)");
			}
		}
		else
			mob.tell("(no change)");
	}
	static void genAStats(MOB mob, Race R, String Field, String FieldName, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		CharStats S=new DefaultCharStats(0);
		CoffeeMaker.setCharStats(S,R.getStat(Field));
		StringBuffer parts=new StringBuffer("");
		for(int i=0;i<CharStats.TRAITS.length;i++)
			if(S.getStat(i)!=0)
				parts.append(Util.capitalizeAndLower(CharStats.TRAITS[i])+"("+S.getStat(i)+") ");
		mob.tell(showNumber+". "+FieldName+": "+parts.toString()+".");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a stat name\n\r:","");
		if(newName.length()>0)
		{
			int partNum=-1;
			for(int i=0;i<CharStats.TRAITS.length;i++)
				if(newName.equalsIgnoreCase(CharStats.TRAITS[i]))
				{ partNum=i; break;}
			if(partNum<0)
			{
				StringBuffer str=new StringBuffer("That stat is invalid.  Valid stats include: ");
				for(int i=0;i<CharStats.TRAITS.length;i++)
					str.append(CharStats.TRAITS[i]+", ");
				mob.tell(str.toString().substring(0,str.length()-2)+".");
			}
			else
			{
				newName=mob.session().prompt("Enter a value\n\r:","");
				if(newName.length()>0)
				{
					S.setStat(partNum,Util.s_int(newName));
					boolean zereoed=true;
					for(int i=0;i<CharStats.TRAITS.length;i++)
					{
						if(S.getStat(i)!=0)
						{ zereoed=false; break;}
					}
					if(zereoed)
						R.setStat(Field,"");
					else
						R.setStat(Field,CoffeeMaker.getCharStatsStr(S));
				}
				else
					mob.tell("(no change)");
			}
		}
		else
			mob.tell("(no change)");
	}

	static void genEStats(MOB mob, CharClass R, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		EnvStats S=new DefaultEnvStats(0);
		CoffeeMaker.setEnvStats(S,R.getStat("ESTATS"));
		StringBuffer parts=new StringBuffer("");
		for(int i=0;i<S.getCodes().length;i++)
			if(Util.s_int(S.getStat(S.getCodes()[i]))!=0)
				parts.append(Util.capitalizeAndLower(S.getCodes()[i])+"("+S.getStat(S.getCodes()[i])+") ");
		mob.tell(showNumber+". EStat Adjustments: "+parts.toString()+".");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a stat name\n\r:","");
		if(newName.length()>0)
		{
			String partName=null;
			for(int i=0;i<S.getCodes().length;i++)
				if(newName.equalsIgnoreCase(S.getCodes()[i]))
				{ partName=S.getCodes()[i]; break;}
			if(partName==null)
			{
				StringBuffer str=new StringBuffer("That stat is invalid.  Valid stats include: ");
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
					newName=mob.session().prompt("Enter a value\n\r:","");
					if(newName.length()>0)
					{
						S.setStat(partName,newName);
						checkChange=true;
					}
					else
						mob.tell("(no change)");
				}
				if(checkChange)
				{
					boolean zereoed=true;
					for(int i=0;i<S.getCodes().length;i++)
					{
						if(Util.s_int(S.getStat(S.getCodes()[i]))!=0)
						{ zereoed=false; break;}
					}
					if(zereoed)
						R.setStat("ESTATS","");
					else
						R.setStat("ESTATS",CoffeeMaker.getEnvStatsStr(S));
				}
			}
		}
		else
			mob.tell("(no change)");
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
		CharState S=new DefaultCharState(0);
		CoffeeMaker.setCharState(S,R.getStat(field));
		StringBuffer parts=new StringBuffer("");
		for(int i=0;i<S.getCodes().length;i++)
			if(Util.s_int(S.getStat(S.getCodes()[i]))!=0)
				parts.append(Util.capitalizeAndLower(S.getCodes()[i])+"("+S.getStat(S.getCodes()[i])+") ");
		mob.tell(showNumber+". "+prompt+": "+parts.toString()+".");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a stat name\n\r:","");
		if(newName.length()>0)
		{
			String partName=null;
			for(int i=0;i<S.getCodes().length;i++)
				if(newName.equalsIgnoreCase(S.getCodes()[i]))
				{ partName=S.getCodes()[i]; break;}
			if(partName==null)
			{
				StringBuffer str=new StringBuffer("That stat is invalid.  Valid stats include: ");
				for(int i=0;i<S.getCodes().length;i++)
					str.append(S.getCodes()[i]+", ");
				mob.tell(str.toString().substring(0,str.length()-2)+".");
			}
			else
			{
				newName=mob.session().prompt("Enter a value\n\r:","");
				if(newName.length()>0)
				{
					S.setStat(partName,newName);
					boolean zereoed=true;
					for(int i=0;i<S.getCodes().length;i++)
					{
						if(Util.s_int(S.getStat(S.getCodes()[i]))!=0)
						{ zereoed=false; break;}
					}
					if(zereoed)
						R.setStat(field,"");
					else
						R.setStat(field,CoffeeMaker.getCharStateStr(S));
				}
				else
					mob.tell("(no change)");
			}
		}
		else
			mob.tell("(no change)");
	}
	static void genAStats(MOB mob, CharClass R, String Field, String FieldName, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		CharStats S=new DefaultCharStats(0);
		CoffeeMaker.setCharStats(S,R.getStat(Field));
		StringBuffer parts=new StringBuffer("");
		for(int i=0;i<CharStats.TRAITS.length;i++)
			if(S.getStat(i)!=0)
				parts.append(Util.capitalizeAndLower(CharStats.TRAITS[i])+"("+S.getStat(i)+") ");
		mob.tell(showNumber+". "+FieldName+": "+parts.toString()+".");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a stat name\n\r:","");
		if(newName.length()>0)
		{
			int partNum=-1;
			for(int i=0;i<CharStats.TRAITS.length;i++)
				if(newName.equalsIgnoreCase(CharStats.TRAITS[i]))
				{ partNum=i; break;}
			if(partNum<0)
			{
				StringBuffer str=new StringBuffer("That stat is invalid.  Valid stats include: ");
				for(int i=0;i<CharStats.TRAITS.length;i++)
					str.append(CharStats.TRAITS[i]+", ");
				mob.tell(str.toString().substring(0,str.length()-2)+".");
			}
			else
			{
				newName=mob.session().prompt("Enter a value\n\r:","");
				if(newName.length()>0)
				{
					S.setStat(partNum,Util.s_int(newName));
					boolean zereoed=true;
					for(int i=0;i<CharStats.TRAITS.length;i++)
					{
						if(S.getStat(i)!=0)
						{ zereoed=false; break;}
					}
					if(zereoed)
						R.setStat(Field,"");
					else
						R.setStat(Field,CoffeeMaker.getCharStatsStr(S));
				}
				else
					mob.tell("(no change)");
			}
		}
		else
			mob.tell("(no change)");
	}
	static void genResources(MOB mob, Race E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		while(true)
		{
			StringBuffer parts=new StringBuffer("");
			int numResources=Util.s_int(E.getStat("NUMRSC"));
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
			mob.tell(showNumber+". Resources: "+parts.toString()+".");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			String newName=mob.session().prompt("Enter a resource name to remove or\n\rthe word new and an item name to add from your inventory\n\r:","");
			if(newName.length()>0)
			{
				int partNum=-1;
				for(int i=0;i<V.size();i++)
					if(EnglishParser.containsString(((Item)V.elementAt(i)).name(),newName))
					{ partNum=i; break;}
				boolean updateList=false;
				if(partNum<0)
				{
					if(!newName.toLowerCase().startsWith("new "))
						mob.tell("That is neither an existing resource name, or the word new followed by a valid item name.");
					else
					{
						Item I=mob.fetchCarried(null,newName.substring(4).trim());
						if(I!=null)
						{
							I=(Item)I.copyOf();
							V.addElement(I);
							mob.tell(I.name()+" added.");
							updateList=true;
						}

					}
				}
				else
				{
					Item I=(Item)V.elementAt(partNum);
					V.removeElementAt(partNum);
					mob.tell(I.name()+" removed.");
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
				mob.tell("(no change)");
				return;
			}
		}
	}
	static void genOutfit(MOB mob, Race E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		while(true)
		{
			StringBuffer parts=new StringBuffer("");
			int numResources=Util.s_int(E.getStat("NUMOFT"));
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
			mob.tell(showNumber+". Outfit: "+parts.toString()+".");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			String newName=mob.session().prompt("Enter an item name to remove or\n\rthe word new and an item name to add from your inventory\n\r:","");
			if(newName.length()>0)
			{
				int partNum=-1;
				for(int i=0;i<V.size();i++)
					if(EnglishParser.containsString(((Item)V.elementAt(i)).name(),newName))
					{ partNum=i; break;}
				boolean updateList=false;
				if(partNum<0)
				{
					if(!newName.toLowerCase().startsWith("new "))
						mob.tell("That is neither an existing item name, or the word new followed by a valid item name.");
					else
					{
						Item I=mob.fetchCarried(null,newName.substring(4).trim());
						if(I!=null)
						{
							I=(Item)I.copyOf();
							V.addElement(I);
							mob.tell(I.name()+" added.");
							updateList=true;
						}

					}
				}
				else
				{
					Item I=(Item)V.elementAt(partNum);
					V.removeElementAt(partNum);
					mob.tell(I.name()+" removed.");
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
				mob.tell("(no change)");
				return;
			}
		}
	}
	static void genOutfit(MOB mob, CharClass E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		while(true)
		{
			StringBuffer parts=new StringBuffer("");
			int numResources=Util.s_int(E.getStat("NUMOFT"));
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
			mob.tell(showNumber+". Outfit: "+parts.toString()+".");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			String newName=mob.session().prompt("Enter an item name to remove or\n\rthe word new and an item name to add from your inventory\n\r:","");
			if(newName.length()>0)
			{
				int partNum=-1;
				for(int i=0;i<V.size();i++)
					if(EnglishParser.containsString(((Item)V.elementAt(i)).name(),newName))
					{ partNum=i; break;}
				boolean updateList=false;
				if(partNum<0)
				{
					if(!newName.toLowerCase().startsWith("new "))
						mob.tell("That is neither an existing item name, or the word new followed by a valid item name.");
					else
					{
						Item I=mob.fetchCarried(null,newName.substring(4).trim());
						if(I!=null)
						{
							I=(Item)I.copyOf();
							V.addElement(I);
							mob.tell(I.name()+" added.");
							updateList=true;
						}

					}
				}
				else
				{
					Item I=(Item)V.elementAt(partNum);
					V.removeElementAt(partNum);
					mob.tell(I.name()+" removed.");
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
				mob.tell("(no change)");
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
		mob.tell(showNumber+". Natural Weapon: "+parts.toString()+".");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a weapon name from your inventory to change, or 'null' for human\n\r:","");
		if(newName.equalsIgnoreCase("null"))
		{
			E.setStat("WEAPONCLASS","");
			mob.tell("Human weapons set.");
		}
		else
		if(newName.length()>0)
		{
			I=mob.fetchCarried(null,newName);
			if(I==null)
			{
				mob.tell("'"+newName+"' is not in your inventory.");
				mob.tell("(no change)");
				return;
			}
			I=(Item)I.copyOf();
			E.setStat("WEAPONCLASS",I.ID());
			E.setStat("WEAPONXML",I.text());
		}
		else
		{
			mob.tell("(no change)");
			return;
		}
	}

	static void genAgingChart(MOB mob, Race E, int showNumber, int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		
		mob.tell(showNumber+". Aging Chart: "+Util.toStringList(E.getAgingChart())+".");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		while(true)
		{
			String newName=mob.session().prompt("Enter a comma-delimited list of 9 numbers, running from infant -> ancient\n\r:","");
			if(newName.length()==0)
			{
				mob.tell("(no change)");
				return;
			}
			Vector V=Util.parseCommas(newName,true);
			if(V.size()==9)
			{
			    int highest=-1;
			    boolean cont=false;
			    for(int i=0;i<V.size();i++)
			    {
			        if(Util.s_int((String)V.elementAt(i))<highest)
			        {
			            mob.tell("Entry "+((String)V.elementAt(i))+" is out of place.");
			            cont=true;
			            break;
			        }
			        highest=Util.s_int((String)V.elementAt(i));
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
		
		int flags=Util.s_int(E.getStat("DISFLAGS"));
		StringBuffer sets=new StringBuffer("");
	    if(Util.bset(flags,Race.GENFLAG_NOCLASS))
		    sets.append("Classless ");
	    if(Util.bset(flags,Race.GENFLAG_NOLEVELS))
		    sets.append("Leveless ");
	    if(Util.bset(flags,Race.GENFLAG_NOEXP))
		    sets.append("Expless ");
		
		mob.tell(showNumber+". Extra Racial Flags: "+sets.toString()+".");
		if((showFlag!=showNumber)&&(showFlag>-999)) 
		    return;
		String newName=mob.session().prompt("Enter: 1) Classless, 2) Leveless, 3) Expless\n\r:","");
		switch(Util.s_int(newName))
		{
		case 1:
		    if(Util.bset(flags,Race.GENFLAG_NOCLASS))
		        flags=Util.unsetb(flags,Race.GENFLAG_NOCLASS);
		    else
		        flags=flags|Race.GENFLAG_NOCLASS;
		    break;
		case 2:
		    if(Util.bset(flags,Race.GENFLAG_NOLEVELS))
		        flags=Util.unsetb(flags,Race.GENFLAG_NOLEVELS);
		    else
		        flags=flags|Race.GENFLAG_NOLEVELS;
		    break;
		case 3:
		    if(Util.bset(flags,Race.GENFLAG_NOEXP))
		        flags=Util.unsetb(flags,Race.GENFLAG_NOEXP);
		    else
		        flags=flags|Race.GENFLAG_NOEXP;
		    break;
		default:
			mob.tell("(no change)");
			break;
		}
		E.setStat("DISFLAGS",""+flags);
	}

	static void genClassFlags(MOB mob, CharClass E, int showNumber, int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) 
		    return;
		
		int flags=Util.s_int(E.getStat("DISFLAGS"));
		StringBuffer sets=new StringBuffer("");
	    if(Util.bset(flags,CharClass.GENFLAG_NORACE))
		    sets.append("Raceless ");
	    if(Util.bset(flags,CharClass.GENFLAG_NOLEVELS))
		    sets.append("Leveless ");
	    if(Util.bset(flags,CharClass.GENFLAG_NOEXP))
		    sets.append("Expless ");
		
		mob.tell(showNumber+". Extra CharClass Flags: "+sets.toString()+".");
		if((showFlag!=showNumber)&&(showFlag>-999)) 
		    return;
		String newName=mob.session().prompt("Enter: 1) Raceless, 2) Leveless, 3) Expless\n\r:","");
		switch(Util.s_int(newName))
		{
		case 1:
		    if(Util.bset(flags,CharClass.GENFLAG_NORACE))
		        flags=Util.unsetb(flags,CharClass.GENFLAG_NORACE);
		    else
		        flags=flags|CharClass.GENFLAG_NORACE;
		    break;
		case 2:
		    if(Util.bset(flags,CharClass.GENFLAG_NOLEVELS))
		        flags=Util.unsetb(flags,CharClass.GENFLAG_NOLEVELS);
		    else
		        flags=flags|CharClass.GENFLAG_NOLEVELS;
		    break;
		case 3:
		    if(Util.bset(flags,CharClass.GENFLAG_NOEXP))
		        flags=Util.unsetb(flags,CharClass.GENFLAG_NOEXP);
		    else
		        flags=flags|CharClass.GENFLAG_NOEXP;
		    break;
		default:
			mob.tell("(no change)");
			break;
		}
		E.setStat("DISFLAGS",""+flags);
	}

	static void genRacialAbilities(MOB mob, Race E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		while(true)
		{
			StringBuffer parts=new StringBuffer("");
			int numResources=Util.s_int(E.getStat("NUMRABLE"));
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
			mob.tell(showNumber+". Racial Abilities: "+parts.toString()+".");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			String newName=mob.session().prompt("Enter an ability name to add or remove\n\r:","");
			if(newName.equalsIgnoreCase("?"))
				mob.tell(CMLister.reallyList(CMClass.abilities(),-1).toString());
			else
			if(newName.length()>0)
			{
				int partNum=-1;
				for(int i=0;i<ables.size();i++)
					if(EnglishParser.containsString(((Ability)ables.elementAt(i)).ID(),newName))
					{ partNum=i; break;}
				boolean updateList=false;
				if(partNum<0)
				{
					Ability A=CMClass.getAbility(newName);
					if(A==null)
						mob.tell("That is neither an existing ability name, nor a valid one to add.  Use ? for a list.");
					else
					if(A.isAutoInvoked())
						mob.tell("'"+A.name()+"' cannot be named, as it is autoinvoked.");
					else
					if((A.triggerStrings()==null)||(A.triggerStrings().length==0))
						mob.tell("'"+A.name()+"' cannot be named, as it has no trigger/command words.");
					else
					{
						StringBuffer str=new StringBuffer(A.ID()+";");
						String level=mob.session().prompt("Enter the level of this skill (1): ","1");
						str.append((""+Util.s_int(level))+";");
						if(mob.session().confirm("Is this skill automatically gained (Y/n)?","Y"))
							str.append("false;");
						else
							str.append("true;");
						String prof=mob.session().prompt("Enter the (perm) profficiency level (100): ","100");
						str.append((""+Util.s_int(prof)));
						data.addElement(str.toString());
						ables.addElement(A);
						mob.tell(A.name()+" added.");
						updateList=true;
					}
				}
				else
				{
					Ability A=(Ability)ables.elementAt(partNum);
					ables.removeElementAt(partNum);
					data.removeElementAt(partNum);
					updateList=true;
					mob.tell(A.name()+" removed.");
				}
				if(updateList)
				{
					if(data.size()>0)
						E.setStat("NUMRABLE",""+data.size());
					else
						E.setStat("NUMRABLE","");
					for(int i=0;i<data.size();i++)
					{
						Vector V=Util.parseSemicolons((String)data.elementAt(i),false);
						E.setStat("GETRABLE"+i,((String)V.elementAt(0)));
						E.setStat("GETRABLELVL"+i,((String)V.elementAt(1)));
						E.setStat("GETRABLEQUAL"+i,((String)V.elementAt(2)));
						E.setStat("GETRABLEPROF"+i,((String)V.elementAt(3)));
					}
				}
			}
			else
			{
				mob.tell("(no change)");
				return;
			}
		}
	}
	static void genRacialEffects(MOB mob, Race E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		while(true)
		{
			StringBuffer parts=new StringBuffer("");
			int numResources=Util.s_int(E.getStat("NUMREFF"));
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
			mob.tell(showNumber+". Racial Effects: "+parts.toString()+".");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			String newName=mob.session().prompt("Enter an effect name to add or remove\n\r:","");
			if(newName.equalsIgnoreCase("?"))
				mob.tell(CMLister.reallyList(CMClass.abilities(),-1).toString());
			else
			if(newName.length()>0)
			{
				int partNum=-1;
				for(int i=0;i<ables.size();i++)
					if(EnglishParser.containsString(((Ability)ables.elementAt(i)).ID(),newName))
					{ partNum=i; break;}
				boolean updateList=false;
				if(partNum<0)
				{
					Ability A=CMClass.getAbility(newName);
					if(A==null)
						mob.tell("That is neither an existing effect name, nor a valid one to add.  Use ? for a list.");
					else
					{
						StringBuffer str=new StringBuffer(A.ID()+"~");
						String level=mob.session().prompt("Enter the level to gain this effect (1): ","1");
						str.append((""+Util.s_int(level))+"~");
						String prof=mob.session().prompt("Enter any parameters: ","");
						str.append(""+prof);
						data.addElement(str.toString());
						ables.addElement(A);
						mob.tell(A.name()+" added.");
						updateList=true;
					}
				}
				else
				{
					Ability A=(Ability)ables.elementAt(partNum);
					ables.removeElementAt(partNum);
					data.removeElementAt(partNum);
					updateList=true;
					mob.tell(A.name()+" removed.");
				}
				if(updateList)
				{
					if(data.size()>0)
						E.setStat("NUMREFF",""+data.size());
					else
						E.setStat("NUMREFF","");
					for(int i=0;i<data.size();i++)
					{
						Vector V=Util.parseSquiggleDelimited((String)data.elementAt(i),false);
						E.setStat("GETREFF"+i,((String)V.elementAt(0)));
						E.setStat("GETREFFLVL"+i,((String)V.elementAt(1)));
						E.setStat("GETREFFPARM"+i,((String)V.elementAt(2)));
					}
				}
			}
			else
			{
				mob.tell("(no change)");
				return;
			}
		}
	}
	static void genClassAbilities(MOB mob, CharClass E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		while(true)
		{
			StringBuffer parts=new StringBuffer("");
			int numResources=Util.s_int(E.getStat("NUMCABLE"));
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
			mob.tell(showNumber+". Class Abilities: "+parts.toString()+".");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			String newName=mob.session().prompt("Enter an ability name to add or remove\n\r:","");
			if(newName.equalsIgnoreCase("?"))
				mob.tell(CMLister.reallyList(CMClass.abilities(),-1).toString());
			else
			if(newName.length()>0)
			{
				int partNum=-1;
				for(int i=0;i<ables.size();i++)
					if(EnglishParser.containsString(((Ability)ables.elementAt(i)).ID(),newName))
					{ partNum=i; break;}
				boolean updateList=false;
				if(partNum<0)
				{
					Ability A=CMClass.getAbility(newName);
					if(A==null)
						mob.tell("That is neither an existing ability name, nor a valid one to add.  Use ? for a list.");
					else
					{
						StringBuffer str=new StringBuffer(A.ID()+";");
						String level=mob.session().prompt("Enter the level of this skill (1): ","1");
						str.append((""+Util.s_int(level))+";");
						String prof=mob.session().prompt("Enter the (default) profficiency level (0): ","0");
						str.append((""+Util.s_int(prof))+";");
						if(mob.session().confirm("Is this skill automatically gained (Y/n)?","Y"))
							str.append("true;");
						else
							str.append("false;");
						if(mob.session().confirm("Is this skill secret (N/y)?","N"))
							str.append("true;");
						else
							str.append("false;");
						String parm=mob.session().prompt("Enter any properties (): ","");
						str.append(parm);
						data.addElement(str.toString());
						ables.addElement(A);
						mob.tell(A.name()+" added.");
						updateList=true;
					}
				}
				else
				{
					Ability A=(Ability)ables.elementAt(partNum);
					ables.removeElementAt(partNum);
					data.removeElementAt(partNum);
					updateList=true;
					mob.tell(A.name()+" removed.");
				}
				if(updateList)
				{
					if(data.size()>0)
						E.setStat("NUMCABLE",""+data.size());
					else
						E.setStat("NUMCABLE","");
					for(int i=0;i<data.size();i++)
					{
						Vector V=Util.parseSemicolons((String)data.elementAt(i),false);
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
				mob.tell("(no change)");
				return;
			}
		}
	}
	static void genCulturalAbilities(MOB mob, Race E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		while(true)
		{
			StringBuffer parts=new StringBuffer("");
			int numResources=Util.s_int(E.getStat("NUMCABLE"));
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
			mob.tell(showNumber+". Cultural Abilities: "+parts.toString()+".");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			String newName=mob.session().prompt("Enter an ability name to add or remove\n\r:","");
			if(newName.equalsIgnoreCase("?"))
				mob.tell(CMLister.reallyList(CMClass.abilities(),-1).toString());
			else
			if(newName.length()>0)
			{
				int partNum=-1;
				for(int i=0;i<ables.size();i++)
					if(EnglishParser.containsString(((Ability)ables.elementAt(i)).ID(),newName))
					{ partNum=i; break;}
				boolean updateList=false;
				if(partNum<0)
				{
					Ability A=CMClass.getAbility(newName);
					if(A==null)
						mob.tell("That is neither an existing ability name, nor a valid one to add.  Use ? for a list.");
					else
					{
						StringBuffer str=new StringBuffer(A.ID()+";");
						String prof=mob.session().prompt("Enter the default profficiency level (100): ","100");
						str.append((""+Util.s_int(prof)));
						data.addElement(str.toString());
						ables.addElement(A);
						mob.tell(A.name()+" added.");
						updateList=true;
					}
				}
				else
				{
					Ability A=(Ability)ables.elementAt(partNum);
					ables.removeElementAt(partNum);
					data.removeElementAt(partNum);
					updateList=true;
					mob.tell(A.name()+" removed.");
				}
				if(updateList)
				{
					if(data.size()>0)
						E.setStat("NUMCABLE",""+data.size());
					else
						E.setStat("NUMCABLE","");
					for(int i=0;i<data.size();i++)
					{
						Vector V=Util.parseSemicolons((String)data.elementAt(i),false);
						E.setStat("GETCABLE"+i,((String)V.elementAt(0)));
						E.setStat("GETCABLEPROF"+i,((String)V.elementAt(1)));
					}
				}
			}
			else
			{
				mob.tell("(no change)");
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
		if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while(!ok)
		{
			int showNumber=0;

            genInt(mob,me,++showNumber,showFlag,"Number of Class Names: ","NUMNAME");
            int numNames=Util.s_int(me.getStat("NUMNAME"));
            if(numNames<=1)
    			genText(mob,me,++showNumber,showFlag,"Name","NAME0");
            else
            for(int i=0;i<numNames;i++)
            {
                genText(mob,me,++showNumber,showFlag,"Name #"+i,"NAME"+i);
                if(i>0)
                while(!mob.session().killFlag())
                {
                    int oldNameLevel=Util.s_int(me.getStat("NAMELEVEL"+i));
                    genInt(mob,me,++showNumber,showFlag,"Name #"+i+" class level: ","NAMELEVEL"+i);
                    int previousNameLevel=Util.s_int(me.getStat("NAMELEVEL"+(i-1)));
                    int newNameLevel=Util.s_int(me.getStat("NAMELEVEL"+i));
                    if((oldNameLevel!=newNameLevel)&&(newNameLevel<(previousNameLevel+1)))
                    {
                        mob.tell("This level may not be less than "+(previousNameLevel+1)+".");
                        showNumber--;
                    }
                    else
                        break;
                }
            }
			genText(mob,me,++showNumber,showFlag,"Base Class","BASE");
			genBool(mob,me,++showNumber,showFlag,"Player Class","PLAYER");
			genInt(mob,me,++showNumber,showFlag,"HP Con Divisor","HPDIV");
			genInt(mob,me,++showNumber,showFlag,"HP #Dice","HPDICE");
			genInt(mob,me,++showNumber,showFlag,"HP Die","HPDIE");
			genInt(mob,me,++showNumber,showFlag,"Mana Divisor","MANADIV");
			genInt(mob,me,++showNumber,showFlag,"Mana #Dice","MANADICE");
			genInt(mob,me,++showNumber,showFlag,"Mana Die","MANADIE");
			genInt(mob,me,++showNumber,showFlag,"Prac/Level","LVLPRAC");
			genInt(mob,me,++showNumber,showFlag,"Attack/Level","LVLATT");
			genAttackAttribute(mob,me,++showNumber,showFlag,"Attack Attribute","ATTATT");
			genInt(mob,me,++showNumber,showFlag,"Practices/1stLvl","FSTPRAC");
			genInt(mob,me,++showNumber,showFlag,"Trains/1stLvl","FSTTRAN");
			genInt(mob,me,++showNumber,showFlag,"Levels/Dmg Pt","LVLDAM");
			genInt(mob,me,++showNumber,showFlag,"Moves/Level","LVLMOVE");
			genArmorCode(mob,me,++showNumber,showFlag,"Armor Restr.","ARMOR");
			genText(mob,me,++showNumber,showFlag,"Limitations","STRLMT");
			genText(mob,me,++showNumber,showFlag,"Bonuses","STRBON");
			genQualifications(mob,me,++showNumber,showFlag,"Qualifications","QUAL");
			genEStats(mob,me,++showNumber,showFlag);
			genAStats(mob,me,"ASTATS","CharStat Adjustments",++showNumber,showFlag);
			genAStats(mob,me,"CSTATS","CharStat Settings",++showNumber,showFlag);
			genAState(mob,me,"ASTATE","CharState Adjustments",++showNumber,showFlag);
			genAState(mob,me,"STARTASTATE","New Player CharState Adj.",++showNumber,showFlag);
			genClassFlags(mob,me,++showNumber,showFlag);
			genWeaponRestr(mob,me,++showNumber,showFlag,"Weapon Restr.","NUMWEP","GETWEP");
			genOutfit(mob,me,++showNumber,showFlag);
			genClassAbilities(mob,me,++showNumber,showFlag);
            genInt(mob,me,++showNumber,showFlag,"Number of Security Code Sets: ","NUMGROUP");
            int numGroups=Util.s_int(me.getStat("NUMGROUP"));
            for(int i=0;i<numGroups;i++)
            {
                genText(mob,me,++showNumber,showFlag,"Security Codes in Set #"+i,"GROUP"+i);
                if(i>0)
                while(!mob.session().killFlag())
                {
                    int oldGroupLevel=Util.s_int(me.getStat("GROUPLEVEL"+i));
                    genInt(mob,me,++showNumber,showFlag,"Class Level for Security Set #"+i+": ","GROUPLEVEL"+i);
                    int previousGroupLevel=Util.s_int(me.getStat("GROUPLEVEL"+(i-1)));
                    int newGroupLevel=Util.s_int(me.getStat("GROUPLEVEL"+i));
                    if((oldGroupLevel!=newGroupLevel)&&(newGroupLevel<(previousGroupLevel+1)))
                    {
                        mob.tell("This level may not be less than "+(previousGroupLevel+1)+".");
                        showNumber--;
                    }
                    else
                        break;
                }
            }
            
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
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
        if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
            showFlag=-999;
        while(!ok)
        {
            int showNumber=0;
            // name
            me.name=EnglishParser.promptText(mob,me.name,++showNumber,showFlag,"Name");
            
            // ranges
            ++showNumber;
            if(me.ranges.size()==0)
                me.ranges.addElement(new Faction.FactionRange(me,"0;100;Sample Range;SAMPLE;"));
            while((mob.session()!=null)&&(!mob.session().killFlag())&&(!((showFlag>0)&&(showFlag!=showNumber))))
            {
                StringBuffer list=new StringBuffer(showNumber+") Faction Division/Ranges List:\n\r");
                list.append(Util.padRight("   Name",21)+Util.padRight("Min",11)+Util.padRight("Max",11)+Util.padRight("Code",16)+Util.padRight("Align",6)+"\n\r");
                for(int r=0;r<me.ranges.size();r++)
                {
                    Faction.FactionRange FR=(Faction.FactionRange)me.ranges.elementAt(r);
                    list.append(Util.padRight("   "+FR.Name,20)+" ");
                    list.append(Util.padRight(""+FR.low,10)+" ");
                    list.append(Util.padRight(""+FR.high,10)+" ");
                    list.append(Util.padRight(FR.CodeName,15)+" ");
                    list.append(Util.padRight(Faction.ALIGN_NAMES[FR.AlignEquiv],5)+"\n\r");
                }
                mob.tell(list.toString());
                if((showFlag!=showNumber)&&(showFlag>-999)) break;
                String which=mob.session().prompt("Enter a name to add, remove, or modify:","");
                if(which.length()==0)
                    break;
                Faction.FactionRange FR=null;
                for(int r=0;r<me.ranges.size();r++)
                {
                    if(((Faction.FactionRange)me.ranges.elementAt(r)).Name.equalsIgnoreCase(which))
                        FR=(Faction.FactionRange)me.ranges.elementAt(r);
                }
                if(FR==null)
                {
                    if(mob.session().confirm("Create a new range called '"+which+"' (y/N): ","N"))
                    {
                        FR=new Faction.FactionRange(me,"0;100;"+which+";CHANGEMYCODENAME;");
                        me.ranges.addElement(FR);
                    }
                }
                else
                if(mob.session().choose("Would you like to M)odify or D)elete this range (M/d): ","MD","M").toUpperCase().startsWith("D"))
                {
                    me.ranges.remove(FR);
                    mob.tell("Range deleted.");
                    FR=null;
                }
                if(FR!=null)
                {
                    String newName=mob.session().prompt("Enter a new name ("+FR.Name+")\n\r: ",FR.Name);
                    boolean error99=false;
                    if(newName.length()==0)
                        error99=true;
                    else
                    for(int r=0;r<me.ranges.size();r++)
                    {
                        Faction.FactionRange FR3=(Faction.FactionRange)me.ranges.elementAt(r);
                        if(FR3.Name.equalsIgnoreCase(FR.Name)&&(FR3!=FR))
                        { mob.tell("A range already exists with that name!"); error99=true; break;} 
                    }
                    if(error99)
                        mob.tell("No Change");
                    else
                        FR.Name=newName;
                    newName=mob.session().prompt("Enter the low end of the range ("+FR.low+")\n\r: ",""+FR.low);
                    if(!Util.isInteger(newName))
                        mob.tell("No Change");
                    else
                        FR.low=Util.s_int(newName);
                    newName=mob.session().prompt("Enter the high end of the range ("+FR.high+")\n\r: ",""+FR.high);
                    if((!Util.isInteger(newName))||(Util.s_int(newName)<FR.low))
                        mob.tell("No Change");
                    else
                        FR.high=Util.s_int(newName);
                    newName=mob.session().prompt("Enter a code-name ("+FR.CodeName+")\n\r: ",""+FR.CodeName);
                    if(newName.trim().length()==0)
                        mob.tell("No Change");
                    else
                        FR.CodeName=newName.toUpperCase().trim();
                    StringBuffer prompt=new StringBuffer("Select the 'virtue' (if any) of this range:\n\r");
                    StringBuffer choices=new StringBuffer("");
                    for(int r=0;r<Faction.ALIGN_NAMES.length;r++)
                    {
                        choices.append(""+r);
                        if(r==Faction.ALIGN_INDIFF)
                            prompt.append(r+") Not applicable\n\r");
                        else
                            prompt.append(r+") "+Faction.ALIGN_NAMES[r].toLowerCase()+"\n\r");
                    }
                    FR.AlignEquiv=Util.s_int(mob.session().choose(prompt.toString()+"Enter alignment equivalency or 0: ",choices.toString(),""+FR.AlignEquiv));
                }
            }
            
            
            // show in score
            me.showinscore=EnglishParser.promptBool(mob,me.showinscore,++showNumber,showFlag,"Show in 'Score'");
            
            // show in factions
            me.showinfactionscommand=EnglishParser.promptBool(mob,me.showinfactionscommand,++showNumber,showFlag,"Show in 'Factions' command");
            
            // show in special reports
            boolean alreadyReporter=false;
            for(Enumeration e=Factions.factionSet.elements();e.hasMoreElements();)
            {
                Faction F2=(Faction)e.nextElement();
                if(F2.showinspecialreported) alreadyReporter=true;
            }
            if(!alreadyReporter)
                me.showinspecialreported=EnglishParser.promptBool(mob,me.showinspecialreported,++showNumber,showFlag,"Show in Reports");
            
            // show in editor
            me.showineditor=EnglishParser.promptBool(mob,me.showineditor,++showNumber,showFlag,"Show in MOB Editor");
            
            // auto defaults
            boolean error=true;
            me.autoDefaults=Util.parseSemicolons(EnglishParser.promptText(mob,Util.toSemicolonList(me.autoDefaults),++showNumber,showFlag,"Optional automatic assigned values with zapper masks (semicolon delimited).\n\r   "),true);
            
            // non-auto defaults
            error=true;
            if(me.defaults.size()==0)
                me.defaults.addElement("0");
            ++showNumber;
            while(error&&(mob.session()!=null)&&(!mob.session().killFlag()))
            {
                error=false;
                String newDefaults=EnglishParser.promptText(mob,Util.toSemicolonList(me.defaults),showNumber,showFlag,"Other default values with zapper masks (semicolon delimited).\n\r   ");
                if((showFlag!=showNumber)&&(showFlag>-999)) break;
                Vector V=Util.parseSemicolons(newDefaults,true);
                if(V.size()==0)
                {
                    mob.tell("This field may not be empty.");
                    error=true;
                }
                me.defaults=Util.parseSemicolons(newDefaults,true);
            }
            
            // choices and choice intro
            me.choices=Util.parseSemicolons(EnglishParser.promptText(mob,Util.toSemicolonList(me.choices),++showNumber,showFlag,"Optional new player value choices (semicolon-delimited).\n\r   "),true);
            if(me.choices.size()>0)
                me.choiceIntro=EnglishParser.promptText(mob,me.choiceIntro,++showNumber,showFlag,"Optional choices introduction text. Filename");
            
            // rate modifier
            String newModifier=EnglishParser.promptText(mob,Math.round(me.rateModifier*100.0)+"%",++showNumber,showFlag,"Rate modifier");
            if(newModifier.endsWith("%"))
                newModifier=newModifier.substring(0,newModifier.length()-1);
            if(Util.isNumber(newModifier))
                me.rateModifier=Util.s_double(newModifier)/100.0;
            
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
                    if(me.experienceFlag.equalsIgnoreCase(Faction.EXPAFFECT_NAMES[i]))
                        myval=i;
                    nextPrompt.append("  "+(i+1)+") "+Util.capitalizeAndLower(Faction.EXPAFFECT_NAMES[i].toLowerCase())+"\n\r");
                }
                if(myval<0){ me.experienceFlag="NONE"; myval=0;}
                if((showFlag!=showNumber)&&(showFlag>-999))
                {
                    mob.tell(showNumber+") Affect on experience: "+Faction.EXPAFFECT_NAMES[myval]);
                    break;
                }
                String prompt="Affect on experience: "+Faction.EXPAFFECT_NAMES[myval]+nextPrompt.toString()+"\n\rSelect a value: ";
                int mynewval=EnglishParser.promptInteger(mob,myval+1,showNumber,showFlag,prompt);
                if((showFlag!=showNumber)&&(showFlag>-999)) break;
                if((mynewval<=0)||(mynewval>Faction.EXPAFFECT_NAMES.length))
                {
                    mob.tell("That value is not valid.");
                    error2=true;
                }
                else
                    me.experienceFlag=Faction.EXPAFFECT_NAMES[mynewval-1];
            }
            
            // factors by mask
            ++showNumber;
            while((mob.session()!=null)&&(!mob.session().killFlag())&&(!((showFlag>0)&&(showFlag!=showNumber))))
            {
                StringBuffer list=new StringBuffer(showNumber+") Faction change adjustment Factors with Zapper Masks:\n\r");
                list.append("    #) "+Util.padRight("Zapper Mask",31)+Util.padRight("Loss",6)+Util.padRight("Gain",6)+"\n\r");
                StringBuffer choices=new StringBuffer("");
                for(int r=0;r<me.factors.size();r++)
                {
                    Vector factor=(Vector)me.factors.elementAt(r);
                    if(factor.size()!=3)
                        me.factors.removeElement(factor);
                    else
                    {
                        choices.append(((char)('A'+r)));
                        list.append("    "+(((char)('A'+r))+") "));
                        list.append(Util.padRight((String)factor.elementAt(2),30)+" ");
                        list.append(Util.padRight(""+Math.round(Util.s_double((String)factor.elementAt(1))*100.0)+"%",5)+" ");
                        list.append(Util.padRight(""+Math.round(Util.s_double((String)factor.elementAt(0))*100.0)+"%",5)+"\n\r");
                    }
                }
                mob.tell(list.toString());
                if((showFlag!=showNumber)&&(showFlag>-999)) break;
                String which=mob.session().choose("Enter a # to remove, or modify, or enter 0 to Add:","0"+choices.toString(),"").trim().toUpperCase();
                int factorNum=choices.toString().indexOf(which);
                if((which.length()!=1)
                ||((!which.equalsIgnoreCase("0"))
                    &&((factorNum<0)||(factorNum>=me.factors.size()))))
                    break;
                Vector factor=null;
                if(!which.equalsIgnoreCase("0"))
                {
                    factor=(Vector)me.factors.elementAt(factorNum);
                    if(factor!=null)
                        if(mob.session().choose("Would you like to M)odify or D)elete this range (M/d): ","MD","M").toUpperCase().startsWith("D"))
                        {
                            me.factors.remove(factor);
                            mob.tell("Factor deleted.");
                            factor=null;
                        }
                }
                else
                {
                    factor=new Vector();
                    factor.addElement("1.0");
                    factor.addElement("1.0");
                    factor.addElement("");
                    me.factors.addElement(factor);
                }
                if(factor!=null)
                {
                    String mask=mob.session().prompt("Enter a new zapper mask ("+((String)factor.elementAt(2))+")\n\r: ",((String)factor.elementAt(2)));
                    double newHigh=Util.s_double((String)factor.elementAt(0));
                    String newName=mob.session().prompt("Enter gain adjustment ("+Math.round(newHigh*100)+"%): ",Math.round(newHigh*100)+"%").trim();
                    if(newName.endsWith("%"))
                        newName=newName.substring(0,newName.length()-1);
                    if(!Util.isNumber(newName))
                        mob.tell("No Change");
                    else
                        newHigh=Util.s_double(newName)/100.0;
                    
                    double newLow=Util.s_double((String)factor.elementAt(1));
                    newName=mob.session().prompt("Enter loss adjustment ("+Math.round(newLow*100)+"%): ",Math.round(newLow*100)+"%").trim();
                    if(newName.endsWith("%"))
                        newName=newName.substring(0,newName.length()-1);
                    if(!Util.isNumber(newName))
                        mob.tell("No Change");
                    else
                        newLow=Util.s_double(newName)/100.0;
                    me.factors.removeElement(factor);
                    factor=new Vector();
                    factor.addElement(""+newHigh);
                    factor.addElement(""+newLow);
                    factor.addElement(""+mask);
                    me.factors.addElement(factor);
                }
            }
            
            // relations between factions
            ++showNumber;
            while((mob.session()!=null)&&(!mob.session().killFlag())&&(!((showFlag>0)&&(showFlag!=showNumber))))
            {
                StringBuffer list=new StringBuffer(showNumber+") Cross-Faction Relations:\n\r");
                list.append("    "+Util.padRight("Faction",31)+"Percentage change\n\r");
                for(Enumeration e=me.relations.keys();e.hasMoreElements();)
                {
                    String key=(String)e.nextElement();
                    Double value=(Double)me.relations.get(key);
                    Faction F=Factions.getFaction(key);
                    if(F!=null)
                    {
                        list.append("    "+Util.padRight(F.name,31)+" ");
                        long lval=Math.round(value.doubleValue()*100.0);
                        list.append(lval+"%");
                        list.append("\n\r");
                    }
                }
                mob.tell(list.toString());
                if((showFlag!=showNumber)&&(showFlag>-999)) break;
                String which=mob.session().prompt("Enter a faction to add, remove, or modify relations:","");
                if(which.length()==0)
                    break;
                Faction theF=null;
                for(Enumeration e=me.relations.keys();e.hasMoreElements();)
                {
                    String key=(String)e.nextElement();
                    Faction F=Factions.getFaction(key);
                    if((F!=null)&&(F.name.equalsIgnoreCase(which)))
                        theF=F;
                }
                if(theF==null)
                {
                    Faction possibleF=Factions.getFaction(which);
                    if(possibleF==null) possibleF=Factions.getFactionByName(which);
                    if(possibleF==null)
                        mob.tell("'"+which+"' is not a valid faction.");
                    else
                    if(mob.session().confirm("Create a new relation for faction  '"+possibleF.name+"' (y/N): ","N"))
                    {
                        theF=possibleF;
                        me.relations.put(theF.ID,new Double(1.0));
                    }
                }
                else
                if(mob.session().choose("Would you like to M)odify or D)elete this relation (M/d): ","MD","M").toUpperCase().startsWith("D"))
                {
                    me.relations.remove(theF.ID);
                    mob.tell("Relation deleted.");
                    theF=null;
                }
                if(theF!=null)
                {
                    long amount=Math.round(((Double)me.relations.get(theF.ID)).doubleValue()*100.0);
                    String newName=mob.session().prompt("Enter a relation amount ("+amount+"%): ",""+amount+"%");
                    if(newName.endsWith("%")) newName=newName.substring(0,newName.length()-1);
                    if(!Util.isInteger(newName))
                        mob.tell("(no change)");
                    else
                        amount=Util.s_long(newName);
                    me.relations.remove(theF.ID);
                    me.relations.put(theF.ID,new Double(amount/100.0));
                }
            }
            
            // faction change triggers
            ++showNumber;
            while((mob.session()!=null)&&(!mob.session().killFlag())&&(!((showFlag>0)&&(showFlag!=showNumber))))
            {
                StringBuffer list=new StringBuffer(showNumber+") Faction Change Triggers:\n\r");
                list.append("    "+Util.padRight("Type",15)
                        +" "+Util.padRight("Direction",10)
                        +" "+Util.padRight("Factor",10)
                        +" "+Util.padRight("Flags",20)
                        +" Mask\n\r");
                for(Enumeration e=me.Changes.elements();e.hasMoreElements();)
                {
                    Faction.FactionChangeEvent CE=(Faction.FactionChangeEvent)e.nextElement();
                    if(CE!=null)
                    {
                        list.append("    ");
                        list.append(Util.padRight(CE.ID,15)+" ");
                        list.append(Util.padRight(Faction.FactionChangeEvent.FACTION_DIRECTIONS[CE.direction],10)+" ");
                        list.append(Util.padRight(Math.round(CE.factor*100.0)+"%",10)+" ");
                        list.append(Util.padRight(CE.flagCache,20)+" ");
                        list.append(CE.zapper+"\n\r");
                    }
                }
                mob.tell(list.toString());
                if((showFlag!=showNumber)&&(showFlag>-999)) break;
                String which=mob.session().prompt("Select a trigger ID to add, remove, or modify (?):","");
                which=which.toUpperCase().trim();
                if(which.length()==0) break;
                if(which.equalsIgnoreCase("?"))
                {
                    mob.tell("Valid triggers: \n\r"+Faction.FactionChangeEvent.ALL_TYPES());
                    continue;
                }
                Faction.FactionChangeEvent CE=(Faction.FactionChangeEvent)me.Changes.get(which);
                if(CE==null)
                {
                    CE=new Faction.FactionChangeEvent();
                    if(!CE.setFilterID(which))
                    {
                        mob.tell("That ID is invalid.  Try '?'.");
                        continue;
                    }
                    else
                    if(!mob.session().confirm("Create a new trigger using ID '"+which+"' (y/N): ","N"))
                    {
                        CE=null;
                        break;
                    }
                    else
                        me.Changes.put(CE.ID.toUpperCase(),CE);
                }
                else
                if(mob.session().choose("Would you like to M)odify or D)elete this trigger (M/d): ","MD","M").toUpperCase().startsWith("D"))
                {
                    me.Changes.remove(CE.ID);
                    mob.tell("Trigger deleted.");
                    CE=null;
                }
                
                if(CE!=null)
                {
                    StringBuffer directions=new StringBuffer("Valid directions:\n\r");
                    StringBuffer cmds=new StringBuffer("");
                    for(int i=0;i<Faction.FactionChangeEvent.FACTION_DIRECTIONS.length;i++)
                    {
                        directions.append(((char)('A'+i))+") "+Faction.FactionChangeEvent.FACTION_DIRECTIONS[i]+"\n\r");
                        cmds.append((char)('A'+i));
                    }
                    String str=mob.session().choose(directions+"\n\rSelect a new direction ("+Faction.FactionChangeEvent.FACTION_DIRECTIONS[CE.direction]+"): ",cmds.toString()+"\n\r","");
                    if((str.length()==0)||str.equals("\n")||str.equals("\r")||(cmds.toString().indexOf(str.charAt(0))<0))
                        mob.tell("(no change)");
                    else
                        CE.direction=(cmds.toString().indexOf(str.charAt(0)));
                }
                if(CE!=null)
                {
                    if(CE.factor==0.0) CE.factor=1.0;
                    int amount=(int)Math.round(CE.factor*100.0);
                    String newName=mob.session().prompt("Enter the amount factor ("+amount+"%): ",""+amount+"%");
                    if(newName.endsWith("%")) newName=newName.substring(0,newName.length()-1);
                    if(!Util.isInteger(newName))
                        mob.tell("(no Change)");
                    else
                        CE.factor=new Double(Util.s_int(newName)/100.0).doubleValue();
                }
                if(CE!=null)
                {
                    mob.tell("Valid flags include: "+Util.toStringList(Faction.FactionChangeEvent.VALID_FLAGS)+"\n\r");
                    String newFlags=mob.session().prompt("Enter new flag(s) ("+CE.flagCache+"): ",CE.flagCache);
                    if((newFlags.length()==0)||(newFlags.equals(CE.flagCache)))
                        mob.tell("(no change)");
                    else
                        CE.setFlags(newFlags);
                }
                if(CE!=null)
                {
                    String newFlags=mob.session().prompt("Zapper mask ("+CE.zapper+"): ",CE.zapper);
                    if((newFlags.length()==0)||(newFlags.equals(CE.zapper)))
                        mob.tell("(no change)");
                    else
                        CE.zapper=newFlags;
                }
            }
            
            // Ability allowances
            ++showNumber;
            while((mob.session()!=null)&&(!mob.session().killFlag())&&(!((showFlag>0)&&(showFlag!=showNumber))))
            {
                if((showFlag>0)&&(showFlag!=showNumber)) break;
                StringBuffer list=new StringBuffer(showNumber+") Ability allowances:\n\r");
                list.append("    #) "
                        +Util.padRight("Ability masks",40)
                        +" "+Util.padRight("Low value",10)
                        +" "+Util.padRight("High value",10)
                        +"\n\r");
                int num=0;
                StringBuffer choices=new StringBuffer("0\n\r");
                for(Enumeration e=me.abilityUsages.elements();e.hasMoreElements();)
                {
                    Faction.FactionAbilityUsage CA=(Faction.FactionAbilityUsage)e.nextElement();
                    if(CA!=null)
                    {
                        list.append("    "+((char)('A'+num)+") "));
                        list.append(Util.padRight(CA.ID,40)+" ");
                        list.append(Util.padRight(CA.low+"",10)+" ");
                        list.append(Util.padRight(CA.high+"",10)+" "); 
                        list.append("\n\r");
                        choices.append((char)('A'+num));
                        num++;
                    }
                }
                mob.tell(list.toString());
                if((showFlag!=showNumber)&&(showFlag>-999)) break;
                String which=mob.session().choose("Select an allowance to remove or modify, or enter 0 to Add:",choices.toString(),"");
                if(which.length()!=1)
                    break;
                which=which.toUpperCase().trim();
                Faction.FactionAbilityUsage CA=null;
                if(!which.equalsIgnoreCase("0"))
                {
                    num=(which.charAt(0)-'A');
                    if((num<0)||(num>=me.abilityUsages.size()))
                        break;
                    CA=(Faction.FactionAbilityUsage)me.abilityUsages.elementAt(num);
                    if(CA==null)
                    {
                        mob.tell("That allowance is invalid..");
                        continue;
                    }
                    if(mob.session().choose("Would you like to M)odify or D)elete this allowance (M/d): ","MD","M").toUpperCase().startsWith("D"))
                    {
                        me.abilityUsages.remove(CA);
                        mob.tell("Allowance deleted.");
                        CA=null;
                    }
                }
                else
                if(!mob.session().confirm("Create a new allowance (y/N): ","N"))
                {
                    CA=null;
                    continue;
                }
                else
                {
                    CA=new Faction.FactionAbilityUsage();
                    me.abilityUsages.addElement(CA);
                }
                if(CA!=null)
                {
                    boolean cont=false;
                    while((!cont)&&(!mob.session().killFlag()))
                    {
                        String newFlags=mob.session().prompt("Ability determinate masks or ? ("+CA.ID+"): ",CA.ID);
                        if(newFlags.equalsIgnoreCase("?"))
                        {
                            StringBuffer vals=new StringBuffer("Valid masks: \n\r");
                            for(int i=0;i<Ability.TYPE_DESCS.length;i++)
                                vals.append(Ability.TYPE_DESCS[i]+", ");
                            for(int i=0;i<Ability.DOMAIN_DESCS.length;i++) 
                                vals.append(Ability.DOMAIN_DESCS[i]+", ");
                            for(int i=0;i< Ability.FLAG_DESCS.length;i++)
                                vals.append(Ability.FLAG_DESCS[i]+", ");
                            vals.append(" * Any ABILITY ID (skill/prayer/spell/etc)");
                            mob.tell(vals.toString());
                            cont=false;
                        }
                        else
                        {
                            cont=true;
                            if((newFlags.length()==0)||(newFlags.equals(CA.ID)))
                                mob.tell("(no change)");
                            else
                            {
                                Vector unknowns=CA.setAbilityFlag(newFlags);
                                if(unknowns.size()>0)
                                    for(int i=unknowns.size()-1;i>=0;i--)
                                        if(CMClass.getAbility((String)unknowns.elementAt(i))!=null)
                                            unknowns.removeElementAt(i);
                                if(unknowns.size()>0)
                                {
                                    mob.tell("The following are unknown masks: '"+Util.toStringList(unknowns)+"'.  Please correct them.");
                                    cont=false;
                                }
                            }
                        }
                    }
                    String newName=mob.session().prompt("Enter the minimum value to use the ability ("+CA.low+"): ",""+CA.low);
                    if((!Util.isInteger(newName))||(CA.low==Util.s_int(newName)))
                        mob.tell("(no Change)");
                    else
                        CA.low=Util.s_int(newName);
                    newName=mob.session().prompt("Enter the maximum value to use the ability ("+CA.high+"): ",""+CA.high);
                    if((!Util.isInteger(newName))||(CA.high==Util.s_int(newName)))
                        mob.tell("(no Change)");
                    else
                        CA.high=Util.s_int(newName);
                    if(CA.high<CA.low) CA.high=CA.low;
                }
            }
            
            // calculate new max/min
            me.minimum=Integer.MAX_VALUE;
            me.maximum=Integer.MIN_VALUE;
            for(int r=0;r<me.ranges.size();r++)
            {
                Faction.FactionRange FR=(Faction.FactionRange)me.ranges.elementAt(r);
                if(FR.high>me.maximum) me.maximum=FR.high;
                if(FR.low<me.minimum) me.minimum=FR.low;
            }
            if(me.minimum==Integer.MAX_VALUE) me.minimum=Integer.MIN_VALUE;
            if(me.maximum==Integer.MIN_VALUE) me.maximum=Integer.MAX_VALUE;
            if(me.maximum<me.minimum)
            {
                int oldMin=me.minimum;
                me.minimum=me.maximum;
                me.maximum=oldMin;
            }
            me.middle=me.minimum+(int)Math.round(Util.div(me.maximum-me.minimum,2.0));
            me.difference=Util.abs(me.maximum-me.minimum);
            
            
            
            if(showFlag<-900){ ok=true; break;}
            if(showFlag>0){ showFlag=-1; continue;}
            showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
            if(showFlag<=0)
            {
                showFlag=-1;
                ok=true;
            }
        }
        if((me.ID.length()>0)&&(Factions.getFaction(me.ID)!=null))
        {
            Vector oldV=Resources.getFileLineVector(Resources.getFileResource(me.ID));
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
                    int tagRef=Faction.isTag(tag);
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
                        first=Faction.isTag(s.substring(first).trim().toUpperCase());
                        if(first>=0) lastCommented=first;
                    }
                }
                else
                {
                    String tag=s.substring(0,s.indexOf("=")).trim().toUpperCase();
                    int tagRef=Faction.isTag(tag);
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
            Resources.removeResource(me.ID);
            Resources.submitResource(me.ID,buf);
            if(!Resources.saveFileResource(me.ID))
                mob.tell("Faction File '"+me.ID+"' could not be modified.  Make sure it is not READ-ONLY.");
        }
    }

	public static void modifyGenRace(MOB mob, Race me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		int showFlag=-1;
		if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while(!ok)
		{
			int showNumber=0;
			genText(mob,me,++showNumber,showFlag,"Name","NAME");
			genCat(mob,me,++showNumber,showFlag);
			genInt(mob,me,++showNumber,showFlag,"Base Weight","BWEIGHT");
			genInt(mob,me,++showNumber,showFlag,"Weight Variance","VWEIGHT");
			genInt(mob,me,++showNumber,showFlag,"Base Male Height","MHEIGHT");
			genInt(mob,me,++showNumber,showFlag,"Base Female Height","FHEIGHT");
			genInt(mob,me,++showNumber,showFlag,"Height Variance","VHEIGHT");
			genRaceAvailability(mob,me,++showNumber,showFlag);
			genText(mob,me,++showNumber,showFlag,"Leaving text","LEAVE");
			genText(mob,me,++showNumber,showFlag,"Arriving text","ARRIVE");
			genHealthBuddy(mob,me,++showNumber,showFlag);
			genBodyParts(mob,me,++showNumber,showFlag);
			genAgingChart(mob,me,++showNumber,showFlag);
            genBool(mob,me,++showNumber,showFlag,"Never create corpse","BODYKILL");
			genEStats(mob,me,++showNumber,showFlag);
			genAStats(mob,me,"ASTATS","CharStat Adjustments",++showNumber,showFlag);
			genAStats(mob,me,"CSTATS","CharStat Settings",++showNumber,showFlag);
			genAState(mob,me,"ASTATE","CharState Adjustments",++showNumber,showFlag);
			genAState(mob,me,"STARTASTATE","New Player CharState Adj.",++showNumber,showFlag);
			genRaceFlags(mob,me,++showNumber,showFlag);
			genResources(mob,me,++showNumber,showFlag);
			genOutfit(mob,me,++showNumber,showFlag);
			genWeapon(mob,me,++showNumber,showFlag);
			genRacialAbilities(mob,me,++showNumber,showFlag);
			genCulturalAbilities(mob,me,++showNumber,showFlag);
			//genRacialEffects(mob,me,++showNumber,showFlag);
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
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
		if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while(!ok)
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
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				me.recoverEnvStats();
				if(me.text().length()>=maxLength)
				{
					mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
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
		if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while(!ok)
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
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				me.recoverEnvStats();
				if(me.text().length()>=maxLength)
				{
					mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
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
		if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while(!ok)
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
				((Perfume)me).setSmellList(EnglishParser.promptText(mob,((Perfume)me).getSmellList(),++showNumber,showFlag,"Smells list (; delimited)"));
			genImage(mob,me,++showNumber,showFlag);
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				me.recoverEnvStats();
				if(me.text().length()>=maxLength)
				{
					mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
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
		if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while(!ok)
		{
			int showNumber=0;
			genName(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			genReadable1(mob,me,++showNumber,showFlag);
			genReadable2(mob,me,++showNumber,showFlag);
			if(me instanceof Light)	genBurnout(mob,(Light)me,++showNumber,showFlag);
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				me.recoverEnvStats();
				if(me.text().length()>=maxLength)
				{
					mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
					ok=false;
				}
			}
		}
	}

	public static void modifyGenMap(MOB mob, com.planet_ink.coffee_mud.interfaces.Map me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		int showFlag=-1;
		if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while(!ok)
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
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				me.recoverEnvStats();
				if(me.text().length()>=maxLength)
				{
					mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
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
		if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while(!ok)
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
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				me.recoverEnvStats();
				if(me.text().length()>=maxLength)
				{
					mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
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
		if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while(!ok)
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
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				me.recoverEnvStats();
				if(me.text().length()>=maxLength)
				{
					mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
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
		if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while(!ok)
		{
			int showNumber=0;
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			genLevel(mob,me,++showNumber,showFlag);
			genMaterialCode(mob,me,++showNumber,showFlag);
			genWornLocation(mob,me,++showNumber,showFlag);
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
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				me.recoverEnvStats();
				if(me.text().length()>=maxLength)
				{
					mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
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
		if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while(!ok)
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
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				me.recoverEnvStats();
				if(me.text().length()>=maxLength)
				{
					mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
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
		if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while(!ok)
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
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				me.recoverEnvStats();
				if(me.text().length()>=maxLength)
				{
					mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
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
		if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while(!ok)
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
			for(Enumeration e=Factions.factionSet.elements();e.hasMoreElements();)
			{
			    F=(Faction)e.nextElement();
			    if((!F.hasFaction(me))&&(F.findAutoDefault(me)!=Integer.MAX_VALUE))
			        mob.addFaction(F.ID,F.findAutoDefault(me));
			    if(F.showineditor)
				    genSpecialFaction(mob,me,++showNumber,showFlag,F);
			}
			genGender(mob,me,++showNumber,showFlag);
			genHeight(mob,me,++showNumber,showFlag);
			genWeight(mob,me,++showNumber,showFlag);
			genClan(mob,me,++showNumber,showFlag);
			genSpeed(mob,me,++showNumber,showFlag);
			if((oldLevel<2)&&(me.baseEnvStats().level()>1))
				me.baseEnvStats().setDamage((int)Math.round(Util.div(me.baseEnvStats().damage(),me.baseEnvStats().speed())));
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
			}
			genFaction(mob,me,++showNumber,showFlag);
			genTattoos(mob,me,++showNumber,showFlag);
			genEducations(mob,me,++showNumber,showFlag);
			genImage(mob,me,++showNumber,showFlag);
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
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
					mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
					ok=false;
				}
				me.setMiscText(me.text());
			}
		}

		mob.tell("\n\rNow don't forget to equip "+me.charStats().himher()+" with stuff before saving!\n\r");
	}

	public static void modifyPlayer(MOB mob, MOB me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		int showFlag=-1;
		if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		String oldName=me.Name();
		while(!ok)
		{
			int showNumber=0;
			genName(mob,me,++showNumber,showFlag);
			while((!me.Name().equals(oldName))&&(CMClass.DBEngine().DBUserSearch(null,me.Name())))
			{
				mob.tell("The name given cannot be chosen, as it is already being used.");
				genName(mob,me,showNumber,showFlag);
			}
			genPassword(mob,me,++showNumber,showFlag);

			genDescription(mob,me,++showNumber,showFlag);
			genLevel(mob,me,++showNumber,showFlag);
			genRace(mob,me,++showNumber,showFlag);
			genCharClass(mob,me,++showNumber,showFlag);
			genCharStats(mob,me,++showNumber,showFlag);
			Faction F=null;
			for(Enumeration e=Factions.factionSet.elements();e.hasMoreElements();)
			{
			    F=(Faction)e.nextElement();
			    if((!F.hasFaction(me))&&(F.findAutoDefault(me)!=Integer.MAX_VALUE))
			        mob.addFaction(F.ID,F.findAutoDefault(me));
			    if(F.showineditor)
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
            me.setTrains(EnglishParser.promptInteger(mob,me.getTrains(),++showNumber,showFlag,"Training Points"));
            me.setPractices(EnglishParser.promptInteger(mob,me.getPractices(),++showNumber,showFlag,"Practice Points"));
            me.setQuestPoint(EnglishParser.promptInteger(mob,me.getQuestPoint(),++showNumber,showFlag,"Quest Points"));
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
			genEducations(mob,me,++showNumber,showFlag);
			genTitles(mob,me,++showNumber,showFlag);
			genEmail(mob,me,++showNumber,showFlag);
			genSecurity(mob,me,++showNumber,showFlag);
			genImage(mob,me,++showNumber,showFlag);
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
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
					CMClass.DBEngine().DBDeleteMOB(fakeMe);
					CMClass.DBEngine().DBCreateCharacter(me);
				}
				CMClass.DBEngine().DBUpdatePlayer(me);
				CMClass.DBEngine().DBUpdateFollowers(me);
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
		if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while(!ok)
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
			for(Enumeration e=Factions.factionSet.elements();e.hasMoreElements();)
			{
			    F=(Faction)e.nextElement();
			    if((!F.hasFaction((MOB)me))&&(F.findAutoDefault((MOB)me)!=Integer.MAX_VALUE))
			        mob.addFaction(F.ID,F.findAutoDefault((MOB)me));
			    if(F.showineditor)
				    genSpecialFaction(mob,(MOB)me,++showNumber,showFlag,F);
			}
			genGender(mob,mme,++showNumber,showFlag);
			genClan(mob,mme,++showNumber,showFlag);
			genSpeed(mob,me,++showNumber,showFlag);
			if((oldLevel<2)&&(me.baseEnvStats().level()>1))
				me.baseEnvStats().setDamage((int)Math.round(Util.div(me.baseEnvStats().damage(),me.baseEnvStats().speed())));
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
			}
			else
            if(me instanceof PostOffice)
            {
                ((PostOffice)me).setPostalChain(EnglishParser.promptText(mob,((PostOffice)me).postalChain(),++showNumber,showFlag,"Postal chain"));
                ((PostOffice)me).setFeeForNewBox(EnglishParser.promptDouble(mob,((PostOffice)me).feeForNewBox(),++showNumber,showFlag,"Fee to open a new box"));
                ((PostOffice)me).setMinimumPostage(EnglishParser.promptDouble(mob,((PostOffice)me).minimumPostage(),++showNumber,showFlag,"Minimum postage cost"));
                ((PostOffice)me).setPostagePerPound(EnglishParser.promptDouble(mob,((PostOffice)me).postagePerPound(),++showNumber,showFlag,"Postage cost per pound after 1st pound"));
                ((PostOffice)me).setHoldFeePerPound(EnglishParser.promptDouble(mob,((PostOffice)me).holdFeePerPound(),++showNumber,showFlag,"Holding fee per pound per month"));
                ((PostOffice)me).setMaxMudMonthsHeld(EnglishParser.promptInteger(mob,((PostOffice)me).maxMudMonthsHeld(),++showNumber,showFlag,"Maximum number of months held"));
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
			genEducations(mob,(MOB)me,++showNumber,showFlag);
			genImage(mob,me,++showNumber,showFlag);
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
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
					mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
					ok=false;
				}
				me.setMiscText(me.text());
			}
		}
		mob.tell("\n\rNow don't forget to equip him with non-generic items before saving! If you DO add items to his list, be sure to come back here in case you've exceeded the string limit again.\n\r");
	}
}
