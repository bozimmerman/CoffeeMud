package com.planet_ink.coffee_mud.system;

import java.sql.*;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
public class RoomLoader
{
	public static void updateBootStatus(Host myHost, double pct)
	{
		myHost.setGameStatusStr("Booting: loading rooms ("+((int)Math.round(pct*100.0))+"% completed)");
	}
	
	public static void DBRead(Host myHost, Vector areas, Vector h)
	{
		double pct=0.0;
		updateBootStatus(myHost,pct);
		
		DBConnection D=null;
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMAREA");
			while(R.next())
			{
				String areaName=DBConnections.getRes(R,"CMAREA");
				String areaType=DBConnections.getRes(R,"CMTYPE");
				Area A=CMClass.getAreaType(areaType);
				if(A==null) A=CMClass.getAreaType("StdArea");
				if(A==null)
				{
					Log.errOut("Could not create area: "+areaName);
					return;
				}
				A.setName(areaName);
				areas.addElement(A);
				A.setClimateType((int)DBConnections.getLongRes(R,"CMCLIM"));
				A.setSubOpList(DBConnections.getRes(R,"CMSUBS"));
				A.setDescription(DBConnections.getRes(R,"CMDESC"));
				A.setMiscText(DBConnections.getRes(R,"CMROTX"));
				A.tickControl(true);
			}
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Area",sqle);
			if(D!=null) DBConnector.DBDone(D);
			return;
		}
		
