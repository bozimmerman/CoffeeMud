package com.planet_ink.coffee_mud.Areas;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class StdPlanet extends StdArea implements SpaceObject
{
	public String ID(){	return "StdPlanet";}
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
