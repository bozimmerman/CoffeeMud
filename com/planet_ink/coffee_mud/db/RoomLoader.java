package com.planet_ink.coffee_mud.db;

import java.sql.*;
import java.util.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.Locales.*;
public class RoomLoader
{
	public static void DBRead(Vector h)
	{
		DBConnection D=null;
		try
		{
			D=MUD.DBs.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMROOM");
			while(R.next())
			{
				String roomID=DBConnections.getRes(R,"CMROID");
				String localeID=DBConnections.getRes(R,"CMLOID");
				Room newRoom=(Room)MUD.getLocale(localeID);
				if(newRoom==null)
					Log.errOut("Room","Couldn't load room '"+roomID+"', localeID '"+localeID+"'.");
				else
				{
					newRoom=(Room)newRoom.newInstance();
					newRoom.setID(roomID);
					newRoom.setAreaID(DBConnections.getRes(R,"CMAREA"));
					newRoom.setDisplayText(DBConnections.getRes(R,"CMDESC1"));
					newRoom.setDescription(DBConnections.getRes(R,"CMDESC2"));
					h.addElement(newRoom);
				}
			}
			MUD.DBs.DBDone(D);					
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
			if(D!=null) MUD.DBs.DBDone(D);
			return;
		}
		
		// now grab the exits
		try
		{
			D=MUD.DBs.DBFetch();
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
					Exit newExit=MUD.getExit(exitID);
					Room newRoom=MUD.getRoom(nextRoomID);
					if((newExit==null)&&(newRoom==null))
						Log.errOut("Room","Couldn't find room '"+nextRoomID+"', exit type '"+exitID+"', direction: "+direction);
					else
					{
						if(newExit!=null)
						{
							newExit=(Exit)newExit.newInstance();
							newExit.setMiscText(exitMiscText);
							thisRoom.exits()[direction]=newExit;
						}
						if(newRoom!=null)
							thisRoom.doors()[direction]=newRoom;
					}
										
				}
			}
			
