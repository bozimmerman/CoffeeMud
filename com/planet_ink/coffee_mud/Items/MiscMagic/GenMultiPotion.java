package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.GenDrink;
import java.util.*;

public class GenMultiPotion extends GenDrink implements Potion
{
	public String ID(){	return "GenMultiPotion";}
	protected Ability theSpell;

	public GenMultiPotion()
	{
		super();

		material=EnvResource.RESOURCE_GLASS;
		setName("a flask");
		baseEnvStats.setWeight(1);
		setDisplayText("A flask sits here.");
		setDescription("A strange flask with stranger markings.");
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
		return StdScroll.makeSecretIdentity("potion",super.secretIdentity(),"",getSpells(this));
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

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((msg.amITarget(this))
		   &&(msg.targetMinor()==CMMsg.TYP_DRINK)
		   &&(msg.othersMessage()==null)
		   &&(msg.sourceMessage()==null))
				return true;
		return super.okMessage(myHost,msg);
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_DRINK:
				if((msg.sourceMessage()==null)&&(msg.othersMessage()==null))
				{
					drinkIfAble(mob,this);
					if(isDrunk())
					{
						mob.tell(name()+" vanishes!");
						destroy();
					}
				}
				else
				{
					msg.addTrailerMsg(new FullMsg(msg.source(),this,msg.tool(),msg.NO_EFFECT,null,msg.targetCode(),msg.targetMessage(),msg.NO_EFFECT,null));
					super.executeMsg(myHost,msg);
				}
				break;
			default:
				super.executeMsg(myHost,msg);
				break;
			}
		}
		else
			super.executeMsg(myHost,msg);
	}
	// stats handled by gendrink, spells by readabletext
}
