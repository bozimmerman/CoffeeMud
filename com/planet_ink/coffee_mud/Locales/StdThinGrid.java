package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

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
public class StdThinGrid extends StdRoom implements GridLocale
{
	public String ID(){return "StdThinGrid";}
	
	public final static long EXPIRATION=60000;
	
	protected Vector descriptions=new Vector();
	protected Vector displayTexts=new Vector();
	protected Vector gridexits=new Vector();
	protected static HashSet working=new HashSet();
	
	protected int xsize=5;
	protected int ysize=5;
	protected Exit ox=null;
	
	protected final DVector rooms=new DVector(4);
	protected static boolean tickStarted=false;
	protected static Ability watcher=null;

	public StdThinGrid()
	{
		super();
		myID=getClass().getName().substring(getClass().getName().lastIndexOf('.')+1);
	}

	public String getChildLocaleID(){return "StdRoom";}

	public int xSize(){return xsize;}
	public int ySize(){return ysize;}
	public void setXSize(int x){ if(x>0)xsize=x; }
	public void setYSize(int y){ if(y>0)ysize=y; }

	
	public void setDescription(String newDescription)
	{
		super.setDescription(newDescription);
		descriptions=new Vector();
		int x=newDescription.indexOf("<P>");
		while(x>=0)
		{
			String s=newDescription.substring(0,x).trim();
			if(s.length()>0) descriptions.addElement(s);
			newDescription=newDescription.substring(x+3).trim();
			x=newDescription.indexOf("<P>");
		}
		if(newDescription.length()>0)
			descriptions.addElement(newDescription);
	}

	public void setDisplayText(String newDisplayText)
	{
		super.setDisplayText(newDisplayText);
		displayTexts=new Vector();
		int x=newDisplayText.indexOf("<P>");
		while(x>=0)
		{
			String s=newDisplayText.substring(0,x).trim();
			if(s.length()>0) displayTexts.addElement(s);
			newDisplayText=newDisplayText.substring(x+3).trim();
			x=newDisplayText.indexOf("<P>");
		}
		if(newDisplayText.length()>0)
			displayTexts.addElement(newDisplayText);
	}

	protected Room getGridRoomIfExists(int x, int y)
	{
		synchronized(rooms)
		{
			for(int i=0;i<rooms.size();i++)
			{
				if((((Integer)rooms.elementAt(i,2)).intValue()==x)
				&&(((Integer)rooms.elementAt(i,3)).intValue()==y))
					return (Room)rooms.elementAt(i,1);
			}
			return null;
		}
	}
	
	protected static boolean cleanRoom(Room R)
	{
		if(R.getGridParent()==null) return true;
		if(R.numInhabitants()>0) return false;
		if(R.numItems()>0) return false;
		for(int a=0;a<R.numEffects();a++)
		{
			Ability A=R.fetchEffect(a);
			if((A!=null)&&(A.isBorrowed(R)))
				return false;
		}
		if(R.getGridParent() instanceof StdThinGrid)
		{
			StdThinGrid STG=(StdThinGrid)R.getGridParent();
			synchronized(STG.rooms)
			{
				for(int i=0;i<STG.rooms.size();i++)
				{
					if(STG.rooms.elementAt(i,1)==R)
					{
						long l=((Long)STG.rooms.elementAt(i,4)).longValue();
						if((System.currentTimeMillis()-l)<EXPIRATION)
							return false;
					}
				}
			}
		}
		return true;
	}
	
