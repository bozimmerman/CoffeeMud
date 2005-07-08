package com.planet_ink.coffee_mud.Commands;
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
public class BaseItemParser extends StdCommand
{
    // mostly deprecated by the extension of util.EnglishParser
    public boolean hasOnlyGoldInInventory(MOB mob)
    {
        if(mob==null) return true;
        for(int i=0;i<mob.inventorySize();i++)
        {
            Item I=mob.fetchInventory(i);
            if((I.amWearingAt(Item.INVENTORY))
            &&(!(I instanceof Coins)))
                return false;
        }
        return true;
    }
    
}
