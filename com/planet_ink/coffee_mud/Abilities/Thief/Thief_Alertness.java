package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Thief_Alertness extends ThiefSkill
{
	public String ID() { return "Thief_Alertness"; }
	public String name(){ return "Alertness";}
	public String displayText(){return "(Alertness)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"ALERTNESS"};
	protected boolean disregardsArmorCheck(MOB mob){return true;}
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_Alertness();	}
	Room room=null;


	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			
			MOB mob=(MOB)affected;
			if(!Sense.aliveAwakeMobile(mob,true))
			{ unInvoke(); return false;}
			if(mob.location()!=room)
			{
				room=mob.location();
				Vector choices=null;
				for(int i=0;i<room.numItems();i++)
				{
					Item I=room.fetchItem(i);
					if((I!=null)
					&&(Sense.canBeSeenBy(I,mob))
					&&(I.displayText().length()==0))
					{
						if(choices==null) choices=new Vector();
						choices.addElement(I);
					}
				}
				if(choices!=null)
				{
					Item I=(Item)choices.elementAt(Dice.roll(1,choices.size(),-1));
					mob.tell(I.name()+": "+I.description());
				}
			}
		}
		return true;
	}

	public void unInvoke()
	{
		MOB M=(MOB)affected;
		super.unInvoke();
		if((M!=null)&&(!M.amDead()))
			M.tell("You don't feel quite so alert any more.");
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already alert.");
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;


		boolean success=profficiencyCheck(mob,0,auto);

		FullMsg msg=new FullMsg(mob,target,this,auto?CMMsg.MSG_OK_ACTION:(CMMsg.MSG_DELICATE_HANDS_ACT|CMMsg.MASK_EYES),auto?"<T-NAME> become(s) alert.":"<S-NAME> become(s) suddenly alert.");
		if(!success)
			return beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to look alert, but become(s) distracted.");
		else
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,target,0);
		}
		return success;
	}
}
