package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_SilentLoot extends ThiefSkill
{
	public String ID() { return "Thief_SilentLoot"; }
	public String displayText() {return "(Silent AutoLoot)";}
	public String name(){ return "Silent AutoLoot";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.OK_SELF;}
	private static final String[] triggerStrings = {"SILENTLOOT"};
	public String[] triggerStrings(){return triggerStrings;}
	private Item item=null;
	public Environmental newInstance(){	return new Thief_SilentLoot();	}

	public void affect(Environmental myHost, Affect affect)
	{
		super.affect(myHost,affect);
		if((affected!=null)&&(affected instanceof MOB))
		{
			if((affect.sourceMinor()==Affect.TYP_DEATH)
			&&(affect.source()!=affected)
			&&(Sense.canBeSeenBy(affect.source(),(MOB)affected))
			&&(affect.source().location()==((MOB)affected).location())
			&&((affect.source().inventorySize())>0))
			{
				Item item=affect.source().fetchCarried(null,"all");
				if(item==null) item=affect.source().fetchWornItem("all");
				if((item!=null)&&(affect.source().isMine(item)))
				{
					item.unWear();
					item.removeFromOwnerContainer();
					item.setContainer(null);
					MOB mob=(MOB)affected;
					mob.location().addItemRefuse(item,Item.REFUSE_MONSTER_EQ);
					MOB victim=mob.getVictim();
					mob.setVictim(null);
					FullMsg msg=new FullMsg(mob,item,this,Affect.MSG_THIEF_ACT,"You silently autoloot "+item.name()+" from the corpse of "+affect.source().name(),Affect.MSG_THIEF_ACT,null,Affect.NO_EFFECT,null);
					if(mob.location().okAffect(mob,msg))
					{
						mob.location().send(mob,msg);
						ExternalPlay.get(mob,null,item,true);
					}
					if(victim!=null) mob.setVictim(victim);
				}
			}
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((mob.fetchAffect(ID())!=null))
		{
			mob.tell("You are no longer automatically looting items from corpses silently.");
			mob.delAffect(mob.fetchAffect(ID()));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			mob.tell("You will now automatically loot items from corpses silently.");
			beneficialAffect(mob,mob,0);
			Ability A=mob.fetchAffect(ID());
			if(A!=null) A.makeLongLasting();
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to start silently looting items from corpses, but fail(s).");
		return success;
	}

}