package com.planet_ink.coffee_mud.Items.MiscMagic;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.StdFood;
import java.util.*;


public class StdPill extends StdFood implements Pill
{
	public String ID(){	return "StdPill";}
	protected Ability theSpell;

	public StdPill()
	{
		super();

		setName("a pill");
		baseEnvStats.setWeight(1);
		setDisplayText("An strange pill lies here.");
		setDescription("Large and round, with strange markings.");
		secretIdentity="Surely this is a potent pill!";
		baseGoldValue=200;
		recoverEnvStats();
		material=EnvResource.RESOURCE_CORN;
	}

	public Environmental newInstance()
	{
		return new StdPill();
	}

	public String secretIdentity()
	{
		return StdScroll.makeSecretIdentity("pill",super.secretIdentity(),"",getSpells(this));
	}

	public void eatIfAble(MOB mob, Pill me)
	{
		Vector spells=me.getSpells(me);
		if((mob.isMine(me))&&(spells.size()>0))
			for(int i=0;i<spells.size();i++)
			{
				Ability thisOne=(Ability)((Ability)spells.elementAt(i)).copyOf();
				thisOne.invoke(mob,mob,true);
			}
	}

	public String getSpellList()
	{ return miscText;}
	public void setSpellList(String list){miscText=list;}
	public Vector getSpells(Pill me)
	{
		String names=me.getSpellList();

		Vector theSpells=new Vector();
		int del=names.indexOf(";");
		while(del>=0)
		{
			String thisOne=names.substring(0,del);
			if((thisOne.length()>0)&&(!thisOne.equals(";")))
			{
				Ability A=(Ability)CMClass.getAbility(thisOne);
				if(A!=null)
				{
					A=(Ability)A.copyOf();
					theSpells.addElement(A);
				}
			}
			names=names.substring(del+1);
			del=names.indexOf(";");
		}
		if((names.length()>0)&&(!names.equals(";")))
		{
			Ability A=(Ability)CMClass.getAbility(names);
			if(A!=null)
			{
				A=(Ability)A.copyOf();
				theSpells.addElement(A);
			}
		}
		me.recoverEnvStats();
		return theSpells;
	}

	public void affect(Environmental myHost, Affect affect)
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
					super.affect(myHost,affect);
				}
				else
					affect.addTrailerMsg(new FullMsg(affect.source(),affect.target(),affect.tool(),affect.NO_EFFECT,null,affect.targetCode(),affect.targetMessage(),affect.NO_EFFECT,null));
				break;
			default:
				super.affect(myHost,affect);
				break;
			}
		}
		else
			super.affect(myHost,affect);
	}
}
