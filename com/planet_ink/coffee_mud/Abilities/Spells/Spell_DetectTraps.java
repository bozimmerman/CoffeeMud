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
			msg.append(trapCheck(mob.location()));
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

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.target()!=null)
		&&(msg.amISource((MOB)affected))
		&&(msg.sourceMinor()==CMMsg.TYP_EXAMINESOMETHING))
		{
			if((msg.tool()!=null)&&(msg.tool().ID().equals(ID())))
			{
				String str=trapHere((MOB)affected,msg.target());
				if(str.length()>0)
					((MOB)affected).tell(str);
			}
			else
			{
				FullMsg msg2=new FullMsg(msg.source(),msg.target(),this,CMMsg.MSG_EXAMINESOMETHING,msg.NO_EFFECT,msg.NO_EFFECT,null);
				msg.addTrailerMsg(msg2);
			}
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already detecting traps.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> gain(s) trap sensitivities!":"^S<S-NAME> incant(s) softly, and gain(s) sensitivity to traps!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> incant(s) and open(s) <S-HIS-HER> eyes, but the spell fizzles.");

		return success;
	}
}
