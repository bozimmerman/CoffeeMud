package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Map extends StdAbility
{
	public String ID() { return "Skill_Map"; }
	public String name(){ return "Make Maps";}
	public String displayText(){return "(Mapping)";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"MAP"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public Environmental newInstance(){	return new Skill_Map();}

	Vector roomsMappedAlready=new Vector();
	protected Item map=null;

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("You stop mapping.");
		map=null;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if((map.owner()==null)
		||(map.owner()!=mob))
			unInvoke();
		else
		if((msg.amISource(mob))
		&&(map!=null)
		&&(msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(msg.target()!=null)
		&&(msg.target() instanceof Room)
		&&(!roomsMappedAlready.contains(msg.target())))
		{
			roomsMappedAlready.addElement(msg.target());
			map.setReadableText(map.readableText()+";"+CMMap.getExtendedRoomID((Room)msg.target()));
			if(map instanceof com.planet_ink.coffee_mud.interfaces.Map)
				((com.planet_ink.coffee_mud.interfaces.Map)map).doMapArea();
		}

		super.executeMsg(myHost,msg);
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Ability A=mob.fetchEffect(ID());
		if(A!=null)
		{
			A.unInvoke();
			return true;
		}
		if(mob.charStats().getStat(CharStats.INTELLIGENCE)<5)
		{
			mob.tell("You are too stupid to actually make a map.");
			return false;
		}
		Item target=getTarget(mob,null,givenTarget,commands,Item.WORN_REQ_UNWORNONLY);
		if(target==null)return false;

		Item item=target;
		if((item==null)||((item!=null)&&(!item.isReadable())))
		{
			mob.tell("You can't map on that.");
			return false;
		}

		if(item instanceof Scroll)
		{
			mob.tell("You can't map on a scroll.");
			return false;
		}

		if(item instanceof com.planet_ink.coffee_mud.interfaces.Map)
		{
			if(!item.ID().equals("BardMap"))
			{
				mob.tell("There's no more room to add to that map.");
				return false;
			}
		}
		else
		if(item.readableText().length()>0)
		{
			mob.tell("There's no more room to map on that.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_WRITE,"<S-NAME> start(s) mapping on <T-NAMESELF>.",CMMsg.MSG_WRITE,";",CMMsg.MSG_WRITE,"<S-NAME> start(s) mapping on <T-NAMESELF>.");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(!item.ID().equals("BardMap"));
				{
					Item B=CMClass.getItem("BardMap");
					B.setContainer(item.container());
					B.setName(item.Name());
					B.setBaseEnvStats(item.baseEnvStats());
					B.setBaseValue(item.baseGoldValue()*2);
					B.setDescription(item.description());
					B.setDisplayText(item.displayText());
					B.setDroppable(item.isDroppable());
					B.setGettable(item.isGettable());
					B.setMaterial(item.material());
					B.setRawLogicalAnd(item.rawLogicalAnd());
					B.setRawProperLocationBitmap(item.rawProperLocationBitmap());
					B.setSecretIdentity(item.secretIdentity());
					B.setRemovable(item.isRemovable());
					B.setUsesRemaining(item.usesRemaining());
					item.destroy();
					mob.addInventory(B);
					item=B;
				}
				map=item;
				if(!roomsMappedAlready.contains(mob.location()))
				{
					roomsMappedAlready.addElement(mob.location());
					map.setReadableText(map.readableText()+";"+CMMap.getExtendedRoomID(mob.location()));
					if(map instanceof com.planet_ink.coffee_mud.interfaces.Map)
						((com.planet_ink.coffee_mud.interfaces.Map)map).doMapArea();
				}
				String rooms=item.readableText();
				int x=rooms.indexOf(";");
				while(x>=0)
				{
					String roomID=rooms.substring(0,x);
					Room room=CMMap.getRoom(roomID);
					if(room!=null)
						if(!roomsMappedAlready.contains(room))
							roomsMappedAlready.addElement(room);
					rooms=rooms.substring(x+1);
					x=rooms.indexOf(";");
				}
				beneficialAffect(mob,mob,0);
			}
		}
		else
			mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,"<S-NAME> attempt(s) to start mapping on <T-NAMESELF>, but mess(es) up.");
		return success;
	}

}