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
		StringBuffer str=new StringBuffer("");
		String[] dispositions={"ISSEEN",
							   "ISHIDDEN",
							   "ISINVISIBLE",
							   "ISEVIL",
							   "ISGOOD",
							   "ISSNEAKING",
							   "ISBONUS",
							   "ISDARK",
							   "ISINFRARED",
							   "ISSLEEPING",
							   "ISSITTING",
							   "ISFLYING",
							   "ISSWIMMING",
							   "ISLIGHT",
							   "ISCLIMBING",
							   "ISFALLING"};
		for(int d=0;d<dispositions.length;d++)
		{
			String parm=(String)httpReq.getRequestParameters().get(dispositions[d]);
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
		String newClassID=(String)httpReq.getRequestParameters().get("CLASSES");
		if((newClassID!=null)&&(!CMClass.className(E).equals(newClassID)))
		{
			E=CMClass.getExit(newClassID);
			R.rawExits()[dir]=E;
		}
		
		StringBuffer str=new StringBuffer("");
		String[] okparms={"NAME"," CLASSES","DISPLAYTEXT","DESCRIPTION",
						  "LEVEL","LEVELRESTRICTED","ISTRAPPED","HASADOOR",
						  "CLOSEDTEXT","DEFAULTSCLOSED","OPENWORD","CLOSEWORD",
						  "HASALOCK","DEFAULTSLOCKED","KEYNAME","ISREADABLE",
						  "READABLETEXT","ISCLASSRESTRICTED","RESTRICTEDCLASSES",
						  "ISALIGNMENTRESTRICTED","RESTRICTEDALIGNMENTS"," MISCTEXT","ISGENERIC"};
		for(int o=0;o<okparms.length;o++)
		{
			String parm=okparms[o];
			boolean generic=true;
			if(parm.startsWith(" "))
			{
				generic=false;
				parm=parm.substring(1);
			}
			String old=(String)httpReq.getRequestParameters().get(parm);
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
				E.setLevelRestricted(old.equals("on")); 
				break;
			case 6: // istrapped
				E.setTrapped(old.equals("on")); 
				break;
			case 7: // hasadoor
				if(old.equals("on"))
					E.setDoorsNLocks(true,false,E.defaultsClosed(),E.hasALock(),E.hasALock(),E.defaultsLocked());
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
					E.setDoorsNLocks(true,false,E.defaultsClosed(),true,true,E.defaultsLocked());
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
				E.setClassRestricted(old.equals("on"));
				break;
			case 18: // restrictedclasses
				if(E.classRestricted())
					E.setClassRestrictedName(old);
				break;
			case 19: // isalignmentrestricuted
				E.setAlignmentRestricted(old.equals("on"));
				break;
			case 20: // restrictedalignments
				String mask=((String)httpReq.getRequestParameters().get("RESTRICTEDALIGNMENTS"))+" ";
				String[] alignments={"GOOD","NEUTRAL","EVIL"};
				for(int i=1;;i++)
				{
					String selection=(String)httpReq.getRequestParameters().get("RESTRICTEDALIGNMENTS"+i);
					if(selection!=null)
						mask+=selection+" ";
					else
						break;
				}
				if(E.alignmentRestricted())
					E.setAlignmentRestrictedMask(mask.trim());
				break;
			case 21: // misctext
				if(!E.isGeneric())
					E.setMiscText(old); 
				break;
			case 22: // is generic
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
		if(E.hasADoor())
		{
			E.setClassRestricted(false);
			E.setAlignmentRestricted(false);
			if(E.hasALock())
				E.setReadable(false);
		}
		else
		{
			E.setDoorsNLocks(false,true,false,false,false,false);
			E.setReadable(false);
			if(E.classRestricted())
				E.setAlignmentRestricted(false);
			if(E.alignmentRestricted())
				E.setClassRestricted(false);
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
		if(R.rawDoors()[dir]==null) R.rawDoors()[dir]=R2;
		if(R2.rawDoors()[dir2]==null) R2.rawDoors()[dir2]=R;
			
		if(R.rawExits()[dir]==null)
			R.rawExits()[dir]=CMClass.getExit("StdOpenDoorway");
		if(R2.rawExits()[dir2]==null)
			R2.rawExits()[dir2]=CMClass.getExit("StdOpenDoorway");
			
		ExternalPlay.DBUpdateExits(R);
		ExternalPlay.DBUpdateExits(R2);
			
		if(R instanceof GridLocale)
			((GridLocale)R).buildGrid();
		if(R2 instanceof GridLocale)
			((GridLocale)R2).buildGrid();
		return "";
	}
}
