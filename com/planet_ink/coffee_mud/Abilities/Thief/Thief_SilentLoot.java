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
			&&((affect.source().inventorySize())>0))
			{
				item=affect.source().fetchCarried(null,"all");
				if(item==null) item=affect.source().fetchWornItem("all");
				if(item!=null)
					affect.addTrailerMsg(new FullMsg((MOB)affected,affect.source(),this,Affect.MSG_THIEF_ACT|Affect.MASK_MALICIOUS,"You silently autoloot "+item.name()+" from the corpse of "+affect.source().name(),Affect.NO_EFFECT,null,Affect.NO_EFFECT,null));
			}
			else
			if((affect.sourceMinor()==Affect.TYP_DELICATE_HANDS_ACT)
			&&(affect.tool()==this)
			&&(affect.target()!=null)
			&&(affect.target() instanceof MOB)
			&&(affect.amISource((MOB)affected)))
			{
				MOB mob=(MOB)affect.source();
				MOB target=(MOB)affect.target();
				if((item!=null)&&(target.isMine(item)))
				{
					item.remove();
					item.removeThis();
					item.setContainer(null);
					mob.location().addItemRefuse(item,Item.REFUSE_PLAYER_DROP);
					ExternalPlay.get(mob,null,item,true);
				}
				else
					mob.tell("Oops.. it's GONE!");
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