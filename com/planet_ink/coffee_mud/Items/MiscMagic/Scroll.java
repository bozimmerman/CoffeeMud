package com.planet_ink.coffee_mud.Items.MiscMagic;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.Items.*;
import java.util.*;


public class Scroll extends StdItem implements MiscMagic
{
	protected boolean readable=false;
	
	public Scroll()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a scroll";
		baseEnvStats.setWeight(1);
		displayText="a scroll is rolled up here.";
		description="A rolled up parchment marked with mystical symbols.";
		secretIdentity="A magical scroll whose markings have been read off.";
		baseGoldValue=200;
		recoverEnvStats();
	}
	
	public Environmental newInstance()
	{
		return new Scroll();
	}
	
	public boolean useTheScroll(Ability A, MOB mob)
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
	
	public void affect(Affect affect)
	{
		if(affect.amITarget(this))
		{
			MOB mob=affect.source();
			switch(affect.targetCode())
			{
			case Affect.VISUAL_READ:
				if(mob.isMine(this))
				{
					if(mob.fetchAffect(new Spell_ReadMagic().ID())!=null)
						mob.tell("The markings have been read off the parchment, and are no longer discernable.");
					else
						mob.tell("The markings look magical, and are unknown to you.");
				}
				return;
			default:
				break;
			}
		}
		super.affect(affect);
	}
	
}
