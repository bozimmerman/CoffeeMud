package com.planet_ink.coffee_mud.Areas;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
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
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
@SuppressWarnings("unchecked")
public class StdSpaceShip implements Area, SpaceObject, SpaceShip
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
	public long accelleration=0;
	public long accelleration(){return accelleration;}
	public void setAccelleration(long v){accelleration=v;}
    public void initializeClass(){}
	protected static Climate climateObj=null;
    protected String[] xtraValues=null;
	protected Vector parents=null;
    protected Vector parentsToLoad=new Vector();
    protected Vector blurbFlags=new Vector();
    protected String imageName="";
	protected RoomnumberSet properRoomIDSet=null;
	public void setClimateObj(Climate obj){climateObj=obj;}
	public Climate getClimateObj()
	{
		if(climateObj==null)
		{
			climateObj=(Climate)CMClass.getCommon("DefaultClimate");
			climateObj.setCurrentWeatherType(Climate.WEATHER_CLEAR);
			climateObj.setNextWeatherType(Climate.WEATHER_CLEAR);
		}
		return climateObj;
	}
	protected TimeClock localClock=(TimeClock)CMClass.getCommon("DefaultTimeClock");
	public TimeClock getTimeObj(){return localClock;}
	public void setTimeObj(TimeClock obj){localClock=obj;}
	protected String currency="";
	public void setCurrency(String newCurrency){currency=newCurrency;}
	public String getCurrency(){return currency;}
	private long expirationDate=0;
	public long expirationDate(){return expirationDate;}
	public void setExpirationDate(long time){expirationDate=time;}
	public long flags(){return 0;}
	
	public SpaceObject spaceTarget=null;
	public SpaceObject knownTarget(){return spaceTarget;}
	public void setKnownTarget(SpaceObject O){spaceTarget=O;}
	public SpaceObject spaceSource=null;
	public SpaceObject knownSource(){return spaceSource;}
	public void setKnownSource(SpaceObject O){spaceSource=O;}
	public SpaceObject orbiting=null;
	public SpaceObject orbiting(){return orbiting;}
	public void setOrbiting(SpaceObject O){orbiting=O;}
	protected boolean amDestroyed=false;
    
    public void destroy()
    {
        envStats=(EnvStats)CMClass.getCommon("DefaultEnvStats");
        coordinates=null;
        direction=null;
        spaceSource=null;
        spaceTarget=null;
        orbiting=null;
        baseEnvStats=envStats;
        miscText=null;
        imageName=null;
        affects=null;
        behaviors=null;
        scripts=null;
        author=null;
        currency=null;
        parents=null;
        parentsToLoad=null;
        climateObj=null;
        amDestroyed=true;
    }
    public boolean amDestroyed(){return amDestroyed;}
    public boolean savable(){return ((!amDestroyed) && (!CMath.bset(flags(),Area.FLAG_INSTANCE_CHILD)));}
	public String ID(){	return "StdSpaceShip";}
	protected String name="a space ship";
	protected Room savedDock=null;
	protected Room getDock(){ return CMLib.map().getRoom(savedDock);}
	protected String description="";
	protected String miscText="";
	protected Vector myRooms=new Vector();
	protected int flag=Area.STATE_ACTIVE;
	protected long tickStatus=Tickable.STATUS_NOT;
	protected String author=""; // will be used for owner, I guess.
	public void setAuthorID(String authorID){author=authorID;}
	public String getAuthorID(){return author;}

	protected EnvStats envStats=(EnvStats)CMClass.getCommon("DefaultEnvStats");
	protected EnvStats baseEnvStats=(EnvStats)CMClass.getCommon("DefaultEnvStats");

	protected Vector affects=new Vector(1);
	protected Vector behaviors=new Vector(1);
    protected Vector scripts=new Vector(1);
	public int climateType(){return Area.CLIMASK_NORMAL;}
	public void setClimateType(int newClimateType){}

	public StdSpaceShip()
	{
        super();
        CMClass.bumpCounter(this,CMClass.OBJECT_AREA);
        xtraValues=CMProps.getExtraStatCodesHolder(this);
	}
    protected void finalize(){CMClass.unbumpCounter(this,CMClass.OBJECT_AREA);}
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
		baseEnvStats.copyInto(envStats);
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if(A!=null)
				A.affectEnvStats(this,envStats);
		}
	}
	public void setBaseEnvStats(EnvStats newBaseEnvStats)
	{
		baseEnvStats=(EnvStats)newBaseEnvStats.copyOf();
	}
	public void setNextWeatherType(int weatherCode){}
	public void setCurrentWeatherType(int weatherCode){}
	public int getTechLevel(){return Area.THEME_TECHNOLOGY;}
	public void setTechLevel(int level){}

	public String image(){return imageName;}
    public String rawImage(){return imageName;}
	public void setImage(String newImage){imageName=newImage;}
	
	public String getArchivePath(){return "";}
	public void setArchivePath(String pathFile){}
	
    public void setAreaState(int newState)
    {
        if((newState==0)&&(!CMLib.threads().isTicking(this,Tickable.TICKID_AREA)))
            CMLib.threads().startTickDown(this,Tickable.TICKID_AREA,1);
        flag=newState;
    }
    public int getAreaState(){return flag;}
	public boolean amISubOp(String username){return false;}
	public String getSubOpList(){return "";}
	public void setSubOpList(String list){}
	public void addSubOp(String username){}
	public void delSubOp(String username){}
	public CMObject newInstance()
	{
		try
        {
			return (CMObject)this.getClass().newInstance();
		}
		catch(Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new StdSpaceShip();
	}
	public boolean isGeneric(){return false;}
	protected void cloneFix(StdSpaceShip E)
	{
		baseEnvStats=(EnvStats)E.baseEnvStats().copyOf();
		envStats=(EnvStats)E.envStats().copyOf();

		affects=new Vector(1);
		behaviors=new Vector(1);
        scripts=new Vector(1);
		parents=null;
		if(E.parents!=null)
			parents=(Vector)E.parents.clone();
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
				affects.addElement(A.copyOf());
		}
        ScriptingEngine S=null;
        for(int i=0;i<E.numScripts();i++)
        {
            S=E.fetchScript(i);
            if(S!=null)
                addScript((ScriptingEngine)S.copyOf());
        }
		setTimeObj((TimeClock)CMClass.getCommon("DefaultTimeClock"));
	}
	public CMObject copyOf()
	{
		try
		{
			StdSpaceShip E=(StdSpaceShip)this.clone();
            CMClass.bumpCounter(E,CMClass.OBJECT_AREA);
            E.xtraValues=(xtraValues==null)?null:(String[])xtraValues.clone();
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

	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	public String miscTextFormat(){return CMParms.FORMAT_UNDEFINED;}
	public String text()
	{
		return CMLib.coffeeMaker().getPropertiesStr(this,true);
	}
	public void setMiscText(String newMiscText)
	{
		miscText="";
		if(newMiscText.trim().length()>0)
			CMLib.coffeeMaker().setPropertiesStr(this,newMiscText,true);
	}

	public String description()
	{ return description;}
	public void setDescription(String newDescription)
	{ description=newDescription;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
        MsgListener N=null;
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
        for(int i=0;i<numEffects();i++)
        {
            N=fetchEffect(i);
            if((N!=null)&&(!N.okMessage(this,msg)))
                return false;
        }
        
		if((flag>=Area.STATE_FROZEN)||(!CMLib.flags().allowsMovement(this)))
		{
			if((msg.sourceMinor()==CMMsg.TYP_ENTER)
			||(msg.sourceMinor()==CMMsg.TYP_LEAVE)
			||(msg.sourceMinor()==CMMsg.TYP_FLEE))
				return false;
		}
		if((CMath.bset(msg.sourceCode(),CMMsg.MASK_MAGIC))
		||(CMath.bset(msg.targetCode(),CMMsg.MASK_MAGIC))
		||(CMath.bset(msg.othersCode(),CMMsg.MASK_MAGIC)))
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

    protected Vector allBlurbFlags()
    {
        Vector V=(Vector)blurbFlags.clone();
        String flag=null;
        Area A=null;
        int num=0;
        for(Enumeration e=getParents();e.hasMoreElements();)
        {
            A=(Area)e.nextElement();
            num=A.numBlurbFlags();
            for(int x=0;x<num;x++)
            {
                flag=A.getBlurbFlag(x);
                V.addElement(flag+" "+A.getBlurbFlag(flag));
            }
        }
        return V;
    }
    
    public String getBlurbFlag(String flag)
    {
        if((flag==null)||(flag.trim().length()==0))
            return null;
        flag=flag.toUpperCase().trim()+" ";
        Vector V=allBlurbFlags();
        for(int i=0;i<V.size();i++)
            if(((String)V.elementAt(i)).startsWith(flag))
                return ((String)V.elementAt(i)).substring(flag.length());
        return null;
    }
    public int numAllBlurbFlags(){return allBlurbFlags().size();}
    public int numBlurbFlags(){return blurbFlags.size();}
    public String getBlurbFlag(int which)
    {
        if(which<0) return null;
        Vector V=allBlurbFlags();
        if(which>=V.size()) return null;
        try{
            String s=(String)V.elementAt(which);
            int x=s.indexOf(' ');
            return s.substring(0,x).trim();
        }catch(Exception e){}
        return null;
    }
    public void addBlurbFlag(String flagPlusDesc)
    {
        if(flagPlusDesc==null) return;
        flagPlusDesc=flagPlusDesc.trim();
        if(flagPlusDesc.length()==0) return;
        int x=flagPlusDesc.indexOf(' ');
        String flag=null;
        if(x>=0) 
        {
            flag=flagPlusDesc.substring(0,x).toUpperCase();
            flagPlusDesc=flagPlusDesc.substring(x);
        }
        else
        {
            flag=flagPlusDesc.toUpperCase().trim();
            flagPlusDesc="";
        }
        if(getBlurbFlag(flag)==null)
            blurbFlags.addElement((flag+" "+flagPlusDesc).trim());
    }
    public void delBlurbFlag(String flagOnly)
    {
        if(flagOnly==null) return;
        flagOnly=flagOnly.toUpperCase().trim();
        if(flagOnly.length()==0) return;
        flagOnly+=" ";
        try{
            for(int v=0;v<blurbFlags.size();v++)
                if(((String)blurbFlags.elementAt(v)).startsWith(flagOnly))
                {
                    blurbFlags.removeElementAt(v);
                    return;
                }
        }catch(Exception e){}
    }
    
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
        MsgListener N=null;
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
	}

	public Enumeration getCompleteMap(){return getProperMap();}
	public Vector getMetroCollection(){return (Vector)myRooms.clone();}
	
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
		if(flag>=Area.STATE_STOPPED) return false;
		tickStatus=Tickable.STATUS_START;
		if(tickID==Tickable.TICKID_AREA)
		{
			getTimeObj().tick(this,tickID);
			for(int b=0;b<numBehaviors();b++)
			{
				tickStatus=Tickable.STATUS_BEHAVIOR+b;
				Behavior B=fetchBehavior(b);
				if(B!=null)
					B.tick(ticking,tickID);
			}
            for(int s=0;s<numScripts();s++)
            {
                ScriptingEngine S=fetchScript(s);
                tickStatus=Tickable.STATUS_SCRIPT+s;
                if(S!=null) 
                    S.tick(ticking,tickID);
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
		if(fetchEffect(to.ID())!=null) return;
		to.makeNonUninvokable();
		to.makeLongLasting();
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void addEffect(Ability to)
	{
		if(to==null) return;
		if(fetchEffect(to.ID())!=null) return;
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

	public void fillInAreaRooms() { }

	public boolean inMyMetroArea(Area A)
	{
		if(A==this) return true;
		return false;
	}
	public void fillInAreaRoom(Room R){}
	public void dockHere(Room R)
	{
		if(R==null) return;
		if(getDock()!=null) unDock(false);
		Room airLockRoom=null;
		int airLockDir=-1;
		Room backupRoom=null;
		int backupDir=-1;
		for(Enumeration e=getProperMap();e.hasMoreElements();)
		{
			Room R2=(Room)e.nextElement();
			if(R2!=null)
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				if((R2.getRawExit(d)!=null)
				&&((R2.rawDoors()[d]==null)||(R2.rawDoors()[d].getArea()!=this))
				&&(R2.getRawExit(d).ID().endsWith("AirLock")))
				{ 
					airLockRoom=R2; 
					R2.rawDoors()[d]=null; 
					airLockDir=d; 
					break;
				}
				
				if((d<4)&&(R2.rawDoors()==null))
				{
					backupRoom=R2;
					backupDir=d;
				}
			}
			if(airLockDir>=0) break;
		}
		if(airLockRoom==null)
		{
			airLockRoom=backupRoom;
			airLockDir=backupDir;
		}
		
		if(airLockRoom!=null)
		{
			if(airLockRoom.rawDoors()[airLockDir]==null)
				airLockRoom.rawDoors()[airLockDir]=R;
			if(airLockRoom.getRawExit(airLockDir)==null)
				airLockRoom.setRawExit(airLockDir,CMClass.getExit("GenAirLock"));
			Item portal=CMClass.getMiscTech("GenSSPortal");
			portal.setName(Name());
			portal.setDisplayText(Name());
			portal.setDescription(description());
			portal.setReadableText(CMLib.map().getExtendedRoomID(R));
			CMLib.flags().setGettable(portal,false);
			R.addItem(portal);
			portal.setExpirationDate(0);
			savedDock=R;
			CMLib.map().delObjectInSpace(this);
			R.recoverRoomStats();
		}
	}
	public void unDock(boolean toSpace)
	{
		if(getDock()==null) return;
		Room dock=getDock();
		for(int i=0;i<dock.numItems();i++)
		{
			Item I=dock.fetchItem(i);
			if(I.Name().equals(Name()))
				I.destroy();
		}
		for(Enumeration e=getProperMap();e.hasMoreElements();)
		{
			Room R=(Room)e.nextElement();
			if(R!=null)
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				if(R.rawDoors()[d]==dock)
					R.rawDoors()[d]=null;
			}
		}
		dock=null;
		if(toSpace)
		{
			CMLib.map().addObjectToSpace(this);
		}
	}

	public RoomnumberSet getCachedRoomnumbers()
	{
		RoomnumberSet set=(RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet");
		synchronized(myRooms)
		{
			Room R=null;
			for(int p=myRooms.size()-1;p>=0;p--)
			{
				R=(Room)myRooms.elementAt(p);
				if(R.roomID().length()>0)
					set.add(R.roomID());
			}
		}
		return set;
	}
	public RoomnumberSet getProperRoomnumbers()
	{
		if(properRoomIDSet==null)
			properRoomIDSet=(RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet");
		return properRoomIDSet;
	}
	
	public String getNewRoomID(Room startRoom, int direction)
	{
		int highest=Integer.MIN_VALUE;
		int lowest=Integer.MAX_VALUE;
		Hashtable allNums=new Hashtable();
		try
		{
			for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if((R.getArea().Name().equals(Name()))
				&&(R.roomID().startsWith(Name()+"#")))
				{
					int newnum=CMath.s_int(R.roomID().substring(Name().length()+1));
					if(newnum>=highest)	highest=newnum;
					if(newnum<=lowest) lowest=newnum;
					allNums.put(Integer.valueOf(newnum),R);
				}
			}
	    }catch(NoSuchElementException e){}
		if((highest<0)&&(CMLib.map().getRoom(Name()+"#0"))==null)
			return Name()+"#0";
		if(lowest>highest) lowest=highest+1;
		for(int i=lowest;i<=highest+1000;i++)
		{
			if((!allNums.containsKey(Integer.valueOf(i)))
			&&(CMLib.map().getRoom(Name()+"#"+i)==null))
				return Name()+"#"+i;
		}
		return Name()+"#"+Math.random();
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

	public int[] getAreaIStats(){return new int[Area.AREASTAT_NUMBER];}
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

    /** Manipulation of the scripts list */
    public void addScript(ScriptingEngine S)
    {
        if(scripts==null) scripts=new Vector(1);
        if(S==null) return;
        if(!scripts.contains(S)) {
            ScriptingEngine S2=null;
            for(int s=0;s<scripts.size();s++)
            {
                S2=(ScriptingEngine)scripts.elementAt(s);
                if((S2!=null)&&(S2.getScript().equalsIgnoreCase(S.getScript())))
                    return;
            }
            scripts.addElement(S);
        }
    }
    public void delScript(ScriptingEngine S)
    {
        if(scripts!=null)
        {
            scripts.removeElement(S);
            if(scripts.size()==0)
                scripts=new Vector(1);
        }
    }
    public int numScripts(){return (scripts==null)?0:scripts.size();}
    public ScriptingEngine fetchScript(int x){try{return (ScriptingEngine)scripts.elementAt(x);}catch(Exception e){} return null;}
    
    public void addProperRoom(Room R)
    {
        if(R==null) return;
        if(R.getArea()!=this)
        {
            R.setArea(this);
            return;
        }
        synchronized(myRooms)
        {
            if(!myRooms.contains(R))
            {
            	addProperRoomnumber(R.roomID());
                Room R2=null;
                for(int i=0;i<myRooms.size();i++)
                {
                    R2=(Room)myRooms.elementAt(i);
                    if(R2.roomID().compareToIgnoreCase(R.roomID())>=0)
                    {
                        if(R2.ID().compareToIgnoreCase(R.roomID())==0)
                            myRooms.setElementAt(R,i);
                        else
                            myRooms.insertElementAt(R,i);
                        return;
                    }
                }
                myRooms.addElement(R);
            }
        }
    }
    
    public void delProperRoom(Room R)
    {
        if(R==null) return;
        if(R instanceof GridLocale)
            ((GridLocale)R).clearGrid(null);
        synchronized(myRooms)
        {
            if(myRooms.removeElement(R))
                delProperRoomnumber(R.roomID());
        }
    }
    
    public void addProperRoomnumber(String roomID)
    {
    	if((roomID!=null)&&(roomID.length()>0))
	        getProperRoomnumbers().add(roomID);
    }
    public void delProperRoomnumber(String roomID)
    {
    	if((roomID!=null)&&(roomID.length()>0))
	        getProperRoomnumbers().remove(roomID);
    }
    public boolean isRoom(Room R)
    {
        if(R==null) return false;
        return myRooms.contains(R);
    }
    
    public Room getRoom(String roomID)
    {
        if(myRooms.size()==0) return null;
        synchronized(myRooms)
        {
	        int start=0;
	        int end=myRooms.size()-1;
	        while(start<=end)
	        {
	            int mid=(end+start)/2;
	            int comp=((Room)myRooms.elementAt(mid)).roomID().compareToIgnoreCase(roomID);
	            if(comp==0)
	                return (Room)myRooms.elementAt(mid);
	            else
	            if(comp>0)
	                end=mid-1;
	            else
	                start=mid+1;
	
	        }
        }
        return null;
    }

	public int metroSize(){return properSize();}
	public int properSize()
	{
		synchronized(myRooms)
		{
			return myRooms.size();
		}
	}
	public int numberOfProperIDedRooms()
	{
		int num=0;
		for(Enumeration e=getProperMap();e.hasMoreElements();)
		{
			Room R=(Room)e.nextElement();
			if(R.roomID().length()>0)
				if(R instanceof GridLocale)
					num+=((GridLocale)R).xGridSize()*((GridLocale)R).yGridSize();
				else
					num++;
		}
		return num;
	}
	public Room getRandomMetroRoom(){return getRandomProperRoom();}
	public Room getRandomProperRoom()
	{
		synchronized(myRooms)
		{
			if(properSize()==0) return null;
			Room R=(Room)myRooms.elementAt(CMLib.dice().roll(1,properSize(),-1));
			if(R instanceof GridLocale) return ((GridLocale)R).getRandomGridChild();
			return R;
		}
	}
	public void setProperRoomnumbers(RoomnumberSet set){ properRoomIDSet=set;}
	public RoomnumberSet getMetroRoomnumbers(){return getProperRoomnumbers();}
	public Enumeration getMetroMap(){return getProperMap();}
	public void addMetroRoomnumber(String roomID){}
	public void delMetroRoomnumber(String roomID){}
	public void addMetroRoom(Room R){}
	public void delMetroRoom(Room R){}
	public Enumeration getProperMap()
	{
		synchronized(myRooms)
		{
			return myRooms.elements();
		}
	}
	public Enumeration getFilledProperMap() { return getProperMap();}
	public Vector getSubOpVectorList(){	return new Vector();}

    public void addChildToLoad(String str){}
    public void addParentToLoad(String str) { parentsToLoad.addElement(str);}

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
	// Parent
	public void initParents() {
	        if (parents == null) {
	                parents = new Vector();
	                for (int i = 0; i < parentsToLoad.size(); i++) {
	                        Area A = CMLib.map().getArea( (String) parentsToLoad.elementAt(i));
	                        if (A == null)
	                                continue;
	                        parents.addElement(A);
	                }
	        }
	}
	public Enumeration getParents() { initParents(); return parents.elements(); }
    public Vector getParentsRecurse()
    {
        Vector V=new Vector();
        Area A=null;
        for(Enumeration e=getParents();e.hasMoreElements();)
        {
            A=(Area)e.nextElement();
            V.addElement(A);
            CMParms.addToVector(A.getParentsRecurse(),V);
        }
        return V;
    }
	public String getParentsList() {
	        initParents();
	        StringBuffer str=new StringBuffer("");
	        for(Enumeration e=getParents(); e.hasMoreElements();) {
	                Area A=(Area)e.nextElement();
	                if(str.length()>0) str.append(";");
	                str.append(A.name());
	        }
	        return str.toString();
	}

	public int getNumParents() { initParents(); return parents.size(); }
	public Area getParent(int num) { initParents(); return (Area)parents.elementAt(num); }
	public Area getParent(String named) {
	        initParents();
	        for(int i=0;i<parents.size();i++){
	                Area A=(Area)parents.elementAt(i);
	                if((A.name().equalsIgnoreCase(named))
	                   ||(A.Name().equalsIgnoreCase(named)))
	                       return A;
	        }
	        return null;
	}
	public boolean isParent(Area named) {
	        initParents();
	        for(int i=0;i<parents.size();i++){
	                Area A=(Area)parents.elementAt(i);
	                if(A.equals(named))
	                       return true;
	        }
	        return false;
	}
	public boolean isParent(String named) {
	        initParents();
	        for(int i=0;i<parents.size();i++){
	                Area A=(Area)parents.elementAt(i);
	                if((A.name().equalsIgnoreCase(named))
	                   ||(A.Name().equalsIgnoreCase(named)))
	                        return true;
	        }
	        return false;
	}
	public void addParent(Area Adopted) {
	        initParents();
	        for(int i=0;i<parents.size();i++){
	                Area A=(Area)parents.elementAt(i);
	                if(A.Name().equalsIgnoreCase(Adopted.Name())){
	                        parents.setElementAt(Adopted, i);
	                        return;
	                }
	        }
	        parents.addElement(Adopted);
	}
	public void removeParent(Area Disowned) { initParents();parents.removeElement(Disowned); }
	public void removeParent(int Disowned) { initParents();parents.removeElementAt(Disowned); }
	public boolean canParent(Area newParent) {
	    return true;
	}

    public String prejudiceFactors(){return "";}
    public void setPrejudiceFactors(String factors){}
    public final static String[] empty=new String[0];
    public String[] itemPricingAdjustments(){return empty;}
    public void setItemPricingAdjustments(String[] factors){}
    public String ignoreMask(){return "";}
    public void setIgnoreMask(String factors){}
    public String budget(){return "";}
    public void setBudget(String factors){}
    public String devalueRate(){return "";}
    public void setDevalueRate(String factors){}
    public int invResetRate(){return 0;}
    public void setInvResetRate(int ticks){}
    public int finalInvResetRate(){ return 0;}
    public String finalPrejudiceFactors(){ return "";}
    public String finalIgnoreMask(){ return "";}
    public String[] finalItemPricingAdjustments(){ return empty;}
    public String finalBudget(){ return "";}
    public String finalDevalueRate(){ return "";}
   
	public int getSaveStatIndex(){return getStatCodes().length;}
	private static final String[] CODES={"CLASS","CLIMATE","DESCRIPTION","TEXT","TECHLEVEL","BLURBS"};
	public String[] getStatCodes(){return CODES;}
    public boolean isStat(String code){ return CMParms.indexOf(getStatCodes(),code.toUpperCase().trim())>=0;}
	protected int getCodeNum(String code){
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
        case 5: return ""+CMLib.xml().getXMLList(blurbFlags);
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: return;
		case 1: setClimateType(CMath.s_parseBitIntExpression(Area.CLIMATE_DESCS,val)); break;
		case 2: setDescription(val); break;
		case 3: setMiscText(val); break;
		case 4: setTechLevel(CMath.s_parseBitIntExpression(Area.THEME_DESCS,val)); break;
        case 5:
        {
            if(val.startsWith("+"))
                addBlurbFlag(val.substring(1));
            else
            if(val.startsWith("-"))
                delBlurbFlag(val.substring(1));
            else
                blurbFlags=CMLib.xml().parseXMLList(val);
            break;
        }
		}
	}
    public boolean sameAs(Environmental E)
    {
        if(!(E instanceof StdSpaceShip)) return false;
        String[] codes=getStatCodes();
        for(int i=0;i<codes.length;i++)
            if(!E.getStat(codes[i]).equals(getStat(codes[i])))
                return false;
        return true;
    }
}
