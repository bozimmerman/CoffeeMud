package com.planet_ink.coffee_mud.utils;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;
import java.io.*;

public class CoffeeMaker
{
	private CoffeeMaker(){}

	public static boolean get(int x, int m)
	{
		return (x&m)==m;
	}

	public static void resetGenMOB(MOB mob, String newText)
	{
		if((newText!=null)&&((newText.length()>10)||newText.startsWith("%DBID>")))
		{
			if(newText.startsWith("%DBID>"))
			{
				String dbstr=CMClass.DBEngine().DBReadRoomMOBData(newText.substring(6,newText.indexOf("@")),
																  ((Object)mob).getClass().getName()+newText.substring(newText.indexOf("@")).trim());
				if(dbstr!=null)
					setPropertiesStr(mob,dbstr,false);
				else
					Log.errOut("Unable to re-read mob data: "+newText);
			}
			else
			{
				setPropertiesStr(mob,newText,false);
			}
		}
		mob.recoverEnvStats();
		mob.recoverCharStats();
		mob.baseState().setHitPoints(Dice.rollHP(mob.baseEnvStats().level(),mob.baseEnvStats().ability()));
		mob.baseState().setMana(mob.baseCharStats().getCurrentClass().getLevelMana(mob));
		mob.baseState().setMovement(mob.baseCharStats().getCurrentClass().getLevelMove(mob));
		mob.recoverMaxState();
		mob.resetToMaxState();
		if(mob.getWimpHitPoint()>0)
			mob.setWimpHitPoint((int)Math.round(Util.mul(mob.curState().getHitPoints(),.10)));
		mob.setExperience(mob.charStats().getCurrentClass().getLevelExperience(mob.envStats().level()));
		mob.setExpNextLevel(mob.charStats().getCurrentClass().getLevelExperience(mob.envStats().level()+1));
	}

