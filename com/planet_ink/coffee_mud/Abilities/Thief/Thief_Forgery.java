package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Forgery extends ThiefSkill
{
	public String ID() { return "Thief_Forgery"; }
	public String name(){ return "Forgery";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"FORGERY"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_Forgery();}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<2)
		{
			mob.tell("What would you like to forge, and onto what?");
			return false;
		}
		Item target=mob.fetchInventory((String)commands.lastElement());
		if((target==null)||(!Sense.canBeSeenBy(target,mob)))
		{
			mob.tell("You don't see '"+target.displayName()+"' here.");
			return false;
		}
		commands.removeElement(commands.lastElement());

		if((target==null)
		   ||(!target.isGeneric())
		   ||((!(target instanceof Scroll))&&(!target.isReadable())))
		{
			mob.tell("You can't forge anything on that.");
			return false;
		}

		String forgeWhat=Util.combine(commands,0);
		if(forgeWhat.length()==0)
		{
			mob.tell("Forge what onto '"+target.displayName()+"'?  Try a spell name, a room ID, or a bank note name.");
			return false;
		}
		
		String newName="";
		String newDisplay="";
		String newDescription="";
		String newSecretIdentity="";
		Room room=CMMap.getRoom(forgeWhat);
		if(room!=null)
		{
			Item I=CMClass.getItem("StdTitle");
			((LandTitle)I).setLandRoomID(room.ID());
			newName=I.name();
			newDescription=I.description();
			newDisplay=I.displayText();
			newSecretIdentity=I.secretIdentity();
		}
		if(newName.length()==0)
		{
			Ability A=CMClass.findAbility(forgeWhat);
			if((A!=null)&&((A.classificationCode()&Ability.ALL_CODES)!=Ability.SPELL))
			{
				mob.tell("You can't forge '"+A.displayName()+"'.");
				return false;
			}
			else
			if(A!=null)
			{
				if(!(target instanceof Scroll))
				{
					mob.tell("You can only forge a spell onto real scrollpaper.");
					return false;
				}
				else
				if(((Scroll)target).numSpells()>0)
				{
					mob.tell("That already has real spells on it!");
					return false;
				}
				else					  
				{
					newName=target.name();
					newDisplay=target.displayText();
					newDescription=target.description();
					newSecretIdentity="a scroll of "+A.name()+" Charges: 10\n";
				}
			}
		}
		if(newName.length()==0)
		{
			int[] coins={5,10,50,100,500,1000,5000,10000,100000,1000000,10000000};
			for(int i=0;i<coins.length;i++)
			{
				Item note=Money.makeNote(coins[i],null,null);
				if(CoffeeUtensils.containsString(note.name(),forgeWhat))
				{
					newName=note.name();
					newDisplay=note.displayText();
					newDescription=note.description();
					newSecretIdentity=note.rawSecretIdentity();
					break;
				}
			}
		}
		if(newName.length()==0)
		{
			mob.tell("You don't know how to forge a '"+forgeWhat+"'.  Try a spell name, a room ID, or a bank note name.");
			return false;
		}
		forgeWhat=newName;
		
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_THIEF_ACT,"<S-NAME> forge(s) "+forgeWhat+" on <T-NAMESELF>.");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				target.setName(newName);
				target.setDescription(newDescription);
				target.setDisplayText(newDisplay);
				target.setSecretIdentity(newSecretIdentity);
			}
		}
		else
			beneficialVisualFizzle(mob,target,"<S-NAME> attempt(s) to forge "+forgeWhat+", but fail(s).");
		return success;
	}
}