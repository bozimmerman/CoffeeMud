package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_SpellAdder extends Property
{
	public String ID() { return "Prop_SpellAdder"; }
	public String name(){ return "Casting spells on oneself";}
	protected int canAffectCode(){return Ability.CAN_ITEMS|Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_MOBS;}
	private Item myItem=null;
	private Environmental lastMOB=null;
	boolean processing=false;
	public Environmental newInstance(){	Prop_SpellAdder BOB=new Prop_SpellAdder(); BOB.setMiscText(text());	return BOB;}

	protected Hashtable spellH=null;
	protected Vector spellV=null;
	public Vector getMySpellsV()
	{
		if(spellV!=null) return spellV;
		spellV=Prop_SpellAdder.getMySpellsV(this);
		return spellV;
	}
	public Hashtable getMySpellsH()
	{
		if(spellH!=null) return spellH;
		spellH=Prop_SpellAdder.getMySpellsH(this);
		return spellH;
	}

	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		spellV=null;
		spellH=null;
	}

	public static Vector getMySpellsV(Ability spellHolder)
	{
		Vector theSpells=new Vector();
		String names=spellHolder.text();
		int del=names.indexOf(";");
		while(del>=0)
		{
			String thisOne=names.substring(0,del).trim();
			String parm="";
			if(thisOne.endsWith(")"))
			{
				int x=thisOne.indexOf("(");
				if(x>0)
				{
					parm=thisOne.substring(x+1,thisOne.length()-1);
					thisOne=thisOne.substring(0,x).trim();
				}
			}
				
			Ability A=(Ability)CMClass.getAbility(thisOne);
			if(A!=null)
			{
				A=(Ability)A.copyOf();
				A.setMiscText(parm);
				theSpells.addElement(A);
			}
			names=names.substring(del+1);
			del=names.indexOf(";");
		}
		String parm="";
		if(names.endsWith(")"))
		{
			int x=names.indexOf("(");
			if(x>0)
			{
				parm=names.substring(x+1,names.length()-1);
				names=names.substring(0,x).trim();
			}
		}
		Ability A=(Ability)CMClass.getAbility(names);
		if(A!=null)
		{
			A=(Ability)A.copyOf();
			A.setMiscText(parm);
			theSpells.addElement(A);
		}
		return theSpells;
	}

	public static boolean didHappen(int defaultPct, Ability A)
	{
		if(A==null) return false;
		int x=A.text().indexOf("%");
		if(x<0)
		{
			if(Dice.rollPercentage()<defaultPct)
				return true;
			else
				return false;
		}
		else
		{
			int mul=1;
			int tot=0;
			while((--x)>=0)
			{
				if(Character.isDigit(A.text().charAt(x)))
					tot+=Util.s_int(""+A.text().charAt(x))*mul;
				else
					x=-1;
				mul=mul*10;
			}
			if(Dice.rollPercentage()<tot)
				return true;
			else
				return false;
		}
	}
	public static Hashtable getMySpellsH(Ability spellHolder)
	{
		Hashtable h=new Hashtable();
		Vector V=getMySpellsV(spellHolder);
		for(int v=0;v<V.size();v++)
			h.put(((Ability)V.elementAt(v)).ID(),((Ability)V.elementAt(v)).ID());
		return h;
	}


	public MOB qualifiedMOB(Environmental target)
	{
		if((target!=null)&&(target instanceof MOB))
			return (MOB)target;

		if((target instanceof Item)&&(((Item)target).owner()!=null)&&(((Item)target).owner() instanceof MOB))
			return (MOB)((Item)target).owner();
		MOB mob=CMClass.getMOB("StdMOB");
		mob.setLocation(CMClass.getLocale("StdRoom"));
		return mob;
	}

	public void addMeIfNeccessary(Environmental target)
	{
		if(target==null) return;

		Vector V=getMySpellsV();
		for(int v=0;v<V.size();v++)
		{
			Ability A=(Ability)V.elementAt(v);
			Ability EA=target.fetchEffect(A.ID());
			if((EA==null)&&(didHappen(100,this)))
			{
				String t=A.text();
				A=(Ability)A.copyOf();
				Vector V2=new Vector();
				if(t.length()>0)
				{
					int x=t.indexOf("/");
					if(x<0)
					{ 
						V2=Util.parse(t);
						A.setMiscText("");
					}
					else
					{
						V2=Util.parse(t.substring(0,x));
						A.setMiscText(t.substring(x+1));
					}
				}
				A.invoke(qualifiedMOB(target),V2,target,true);
				EA=target.fetchEffect(A.ID());
			}
			if(EA!=null)
				EA.makeLongLasting();
		}
	}

	public void removeMyAffectsFrom(Environmental lastMOB)
	{
		Hashtable h=getMySpellsH();
		int x=0;
		while(x<lastMOB.numEffects())
		{
			Ability thisAffect=lastMOB.fetchEffect(x);
			if(thisAffect!=null)
			{
				String ID=(String)h.get(thisAffect.ID());
				if((ID!=null)&&(thisAffect.invoker()==lastMOB))
				{
					thisAffect.unInvoke();
					x=-1;
				}
			}
			x++;
		}
		lastMOB=null;
	}

	public void executeMsg(Environmental host, CMMsg msg)
	{
		if((affected instanceof Room)||(affected instanceof Area))
		{
			if((msg.targetMinor()==CMMsg.TYP_LEAVE)
			||(msg.sourceMinor()==CMMsg.TYP_RECALL))
				removeMyAffectsFrom(msg.source());
			if(msg.targetMinor()==CMMsg.TYP_ENTER)
				addMeIfNeccessary(msg.source());
		}
	}

	public void affectEnvStats(Environmental affectedMOB, EnvStats affectableStats)
	{
		if(processing) return;
		if((affected instanceof MOB)
		   ||(affected instanceof Item))
		{
			processing=true;
			if((lastMOB!=null)
			 &&(affectedMOB!=lastMOB))
				removeMyAffectsFrom(lastMOB);

			if((lastMOB==null)&&(affectedMOB!=null))
			{
				addMeIfNeccessary(affectedMOB);
				lastMOB=affectedMOB;
			}
			super.affectEnvStats(affectedMOB,affectableStats);
			processing=false;
		}
	}
}