	protected static boolean cleanRoomCenter(Room R)
	{
		if(!cleanRoom(R)) 
			return false;
		boolean foundOne=false;
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Room R2=R.rawDoors()[d];
			if(R2!=null)
			{
				foundOne=true;
				if(!cleanRoom(R2))
					return false;
			}
		}
		return foundOne;
	}
	
	protected Room getMakeSingleGridRoom(int x, int y)
	{
		if((x<0)||(y<0)||(y>=ySize())||(x>=xSize())) 
			return null;
		
		Room R=getGridRoomIfExists(x,y);
		if(R==null)
		{
			synchronized(rooms)
			{
				R=CMClass.getLocale(getChildLocaleID());
				if(R==null) return null;
				R.setGridParent(this);
				R.setArea(getArea());
				R.setRoomID("");
				R.setDisplayText(displayText());
				R.setDescription(description());
				int c=-1;
				if(displayTexts!=null)
				if(displayTexts.size()>0)
				{
					c=Dice.roll(1,displayTexts.size(),-1);
					R.setDisplayText((String)displayTexts.elementAt(c));
				}
				if(descriptions!=null)
				if(descriptions.size()>0)
				{
					if((c<0)||(c>descriptions.size())||(descriptions.size()!=displayTexts.size()))
						c=Dice.roll(1,descriptions.size(),-1);
					R.setDescription((String)descriptions.elementAt(c));
				}

				for(int a=0;a<numEffects();a++)
					R.addEffect((Ability)fetchEffect(a).copyOf());
				for(int b=0;b<numBehaviors();b++)
					R.addBehavior(fetchBehavior(b).copyOf());
				if(watcher==null)
					watcher=new ThinGridChildWatch();
				R.addNonUninvokableEffect(watcher);
				rooms.addElement(R,new Integer(x),new Integer(y),new Long(System.currentTimeMillis()));
				CMMap.addRoom(R);
			}
		}
		return R;
	}
	
	protected void fillExitsOfGridRoom(Room R, int x, int y)
	{
		if((x<0)||(y<0)||(y>=ySize())||(x>=xSize())) 
			return;
		if(working.contains(R)) return;
		working.add(R);
		
		// the adjacent rooms created by this method should also take
		// into account the possibility that they are on the edge.
		// it does NOT
		if(ox==null) ox=CMClass.getExit("Open");
		Room R2=null;
		if(y>0)
		{
			R2=getMakeSingleGridRoom(x,y-1);
			if(R2!=null)
				linkRoom(R,R2,Directions.NORTH,ox,ox);
		}
		else
		if((rawDoors()[Directions.NORTH]!=null)&&(rawExits()[Directions.NORTH]!=null))
			linkRoom(R,rawDoors()[Directions.NORTH],Directions.NORTH,rawExits()[Directions.NORTH],rawExits()[Directions.NORTH]);
		
		if(x>0)
		{
			R2=getMakeSingleGridRoom(x-1,y);
			if(R2!=null) 
				linkRoom(R,R2,Directions.WEST,ox,ox);
		}
		else
		if((rawDoors()[Directions.WEST]!=null)&&(rawExits()[Directions.WEST]!=null))
			linkRoom(R,rawDoors()[Directions.WEST],Directions.WEST,rawExits()[Directions.WEST],rawExits()[Directions.WEST]);
		if(y<(ySize()-1))
		{
			R2=getMakeSingleGridRoom(x,y+1);
			if(R2!=null) 
				linkRoom(R,R2,Directions.SOUTH,ox,ox);
		}
		else
		if((rawDoors()[Directions.SOUTH]!=null)&&(rawExits()[Directions.SOUTH]!=null))
			linkRoom(R,rawDoors()[Directions.SOUTH],Directions.SOUTH,rawExits()[Directions.SOUTH],rawExits()[Directions.SOUTH]);
		if(x<(xSize()-1))
		{
			R2=getMakeSingleGridRoom(x+1,y);
			if(R2!=null) 
				linkRoom(R,R2,Directions.EAST,ox,ox);
		}
		else
		if((rawDoors()[Directions.EAST]!=null)&&(rawExits()[Directions.EAST]!=null))
			linkRoom(R,rawDoors()[Directions.EAST],Directions.EAST,rawExits()[Directions.EAST],rawExits()[Directions.EAST]);
		
		for(int d=0;d<gridexits.size();d++)
		{
			CMMap.CrossExit EX=(CMMap.CrossExit)gridexits.elementAt(d);
			try{
				if((EX.out)&&(EX.x==x)&&(EX.y==y))
				{
					if((EX.x==0)&&(EX.dir==Directions.WEST))
						tryFillInExtraneousExternal(EX,ox,R);
					if((EX.x==xSize()-1)&&(EX.dir==Directions.EAST))
						tryFillInExtraneousExternal(EX,ox,R);
					if((EX.y==0)&&(EX.dir==Directions.NORTH))
						tryFillInExtraneousExternal(EX,ox,R);
					if((EX.y==ySize()-1)&&(EX.dir==Directions.SOUTH))
						tryFillInExtraneousExternal(EX,ox,R);
				}
			}catch(Exception e){}
		}
		working.remove(R);
	}
	
	public void tryFillInExtraneousExternal(CMMap.CrossExit EX, Exit ox, Room linkFrom)
	{
		if(EX==null) return;
		Room linkTo=CMMap.getRoom(EX.destRoomID);
		if((linkTo!=null)&&(linkTo.getGridParent()!=null)) 
			linkTo=linkTo.getGridParent();
		if((linkTo!=null)&&(linkFrom.rawDoors()[EX.dir]!=linkTo))
		{
			if(ox==null) ox=CMClass.getExit("Open");
			linkFrom.rawDoors()[EX.dir]=linkTo;
			linkFrom.rawExits()[EX.dir]=ox;
		}
	}
	
	protected Room getMakeGridRoom(int x, int y)
	{
		if((x<0)||(y<0)||(y>=ySize())||(x>=xSize())) 
			return null;
		
		synchronized(rooms)
		{
			startThinTick();
			Room R=getMakeSingleGridRoom(x,y);
			if(R==null) return null;
			fillExitsOfGridRoom(R,x,y);
			return R;
		}
	}
	
	public Vector outerExits(){return (Vector)gridexits.clone();}
	public void delOuterExit(CMMap.CrossExit x){gridexits.remove(x);}
	public void addOuterExit(CMMap.CrossExit x){gridexits.addElement(x);}
	
	public Room getAltRoomFrom(Room loc, int direction)
	{
		if((loc==null)||(direction<0))
			return null;
		int opDirection=Directions.getOpDirectionCode(direction);
		
		String roomID=CMMap.getExtendedRoomID(loc);
		for(int d=0;d<gridexits.size();d++)
		{
			CMMap.CrossExit EX=(CMMap.CrossExit)gridexits.elementAt(d);
			if((!EX.out)
			&&(EX.destRoomID.equalsIgnoreCase(roomID))
			&&(EX.dir==direction)
			&&(EX.x>=0)&&(EX.y>=0)&&(EX.x<ySize())&&(EX.y<ySize()))
				return getMakeGridRoom(EX.x,EX.y);
		}
		
		Room oldLoc=loc;
		if(loc.getGridParent()!=null)
			loc=loc.getGridParent();
		if((oldLoc!=loc)&&(loc instanceof GridLocale))
		{
			int y=((GridLocale)loc).getChildY(oldLoc);
			int x=((GridLocale)loc).getChildX(oldLoc);
			
			if((x>=0)&&(y>=0))
			switch(opDirection)
			{
			case Directions.EAST:
				if((((GridLocale)loc).ySize()==ySize()))
					return getMakeGridRoom(xSize()-1,y);
				break;
			case Directions.WEST:
				if((((GridLocale)loc).ySize()==ySize()))
					return getMakeGridRoom(0,y);
				break;
			case Directions.NORTH:
				if((((GridLocale)loc).xSize()==xSize()))
					return getMakeGridRoom(x,0);
				break;
			case Directions.SOUTH:
				if((((GridLocale)loc).xSize()==xSize()))
					return getMakeGridRoom(x,ySize()-1);
				break;
			}
		}
		int x=0;
		int y=0;
		switch(opDirection)
		{
		case Directions.NORTH:
			x=xSize()/2;
			break;
		case Directions.SOUTH:
			x=xSize()/2;
			y=ySize()-1;
			break;
		case Directions.EAST:
			x=xSize()-1;
			y=ySize()/2;
			break;
		case Directions.WEST:
			y=ySize()/2;
			break;
		case Directions.UP:
		case Directions.DOWN:
			x=xSize()/2;
			y=ySize()/2;
			break;
		}
		return getMakeGridRoom(x,y);
	}

	public Vector getAllRooms()
	{
		Vector V=new Vector();
		getRandomChild();
		synchronized(rooms)
		{
			for(int i=0;i<rooms.size();i++)
				V.addElement(rooms.elementAt(i,1));
		}
		return V;
	}
	
	protected Room alternativeLink(Room room, Room defaultRoom, int dir)
	{
		if(room.getGridParent()==this)
		for(int d=0;d<gridexits.size();d++)
		{
			CMMap.CrossExit EX=(CMMap.CrossExit)gridexits.elementAt(d);
			try{
				if((EX.out)&&(EX.dir==dir)
				&&(getGridRoomIfExists(EX.x,EX.y)==room))
				{
					Room R=CMMap.getRoom(EX.destRoomID);
					if(R!=null)
					{
						if(R.getGridParent()!=null)
							return R.getGridParent();
						else
							return R;
					}
				}
			}catch(Exception e){}
		}
		return defaultRoom;
	}
	
	protected void halfLink(Room room, Room loc, int dirCode, Exit o)
	{
		if(room==null) return;
		if(loc==null) return;
		if(room.rawDoors()[dirCode]!=null)
		{
			if(room.rawDoors()[dirCode].getGridParent()==null)
				return;
			if(room.rawDoors()[dirCode].getGridParent().isMyChild(room.rawDoors()[dirCode]))
				return;
			room.rawDoors()[dirCode]=null;
		}
		if(o==null) o=CMClass.getExit("Open");
		room.rawDoors()[dirCode]=alternativeLink(room,loc,dirCode);
		room.rawExits()[dirCode]=o;
	}

	protected void linkRoom(Room room, Room loc, int dirCode, Exit o, Exit ao)
	{
		if(loc==null) return;
		if(room==null) return;
		int opCode=Directions.getOpDirectionCode(dirCode);
		if(room.rawDoors()[dirCode]!=null)
		{
			if(room.rawDoors()[dirCode].getGridParent()==null)
				return;
			if(room.rawDoors()[dirCode].getGridParent().isMyChild(room.rawDoors()[dirCode]))
				return;
			room.rawDoors()[dirCode]=null;
		}
		if(o==null) o=CMClass.getExit("Open");
		room.rawDoors()[dirCode]=alternativeLink(room,loc,dirCode);
		room.rawExits()[dirCode]=o;
		if(loc.rawDoors()[opCode]!=null)
		{
			if(loc.rawDoors()[opCode].getGridParent()==null)
				return;
			if(loc.rawDoors()[opCode].getGridParent().isMyChild(loc.rawDoors()[opCode]))
				return;
			loc.rawDoors()[opCode]=null;
		}
		if(ao==null) ao=CMClass.getExit("Open");
		loc.rawDoors()[opCode]=alternativeLink(loc,room,opCode);
		loc.rawExits()[opCode]=ao;
	}

	public void buildGrid()
	{
		clearGrid(null);
	}
	
	public boolean isMyChild(Room loc)
	{
		for(int i=0;i<rooms.size();i++)
			if(loc==rooms.elementAt(i,1))
				return true;
		return false;
	}

	protected static void clearRoom(Room room, Room bringBackHere)
	{
		while(room.numInhabitants()>0)
		{
			MOB M=room.fetchInhabitant(0);
			if(M!=null)
			{
				if(bringBackHere!=null)
					bringBackHere.bringMobHere(M,false);
				else
				if((M.getStartRoom()==null)
				||(M.getStartRoom()==room)
				||(M.getStartRoom().ID().length()==0))
					M.destroy();
				else
					M.getStartRoom().bringMobHere(M,false);
			}
		}
		while(room.numItems()>0)
		{
			Item I=room.fetchItem(0);
			if(I!=null) 
			{
				if(bringBackHere!=null)
					bringBackHere.bringItemHere(I,Item.REFUSE_PLAYER_DROP);
				else
					I.destroy();
			}
		}
		room.clearSky();
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			room.rawDoors()[d]=null;
			room.rawExits()[d]=null;
		}
		room.setGridParent(null);
		CMMap.delRoom(room);
	}
	
	public void clearGrid(Room bringBackHere)
	{
		try
		{
			while(rooms.size()>0)
			{
				Room room=(Room)rooms.elementAt(0,1);
				rooms.removeElementAt(0);
				clearRoom(room,bringBackHere);
			}
		}
		catch(Exception e){}
	}

	public String getChildCode(Room loc)
	{
		if(roomID().length()==0) return "";
		DVector rs=rooms.copyOf();
		for(int i=0;i<rs.size();i++)
			if(rs.elementAt(i,1)==loc)
				return roomID()+"#("+((Integer)rs.elementAt(i,2)).intValue()+","+((Integer)rs.elementAt(i,3)).intValue()+")";
		return "";
	}
	
	public int getChildX(Room loc)
	{
		DVector rs=rooms.copyOf();
		for(int i=0;i<rs.size();i++)
			if(rs.elementAt(i,1)==loc)
				return ((Integer)rs.elementAt(i,2)).intValue();
		return -1;
	}
	
	public Room getRandomChild()
	{
		int x=Dice.roll(1,xSize(),-1);
		int y=Dice.roll(1,ySize(),-1);
		Room R=getMakeGridRoom(x,y);
		if(R==null)
			Log.errOut("StdThinGrid",roomID()+" failed to get a random child!");
		return R;
	}
	
	public int getChildY(Room loc)
	{
		DVector rs=rooms.copyOf();
		for(int i=0;i<rs.size();i++)
			if(rs.elementAt(i,1)==loc)
				return ((Integer)rs.elementAt(i,3)).intValue();
		return -1;
	}
	
	public Room getChild(String childCode)
	{
		if(childCode.equals(roomID()))
			return this;
		if(!childCode.startsWith(roomID()+"#("))
			return null;
		int len=roomID().length()+2;
		int comma=childCode.indexOf(',',len);
		if(comma<0) return null;
		int x=Util.s_int(childCode.substring(len,comma));
		int y=Util.s_int(childCode.substring(comma+1,childCode.length()-1));
		return getMakeGridRoom(x,y);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if(msg.targetMinor()==CMMsg.TYP_ENTER)
		{
			if(msg.target()==this)
			{
				MOB mob=msg.source();
				if((mob.location()!=null)&&(mob.location().roomID().length()>0))
				{
					int direction=-1;
					for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
					{
						if(mob.location().getRoomInDir(d)==this)
							direction=d;
					}
					if(direction<0)
					{
						mob.tell("Some great evil is preventing your movement that way.");
						return false;
					}
					else
					msg.modify(msg.source(),
								  getAltRoomFrom(mob.location(),direction),
								  msg.tool(),
								  msg.sourceCode(),
								  msg.sourceMessage(),
								  msg.targetCode(),
								  msg.targetMessage(),
								  msg.othersCode(),
								  msg.othersMessage());
				}
			}
		}
		return true;
	}
	
	public static synchronized void startThinTick()
	{
		if(tickStarted) 
			return;
		if(!CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED))
			return;
		tickStarted=true;
		ThinGridVacuum TGV=new ThinGridVacuum();
		CMClass.ThreadEngine().startTickDown(TGV,MudHost.TICK_MOB,30);
	}
	
	protected static class ThinGridChildWatch implements Ability
	{
		public String ID() { return "ThinGridChildWatch"; }
		public String name(){ return "a Thin Child Watching Property";}
		public String Name(){return name();}
		public String description(){return "";}
		public String displayText(){return "";}
		protected Environmental affected=null;
		protected int canAffectCode(){return 0;}
		protected int canTargetCode(){return 0;}
		public int castingTime(){return 0;}
		public int combatCastingTime(){return 0;}
		public int abilityCode(){return 0;}
		public void setAbilityCode(int newCode){}
		public int adjustedLevel(MOB mob, int asLevel){return -1;}
		public boolean bubbleAffect(){return false;}
		public long flags(){return 0;}
		public long getTickStatus(){return Tickable.STATUS_NOT;}
		public int usageType(){return 0;}
		public void setName(String newName){}
		public void setDescription(String newDescription){}
		public void setDisplayText(String newDisplayText){}
		public MOB invoker(){return null;}
		public void setInvoker(MOB mob){}
		public static final String[] empty={};
		public String[] triggerStrings(){return empty;}
		public boolean invoke(MOB mob, Vector commands, Environmental target, boolean auto, int asLevel){return false;}
		public boolean invoke(MOB mob, Environmental target, boolean auto, int asLevel){return false;}
		public boolean autoInvocation(MOB mob){return false;}
		public void unInvoke(){}
		public boolean canBeUninvoked(){return false;}
		public boolean isAutoInvoked(){return true;}
		public boolean isNowAnAutoEffect(){return true;}
		public boolean canBeTaughtBy(MOB teacher, MOB student){return false;}
		public boolean canBePracticedBy(MOB teacher, MOB student){return false;}
		public boolean canBeLearnedBy(MOB teacher, MOB student){return false;}
		public void teach(MOB teacher, MOB student){}
		public void practice(MOB teacher, MOB student){}
		public int maxRange(){return Integer.MAX_VALUE;}
		public int minRange(){return Integer.MIN_VALUE;}
		public int profficiency(){return 0;}
		public void setProfficiency(int newProfficiency){}
		public boolean profficiencyCheck(MOB mob, int adjustment, boolean auto){return false;}
		public void helpProfficiency(MOB mob){}
		public Environmental affecting(){return affected;}
		public void setAffectedOne(Environmental being){affected=being;}
		public boolean putInCommandlist(){return false;}
		public int quality(){return Ability.INDIFFERENT;}
		public int classificationCode(){ return Ability.PROPERTY;}
		public boolean isBorrowed(Environmental toMe){ return true;	}
		public void setBorrowed(Environmental toMe, boolean truefalse){}
		protected static final EnvStats envStats=new DefaultEnvStats();
		public EnvStats envStats(){return envStats;}
		public EnvStats baseEnvStats(){return envStats;}
		public void recoverEnvStats(){}
		public void setBaseEnvStats(EnvStats newBaseEnvStats){}
		public void startTickDown(MOB mob, Environmental E, int tickID){E.addNonUninvokableEffect(this);}
		public Environmental newInstance(){ return this;}
		private static final String[] CODES={};
		public String[] getStatCodes(){return CODES;}
		public String getStat(String code){ return "";}
		public void setStat(String code, String val){}
		public boolean sameAs(Environmental E){ return (E instanceof ThinGridChildWatch);}
		public Environmental copyOf(){ return this;}
		public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
		public void setMiscText(String newMiscText){}
		public String text(){ return "";}
		public boolean appropriateToMyAlignment(int alignment){return true;}
		public String accountForYourself(){return "";}
		public int affectType(){return 0;}
		public String requirements(){return "";}
		public String image(){return "";}
		public void setImage(String newImage){}
		public boolean canAffect(Environmental E){	return false;}
		public boolean canTarget(Environmental E){ return false;}
		public void affectEnvStats(Environmental affected, EnvStats affectableStats){}
		public void affectCharStats(MOB affectedMob, CharStats affectableStats){}
		public void affectCharState(MOB affectedMob, CharState affectableMaxState){}
		public void executeMsg(Environmental myHost, CMMsg msg)
		{
			if((msg.target() instanceof Room)
			&&((msg.targetMinor()==CMMsg.TYP_EXAMINESOMETHING)
			   ||(msg.targetMinor()==CMMsg.TYP_ENTER))
			&&(((Room)msg.target()).getGridParent() instanceof StdThinGrid))
			{
				Room R=(Room)msg.target();
				StdThinGrid STG=((StdThinGrid)R.getGridParent());
				int x=STG.getChildX(R);
				int y=STG.getChildY(R);
				if((x>=0)&&(x<STG.xSize())&&(y>=0)&&(y<STG.ySize()))
					STG.fillExitsOfGridRoom(R,x,y);
			}
		}
		public boolean okMessage(Environmental myHost, CMMsg msg){return true;}
		public boolean tick(Tickable ticking, int tickID){ return true;	}
		public void makeLongLasting(){}
		public void makeNonUninvokable(){}
		private static final int[] cost=new int[3];
		public int[] usageCost(MOB mob){return cost;}
		public void addEffect(Ability to){}
		public void addNonUninvokableEffect(Ability to){}
		public void delEffect(Ability to){}
		public int numEffects(){ return 0;}
		public Ability fetchEffect(int index){return null;}
		public Ability fetchEffect(String ID){return null;}
		public void addBehavior(Behavior to){}
		public void delBehavior(Behavior to){}
		public int numBehaviors(){return 0;}
		public Behavior fetchBehavior(int index){return null;}
		public Behavior fetchBehavior(String ID){return null;}
		public boolean isGeneric(){return false;}
	}
	
	protected static class ThinGridVacuum implements Tickable
	{
		public String ID(){return "ThinGridVacuum";}
		public String name(){return ID();}
		public long tickStatus=Tickable.STATUS_NOT;
		public long getTickStatus(){return tickStatus;}
		public boolean tick(Tickable ticking, int tickID)
		{
			Room R=null;
			for(Enumeration e=CMMap.rooms();e.hasMoreElements();)
			{
				R=(Room)e.nextElement();
				if(R instanceof StdThinGrid)
				{
					DVector DV=((StdThinGrid)R).rooms;
					if(DV.size()>0)
					{
						synchronized(DV)
						{
							long time=System.currentTimeMillis()-EXPIRATION;
							for(int r=DV.size()-1;r>=0;r--)
								if(((Long)DV.elementAt(r,4)).longValue()<time)
								{
									R=(Room)DV.elementAt(r,1);
									if(cleanRoomCenter(R))
									{
										DV.removeElement(R);
										clearRoom(R,null);
									}
								}
						}
					}
				}
			}
			return true;
		}
	}
}
