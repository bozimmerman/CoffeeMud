package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Spell_DetectTraps extends Spell
{
	public String ID() { return "Spell_DetectTraps"; }
	public String name(){return "Detect Traps";}
	public String displayText(){return "(Detecting Traps)";}
	public int quality(){ return OK_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	Room lastRoom=null;
	public Environmental newInstance(){	return new Spell_DetectTraps();	}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_DIVINATION;	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			lastRoom=null;
		super.unInvoke();
		if(canBeUninvoked())
			mob.tell("Your senses are no longer sensitive to traps.");
	}
	public String trapCheck(Environmental E)
	{
		if(E!=null)
		if(CoffeeUtensils.fetchMyTrap(E)!=null)
			return E.name()+" is trapped.\n\r";
		return "";
	}
	
	public String trapHere(MOB mob, Environmental E)
	{
		StringBuffer msg=new StringBuffer("");
		if(E==null) return msg.toString();
		if((E instanceof Room)&&(Sense.canBeSeenBy(E,mob)))
		{
			Room room=(Room)E;
			msg.append(trapCheck(mob.location()));
		}
		else
		if((E instanceof Container)&&(Sense.canBeSeenBy(E,mob)))
		{
			Container C=(Container)E;
			Vector V=C.getContents();
			for(int v=0;v<V.size();v++)
				if(trapCheck((Item)V.elementAt(v)).length()>0)
					msg.append(C.name()+" contains something trapped.");
		}
		else
		if((E instanceof Item)&&(Sense.canBeSeenBy(E,mob)))
			msg.append(trapCheck((Item)E));
		else
		if((E instanceof Exit)&&(Sense.canBeSeenBy(E,mob)))
		{
			int dir=-1;
			Room room=mob.location();
			if(room!=null)
			for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
			{
				if(room.getExitInDir(d)==E)
				{
					Exit E2=room.getReverseExit(d);
					Room R2=room.getRoomInDir(d);
					msg.append(trapCheck(E));
					msg.append(trapCheck(E2));
					msg.append(trapCheck(R2));
					break;
				}
			}
		}
		else
		if((E instanceof MOB)&&(Sense.canBeSeenBy(E,mob)))
		{
			for(int i=0;i<((MOB)E).inventorySize();i++)
			{
				Item I=((MOB)E).fetchInventory(i);
				if(trapCheck(I).length()>0)
					return E.name()+" is carrying something trapped.";
			}
			if(CoffeeUtensils.getShopKeeper((MOB)E)!=null)
			{
				Vector V=CoffeeUtensils.getShopKeeper((MOB)E).getUniqueStoreInventory();
				for(int v=0;v<V.size();v++)
				{
					Environmental E2=(Environmental)V.elementAt(v);
					if(E2 instanceof Item)	
						if(trapCheck((Item)E2).length()>0)
							return E.name()+" has something trapped in stock.";
				}
			}
		}
		return msg.toString();
	}
	
	public void affect(Environmental myHost, Affect affect)
	{
		super.affect(myHost,affect);
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(affect.target()!=null)
		&&(affect.amISource((MOB)affected))
		&&(affect.sourceMinor()==Affect.TYP_EXAMINESOMETHING))
		{
			if((affect.tool()!=null)&&(affect.tool().ID().equals(ID())))
			{
				String msg=trapHere((MOB)affected,affect.target());
				if(msg.length()>0)
					((MOB)affected).tell(msg);
			}
			else
			{
				FullMsg msg=new FullMsg(affect.source(),affect.target(),this,affect.MSG_EXAMINESOMETHING,affect.NO_EFFECT,affect.NO_EFFECT,null);
				affect.addTrailerMsg(msg);
			}
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You are already detecting traps.");
			return false;
		}

		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> gain(s) liquid sensitivities!":"^S<S-NAME> incant(s) softly, and gain(s) sensitivity to traps!^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> incant(s) and open(s) <S-HIS-HER> liquified eyes, but the spell fizzles.");

		return success;
	}
}