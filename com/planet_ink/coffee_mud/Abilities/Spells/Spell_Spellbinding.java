package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.*;

public class Spell_Spellbinding extends Spell
{
	public String ID() { return "Spell_Spellbinding"; }
	public String name(){return "Spellbinding";}
	protected int overrideMana(){return 0;}
	public String displayText()
	{
		StringBuffer bindings=new StringBuffer("");
		for(int i=0;i<spellbindings.size();i++)
			bindings.append(" "+((String)spellbindings.elementAt(i,1)));
		return "(Bindings: "+bindings.toString()+")";
	}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Spell_Spellbinding();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_ALTERATION;}
	protected DVector spellbindings=new DVector(2);

	public void affectCharState(MOB affected, CharState affectableState)
	{
		super.affectCharState(affected,affectableState);
		int total=0;
		for(int i=0;i<spellbindings.size();i++)
		{
			DVector V=(DVector)spellbindings.elementAt(i,2);
			for(int x=0;x<V.size();x++)
				total+=((Integer)V.elementAt(x,2)).intValue();
		}
		if(affectableState.getMana()>=total)
			affectableState.setMana(affectableState.getMana()-total);
		else
			affectableState.setMana(0);
	}

	public String text()
	{
		if(spellbindings.size()==0)
			return super.text();
		try
		{
			ByteArrayOutputStream bytes=new ByteArrayOutputStream();
			new ObjectOutputStream(bytes).writeObject(spellbindings);
			return Util.toByteList(bytes.toByteArray());
		}
		catch(Exception e)
		{ 
			Log.errOut("Spell_Spellbinding",e);
		}
		return super.text();
	}
	
	public void setMiscText(String text)
	{
		if(text.length()==0)
			spellbindings=new DVector(2);
		else
		{
			try
			{
				ByteArrayInputStream bytes=new ByteArrayInputStream(Util.fromByteList(text));
				spellbindings=(DVector)new ObjectInputStream(bytes).readObject();
			}
			catch(Exception e)
			{ 
				Log.errOut("Spell_Spellbinding",e);
			}
		}
	}
	
	protected String getMsgFromAffect(String msg)
	{
		if(msg==null) return null;
		int start=msg.indexOf("'");
		int end=msg.lastIndexOf("'");
		if((start>0)&&(end>start))
			return msg.substring(start+1,end);
		return null;
	}

	public void executeMsg(Environmental host, CMMsg msg)
	{
		if((msg.source()==affected)
		&&(msg.sourceMinor()==CMMsg.TYP_SPEAK)
		&&(msg.sourceMessage()!=null)
		&&(msg.sourceMessage().length()>0))
		{
			String s=getMsgFromAffect(msg.sourceMessage());
			for(int v=0;v<spellbindings.size();v++)
				if(((String)spellbindings.elementAt(v,1)).equalsIgnoreCase(s))
					msg.addTrailerMsg(new FullMsg(msg.source(),msg.target(),this,CMMsg.MASK_GENERAL|CMMsg.TYP_WAND_USE,"The magic of '"+s+"' swells within you!",CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
		}
		else
		if((msg.tool()==this)
		&&(msg.source()==affected)
		&&(msg.sourceMinor()==CMMsg.TYP_WAND_USE)
		&&(msg.sourceMessage()!=null)
		&&(msg.sourceMessage().length()>0))
		{
			String s=getMsgFromAffect(msg.sourceMessage());
			for(int v=spellbindings.size()-1;v>=0;v--)
				if(((String)spellbindings.elementAt(v,1)).equalsIgnoreCase(s))
				{
					DVector V2=(DVector)spellbindings.elementAt(v,2);
					for(int v2=0;v2<V2.size();v2++)
					{
						Ability A=msg.source().fetchAbility((String)V2.elementAt(v2,1));
						int curMana=msg.source().curState().getMana();
						msg.source().curState().setMana(1000);
						if(msg.target()!=null)
							A.invoke(msg.source(),Util.parse(msg.target().Name()),null,false);
						else
							A.invoke(msg.source(),new Vector(),null,false);
						msg.source().curState().setMana(curMana);
					}
					if(canBeUninvoked())
						spellbindings.removeElementAt(v);
				}
		}
		if((spellbindings.size()==0)&&(canBeUninvoked()))
			unInvoke();
		super.executeMsg(host,msg);
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		MOB target = mob;
		Spell_Spellbinding priorBinding=(Spell_Spellbinding)target.fetchEffect(ID());
		if(commands.size()<2)
		{
			mob.tell("You must specify your trigger word, followed by a list of spells, seperated by spaces.");
			return false;
		}
		String key=(String)commands.elementAt(0);
		commands.removeElementAt(0);
		String combined=Util.combine(commands,0);
		DVector V=new DVector(2);
		Ability A=mob.fetchAbility(combined);
		if(A!=null)
		{
			if(((A.classificationCode()&ALL_CODES)!=SPELL)
			||(A.ID().equals(ID()))
			||(A.usageCost(mob)[Ability.USAGE_MANAINDEX]>50))
			{
				mob.tell("You can't bind '"+A.ID()+"'.");
				return false;
			}
			else
				V.addElement(A.ID(),new Integer(50+A.usageCost(mob)[Ability.USAGE_MANAINDEX]));
		}
		else
		for(int v=0;v<commands.size();v++)
		{
			A=mob.fetchAbility((String)commands.elementAt(v));
			if((A==null)
			||(A.ID().equals(ID()))
			||((A.classificationCode()&ALL_CODES)!=SPELL)
			||(A.usageCost(mob)[Ability.USAGE_MANAINDEX]>50))
			{
				mob.tell("You can't bind '"+((String)commands.elementAt(v))+"'.");
				return false;
			}
			else
				V.addElement(A.ID(),new Integer(50+A.usageCost(mob)[Ability.USAGE_MANAINDEX]));
		}
		
		int totalcost=0;
		for(int v=0;v<V.size();v++)
			totalcost+=((Integer)V.elementAt(v,2)).intValue();
		int curMana=mob.curState().getMana();
		if(curMana<totalcost)
		{
			mob.tell("You need "+totalcost+" mana to bind those spells.");
			return false;
		}
		DVector thePriorKey=null;
		if(priorBinding!=null)
			for(int x=0;x<priorBinding.spellbindings.size();x++)
				if(((String)priorBinding.spellbindings.elementAt(x,1)).equalsIgnoreCase(key))
				{	thePriorKey=(DVector)priorBinding.spellbindings.elementAt(x,2);}
		
		for(int v=0;v<V.size();v++)
			for(int v2=0;v2<V.size();v2++)
				if((v!=v2)&&(((String)V.elementAt(v,1)).equals((String)V.elementAt(v2,1))))
				{
					mob.tell("The same spell can not be bound to the same trigger more than once.");
					return false;
				}
		if(thePriorKey!=null)
			for(int v=0;v<V.size();v++)
				if(thePriorKey.contains(V.elementAt(v,1)))
				{
					mob.tell("The same spell can not be bound to the same trigger more than once.");
					return false;
				}
		
		boolean success=profficiencyCheck(mob,0,auto);
		
		if(mob.curState().getMana()>(curMana-totalcost))
			mob.curState().setMana(curMana-totalcost);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			FullMsg msg = new FullMsg(mob, null, this, affectType(auto),(auto?"":"^S<S-NAME> shout(s) the magic of spellbinding!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(priorBinding==null)
				{
					beneficialAffect(mob,target,0);
					priorBinding=(Spell_Spellbinding)target.fetchEffect(ID());
					if(priorBinding==null) return false;
					priorBinding.makeLongLasting();
				}
				if(thePriorKey==null)
					priorBinding.spellbindings.addElement(key,V);
				else
				for(int v=0;v<V.size();v++)
					thePriorKey.addElement(V.elementAt(v,1),V.elementAt(v,2));
				target.recoverMaxState();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> whisper(s) about spellbinding, and the magic fizzles.");

		// return whether it worked
		return success;
	}
}
