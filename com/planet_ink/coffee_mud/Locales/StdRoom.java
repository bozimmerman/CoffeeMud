package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;
public class StdRoom
	implements Room
{
	protected String myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	protected String name="room";
	protected String displayText="Standard Room";
	protected String miscText="";
	protected String description="";
	private String objectID=myID;
	protected Area myArea=null;
	protected EnvStats envStats=new DefaultEnvStats();
	protected EnvStats baseEnvStats=new DefaultEnvStats();
	public Exit[] exits=new Exit[Directions.NUM_DIRECTIONS];
	public Room[] doors=new Room[Directions.NUM_DIRECTIONS];
	protected Vector affects=new Vector();
	protected Vector behaviors=new Vector();
	protected Vector contents=new Vector();
	protected Vector inhabitants=new Vector();
	protected int domainType=Room.DOMAIN_OUTDOORS_CITY;
	protected int domainCondition=Room.CONDITION_NORMAL;
	protected int maxRange=-1; // -1 = use indoor/outdoor algorithm

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
	public boolean isGeneric(){return false;}
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
			if(E.rawExits()[d]!=null)
				exits[d]=(Exit)E.rawExits()[d].copyOf();
			if(E.rawDoors()[d]!=null)
				doors[d]=E.rawDoors()[d];

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
		return Generic.getPropertiesStr(this,true);
	}
	public void setMiscText(String newMiscText)
	{
		miscText="";
		if(newMiscText.trim().length()>0)
			Generic.setPropertiesStr(this,newMiscText,true);
	}
	public void setID(String newID)
	{
		myID=newID;
	}
	public Area getArea()
	{
		if(myArea==null) return (Area)CMClass.getAreaType("StdArea");
		return myArea;
	}
	public void setArea(Area newArea)
	{
		myArea=newArea;
	}

	protected void giveASky(Room room)
	{
		skyedYet=true;
		if((room.rawDoors()[Directions.UP]==null)
		&&((room.domainType()&Room.INDOORS)==0)
		&&(room.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
		&&(room.domainType()!=Room.DOMAIN_OUTDOORS_AIR))
		{
			Exit o=(Exit)CMClass.getExit("StdOpenDoorway").newInstance();
			EndlessSky sky=new EndlessSky();
			sky.setArea(room.getArea());
			sky.setID("");
			room.rawDoors()[Directions.UP]=sky;
			room.rawExits()[Directions.UP]=o;
			sky.rawDoors()[Directions.DOWN]=room;
			sky.rawExits()[Directions.DOWN]=o;
			CMMap.addRoom(sky);
		}
	}

	public boolean okAffect(Affect affect)
	{
		if(!getArea().okAffect(affect))
			return false;
		
		if(affect.amITarget(this))
		{
			MOB mob=(MOB)affect.source();
			switch(affect.targetMinor())
			{
			case Affect.TYP_LEAVE:
				break;
			case Affect.TYP_FLEE:
			case Affect.TYP_ENTER:
				if((!skyedYet)&&(!mob.isMonster()))
					giveASky(this);
				break;
			case Affect.TYP_AREAAFFECT:
				// obsolete with the area objects
				break;
			case Affect.TYP_CAST_SPELL:
				break;
			default:
				if((Util.bset(affect.targetMajor(),Affect.AFF_TOUCHED))
				||(Util.bset(affect.targetMajor(),Affect.AFF_CONSUMED)))
				{
					mob.tell("You can't do that here.");
					return false;
				}
				break;
			}
		}

		for(int i=0;i<numInhabitants();i++)
		{
			MOB inhab=fetchInhabitant(i);
			if((inhab!=null)&&(!inhab.okAffect(affect)))
				return false;
		}
		for(int i=0;i<numItems();i++)
		{
			Item content=fetchItem(i);
			if((content!=null)&&(!content.okAffect(affect)))
				return false;
		}
		for(int i=0;i<numAffects();i++)
		{
			Ability A=fetchAffect(i);
			if((A!=null)&&(!A.okAffect(affect)))
				return false;
		}
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if((B!=null)&&(!B.okAffect(this,affect)))
				return false;
		}

		for(int i=0;i<Directions.NUM_DIRECTIONS;i++)
		{
			Exit thisExit=rawExits()[i];
			if(thisExit!=null)
				if(!thisExit.okAffect(affect))
					return false;
		}
		return true;
	}

	public void affect(Affect affect)
	{
		getArea().affect(affect);
		
		if(affect.amITarget(this))
		{
			MOB mob=(MOB)affect.source();
			switch(affect.targetMinor())
			{
			case Affect.TYP_LEAVE:
			{
				recoverRoomStats();
				break;
			}
			case Affect.TYP_FLEE:
			{
				recoverRoomStats();
				break;
			}
			case Affect.TYP_ENTER:
			case Affect.TYP_RECALL:
			{
				recoverRoomStats();
				break;
			}
			case Affect.TYP_EXAMINESOMETHING:
				look(mob);
				break;
			case Affect.TYP_READSOMETHING:
				if(Sense.canBeSeenBy(this,mob))
					mob.tell("There is nothing written here.");
				else
					mob.tell("You can't see that!");
				break;
			case Affect.TYP_AREAAFFECT:
				// obsolete with the area objects
				break;
			default:
				break;
			}
		}

		for(int i=0;i<numItems();i++)
		{
			Item content=fetchItem(i);
			if(content!=null)
				content.affect(affect);
		}

		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Exit thisExit=rawExits()[d];
			if(thisExit!=null)
				thisExit.affect(affect);
		}

		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if(B!=null)
				B.affect(this,affect);
		}

		for(int a=0;a<numAffects();a++)
		{
			Ability A=fetchAffect(a);
			if(A!=null)
				A.affect(affect);
		}
		
	}

	public void startItemRejuv()
	{
		for(int c=0;c<numItems();c++)
		{
			Item item=fetchItem(c);
			if((item!=null)&&(item.location()==null))
			{
				ItemTicker I=(ItemTicker)CMClass.getAbility("ItemRejuv");
				I.unloadIfNecessary(item);
				if((item.envStats().rejuv()<Integer.MAX_VALUE)&&(item.envStats().rejuv()>0))
					I.loadMeUp(item,this);
			}
		}
	}

	public boolean tick(int tickID)
	{
		if(tickID==Host.ROOM_BEHAVIOR_TICK)
		{
			if(behaviors.size()==0) return false;
			for(int b=0;b<numBehaviors();b++)
			{
				Behavior B=fetchBehavior(b);
				if(B!=null) B.tick(this,tickID);
			}
		}
		else
		{
			int a=0;
			while(a<numAffects())
			{
				Ability A=fetchAffect(a);
				if(A!=null)
				{
					int s=affects.size();
					if(!A.tick(tickID))
						A.unInvoke();
					if(affects.size()==s)
						a++;
				}
				else
					a++;
			}
		}
		return true;
	}

	public EnvStats envStats()
	{
		return envStats;
	}
	public EnvStats baseEnvStats()
	{
		return baseEnvStats;
	}
	public void recoverEnvStats()
	{
		envStats=baseEnvStats.cloneStats();
		Area myArea=getArea();
		if(myArea!=null)
			myArea.affectEnvStats(this,envStats());
		for(int a=0;a<numAffects();a++)
		{
			Ability affect=fetchAffect(a);
			if(affect!=null)
				affect.affectEnvStats(this,envStats);
		}
		for(int i=0;i<numItems();i++)
		{
			Item item=fetchItem(i);
			if(item!=null)
				item.affectEnvStats(this,envStats);
		}
		for(int m=0;m<numInhabitants();m++)
		{
			MOB mob=fetchInhabitant(m);
			if(mob!=null)
				mob.affectEnvStats(this,envStats);
		}
	}
	public void recoverRoomStats()
	{
		recoverEnvStats();
		for(int m=0;m<numInhabitants();m++)
		{
			MOB mob=fetchInhabitant(m);
			if(mob!=null)
			{
				mob.recoverCharStats();
				mob.recoverEnvStats();
				mob.recoverMaxState();
			}
		}
		for(int i=0;i<numItems();i++)
		{
			Item item=fetchItem(i);
			if(item!=null)
				item.recoverEnvStats();
		}
	}

	public void setBaseEnvStats(EnvStats newBaseEnvStats)
	{
		baseEnvStats=newBaseEnvStats.cloneStats();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		getArea().affectEnvStats(affected,affectableStats);
		if(envStats().sensesMask()>0)
			affectableStats.setSensesMask(affectableStats.sensesMask()|envStats().sensesMask());
		int disposition=envStats().disposition();
		if((disposition&EnvStats.IS_DARK)==EnvStats.IS_DARK)
			disposition=disposition-EnvStats.IS_DARK;
		if((disposition&EnvStats.IS_LIGHT)==EnvStats.IS_LIGHT)
			disposition=disposition-EnvStats.IS_LIGHT;
		if(disposition>0)
			affectableStats.setDisposition(affectableStats.disposition()|disposition);
	}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{
		getArea().affectCharStats(affectedMob,affectableStats);
	}
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{
		getArea().affectCharState(affectedMob,affectableMaxState);
	}
	
	private void look(MOB mob)
	{
		StringBuffer Say=new StringBuffer("");
		if((mob.getBitmap()&MOB.ATT_SYSOPMSGS)>0)
		{
			if(myArea!=null)
				Say.append("^BArea  :^N("+myArea.name()+")"+"\n\r");
			Say.append("^BLocale:^N("+CMClass.className(this)+")"+"\n\r");
			Say.append("^H("+ID()+")^N ");
		}
		if((Sense.canBeSeenBy(this,mob))||((mob.getBitmap()&MOB.ATT_SYSOPMSGS)>0))
		{
			Say.append("^R" + displayText()+Sense.colorCodes(this,mob)+"^L\n\r");
			Say.append("^L" + description()+"^N\n\r\n\r");
		}
		
		Vector viewItems=new Vector();
		for(int c=0;c<numItems();c++)
		{
			Item item=fetchItem(c);
			if((item!=null)&&(item.location()==null))
				viewItems.addElement(item);
		}
		Say.append(ExternalPlay.niceLister(mob,viewItems,false));
		
		for(int i=0;i<numInhabitants();i++)
		{
			MOB mob2=fetchInhabitant(i);
			if((mob2!=null)
			   &&(mob2!=mob)
			   &&((Sense.canBeSeenBy(mob2,mob)))
			   &&((mob2.displayText().length()>0)
				  ||((mob.getBitmap()&MOB.ATT_SYSOPMSGS)>0)))
			{
				if((mob.getBitmap()&MOB.ATT_SYSOPMSGS)>0)
					Say.append("^H("+CMClass.className(mob2)+")^N ");

				Say.append("^M");
				if(mob2.displayText().length()>0)
					Say.append(mob2.displayText());
				else
					Say.append(mob2.name());
				Say.append(Sense.colorCodes(mob2,mob)+"^N\n\r");
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
			if(fol!=null)
			{
				if(fol.location()==oldRoom)
					oldRoom.delInhabitant(fol);
				addInhabitant(fol);
				fol.setLocation(this);
			}
		}
		oldRoom.recoverRoomStats();
		recoverRoomStats();
	}

	public Exit getReverseExit(int direction)
	{
		if(direction>=Directions.NUM_DIRECTIONS)
			return null;
		Room opRoom=getRoomInDir(direction);
		if(opRoom!=null)
			return opRoom.getExitInDir(Directions.getOpDirectionCode(direction));
		else
			return null;
	}
	public Exit getPairedExit(int direction)
	{
		Exit opExit=getReverseExit(direction);
		Exit myExit=getExitInDir(direction);
		if((myExit==null)||(opExit==null))
			return null;
		if(myExit.hasADoor()!=opExit.hasADoor())
			return null;
		return opExit;
	}

	public Room getRoomInDir(int direction)
	{
		if(direction>=Directions.NUM_DIRECTIONS)
			return null;
		Room nextRoom=rawDoors()[direction];
		if(nextRoom!=null)
		{
			if(nextRoom instanceof GridLocale)
			{
				Room realRoom=((GridLocale)nextRoom).getAltRoomFrom(this);
				if(realRoom!=null) return realRoom;
			}
		}
		return nextRoom;
	}
	public Exit getExitInDir(int direction)
	{
		if(direction>=Directions.NUM_DIRECTIONS)
			return null;
		return rawExits()[direction];
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
			Exit exit=getExitInDir(i);
			Room room=getRoomInDir(i);

			String Dir=Directions.getDirectionName(i);
			StringBuffer Say=new StringBuffer("");
			if(exit!=null)
				Say=exit.viewableText(mob, room);
			if(Say.length()>0)
				mob.tell("^D" + Util.padRight(Dir,5)+":^N ^d"+Say+"^N");
		}
	}

	private void reallyReallySend(MOB source, Affect msg)
	{
		if(Log.debugChannelOn())
			Log.debugOut("StdRoom",((msg.source()!=null)?msg.source().ID():"null")+":"+msg.sourceCode()+":"+msg.sourceMessage()+"/"+((msg.target()!=null)?msg.target().ID():"null")+":"+msg.targetCode()+":"+msg.targetMessage()+"/"+((msg.tool()!=null)?msg.tool().ID():"null")+"/"+msg.othersCode()+":"+msg.othersMessage());
		for(int i=0;i<numInhabitants();i++)
		{
			MOB otherMOB=fetchInhabitant(i);
			if((otherMOB!=null)&&(otherMOB!=source))
				otherMOB.affect(msg);
		}
		affect(msg);
	}
	
	private void reallySend(MOB source, Affect msg)
	{
		reallyReallySend(source,msg);
		// now handle trailer msgs
		if(msg.trailerMsgs()!=null)
		{
			for(int i=0;i<msg.trailerMsgs().size();i++)
			{
				Affect affect=(Affect)msg.trailerMsgs().elementAt(i);
				if((affect!=msg)
				&&((affect.target()==null)
				   ||(!(affect.target() instanceof MOB))
				   ||(!((MOB)affect.target()).amDead()))
				&&(okAffect(affect)))
				{
					source.affect(affect);
					reallyReallySend(source,affect);
				}
			}
		}
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

	public Exit[] rawExits()
	{
		return exits;
	}
	public Room[] rawDoors()
	{
		return doors;
	}

	public MOB fetchInhabitant(String inhabitantID)
	{
		MOB mob=(MOB)CoffeeUtensils.fetchEnvironmental(inhabitants,inhabitantID,true);
		if(mob==null)
			mob=(MOB)CoffeeUtensils.fetchEnvironmental(inhabitants,inhabitantID, false);
		return mob;
	}
	public void addInhabitant(MOB mob)
	{
		inhabitants.addElement(mob);
	}
	public int numInhabitants()
	{
		return inhabitants.size();
	}
	public int numPCInhabitants()
	{
		int numUsers=0;
		for(int i=0;i<numInhabitants();i++)
		{
			MOB inhab=fetchInhabitant(i);
			if((inhab!=null)
			&&(!inhab.isMonster()))
				numUsers++;
		}
		return numUsers;
	}
	public MOB fetchPCInhabitant(int which)
	{
		int numUsers=0;
		for(int i=0;i<numInhabitants();i++)
		{
			MOB inhab=fetchInhabitant(i);
			if((inhab!=null)
			&&(!inhab.isMonster()))
			{
				if(numUsers==which)
					return inhab;
				else
					numUsers++;
			}
		}
		return null;
	}
	public boolean isInhabitant(MOB mob)
	{
		return inhabitants.contains(mob);
	}
	public MOB fetchInhabitant(int i)
	{
		try
		{
			return (MOB)inhabitants.elementAt(i);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public void delInhabitant(MOB mob)
	{
		inhabitants.removeElement(mob);
	}

	public Item fetchItem(Item goodLocation, String itemID)
	{
		Item item=(Item)CoffeeUtensils.fetchAvailableItem(contents,itemID,goodLocation,false,true,true);
		if(item==null) item=(Item)CoffeeUtensils.fetchAvailableItem(contents,itemID,goodLocation,false,true,false);
		return item;
	}
	public void addItem(Item item)
	{
		item.setOwner(this);
		contents.addElement(item);
		item.recoverEnvStats();
	}
	public void delItem(Item item)
	{
		contents.removeElement(item);
		item.recoverEnvStats();
	}
	public int numItems()
	{
		return contents.size();
	}
	public boolean isContent(Item item)
	{
		return contents.contains(item);
	}
	public Item fetchItem(int i)
	{
		try
		{
			return (Item)contents.elementAt(i);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Environmental fetchFromRoomFavorItems(Item goodLocation, String thingName)
	{
		Environmental found=null;
		if(found==null) found=CoffeeUtensils.fetchAvailableItem(contents,thingName,goodLocation,false,true,true);
		if(found==null)	found=CoffeeUtensils.fetchEnvironmental(exits,thingName,true);
		if(found==null)	found=CoffeeUtensils.fetchEnvironmental(inhabitants,thingName,true);
		if(found==null) found=CoffeeUtensils.fetchAvailableItem(contents,thingName,goodLocation,false,true,false);
		if(found==null)	found=CoffeeUtensils.fetchEnvironmental(exits,thingName,false);
		if(found==null)	found=CoffeeUtensils.fetchEnvironmental(inhabitants,thingName,false);
		
		if((found!=null) // the smurfy well exception
		&&(found instanceof Item)
		&&(goodLocation==null)
		&&(found.displayText().length()==0)
		&&(thingName.indexOf(".")<0))
		{
			Environmental visibleItem=fetchFromRoomFavorItems(null,thingName+".2");
			if(visibleItem!=null)
				found=visibleItem;
		}
		return found;
	}

	public Environmental fetchFromRoomFavorMOBs(Item goodLocation, String thingName)
	{
		Environmental found=null;
		if(found==null)	found=CoffeeUtensils.fetchEnvironmental(inhabitants,thingName,true);
		if(found==null)	found=CoffeeUtensils.fetchAvailableItem(contents,thingName,goodLocation,false,true,true);
		if(found==null)	found=CoffeeUtensils.fetchEnvironmental(exits,thingName,true);
		if(found==null)	found=CoffeeUtensils.fetchEnvironmental(inhabitants,thingName,false);
		if(found==null) found=CoffeeUtensils.fetchAvailableItem(contents,thingName,goodLocation,false,true,false);
		if(found==null)	found=CoffeeUtensils.fetchEnvironmental(exits,thingName,false);
		return found;
	}

	public Environmental fetchFromMOBRoomFavorsItems(MOB mob, Item goodLocation, String thingName)
	{
		Environmental found=null;
		if(mob!=null)
			found=mob.fetchCarried(goodLocation, thingName);
		if(found==null)
		{
			found=fetchFromRoomFavorItems(goodLocation, thingName);
			if((found!=null)&&(Sense.canBeSeenBy(found,mob)))
				return found;
			else
				found=null;
		}
		if((mob!=null)&&(found==null))
			found=mob.fetchWornItem(thingName);
		return found;
	}

	public int pointsPerMove(MOB mob)
	{
		switch(domainType())
		{
		case Room.DOMAIN_OUTDOORS_AIR:
		case Room.DOMAIN_OUTDOORS_CITY:
			return getArea().adjustMovement(1,mob,this);
		case Room.DOMAIN_OUTDOORS_PLAINS:
			return getArea().adjustMovement(2,mob,this);
		case Room.DOMAIN_OUTDOORS_ROCKS:
			return getArea().adjustMovement(3,mob,this);
		case Room.DOMAIN_OUTDOORS_UNDERWATER:
		case Room.DOMAIN_OUTDOORS_WATERSURFACE:
			return getArea().adjustMovement(3,mob,this);
		case Room.DOMAIN_OUTDOORS_WOODS:
			return getArea().adjustMovement(3,mob,this);
		case Room.DOMAIN_INDOORS_CAVE:
			return getArea().adjustMovement(2,mob,this);
		case Room.DOMAIN_INDOORS_MAGIC:
		case Room.DOMAIN_INDOORS_STONE:
		case Room.DOMAIN_INDOORS_WOOD:
			return getArea().adjustMovement(1,mob,this);
		}
		return getArea().adjustMovement(2,mob,this);
	}
	public int thirstPerRound(MOB mob)
	{
		switch(domainType())
		{
		case Room.DOMAIN_OUTDOORS_UNDERWATER:
		case Room.DOMAIN_OUTDOORS_WATERSURFACE:
			return getArea().adjustWaterConsumption(0,mob,this);
		}
		switch(domainConditions())
		{
		case Room.CONDITION_HOT:
			return getArea().adjustWaterConsumption(2,mob,this);
		case Room.CONDITION_WET:
			return getArea().adjustWaterConsumption(0,mob,this);
		}
		return getArea().adjustWaterConsumption(1,mob,this);
	}
	public int minRange(){return Integer.MIN_VALUE;}
	public int maxRange()
	{
		if(maxRange>=0)
			return maxRange;
		else
		if((domainType&Room.INDOORS)>0)
			return 1;
		else
			return 10;
	}

	public void addAffect(Ability to)
	{
		if(to==null) return;
		if(affects.contains(to)) return;
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void addNonUninvokableAffect(Ability to)
	{
		if(to==null) return;
		if(affects.contains(to)) return;
		to.makeNonUninvokable();
		to.makeLongLasting();
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
		try
		{
			return (Ability)affects.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Ability fetchAffect(String ID)
	{
		for(int a=0;a<numAffects();a++)
		{
			Ability A=fetchAffect(a);
			if((A!=null)&&(A.ID().equals(ID)))
			   return A;
		}
		return null;
	}

	/** Manipulation of Behavior objects, which includes
	 * movement, speech, spellcasting, etc, etc.*/
	public void addBehavior(Behavior to)
	{
		if(to==null) return;
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if((B!=null)&&(B.ID().equals(to.ID())))
			   return;
		}
		if(behaviors.size()==0)
			ExternalPlay.startTickDown(this,Host.ROOM_BEHAVIOR_TICK,1);
		to.startBehavior(this);
		behaviors.addElement(to);
	}
	public void delBehavior(Behavior to)
	{
		behaviors.removeElement(to);
		if(behaviors.size()==0)
			ExternalPlay.deleteTick(this,Host.ROOM_BEHAVIOR_TICK);
	}
	public int numBehaviors()
	{
		return behaviors.size();
	}
	public Behavior fetchBehavior(int index)
	{
		try
		{
			return (Behavior)behaviors.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
}