	public static String parseOutAngleBrackets(String s)
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
	public static String restoreAngleBrackets(String s)
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
					if(buf.substring(loop+1,loop+4).equalsIgnoreCase("lt;"))
						buf.replace(loop,loop+4,"<");
					else
					if(buf.substring(loop+1,loop+4).equalsIgnoreCase("gt;"))
						buf.replace(loop,loop+4,">");
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
			if(!Util.bset(item.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNODROP))
				f=f|1;
			if(!Util.bset(item.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNOTGET))
				f=f|2;
			if(Util.bset(item.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMREADABLE))
				f=f|4;
			if(!Util.bset(item.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNOREMOVE))
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
			// deprecated, but unfortunately, its here to stay.
			Sense.setDroppable(item,get(f,1));
			Sense.setGettable(item,get(f,2));
			Sense.setReadable(item,get(f,4));
			Sense.setRemovable(item,get(f,8));
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
			if(get(f,16)) Log.errOut("CoffeeMaker","Exit has deprecated trap flag set!");
			boolean HasDoor=get(f,32);
			boolean HasLock=get(f,64);
			boolean DefaultsClosed=get(f,128);
			boolean DefaultsLocked=get(f,256);
			if(get(f,512)) Log.errOut("CoffeeMaker","Exit has deprecated level restriction flag set!");
			if(get(f,1024)) Log.errOut("CoffeeMaker","Exit has deprecated class restriction flag set!");
			if(get(f,2048)) Log.errOut("CoffeeMaker","Exit has deprecated alignment restriction flag set!");
			exit.setDoorsNLocks(HasDoor,(!HasDoor)||(!DefaultsClosed),DefaultsClosed,HasLock,HasLock&&DefaultsLocked,DefaultsLocked);
		}
	}

	public static String getPropertiesStr(Environmental E, boolean fromTop)
	{
		if(E==null)
		{
			Log.errOut("CoffeeMaker","getPropertiesStr: null 'E'");
			return "";
		}
		else
			return (E.isGeneric()?getGenPropertiesStr(E):"") + (fromTop?getOrdPropertiesStr(E):"");
	}

	private static String getOrdPropertiesStr(Environmental E)
	{
		if(E instanceof Room)
		{
			if(E instanceof GridLocale)
				return XMLManager.convertXMLtoTag("XGRID",((GridLocale)E).xSize())
					  +XMLManager.convertXMLtoTag("YGRID",((GridLocale)E).ySize())
					  +getExtraEnvPropertiesStr(E);
			else
				return getExtraEnvPropertiesStr(E);
		}
		else
		if(E instanceof Area)
		{
		    StringBuffer str = new StringBuffer();
		    StringBuffer parentstr = new StringBuffer();
		    StringBuffer childrenstr = new StringBuffer();
		    str.append(XMLManager.convertXMLtoTag("ARCHP", ( (Area) E).getArchivePath()));
		    for(Enumeration e=((Area)E).getParents(); e.hasMoreElements();)
			{
		        Area A=(Area)e.nextElement();
		        parentstr.append("<PARENT>");
		        parentstr.append(XMLManager.convertXMLtoTag("PARENTNAMED", A.name()));
		        parentstr.append("</PARENT>");
		    }
		    str.append(XMLManager.convertXMLtoTag("PARENTS",parentstr.toString()));
		    for(Enumeration e=((Area)E).getChildren(); e.hasMoreElements();)
			{
		        Area A=(Area)e.nextElement();
		        childrenstr.append("<CHILD>");
		        childrenstr.append(XMLManager.convertXMLtoTag("CHILDNAMED", A.name()));
		        childrenstr.append("</CHILD>");
		    }
		    str.append(XMLManager.convertXMLtoTag("CHILDREN",childrenstr.toString()));
		    str.append(getExtraEnvPropertiesStr(E));
			str.append(XMLManager.convertXMLtoTag("AUTHOR",((Area)E).getAuthorID()));
		    return str.toString();
		}
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
				+XMLManager.convertXMLtoTag("MREJV",""+E.baseEnvStats().rejuv())
				+((E.isGeneric()?"":XMLManager.convertXMLtoTag("ITEXT",""+E.text())));
			return xml;
		}
		return "";
	}

	private static String getGenMobAbilities(MOB M)
	{
		StringBuffer abilitystr=new StringBuffer("");
		for(int b=0;b<M.numLearnedAbilities();b++)
		{
			Ability A=M.fetchAbility(b);
			if((A!=null)&&(!A.isBorrowed(M)))
			{
				abilitystr.append("<ABLTY>");
				abilitystr.append(XMLManager.convertXMLtoTag("ACLASS",CMClass.className(A)));
				abilitystr.append(XMLManager.convertXMLtoTag("APROF",""+A.profficiency()));
				abilitystr.append(XMLManager.convertXMLtoTag("ADATA",getPropertiesStr(A,true)));
				abilitystr.append("</ABLTY>");
			}
		}
		return (XMLManager.convertXMLtoTag("ABLTYS",abilitystr.toString()));
	}

	private static String getGenMobInventory(MOB M)
	{
		StringBuffer itemstr=new StringBuffer("");
		for(int b=0;b<M.inventorySize();b++)
		{
			Item I=M.fetchInventory(b);
			if(I!=null)
			{
				itemstr.append("<ITEM>");
				itemstr.append(XMLManager.convertXMLtoTag("ICLASS",CMClass.className(I)));
				itemstr.append(XMLManager.convertXMLtoTag("IDATA",getPropertiesStr(I,true)));
				itemstr.append("</ITEM>");
			}
		}
		return (XMLManager.convertXMLtoTag("INVEN",itemstr.toString()));
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

		if(E instanceof ClanItem)
		{
			text.append(XMLManager.convertXMLtoTag("CLANID",""+((ClanItem)E).clanID()));
			text.append(XMLManager.convertXMLtoTag("CITYPE",""+((ClanItem)E).ciType()));
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

		if(E instanceof Light)
			text.append(XMLManager.convertXMLtoTag("BURNOUT",((Light)E).destroyedWhenBurnedOut()));
		
		if(E instanceof Rideable)
		{
			text.append(XMLManager.convertXMLtoTag("RIDET",((Rideable)E).rideBasis()));
			text.append(XMLManager.convertXMLtoTag("RIDEC",((Rideable)E).riderCapacity()));
		}
		
		if(E instanceof EnvResource)
			text.append(XMLManager.convertXMLtoTag("DOMN",((EnvResource)E).domainSource()+""));

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

		if(E instanceof LandTitle)
			text.append(XMLManager.convertXMLtoTag("LANDID",((LandTitle)E).landPropertyID()));

		if(E instanceof Perfume)
			text.append(XMLManager.convertXMLtoTag("SMELLLST",((Perfume)E).getSmellList()));
		
		if(E instanceof DeadBody)
		{
			if(((DeadBody)E).charStats()!=null)
			{
				text.append(XMLManager.convertXMLtoTag("GENDER",""+(char)((DeadBody)E).charStats().getStat(CharStats.GENDER)));
				text.append(XMLManager.convertXMLtoTag("MRACE",""+((DeadBody)E).charStats().getMyRace().ID()));
				text.append(XMLManager.convertXMLtoTag("MDNAME",""+((DeadBody)E).mobName()));
				text.append(XMLManager.convertXMLtoTag("MDDESC",""+((DeadBody)E).mobDescription()));
				text.append(XMLManager.convertXMLtoTag("MKNAME",""+((DeadBody)E).killerName()));
				text.append(XMLManager.convertXMLtoTag("MKPLAY",""+((DeadBody)E).killerPlayer()));
				text.append(XMLManager.convertXMLtoTag("MDLMSG",""+((DeadBody)E).lastMessage()));
				text.append(XMLManager.convertXMLtoTag("MBREAL",""+((DeadBody)E).destroyAfterLooting()));
				text.append(XMLManager.convertXMLtoTag("MPLAYR",""+((DeadBody)E).playerCorpse()));
				text.append(XMLManager.convertXMLtoTag("MPKILL",""+((DeadBody)E).mobPKFlag()));
				if(((DeadBody)E).killingTool()==null) text.append("<KLTOOL />");
				else
				{
					text.append("<KLTOOL>");
					text.append(XMLManager.convertXMLtoTag("KLCLASS",CMClass.className(((DeadBody)E).killingTool())));
					text.append(XMLManager.convertXMLtoTag("KLDATA",getPropertiesStr(((DeadBody)E).killingTool(),true)));
					text.append("</KLTOOL>");
				}
			}
			else
			{
				text.append(XMLManager.convertXMLtoTag("GENDER","M"));
				text.append(XMLManager.convertXMLtoTag("MRACE","Human"));
				text.append(XMLManager.convertXMLtoTag("MPLAYR","false"));
			}
		}

		if(E instanceof MOB)
		{
			text.append(XMLManager.convertXMLtoTag("ALIG",((MOB)E).getAlignment()));
			text.append(XMLManager.convertXMLtoTag("MONEY",((MOB)E).getMoney()));
			text.append(XMLManager.convertXMLtoTag("CLAN",((MOB)E).getClanID()));
			text.append(XMLManager.convertXMLtoTag("GENDER",""+(char)((MOB)E).baseCharStats().getStat(CharStats.GENDER)));
			text.append(XMLManager.convertXMLtoTag("MRACE",""+((MOB)E).baseCharStats().getMyRace().ID()));
			text.append(getGenMobInventory((MOB)E));
			text.append(getGenMobAbilities((MOB)E));

			if(E instanceof Banker)
			{
				text.append(XMLManager.convertXMLtoTag("BANK",""+((Banker)E).bankChain()));
				text.append(XMLManager.convertXMLtoTag("COININT",""+((Banker)E).getCoinInterest()));
				text.append(XMLManager.convertXMLtoTag("ITEMINT",""+((Banker)E).getCoinInterest()));
			}
			if(E instanceof Deity)
			{
				text.append(XMLManager.convertXMLtoTag("CLEREQ",((Deity)E).getClericRequirements()));
				text.append(XMLManager.convertXMLtoTag("WORREQ",((Deity)E).getWorshipRequirements()));
				text.append(XMLManager.convertXMLtoTag("CLERIT",((Deity)E).getClericRitual()));
				text.append(XMLManager.convertXMLtoTag("WORRIT",((Deity)E).getWorshipRitual()));
				text.append(XMLManager.convertXMLtoTag("CLERSIT",((Deity)E).getClericSin()));
				text.append(XMLManager.convertXMLtoTag("WORRSIT",((Deity)E).getWorshipSin()));
				text.append(XMLManager.convertXMLtoTag("CLERPOW",((Deity)E).getClericPowerup()));

				StringBuffer itemstr=new StringBuffer("");
				for(int b=0;b<((Deity)E).numBlessings();b++)
				{
					Ability A=((Deity)E).fetchBlessing(b);
					if(A==null) continue;
					itemstr.append("<BLESS>");
					itemstr.append(XMLManager.convertXMLtoTag("BLCLASS",CMClass.className(A)));
					itemstr.append(XMLManager.convertXMLtoTag("BLDATA",getPropertiesStr(A,true)));
					itemstr.append("</BLESS>");
				}
				text.append(XMLManager.convertXMLtoTag("BLESSINGS",itemstr.toString()));

				itemstr=new StringBuffer("");
				for(int b=0;b<((Deity)E).numCurses();b++)
				{
					Ability A=((Deity)E).fetchCurse(b);
					if(A==null) continue;
					itemstr.append("<CURSE>");
					itemstr.append(XMLManager.convertXMLtoTag("CUCLASS",CMClass.className(A)));
					itemstr.append(XMLManager.convertXMLtoTag("CUDATA",getPropertiesStr(A,true)));
					itemstr.append("</CURSE>");
				}
				text.append(XMLManager.convertXMLtoTag("CURSES",itemstr.toString()));

				itemstr=new StringBuffer("");
				for(int b=0;b<((Deity)E).numPowers();b++)
				{
					Ability A=((Deity)E).fetchPower(b);
					if(A==null) continue;
					itemstr.append("<POWER>");
					itemstr.append(XMLManager.convertXMLtoTag("POCLASS",CMClass.className(A)));
					itemstr.append(XMLManager.convertXMLtoTag("PODATA",getPropertiesStr(A,true)));
					itemstr.append("</POWER>");
				}
				text.append(XMLManager.convertXMLtoTag("POWERS",itemstr.toString()));
			}
			if(E instanceof ShopKeeper)
			{
				text.append(XMLManager.convertXMLtoTag("SELLCD",((ShopKeeper)E).whatIsSold()));
				text.append(XMLManager.convertXMLtoTag("PREJFC",((ShopKeeper)E).prejudiceFactors()));

				Vector V=((ShopKeeper)E).getUniqueStoreInventory();
				StringBuffer itemstr=new StringBuffer("");
				for(int b=0;b<V.size();b++)
				{
					Environmental Env=(Environmental)V.elementAt(b);
					itemstr.append("<SHITEM>");
					itemstr.append(XMLManager.convertXMLtoTag("SICLASS",CMClass.className(Env)));
					itemstr.append(XMLManager.convertXMLtoTag("SISTOCK",((ShopKeeper)E).numberInStock(Env)));
					itemstr.append(XMLManager.convertXMLtoTag("SIPRICE",((ShopKeeper)E).stockPrice(Env)));
					itemstr.append(XMLManager.convertXMLtoTag("SIDATA",getPropertiesStr(Env,true)));
					itemstr.append("</SHITEM>");
				}
				text.append(XMLManager.convertXMLtoTag("STORE",itemstr.toString()));
			}
			if(((MOB)E).numTattoos()>0)
			{
				text.append("<TATTS>");
				for(int i=0;i<((MOB)E).numTattoos();i++)
					text.append(((MOB)E).fetchTattoo(i)+";");
				text.append("</TATTS>");
			}
			if(((MOB)E).numEducations()>0)
			{
				text.append("<EDUS>");
				for(int i=0;i<((MOB)E).numEducations();i++)
					text.append(((MOB)E).fetchEducation(i)+";");
				text.append("</EDUS>");
			}
		}
		return text.toString();
	}

	public static String unpackErr(String where, String msg)
	{
		Log.errOut("CoffeeMaker","unpack"+where+"FromXML: "+msg);
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
		newRoom.setRoomID(XMLManager.getValFromPieces(xml,"ROOMID"));
		if(newRoom.roomID().equals("NEW")) newRoom.setRoomID(CMMap.getOpenRoomID(myArea.Name()));
		if(CMMap.getRoom(newRoom.roomID())!=null) return "Room Exists: "+newRoom.roomID();
		newRoom.setArea(myArea);
		CMClass.DBEngine().DBCreateRoom(newRoom,roomClass);
		CMMap.addRoom(newRoom);
		newRoom.setDisplayText(XMLManager.getValFromPieces(xml,"RDISP"));
		newRoom.setDescription(XMLManager.getValFromPieces(xml,"RDESC"));
		newRoom.setMiscText(restoreAngleBrackets(XMLManager.getValFromPieces(xml,"RTEXT")));

		// now EXITS!
		Vector xV=XMLManager.getRealContentsFromPieces(xml,"ROOMEXITS");
		if(xV==null) return unpackErr("Room","null 'xV'"+" in room "+newRoom.roomID());
		for(int x=0;x<xV.size();x++)
		{
			XMLManager.XMLpiece xblk=(XMLManager.XMLpiece)xV.elementAt(x);
			if((!xblk.tag.equalsIgnoreCase("REXIT"))||(xblk.contents==null))
				return unpackErr("Room","??"+xblk.tag+" in room "+newRoom.roomID());
			int dir=XMLManager.getIntFromPieces(xblk.contents,"XDIRE");
			if((dir<0)||(dir>=Directions.NUM_DIRECTIONS))
				return unpackErr("Room","Unknown direction: "+dir+" in room "+newRoom.roomID());
			String doorID=XMLManager.getValFromPieces(xblk.contents,"XDOOR");
			Vector xxV=XMLManager.getContentsFromPieces(xblk.contents,"XEXIT");
			if(xxV==null) return unpackErr("Room","null 'xxV'"+" in room "+newRoom.roomID());
			Exit exit=null;
			if(xxV.size()>0)
			{
				exit=CMClass.getExit(XMLManager.getValFromPieces(xxV,"EXID"));
				if(xxV==null) return unpackErr("Room","null 'exit'"+" in room "+newRoom.roomID());
				exit.setMiscText(restoreAngleBrackets(XMLManager.getValFromPieces(xxV,"EXDAT")));
				newRoom.rawExits()[dir]=exit;
			}
			else
				exit=CMClass.getExit("GenExit");
			exit.recoverEnvStats();
			if(doorID.length()>0)
			{
				Room link=CMMap.getRoom(doorID);
				if(link!=null)
					newRoom.rawDoors()[dir]=link;
				else
				{
					newRoom.rawExits()[dir]=exit; // get will get the fake one too!
					exit.setTemporaryDoorLink(doorID);
				}
			}
		}

		// find any mis-linked exits and fix them!
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			boolean changed=false;
			for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
			{
				Exit exit=R.rawExits()[d];
				if((exit!=null)&&(exit.temporaryDoorLink().equalsIgnoreCase(newRoom.roomID())))
				{
					exit.setTemporaryDoorLink("");
					R.rawDoors()[d]=newRoom;
					changed=true;
				}
				else
				if((R.rawDoors()[d]!=null)&&(R.rawDoors()[d].roomID().equals(newRoom.roomID())))
				{
					R.rawDoors()[d]=newRoom;
					changed=true;
				}
			}
			if(changed) CMClass.DBEngine().DBUpdateExits(R);
		}
		CMClass.DBEngine().DBUpdateRoom(newRoom);
		CMClass.DBEngine().DBUpdateExits(newRoom);
		if(andContent)
		{
			Hashtable identTable=new Hashtable();

			Vector cV=XMLManager.getRealContentsFromPieces(xml,"ROOMCONTENT");
			if(cV==null) return unpackErr("Room","null 'cV'"+" in room "+newRoom.roomID());
			if(cV.size()>0)
			{
				Hashtable mobRideTable=new Hashtable();
				Vector mV=XMLManager.getRealContentsFromPieces(cV,"ROOMMOBS");
				if(mV!=null) //return unpackErr("Room","null 'mV'"+" in room "+newRoom.roomID());
				for(int m=0;m<mV.size();m++)
				{
					XMLManager.XMLpiece mblk=(XMLManager.XMLpiece)mV.elementAt(m);
					if((!mblk.tag.equalsIgnoreCase("RMOB"))||(mblk.contents==null))
						return unpackErr("Room","bad 'mblk'"+" in room "+newRoom.roomID());
					String mClass=XMLManager.getValFromPieces(mblk.contents,"MCLAS");
					MOB newMOB=CMClass.getMOB(mClass);
					if(newMOB==null) return unpackErr("Room","null 'mClass': "+mClass+" in room "+newRoom.roomID());

					// for rideables AND leaders now!
					String iden=XMLManager.getValFromPieces(mblk.contents,"MIDEN");
					if((iden!=null)&&(iden.length()>0)) identTable.put(iden,newMOB);

					newMOB.setMiscText(restoreAngleBrackets(XMLManager.getValFromPieces(mblk.contents,"MTEXT")));
					newMOB.baseEnvStats().setLevel(XMLManager.getIntFromPieces(mblk.contents,"MLEVL"));
					newMOB.baseEnvStats().setAbility(XMLManager.getIntFromPieces(mblk.contents,"MABLE"));
					newMOB.baseEnvStats().setRejuv(XMLManager.getIntFromPieces(mblk.contents,"MREJV"));
					String ride=XMLManager.getValFromPieces(mblk.contents,"MRIDE");
					if((ride!=null)&&(ride.length()>0))
						mobRideTable.put(newMOB,ride);
					newMOB.setStartRoom(newRoom);
					newMOB.setLocation(newRoom);
					newMOB.recoverCharStats();
					newMOB.recoverEnvStats();
					newMOB.recoverMaxState();
					newMOB.resetToMaxState();
					newMOB.bringToLife(newRoom,true);
				}

				Hashtable itemLocTable=new Hashtable();
				Vector iV=XMLManager.getRealContentsFromPieces(cV,"ROOMITEMS");
				if(iV!=null) //return unpackErr("Room","null 'iV'"+" in room "+newRoom.roomID());
				for(int i=0;i<iV.size();i++)
				{
					XMLManager.XMLpiece iblk=(XMLManager.XMLpiece)iV.elementAt(i);
					if((!iblk.tag.equalsIgnoreCase("RITEM"))||(iblk.contents==null))
						return unpackErr("Room","bad 'iblk'"+" in room "+newRoom.roomID());
					String iClass=XMLManager.getValFromPieces(iblk.contents,"ICLAS");
					Item newItem=CMClass.getItem(iClass);
					if(newItem==null) return unpackErr("Room","null 'iClass': "+iClass+" in room "+newRoom.roomID());
					if((newItem instanceof Container)||(newItem instanceof Rideable))
					{
						String iden=XMLManager.getValFromPieces(iblk.contents,"IIDEN");
						if((iden!=null)&&(iden.length()>0)) identTable.put(iden,newItem);
					}
					String iloc=XMLManager.getValFromPieces(iblk.contents,"ILOCA");
					if(iloc.length()>0) itemLocTable.put(newItem,iloc);
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
					Item childI=(Item)e.nextElement();
					String loc=(String)itemLocTable.get(childI);
					Item parentI=(Item)identTable.get(loc);
					if(parentI!=null)
					{
						childI.setContainer(parentI);
						childI.recoverEnvStats();
						parentI.recoverEnvStats();
					}
				}
				for(Enumeration e=mobRideTable.keys();e.hasMoreElements();)
				{
					MOB M=(MOB)e.nextElement();
					String ride=(String)mobRideTable.get(M);
					if((ride!=null)&&(ride.length()>0))
					{
						Environmental E=(Environmental)identTable.get(ride);
						if(E instanceof Rideable)
							M.setRiding((Rideable)E);
						else
						if(E instanceof MOB)
							M.setFollowing((MOB)E);
					}
				}
			}
		}
		// equivalent to clear debriandrestart
		CMClass.ThreadEngine().clearDebri(newRoom,0);
		CMClass.DBEngine().DBUpdateItems(newRoom);
		newRoom.startItemRejuv();
		CMClass.DBEngine().DBUpdateMOBs(newRoom);
		return "";
	}

	public static String fillAreaAndCustomVectorFromXML(String buf, Vector area, Vector custom)
	{
		Vector xml=XMLManager.parseAllXML(buf);
		if(xml==null) return unpackErr("Fill","null 'xml'");
		String error=fillCustomVectorFromXML(xml,custom);
		if(error.length()>0) return error;
		Vector areaData=XMLManager.getRealContentsFromPieces(xml,"AREA");
		if(areaData==null) return unpackErr("Fill","null 'aV'");
		for(int a=0;a<areaData.size();a++)
			area.addElement(areaData.elementAt(a));
		return "";
	}
	public static String fillCustomVectorFromXML(String xml, Vector custom)
	{
		Vector xmlv=XMLManager.parseAllXML(xml);
		if(xmlv==null) return unpackErr("Custom","null 'xmlv'");
		return fillCustomVectorFromXML(xmlv,custom);
	}
	public static String fillCustomVectorFromXML(Vector xml, Vector custom)
	{
		Vector aV=XMLManager.getRealContentsFromPieces(xml,"CUSTOM");
		if(aV==null) return "";
		for(int r=0;r<aV.size();r++)
		{
			XMLManager.XMLpiece ablk=(XMLManager.XMLpiece)aV.elementAt(r);
			if(ablk.tag.equalsIgnoreCase("RACE"))
			{
				Race R=CMClass.getRace("GenRace");
				if(R!=null)
				{
					R=R.copyOf();
					R.setRacialParms("<RACE>"+ablk.value+"</RACE>");
					if(!R.ID().equals("GenRace"))
						custom.addElement(R);
				}
			}
			else
			if(ablk.tag.equalsIgnoreCase("CCLASS"))
			{
				CharClass C=CMClass.getCharClass("GenCharClass");
				if(C!=null)
				{
					C=C.copyOf();
					C.setClassParms("<CCLASS>"+ablk.value+"</CCLASS>");
					if(!C.ID().equals("GenCharClass"))
						custom.addElement(C);
				}
			}
			else
				return unpackErr("Custom","??"+ablk.tag);
		}
		return "";
	}

	public static String fillAreasVectorFromXML(String buf, Vector areas, Vector custom)
	{
		Vector xml=XMLManager.parseAllXML(buf);
		if(xml==null) return unpackErr("Areas","null 'xml'");
		fillCustomVectorFromXML(xml,custom);
		Vector aV=XMLManager.getRealContentsFromPieces(xml,"AREAS");
		if(aV==null) return unpackErr("Areas","null 'aV'");
		for(int r=0;r<aV.size();r++)
		{
			XMLManager.XMLpiece ablk=(XMLManager.XMLpiece)aV.elementAt(r);
			if((!ablk.tag.equalsIgnoreCase("AREA"))||(ablk.contents==null))
				return unpackErr("Areas","??"+ablk.tag);
			areas.addElement(ablk.contents);
		}
		return "";
	}

	public static String unpackAreaFromXML(Vector aV, Session S, boolean andRooms)
	{
		String areaClass=XMLManager.getValFromPieces(aV,"ACLAS");
		String areaName=XMLManager.getValFromPieces(aV,"ANAME");

		if(CMMap.getArea(areaName)!=null) return "Area Exists: "+areaName;
		if(CMClass.getAreaType(areaClass)==null) return unpackErr("Area","No class: "+areaClass);
		Area newArea=CMClass.DBEngine().DBCreateArea(areaName,areaClass);
		if(newArea==null) return unpackErr("Area","null 'area'");

		newArea.setDescription(Util.safetyFilter(XMLManager.getValFromPieces(aV,"ADESC")));
		newArea.setClimateType(XMLManager.getIntFromPieces(aV,"ACLIM"));
		newArea.setTechLevel(XMLManager.getIntFromPieces(aV,"ATECH"));
		newArea.setSubOpList(XMLManager.getValFromPieces(aV,"ASUBS"));
		newArea.setMiscText(restoreAngleBrackets(XMLManager.getValFromPieces(aV,"ADATA")));
		CMClass.DBEngine().DBUpdateArea(newArea.Name(),newArea);
		if(andRooms)
		{
			Vector rV=XMLManager.getRealContentsFromPieces(aV,"AROOMS");
			if(rV==null) return unpackErr("Area","null 'rV'");
			for(int r=0;r<rV.size();r++)
			{
				XMLManager.XMLpiece ablk=(XMLManager.XMLpiece)rV.elementAt(r);
				if((!ablk.tag.equalsIgnoreCase("AROOM"))||(ablk.contents==null))
					return unpackErr("Area","??"+ablk.tag);
				if(S!=null) S.rawPrint(".");
				String err=unpackRoomFromXML(ablk.contents,true);
				if(err.length()>0) return err;
			}
		}
		return "";
	}
	public static String unpackAreaFromXML(String buf, Session S, boolean andRooms)
	{
		Vector xml=XMLManager.parseAllXML(buf);
		if(xml==null) return unpackErr("Area","null 'xml'");
		Vector aV=XMLManager.getRealContentsFromPieces(xml,"AREA");
		if(aV==null) return unpackErr("Area","null 'aV'");
		return unpackAreaFromXML(aV,S,andRooms);
	}

	public static StringBuffer getAreaXML(Area area, Session S, HashSet custom, boolean andRooms)
	{
		StringBuffer buf=new StringBuffer("");
		if(area==null) return buf;
		boolean mobile=area.getMobility();
		area.toggleMobility(false);
		buf.append("<AREA>");
		buf.append(XMLManager.convertXMLtoTag("ACLAS",area.ID()));
		buf.append(XMLManager.convertXMLtoTag("ANAME",area.Name()));
		buf.append(XMLManager.convertXMLtoTag("ADESC",area.description()));
		buf.append(XMLManager.convertXMLtoTag("ACLIM",area.climateType()));
		buf.append(XMLManager.convertXMLtoTag("ASUBS",area.getSubOpList()));
		buf.append(XMLManager.convertXMLtoTag("ATECH",area.getTechLevel()));
		buf.append(XMLManager.convertXMLtoTag("ADATA",area.text()));
		if(andRooms)
		{
			if(area.mapSize()==0)
				buf.append("<AROOMS />");
			else
			{
				buf.append("<AROOMS>");
				for(Enumeration r=area.getMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					if(S!=null) S.rawPrint(".");
					if((R!=null)&&(R.roomID()!=null)&&(R.roomID().length()>0))
						buf.append(getRoomXML(R,custom,true)+"\n\r");
				}
				buf.append("</AROOMS>");
			}
		}
		buf.append("</AREA>");
		area.toggleMobility(mobile);
		return buf;
	}

	private static StringBuffer logTextDiff(String e1, String e2)
	{
		int start=0;
		int end=e1.length()-1;
		int end2=e2.length()-1;
		boolean stopStart=false;
		boolean stopEnd=false;
		while((!stopStart)||(!stopEnd))
		{
			if(!stopStart)
			{
				if((start>=end)
				 ||(start>=end2)
				 ||(e1.charAt(start)!=e2.charAt(start)))
					stopStart=true;
				else
					start++;
			}

			if(!stopEnd)
			{
				if((end<=start)
				||(end2<=start)
				||(e1.charAt(end)!=e2.charAt(end2)))
					stopEnd=true;
				else
				{
					end--;
					end2--;
				}
			}
		}
		StringBuffer str=new StringBuffer("*1>");
		if(end<start) str.append("");
		else str.append(e1.substring(start,end));
		str.append("\n\r*2>");
		if(end2<start) str.append("");
		else str.append(e2.substring(start,end2));
		return str;
	}

	private static void logDiff(Environmental E1, Environmental E2)
	{
		StringBuffer str=new StringBuffer("Unmatched - "+E1.Name()+"\n\r");
		if(E1 instanceof MOB)
		{
			MOB mob=(MOB)E1;
			MOB dup=(MOB)E2;
			if(!CMClass.className(mob).equals(CMClass.className(dup)))
			   str.append(CMClass.className(mob)+"!="+CMClass.className(dup)+"\n\r");
			if(mob.baseEnvStats().level()!=dup.baseEnvStats().level())
			   str.append("Level- "+mob.baseEnvStats().level()+"!="+dup.baseEnvStats().level()+"\n\r");
			if(mob.baseEnvStats().ability()!=dup.baseEnvStats().ability())
			   str.append("Ability- "+mob.baseEnvStats().ability()+"!="+dup.baseEnvStats().ability()+"\n\r");
			if(!mob.text().equals(dup.text()))
				str.append(logTextDiff(mob.text(),dup.text()));
		}
		else
		if(E1 instanceof Item)
		{
			Item item=(Item)E1;
			Item dup=(Item)E2;
			if(!CMClass.className(item).equals(CMClass.className(dup)))
			   str.append(CMClass.className(item)+"!="+CMClass.className(dup)+"\n\r");
			if(item.baseEnvStats().level()!=dup.baseEnvStats().level())
			   str.append("Level- "+item.baseEnvStats().level()+"!="+dup.baseEnvStats().level()+"\n\r");
			if(item.baseEnvStats().ability()!=dup.baseEnvStats().ability())
			   str.append("Ability- "+item.baseEnvStats().ability()+"!="+dup.baseEnvStats().ability()+"\n\r");
			if(item.usesRemaining()!=dup.usesRemaining())
			   str.append("Uses- "+item.usesRemaining()+"!="+dup.usesRemaining()+"\n\r");
			if(!item.text().equals(dup.text()))
				str.append(logTextDiff(item.text(),dup.text()));
		}
		if(Log.debugChannelOn())
			Log.debugOut("CoffeeMaker",str.toString());
	}

	public static StringBuffer getRoomMobs(Room room, HashSet custom, Hashtable found)
	{
		StringBuffer buf=new StringBuffer("");
		if(room==null) return buf;
		boolean mobile=room.getArea().getMobility();
		room.getArea().toggleMobility(false);
		CoffeeUtensils.resetRoom(room);
		Vector mobs=new Vector();
		for(int i=0;i<room.numInhabitants();i++)
			mobs.addElement(room.fetchInhabitant(i));
		room.getArea().toggleMobility(mobile);
		for(int i=0;i<mobs.size();i++)
		{
			MOB mob=(MOB)mobs.elementAt(i);
			if(mob.isEligibleMonster())
			{
				Vector dups=(Vector)found.get(mob.Name()+mob.displayText());
				if(dups==null)
				{
					dups=new Vector();
					found.put(mob.Name()+mob.displayText(),dups);
					dups.addElement(mob);
				}
				else
				{
					boolean matched=false;
					for(int v=0;v<dups.size();v++)
					{
						MOB dup=(MOB)dups.elementAt(v);
						int oldHeight=mob.baseEnvStats().height();
						int oldWeight=mob.baseEnvStats().weight();
						int oldGender=mob.baseCharStats().getStat(CharStats.GENDER);
						dup.baseEnvStats().setHeight(mob.baseEnvStats().height());
						dup.baseEnvStats().setWeight(mob.baseEnvStats().weight());
						dup.baseCharStats().setStat(CharStats.GENDER,mob.baseCharStats().getStat(CharStats.GENDER));
						if(CMClass.className(mob).equals(CMClass.className(dup))
						&&(mob.baseEnvStats().level()==dup.baseEnvStats().level())
						&&(mob.baseEnvStats().ability()==dup.baseEnvStats().ability())
						&&(mob.text().equals(dup.text())))
							matched=true;
						dup.baseEnvStats().setHeight(oldHeight);
						dup.baseEnvStats().setWeight(oldWeight);
						dup.baseCharStats().setStat(CharStats.GENDER,oldGender);
						if(matched) break;
					}
					if(!matched)
					{
						for(int v=0;v<dups.size();v++)
						{
							MOB dup=(MOB)dups.elementAt(v);
							int oldHeight=mob.baseEnvStats().height();
							int oldWeight=mob.baseEnvStats().weight();
							int oldGender=mob.baseCharStats().getStat(CharStats.GENDER);
							dup.baseEnvStats().setHeight(mob.baseEnvStats().height());
							dup.baseEnvStats().setWeight(mob.baseEnvStats().weight());
							dup.baseCharStats().setStat(CharStats.GENDER,mob.baseCharStats().getStat(CharStats.GENDER));
							if(Log.debugChannelOn()&&CMSecurity.isDebugging("EXPORT"))
								logDiff(mob,dup);
							dup.baseEnvStats().setHeight(oldHeight);
							dup.baseEnvStats().setWeight(oldWeight);
							dup.baseCharStats().setStat(CharStats.GENDER,oldGender);
						}
						dups.addElement(mob);
					}
					else
						continue;
				}
				buf.append("<MOB>");
				buf.append(XMLManager.convertXMLtoTag("MCLAS",CMClass.className(mob)));
				buf.append(XMLManager.convertXMLtoTag("MLEVL",mob.baseEnvStats().level()));
				buf.append(XMLManager.convertXMLtoTag("MABLE",mob.baseEnvStats().ability()));
				buf.append(XMLManager.convertXMLtoTag("MREJV",mob.baseEnvStats().rejuv()));
				buf.append(XMLManager.convertXMLtoTag("MTEXT",parseOutAngleBrackets(mob.text())));
				if((mob.baseCharStats().getMyRace().isGeneric())
				&&(!custom.contains(mob.baseCharStats().getMyRace())))
				   custom.add(mob.baseCharStats().getMyRace());
				buf.append("</MOB>\n\r");
			}
		}
		return buf;
	}

	public static StringBuffer getUniqueItemXML(Item item, int type, Hashtable found)
	{
		StringBuffer buf=new StringBuffer("");
		switch(type)
		{
		case 1: if(!(item instanceof Weapon)) return buf;
				break;
		case 2: if(!(item instanceof Armor)) return buf;
				break;
		}
		if(item.displayText().length()>0)
		{
			Vector dups=(Vector)found.get(item.Name()+item.displayText());
			if(dups==null)
			{
				dups=new Vector();
				found.put(item.Name()+item.displayText(),dups);
				dups.addElement(item);
			}
			else
			{
				for(int v=0;v<dups.size();v++)
				{
					Item dup=(Item)dups.elementAt(v);
					int oldHeight=item.baseEnvStats().height();
					item.baseEnvStats().setHeight(dup.baseEnvStats().height());
					if(CMClass.className(item).equals(CMClass.className(dup))
					&&(item.baseEnvStats().level()==dup.baseEnvStats().level())
					&&(item.usesRemaining()==dup.usesRemaining())
					&&(item.baseEnvStats().ability()==dup.baseEnvStats().ability())
					&&(item.text().equals(dup.text())))
					{
						item.baseEnvStats().setHeight(oldHeight);
						return buf;
					}
					item.baseEnvStats().setHeight(oldHeight);
				}
				for(int v=0;v<dups.size();v++)
				{
					Item dup=(Item)dups.elementAt(v);
					int oldHeight=item.baseEnvStats().height();
					item.baseEnvStats().setHeight(dup.baseEnvStats().height());
					if(Log.debugChannelOn()&&CMSecurity.isDebugging("EXPORT"))
						logDiff(item,dup);
					item.baseEnvStats().setHeight(oldHeight);
				}
				dups.addElement(item);
			}
			buf.append("<ITEM>");
			buf.append(XMLManager.convertXMLtoTag("ICLAS",CMClass.className(item)));
			buf.append(XMLManager.convertXMLtoTag("IUSES",item.usesRemaining()));
			buf.append(XMLManager.convertXMLtoTag("ILEVL",item.baseEnvStats().level()));
			buf.append(XMLManager.convertXMLtoTag("IABLE",item.baseEnvStats().ability()));
			buf.append(XMLManager.convertXMLtoTag("IREJV",item.baseEnvStats().rejuv()));
			buf.append(XMLManager.convertXMLtoTag("ITEXT",parseOutAngleBrackets(item.text())));
			buf.append("</ITEM>\n\r");
		}
		return buf;
	}

	public static String addItemsFromXML(String xmlBuffer,
										 Vector addHere,
										 Session S)
	{
		Vector xml=XMLManager.parseAllXML(xmlBuffer);
		if(xml==null) return unpackErr("Items","null 'xml'");
		Vector iV=XMLManager.getRealContentsFromPieces(xml,"ITEMS");
		if(iV==null) return unpackErr("Items","null 'iV'");
		for(int i=0;i<iV.size();i++)
		{
			XMLManager.XMLpiece iblk=(XMLManager.XMLpiece)iV.elementAt(i);
			if((!iblk.tag.equalsIgnoreCase("ITEM"))||(iblk.contents==null))
				return unpackErr("Items","??"+iblk.tag);
			if(S!=null) S.rawPrint(".");
			String itemClass=XMLManager.getValFromPieces(iblk.contents,"ICLAS");
			Item newItem=CMClass.getItem(itemClass);
			if(newItem==null) return unpackErr("Items","null 'iClass': "+itemClass);
			newItem.baseEnvStats().setLevel(XMLManager.getIntFromPieces(iblk.contents,"ILEVL"));
			newItem.baseEnvStats().setAbility(XMLManager.getIntFromPieces(iblk.contents,"IABLE"));
			newItem.baseEnvStats().setRejuv(XMLManager.getIntFromPieces(iblk.contents,"IREJV"));
			newItem.setUsesRemaining(XMLManager.getIntFromPieces(iblk.contents,"IUSES"));
			newItem.setMiscText(restoreAngleBrackets(XMLManager.getValFromPieces(iblk.contents,"ITEXT")));
			newItem.setContainer(null);
			newItem.recoverEnvStats();
			addHere.addElement(newItem);
		}
		return "";
	}

	public static String addMOBsFromXML(String xmlBuffer,
										Vector addHere,
										Session S)
	{
		Vector xml=XMLManager.parseAllXML(xmlBuffer);
		if(xml==null) return unpackErr("MOBs","null 'xml'");
		Vector mV=XMLManager.getRealContentsFromPieces(xml,"MOBS");
		if(mV==null) return unpackErr("MOBs","null 'mV'");
		for(int m=0;m<mV.size();m++)
		{
			XMLManager.XMLpiece mblk=(XMLManager.XMLpiece)mV.elementAt(m);
			if((!mblk.tag.equalsIgnoreCase("MOB"))||(mblk.contents==null))
				return unpackErr("MOBs","bad 'mblk'");
			String mClass=XMLManager.getValFromPieces(mblk.contents,"MCLAS");
			MOB newMOB=CMClass.getMOB(mClass);
			if(newMOB==null) return unpackErr("MOBs","null 'mClass': "+mClass);
			String text=restoreAngleBrackets(XMLManager.getValFromPieces(mblk.contents,"MTEXT"));
			newMOB.setMiscText(text);
			newMOB.baseEnvStats().setLevel(XMLManager.getIntFromPieces(mblk.contents,"MLEVL"));
			newMOB.baseEnvStats().setAbility(XMLManager.getIntFromPieces(mblk.contents,"MABLE"));
			newMOB.baseEnvStats().setRejuv(XMLManager.getIntFromPieces(mblk.contents,"MREJV"));
			newMOB.recoverCharStats();
			newMOB.recoverEnvStats();
			newMOB.recoverMaxState();
			newMOB.resetToMaxState();
			addHere.addElement(newMOB);
		}
		return "";
	}



	public static StringBuffer getRoomItems(Room room,
											Hashtable found,
											int type) // 0=item, 1=weapon, 2=armor
	{
		StringBuffer buf=new StringBuffer("");
		if(room==null) return buf;
		boolean mobile=room.getArea().getMobility();
		room.getArea().toggleMobility(false);
		CoffeeUtensils.resetRoom(room);
		Vector items=new Vector();
		for(int i=0;i<room.numItems();i++)
			items.addElement(room.fetchItem(i));
		Vector mobs=new Vector();
		for(int i=0;i<room.numInhabitants();i++)
			mobs.addElement(room.fetchInhabitant(i));
		room.getArea().toggleMobility(mobile);
		for(int i=0;i<items.size();i++)
		{
			Item item=(Item)items.elementAt(i);
			buf.append(getUniqueItemXML(item,type,found));
		}
		for(int m=0;m<mobs.size();m++)
		{
			MOB M=(MOB)mobs.elementAt(m);
			if((M!=null)&&(M.isEligibleMonster()))
			{
				for(int i=0;i<M.inventorySize();i++)
				{
					Item item=M.fetchInventory(i);
					buf.append(getUniqueItemXML(item,type,found));
				}
				if(CoffeeUtensils.getShopKeeper(M)!=null)
				{
					Vector V=CoffeeUtensils.getShopKeeper(M).getUniqueStoreInventory();
					for(int v=0;v<V.size();v++)
					{
						Environmental E=(Environmental)V.elementAt(v);
						if(E instanceof Item)
							buf.append(getUniqueItemXML((Item)E,type,found));
					}
				}
			}
		}
		return buf;
	}

	public static StringBuffer getRoomXML(Room room, HashSet custom, boolean andContent)
	{
		StringBuffer buf=new StringBuffer("");
		if(room==null) return buf;

		// do this quick before a tick messes it up!
		Vector inhabs=new Vector();
		boolean mobile=room.getArea().getMobility();
		room.getArea().toggleMobility(false);
		CoffeeUtensils.resetRoom(room);
		if(andContent)
		for(int i=0;i<room.numInhabitants();i++)
			inhabs.addElement(room.fetchInhabitant(i));
		Vector items=new Vector();
		if(andContent)
		for(int i=0;i<room.numItems();i++)
			items.addElement(room.fetchItem(i));
		room.getArea().toggleMobility(mobile);

		buf.append("<AROOM>");
		buf.append(XMLManager.convertXMLtoTag("ROOMID",room.roomID()));
		buf.append(XMLManager.convertXMLtoTag("RAREA",room.getArea().Name()));
		buf.append(XMLManager.convertXMLtoTag("RCLAS",CMClass.className(room)));
		buf.append(XMLManager.convertXMLtoTag("RDISP",room.displayText()));
		buf.append(XMLManager.convertXMLtoTag("RDESC",room.description()));
		buf.append(XMLManager.convertXMLtoTag("RTEXT",parseOutAngleBrackets(room.text())));
		buf.append("<ROOMEXITS>");
		for(int e=0;e<Directions.NUM_DIRECTIONS;e++)
		{
			Room door=room.rawDoors()[e];
			Exit exit=room.rawExits()[e];
			if(((door!=null)&&(door.roomID().length()>0))||((door==null)&&(exit!=null)))
			{
				buf.append("<REXIT>");
				buf.append(XMLManager.convertXMLtoTag("XDIRE",e));
				if(door==null)
					buf.append("<XDOOR />");
				else
					buf.append(XMLManager.convertXMLtoTag("XDOOR",door.roomID()));
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
						if((mob.charStats().getMyRace().isGeneric())
						&&(!custom.contains(mob.charStats().getMyRace())))
						   custom.add(mob.charStats().getMyRace());

						buf.append("<RMOB>");
						buf.append(XMLManager.convertXMLtoTag("MCLAS",CMClass.className(mob)));
						if((((mob instanceof Rideable)&&(((Rideable)mob).numRiders()>0)))||(mob.numFollowers()>0))
							buf.append(XMLManager.convertXMLtoTag("MIDEN",""+mob));
						buf.append(XMLManager.convertXMLtoTag("MLEVL",mob.baseEnvStats().level()));
						buf.append(XMLManager.convertXMLtoTag("MABLE",mob.baseEnvStats().ability()));
						buf.append(XMLManager.convertXMLtoTag("MREJV",mob.baseEnvStats().rejuv()));
						buf.append(XMLManager.convertXMLtoTag("MTEXT",parseOutAngleBrackets(mob.text())));
						if(mob.riding()!=null)
							buf.append(XMLManager.convertXMLtoTag("MRIDE",""+mob.riding()));
						else
						if(mob.amFollowing()!=null)
							buf.append(XMLManager.convertXMLtoTag("MRIDE",""+mob.amFollowing()));
						else
							buf.append("<MRIDE />");
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
					if(((item instanceof Container)&&(((Container)item).capacity()>0))
					||((item instanceof Rideable)&&(((Rideable)item).numRiders()>0)))
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
			Log.errOut("CoffeeMaker","setPropertiesStr: null 'V': "+((E==null)?"":E.Name()));
		else
		if(E==null)
			Log.errOut("CoffeeMaker","setPropertiesStr: null 'E': "+((E==null)?"":E.Name()));
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

	public static void setPropertiesStr(Environmental E, Vector V, boolean fromTop)
	{
		if(E==null)
			Log.errOut("CoffeeMaker","setPropertiesStr2: null 'E'");
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
			Log.errOut("CoffeeMaker","null XML returned on "+E.ID()+" parse. Load aborted.");
			return;
		}

		if(E instanceof Room)
		{
			setExtraEnvProperties(E,V);
			if(E instanceof GridLocale)
			{
				((GridLocale)E).setXSize(XMLManager.getIntFromPieces(V,"XGRID"));
				((GridLocale)E).setYSize(XMLManager.getIntFromPieces(V,"YGRID"));
			}
		}
		else
		if(E instanceof Area)
		{
			((Area)E).setArchivePath(XMLManager.getValFromPieces(V,"ARCHP"));
			((Area)E).setAuthorID(XMLManager.getValFromPieces(V,"AUTHOR"));
            Vector VP=XMLManager.getRealContentsFromPieces(V,"PARENTS");
            if(VP!=null)
            {
                for(int i=0;i<VP.size();i++)
                {
                    XMLManager.XMLpiece ablk=(XMLManager.XMLpiece)VP.elementAt(i);
                    if((!ablk.tag.equalsIgnoreCase("PARENT"))||(ablk.contents==null))
                    {
                        Log.errOut("CoffeeMaker","Error parsing 'PARENT' of "+E.name()+" ("+E.ID()+").  Load aborted");
                        return;
                    }
                    ((Area)E).addParentToLoad(XMLManager.getValFromPieces(ablk.contents,"PARENTNAMED"));
                }
            }
            Vector VC=XMLManager.getRealContentsFromPieces(V,"CHILDREN");
            if(VC!=null)
            {
                for(int i=0;i<VC.size();i++)
                {
                    XMLManager.XMLpiece ablk=(XMLManager.XMLpiece)VC.elementAt(i);
                    if((!ablk.tag.equalsIgnoreCase("CHILD"))||(ablk.contents==null))
                    {
                        Log.errOut("CoffeeMaker","Error parsing 'CHILD' of "+E.name()+" ("+E.ID()+").  Load aborted");
                        return;
                    }
                    ((Area)E).addChildToLoad(XMLManager.getValFromPieces(ablk.contents,"CHILDNAMED"));
                }
            }
			setExtraEnvProperties(E,V);
		}
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
			//item.wearAt(XMLManager.getIntFromPieces(V,"USES"));
		}
		else
		if(E instanceof MOB)
		{
			E.baseEnvStats().setLevel(XMLManager.getIntFromPieces(V,"MLEVL"));
			E.baseEnvStats().setAbility(XMLManager.getIntFromPieces(V,"MABLE"));
			E.baseEnvStats().setRejuv(XMLManager.getIntFromPieces(V,"MREJV"));
			if(!E.isGeneric())
				E.setMiscText(XMLManager.getValFromPieces(V,"MTEXT"));
		}
	}

	private static void setGenMobAbilities(MOB M, Vector buf)
	{
		Vector V=XMLManager.getRealContentsFromPieces(buf,"ABLTYS");
		if(V==null)
		{
			Log.errOut("CoffeeMaker","Error parsing 'ABLTYS' of "+M.Name()+".  Load aborted");
			return;
		}
		else
		{
			for(int i=0;i<V.size();i++)
			{
				XMLManager.XMLpiece ablk=(XMLManager.XMLpiece)V.elementAt(i);
				if((!ablk.tag.equalsIgnoreCase("ABLTY"))||(ablk.contents==null))
				{
					Log.errOut("CoffeeMaker","Error parsing 'ABLTY' of "+M.Name()+".  Load aborted");
					return;
				}
				Ability newOne=CMClass.getAbility(XMLManager.getValFromPieces(ablk.contents,"ACLASS"));
				Vector adat=XMLManager.getRealContentsFromPieces(ablk.contents,"ADATA");
				if((adat==null)||(newOne==null))
				{
					Log.errOut("CoffeeMaker","Error parsing 'ABLTY DATA' of "+M.name()+" ("+M.ID()+").  Load aborted");
					return;
				}
				String proff=XMLManager.getValFromPieces(ablk.contents,"APROF");
				if(proff.length()>0)
					newOne.setProfficiency(Util.s_int(proff));
				else
					newOne.setProfficiency(100);
				setPropertiesStr(newOne,adat,true);
				if(M.fetchAbility(newOne.ID())==null)
				{
					M.addAbility(newOne);
					newOne.autoInvocation(M);
				}
			}
		}
	}

	private static void setGenMobInventory(MOB M, Vector buf)
	{
		Vector V=XMLManager.getRealContentsFromPieces(buf,"INVEN");
		boolean variableEq=false;
		if(V==null)
		{
			Log.errOut("CoffeeMaker","Error parsing 'INVEN' of "+M.Name()+".  Load aborted");
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
					Log.errOut("CoffeeMaker","Error parsing 'ITEM' of "+M.Name()+".  Load aborted");
					return;
				}
				Item newOne=CMClass.getItem(XMLManager.getValFromPieces(iblk.contents,"ICLASS"));
				Vector idat=XMLManager.getRealContentsFromPieces(iblk.contents,"IDATA");
				if((idat==null)||(newOne==null))
				{
					Log.errOut("CoffeeMaker","Error parsing 'ITEM DATA' of "+M.name()+" ("+M.ID()+").  Load aborted");
					return;
				}
				int wornCode=XMLManager.getIntFromPieces(idat,"IWORN");
				if((newOne instanceof Container)&&(((Container)newOne).capacity()>0))
					IIDmap.put(XMLManager.getValFromPieces(idat,"IID"),newOne);
				String ILOC=XMLManager.getValFromPieces(idat,"ILOC");
				M.addInventory(newOne);
				if(ILOC.length()>0)
					LOCmap.put(newOne,ILOC);
				setPropertiesStr(newOne,idat,true);
				if(newOne.baseEnvStats().rejuv()>0&&newOne.baseEnvStats().rejuv()<Integer.MAX_VALUE)
					variableEq=true;
				newOne.wearAt(wornCode);
			}
			for(int i=0;i<M.inventorySize();i++)
			{
				Item item=M.fetchInventory(i);
				if(item!=null)
				{
					String ILOC=(String)LOCmap.get(item);
					if(ILOC!=null)
						item.setContainer((Item)IIDmap.get(ILOC));
					else
					if(item.amWearingAt(Item.HELD)
					&&(!item.rawLogicalAnd())
					&&((item.rawProperLocationBitmap()&Item.WIELD)>0)
					&&(M.numWearingHere(Item.WIELD)==0))
						item.wearAt(Item.WIELD);
				}
			}
		}
		if(variableEq) M.flagVariableEq();
	}

	private static void setGenPropertiesStr(Environmental E, Vector buf)
	{
		if(buf==null)
		{
			Log.errOut("CoffeeMaker","null XML returned on "+E.ID()+" parse.  Load aborted.");
			return;
		}

		if((E instanceof MOB)&&(XMLManager.getValFromPieces(buf,"GENDER").length()==0))
		{
			Log.errOut("CoffeeMaker","MOB "+E.ID()+"/"+E.name()+" has malformed XML. Load aborted.");
			return;
		}
		
		if(E instanceof MOB)
		{
			while(((MOB)E).numLearnedAbilities()>0)
			{
				Ability A=((MOB)E).fetchAbility(0);
				if(A!=null)
					((MOB)E).delAbility(A);
			}
			while(((MOB)E).inventorySize()>0)
			{
				Item I=((MOB)E).fetchInventory(0);
				if(I!=null)
					I.destroy();
			}
			if(E instanceof ShopKeeper)
			{
				Vector V=((ShopKeeper)E).getUniqueStoreInventory();
				for(int b=0;b<V.size();b++)
					((ShopKeeper)E).delStoreInventory(((Environmental)V.elementAt(b)));
			}
			if(E instanceof Deity)
			{
				while(((Deity)E).numBlessings()>0)
					((Deity)E).delBlessing(((Deity)E).fetchBlessing(0));
				while(((Deity)E).numCurses()>0)
					((Deity)E).delCurse(((Deity)E).fetchCurse(0));
				while(((Deity)E).numPowers()>0)
					((Deity)E).delPower(((Deity)E).fetchPower(0));
			}
		}
		while(E.numEffects()>0)
		{
			Ability aff=E.fetchEffect(0);
			if(aff!=null)
				E.delEffect(aff);
		}
		while(E.numBehaviors()>0)
		{
			Behavior behav=E.fetchBehavior(0);
			if(behav!=null)
				E.delBehavior(behav);
		}

		if(E instanceof MOB)
		{
			MOB mob=(MOB)E;
			mob.baseCharStats().setStat(CharStats.GENDER,(int)(char)XMLManager.getValFromPieces(buf,"GENDER").charAt(0));
			mob.setClanID(XMLManager.getValFromPieces(buf,"CLAN"));
			if(mob.getClanID().length()>0) mob.setClanRole(Clan.POS_MEMBER);
			String raceID=XMLManager.getValFromPieces(buf,"MRACE");
			if((raceID.length()>0)&&(CMClass.getRace(raceID)!=null))
			{
				Race R=CMClass.getRace(raceID);
				if(R!=null)
				{
					mob.baseCharStats().setMyRace(R);
					R.startRacing(mob,false);
				}
			}
		}

		setEnvProperties(E,buf);
		String deprecatedFlag=XMLManager.getValFromPieces(buf,"FLAG");
		if((deprecatedFlag!=null)&&(deprecatedFlag.length()>0))
			setEnvFlags(E,Util.s_int(deprecatedFlag));

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

		if(E instanceof ClanItem)
		{
			((ClanItem)E).setClanID(XMLManager.getValFromPieces(buf,"CLANID"));
			((ClanItem)E).setCIType(XMLManager.getIntFromPieces(buf,"CITYPE"));
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
			((Rideable)E).setRiderCapacity(XMLManager.getIntFromPieces(buf,"RIDEC"));
		}
		if(E instanceof Light)
		{
			String bo=XMLManager.getValFromPieces(buf,"BURNOUT");
			if((bo!=null)&&(bo.length()>0))
				((Light)E).setDestroyedWhenBurntOut(Util.s_bool(bo));
		}

		if(E instanceof LandTitle)
			((LandTitle)E).setLandPropertyID(XMLManager.getValFromPieces(buf,"LANDID"));

		if(E instanceof Perfume)
			((Perfume)E).setSmellList(XMLManager.getValFromPieces(buf,"SMELLLST"));
		
		if(E instanceof Food)
			((Food)E).setNourishment(XMLManager.getIntFromPieces(buf,"CAPA2"));
		
		if(E instanceof EnvResource)
			((EnvResource)E).setDomainSource(XMLManager.getIntFromPieces(buf,"DOMN"));

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
		if(E instanceof DeadBody)
		{
			if(((DeadBody)E).charStats()==null)
				((DeadBody)E).setCharStats(new DefaultCharStats());
			try{
				((DeadBody)E).charStats().setStat(CharStats.GENDER,(int)(char)XMLManager.getValFromPieces(buf,"GENDER").charAt(0));
				((DeadBody)E).setPlayerCorpse(XMLManager.getBoolFromPieces(buf,"MPLAYR"));
				String mobName=XMLManager.getValFromPieces(buf,"MDNAME");
				if(mobName.length()>0)
				{
					((DeadBody)E).setMobName(mobName);
					((DeadBody)E).setMobDescription(XMLManager.getValFromPieces(buf,"MDDESC"));
					((DeadBody)E).setKillerName(XMLManager.getValFromPieces(buf,"MKNAME"));
					((DeadBody)E).setKillerPlayer(XMLManager.getBoolFromPieces(buf,"MKPLAY"));
					((DeadBody)E).setMobPKFlag(XMLManager.getBoolFromPieces(buf,"MPKILL"));
					((DeadBody)E).setDestroyAfterLooting(XMLManager.getBoolFromPieces(buf,"MBREAL"));
					((DeadBody)E).setLastMessage(XMLManager.getValFromPieces(buf,"MDLMSG"));
					Vector dblk=XMLManager.getContentsFromPieces(buf,"KLTOOL");
					if((dblk!=null)&&(dblk.size()>0))
					{
						String itemi=XMLManager.getValFromPieces(dblk,"KLCLASS");
						Environmental newOne=null;
						Vector idat=XMLManager.getRealContentsFromPieces(dblk,"KLDATA");
						if(newOne==null) newOne=CMClass.getUnknown(itemi);
						if(newOne==null)
						{
							Log.errOut("CoffeeMaker","Error parsing 'TOOL DATA' of "+E.name()+" ("+E.ID()+").  Load aborted");
							return;
						}
						setPropertiesStr(newOne,idat,true);
						((DeadBody)E).setKillingTool(newOne);
					}
					else
						((DeadBody)E).setKillingTool(null);
				}
			} catch(Exception e){}
			String raceID=XMLManager.getValFromPieces(buf,"MRACE");
			if((raceID.length()>0)&&(CMClass.getRace(raceID)!=null))
			{
				Race R=CMClass.getRace(raceID);
				((DeadBody)E).charStats().setMyRace(R);
			}
		}
		if(E instanceof MOB)
		{
			MOB mob=(MOB)E;
			mob.setAlignment(XMLManager.getIntFromPieces(buf,"ALIG"));
			mob.setMoney(XMLManager.getIntFromPieces(buf,"MONEY"));
			setGenMobInventory((MOB)E,buf);
			setGenMobAbilities((MOB)E,buf);

			if(E instanceof Banker)
			{
				((Banker)E).setBankChain(XMLManager.getValFromPieces(buf,"BANK"));
				((Banker)E).setCoinInterest(XMLManager.getDoubleFromPieces(buf,"COININT"));
				((Banker)E).setCoinInterest(XMLManager.getDoubleFromPieces(buf,"ITEMINT"));
			}

			if(E instanceof Deity)
			{
				Deity godmob=(Deity)E;
				godmob.setClericRequirements(XMLManager.getValFromPieces(buf,"CLEREQ"));
				godmob.setWorshipRequirements(XMLManager.getValFromPieces(buf,"WORREQ"));
				godmob.setClericRitual(XMLManager.getValFromPieces(buf,"CLERIT"));
				godmob.setWorshipRitual(XMLManager.getValFromPieces(buf,"WORRIT"));
				godmob.setClericSin(XMLManager.getValFromPieces(buf,"CLERSIT"));
				godmob.setWorshipSin(XMLManager.getValFromPieces(buf,"WORRSIT"));
				godmob.setClericPowerup(XMLManager.getValFromPieces(buf,"CLERPOW"));

				Vector V=XMLManager.getRealContentsFromPieces(buf,"BLESSINGS");
				if(V==null)
				{
					Log.errOut("CoffeeMaker","Error parsing 'BLESSINGS' of "+E.Name()+".  Load aborted");
					return;
				}
				else
				{
					for(int i=0;i<V.size();i++)
					{
						XMLManager.XMLpiece ablk=(XMLManager.XMLpiece)V.elementAt(i);
						if((!ablk.tag.equalsIgnoreCase("BLESS"))||(ablk.contents==null))
						{
							Log.errOut("CoffeeMaker","Error parsing 'BLESS' of "+E.Name()+".  Load aborted");
							return;
						}
						Ability newOne=CMClass.getAbility(XMLManager.getValFromPieces(ablk.contents,"BLCLASS"));
						Vector adat=XMLManager.getRealContentsFromPieces(ablk.contents,"BLDATA");
						if((adat==null)||(newOne==null))
						{
							Log.errOut("CoffeeMaker","Error parsing 'BLESS DATA' of "+E.name()+" ("+E.ID()+").  Load aborted");
							return;
						}
						setPropertiesStr(newOne,adat,true);
						godmob.addBlessing(newOne);
					}
				}
				V=XMLManager.getRealContentsFromPieces(buf,"CURSES");
				if(V!=null)
				{
					for(int i=0;i<V.size();i++)
					{
						XMLManager.XMLpiece ablk=(XMLManager.XMLpiece)V.elementAt(i);
						if((!ablk.tag.equalsIgnoreCase("CURSE"))||(ablk.contents==null))
						{
							Log.errOut("CoffeeMaker","Error parsing 'CURSE' of "+E.Name()+".  Load aborted");
							return;
						}
						Ability newOne=CMClass.getAbility(XMLManager.getValFromPieces(ablk.contents,"CUCLASS"));
						Vector adat=XMLManager.getRealContentsFromPieces(ablk.contents,"CUDATA");
						if((adat==null)||(newOne==null))
						{
							Log.errOut("CoffeeMaker","Error parsing 'CURSE DATA' of "+E.name()+" ("+E.ID()+").  Load aborted");
							return;
						}
						setPropertiesStr(newOne,adat,true);
						godmob.addCurse(newOne);
					}
				}
				V=XMLManager.getRealContentsFromPieces(buf,"POWERS");
				if(V!=null)
				{
					for(int i=0;i<V.size();i++)
					{
						XMLManager.XMLpiece ablk=(XMLManager.XMLpiece)V.elementAt(i);
						if((!ablk.tag.equalsIgnoreCase("POWER"))||(ablk.contents==null))
						{
							Log.errOut("CoffeeMaker","Error parsing 'POWER' of "+E.Name()+".  Load aborted");
							return;
						}
						Ability newOne=CMClass.getAbility(XMLManager.getValFromPieces(ablk.contents,"POCLASS"));
						Vector adat=XMLManager.getRealContentsFromPieces(ablk.contents,"PODATA");
						if((adat==null)||(newOne==null))
						{
							Log.errOut("CoffeeMaker","Error parsing 'POWER DATA' of "+E.name()+" ("+E.ID()+").  Load aborted");
							return;
						}
						setPropertiesStr(newOne,adat,true);
						godmob.addPower(newOne);
					}
				}
			}
			Vector V9=Util.parseSemicolons(XMLManager.getValFromPieces(buf,"TATTS"),true);
			while(((MOB)E).numTattoos()>0)((MOB)E).delTattoo(((MOB)E).fetchTattoo(0));
			for(int v=0;v<V9.size();v++) ((MOB)E).addTattoo((String)V9.elementAt(v));
			
			V9=Util.parseSemicolons(XMLManager.getValFromPieces(buf,"EDUS"),true);
			while(((MOB)E).numEducations()>0)((MOB)E).delEducation(((MOB)E).fetchEducation(0));
			for(int v=0;v<V9.size();v++) ((MOB)E).addEducation((String)V9.elementAt(v));

			if(E instanceof ShopKeeper)
			{
				boolean variableEq=false;
				ShopKeeper shopmob=(ShopKeeper)E;
				shopmob.setWhatIsSold(XMLManager.getIntFromPieces(buf,"SELLCD"));
				shopmob.setPrejudiceFactors(XMLManager.getValFromPieces(buf,"PREJFC"));


				Vector V=XMLManager.getRealContentsFromPieces(buf,"STORE");
				if(V==null)
				{
					Log.errOut("CoffeeMaker","Error parsing 'STORE' of "+E.Name()+".  Load aborted");
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
							Log.errOut("CoffeeMaker","Error parsing 'SHITEM' of "+E.Name()+".  Load aborted");
							return;
						}
						String itemi=XMLManager.getValFromPieces(iblk.contents,"SICLASS");
						int numStock=XMLManager.getIntFromPieces(iblk.contents,"SISTOCK");
						String prc=XMLManager.getValFromPieces(iblk.contents,"SIPRICE");
						int stockPrice=-1;
						if((prc!=null)&&(prc.length()>0))
							stockPrice=Util.s_int(prc);
						Environmental newOne=null;
						Vector idat=XMLManager.getRealContentsFromPieces(iblk.contents,"SIDATA");
						if((iblk.value.indexOf("<ABLTY>")>=0)||(iblk.value.indexOf("&lt;ABLTY&gt;")>=0))
							newOne=CMClass.getMOB(itemi);
						if(newOne==null) newOne=CMClass.getUnknown(itemi);
						if((idat==null)||(newOne==null))
						{
							Log.errOut("CoffeeMaker","Error parsing 'SHOP DATA' of "+E.name()+" ("+E.ID()+").  Load aborted");
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
						if((newOne.baseEnvStats().rejuv()>0)&&(newOne.baseEnvStats().rejuv()<Integer.MAX_VALUE))
							variableEq=true;
						shopmob.addStoreInventory(newOne,numStock,stockPrice);
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
				if(variableEq) ((MOB)E).flagVariableEq();
			}
		}
	}
	
	public static String getPlayerXML(MOB mob, HashSet custom)
	{
		if(mob==null) return "";
		if(mob.Name().length()==0) return "";
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return "";

		String strStartRoomID=(mob.getStartRoom()!=null)?CMMap.getExtendedRoomID(mob.getStartRoom()):"";
		String strOtherRoomID=(mob.location()!=null)?CMMap.getExtendedRoomID(mob.location()):"";
		StringBuffer pfxml=new StringBuffer(pstats.getXML());
		if(mob.numTattoos()>0)
		{
			pfxml.append("<TATTS>");
			for(int i=0;i<mob.numTattoos();i++)
				pfxml.append(mob.fetchTattoo(i)+";");
			pfxml.append("</TATTS>");
		}
		if(mob.numEducations()>0)
		{
			pfxml.append("<EDUS>");
			for(int i=0;i<mob.numEducations();i++)
				pfxml.append(mob.fetchEducation(i)+";");
			pfxml.append("</EDUS>");
		}
		
		StringBuffer str=new StringBuffer("");
		str.append(XMLManager.convertXMLtoTag("NAME",mob.Name()));
		str.append(XMLManager.convertXMLtoTag("PASS",pstats.password()));
		str.append(XMLManager.convertXMLtoTag("CLASS",mob.baseCharStats().getMyClassesStr()));
		str.append(XMLManager.convertXMLtoTag("STR",mob.baseCharStats().getStat(CharStats.STRENGTH)));
		str.append(XMLManager.convertXMLtoTag("RACE",mob.baseCharStats().getMyRace().ID()));
		str.append(XMLManager.convertXMLtoTag("DEX",mob.baseCharStats().getStat(CharStats.DEXTERITY)));
		str.append(XMLManager.convertXMLtoTag("CON",mob.baseCharStats().getStat(CharStats.CONSTITUTION)));
		str.append(XMLManager.convertXMLtoTag("GEND",""+((char)mob.baseCharStats().getStat(CharStats.GENDER))));
		str.append(XMLManager.convertXMLtoTag("WIS",mob.baseCharStats().getStat(CharStats.WISDOM)));
		str.append(XMLManager.convertXMLtoTag("INT",mob.baseCharStats().getStat(CharStats.INTELLIGENCE)));
		str.append(XMLManager.convertXMLtoTag("CHA",mob.baseCharStats().getStat(CharStats.CHARISMA)));
		str.append(XMLManager.convertXMLtoTag("HIT",mob.baseState().getHitPoints()));
		str.append(XMLManager.convertXMLtoTag("LVL",mob.baseCharStats().getMyLevelsStr()));
		str.append(XMLManager.convertXMLtoTag("MANA",mob.baseState().getMana()));
		str.append(XMLManager.convertXMLtoTag("MOVE",mob.baseState().getMovement()));
		str.append(XMLManager.convertXMLtoTag("ALIG",mob.getAlignment()));
		str.append(XMLManager.convertXMLtoTag("EXP",mob.getExperience()));
		str.append(XMLManager.convertXMLtoTag("EXLV",mob.getExpNextLevel()));
		str.append(XMLManager.convertXMLtoTag("WORS",mob.getWorshipCharID()));
		str.append(XMLManager.convertXMLtoTag("PRAC",mob.getPractices()));
		str.append(XMLManager.convertXMLtoTag("TRAI",mob.getTrains()));
		str.append(XMLManager.convertXMLtoTag("AGEH",mob.getAgeHours()));
		str.append(XMLManager.convertXMLtoTag("GOLD",mob.getMoney()));
		str.append(XMLManager.convertXMLtoTag("WIMP",mob.getWimpHitPoint()));
		str.append(XMLManager.convertXMLtoTag("QUES",mob.getQuestPoint()));
		str.append(XMLManager.convertXMLtoTag("ROID",strStartRoomID+"||"+strOtherRoomID));
		str.append(XMLManager.convertXMLtoTag("DATE",pstats.lastDateTime()));
		str.append(XMLManager.convertXMLtoTag("CHAN",pstats.getChannelMask()));
		str.append(XMLManager.convertXMLtoTag("ATTA",mob.baseEnvStats().attackAdjustment()));
		str.append(XMLManager.convertXMLtoTag("AMOR",mob.baseEnvStats().armor()));
		str.append(XMLManager.convertXMLtoTag("DAMG",mob.baseEnvStats().damage()));
		str.append(XMLManager.convertXMLtoTag("BTMP",mob.getBitmap()));
		str.append(XMLManager.convertXMLtoTag("LEIG",mob.getLiegeID()));
		str.append(XMLManager.convertXMLtoTag("HEIT",mob.baseEnvStats().height()));
		str.append(XMLManager.convertXMLtoTag("WEIT",mob.baseEnvStats().weight()));
		str.append(XMLManager.convertXMLtoTag("PRPT",pstats.getPrompt()));
		str.append(XMLManager.convertXMLtoTag("COLR",pstats.getColorStr()));
		str.append(XMLManager.convertXMLtoTag("CLAN",mob.getClanID()));
		str.append(XMLManager.convertXMLtoTag("LSIP",pstats.lastIP()));
		str.append(XMLManager.convertXMLtoTag("CLRO",mob.getClanRole()));
		str.append(XMLManager.convertXMLtoTag("EMAL",pstats.getEmail()));
		str.append(XMLManager.convertXMLtoTag("PFIL",pfxml.toString()));
		str.append(XMLManager.convertXMLtoTag("SAVE",mob.baseCharStats().getSavesStr()));
		str.append(XMLManager.convertXMLtoTag("DESC",mob.description()));
		
		str.append(getExtraEnvPropertiesStr(mob));
		
		str.append(getGenMobAbilities(mob));
		
		str.append(getGenMobInventory(mob));
		
		StringBuffer fols=new StringBuffer("");
		for(int f=0;f<mob.numFollowers();f++)
		{
			MOB thisMOB=mob.fetchFollower(f);
			if((thisMOB!=null)&&(thisMOB.isMonster()))
			{
				fols.append("<FOLLOWER>");
				fols.append(XMLManager.convertXMLtoTag("FCLAS",CMClass.className(thisMOB)));
				fols.append(XMLManager.convertXMLtoTag("FTEXT",thisMOB.text()));
				fols.append(XMLManager.convertXMLtoTag("FLEVL",thisMOB.baseEnvStats().level()));
				fols.append(XMLManager.convertXMLtoTag("FABLE",thisMOB.baseEnvStats().ability()));
				fols.append("</FOLLOWER>");
			}
		}
		str.append(XMLManager.convertXMLtoTag("FOLLOWERS",fols.toString()));
		if((mob.baseCharStats().getMyRace().isGeneric())
		&&(!custom.contains(mob.baseCharStats().getMyRace())))
		   custom.add(mob.baseCharStats().getMyRace());
		for(int c=0;c<mob.baseCharStats().numClasses();c++)
		{
			CharClass C=mob.baseCharStats().getMyClass(c);
			if((C.isGeneric())&&(!custom.contains(C)))
				custom.add(C);
		}
		return str.toString();
	}

	public static String addPLAYERsFromXML(String xmlBuffer,
											Vector addHere,
											Session S)
	{
		Vector xml=XMLManager.parseAllXML(xmlBuffer);
		if(xml==null) return unpackErr("PLAYERs","null 'xml'");
		Vector mV=XMLManager.getRealContentsFromPieces(xml,"PLAYERS");
		if(mV==null) return unpackErr("PLAYERs","null 'mV'");
		for(int m=0;m<mV.size();m++)
		{
			XMLManager.XMLpiece mblk=(XMLManager.XMLpiece)mV.elementAt(m);
			if((!mblk.tag.equalsIgnoreCase("PLAYER"))||(mblk.contents==null))
				return unpackErr("PLAYERs","bad 'mblk'");
			MOB mob=CMClass.getMOB("StdMOB");
			mob.setPlayerStats(new DefaultPlayerStats());
			mob.setName(XMLManager.getValFromPieces(mblk.contents,"NAME"));
			mob.playerStats().setPassword(XMLManager.getValFromPieces(mblk.contents,"PASS"));
			mob.baseCharStats().setMyClasses(XMLManager.getValFromPieces(mblk.contents,"CLASS"));
			mob.baseCharStats().setMyLevels(XMLManager.getValFromPieces(mblk.contents,"LVL"));
			int level=0;
			for(int i=0;i<mob.baseCharStats().numClasses();i++)
				level+=mob.baseCharStats().getClassLevel(mob.baseCharStats().getMyClass(i));
			mob.baseEnvStats().setLevel(level);
			mob.baseCharStats().setStat(CharStats.STRENGTH,XMLManager.getIntFromPieces(mblk.contents,"STR"));
			mob.baseCharStats().setMyRace(CMClass.getRace(XMLManager.getValFromPieces(mblk.contents,"RACE")));
			mob.baseCharStats().setStat(CharStats.DEXTERITY,XMLManager.getIntFromPieces(mblk.contents,"DEX"));
			mob.baseCharStats().setStat(CharStats.CONSTITUTION,XMLManager.getIntFromPieces(mblk.contents,"CON"));
			mob.baseCharStats().setStat(CharStats.GENDER,(int)XMLManager.getValFromPieces(mblk.contents,"GEND").charAt(0));
			mob.baseCharStats().setStat(CharStats.WISDOM,XMLManager.getIntFromPieces(mblk.contents,"WIS"));
			mob.baseCharStats().setStat(CharStats.INTELLIGENCE,XMLManager.getIntFromPieces(mblk.contents,"INT"));
			mob.baseCharStats().setStat(CharStats.CHARISMA,XMLManager.getIntFromPieces(mblk.contents,"CHA"));
			mob.baseState().setHitPoints(XMLManager.getIntFromPieces(mblk.contents,"HIT"));
			mob.baseState().setMana(XMLManager.getIntFromPieces(mblk.contents,"MANA"));
			mob.baseState().setMovement(XMLManager.getIntFromPieces(mblk.contents,"MOVE"));
			mob.setAlignment(XMLManager.getIntFromPieces(mblk.contents,"ALIG"));
			mob.setExperience(XMLManager.getIntFromPieces(mblk.contents,"EXP"));
			mob.setExpNextLevel(XMLManager.getIntFromPieces(mblk.contents,"EXLV"));
			mob.setWorshipCharID(XMLManager.getValFromPieces(mblk.contents,"WORS"));
			mob.setPractices(XMLManager.getIntFromPieces(mblk.contents,"PRAC"));
			mob.setTrains(XMLManager.getIntFromPieces(mblk.contents,"TRAI"));
			mob.setAgeHours(XMLManager.getIntFromPieces(mblk.contents,"AGEH"));
			mob.setWimpHitPoint(XMLManager.getIntFromPieces(mblk.contents,"WIMP"));
			mob.setQuestPoint(XMLManager.getIntFromPieces(mblk.contents,"QUES"));
			String roomID=XMLManager.getValFromPieces(mblk.contents,"ROID");
			if(roomID==null) roomID="";
			int x=roomID.indexOf("||");
			if(x>=0)
			{
				mob.setLocation(CMMap.getRoom(roomID.substring(x+2)));
				roomID=roomID.substring(0,x);
			}
			mob.setStartRoom(CMMap.getRoom(roomID));
			mob.playerStats().setLastDateTime(XMLManager.getLongFromPieces(mblk.contents,"DATE"));
			mob.playerStats().setChannelMask(XMLManager.getIntFromPieces(mblk.contents,"CHAN"));
			mob.baseEnvStats().setAttackAdjustment(XMLManager.getIntFromPieces(mblk.contents,"ATTA"));
			mob.baseEnvStats().setArmor(XMLManager.getIntFromPieces(mblk.contents,"AMOR"));
			mob.baseEnvStats().setDamage(XMLManager.getIntFromPieces(mblk.contents,"DAMG"));
			mob.setBitmap(XMLManager.getIntFromPieces(mblk.contents,"BTMP"));
			mob.setLiegeID(XMLManager.getValFromPieces(mblk.contents,"LEIG"));
			mob.baseEnvStats().setHeight(XMLManager.getIntFromPieces(mblk.contents,"HEIT"));
			mob.baseEnvStats().setWeight(XMLManager.getIntFromPieces(mblk.contents,"WEIT"));
			mob.playerStats().setPrompt(XMLManager.getValFromPieces(mblk.contents,"PRPT"));
			String colorStr=XMLManager.getValFromPieces(mblk.contents,"COLR");
			if((colorStr!=null)&&(colorStr.length()>0)&&(!colorStr.equalsIgnoreCase("NULL")))
				mob.playerStats().setColorStr(colorStr);
			mob.setClanID(XMLManager.getValFromPieces(mblk.contents,"CLAN"));
			mob.playerStats().setLastIP(XMLManager.getValFromPieces(mblk.contents,"LSIP"));
			mob.setClanRole(XMLManager.getIntFromPieces(mblk.contents,"CLRO"));
			mob.playerStats().setEmail(XMLManager.getValFromPieces(mblk.contents,"EMAL"));
			String buf=XMLManager.getValFromPieces(mblk.contents,"CMPFIL");
			mob.playerStats().setXML(buf);
			Vector V9=Util.parseSemicolons(XMLManager.returnXMLValue(buf,"TATTS"),true);
			while(mob.numTattoos()>0)mob.delTattoo(mob.fetchTattoo(0));
			for(int v=0;v<V9.size();v++) mob.addTattoo((String)V9.elementAt(v));
			V9=Util.parseSemicolons(XMLManager.returnXMLValue(buf,"EDUS"),true);
			while(mob.numEducations()>0)mob.delEducation(mob.fetchEducation(0));
			for(int v=0;v<V9.size();v++) mob.addEducation((String)V9.elementAt(v));
			mob.baseCharStats().setSaves(XMLManager.getValFromPieces(mblk.contents,"SAVE"));
			mob.setDescription(XMLManager.getValFromPieces(mblk.contents,"DESC"));
			
			setExtraEnvProperties(mob,mblk.contents);
			
			setGenMobAbilities(mob,mblk.contents);
			
			setGenMobInventory(mob,mblk.contents);
			
			Vector iV=XMLManager.getRealContentsFromPieces(mblk.contents,"FOLLOWERS");
			if(iV==null) return unpackErr("PFols","null 'iV'");
			for(int i=0;i<iV.size();i++)
			{
				XMLManager.XMLpiece fblk=(XMLManager.XMLpiece)iV.elementAt(i);
				if((!fblk.tag.equalsIgnoreCase("FOLLOWER"))||(fblk.contents==null))
					return unpackErr("PFols","??"+fblk.tag);
				String mobClass=XMLManager.getValFromPieces(fblk.contents,"FCLAS");
				MOB newFollower=CMClass.getMOB(mobClass);
				if(newFollower==null) return unpackErr("PFols","null 'iClass': "+mobClass);
				newFollower.baseEnvStats().setLevel(XMLManager.getIntFromPieces(fblk.contents,"FLEVL"));
				newFollower.baseEnvStats().setAbility(XMLManager.getIntFromPieces(fblk.contents,"FABLE"));
				newFollower.setMiscText(XMLManager.getValFromPieces(fblk.contents,"FTEXT"));
				newFollower.recoverCharStats();
				newFollower.recoverEnvStats();
				newFollower.recoverMaxState();
				newFollower.resetToMaxState();
				mob.addFollower(newFollower);
			}
			
			mob.recoverCharStats();
			mob.recoverEnvStats();
			mob.recoverMaxState();
			mob.resetToMaxState();
			addHere.addElement(mob);
		}
		return "";
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
				behaviorstr.append(XMLManager.convertXMLtoTag("BPARMS",parseOutAngleBrackets(B.getParms())));
				behaviorstr.append("</BHAVE>");
			}
		}
		text.append(XMLManager.convertXMLtoTag("BEHAVES",behaviorstr.toString()));

		StringBuffer affectstr=new StringBuffer("");
		for(int a=0;a<E.numEffects();a++)
		{
			Ability A=E.fetchEffect(a);
			if((A!=null)&&(!A.isBorrowed(E)))
			{
				affectstr.append("<AFF>");
				affectstr.append(XMLManager.convertXMLtoTag("ACLASS",CMClass.className(A)));
				affectstr.append(XMLManager.convertXMLtoTag("ATEXT",parseOutAngleBrackets(A.text())));
				affectstr.append("</AFF>");
			}
		}
		text.append(XMLManager.convertXMLtoTag("AFFECS",affectstr.toString()));
		return text.toString();
	}

	public static String getEnvStatsStr(EnvStats E)
	{
		return E.ability()+"|"+
				E.armor()+"|"+
				E.attackAdjustment()+"|"+
				E.damage()+"|"+
				E.disposition()+"|"+
				E.level()+"|"+
				E.rejuv()+"|"+
				E.speed()+"|"+
				E.weight()+"|"+
				E.height()+"|"+
				E.sensesMask()+"|";
	}
	public static String getCharStateStr(CharState E)
	{
		return E.getFatigue()+"|"+
				E.getHitPoints()+"|"+
				E.getHunger()+"|"+
				E.getMana()+"|"+
				E.getMovement()+"|"+
				E.getThirst()+"|";
	}
	public static String getCharStatsStr(CharStats E)
	{
		StringBuffer str=new StringBuffer("");
		for(int i=0;i<CharStats.NUM_STATS;i++)
			str.append(E.getStat(i)+"|");
		return str.toString();
	}

	private static String getEnvPropertiesStr(Environmental E)
	{
		StringBuffer text=new StringBuffer("");
		text.append(XMLManager.convertXMLtoTag("NAME",E.Name()));
		text.append(XMLManager.convertXMLtoTag("DESC",E.description()));
		text.append(XMLManager.convertXMLtoTag("DISP",E.displayText()));
		text.append(XMLManager.convertXMLtoTag("PROP",getEnvStatsStr(E.baseEnvStats())));

		text.append(getExtraEnvPropertiesStr(E));
		return text.toString();
	}

	public static void setCharStats(CharStats E, String props)
	{
		int x=0;
		for(int y=props.indexOf("|");y>=0;y=props.indexOf("|"))
		{
			try
			{
				E.setStat(x,Integer.valueOf(props.substring(0,y)).intValue());
			}
			catch(Exception e)
			{
				E.setStat(x,new Integer(Util.s_int(props.substring(0,y))).intValue());
			}
			x++;
			props=props.substring(y+1);
		}
	}
	public static void setCharState(CharState E, String props)
	{
		int[] nums=new int[6];
		int x=0;
		for(int y=props.indexOf("|");y>=0;y=props.indexOf("|"))
		{
			try
			{
				nums[x]=Integer.valueOf(props.substring(0,y)).intValue();
			}
			catch(Exception e)
			{
				nums[x]=new Integer(Util.s_int(props.substring(0,y))).intValue();
			}
			x++;
			props=props.substring(y+1);
		}
		E.setFatigue(nums[0]);
		E.setHitPoints(nums[1]);
		E.setHunger(nums[2]);
		E.setMana(nums[3]);
		E.setMovement(nums[4]);
		E.setThirst(nums[5]);
	}
	public static void setEnvStats(EnvStats E, String props)
	{
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
		E.setAbility((int)Math.round(nums[0]));
		E.setArmor((int)Math.round(nums[1]));
		E.setAttackAdjustment((int)Math.round(nums[2]));
		E.setDamage((int)Math.round(nums[3]));
		E.setDisposition((int)Math.round(nums[4]));
		E.setLevel((int)Math.round(nums[5]));
		E.setRejuv((int)Math.round(nums[6]));
		E.setSpeed(nums[7]);
		E.setWeight((int)Math.round(nums[8]));
		E.setHeight((int)Math.round(nums[9]));
		E.setSensesMask((int)Math.round(nums[10]));
	}

	private static void setEnvProperties(Environmental E, Vector buf)
	{
		E.setName(XMLManager.getValFromPieces(buf,"NAME"));
		E.setDescription(XMLManager.getValFromPieces(buf,"DESC"));
		E.setDisplayText(XMLManager.getValFromPieces(buf,"DISP"));
		setEnvStats(E.baseEnvStats(),XMLManager.getValFromPieces(buf,"PROP"));
		setExtraEnvProperties(E,buf);
	}

	private static void setExtraEnvProperties(Environmental E, Vector buf)
	{

		Vector V=XMLManager.getRealContentsFromPieces(buf,"BEHAVES");
		if(V==null)
		{
			Log.errOut("CoffeeMaker","Error parsing 'BEHAVES' of "+E.name()+" ("+E.ID()+").  Load aborted");
			return;
		}
		else
		{
			for(int i=0;i<V.size();i++)
			{
				XMLManager.XMLpiece ablk=(XMLManager.XMLpiece)V.elementAt(i);
				if((!ablk.tag.equalsIgnoreCase("BHAVE"))||(ablk.contents==null))
				{
					Log.errOut("CoffeeMaker","Error parsing 'BHAVE' of "+E.name()+" ("+E.ID()+").  Load aborted");
					return;
				}
				Behavior newOne=CMClass.getBehavior(XMLManager.getValFromPieces(ablk.contents,"BCLASS"));
				String bparms=XMLManager.getValFromPieces(ablk.contents,"BPARMS");
				if(newOne==null)
				{
					Log.errOut("CoffeeMaker","Error parsing 'BHAVE DATA' of "+E.name()+" ("+E.ID()+").  Load aborted");
					return;
				}
				newOne.setParms(restoreAngleBrackets(bparms));
				E.addBehavior(newOne);
			}
		}

		V=XMLManager.getRealContentsFromPieces(buf,"AFFECS");
		if(V==null)
		{
			Log.errOut("CoffeeMaker","Error parsing 'AFFECS' of "+E.name()+" ("+E.ID()+").  Load aborted");
			return;
		}
		else
		{
			for(int i=0;i<V.size();i++)
			{
				XMLManager.XMLpiece ablk=(XMLManager.XMLpiece)V.elementAt(i);
				if((!ablk.tag.equalsIgnoreCase("AFF"))||(ablk.contents==null))
				{
					Log.errOut("CoffeeMaker","Error parsing 'AFF' of "+E.name()+" ("+E.ID()+").  Load aborted");
					return;
				}
				Ability newOne=CMClass.getAbility(XMLManager.getValFromPieces(ablk.contents,"ACLASS"));
				String aparms=XMLManager.getValFromPieces(ablk.contents,"ATEXT");
				if(newOne==null)
				{
					Log.errOut("CoffeeMaker","Error parsing 'AFF DATA' of "+E.name()+" ("+E.ID()+").  Load aborted");
					return;
				}
				newOne.setMiscText(restoreAngleBrackets(aparms));
				E.addNonUninvokableEffect(newOne);
			}
		}
	}

	public static Hashtable GENITEMCODESHASH=new Hashtable();
	public static String[] GENITEMCODES={"CLASS","USES","LEVEL","ABILITY","NAME",
									 "DISPLAY","DESCRIPTION","SECRET","PROPERWORN",
									 "WORNAND","BASEGOLD","ISREADABLE","ISDROPPABLE",
									 "ISREMOVABLE","MATERIAL","AFFBEHAV",
									 "DISPOSITION","WEIGHT","ARMOR",
									 "DAMAGE","ATTACK","READABLETEXT"};
	public static int getGenItemCodeNum(String code)
	{
		if(GENITEMCODESHASH.size()==0)
		{
			for(int i=0;i<GENITEMCODES.length;i++)
				GENITEMCODESHASH.put(GENITEMCODES[i],new Integer(i));
		}
		if(GENITEMCODESHASH.containsKey(code.toUpperCase()))
			return ((Integer)GENITEMCODESHASH.get(code.toUpperCase())).intValue();
		for(int i=0;i<GENITEMCODES.length;i++)
			if(code.toUpperCase().startsWith(GENITEMCODES[i])) return i;
		return -1;
	}
	public static String getGenItemStat(Item I, String code)
	{
		switch(getGenItemCodeNum(code))
		{
		case 0: return I.ID();
		case 1: return ""+I.usesRemaining();
		case 2: return ""+I.baseEnvStats().level();
		case 3: return ""+I.baseEnvStats().ability();
		case 4: return I.Name();
		case 5: return I.displayText();
		case 6: return I.description();
		case 7: return I.rawSecretIdentity();
		case 8: return ""+I.rawProperLocationBitmap();
		case 9: return ""+I.rawLogicalAnd();
		case 10: return ""+I.baseGoldValue();
		case 11: return ""+(Util.bset(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMREADABLE));
		case 12: return ""+(!Util.bset(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNODROP));
		case 13: return ""+(!Util.bset(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNOREMOVE));
		case 14: return ""+I.material();
		case 15: return getExtraEnvPropertiesStr(I);
		case 16: return ""+I.baseEnvStats().disposition();
		case 17: return ""+I.baseEnvStats().weight();
		case 18: return ""+I.baseEnvStats().armor();
		case 19: return ""+I.baseEnvStats().damage();
		case 20: return ""+I.baseEnvStats().attackAdjustment();
		case 21: return I.readableText();
		}
		return "";
	}
	public static void setGenItemStat(Item I, String code, String val)
	{
		switch(getGenItemCodeNum(code))
		{
		case 0: break;
		case 1: I.setUsesRemaining(Util.s_int(val)); break;
		case 2: I.baseEnvStats().setLevel(Util.s_int(val)); break;
		case 3: I.baseEnvStats().setAbility(Util.s_int(val)); break;
		case 4: I.setName(val); break;
		case 5: I.setDisplayText(val); break;
		case 6: I.setDescription(val); break;
		case 7: I.setSecretIdentity(val); break;
		case 8: I.setRawProperLocationBitmap(Util.s_long(val)); break;
		case 9: I.setRawLogicalAnd(Util.s_bool(val)); break;
		case 10: I.setBaseValue(Util.s_int(val)); break;
		case 11: Sense.setReadable(I,Util.s_bool(val)); break;
		case 12: Sense.setDroppable(I,Util.s_bool(val)); break;
		case 13: Sense.setRemovable(I,Util.s_bool(val)); break;
		case 14: I.setMaterial(Util.s_int(val)); break;
		case 15: {
					 while(I.numEffects()>0)
					 {
						 Ability A=I.fetchEffect(0);
						 if(A!=null){ A.unInvoke(); I.delEffect(A);}
					 }
					 while(I.numBehaviors()>0)
					 {
						 Behavior B=I.fetchBehavior(0);
						 if(B!=null) I.delBehavior(B);
					 }
					 setExtraEnvProperties(I,XMLManager.parseAllXML(val)); 
					 break;
				 }
		case 16: I.baseEnvStats().setDisposition(Util.s_int(val)); break;
		case 17: I.baseEnvStats().setWeight(Util.s_int(val)); break;
		case 18: I.baseEnvStats().setArmor(Util.s_int(val)); break;
		case 19: I.baseEnvStats().setDamage(Util.s_int(val)); break;
		case 20: I.baseEnvStats().setAttackAdjustment(Util.s_int(val)); break;
		case 21: I.setReadableText(val); break;
		}
	}
	public static Hashtable GENMOBCODESHASH=new Hashtable();
	public static final String[] GENMOBCODES={"CLASS","RACE","LEVEL","ABILITY","NAME",
									 "DISPLAY","DESCRIPTION","MONEY","ALIGNMENT",
									 "DISPOSITION","SENSES","ARMOR",
									 "DAMAGE","ATTACK","SPEED","AFFBEHAV",
									 "ABLES","INVENTORY","TATTS","EDUS"};

	public static int getGenMobCodeNum(String code)
	{
		if(GENMOBCODESHASH.size()==0)
		{
			for(int i=0;i<GENMOBCODES.length;i++)
				GENMOBCODESHASH.put(GENMOBCODES[i],new Integer(i));
		}
		if(GENMOBCODESHASH.containsKey(code.toUpperCase()))
			return ((Integer)GENMOBCODESHASH.get(code.toUpperCase())).intValue();
		for(int i=0;i<GENMOBCODES.length;i++)
			if(code.toUpperCase().startsWith(GENMOBCODES[i])) return i;
		return -1;
	}
	public static String getGenMobStat(MOB M, String code)
	{
		switch(getGenMobCodeNum(code))
		{
		case 0: return CMClass.className(M);
		case 1: return M.baseCharStats().getMyRace().ID();
		case 2: return ""+M.baseEnvStats().level();
		case 3: return ""+M.baseEnvStats().ability();
		case 4: return M.Name();
		case 5: return M.displayText();
		case 6: return M.description();
		case 7: return ""+M.getMoney();
		case 8: return ""+M.getAlignment();
		case 9: return ""+M.baseEnvStats().disposition();
		case 10: return ""+M.baseEnvStats().sensesMask();
		case 11: return ""+M.baseEnvStats().armor();
		case 12: return ""+M.baseEnvStats().damage();
		case 13: return ""+M.baseEnvStats().attackAdjustment();
		case 14: return ""+M.baseEnvStats().speed();
		case 15: return getExtraEnvPropertiesStr(M);
		case 16: return getGenMobAbilities(M);
		case 17:{
					StringBuffer str=new StringBuffer(getGenMobInventory(M));
					int x=str.indexOf("<IID>");
					while(x>0)
					{
						int y=str.indexOf("</IID>",x);
						if(y>x)	str.delete(x,y+6);
						else break;
						x=str.indexOf("<IID>");
					}
					x=str.indexOf("<ILOC>");
					while(x>0)
					{
						int y=str.indexOf("</ILOC>",x);
						if(y>x)	str.delete(x,y+7);
						else break;
						x=str.indexOf("<ILOC>");
					}
					return str.toString();
				}
		case 18:{StringBuffer str=new StringBuffer("");
				 for(int i=0;i<M.numTattoos();i++)
					 str.append(M.fetchTattoo(i)+";");
				 return str.toString();
				}
		case 19:{StringBuffer str=new StringBuffer("");
				 for(int i=0;i<M.numEducations();i++)
					 str.append(M.fetchEducation(i)+";");
				 return str.toString();
				}
				 
		}
		return "";
	}
	public static void setGenMobStat(MOB M, String code, String val)
	{
		switch(getGenMobCodeNum(code))
		{
		case 0: break;
		case 1: M.baseCharStats().setMyRace(CMClass.getRace(val)); break;
		case 2: M.baseEnvStats().setLevel(Util.s_int(val)); break;
		case 3: M.baseEnvStats().setAbility(Util.s_int(val)); break;
		case 4: M.setName(val); break;
		case 5: M.setDisplayText(val); break;
		case 6: M.setDescription(val); break;
		case 7: M.setMoney(Util.s_int(val)); break;
		case 8: M.setAlignment(Util.s_int(val)); break;
		case 9: M.baseEnvStats().setDisposition(Util.s_int(val)); break;
		case 10: M.baseEnvStats().setSensesMask(Util.s_int(val)); break;
		case 11: M.baseEnvStats().setArmor(Util.s_int(val)); break;
		case 12: M.baseEnvStats().setDamage(Util.s_int(val)); break;
		case 13: M.baseEnvStats().setAttackAdjustment(Util.s_int(val)); break;
		case 14: M.baseEnvStats().setSpeed(Util.s_double(val)); break;
		case 15: {
					 while(M.numEffects()>0)
					 {
						 Ability A=M.fetchEffect(0);
						 if(A!=null){ A.unInvoke(); M.delEffect(A);}
					 }
					 while(M.numBehaviors()>0)
					 {
						 Behavior B=M.fetchBehavior(0);
						 if(B!=null) M.delBehavior(B);
					 }
					 setExtraEnvProperties(M,XMLManager.parseAllXML(val)); 
					 break;
				 }
		case 16:
			{
				String extras=getExtraEnvPropertiesStr(M);
				while(M.numLearnedAbilities()>0)
				{
					Ability A=M.fetchAbility(0);
					if(A!=null) M.delAbility(A);
				}
				setExtraEnvProperties(M,XMLManager.parseAllXML(extras));
				setGenMobAbilities(M,XMLManager.parseAllXML(val));
				break;
			}
		case 17:
			{
				while(M.inventorySize()>0)
				{
					Item I=M.fetchInventory(0);
					if(I!=null) I.destroy();
				}
				setGenMobInventory(M,XMLManager.parseAllXML(val));
			}
			break;
		case 18:
			{
				Vector V9=Util.parseSemicolons(val,true);
				while(M.numTattoos()>0)M.delTattoo(M.fetchTattoo(0));
				for(int v=0;v<V9.size();v++) M.addTattoo((String)V9.elementAt(v));
			}
			break;
		case 19:
			{
				Vector V9=Util.parseSemicolons(val,true);
				while(M.numEducations()>0)M.delEducation(M.fetchEducation(0));
				for(int v=0;v<V9.size();v++) M.addEducation((String)V9.elementAt(v));
			}
			break;
		}
	}

	public static int levelsFromAbility(Item savedI)
	{ return savedI.baseEnvStats().ability()*5;}
	
	public static int levelsFromCaster(Item savedI, Ability CAST)
	{
		int level=0;
		if(CAST!=null)
		{
			String ID=CAST.ID().toUpperCase();
			Vector theSpells=new Vector();
			String names=CAST.text();
			int del=names.indexOf(";");
			while(del>=0)
			{
				String thisOne=names.substring(0,del);
				Ability A=(Ability)CMClass.getAbility(thisOne);
				if(A!=null)	theSpells.addElement(A);
				names=names.substring(del+1);
				del=names.indexOf(";");
			}
			Ability A=(Ability)CMClass.getAbility(names);
			if(A!=null) theSpells.addElement(A);
			for(int v=0;v<theSpells.size();v++)
			{
				A=(Ability)theSpells.elementAt(v);
				int mul=1;
				if(A.quality()==Ability.MALICIOUS) mul=-1;
				if(ID.indexOf("HAVE")>=0)
					level+=(mul*CMAble.lowestQualifyingLevel(A.ID()));
				else
					level+=(mul*CMAble.lowestQualifyingLevel(A.ID())/2);
			}
		}
		return level;
	}
	public static int levelsFromAdjuster(Item savedI, Ability ADJ)
	{
		int level=0;
		if(ADJ!=null)
		{
			String newText=ADJ.text();
			int ab=Util.getParmPlus(newText,"abi");
			int arm=Util.getParmPlus(newText,"arm")*-1;
			int att=Util.getParmPlus(newText,"att");
			int dam=Util.getParmPlus(newText,"dam");
			if(savedI instanceof Weapon)
				level+=(arm*2);
			else
			if(savedI instanceof Armor)
			{
				level+=(att/2);
				level+=(dam*3);
			}
			level+=ab*5;
			
			
			int dis=Util.getParmPlus(newText,"dis");
			if(dis!=0) level+=5;
			int sen=Util.getParmPlus(newText,"sen");
			if(sen!=0) level+=5;
			level+=(int)Math.round(5.0*Util.getParmDoublePlus(newText,"spe"));
			for(int i=0;i<CharStats.NUM_BASE_STATS;i++)
			{
				int stat=Util.getParmPlus(newText,CharStats.TRAITS[i].substring(0,3).toLowerCase());
				int max=Util.getParmPlus(newText,("max"+(CharStats.TRAITS[i].substring(0,3).toLowerCase())));
				level+=(stat*5);
				level+=(max*5);
			}

			int hit=Util.getParmPlus(newText,"hit");
			int man=Util.getParmPlus(newText,"man");
			int mv=Util.getParmPlus(newText,"mov");
			level+=(hit/5);
			level+=(man/5);
			level+=(mv/5);
		}
		return level;
	}
	
	public static Hashtable timsItemAdjustments(Item I,
												int level,
												int material,
												int weight,
												int hands,
												int wclass,
												int reach,
												long worndata)
	{
		Hashtable vals=new Hashtable();
		int materialvalue=EnvResource.RESOURCE_DATA[material&EnvResource.RESOURCE_MASK][1];
		Ability ADJ=I.fetchEffect("Prop_WearAdjuster");
		if(ADJ==null) ADJ=I.fetchEffect("Prop_HaveAdjuster");
		if(ADJ==null) ADJ=I.fetchEffect("Prop_RideAdjuster");
		Ability RES=I.fetchEffect("Prop_WearResister");
		if(RES==null) RES=I.fetchEffect("Prop_HaveResister");
		Ability CAST=I.fetchEffect("Prop_WearSpellCast");
		if(CAST==null) CAST=I.fetchEffect("Prop_UseSpellCast");
		if(CAST==null) CAST=I.fetchEffect("Prop_UseSpellCast2");
		if(CAST==null) CAST=I.fetchEffect("Prop_HaveSpellCast");
		if(CAST==null) CAST=I.fetchEffect("Prop_FightSpellCast");
		level-=levelsFromAbility(I);
		level-=levelsFromAdjuster(I,ADJ);
		level-=levelsFromCaster(I,CAST);
		
		if(I instanceof Weapon)
		{
			int baseattack=0;
			int basereach=0;
			int maxreach=0;
			int basematerial=EnvResource.MATERIAL_WOODEN;
			if(wclass==Weapon.CLASS_FLAILED) baseattack=-5;
			if(wclass==Weapon.CLASS_POLEARM){ basereach=1; basematerial=EnvResource.MATERIAL_METAL;}
			if(wclass==Weapon.CLASS_RANGED){ basereach=1; maxreach=5;}
			if(wclass==Weapon.CLASS_THROWN){ basereach=1; maxreach=5;}
			if(wclass==Weapon.CLASS_EDGED){ baseattack=10; basematerial=EnvResource.MATERIAL_METAL;}
			if(wclass==Weapon.CLASS_DAGGER){ baseattack=10; basematerial=EnvResource.MATERIAL_METAL;}
			if(wclass==Weapon.CLASS_SWORD){ basematerial=EnvResource.MATERIAL_METAL;}
			if(weight==0) weight=10;
			if(basereach>maxreach) maxreach=basereach;
			if(reach<basereach)
			{
				reach=basereach;
				vals.put("MINRANGE",""+basereach);
				vals.put("MAXRANGE",""+maxreach);
			}
			else
			if(reach>basereach)
				basereach=reach;
			int damage=((level-1)/((reach/weight)+2) + (weight-baseattack)/5 -reach)*(((hands*2)+1)/2);
			int cost=2*((weight*materialvalue)+((2*damage)+baseattack+(reach*10))*damage)/(hands+1);

			if(basematerial==EnvResource.MATERIAL_METAL)
			{
				switch(material&EnvResource.MATERIAL_MASK)
				{
				case EnvResource.MATERIAL_MITHRIL:
				case EnvResource.MATERIAL_METAL:
				case EnvResource.MATERIAL_ENERGY:
					break;
				case EnvResource.MATERIAL_WOODEN:
				case EnvResource.MATERIAL_PLASTIC:
					damage-=4;
					baseattack-=0;
					break;
				case EnvResource.MATERIAL_PRECIOUS:
					damage-=4;
					baseattack-=10;
					break;
				case EnvResource.MATERIAL_LEATHER:
					damage-=6;
					baseattack-=10;
					break;
				case EnvResource.MATERIAL_ROCK:
					damage-=2;
					baseattack-=10;
					break;
				case EnvResource.MATERIAL_GLASS:
					damage-=4;
					baseattack-=20;
					break;
				default:
					damage-=8;
					baseattack-=30;
					break;
				}
				switch(material)
				{
				case EnvResource.RESOURCE_BALSA:
				case EnvResource.RESOURCE_LIMESTONE:
				case EnvResource.RESOURCE_FLINT:
					baseattack-=10;
					damage-=2;
					break;
				case EnvResource.RESOURCE_CLAY:
					baseattack-=20;
					damage-=4;
					break;
				case EnvResource.RESOURCE_BONE:
					baseattack+=20;
					damage+=4;
					break;
				case EnvResource.RESOURCE_GRANITE:
				case EnvResource.RESOURCE_OBSIDIAN:
				case EnvResource.RESOURCE_IRONWOOD:
					baseattack+=10;
					damage+=2;
					break;
				case EnvResource.RESOURCE_SAND:
				case EnvResource.RESOURCE_COAL:
					baseattack-=40;
					damage-=8;
					break;
				}
			}
			if(basematerial==EnvResource.MATERIAL_WOODEN)
			{
				switch(material&EnvResource.MATERIAL_MASK)
				{
				case EnvResource.MATERIAL_WOODEN:
				case EnvResource.MATERIAL_ENERGY:
					break;
				case EnvResource.MATERIAL_METAL:
				case EnvResource.MATERIAL_MITHRIL:
					damage+=2;
					baseattack-=0;
					break;
				case EnvResource.MATERIAL_PRECIOUS:
					damage+=2;
					baseattack-=10;
					break;
				case EnvResource.MATERIAL_LEATHER:
				case EnvResource.MATERIAL_PLASTIC:
					damage-=2;
					baseattack-=0;
					break;
				case EnvResource.MATERIAL_ROCK:
					damage+=2;
					baseattack-=10;
					break;
				case EnvResource.MATERIAL_GLASS:
					damage-=2;
					baseattack-=10;
					break;
				default:
					damage-=6;
					baseattack-=30;
					break;
				}
				switch(material)
				{
				case EnvResource.RESOURCE_LIMESTONE:
				case EnvResource.RESOURCE_FLINT:
					baseattack-=10;
					damage-=2;
					break;
				case EnvResource.RESOURCE_CLAY:
					baseattack-=20;
					damage-=4;
					break;
				case EnvResource.RESOURCE_BONE:
					baseattack+=20;
					damage+=4;
					break;
				case EnvResource.RESOURCE_GRANITE:
				case EnvResource.RESOURCE_OBSIDIAN:
					baseattack+=10;
					damage+=2;
					break;
				case EnvResource.RESOURCE_SAND:
				case EnvResource.RESOURCE_COAL:
					baseattack-=40;
					damage-=8;
					break;
				}
			}
			if(damage<=0) damage=1;

			vals.put("DAMAGE",""+damage);
			vals.put("ATTACK",""+baseattack);
			vals.put("VALUE",""+cost);
		}
		else
		if(I instanceof Armor)
		{
			int[] leatherPoints={ 0, 0, 1, 5,10,16,23,31,40,49,58,67,76,85,94};
			int[] clothPoints=  { 0, 3, 7,12,18,25,33,42,52,62,72,82,92,102};
			int[] metalPoints=  { 0, 0, 0, 0, 1, 3, 5, 8,12,17,23,30,38,46,54,62,70,78,86,94};
			double pts=0.0;
			if(level<0) level=0;
			int materialCode=material&EnvResource.MATERIAL_MASK;
			int[] useArray=null;
			switch(materialCode)
			{
			case EnvResource.MATERIAL_METAL:
			case EnvResource.MATERIAL_MITHRIL:
			case EnvResource.MATERIAL_PRECIOUS:
			case EnvResource.MATERIAL_ENERGY:
				useArray=metalPoints;
				break;
			case EnvResource.MATERIAL_PLASTIC:
			case EnvResource.MATERIAL_LEATHER:
			case EnvResource.MATERIAL_GLASS:
			case EnvResource.MATERIAL_ROCK:
			case EnvResource.MATERIAL_WOODEN:
				useArray=leatherPoints;
				break;
			default:
				useArray=clothPoints;
				break;
			}
			if(level>=useArray[useArray.length-1])
				pts=new Integer(useArray.length-2).doubleValue();
			else
			for(int i=0;i<useArray.length;i++)
			{
				int lvl=useArray[i];
				if(lvl>level)
				{
					pts=new Integer(i-1).doubleValue();
					break;
				}
			}

			double totalpts=0.0;
			double weightpts=0.0;
			double wornweights=0.0;
			for(int i=0;i<Item.wornWeights.length-1;i++)
			{
				if(Util.isSet(worndata,i))
				{
					totalpts+=(pts*Item.wornWeights[i+1]);
					wornweights+=Item.wornWeights[i+1];
					switch(materialCode)
					{
					case EnvResource.MATERIAL_METAL:
					case EnvResource.MATERIAL_MITHRIL:
					case EnvResource.MATERIAL_PRECIOUS:
						weightpts+=Item.wornHeavyPts[i+1][2];
						break;
					case EnvResource.MATERIAL_LEATHER:
					case EnvResource.MATERIAL_GLASS:
					case EnvResource.MATERIAL_PLASTIC:
					case EnvResource.MATERIAL_ROCK:
					case EnvResource.MATERIAL_WOODEN:
						weightpts+=Item.wornHeavyPts[i+1][1];
						break;
					case EnvResource.MATERIAL_ENERGY:
						break;
					default:
						weightpts+=Item.wornHeavyPts[i+1][0];
						break;
					}
					if(hands==1) break;
				}
			}
			int cost=(int)Math.round(((pts*pts) + new Integer(materialvalue).doubleValue())
									 * ( weightpts / 2));
			int armor=(int)Math.round(totalpts);
			switch(material)
			{
				case EnvResource.RESOURCE_BALSA:
				case EnvResource.RESOURCE_LIMESTONE:
				case EnvResource.RESOURCE_FLINT:
					armor-=1;
					break;
				case EnvResource.RESOURCE_CLAY:
					armor-=2;
					break;
				case EnvResource.RESOURCE_BONE:
					armor+=2;
					break;
				case EnvResource.RESOURCE_GRANITE:
				case EnvResource.RESOURCE_OBSIDIAN:
				case EnvResource.RESOURCE_IRONWOOD:
					armor+=1;
					break;
				case EnvResource.RESOURCE_SAND:
				case EnvResource.RESOURCE_COAL:
					armor-=4;
					break;
			}
			vals.put("ARMOR",""+armor);
			vals.put("VALUE",""+cost);
			vals.put("WEIGHT",""+(int)Math.round(new Integer(armor).doubleValue()/wornweights*weightpts));
		}
		return vals;
	}

	public static Area copyArea(Area A, String newName)
	{
		Area newArea=(Area)A.copyOf();
		newArea.setName(newName);
		CMClass.DBEngine().DBCreateArea(newName,newArea.ID());
		Hashtable altIDs=new Hashtable();
		for(Enumeration e=A.getMap();e.hasMoreElements();)
		{
			Room room=(Room)e.nextElement();
			Room newRoom=(Room)room.copyOf();
			newRoom.clearSky();
			if(newRoom instanceof GridLocale)
				((GridLocale)newRoom).clearGrid();
			for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				newRoom.rawDoors()[d]=null;
			newRoom.setRoomID(CMMap.getOpenRoomID(newName));
			newRoom.setArea(newArea);
			CMClass.DBEngine().DBCreateRoom(newRoom,CMClass.className(newRoom));
			altIDs.put(room.roomID(),newRoom.roomID());
			if(newRoom.numInhabitants()>0)
				CMClass.DBEngine().DBUpdateMOBs(newRoom);
			if(newRoom.numItems()>0)
				CMClass.DBEngine().DBUpdateItems(newRoom);
			CMMap.addRoom(newRoom);
		}
		for(Enumeration e=A.getMap();e.hasMoreElements();)
		{
			Room room=(Room)e.nextElement();
			String altID=(String)altIDs.get(room.roomID());
			if(altID==null) continue;
			Room newRoom=CMMap.getRoom(altID);
			if(newRoom==null) continue;
			
			for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
			{
				Room R=room.rawDoors()[d];
				String myRID=null;
				if(R!=null) myRID=(String)altIDs.get(R.roomID());
				Room myR=null;
				if(myRID!=null) myR=CMMap.getRoom(myRID);
				newRoom.rawDoors()[d]=myR;
			}
			CMClass.DBEngine().DBUpdateExits(newRoom);
			newRoom.getArea().fillInAreaRoom(newRoom);
		}
		return newArea;
	}
}
