package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Dance_Cotillon extends Dance
{
	public String ID() { return "Dance_Cotillon"; }
	public String name(){ return "Cotillon";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected String danceOf(){return name()+" Dance";}
	protected MOB whichLast=null;

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if((affected==invoker())&&(((MOB)invoker()).isInCombat()))
		{
			if(whichLast==null)
				whichLast=(MOB)invoker();
			else
			{
				MOB M=(MOB)affected;
				boolean pass=false;
				boolean found=false;
				for(int i=0;i<M.location().numInhabitants();i++)
				{
					MOB M2=M.location().fetchInhabitant(i);
					if(M2==whichLast)
						found=true;
					else
					if((M2!=whichLast)
					&&(found)
					&&(M2.fetchEffect(ID())!=null)
					&&(M2.isInCombat()))
					{
						whichLast=M2;
						break;
					}
					if(i==(M.location().numInhabitants()-1))
					{
						if(pass)
							return true;
						else
						{
							pass=true;
							i=-1;
						}
					}
				}
				if((whichLast!=null)
				&&(M.isInCombat())
				&&(M.getVictim().getVictim()!=whichLast)
				&&(whichLast.location().show(whichLast,null,M.getVictim(),CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> dance(s) into <O-YOUPOSS> way.")))
					M.getVictim().setVictim(whichLast);
			}
		}
		return true;
	}

}