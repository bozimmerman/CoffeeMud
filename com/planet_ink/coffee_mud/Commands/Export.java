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

/*
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Export extends StdCommand
{
	public Export(){}

	private String[] access={"EXPORT"};
	public String[] getAccessWords(){return access;}

	public static void reallyExport(MOB mob, Session S, String fileName, String xml)
	{
		if(fileName==null) return;
		if(mob==null) return;
		if(xml==null) return;
		if(xml.length()==0) return;

		if(fileName.equalsIgnoreCase("SCREEN"))
		{
			if(S!=null) mob.tell("Here it is:\n\r\n\r");
			xml=xml.replace('\n',' ');
			xml=xml.replace('\r',' ');
			if(S!=null) S.rawPrintln(xml+"\n\r\n\r");
		}
		else
		if(fileName.equalsIgnoreCase("EMAIL"))
		{
			if(!CMProps.getBoolVar(CMProps.SYSTEMB_EMAILFORWARDING))
			{
				if(S!=null) mob.tell("Mail forwarding is not enabled on this mud.");
			    return;
			}
			if(CMProps.getVar(CMProps.SYSTEM_MAILBOX).length()==0)
			{
				if(S!=null) mob.tell("No email box has been defined.");
			    return;
			}
			if((mob.playerStats()==null)||(mob.playerStats().getEmail().length()==0))
			{
				if(S!=null) mob.tell("No email address has been defined.");
			    return;
			}
			xml=xml.replace('\n',' ');
			xml=xml.replace('\r',' ');
			CMLib.database().DBWriteJournal(
			        CMProps.getVar(CMProps.SYSTEM_MAILBOX),
			        mob.Name(),
			        mob.Name(),
			        "Exported XML",
			        xml);
			if(S!=null) mob.tell("XML emailed to "+mob.playerStats().getEmail());
		}
		else
		{
			if(S!=null) mob.tell("Writing file...");
			if(fileName.indexOf(".")<0)
				fileName=fileName+".cmare";
            new CMFile(fileName,mob,false).saveText(xml);
            if(S!=null) mob.tell("File '"+fileName+"' written.");
		}
	}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String commandType="";
		String fileName="";
		int fileNameCode=-1; // -1=indetermined, 0=screen, 1=file, 2=path, 3=email
		HashSet custom=new HashSet();
		HashSet files=new HashSet();
		Room room=mob.location();
		Area area=(room!=null)?room.getArea():null;
		Session S=mob.session();
		while((commands.size()>0)&&(!(commands.lastElement() instanceof String)))
		{
			S=null;
			Object O=commands.lastElement();
			if(O instanceof Room)
			{
				room=(Room)O;
				area=room.getArea();
			}
			else
			if(O instanceof Area)
				area=(Area)O;
			commands.removeElementAt(commands.size()-1);
		}
		if(commands.size()>0)
			commands.removeElementAt(0);
		if(commands.size()>0)
		{
			commandType=((String)commands.elementAt(0)).toUpperCase();
			commands.removeElementAt(0);
		}
		if((!commandType.equalsIgnoreCase("ROOM"))
		&&(!commandType.equalsIgnoreCase("WORLD"))
		&&(!commandType.equalsIgnoreCase("PLAYER"))
		&&(!commandType.equalsIgnoreCase("AREA")))
		{
			if(S!=null) mob.tell("Export what?  Room, World, Player, or Area?");
			return false;
		}
		if(commandType.equalsIgnoreCase("PLAYER"))
		{
			if(!CMSecurity.isAllowedEverywhere(mob,"EXPORTPLAYERS"))
			{
				if(S!=null) mob.tell("You are not allowed to export player data.");
				return false;
			}
		}
		else
		if(!CMSecurity.isAllowed(mob,room,"EXPORT"))
		{
			if(S!=null) mob.tell("You are not allowed to export room, mob, or item data.");
			return false;
		}

		String subType="DATA";
		if(commands.size()>0)
		{
			String sub=((String)commands.firstElement()).toUpperCase().trim();
			if((sub.equalsIgnoreCase("ITEMS")
				||sub.equalsIgnoreCase("MOBS")
				||sub.equalsIgnoreCase("WEAPONS")
				||sub.equalsIgnoreCase("ARMOR"))
			&&(!commandType.equalsIgnoreCase("PLAYER")))
			{
				subType=sub;
				commands.removeElementAt(0);
			}
			else
			if(sub.equalsIgnoreCase("data"))
				commands.removeElementAt(0);
			else
			if((commandType.equalsIgnoreCase("PLAYER"))
			&&(CMLib.players().getLoadPlayer(sub)!=null))
			{
				subType=sub;
				commands.removeElementAt(0);
			}

			if(commands.size()==0)
			{
				if(S!=null) mob.tell("You must specify a file name to create, or enter 'SCREEN' to have a screen dump, or 'EMAIL' to send to your email address.");
				return false;
			}
			fileName=CMParms.combine(commands,0);
			if(fileName.equalsIgnoreCase("screen"))
				fileNameCode=0;
			else
			if(fileName.equalsIgnoreCase("email"))
				fileNameCode=3;
			else
			if(fileName.equalsIgnoreCase("memory"))
			{
				if(!CMSecurity.isAllowedAnywhere(mob,"EXPORTFILE"))
				{
					if(S!=null) mob.tell("You are not allowed to export to memory.");
					return false;
				}
				fileNameCode=4;
			}
			else
			{
				if(!CMSecurity.isAllowedAnywhere(mob,"EXPORTFILE"))
				{
					if(S!=null) mob.tell("You are not allowed to export to a file.");
					return false;
				}
				CMFile F=new CMFile(fileName,mob,false);
				if(F.isDirectory())
					fileNameCode=2;
			}
			if(fileNameCode<0)
				fileNameCode=1;
		}
		else
		{
			if(S!=null) mob.tell("You must specify a file name to create, or enter 'SCREEN' to have a screen dump or 'EMAIL' to send to an email address.");
			return false;
		}

		String xml="";
		if(subType.equalsIgnoreCase("DATA"))
		{
			if(commandType.equalsIgnoreCase("PLAYER"))
			{
				StringBuffer x=new StringBuffer("<PLAYERS>");
				if(S!=null)
					S.rawPrint("Reading players...");
				java.util.List<String> V=CMLib.database().getUserList();
				for(String name : V)
				{
					//if(S!=null) S.rawPrint(".");
					MOB M=CMLib.players().getLoadPlayer(name);
					if(M!=null)
					{
						x.append("\r\n<PLAYER>");
						x.append(CMLib.coffeeMaker().getPlayerXML(M,custom,files));
						x.append("</PLAYER>");
					}
				}
				if(fileNameCode==2) fileName=fileName+"/player";
				xml=x.toString()+"</PLAYERS>";
				if(S!=null)
					S.rawPrintln("!");
			}
			else
			if(commandType.equalsIgnoreCase("ROOM"))
			{
				xml=CMLib.coffeeMaker().getRoomXML(room,custom,files,true).toString();
				if(fileNameCode==2) fileName=fileName+"/room";
			}
			else
			if(commandType.equalsIgnoreCase("AREA"))
			{
				if(S!=null)
					S.rawPrint("Reading area '"+area.Name()+"'...");
				xml=CMLib.coffeeMaker().getAreaXML(area,S,custom,files,true).toString();
				if(fileNameCode==2){
					if(area.getArchivePath().length()>0)
						fileName=fileName+"/"+area.getArchivePath();
					else
						fileName=fileName+"/"+area.Name();
				}
				if(S!=null)
					S.rawPrintln("!");
			}
			else
			{
				if(!CMSecurity.isAllowedEverywhere(mob,"EXPORT"))
				{
					if(S!=null) mob.tell("You are not allowed to export world data.");
					return false;
				}
				StringBuffer buf=new StringBuffer("");
				if(fileNameCode!=2) buf.append("<AREAS>");
				for(Enumeration a=CMLib.map().areas();a.hasMoreElements();)
				{
					Area A=(Area)a.nextElement();
					if(A!=null)
					{
						if(S!=null)
							S.rawPrint("Reading area '"+A.name()+"'...");
						buf.append(CMLib.coffeeMaker().getAreaXML(A,S,custom,files,true).toString());
						if(S!=null)
							S.rawPrintln("!");
						if(fileNameCode==2)
						{
							String name=fileName;
							if(A.getArchivePath().length()>0)
								name=fileName+"/"+A.getArchivePath();
							else
								name=fileName+"/"+A.Name();
							reallyExport(mob,S,name,buf.toString());
							buf=new StringBuffer("");
						}
					}
				}
				if(fileNameCode!=2) xml=buf.toString()+"</AREAS>";
			}
		}
		else
		if(commandType.equalsIgnoreCase("PLAYER"))
		{
			StringBuffer x=new StringBuffer("<PLAYERS>");
			MOB M=CMLib.players().getLoadPlayer(subType);
			if(M!=null)
			{
				x.append("\r\n<PLAYER>");
				x.append(CMLib.coffeeMaker().getPlayerXML(M,custom,files));
				x.append("</PLAYER>");
			}
			if(fileNameCode==2) fileName=fileName+"/player";
			xml=x.toString()+"</PLAYERS>";
		}
		else
		if(subType.equalsIgnoreCase("MOBS"))
		{
			if(fileNameCode==2) fileName=fileName+"/mobs";
			Hashtable found=new Hashtable();
			if(commandType.equalsIgnoreCase("ROOM"))
				xml="<MOBS>"+CMLib.coffeeMaker().getRoomMobs(room,custom,files,found).toString()+"</MOBS>";
			else
			if(commandType.equalsIgnoreCase("AREA"))
			{
				if(S!=null)
					S.rawPrint("Reading area mobs '"+area.Name()+"'...");
				StringBuffer buf=new StringBuffer("<MOBS>");
				for(Enumeration r=area.getCompleteMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					//if(S!=null) S.rawPrint(".");
					buf.append(CMLib.coffeeMaker().getRoomMobs(R,custom,files,found).toString());
				}
				xml=buf.toString()+"</MOBS>";
				if(S!=null)
					S.rawPrintln("!");
			}
			else
			{
				if(S!=null)
					S.rawPrint("Reading world mobs ...");
				StringBuffer buf=new StringBuffer("<MOBS>");
				try
				{
					for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
					{
						Room R=(Room)r.nextElement();
						//if(S!=null) S.rawPrint(".");
						buf.append(CMLib.coffeeMaker().getRoomMobs(R,custom,files,found).toString());
					}
			    }catch(NoSuchElementException e){}
				xml=buf.toString()+"</MOBS>";
				if(S!=null)
					S.rawPrintln("!");
			}
		}
		else
		if((subType.equalsIgnoreCase("ITEMS"))
		||(subType.equalsIgnoreCase("WEAPONS"))
		||(subType.equalsIgnoreCase("ARMOR")))
		{
			int type=0;
			if(subType.equalsIgnoreCase("WEAPONS"))
			{
				if(fileNameCode==2)
					fileName=fileName+"/weapons";
				type=1;
			}
			else
			if(subType.equalsIgnoreCase("ARMOR"))
			{
				if(fileNameCode==2)
					fileName=fileName+"/armor";
				type=2;
			}
			else
			if(fileNameCode==2)
			{
				fileName=fileName+"/items";
			}

			Hashtable found=new Hashtable();
			if(commandType.equalsIgnoreCase("ROOM"))
				xml="<ITEMS>"+CMLib.coffeeMaker().getRoomItems(room,found,files,type).toString()+"</ITEMS>";
			else
			if(commandType.equalsIgnoreCase("AREA"))
			{
				if(S!=null)
					S.rawPrint("Reading area "+subType.toLowerCase()+" '"+area.Name()+"'...");
				StringBuffer buf=new StringBuffer("<ITEMS>");
				for(Enumeration r=area.getCompleteMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					//if(S!=null) S.rawPrint(".");
					buf.append(CMLib.coffeeMaker().getRoomItems(R,found,files,type).toString());
				}
				xml=buf.toString()+"</ITEMS>";
				if(S!=null)
					S.rawPrintln("!");
			}
			else
			{
				if(S!=null)
					S.rawPrint("Reading world "+subType.toLowerCase()+" ...");
				StringBuffer buf=new StringBuffer("<ITEMS>");
				try
				{
					for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
					{
						Room R=(Room)r.nextElement();
						//if(S!=null) S.rawPrint(".");
						buf.append(CMLib.coffeeMaker().getRoomItems(R,found,files,type).toString());
					}
			    }catch(NoSuchElementException e){}
				xml=buf.toString()+"</ITEMS>";
				if(S!=null)
					S.rawPrintln("!");
			}
		}
		if(custom.size()>0)
		{
			StringBuffer str=new StringBuffer("<CUSTOM>");
			for(Iterator i=custom.iterator();i.hasNext();)
			{
				Object o=i.next();
				if(o instanceof Race)
					str.append(((Race)o).racialParms());
				else
				if(o instanceof CharClass)
					str.append(((CharClass)o).classParms());
			}
			str.append("</CUSTOM>");
			xml+=str.toString();
		}
		if(files.size()>0)
		{
			StringBuffer str=new StringBuffer("<FILES>");
			for(Iterator i=files.iterator();i.hasNext();)
			{
                Object O=i.next();
				String filename=(String)O;
				StringBuffer buf=new CMFile(Resources.makeFileResourceName(filename),null,true).text();
				if((buf!=null)&&(buf.length()>0))
				{
					str.append("<FILE NAME=\""+filename+"\">");
					str.append(buf);
					str.append("</FILE>");
				}
			}
			str.append("</FILES>");
			xml+=str.toString();
		}
        if(fileNameCode==2) fileName=fileName+"/extras";
        if(fileNameCode==4)
        {
        	commands.clear();
        	commands.addElement(xml);
        	return true;
        }
		reallyExport(mob,S,fileName,xml);
		return true;
	}

	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowedStartsWith(mob,mob.location(),"EXPORT");}


}