			// now make all exits consistant (if possible)!
			for(int i=0;i<h.size();i++)
			{
				Room thisRoom=(Room)h.elementAt(i);
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				{
					Room opRoom=(Room)thisRoom.getRoom(d);
					if(opRoom!=null)
					{
						Exit myExit=thisRoom.getExit(d);
						Exit opExit=opRoom.getExit(Directions.getOpDirectionCode(d));
						if((myExit!=null)
						&&(opExit!=null)
						&&(myExit!=opExit)
						&&(myExit.ID().equals(opExit.ID()))
						&&(myExit.text().equals(opExit.text())))
							thisRoom.exits()[d]=opExit;
					}
				}
				
			}
			MUD.DBs.DBDone(D);					
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
			if(D!=null) MUD.DBs.DBDone(D);
		}
		for(int m=0;m<h.size();m++)
		{
			Room thisRoom=(Room)h.elementAt(m);
			DBReadContent(thisRoom);
			thisRoom.startItemRejuv();
			thisRoom.recoverRoomStats();
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
			D=MUD.DBs.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMROIT WHERE CMROID='"+thisRoom.ID()+"' order by CMITNM");
			Hashtable itemNums=new Hashtable();
			while(R.next())
			{
				int itemNumber=(int)DBConnections.getLongRes(R,"CMITNM");
				String itemID=DBConnections.getRes(R,"CMITID");
				Item newItem=(Item)MUD.getItem(itemID);
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
			MUD.DBs.DBDone(D);					
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
			if(D!=null) MUD.DBs.DBDone(D);
		}
		
		// now grab the inhabitants
		try
		{
			D=MUD.DBs.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMROCH WHERE CMROID='"+thisRoom.ID()+"' order by CMCHNM");
			while(R.next())
			{
				String MOBID=DBConnections.getRes(R,"CMCHID");
				MOB newMOB=(MOB)MUD.getMOB(MOBID);
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
					newMOB.recoverMaxState();
					newMOB.recoverEnvStats();
					newMOB.bringToLife(thisRoom);
				}
			}
			MUD.DBs.DBDone(D);					
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
			if(D!=null) MUD.DBs.DBDone(D);
		}
	}
	
	private static int DBUpdateContents(Room room, 
										Item item, 
										int itemNumber)
		throws SQLException
	{
		DBConnection D=null;
		int newItemNumber=itemNumber;
		try
		{
			for(int i=0;i<room.numItems();i++)
			{
				Item thisItem=room.fetchItem(i);
				if(thisItem.location()==item)
				{
					D=MUD.DBs.DBFetch();
					String sql=
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
					MUD.DBs.DBDone(D);
					newItemNumber=DBUpdateContents(room,thisItem,newItemNumber);
				}
			}
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room","UpdateItems"+sqle);
			if(D!=null) MUD.DBs.DBDone(D);
		}
		return newItemNumber;
	}
	
	public static void DBUpdateItems(Room room)
	{
		if(room.ID().length()==0) return;
		DBConnection D=null;
		try
		{
			D=MUD.DBs.DBFetch();
			D.update("DELETE FROM CMROIT WHERE CMROID='"+room.ID()+"'");
			if(MUD.DBConfirmDeletions)
			{
				ResultSet R=D.query("SELECT * FROM CMROIT WHERE CMROID='"+room.ID()+"'");
				if(R.next())
					Log.errOut("DBUpdateItems","Delete Failed.");
			}
			MUD.DBs.DBDone(D);
			DBUpdateContents(room,null,-1);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room","UpdateItems"+sqle);
			if(D!=null) MUD.DBs.DBDone(D);
		}
	}
	
	public static void DBUpdateExits(Room room)
	{
		if(room.ID().length()==0) return;
		DBConnection D=null;
		try
		{
			D=MUD.DBs.DBFetch();
			D.update("DELETE FROM CMROEX WHERE CMROID='"+room.ID()+"'");
			if(MUD.DBConfirmDeletions)
			{
				ResultSet R=D.query("SELECT * FROM CMROEX WHERE CMROID='"+room.ID()+"'");
				if(R.next())
					Log.errOut("DBUpdateExits","Delete Failed.");
			}
			MUD.DBs.DBDone(D);
			for(int e=0;e<Directions.NUM_DIRECTIONS;e++)
			{
				Exit thisExit=room.getExit(e);
				Room thisRoom=room.getRoom(e);
				if((thisRoom!=null)&&(thisRoom.ID().length()>0))
				{
					D=MUD.DBs.DBFetch();
					D.update("INSERT INTO CMROEX ("
							 +"CMROID, "
							 +"CMDIRE, "
							 +"CMEXID, "
							 +"CMEXTX, "
							 +"CMNRID"
							 +") values ("
							 +"'"+room.ID()+"',"
							 +e+","
							 +"'"+Util.id(thisExit)+"',"
							 +"'"+((thisExit==null)?" ":thisExit.text())+" ',"
							 +"'"+Util.id(thisRoom)+"')");
					MUD.DBs.DBDone(D);
				}
			}
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room","UpdateExits"+sqle);
			if(D!=null) MUD.DBs.DBDone(D);
		}
	}
	
	public static boolean isEligibleMonster(MOB mob)
	{
		if(!mob.isMonster())
			return false;
		MOB followed=mob.amFollowing();
		if(followed!=null)
			if(!followed.isMonster())
				return false;
		return true;
		
	}
	
	
	public static void DBUpdateMOBs(Room room)
	{
		if(room.ID().length()==0) return;
		DBConnection D=null;
		try
		{
			D=MUD.DBs.DBFetch();
			D.update("DELETE FROM CMROCH WHERE CMROID='"+room.ID()+"'");
			if(MUD.DBConfirmDeletions)
			{
				ResultSet R=D.query("SELECT * FROM CMROCH WHERE CMROID='"+room.ID()+"'");
				if(R.next())
					Log.errOut("DBUpdateMOBs","Delete Failed.");
			}
			MUD.DBs.DBDone(D);
			for(int m=0;m<room.numInhabitants();m++)
			{
				MOB thisMOB=room.fetchInhabitant(m);
				
				if(isEligibleMonster(thisMOB))
				{
					D=MUD.DBs.DBFetch();
					D.update("INSERT INTO CMROCH ("
							 +"CMROID, "
							 +"CMCHNM, "
							 +"CMCHID, "
							 +"CMCHTX, "
							 +"CMCHLV, "
							 +"CMCHAB, "
							 +"CMCHRE"
							 +") values ("
							 +"'"+room.ID()+"',"
							 +m+","
							 +"'"+INI.className(thisMOB)+"',"
							 +"'"+thisMOB.text()+" ',"
							 +thisMOB.baseEnvStats().level()+","
							 +thisMOB.baseEnvStats().ability()+","
							 +thisMOB.baseEnvStats().rejuv()
							 +")");
					MUD.DBs.DBDone(D);
				}
			}
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room","UpdateMOBs"+sqle);
			if(D!=null) MUD.DBs.DBDone(D);
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
			D=MUD.DBs.DBFetch();
			D.update("UPDATE CMROOM SET "
					+"CMAREA='"+room.getAreaID()+"',"
					+"CMDESC1='"+room.displayText()+" ',"
					+"CMDESC2='"+room.description()+" '"
					+"WHERE CMROID='"+room.ID()+"'");
			MUD.DBs.DBDone(D);					
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
			if(D!=null) MUD.DBs.DBDone(D);
			return;
		}
	}
	
	
	public static void DBReCreate(Room room, String oldID)
	{
		if(room.ID().length()==0) return;
		DBConnection D=null;
		try
		{
			D=MUD.DBs.DBFetch();
			D.update("UPDATE CMROOM SET "
					+"CMROID='"+room.ID()+"', "
					+"CMAREA='"+room.getAreaID()+"' "
					+"WHERE CMROID='"+oldID+"'");
			MUD.DBs.DBDone(D);
			
			D=MUD.DBs.DBFetch();
			D.update("UPDATE CMROCH SET "
					+"CMROID='"+room.ID()+"' "
					+"WHERE CMROID='"+oldID+"'");
			MUD.DBs.DBDone(D);
			
			D=MUD.DBs.DBFetch();
			D.update("UPDATE CMROEX SET "
					+"CMROID='"+room.ID()+"' "
					+"WHERE CMROID='"+oldID+"'");
			MUD.DBs.DBDone(D);
			
			D=MUD.DBs.DBFetch();
			D.update("UPDATE CMROIT SET "
					+"CMROID='"+room.ID()+"' "
					+"WHERE CMROID='"+oldID+"'");
			MUD.DBs.DBDone(D);
			
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
			if(D!=null) MUD.DBs.DBDone(D);
			return;
		}
	}
	
	
	public static void DBCreate(Room room, String LocaleID)
	{
		if(room.ID().length()==0) return;
		DBConnection D=null;
		try
		{
			D=MUD.DBs.DBFetch();
			D.update("INSERT INTO CMROOM ("
					+"CMROID,"
					+"CMLOID,"
					+"CMAREA,"
					+"CMDESC1,"
					+"CMDESC2 "
					+") values ("
					+"'"+room.ID()+"',"
					+"'"+LocaleID+"',"
					+"'"+room.getAreaID()+"',"
					+"'"+room.displayText()+" ',"
					+"'"+room.description()+" ')");
			MUD.DBs.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
			if(D!=null) MUD.DBs.DBDone(D);
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
				room.delInhabitant(room.fetchInhabitant(0));
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
				thisItem.setLocation(null);
				room.delItem(thisItem);
			}
			DBUpdateItems(room);
			
			D=MUD.DBs.DBFetch();
			D.update("DELETE FROM CMROOM WHERE CMROID='"+room.ID()+"'");
			if(MUD.DBConfirmDeletions)
			{
				ResultSet R=D.query("SELECT * FROM CMROOM WHERE CMROID='"+room.ID()+"'");
				if(R.next())
					Log.errOut("DBDeleteRoom","Delete Failed.");
			}
			MUD.DBs.DBDone(D);
			
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room","Delete"+sqle);
			if(D!=null) MUD.DBs.DBDone(D);
		}
	}
}