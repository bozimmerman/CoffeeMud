package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenMultiPotion extends GenPotion
{
	protected Ability theSpell;

	public GenMultiPotion()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		material=EnvResource.RESOURCE_GLASS;
	}


	public Environmental newInstance()
	{
		return new GenMultiPotion();
	}
	public boolean isGeneric(){return true;}

	public void drinkIfAble(MOB mob, Potion me)
	{
		if(!(me instanceof Drink)) return;

		Vector spells=getSpells(me);
		if(mob.isMine(me))
			if((!me.isDrunk())&&(spells.size()>0)&&(((Drink)me).liquidRemaining()>0))
			{
				for(int i=0;i<spells.size();i++)
				{
					Ability thisOne=(Ability)((Ability)spells.elementAt(i)).copyOf();
					thisOne.invoke(mob,mob,true);
				}
			}
			else
			if(!me.isDrunk())
				setDrunk(me,true);

	}
}
