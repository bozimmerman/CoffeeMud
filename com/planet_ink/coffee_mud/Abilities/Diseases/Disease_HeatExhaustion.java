package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

public class Disease_HeatExhaustion extends Disease
{
    public String ID() { return "Disease_HeatExhaustion"; }
    public String name(){ return "Heat Exhaustion";}
    public String displayText(){ return "(Heat Exhaustion)";}
    protected int canAffectCode(){return CAN_MOBS;}
    protected int canTargetCode(){return CAN_MOBS;}
    public int quality(){return Ability.MALICIOUS;}
    public boolean putInCommandlist(){return false;}
    public int difficultyLevel(){return 1;}

    protected int DISEASE_TICKS(){return 300;}
    protected int DISEASE_DELAY(){return 3;}
    protected String DISEASE_DONE(){return "You head stops spinning.";}
    protected String DISEASE_START(){return "^G<S-NAME> <S-IS-ARE> overcome by the heat.^?";}
    protected String DISEASE_AFFECT(){return "";}
    public int abilityCode(){return 0;}
    protected Room theRoom=null;
    protected int changeDown=300;
    
    public Room room(Room R)
    {
        if((theRoom==null)
        &&(R!=null))
            theRoom=R.getArea().getRandomProperRoom();
        if(R==theRoom) theRoom=null;
        return theRoom;
    }
    
    public boolean okMessage(Environmental myHost, CMMsg msg)
    {
        if((affected!=null)
        &&(affected==msg.source())
        &&(msg.amITarget(msg.source().location()))
        &&((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE)))
        {
            Room R=room(msg.source().location());
            if((R==null)||(R==msg.source().location())) return true;
            FullMsg msg2=new FullMsg(msg.source(),R,msg.tool(),
                          msg.sourceCode(),msg.sourceMessage(),
                          msg.targetCode(),msg.targetMessage(),
                          msg.othersCode(),msg.othersMessage());
            if(R.okMessage(msg.source(),msg2))
            {
                R.executeMsg(msg.source(),msg2);
                return false;
            }
        }
        return super.okMessage(myHost,msg);
    }
    
    public boolean tick(Tickable ticking, int tickID)
    {
        if(!super.tick(ticking,tickID))
            return false;
        if((--changeDown)<=0)
        {
            changeDown=300;
            theRoom=null;
        }
        return true;
    }
}