package com.planet_ink.coffee_mud.Behaviors;

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

	//private static final int CODE0_TRAVELDIRECTION=0;
	//private static final int CODE0_DOORSDIRECTION=1;
	//private static final int CODE0_INSIDEARRIVEMSG=2;
	private static final int CODE0_INSIDEDEPARTMSG=3;
	//private static final int CODE0_OUTSIDEARRIVEMSG=4;
	private static final int CODE0_OUTSIDEDEPARTMSG=5;

	private static final int CODE1_NORMALINSIDEOPEN=0;
	private static final int CODE1_NORMALINSIDECLOSED=1;
	private static final int CODE1_NORMALOUTSIDEOPEN=2;
	private static final int CODE1_NORMALOUTSIDECLOSED=3;
	private static final int CODE1_REVERSEINSIDEOPEN=4;
	private static final int CODE1_REVERSEINSIDECLOSED=5;
	private static final int CODE1_REVERSEOUTSIDEOPEN=6;
	private static final int CODE1_REVERSEOUTSIDECLOSED=7;

	private static final int CODE_NORMALBLOCK=0;
	private static final int CODE_REVERSEBLOCK=1;
	private static final int CODE_DESCRIPTIONBLOCK=2;

	public MovingRoom()
	{
		super();
		minTicks=10;maxTicks=30;chance=100;
		tickReset();
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
		roomInfos=Util.parseSemicolons(myParms,false);
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
		while (thisone!="")
		{
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
	private String fixOutputString(String incoming, Room busstopRoom)
	{
		String repWord="";
		incoming = " " + incoming;
		int i=0;
		if (incoming.indexOf("$disproom")>0)
		{
			i = incoming.indexOf("$disproom");
			repWord=incoming.substring(1,i)+busstopRoom.displayText()+incoming.substring(i+9);
		}
		else
		if (incoming.indexOf("$traveldir")>0)
		{
			i = incoming.indexOf("$traveldir");
			int pos=listOfRooms.indexOf(CMMap.getExtendedRoomID(busstopRoom));
			boolean revDirName=false;
			if (((pos==0)||(pos==listOfRooms.size()-1))&&(currentStatus==1))
				revDirName=true;
			Vector V=new Vector();
			if (!revDirName)
			{
				if (isReversed)
					V=(Vector)messageInfo.elementAt(CODE_REVERSEBLOCK);
				else
					V=(Vector)messageInfo.elementAt(CODE_NORMALBLOCK);
			}
			else
			{
				if (isReversed)
					V=(Vector)messageInfo.elementAt(CODE_NORMALBLOCK);
				else
					V=(Vector)messageInfo.elementAt(CODE_REVERSEBLOCK);
			}
			repWord=incoming.substring(1,i)+V.elementAt(0).toString()+incoming.substring(i+10);
		}
		else
		if (incoming.indexOf("$outopendir")>0)
		{
			i = incoming.indexOf("$outopendir");
			Vector V=new Vector();
			if (isReversed)
				V=(Vector)messageInfo.elementAt(CODE_REVERSEBLOCK);
			else
				V=(Vector)messageInfo.elementAt(CODE_NORMALBLOCK);
			repWord=incoming.substring(1,i)+Directions.getDirectionName(Directions.getOpDirectionCode(V.elementAt(1).toString()))+incoming.substring(i+11);
		}
		else
		if (incoming.indexOf("$inopendir")>0)
		{
			i = incoming.indexOf("$inopendir");
			Vector V=new Vector();
			if (isReversed)
				V=(Vector)messageInfo.elementAt(CODE_REVERSEBLOCK);
			else
				V=(Vector)messageInfo.elementAt(CODE_NORMALBLOCK);
			repWord=incoming.substring(1,i)+V.elementAt(1).toString()+incoming.substring(i+10);
		}
		else
		{
			repWord=incoming.substring(1);
			return repWord;
		}
		repWord = fixOutputString(repWord,busstopRoom);
		return repWord;
	}
	private void removeStubs(Room busstopRoom1,Room busstopRoom2)
	{
		if (!stubs.isEmpty())
		for(int s=0;s<stubs.size();s++)
		{
			int i=busstopRoom1.description().indexOf(stubs.elementAt(s).toString());
			if (i>0)
				busstopRoom1.setDescription(busstopRoom1.description().substring(0,i).trim());
			i=busstopRoom2.description().indexOf(stubs.elementAt(s).toString());
			if (i>0)
				busstopRoom2.setDescription(busstopRoom2.description().substring(0,i).trim());
		}
	}
	public boolean tick(Tickable ticking, int tickID)
	{
		Vector normalVec = (Vector)messageInfo.elementAt(CODE_NORMALBLOCK);
		Vector reverseVec = (Vector)messageInfo.elementAt(CODE_REVERSEBLOCK);
		Vector theDescriptions = (Vector)messageInfo.elementAt(CODE_DESCRIPTIONBLOCK);
		super.tick(ticking,tickID);
		if(canAct(ticking,tickID))
		{
			Room subwayRoom=getBehaversRoom(ticking);
			if (currentStop==0)
			{
                isReversed=false;
                nextStop=currentStop+1;
            }
			else
			if (currentStop==(listOfRooms.size()-1))
			{
                isReversed=true;
                nextStop=currentStop-1;
            }
			else
			if (isReversed)
			{
                nextStop=currentStop-1;
            }
			else
			{
                nextStop=currentStop+1;
			}
		    String currentStopS=(String)listOfRooms.elementAt(currentStop);
            String nextStopS=(String)listOfRooms.elementAt(nextStop);
		    if(ticking instanceof Room)
			{
                Room currentStopRoom = CMMap.getRoom(currentStopS);
				if (currentStopRoom == null)
					currentStopRoom=getBehaversRoom(ticking);
                Room nextStopRoom =CMMap.getRoom(nextStopS);
				if (nextStopRoom == null)
					nextStopRoom=getBehaversRoom(ticking);
				if (currentStatus==0)
				{
					//DEPARTING
					if (isReversed)
					{
						currentStopRoom.showHappens(CMMsg.MSG_OK_ACTION,fixOutputString(reverseVec.elementAt(CODE0_OUTSIDEDEPARTMSG).toString(),nextStopRoom));
						subwayRoom.showHappens(CMMsg.MSG_OK_ACTION,fixOutputString(reverseVec.elementAt(CODE0_INSIDEDEPARTMSG).toString(),nextStopRoom));
						if (((subwayRoom.rawDoors()[Directions.getGoodDirectionCode(normalVec.elementAt(1).toString())]!=null) && ((currentStop==0) || (currentStop==(listOfRooms.size()-1))))||(subwayRoom.rawDoors()[Directions.getGoodDirectionCode(reverseVec.elementAt(1).toString())]!=null))
						{
							if ((currentStop==0)||(currentStop==(listOfRooms.size()-1)))
							{
								currentStopRoom.rawDoors()[Directions.getOpDirectionCode(normalVec.elementAt(1).toString())]=null;
								currentStopRoom.rawExits()[Directions.getOpDirectionCode(normalVec.elementAt(1).toString())]=null;
								subwayRoom.rawDoors()[Directions.getGoodDirectionCode(normalVec.elementAt(1).toString())]=null;
								subwayRoom.rawExits()[Directions.getGoodDirectionCode(normalVec.elementAt(1).toString())]=null;
							}
							else
							{
								currentStopRoom.rawDoors()[Directions.getOpDirectionCode(reverseVec.elementAt(1).toString())]=null;
								currentStopRoom.rawExits()[Directions.getOpDirectionCode(reverseVec.elementAt(1).toString())]=null;
								subwayRoom.rawDoors()[Directions.getGoodDirectionCode(reverseVec.elementAt(1).toString())]=null;
								subwayRoom.rawExits()[Directions.getGoodDirectionCode(reverseVec.elementAt(1).toString())]=null;
							}
							CMClass.DBEngine().DBUpdateExits(subwayRoom);
							CMClass.DBEngine().DBUpdateExits(currentStopRoom);
							subwayRoom.getArea().fillInAreaRoom(subwayRoom);
							nextStopRoom.getArea().fillInAreaRoom(currentStopRoom);
							removeStubs(subwayRoom,currentStopRoom);
							String s1=(fixOutputString(theDescriptions.elementAt(CODE1_REVERSEINSIDECLOSED).toString(),nextStopRoom));
							String s2=(fixOutputString(theDescriptions.elementAt(CODE1_REVERSEOUTSIDECLOSED).toString(),nextStopRoom));
							if(!stubs.contains(s1)) stubs.addElement(s1);
							if(!stubs.contains(s2)) stubs.addElement(s2);
                        	subwayRoom.setDescription(subwayRoom.description()+"  "+s1);
							currentStopRoom.setDescription(currentStopRoom.description()+"  "+s2);
						}
						else
						{
							Log.errOut("MovingRoom","Previous room links exists, "+subwayRoom.roomID()+" "+currentStopRoom.roomID()+" not unlinking exit.");
						}
						super.setParms("min="+roomInfos.elementAt(2)+" max="+roomInfos.elementAt(2)+" chance=100;"+roomInfos.elementAt(0)+";"+roomInfos.elementAt(1)+";"+roomInfos.elementAt(2));
						currentStatus=1;
				  	}
					else
					{
						// departing, not reversed
						currentStopRoom.showHappens(CMMsg.MSG_OK_ACTION,fixOutputString(normalVec.elementAt(CODE0_OUTSIDEDEPARTMSG).toString(),nextStopRoom));
						subwayRoom.showHappens(CMMsg.MSG_OK_ACTION,fixOutputString(normalVec.elementAt(CODE0_INSIDEDEPARTMSG).toString(),nextStopRoom));
						if (((subwayRoom.rawDoors()[Directions.getGoodDirectionCode(reverseVec.elementAt(1).toString())]!=null) && ((currentStop==0) || (currentStop==(listOfRooms.size()-1))))||(subwayRoom.rawDoors()[Directions.getGoodDirectionCode(normalVec.elementAt(1).toString())]!=null))
						{
							if ((currentStop==0)||(currentStop==(listOfRooms.size()-1)))
							{
								currentStopRoom.rawDoors()[Directions.getOpDirectionCode(reverseVec.elementAt(1).toString())]=null;
								currentStopRoom.rawExits()[Directions.getOpDirectionCode(reverseVec.elementAt(1).toString())]=null;
								subwayRoom.rawDoors()[Directions.getGoodDirectionCode(reverseVec.elementAt(1).toString())]=null;
								subwayRoom.rawExits()[Directions.getGoodDirectionCode(reverseVec.elementAt(1).toString())]=null;
							}
							else
							{
								currentStopRoom.rawDoors()[Directions.getOpDirectionCode(normalVec.elementAt(1).toString())]=null;
								currentStopRoom.rawExits()[Directions.getOpDirectionCode(normalVec.elementAt(1).toString())]=null;
								subwayRoom.rawDoors()[Directions.getGoodDirectionCode(normalVec.elementAt(1).toString())]=null;
								subwayRoom.rawExits()[Directions.getGoodDirectionCode(normalVec.elementAt(1).toString())]=null;
							}
							CMClass.DBEngine().DBUpdateExits(subwayRoom);
							CMClass.DBEngine().DBUpdateExits(currentStopRoom);
							subwayRoom.getArea().fillInAreaRoom(subwayRoom);
							nextStopRoom.getArea().fillInAreaRoom(currentStopRoom);
							removeStubs(subwayRoom,currentStopRoom);
							String s1=(fixOutputString(theDescriptions.elementAt(CODE1_NORMALINSIDECLOSED).toString(),nextStopRoom));
							String s2=(fixOutputString(theDescriptions.elementAt(CODE1_NORMALOUTSIDECLOSED).toString(),nextStopRoom));
							if(!stubs.contains(s1)) stubs.addElement(s1);
							if(!stubs.contains(s2)) stubs.addElement(s2);
                        	subwayRoom.setDescription(subwayRoom.description()+"  "+s1);
							currentStopRoom.setDescription(currentStopRoom.description()+"  "+s2);
						}
						else
						{
							Log.errOut("MovingRoom","Previous room links exists, "+subwayRoom.roomID()+" "+currentStopRoom.roomID()+" not unlinking exit.");
						}
						super.setParms("min="+roomInfos.elementAt(2)+" max="+roomInfos.elementAt(2)+" chance=100;"+roomInfos.elementAt(0)+";"+roomInfos.elementAt(1)+";"+roomInfos.elementAt(2));
						currentStatus=1;
					}
                }
				else
				{
					//ARRIVING
					if (isReversed)
					{
						subwayRoom.showHappens(CMMsg.MSG_OK_ACTION,fixOutputString(reverseVec.elementAt(2).toString(),nextStopRoom));
						nextStopRoom.showHappens(CMMsg.MSG_OK_ACTION,fixOutputString(reverseVec.elementAt(4).toString(),nextStopRoom));
						currentStatus=0;
						if ((nextStopRoom.rawDoors()[Directions.getOpDirectionCode(reverseVec.elementAt(1).toString())]==null)||(nextStopRoom.rawDoors()[Directions.getOpDirectionCode(reverseVec.elementAt(1).toString())]==subwayRoom))
						{
							Exit thisNewExit=CMClass.getExit("StdOpenDoorway");
							subwayRoom.rawDoors()[Directions.getGoodDirectionCode(reverseVec.elementAt(1).toString())]=nextStopRoom;
							subwayRoom.rawExits()[Directions.getGoodDirectionCode(reverseVec.elementAt(1).toString())]=thisNewExit;
							nextStopRoom.rawDoors()[Directions.getOpDirectionCode(reverseVec.elementAt(1).toString())]=subwayRoom;
							nextStopRoom.rawExits()[Directions.getOpDirectionCode(reverseVec.elementAt(1).toString())]=thisNewExit;
							CMClass.DBEngine().DBUpdateExits(subwayRoom);
							CMClass.DBEngine().DBUpdateExits(nextStopRoom);
							subwayRoom.getArea().fillInAreaRoom(subwayRoom);
							nextStopRoom.getArea().fillInAreaRoom(nextStopRoom);
							removeStubs(subwayRoom,nextStopRoom);
							String s1=(fixOutputString(theDescriptions.elementAt(CODE1_REVERSEINSIDEOPEN).toString(),nextStopRoom));
							String s2=(fixOutputString(theDescriptions.elementAt(CODE1_REVERSEOUTSIDEOPEN).toString(),nextStopRoom));
							if(!stubs.contains(s1)) stubs.addElement(s1);
							if(!stubs.contains(s2)) stubs.addElement(s2);
                        	subwayRoom.setDescription(subwayRoom.description()+"  "+s1);
							nextStopRoom.setDescription(nextStopRoom.description()+"  "+s2);
						}
						else
						{
							Log.errOut("MovingRoom","Previous room links exists, "+subwayRoom.roomID()+" "+nextStopRoom.roomID()+" not linking exits.");
						}
						super.setParms("min="+roomInfos.elementAt(1)+" max="+roomInfos.elementAt(1)+" chance=100;"+roomInfos.elementAt(0)+";"+roomInfos.elementAt(1)+";"+roomInfos.elementAt(2));
						currentStop = nextStop;
				  	}
					else
					{
						// arriving, not reversed
						subwayRoom.showHappens(CMMsg.MSG_OK_ACTION,fixOutputString(normalVec.elementAt(2).toString(),nextStopRoom));
						nextStopRoom.showHappens(CMMsg.MSG_OK_ACTION,fixOutputString(normalVec.elementAt(4).toString(),nextStopRoom));
						currentStatus=0;
						if ((nextStopRoom.rawDoors()[Directions.getOpDirectionCode(normalVec.elementAt(1).toString())]==null)||(nextStopRoom.rawDoors()[Directions.getOpDirectionCode(normalVec.elementAt(1).toString())]==subwayRoom))
						{
							Exit thisNewExit=CMClass.getExit("StdOpenDoorway");
							subwayRoom.rawDoors()[Directions.getGoodDirectionCode(normalVec.elementAt(1).toString())]=nextStopRoom;
							subwayRoom.rawExits()[Directions.getGoodDirectionCode(normalVec.elementAt(1).toString())]=thisNewExit;
							nextStopRoom.rawDoors()[Directions.getOpDirectionCode(normalVec.elementAt(1).toString())]=subwayRoom;
							nextStopRoom.rawExits()[Directions.getOpDirectionCode(normalVec.elementAt(1).toString())]=thisNewExit;
							CMClass.DBEngine().DBUpdateExits(subwayRoom);
							CMClass.DBEngine().DBUpdateExits(nextStopRoom);
							subwayRoom.getArea().fillInAreaRoom(subwayRoom);
							nextStopRoom.getArea().fillInAreaRoom(nextStopRoom);
							removeStubs(subwayRoom,nextStopRoom);
							String s1=(fixOutputString(theDescriptions.elementAt(CODE1_NORMALINSIDEOPEN).toString(),nextStopRoom));
							String s2=(fixOutputString(theDescriptions.elementAt(CODE1_NORMALOUTSIDEOPEN).toString(),nextStopRoom));
							if(!stubs.contains(s1)) stubs.addElement(s1);
							if(!stubs.contains(s2)) stubs.addElement(s2);
                        	subwayRoom.setDescription(subwayRoom.description()+"  "+s1);
							nextStopRoom.setDescription(nextStopRoom.description()+"  "+s2);
						}
						else
						{
							Log.errOut("MovingRoom","Previous room links exists, "+subwayRoom.roomID()+" "+nextStopRoom.roomID()+" not linking exits.");
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
