package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.GenFood;
import java.util.*;

public class GenPill extends GenFood implements Pill
{
	public String ID(){	return "GenPill";}
	protected Ability theSpell;

	public GenPill()
	{
		super();

		setName("a pill");
		baseEnvStats.setWeight(1);
		setDisplayText("An strange pill lies here.");
		setDescription("Large and round, with strange markings.");
		secretIdentity="";
		baseGoldValue=200;
		recoverEnvStats();
		material=EnvResource.RESOURCE_CORN;
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
		return StdScroll.makeSecretIdentity("pill",super.secretIdentity(),"",getSpells(this));
	}

	public String getSpellList()
	{ return readableText;}
	public void setSpellList(String list){readableText=list;}
	public Vector getSpells(Pill me)
	{ return new StdPill().getSpells(me);}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_EAT:
				if((msg.sourceMessage()==null)&&(msg.othersMessage()==null))
				{
					eatIfAble(mob,this);
					super.executeMsg(myHost,msg);
				}
				else
					msg.addTrailerMsg(new FullMsg(msg.source(),msg.target(),msg.tool(),msg.NO_EFFECT,null,msg.targetCode(),msg.targetMessage(),msg.NO_EFFECT,null));
				break;
			default:
				super.executeMsg(myHost,msg);
				break;
			}
		}
		else
			super.executeMsg(myHost,msg);
	}
	// stats handled by genfood, spells by readabletext
}
