package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class ExitData extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	private static final String[] dispositions={"ISSEEN",
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
	
	public static String dispositions(Environmental E, 
									  boolean firstTime,
									  ExternalHTTPRequests httpReq, 
									  Hashtable parms)
	{
		StringBuffer str=new StringBuffer("");
		for(int d=0;d<dispositions.length;d++)
		{
			if(parms.containsKey(dispositions[d]))
			{
				String parm=(String)httpReq.getRequestParameters().get(dispositions[d]);
				if(firstTime)
					parm=(((E.baseEnvStats().disposition()&(1<<d))>0)?"on":"");
				if((parm==null)||(parm.length()>0))
					str.append("checked");
			}
		}
		return str.toString();
	}
	
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=(String)httpReq.getRequestParameters().get("ROOM");
		if(last==null) return " @break@";
		String linkdir=(String)httpReq.getRequestParameters().get("LINK");
		if(linkdir==null) return "@break@";
		int link=Directions.getGoodDirectionCode(linkdir);
		if((link<0)||(link>=Directions.NUM_DIRECTIONS)) return " @break@";
		
		Room R=CMMap.getRoom(last);
		Exit E=((R==null)?null:R.rawExits()[link]);
		
		boolean classChanged=(((String)httpReq.getRequestParameters().get("CLASSCHANGED")!=null)
							 &&(((String)httpReq.getRequestParameters().get("CLASSCHANGED")).equals("true")));
		// important generic<->non generic swap!
		String newClassID=(String)httpReq.getRequestParameters().get("CLASSES");
		boolean firstTime=true;
		Exit E2=null;
		if(newClassID!=null){ E2=CMClass.getExit(newClassID); firstTime=false;}
		if((E2!=null)&&(E.isGeneric()!=E2.isGeneric()))
			E=E2;
		
		if(E!=null)
		{
			StringBuffer str=new StringBuffer("");
			boolean resetIfNecessary=false;
			String[] okparms={"NAME","CLASSES","DISPLAYTEXT","DESCRIPTION",
							  "LEVEL","LEVELRESTRICTED","ISTRAPPED","HASADOOR",
							  "CLOSEDTEXT","DEFAULTSCLOSED","OPENWORD","CLOSEWORD",
							  "HASALOCK","DEFAULTSLOCKED","KEYNAME","ISREADABLE",
							  "READABLETEXT","ISCLASSRESTRICTED","RESTRICTEDCLASSES",
							  "ISALIGNMENTRESTRICTED","RESTRICTEDALIGNMENTS","MISCTEXT","ISGENERIC"};
			for(int o=0;o<okparms.length;o++)
			if(parms.containsKey(okparms[o]))
			{
				String old=(String)httpReq.getRequestParameters().get(okparms[o]);
				String oldold=old;
				if(old==null) old="";
				switch(o)
				{
				case 0: // name
					if(firstTime) old=E.name();
					str.append(old);
					break;
				case 1: // classes
					if(firstTime) old=CMClass.className(E); 
					for(int r=0;r<CMClass.exits.size();r++)
					{
						Exit cnam=(Exit)CMClass.exits.elementAt(r);
						str.append("<OPTION VALUE=\""+CMClass.className(cnam)+"\"");
						if(old.equalsIgnoreCase(CMClass.className(cnam)))
							str.append(" SELECTED");
						str.append(">"+CMClass.className(cnam));
					}
					break;
				case 2: // displaytext
					if(firstTime) old=E.displayText(); 
					str.append(old);
					break;
				case 3: // description
					if(firstTime) old=E.description(); 
					str.append(old);
					break;
				case 4: // level
					if(firstTime) old=""+E.baseEnvStats().level(); 
					str.append(old);
					break;
				case 5: // levelrestricted;
					if(firstTime) 
						old=E.levelRestricted()?"checked":""; 
					else 
					if(old.equals("on")) 
						old="checked";
					str.append(old);
					break;
				case 6: // istrapped
					if(firstTime) 
						old=E.isTrapped()?"checked":""; 
					else 
					if(old.equals("on")) 
						old="checked";
					str.append(old);
					break;
				case 7: // hasadoor
					if(firstTime) 
						old=E.hasADoor()?"checked":""; 
					else 
					if(old.equals("on")) 
						old="checked";
					str.append(old);
					break;
				case 8: // closedtext
					if(firstTime) old=E.closedText(); 
					str.append(old);
					break;
				case 9: // defaultsclosed
					if(firstTime) 
						old=E.defaultsClosed()?"checked":""; 
					else 
					if(old.equals("on")) 
						old="checked";
					str.append(old);
					break;
				case 10: // openword
					if(firstTime) old=E.openWord(); 
					str.append(old);
					break;
				case 11: // closeword
					if(firstTime) old=E.closeWord(); 
					str.append(old);
					break;
				case 12: // hasalock
					if(firstTime) 
						old=E.hasALock()?"checked":""; 
					else 
					if(old.equals("on")) 
						old="checked";
					str.append(old);
					break;
				case 13: // defaultslocked
					if(firstTime) 
						old=E.defaultsLocked()?"checked":""; 
					else 
					if(old.equals("on")) 
						old="checked";
					str.append(old);
					break;
				case 14: // keyname
					if(firstTime) old=E.keyName(); 
					str.append(old);
					break;
				case 15: // isreadable
					if(firstTime) 
						old=E.isReadable()?"checked":""; 
					else 
					if(old.equals("on")) 
						old="checked";
					str.append(old);
					break;
				case 16: // readable text
					if(firstTime) old=E.readableText(); 
					str.append(old);
					break;
				case 17: // isclassrestricuted
					if(firstTime) old=E.classRestricted()?"checked":""; 
					else 
					if(old.equals("on")) 
						old="checked";
					str.append(old);
					break;
				case 18: // restrictedclasses
					if(firstTime) old=E.classRestrictedName(); 
					str.append("<SELECT NAME=RESTRICTEDCLASSES>");
					for(int c=0;c<CMClass.charClasses.size();c++)
					{
						CharClass C=(CharClass)CMClass.charClasses.elementAt(c);
						str.append("<OPTION VALUE=\""+C.ID()+"\"");
						if(C.ID().equalsIgnoreCase(old))
							str.append(" SELECTED");
						str.append(">"+C.name());
					}
					str.append("</SELECT>");
					str.append(old);
					break;
				case 19: // isalignmentrestricuted
					if(firstTime) 
						old=E.alignmentRestricted()?"checked":""; 
					else 
					if(old.equals("on")) 
						old="checked";
					str.append(old);
					break;
				case 20: // restrictedalignments
					String mask=E.alignmentRestrictedMask();
					String[] alignments={"GOOD","NEUTRAL","EVIL"};
					if(httpReq.getRequestParameters().containsKey("RESTRICTEDALIGNMENTS"))
					{
						mask=((String)httpReq.getRequestParameters().get("RESTRICTEDALIGNMENTS"))+" ";
						for(int i=1;;i++)
						{
							String selection=(String)httpReq.getRequestParameters().get("RESTRICTEDALIGNMENTS"+i);
							if(selection!=null)
								mask+=selection+" ";
							else
								break;
						}
					}
					mask=mask.trim();
					str.append("<SELECT MULTIPLE NAME=RESTRICTEDALIGNMENTS>");
					for(int i=0;i<alignments.length;i++)
					{
						str.append("<OPTION VALUE=\""+alignments[i]+"\"");
						if(mask.toUpperCase().indexOf(alignments[i])>=0)
							str.append(" SELECTED");
						str.append(">"+alignments[i]);
					}
					str.append("</SELECT>");
					break;
				case 21: // misc text
					if(firstTime) old=E.text(); 
					str.append(old);
					break;
				case 22: // is generic
					httpReq.getRequestParameters().put("ISGENERIC",""+E.isGeneric());
					httpReq.resetRequestEncodedParameters();
					break;
				}
				if((oldold==null)&&(!firstTime))
				{
					resetIfNecessary=true;
					httpReq.getRequestParameters().put(okparms[o],old.equals("checked")?"on":old);
				}
				
			}
			str.append(ExitData.dispositions(E,firstTime,httpReq,parms));
			str.append(AreaData.affectsNBehaves(E,httpReq,parms));
			E.recoverEnvStats();
			E.text();
			ExternalPlay.DBUpdateExits(R);
			
			if(resetIfNecessary)
				httpReq.resetRequestEncodedParameters();
			
			String strstr=str.toString();
			if(strstr.endsWith(", "))
				strstr=strstr.substring(0,strstr.length()-2);
			return strstr;
		}
		return "";
	}
}