		Hashtable newAreasToCreate=new Hashtable();
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMROOM");
			while(R.next())
			{
				String roomID=DBConnections.getRes(R,"CMROID");
				String localeID=DBConnections.getRes(R,"CMLOID");
				Room newRoom=(Room)CMClass.getLocale(localeID);
				if(newRoom==null)
					Log.errOut("Room","Couldn't load room '"+roomID+"', localeID '"+localeID+"'.");
				else
				{
					newRoom.setID(roomID);
					String areaName=DBConnections.getRes(R,"CMAREA");
					Area myArea=CMMap.getArea(areaName);
					if(myArea==null)
					{
						myArea=(Area)CMClass.getAreaType("StdArea").copyOf();
						myArea.setName(areaName);
						if(!newAreasToCreate.containsKey(areaName))
							newAreasToCreate.put(areaName,areaName);
					}
					newRoom.setArea(myArea);
					newRoom.setDisplayText(DBConnections.getRes(R,"CMDESC1"));
					newRoom.setDescription(DBConnections.getRes(R,"CMDESC2"));
					newRoom.setMiscText(DBConnections.getRes(R,"CMROTX"));
					h.addElement(newRoom);
				}
			}
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
			if(D!=null) DBConnector.DBDone(D);
			return;
		}
		
		pct+=(0.006);
		updateBootStatus(myHost,pct);
		
		// handle stray areas
		for(Enumeration e=newAreasToCreate.elements();e.hasMoreElements();)
		{
			String areaName=(String)e.nextElement();
			Log.sysOut("Area","Creating unhandled area: "+areaName);
			Area realArea=DBCreate(areaName,"StdArea");
			for(int r=0;r<h.size();r++)
			{
				Room R=(Room)h.elementAt(r);
				if(R.getArea().name().equals(areaName))
					R.setArea(realArea);
			}
		}
		
		// now grab the exits
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMROEX");
			while(R.next())
			{
				String roomID=DBConnections.getRes(R,"CMROID");
				Room thisRoom=null;
				for(int m=0;m<h.size();m++)
					if(((Room)h.elementAt(m)).ID().equalsIgnoreCase(roomID))
					{
						thisRoom=(Room)h.elementAt(m);
						break;
					}
				if(thisRoom==null)
					Log.errOut("Room","Couldn't set exit for unknown room '"+roomID+"'");
				else
				{
					int direction=(int)DBConnections.getLongRes(R,"CMDIRE");
					String exitID=DBConnections.getRes(R,"CMEXID");
					String exitMiscText=DBConnections.getResQuietly(R,"CMEXTX");
					String nextRoomID=DBConnections.getRes(R,"CMNRID");
					Exit newExit=CMClass.getExit(exitID);
					Room newRoom=CMMap.getRoom(nextRoomID);
					if((newExit==null)&&(newRoom==null))
						Log.errOut("Room","Couldn't find room '"+nextRoomID+"', exit type '"+exitID+"', direction: "+direction);
					else
					if(newExit!=null)
						newExit.setMiscText(exitMiscText);
					thisRoom.rawDoors()[direction]=newRoom;
					thisRoom.rawExits()[direction]=newExit;
				}
			}
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
			if(D!=null) DBConnector.DBDone(D);
		}
		
		pct+=(0.05);
		updateBootStatus(myHost,pct);
		
		double pcteach=Util.div(0.941516,h.size());
		for(int m=0;m<h.size();m++)
		{
			Room thisRoom=(Room)h.elementAt(m);
			DBReadContent(thisRoom);
			thisRoom.startItemRejuv();
			thisRoom.recoverRoomStats();
			pct+=pcteach;
			updateBootStatus(myHost,pct);
		}
		
		for(int a=0;a<areas.size();a++)
		{
			Area A=(Area)areas.elementAt(a);
			StringBuffer s=A.getAreaStats();
			Resources.submitResource("HELP_"+A.name().toUpperCase(),s);
		}
		
		pct=1.0;
		updateBootStatus(myHost,pct);
	}

	public static void DBReadContent(Room thisRoom)
	{

		if(thisRoom==null)
			return;

		Hashtable itemTimes=new Hashtable();
		Hashtable mobTimes=new Hashtable();
		
		DBConnection D=null;
		// now grab the items
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMROIT WHERE CMROID='"+thisRoom.ID()+"'");
			
			Hashtable itemNums=new Hashtable();
			Hashtable itemLocs=new Hashtable();
			while(R.next())
			{
				String itemNum=DBConnections.getRes(R,"CMITNM");
				String itemID=DBConnections.getRes(R,"CMITID");
				Item newItem=(Item)CMClass.getItem(itemID);
				if(newItem==null)
					Log.errOut("Room","Couldn't find item '"+itemID+"'");
				else
				{
					itemNums.put(itemNum,newItem);
					String loc=DBConnections.getResQuietly(R,"CMITLO");
					if(loc.length()>0) 
					{
						Item container=(Item)itemNums.get(loc);
						if(container!=null)
							newItem.setContainer(container);
						else
							itemLocs.put(newItem,loc);
					}
					newItem.setMiscText(DBConnections.getResQuietly(R,"CMITTX"));
					newItem.baseEnvStats().setRejuv((int)DBConnections.getLongRes(R,"CMITRE"));
					newItem.setUsesRemaining((int)DBConnections.getLongRes(R,"CMITUR"));
					newItem.baseEnvStats().setLevel((int)DBConnections.getLongRes(R,"CMITLV"));
					newItem.baseEnvStats().setAbility((int)DBConnections.getLongRes(R,"CMITAB"));
					newItem.baseEnvStats().setHeight((int)DBConnections.getLongRes(R,"CMHEIT"));
					newItem.recoverEnvStats();
					thisRoom.addItem(newItem);
				}
			}
			DBConnector.DBDone(D);
			for(Enumeration e=itemLocs.keys();e.hasMoreElements();)
			{
				Item keyItem=(Item)e.nextElement();
				String location=(String)itemLocs.get(keyItem);
				Item container=(Item)itemNums.get(location);
				if(container!=null)
				{
					keyItem.setContainer(container);
					keyItem.recoverEnvStats();
					container.recoverEnvStats();
				}
			}
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
			if(D!=null) DBConnector.DBDone(D);
		}

		// now grab the inhabitants
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMROCH WHERE CMROID='"+thisRoom.ID()+"'");
			while(R.next())
			{
				String MOBID=DBConnections.getRes(R,"CMCHID");
				MOB newMOB=(MOB)CMClass.getMOB(MOBID);
				if(newMOB==null)
					Log.errOut("Room","Couldn't find MOB '"+MOBID+"'");
				else
				{
					newMOB.setStartRoom(thisRoom);
					newMOB.setLocation(thisRoom);
					newMOB.setMiscText(DBConnections.getResQuietly(R,"CMCHTX"));
					newMOB.baseEnvStats().setLevel(((int)DBConnections.getLongRes(R,"CMCHLV")));
					newMOB.baseEnvStats().setAbility((int)DBConnections.getLongRes(R,"CMCHAB"));
					newMOB.baseEnvStats().setRejuv((int)DBConnections.getLongRes(R,"CMCHRE"));
					newMOB.recoverCharStats();
					newMOB.recoverEnvStats();
					newMOB.recoverMaxState();
					newMOB.resetToMaxState();
					newMOB.bringToLife(thisRoom,true);
				}
			}
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
			if(D!=null) DBConnector.DBDone(D);
		}
	}

	private static void DBUpdateContents(Room room)
		throws SQLException
	{
		DBConnection D=null;
		String sql=null;
		try
		{
			for(int i=0;i<room.numItems();i++)
			{
				Item thisItem=room.fetchItem(i);
				if(thisItem!=null)
				{
					thisItem.setDispossessionTime(null); // saved items won't clear!
					D=DBConnector.DBFetch();
					sql=
					"INSERT INTO CMROIT ("
					+"CMROID, "
					+"CMITNM, "
					+"CMITID, "
					+"CMITLO, "
					+"CMITTX, "
					+"CMITRE, "
					+"CMITUR, "
					+"CMITLV, "
					+"CMITAB, "
					+"CMHEIT"
					+") values ("
					+"'"+room.ID()+"',"
					+"'"+thisItem+"',"
					+"'"+thisItem.ID()+"',"
					+"'"+((thisItem.container()!=null)?(""+thisItem.container()):"")+"',"
					+"'"+thisItem.text()+" ',"
					+thisItem.baseEnvStats().rejuv()+","
					+thisItem.usesRemaining()+","
					+thisItem.baseEnvStats().level()+","
					+thisItem.baseEnvStats().ability()+","
					+thisItem.baseEnvStats().height()+")";
					D.update(sql);
					DBConnector.DBDone(D);
				}
			}
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sql);
			Log.errOut("Room","UpdateItems"+sqle);
			if(D!=null) DBConnector.DBDone(D);
		}
	}

	public static void DBUpdateItems(Room room)
	{
		if(room.ID().length()==0) return;
		DBConnection D=null;
		try
		{
			D=DBConnector.DBFetch();
			D.update("DELETE FROM CMROIT WHERE CMROID='"+room.ID()+"'");
			DBConnector.DBDone(D);
			DBUpdateContents(room);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room","UpdateItems"+sqle);
			if(D!=null) DBConnector.DBDone(D);
		}
	}

	public static void DBUpdateExits(Room room)
	{
		if(room.ID().length()==0) return;
		DBConnection D=null;
		String str=null;
		try
		{
			D=DBConnector.DBFetch();
			D.update("DELETE FROM CMROEX WHERE CMROID='"+room.ID()+"'");
			DBConnector.DBDone(D);
			for(int e=0;e<Directions.NUM_DIRECTIONS;e++)
			{
				Exit thisExit=room.rawExits()[e];
				Room thisRoom=room.rawDoors()[e];
				if(((thisRoom==null)&&(thisExit!=null))||(thisRoom!=null)&&(thisRoom.ID().length()>0))
				{
					D=DBConnector.DBFetch();
					str="INSERT INTO CMROEX ("
					+"CMROID, "
					+"CMDIRE, "
					+"CMEXID, "
					+"CMEXTX, "
					+"CMNRID"
					+") values ("
					+"'"+room.ID()+"',"
					+e+","
					+"'"+CoffeeUtensils.id(thisExit)+"',"
					+"'"+((thisExit==null)?" ":thisExit.text())+" ',"
					+"'"+CoffeeUtensils.id(thisRoom)+"')";
					D.update(str);
					DBConnector.DBDone(D);
				}
			}
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",str);
			Log.errOut("Room","UpdateExits"+sqle);
			if(D!=null) DBConnector.DBDone(D);
		}
	}

	public static void DBUpdateMOBs(Room room)
	{
		if(room.ID().length()==0) return;
		DBConnection D=null;
		String str=null;
		try
		{
			D=DBConnector.DBFetch();
			D.update("DELETE FROM CMROCH WHERE CMROID='"+room.ID()+"'");
			DBConnector.DBDone(D);
			for(int m=0;m<room.numInhabitants();m++)
			{
				MOB thisMOB=room.fetchInhabitant(m);

				if((thisMOB!=null)&&(thisMOB.isEligibleMonster()))
				{
					D=DBConnector.DBFetch();
					str=
					 "INSERT INTO CMROCH ("
					 +"CMROID, "
					 +"CMCHNM, "
					 +"CMCHID, "
					 +"CMCHTX, "
					 +"CMCHLV, "
					 +"CMCHAB, "
					 +"CMCHRE "
					 +") values ("
					 +"'"+room.ID()+"',"
					 +m+","
					 +"'"+CMClass.className(thisMOB)+"',"
					 +"'"+thisMOB.text()+" ',"
					 +thisMOB.baseEnvStats().level()+","
					 +thisMOB.baseEnvStats().ability()+","
					 +thisMOB.baseEnvStats().rejuv()+""
					 +")";
					D.update(str);
					DBConnector.DBDone(D);
				}
			}
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",str);
			Log.errOut("Room","UpdateMOBs"+sqle);
			if(D!=null) DBConnector.DBDone(D);
		}
	}


	public static void DBUpdateAll(Room room)
	{
		if(room.ID().length()==0) return;
		DBUpdateRoom(room);
		DBUpdateMOBs(room);
		DBUpdateExits(room);
		DBUpdateItems(room);
	}


	public static void DBUpdateRoom(Room room)
	{
		if(room.ID().length()==0) return;
		DBConnection D=null;
		try
		{
			D=DBConnector.DBFetch();
			String str="UPDATE CMROOM SET "
					+"CMLOID='"+CMClass.className(room)+"',"
					+"CMAREA='"+room.getArea().name()+"',"
					+"CMDESC1='"+room.displayText()+" ',"
					+"CMDESC2='"+room.description()+" ',"
					+"CMROTX='"+room.text()+" '"
					+"WHERE CMROID='"+room.ID()+"'";
			D.update(str);
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
			if(D!=null) DBConnector.DBDone(D);
			return;
		}
	}


	public static void DBReCreate(Room room, String oldID)
	{
		if(room.ID().length()==0) return;
		DBConnection D=null;
		try
		{
			D=DBConnector.DBFetch();
			D.update("UPDATE CMROOM SET "
					+"CMROID='"+room.ID()+"', "
					+"CMAREA='"+room.getArea().name()+"' "
					+"WHERE CMROID='"+oldID+"'");
			DBConnector.DBDone(D);

			D=DBConnector.DBFetch();
			D.update("UPDATE CMROCH SET "
					+"CMROID='"+room.ID()+"' "
					+"WHERE CMROID='"+oldID+"'");
			DBConnector.DBDone(D);

			D=DBConnector.DBFetch();
			D.update("UPDATE CMROEX SET "
					+"CMROID='"+room.ID()+"' "
					+"WHERE CMROID='"+oldID+"'");
			DBConnector.DBDone(D);

			D=DBConnector.DBFetch();
			D.update("UPDATE CMROIT SET "
					+"CMROID='"+room.ID()+"' "
					+"WHERE CMROID='"+oldID+"'");
			DBConnector.DBDone(D);

			D=DBConnector.DBFetch();
			D.update("UPDATE CMCHAR SET "
					+"CMROID='"+room.ID()+"' "
					+"WHERE CMROID='"+oldID+"'");
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
			if(D!=null) DBConnector.DBDone(D);
			return;
		}
	}

	public static Area DBCreate(String areaName, String areaType)
	{
		Area A=CMClass.getAreaType(areaType);
		if(A==null) A=CMClass.getAreaType("StdArea");
		if((A==null)||(areaName.length()==0)) return null;
		Resources.removeResource("areasList");
		Resources.removeResource("areasListHTML");
		
		A=(Area)A.copyOf();
		A.setName(areaName);
		CMMap.addArea(A);
		DBConnection D=null;
		String str=null;
		try
		{
			D=DBConnector.DBFetch();
			str="INSERT INTO CMAREA ("
			+"CMAREA,"
			+"CMTYPE,"
			+"CMCLIM,"
			+"CMSUBS,"
			+"CMDESC,"
			+"CMROTX"
			+") values ("
			+"'"+A.name()+"',"
			+"'"+CMClass.className(A)+"',"
			+""+A.climateType()+","
			+"'"+A.getSubOpList()+"',"
			+"'"+A.description()+" ',"
			+"'"+A.text()+" ')";
			D.update(str);
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Area",str);
			Log.errOut("Area",sqle);
			if(D!=null) DBConnector.DBDone(D);
		}
		if(A==null) return null;
		A.tickControl(true);
		return A;
	}
	
	public static void DBUpdate(Area A)
	{
		DBConnection D=null;
		String str=null;
		try
		{
			D=DBConnector.DBFetch();
			str="UPDATE CMAREA SET "
				+"CMTYPE='"+CMClass.className(A)+"',"
				+"CMCLIM="+A.climateType()+","
				+"CMSUBS='"+A.getSubOpList()+"',"
				+"CMDESC='"+A.description()+" ',"
				+"CMROTX='"+A.text()+" '"
				+"WHERE CMAREA='"+A.name()+"'";
			D.update(str);
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Area",str);
			Log.errOut("Area",sqle);
			if(D!=null) DBConnector.DBDone(D);
			return;
		}
	}
	
	public static void DBDelete(Area A)
	{
		if(A==null) return;
		A.tickControl(false);
		DBConnection D=null;
		try
		{
			D=DBConnector.DBFetch();
			D.update("DELETE FROM CMAREA WHERE CMAREA='"+A.name()+"'");
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
			if(D!=null) DBConnector.DBDone(D);
			return;
		}
	}
	
	
	public static void DBCreate(Room room, String LocaleID)
	{
		if(room.ID().length()==0) return;
		DBConnection D=null;
		String str=null;
		try
		{
			D=DBConnector.DBFetch();
			str="INSERT INTO CMROOM ("
			+"CMROID,"
			+"CMLOID,"
			+"CMAREA,"
			+"CMDESC1,"
			+"CMDESC2,"
			+"CMROTX"
			+") values ("
			+"'"+room.ID()+"',"
			+"'"+LocaleID+"',"
			+"'"+room.getArea().name()+"',"
			+"'"+room.displayText()+" ',"
			+"'"+room.description()+" ',"
			+"'"+room.text()+" ')";
			D.update(str);
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",str);
			Log.errOut("Room",sqle);
			if(D!=null) DBConnector.DBDone(D);
			return;
		}

	}

	public static void DBDelete(Room room)
	{
		if(room.ID().length()==0) return;
		DBConnection D=null;
		try
		{
			while(room.numInhabitants()>0)
			{
				MOB inhab=room.fetchInhabitant(0);
				if(inhab!=null)
					room.delInhabitant(inhab);
			}
			DBUpdateMOBs(room);

			for(int i=0;i<Directions.NUM_DIRECTIONS;i++)
			{
				room.rawExits()[i]=null;
				room.rawDoors()[i]=null;
			}
			DBUpdateExits(room);

			while(room.numItems()>0)
			{
				Item thisItem=room.fetchItem(0);
				if(thisItem!=null)
				{
					thisItem.setContainer(null);
					room.delItem(thisItem);
				}
			}
			DBUpdateItems(room);

			D=DBConnector.DBFetch();
			D.update("DELETE FROM CMROOM WHERE CMROID='"+room.ID()+"'");
			DBConnector.DBDone(D);

		}
		catch(SQLException sqle)
		{
			Log.errOut("Room","Delete"+sqle);
			if(D!=null) DBConnector.DBDone(D);
		}
	}
}