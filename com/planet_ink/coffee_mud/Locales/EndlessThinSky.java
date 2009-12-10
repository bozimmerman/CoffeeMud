package com.planet_ink.coffee_mud.Locales;
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
public class EndlessThinSky extends StdThinGrid
{
	public String ID(){return "EndlessThinSky";}
	protected boolean crossLinked=false;

	public EndlessThinSky()
	{
		super();
		baseEnvStats.setWeight(1);
		recoverEnvStats();
		setDisplayText("Up in the sky");
		setDescription("");
		xsize=CMProps.getIntVar(CMProps.SYSTEMI_SKYSIZE);
		ysize=CMProps.getIntVar(CMProps.SYSTEMI_SKYSIZE);
		if(xsize<0) xsize=xsize*-1;
		if(ysize<0) ysize=ysize*-1;
		if((xsize==0)||(ysize==0))
		{
			xsize=3;
			ysize=3;
		}
	}
	public int domainType(){return Room.DOMAIN_OUTDOORS_AIR;}
	public int domainConditions(){return Room.CONDITION_NORMAL;}


	public CMObject newInstance()
	{
	    if(!CMSecurity.isDisabled("THINGRIDS"))
	        return super.newInstance();
        return new EndlessSky().newInstance();
	}
	
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		return InTheAir.isOkAirAffect(this,msg);
	}
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		InTheAir.airAffects(this,msg);
	}
	public String getGridChildLocaleID(){return "InTheAir";}

	protected void fillExitsOfGridRoom(Room R, int x, int y)
	{
		super.fillExitsOfGridRoom(R,x,y);
		
		if((x<0)||(y<0)||(y>=yGridSize())||(x>=xGridSize())) 
			return;
		// the adjacent rooms created by this method should also take
		// into account the possibility that they are on the edge.
		// it does NOT
		if(ox==null) ox=CMClass.getExit("Open");
		Room R2=null;
		if((y==0)
		&&(R.rawDoors()[Directions.UP]!=rawDoors()[Directions.UP])
		&&(rawDoors()[Directions.UP]!=null)
		&&(exits[Directions.UP]!=null))
		{
		    R.rawDoors()[Directions.UP]=null;
		    R.setRawExit(Directions.UP,null);
			linkRoom(R,rawDoors()[Directions.UP],Directions.UP,exits[Directions.UP],exits[Directions.UP]);
		}
		else
		if(R.rawDoors()[Directions.UP]==null)
		{
			if(y>0)
			{
				R2=getMakeSingleGridRoom(x,y-1);
				if(R2!=null)
					linkRoom(R,R2,Directions.UP,ox,ox);
			}
			else
			if(x>0)
			{
				R2=getMakeSingleGridRoom(x-1,yGridSize()-1);
				if(R2!=null)
					linkRoom(R,R2,Directions.UP,ox,ox);
			}
			else
			{
				R2=getMakeSingleGridRoom(xGridSize()-1,yGridSize()-1);
				if(R2!=null)
					linkRoom(R,R2,Directions.UP,ox,ox);
			}
		}
		if((y==yGridSize()-1)
		&&(R.rawDoors()[Directions.DOWN]!=rawDoors()[Directions.DOWN])
		&&(rawDoors()[Directions.DOWN]!=null)
	    &&(exits[Directions.DOWN]!=null))
		{
		    R.rawDoors()[Directions.DOWN]=null;
		    R.setRawExit(Directions.DOWN,null);
			linkRoom(R,rawDoors()[Directions.DOWN],Directions.DOWN,exits[Directions.DOWN],exits[Directions.DOWN]);
		}
		else
		if(R.rawDoors()[Directions.DOWN]==null)
		{
			if(y<yGridSize()-1)
			{
				R2=getMakeSingleGridRoom(x,y+1);
				if(R2!=null)
					linkRoom(R,R2,Directions.DOWN,ox,ox);
			}
			else
			if(x<xGridSize()-1)
			{
				R2=getMakeSingleGridRoom(x+1,0);
				if(R2!=null)
					linkRoom(R,R2,Directions.DOWN,ox,ox);
			}
			else
			{
				R2=getMakeSingleGridRoom(0,0);
				if(R2!=null)
					linkRoom(R,R2,Directions.DOWN,ox,ox);
			}
		}
		
		if((y==0)&&(R.rawDoors()[Directions.NORTH]==null))
		{
			R2=getMakeSingleGridRoom(x,yGridSize()-1);
			if(R2!=null)
				linkRoom(R,R2,Directions.NORTH,ox,ox);
		}
		else
		if((y==yGridSize()-1)&&(R.rawDoors()[Directions.SOUTH]==null))
		{
			R2=getMakeSingleGridRoom(x,0);
			if(R2!=null)
				linkRoom(R,R2,Directions.SOUTH,ox,ox);
		}
		
		
		if((x==0)&&(R.rawDoors()[Directions.WEST]==null))
		{
			R2=getMakeSingleGridRoom(xGridSize()-1,y);
			if(R2!=null)
				linkRoom(R,R2,Directions.WEST,ox,ox);
		}
		else
		if((x==xGridSize()-1)&&(R.rawDoors()[Directions.EAST]==null))
		{
			R2=getMakeSingleGridRoom(0,y);
			if(R2!=null)
				linkRoom(R,R2,Directions.EAST,ox,ox);
		}
        
        if(Directions.NORTHEAST<Directions.NUM_DIRECTIONS())
        {
            if(((x==0)||(y==0))&&(R.rawDoors()[Directions.NORTHWEST]==null))
            {
                R2=getMakeSingleGridRoom(xGridSize()-1,yGridSize()-1);
                if(R2!=null)
                    linkRoom(R,R2,Directions.NORTHWEST,ox,ox);
            }
            else
            if(((x==xGridSize()-1)||(y==yGridSize()-1))&&(R.rawDoors()[Directions.SOUTHEAST]==null))
            {
                R2=getMakeSingleGridRoom(0,0);
                if(R2!=null)
                    linkRoom(R,R2,Directions.SOUTHEAST,ox,ox);
            }
            
            if(((x==xGridSize()-1)||(y==0))&&(R.rawDoors()[Directions.NORTHEAST]==null))
            {
                R2=getMakeSingleGridRoom(0,yGridSize()-1);
                if(R2!=null)
                    linkRoom(R,R2,Directions.NORTHEAST,ox,ox);
            }
            else
            if(((x==0)||(y==yGridSize()-1))&&(R.rawDoors()[Directions.SOUTHWEST]==null))
            {
                R2=getMakeSingleGridRoom(xGridSize()-1,0);
                if(R2!=null)
                    linkRoom(R,R2,Directions.SOUTHWEST,ox,ox);
            }
        }
	}
}
