package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class ExitData extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	
	public static String dispositions(Environmental E, 
									  boolean firstTime,
									  ExternalHTTPRequests httpReq, 
									  Hashtable parms)
	{
		StringBuffer str=new StringBuffer("");
		for(int d=0;d<EnvStats.dispositionsDescs.length;d++)
		{
			if(parms.containsKey(EnvStats.dispositionsDescs[d]))
			{
				String parm=(String)httpReq.getRequestParameters().get(EnvStats.dispositionsDescs[d]);
				if(firstTime)
					parm=(((E.baseEnvStats().disposition()&(1<<d))>0)?"on":"");
				if((parm!=null)&&(parm.length()>0))
					str.append("checked");
			}
		}
		return str.toString();
	}
	
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		Hashtable reqs=httpReq.getRequestParameters();
		
		String last=(String)reqs.get("ROOM");
		if(last==null) return " @break@";
		Room R=CMMap.getRoom(last);
		if(R==null) return "@break@";
		
		if(!httpReq.getMUD().gameStatusStr().equalsIgnoreCase("OK"))
			return httpReq.getMUD().gameStatusStr();
		
		String linkdir=(String)reqs.get("LINK");
		if(linkdir==null) return "@break@";
		int link=Directions.getGoodDirectionCode(linkdir);
		if((link<0)||(link>=Directions.NUM_DIRECTIONS)) return " @break@";
		
		Exit E=R.rawExits()[link];
		
		// important generic<->non generic swap!
		String newClassID=(String)reqs.get("CLASSES");
		if((newClassID!=null)&&(!newClassID.equals(CMClass.className(E))))
				E=CMClass.getExit(newClassID);
		
		boolean firstTime=(reqs.get("ACTION")==null)
					||(!((String)reqs.get("ACTION")).equals("MODIFYEXIT"))
					||(((reqs.get("CHANGEDCLASS")!=null)&&((String)reqs.get("CHANGEDCLASS")).equals("true")));
		
		if(E==null) return "@break@";
		
		StringBuffer str=new StringBuffer("");
		String[] okparms={"NAME","CLASSES","DISPLAYTEXT","DESCRIPTION",
						  "LEVEL","LEVELRESTRICTED","ISTRAPPED","HASADOOR",
						  "CLOSEDTEXT","DEFAULTSCLOSED","OPENWORD","CLOSEWORD",
						  "HASALOCK","DEFAULTSLOCKED","KEYNAME","ISREADABLE",
						  "READABLETEXT","ISCLASSRESTRICTED","RESTRICTEDCLASSES",
						  "ISALIGNMENTRESTRICTED","RESTRICTEDALIGNMENTS",
						  "MISCTEXT","ISGENERIC","DOORNAME"};
		for(int o=0;o<okparms.length;o++)
		if(parms.containsKey(okparms[o]))
		{
			String old=(String)reqs.get(okparms[o]);
			String oldold=old;
			if(old==null) old="";
			switch(o)
			{
			case 0: // name
				if(firstTime) old=E.name();
				str.append(old);
				break;
			case 1: // classes
				{
					if(firstTime) old=CMClass.className(E); 
					Vector sortMe=new Vector();
					for(int r=0;r<CMClass.exits.size();r++)
						sortMe.addElement(CMClass.className(CMClass.exits.elementAt(r)));
					Object[] sorted=(Object[])(new TreeSet(sortMe)).toArray();
					for(int r=0;r<sorted.length;r++)
					{
						String cnam=(String)sorted[r];
						str.append("<OPTION VALUE=\""+cnam+"\"");
						if(old.equals(cnam))
							str.append(" SELECTED");
						str.append(">"+cnam);
					}
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
				if(reqs.containsKey("RESTRICTEDALIGNMENTS"))
				{
					mask=((String)reqs.get("RESTRICTEDALIGNMENTS"))+" ";
					for(int i=1;;i++)
					{
						String selection=(String)reqs.get("RESTRICTEDALIGNMENTS"+i);
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
				if(E.isGeneric())
					return "true";
				else
					return "false";
			case 23: // closedtext
				if(firstTime) old=E.doorName(); 
				str.append(old);
				break;
			}
			if(firstTime)
				reqs.put(okparms[o],old.equals("checked")?"on":old);
				
		}
		str.append(ExitData.dispositions(E,firstTime,httpReq,parms));
		str.append(AreaData.affectsNBehaves(E,httpReq,parms));
		E.recoverEnvStats();
		E.text();
			
		if(firstTime)
			httpReq.resetRequestEncodedParameters();
			
		String strstr=str.toString();
		if(strstr.endsWith(", "))
			strstr=strstr.substring(0,strstr.length()-2);
		return strstr;
	}
}
