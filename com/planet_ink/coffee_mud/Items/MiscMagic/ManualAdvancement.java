package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.StdItem;
import java.util.*;


public class ManualAdvancement extends StdItem implements MiscMagic
{
	public String ID(){	return "ManualAdvancement";}
	public ManualAdvancement()
	{
		super();

		setName("a book");
		baseEnvStats().setWeight(1);
		setDisplayText("an ornately bound book sits here.");
		setDescription("An ornately bound book filled with mystical symbols.");
		secretIdentity="The Manual of Advancement.";
		this.setUsesRemaining(5);
		baseGoldValue=10000;
		material=EnvResource.RESOURCE_PAPER;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new ManualAdvancement();
	}

	public void affect(Environmental myHost, Affect affect)
	{
		if(affect.amITarget(this))
		{
			MOB mob=affect.source();
			switch(affect.targetMinor())
			{
			case Affect.TYP_READSOMETHING:
				if(mob.isMine(this))
				{
					if(mob.fetchAffect("Spell_ReadMagic")!=null)
					{
						if(this.usesRemaining()<=0)
							mob.tell("The markings have been read off the parchment, and are no longer discernable.");
						else
						{
							this.setUsesRemaining(this.usesRemaining()-1);
							mob.tell("The manual glows softly, enveloping you in its wisdom.");
							mob.charStats().getCurrentClass().gainExperience(mob,null,null,mob.getExpNeededLevel()+1,false);
						}
					}
					else
						mob.tell("The markings look magical, and are unknown to you.");
				}
				return;
			default:
				break;
			}
		}
		super.affect(myHost,affect);
	}

}
