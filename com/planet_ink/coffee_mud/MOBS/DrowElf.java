package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Races.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.db.*;
import com.planet_ink.coffee_mud.StdAffects.*;

public class DrowElf extends StdMOB
{

	public static final int MALE	= 0;
	public static final int FEMALE	= 1;

	public int darkDown=4;

	public DrowElf()
	{
		super();

		Random randomizer = new Random(System.currentTimeMillis());
		
		baseEnvStats().setLevel(4 + Math.abs(randomizer.nextInt() % 7));

		int gender = Math.abs(randomizer.nextInt() % 2);
		String sex = null;
		if (gender == MALE)
			sex = "male";
		else
			sex = "female";

		// ===== set the basics
		Username="a Drow Elf";
		setDescription("a " + sex + " Drow Fighter");
		setDisplayText("The drow is armored in black chain mail and carrying a nice arsenal of weapons");
		
		maxState.setHitPoints(25 + (baseEnvStats.level() * 2) + (randomizer.nextInt() % 15));
		setMoney((int)Math.round(Util.div((50 * baseEnvStats().level()),(randomizer.nextInt() % 10 + 1))));
		baseEnvStats.setWeight(70 + Math.abs(randomizer.nextInt() % 20));
		
		setWimpHitPoint(5);
		
		baseEnvStats().setSpeed(2.0);
		baseEnvStats().setSensesMask(Sense.CAN_SEE_DARK | Sense.CAN_SEE_INFRARED);
		
		if(gender == MALE)
			baseCharStats().setGender('M');
		else
			baseCharStats().setGender('F');

		baseCharStats().setStrength(12 + Math.abs(randomizer.nextInt() % 6));
		baseCharStats().setIntelligence(14 + Math.abs(randomizer.nextInt() % 6));
		baseCharStats().setWisdom(13 + Math.abs(randomizer.nextInt() % 6));
		baseCharStats().setDexterity(15 + Math.abs(randomizer.nextInt() % 6));
		baseCharStats().setConstitution(12 + Math.abs(randomizer.nextInt() % 6));
		baseCharStats().setCharisma(13 + Math.abs(randomizer.nextInt() % 6));


		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	
	public boolean tick(int tickID)
	{
		if((!amDead())&&(tickID==ServiceEngine.MOB_TICK))
		{
			if (isInCombat())
			{
				if((--darkDown)<=0)
				{
					darkDown=4;
					castDarkness();
				}
			}
									 
		}
		return super.tick(tickID);
	}

	protected boolean castDarkness()
	{
		if(this.location()==null) 
			return true;
		if(Sense.isInDark(this.location()))
			return true;
		
		com.planet_ink.coffee_mud.Abilities.Spell_Darkness dark=new com.planet_ink.coffee_mud.Abilities.Spell_Darkness();
		dark.setProfficiency(100);
		if(this.fetchAbility(dark.ID())==null)
		   this.addAbility(dark);
		else
			dark=(com.planet_ink.coffee_mud.Abilities.Spell_Darkness)this.fetchAbility(dark.ID());
		
		dark.invoke(this,new Vector());
		return true;
	}

	public Environmental newInstance()
	{
		return new DrowElf();
	}
	
}
