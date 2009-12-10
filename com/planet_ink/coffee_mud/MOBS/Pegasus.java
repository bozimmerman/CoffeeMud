package com.planet_ink.coffee_mud.MOBS;
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
   Copyright 2000-2010 Lee H. Fox

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
public class Pegasus extends StdRideable
{
    Random randomizer = null;

    public Pegasus()
    {
        super();
        randomizer = new Random(System.currentTimeMillis());

        Username="a Pegasus";
        setDescription("a beautiful, white stallion with wings.");
        setDisplayText("A Pegasus flaps its wings.");
        CMLib.factions().setAlignment(this,Faction.ALIGN_NEUTRAL);
        setMoney(0);
        setWimpHitPoint(0);
        rideBasis=Rideable.RIDEABLE_AIR;

        baseEnvStats.setWeight(1500 + Math.abs(randomizer.nextInt() % 200));


        baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,8 + Math.abs(randomizer.nextInt() % 3));
        baseCharStats().setStat(CharStats.STAT_STRENGTH,11);
        baseCharStats().setStat(CharStats.STAT_DEXTERITY,17);
        baseCharStats().setMyRace(CMClass.getRace("Horse"));
        baseCharStats().getMyRace().startRacing(this,false);

        baseEnvStats().setDamage(8);
        baseEnvStats().setSpeed(3.0);
        baseEnvStats().setAbility(0);
        baseEnvStats().setLevel(4);
        baseEnvStats().setArmor(60);
        baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_FLYING);

        baseState.setHitPoints(CMLib.dice().roll(baseEnvStats().level(),20,baseEnvStats().level()));

        recoverMaxState();
        resetToMaxState();
        recoverEnvStats();
        recoverCharStats();
    }

}
