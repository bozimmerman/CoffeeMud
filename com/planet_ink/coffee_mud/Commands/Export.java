package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.*;

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
		{
			mob.tell("Exporting room(s)...");
			try
			{
				if(fileName.indexOf(".")<0)
					fileName=fileName+".cmare";
				File f=new File(fileName);
				FileOutputStream out=new FileOutputStream(f);
				out.write(xml.getBytes());
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
		if(!mob.isASysOp(null))
		{
			mob.tell("Only the Archons can do that.");
			return false;
		}
		String commandType="";
		String fileName="";
		int fileNameCode=-1; // -1=indetermined, 0=screen, 1=file, 2=path
		HashSet custom=new HashSet();

		commands.removeElementAt(0);
		if(commands.size()>0)
		{
			commandType=((String)commands.elementAt(0)).toUpperCase();
			commands.removeElementAt(0);
		}
		if((!commandType.equalsIgnoreCase("ROOM"))
		&&(!commandType.equalsIgnoreCase("WORLD"))
		&&(!commandType.equalsIgnoreCase("AREA")))
		{
			mob.tell("Export what?  Room, or Area?");
			return false;
		}

		String subType="DATA";
		if(commands.size()>0)
		{
			String sub=((String)commands.firstElement()).toUpperCase().trim();
			if(sub.equalsIgnoreCase("ITEMS")
			||sub.equalsIgnoreCase("MOBS")
			||sub.equalsIgnoreCase("WEAPONS")
			||sub.equalsIgnoreCase("ARMOR"))
			{
				subType=sub;
				commands.removeElementAt(0);
			}
			else
			if(sub.equalsIgnoreCase("data"))
				commands.removeElementAt(0);

			if(commands.size()==0)
			{
				mob.tell("You must specify a file name to create, or enter 'SCREEN' to have a screen dump.");
				return false;
			}
			fileName=Util.combine(commands,0);
			if(fileName.equalsIgnoreCase("screen"))
				fileNameCode=0;
			else
			{
				if(!mob.isASysOp(null))
				{
					mob.tell("Only Archons may export to a file.");
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
			mob.tell("You must specify a file name to create, or enter 'SCREEN' to have a screen dump.");
			return false;
		}

		String xml="";
		if(subType.equalsIgnoreCase("DATA"))
		{
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
				for(Enumeration r=mob.location().getArea().getMap();r.hasMoreElements();)
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
				for(Enumeration r=mob.location().getArea().getMap();r.hasMoreElements();)
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
			}
			str.append("</CUSTOM>");
			xml=str.toString()+xml;
		}
		reallyExport(mob,fileName,xml);
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean arcCommand(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
