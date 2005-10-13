package com.planet_ink.coffee_mud.Races;

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
public class Shark extends GiantFish
{
    public String ID(){ return "Shark"; }
    public String name(){ return "Shark"; }
    public int shortestMale(){return 10;}
    public int shortestFemale(){return 15;}
    public int heightVariance(){return 20;}
    public int lightestWeight(){return 355;}
    public int weightVariance(){return 105;}
    protected static Vector resources=new Vector();

    public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
    {
        super.affectCharStats(affectedMOB, affectableStats);
        affectableStats.setPermaStat(CharStats.INTELLIGENCE,1);
        affectableStats.setPermaStat(CharStats.STRENGTH,16);
        affectableStats.setPermaStat(CharStats.DEXTERITY,15);
    }
    public Vector myResources()
    {
        synchronized(resources)
        {
            if(resources.size()==0)
            {
                for(int i=0;i<25;i++)
                resources.addElement(makeResource
                ("some "+name().toLowerCase(),EnvResource.RESOURCE_FISH));
                for(int i=0;i<15;i++)
                resources.addElement(makeResource
                ("a "+name().toLowerCase()+" hide",EnvResource.RESOURCE_HIDE));
                for(int i=0;i<5;i++)
                resources.addElement(makeResource
                ("a "+name().toLowerCase()+" tooth",EnvResource.RESOURCE_BONE));
                resources.addElement(makeResource
                ("some "+name().toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
            }
        }
        return resources;
    }
}