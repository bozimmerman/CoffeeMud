package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_SparringRoom extends Property
{
	public String ID() { return "Prop_SparringRoom"; }
	public String name(){ return "Player Death Neutralizing";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Prop_SparringRoom();}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!super.okAffect(myHost,affect))
			return false;

		if((affect.sourceMinor()==Affect.TYP_DEATH)
		&&(!affect.source().isMonster()))
		{
			MOB source=null;
			if((affect.tool()!=null)&&(affect.tool() instanceof MOB))
				source=(MOB)affect.tool();
			MOB target=affect.source();
			Room deathRoom=target.location();
			deathRoom.show(source,source,Affect.MSG_OK_VISUAL,affect.sourceMessage());
			Hashtable beneficiaries=new Hashtable();
			if((source!=null)&&(source.charStats()!=null))
			{
				CharClass C=source.charStats().getCurrentClass();
				if(source.isMonster()
				   &&(source.amFollowing()!=null)
				   &&(!source.amFollowing().isMonster())
				   &&(source.amFollowing().charStats()!=null))
					C=source.amFollowing().charStats().getCurrentClass();

				beneficiaries=C.dispenseExperience(source,target);
			}
			target.makePeace();
			target.setRiding(null);
			for(int a=target.numAffects()-1;a>=0;a--)
			{
				Ability A=target.fetchAffect(a);
				if(A!=null) A.unInvoke();
			}
			target.setLocation(null);
			while(target.numFollowers()>0)
			{
				MOB follower=target.fetchFollower(0);
				if(follower!=null)
				{
					follower.setFollowing(null);
					target.delFollower(follower);
				}
			}
			target.setFollowing(null);
			Room R=null;
			if(text().trim().length()>0)
				R=CMMap.getRoom(text().trim());
			if(R==null) R=target.getStartRoom();
			R.bringMobHere(target,false);
			target.bringToLife(R,true);
			target.location().showOthers(target,null,Affect.MSG_OK_ACTION,"<S-NAME> appears!");
			deathRoom.recoverRoomStats();
			return false;
		}
		return true;
	}
}
