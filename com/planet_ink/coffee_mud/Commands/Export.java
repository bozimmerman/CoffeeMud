package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMClass.CMObjectType;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2018 Bo Zimmerman

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

	private final String[] access=I(new String[]{"EXPORT"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@SuppressWarnings("rawtypes")
	private final static Class[][] internalParameters=new Class[][]{
		{
			String.class,String.class,String.class,Integer.class,null,Area.class,Room.class
		}
	};

	public void reallyExport(MOB mob, Session S, String fileName, String xml)
	{
		if(fileName==null)
			return;
		if(mob==null)
			return;
		if(xml==null)
			return;
		if(xml.length()==0)
			return;

		if(fileName.equalsIgnoreCase("SCREEN"))
		{
			if(S!=null)
				mob.tell(L("Here it is:\n\r\n\r"));
			xml=xml.replace('\n',' ');
			xml=xml.replace('\r',' ');
			if(S!=null)
				S.safeRawPrintln(xml+"\n\r\n\r");
		}
		else
		if(fileName.equalsIgnoreCase("EMAIL"))
		{
			if(!CMProps.getBoolVar(CMProps.Bool.EMAILFORWARDING))
			{
				if(S!=null)
					mob.tell(L("Mail forwarding is not enabled on this mud."));
				return;
			}
			if(CMProps.getVar(CMProps.Str.MAILBOX).length()==0)
			{
				if(S!=null)
					mob.tell(L("No email box has been defined."));
				return;
			}
			if((mob.playerStats()==null)||(mob.playerStats().getEmail().length()==0))
			{
				if(S!=null)
					mob.tell(L("No email address has been defined."));
				return;
			}
			xml=xml.replace('\n',' ');
			xml=xml.replace('\r',' ');
			CMLib.database().DBWriteJournal(
					CMProps.getVar(CMProps.Str.MAILBOX),
					mob.Name(),
					mob.Name(),
					"Exported XML",
					xml);
			if(S!=null)
				mob.tell(L("XML emailed to @x1",mob.playerStats().getEmail()));
		}
		else
		{
			if(S!=null)
				mob.tell(L("Writing file..."));
			if(fileName.indexOf('.')<0)
				fileName=fileName+".cmare";
			new CMFile(fileName,mob).saveText(xml);
			if(S!=null)
				mob.tell(L("File '@x1' written.",fileName));
		}
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		String commandType="";
		String fileName="";
		int fileNameCode=-1; // -1=indetermined, 0=screen, 1=file, 2=path, 3=email
		final Room room=mob.location();
		final Area area=(room!=null)?room.getArea():null;
		final Session S=mob.session();
		if(commands.size()>0)
			commands.remove(0);
		if(commands.size()>0)
		{
			commandType=commands.get(0).toUpperCase();
			commands.remove(0);
		}
		if(commandType.equalsIgnoreCase("REALESTATE"))
		{
			if((commands.size()==0)||(area==null))
			{
				if(S!=null)
					mob.tell(L("Export which real estate?  Name a clan or player!"));
				return false;
			}
			else
			{
				String clanName = commands.remove(0);
				List<Room> rooms=new ArrayList<Room>();
				for(Enumeration<Room> r=area.getProperMap();r.hasMoreElements();)
				{
					final Room R=CMLib.map().getRoom(r.nextElement());
					if((R!=null)&&(R.roomID().length()>0))
					{
						LandTitle title=CMLib.law().getLandTitle(R);
						if((title != null)&&(title.getOwnerName().equalsIgnoreCase(clanName)))
							rooms.add(R);
					}
				}
				if(rooms.size()==0)
				{
					if(S!=null)
						mob.tell(L("No rooms in this area match '@x1'",clanName));
					return false;
				}
				else
				{
					for(Room R : rooms)
					{
						R.bringMobHere(mob, false);
						if(R.isInhabitant(mob))
						{
							List<String> cmds=new XVector<String>(commands);
							if(cmds.size()==0)
								cmds.add("EXPORT");
							else
								cmds.add(0,"EXPORT");
							if(cmds.size()==1)
								cmds.add("ROOM");
							else
								cmds.add(1,"ROOM");
							if(cmds.size()==2)
								cmds.add(R.roomID().replaceAll("#","_"));
							execute(mob,cmds,metaFlags);
						}
						else
						if(S!=null)
							mob.tell(L("Failed room '@x1'",R.roomID()));
					}
					if(room!=null)
						room.bringMobHere(mob, false);
					return true;
				}
			}
		}
		
		if((!commandType.equalsIgnoreCase("ROOM"))
		&&(!commandType.equalsIgnoreCase("WORLD"))
		&&(!commandType.equalsIgnoreCase("CATALOG"))
		&&(!commandType.equalsIgnoreCase("PLAYER"))
		&&(!commandType.equalsIgnoreCase("USER"))
		&&(!(commandType.equalsIgnoreCase("ACCOUNT")&&(CMProps.isUsingAccountSystem())))
		&&(!commandType.equalsIgnoreCase("AREA")))
		{
			if(S!=null)
			{
				if(CMProps.isUsingAccountSystem())
					mob.tell(L("Export what?  Room, World, Player, Account, or Area?"));
				else
					mob.tell(L("Export what?  Room, World, Player, or Area?"));
			}
			return false;
		}
		if(commandType.equalsIgnoreCase("PLAYER")||commandType.equalsIgnoreCase("ACCOUNT")||commandType.equalsIgnoreCase("USER"))
		{
			if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.EXPORTPLAYERS))
			{
				if(S!=null)
					mob.tell(L("You are not allowed to export player data."));
				return false;
			}
		}
		else
		if(!CMSecurity.isAllowed(mob,room,CMSecurity.SecFlag.EXPORT))
		{
			if(S!=null)
				mob.tell(L("You are not allowed to export room, mob, or item data."));
			return false;
		}

		String subType="DATA";
		if(commands.size()>0)
		{
			final String sub=commands.get(0).toUpperCase().trim();
			if((sub.equalsIgnoreCase("ITEMS")
				||sub.equalsIgnoreCase("MOBS")
				||sub.equalsIgnoreCase("WEAPONS")
				||sub.equalsIgnoreCase("ARMOR"))
			&&(!commandType.equalsIgnoreCase("PLAYER"))
			&&(!commandType.equalsIgnoreCase("USER"))
			&&(!commandType.equalsIgnoreCase("CATALOG"))
			&&(!commandType.equalsIgnoreCase("ACCOUNT")))
			{
				subType=sub;
				commands.remove(0);
			}
			else
			if(sub.equalsIgnoreCase("data"))
				commands.remove(0);
			else
			if((commandType.equalsIgnoreCase("PLAYER")||commandType.equalsIgnoreCase("USER"))
			&&(CMLib.players().getLoadPlayer(sub)!=null))
			{
				subType=sub;
				commands.remove(0);
			}
			else
			if((commandType.equalsIgnoreCase("ACCOUNT"))
			&&(CMLib.players().getLoadAccount(sub)!=null))
			{
				subType=sub;
				commands.remove(0);
			}

			if(commands.size()==0)
			{
				if(S!=null)
					mob.tell(L("You must specify a file name to create, or enter 'SCREEN' to have a screen dump, or 'EMAIL' to send to your email address."));
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
				if(!CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.EXPORTFILE))
				{
					if(S!=null)
						mob.tell(L("You are not allowed to export to memory."));
					return false;
				}
				fileNameCode=4;
			}
			else
			{
				if(!CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.EXPORTFILE))
				{
					if(S!=null)
						mob.tell(L("You are not allowed to export to a file."));
					return false;
				}
				final CMFile F=new CMFile(fileName,mob);
				if(F.isDirectory())
					fileNameCode=2;
			}
			if(fileNameCode<0)
				fileNameCode=1;
		}
		else
		{
			if(S!=null)
				mob.tell(L("You must specify a file name to create, or enter 'SCREEN' to have a screen dump or 'EMAIL' to send to an email address."));
			return false;
		}
		executeInternal(mob,metaFlags,commandType,subType,fileName,Integer.valueOf(fileNameCode),S,area,room);
		return true;
	}

	protected CMObjectType getItemSubType(String subType, int fileNameCode, String[] fileName)
	{
		CMObjectType type=CMObjectType.ITEM;
		if(subType.equalsIgnoreCase("WEAPONS"))
		{
			if(fileNameCode==2)
				fileName[0]=fileName[0]+"/weapons";
			type=CMObjectType.WEAPON;
		}
		else
		if(subType.equalsIgnoreCase("ARMOR"))
		{
			if(fileNameCode==2)
				fileName[0]=fileName[0]+"/armor";
			type=CMObjectType.ARMOR;
		}
		else
		if(subType.equalsIgnoreCase("TECH"))
		{
			if(fileNameCode==2)
				fileName[0]=fileName[0]+"/tech";
			type=CMObjectType.TECH;
		}
		else
		if(subType.equalsIgnoreCase("MISCMAGIC"))
		{
			if(fileNameCode==2)
				fileName[0]=fileName[0]+"/miscmagic";
			type=CMObjectType.MISCMAGIC;
		}
		else
		if(subType.equalsIgnoreCase("CLANITEM"))
		{
			if(fileNameCode==2)
				fileName[0]=fileName[0]+"/clanitem";
			type=CMObjectType.CLANITEM;
		}
		else
		if(subType.equalsIgnoreCase("COMPTECH"))
		{
			if(fileNameCode==2)
				fileName[0]=fileName[0]+"/comptech";
			type=CMObjectType.COMPTECH;
		}
		else
		if(subType.equalsIgnoreCase("SOFTWARE"))
		{
			if(fileNameCode==2)
				fileName[0]=fileName[0]+"/software";
			type=CMObjectType.SOFTWARE;
		}
		else
		if(fileNameCode==2)
		{
			fileName[0]=fileName[0]+"/items";
		}
		return type;
	}

	@SuppressWarnings("rawtypes")
	protected String getCatalogData(Map<String,?> found)
	{
		StringBuilder str = new StringBuilder("<CATADATAS>");
		for(String key : found.keySet())
		{
			List foundMs = (List)found.get(key);
			for(Object P : foundMs)
			{
				CatalogLibrary.CataData data = CMLib.catalog().getCatalogData((Physical)P);
				str.append(data.data(((Physical)P).name()));
			}
		}
		str.append("</CATADATAS>");
		return str.toString();
	}
	
	/**
	 * @see com.planet_ink.coffee_mud.Commands.interfaces.Command#executeInternal(MOB, int, Object...)
	 * args[0] = commandType: AREA, PLAYER, ROOM
	 * args[1] = subType: DATA, PLAYER, MOBS, ITEMS, WEAPONS, ARMOR
	 * args[2] = fileName: MEMORY, SCREEN, EMAIL
	 * args[3] = fileNameType (0=screen, 1=disk, 2=directory, 3=email, 4=memory)
	 * args[4] = Session
	 * args[5] = area
	 * args[6] = room
	 * @return xml document, with filenameType=4, or null
	 */
	@Override
	public Object executeInternal(MOB mob, int metaFlags, Object... args) throws java.io.IOException
	{
		if(!super.checkArguments(internalParameters, args))
			return null;

		final String commandType=(String)args[0];
		final String subType=(String)args[1];
		String fileName=(String)args[2];
		final int fileNameCode = ((Integer)args[3]).intValue();
		final Session S = (Session)args[4];
		final Area area = (Area)args[5];
		final Room room = (Room)args[6];

		final Set<CMObject> custom=new HashSet<CMObject>();
		final Set<String> files=new HashSet<String>();

		String xml="";
		if(subType.equalsIgnoreCase("DATA"))
		{
			if(commandType.equalsIgnoreCase("PLAYER")||commandType.equalsIgnoreCase("USER"))
			{
				final StringBuffer x=new StringBuffer("<PLAYERS>");
				if(S!=null)
					S.rawPrint(L("Reading players..."));
				final java.util.List<String> V=CMLib.database().getUserList();
				for(final String name : V)
				{
					//if(S!=null) S.rawPrint(".");
					final MOB M=CMLib.players().getLoadPlayer(name);
					if(M!=null)
					{
						x.append("\r\n<PLAYER>");
						x.append(CMLib.coffeeMaker().getPlayerXML(M,custom,files));
						x.append("</PLAYER>");
					}
				}
				if(fileNameCode==2)
					fileName=fileName+"/player";
				xml=x.toString()+"</PLAYERS>";
				if(S!=null)
					S.rawPrintln("!");
			}
			else
			if(commandType.equalsIgnoreCase("ACCOUNT"))
			{
				final StringBuffer x=new StringBuffer("<ACCOUNTS>");
				if(S!=null)
					S.rawPrint(L("Reading accounts and players..."));
				for(final Enumeration<PlayerAccount> a=CMLib.players().accounts("",null);a.hasMoreElements();)
				{
					final PlayerAccount A=a.nextElement();
					//if(S!=null) S.rawPrint(".");
					x.append("\r\n<ACCOUNT>");
					x.append(CMLib.coffeeMaker().getAccountXML(A,custom,files));
					x.append("</ACCOUNT>");
				}
				if(fileNameCode==2)
					fileName=fileName+"/account";
				xml=x.toString()+"</ACCOUNTS>";
				if(S!=null)
					S.rawPrintln("!");
			}
			else
			if(commandType.equalsIgnoreCase("CATALOG"))
			{
				final StringBuffer x=new StringBuffer("<CATALOG HASBOTH>");
				if(S!=null)
					S.rawPrint(L("Reading catalog items and mobs..."));
				x.append("\r\n<MOBS>");
				Map<String,List<MOB>> foundMobs=new TreeMap<String,List<MOB>>();
				x.append(CMLib.coffeeMaker().getMobsXML(Arrays.asList(CMLib.catalog().getCatalogMobs()), custom, files, foundMobs));
				x.append("\r\n</MOBS>\r\n");
				x.append("\r\n").append(getCatalogData(foundMobs));
				foundMobs.clear();
				x.append("\r\n<ITEMS>");
				Map<String,List<Item>> foundItems=new TreeMap<String,List<Item>>();
				x.append(CMLib.coffeeMaker().getItemsXML(Arrays.asList(CMLib.catalog().getCatalogItems()), foundItems, files, null));
				x.append("\r\n</ITEMS>\r\n");
				x.append("\r\n").append(getCatalogData(foundItems));
				foundItems.clear();
				if(fileNameCode==2)
					fileName=fileName+"/catalog";
				if(S!=null)
					S.rawPrintln("!");
				xml=x.toString()+"</CATALOG>";
			}
			else
			if(commandType.equalsIgnoreCase("ROOM"))
			{
				xml=CMLib.coffeeMaker().getRoomXML(room,custom,files,true).toString();
				if(fileNameCode==2)
					fileName=fileName+"/room";
			}
			else
			if(commandType.equalsIgnoreCase("AREA"))
			{
				if(S!=null)
					S.rawPrint(L("Reading area '@x1'...",area.Name()));
				xml=CMLib.coffeeMaker().getAreaXML(area,S,custom,files,true).toString();
				if(fileNameCode==2)
				{
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
				if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.EXPORT))
				{
					if(S!=null)
						mob.tell(L("You are not allowed to export world data."));
					return Boolean.FALSE;
				}
				StringBuffer buf=new StringBuffer("");
				if(fileNameCode!=2)
					buf.append("<AREAS>");
				for(final Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
				{
					final Area A=a.nextElement();
					if(A!=null)
					{
						if(S!=null)
							S.rawPrint(L("Reading area '@x1'...",A.name()));
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
				if(fileNameCode!=2)
					xml=buf.toString()+"</AREAS>";
			}
		}
		else
		if(commandType.equalsIgnoreCase("PLAYER")||commandType.equalsIgnoreCase("USER"))
		{
			final StringBuffer x=new StringBuffer("<PLAYERS>");
			final MOB M=CMLib.players().getLoadPlayer(subType);
			if(M!=null)
			{
				x.append("\r\n<PLAYER>");
				x.append(CMLib.coffeeMaker().getPlayerXML(M,custom,files));
				x.append("</PLAYER>");
			}
			if(fileNameCode==2)
				fileName=fileName+"/player";
			xml=x.toString()+"</PLAYERS>";
		}
		else
		if(commandType.equalsIgnoreCase("ACCOUNT"))
		{
			final StringBuffer x=new StringBuffer("<ACCOUNTS>");
			final PlayerAccount A=CMLib.players().getLoadAccount(subType);
			if(A!=null)
			{
				x.append("\r\n<ACCOUNT>");
				x.append(CMLib.coffeeMaker().getAccountXML(A,custom,files));
				x.append("</ACCOUNT>");
			}
			if(fileNameCode==2)
				fileName=fileName+"/player";
			xml=x.toString()+"</ACCOUNTS>";
		}
		else
		if(commandType.equalsIgnoreCase("CATALOG"))
		{
			String[] fileNameAdj = new String[]{fileName};
			CMObjectType type = getItemSubType(subType, fileNameCode,fileNameAdj);
			fileName = fileNameAdj[0];
			
			final StringBuffer x=new StringBuffer("<CATALOG");
			if(((type!=null)&&(type != CMObjectType.ITEM))||(subType.equalsIgnoreCase("ITEMS")))
				x.append(" HASITEMS>");
			else
			if(subType.equalsIgnoreCase("MOBS"))
				x.append(" HASMOBS>");
			else
				x.append(" HASBOTH>");
			if((((type==null)||(type == CMObjectType.ITEM)))&&(!subType.equalsIgnoreCase("ITEMS")))
			{
				x.append("\r\n<MOBS>");
				final Map<String,List<MOB>> found = new TreeMap<String,List<MOB>>();
				x.append(CMLib.coffeeMaker().getMobsXML(Arrays.asList(CMLib.catalog().getCatalogMobs()), custom, files, found));
				x.append("\r\n</MOBS>\r\n");
				x.append("\r\n").append(getCatalogData(found));
			}
			if(!subType.equalsIgnoreCase("MOBS"))
			{
				Map<String,List<Item>> found=new TreeMap<String,List<Item>>();
				x.append("\r\n<ITEMS>");
				x.append(CMLib.coffeeMaker().getItemsXML(Arrays.asList(CMLib.catalog().getCatalogItems()), found, files, type));
				x.append("\r\n</ITEMS>\r\n");
				x.append("\r\n").append(getCatalogData(found));
			}
			if((fileNameCode==2)&&(((type==null)||(type == CMObjectType.ITEM))))
				fileName=fileName+"/catalog";
			if(S!=null)
				S.rawPrintln("!");
			xml=x.toString()+"</CATALOG>";
		}
		else
		if(subType.equalsIgnoreCase("MOBS"))
		{
			if(fileNameCode==2)
				fileName=fileName+"/mobs";
			final Hashtable<String,List<MOB>> found=new Hashtable<String,List<MOB>>();
			if(commandType.equalsIgnoreCase("ROOM"))
				xml="<MOBS>"+CMLib.coffeeMaker().getRoomMobs(room,custom,files,found).toString()+"</MOBS>";
			else
			if(commandType.equalsIgnoreCase("AREA"))
			{
				if(S!=null)
					S.rawPrint(L("Reading area mobs '@x1'...",area.Name()));
				final StringBuffer buf=new StringBuffer("<MOBS>");
				for(final Enumeration<Room> r=area.getCompleteMap();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
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
					S.rawPrint(L("Reading world mobs ..."));
				final StringBuffer buf=new StringBuffer("<MOBS>");
				try
				{
					for(final Enumeration<Room> r=CMLib.map().rooms();r.hasMoreElements();)
					{
						final Room R=r.nextElement();
						//if(S!=null) S.rawPrint(".");
						buf.append(CMLib.coffeeMaker().getRoomMobs(R,custom,files,found).toString());
					}
				}
				catch(final NoSuchElementException e)
				{
				}
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

			String[] fileNameAdj = new String[]{fileName};
			CMObjectType type = getItemSubType(subType, fileNameCode,fileNameAdj);
			fileName = fileNameAdj[0];
			
			final Hashtable<String,List<Item>> found=new Hashtable<String,List<Item>>();
			if(commandType.equalsIgnoreCase("ROOM"))
				xml="<ITEMS>"+CMLib.coffeeMaker().getRoomItems(room,found,files,type).toString()+"</ITEMS>";
			else
			if(commandType.equalsIgnoreCase("AREA"))
			{
				if(S!=null)
					S.rawPrint(L("Reading area @x1 '@x2'...",subType.toLowerCase(),area.Name()));
				final StringBuffer buf=new StringBuffer("<ITEMS>");
				for(final Enumeration<Room> r=area.getCompleteMap();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
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
					S.rawPrint(L("Reading world @x1 ...",subType.toLowerCase()));
				final StringBuffer buf=new StringBuffer("<ITEMS>");
				try
				{
					for(final Enumeration<Room> r=CMLib.map().rooms();r.hasMoreElements();)
					{
						final Room R=r.nextElement();
						//if(S!=null) S.rawPrint(".");
						buf.append(CMLib.coffeeMaker().getRoomItems(R,found,files,type).toString());
					}
				}
				catch (final NoSuchElementException e)
				{
				}
				xml=buf.toString()+"</ITEMS>";
				if(S!=null)
					S.rawPrintln("!");
			}
		}
		if(custom.size()>0)
		{
			final StringBuffer str=new StringBuffer("<CUSTOM>");
			for (final Object o : custom)
			{
				if(o instanceof Race)
					str.append(((Race)o).racialParms());
				else
				if(o instanceof CharClass)
					str.append(((CharClass)o).classParms());
				else
				if(o instanceof Ability)
					str.append(CMLib.coffeeMaker().getGenAbilityXML((Ability)o));
				else
				if(o instanceof Manufacturer)
					str.append("<MANUFACTURER>").append(((Manufacturer)o).getXml()).append("</MANUFACTURER>");
			}
			str.append("</CUSTOM>");
			xml+=str.toString();
		}
		if(files.size()>0)
		{
			final StringBuffer str=new StringBuffer("<FILES>");
			for (final Object O : files)
			{
				final String filename=(String)O;
				final StringBuffer buf=new CMFile(Resources.makeFileResourceName(filename),null,CMFile.FLAG_LOGERRORS).text();
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
		if(fileNameCode==2)
			fileName=fileName+"/extras";
		if(fileNameCode==4)
			return xml;
		reallyExport(mob,S,fileName,xml);
		return null;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

	@Override
	public boolean securityCheck(MOB mob)
	{
		return CMSecurity.isAllowedContainsAny(mob, mob.location(), CMSecurity.SECURITY_EXPORT_GROUP);
	}
}
