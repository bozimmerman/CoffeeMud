package com.planet_ink.coffee_mud.system;

import java.sql.*;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
public class RoomLoader
{
	public static void DBRead(Vector areas, Vector h)
	{
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
		
		// handle stray areas
		for(Enumeration e=newAreasToCreate.elements();e.hasMoreElements();)
		{
			String areaName=(String)e.nextElement();
			Log.sysOut("Area","Creating unhandled area: "+areaName);
			DBCreate(areaName,"StdArea");
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
					{
						newExit=(Exit)newExit.newInstance();
						newExit.setMiscText(exitMiscText);
					}
					thisRoom.doors()[direction]=newRoom;
					thisRoom.exits()[direction]=newExit;
				}
			}
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
			if(D!=null) DBConnector.DBDone(D);
		}
		for(int m=0;m<h.size();m++)
		{
			Room thisRoom=(Room)h.elementAt(m);
			DBReadContent(thisRoom);
			thisRoom.startItemRejuv();
			thisRoom.recoverRoomStats();
		}
		for(int a=0;a<areas.size();a++)
		{
			Area A=(Area)areas.elementAt(a);
			StringBuffer s=A.getAreaStats();
			Resources.submitResource("HELP_"+A.name().toUpperCase(),s);
		}
	}

	public static void DBReadContent(Room thisRoom)
	{

		if(thisRoom==null)
			return;

		DBConnection D=null;
		// now grab the items
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMROIT WHERE CMROID='"+thisRoom.ID()+"' order by CMITNM");
			Hashtable itemNums=new Hashtable();
			while(R.next())
			{
				int itemNumber=(int)DBConnections.getLongRes(R,"CMITNM");
				String itemID=DBConnections.getRes(R,"CMITID");
				Item newItem=(Item)CMClass.getItem(itemID);
				if(newItem==null)
					Log.errOut("Room","Couldn't find item '"+itemID+"'");
				else
				{
					newItem=(Item)newItem.newInstance();
					int locationNumber=(int)DBConnections.getLongRes(R,"CMITLO");
					newItem.setMiscText(DBConnections.getResQuietly(R,"CMITTX"));
					newItem.baseEnvStats().setRejuv((int)DBConnections.getLongRes(R,"CMITRE"));
					newItem.setUsesRemaining((int)DBConnections.getLongRes(R,"CMITUR"));
					newItem.baseEnvStats().setLevel((int)DBConnections.getLongRes(R,"CMITLV"));
					newItem.baseEnvStats().setAbility((int)DBConnections.getLongRes(R,"CMITAB"));
					newItem.recoverEnvStats();
					thisRoom.addItem(newItem);
					newItem.setLocation((Item)itemNums.get(new Integer(locationNumber)));
					itemNums.put(new Integer(itemNumber),newItem);
				}
			}
			DBConnector.DBDone(D);
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
			ResultSet R=D.query("SELECT * FROM CMROCH WHERE CMROID='"+thisRoom.ID()+"' order by CMCHNM");
			while(R.next())
			{
				String MOBID=DBConnections.getRes(R,"CMCHID");
				MOB newMOB=(MOB)CMClass.getMOB(MOBID);
				if(newMOB==null)
					Log.errOut("Room","Couldn't find MOB '"+MOBID+"'");
				else
				{
					newMOB=(MOB)newMOB.newInstance();
					newMOB.setStartRoom(thisRoom);
					newMOB.setLocation(thisRoom);
					newMOB.setMiscText(DBConnections.getResQuietly(R,"CMCHTX"));
					newMOB.baseEnvStats().setLevel(((int)DBConnections.getLongRes(R,"CMCHLV")));
					newMOB.baseEnvStats().setAbility((int)DBConnections.getLongRes(R,"CMCHAB"));
					newMOB.baseEnvStats().setRejuv((int)DBConnections.getLongRes(R,"CMCHRE"));
					newMOB.baseCharStats().getMyRace().setWeight(newMOB);
					newMOB.recoverCharStats();
					newMOB.recoverEnvStats();
					newMOB.recoverMaxState();
					newMOB.resetToMaxState();
					newMOB.bringToLife(thisRoom);
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

	private static int DBUpdateContents(Room room,
										Item item,
										int itemNumber)
		throws SQLException
	{
		DBConnection D=null;
		int newItemNumber=itemNumber;
		String sql=null;
		try
		{
			for(int i=0;i<room.numItems();i++)
			{
				Item thisItem=room.fetchItem(i);
				if(thisItem!=null)
				{
					thisItem.setPossessionTime(null); // saved items won't clear!
					if(thisItem.location()==item)
					{
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
						+"CMITAB"
						+") values ("
						+"'"+room.ID()+"',"
						+(++newItemNumber)+","
						+"'"+thisItem.ID()+"',"
						+Integer.toString(itemNumber)+","
						+"'"+thisItem.text()+" ',"
						+thisItem.baseEnvStats().rejuv()+","
						+thisItem.usesRemaining()+","
						+thisItem.baseEnvStats().level()+","
						+thisItem.baseEnvStats().ability()+")";
						D.update(sql);
						DBConnector.DBDone(D);
						newItemNumber=DBUpdateContents(room,thisItem,newItemNumber);
					}
				}
			}
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sql);
			Log.errOut("Room","UpdateItems"+sqle);
			if(D!=null) DBConnector.DBDone(D);
		}
		return newItemNumber;
	}

	public static void DBUpdateItems(Room room)
	{
		if(room.ID().length()==0) return;
		DBConnection D=null;
		try
		{
			D=DBConnector.DBFetch();
			D.update("DELETE FROM CMROIT WHERE CMROID='"+room.ID()+"'");
			if(DBConnector.DBConfirmDeletions)
			{
				ResultSet R=D.query("SELECT * FROM CMROIT WHERE CMROID='"+room.ID()+"'");
				if((R!=null)&&(R.next()))
					Log.errOut("DBUpdateItems","Delete Failed.");
			}
			DBConnector.DBDone(D);
			DBUpdateContents(room,null,-1);
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
			if(DBConnector.DBConfirmDeletions)
			{
				ResultSet R=D.query("SELECT * FROM CMROEX WHERE CMROID='"+room.ID()+"'");
				if((R!=null)&&(R.next()))
					Log.errOut("DBUpdateExits","Delete Failed.");
			}
			DBConnector.DBDone(D);
			for(int e=0;e<Directions.NUM_DIRECTIONS;e++)
			{
				Exit thisExit=room.exits()[e];
				Room thisRoom=room.doors()[e];
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
			if(DBConnector.DBConfirmDeletions)
			{
				ResultSet R=D.query("SELECT * FROM CMROCH WHERE CMROID='"+room.ID()+"'");
				if((R!=null)&&(R.next()))
					Log.errOut("DBUpdateMOBs","Delete Failed.");
			}
			DBConnector.DBDone(D);
			for(int m=0;m<room.numInhabitants();m++)
			{
				MOB thisMOB=room.fetchInhabitant(m);

				if((thisMOB!=null)&&(CoffeeUtensils.isEligibleMonster(thisMOB)))
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
		A=(Area)A.copyOf();
		A.setName(areaName);
		CMMap.AREAS.addElement(A);
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
		DBConnection D=null;
		try
		{
			D=DBConnector.DBFetch();
			D.update("DELETE FROM CMAREA WHERE CMAREA='"+A.name()+"'");
			if(DBConnector.DBConfirmDeletions)
			{
				ResultSet R=D.query("SELECT * FROM CMAREA WHERE CMAREA='"+A.name()+"'");
				if((R!=null)&&(R.next()))
					Log.errOut("DBDeleteArea","Delete Failed.");
			}
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
				room.exits()[i]=null;
				room.doors()[i]=null;
			}
			DBUpdateExits(room);

			while(room.numItems()>0)
			{
				Item thisItem=room.fetchItem(0);
				if(thisItem!=null)
				{
					thisItem.setLocation(null);
					room.delItem(thisItem);
				}
			}
			DBUpdateItems(room);

			D=DBConnector.DBFetch();
			D.update("DELETE FROM CMROOM WHERE CMROID='"+room.ID()+"'");
			if(DBConnector.DBConfirmDeletions)
			{
				ResultSet R=D.query("SELECT * FROM CMROOM WHERE CMROID='"+room.ID()+"'");
				if((R!=null)&&(R.next()))
					Log.errOut("DBDeleteRoom","Delete Failed.");
			}
			DBConnector.DBDone(D);

		}
		catch(SQLException sqle)
		{
			Log.errOut("Room","Delete"+sqle);
			if(D!=null) DBConnector.DBDone(D);
		}
	}
}