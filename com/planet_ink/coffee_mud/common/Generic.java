package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Generic
{

	public static boolean get(int x, int m)
	{
		return (x&m)==m;
	}
	
	private static String parseOutAngleBrackets(String s)
	{
		int x=s.indexOf("<");
		while(x>=0)
		{
			s=s.substring(0,x)+"&lt;"+s.substring(x+1);
			x=s.indexOf("<");
		}
		x=s.indexOf(">");
		while(x>=0)
		{
			s=s.substring(0,x)+"&gt;"+s.substring(x+1);
			x=s.indexOf(">");
		}
		return s;
	}
	
	private final static String hexStr="0123456789ABCDEF";
	private static String restoreAngleBrackets(String s)
	{
		StringBuffer buf=new StringBuffer(s);
		int loop=0;
		while(loop<buf.length())
		{
			switch(buf.charAt(loop))
			{
			case '&':
				if(loop<buf.length()-3)
				{
					if(buf.substring(loop,loop+3).equalsIgnoreCase("lt;"))
						buf.replace(loop,loop+3,"<");
					else
					if(buf.substring(loop,loop+3).equalsIgnoreCase("gt;"))
						buf.replace(loop,loop+3,">");
				}
				break;
			case '%':
				if(loop<buf.length()-2)
				{
					int dig1=hexStr.indexOf(buf.charAt(loop+1));
					int dig2=hexStr.indexOf(buf.charAt(loop+2));
					if((dig1>=0)&&(dig2>=0))
					{
						buf.setCharAt(loop,(char)((dig1*16)+dig2));
						buf.deleteCharAt(loop+1);
						buf.deleteCharAt(loop+1);
					}
				}
				break;
			}
			++loop;
		}
		return buf.toString();
	}
	
	public static int envFlags(Environmental E)
	{
		int f=0;
		if(E instanceof Item)
		{
			Item item=(Item)E;
			if(item.isDroppable())
				f=f|1;
			if(item.isGettable())
				f=f|2;
			if(item.isReadable())
				f=f|4;
			if(item.isRemovable())
				f=f|8;
		}
		if(E instanceof Container)
		{
			Container container=(Container)E;

			if(container.hasALid())
				f=f|32;
			if(container.hasALock())
				f=f|64;
			// defaultsclosed 128
			// defaultslocked 256
		}
		else
		if(E instanceof Exit)
		{
			Exit exit=(Exit)E;
			if(exit.isReadable())
				f=f|4;
			//if(exit.isTrapped())
			//	f=f|16;
			if(exit.hasADoor())
				f=f|32;
			if(exit.hasALock())
				f=f|64;
			if(exit.defaultsClosed())
				f=f|128;
			if(exit.defaultsLocked())
				f=f|256;
			//if(exit.levelRestricted())
			//	f=f|512;
			//if(exit.classRestricted())
			//	f=f|1024;
			//if(exit.alignmentRestricted())
			//	f=f|2048;
		}
		return f;
	}

	public static void setEnvFlags(Environmental E, int f)
	{
		if(E instanceof Item)
		{
			Item item=(Item)E;
			item.setDroppable(get(f,1));
			item.setGettable(get(f,2));
			item.setReadable(get(f,4));
			item.setRemovable(get(f,8));
		}
		if(E instanceof Container)
		{
			Container container=(Container)E;
			container.setLidsNLocks(get(f,32),!get(f,32),get(f,64),get(f,64));
		}
		else
		if(E instanceof Exit)
		{
			Exit exit=(Exit)E;
			exit.setReadable(get(f,4));
			if(get(f,16)) Log.errOut("Generic","Exit is trapped!");
			boolean HasDoor=get(f,32);
			boolean HasLock=get(f,64);
			boolean DefaultsClosed=get(f,128);
			boolean DefaultsLocked=get(f,256);
			if(get(f,512)) Log.errOut("Generic","Exit is level restricted!");
			if(get(f,1024)) Log.errOut("Generic","Exit is class restricted!");
			if(get(f,2048)) Log.errOut("Generic","Exit is alignment restricted!");
			exit.setDoorsNLocks(HasDoor,!DefaultsClosed,DefaultsClosed,HasLock,DefaultsLocked,DefaultsLocked);
		}
	}

	public static String getPropertiesStr(Environmental E, boolean fromTop)
	{
		if(E==null)
		{
			Log.errOut("Generic","getPropertiesStr: null 'E'");
			return "";
		}
		else
			return (E.isGeneric()?getGenPropertiesStr(E):"") + (fromTop?getOrdPropertiesStr(E):"");
	}

	private static String getOrdPropertiesStr(Environmental E)
	{
		if(E instanceof Room)
			return getExtraEnvPropertiesStr(E);
		else
		if(E instanceof Area)
			return getExtraEnvPropertiesStr(E);
		else
		if(E instanceof Ability)
			return XMLManager.convertXMLtoTag("AWRAP",E.text());
		else
		if(E instanceof Item)
		{
			Item item=(Item)E;
			String xml=
				(((item instanceof Container)&&(((Container)item).capacity()>0))
				?XMLManager.convertXMLtoTag("IID",""+E):"")
				+XMLManager.convertXMLtoTag("IWORN",""+item.rawWornCode())
				+XMLManager.convertXMLtoTag("ILOC",""+((item.container()!=null)?(""+item.container()):""))
				+XMLManager.convertXMLtoTag("IUSES",""+item.usesRemaining())
				+XMLManager.convertXMLtoTag("ILEVL",""+E.baseEnvStats().level())
				+XMLManager.convertXMLtoTag("IABLE",""+E.baseEnvStats().ability())
				+((E.isGeneric()?"":XMLManager.convertXMLtoTag("ITEXT",""+E.text())));
			return xml;
		}
		else
		if(E instanceof MOB)
		{
			String xml=
				 XMLManager.convertXMLtoTag("MLEVL",""+E.baseEnvStats().level())
				+XMLManager.convertXMLtoTag("MABLE",""+E.baseEnvStats().ability())
				+XMLManager.convertXMLtoTag("MREJUV",""+E.baseEnvStats().rejuv())
				+((E.isGeneric()?"":XMLManager.convertXMLtoTag("ITEXT",""+E.text())));
			return xml;
		}
		return "";
	}

	private static String getGenPropertiesStr(Environmental E)
	{
		StringBuffer text=new StringBuffer("");
		text.append(getEnvPropertiesStr(E));

		text.append(XMLManager.convertXMLtoTag("FLAG",envFlags(E)));

		if(E instanceof Exit)
		{
			Exit exit=(Exit)E;
			text.append(
			 XMLManager.convertXMLtoTag("CLOSTX",exit.closedText())
			+XMLManager.convertXMLtoTag("DOORNM",exit.doorName())
			+XMLManager.convertXMLtoTag("OPENNM",exit.openWord())
			+XMLManager.convertXMLtoTag("CLOSNM",exit.closeWord())
			+XMLManager.convertXMLtoTag("KEYNM",exit.keyName())
			+XMLManager.convertXMLtoTag("OPENTK",exit.openDelayTicks()));
		}

		if(E instanceof Item)
		{
			Item item=(Item)E;
			text.append(
			 XMLManager.convertXMLtoTag("IDENT",item.rawSecretIdentity())
			+XMLManager.convertXMLtoTag("VALUE",item.baseGoldValue())
			//+XMLManager.convertXMLtoTag("USES",item.usesRemaining()) // handled 'from top' & in db
			+XMLManager.convertXMLtoTag("MTRAL",item.material())
			+XMLManager.convertXMLtoTag("READ",item.readableText())
			+XMLManager.convertXMLtoTag("WORNL",item.rawLogicalAnd())
			+XMLManager.convertXMLtoTag("WORNB",item.rawProperLocationBitmap()));
			if(E instanceof Container)
			{
				text.append(XMLManager.convertXMLtoTag("CAPA",((Container)item).capacity()));
				text.append(XMLManager.convertXMLtoTag("CONT",((Container)item).containTypes()));
			}
			if(E instanceof Weapon)
				text.append(XMLManager.convertXMLtoTag("CAPA",((Weapon)item).ammunitionCapacity()));
		}
		
		if(E instanceof Rideable)
		{
			text.append(XMLManager.convertXMLtoTag("RIDET",((Rideable)E).rideBasis()));
			text.append(XMLManager.convertXMLtoTag("RIDEC",((Rideable)E).mobCapacity()));
		}

		if(E instanceof Food)
			text.append(XMLManager.convertXMLtoTag("CAPA2",((Food)E).nourishment()));

		if(E instanceof Drink)
		{
			text.append(XMLManager.convertXMLtoTag("CAPA2",((Drink)E).liquidHeld()));
			text.append(XMLManager.convertXMLtoTag("DRINK",((Drink)E).thirstQuenched()));
		}

		if(E instanceof Weapon)
		{
			text.append(XMLManager.convertXMLtoTag("TYPE",((Weapon)E).weaponType()));
			text.append(XMLManager.convertXMLtoTag("CLASS",((Weapon)E).weaponClassification()));
			text.append(XMLManager.convertXMLtoTag("MINR",((Weapon)E).minRange()));
			text.append(XMLManager.convertXMLtoTag("MAXR",((Weapon)E).maxRange()));
		}

		if(E instanceof MOB)
		{
			text.append(XMLManager.convertXMLtoTag("ALIG",((MOB)E).getAlignment()));
			text.append(XMLManager.convertXMLtoTag("MONEY",((MOB)E).getMoney()));
			text.append(XMLManager.convertXMLtoTag("GENDER",""+(char)((MOB)E).baseCharStats().getStat(CharStats.GENDER)));
			text.append(XMLManager.convertXMLtoTag("MRACE",""+((MOB)E).baseCharStats().getMyRace().ID()));

			StringBuffer itemstr=new StringBuffer("");
			for(int b=0;b<((MOB)E).inventorySize();b++)
			{
				Item I=((MOB)E).fetchInventory(b);
				if(I!=null)
				{
					itemstr.append("<ITEM>");
					itemstr.append(XMLManager.convertXMLtoTag("ICLASS",CMClass.className(I)));
					itemstr.append(XMLManager.convertXMLtoTag("IDATA",getPropertiesStr(I,true)));
					itemstr.append("</ITEM>");
				}
			}
			text.append(XMLManager.convertXMLtoTag("INVEN",itemstr.toString()));

			StringBuffer abilitystr=new StringBuffer("");
			for(int b=0;b<((MOB)E).numAbilities();b++)
			{
				Ability A=((MOB)E).fetchAbility(b);
				if((A!=null)&&(!A.isBorrowed(E)))
				{
					abilitystr.append("<ABLTY>");
					abilitystr.append(XMLManager.convertXMLtoTag("ACLASS",CMClass.className(A)));
					abilitystr.append(XMLManager.convertXMLtoTag("ADATA",getPropertiesStr(A,true)));
					abilitystr.append("</ABLTY>");
				}
			}
			text.append(XMLManager.convertXMLtoTag("ABLTYS",abilitystr.toString()));

			if(E instanceof Banker)
			{
				text.append(XMLManager.convertXMLtoTag("BANK",""+((Banker)E).bankChain()));
				text.append(XMLManager.convertXMLtoTag("COININT",""+((Banker)E).getCoinInterest()));
				text.append(XMLManager.convertXMLtoTag("ITEMINT",""+((Banker)E).getCoinInterest()));
			}
			if(E instanceof ShopKeeper)
			{
				text.append(XMLManager.convertXMLtoTag("SELLCD",((ShopKeeper)E).whatIsSold()));

				Vector V=((ShopKeeper)E).getUniqueStoreInventory();
				itemstr=new StringBuffer("");
				for(int b=0;b<V.size();b++)
				{
					Environmental Env=(Environmental)V.elementAt(b);
					itemstr.append("<SHITEM>");
					itemstr.append(XMLManager.convertXMLtoTag("SICLASS",CMClass.className(Env)));
					itemstr.append(XMLManager.convertXMLtoTag("SISTOCK",((ShopKeeper)E).numberInStock(Env)));
					itemstr.append(XMLManager.convertXMLtoTag("SIDATA",getPropertiesStr(Env,true)));
					itemstr.append("</SHITEM>");
				}
				text.append(XMLManager.convertXMLtoTag("STORE",itemstr.toString()));
			}
		}
		return text.toString();
	}
	
	public static String unpackErr(String where, String msg)
	{
		Log.errOut("Generic","unpack"+where+"FromXML: "+msg);
		return msg;
	}
	
	public static String unpackRoomFromXML(String buf, boolean andContent)
	{
		Vector xml=XMLManager.parseAllXML(buf);
		if(xml==null) return unpackErr("Room","null 'xml'");
		Vector roomData=XMLManager.getRealContentsFromPieces(xml,"AROOM");
		if(roomData==null) return unpackErr("Room","null 'roomData'");
		return unpackRoomFromXML(roomData,andContent);
	}
	
	public static String unpackRoomFromXML(Vector xml, boolean andContent)
	{
		Area myArea=CMMap.getArea(XMLManager.getValFromPieces(xml,"RAREA"));
		if(myArea==null) return unpackErr("Room","null 'myArea'");
		String roomClass=XMLManager.getValFromPieces(xml,"RCLAS");
		Room newRoom=CMClass.getLocale(roomClass);
		if(newRoom==null) return unpackErr("Room","null 'newRoom'");
		newRoom.setID(XMLManager.getValFromPieces(xml,"ROOMID"));
		if(newRoom.ID().equals("NEW")) newRoom.setID(ExternalPlay.getOpenRoomID(myArea.name()));
		if(CMMap.getRoom(newRoom.ID())!=null) return "Room Exists: "+newRoom.ID();
		newRoom.setArea(myArea);
		ExternalPlay.DBCreateRoom(newRoom,roomClass);
		CMMap.map.addElement(newRoom);
		newRoom.setDisplayText(XMLManager.getValFromPieces(xml,"RDISP"));
		newRoom.setDescription(XMLManager.getValFromPieces(xml,"RDESC"));
		newRoom.setMiscText(restoreAngleBrackets(XMLManager.getValFromPieces(xml,"RTEXT")));
		
		// now EXITS!
		Vector xV=XMLManager.getRealContentsFromPieces(xml,"ROOMEXITS");
		if(xV==null) return unpackErr("Room","null 'xV'"+" in room "+newRoom.ID());
		for(int x=0;x<xV.size();x++)
		{
			XMLManager.XMLpiece xblk=(XMLManager.XMLpiece)xV.elementAt(x);
			if((!xblk.tag.equalsIgnoreCase("REXIT"))||(xblk.contents==null))
				return unpackErr("Room","??"+xblk.tag+" in room "+newRoom.ID());
			int dir=XMLManager.getIntFromPieces(xblk.contents,"XDIRE");
			if((dir<0)||(dir>=Directions.NUM_DIRECTIONS))
				return unpackErr("Room","Unknown direction: "+dir+" in room "+newRoom.ID());
			String doorID=XMLManager.getValFromPieces(xblk.contents,"XDOOR");
			Vector xxV=XMLManager.getContentsFromPieces(xblk.contents,"XEXIT");
			if(xxV==null) return unpackErr("Room","null 'xxV'"+" in room "+newRoom.ID());
			Exit exit=CMClass.getExit("GenExit");
			if(xxV.size()>0)
			{
				exit=CMClass.getExit(XMLManager.getValFromPieces(xxV,"EXID"));
				if(xxV==null) return unpackErr("Room","null 'exit'"+" in room "+newRoom.ID());
				exit.setMiscText(restoreAngleBrackets(XMLManager.getValFromPieces(xxV,"EXDAT")));
				newRoom.rawExits()[dir]=exit;
			}
			exit.recoverEnvStats();
			if(doorID.length()>0)
			{
				Room link=CMMap.getRoom(doorID);
				if(link!=null)
					newRoom.rawDoors()[dir]=link;
				else
				{
					newRoom.rawExits()[dir]=exit; // get will get the fake one too!
					exit.setExitParams(exit.doorName(),doorID,exit.openWord(),exit.closedText());
				}
			}
		}
		
		// find any mis-linked exits and fix them!
		for(int m=0;m<CMMap.map.size();m++)
		{
			boolean changed=false;
			Room room=(Room)CMMap.map.elementAt(m);
			for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
			{
				Exit exit=room.rawExits()[d];
				if((exit!=null)&&(exit.closeWord().equalsIgnoreCase(newRoom.ID())))
				{
					Exit newExit=(Exit)exit.newInstance();
					exit.setExitParams(exit.doorName(),newExit.closeWord(),exit.openWord(),exit.closedText());
					room.rawDoors()[d]=newRoom;
					changed=true;
				}
				else
				if((room.rawDoors()[d]!=null)&&(room.rawDoors()[d].ID().equals(newRoom.ID())))
				{
					room.rawDoors()[d]=newRoom;
					changed=true;
				}
			}
			if(changed) ExternalPlay.DBUpdateExits(room);
		}
		ExternalPlay.DBUpdateRoom(newRoom);
		ExternalPlay.DBUpdateExits(newRoom);
		if(andContent)
		{
			Vector cV=XMLManager.getRealContentsFromPieces(xml,"ROOMCONTENT");
			if(cV==null) return unpackErr("Room","null 'cV'"+" in room "+newRoom.ID());
			if(cV.size()>0)
			{
				Vector mV=XMLManager.getRealContentsFromPieces(cV,"ROOMMOBS");
				if(mV!=null) //return unpackErr("Room","null 'mV'"+" in room "+newRoom.ID());
				for(int m=0;m<mV.size();m++)
				{
					XMLManager.XMLpiece mblk=(XMLManager.XMLpiece)mV.elementAt(m);
					if((!mblk.tag.equalsIgnoreCase("RMOB"))||(mblk.contents==null))
						return unpackErr("Room","bad 'mblk'"+" in room "+newRoom.ID());
					String mClass=XMLManager.getValFromPieces(mblk.contents,"MCLAS");
					MOB newMOB=CMClass.getMOB(mClass);
					if(newMOB==null) return unpackErr("Room","null 'mClass': "+mClass+" in room "+newRoom.ID());
					newMOB.setMiscText(restoreAngleBrackets(XMLManager.getValFromPieces(mblk.contents,"MTEXT")));
					newMOB.baseEnvStats().setLevel(XMLManager.getIntFromPieces(mblk.contents,"MLEVL"));
					newMOB.baseEnvStats().setAbility(XMLManager.getIntFromPieces(mblk.contents,"MABLE"));
					newMOB.baseEnvStats().setRejuv(XMLManager.getIntFromPieces(mblk.contents,"MREJV"));
					newMOB.setStartRoom(newRoom);
					newMOB.setLocation(newRoom);
					newMOB.recoverCharStats();
					newMOB.recoverEnvStats();
					newMOB.recoverMaxState();
					newMOB.resetToMaxState();
					newMOB.bringToLife(newRoom,true);
				}
				Hashtable itemLocTable=new Hashtable();
				Hashtable identTable=new Hashtable();
				Vector iV=XMLManager.getRealContentsFromPieces(cV,"ROOMITEMS");
				if(iV!=null) //return unpackErr("Room","null 'iV'"+" in room "+newRoom.ID());
				for(int i=0;i<iV.size();i++)
				{
					XMLManager.XMLpiece iblk=(XMLManager.XMLpiece)iV.elementAt(i);
					if((!iblk.tag.equalsIgnoreCase("RITEM"))||(iblk.contents==null))
						return unpackErr("Room","bad 'iblk'"+" in room "+newRoom.ID());
					String iClass=XMLManager.getValFromPieces(iblk.contents,"ICLAS");
					Item newItem=CMClass.getItem(iClass);
					if(newItem==null) return unpackErr("Room","null 'iClass': "+iClass+" in room "+newRoom.ID());
					if(newItem instanceof Container)
						identTable.put(XMLManager.getValFromPieces(iblk.contents,"IIDEN"),newItem);
					String iloc=XMLManager.getValFromPieces(iblk.contents,"ILOCA");
					if(iloc.length()>0) itemLocTable.put(iloc,newItem);
					newItem.baseEnvStats().setLevel(XMLManager.getIntFromPieces(iblk.contents,"ILEVL"));
					newItem.baseEnvStats().setAbility(XMLManager.getIntFromPieces(iblk.contents,"IABLE"));
					newItem.baseEnvStats().setRejuv(XMLManager.getIntFromPieces(iblk.contents,"IREJV"));
					newItem.setUsesRemaining(XMLManager.getIntFromPieces(iblk.contents,"IUSES"));
					newItem.setMiscText(restoreAngleBrackets(XMLManager.getValFromPieces(iblk.contents,"ITEXT")));
					newItem.setContainer(null);
					newItem.recoverEnvStats();
					newRoom.addItem(newItem);
					newItem.recoverEnvStats();
				}
				for(Enumeration e=itemLocTable.keys();e.hasMoreElements();)
				{
					String loc=(String)e.nextElement();
					Item childI=(Item)itemLocTable.get(loc);
					Item parentI=(Item)identTable.get(loc);
					childI.setContainer(parentI);
					childI.recoverEnvStats();
					parentI.recoverEnvStats();
				}
			}
		}
		// equivalent to clear debriandrestart
		ExternalPlay.clearDebri(newRoom,0);
		ExternalPlay.DBUpdateItems(newRoom);
		newRoom.startItemRejuv();
		ExternalPlay.DBUpdateMOBs(newRoom);
		return "";
	}
	public static String unpackAreaFromXML(String buf, boolean andRooms)
	{
		Vector xml=XMLManager.parseAllXML(buf);
		if(xml==null) return unpackErr("Area","null 'xml'");
		Vector aV=XMLManager.getRealContentsFromPieces(xml,"AREA");
		if(aV==null) return unpackErr("Area","null 'aV'");
		String areaClass=XMLManager.getValFromPieces(aV,"ACLAS");
		String areaName=XMLManager.getValFromPieces(aV,"ANAME");
		if(CMMap.getArea(areaName)!=null) return "Area Exists: "+areaName;
		if(CMClass.getAreaType(areaClass)==null) return unpackErr("Area","No class: "+areaClass);
		Area newArea=ExternalPlay.DBCreateArea(areaName,areaClass);
		if(newArea==null) return unpackErr("Area","null 'area'");
		
		newArea.setDescription(XMLManager.getValFromPieces(aV,"ADESC"));
		newArea.setClimateType(XMLManager.getIntFromPieces(aV,"ACLIM"));
		newArea.setSubOpList(XMLManager.getValFromPieces(aV,"ASUBS"));
		newArea.setMiscText(restoreAngleBrackets(XMLManager.getValFromPieces(aV,"ADATA")));
		ExternalPlay.DBUpdateArea(newArea);
		if(andRooms)
		{
			Vector rV=XMLManager.getRealContentsFromPieces(aV,"AROOMS");
			if(rV==null) return unpackErr("Area","null 'rV'");
			for(int r=0;r<rV.size();r++)
			{
				XMLManager.XMLpiece ablk=(XMLManager.XMLpiece)rV.elementAt(r);
				if((!ablk.tag.equalsIgnoreCase("AROOM"))||(ablk.contents==null))
					return unpackErr("Area","??"+ablk.tag);
				String err=unpackRoomFromXML(ablk.contents,true);
				if(err.length()>0) return err;
			}
		}
		return "";
	}

	public static StringBuffer getAreaXML(Area area, boolean andRooms)
	{
		StringBuffer buf=new StringBuffer("");
		if(area==null) return buf;
		boolean mobile=area.getMobility();
		area.toggleMobility(false);
		buf.append("<AREA>");
		buf.append(XMLManager.convertXMLtoTag("ACLAS",area.ID()));
		buf.append(XMLManager.convertXMLtoTag("ANAME",area.name()));
		buf.append(XMLManager.convertXMLtoTag("ADESC",area.description()));
		buf.append(XMLManager.convertXMLtoTag("ACLIM",area.climateType()));
		buf.append(XMLManager.convertXMLtoTag("ASUBS",area.getSubOpList()));
		buf.append(XMLManager.convertXMLtoTag("ADATA",area.text()));
		if(andRooms)
		{
			Vector rooms=area.getMyMap();
			if(rooms.size()==0)
				buf.append("<AROOMS />");
			else
			{
				buf.append("<AROOMS>");
				for(int r=0;r<rooms.size();r++)
				{
					Room room=(Room)rooms.elementAt(r);
					if((room!=null)&&(room.ID()!=null)&&(room.ID().length()>0))
						buf.append(getRoomXML(room,true));
				}
				buf.append("</AROOMS>");
			}
		}
		buf.append("</AREA>");
		area.toggleMobility(mobile);
		return buf;
	}
	
	public static StringBuffer getRoomXML(Room room, boolean andContent)
	{
		StringBuffer buf=new StringBuffer("");
		if(room==null) return buf;
		
		// do this quick before a tick messes it up!
		Vector inhabs=new Vector();
		boolean mobile=room.getArea().getMobility();
		room.getArea().toggleMobility(false);
		ExternalPlay.resetRoom(room);
		if(andContent)
		for(int i=0;i<room.numInhabitants();i++)
			inhabs.addElement(room.fetchInhabitant(i));
		Vector items=new Vector();
		if(andContent)
		for(int i=0;i<room.numItems();i++)
			items.addElement(room.fetchItem(i));
		room.getArea().toggleMobility(mobile);
		
		buf.append("<AROOM>");
		buf.append(XMLManager.convertXMLtoTag("ROOMID",room.ID()));
		buf.append(XMLManager.convertXMLtoTag("RAREA",room.getArea().name()));
		buf.append(XMLManager.convertXMLtoTag("RCLAS",CMClass.className(room)));
		buf.append(XMLManager.convertXMLtoTag("RDISP",room.displayText()));
		buf.append(XMLManager.convertXMLtoTag("RDESC",room.description()));
		buf.append(XMLManager.convertXMLtoTag("RTEXT",parseOutAngleBrackets(room.text())));
		buf.append("<ROOMEXITS>");
		for(int e=0;e<Directions.NUM_DIRECTIONS;e++)
		{
			Room door=room.rawDoors()[e];
			Exit exit=room.rawExits()[e];
			if(((door!=null)&&(door.ID().length()>0))||((door==null)&&(exit!=null)))
			{
				buf.append("<REXIT>");
				buf.append(XMLManager.convertXMLtoTag("XDIRE",e));
				if(door==null)
					buf.append("<XDOOR />");
				else
					buf.append(XMLManager.convertXMLtoTag("XDOOR",door.ID()));
				if(exit==null)
					buf.append("<XEXIT />");
				else
				{
					buf.append("<XEXIT>");
					buf.append(XMLManager.convertXMLtoTag("EXID",exit.ID()));
					buf.append(XMLManager.convertXMLtoTag("EXDAT",parseOutAngleBrackets(exit.text())));
					buf.append("</XEXIT>");
				}
				buf.append("</REXIT>");
			}
		}
		buf.append("</ROOMEXITS>");
		if(andContent)
		{
			buf.append("<ROOMCONTENT>");
			if(inhabs.size()==0)
				buf.append("<ROOMMOBS />");
			else
			{
				buf.append("<ROOMMOBS>");
				for(int i=0;i<inhabs.size();i++)
				{
					MOB mob=(MOB)inhabs.elementAt(i);
					if((mob.isMonster())&&((mob.amFollowing()==null)||(mob.amFollowing().isMonster())))
					{
						buf.append("<RMOB>");
						buf.append(XMLManager.convertXMLtoTag("MCLAS",CMClass.className(mob)));
						buf.append(XMLManager.convertXMLtoTag("MLEVL",mob.baseEnvStats().level()));
						buf.append(XMLManager.convertXMLtoTag("MABLE",mob.baseEnvStats().ability()));
						buf.append(XMLManager.convertXMLtoTag("MREJV",mob.baseEnvStats().rejuv()));
						buf.append(XMLManager.convertXMLtoTag("MTEXT",parseOutAngleBrackets(mob.text())));
						buf.append("</RMOB>");
					}
				}
				buf.append("</ROOMMOBS>");
			}
			if(items.size()==0)
				buf.append("<ROOMITEMS />");
			else
			{
				buf.append("<ROOMITEMS>");
				for(int i=0;i<items.size();i++)
				{
					buf.append("<RITEM>");
					Item item=(Item)items.elementAt(i);
					buf.append(XMLManager.convertXMLtoTag("ICLAS",CMClass.className(item)));
					if((item instanceof Container)&&(((Container)item).capacity()>0))
						buf.append(XMLManager.convertXMLtoTag("IIDEN",""+item));
					if(item.container()==null)
						buf.append("<ILOCA />");
					else
						buf.append(XMLManager.convertXMLtoTag("ILOCA",""+item.container()));
					buf.append(XMLManager.convertXMLtoTag("IREJV",item.baseEnvStats().rejuv()));
					buf.append(XMLManager.convertXMLtoTag("IUSES",item.usesRemaining()));
					buf.append(XMLManager.convertXMLtoTag("ILEVL",item.baseEnvStats().level()));
					buf.append(XMLManager.convertXMLtoTag("IABLE",item.baseEnvStats().ability()));
					buf.append(XMLManager.convertXMLtoTag("ITEXT",parseOutAngleBrackets(item.text())));
					buf.append("</RITEM>");
				}
				buf.append("</ROOMITEMS>");
			}
			buf.append("</ROOMCONTENT>");
		}
		buf.append("</AROOM>");
		return buf;
	}
	
	public static void setPropertiesStr(Environmental E, String buf, boolean fromTop)
	{
		Vector V=XMLManager.parseAllXML(buf);
		if(V==null)
			Log.errOut("Generic","setPropertiesStr: null 'V': "+((E==null)?"":E.name()));
		else
		if(E==null)
			Log.errOut("Generic","setPropertiesStr: null 'E': "+((E==null)?"":E.name()));
		else
		{
			if(E.isGeneric())
				setGenPropertiesStr(E,V);
			if(fromTop)
				setOrdPropertiesStr(E,V);
			recoverEnvironmental(E);
		}
	}

	private static void recoverEnvironmental(Environmental E)
	{
		if(E==null) return;
		E.recoverEnvStats();
		if(E instanceof MOB)
		{
			((MOB)E).recoverCharStats();
			((MOB)E).recoverMaxState();
			((MOB)E).resetToMaxState();
		}
	}

	private static void setPropertiesStr(Environmental E, Vector V, boolean fromTop)
	{
		if(E==null)
			Log.errOut("Generic","setPropertiesStr2: null 'E'");
		else
		{
			if(E.isGeneric())
				setGenPropertiesStr(E,V);
			if(fromTop)
				setOrdPropertiesStr(E,V);
			recoverEnvironmental(E);
		}
	}

	private static void setOrdPropertiesStr(Environmental E, Vector V)
	{
		if(V==null)
		{
			Log.errOut("Generic","null XML returned on "+E.ID()+" parse. Load aborted.");
			return;
		}


		if(E instanceof Room)
			setExtraEnvProperties(E,V);
		else
		if(E instanceof Area)
			setExtraEnvProperties(E,V);
		else
		if(E instanceof Ability)
			E.setMiscText(XMLManager.getValFromPieces(V,"AWRAP"));
		else
		if(E instanceof Item)
		{
			Item item=(Item)E;
			item.setUsesRemaining(XMLManager.getIntFromPieces(V,"IUSES"));
			item.baseEnvStats().setLevel(XMLManager.getIntFromPieces(V,"ILEVL"));
			item.baseEnvStats().setAbility(XMLManager.getIntFromPieces(V,"IABLE"));
			if(!E.isGeneric())
				item.setMiscText(XMLManager.getValFromPieces(V,"ITEXT"));
			item.wearAt(XMLManager.getIntFromPieces(V,"USES"));
		}
		else
		if(E instanceof MOB)
		{
			E.baseEnvStats().setLevel(XMLManager.getIntFromPieces(V,"MLEVL"));
			E.baseEnvStats().setAbility(XMLManager.getIntFromPieces(V,"MABLE"));
			E.baseEnvStats().setAbility(XMLManager.getIntFromPieces(V,"MREJUV"));
			if(!E.isGeneric())
				E.setMiscText(XMLManager.getValFromPieces(V,"MTEXT"));
		}
	}

	private static void setGenPropertiesStr(Environmental E, Vector buf)
	{
		if(buf==null)
		{
			Log.errOut("Generic","null XML returned on "+E.ID()+" parse.  Load aborted.");
			return;
		}

		if(E instanceof MOB)
		{
			while(((MOB)E).numAbilities()>0)
			{
				Ability A=((MOB)E).fetchAbility(0);
				if(A!=null)
					((MOB)E).delAbility(A);
			}
			while(((MOB)E).inventorySize()>0)
			{
				Item I=((MOB)E).fetchInventory(0);
				if(I!=null)
					((MOB)E).delInventory(I);
			}
			if(E instanceof ShopKeeper)
			{
				Vector V=((ShopKeeper)E).getUniqueStoreInventory();
				for(int b=0;b<V.size();b++)
					((ShopKeeper)E).delStoreInventory(((Environmental)V.elementAt(b)));
			}
		}
		while(E.numAffects()>0)
		{
			Ability aff=E.fetchAffect(0);
			if(aff!=null)
				E.delAffect(aff);
		}
		while(E.numBehaviors()>0)
		{
			Behavior behav=E.fetchBehavior(0);
			if(behav!=null)
				E.delBehavior(behav);
		}

		setEnvProperties(E,buf);

		setEnvFlags(E,Util.s_int(XMLManager.getValFromPieces(buf,"FLAG")));

		if(E instanceof Exit)
		{
			Exit exit=(Exit)E;
			String closedText=XMLManager.getValFromPieces(buf,"CLOSTX");
			String doorName=XMLManager.getValFromPieces(buf,"DOORNM");
			String openName=XMLManager.getValFromPieces(buf,"OPENNM");
			String closeName=XMLManager.getValFromPieces(buf,"CLOSNM");
			exit.setExitParams(doorName,closeName,openName,closedText);
			exit.setKeyName(XMLManager.getValFromPieces(buf,"KEYNM"));
			exit.setOpenDelayTicks(XMLManager.getIntFromPieces(buf,"OPENTK"));
		}

		if(E instanceof Item)
		{
			Item item=(Item)E;
			item.setSecretIdentity(XMLManager.getValFromPieces(buf,"IDENT"));
			item.setBaseValue(XMLManager.getIntFromPieces(buf,"VALUE"));
			item.setMaterial(XMLManager.getIntFromPieces(buf,"MTRAL"));
			//item.setUsesRemaining(Util.s_int(XMLManager.returnXMLValue(buf,"USES")));
			if(item instanceof Container)
			{
				((Container)item).setCapacity(XMLManager.getIntFromPieces(buf,"CAPA"));
				((Container)item).setContainTypes(XMLManager.getLongFromPieces(buf,"CONT"));
				
			}
			if(item instanceof Weapon)
				((Weapon)item).setAmmoCapacity(XMLManager.getIntFromPieces(buf,"CAPA"));
			item.setRawLogicalAnd(XMLManager.getBoolFromPieces(buf,"WORNL"));
			item.setRawProperLocationBitmap(XMLManager.getIntFromPieces(buf,"WORNB"));
			item.setReadableText(XMLManager.getValFromPieces(buf,"READ"));

		}
		
		if(E instanceof Rideable)
		{
			((Rideable)E).setRideBasis(XMLManager.getIntFromPieces(buf,"RIDET"));
			((Rideable)E).setMobCapacity(XMLManager.getIntFromPieces(buf,"RIDEC"));
		}
		
		if(E instanceof Food)
			((Food)E).setNourishment(XMLManager.getIntFromPieces(buf,"CAPA2"));

		if(E instanceof Drink)
		{
			((Drink)E).setLiquidHeld(XMLManager.getIntFromPieces(buf,"CAPA2"));
			((Drink)E).setLiquidRemaining(XMLManager.getIntFromPieces(buf,"CAPA2"));
			((Drink)E).setThirstQuenched(XMLManager.getIntFromPieces(buf,"DRINK"));
		}
		if(E instanceof Weapon)
		{
			((Weapon)E).setWeaponType(XMLManager.getIntFromPieces(buf,"TYPE"));
			((Weapon)E).setWeaponClassification(XMLManager.getIntFromPieces(buf,"CLASS"));
			((Weapon)E).setRanges(XMLManager.getIntFromPieces(buf,"MINR"),XMLManager.getIntFromPieces(buf,"MAXR"));
		}
		if(E instanceof MOB)
		{
			MOB mob=(MOB)E;
			mob.setAlignment(XMLManager.getIntFromPieces(buf,"ALIG"));
			mob.setMoney(XMLManager.getIntFromPieces(buf,"MONEY"));
			mob.baseCharStats().setStat(CharStats.GENDER,(int)(char)XMLManager.getValFromPieces(buf,"GENDER").charAt(0));
			
			String raceID=XMLManager.getValFromPieces(buf,"MRACE");
			if((raceID.length()>0)&&(CMClass.getRace(raceID)!=null))
			{
				Race R=CMClass.getRace(raceID);
				if(R!=null)
				{
					mob.baseCharStats().setMyRace(R);
					R.startRacing(mob,true);
				}
			}
			
			Vector V=XMLManager.getRealContentsFromPieces(buf,"INVEN");
			if(V==null)
			{
				Log.errOut("Generic","Error parsing 'INVEN' of "+E.ID()+".  Load aborted");
				return;
			}
			else
			{
				Hashtable IIDmap=new Hashtable();
				Hashtable LOCmap=new Hashtable();
				for(int i=0;i<V.size();i++)
				{
					XMLManager.XMLpiece iblk=(XMLManager.XMLpiece)V.elementAt(i);
					if((!iblk.tag.equalsIgnoreCase("ITEM"))||(iblk.contents==null))
					{
						Log.errOut("Generic","Error parsing 'ITEM' of "+E.ID()+".  Load aborted");
						return;
					}
					Item newOne=CMClass.getItem(XMLManager.getValFromPieces(iblk.contents,"ICLASS"));
					Vector idat=XMLManager.getRealContentsFromPieces(iblk.contents,"IDATA");
					if((idat==null)||(newOne==null))
					{
						Log.errOut("Generic","Error parsing 'ITEM DATA' of "+CMClass.className(newOne)+".  Load aborted");
						return;
					}
					int wornCode=XMLManager.getIntFromPieces(idat,"IWORN");
					if((newOne instanceof Container)&&(((Container)newOne).capacity()>0))
						IIDmap.put(XMLManager.getValFromPieces(idat,"IID"),newOne);
					String ILOC=XMLManager.getValFromPieces(idat,"ILOC");
					mob.addInventory(newOne);
					if(ILOC.length()>0)
						LOCmap.put(newOne,ILOC);
					setPropertiesStr(newOne,idat,true);
					newOne.wearAt(wornCode);
				}
				for(int i=0;i<mob.inventorySize();i++)
				{
					Item item=mob.fetchInventory(i);
					if(item!=null)
					{
						String ILOC=(String)LOCmap.get(item);
						if(ILOC!=null)
							item.setContainer((Item)IIDmap.get(ILOC));
					}
				}
			}


			V=XMLManager.getRealContentsFromPieces(buf,"ABLTYS");
			if(V==null)
			{
				Log.errOut("Generic","Error parsing 'ABLTYS' of "+E.ID()+".  Load aborted");
				return;
			}
			else
			{
				for(int i=0;i<V.size();i++)
				{
					XMLManager.XMLpiece ablk=(XMLManager.XMLpiece)V.elementAt(i);
					if((!ablk.tag.equalsIgnoreCase("ABLTY"))||(ablk.contents==null))
					{
						Log.errOut("Generic","Error parsing 'ABLTY' of "+E.ID()+".  Load aborted");
						return;
					}
					Ability newOne=CMClass.getAbility(XMLManager.getValFromPieces(ablk.contents,"ACLASS"));
					Vector adat=XMLManager.getRealContentsFromPieces(ablk.contents,"ADATA");
					if((adat==null)||(newOne==null))
					{
						Log.errOut("Generic","Error parsing 'ABLTY DATA' of "+CMClass.className(newOne)+".  Load aborted");
						return;
					}
					newOne.setProfficiency(100);
					setPropertiesStr(newOne,adat,true);
					if(((MOB)E).fetchAbility(newOne.ID())==null)
					{
						((MOB)E).addAbility(newOne);
						newOne.autoInvocation(((MOB)E));
					}
				}
			}

			if(E instanceof Banker)
			{
				((Banker)E).setBankChain(XMLManager.getValFromPieces(buf,"BANK"));
				((Banker)E).setCoinInterest(XMLManager.getDoubleFromPieces(buf,"COININT"));
				((Banker)E).setCoinInterest(XMLManager.getDoubleFromPieces(buf,"ITEMINT"));
			}
			
			if(E instanceof ShopKeeper)
			{
				ShopKeeper shopmob=(ShopKeeper)E;
				shopmob.setWhatIsSold(XMLManager.getIntFromPieces(buf,"SELLCD"));

				V=XMLManager.getRealContentsFromPieces(buf,"STORE");
				if(V==null)
				{
					Log.errOut("Generic","Error parsing 'STORE' of "+E.ID()+".  Load aborted");
					return;
				}
				else
				{
					Hashtable IIDmap=new Hashtable();
					Hashtable LOCmap=new Hashtable();
					for(int i=0;i<V.size();i++)
					{
						XMLManager.XMLpiece iblk=(XMLManager.XMLpiece)V.elementAt(i);
						if((!iblk.tag.equalsIgnoreCase("SHITEM"))||(iblk.contents==null))
						{
							Log.errOut("Generic","Error parsing 'SHITEM' of "+E.ID()+".  Load aborted");
							return;
						}
						String itemi=XMLManager.getValFromPieces(iblk.contents,"SICLASS");
						int numStock=XMLManager.getIntFromPieces(iblk.contents,"SISTOCK");
						Environmental newOne=CMClass.getUnknown(itemi);
						Vector idat=XMLManager.getRealContentsFromPieces(iblk.contents,"SIDATA");
						if((idat==null)||(newOne==null))
						{
							Log.errOut("Generic","Error parsing 'SHOP DATA' of "+CMClass.className(newOne)+".  Load aborted");
							return;
						}
						if(newOne instanceof Item)
						{
							if(newOne instanceof Container)
								IIDmap.put(XMLManager.getValFromPieces(idat,"IID"),newOne);
							String ILOC=XMLManager.getValFromPieces(idat,"ILOC");
							if(ILOC.length()>0)
								LOCmap.put(ILOC,newOne);
						}
						setPropertiesStr(newOne,idat,true);
						shopmob.addStoreInventory(newOne,numStock);
					}
					for(int i=0;i<shopmob.getUniqueStoreInventory().size();i++)
					{
						Environmental stE=(Environmental)shopmob.getUniqueStoreInventory().elementAt(i);
						if(stE instanceof Item)
						{
							Item item=(Item)stE;
							String ILOC=(String)LOCmap.get(item);
							if(ILOC!=null)
								item.setContainer((Item)IIDmap.get(ILOC));
						}
					}
				}
			}
		}
	}

	private static String getExtraEnvPropertiesStr(Environmental E)
	{
		StringBuffer text=new StringBuffer("");
		StringBuffer behaviorstr=new StringBuffer("");
		for(int b=0;b<E.numBehaviors();b++)
		{
			Behavior B=E.fetchBehavior(b);
			if(B!=null)
			{
				behaviorstr.append("<BHAVE>");
				behaviorstr.append(XMLManager.convertXMLtoTag("BCLASS",CMClass.className(B)));
				behaviorstr.append(XMLManager.convertXMLtoTag("BPARMS",B.getParms()));
				behaviorstr.append("</BHAVE>");
			}
		}
		text.append(XMLManager.convertXMLtoTag("BEHAVES",behaviorstr.toString()));

		StringBuffer affectstr=new StringBuffer("");
		for(int a=0;a<E.numAffects();a++)
		{
			Ability A=E.fetchAffect(a);
			if((A!=null)&&(!A.isBorrowed(E)))
			{
				affectstr.append("<AFF>");
				affectstr.append(XMLManager.convertXMLtoTag("ACLASS",CMClass.className(A)));
				affectstr.append(XMLManager.convertXMLtoTag("ATEXT",A.text()));
				affectstr.append("</AFF>");
			}
		}
		text.append(XMLManager.convertXMLtoTag("AFFECS",affectstr.toString()));
		return text.toString();
	}

	private static String getEnvPropertiesStr(Environmental E)
	{
		StringBuffer text=new StringBuffer("");
		text.append(XMLManager.convertXMLtoTag("NAME",E.name()));
		text.append(XMLManager.convertXMLtoTag("DESC",E.description()));
		text.append(XMLManager.convertXMLtoTag("DISP",E.displayText()));
		text.append(XMLManager.convertXMLtoTag("PROP",
			E.baseEnvStats().ability()+"|"+
			E.baseEnvStats().armor()+"|"+
			E.baseEnvStats().attackAdjustment()+"|"+
			E.baseEnvStats().damage()+"|"+
			E.baseEnvStats().disposition()+"|"+
			E.baseEnvStats().level()+"|"+
			E.baseEnvStats().rejuv()+"|"+
			E.baseEnvStats().speed()+"|"+
			E.baseEnvStats().weight()+"|"+
			E.baseEnvStats().height()+"|"+
			E.baseEnvStats().sensesMask()+"|"));

		text.append(getExtraEnvPropertiesStr(E));
		return text.toString();
	}

	private static void setEnvProperties(Environmental E, Vector buf)
	{
		E.setName(XMLManager.getValFromPieces(buf,"NAME"));
		E.setDescription(XMLManager.getValFromPieces(buf,"DESC"));
		E.setDisplayText(XMLManager.getValFromPieces(buf,"DISP"));
		String props=XMLManager.getValFromPieces(buf,"PROP");
		double[] nums=new double[11];
		int x=0;
		for(int y=props.indexOf("|");y>=0;y=props.indexOf("|"))
		{
			try
			{
				nums[x]=Double.valueOf(props.substring(0,y)).doubleValue();
			}
			catch(Exception e)
			{
				nums[x]=new Integer(Util.s_int(props.substring(0,y))).doubleValue();
			}
			x++;
			props=props.substring(y+1);
		}
		E.baseEnvStats().setAbility((int)Math.round(nums[0]));
		E.baseEnvStats().setArmor((int)Math.round(nums[1]));
		E.baseEnvStats().setAttackAdjustment((int)Math.round(nums[2]));
		E.baseEnvStats().setDamage((int)Math.round(nums[3]));
		E.baseEnvStats().setDisposition((int)Math.round(nums[4]));
		E.baseEnvStats().setLevel((int)Math.round(nums[5]));
		E.baseEnvStats().setRejuv((int)Math.round(nums[6]));
		E.baseEnvStats().setSpeed(nums[7]);
		E.baseEnvStats().setWeight((int)Math.round(nums[8]));
		E.baseEnvStats().setHeight((int)Math.round(nums[9]));
		E.baseEnvStats().setSensesMask((int)Math.round(nums[10]));

		setExtraEnvProperties(E,buf);
	}

	private static void setExtraEnvProperties(Environmental E, Vector buf)
	{

		Vector V=XMLManager.getRealContentsFromPieces(buf,"BEHAVES");
		if(V==null)
		{
			Log.errOut("Generic","Error parsing 'BEHAVES' of "+E.ID()+".  Load aborted");
			return;
		}
		else
		{
			for(int i=0;i<V.size();i++)
			{
				XMLManager.XMLpiece ablk=(XMLManager.XMLpiece)V.elementAt(i);
				if((!ablk.tag.equalsIgnoreCase("BHAVE"))||(ablk.contents==null))
				{
					Log.errOut("Generic","Error parsing 'BHAVE' of "+E.ID()+".  Load aborted");
					return;
				}
				Behavior newOne=CMClass.getBehavior(XMLManager.getValFromPieces(ablk.contents,"BCLASS"));
				String bparms=XMLManager.getValFromPieces(ablk.contents,"BPARMS");
				if(newOne==null)
				{
					Log.errOut("Generic","Error parsing 'BHAVE DATA' of "+CMClass.className(newOne)+".  Load aborted");
					return;
				}
				newOne.setParms(bparms);
				E.addBehavior(newOne);
			}
		}

		V=XMLManager.getRealContentsFromPieces(buf,"AFFECS");
		if(V==null)
		{
			Log.errOut("Generic","Error parsing 'AFFECS' of "+E.ID()+".  Load aborted");
			return;
		}
		else
		{
			for(int i=0;i<V.size();i++)
			{
				XMLManager.XMLpiece ablk=(XMLManager.XMLpiece)V.elementAt(i);
				if((!ablk.tag.equalsIgnoreCase("AFF"))||(ablk.contents==null))
				{
					Log.errOut("Generic","Error parsing 'AFF' of "+E.ID()+".  Load aborted");
					return;
				}
				Ability newOne=CMClass.getAbility(XMLManager.getValFromPieces(ablk.contents,"ACLASS"));
				String aparms=XMLManager.getValFromPieces(ablk.contents,"ATEXT");
				if(newOne==null)
				{
					Log.errOut("Generic","Error parsing 'AFF DATA' of "+CMClass.className(newOne)+".  Load aborted");
					return;
				}
				newOne.setMiscText(aparms);
				E.addNonUninvokableAffect(newOne);
			}
		}
	}

}
