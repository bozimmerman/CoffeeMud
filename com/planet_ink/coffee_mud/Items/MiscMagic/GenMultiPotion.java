package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.GenWater;
import java.util.*;

public class GenMultiPotion extends GenWater implements Potion
{
	public String ID(){	return "GenMultiPotion";}
	protected Ability theSpell;

	public GenMultiPotion()
	{
		super();

		material=EnvResource.RESOURCE_GLASS;
		name="a flask";
		baseEnvStats.setWeight(1);
		displayText="A flask sits here.";
		description="A strange flask with stranger markings.";
		secretIdentity="";
		baseGoldValue=200;
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new GenMultiPotion();
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

	public String getSpellList()
	{ return readableText;}
	public void setSpellList(String list){readableText=list;}
	public Vector getSpells(Potion me)
	{	return new StdPotion().getSpells(me);}


	public void drinkIfAble(MOB mob, Potion me)
	{
		if(!(me instanceof Drink)) return;

		Vector spells=getSpells(me);
		if(mob.isMine(me))
		{
			if((!me.isDrunk())&&(spells.size()>0))
			{
				for(int i=0;i<spells.size();i++)
				{
					Ability thisOne=(Ability)((Ability)spells.elementAt(i)).copyOf();
					thisOne.invoke(mob,mob,true);
				}
			}
			
			if((((Drink)me).liquidRemaining()<=((Drink)me).thirstQuenched())&&(!me.isDrunk()))
				setDrunk(me,true);
		}

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
					if(isDrunk())
					{
						mob.tell(name()+" vanishes!");
						destroyThis();
					}
				}
				else
				{
					affect.addTrailerMsg(new FullMsg(affect.source(),this,affect.tool(),affect.NO_EFFECT,null,affect.targetCode(),affect.targetMessage(),affect.NO_EFFECT,null));
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
	// stats handled by gendrink, spells by readabletext
}
