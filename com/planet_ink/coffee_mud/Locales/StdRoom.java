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
	protected String myAreaID="StdArea";
	protected String displayText="Standard Room";
	protected String miscText="";
	protected String description="";
	private String objectID=myID;
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
		return Generic.getPropertiesStr(this,true);
	}
	public void setMiscText(String newMiscText)
	{
		miscText="";
		if(newMiscText.trim().length()>0)
			Generic.setPropertiesStr(this,newMiscText,true);
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
			Exit o=(Exit)CMClass.getExit("StdOpenDoorway").newInstance();
			EndlessSky sky=new EndlessSky();
			sky.setAreaID(room.getAreaID());
			sky.setID("");
			room.doors()[Directions.UP]=sky;
			room.exits()[Directions.UP]=o;
			sky.doors()[Directions.DOWN]=room;
			sky.exits()[Directions.DOWN]=o;
			CMMap.map.addElement(sky);
		}
	}

	public boolean okAffect(Affect affect)
	{
		if(affect.amITarget(this))
		{
			MOB mob=(MOB)affect.source();

			switch(affect.targetMinor())
			{
			case Affect.TYP_LEAVE:
			case Affect.TYP_FLEE:
			case Affect.TYP_ENTER:
				if((!skyedYet)
				&&(affect.targetMinor()==Affect.TYP_ENTER)
				&&(affect.source()!=null)
				&&(!affect.source().isMonster()))
					giveASky(this);
				break;
			case Affect.TYP_AREAAFFECT:
				if(affect.source().location()==this)
				{
					for(int m=0;m<CMMap.map.size();m++)
					{
						Room otherRoom=(Room)CMMap.map.elementAt(m);
						if((otherRoom!=null)&&(otherRoom.getAreaID().equals(getAreaID())))
						   if(!otherRoom.okAffect(affect)) return false;
					}
				}
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
			Exit thisExit=exits()[i];
			if(thisExit!=null)
				if(!thisExit.okAffect(affect))
					return false;
		}
		return true;
	}

	public void affect(Affect affect)
	{
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
				if(affect.source().location()==this)
				{
					for(int m=0;m<CMMap.map.size();m++)
					{
						Room otherRoom=(Room)CMMap.map.elementAt(m);
						if((otherRoom!=null)&&(otherRoom.getAreaID().equals(getAreaID())))
						   otherRoom.affect(affect);
					}
				}
				break;
			default:
				break;
			}
		}

		for(int i=0;i<contents.size();i++)
			((Item)contents.elementAt(i)).affect(affect);

		for(int i=0;i<Directions.NUM_DIRECTIONS;i++)
		{
			Exit thisExit=exits()[i];
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

			for(int b=0;b<behaviors.size();b++)
			{
				Behavior B=(Behavior)behaviors.elementAt(b);
				B.tick(this,tickID);
			}
		}
		else
		{
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
		for(int b=0;b<inhabitants.size();b++)
		{
			MOB mob=(MOB)inhabitants.elementAt(b);
			mob.affectEnvStats(this,envStats);
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
			mob.recoverMaxState();
		}
		for(int i=0;i<contents.size();i++)
		{
			Item item=(Item)contents.elementAt(i);
			item.recoverEnvStats();
		}
	}

	public void setBaseEnvStats(EnvStats newBaseEnvStats)
	{
		baseEnvStats=newBaseEnvStats.cloneStats();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
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
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{}//rooms will never be asked this, so this method should always do NOTHING

	public void look(MOB mob)
	{
		StringBuffer Say=new StringBuffer("");
		if(mob.readSysopMsgs())
		{
			Say.append("^BArea  :^N("+myAreaID+")"+"\n\r");
			Say.append("^BLocale:^N("+CMClass.className(this)+")"+"\n\r");
			Say.append("^H("+ID()+")^N ");
		}
		if((Sense.canBeSeenBy(this,mob))||(mob.readSysopMsgs()))
		{
			Say.append("^R" + displayText()+Sense.colorCodes(this,mob)+"^L\n\r");
			Say.append("^L" + description()+"^N\n\r\n\r");
		}
		for(int c=0;c<contents.size();c++)
		{
			Item item=(Item)contents.elementAt(c);
			if(item.location()==null)
				if((Sense.canBeSeenBy(item,mob))&&((item.displayText().length()>0)||(mob.readSysopMsgs())))
				{
					Say.append("     ");
					if(mob.readSysopMsgs())
						Say.append("^H("+CMClass.className(item)+")^N ");

					Say.append("^I");
					if(item.displayText().length()>0)
						Say.append(item.displayText());
					else
						Say.append(item.name());
					Say.append(" "+Sense.colorCodes(item,mob)+"^N\n\r");
				}
		}
		for(int i=0;i<inhabitants.size();i++)
		{
			MOB mob2=(MOB)inhabitants.elementAt(i);
			if((mob2!=mob)&&((Sense.canBeSeenBy(mob2,mob)))&&((mob2.displayText().length()>0)||(mob.readSysopMsgs())))
			{
				if(mob.readSysopMsgs())
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
			if(fol.location()==oldRoom)
				oldRoom.delInhabitant(fol);
			addInhabitant(fol);
			fol.setLocation(this);
		}
		oldRoom.recoverRoomStats();
		recoverRoomStats();
	}

	public Exit getReverseExit(int direction)
	{
		if(direction>=Directions.NUM_DIRECTIONS)
			return null;
		Room opRoom=doors()[direction];
		if(opRoom!=null)
			return opRoom.exits()[Directions.getOpDirectionCode(direction)];
		else
			return null;
	}
	public Exit getPairedExit(int direction)
	{
		Exit opExit=getReverseExit(direction);
		Exit myExit=exits()[direction];
		if((myExit==null)||(opExit==null))
			return null;
		if(myExit.hasADoor()!=opExit.hasADoor())
			return null;
		return opExit;
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
			Exit exit=exits()[i];
			Room room=doors()[i];

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
		for(int i=0;i<inhabitants.size();i++)
		{
			MOB otherMOB=(MOB)inhabitants.elementAt(i);
			if(otherMOB!=source)
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
			if(!fetchInhabitant(i).isMonster())
				numUsers++;
		return numUsers;
	}
	public MOB fetchPCInhabitant(int which)
	{
		int numUsers=0;
		for(int i=0;i<numInhabitants();i++)
			if(!fetchInhabitant(i).isMonster())
				if(numUsers==which)
					return fetchInhabitant(i);
				else
					numUsers++;
		return null;
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

	public int pointsPerMove()
	{
		if((domainType&Room.INDOORS)>0)
			return 1;
		else
			return 3;
	}

	public void addAffect(Ability to)
	{
		if(to==null) return;
		for(int i=0;i<affects.size();i++)
			if(affects.elementAt(i)==to)
				return;
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void addNonUninvokableAffect(Ability to)
	{
		if(to==null) return;
		for(int i=0;i<affects.size();i++)
			if(affects.elementAt(i)==to)
				return;
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
		if(index <numBehaviors())
			return (Behavior)behaviors.elementAt(index);
		return null;
	}
}
