package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;
public class CMMap
{
	protected static Vector areasList = new Vector();
	protected static Vector roomsList = new Vector();
	protected static Vector playersList = new Vector();
	protected static Vector deitiesList = new Vector();
	protected static Hashtable startRooms=new Hashtable();
	protected static Hashtable deathRooms=new Hashtable();
	protected static Hashtable bodyRooms=new Hashtable();
	protected static final int QUADRANT_WIDTH=10;
	protected static Vector space=new Vector();

	private static void theWorldChanged()
	{
		for (Enumeration a=areas(); a.hasMoreElements();)
			((Area)a.nextElement()).clearMap();
	}
	// areas
	public static int numAreas() { return areasList.size(); }
	public static void addArea(Area newOne)
	{
		areasList.addElement(newOne);
	}
	public static void delArea(Area oneToDel)
	{
		areasList.remove(oneToDel);
	}
	public static Area getArea(String calledThis)
	{
		for(Enumeration a=areas();a.hasMoreElements();)
		{
			Area A=(Area)a.nextElement();
			if(A.Name().equalsIgnoreCase(calledThis))
				return A;
		}
		return null;
	}
	public static Enumeration areas(){
		return areasList.elements();
	}
	public static Area getFirstArea()
	{
		if (areas().hasMoreElements())
			return (Area) areas().nextElement();
		return null;
	}
	public static Area getRandomArea()
	{
		Area A=null;
		while((numAreas()>0)&&(A==null))
		{
			try{
				A=(Area)areasList.elementAt(Dice.roll(1,numAreas(),-1));
			}catch(Exception e){}
		}
		return A;
	}

	public static boolean isObjectInSpace(SpaceObject O){return space.contains(O);}
	public static void delObjectInSpace(SpaceObject O){	space.removeElement(O);}
	public static void addObjectToSpace(SpaceObject O){	space.addElement(O);}
	
	public static long getDistanceFrom(SpaceObject O1, SpaceObject O2)
	{
		return Math.round(Math.sqrt(Util.mul((O1.coordinates()[0]-O2.coordinates()[0]),(O1.coordinates()[0]-O2.coordinates()[0]))
									+Util.mul((O1.coordinates()[1]-O2.coordinates()[1]),(O1.coordinates()[1]-O2.coordinates()[1]))
									+Util.mul((O1.coordinates()[2]-O2.coordinates()[2]),(O1.coordinates()[2]-O2.coordinates()[2]))));
	}
	public static double[] getDirection(SpaceObject FROM, SpaceObject TO)
	{
		double[] dir=new double[2];
		double x=new Long(TO.coordinates()[0]-FROM.coordinates()[0]).doubleValue();
		double y=new Long(TO.coordinates()[1]-FROM.coordinates()[1]).doubleValue();
		double z=new Long(TO.coordinates()[2]-FROM.coordinates()[2]).doubleValue();
		dir[0]=Math.toDegrees(Math.acos(x/Math.sqrt((x*x)+(y*y))));
		dir[1]=Math.toDegrees(Math.acos(z/Math.sqrt((z*z)+(y*y))));
		return dir;
	}
	
	public static void moveSpaceObject(SpaceObject O)
	{
		double x1=Math.cos(Math.toRadians(O.direction()[0]))*Math.sin(Math.toRadians(O.direction()[1]));
		double y1=Math.sin(Math.toRadians(O.direction()[0]))*Math.sin(Math.toRadians(O.direction()[1]));
		double z1=Math.cos(O.direction()[1]);
		O.coordinates()[0]=O.coordinates()[0]+Math.round(Util.mul(O.velocity(),x1));
		O.coordinates()[1]=O.coordinates()[1]+Math.round(Util.mul(O.velocity(),y1));
		O.coordinates()[2]=O.coordinates()[2]+Math.round(Util.mul(O.velocity(),z1));
	}
	
	public static long getRelativeVelocity(SpaceObject O1, SpaceObject O2)
	{
		return Math.round(Math.sqrt(new Long(((O1.velocity()*O1.coordinates()[0])-(O2.velocity()*O2.coordinates()[0])*(O1.velocity()*O1.coordinates()[0])-(O2.velocity()*O2.coordinates()[0]))
									+((O1.velocity()*O1.coordinates()[1])-(O2.velocity()*O2.coordinates()[1])*(O1.velocity()*O1.coordinates()[1])-(O2.velocity()*O2.coordinates()[1]))
									+((O1.velocity()*O1.coordinates()[2])-(O2.velocity()*O2.coordinates()[2])*(O1.velocity()*O1.coordinates()[2])-(O2.velocity()*O2.coordinates()[2]))).doubleValue()));
	}
	
