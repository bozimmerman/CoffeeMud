package com.planet_ink.coffee_mud.Items.MiscMagic;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.StdDrink;
import java.util.*;


public class StdPotion extends StdDrink implements Potion
{
	public String ID(){	return "StdPotion";}
	public StdPotion()
	{
		super();

		name="a potion";
		baseEnvStats.setWeight(1);
		displayText="An empty potion sits here.";
		description="An empty potion with strange residue.";
		secretIdentity="What was once a powerful potion.";
		baseGoldValue=200;
		material=EnvResource.RESOURCE_GLASS;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new StdPotion();
	}

	public int liquidType(){return EnvResource.RESOURCE_DRINKABLE;}
	public boolean isDrunk(){return (miscText.toUpperCase().indexOf(";DRUNK")>=0);}
	public int value()
	{
		if(isDrunk()) 
			return 0;
		else 
			return super.value();
	}

	public void setDrunk(Potion me, boolean isTrue)
	{
		if(isTrue&&me.isDrunk()) return;
		if((!isTrue)&&(!me.isDrunk())) return;
		if(isTrue)
			me.setSpellList(me.getSpellList()+";DRUNK");
		else
		{
			String list="";
			Vector theSpells=me.getSpells(me);
			for(int v=0;v<theSpells.size();v++)
				list+=((Ability)theSpells.elementAt(v)).ID()+";";
			me.setSpellList(list);
		}
	}

	public void drinkIfAble(MOB mob, Potion me)
	{
		Vector spells=getSpells(me);
		if(mob.isMine(me))
			if((!me.isDrunk())&&(spells.size()>0))
				for(int i=0;i<spells.size();i++)
				{
					Ability thisOne=(Ability)((Ability)spells.elementAt(i)).copyOf();
					thisOne.invoke(mob,mob,true);
					me.setDrunk(me,true);
					if(me instanceof Drink)
						((Drink)me).setLiquidRemaining(0);
				}
	}

	public String getSpellList()
	{ return miscText;}
	public void setSpellList(String list){miscText=list;}
	public Vector getSpells(Potion me)
	{
		String names=me.getSpellList();

		Vector theSpells=new Vector();
		int del=names.indexOf(";");
		while(del>=0)
		{
			String thisOne=names.substring(0,del);
			if((thisOne.length()>0)&&(!thisOne.equals(";"))&&(!thisOne.equals("DRUNK")))
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
		if((names.length()>0)&&(!names.equals(";"))&&(!names.equals("DRUNK")))
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

	public String secretIdentity()
	{
		return StdScroll.makeSecretIdentity("potion",super.secretIdentity(),getSpells(this));
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affect.amITarget(this))
		   &&(affect.targetMinor()==Affect.TYP_DRINK)
		   &&(affect.othersMessage()==null)
		   &&(affect.sourceMessage()==null))
				return true;
		return super.okAffect(myHost,affect);
	}
	
	public void affect(Environmental myHost, Affect affect)
	{
		if(affect.amITarget(this))
		{
			MOB mob=affect.source();
			switch(affect.targetMinor())
			{
			case Affect.TYP_DRINK:
				if((affect.sourceMessage()==null)&&(affect.othersMessage()==null))
				{
					drinkIfAble(mob,this);
					mob.tell(name()+" vanishes!");
					destroyThis();
				}
				else
				{
					affect.addTrailerMsg(new FullMsg(affect.source(),affect.target(),affect.tool(),affect.NO_EFFECT,null,affect.targetCode(),affect.targetMessage(),affect.NO_EFFECT,null));
					super.affect(myHost,affect);
				}
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
