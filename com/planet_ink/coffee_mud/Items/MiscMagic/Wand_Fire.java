package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.Abilities.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.Items.*;
import java.util.*;

public class Wand_Fire extends Wand implements MiscMagic
{
	
	public Wand_Fire()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a gold wand";
		displayText="a golden wand is here.";
		description="A wand made out of gold, with a deep red ruby at the tip";
		secretIdentity="The wand of fire.  Responds to 'Blaze' and 'Burn'";
		this.setUsesRemaining(50);
		baseGoldValue=20000;
		baseEnvStats().setLevel(12);
		recoverEnvStats();
	}
	
	public Environmental newInstance()
	{
		return new Wand_Fire();
	}
	
	public void affect(Affect affect)
	{
		MOB mob=affect.source();
		switch(affect.sourceCode())
		{
		case Affect.SOUND_WORDS:
			if((mob.isMine(this))
			   &&(!amWearingAt(Item.INVENTORY))
			   &&(affect.target() instanceof MOB)
			   &&(mob.location().isInhabitant((MOB)affect.target())))
			{
				MOB target=(MOB)affect.target();
				int x=affect.targetMessage().toUpperCase().indexOf("BLAZE");
				if(x>=0)
				{
					if((usesRemaining()>0)&&(useTheWand(new Spell_BurningHands(),mob)))
					{
						this.setUsesRemaining(this.usesRemaining()-1);
						Spell_BurningHands spell = new Spell_BurningHands();
						spell.invoke(mob, target, true);
						return;
					}
				}
				x=affect.targetMessage().toUpperCase().indexOf("BURN");
				if(x>=0)
				{
					if((usesRemaining()>4)&&(useTheWand(new Spell_Fireball(),mob)))
					{
						this.setUsesRemaining(this.usesRemaining()-5);
						Spell_Fireball spell = new Spell_Fireball();
						spell.invoke(mob, target, true);
						return;
					}
				}
			}
			break;
		default:
			break;
		}
		super.affect(affect);
	}
}
