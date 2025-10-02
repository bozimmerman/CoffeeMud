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
   Copyright 2002-2025 Bo Zimmerman

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
	public enum ExitDataField
	{
		NAME,CLASSES,DISPLAYTEXT,DESCRIPTION,
		LEVEL,LEVELRESTRICTED,ISTRAPPED,HASADOOR,
		CLOSEDTEXT,DEFAULTSCLOSED,OPENWORD,CLOSEWORD,
		HASALOCK,DEFAULTSLOCKED,KEYNAME,ISREADABLE,
		READABLETEXT,ISCLASSRESTRICTED,RESTRICTEDCLASSES,
		ISALIGNMENTRESTRICTED,RESTRICTEDALIGNMENTS,
		MISCTEXT,ISGENERIC,DOORNAME,IMAGE,OPENTICKS,
		TAGS
	}

	public static String dispositions(final Physical P, final HTTPRequest httpReq, final java.util.Map<String,String> parms)
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

	public static String editExit(Room R, final int dir,final HTTPRequest httpReq, final java.util.Map<String,String> parms)
	{
		synchronized(CMClass.getSync("SYNC"+R.roomID()))
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

			for (final ExitDataField field : ExitDataField.values())
			{
				String parm=field.name();
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
				{
					switch(field)
					{
					case NAME: // name
						X.setName(old);
						break;
					case CLASSES: // classes
						break;
					case DISPLAYTEXT: // displaytext
						X.setDisplayText(old);
						break;
					case DESCRIPTION: // description
						X.setDescription(old);
						break;
					case LEVEL: // level
						X.basePhyStats().setLevel(CMath.s_int(old));
						break;
					case LEVELRESTRICTED: // levelrestricted;
						break;
					case ISTRAPPED: // istrapped
						break;
					case HASADOOR: // hasadoor
						if(old.equals("on"))
							X.setDoorsNLocks(true,!X.defaultsClosed(),X.defaultsClosed(),X.hasALock(),X.hasALock(),X.defaultsLocked());
						else
							X.setDoorsNLocks(false,true,false,false,false,false);
						break;
					case CLOSEDTEXT: // closedtext
						X.setExitParams(X.doorName(),X.closeWord(),X.openWord(),old);
						break;
					case DEFAULTSCLOSED: // defaultsclosed
						X.setDoorsNLocks(X.hasADoor(),X.isOpen(),old.equals("on"),X.hasALock(),X.isLocked(),X.defaultsLocked());
						break;
					case OPENWORD: // openword
						X.setExitParams(X.doorName(),X.closeWord(),old,X.closedText());
						break;
					case CLOSEWORD: // closeword
						X.setExitParams(X.doorName(),old,X.openWord(),X.closedText());
						break;
					case HASALOCK: // hasalock
						if(old.equals("on"))
							X.setDoorsNLocks(true,!X.defaultsClosed(),X.defaultsClosed(),true,X.defaultsLocked(),X.defaultsLocked());
						else
							X.setDoorsNLocks(X.hasADoor(),X.isOpen(),X.defaultsClosed(),false,false,false);
						break;
					case DEFAULTSLOCKED: // defaultslocked
						X.setDoorsNLocks(X.hasADoor(),X.isOpen(),X.defaultsClosed(),X.hasALock(),X.isLocked(),old.equals("on"));
						break;
					case KEYNAME: // keyname
						if(X.hasALock()&&(old.length()>0))
							X.setKeyName(old);
						break;
					case ISREADABLE: // isreadable
						X.setReadable(old.equals("on"));
						break;
					case READABLETEXT: // readable text
						if(X.isReadable())
							X.setReadableText(CMStrings.fixMudCRLF(old));
						break;
					case ISCLASSRESTRICTED: // isclassrestricuted
						break;
					case RESTRICTEDCLASSES: // restrictedclasses
						break;
					case ISALIGNMENTRESTRICTED: // isalignmentrestricuted
						break;
					case RESTRICTEDALIGNMENTS: // restrictedalignments
						break;
					case MISCTEXT: // misctext
						if(!X.isGeneric())
							X.setMiscText(old);
						break;
					case ISGENERIC: // is generic
						break;
					case DOORNAME: // door name
						X.setExitParams(old,X.closeWord(),X.openWord(),X.closedText());
						break;
					case IMAGE: // image
						X.setImage(old);
						break;
					case OPENTICKS:
						X.setOpenDelayTicks(CMath.s_int(old));
						break;
					case TAGS: // tags
						{
							final List<String> V=CMParms.parseSemicolons(old,true);
							for(final Enumeration<String> e=X.tags();e.hasMoreElements();)
								X.delTag(e.nextElement());
							for(final String tatt : V)
								X.addTag(tatt);
						}
						break;
					}
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
				Room R2=CMLib.map().getRoom(R.rawDoors()[dir]);
				if((R2!=null)
				&&(R2.ID().equals("ThinRoom")))
				{
					if(R2.roomID().length()>0)
						R2=CMLib.map().getRoom(R2.roomID());
					if(R2==null)
						R2=CMLib.map().getRoom(R.getRoomInDir(dir));
				}
				Exit E2=R.getReverseExit(dir);
				if((R2!=null)&&(E2!=null))
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

	public static String delExit(Room R, final int dir)
	{
		synchronized(CMClass.getSync("SYNC"+R.roomID()))
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

	public static String linkRooms(Room R, Room R2, final int dir, final int dir2)
	{
		synchronized(CMClass.getSync("SYNC"+R.roomID()))
		{
			R=CMLib.map().getRoom(R);
			if((R!=null)
			&&(R.ID().equals("ThinRoom")))
				R=CMLib.map().getRoom(R.roomID());
			if(R==null)
				return "Failed exit!";
			if(dir>=R.rawDoors().length)
				return "";
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
		synchronized(CMClass.getSync("SYNC"+R2.roomID()))
		{
			R2=CMLib.map().getRoom(R2);
			if((R2!=null)
			&&(R2.ID().equals("ThinRoom")))
				R2=CMLib.map().getRoom(R.getRoomInDir(dir));
			if(R2==null)
				return "Failed exit2!";
			if(dir2>=R2.rawDoors().length)
				return "";
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

	public static String createExitForRoom(Room R, final int dir)
	{
		synchronized(CMClass.getSync("SYNC"+R.roomID()))
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
