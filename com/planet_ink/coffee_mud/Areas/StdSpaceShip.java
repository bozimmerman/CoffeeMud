package com.planet_ink.coffee_mud.Areas;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdSpaceShip implements Area, SpaceObject
{
	public long[] coordinates=new long[3];
	public long[] coordinates(){return coordinates;}
	public void setCoords(long[] coords){coordinates=coords;}
	public double[] direction=new double[2];
	public double[] direction(){return direction;}
	public void setDirection(double[] dir){direction=dir;}
	public long velocity=0;
	public long velocity(){return velocity;}
	public void setVelocity(long v){velocity=v;}
	protected static Climate climateObj=null;
	public void setClimateObj(Climate obj){climateObj=obj;}
	public Climate getClimateObj()
	{
		if(climateObj==null)
		{
			climateObj=new DefaultClimate();
			climateObj.setCurrentWeatherType(Climate.WEATHER_CLEAR);
			climateObj.setNextWeatherType(Climate.WEATHER_CLEAR);
		}
		return climateObj;
	}
	protected TimeClock localClock=new DefaultTimeClock();
	public TimeClock getTimeObj(){return localClock;}
	public void setTimeObj(TimeClock obj){localClock=obj;}
	
	public SpaceObject spaceTarget=null;
	public SpaceObject knownTarget(){return spaceTarget;}
	public void setKnownTarget(SpaceObject O){spaceTarget=O;}
	public SpaceObject spaceSource=null;
	public SpaceObject knownSource(){return spaceSource;}
	public void setKnownSource(SpaceObject O){spaceSource=O;}
	public SpaceObject orbiting=null;
	public SpaceObject orbiting(){return orbiting;}
	public void setOrbiting(SpaceObject O){orbiting=O;}
	
	public String ID(){	return "StdSpaceShip";}
	protected String name="a space ship";
	protected String description="";
	protected String miscText="";
	protected Vector myRooms=null;
	protected boolean mobility=true;
	protected long tickStatus=Tickable.STATUS_NOT;
	private Boolean roomSemaphore=new Boolean(true);
	private int[] statData=null;
	protected String author=""; // will be used for owner, I guess.
	public void setAuthorID(String authorID){author=authorID;}
	public String getAuthorID(){return author;}

	protected EnvStats envStats=new DefaultEnvStats();
	protected EnvStats baseEnvStats=new DefaultEnvStats();

	protected Vector affects=new Vector();
	protected Vector behaviors=new Vector();
	public int climateType(){return Area.CLIMASK_NORMAL;}
	public void setClimateType(int newClimateType){}

	public StdSpaceShip()
	{
	}
	public String name()
	{
		if(envStats().newName()!=null) return envStats().newName();
		return name;
	}
	public void setName(String newName){
		name=newName;
		localClock.setLoadName(newName);
	}
	public String Name(){return name;}
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
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if(A!=null)
				A.affectEnvStats(this,envStats);
		}
	}
	public void setBaseEnvStats(EnvStats newBaseEnvStats)
	{
		baseEnvStats=newBaseEnvStats.cloneStats();
	}
	public void setNextWeatherType(int weatherCode){}
	public void setCurrentWeatherType(int weatherCode){}
	public int getTechLevel(){return Area.TECH_HIGH;}
	public void setTechLevel(int level){}

	public String getArchivePath(){return "";}
	public void setArchivePath(String pathFile){}
	
	public boolean getMobility(){return mobility;}
	public void toggleMobility(boolean onoff){mobility=onoff;}
	public boolean amISubOp(String username){return false;}
	public String getSubOpList(){return "";}
	public void setSubOpList(String list){}
	public void addSubOp(String username){}
	public void delSubOp(String username){}
	public Environmental newInstance(){	return new StdSpaceShip();}
	public boolean isGeneric(){return false;}
	private void cloneFix(Area E)
	{
		baseEnvStats=E.baseEnvStats().cloneStats();
		envStats=E.envStats().cloneStats();

		affects=new Vector();
		behaviors=new Vector();
		for(int b=0;b<E.numBehaviors();b++)
		{
			Behavior B=E.fetchBehavior(b);
			if(B!=null)
				behaviors.addElement(B);
		}
		for(int a=0;a<E.numEffects();a++)
		{
			Ability A=E.fetchEffect(a);
			if(A!=null)
				affects.addElement(A);
		}
	}
	public Environmental copyOf()
	{
		try
		{
			StdSpaceShip E=(StdSpaceShip)this.clone();
			E.cloneFix(this);
			return E;

		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}
	public String displayText(){return "";}
	public void setDisplayText(String newDisplayText){}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	public String text()
	{
		return CoffeeMaker.getPropertiesStr(this,true);
	}
	public void setMiscText(String newMiscText)
	{
		miscText="";
		if(newMiscText.trim().length()>0)
			CoffeeMaker.setPropertiesStr(this,newMiscText,true);
	}

	public String description()
	{ return description;}
	public void setDescription(String newDescription)
	{ description=newDescription;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if((B!=null)&&(!B.okMessage(this,msg)))
				return false;
		}
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if((A!=null)&&(!A.okMessage(this,msg)))
				return false;
		}
		if((!mobility)||(!Sense.canMove(this)))
		{
			if((msg.sourceMinor()==CMMsg.TYP_ENTER)
			||(msg.sourceMinor()==CMMsg.TYP_LEAVE)
			||(msg.sourceMinor()==CMMsg.TYP_FLEE))
				return false;
		}
		if((Util.bset(msg.sourceCode(),CMMsg.MASK_MAGIC))
		||(Util.bset(msg.targetCode(),CMMsg.MASK_MAGIC))
		||(Util.bset(msg.othersCode(),CMMsg.MASK_MAGIC)))
		{
			Room room=null;
			if((msg.target()!=null)
			&&(msg.target() instanceof MOB)
			&&(((MOB)msg.target()).location()!=null))
				room=((MOB)msg.target()).location();
			else
			if((msg.source()!=null)
			&&(msg.source().location()!=null))
				room=msg.source().location();
			if(room!=null)
			{
				if(room.getArea()==this)
					room.showHappens(CMMsg.MSG_OK_VISUAL,"Magic doesn't seem to work here.");
				else
					room.showHappens(CMMsg.MSG_OK_VISUAL,"Magic doesn't seem to work there.");
			}

			return false;
		}
		return true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if(B!=null)
				B.executeMsg(this,msg);
		}
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if(A!=null)
				A.executeMsg(this,msg);
		}
	}

	private boolean stopTicking=false;
	public void tickControl(boolean start)
	{
		if(start)
		{
			stopTicking=false;
			CMClass.ThreadEngine().startTickDown(this,MudHost.TICK_AREA,1);
		}
		else
			stopTicking=true;

	}

	public int[] addMaskAndReturn(int[] one, int[] two)
	{
		if(one.length!=two.length)
			return one;
		int[] returnable=new int[one.length];
		for(int o=0;o<one.length;o++)
			returnable[o]=one[o]+two[o];
		return returnable;
	}

	public long getTickStatus(){ return tickStatus;}
	public boolean tick(Tickable ticking, int tickID)
	{
		if(stopTicking) return false;
		tickStatus=Tickable.STATUS_START;
		if(tickID==MudHost.TICK_AREA)
		{
			getTimeObj().tick(this,tickID);
			for(int b=0;b<numBehaviors();b++)
			{
				tickStatus=Tickable.STATUS_BEHAVIOR+b;
				Behavior B=fetchBehavior(b);
				if(B!=null)
					B.tick(ticking,tickID);
			}

			int a=0;
			while(a<numEffects())
			{
				Ability A=fetchEffect(a);
				if(A!=null)
				{
					tickStatus=Tickable.STATUS_AFFECT+a;
					int s=affects.size();
					if(!A.tick(ticking,tickID))
						A.unInvoke();
					if(affects.size()==s)
						a++;
				}
				else
					a++;
			}
		}
		tickStatus=Tickable.STATUS_NOT;
		return true;
	}

	public String getWeatherDescription(){return "There is no weather here.";}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if(envStats().sensesMask()>0)
			affectableStats.setSensesMask(affectableStats.sensesMask()|envStats().sensesMask());
		int disposition=envStats().disposition()
			&((Integer.MAX_VALUE-(EnvStats.IS_SLEEPING|EnvStats.IS_HIDDEN)));
		if(disposition>0)
			affectableStats.setDisposition(affectableStats.disposition()|disposition);
		affectableStats.setWeight(affectableStats.weight()+envStats().weight());
	}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{}
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{}

	public void addNonUninvokableEffect(Ability to)
	{
		if(to==null) return;
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if((A!=null)&&(A==to))
				return;
		}
		to.makeNonUninvokable();
		to.makeLongLasting();
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void addEffect(Ability to)
	{
		if(to==null) return;
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if((A!=null)&&(A==to))
				return;
		}
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void delEffect(Ability to)
	{
		int size=affects.size();
		affects.removeElement(to);
		if(affects.size()<size)
			to.setAffectedOne(null);
	}
	public int numEffects()
	{
		return affects.size();
	}
	public Ability fetchEffect(int index)
	{
		try
		{
			return (Ability)affects.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Ability fetchEffect(String ID)
	{
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if((A!=null)&&(A.ID().equals(ID)))
			   return A;
		}
		return null;
	}

	public void fillInAreaRooms()
	{
		clearMap();
	}

	public void fillInAreaRoom(Room R){}

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
	public int maxRange(){return Integer.MAX_VALUE;}
	public int minRange(){return Integer.MIN_VALUE;}

	public int[] getAreaIStats()
	{
		if(!CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED))
			return null;
		getAreaStats();
		return statData;
	}
	public StringBuffer getAreaStats(){	return new StringBuffer("This is a space ship");}

	public Behavior fetchBehavior(int index)
	{
		try
		{
			return (Behavior)behaviors.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Behavior fetchBehavior(String ID)
	{
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if((B!=null)&&(B.ID().equalsIgnoreCase(ID)))
				return B;
		}
		return null;
	}

	public void clearMap(){}

	public int mapSize()
	{
		synchronized(roomSemaphore)
		{
			if(myRooms!=null)
				return myRooms.size();
			else
				makeMap();
			return myRooms.size();
		}
	}
	public int numberOfIDedRooms()
	{
		int num=0;
		for(Enumeration e=getMap();e.hasMoreElements();)
		{
			Room R=(Room)e.nextElement();
			if(R.roomID().length()>0)
				if(R instanceof GridLocale)
					num+=((GridLocale)R).getAllRooms().size();
				else
					num++;
		}
		return num;
	}
	public Room getRandomRoom()
	{
		synchronized(roomSemaphore)
		{
			if(myRooms==null) makeMap();
			if(mapSize()==0) return null;
			return (Room)myRooms.elementAt(Dice.roll(1,mapSize(),-1));
		}
	}
	public Enumeration getMap()
	{
		synchronized(roomSemaphore)
		{
			if(myRooms!=null) return myRooms.elements();
			makeMap();
			return myRooms.elements();
		}
	}
	private void makeMap()
	{
		synchronized(roomSemaphore)
		{
			if(myRooms!=null) return;
			Vector myMap=new Vector();
			for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(R.getArea()==this)
					myMap.addElement(R);
			}
			myRooms=myMap;
		}
	}
	public Vector getSubOpVectorList(){	return new Vector();}

    public void addChildToLoad(String str){}
    public void addParentToLoad(String str){}

	// Children
	public void initChildren() {}
	public Enumeration getChildren() {return new Vector().elements(); }
	public String getChildrenList() { return "";}
	public int getNumChildren() { return 0; }
	public Area getChild(int num) { return null; }
	public Area getChild(String named) { return null;}
	public boolean isChild(Area named) { return false;}
	public boolean isChild(String named) { return false;}
	public void addChild(Area Adopted) {}
	public void removeChild(Area Disowned) {}
	public void removeChild(int Disowned) {}
	public boolean canChild(Area newChild) { return false;}
	public void initParents() {}
	public Enumeration getParents() { return new Vector().elements();}
	public String getParentsList() { return "";}
	public int getNumParents() { return 0; }
	public Area getParent(int num) { return null; }
	public Area getParent(String named) { return null;}
	public boolean isParent(Area named) { return false;}
	public boolean isParent(String named) { return false;}
	public void addParent(Area Adopted) {}
	public void removeParent(Area Disowned) {}
	public void removeParent(int Disowned) {}
	public boolean canParent(Area newParent) { return false;}

	private static final String[] CODES={"CLASS","CLIMATE","DESCRIPTION","TEXT","TECHLEVEL"};
	public String[] getStatCodes(){return CODES;}
	private int getCodeNum(String code){
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
	public String getStat(String code){
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return ""+climateType();
		case 2: return description();
		case 3: return text();
		case 4: return ""+getTechLevel();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: return;
		case 1: setClimateType(Util.s_int(val)); break;
		case 2: setDescription(val); break;
		case 3: setMiscText(val); break;
		case 4: setTechLevel(Util.s_int(val)); break;
		}
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdSpaceShip)) return false;
		for(int i=0;i<CODES.length;i++)
			if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
				return false;
		return true;
	}
}
