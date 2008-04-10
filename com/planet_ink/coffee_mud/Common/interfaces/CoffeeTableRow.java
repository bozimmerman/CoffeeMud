package com.planet_ink.coffee_mud.Common.interfaces;
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

/* 
   Copyright 2000-2008 Bo Zimmerman

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
public interface CoffeeTableRow extends CMCommon
{
    public final int STAT_LOGINS=0;
    public final int STAT_TICKSONLINE=1;
    public final int STAT_NEWPLAYERS=2;
    public final int STAT_LEVELSGAINED=3;
    public final int STAT_DEATHS=4;
    public final int STAT_PKDEATHS=5;
    public final int STAT_MARRIAGES=6;
    public final int STAT_BIRTHS=7;
    public final int STAT_DIVORCES=8;
    public final int STAT_CLASSCHANGE=9;
    public final int STAT_PURGES=10;
    public final int STAT_SKILLUSE=11;
    public final int STAT_TOTAL=12;
    
    public final int STAT_SPECIAL_NUMONLINE=1000;
    
    public long startTime();
    public long endTime();
    public void setStartTime(long time);
    public void setEndTime(long time);
    public long highestOnline();
    public long numberOnlineTotal();
    public long numberOnlineCounter();
    public String data();
    public void bumpVal(String s, int type);
    public void bumpVal(Environmental E, int type);
    public void totalUp(String code, long[] tot);
    public void populate(long start, long end, String data);

}
