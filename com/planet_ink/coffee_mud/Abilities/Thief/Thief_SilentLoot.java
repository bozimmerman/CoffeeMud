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

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)&&(affected instanceof MOB))
		{
			if((msg.sourceMinor()==CMMsg.TYP_DEATH)
			&&(msg.source()!=affected)
			&&(Sense.canBeSeenBy(msg.source(),(MOB)affected))
			&&(msg.source().location()==((MOB)affected).location())
			&&((msg.source().inventorySize())>0))
			{
				Item item=msg.source().fetchCarried(null,"all");
				if(item==null) item=msg.source().fetchWornItem("all");
				if((item!=null)&&(msg.source().isMine(item)))
				{
					item.unWear();
					item.removeFromOwnerContainer();
					item.setContainer(null);
					MOB mob=(MOB)affected;
					mob.location().addItemRefuse(item,Item.REFUSE_MONSTER_EQ);
					MOB victim=mob.getVictim();
					mob.setVictim(null);
					FullMsg msg2=new FullMsg(mob,item,this,CMMsg.MSG_THIEF_ACT,"You silently autoloot "+item.name()+" from the corpse of "+msg.source().name(),CMMsg.MSG_THIEF_ACT,null,CMMsg.NO_EFFECT,null);
					if(mob.location().okMessage(mob,msg2))
					{
						mob.location().send(mob,msg2);
						ExternalPlay.get(mob,null,item,true);
					}
					if(victim!=null) mob.setVictim(victim);
				}
			}
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((mob.fetchEffect(ID())!=null))
		{
			mob.tell("You are no longer automatically looting items from corpses silently.");
			mob.delEffect(mob.fetchEffect(ID()));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			mob.tell("You will now automatically loot items from corpses silently.");
			beneficialAffect(mob,mob,0);
			Ability A=mob.fetchEffect(ID());
			if(A!=null) A.makeLongLasting();
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to start silently looting items from corpses, but fail(s).");
		return success;
	}

}