package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Wand_Advancement extends StdWand
{
	public String ID(){	return "Wand_Advancement";}
	public Wand_Advancement()
	{
		super();

		name="a platinum wand";
		displayText="a platinum wand is here.";
		description="A wand made out of platinum";
		secretIdentity="The wand of Advancement.  Hold the wand say `level up` to it.";
		this.setUsesRemaining(50);
		material=EnvResource.RESOURCE_OAK;
		baseGoldValue=20000;
		recoverEnvStats();
		secretWord="LEVEL UP, LEVEL ALL UP";
	}

	public Environmental newInstance()
	{
		return new Wand_Advancement();
	}
	public void setSpell(Ability theSpell)
	{
		super.setSpell(theSpell);
		secretWord="LEVEL UP, LEVEL ALL UP";
	}
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		secretWord="LEVEL UP, LEVEL ALL UP";
	}
	

	public void affect(Affect affect)
	{
		MOB mob=affect.source();
		switch(affect.sourceMinor())
		{
		case Affect.TYP_SPEAK:
			if((mob.isMine(this))
			   &&(!amWearingAt(Item.INVENTORY))
			   &&(affect.target() instanceof MOB)
			   &&(mob.location().isInhabitant((MOB)affect.target())))
			{
				MOB target=(MOB)affect.target();
				int x=affect.targetMessage().toUpperCase().indexOf("'LEVEL UP'");
				if(x>=0)
				{
					if((usesRemaining()>0)&&(useTheWand(CMClass.getAbility("Falling"),mob)))
					{
						this.setUsesRemaining(this.usesRemaining()-1);
						FullMsg msg=new FullMsg(mob,affect.target(),null,Affect.MSG_HANDS,Affect.MSG_OK_ACTION,Affect.MSG_OK_ACTION,"<S-NAME> point(s) "+this.name()+" at <T-NAMESELF>, who begins to glow softly.");
						if(mob.location().okAffect(msg))
						{
							mob.location().send(mob,msg);
							target.charStats().getCurrentClass().gainExperience(target,null,null,target.getExpNeededLevel()+1);
						}

					}
				}
				x=affect.targetMessage().toUpperCase().indexOf("'LEVEL ALL UP'");
				if(x>=0)
				{
					if((usesRemaining()>0)&&(useTheWand(CMClass.getAbility("Falling"),mob)))
					{
						this.setUsesRemaining(this.usesRemaining()-1);
						FullMsg msg=new FullMsg(mob,affect.target(),null,Affect.MSG_HANDS,Affect.MSG_OK_ACTION,Affect.MSG_OK_ACTION,"<S-NAME> point(s) "+this.name()+" at <T-NAMESELF>, who begins to glow softly.");
						if(mob.location().okAffect(msg))
						{
							mob.location().send(mob,msg);
							while(target.envStats().level()<30)
								target.charStats().getCurrentClass().gainExperience(target,null,null,target.getExpNeededLevel()+1);
						}

					}
				}
			}
			break;
		default:
			break;
		}
	}
}
