package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Wand_Fire extends StdWand
{
	public String ID(){	return "Wand_Fire";}
	public Wand_Fire()
	{
		super();

		setName("a gold wand");
		setDisplayText("a golden wand is here.");
		setDescription("A wand made out of gold, with a deep red ruby at the tip");
		secretIdentity="The wand of fire.  Responds to 'Blaze' and 'Burn'";
		this.setUsesRemaining(50);
		baseGoldValue=20000;
		baseEnvStats().setLevel(12);
		material=EnvResource.RESOURCE_OAK;
		recoverEnvStats();
		secretWord="BLAZE, BURN";
	}

	public Environmental newInstance()
	{
		return new Wand_Fire();
	}
	public void setSpell(Ability theSpell)
	{
		super.setSpell(theSpell);
		secretWord="BLAZE, BURN";
	}
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		secretWord="BLAZE, BURN";
	}


	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		MOB mob=msg.source();
		switch(msg.sourceMinor())
		{
		case CMMsg.TYP_WAND_USE:
			if((mob.isMine(this))
			   &&(!amWearingAt(Item.INVENTORY))
			   &&(msg.target() instanceof MOB)
			   &&(mob.location().isInhabitant((MOB)msg.target())))
			{
				MOB target=(MOB)msg.target();
				int x=msg.targetMessage().toUpperCase().indexOf("BLAZE");
				if(x>=0)
				{
					Ability spell = CMClass.getAbility("Spell_BurningHands");
					if((usesRemaining()>0)&&(spell!=null)&&(useTheWand(spell,mob)))
					{
						this.setUsesRemaining(this.usesRemaining()-1);
						spell.invoke(mob, target, true);
						return;
					}
				}
				x=msg.targetMessage().toUpperCase().indexOf("BURN");
				if(x>=0)
				{
					Ability spell = CMClass.getAbility("Spell_Fireball");
					if((usesRemaining()>4)&&(spell!=null)&&(useTheWand(spell,mob)))
					{
						this.setUsesRemaining(this.usesRemaining()-5);
						spell.invoke(mob, target, true);
						return;
					}
				}
			}
			break;
		default:
			break;
		}
		super.executeMsg(myHost,msg);
	}
}
