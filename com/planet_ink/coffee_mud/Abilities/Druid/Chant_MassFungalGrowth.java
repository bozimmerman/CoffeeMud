package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Chant_MassFungalGrowth extends Chant_SummonFungus
{
	public String ID() { return "Chant_MassFungalGrowth"; }
	public String name(){ return "Mass Fungal Growth";}
	public Environmental newInstance(){	return new Chant_MassFungalGrowth();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		
		Vector V=new Vector();
		MUDTracker.getRadiantRooms(mob.location(),V,false,false,true,null,adjustedLevel(mob));
		for(int v=V.size()-1;v>=0;v--)
		{
			Room R=(Room)V.elementAt(v);
			if((R.domainType()!=Room.DOMAIN_INDOORS_CAVE)
			||(R==mob.location()))
				V.removeElementAt(v);
		}
		if(V.size()>0)
		{
			mob.location().show(mob,null,CMMsg.MASK_GENERAL|CMMsg.TYP_NOISE,"The faint sound of fungus popping into existence can be heard.");
			int done=0;
			for(int v=0;v<V.size();v++)
			{
				Room R=(Room)V.elementAt(v);
				if(R==mob.location()) continue;
				buildMyPlant(mob,R);
				if((done++)==adjustedLevel(mob))
					break;
			}
		}
		
		return true;
	}
}
