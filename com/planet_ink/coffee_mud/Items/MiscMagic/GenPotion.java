package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.GenWater;
import java.util.*;

public class GenPotion extends GenWater implements Potion
{
	public String ID(){	return "GenPotion";}
	protected Ability theSpell;

	public GenPotion()
	{
		super();

		name="a potion";
		baseEnvStats.setWeight(1);
		displayText="A potion sits here.";
		description="A strange potion with stranger markings.";
		secretIdentity="";
		baseGoldValue=200;
		recoverEnvStats();
		material=EnvResource.RESOURCE_GLASS;
	}


	public Environmental newInstance()
	{
		return new GenPotion();
	}
	public boolean isGeneric(){return true;}
	public int liquidType(){return EnvResource.RESOURCE_DRINKABLE;}

	public boolean isDrunk(){return (readableText.toUpperCase().indexOf(";DRUNK")>=0);}
	public void setDrunk(Potion me, boolean isTrue)
	{ new StdPotion().setDrunk(this,isTrue);}

	public String secretIdentity()
	{
		return StdScroll.makeSecretIdentity("potion",super.secretIdentity(),getSpells(this));
	}
	
	public int value()
	{
		if(isDrunk()) 
			return 0;
		else 
			return super.value();
	}

	public void drinkIfAble(MOB mob, Potion me)
	{new StdPotion().drinkIfAble(mob,me);}

	public String getSpellList()
	{ return readableText;}
	public void setSpellList(String list){readableText=list;}
	public Vector getSpells(Potion me)
	{	return new StdPotion().getSpells(me);}

	public boolean okAffect(Affect affect)
	{
		if((affect.amITarget(this))
		   &&(affect.targetMinor()==Affect.TYP_DRINK)
		   &&(affect.othersMessage()==null)
		   &&(affect.sourceMessage()==null))
				return true;
		return super.okAffect(affect);
	}
	
	public void affect(Affect affect)
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
					this.destroyThis();
				}
				else
				{
					affect.addTrailerMsg(new FullMsg(affect.source(),affect.target(),affect.tool(),affect.NO_EFFECT,null,affect.targetCode(),affect.targetMessage(),affect.NO_EFFECT,null));
					super.affect(affect);
				}
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
