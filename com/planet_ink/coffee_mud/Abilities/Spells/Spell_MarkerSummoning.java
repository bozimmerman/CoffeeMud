package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_MarkerSummoning extends Spell
{
	public String ID() { return "Spell_MarkerSummoning"; }
	public String name(){return "Marker Summoning";}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Spell_MarkerSummoning();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_CONJURATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Room oldRoom=null;
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if(R!=null)
			for(int a=0;a<R.numAffects();a++)
			{
				Ability A=R.fetchAffect(a);
				if((A!=null)
				&&(A.invoker()==mob))
				{
					if(A.ID().equals("Spell_SummonMarker"))
					{
						oldRoom=R;
						break;
					}
				}
			}
			if(oldRoom!=null) break;
		}
		if(oldRoom==null)
		{
			mob.tell("You can't seem to focus on your marker.  Are you sure you've already summoned it?");
			return false;
		}
		Room newRoom=mob.location();
		if(oldRoom==newRoom)
		{
			mob.tell("But your marker is HERE!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;


		Vector inhabs=new Vector();
		int profNeg=0;
		for(int m=0;m<oldRoom.numInhabitants();m++)
		{
			MOB M=oldRoom.fetchInhabitant(m);
			if(M!=null){
				inhabs.addElement(M);
				int adjustment=M.envStats().level()-mob.envStats().level();
				profNeg+=adjustment;
			}
		}
		profNeg+=newRoom.numItems();

		boolean success=profficiencyCheck(-(profNeg/2),auto);

		if((success)&&(inhabs.size()>0))
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> summon(s) the power of <S-HIS-HER> marker energy!^?");
			if((mob.location().okAffect(mob,msg))&&(oldRoom.okAffect(mob,msg)))
			{
				mob.location().send(mob,msg);
				for(int i=0;i<inhabs.size();i++)
				{
					MOB follower=(MOB)inhabs.elementAt(i);
					FullMsg enterMsg=new FullMsg(follower,newRoom,this,Affect.MSG_ENTER,null,Affect.MSG_ENTER,null,Affect.MSG_ENTER,"<S-NAME> appear(s) in a burst of light.");
					FullMsg leaveMsg=new FullMsg(follower,oldRoom,this,Affect.MSG_LEAVE|Affect.MASK_MAGIC,"<S-NAME> disappear(s) in a great summoning swirl.");
					if(oldRoom.okAffect(follower,leaveMsg)&&newRoom.okAffect(follower,enterMsg))
					{
						follower.makePeace();
						oldRoom.send(follower,leaveMsg);
						newRoom.bringMobHere(follower,false);
						newRoom.send(follower,enterMsg);
						follower.tell("\n\r\n\r");
						ExternalPlay.look(follower,null,true);
					}
				}
				Vector items=new Vector();
				for(int i=oldRoom.numItems()-1;i>=0;i--)
				{
					Item I=oldRoom.fetchItem(i);
					if(I!=null) items.addElement(I);
				}
				for(int i=0;i<items.size();i++)
				{
					Item I=(Item)items.elementAt(i);
					oldRoom.showHappens(Affect.MSG_OK_VISUAL,I.name()+" disappears in a summoning swirl!");
					newRoom.bringItemHere(I,-1);
					newRoom.showHappens(Affect.MSG_OK_VISUAL,I.name()+" appears in a burst of light!");
				}
			}

		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> attempt(s) to summon <S-HIS-HER> marker energy, but fail(s).");


		// return whether it worked
		return success;
	}
}