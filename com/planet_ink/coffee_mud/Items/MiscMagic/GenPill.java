package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.GenFood;
import java.util.*;

public class GenPill extends GenFood implements Pill
{
	protected Ability theSpell;

	public GenPill()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a pill";
		baseEnvStats.setWeight(1);
		displayText="An strange pill lies here.";
		description="Large and round, with strange markings.";
		secretIdentity="";
		baseGoldValue=200;
		recoverEnvStats();
		material=Item.VEGETATION;
	}

	public Environmental newInstance()
	{
		return new GenPill();
	}
	public boolean isGeneric(){return true;}

	public void eatIfAble(MOB mob, Pill me)
	{ new StdPill().eatIfAble(mob,me);	}

	public String secretIdentity()
	{
		return StdScroll.makeSecretIdentity("pill",super.secretIdentity(),getSpells(this));
	}

	public String getSpellList()
	{ return readableText;}
	public void setSpellList(String list){readableText=list;}
	public Vector getSpells(Pill me)
	{ return new StdPill().getSpells(me);}

	public void affect(Affect affect)
	{
		if(affect.amITarget(this))
		{
			MOB mob=affect.source();
			switch(affect.targetMinor())
			{
			case Affect.TYP_EAT:
				if((affect.sourceMessage()==null)&&(affect.othersMessage()==null))
				{
					eatIfAble(mob,this);
					super.affect(affect);
				}
				else
					affect.addTrailerMsg(new FullMsg(affect.source(),affect.target(),affect.tool(),affect.NO_EFFECT,null,affect.targetCode(),affect.targetMessage(),affect.NO_EFFECT,null));
				break;
			default:
				super.affect(affect);
				break;
			}
		}
		else
			super.affect(affect);
	}
}
