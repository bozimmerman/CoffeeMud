package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Wand_Nourishment extends StdWand
{
	public String ID(){	return "Wand_Nourishment";}
	public Wand_Nourishment()
	{
		super();

		name="a wooden wand";
		displayText="a small wooden wand is here.";
		description="A wand made out of wood";
		secretIdentity="The wand of nourishment.  Hold the wand say \\`shazam\\` to it.";
		baseGoldValue=200;
		material=EnvResource.RESOURCE_OAK;
		recoverEnvStats();
		secretWord="SHAZAM";
	}

	public Environmental newInstance()
	{
		return new Wand_Nourishment();
	}
	public void setSpell(Ability theSpell)
	{
		super.setSpell(theSpell);
		secretWord="SHAZAM";
	}
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		secretWord="SHAZAM";
	}

	public void affect(Environmental myHost, Affect affect)
	{
		if(affect.amITarget(this))
		{
			MOB mob=affect.source();
			switch(affect.targetMinor())
			{
			case Affect.TYP_SPEAK:
				if((mob.isMine(this))&&(!amWearingAt(Item.INVENTORY)))
					if(affect.targetMessage().toUpperCase().indexOf("'SHAZAM'")>=0)
						if(mob.curState().adjHunger(50,mob.maxState()))
							mob.tell("You are full.");
				break;
			default:
				break;
			}
		}
		super.affect(myHost,affect);
	}
}