	public static String createNewExit(Room from, Room room, int direction)
	{
		Room opRoom=from.rawDoors()[direction];
		if((opRoom!=null)&&(opRoom.roomID().length()==0))
			opRoom=null;
		Room reverseRoom=null;
		if(opRoom!=null)
			reverseRoom=opRoom.rawDoors()[Directions.getOpDirectionCode(direction)];

		if((reverseRoom!=null)&&(reverseRoom==from))
			return "Opposite room already exists and heads this way.  One-way link created.";

		if(opRoom!=null)
			from.rawDoors()[direction]=null;

		from.rawDoors()[direction]=room;
		Exit thisExit=from.rawExits()[direction];
		if(thisExit==null)
		{
			thisExit=CMClass.getExit("StdOpenDoorway");
			from.rawExits()[direction]=thisExit;
		}
		CMClass.DBEngine().DBUpdateExits(from);

		if(room.rawDoors()[Directions.getOpDirectionCode(direction)]==null)
		{
			room.rawDoors()[Directions.getOpDirectionCode(direction)]=from;
			room.rawExits()[Directions.getOpDirectionCode(direction)]=thisExit;
			CMClass.DBEngine().DBUpdateExits(room);
		}
		return "";
	}

	public static String getOpenRoomID(String AreaID)
	{
		int highest=Integer.MIN_VALUE;
		int lowest=Integer.MAX_VALUE;
		Hashtable allNums=new Hashtable();
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if((R.getArea().Name().equals(AreaID))
			&&(R.roomID().startsWith(AreaID+"#")))
			{
				int newnum=Util.s_int(R.roomID().substring(AreaID.length()+1));
				if(newnum>=highest)	highest=newnum;
				if(newnum<=lowest) lowest=newnum;
				allNums.put(new Integer(newnum),R);
			}
		}
		if((highest<0)&&(getRoom(AreaID+"#0"))==null)
			return AreaID+"#0";
		if(lowest>highest) lowest=highest+1;
		for(int i=lowest;i<=highest+1000;i++)
		{
			if((!allNums.containsKey(new Integer(i)))
			&&(getRoom(AreaID+"#"+i)==null))
				return AreaID+"#"+i;
		}
		return AreaID+"#"+Math.random();
	}


	public static int numRooms() { return roomsList.size(); }
	public static void addRoom(Room newOne)
	{
		roomsList.addElement(newOne);
		theWorldChanged();
	}
	public static void delRoom(Room oneToDel)
	{
		if(oneToDel instanceof GridLocale)
			((GridLocale)oneToDel).clearGrid();
		roomsList.remove(oneToDel);
		theWorldChanged();
	}

	public static String getExtendedRoomID(Room R)
	{
		if(R==null) return "";
		if(R.roomID().length()>0) return R.roomID();
		Area A=R.getArea();
		if(A==null) return "";
		for(Enumeration e=A.getMap();e.hasMoreElements();)
		{
			Room anyR=(Room)e.nextElement();
			if((anyR instanceof GridLocale)
			&&(((GridLocale)anyR).isMyChild(R)))
				return ((GridLocale)anyR).getChildCode(R);
		}
		return R.roomID();
	}

	public static Room getRoom(String calledThis)
	{
		Room R = null;
		if(calledThis==null) return null;
		if(calledThis.endsWith(")"))
		{
			int child=calledThis.lastIndexOf("#(");
			if(child>1)
			{
				R=getRoom(calledThis.substring(0,child));
				if((R!=null)&&(R instanceof GridLocale))
					R=((GridLocale)R).getChild(calledThis);
				else
					R=null;
			}
		}
		if(R!=null) return R;
		for (Enumeration i=rooms(); i.hasMoreElements();)
		{
			R = (Room)i.nextElement();
			if (R.roomID().equalsIgnoreCase(calledThis))
				return R;
		}
		return null;
	}
	public static Enumeration rooms() {
		return roomsList.elements();
	}
	public static void replaceRoom(Room newOne, Room oldOne)
	{
		if(oldOne instanceof GridLocale)
		  ((GridLocale)oldOne).clearGrid();
		roomsList.remove(oldOne);
		roomsList.addElement(newOne);
		theWorldChanged();
	}
	public static Room getFirstRoom()
	{
		if (rooms().hasMoreElements())
			return (Room) rooms().nextElement();
		return null;
	}
	public static Room getRandomRoom()
	{
		Room R=null;
		while((numRooms()>0)&&(R==null))
		{
			try{
				R=(Room)roomsList.elementAt(Dice.roll(1,numRooms(),-1));
			}catch(Exception e){}
		}
		return R;
	}

	public static int numDeities() { return deitiesList.size(); }
	public static void addDeity(Deity newOne)
	{
		if (!deitiesList.contains(newOne))
			deitiesList.add(newOne);
	}
	public static void delDeity(Deity oneToDel)
	{
		deitiesList.remove(oneToDel);
	}
	public static Deity getDeity(String calledThis)
	{
		Deity D = null;
		for (Enumeration i=deities(); i.hasMoreElements();)
		{
			D = (Deity)i.nextElement();
			if (D.Name().equalsIgnoreCase(calledThis))
				return D;
		}
		return null;
	}
	public static Enumeration deities() { return deitiesList.elements(); }

	public static int numPlayers() { return playersList.size(); }
	public static void addPlayer(MOB newOne) { playersList.add(newOne); }
	public static void delPlayer(MOB oneToDel) { playersList.remove(oneToDel); }
	public static MOB getPlayer(String calledThis)
	{
		MOB M = null;

		for (Enumeration p=players(); p.hasMoreElements();)
		{
			M = (MOB)p.nextElement();
			if (M.Name().equalsIgnoreCase(calledThis))
				return M;
		}
		return null;
	}

	public static MOB getLoadPlayer(String last)
	{
		if(!CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED))
			return null;

		MOB M=getPlayer(last);
		if(M!=null) return M;

		for(Enumeration p=players();p.hasMoreElements();)
		{
			MOB mob2=(MOB)p.nextElement();
			if(mob2.Name().equalsIgnoreCase(last))
			{ return mob2;}
		}

		MOB TM=CMClass.getMOB("StdMOB");
		if(CMClass.DBEngine().DBUserSearch(TM,last))
		{
			M=CMClass.getMOB("StdMOB");
			M.setName(TM.Name());
			CMClass.DBEngine().DBReadMOB(M);
			CMClass.DBEngine().DBReadFollowers(M,false);
			if(M.playerStats()!=null)
				M.playerStats().setUpdated(M.playerStats().lastDateTime());
			M.recoverEnvStats();
			M.recoverCharStats();
		}
		return M;
	}

	public static Enumeration players() { return playersList.elements(); }

	public static Room getStartRoom(MOB mob)
	{
		String race=mob.baseCharStats().getMyRace().racialCategory().toUpperCase();
		race.replace(' ','_');
		String deity=mob.getWorshipCharID().toUpperCase();
		deity.replace(' ','_');
		String align=CommonStrings.shortAlignmentStr(mob.getAlignment()).toUpperCase();
		String roomID=(String)startRooms.get(race);
		if((roomID==null)||(roomID.length()==0))
			roomID=(String)startRooms.get(align);
		if(((roomID==null)||(roomID.length()==0))&&(deity.length()>0))
			roomID=(String)startRooms.get(deity);
		if((roomID==null)||(roomID.length()==0))
			roomID=(String)startRooms.get("ALL");

		Room room=null;
		if((roomID!=null)&&(roomID.length()>0))
			room=getRoom(roomID);
		if(room==null)
			room=getRoom("START");
		if((room==null)&&(numRooms()>0))
			room=getFirstRoom();
		return room;
	}

	public static Room getDeathRoom(MOB mob)
	{
		String race=mob.baseCharStats().getMyRace().racialCategory().toUpperCase();
		race.replace(' ','_');
		String deity=mob.getWorshipCharID().toUpperCase();
		deity.replace(' ','_');
		String align=CommonStrings.shortAlignmentStr(mob.getAlignment()).toUpperCase();
		String roomID=(String)deathRooms.get(race);
		if((roomID==null)||(roomID.length()==0))
			roomID=(String)deathRooms.get(align);
		if(((roomID==null)||(roomID.length()==0))&&(deity.length()>0))
			roomID=(String)deathRooms.get(deity);
		if((roomID==null)||(roomID.length()==0))
			roomID=(String)deathRooms.get("ALL");

		Room room=null;
		if((roomID!=null)&&(roomID.equalsIgnoreCase("START")))
			room=mob.getStartRoom();
		if((room==null)&&(roomID!=null)&&(roomID.length()>0))
			room=getRoom(roomID);
		if(room==null)
			room=mob.getStartRoom();
		if((room==null)&&(numRooms()>0))
			room=getFirstRoom();
		return room;
	}

	public static Room getBodyRoom(MOB mob)
	{
		String race=mob.baseCharStats().getMyRace().racialCategory().toUpperCase();
		race.replace(' ','_');
		String deity=mob.getWorshipCharID().toUpperCase();
		deity.replace(' ','_');
		String align=CommonStrings.shortAlignmentStr(mob.getAlignment()).toUpperCase();
		String roomID=(String)bodyRooms.get(race);
		if((roomID==null)||(roomID.length()==0))
			roomID=(String)bodyRooms.get(align);
		if(((roomID==null)||(roomID.length()==0))&&(deity.length()>0))
			roomID=(String)bodyRooms.get(deity);
		if((roomID==null)||(roomID.length()==0))
			roomID=(String)bodyRooms.get("ALL");

		Room room=null;
		if((roomID!=null)&&(roomID.equalsIgnoreCase("START")))
			room=mob.location();
		if((room==null)&&(roomID!=null)&&(roomID.length()>0))
			room=getRoom(roomID);
		if(room==null)
			room=mob.location();
		if((room==null)&&(numRooms()>0))
			room=getFirstRoom();
		return room;
	}

	private static void pageRooms(INI page, Hashtable table, String start)
	{
		for(Enumeration i=page.keys();i.hasMoreElements();)
		{
			String k=(String)i.nextElement();
			if(k.startsWith(start+"_"))
				table.put(k.substring(start.length()+1),page.getProperty(k));
		}
		String thisOne=page.getProperty(start);
		if((thisOne!=null)&&(thisOne.length()>0))
			table.put("ALL",thisOne);
	}

	public static void initStartRooms(INI page)
	{
		startRooms=new Hashtable();
		pageRooms(page,startRooms,"START");
	}

	public static void initDeathRooms(INI page)
	{
		deathRooms=new Hashtable();
		pageRooms(page,deathRooms,"DEATH");
	}

	public static void initBodyRooms(INI page)
	{
		bodyRooms=new Hashtable();
		pageRooms(page,bodyRooms,"MORGUE");
	}

	public static void renameRooms(Area A, String oldName, Vector allMyDamnRooms)
	{
		Vector onesToRenumber=new Vector();
		for(int r=0;r<allMyDamnRooms.size();r++)
		{
			Room R=(Room)allMyDamnRooms.elementAt(r);
			R.setArea(A);
			if(oldName!=null)
			{
				if(R.roomID().startsWith(oldName+"#"))
				{
					Room R2=CMMap.getRoom(A.Name()+"#"+R.roomID().substring(oldName.length()+1));
					if((R2==null)||(!R2.roomID().startsWith(A.Name()+"#")))
					{
						String oldID=R.roomID();
						R.setRoomID(A.Name()+"#"+R.roomID().substring(oldName.length()+1));
						CMClass.DBEngine().DBReCreate(R,oldID);
					}
					else
						onesToRenumber.addElement(R);
				}
				else
					CMClass.DBEngine().DBUpdateRoom(R);
			}
		}
		A.clearMap();
		if(oldName!=null)
		{
			for(int r=0;r<onesToRenumber.size();r++)
			{
				Room R=(Room)onesToRenumber.elementAt(r);
				String oldID=R.roomID();
				R.setRoomID(CMMap.getOpenRoomID(A.Name()));
				CMClass.DBEngine().DBReCreate(R,oldID);
			}
		}
	}

	public static void unLoad()
	{
		areasList.clear();
		roomsList.clear();
		deitiesList.clear();
		playersList.clear();
		space=new Vector();
		bodyRooms=new Hashtable();
		startRooms=new Hashtable();
		deathRooms=new Hashtable();
	}
}
