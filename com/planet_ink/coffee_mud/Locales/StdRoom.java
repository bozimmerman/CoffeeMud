package com.planet_ink.coffee_mud.Locales;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Move;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;



import java.util.*;
/* 
   Copyright 2000-2010 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
@SuppressWarnings("unchecked")
public class StdRoom implements Room
{
	public String ID(){return "StdRoom";}
	protected String 	myID="";
	protected String 	name="the room";
	protected String 	displayText="Standard Room";
	protected String 	imageName=null;
	protected byte[] 	description=null;
	protected Area 		myArea=null;
	protected PhyStats 	phyStats=(PhyStats)CMClass.getCommon("DefaultPhyStats");
	protected PhyStats 	basePhyStats=(PhyStats)CMClass.getCommon("DefaultPhyStats");
	public Exit[] 		exits=new Exit[Directions.NUM_DIRECTIONS()];
	public Room[] 		doors=new Room[Directions.NUM_DIRECTIONS()];
    protected String[] 	xtraValues=null;
	protected SVector<Ability> 			affects=null;
	protected SVector<Behavior> 		behaviors=null;
    protected SVector<ScriptingEngine>  scripts=null;
	protected SVector<Item> 			contents=new SVector(1);
	protected SVector<MOB> 				inhabitants=new SVector(1);
	protected boolean mobility=true;
	protected GridLocale gridParent=null;
	protected long tickStatus=Tickable.STATUS_NOT;
	protected long expirationDate=0;

	// base move points and thirst points per round
	protected int myResource=-1;
	protected long resourceFound=0;
    protected boolean amDestroyed=false;
	protected boolean skyedYet=false;
	public StdRoom()
	{
        super();
        CMClass.bumpCounter(this,CMClass.OBJECT_LOCALE);
        xtraValues=CMProps.getExtraStatCodesHolder(this);
		basePhyStats.setWeight(2);
		recoverPhyStats();
	}
    protected void finalize(){
        for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
            setRawExit(d,null);
        CMClass.unbumpCounter(this,CMClass.OBJECT_LOCALE);
    }
    public void initializeClass(){}
	public CMObject newInstance()
	{
		try
        {
			return (Environmental)this.getClass().newInstance();
		}
		catch(Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new StdRoom();
	}

	public String roomID()
	{
		return myID	;
	}
	public String Name(){ return name;}
	public void setName(String newName){name=newName;}
	public String name()
	{
		if(phyStats().newName()!=null) return phyStats().newName();
		return name;
	}

    public String image()
    {
        if(imageName==null) 
            imageName=CMProps.getDefaultMXPImage(this);
        return imageName;
    }
    public String rawImage()
    {
        if(imageName==null) 
            return "";
        return imageName;
    }
    public void setImage(String newImage)
    {
        if((newImage==null)||(newImage.trim().length()==0))
            imageName=null;
        else
            imageName=newImage;
    }
    
	
	public boolean isGeneric(){return false;}
	
	protected void cloneFix(Room R)
	{
		basePhyStats=(PhyStats)R.basePhyStats().copyOf();
		phyStats=(PhyStats)R.phyStats().copyOf();

		contents=new SVector(1);
		inhabitants=new SVector(1);
		affects=null;
		behaviors=null;
        scripts=null;
		exits=new Exit[Directions.NUM_DIRECTIONS()];
		doors=new Room[Directions.NUM_DIRECTIONS()];
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			if(R.getRawExit(d)!=null)
				exits[d]=(Exit)R.getRawExit(d).copyOf();
			if(R.rawDoors()[d]!=null)
				doors[d]=R.rawDoors()[d];
		}
		for(int i=0;i<R.numItems();i++)
		{
			Item I2=R.getItem(i);
			if(I2!=null)
			{
				Item I=(Item)I2.copyOf();
				I.setOwner(this);
				contents.addElement(I);
			}
		}
		for(int i=0;i<numItems();i++)
		{
			Item I2=getItem(i);
			if((I2!=null)
			&&(I2.container()!=null)
			&&(!isContent(I2.container())))
				for(int ii=0;ii<R.numItems();ii++)
					if((R.getItem(ii)==I2.container())&&(ii<numItems()))
					{I2.setContainer((Container)getItem(ii)); break;}
		}
		for(int m=0;m<R.numInhabitants();m++)
		{
			MOB M2=R.fetchInhabitant(m);
			if((M2!=null)&&(M2.isSavable()))
			{
				MOB M=(MOB)M2.copyOf();
				if(M.getStartRoom()==R)
					M.setStartRoom(this);
				M.setLocation(this);
				inhabitants.addElement(M);
			}
		}
		for(int i=0;i<R.numEffects();i++)
		{
			Ability A=R.fetchEffect(i);
			if((A!=null)&&(!A.canBeUninvoked()))
				addEffect((Ability)A.copyOf());
		}
		for(Enumeration<Behavior> e=R.behaviors();e.hasMoreElements();)
		{
			Behavior B=e.nextElement();
			if(B!=null)
				addBehavior((Behavior)B.copyOf());
		}
		for(Enumeration<ScriptingEngine> e=R.scripts();e.hasMoreElements();)
		{
			ScriptingEngine SE=e.nextElement();
            if(SE!=null) addScript((ScriptingEngine)SE.copyOf());
        }
	}
	public CMObject copyOf()
	{
		try
		{
			StdRoom R=(StdRoom)this.clone();
            CMClass.bumpCounter(R,CMClass.OBJECT_LOCALE);
            R.xtraValues=(xtraValues==null)?null:(String[])xtraValues.clone();
			R.cloneFix(this);
			return R;

		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}
	public int domainType(){return Room.DOMAIN_OUTDOORS_CITY;} 
	public int domainConditions(){return Room.CONDITION_NORMAL;}
	public long expirationDate(){return expirationDate;}
	public void setExpirationDate(long time){expirationDate=time;}
	
    public void setRawExit(int direction, Environmental to)
    {
        if((direction<0)||(direction>=exits.length)) 
            return;
        Exit E=exits[direction];
        if(to instanceof Room)
            to=((Room)to).getRawExit(direction);
        
        if(E==to) return;
        if(E!=null) E.exitUsage((short)-1);
        
        if(to instanceof Exit)
        {
            ((Exit)to).exitUsage((short)1);
            exits[direction]=(Exit)to;
        }
        else
            exits[direction]=null;
        
        /**
         * cant be done
         * 
        if((E!=null)&&(E.exitUsage((short)0)==0))
            E.destroy();
         */
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
		if(CMProps.getBoolVar(CMProps.SYSTEMB_ROOMDNOCACHE)
		&&((description==null)||(description.length==0))
		&&(roomID().trim().length()>0))
		{
			String txt=CMLib.database().DBReadRoomDesc(roomID());
			if(txt==null)
			{
				Log.errOut("Unable to recover description for "+roomID()+".");
				return "";
			}
			return txt;
		}
		else
		if((description==null)||(description.length==0))
			return "";
		else
		if(CMProps.getBoolVar(CMProps.SYSTEMB_ROOMDCOMPRESS))
			return CMLib.encoder().decompressString(description);
		else
			return CMStrings.bytesToStr(description);
	}
	public void setDescription(String newDescription)
	{
		if(newDescription.length()==0)
			description=null;
		else
		if(CMProps.getBoolVar(CMProps.SYSTEMB_ROOMDCOMPRESS))
			description=CMLib.encoder().compressString(newDescription);
		else
			description=CMStrings.strToBytes(newDescription);
	}
	public String text()
	{
		return CMLib.coffeeMaker().getPropertiesStr(this,true);
	}
	public String miscTextFormat(){return CMParms.FORMAT_UNDEFINED;}
	public void setMiscText(String newMiscText)
	{
		if(newMiscText.trim().length()>0)
			CMLib.coffeeMaker().setPropertiesStr(this,newMiscText,true);
	}
	public void setRoomID(String newID)
	{
        if((myID!=null)&&(!myID.equals(newID)))
        {
            myID=newID;
            if(myArea!=null)
            { 
                // force the re-sort
                myArea.delProperRoom(this);
                myArea.addProperRoom(this);
            }
        }
        else
            myID=newID;
	}
	public Area getArea()
	{
		if(myArea==null) return CMClass.anyOldArea();
		return myArea;
	}
	public void setArea(Area newArea)
	{
        if(newArea!=myArea)
        {
            if(myArea!=null) myArea.delProperRoom(this);
            myArea=newArea;
            if(myArea!=null) myArea.addProperRoom(this);
        }
	}

	public void setGridParent(GridLocale room){gridParent=room;}
	public GridLocale getGridParent(){return gridParent;}
	
	public void giveASky(int depth)
	{
		if(skyedYet) return;
		if(depth>1000) return;
		
		skyedYet=true;
		if((roomID().length()==0)
		&&(getGridParent()!=null)
		&&(getGridParent().roomID().length()==0))
			return;
		
		if((rawDoors()[Directions.UP]==null)
		&&((domainType()&Room.INDOORS)==0)
		&&(domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
		&&(domainType()!=Room.DOMAIN_OUTDOORS_AIR)
		&&(CMProps.getIntVar(CMProps.SYSTEMI_SKYSIZE)!=0))
		{
			Exit upE=null;
			Exit dnE=CMClass.getExit("StdOpenDoorway");
			if(CMProps.getIntVar(CMProps.SYSTEMI_SKYSIZE)>0)
				upE=dnE;
			else
				upE=CMClass.getExit("UnseenWalkway");
				
					
			GridLocale sky=(GridLocale)CMClass.getLocale("EndlessThinSky");
			sky.setRoomID("");
			sky.setArea(getArea());
			rawDoors()[Directions.UP]=sky;
			setRawExit(Directions.UP,upE);
			sky.rawDoors()[Directions.DOWN]=this;
			sky.setRawExit(Directions.DOWN,dnE);
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				if((d!=Directions.UP)&&(d!=Directions.DOWN))
				{
					Room thatRoom=rawDoors()[d];
					Room thatSky=null;
					if((thatRoom!=null)&&(getRawExit(d)!=null))
					{
						thatRoom.giveASky(depth+1);
						thatSky=thatRoom.rawDoors()[Directions.UP];
					}
					if((thatSky!=null)&&(thatSky.roomID().length()==0)
					&&((thatSky instanceof EndlessThinSky)||(thatSky instanceof EndlessSky)))
					{
						sky.rawDoors()[d]=thatSky;
						Exit xo=getRawExit(d);
						if((xo==null)||(xo.hasADoor())) xo=dnE;
						sky.setRawExit(d,dnE);
						thatSky.rawDoors()[Directions.getOpDirectionCode(d)]=sky;
						if(thatRoom!=null)
						{
							xo=thatRoom.getRawExit(Directions.getOpDirectionCode(d));
							if((xo==null)||(xo.hasADoor())) xo=dnE;
							thatSky.setRawExit(Directions.getOpDirectionCode(d),xo);
						}
						((GridLocale)thatSky).clearGrid(null);
					}
				}
			sky.clearGrid(null);
		}
	}

	public void clearSky()
	{
		if(!skyedYet) return;
		Room skyGridRoom=rawDoors()[Directions.UP];
		if(skyGridRoom==null) return;
		if(((skyGridRoom.roomID()==null)||(skyGridRoom.roomID().length()==0))
		&&((skyGridRoom instanceof EndlessSky)||(skyGridRoom instanceof EndlessThinSky)))
		{
			((GridLocale)skyGridRoom).clearGrid(null);
			rawDoors()[Directions.UP]=null;
			setRawExit(Directions.UP,null);
            skyGridRoom.rawDoors()[Directions.DOWN]=null;
            skyGridRoom.setRawExit(Directions.DOWN,null);
            CMLib.map().emptyRoom(skyGridRoom,null);
            skyGridRoom.destroy();
			skyedYet=false;
		}
	}

	public List<Integer> resourceChoices(){return null;}
	public void setResource(int resourceCode)
	{
		myResource=resourceCode;
		resourceFound=0;
		if(resourceCode>=0)
			resourceFound=System.currentTimeMillis();
	}

	public int myResource()
	{
		if(resourceFound!=0)
		{
			if(resourceFound<(System.currentTimeMillis()-(30*TimeManager.MILI_MINUTE)))
				setResource(-1);
		}
		if(myResource<0)
		{
			if(resourceChoices()==null)
				setResource(-1);
			else
			{
				int totalChance=0;
				for(int i=0;i<resourceChoices().size();i++)
				{
					int resource=((Integer)resourceChoices().get(i)).intValue();
					totalChance+=RawMaterial.CODES.FREQUENCY(resource);
				}
				setResource(-1);
				int theRoll=CMLib.dice().roll(1,totalChance,0);
				totalChance=0;
				for(int i=0;i<resourceChoices().size();i++)
				{
					int resource=((Integer)resourceChoices().get(i)).intValue();
					totalChance+=RawMaterial.CODES.FREQUENCY(resource);
					if(theRoll<=totalChance)
					{
						setResource(resource);
						break;
					}
				}
			}
		}
		return myResource;
	}

	public void toggleMobility(boolean onoff){mobility=onoff;}
	public boolean getMobility(){return mobility;}
	
	protected Vector herbTwistChart(){return null;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!getArea().okMessage(this,msg))
			return false;

		if(msg.amITarget(this))
		{
			MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_EXPIRE:
			{
				if((gridParent!=null)&&(!gridParent.okMessage(myHost,msg)))
					return false;
				if(!CMLib.map().isClearableRoom(this)) return false;
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					Room R2=rawDoors()[d];
					if((R2!=null)&&(!CMLib.map().isClearableRoom(R2)))
						return false;
				}
				break;
			}
			case CMMsg.TYP_LEAVE:
				if((!CMLib.flags().allowsMovement(this))||(!getMobility()))
					return false;
				break;
			case CMMsg.TYP_FLEE:
			case CMMsg.TYP_ENTER:
				if((!CMLib.flags().allowsMovement(this))||(!getMobility()))
					return false;
				if(!mob.isMonster())
				{
					if((mob.location()!=null)&&(mob.location().getArea()!=getArea()))
						CMLib.factions().updatePlayerFactions(mob,this);
					giveASky(0);
				}
				break;
			case CMMsg.TYP_AREAAFFECT:
				// obsolete with the area objects
				break;
			case CMMsg.TYP_CAST_SPELL:
			case CMMsg.TYP_DELICATE_HANDS_ACT:
			case CMMsg.TYP_OK_ACTION:
			case CMMsg.TYP_JUSTICE:
			case CMMsg.TYP_OK_VISUAL:
			case CMMsg.TYP_SNIFF:
				break;
            case CMMsg.TYP_LIST:
            case CMMsg.TYP_BUY:
            case CMMsg.TYP_BID:
            case CMMsg.TYP_SELL:
            case CMMsg.TYP_VIEW:
            case CMMsg.TYP_VALUE:
                if(CMLib.coffeeShops().getShopKeeper(this)==null)
                {
                    mob.tell("You can't shop here.");
                    return false;
                }
                break;
			case CMMsg.TYP_SPEAK:
				break;
			default:
				if(((CMath.bset(msg.targetMajor(),CMMsg.MASK_HANDS))||(CMath.bset(msg.targetMajor(),CMMsg.MASK_MOUTH)))
                &&(msg.targetMinor()!=CMMsg.TYP_THROW))
				{
					mob.tell("You can't do that here.");
					return false;
				}
				break;
			}
		}

		if(isInhabitant(msg.source()))
			if(!msg.source().okMessage(this,msg))
				return false;
	    MsgListener N=null;
		for(int i=0;i<numInhabitants();i++)
		{
			N=fetchInhabitant(i);
			if((N!=null)
			&&(N!=msg.source())
			&&(!N.okMessage(this,msg)))
				return false;
		}
		for(int i=0;i<numItems();i++)
		{
			N=getItem(i);
			if((N!=null)&&(!N.okMessage(this,msg)))
				return false;
		}
		for(int i=0;i<numEffects();i++)
		{
			N=fetchEffect(i);
			if((N!=null)&&(!N.okMessage(this,msg)))
				return false;
		}
		for(int b=0;b<numBehaviors();b++)
		{
			N=fetchBehavior(b);
			if((N!=null)&&(!N.okMessage(this,msg)))
				return false;
		}
        for(int s=0;s<numScripts();s++)
        {
            N=fetchScript(s);
            if((N!=null)&&(!N.okMessage(this,msg)))
                return false;
        }

		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			Exit thisExit=getRawExit(d);
			if(thisExit!=null)
				if(!thisExit.okMessage(this,msg))
					return false;
		}
		return true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		getArea().executeMsg(this,msg);

		if(msg.amITarget(this))
		{
			MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_LEAVE:
			{
				if(!CMath.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
					recoverRoomStats();
				break;
			}
			case CMMsg.TYP_FLEE:
			{
				if(!CMath.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
					recoverRoomStats();
				break;
			}
			case CMMsg.TYP_ENTER:
			case CMMsg.TYP_RECALL:
			{
				if(!CMath.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
					recoverRoomStats();
                if(msg.source().playerStats()!=null)
                {
                    msg.source().playerStats().addRoomVisit(this);
                }
				break;
			}
			case CMMsg.TYP_LOOK:
            case CMMsg.TYP_EXAMINE:
                CMLib.commands().handleBeingLookedAt(msg);
				break;
			case CMMsg.TYP_SNIFF:
                CMLib.commands().handleBeingSniffed(msg);
    			break;
			case CMMsg.TYP_READ:
				if(CMLib.flags().canBeSeenBy(this,mob))
					mob.tell("There is nothing written here.");
				else
					mob.tell("You can't see that!");
				break;
			case CMMsg.TYP_AREAAFFECT:
				// obsolete with the area objects
				break;
			default:
				break;
			}
		}
		MsgListener N=null;
		for(int i=0;i<numItems();i++)
		{
			N=getItem(i);
			if(N!=null)
				N.executeMsg(this,msg);
		}

		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			N=getRawExit(d);
			if(N!=null)
				N.executeMsg(this,msg);
		}

		for(int b=0;b<numBehaviors();b++)
		{
			N=fetchBehavior(b);
			if(N!=null)
				N.executeMsg(this,msg);
		}
        
        for(int s=0;s<numScripts();s++)
        {
            N=fetchScript(s);
            if(N!=null)
                N.executeMsg(this,msg);
        }
        
		for(int a=0;a<numEffects();a++)
		{
			N=fetchEffect(a);
			if(N!=null)
				N.executeMsg(this,msg);
		}
        
		if(msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
		{
		    try
		    {
				MOB M=null;
				if(CMSecurity.isSaveFlag("ROOMMOBS"))
				{
				    if(roomID().length()==0)
				    for(int m=0;m<numInhabitants();m++)
				    {
				        M=fetchInhabitant(m);
				        if((M!=null)
				        &&(M.isSavable())
				        &&(M.getStartRoom()!=this)
				        &&(M.getStartRoom()!=null)
				        &&(M.getStartRoom().roomID().length()>0))
				            M.getStartRoom().bringMobHere(M,false);
				    }
				}
				else
				if(CMSecurity.isSaveFlag("ROOMSHOPS"))
				{
				    for(int m=0;m<numInhabitants();m++)
				    {
				        M=fetchInhabitant(m);
				        if((M instanceof ShopKeeper)
				        &&(M.isSavable())
				        &&(M.getStartRoom()!=this)
				        &&(M.getStartRoom()!=null))
				            M.getStartRoom().bringMobHere(M,false);
				    }
				}
		    }catch(NoSuchElementException e){}
		}
		
		if(msg.amITarget(this)&&(msg.targetMinor()==CMMsg.TYP_EXPIRE))
		{
			synchronized(("SYNC"+roomID()).intern())
			{
				if(gridParent!=null)
					gridParent.executeMsg(myHost,msg);
				else
				if((roomID().length()>0)
				&&(CMSecurity.isSaveFlag("ROOMMOBS")
						||CMSecurity.isSaveFlag("ROOMITEMS")
						||CMSecurity.isSaveFlag("ROOMSHOPS")))
				{
					MOB M=null;
					Vector shopmobs=new Vector(1);
					Vector bodies=new Vector(1);
			        if(CMSecurity.isSaveFlag("ROOMMOBS"))
			        {
			            for(int m=0;m<numInhabitants();m++)
			            {
			                M=fetchInhabitant(m);
			                if((M!=null)&&(M.isSavable()))
			                {
			                    M.setStartRoom(this);
			                    M.text(); // this permanizes his current state
			                }
			            }
			            CMLib.database().DBUpdateMOBs(this);
			        }
			        else
			        if(CMSecurity.isSaveFlag("ROOMSHOPS"))
			        {
			            for(int m=0;m<numInhabitants();m++)
			            {
			                M=fetchInhabitant(m);
			                if((M!=null)
			                &&(M.isSavable())
			                &&(M instanceof ShopKeeper)
			                &&(M.getStartRoom()==this))
			                    shopmobs.addElement(M);
			            }
			            if(shopmobs.size()>0)
				            CMLib.database().DBUpdateTheseMOBs(this,shopmobs);
			        }
			        if(CMSecurity.isSaveFlag("ROOMITEMS"))
			        {
				        for(int i=0;i<numItems();i++)
				        {
				            Item I=getItem(i);
				            if(I instanceof DeadBody)
				                bodies.addElement(I);
				        }
				        for(int i=0;i<bodies.size();i++)
				            ((Item)bodies.elementAt(i)).destroy();
			            CMLib.database().DBUpdateItems(this);
			        }
				}
				Area A=getArea();
				String roomID=roomID();
				setGridParent(null);
				if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSHUTTINGDOWN))
				{
					CMLib.map().emptyRoom(this,null);
		            destroy();
		            if(roomID.length()>0)
			            A.addProperRoomnumber(roomID);
				}
			}
		}
	}

	public void startItemRejuv()
	{
		for(int c=0;c<numItems();c++)
		{
			Item item=getItem(c);
			if((item!=null)&&(item.container()==null))
			{
				ItemTicker I=(ItemTicker)CMClass.getAbility("ItemRejuv");
				I.unloadIfNecessary(item);
				if((item.phyStats().rejuv()<Integer.MAX_VALUE)
				&&(item.phyStats().rejuv()>0))
					I.loadMeUp(item,this);
			}
		}
	}
    
	public long getTickStatus(){return tickStatus;}
	public boolean tick(Tickable ticking, int tickID)
	{
		tickStatus=Tickable.STATUS_START;
		if(tickID==Tickable.TICKID_ROOM_BEHAVIOR)
		{
            int numB=numBehaviors();
            Tickable T=null;
            for(int b=0;b<numB;b++)
            {
                tickStatus=Tickable.STATUS_BEHAVIOR+b;
                T=fetchBehavior(b);
                if(T!=null)
                    T.tick(ticking,tickID);
            }
            int numS=numScripts();
            if((numB<=0)&&(numS<=0)) return false;
            for(int s=0;s<numS;s++)
            {
                tickStatus=Tickable.STATUS_SCRIPT+s;
                T=fetchScript(s);
                if(T!=null)
                    T.tick(ticking,tickID);
            }
		}
		else
		{
			int a=0;
			while(a<numEffects())
			{
				Ability A=fetchEffect(a);
				if(A!=null)
				{
					int s=numEffects();
					tickStatus=Tickable.STATUS_AFFECT+a;
					if(!A.tick(ticking,tickID))
						A.unInvoke();
					if(numEffects()==s)
						a++;
				}
				else
					a++;
			}
		}
		tickStatus=Tickable.STATUS_NOT;
		return !amDestroyed();
	}

	public PhyStats phyStats()
	{
		return phyStats;
	}
	public PhyStats basePhyStats()
	{
		return basePhyStats;
	}
	public void recoverPhyStats()
	{
		basePhyStats.copyInto(phyStats);
		Area myArea=getArea();
		if(myArea!=null)
			myArea.affectPhyStats(this,phyStats());

		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if(A!=null) A.affectPhyStats(this,phyStats);
		}
		for(Item I : contents) I.affectPhyStats(this,phyStats);
		for(MOB M : inhabitants) M.affectPhyStats(this,phyStats);
		
	}
	public void recoverRoomStats()
	{
		recoverPhyStats();
		for(int m=0;m<numInhabitants();m++)
		{
			MOB M=fetchInhabitant(m);
			if(M!=null)
			{
				M.recoverCharStats();
				M.recoverPhyStats();
				M.recoverMaxState();
			}
		}
		for(int d=0;d<exits.length;d++)
		{
			Exit X=exits[d];
			if(X!=null) X.recoverPhyStats();
		}
		for(int i=0;i<numItems();i++)
		{
			Item I=getItem(i);
			if(I!=null) I.recoverPhyStats();
		}
	}

	public void setBasePhyStats(PhyStats newStats)
	{
		basePhyStats=(PhyStats)newStats.copyOf();
	}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		getArea().affectPhyStats(affected,affectableStats);
		//if(phyStats().sensesMask()>0)
		//	affectableStats.setSensesMask(affectableStats.sensesMask()|phyStats().sensesMask());
		int disposition=phyStats().disposition()
			&((Integer.MAX_VALUE-(PhyStats.IS_DARK|PhyStats.IS_LIGHTSOURCE|PhyStats.IS_SLEEPING|PhyStats.IS_HIDDEN)));
		if(disposition>0)
			affectableStats.setDisposition(affectableStats.disposition()|disposition);
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if((A!=null)&&(A.bubbleAffect()))
			   A.affectPhyStats(affected,affectableStats);
		}
	}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{
		getArea().affectCharStats(affectedMob,affectableStats);
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if((A!=null)&&(A.bubbleAffect()))
			   A.affectCharStats(affectedMob,affectableStats);
		}
	}
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{
		getArea().affectCharState(affectedMob,affectableMaxState);
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if((A!=null)&&(A.bubbleAffect()))
			   A.affectCharState(affectedMob,affectableMaxState);
		}
	}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	protected String parseVariesCodes(MOB mob, String text)
	{
		StringBuffer buf=new StringBuffer("");
		int x=text.indexOf('<');
		String elseStr=null;
		while(x>=0)
		{
			buf.append(text.substring(0,x));
			text=text.substring(x);
			boolean found=false;
			for(int i=0;i<VARIATION_CODES.length;i++)
				if(text.startsWith("<"+VARIATION_CODES[i][0]+">"))
				{
					found=true;
					int y=text.indexOf("</"+VARIATION_CODES[i][0]+">");
					String dispute=null;
					if(y>0)
					{
						dispute=text.substring(VARIATION_CODES[i][0].length()+2,y);
						text=text.substring(y+VARIATION_CODES[i][0].length()+3);
					}
					else
					{
						dispute=text.substring(VARIATION_CODES[i][0].length()+2);
						text="";
					}
					int num=CMath.s_int(VARIATION_CODES[i][1].substring(1));
					switch(VARIATION_CODES[i][1].charAt(0))
					{
					case '\n': elseStr=dispute; break;
					case '\r': buf.append(parseVariesCodes(mob,dispute)); break;
					case 'W':
						if(getArea().getClimateObj().weatherType(null)==num)
							buf.append(parseVariesCodes(mob,dispute));
						break;
					case 'C':
						if(getArea().getTimeObj().getTODCode()==num)
							buf.append(parseVariesCodes(mob,dispute));
						break;
					case 'S':
						if(getArea().getTimeObj().getSeasonCode()==num)
							buf.append(parseVariesCodes(mob,dispute));
						break;
					case 'M':
						if((mob!=null)&&(CMath.bset(mob.phyStats().disposition(),num)))
							buf.append(parseVariesCodes(mob,dispute));
						break;
					}
					break;
				}
			if(!found)
				x=text.indexOf('<',1);
			else
				x=text.indexOf('<');
		}
		if((buf.length()==0)&&(elseStr!=null))
		{
			buf.append(text);
			buf.append(parseVariesCodes(mob,elseStr));
		}
		else
			buf.append(text);
		return buf.toString();
	}

	protected String parseVaries(MOB mob, String text)
	{
		if(text.startsWith("<VARIES>"))
		{
			int x=text.indexOf("</VARIES>");
			if(x>=0)
				return parseVariesCodes(mob,text.substring(8,x))+text.substring(x+9);
			return parseVariesCodes(mob,text.substring(8));
		}
		return text;
	}

	public String roomTitle(MOB mob){
		return parseVaries(mob,displayText());
	}
	public String roomDescription(MOB mob){
		return parseVaries(mob,description());
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
		{
			for(int f=0;f<mob.numFollowers();f++)
			{
				MOB fol=mob.fetchFollower(f);
				if((fol!=null)&&(fol.location()==oldRoom))
					bringMobHere(fol,true);
			}
		}
		Rideable RI=mob.riding();
		if((RI!=null)&&(CMLib.map().roomLocation(RI)==oldRoom))
		{
			if((RI.isMobileRideBasis())
			&&((!(RI instanceof Item))||(!CMLib.flags().isMobile(RI))))
			{
				if(RI instanceof MOB)
					bringMobHere((MOB)RI,andFollowers);
				else
				if(RI instanceof Item)
				{
					if(andFollowers)
						moveItemTo((Item)RI,ItemPossessor.Expire.Player_Drop,Move.Followers);
					else
						moveItemTo((Item)RI,ItemPossessor.Expire.Player_Drop);
				}
				// refuse is good for above, since mostly moving player stuff around
			}
			else
				mob.setRiding(null);
		}
		if((oldRoom!=null)&&(mob instanceof Rideable)&&(oldRoom!=this))
		{
			Rider RR=null;
			for(int r=0;r<((Rideable)mob).numRiders();r++)
			{
				RR=((Rideable)mob).fetchRider(r);
				if(CMLib.map().roomLocation(RR)==oldRoom)
				{
					if(((Rideable)mob).isMobileRideBasis())
					{
						if((RR instanceof MOB)&&(andFollowers))
							bringMobHere((MOB)RR,andFollowers);
						else
						if(RR instanceof Item)
						{
							if(andFollowers)
								moveItemTo((Item)RR,ItemPossessor.Expire.Player_Drop,Move.Followers);
							else
								moveItemTo((Item)RR,ItemPossessor.Expire.Player_Drop);
							// refuse is good for above, since mostly moving player stuff around
						}
					}
					else
						RR.setRiding(null);
				}
			}
		}
		if(oldRoom!=null)
			oldRoom.recoverRoomStats();
		recoverRoomStats();
	}

	public void moveItemTo(Item container) { moveItemTo(container, Expire.Never);}
	public void moveItemTo(Item item, Expire expire, Move... moveFlags)
	{
		if(item==null) return;

		if(item.owner()==null) return;
		Environmental o=item.owner();
		
		List<Item> V=new Vector();
		if(item instanceof Container)
			V=((Container)item).getContents();
		if(o instanceof MOB)((MOB)o).delItem(item);
		if(o instanceof Room) ((Room)o).delItem(item);

		addItem(item, expire);
		for(int v=0;v<V.size();v++)
		{
			Item i2=(Item)V.get(v);
			if(o instanceof MOB) ((MOB)o).delItem(i2);
			if(o instanceof Room) ((Room)o).delItem(i2);
			addItem(i2);
		}
		item.setContainer(null);
		
		Rideable RI=item.riding();
		if((RI!=null)&&(o instanceof Room)&&(CMLib.map().roomLocation(RI)==o))
		{
			if((RI.isMobileRideBasis())
			&&((!(RI instanceof Item))||(!CMLib.flags().isMobile(RI))))
			{
				if(RI instanceof MOB)
					bringMobHere((MOB)RI,true);
				else
				if(RI instanceof Item)
					moveItemTo((Item)RI,null,ItemPossessor.Move.Followers);
			}
			else
				item.setRiding(null);
		}
		if(CMParms.contains(moveFlags, Move.Followers)
		&&(o instanceof Room)
		&&(item instanceof Rideable)
		&&(o!=this))
		{
			Rider RR=null;
			for(int r=0;r<((Rideable)item).numRiders();r++)
			{
				RR=((Rideable)item).fetchRider(r);
				if(CMLib.map().roomLocation(RR)==o)
				{
					if((((Rideable)item).isMobileRideBasis())
					&&(!CMLib.flags().isMobile(item)))
					{
						if(RR instanceof MOB)
							bringMobHere((MOB)RR,true);
						else
						if(RR instanceof Item)
							moveItemTo((Item)RR,ItemPossessor.Expire.Player_Drop,ItemPossessor.Move.Followers);
					}
					else
						RR.setRiding(null);
				}
			}
		}
		
		if(o instanceof Room)
			((Room)o).recoverRoomStats();
		else
		if(o instanceof MOB)
		{
			((MOB)o).recoverCharStats();
			((MOB)o).recoverPhyStats();
			((MOB)o).recoverMaxState();
		}
		recoverRoomStats();
	}
	public Exit getReverseExit(int direction)
	{
		if((direction<0)||(direction>=Directions.NUM_DIRECTIONS()))
			return null;
		Room opRoom=getRoomInDir(direction);
		if(opRoom!=null)
			return opRoom.getExitInDir(Directions.getOpDirectionCode(direction));
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

	public Room prepareRoomInDir(Room R, int direction)
	{
		if(amDestroyed)
		{
			if(roomID().length()>0)
			{
				Room thinMeR=CMClass.getLocale("ThinRoom");
				thinMeR.setRoomID(roomID());
				thinMeR.setArea(myArea);
				if(R.rawDoors()[direction]==this)
					R.rawDoors()[direction]=thinMeR;
				return thinMeR.prepareRoomInDir(R,direction);
			}
			return null;
		}
		if(expirationDate()!=0)
			setExpirationDate(System.currentTimeMillis()+WorldMap.ROOM_EXPIRATION_MILLIS);
		return this;
	}
	
	public Room getRoomInDir(int direction)
	{
		if((direction<0)||(direction>=Directions.NUM_DIRECTIONS())||(amDestroyed))
			return null;
		Room nextRoom=rawDoors()[direction];
		if(gridParent!=null) nextRoom=gridParent.prepareGridLocale(this,nextRoom,direction);
		if(nextRoom!=null) 
		{
			nextRoom=nextRoom.prepareRoomInDir(this,direction);
			if((nextRoom!=null)&&(nextRoom.amDestroyed())) 
				return null;
		}
		return nextRoom;
	}
	public Exit getExitInDir(int direction)
	{
		if((direction<0)||(direction>=Directions.NUM_DIRECTIONS()))
			return null;
		if((gridParent!=null)&&(getRawExit(direction)==null)) getRoomInDir(direction);
		return getRawExit(direction);
	}

    protected void reallyReallySend(MOB source, CMMsg msg)
	{
		if((Log.debugChannelOn())&&(CMSecurity.isDebugging("MESSAGES")))
			Log.debugOut("StdRoom",((msg.source()!=null)?msg.source().ID():"null")+":"+msg.sourceCode()+":"+msg.sourceMessage()+"/"+((msg.target()!=null)?msg.target().ID():"null")+":"+msg.targetCode()+":"+msg.targetMessage()+"/"+((msg.tool()!=null)?msg.tool().ID():"null")+"/"+msg.othersCode()+":"+msg.othersMessage());
		for(MOB otherMOB : inhabitants)
			if((otherMOB!=null)&&(otherMOB!=source))
				otherMOB.executeMsg(otherMOB,msg);
		executeMsg(source,msg);
		CMLib.commands().monitorGlobalMessage(this, msg);
	}

    protected void reallySend(MOB source, CMMsg msg, int depth)
	{
		reallyReallySend(source,msg);
		// now handle trailer msgs
		if(depth<3)
		{
            if(msg.trailerMsgs()!=null)
            {
	    		for(CMMsg msg2 : msg.trailerMsgs())
					if((msg!=msg2)
					&&((msg2.target()==null)
					   ||(!(msg2.target() instanceof MOB))
					   ||((!((MOB)msg2.target()).amDead())||(msg2.sourceMinor()==CMMsg.TYP_DEATH)))
					&&(okMessage(source,msg2)))
					{
						source.executeMsg(source,msg2);
	                    reallySend(source,msg2,depth+1);
					}
			}
		}
	}

	public void send(MOB source, CMMsg msg)
	{
		source.executeMsg(source,msg);
		reallySend(source,msg,0);
	}
	public void sendOthers(MOB source, CMMsg msg)
	{
		reallySend(source,msg,0);
	}

	public void showHappens(int allCode, String allMessage)
	{
		MOB everywhereMOB=CMLib.map().mobCreated(this);
		CMMsg msg=CMClass.getMsg(everywhereMOB,null,null,allCode,allCode,allCode,allMessage);
		sendOthers(everywhereMOB,msg);
        everywhereMOB.destroy();
	}
	public void showHappens(int allCode, Environmental like, String allMessage)
	{
		MOB everywhereMOB=CMClass.getMOB("StdMOB");
		everywhereMOB.setName(like.name());
		if(like instanceof Physical)
			everywhereMOB.setBasePhyStats(((Physical)like).phyStats());
		everywhereMOB.setLocation(this);
		everywhereMOB.recoverPhyStats();
		CMMsg msg=CMClass.getMsg(everywhereMOB,null,null,allCode,allCode,allCode,allMessage);
		send(everywhereMOB,msg);
        everywhereMOB.destroy();
	}
	public boolean show(MOB source, Environmental target, int allCode, String allMessage)
	{
		CMMsg msg=CMClass.getMsg(source,target,null,allCode,allCode,allCode,allMessage);
		if((!CMath.bset(allCode,CMMsg.MASK_ALWAYS))&&(!okMessage(source,msg)))
			return false;
		send(source,msg);
		return true;
	}
	public boolean show(MOB source, 
                        Environmental target, 
                        Environmental tool, 
                        int allCode, 
                        String allMessage)
	{
		CMMsg msg=CMClass.getMsg(source,target,tool,allCode,allCode,allCode,allMessage);
		if((!CMath.bset(allCode,CMMsg.MASK_ALWAYS))&&(!okMessage(source,msg)))
			return false;
		send(source,msg);
		return true;
	}
    public boolean show(MOB source, 
                        Environmental target, 
                        Environmental tool, 
                        int srcCode, 
                        int tarCode, 
                        int othCode, 
                        String allMessage)
    {
        CMMsg msg=CMClass.getMsg(source,target,tool,srcCode,tarCode,othCode,allMessage);
        if((!CMath.bset(srcCode,CMMsg.MASK_ALWAYS))&&(!okMessage(source,msg)))
            return false;
        send(source,msg);
        return true;
    }
    public boolean show(MOB source,
                        Environmental target,
                        Environmental tool,
                        int allCode,
                        String srcMessage,
                        String tarMessage,
                        String othMessage)
    {
        CMMsg msg=CMClass.getMsg(source,target,tool,allCode,srcMessage,allCode,tarMessage,allCode,othMessage);
        if((!CMath.bset(allCode,CMMsg.MASK_ALWAYS))&&(!okMessage(source,msg)))
            return false;
        send(source,msg);
        return true;
    }
    public boolean show(MOB source,
                        Environmental target,
                        Environmental tool,
                        int srcCode,
                        String srcMessage,
                        int tarCode,
                        String tarMessage,
                        int othCode,
                        String othMessage)
    {
        CMMsg msg=CMClass.getMsg(source,target,tool,srcCode,srcMessage,tarCode,tarMessage,othCode,othMessage);
        if((!CMath.bset(srcCode,CMMsg.MASK_ALWAYS))&&(!okMessage(source,msg)))
            return false;
        send(source,msg);
        return true;
    }
	public boolean showOthers(MOB source,
						      Environmental target,
						      int allCode,
						      String allMessage)
	{
		CMMsg msg=CMClass.getMsg(source,target,null,allCode,allCode,allCode,allMessage);
		if((!CMath.bset(allCode,CMMsg.MASK_ALWAYS))&&(!okMessage(source,msg)))
			return false;
		reallySend(source,msg,0);
		return true;
	}
	public boolean showOthers(MOB source,
						   Environmental target,
						   Environmental tool,
						   int allCode,
						   String allMessage)
	{
		CMMsg msg=CMClass.getMsg(source,target,tool,allCode,allCode,allCode,allMessage);
		if((!CMath.bset(allCode,CMMsg.MASK_ALWAYS))&&(!okMessage(source,msg)))
			return false;
		reallySend(source,msg,0);
		return true;
	}
	public boolean showSource(MOB source,
						   Environmental target,
						   int allCode,
						   String allMessage)
	{
		CMMsg msg=CMClass.getMsg(source,target,null,allCode,allCode,allCode,allMessage);
		if((!CMath.bset(allCode,CMMsg.MASK_ALWAYS))&&(!okMessage(source,msg)))
			return false;
		source.executeMsg(source,msg);
		return true;
	}
	public boolean showSource(MOB source,
						   Environmental target,
						   Environmental tool,
						   int allCode,
						   String allMessage)
	{
		CMMsg msg=CMClass.getMsg(source,target,tool,allCode,allCode,allCode,allMessage);
		if((!CMath.bset(allCode,CMMsg.MASK_ALWAYS))&&(!okMessage(source,msg)))
			return false;
		source.executeMsg(source,msg);
		return true;
	}

	public Exit getRawExit(int dir)
	{
	    if(dir<exits.length)
	        return exits[dir];
	    return null;
	}
	public Room[] rawDoors()
	{
		return doors;
	}
    
    public boolean isSavable(){
    	return ((roomID().length()>0)
			    &&((getArea()==null)
					|| (!CMath.bset(getArea().flags(),Area.FLAG_INSTANCE_CHILD)))
				&&(CMLib.flags().isSavable(this)));
	}
	public void setSavable(boolean truefalse){ CMLib.flags().setSavable(this, truefalse);}

	public void destroy()
	{
        CMLib.map().registerWorldObjectDestroyed(getArea(),this,this);
		try{
		for(int a=numEffects()-1;a>=0;a--)
			fetchEffect(a).unInvoke();
		}catch(Exception e){}
		while(numEffects()>0)
			delEffect(fetchEffect(0));
		try{
            Vector V=new Vector();
            for(int v=0;v<numInhabitants();v++)
                V.addElement(fetchInhabitant(v));
            for(int v=0;v<V.size();v++)
                ((MOB)V.elementAt(v)).destroy();
            if(numInhabitants()>0)
            for(int v=0;v<V.size();v++)
                delInhabitant((MOB)V.elementAt(v));
		}catch(Exception e){}
		while(numBehaviors()>0)
			delBehavior(fetchBehavior(0));
        while(numScripts()>0)
            delScript(fetchScript(0));
        CMLib.threads().deleteTick(this,Tickable.TICKID_ROOM_BEHAVIOR);
		try{
            Vector V=new Vector();
            for(int v=0;v<numItems();v++)
                V.addElement(getItem(v));
            for(int v=0;v<V.size();v++)
                ((Item)V.elementAt(v)).destroy();
            if(numItems()>0)
            for(int v=0;v<V.size();v++)
                delItem((Item)V.elementAt(v));
		}catch(Exception e){}
		if(this instanceof GridLocale)
			((GridLocale)this).clearGrid(null);
		clearSky();
		if((roomID().length()==0)&&(rawDoors()!=null))
		{
			Room roomDir=null;
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				roomDir=rawDoors()[d];
	            if((roomDir!=null)&&(roomDir.rawDoors()!=null))
	            for(int d2=Directions.NUM_DIRECTIONS()-1;d2>=0;d2--)
	                if(roomDir.rawDoors()[d2]==this)
	                {
	                	roomDir.rawDoors()[d2]=null;
	                	roomDir.setRawExit(d2,null);
	                }
			}
		}
		CMLib.threads().deleteTick(this,-1);
        imageName=null;
        setArea(null); // this actually deletes the room from the cache map
        basePhyStats=(PhyStats)CMClass.getCommon("DefaultPhyStats");
        phyStats=basePhyStats;
        for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
            setRawExit(d,null);
        exits=new Exit[Directions.NUM_DIRECTIONS()];
        doors=new Room[Directions.NUM_DIRECTIONS()];
        affects=null;
        behaviors=null;
        scripts=null;
        contents=new SVector(1);
        inhabitants=new SVector(1);
        gridParent=null;
        amDestroyed=true;
	}
    public boolean amDestroyed(){return amDestroyed;}

	public boolean isHere(Environmental E)
	{
		if(E instanceof Item)
			return isContent((Item)E);
		else
		if(E instanceof MOB)
			return isInhabitant((MOB)E);
		else
		if(E instanceof Exit)
		{
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				if(getRawExit(d)==E) return true;
		}
		else
		if(E instanceof Room)
			return isSameRoom(E);
		else
		if(E instanceof Ability)
			return fetchEffect(E.ID())!=null;
		else
		if(E instanceof Behavior)
			return fetchBehavior(E.ID())!=null;
		return false;
	}
	
	public MOB fetchInhabitant(String inhabitantID)
	{
		MOB mob=(MOB)CMLib.english().fetchEnvironmental(inhabitants,inhabitantID,true);
		if(mob==null)
			mob=(MOB)CMLib.english().fetchEnvironmental(inhabitants,inhabitantID, false);
		return mob;
	}
	public List<MOB> fetchInhabitants(String inhabitantID)
	{
		List inhabs=CMLib.english().fetchEnvironmentals(inhabitants,inhabitantID,true);
		if(inhabs.size()==0)
			inhabs=CMLib.english().fetchEnvironmentals(inhabitants,inhabitantID, false);
		return inhabs;
	}
	public void addInhabitant(MOB mob)
	{
		inhabitants.addElement(mob);
	}
	public Enumeration<MOB> inhabitants()
	{ 
		return inhabitants.elements();
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

	public Item findItem(String itemID)
	{
		Item item=(Item)CMLib.english().fetchEnvironmental(contents,itemID,true);
		if(item==null) item=(Item)CMLib.english().fetchEnvironmental(contents,itemID,false);
		return item;
	}
	public Enumeration<Item> items() { return contents.elements();}
	public Item findItem(Item goodLocation, String itemID)
	{
		Item item=CMLib.english().fetchAvailableItem(contents,itemID,goodLocation,Wearable.FILTER_ANY,true);
		if(item==null) item=CMLib.english().fetchAvailableItem(contents,itemID,goodLocation,Wearable.FILTER_ANY,false);
		return item;
	}
	public List<Item> findItems(Item goodLocation, String itemID)
	{
		List<Item> items=CMLib.english().fetchAvailableItems(contents,itemID,goodLocation,Wearable.FILTER_ANY,true);
		if(items.size()==0)
			items=CMLib.english().fetchAvailableItems(contents,itemID,goodLocation,Wearable.FILTER_ANY,false);
		return items;
	}
	public List<Item> findItems(String itemID)
	{
		List items=CMLib.english().fetchEnvironmentals(contents,itemID,true);
		if(items.size()==0)
			items=CMLib.english().fetchEnvironmentals(contents,itemID, false);
		return items;
	}
	public void addItem(Item item, Expire expire)
	{
		addItem(item);
		if(expire == null) expire=Expire.Never;
		int numMins = 0;
		switch(expire)
		{
		case Monster_EQ: numMins = CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_MONSTER_EQ); break;
		case Monster_Body: numMins = CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_MONSTER_BODY); break;
		case Player_Body: numMins = CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_PLAYER_BODY); break;
		case Player_Drop: numMins = CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_PLAYER_DROP); break;
		case Resource: numMins = CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_RESOURCE); break;
		case Never: break;
		}
		if(numMins==0)
			item.setExpirationDate(0);
		else
			item.setExpirationDate(System.currentTimeMillis()+(numMins * TimeManager.MILI_MINUTE));
	}
	
	public void addItem(Item item)
	{
		item.setOwner(this);
		contents.addElement(item);
		item.recoverPhyStats();
	}
	public void delItem(Item item)
	{
		contents.removeElement(item);
		item.recoverPhyStats();
	}
	public int numItems()
	{
		return contents.size();
	}
	public boolean isContent(Item item)
	{
		return contents.contains(item);
	}
	public Item getItem(int i)
	{
		try
		{
            return (Item)contents.elementAt(i);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public String getContextName(Environmental E)
	{
		if(E instanceof Exit)
		{
			for(int e=0;e<exits.length;e++)
				if(exits[e]==E)
					return Directions.getDirectionName(e);
			return E.Name();
		}
		else
		if(E instanceof MOB)
		{
            String ctxName=CMLib.english().getContextName(inhabitants,E);
            if(ctxName!=null) return ctxName;
		}
		else
		if(E instanceof Item)
		{
            String ctxName=CMLib.english().getContextName(inhabitants,E);
            if(ctxName!=null) return ctxName;
		}
		else
		if(E!=null)
			return E.name();
		return "nothing";
	}
	
	public PhysicalAgent fetchFromMOBRoomItemExit(MOB mob, Item goodLocation, String thingName, int wornFilter)
	{
		PhysicalAgent found=null;
		String newThingName=CMLib.lang().preItemParser(thingName);
		if(newThingName!=null) thingName=newThingName;
		boolean mineOnly=(mob!=null)&&(thingName.toUpperCase().trim().startsWith("MY "));
		if(mineOnly) thingName=thingName.trim().substring(3).trim();
		if((mob!=null)&&(wornFilter!=Wearable.FILTER_WORNONLY))
			found=mob.fetchCarried(goodLocation, thingName);
		if((found==null)&&(!mineOnly))
		{
			found=(Exit)CMLib.english().fetchEnvironmental(Arrays.asList(exits),thingName,true);
			if(found==null) found=CMLib.english().fetchAvailableItem(contents,thingName,goodLocation,wornFilter,true);
			if(found==null)	found=(Exit)CMLib.english().fetchEnvironmental(Arrays.asList(exits),thingName,false);
			if(found==null) found=CMLib.english().fetchAvailableItem(contents,thingName,goodLocation,wornFilter,false);
			if((found!=null)&&(CMLib.flags().canBeSeenBy(found,mob)))
				return found;
			while((found!=null)&&(!CMLib.flags().canBeSeenBy(found,mob)))
			{
				newThingName=CMLib.english().bumpDotNumber(thingName);
				if(!newThingName.equals(thingName))
				{
					thingName=newThingName;
					found=fetchFromRoomFavorItems(goodLocation, thingName);
				}
				else
					found=null;
			}
		}

		if((found!=null) // the smurfy well/gate exception
		&&(found instanceof Item)
		&&(goodLocation==null)
		&&(found.displayText().length()==0)
		&&(thingName.indexOf('.')<0))
		{
			PhysicalAgent visibleItem=null;
			visibleItem=(Exit)CMLib.english().fetchEnvironmental(Arrays.asList(exits),thingName,false);
			if(visibleItem==null)
				visibleItem=fetchFromMOBRoomItemExit(null,null,thingName+".2",wornFilter);
			if(visibleItem!=null)
				found=visibleItem;
		}
		if((mob!=null)&&(found==null)&&(wornFilter!=Wearable.FILTER_UNWORNONLY))
			found=mob.fetchWornItem(thingName);
		if(found==null)
		{
			newThingName=CMLib.lang().failedItemParser(thingName);
			if(newThingName!=null) return fetchFromMOBRoomItemExit(mob,goodLocation,newThingName,wornFilter); 
		}
		return found;
	}
	public PhysicalAgent fetchFromRoomFavorItems(Item goodLocation, String thingName)
	{
		// def was Wearable.FILTER_UNWORNONLY;
		String newThingName=CMLib.lang().preItemParser(thingName);
		if(newThingName!=null) thingName=newThingName;
		PhysicalAgent found=null;
		SVector V=contents.copyOf();
		for(int e=0;e<exits.length;e++)
		    if(exits[e]!=null)V.addElement(exits[e]);
		V.addAll(inhabitants);
		found=(PhysicalAgent)CMLib.english().fetchAvailable(V,thingName,goodLocation,Wearable.FILTER_ANY,true);
		if(found==null) found=(PhysicalAgent)CMLib.english().fetchAvailable(V,thingName,goodLocation,Wearable.FILTER_ANY,false);

		if((found!=null) // the smurfy well exception
		&&(found instanceof Item)
		&&(goodLocation==null)
		&&(found.displayText().length()==0)
		&&(thingName.indexOf('.')<0))
		{
			PhysicalAgent visibleItem=fetchFromRoomFavorItems(null,thingName+".2");
			if(visibleItem!=null)
				found=visibleItem;
		}
		if(found==null)
		{
			newThingName=CMLib.lang().failedItemParser(thingName);
			if(newThingName!=null) return fetchFromRoomFavorItems(goodLocation,newThingName); 
		}
		return found;
	}

	public PhysicalAgent fetchFromRoomFavorMOBs(Item goodLocation, String thingName)
	{
		// def was Wearable.FILTER_UNWORNONLY;
		String newThingName=CMLib.lang().preItemParser(thingName);
		if(newThingName!=null) thingName=newThingName;
		PhysicalAgent found=null;
		SVector V=inhabitants.copyOf();
		V.addAll(contents);
		for(int e=0;e<exits.length;e++)
		    if(exits[e]!=null)
		    	V.addElement(exits[e]);
		found=(PhysicalAgent)CMLib.english().fetchAvailable(V,thingName,goodLocation,Wearable.FILTER_ANY,true);
		if(found==null) found=(PhysicalAgent)CMLib.english().fetchAvailable(V,thingName,goodLocation,Wearable.FILTER_ANY,false);
		if(found==null)
		{
			newThingName=CMLib.lang().failedItemParser(thingName);
			if(newThingName!=null) return fetchFromRoomFavorMOBs(goodLocation,newThingName); 
		}
		return found;
	}

	public PhysicalAgent fetchFromMOBRoomFavorsItems(MOB mob, Item goodLocation, String thingName, int wornFilter)
	{
		return fetchFromMOBRoom(mob,goodLocation,thingName,wornFilter,true);
	}
	public PhysicalAgent fetchFromMOBRoomFavorsMOBs(MOB mob, Item goodLocation, String thingName, int wornFilter)
	{
		return fetchFromMOBRoom(mob,goodLocation,thingName,wornFilter,false);
	}
	private PhysicalAgent fetchFromMOBRoom(MOB mob, Item goodLocation, String thingName, int wornFilter, boolean favorItems)
	{
		PhysicalAgent found=null;
		String newThingName=CMLib.lang().preItemParser(thingName);
		if(newThingName!=null) thingName=newThingName;
		boolean mineOnly=(mob!=null)&&(thingName.toUpperCase().trim().startsWith("MY "));
		if(mineOnly) thingName=thingName.trim().substring(3).trim();
		if((mob!=null)&&(favorItems)&&(wornFilter!=Wearable.FILTER_WORNONLY))
			found=mob.fetchCarried(goodLocation, thingName);
		if((found==null)&&(!mineOnly))
		{
			if(favorItems)
				found=fetchFromRoomFavorItems(goodLocation, thingName);
			else
				found=fetchFromRoomFavorMOBs(goodLocation, thingName);
			if((found!=null)&&(CMLib.flags().canBeSeenBy(found,mob)))
				return found;
			while((found!=null)&&(!CMLib.flags().canBeSeenBy(found,mob)))
			{
				newThingName=CMLib.english().bumpDotNumber(thingName);
				if(!newThingName.equals(thingName))
				{
					thingName=newThingName;
					if(favorItems)
						found=fetchFromRoomFavorItems(goodLocation, thingName);
					else
						found=fetchFromRoomFavorMOBs(goodLocation, thingName);
				}
				else
					found=null;
			}
		}
		if((mob!=null)&&(!favorItems)&&(wornFilter!=Wearable.FILTER_WORNONLY))
			found=mob.fetchCarried(goodLocation, thingName);
		if((mob!=null)&&(found==null)&&(wornFilter!=Wearable.FILTER_UNWORNONLY))
			found=mob.fetchWornItem(thingName);
        if(found==null)
        for(int d=0;d<exits.length;d++)
            if((exits[d]!=null)&&(thingName.equalsIgnoreCase(Directions.getDirectionName(d))))
                return getExitInDir(d);
		if(found==null)
		{
			newThingName=CMLib.lang().failedItemParser(thingName);
			if(newThingName!=null) return fetchFromMOBRoom(mob,goodLocation,newThingName,wornFilter,favorItems); 
		}
		return found;
	}

	public int pointsPerMove(MOB mob)
	{	return getArea().getClimateObj().adjustMovement(phyStats().weight(),mob,this);	}
	protected int baseThirst(){return 1;}
	public int thirstPerRound(MOB mob)
	{
		switch(domainConditions())
		{
		case Room.CONDITION_HOT:
			return getArea().getClimateObj().adjustWaterConsumption(baseThirst()+1,mob,this);
		case Room.CONDITION_WET:
			return getArea().getClimateObj().adjustWaterConsumption(baseThirst()-1,mob,this);
		}
		return getArea().getClimateObj().adjustWaterConsumption(baseThirst(),mob,this);
	}
	public int minRange(){return Integer.MIN_VALUE;}
	public int maxRange(){return((domainType()&Room.INDOORS)>0)?1:10;}

	public void addEffect(Ability to)
	{
		if(to==null) return;
		if(fetchEffect(to.ID())!=null) return;
		if(affects==null) affects=new SVector(1);
		if(affects.contains(to)) return;
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void addNonUninvokableEffect(Ability to)
	{
		if(to==null) return;
		if(fetchEffect(to.ID())!=null) return;
		if(affects==null) affects=new SVector(1);
		to.makeNonUninvokable();
		to.makeLongLasting();
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void delEffect(Ability to)
	{
		if(affects==null) return;
		if(affects.remove(to))
		{
			to.setAffectedOne(null);
			if(affects.size()==0)
			    affects=new SVector(1);
		}
	}
	public int numEffects()
	{
		if(affects==null) return 0;
		return affects.size();
	}
	public Ability fetchEffect(int index)
	{
		if(affects==null) return null;
		try
		{
			return (Ability)affects.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Ability fetchEffect(String ID)
	{
		if(affects==null) return null;
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
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
		if(behaviors==null) behaviors=new SVector(1);
		for(Behavior B : behaviors)
			if((B!=null)&&(B.ID().equals(to.ID())))
			   return;
		if(behaviors.size()==0)
			CMLib.threads().startTickDown(this,Tickable.TICKID_ROOM_BEHAVIOR,1);
		to.startBehavior(this);
		behaviors.addElement(to);
	}
	public void delBehavior(Behavior to)
	{
		if(behaviors==null) return;
		if(behaviors.remove(to))
		{
    		if(behaviors.size()==0)
    		    behaviors=new SVector(1);
            if(((behaviors==null)||(behaviors.size()==0))&&((scripts==null)||(scripts.size()==0)))
    			CMLib.threads().deleteTick(this,Tickable.TICKID_ROOM_BEHAVIOR);
		}
	}
	public int numBehaviors()
	{
		if(behaviors==null) return 0;
		return behaviors.size();
	}
    public Enumeration<Behavior> behaviors() { return (behaviors==null)?EmptyEnumeration.INSTANCE:behaviors.elements();}
	public Behavior fetchBehavior(int index)
	{
		if(behaviors==null) return null;
		try
		{
			return (Behavior)behaviors.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Behavior fetchBehavior(String ID)
	{
		if(behaviors==null) return null;
		for(Behavior B : behaviors)
			if((B!=null)&&(B.ID().equalsIgnoreCase(ID)))
				return B;
		return null;
	}

    /** Manipulation of the scripts list */
    public void addScript(ScriptingEngine S)
    {
        if(scripts==null) scripts=new SVector(1);
        if(S==null) return;
        if(!scripts.contains(S)) 
        {
            ScriptingEngine S2=null;
            for(int s=0;s<scripts.size();s++)
            {
                S2=(ScriptingEngine)scripts.elementAt(s);
                if((S2!=null)&&(S2.getScript().equalsIgnoreCase(S.getScript())))
                    return;
            }
            if(scripts.size()==0)
                CMLib.threads().startTickDown(this,Tickable.TICKID_ROOM_BEHAVIOR,1);
            scripts.addElement(S);
        }
    }
    public void delScript(ScriptingEngine S)
    {
        if(scripts!=null)
        {
            if(scripts.remove(S))
            {
                if(scripts.size()==0)
                    scripts=new SVector(1);
                if(((behaviors==null)||(behaviors.size()==0))&&((scripts==null)||(scripts.size()==0)))
                    CMLib.threads().deleteTick(this,Tickable.TICKID_ROOM_BEHAVIOR);
            }
        }
    }
    public int numScripts(){return (scripts==null)?0:scripts.size();}
    public Enumeration<ScriptingEngine> scripts() { return (scripts==null)?EmptyEnumeration.INSTANCE:scripts.elements();}
    public ScriptingEngine fetchScript(int x){try{return (ScriptingEngine)scripts.elementAt(x);}catch(Exception e){} return null;}
    
    public int getSaveStatIndex(){return (xtraValues==null)?getStatCodes().length:getStatCodes().length-xtraValues.length;}
	protected static final String[] STDCODES={"CLASS","DISPLAY","DESCRIPTION","TEXT"};
    private static String[] codes=null;
    public String[] getStatCodes()
    {
        if(codes==null)
            codes=CMProps.getStatCodesList(STDCODES,this);
        return codes; 
    }
    public boolean isStat(String code){ return CMParms.indexOf(getStatCodes(),code.toUpperCase().trim())>=0;}
	protected int getCodeNum(String code){ return CMParms.indexOf(codes, code.toUpperCase());}
	public String getStat(String code){
		switch(getCodeNum(code))
		{
		case 0: return CMClass.classID(this);
		case 1: return displayText();
		case 2: return description();
		case 3: return text();
        default: return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: return;
		case 1: setDisplayText(val); break;
		case 2: setDescription(val); break;
		case 3: setMiscText(val); break;
        default:
            CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
            break;
		}
	}
    public boolean sameAs(Environmental E)
    {
        if(!(E instanceof StdRoom)) return false;
        String[] codes=getStatCodes();
        for(int i=0;i<codes.length;i++)
            if(!E.getStat(codes[i]).equals(getStat(codes[i])))
                return false;
        return true;
    }
	public boolean isSameRoom(Object O)
	{
	    if(O==this) return true;
	    if(O instanceof Room)
	    {
	        if(CMLib.map().getExtendedRoomID(this).equals(CMLib.map().getExtendedRoomID((Room)O)))
	            return true;
	    }
	    return false;
	}
}
