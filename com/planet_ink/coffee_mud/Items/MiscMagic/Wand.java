package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.Items.*;
import java.util.*;

public class Wand extends StdItem implements MiscMagic
{
	
	public Wand()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a wand";
		baseEnvStats.setWeight(1);
		displayText="a wand is here.";
		description="Looks like an ordinary magic wand.";
		secretIdentity="The wand of undefinedness.";
		baseGoldValue=200;
		baseEnvStats().setDisposition(Sense.IS_BONUS);
		recoverEnvStats();
	}
	
	public boolean useTheWand(Ability A, MOB mob)
	{
		int manaRequired=5;
		int q=A.qualifyingLevel(mob);
		if(q>0)
		{
			if(q<mob.envStats().level())
				manaRequired=0;
			else
				manaRequired=5;
		}
		else
		{
			manaRequired=50;
		}
		if(manaRequired>mob.curState().getMana())
		{
			mob.tell("You don't have enough mana.");
			return false;
		}
		mob.curState().adjMana(-manaRequired,mob.maxState());
		return true;
	}
	
	public Environmental newInstance()
	{
		return new Wand();
	}
}
