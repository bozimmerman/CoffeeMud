package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_SilentLoot extends ThiefSkill
{
	private Item item=null;
	public Thief_SilentLoot()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Silent AutoLoot";
		displayText="(Silent AutoLoot)";
		miscText="";

		triggerStrings.addElement("SILENTLOOT");

		canTargetCode=0;
		canAffectCode=Ability.CAN_MOBS;
		
		canBeUninvoked=true;
		isAutoinvoked=false;

		quality=Ability.OK_SELF;

		baseEnvStats().setLevel(1);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Thief_SilentLoot();
	}
	
	public void affect(Affect affect)
	{
		super.affect(affect);
		if((affected!=null)&&(affected instanceof MOB))
		{
			if((affect.sourceMinor()==Affect.TYP_DEATH)
			&&(affect.source()!=affected)
			&&((affect.source().inventorySize())>0))
			{
				item=affect.source().fetchCarried(null,"all");
				if(item==null) item=affect.source().fetchWornItem("all");
				if(item!=null)
					affect.addTrailerMsg(new FullMsg((MOB)affected,affect.source(),this,Affect.MSG_DELICATE_HANDS_ACT|Affect.MASK_MALICIOUS,"You silently autoloot "+item.name()+" from the corpse of "+affect.source().name(),Affect.NO_EFFECT,null,Affect.NO_EFFECT,null));
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
					mob.location().addItem(item);
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