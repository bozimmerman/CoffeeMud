package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class LizardManShaman extends LizardMan
{
	public String ID(){return "LizardManShaman";}
	private int spellDown=3;

	public LizardManShaman()
	{
		super();
		Username="a Lizard Man";
		setDescription("a 6 foot tall reptilian humanoid.");
		setDisplayText("A mean looking Lizard Man stands here.");
		setAlignment(0);
		setMoney(20);
		baseEnvStats.setWeight(225);
		setWimpHitPoint(0);

		baseCharStats().setStat(CharStats.INTELLIGENCE,9);
		baseCharStats().setStat(CharStats.CHARISMA,2);
		baseCharStats().setStat(CharStats.STRENGTH,18);

		baseCharStats().setMyRace(CMClass.getRace("LizardMan"));
		baseEnvStats().setAbility(0);
		baseEnvStats().setDamage(6);
		baseEnvStats().setSpeed(3);
		baseEnvStats().setLevel(3);
		baseEnvStats().setArmor(30);
		baseCharStats().setCurrentClass(CMClass.getCharClass("Cleric"));

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

    public void addNaturalAbilities()
    {
        Ability p1 =CMClass.getAbility("Prayer_ProtGood");
        p1.setProfficiency(Dice.roll(5, 10, 50));
		p1.setBorrowed(this,true);
        this.addAbility(p1);

        Ability p2 =CMClass.getAbility("Prayer_CauseLight");
        p2.setProfficiency(Dice.roll(5, 10, 50));
		p2.setBorrowed(this,true);
        this.addAbility(p2);

        Ability p3 =CMClass.getAbility("Prayer_Curse");
        p3.setProfficiency(Dice.roll(5, 10, 50));
		p3.setBorrowed(this,true);
        this.addAbility(p3);

        Ability p4 =CMClass.getAbility("Prayer_Paralyze");
        p4.setProfficiency(Dice.roll(5, 10, 50));
		p4.setBorrowed(this,true);
        this.addAbility(p4);

    }

	public boolean tick(Tickable ticking, int tickID)
	{
		if((!amDead())&&(tickID==MudHost.TICK_MOB))
		{
			if (isInCombat())
			{
				if((--spellDown)<=0)
				{
					spellDown=3;
					castSpell();
				}
			}

		}
		return super.tick(ticking,tickID);
	}

    public boolean castSpell()
    {
	    Ability prayer = null;
        if(Dice.rollPercentage() < 70)
        {
            prayer = fetchAbility(Dice.roll(1,numLearnedAbilities(),-1));
            while((prayer==null)||(this.baseEnvStats().level() < prayer.baseEnvStats().level()))
				prayer = fetchAbility(Dice.roll(1,numLearnedAbilities(),-1));
        }
        else
        {
            prayer = CMClass.getAbility("Prayer_CureLight");
            prayer.setProfficiency(Dice.roll(5, 10, 50));
        }

		if(prayer!=null)
		    return prayer.invoke(this,null,false,0);
		else
	        return false;
    }


}
