package com.planet_ink.coffee_mud.Items.MiscMagic;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.StdFood;
import java.util.*;


public class StdPill extends StdFood implements Pill
{
	protected Ability theSpell;

	public StdPill()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a pill";
		baseEnvStats.setWeight(1);
		displayText="An strange pill lies here.";
		description="Large and round, with strange markings.";
		secretIdentity="Surely this is a potent pill!";
		baseGoldValue=200;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new StdPill();
	}

	public String secretIdentity()
	{
		return StdScroll.makeSecretIdentity("pill",super.secretIdentity(),getSpells(this));
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
