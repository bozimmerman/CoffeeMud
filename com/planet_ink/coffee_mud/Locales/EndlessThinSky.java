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
public class EndlessThinSky extends StdThinGrid
{
	public String ID(){return "EndlessThinSky";}
	protected boolean crossLinked=false;

	public EndlessThinSky()
	{
		super();
		baseEnvStats.setWeight(1);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_AIR;
		domainCondition=Room.CONDITION_NORMAL;
		setDisplayText("Up in the sky");
		setDescription("");
		xsize=CommonStrings.getIntVar(CommonStrings.SYSTEMI_SKYSIZE);
		ysize=CommonStrings.getIntVar(CommonStrings.SYSTEMI_SKYSIZE);
		if((xsize==0)||(ysize==0))
		{
			xsize=3;
			ysize=3;
		}
	}


	public Environmental newInstance()
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
	public String getChildLocaleID(){return "InTheAir";}

	protected void fillExitsOfGridRoom(Room R, int x, int y)
	{
		super.fillExitsOfGridRoom(R,x,y);
		
		if((x<0)||(y<0)||(y>=ySize())||(x>=xSize())) 
			return;
		// the adjacent rooms created by this method should also take
		// into account the possibility that they are on the edge.
		// it does NOT
		if(ox==null) ox=CMClass.getExit("Open");
		Room R2=null;
		if((y==0)
		&&(R.rawDoors()[Directions.UP]!=rawDoors()[Directions.UP])
		&&(rawDoors()[Directions.UP]!=null)
		&&(rawExits()[Directions.UP]!=null))
		{
		    R.rawDoors()[Directions.UP]=null;
		    R.rawExits()[Directions.UP]=null;
			linkRoom(R,rawDoors()[Directions.UP],Directions.UP,rawExits()[Directions.UP],rawExits()[Directions.UP]);
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
				R2=getMakeSingleGridRoom(x-1,ySize()-1);
				if(R2!=null)
					linkRoom(R,R2,Directions.UP,ox,ox);
			}
			else
			{
				R2=getMakeSingleGridRoom(xSize()-1,ySize()-1);
				if(R2!=null)
					linkRoom(R,R2,Directions.UP,ox,ox);
			}
		}
		if((y==ySize()-1)
		&&(R.rawDoors()[Directions.DOWN]!=rawDoors()[Directions.DOWN])
		&&(rawDoors()[Directions.DOWN]!=null)
	    &&(rawExits()[Directions.DOWN]!=null))
		{
		    R.rawDoors()[Directions.DOWN]=null;
		    R.rawExits()[Directions.DOWN]=null;
			linkRoom(R,rawDoors()[Directions.DOWN],Directions.DOWN,rawExits()[Directions.DOWN],rawExits()[Directions.DOWN]);
		}
		else
		if(R.rawDoors()[Directions.DOWN]==null)
		{
			if(y<ySize()-1)
			{
				R2=getMakeSingleGridRoom(x,y+1);
				if(R2!=null)
					linkRoom(R,R2,Directions.DOWN,ox,ox);
			}
			else
			if(x<xSize()-1)
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
			R2=getMakeSingleGridRoom(x,ySize()-1);
			if(R2!=null)
				linkRoom(R,R2,Directions.NORTH,ox,ox);
		}
		else
		if((y==ySize()-1)&&(R.rawDoors()[Directions.SOUTH]==null))
		{
			R2=getMakeSingleGridRoom(x,0);
			if(R2!=null)
				linkRoom(R,R2,Directions.SOUTH,ox,ox);
		}
		
		
		if((x==0)&&(R.rawDoors()[Directions.WEST]==null))
		{
			R2=getMakeSingleGridRoom(xSize()-1,y);
			if(R2!=null)
				linkRoom(R,R2,Directions.WEST,ox,ox);
		}
		else
		if((x==xSize()-1)&&(R.rawDoors()[Directions.EAST]==null))
		{
			R2=getMakeSingleGridRoom(0,y);
			if(R2!=null)
				linkRoom(R,R2,Directions.EAST,ox,ox);
		}
        
        if(Directions.NORTHEAST<Directions.NUM_DIRECTIONS)
        {
            if(((x==0)||(y==0))&&(R.rawDoors()[Directions.NORTHWEST]==null))
            {
                R2=getMakeSingleGridRoom(xSize()-1,ySize()-1);
                if(R2!=null)
                    linkRoom(R,R2,Directions.NORTHWEST,ox,ox);
            }
            else
            if(((x==xSize()-1)||(y==ySize()-1))&&(R.rawDoors()[Directions.SOUTHEAST]==null))
            {
                R2=getMakeSingleGridRoom(0,0);
                if(R2!=null)
                    linkRoom(R,R2,Directions.SOUTHEAST,ox,ox);
            }
            
            if(((x==xSize()-1)||(y==0))&&(R.rawDoors()[Directions.NORTHEAST]==null))
            {
                R2=getMakeSingleGridRoom(0,ySize()-1);
                if(R2!=null)
                    linkRoom(R,R2,Directions.NORTHEAST,ox,ox);
            }
            else
            if(((x==0)||(y==ySize()-1))&&(R.rawDoors()[Directions.SOUTHWEST]==null))
            {
                R2=getMakeSingleGridRoom(xSize()-1,0);
                if(R2!=null)
                    linkRoom(R,R2,Directions.SOUTHWEST,ox,ox);
            }
        }
	}
}
