package com.planet_ink.coffee_mud.Areas;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdPlanet extends StdArea implements SpaceObject
{
	public String ID(){	return "StdPlanet";}
	public Environmental newInstance()
	{
		return new StdPlanet();
	}
	public Environmental copyOf()
	{
		try
		{
			StdPlanet E=(StdPlanet)this.clone();
			E.cloneFix(this);
			E.setTimeObj(new DefaultTimeClock());
			return E;
		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}
	public long[] coordinates=new long[3];
	public long[] coordinates(){return coordinates;}
	public void setCoords(long[] coords){coordinates=coords;}
	public double[] direction=new double[2];
	public double[] direction(){return direction;}
	public void setDirection(double[] dir){direction=dir;}
	public long velocity=0;
	public long velocity(){return velocity;}
	public void setVelocity(long v){velocity=v;}
	protected TimeClock myClock=new DefaultTimeClock();
	public TimeClock getTimeObj(){return myClock;}
	public void setName(String newName)
	{
		super.setName(newName);
		myClock.setLoadName(newName);
	}
	
	public SpaceObject knownTarget(){return null;}
	public void setKnownTarget(SpaceObject O){}
	public SpaceObject knownSource(){return null;}
	public void setKnownSource(SpaceObject O){}
	public SpaceObject orbiting=null;
	public SpaceObject orbiting(){return orbiting;}
	public void setOrbiting(SpaceObject O){orbiting=O;}
	
	public void addChild(Area Adopted) {
		super.addChild(Adopted);
		Adopted.setTimeObj(getTimeObj());
	}
	public void initChildren() {
		super.initChildren();
		if(children!=null)
			for(int i=0;i<children.size();i++)
				((Area)children.elementAt(i)).setTimeObj(getTimeObj());
	}
}
