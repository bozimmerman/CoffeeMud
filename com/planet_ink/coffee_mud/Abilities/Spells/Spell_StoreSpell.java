package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_StoreSpell extends Spell
{
	public String ID() { return "Spell_StoreSpell"; }
	public String name(){return "Store Spell";}
	protected int canTargetCode(){return CAN_ITEMS;}
	public Environmental newInstance(){	return new Spell_StoreSpell();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;}
	protected int overrideMana(){return overridemana;}
	public String spellName="";
	private int overridemana=-1;

	public void waveIfAble(MOB mob,
						   Environmental afftarget,
						   String message,
						   Item me)
	{
		if((mob.isMine(me))&&(!me.amWearingAt(Item.INVENTORY)))
		{
			Environmental target=null;
			if((mob.location()!=null))
				target=afftarget;
			String name=Util.removeColors(me.name().toUpperCase());
			if(name.startsWith("A ")) name=name.substring(2).trim();
			if(name.startsWith("AN ")) name=name.substring(3).trim();
			if(name.startsWith("THE ")) name=name.substring(4).trim();
			if(name.startsWith("SOME ")) name=name.substring(5).trim();
			int x=message.toUpperCase().indexOf(name);
			if(x>=0)
			{
				message=message.substring(x+name.length());
				int y=message.indexOf("'");
				if(y>=0) message=message.substring(0,y);
				message=message.trim();
				x=text().indexOf("/");
				int charges=0;
				Ability A=null;
				if(x>0){
					charges=Util.s_int(text().substring(x+1));
					A=CMClass.getAbility(text().substring(0,x));
				}
				if(A==null)
					mob.tell("Something seems wrong with "+me.name()+".");
				else
				if(charges<=0)
				{
					mob.tell(me.name()+" seems spent.");
					me.delAffect(this);
				}
				else
				{
					setMiscText(A.ID()+"/"+(charges-1));
					A=(Ability)A.newInstance();
					Vector V=new Vector();
					if(target!=null)
						V.addElement(target.name());
					V.addElement(message);
					mob.location().show(mob,null,Affect.MSG_OK_VISUAL,me.name()+" glows brightly.");
					A.invoke(mob, V, target, true);
				}
			}
		}
	}

	public void affect(Environmental myHost, Affect affect)
	{
		MOB mob=affect.source();

		switch(affect.targetMinor())
		{
		case Affect.TYP_WAND_USE:
			if((affect.amITarget(affected))&&(affected instanceof Item))
				waveIfAble(mob,affect.tool(),affect.targetMessage(),(Item)affected);
			break;
		case Affect.TYP_SPEAK:
			if(affect.sourceMinor()==Affect.TYP_SPEAK)
				affect.addTrailerMsg(new FullMsg(affect.source(),affected,affect.target(),affect.NO_EFFECT,null,Affect.MASK_GENERAL|Affect.TYP_WAND_USE,affect.targetMessage(),affect.NO_EFFECT,null));
			break;
		default:
			break;
		}
		super.affect(myHost,affect);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<2)
		{
			mob.tell("Store which spell onto what?");
			return false;
		}
		Environmental target=mob.location().fetchFromMOBRoomFavorsItems(mob,null,(String)commands.lastElement(),Item.WORN_REQ_UNWORNONLY);
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("You don't see '"+((String)commands.lastElement())+"' here.");
			return false;
		}
		if(!(target instanceof Item))
		{
			mob.tell("You can't enchant '"+target.name()+"'.");
			return false;
		}

		Item item=(Item)target;

		commands.removeElementAt(commands.size()-1);

		String spellName=Util.combine(commands,0).trim();
		Spell wandThis=null;
		for(int a=0;a<mob.numAbilities();a++)
		{
			Ability A=mob.fetchAbility(a);
			if((A!=null)
			&&(A instanceof Spell)
			&&(A.isBorrowed(mob)||(CMAble.qualifiesByLevel(mob,A)))
			&&(A.name().toUpperCase().startsWith(spellName.toUpperCase()))
			&&(!A.ID().equals(this.ID())))
				wandThis=(Spell)A;
		}
		if(wandThis==null)
		{
			mob.tell("You don't know how to enchant anything with '"+spellName+"'.");
			return false;
		}
		Ability A=item.fetchAffect(ID());
		if((A!=null)&&(A.text().length()>0)&&(!A.text().startsWith(wandThis.ID()+"/")))
		{
			mob.tell("'"+item.name()+"' already has a different spell stored in it.");
			return false;
		}
		else
		if(A==null)
		{
			A=(Ability)copyOf();
			A.setMiscText(wandThis.ID()+"/0");
		}
		int charges=0;
		int x=A.text().indexOf("/");
		if(x>=0) charges=Util.s_int(A.text().substring(x+1));
		overridemana=-1;
		int mana=usageCost(mob)[0]+wandThis.usageCost(mob)[0];
		if(mana>mob.maxState().getMana())
			mana=mob.maxState().getMana();
		overridemana=mana;

		// lose all the mana!
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		overridemana=-1;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			setMiscText(wandThis.ID());
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),"^S<S-NAME> move(s) <S-HIS-HER> fingers around <T-NAMESELF>, incanting softly.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				if(target.fetchAffect(ID())==null)
				{
					A.setInvoker(mob);
					target.addNonUninvokableAffect(A);
				}
				A.setMiscText(wandThis.ID()+"/"+(charges+1));
				mob.location().show(mob,target,null,Affect.MSG_OK_VISUAL,"<T-NAME> glow(s) softly.");
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> move(s) <S-HIS-HER> fingers around <T-NAMESELF>, incanting softly, and looking very frustrated.");


		// return whether it worked
		return success;
	}
}
