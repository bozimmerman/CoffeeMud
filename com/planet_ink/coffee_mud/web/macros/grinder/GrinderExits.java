package com.planet_ink.coffee_mud.web.macros.grinder;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class GrinderExits
{
	public static String dispositions(Environmental E, ExternalHTTPRequests httpReq, Hashtable parms)
	{
		E.baseEnvStats().setDisposition(0);
		for(int d=0;d<EnvStats.dispositionsNames.length;d++)
		{
			String parm=httpReq.getRequestParameter(EnvStats.dispositionsNames[d]);
			if((parm!=null)&&(parm.equals("on")))
			   E.baseEnvStats().setDisposition(E.baseEnvStats().disposition()|(1<<d));
		}
		return "";
	}
	
	public static String editExit(Room R, int dir,ExternalHTTPRequests httpReq, Hashtable parms)
	{
		Exit E=R.rawExits()[dir];
		if(E==null) return "No Exit to edit?!";
		
		// important generic<->non generic swap!
		String newClassID=httpReq.getRequestParameter("CLASSES");
		if((newClassID!=null)&&(!CMClass.className(E).equals(newClassID)))
		{
			E=CMClass.getExit(newClassID);
			R.rawExits()[dir]=E;
		}
		
		String[] okparms={"NAME"," CLASSES","DISPLAYTEXT","DESCRIPTION",
						  "LEVEL","LEVELRESTRICTED","ISTRAPPED","HASADOOR",
						  "CLOSEDTEXT","DEFAULTSCLOSED","OPENWORD","CLOSEWORD",
						  "HASALOCK","DEFAULTSLOCKED","KEYNAME","ISREADABLE",
						  "READABLETEXT","ISCLASSRESTRICTED","RESTRICTEDCLASSES",
						  "ISALIGNMENTRESTRICTED","RESTRICTEDALIGNMENTS",
						  " MISCTEXT","ISGENERIC","DOORNAME"};
		for(int o=0;o<okparms.length;o++)
		{
			String parm=okparms[o];
			boolean generic=true;
			if(parm.startsWith(" "))
			{
				generic=false;
				parm=parm.substring(1);
			}
			String old=httpReq.getRequestParameter(parm);
			if(old==null) old="";
			if(E.isGeneric()||(!generic))
			switch(o)
			{
			case 0: // name
				E.setName(old);	
				break;
			case 1: // classes
				break;
			case 2: // displaytext
				E.setDisplayText(old);	
				break;
			case 3: // description
				E.setDescription(old); 
				break;
			case 4: // level
				E.baseEnvStats().setLevel(Util.s_int(old));	
				break;
			case 5: // levelrestricted;
				break;
			case 6: // istrapped
				break;
			case 7: // hasadoor
				if(old.equals("on"))
					E.setDoorsNLocks(true,!E.defaultsClosed(),E.defaultsClosed(),E.hasALock(),E.hasALock(),E.defaultsLocked());
				else
					E.setDoorsNLocks(false,true,false,false,false,false);
				break;
			case 8: // closedtext
				E.setExitParams(E.doorName(),E.closeWord(),E.openWord(),old); 
				break;
			case 9: // defaultsclosed
				E.setDoorsNLocks(E.hasADoor(),E.isOpen(),old.equals("on"),E.hasALock(),E.isLocked(),E.defaultsLocked());
				break;
			case 10: // openword
				E.setExitParams(E.doorName(),E.closeWord(),old,E.closedText());	
				break;
			case 11: // closeword
				E.setExitParams(E.doorName(),old,E.openWord(),E.closedText());	
				break;
			case 12: // hasalock
				if(old.equals("on"))
					E.setDoorsNLocks(true,!E.defaultsClosed(),E.defaultsClosed(),true,E.defaultsLocked(),E.defaultsLocked());
				else
					E.setDoorsNLocks(E.hasADoor(),E.isOpen(),E.defaultsClosed(),false,false,false);
				break;
			case 13: // defaultslocked
				E.setDoorsNLocks(E.hasADoor(),E.isOpen(),E.defaultsClosed(),E.hasALock(),E.isLocked(),old.equals("on"));
				break;
			case 14: // keyname
				if(E.hasALock()&&(old.length()>0))
					E.setKeyName(old);
				break;
			case 15: // isreadable
				E.setReadable(old.equals("on"));
				break;
			case 16: // readable text
				if(E.isReadable()) E.setReadableText(old);
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
				if(!E.isGeneric())
					E.setMiscText(old); 
				break;
			case 22: // is generic
				break;
			case 23: // door name
				E.setExitParams(old,E.closeWord(),E.openWord(),E.closedText());
				break;
			}
		}
		
		if(E.isGeneric())
		{
			String error=GrinderExits.dispositions(E,httpReq,parms);
			if(error.length()>0) return error;
			error=GrinderAreas.doAffectsNBehavs(E,httpReq,parms);
			if(error.length()>0) return error;
		}
		
		//adjustments
		if(!E.hasADoor())
			E.setDoorsNLocks(false,true,false,false,false,false);
				
		ExternalPlay.DBUpdateExits(R);
		String makeSame=httpReq.getRequestParameter("MAKESAME");
		if((makeSame!=null)&&(makeSame.equalsIgnoreCase("on")))
		{
			Room R2=R.rawDoors()[dir];
			Exit E2=null;
			if((R2!=null)&&(R2.rawDoors()[Directions.getOpDirectionCode(dir)]==R))
				E2=R2.rawExits()[Directions.getOpDirectionCode(dir)];
			if(E2!=null)
			{
				Exit oldE2=E2;
				E2=(Exit)E.copyOf();
				E2.setDisplayText(oldE2.displayText());
				R2.rawExits()[Directions.getOpDirectionCode(dir)]=E2;
				ExternalPlay.DBUpdateExits(R2);
				R.getArea().fillInAreaRoom(R2);
			}
			R.getArea().fillInAreaRoom(R);
		}
		return "";
	}
	public static String delExit(Room R, int dir)
	{
		R.rawDoors()[dir]=null;
		R.rawExits()[dir]=null;
		ExternalPlay.DBUpdateExits(R);
		if(R instanceof GridLocale)
			((GridLocale)R).buildGrid();
		return "";
	}
	
	public static String linkRooms(Room R, Room R2, int dir, int dir2)
	{
		R.clearSky();
		R2.clearSky();
		if(R instanceof GridLocale)
			((GridLocale)R).clearGrid();
		if(R2 instanceof GridLocale)
			((GridLocale)R2).clearGrid();
		
		if(R.rawDoors()[dir]==null) R.rawDoors()[dir]=R2;
		if(R2.rawDoors()[dir2]==null) R2.rawDoors()[dir2]=R;
			
		if(R.rawExits()[dir]==null)
			R.rawExits()[dir]=CMClass.getExit("StdOpenDoorway");
		if(R2.rawExits()[dir2]==null)
			R2.rawExits()[dir2]=CMClass.getExit("StdOpenDoorway");
		
		ExternalPlay.DBUpdateExits(R);
		ExternalPlay.DBUpdateExits(R2);
			
		R.getArea().fillInAreaRoom(R);
		R.getArea().fillInAreaRoom(R2);
		return "";
	}
}
