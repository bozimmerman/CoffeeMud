package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
/* 
   Copyright 2000-2005 Lee H. Fox

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
public class PegasusGreater extends StdRideable
{
    protected int rideBasis=Rideable.RIDEABLE_AIR;

    public PegasusGreater()
    {
        super();
        Random randomizer = new Random(System.currentTimeMillis());

        Username="a Greater Pegasus";
        setDescription("a beautiful, white stallion with wings.");
        setDisplayText("A regal Pegasus flaps its wings.");
        setAlignment(500);
        setMoney(0);
        setWimpHitPoint(0);

        baseEnvStats.setWeight(1500 + Math.abs(randomizer.nextInt() % 200));


        baseCharStats().setStat(CharStats.INTELLIGENCE,8 + Math.abs(randomizer.nextInt() % 3));
        baseCharStats().setStat(CharStats.STRENGTH,11);
        baseCharStats().setStat(CharStats.DEXTERITY,17);
        baseCharStats().setMyRace(CMClass.getRace("Horse"));
        baseCharStats().getMyRace().startRacing(this,false);

        baseEnvStats().setDamage(8);
        baseEnvStats().setSpeed(3.0);
        baseEnvStats().setAbility(0);
        baseEnvStats().setLevel(6);
        baseEnvStats().setArmor(60);
        baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_FLYING);

        baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

        recoverMaxState();
        resetToMaxState();
        recoverEnvStats();
        recoverCharStats();
    }

}
