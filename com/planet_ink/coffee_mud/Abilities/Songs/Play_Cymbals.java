package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Play_Cymbals extends Play_Instrument
{
	public String ID() { return "Play_Cymbals"; }
	public String name(){ return "Cymbals";}
	protected int requiredInstrumentType(){return MusicalInstrument.TYPE_CYMBALS;}
	public Environmental newInstance(){	return new Play_Cymbals();}
	public String mimicSpell(){return "Spell_Knock";}
	private static Ability theSpell=null;
	protected Ability getSpell()
	{
		if(theSpell!=null) return theSpell;
		if(mimicSpell().length()==0) return null;
		theSpell=CMClass.getAbility(mimicSpell());
		return theSpell;
	}
	
	protected void inpersistantAffect(MOB mob)
	{
		if(getSpell()!=null)
		{
			if((mob==invoker())&&(mob.location()!=null))
			{
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				{
					Exit e=mob.location().getExitInDir(d);
					if((e!=null)&&(e.hasADoor())&&(e.hasALock())&&(e.isLocked()))
					{
						Vector chcommands=new Vector();
						chcommands.addElement(Directions.getDirectionName(d));
						getSpell().invoke(invoker(),chcommands,null,true);
					}
				}
				for(int i=0;i<mob.location().numItems();i++)
				{
					Item I=mob.location().fetchItem(i);
					if((I!=null)&&(I instanceof Container)&&(I.container()==null))
					{
						Container C=(Container)I;
						if(C.hasALid()&&C.hasALock()&&C.isLocked())
						{
							Vector chcommands=new Vector();
							chcommands.addElement(C.name());
							getSpell().invoke(invoker(),chcommands,C,true);
						}
					}
				}
			}
			for(int i=0;i<mob.inventorySize();i++)
			{
				Item I=mob.fetchInventory(i);
				if((I!=null)&&(I instanceof Container)&&(I.container()==null))
				{
					Container C=(Container)I;
					if(C.hasALid()&&C.hasALock()&&C.isLocked())
					{
						Vector chcommands=new Vector();
						chcommands.addElement(C.name());
						getSpell().invoke(mob,chcommands,C,true);
					}
				}
			}
		}
	}
	protected int canAffectCode(){return 0;}
	
		
}
