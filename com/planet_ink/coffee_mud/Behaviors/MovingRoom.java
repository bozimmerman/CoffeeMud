package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.*;

public class MovingRoom extends ActiveTicker
{
	public String ID(){return "MovingRoom";}
	protected int canImproveCode(){return Behavior.CAN_ROOMS;}

	public Vector listOfRooms=new Vector();
	protected Vector roomInfos=new Vector();
	public Vector messageInfo=new Vector();
	public Vector mapInfo=new Vector();
	protected Vector stubs=new Vector();
	protected String xmlInfo="";
        private int currentStop = 0;
        private int nextStop = 0;
        private int currentStatus = 1;
        private boolean isReversed = false;
		private String savedStringInside = "";
        private String savedStringOutside = "";

	public MovingRoom()
	{
		super();
		minTicks=10;maxTicks=30;chance=100;
		tickReset();
	}

	public Behavior newInstance()
	{
		return new MovingRoom();
	}

	public void setParms(String newParms)
	{
		String myParms=newParms;
		roomInfos=new Vector();
		char c=';';
		int x=myParms.indexOf(c);
		if(x<0){ c='/'; x=myParms.indexOf(c);}
		if(x>0)
		{
			String parmText=myParms.substring(0,x);
			myParms=myParms.substring(x+1);
			super.setParms(parmText);
		}
		roomInfos=Util.parseSemicolons(myParms);
		parms=newParms;
		xmlInfo=loadInfo();
		parseMovingXML(xmlInfo);
	}

	private static synchronized String loadInfo()
	{
		StringBuffer str=new StringBuffer("");//Resources.getFile("resources"+File.separatorChar+"Moving.xml");
		try
		{
			FileReader F=new FileReader("resources"+File.separatorChar+"movingroom.xml");
			BufferedReader reader=new BufferedReader(F);
			String line="";
			while((line!=null)&&(reader.ready()))
			{
				line=reader.readLine();
				if(line!=null)
					str.append(line);
			}
			F.close();
		}
		catch(Exception e)
		{
			Log.errOut("MovingRoom",e.getMessage());
			return null;
		}
		String theString = str.toString();
		return theString;
	}

