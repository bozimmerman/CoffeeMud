package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.*;

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
public class Export extends StdCommand
{
	public Export(){}

	private String[] access={"EXPORT"};
	public String[] getAccessWords(){return access;}

	public static void reallyExport(MOB mob, String fileName, String xml)
	{
		if(fileName==null) return;
		if(mob==null) return;
		if(xml==null) return;
		if(xml.length()==0) return;

		if(fileName.equalsIgnoreCase("SCREEN"))
		{
			mob.tell("Here it is:\n\r\n\r");
			xml=xml.replace('\n',' ');
			xml=xml.replace('\r',' ');
			if(mob.session()!=null)
				mob.session().rawPrintln(xml+"\n\r\n\r");
		}
		else
		if(fileName.equalsIgnoreCase("EMAIL"))
		{
			if(!CommonStrings.getBoolVar(CommonStrings.SYSTEMB_EMAILFORWARDING))
			{
			    mob.tell("Mail forwarding is not enabled on this mud.");
			    return;
			}
			if(CommonStrings.getVar(CommonStrings.SYSTEM_MAILBOX).length()==0)
			{
			    mob.tell("No email box has been defined.");
			    return;
			}
			if((mob.playerStats()==null)||(mob.playerStats().getEmail().length()==0))
			{
			    mob.tell("No email address has been defined.");
			    return;
			}
			xml=xml.replace('\n',' ');
			xml=xml.replace('\r',' ');
			CMClass.DBEngine().DBWriteJournal(
			        CommonStrings.getVar(CommonStrings.SYSTEM_MAILBOX),
			        mob.Name(),
			        mob.Name(),
			        "Exported XML",
			        xml.toString(),
			        -1);
			mob.tell("XML emailed to "+mob.playerStats().getEmail());
		}
		else
		{
			mob.tell("Writing file...");
			try
			{
				if(fileName.indexOf(".")<0)
					fileName=fileName+".cmare";
				File f=new File(fileName);
				BufferedOutputStream out=new BufferedOutputStream(new FileOutputStream(f));
				for(int i=0;i<xml.length();i+=65536)
				{
				    if((i+65536)>=xml.length())
						out.write(xml.substring(i).getBytes());
				    else
						out.write(xml.substring(i,i+65536).getBytes());
				    out.flush();
				}
				out.close();
				mob.tell("File '"+fileName+"' written.");
			}
			catch(java.io.IOException e)
			{
				mob.tell("A file error occurred: "+e.getMessage());
			}
		}
	}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String commandType="";
		String fileName="";
		int fileNameCode=-1; // -1=indetermined, 0=screen, 1=file, 2=path, 3=email
		HashSet custom=new HashSet();

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
			mob.tell("Export what?  Room, World, Player, or Area?");
			return false;
		}
		if(commandType.equalsIgnoreCase("PLAYER"))
		{
			if(!CMSecurity.isAllowedEverywhere(mob,"EXPORTPLAYERS"))
			{
				mob.tell("You are not allowed to export player data.");
				return false;
			}
		}
		else
		if(!CMSecurity.isAllowed(mob,mob.location(),"EXPORT"))
		{
			mob.tell("You are not allowed to export room, mob, or item data.");
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

			if(commands.size()==0)
			{
				mob.tell("You must specify a file name to create, or enter 'SCREEN' to have a screen dump, or 'EMAIL' to send to your email address.");
				return false;
			}
			fileName=Util.combine(commands,0);
			if(fileName.equalsIgnoreCase("screen"))
				fileNameCode=0;
			else
			if(fileName.equalsIgnoreCase("email"))
				fileNameCode=3;
			else
			{
				if(!CMSecurity.isAllowedAnywhere(mob,"EXPORTFILE"))
				{
					mob.tell("You are not allowed to export to a file.");
					return false;
				}
				File F=new File(fileName);
				if(F.isDirectory())
					fileNameCode=2;
			}
			if(fileNameCode<0)
				fileNameCode=1;
		}
		else
		{
			mob.tell("You must specify a file name to create, or enter 'SCREEN' to have a screen dump or 'EMAIL' to send to an email address.");
			return false;
		}

		String xml="";
		if(subType.equalsIgnoreCase("DATA"))
		{
			if(commandType.equalsIgnoreCase("PLAYER"))
			{
				StringBuffer x=new StringBuffer("<PLAYERS>");
				if(mob.session()!=null)
					mob.session().rawPrint("Reading players...");
				Vector V=CMClass.DBEngine().getUserList();
				for(int v=0;v<V.size();v++)
				{
					Vector V2=(Vector)V.elementAt(v);
					String name=(String)V2.elementAt(0);
					if(mob.session()!=null) mob.session().rawPrint(".");
					MOB M=CMMap.getLoadPlayer(name);
					if(M!=null)
					{
						x.append("\r\n<PLAYER>");
						x.append(CoffeeMaker.getPlayerXML(M,custom));
						x.append("</PLAYER>");
					}
				}
				if(fileNameCode==2) fileName=fileName+File.separatorChar+"player";
				xml=x.toString()+"</PLAYERS>";
				if(mob.session()!=null)
					mob.session().rawPrintln("!");
			}
			else
			if(commandType.equalsIgnoreCase("ROOM"))
			{
				xml=CoffeeMaker.getRoomXML(mob.location(),custom,true).toString();
				if(fileNameCode==2) fileName=fileName+File.separatorChar+"room";
			}
			else
			if(commandType.equalsIgnoreCase("AREA"))
			{
				if(mob.session()!=null)
					mob.session().rawPrint("Reading area '"+mob.location().getArea().Name()+"'...");
				xml=CoffeeMaker.getAreaXML(mob.location().getArea(),mob.session(),custom,true).toString();
				if(fileNameCode==2){
					if(mob.location().getArea().getArchivePath().length()>0)
						fileName=fileName+File.separatorChar+mob.location().getArea().getArchivePath();
					else
						fileName=fileName+File.separatorChar+mob.location().getArea().Name();
				}
				if(mob.session()!=null)
					mob.session().rawPrintln("!");
			}
			else
			{
				if(!CMSecurity.isAllowedEverywhere(mob,"EXPORT"))
				{
					mob.tell("You are not allowed to export world data.");
					return false;
				}
				StringBuffer buf=new StringBuffer("");
				if(fileNameCode!=2) buf.append("<AREAS>");
				for(Enumeration a=CMMap.areas();a.hasMoreElements();)
				{
					Area A=(Area)a.nextElement();
					if(A!=null)
					{
						if(mob.session()!=null)
							mob.session().rawPrint("Reading area '"+A.name()+"'...");
						buf.append(CoffeeMaker.getAreaXML(A,mob.session(),custom,true).toString());
						if(mob.session()!=null)
							mob.session().rawPrintln("!");
						if(fileNameCode==2)
						{
							String name=fileName;
							if(A.getArchivePath().length()>0)
								name=fileName+File.separatorChar+A.getArchivePath();
							else
								name=fileName+File.separatorChar+A.Name();
							reallyExport(mob,name,buf.toString());
							buf=new StringBuffer("");
						}
					}
				}
				if(fileNameCode!=2) xml=buf.toString()+"</AREAS>";
			}
		}
		else
		if(subType.equalsIgnoreCase("MOBS"))
		{
			if(fileNameCode==2) fileName=fileName+File.separatorChar+"mobs";
			Hashtable found=new Hashtable();
			if(commandType.equalsIgnoreCase("ROOM"))
				xml="<MOBS>"+CoffeeMaker.getRoomMobs(mob.location(),custom,found).toString()+"</MOBS>";
			else
			if(commandType.equalsIgnoreCase("AREA"))
			{
				if(mob.session()!=null)
					mob.session().rawPrint("Reading area mobs '"+mob.location().getArea().Name()+"'...");
				StringBuffer buf=new StringBuffer("<MOBS>");
				for(Enumeration r=mob.location().getArea().getProperMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					if(mob.session()!=null) mob.session().rawPrint(".");
					buf.append(CoffeeMaker.getRoomMobs(R,custom,found).toString());
				}
				xml=buf.toString()+"</MOBS>";
				if(mob.session()!=null)
					mob.session().rawPrintln("!");
			}
			else
			{
				if(mob.session()!=null)
					mob.session().rawPrint("Reading world mobs ...");
				StringBuffer buf=new StringBuffer("<MOBS>");
				for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					if(mob.session()!=null) mob.session().rawPrint(".");
					buf.append(CoffeeMaker.getRoomMobs(R,custom,found).toString());
				}
				xml=buf.toString()+"</MOBS>";
				if(mob.session()!=null)
					mob.session().rawPrintln("!");
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
					fileName=fileName+File.separatorChar+"weapons";
				type=1;
			}
			else
			if(subType.equalsIgnoreCase("ARMOR"))
			{
				if(fileNameCode==2)
					fileName=fileName+File.separatorChar+"armor";
				type=2;
			}
			else
			if(fileNameCode==2)
			{
				fileName=fileName+File.separatorChar+"items";
			}

			Hashtable found=new Hashtable();
			if(commandType.equalsIgnoreCase("ROOM"))
				xml="<ITEMS>"+CoffeeMaker.getRoomItems(mob.location(),found,type).toString()+"</ITEMS>";
			else
			if(commandType.equalsIgnoreCase("AREA"))
			{
				if(mob.session()!=null)
					mob.session().rawPrint("Reading area "+subType.toLowerCase()+" '"+mob.location().getArea().Name()+"'...");
				StringBuffer buf=new StringBuffer("<ITEMS>");
				for(Enumeration r=mob.location().getArea().getProperMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					if(mob.session()!=null) mob.session().rawPrint(".");
					buf.append(CoffeeMaker.getRoomItems(R,found,type).toString());
				}
				xml=buf.toString()+"</ITEMS>";
				if(mob.session()!=null)
					mob.session().rawPrintln("!");
			}
			else
			{
				if(mob.session()!=null)
					mob.session().rawPrint("Reading world "+subType.toLowerCase()+" ...");
				StringBuffer buf=new StringBuffer("<ITEMS>");
				for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					if(mob.session()!=null) mob.session().rawPrint(".");
					buf.append(CoffeeMaker.getRoomItems(R,found,type).toString());
				}
				xml=buf.toString()+"</ITEMS>";
				if(mob.session()!=null)
					mob.session().rawPrintln("!");
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
		reallyExport(mob,fileName,xml);
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowedStartsWith(mob,mob.location(),"EXPORT");}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
