package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.Abilities.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Exits.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;
public class StdRoom 
	implements Room 
{
	protected String myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	protected String name="room";
	protected String myAreaID="StdArea";
	protected String displayText="Standard Room";
	protected String miscText="";
	protected String description="";
	private String objectID=myID;
	protected Stats envStats=new Stats();
	protected Stats baseEnvStats=new Stats();
	public Exit[] exits=new Exit[Directions.NUM_DIRECTIONS];
	public Room[] doors=new Room[Directions.NUM_DIRECTIONS];
	protected Vector affects=new Vector();
	protected Vector behaviors=new Vector();
	protected Vector contents=new Vector();
	protected Vector inhabitants=new Vector();
	protected int domainType=Room.DOMAIN_OUTDOORS_CITY;
	protected int domainCondition=Room.CONDITION_NORMAL;
	
	protected boolean skyedYet=false;
								   
	public StdRoom()
	{
	}
	
	public String ID()
	{
		return myID	;
	}
	public String objectID()
	{
		return objectID;
	}
	public String name(){ return name;}
	public void setName(String newName){name=newName;}
	public Environmental newInstance()
	{
		return new StdRoom();
	}
	private void cloneFix(Room E)
	{
		affects=new Vector();
		baseEnvStats=E.baseEnvStats().cloneStats();
		envStats=E.envStats().cloneStats();
		
		contents=new Vector();
		inhabitants=new Vector();
		exits=new Exit[Directions.NUM_DIRECTIONS];
		doors=new Room[Directions.NUM_DIRECTIONS];
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			if(E.exits()[d]!=null)
				exits[d]=(Exit)E.exits()[d].copyOf();
			if(E.doors()[d]!=null)
				doors[d]=E.doors()[d];
			
		}
	}
	public Environmental copyOf()
	{
		try
		{
			StdRoom R=(StdRoom)this.clone();
			R.cloneFix(this);
			return R;
			
		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}
	public int domainType()
	{
		return domainType;
	}

	public int domainConditions()
	{
		return domainCondition;
	}
	
	public String displayText()
	{
		return displayText;
	}
	public void setDisplayText(String newDisplayText)
	{
		displayText=newDisplayText;
	}
	public String description()
	{
		return description;
	}
	public void setDescription(String newDescription)
	{
		description=newDescription;
	}
	public String text()
	{
		return miscText;
	}
	public void setMiscText(String newMiscText)
	{
		miscText=newMiscText;
	}
	public String getAreaID()
	{
		return myAreaID;
	}
	public void setID(String newID)
	{
		myID=newID;
	}
	public void setAreaID(String newArea)
	{
		myAreaID=newArea;
	}
	
	protected void giveASky(Room room)
	{
		skyedYet=true;
		if((room.doors()[Directions.UP]==null)
		&&((room.domainType()&128)==Room.OUTDOORS)
		&&(room.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
		&&(!(room instanceof EndlessSky))
		&&(!(room instanceof InTheAir)))
		{
			StdOpenDoorway o=new StdOpenDoorway();
			EndlessSky sky=new EndlessSky();
			sky.setAreaID(room.getAreaID());
			room.doors()[Directions.UP]=sky;
			room.exits()[Directions.UP]=o;
			sky.doors()[Directions.DOWN]=room;
			sky.exits()[Directions.DOWN]=o;
			MUD.map.addElement(sky);
		}
	}
	
	public boolean okAffect(Affect affect)
	{
		if(affect.amITarget(this))
		{
			MOB mob=(MOB)affect.source();
			
			boolean legalToDoToThisRoom=false;
			switch(affect.targetType())
			{
			case Affect.MOVE:
				switch(affect.targetCode())
				{
				case Affect.MOVE_LEAVE:
				case Affect.MOVE_FLEE:
				case Affect.MOVE_ENTER:
					legalToDoToThisRoom=true;
					if((!skyedYet)
					&&(affect.targetCode()==Affect.MOVE_ENTER)
					&&(affect.source()!=null)
					&&(!affect.source().isMonster()))
						giveASky(this);
					break;
				default:
					break;
				}
				break;
			case Affect.HANDS:
				switch(affect.targetCode())
				{
				case Affect.HANDS_RECALL:
					legalToDoToThisRoom=true;
					break;
				default:
					break;
				}
				break;
			case Affect.AREA:
				legalToDoToThisRoom=true;
				break;
			case Affect.VISUAL:
				if(!Sense.canBeSeenBy(this,mob))
				{
					mob.tell("You can't see anything!");
					return false;
				}
				legalToDoToThisRoom=true;
				break;
			case Affect.SOUND:
				legalToDoToThisRoom=true;
				break;
			default:
				break;
			}
			if(!legalToDoToThisRoom)
			{
				mob.tell("You can't do that here.");
				return false;
			}
		}
		
		for(int i=0;i<inhabitants.size();i++)
			if(!((MOB)inhabitants.elementAt(i)).okAffect(affect))
				return false;
		
		for(int i=0;i<contents.size();i++)
			if(!((Item)contents.elementAt(i)).okAffect(affect))
				return false;
		
		for(int i=0;i<affects.size();i++)
			if(!((Ability)fetchAffect(i)).okAffect(affect))
				return false;
		
		for(int b=0;b<behaviors.size();b++)
		{
			Behavior B=(Behavior)behaviors.elementAt(b);
			if(!B.okAffect(this,affect))
				return false;
		}
		
		for(int i=0;i<Directions.NUM_DIRECTIONS;i++)
		{
			Exit thisExit=getExit(i);
			if(thisExit!=null) 
				if(!thisExit.okAffect(affect))
					return false;
		}
		
		if((affect.othersType()==Affect.AREA)&&(affect.source().location()==this))
		{
			for(int m=0;m<MUD.map.size();m++)
			{
				Room otherRoom=(Room)MUD.map.elementAt(m);
				if((otherRoom!=null)&&(otherRoom.getAreaID().equals(getAreaID())))
				   if(!otherRoom.okAffect(affect)) return false;
			}
		}
		return true;
	}
	
	public void affect(Affect affect)
	{
		
		if(affect.amITarget(this))
		{
			MOB mob=(MOB)affect.source();
			switch(affect.targetType())
			{
			case Affect.MOVE:
				switch(affect.targetCode())
				{
				case Affect.MOVE_LEAVE:
				{
					recoverRoomStats();
					break;
				}
				case Affect.MOVE_FLEE:
				{
					recoverRoomStats();
					break;
				}
				case Affect.MOVE_ENTER:
				{
					recoverRoomStats();
					break;
				}
				default:
					break;
				}
				break;
			case Affect.VISUAL:
				switch(affect.targetCode())
				{
				case Affect.VISUAL_LOOK:
					look(mob);
					break;
				case Affect.VISUAL_READ:
					if(Sense.canBeSeenBy(this,mob))
						mob.tell("There is nothing written here.");
					else
						mob.tell("You can't see that!");
					break;
				default:
					break;
				}
				break;
			default:
				break;
			}
			if((affect.othersType()==Affect.AREA)&&(affect.source().location()==this))
			{
				for(int m=0;m<MUD.map.size();m++)
				{
					Room otherRoom=(Room)MUD.map.elementAt(m);
					if((otherRoom!=null)&&(otherRoom.getAreaID().equals(getAreaID())))
					   otherRoom.affect(affect);
				}
			}
		}
		
		for(int i=0;i<contents.size();i++)
			((Item)contents.elementAt(i)).affect(affect);
		
		for(int i=0;i<Directions.NUM_DIRECTIONS;i++)
		{
			Exit thisExit=getExit(i);
			if(thisExit!=null) 
				thisExit.affect(affect);
		}
		
		for(int b=0;b<behaviors.size();b++)
		{
			Behavior B=(Behavior)behaviors.elementAt(b);
			B.affect(this,affect);
		}
		
		for(int i=0;i<affects.size();i++)
			((Ability)fetchAffect(i)).affect(affect);
	}
	
	public void startItemRejuv()
	{
		for(int c=0;c<contents.size();c++)
		{
			Item item=(Item)contents.elementAt(c);
			if(item.location()==null)
			{
				ItemRejuv.unloadIfNecessary(item);
				if((item.envStats().rejuv()<Integer.MAX_VALUE)&&(item.envStats().rejuv()>0))
					ItemRejuv.loadMeUp(item,this);
			}
		}
	}
	
	public boolean tick(int tickID)
	{
		for(int b=0;b<behaviors.size();b++)
		{
			Behavior B=(Behavior)behaviors.elementAt(b);
			B.tick(this,tickID);
		}
		
		int a=0;
		while(a<affects.size())
		{
			Ability A=(Ability)affects.elementAt(a);
			int s=affects.size();
			if(!A.tick(tickID))
				A.unInvoke();
			if(affects.size()==s)
				a++;
		}
		return true;
	}
	
	public Stats envStats()
	{
		return envStats;
	}
	public Stats baseEnvStats()
	{
		return baseEnvStats;
	}
	public void recoverEnvStats()
	{
		envStats=baseEnvStats.cloneStats();
		for(int b=0;b<inhabitants.size();b++)
		{
			MOB mob=(MOB)inhabitants.elementAt(b);
			mob.affectEnvStats(this,envStats);
		}
		for(int a=0;a<affects.size();a++)
		{
			Ability affect=(Ability)affects.elementAt(a);
			affect.affectEnvStats(this,envStats);
		}
		for(int i=0;i<contents.size();i++)
		{
			Item item=(Item)contents.elementAt(i);
			item.affectEnvStats(this,envStats);
		}
	}
	public void recoverRoomStats()
	{
		recoverEnvStats();
		for(int b=0;b<inhabitants.size();b++)
		{
			MOB mob=(MOB)inhabitants.elementAt(b);
			mob.recoverCharStats();
			mob.recoverEnvStats();
		}
		for(int i=0;i<contents.size();i++)
		{
			Item item=(Item)contents.elementAt(i);
			item.recoverEnvStats();
		}
	}
	
	public void setBaseEnvStats(Stats newBaseEnvStats)
	{	
		baseEnvStats=newBaseEnvStats.cloneStats(); 
	}
	
	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		if(envStats().sensesMask()>0)
			affectableStats.setSensesMask(affectableStats.sensesMask()|envStats().sensesMask());
		int disposition=envStats().disposition();
		if((disposition&Sense.IS_DARK)==Sense.IS_DARK)
			disposition=disposition-Sense.IS_DARK;
		if((disposition&Sense.IS_LIGHT)==Sense.IS_LIGHT)
			disposition=disposition-Sense.IS_LIGHT;
		if(disposition>0)
			affectableStats.setDisposition(affectableStats.disposition()|disposition);
	}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{}//rooms will never be asked this, so this method should always do NOTHING
	
	public void look(MOB mob)
	{
		StringBuffer Say=new StringBuffer("");
		if(mob.readSysopMsgs())
		{
			Say.append("Area:("+myAreaID+")"+"\n\r");
			Say.append("("+ID()+")");
		}
		if((Sense.canBeSeenBy(this,mob))||(mob.isASysOp()))
		{
			Say.append(displayText()+Sense.colorCodes(this,mob)+"\n\r");
			Say.append(description()+"\n\r\n\r");
		}
		for(int c=0;c<contents.size();c++)
		{
			Item item=(Item)contents.elementAt(c);
			if(item.location()==null)
				if((Sense.canBeSeenBy(item,mob)))
				{
					Say.append("     ");
					if(mob.readSysopMsgs())
						Say.append("("+item.ID()+") ");
					Say.append(item.displayText()+Sense.colorCodes(item,mob)+"\n\r");
				}
		}
		for(int i=0;i<inhabitants.size();i++)
		{
			MOB mob2=(MOB)inhabitants.elementAt(i);
			if((mob2!=mob)&&((Sense.canBeSeenBy(mob2,mob))))
			{
				if(mob.readSysopMsgs())
					Say.append("("+mob2.ID()+") ");
				Say.append(mob2.displayText()+Sense.colorCodes(mob2,mob)+"\n\r");
			}
		}
		if(Say.length()==0)
			mob.tell("You can't see anything!");
		else
			mob.tell(Say.toString());
	}
	
	public void bringMobHere(MOB mob, boolean andFollowers)
	{
		if(mob==null) return;
		
		Room oldRoom=mob.location();
		if(oldRoom!=null)
			oldRoom.delInhabitant(mob);
		addInhabitant(mob);
		mob.setLocation(this);
		
		if((andFollowers)&&(oldRoom!=null))
		for(int f=0;f<mob.numFollowers();f++)
		{
			MOB fol=mob.fetchFollower(f);
			if(fol.location()==oldRoom)
				oldRoom.delInhabitant(fol);
			addInhabitant(fol);
			fol.setLocation(this);
		}
		oldRoom.recoverRoomStats();
		recoverRoomStats();
	}
	public Exit getExit(int direction)
	{
		if((direction<0)||(direction>=exits.length))
		   return null;
		return (Exit)exits[direction];
	}
	public Room getRoom(int direction)
	{
		if((direction<0)||(direction>=doors.length))
		   return null;
		return (Room)doors[direction];
	}
	public void listExits(MOB mob)
	{
		if(!Sense.canSee(mob))
		{
			mob.tell("You can't see anything!"); 
			return; 
		}
		
		for(int i=0;i<Directions.NUM_DIRECTIONS;i++)
		{
			Exit exit=getExit(i);
			Room room=getRoom(i);
			if(room!=null)
			{
				String Dir=Directions.getDirectionName(i);
				StringBuffer Say=new StringBuffer("");
				
				if(exit!=null)
				{
					if((Sense.isInDark(room))
					&&(!Sense.canSeeInDark(mob)))
					{
						Say.append("darkness");
					}
					else
					if((exit.isOpen())||(mob.readSysopMsgs()))
					{
						if(mob.readSysopMsgs()) 
							Say.append("("+room.ID()+") ");
						Say.append(room.displayText()+Sense.colorCodes(room,mob)+" ");
					}
				
					if((Sense.canBeSeenBy(exit,mob))||(mob.readSysopMsgs()))
					{
						String display=exit.displayText();
						if(!exit.isOpen())
							display=exit.closedText();
						else
						if(display.length()>0)
							display="via "+display;
						
						if(display.length()>0)
							Say.append(display+Sense.colorCodes(exit,mob));
						if(mob.readSysopMsgs()) 
							Say.append(" ("+exit.ID()+") ");
					}
						
				}
				if(Say.length()>0)
					mob.tell(Util.padRight(Dir,5)+": "+Say);
			}
		}
	}
	
	
	private void reallySend(MOB source, Affect msg)
	{
		if(Log.debugChannelOn())
			Log.debugOut("StdRoom",((msg.source()!=null)?msg.source().ID():"null")+":"+msg.sourceCode()+":"+msg.sourceMessage()+"/"+((msg.target()!=null)?msg.target().ID():"null")+":"+msg.targetCode()+":"+msg.targetMessage()+"/"+((msg.tool()!=null)?msg.tool().ID():"null")+"/"+msg.othersCode()+":"+msg.othersMessage());
		for(int i=0;i<inhabitants.size();i++)
		{
			MOB otherMOB=(MOB)inhabitants.elementAt(i);
			if(otherMOB!=source)
				otherMOB.affect(msg);
		}
		affect(msg);
	}
	
	
	public void send(MOB source, Affect msg)
	{
		source.affect(msg);
		reallySend(source,msg);
	}
	public void sendOthers(MOB source, Affect msg)
	{
		reallySend(source,msg);
	}

	public void show(MOB source,
					 Environmental target,
					 int allCode,
					 String allMessage)
	{
		FullMsg msg=new FullMsg(source,target,null,allCode,allCode,allCode,allMessage);
		send(source,msg);
	}
	public void showOthers(MOB source,
						   Environmental target,
						   int allCode,
						   String allMessage)
	{
		FullMsg msg=new FullMsg(source,target,null,allCode,allCode,allCode,allMessage);
		reallySend(source,msg);
	}
	
	public void showSource(MOB source,
						   Environmental target,
						   int allCode,
						   String allMessage)
	{
		FullMsg msg=new FullMsg(source,target,null,allCode,allCode,allCode,allMessage);
		source.affect(msg);
	}
	
	public Exit[] exits()
	{
		return exits;
	}
	public Room[] doors()
	{
		return doors;
	}
	
	public MOB fetchInhabitant(String inhabitantID)
	{
		return (MOB)Util.fetchEnvironmental(inhabitants,inhabitantID);
	}
	public void addInhabitant(MOB mob)
	{
		inhabitants.addElement(mob);	
	}
	public int numInhabitants()
	{
		return inhabitants.size();
	}
	public boolean isInhabitant(MOB mob)
	{
		for(int i=0;i<inhabitants.size();i++)
			if(mob==inhabitants.elementAt(i))
				return true;
		return false;
	}
	public MOB fetchInhabitant(int i)
	{
		if(i<inhabitants.size())
			return (MOB)inhabitants.elementAt(i);
		return null;
	}
	public void delInhabitant(MOB mob)
	{
		inhabitants.removeElement(mob);	
	}
	
	public Item fetchItem(Item goodLocation, String itemID)
	{
		return (Item)Util.fetchAvailableItem(contents,itemID,goodLocation,false,true);
	}
	public void addItem(Item item)
	{
		item.setOwner(this);
		contents.addElement(item);
	}
	public void delItem(Item item)
	{
		contents.removeElement(item);
	}
	public int numItems()
	{
		return contents.size();
	}
	public boolean isContent(Item item)
	{
		for(int i=0;i<contents.size();i++)
			if(item==contents.elementAt(i))
				return true;
		return false;
	}
	public Item fetchItem(int i)
	{
		if(i<contents.size())
			return (Item)contents.elementAt(i);
		return null;
	}
	public Environmental fetchFromRoom(Item goodLocation, String thingName)
	{
		Environmental found=null;
		found=fetchItem(goodLocation, thingName);
		if(found==null)
			found=fetchInhabitant(thingName);
		if(found==null)
			found=Util.fetchEnvironmental(exits,thingName);
		return found;
	}
	
	public Environmental fetchFromMOBRoom(MOB mob, Item goodLocation, String thingName)
	{
		Environmental found=null;
		if(mob!=null)
			found=mob.fetchCarried(goodLocation, thingName);
		if(found==null)
			found=fetchFromRoom(goodLocation, thingName);
		if((mob!=null)&&(found==null))
			found=mob.fetchWornItem(thingName);
		if((found!=null)&&(Sense.canBeSeenBy(found,mob)))
			return found;
		return null;
	}

	public int pointsPerMove()
	{
		return 1;
	}
	
	public void addAffect(Ability to)
	{
		if(to==null) return;
		for(int i=0;i<affects.size();i++)
			if(((Ability)affects.elementAt(i)).ID().equals(to.ID()))
				return;
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void delAffect(Ability to)
	{
		int size=affects.size();
		affects.removeElement(to);
		if(affects.size()<size)
			to.setAffectedOne(null);
	}
	public int numAffects()
	{
		return affects.size();
	}
	public Ability fetchAffect(int index)
	{
		if(index <numAffects())
			return (Ability)affects.elementAt(index);
		return null;
	}
	public Ability fetchAffect(String ID)
	{
		for(int a=0;a<affects.size();a++)
			if(((Ability)affects.elementAt(a)).ID().equals(ID))
			   return (Ability)affects.elementAt(a);
		return null;
	}
	
	/** Manipulation of Behavior objects, which includes
	 * movement, speech, spellcasting, etc, etc.*/
	public void addBehavior(Behavior to)
	{
		if(to==null) return;
		for(int i=0;i<behaviors.size();i++)
			if(((Behavior)behaviors.elementAt(i)).ID().equals(to.ID()))
				return;
		behaviors.addElement(to);
	}
	public void delBehavior(Behavior to)
	{
		behaviors.removeElement(to);
	}
	public int numBehaviors()
	{
		return behaviors.size();
	}
	public Behavior fetchBehavior(int index)
	{
		if(index <numBehaviors())
			return (Behavior)behaviors.elementAt(index);
		return null;
	}
}
