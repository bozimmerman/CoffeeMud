package com.planet_ink.coffee_mud.Abilities.Properties;

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
public class Prop_NoChannel extends Property
{
	public String ID() { return "Prop_NoChannel"; }
	public String name(){ return "Channel Neutralizing";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS;}
    protected Vector channels=null;
    protected boolean receive=true;
    protected boolean sendOK=false;

    public String accountForYourself()
    { return "No Channeling Field"; }

    public void setMiscText(String newText)
    {
        super.setMiscText(newText);
        channels=Util.parseSemicolons(newText.toUpperCase(),true);
        int x=channels.indexOf("SENDOK");
        sendOK=(x>=0);
        if(sendOK) channels.removeElementAt(x);
        x=channels.indexOf("QUIET");
        receive=(x<0);
        if(!receive) channels.removeElementAt(x);
    }
    
    public boolean okMessage(Environmental myHost, CMMsg msg)
    {
        if(!super.okMessage(myHost,msg))
            return false;


        if((msg.othersMajor()&CMMsg.MASK_CHANNEL)>0)
        {
            int channelInt=msg.othersMinor()-CMMsg.TYP_CHANNEL;
            if((msg.source()==affected)||(!(affected instanceof MOB))
            &&((channels==null)||(channels.size()==0)||(channels.contains(ChannelSet.getChannelName(channelInt)))))
            {
                if(!sendOK)
                {
                    if(msg.source()==affected)
                        msg.source().tell("Your message drifts into oblivion.");
                    else
                    if((!(affected instanceof MOB))
                    &&(CoffeeUtensils.roomLocation(affected)==msg.source().location()))
                        msg.source().tell("This is a no-channel area.");
                    return false;
                }
                if(!receive)
                {
                    if((msg.source()!=affected)
                    ||((!(affected instanceof MOB))&&(CoffeeUtensils.roomLocation(affected)!=msg.source().location())))
                        return false;
                }
            }
        }
        return true;
	}
}
