package com.planet_ink.coffee_mud.web.macros.grinder;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

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
public class GrinderRoom
{
	public int x=0;
	public int y=0;
    public int z=0;
	public String roomID="";
	public Room room=null;
    public GrinderDir[] doors=new GrinderDir[Directions.NUM_DIRECTIONS];
	public GrinderRoom(Room R)
	{
		room=R;
		roomID=R.roomID();
        for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
        {
            GrinderDir D=new GrinderDir();
			Room R2=R.rawDoors()[d];
            if(R2!=null)
            {
                D.room=R2.roomID();
				Exit E2=R.rawExits()[d];
                if(E2!=null)
					D.exit=E2;
            }
            doors[d]=D;
        }
	}
}
