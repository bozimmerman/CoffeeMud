package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
   Copyright 2003-2018 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "MovingRoom";
	}

	@Override
	protected int canImproveCode()
	{
		return Behavior.CAN_ROOMS;
	}

	public Vector<String>			listOfRooms		= new Vector<String>();
	protected List<String>			roomInfos		= new Vector<String>();
	public Vector<Vector<String>>	messageInfo		= new Vector<Vector<String>>();
	public Vector<String>			mapInfo			= new Vector<String>();
	protected Vector<String>		stubs			= new Vector<String>();
	protected String				xmlInfo			= "";
	private int						currentStop		= 0;
	private int						nextStop		= 0;
	private int						currentStatus	= 1;
	private boolean					isReversed		= false;

	//private static final int CODE0_TRAVELDIRECTION=0;
	//private static final int CODE0_DOORSDIRECTION=1;
	//private static final int CODE0_INSIDEARRIVEMSG=2;
	protected static final int CODE0_INSIDEDEPARTMSG=3;
	//private static final int CODE0_OUTSIDEARRIVEMSG=4;
	protected static final int CODE0_OUTSIDEDEPARTMSG=5;

	protected static final int CODE1_NORMALINSIDEOPEN=0;
	protected static final int CODE1_NORMALINSIDECLOSED=1;
	protected static final int CODE1_NORMALOUTSIDEOPEN=2;
	protected static final int CODE1_NORMALOUTSIDECLOSED=3;
	protected static final int CODE1_REVERSEINSIDEOPEN=4;
	protected static final int CODE1_REVERSEINSIDECLOSED=5;
	protected static final int CODE1_REVERSEOUTSIDEOPEN=6;
	protected static final int CODE1_REVERSEOUTSIDECLOSED=7;

	protected static final int CODE_NORMALBLOCK=0;
	protected static final int CODE_REVERSEBLOCK=1;
	protected static final int CODE_DESCRIPTIONBLOCK=2;

	public MovingRoom()
	{
		super();
		minTicks=10;maxTicks=30;chance=100;
		tickReset();
	}

	@Override
	public String accountForYourself()
	{
		return "rail mobility";
	}

	@Override
	public void setParms(String newParms)
	{
		String myParms=newParms;
		listOfRooms=new Vector<String>();
		roomInfos=new Vector<String>();
		messageInfo=new Vector<Vector<String>>();
		mapInfo=new Vector<String>();
		stubs=new Vector<String>();

		char c=';';
		int x=myParms.indexOf(c);
		if(x<0)
		{
			c='/';
			x=myParms.indexOf(c);
		}
		if(x>0)
		{
			final String parmText=myParms.substring(0,x);
			myParms=myParms.substring(x+1);
			super.setParms(parmText);
		}
		roomInfos=CMParms.parseSemicolons(myParms,false);
		parms=newParms;
		xmlInfo=loadInfo();
		parseMovingXML(xmlInfo);
	}

	protected static synchronized String loadInfo()
	{
		final StringBuffer str=new StringBuffer("");
		final String resourceName = "movingroom.xml";
		CMFile //F=new CMFile(Resources.makeFileResourceName("behavior/"+resourceName),null,0);
		//if((!F.exists()) || (!F.canRead()))
			F=new CMFile(Resources.makeFileResourceName(resourceName),null,0);
		if((F.exists()) && (F.canRead()))
		{
			final List<String> V=Resources.getFileLineVector(F.text());
			for(int v=0;v<V.size();v++)
				str.append(V.get(v));
			final String theString = str.toString();
			return theString;
		}
		else
		{
			Log.errOut("MovingRoom","Unable to load "+Resources.makeFileResourceName("behavior/"+resourceName)+" or "+Resources.makeFileResourceName(resourceName));
			return "";
		}
	}

	protected void parseMovingXML(String roomToParse)
	{
		final Vector<String> V = new Vector<String>();
		final String theFullBlock=CMLib.xml().returnXMLBlock(roomToParse, roomInfos.get(0).toString().toUpperCase());
		final String theStopsBlock=CMLib.xml().returnXMLBlock(theFullBlock, "STOPS");
		final String theNormalDirBlock=CMLib.xml().returnXMLBlock(theFullBlock, "NORMALDIRECTION");
		final String theReverseDirBlock=CMLib.xml().returnXMLBlock(theFullBlock, "REVERSEDIRECTION");
		final String theDescriptionsBlock=CMLib.xml().returnXMLBlock(theFullBlock, "ROOMDESCRIPTIONS");
		int x=1;
		String thisone=CMLib.xml().returnXMLValue(theStopsBlock, "STOP1");
		while (!"".equals(thisone))
		{
			++x;
			listOfRooms.addElement(thisone);
			thisone=CMLib.xml().returnXMLValue(theStopsBlock, "STOP"+x);
		}
		V.addElement(CMLib.xml().returnXMLValue(theNormalDirBlock, "TRAVELDIRECTION"));
		V.addElement(CMLib.xml().returnXMLValue(theNormalDirBlock, "DOORSDIRECTION"));
		V.addElement(CMLib.xml().returnXMLValue(CMLib.xml().returnXMLBlock(theNormalDirBlock, "INSIDE"), "ARRIVALINFO"));
		V.addElement(CMLib.xml().returnXMLValue(CMLib.xml().returnXMLBlock(theNormalDirBlock, "INSIDE"), "DEPARTINFO"));
		V.addElement(CMLib.xml().returnXMLValue(CMLib.xml().returnXMLBlock(theNormalDirBlock, "OUTSIDE"), "ARRIVALINFO"));
		V.addElement(CMLib.xml().returnXMLValue(CMLib.xml().returnXMLBlock(theNormalDirBlock, "OUTSIDE"), "DEPARTINFO"));
		messageInfo.addElement(new Vector<String>(V));
		V.removeAllElements();
		V.addElement(CMLib.xml().returnXMLValue(theReverseDirBlock, "TRAVELDIRECTION"));
		V.addElement(CMLib.xml().returnXMLValue(theReverseDirBlock, "DOORSDIRECTION"));
		V.addElement(CMLib.xml().returnXMLValue(CMLib.xml().returnXMLBlock(theReverseDirBlock, "INSIDE"), "ARRIVALINFO"));
		V.addElement(CMLib.xml().returnXMLValue(CMLib.xml().returnXMLBlock(theReverseDirBlock, "INSIDE"), "DEPARTINFO"));
		V.addElement(CMLib.xml().returnXMLValue(CMLib.xml().returnXMLBlock(theReverseDirBlock, "OUTSIDE"), "ARRIVALINFO"));
		V.addElement(CMLib.xml().returnXMLValue(CMLib.xml().returnXMLBlock(theReverseDirBlock, "OUTSIDE"), "DEPARTINFO"));
		messageInfo.addElement(new Vector<String>(V));
		V.removeAllElements();
		final String theNormalDescBlock = (CMLib.xml().returnXMLBlock(theDescriptionsBlock, "NORMALDIRECTION"));
		final String theReverseDescBlock = (CMLib.xml().returnXMLBlock(theDescriptionsBlock, "REVERSEDIRECTION"));
		V.addElement(CMLib.xml().returnXMLValue(CMLib.xml().returnXMLBlock(theNormalDescBlock, "INSIDE"), "DOOROPENED"));
		V.addElement(CMLib.xml().returnXMLValue(CMLib.xml().returnXMLBlock(theNormalDescBlock, "INSIDE"), "DOORCLOSED"));
		V.addElement(CMLib.xml().returnXMLValue(CMLib.xml().returnXMLBlock(theNormalDescBlock, "OUTSIDE"), "DOOROPENED"));
		V.addElement(CMLib.xml().returnXMLValue(CMLib.xml().returnXMLBlock(theNormalDescBlock, "OUTSIDE"), "DOORCLOSED"));
		V.addElement(CMLib.xml().returnXMLValue(CMLib.xml().returnXMLBlock(theReverseDescBlock, "INSIDE"), "DOOROPENED"));
		V.addElement(CMLib.xml().returnXMLValue(CMLib.xml().returnXMLBlock(theReverseDescBlock, "INSIDE"), "DOORCLOSED"));
		V.addElement(CMLib.xml().returnXMLValue(CMLib.xml().returnXMLBlock(theReverseDescBlock, "OUTSIDE"), "DOOROPENED"));
		V.addElement(CMLib.xml().returnXMLValue(CMLib.xml().returnXMLBlock(theReverseDescBlock, "OUTSIDE"), "DOORCLOSED"));
		messageInfo.addElement(new Vector<String>(V));
		V.removeAllElements();
		mapInfo.addElement(CMLib.xml().returnXMLValue(theFullBlock, "ROOMPRINTNAME"));
		mapInfo.addElement(CMLib.xml().returnXMLValue(theFullBlock, "LINEPRINTNAME"));
		mapInfo.addElement(CMLib.xml().returnXMLValue(theFullBlock, "DISPLOC"));
	}

	protected String fixOutputString(String incoming, Room busstopRoom)
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
			final int pos=listOfRooms.indexOf(CMLib.map().getExtendedRoomID(busstopRoom));
			boolean revDirName=false;
			if (((pos==0)||(pos==listOfRooms.size()-1))&&(currentStatus==1))
				revDirName=true;
			List<String> V=new Vector<String>();
			if (!revDirName)
			{
				if (isReversed)
					V=messageInfo.elementAt(CODE_REVERSEBLOCK);
				else
					V=messageInfo.elementAt(CODE_NORMALBLOCK);
			}
			else
			{
				if (isReversed)
					V=messageInfo.elementAt(CODE_NORMALBLOCK);
				else
					V=messageInfo.elementAt(CODE_REVERSEBLOCK);
			}
			repWord=incoming.substring(1,i)+V.get(0).toString()+incoming.substring(i+10);
		}
		else
		if (incoming.indexOf("$outopendir")>0)
		{
			i = incoming.indexOf("$outopendir");
			List<String> V=new Vector<String>();
			if (isReversed)
				V=messageInfo.elementAt(CODE_REVERSEBLOCK);
			else
				V=messageInfo.elementAt(CODE_NORMALBLOCK);
			repWord=incoming.substring(1,i)+CMLib.directions().getDirectionName(CMLib.directions().getOpDirectionCode(V.get(1).toString()))+incoming.substring(i+11);
		}
		else
		if (incoming.indexOf("$inopendir")>0)
		{
			i = incoming.indexOf("$inopendir");
			List<String> V=new Vector<String>();
			if (isReversed)
				V=messageInfo.elementAt(CODE_REVERSEBLOCK);
			else
				V=messageInfo.elementAt(CODE_NORMALBLOCK);
			repWord=incoming.substring(1,i)+V.get(1).toString()+incoming.substring(i+10);
		}
		else
		{
			repWord=incoming.substring(1);
			return repWord;
		}
		repWord = fixOutputString(repWord,busstopRoom);
		return repWord;
	}

	protected void removeStubs(Room busstopRoom1,Room busstopRoom2)
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

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		final Vector<String> normalVec = messageInfo.elementAt(CODE_NORMALBLOCK);
		final Vector<String> reverseVec = messageInfo.elementAt(CODE_REVERSEBLOCK);
		final Vector<String> theDescriptions = messageInfo.elementAt(CODE_DESCRIPTIONBLOCK);
		super.tick(ticking,tickID);
		if(canAct(ticking,tickID))
		{
			final Room subwayRoom=getBehaversRoom(ticking);
			if(subwayRoom==null)
			{
				Log.errOut("MovingRoom",ticking.ID()+"/"+ticking.name()+" is NOT A DADGUM ROOM!!!!");
				return false;
			}
			if (currentStop==0)
			{
				isReversed=false;
				nextStop=currentStop+1;
			}
			else
			if (currentStop>=(listOfRooms.size()-1))
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
			if((currentStop<0)
			||(currentStop>=listOfRooms.size())
			||(nextStop<0)
			||(nextStop>=listOfRooms.size()))
			{
				Log.errOut("MovingRoom","Moving Room behavior on "+subwayRoom.roomID()+" HAD malformed rooms list in xml file.");
				subwayRoom.delBehavior(this);
				CMLib.threads().deleteTick(this,-1);
				if((ticking instanceof PhysicalAgent)
				&&(ticking!=subwayRoom))
					((PhysicalAgent)ticking).delBehavior(this);
				return false;
			}

			final String currentStopS=listOfRooms.elementAt(currentStop);
			final String nextStopS=listOfRooms.elementAt(nextStop);
			if(ticking instanceof Room)
			{
				Room currentStopRoom = CMLib.map().getRoom(currentStopS);
				if (currentStopRoom == null)
					currentStopRoom=getBehaversRoom(ticking);
				Room nextStopRoom =CMLib.map().getRoom(nextStopS);
				if (nextStopRoom == null)
					nextStopRoom=getBehaversRoom(ticking);
				if (currentStatus==0)
				{
					//DEPARTING
					if (isReversed)
					{
						currentStopRoom.showHappens(CMMsg.MSG_OK_ACTION,fixOutputString(reverseVec.elementAt(CODE0_OUTSIDEDEPARTMSG).toString(),nextStopRoom));
						subwayRoom.showHappens(CMMsg.MSG_OK_ACTION,fixOutputString(reverseVec.elementAt(CODE0_INSIDEDEPARTMSG).toString(),nextStopRoom));
						if (((subwayRoom.rawDoors()[CMLib.directions().getGoodDirectionCode(normalVec.elementAt(1).toString())]!=null)
								&& ((currentStop==0)
										|| (currentStop==(listOfRooms.size()-1))))
						||(subwayRoom.rawDoors()[CMLib.directions().getGoodDirectionCode(reverseVec.elementAt(1).toString())]!=null))
						{
							if ((currentStop==0)||(currentStop==(listOfRooms.size()-1)))
							{
								currentStopRoom.rawDoors()[CMLib.directions().getOpDirectionCode(normalVec.elementAt(1).toString())]=null;
								currentStopRoom.setRawExit(CMLib.directions().getOpDirectionCode(normalVec.elementAt(1).toString()),null);
								subwayRoom.rawDoors()[CMLib.directions().getGoodDirectionCode(normalVec.elementAt(1).toString())]=null;
								subwayRoom.setRawExit(CMLib.directions().getGoodDirectionCode(normalVec.elementAt(1).toString()),null);
							}
							else
							{
								currentStopRoom.rawDoors()[CMLib.directions().getOpDirectionCode(reverseVec.elementAt(1).toString())]=null;
								currentStopRoom.setRawExit(CMLib.directions().getOpDirectionCode(reverseVec.elementAt(1).toString()),null);
								subwayRoom.rawDoors()[CMLib.directions().getGoodDirectionCode(reverseVec.elementAt(1).toString())]=null;
								subwayRoom.setRawExit(CMLib.directions().getGoodDirectionCode(reverseVec.elementAt(1).toString()),null);
							}
							CMLib.database().DBUpdateExits(subwayRoom);
							CMLib.database().DBUpdateExits(currentStopRoom);
							subwayRoom.getArea().fillInAreaRoom(subwayRoom);
							nextStopRoom.getArea().fillInAreaRoom(currentStopRoom);
							removeStubs(subwayRoom,currentStopRoom);
							final String s1=(fixOutputString(theDescriptions.elementAt(CODE1_REVERSEINSIDECLOSED).toString(),nextStopRoom));
							final String s2=(fixOutputString(theDescriptions.elementAt(CODE1_REVERSEOUTSIDECLOSED).toString(),nextStopRoom));
							if(!stubs.contains(s1))
								stubs.addElement(s1);
							if(!stubs.contains(s2))
								stubs.addElement(s2);
							subwayRoom.setDescription(subwayRoom.description()+"  "+s1);
							currentStopRoom.setDescription(currentStopRoom.description()+"  "+s2);
						}
						else
						{
							Log.errOut("MovingRoom","Previous room links exists, "+subwayRoom.roomID()+" "+currentStopRoom.roomID()+" not unlinking exit.");
						}
						super.setParms("min="+roomInfos.get(2)+" max="+roomInfos.get(2)+" chance=100;"+roomInfos.get(0)+";"+roomInfos.get(1)+";"+roomInfos.get(2));
						currentStatus=1;
				  	}
					else
					{
						// departing, not reversed
						currentStopRoom.showHappens(CMMsg.MSG_OK_ACTION,fixOutputString(normalVec.elementAt(CODE0_OUTSIDEDEPARTMSG).toString(),nextStopRoom));
						subwayRoom.showHappens(CMMsg.MSG_OK_ACTION,fixOutputString(normalVec.elementAt(CODE0_INSIDEDEPARTMSG).toString(),nextStopRoom));
						if (((subwayRoom.rawDoors()[CMLib.directions().getGoodDirectionCode(reverseVec.elementAt(1).toString())]!=null)
								&& ((currentStop==0)
										|| (currentStop==(listOfRooms.size()-1))))
						||(subwayRoom.rawDoors()[CMLib.directions().getGoodDirectionCode(normalVec.elementAt(1).toString())]!=null))
						{
							if ((currentStop==0)||(currentStop==(listOfRooms.size()-1)))
							{
								currentStopRoom.rawDoors()[CMLib.directions().getOpDirectionCode(reverseVec.elementAt(1).toString())]=null;
								currentStopRoom.setRawExit(CMLib.directions().getOpDirectionCode(reverseVec.elementAt(1).toString()),null);
								subwayRoom.rawDoors()[CMLib.directions().getGoodDirectionCode(reverseVec.elementAt(1).toString())]=null;
								subwayRoom.setRawExit(CMLib.directions().getGoodDirectionCode(reverseVec.elementAt(1).toString()),null);
							}
							else
							{
								currentStopRoom.rawDoors()[CMLib.directions().getOpDirectionCode(normalVec.elementAt(1).toString())]=null;
								currentStopRoom.setRawExit(CMLib.directions().getOpDirectionCode(normalVec.elementAt(1).toString()),null);
								subwayRoom.rawDoors()[CMLib.directions().getGoodDirectionCode(normalVec.elementAt(1).toString())]=null;
								subwayRoom.setRawExit(CMLib.directions().getGoodDirectionCode(normalVec.elementAt(1).toString()),null);
							}
							CMLib.database().DBUpdateExits(subwayRoom);
							CMLib.database().DBUpdateExits(currentStopRoom);
							subwayRoom.getArea().fillInAreaRoom(subwayRoom);
							nextStopRoom.getArea().fillInAreaRoom(currentStopRoom);
							removeStubs(subwayRoom,currentStopRoom);
							final String s1=(fixOutputString(theDescriptions.elementAt(CODE1_NORMALINSIDECLOSED).toString(),nextStopRoom));
							final String s2=(fixOutputString(theDescriptions.elementAt(CODE1_NORMALOUTSIDECLOSED).toString(),nextStopRoom));
							if(!stubs.contains(s1))
								stubs.addElement(s1);
							if(!stubs.contains(s2))
								stubs.addElement(s2);
							subwayRoom.setDescription(subwayRoom.description()+"  "+s1);
							currentStopRoom.setDescription(currentStopRoom.description()+"  "+s2);
						}
						else
						{
							Log.errOut("MovingRoom","Previous room links exists, "+subwayRoom.roomID()+" "+currentStopRoom.roomID()+" not unlinking exit.");
						}
						super.setParms("min="+roomInfos.get(2)+" max="+roomInfos.get(2)+" chance=100;"+roomInfos.get(0)+";"+roomInfos.get(1)+";"+roomInfos.get(2));
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
						if ((nextStopRoom.rawDoors()[CMLib.directions().getOpDirectionCode(reverseVec.elementAt(1).toString())]==null)
						||(nextStopRoom.rawDoors()[CMLib.directions().getOpDirectionCode(reverseVec.elementAt(1).toString())]==subwayRoom))
						{
							final Exit thisNewExit=CMClass.getExit("StdOpenDoorway");
							subwayRoom.rawDoors()[CMLib.directions().getGoodDirectionCode(reverseVec.elementAt(1).toString())]=nextStopRoom;
							subwayRoom.setRawExit(CMLib.directions().getGoodDirectionCode(reverseVec.elementAt(1).toString()),thisNewExit);
							nextStopRoom.rawDoors()[CMLib.directions().getOpDirectionCode(reverseVec.elementAt(1).toString())]=subwayRoom;
							nextStopRoom.setRawExit(CMLib.directions().getOpDirectionCode(reverseVec.elementAt(1).toString()),thisNewExit);
							CMLib.database().DBUpdateExits(subwayRoom);
							CMLib.database().DBUpdateExits(nextStopRoom);
							subwayRoom.getArea().fillInAreaRoom(subwayRoom);
							nextStopRoom.getArea().fillInAreaRoom(nextStopRoom);
							removeStubs(subwayRoom,nextStopRoom);
							final String s1=(fixOutputString(theDescriptions.elementAt(CODE1_REVERSEINSIDEOPEN).toString(),nextStopRoom));
							final String s2=(fixOutputString(theDescriptions.elementAt(CODE1_REVERSEOUTSIDEOPEN).toString(),nextStopRoom));
							if(!stubs.contains(s1))
								stubs.addElement(s1);
							if(!stubs.contains(s2))
								stubs.addElement(s2);
							subwayRoom.setDescription(subwayRoom.description()+"  "+s1);
							nextStopRoom.setDescription(nextStopRoom.description()+"  "+s2);
						}
						else
						{
							Log.errOut("MovingRoom","Previous room links exists, "+subwayRoom.roomID()+" "+nextStopRoom.roomID()+" not linking exits.");
						}
						super.setParms("min="+roomInfos.get(1)+" max="+roomInfos.get(1)+" chance=100;"+roomInfos.get(0)+";"+roomInfos.get(1)+";"+roomInfos.get(2));
						currentStop = nextStop;
				  	}
					else
					{
						// arriving, not reversed
						subwayRoom.showHappens(CMMsg.MSG_OK_ACTION,fixOutputString(normalVec.elementAt(2).toString(),nextStopRoom));
						nextStopRoom.showHappens(CMMsg.MSG_OK_ACTION,fixOutputString(normalVec.elementAt(4).toString(),nextStopRoom));
						currentStatus=0;
						if ((nextStopRoom.rawDoors()[CMLib.directions().getOpDirectionCode(normalVec.elementAt(1).toString())]==null)
						||(nextStopRoom.rawDoors()[CMLib.directions().getOpDirectionCode(normalVec.elementAt(1).toString())]==subwayRoom))
						{
							final Exit thisNewExit=CMClass.getExit("StdOpenDoorway");
							subwayRoom.rawDoors()[CMLib.directions().getGoodDirectionCode(normalVec.elementAt(1).toString())]=nextStopRoom;
							subwayRoom.setRawExit(CMLib.directions().getGoodDirectionCode(normalVec.elementAt(1).toString()),thisNewExit);
							nextStopRoom.rawDoors()[CMLib.directions().getOpDirectionCode(normalVec.elementAt(1).toString())]=subwayRoom;
							nextStopRoom.setRawExit(CMLib.directions().getOpDirectionCode(normalVec.elementAt(1).toString()),thisNewExit);
							CMLib.database().DBUpdateExits(subwayRoom);
							CMLib.database().DBUpdateExits(nextStopRoom);
							subwayRoom.getArea().fillInAreaRoom(subwayRoom);
							nextStopRoom.getArea().fillInAreaRoom(nextStopRoom);
							removeStubs(subwayRoom,nextStopRoom);
							final String s1=(fixOutputString(theDescriptions.elementAt(CODE1_NORMALINSIDEOPEN).toString(),nextStopRoom));
							final String s2=(fixOutputString(theDescriptions.elementAt(CODE1_NORMALOUTSIDEOPEN).toString(),nextStopRoom));
							if(!stubs.contains(s1))
								stubs.addElement(s1);
							if(!stubs.contains(s2))
								stubs.addElement(s2);
							subwayRoom.setDescription(subwayRoom.description()+"  "+s1);
							nextStopRoom.setDescription(nextStopRoom.description()+"  "+s2);
						}
						else
						{
							Log.errOut("MovingRoom","Previous room links exists, "+subwayRoom.roomID()+" "+nextStopRoom.roomID()+" not linking exits.");
						}
						super.setParms("min="+roomInfos.get(1)+" max="+roomInfos.get(1)+" chance=100;"+roomInfos.get(0)+";"+roomInfos.get(1)+";"+roomInfos.get(2));
						currentStop = nextStop;
					}
				}
			}
		}
		return true;
	}
}
