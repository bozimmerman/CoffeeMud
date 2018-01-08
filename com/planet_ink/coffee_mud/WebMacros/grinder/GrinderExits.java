package com.planet_ink.coffee_mud.WebMacros.grinder;

import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;

/*
   Copyright 2002-2018 Bo Zimmerman

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
public class GrinderExits
{
	private static final String[] okparms=
	{
	  "NAME"," CLASSES","DISPLAYTEXT","DESCRIPTION",
	  "LEVEL","LEVELRESTRICTED","ISTRAPPED","HASADOOR",
	  "CLOSEDTEXT","DEFAULTSCLOSED","OPENWORD","CLOSEWORD",
	  "HASALOCK","DEFAULTSLOCKED","KEYNAME","ISREADABLE",
	  "READABLETEXT","ISCLASSRESTRICTED","RESTRICTEDCLASSES",
	  "ISALIGNMENTRESTRICTED","RESTRICTEDALIGNMENTS",
	  " MISCTEXT","ISGENERIC","DOORNAME","IMAGE","OPENTICKS"
	};

	public static String dispositions(Physical P, HTTPRequest httpReq, java.util.Map<String,String> parms)
	{
		P.basePhyStats().setDisposition(0);
		for(int d=0;d<PhyStats.IS_CODES.length;d++)
		{
			final String parm=httpReq.getUrlParameter(PhyStats.IS_CODES[d]);
			if((parm!=null)&&(parm.equals("on")))
				P.basePhyStats().setDisposition(P.basePhyStats().disposition()|(1<<d));
		}
		return "";
	}

	public static String editExit(Room R, int dir,HTTPRequest httpReq, java.util.Map<String,String> parms)
	{
		synchronized(("SYNC"+R.roomID()).intern())
		{
			R=CMLib.map().getRoom(R);
			Exit X=R.getRawExit(dir);

			// important generic<->non generic swap!
			final String newClassID=httpReq.getUrlParameter("CLASSES");
			if((newClassID!=null)&&(!CMClass.classID(X).equals(newClassID)))
			{
				X=CMClass.getExit(newClassID);
				R.setRawExit(dir,X);
			}
			if(X==null)
				return "No Exit to edit?!";

			for(int o=0;o<okparms.length;o++)
			{
				String parm=okparms[o];
				boolean generic=true;
				if(parm.startsWith(" "))
				{
					generic=false;
					parm=parm.substring(1);
				}
				String old=httpReq.getUrlParameter(parm);
				if(old==null)
					old="";
				if(X.isGeneric()||(!generic))
				switch(o)
				{
				case 0: // name
					X.setName(old);
					break;
				case 1: // classes
					break;
				case 2: // displaytext
					X.setDisplayText(old);
					break;
				case 3: // description
					X.setDescription(old);
					break;
				case 4: // level
					X.basePhyStats().setLevel(CMath.s_int(old));
					break;
				case 5: // levelrestricted;
					break;
				case 6: // istrapped
					break;
				case 7: // hasadoor
					if(old.equals("on"))
						X.setDoorsNLocks(true,!X.defaultsClosed(),X.defaultsClosed(),X.hasALock(),X.hasALock(),X.defaultsLocked());
					else
						X.setDoorsNLocks(false,true,false,false,false,false);
					break;
				case 8: // closedtext
					X.setExitParams(X.doorName(),X.closeWord(),X.openWord(),old);
					break;
				case 9: // defaultsclosed
					X.setDoorsNLocks(X.hasADoor(),X.isOpen(),old.equals("on"),X.hasALock(),X.isLocked(),X.defaultsLocked());
					break;
				case 10: // openword
					X.setExitParams(X.doorName(),X.closeWord(),old,X.closedText());
					break;
				case 11: // closeword
					X.setExitParams(X.doorName(),old,X.openWord(),X.closedText());
					break;
				case 12: // hasalock
					if(old.equals("on"))
						X.setDoorsNLocks(true,!X.defaultsClosed(),X.defaultsClosed(),true,X.defaultsLocked(),X.defaultsLocked());
					else
						X.setDoorsNLocks(X.hasADoor(),X.isOpen(),X.defaultsClosed(),false,false,false);
					break;
				case 13: // defaultslocked
					X.setDoorsNLocks(X.hasADoor(),X.isOpen(),X.defaultsClosed(),X.hasALock(),X.isLocked(),old.equals("on"));
					break;
				case 14: // keyname
					if(X.hasALock()&&(old.length()>0))
						X.setKeyName(old);
					break;
				case 15: // isreadable
					X.setReadable(old.equals("on"));
					break;
				case 16: // readable text
					if(X.isReadable())
						X.setReadableText(old);
					break;
				case 17: // isclassrestricuted
					break;
				case 18: // restrictedclasses
					break;
				case 19: // isalignmentrestricuted
					break;
				case 20: // restrictedalignments
					break;
				case 21: // misctext
					if(!X.isGeneric())
						X.setMiscText(old);
					break;
				case 22: // is generic
					break;
				case 23: // door name
					X.setExitParams(old,X.closeWord(),X.openWord(),X.closedText());
					break;
				case 24: // image
					X.setImage(old);
					break;
				case 25:
					X.setOpenDelayTicks(CMath.s_int(old));
					break;
				}
			}

			if(X.isGeneric())
			{
				String error=GrinderExits.dispositions(X,httpReq,parms);
				if(error.length()>0)
					return error;
				error=GrinderAreas.doAffects(X,httpReq,parms);
				if(error.length()>0)
					return error;
				error=GrinderAreas.doBehavs(X,httpReq,parms);
				if(error.length()>0)
					return error;
			}

			//adjustments
			if(!X.hasADoor())
				X.setDoorsNLocks(false,true,false,false,false,false);

			CMLib.database().DBUpdateExits(R);
			final String makeSame=httpReq.getUrlParameter("MAKESAME");
			if((makeSame!=null)&&(makeSame.equalsIgnoreCase("on")))
			{
				final Room R2=R.rawDoors()[dir];
				Exit E2=null;
				if((R2!=null)&&(R2.rawDoors()[Directions.getOpDirectionCode(dir)]==R))
					E2=R2.getRawExit(Directions.getOpDirectionCode(dir));
				if(E2!=null)
				{
					final Exit oldE2=E2;
					E2=(Exit)X.copyOf();
					E2.setDisplayText(oldE2.displayText());
					if(R2!=null)
					{
						R2.setRawExit(Directions.getOpDirectionCode(dir),E2);
						CMLib.database().DBUpdateExits(R2);
						R.getArea().fillInAreaRoom(R2);
					}
				}
				R.getArea().fillInAreaRoom(R);
			}
		}
		return "";
	}

	public static String delExit(Room R, int dir)
	{
		synchronized(("SYNC"+R.roomID()).intern())
		{
			R=CMLib.map().getRoom(R);
			R.rawDoors()[dir]=null;
			R.setRawExit(dir,null);
			CMLib.database().DBUpdateExits(R);
			if(R instanceof GridLocale)
				((GridLocale)R).buildGrid();
		}
		return "";
	}

	public static String linkRooms(Room R, Room R2, int dir, int dir2)
	{
		synchronized(("SYNC"+R.roomID()).intern())
		{
			R=CMLib.map().getRoom(R);
			R.clearSky();
			if(R instanceof GridLocale)
				((GridLocale)R).clearGrid(null);

			if(R.rawDoors()[dir]==null)
				R.rawDoors()[dir]=R2;

			if(R.getRawExit(dir)==null)
				R.setRawExit(dir,CMClass.getExit("StdOpenDoorway"));

			CMLib.database().DBUpdateExits(R);

			R.getArea().fillInAreaRoom(R);
		}
		synchronized(("SYNC"+R2.roomID()).intern())
		{
			R2=CMLib.map().getRoom(R2);
			R2.clearSky();
			if(R2 instanceof GridLocale)
				((GridLocale)R2).clearGrid(null);
			if(R2.rawDoors()[dir2]==null)
				R2.rawDoors()[dir2]=R;
			if(R2.getRawExit(dir2)==null)
				R2.setRawExit(dir2,CMClass.getExit("StdOpenDoorway"));
			R.getArea().fillInAreaRoom(R2);
			CMLib.database().DBUpdateExits(R2);
		}
		return "";
	}
	
	public static String createExitForRoom(Room R, int dir)
	{
		synchronized(("SYNC"+R.roomID()).intern())
		{
			R=CMLib.map().getRoom(R);
			R.clearSky();
			if(R instanceof GridLocale)
				((GridLocale)R).clearGrid(null);

			if(R.getRawExit(dir)==null)
				R.setRawExit(dir,CMClass.getExit("StdOpenDoorway"));

			CMLib.database().DBUpdateExits(R);

			R.getArea().fillInAreaRoom(R);
		}
		return "";
	}
}
