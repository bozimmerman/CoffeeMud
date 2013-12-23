package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2013 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
@SuppressWarnings({"unchecked","rawtypes"})
public class StdWand extends StdItem implements Wand
{
	public String ID(){    return "StdWand";}
	protected String secretWord=CMProps.getAnyListFileValue(CMProps.ListFile.MAGIC_WORDS);

	public StdWand()
	{
		super();

		setName("a crooked stick");
		basePhyStats.setWeight(1);
		setDisplayText("a small crooked stick is here.");
		setDescription("Looks like an broken piece of a tree.");
		secretIdentity="";
		baseGoldValue=200;
		material=RawMaterial.RESOURCE_OAK;
		basePhyStats().setDisposition(basePhyStats().disposition()|PhyStats.IS_BONUS);
		setUsesRemaining(0);
		recoverPhyStats();
	}

	public int maxUses(){return Integer.MAX_VALUE;}
	public void setMaxUses(int newMaxUses){}
	
	public static boolean useTheWand(Ability A, MOB mob, int level)
	{
		int manaRequired=5;
		int q=CMLib.ableMapper().qualifyingLevel(mob,A);
		if(q>0)
		{
			if(q<CMLib.ableMapper().qualifyingClassLevel(mob,A))
				manaRequired=0;
			else
				manaRequired=5;
		}
		else
			manaRequired=25;
		manaRequired-=(2*level);
		if(manaRequired<5) manaRequired=5;
		if(manaRequired>mob.curState().getMana())
		{
			mob.tell("You don't have enough mana.");
			return false;
		}
		mob.curState().adjMana(-manaRequired,mob.maxState());
		return true;
	}
	
	public int value()
	{
		if(usesRemaining()<=0)
			return 0;
		return super.value();
	}
	
	public static String getWandWord(String from)
	{
		int hash=from.hashCode();
		if(hash<0) hash=hash*-1;
		return CMProps.getListFileValueByHash(CMProps.ListFile.MAGIC_WORDS,hash);
	}

	public void setSpell(Ability theSpell)
	{
		miscText="";
		if(theSpell!=null)
			miscText=theSpell.ID();
		secretWord=StdWand.getWandWord(miscText);
	}
	
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		secretWord=StdWand.getWandWord(newText);
	}

	public Ability getSpell()
	{
		return CMClass.getAbility(text());
	}

	public String secretIdentity()
	{
		String id=super.secretIdentity();
		Ability A=getSpell();
		if(A!=null)
			id="'A wand of "+A.name()+"' Charges: "+usesRemaining()+"\n\r"+id;
		return id+"\n\rSay the magic word :`"+secretWord+"` to the target.";
	}

	public boolean checkWave(MOB mob, String message)
	{
		return StdWand.checkWave(mob, message, this);
	}
	
	public void waveIfAble(MOB mob, Physical afftarget, String message)
	{
		StdWand.waveIfAble(mob, afftarget, message, this);
	}

	public static boolean checkWave(MOB mob, String message, Wand me)
	{
		return (mob.isMine(me)) && (message!=null) && (!me.amWearingAt(Wearable.IN_INVENTORY)) && (message.toUpperCase().indexOf(me.magicWord().toUpperCase()) >= 0);
	}
	
	public static void waveIfAble(MOB mob, Physical afftarget, String message, Wand me)
	{
		if((mob.isMine(me)) &&(message!=null) &&(!me.amWearingAt(Wearable.IN_INVENTORY)))
		{
			Physical target=null;
			if(mob.location()!=null)
				target=afftarget;
			int x=message.toUpperCase().indexOf(me.magicWord().toUpperCase());
			if(x>=0)
			{
				message=message.substring(x+me.magicWord().length());
				int y=message.indexOf('\'');
				if(y>=0) message=message.substring(0,y);
				message=message.trim();
				Ability wandUse=mob.fetchAbility("Skill_WandUse");
				if((wandUse==null)||(!wandUse.proficiencyCheck(null,0,false)))
					mob.tell(me.name()+" glows faintly for a moment, then fades.");
				else
				{
					Ability A=me.getSpell();
					if(A==null)
						mob.tell("Something seems wrong with "+me.name()+".");
					else
					if(me.usesRemaining()<=0)
						mob.tell(me.name()+" seems spent.");
					else
					{
						wandUse.setInvoker(mob);
						A=(Ability)A.newInstance();
						if(useTheWand(A,mob,wandUse.abilityCode()))
						{
							Vector V=new Vector();
							if(target!=null)
								V.addElement(target.name());
							V.addAll(CMParms.parse(message));
							mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,me.name()+" glows brightly.");
							me.setUsesRemaining(me.usesRemaining()-1);
							int level=me.phyStats().level();
							int lowest=CMLib.ableMapper().lowestQualifyingLevel(A.ID());
							if(level<lowest)
								level=lowest;
							A.invoke(mob, V, target, true, level);
							wandUse.helpProficiency(mob, 0);
							return;
						}
					}
				}
			}
		}
	}
	
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		MOB mob=msg.source();

		switch(msg.targetMinor())
		{
		case CMMsg.TYP_WAND_USE:
			if(msg.amITarget(this)&&((msg.tool()==null)||(msg.tool() instanceof Physical)))
				waveIfAble(mob,(Physical)msg.tool(),msg.targetMessage());
			break;
		case CMMsg.TYP_SPEAK:
			if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
			&&(!amWearingAt(Wearable.IN_INVENTORY)))
			{
				boolean alreadyWanding=false;
				final List<CMMsg> trailers =msg.trailerMsgs();
				if(trailers!=null)
					for(CMMsg msg2 : trailers)
						if(msg2.targetMinor()==CMMsg.TYP_WAND_USE)
							alreadyWanding=true;
				final String said=CMStrings.getSayFromMessage(msg.sourceMessage());
				if((!alreadyWanding)&&(checkWave(mob,said)))
					msg.addTrailerMsg(CMClass.getMsg(msg.source(),this,msg.target(),CMMsg.NO_EFFECT,null,CMMsg.MASK_ALWAYS|CMMsg.TYP_WAND_USE,said,CMMsg.NO_EFFECT,null));
			}
			break;
		default:
			break;
		}
		super.executeMsg(myHost,msg);
	}

	public String magicWord(){return secretWord;}


	protected static String[] CODES={"CLASS","LEVEL","ABILITY","TEXT"};
	public String getStat(String code){
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return ""+basePhyStats().ability();
		case 2: return ""+basePhyStats().level();
		case 3: return text();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: return;
		case 1: basePhyStats().setLevel(CMath.s_parseIntExpression(val)); break;
		case 2: basePhyStats().setAbility(CMath.s_parseIntExpression(val)); break;
		case 3: setMiscText(val); break;
		}
	}
	public String[] getStatCodes(){return CODES;}
	protected int getCodeNum(String code){
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdWand)) return false;
		String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		return true;
	}
}
