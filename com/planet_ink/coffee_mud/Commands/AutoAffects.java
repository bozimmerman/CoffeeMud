package com.planet_ink.coffee_mud.Commands;
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
   Copyright 2000-2006 Bo Zimmerman

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
public class AutoAffects extends StdCommand
{
    private String[] access={"AUTOAFFECTS","AUTOAFF","AAF"};
    public String[] getAccessWords(){return access;}

    public String getAutoAffects(MOB mob)
    {
        StringBuffer msg=new StringBuffer("");
        int colnum=2;
        for(int a=0;a<mob.numAllEffects();a++)
        {
            Ability thisAffect=mob.fetchEffect(a);
            String disp=thisAffect.name();
            if((thisAffect!=null)
            &&(thisAffect.displayText().length()==0)
            &&(mob.fetchAbility(thisAffect.ID())!=null)
            &&(thisAffect.isAutoInvoked()))
            {
                if(((++colnum)>2)||(disp.length()>25)){ msg.append("\n\r"); colnum=0;}
                msg.append("^S"+CMStrings.padRightPreserve("^<HELPNAME NAME='"+thisAffect.Name()+"'^>"+disp+"^</HELPNAME^>",25));
                if(disp.length()>25) colnum=99;
            }
        }
        msg.append("^N\n\r");
        return msg.toString();
    }

    public boolean execute(MOB mob, Vector commands)
        throws java.io.IOException
    {
        Session S=mob.session();
        if((commands!=null)&&(commands.size()>0)&&(!(commands.firstElement() instanceof String)))
        {
            if(commands.firstElement() instanceof MOB)
                S=((MOB)commands.firstElement()).session();
            else
            if(commands.firstElement() instanceof StringBuffer)
            {
                ((StringBuffer)commands.firstElement()).append(getAutoAffects(mob));
                return false;
            }
            else
            if(commands.firstElement() instanceof Vector)
            {
                ((Vector)commands.firstElement()).addElement(getAutoAffects(mob));
                return false;
            }
            else
            {
                commands.clear();
                commands.addElement(getAutoAffects(mob));
                return false;
            }
        }

        if(S!=null)
        {
            if(S==mob.session())
                S.colorOnlyPrint(" \n\r^!Your auto-invoked skills are:^?");
            String msg=getAutoAffects(mob);
            if(msg.length()<5)
                S.colorOnlyPrintln(" Non-existant!\n\r^N");
            else
                S.colorOnlyPrintln(msg);
        }
        return false;
    }
    
    public boolean canBeOrdered(){return true;}
}
    