	private void parseMovingXML(String roomToParse)
	{
		Vector V = new Vector();
		String theFullBlock=XMLManager.returnXMLBlock(roomToParse, roomInfos.elementAt(0).toString().toUpperCase());
		String theStopsBlock=XMLManager.returnXMLBlock(theFullBlock, "STOPS");
		String theNormalDirBlock=XMLManager.returnXMLBlock(theFullBlock, "NORMALDIRECTION");
		String theReverseDirBlock=XMLManager.returnXMLBlock(theFullBlock, "REVERSEDIRECTION");
		String theDescriptionsBlock=XMLManager.returnXMLBlock(theFullBlock, "ROOMDESCRIPTIONS");
		int x=1;
		String thisone=XMLManager.returnXMLValue(theStopsBlock, "STOP1");
		while (thisone!="") {
			++x;
			listOfRooms.addElement(thisone);
			thisone=XMLManager.returnXMLValue(theStopsBlock, "STOP"+x);
		}
		V.addElement(XMLManager.returnXMLValue(theNormalDirBlock, "TRAVELDIRECTION"));
		V.addElement(XMLManager.returnXMLValue(theNormalDirBlock, "DOORSDIRECTION"));
		V.addElement(XMLManager.returnXMLValue(XMLManager.returnXMLBlock(theNormalDirBlock, "INSIDE"), "ARRIVALINFO"));
		V.addElement(XMLManager.returnXMLValue(XMLManager.returnXMLBlock(theNormalDirBlock, "INSIDE"), "DEPARTINFO"));
		V.addElement(XMLManager.returnXMLValue(XMLManager.returnXMLBlock(theNormalDirBlock, "OUTSIDE"), "ARRIVALINFO"));
		V.addElement(XMLManager.returnXMLValue(XMLManager.returnXMLBlock(theNormalDirBlock, "OUTSIDE"), "DEPARTINFO"));
		messageInfo.addElement(new Vector(V));
		V.removeAllElements();
		V.addElement(XMLManager.returnXMLValue(theReverseDirBlock, "TRAVELDIRECTION"));
		V.addElement(XMLManager.returnXMLValue(theReverseDirBlock, "DOORSDIRECTION"));
		V.addElement(XMLManager.returnXMLValue(XMLManager.returnXMLBlock(theReverseDirBlock, "INSIDE"), "ARRIVALINFO"));
		V.addElement(XMLManager.returnXMLValue(XMLManager.returnXMLBlock(theReverseDirBlock, "INSIDE"), "DEPARTINFO"));
		V.addElement(XMLManager.returnXMLValue(XMLManager.returnXMLBlock(theReverseDirBlock, "OUTSIDE"), "ARRIVALINFO"));
		V.addElement(XMLManager.returnXMLValue(XMLManager.returnXMLBlock(theReverseDirBlock, "OUTSIDE"), "DEPARTINFO"));
		messageInfo.addElement(new Vector(V));
		V.removeAllElements();
		String theNormalDescBlock = (XMLManager.returnXMLBlock(theDescriptionsBlock, "NORMALDIRECTION"));
		String theReverseDescBlock = (XMLManager.returnXMLBlock(theDescriptionsBlock, "REVERSEDIRECTION"));
		V.addElement(XMLManager.returnXMLValue(XMLManager.returnXMLBlock(theNormalDescBlock, "INSIDE"), "DOOROPENED"));
		V.addElement(XMLManager.returnXMLValue(XMLManager.returnXMLBlock(theNormalDescBlock, "INSIDE"), "DOORCLOSED"));
		V.addElement(XMLManager.returnXMLValue(XMLManager.returnXMLBlock(theNormalDescBlock, "OUTSIDE"), "DOOROPENED"));
		V.addElement(XMLManager.returnXMLValue(XMLManager.returnXMLBlock(theNormalDescBlock, "OUTSIDE"), "DOORCLOSED"));
		V.addElement(XMLManager.returnXMLValue(XMLManager.returnXMLBlock(theReverseDescBlock, "INSIDE"), "DOOROPENED"));
		V.addElement(XMLManager.returnXMLValue(XMLManager.returnXMLBlock(theReverseDescBlock, "INSIDE"), "DOORCLOSED"));
		V.addElement(XMLManager.returnXMLValue(XMLManager.returnXMLBlock(theReverseDescBlock, "OUTSIDE"), "DOOROPENED"));
		V.addElement(XMLManager.returnXMLValue(XMLManager.returnXMLBlock(theReverseDescBlock, "OUTSIDE"), "DOORCLOSED"));
		messageInfo.addElement(new Vector(V));
		V.removeAllElements();
		mapInfo.addElement(XMLManager.returnXMLValue(theFullBlock, "ROOMPRINTNAME"));
		mapInfo.addElement(XMLManager.returnXMLValue(theFullBlock, "LINEPRINTNAME"));
		mapInfo.addElement(XMLManager.returnXMLValue(theFullBlock, "DISPLOC"));
	}
	private String fixOutputString(String incoming, Room aRoom)
	{
		String repWord="";
		incoming = " " + incoming;
		int i=0;
		if (incoming.indexOf("$disproom")>0) {
			i = incoming.indexOf("$disproom");
			repWord=incoming.substring(1,i)+aRoom.displayText()+incoming.substring(i+9);
		} else if (incoming.indexOf("$traveldir")>0) {
			i = incoming.indexOf("$traveldir");
			int pos=listOfRooms.indexOf(CMMap.getExtendedRoomID(aRoom));
			boolean revDirName=false;
			if (((pos==0)||(pos==listOfRooms.size()-1))&&(currentStatus==1))
				revDirName=true;
			Vector V=new Vector();
			if (!revDirName)
			{
				if (isReversed)
					V=(Vector)messageInfo.elementAt(1);
				else
					V=(Vector)messageInfo.elementAt(0);
			}
			else
			{
				if (isReversed)
					V=(Vector)messageInfo.elementAt(0);
				else
					V=(Vector)messageInfo.elementAt(1);
			}
			repWord=incoming.substring(1,i)+V.elementAt(0).toString()+incoming.substring(i+10);
		} else if (incoming.indexOf("$outopendir")>0) {
			i = incoming.indexOf("$outopendir");
			Vector V=new Vector();
			if (isReversed)
				V=(Vector)messageInfo.elementAt(1);
			else
				V=(Vector)messageInfo.elementAt(0);
			repWord=incoming.substring(1,i)+Directions.getDirectionName(Directions.getOpDirectionCode(V.elementAt(1).toString()))+incoming.substring(i+11);
		} else if (incoming.indexOf("$inopendir")>0) {
			i = incoming.indexOf("$inopendir");
			Vector V=new Vector();
			if (isReversed)
				V=(Vector)messageInfo.elementAt(1);
			else
				V=(Vector)messageInfo.elementAt(0);
			repWord=incoming.substring(1,i)+V.elementAt(1).toString()+incoming.substring(i+10);
		} else {
			repWord=incoming.substring(1);
			return repWord;
		}
		repWord = fixOutputString(repWord,aRoom);
		return repWord;
	}
	private void removeStubs(Room aRoom,Room bRoom)
	{
		if (!stubs.isEmpty()){
			int i=0;
			int j=0;
			if (aRoom.description().indexOf(stubs.elementAt(0).toString())>0)
			{
				i = aRoom.description().indexOf(stubs.elementAt(0).toString());
				aRoom.setDescription(aRoom.description().substring(0,i).trim());
			}
			if (bRoom.description().indexOf(stubs.elementAt(1).toString())>0)
			{
				j = bRoom.description().indexOf(stubs.elementAt(1).toString());
				bRoom.setDescription(bRoom.description().substring(0,j).trim());
			}
		}
	}
	public boolean tick(Tickable ticking, int tickID)
	{
		Vector normalVec = (Vector)messageInfo.elementAt(0);
		Vector reverseVec = (Vector)messageInfo.elementAt(1);
		Vector theDescriptions = (Vector)messageInfo.elementAt(2);
		super.tick(ticking,tickID);
		if(canAct(ticking,tickID))
		{
			Room aRoom=this.getBehaversRoom(ticking);
			if (currentStop==0) 
			{
                isReversed=false;
                nextStop=currentStop+1;
            } else if (currentStop==(listOfRooms.size()-1)) {
                isReversed=true;
                nextStop=currentStop-1;
            } else if (isReversed) {
                nextStop=currentStop-1;
            } else {
                nextStop=currentStop+1;
			}
		    String currentStopS=(String)listOfRooms.elementAt(currentStop);
                    String nextStopS=(String)listOfRooms.elementAt(nextStop);
		    if(ticking instanceof Room)
			{
                Room firstRoom = null;
                Room secondRoom = null;
                Room thisRooma = null;
				for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
				{
					thisRooma=(Room)r.nextElement();
					if(thisRooma.roomID().equalsIgnoreCase(currentStopS))
					{
						firstRoom = thisRooma;
                        break;
					}
				}
                Room thisRoomb = null;
				for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
				{
					thisRoomb=(Room)r.nextElement();
					if(thisRoomb.ID().equalsIgnoreCase(nextStopS))
					{
					   secondRoom = thisRoomb;
					   break;
					}
				}
				if (currentStatus==0) 
				{
					//DEPARTING
					if (firstRoom == null)
						firstRoom=this.getBehaversRoom(ticking);
					if (secondRoom == null)
						secondRoom=this.getBehaversRoom(ticking);
					if (thisRooma == null)
						thisRooma=this.getBehaversRoom(ticking);
					if (isReversed)
					{
						firstRoom.showHappens(Affect.MSG_OK_ACTION,fixOutputString(reverseVec.elementAt(5).toString(),secondRoom));
						aRoom.showHappens(Affect.MSG_OK_ACTION,fixOutputString(reverseVec.elementAt(3).toString(),secondRoom));
						if (((aRoom.rawDoors()[Directions.getGoodDirectionCode(normalVec.elementAt(1).toString())]!=null) && ((currentStop==0) || (currentStop==(listOfRooms.size()-1))))||(aRoom.rawDoors()[Directions.getGoodDirectionCode(reverseVec.elementAt(1).toString())]!=null))
						{
							if ((currentStop==0)||(currentStop==(listOfRooms.size()-1)))
							{
								firstRoom.rawDoors()[Directions.getOpDirectionCode(normalVec.elementAt(1).toString())]=null;
								firstRoom.rawExits()[Directions.getOpDirectionCode(normalVec.elementAt(1).toString())]=null;
								aRoom.rawDoors()[Directions.getGoodDirectionCode(normalVec.elementAt(1).toString())]=null;
								aRoom.rawExits()[Directions.getGoodDirectionCode(normalVec.elementAt(1).toString())]=null;
							} else {
								firstRoom.rawDoors()[Directions.getOpDirectionCode(reverseVec.elementAt(1).toString())]=null;
								firstRoom.rawExits()[Directions.getOpDirectionCode(reverseVec.elementAt(1).toString())]=null;
								aRoom.rawDoors()[Directions.getGoodDirectionCode(reverseVec.elementAt(1).toString())]=null;
								aRoom.rawExits()[Directions.getGoodDirectionCode(reverseVec.elementAt(1).toString())]=null;
							}
							ExternalPlay.DBUpdateExits(aRoom);
							ExternalPlay.DBUpdateExits(firstRoom);
							aRoom.getArea().fillInAreaRoom(aRoom);
							secondRoom.getArea().fillInAreaRoom(firstRoom);
							removeStubs(aRoom,firstRoom);
							stubs.removeAllElements();
							stubs.addElement(fixOutputString(theDescriptions.elementAt(5).toString(),secondRoom));
							stubs.addElement(fixOutputString(theDescriptions.elementAt(7).toString(),secondRoom));
                        	        		aRoom.setDescription(aRoom.description()+"  "+fixOutputString(theDescriptions.elementAt(5).toString(),secondRoom));
							firstRoom.setDescription(firstRoom.description()+"  "+fixOutputString(theDescriptions.elementAt(7).toString(),secondRoom));
						} else {
							Log.errOut("MovingRoom","Previous room links exists, "+aRoom.roomID()+" "+firstRoom.roomID()+" not unlinking exit.");
						}
						super.setParms("min="+roomInfos.elementAt(2)+" max="+roomInfos.elementAt(2)+" chance=100;"+roomInfos.elementAt(0)+";"+roomInfos.elementAt(1)+";"+roomInfos.elementAt(2));
						currentStatus=1;
				  	} else {
						firstRoom.showHappens(Affect.MSG_OK_ACTION,fixOutputString(normalVec.elementAt(5).toString(),secondRoom));
						aRoom.showHappens(Affect.MSG_OK_ACTION,fixOutputString(normalVec.elementAt(3).toString(),secondRoom));
						if (((aRoom.rawDoors()[Directions.getGoodDirectionCode(reverseVec.elementAt(1).toString())]!=null) && ((currentStop==0) || (currentStop==(listOfRooms.size()-1))))||(aRoom.rawDoors()[Directions.getGoodDirectionCode(normalVec.elementAt(1).toString())]!=null))
						{
							if ((currentStop==0)||(currentStop==(listOfRooms.size()-1))) {
								firstRoom.rawDoors()[Directions.getOpDirectionCode(reverseVec.elementAt(1).toString())]=null;
								firstRoom.rawExits()[Directions.getOpDirectionCode(reverseVec.elementAt(1).toString())]=null;
								aRoom.rawDoors()[Directions.getGoodDirectionCode(reverseVec.elementAt(1).toString())]=null;
								aRoom.rawExits()[Directions.getGoodDirectionCode(reverseVec.elementAt(1).toString())]=null;
							} else {
								firstRoom.rawDoors()[Directions.getOpDirectionCode(normalVec.elementAt(1).toString())]=null;
								firstRoom.rawExits()[Directions.getOpDirectionCode(normalVec.elementAt(1).toString())]=null;
								aRoom.rawDoors()[Directions.getGoodDirectionCode(normalVec.elementAt(1).toString())]=null;
								aRoom.rawExits()[Directions.getGoodDirectionCode(normalVec.elementAt(1).toString())]=null;
							}
							ExternalPlay.DBUpdateExits(aRoom);
							ExternalPlay.DBUpdateExits(firstRoom);
							aRoom.getArea().fillInAreaRoom(aRoom);
							secondRoom.getArea().fillInAreaRoom(firstRoom);
							removeStubs(aRoom,firstRoom);
							stubs.removeAllElements();
							stubs.addElement(fixOutputString(theDescriptions.elementAt(1).toString(),secondRoom));
							stubs.addElement(fixOutputString(theDescriptions.elementAt(3).toString(),secondRoom));
                        	        		aRoom.setDescription(aRoom.description()+"  "+fixOutputString(theDescriptions.elementAt(1).toString(),secondRoom));
							firstRoom.setDescription(firstRoom.description()+"  "+fixOutputString(theDescriptions.elementAt(3).toString(),secondRoom));
						} else {
							Log.errOut("MovingRoom","Previous room links exists, "+aRoom.roomID()+" "+firstRoom.roomID()+" not unlinking exit.");
						}
						super.setParms("min="+roomInfos.elementAt(2)+" max="+roomInfos.elementAt(2)+" chance=100;"+roomInfos.elementAt(0)+";"+roomInfos.elementAt(1)+";"+roomInfos.elementAt(2));
						currentStatus=1;
					}
                } else {
				//ARRIVING
					if (isReversed)
					{
						aRoom.showHappens(Affect.MSG_OK_ACTION,fixOutputString(reverseVec.elementAt(2).toString(),secondRoom));
						secondRoom.showHappens(Affect.MSG_OK_ACTION,fixOutputString(reverseVec.elementAt(4).toString(),secondRoom));
						currentStatus=0;
						if ((secondRoom.rawDoors()[Directions.getOpDirectionCode(reverseVec.elementAt(1).toString())]==null)||(secondRoom.rawDoors()[Directions.getOpDirectionCode(reverseVec.elementAt(1).toString())]==aRoom))
						{
							Exit thisNewExit=CMClass.getExit("StdOpenDoorway");
							aRoom.rawDoors()[Directions.getGoodDirectionCode(reverseVec.elementAt(1).toString())]=secondRoom;
							aRoom.rawExits()[Directions.getGoodDirectionCode(reverseVec.elementAt(1).toString())]=thisNewExit;
							secondRoom.rawDoors()[Directions.getOpDirectionCode(reverseVec.elementAt(1).toString())]=aRoom;
							secondRoom.rawExits()[Directions.getOpDirectionCode(reverseVec.elementAt(1).toString())]=thisNewExit;
							ExternalPlay.DBUpdateExits(aRoom);
							ExternalPlay.DBUpdateExits(secondRoom);
							aRoom.getArea().fillInAreaRoom(aRoom);
							secondRoom.getArea().fillInAreaRoom(secondRoom);
							removeStubs(aRoom,secondRoom);
							stubs.removeAllElements();
							stubs.addElement(fixOutputString(theDescriptions.elementAt(4).toString(),secondRoom));
							stubs.addElement(fixOutputString(theDescriptions.elementAt(6).toString(),secondRoom));
                	                		aRoom.setDescription(aRoom.description()+"  "+fixOutputString(theDescriptions.elementAt(4).toString(),secondRoom));
							secondRoom.setDescription(secondRoom.description()+"  "+fixOutputString(theDescriptions.elementAt(6).toString(),secondRoom));
						} else {
							Log.errOut("MovingRoom","Previous room links exists, "+aRoom.roomID()+" "+secondRoom.roomID()+" not linking exits.");
						}
						super.setParms("min="+roomInfos.elementAt(1)+" max="+roomInfos.elementAt(1)+" chance=100;"+roomInfos.elementAt(0)+";"+roomInfos.elementAt(1)+";"+roomInfos.elementAt(2));
						currentStop = nextStop;
				  	} else {
						aRoom.showHappens(Affect.MSG_OK_ACTION,fixOutputString(normalVec.elementAt(2).toString(),secondRoom));
						secondRoom.showHappens(Affect.MSG_OK_ACTION,fixOutputString(normalVec.elementAt(4).toString(),secondRoom));
						currentStatus=0;
						if ((secondRoom.rawDoors()[Directions.getOpDirectionCode(normalVec.elementAt(1).toString())]==null)||(secondRoom.rawDoors()[Directions.getOpDirectionCode(normalVec.elementAt(1).toString())]==aRoom))
						{
							Exit thisNewExit=CMClass.getExit("StdOpenDoorway");
							aRoom.rawDoors()[Directions.getGoodDirectionCode(normalVec.elementAt(1).toString())]=secondRoom;
							aRoom.rawExits()[Directions.getGoodDirectionCode(normalVec.elementAt(1).toString())]=thisNewExit;
							secondRoom.rawDoors()[Directions.getOpDirectionCode(normalVec.elementAt(1).toString())]=aRoom;
							secondRoom.rawExits()[Directions.getOpDirectionCode(normalVec.elementAt(1).toString())]=thisNewExit;
							ExternalPlay.DBUpdateExits(aRoom);
							ExternalPlay.DBUpdateExits(secondRoom);
							aRoom.getArea().fillInAreaRoom(aRoom);
							secondRoom.getArea().fillInAreaRoom(secondRoom);
							removeStubs(aRoom,secondRoom);
							stubs.removeAllElements();
							stubs.addElement(fixOutputString(theDescriptions.elementAt(0).toString(),secondRoom));
							stubs.addElement(fixOutputString(theDescriptions.elementAt(2).toString(),secondRoom));
                	       	 		        aRoom.setDescription(aRoom.description()+"  "+fixOutputString(theDescriptions.elementAt(0).toString(),secondRoom));
							secondRoom.setDescription(secondRoom.description()+"  "+fixOutputString(theDescriptions.elementAt(2).toString(),secondRoom));
						} else {
							Log.errOut("MovingRoom","Previous room links exists, "+aRoom.roomID()+" "+secondRoom.roomID()+" not linking exits.");
						}
						super.setParms("min="+roomInfos.elementAt(1)+" max="+roomInfos.elementAt(1)+" chance=100;"+roomInfos.elementAt(0)+";"+roomInfos.elementAt(1)+";"+roomInfos.elementAt(2));
						currentStop = nextStop;
					}
				}
			}
        }
		return true;
	}
}