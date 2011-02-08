package com.planet_ink.coffee_mud.Abilities.Properties;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;



import java.util.*;

/* 
   Copyright 2000-2011 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Prop_HaveAdjuster extends Property
{
	public String ID() { return "Prop_HaveAdjuster"; }
	public String name(){ return "Adjustments to stats when owned";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	public boolean bubbleAffect(){return true;}
    protected Object[] charStatsChanges=null;
    protected Object[] charStateChanges=null;
    protected Object[] phyStatsChanges=null;
    protected MaskingLibrary.CompiledZapperMask mask=null;

    
    public boolean addIfPlussed(String newText, String parm, int parmCode, Vector addTo)
    {
        int val=CMParms.getParmPlus(newText,parm);
        if(val==0) return false;
        addTo.addElement(Integer.valueOf(parmCode));
        addTo.addElement(Integer.valueOf(val));
        return true;
    }

    public Object[] makeObjectArray(Vector V)
    {
        if(V==null) return null;
        if(V.size()==0) return null;
        Object[] O=new Object[V.size()];
        for(int i=0;i<V.size();i++)
            O[i]=V.elementAt(i);
        return O;
    }   
    
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
        this.charStateChanges=null;
        this.phyStatsChanges=null;
        this.charStatsChanges=null;
        this.mask=MaskingLibrary.CompiledZapperMask.EMPTY();
        newText=buildMask(newText,mask);
        Vector phyStatsV=new Vector();
        addIfPlussed(newText,"abi",PhyStats.STAT_ABILITY,phyStatsV);
        addIfPlussed(newText,"arm",PhyStats.STAT_ARMOR,phyStatsV);
        addIfPlussed(newText,"att",PhyStats.STAT_ATTACK,phyStatsV);
        addIfPlussed(newText,"dam",PhyStats.STAT_DAMAGE,phyStatsV);
        addIfPlussed(newText,"dis",PhyStats.STAT_DISPOSITION,phyStatsV);
        addIfPlussed(newText,"lev",PhyStats.STAT_LEVEL,phyStatsV);
        addIfPlussed(newText,"rej",PhyStats.STAT_REJUV,phyStatsV);
        addIfPlussed(newText,"sen",PhyStats.STAT_SENSES,phyStatsV);
        double dval=CMParms.getParmDoublePlus(newText,"spe");
        if(dval!=0)
        {
            phyStatsV.addElement(Integer.valueOf(PhyStats.NUM_STATS));
            phyStatsV.addElement(Double.valueOf(dval));
        }
        addIfPlussed(newText,"wei",PhyStats.STAT_WEIGHT,phyStatsV);
        addIfPlussed(newText,"hei",PhyStats.STAT_HEIGHT,phyStatsV);

        Vector charStatsV=new Vector();
        String val=CMParms.getParmStr(newText,"gen","").toUpperCase();
        if((val.length()>0)&&((val.charAt(0)=='M')||(val.charAt(0)=='F')||(val.charAt(0)=='N')))
        {
            charStatsV.addElement(new Character('G'));
            charStatsV.addElement(new Character(val.charAt(0)));
        }
        val=CMParms.getParmStr(newText,"cla","").toUpperCase();
        if((val.length()>0)&&(CMClass.findCharClass(val)!=null)&&(!val.equalsIgnoreCase("Archon")))
        {
            charStatsV.addElement(new Character('C'));
            charStatsV.addElement(CMClass.findCharClass(val));
        }
        val=CMParms.getParmStr(newText,"rac","").toUpperCase();
        if((val.length()>0)&&(CMClass.getRace(val)!=null))
        {
            charStatsV.addElement(new Character('R'));
            charStatsV.addElement(CMClass.getRace(val));
        }
		for(int i : CharStats.CODES.BASE())
		{
			String name = CMStrings.limit(CharStats.CODES.NAME(i).toLowerCase(),3);
	        addIfPlussed(newText,name,i,charStatsV);
	        addIfPlussed(newText,"max"+name,CharStats.CODES.toMAXBASE(i),charStatsV);
		}
		int[] CMMSGMAP=CharStats.CODES.CMMSGMAP();
		for(int c : CharStats.CODES.SAVING_THROWS())
            if(CMMSGMAP[c]!=-1)
                addIfPlussed(newText,"save"+CMStrings.limit(CharStats.CODES.NAME(c).toLowerCase(),3),c,charStatsV);

        Vector charStateV=new Vector();
        addIfPlussed(newText,"hit",CharState.STAT_HITPOINTS,charStateV);
        addIfPlussed(newText,"hun",CharState.STAT_HUNGER,charStateV);
        addIfPlussed(newText,"man",CharState.STAT_MANA,charStateV);
        addIfPlussed(newText,"mov",CharState.STAT_MOVE,charStateV);
        addIfPlussed(newText,"thi",CharState.STAT_THIRST,charStateV);
        
        this.charStateChanges=makeObjectArray(charStateV);
        this.phyStatsChanges=makeObjectArray(phyStatsV);
        this.charStatsChanges=makeObjectArray(charStatsV);
	}

	public void phyStuff(Object[] changes, PhyStats phyStats)
	{
        if(changes==null) return;
        for(int c=0;c<changes.length;c+=2)
        switch(((Integer)changes[c]).intValue())
        {
        case PhyStats.STAT_ABILITY: phyStats.setAbility(phyStats.ability()+((Integer)changes[c+1]).intValue()); break;
        case PhyStats.STAT_ARMOR: phyStats.setArmor(phyStats.armor()+((Integer)changes[c+1]).intValue()); break;
        case PhyStats.STAT_ATTACK: phyStats.setAttackAdjustment(phyStats.attackAdjustment()+((Integer)changes[c+1]).intValue()); break;
        case PhyStats.STAT_DAMAGE: phyStats.setDamage(phyStats.damage()+((Integer)changes[c+1]).intValue()); break;
        case PhyStats.STAT_DISPOSITION: phyStats.setDisposition(phyStats.disposition()+((Integer)changes[c+1]).intValue()); break;
        case PhyStats.STAT_LEVEL: phyStats.setLevel(phyStats.level()+((Integer)changes[c+1]).intValue()); break;
        case PhyStats.STAT_REJUV: phyStats.setRejuv(phyStats.rejuv()+((Integer)changes[c+1]).intValue()); break;
        case PhyStats.STAT_SENSES: phyStats.setSensesMask(phyStats.sensesMask()+((Integer)changes[c+1]).intValue()); break;
        case PhyStats.STAT_WEIGHT: phyStats.setWeight(phyStats.weight()+((Integer)changes[c+1]).intValue()); break;
        case PhyStats.STAT_HEIGHT: phyStats.setHeight(phyStats.height()+((Integer)changes[c+1]).intValue()); break;
        case PhyStats.NUM_STATS: phyStats.setSpeed(phyStats.speed()+((Double)changes[c+1]).doubleValue()); break;
        }
	}

    public boolean canApply(MOB mob)
    {
        if((affected!=null)
        &&(affected instanceof Item)
        &&(!((Item)affected).amDestroyed())
        &&((mask==null)||(CMLib.masking().maskCheck(mask,mob,true))))
            return true;
        return false;
    }
    
    public boolean canApply(Environmental E)
    {
        if(E instanceof MOB)
            return canApply((MOB)E);
        return false;
    }
    
    protected void ensureStarted()
	{
		if(mask==null)
			setMiscText(text());
	}

	public void affectPhyStats(Physical host, PhyStats affectableStats)
	{
		ensureStarted();
        if(canApply(host)) phyStuff(phyStatsChanges,affectableStats);
		super.affectPhyStats(host,affectableStats);
	}

	public void adjCharStats(Object[] changes, CharStats charStats)
	{
        if(changes==null) return;
        for(int i=0;i<changes.length;i+=2)
        {
            if(changes[i] instanceof Integer)
                charStats.setStat(((Integer)changes[i]).intValue(),charStats.getStat(((Integer)changes[i]).intValue())+((Integer)changes[i+1]).intValue());
            else
            if(changes[i] instanceof Character)
            switch(((Character)changes[i]).charValue())
            {
            case 'G': charStats.setStat(CharStats.STAT_GENDER,(int)((Character)changes[i+1]).charValue()); break;
            case 'C': charStats.setCurrentClass((CharClass)changes[i+1]); break;
            case 'R': charStats.setMyRace((Race)changes[i+1]); break;
            }
        }
	}

	public void adjCharState(Object[] changes, CharState charState)
	{
        if(changes==null) return;
        for(int c=0;c<changes.length;c+=2)
        switch(((Integer)changes[c]).intValue())
        {
        case CharState.STAT_HITPOINTS: charState.setHitPoints(charState.getHitPoints()+((Integer)changes[c+1]).intValue()); break;
        case CharState.STAT_HUNGER: charState.setHunger(charState.getHunger()+((Integer)changes[c+1]).intValue()); break;
        case CharState.STAT_THIRST: charState.setThirst(charState.getThirst()+((Integer)changes[c+1]).intValue()); break;
        case CharState.STAT_MANA: charState.setMana(charState.getMana()+((Integer)changes[c+1]).intValue()); break;
        case CharState.STAT_MOVE: charState.setMovement(charState.getMovement()+((Integer)changes[c+1]).intValue()); break;
        }
	}

	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		ensureStarted();
        if(canApply(affectedMOB)) adjCharStats(charStatsChanges,affectedStats);
		super.affectCharStats(affectedMOB,affectedStats);
	}
	public void affectCharState(MOB affectedMOB, CharState affectedState)
	{
		ensureStarted();
        if(canApply(affectedMOB)) adjCharState(charStateChanges,affectedState);
		super.affectCharState(affectedMOB,affectedState);
	}

	public String fixAccoutingsWithMask(String id)
	{
        String[] strs=separateMask(id);
        id=strs[0];
		int x=id.toUpperCase().indexOf("ARM");
		for(StringBuffer ID=new StringBuffer(id);((x>0)&&(x<id.length()));x++)
			if(id.charAt(x)=='-')
			{
				ID.setCharAt(x,'+');
				id=ID.toString();
				break;
			}
			else
			if(id.charAt(x)=='+')
			{
				ID.setCharAt(x,'-');
				id=ID.toString();
				break;
			}
			else
			if(Character.isDigit(id.charAt(x)))
				break;
		x=id.toUpperCase().indexOf("DIS");
		if(x>=0)
		{
			long val=CMParms.getParmPlus(id,"dis");
			int y=id.indexOf(""+val,x);
			if((val!=0)&&(y>x))
			{
				StringBuffer middle=new StringBuffer("");
				for(int num=0;num<PhyStats.IS_VERBS.length;num++)
					if(CMath.bset(val,CMath.pow(2,num)))
						middle.append(PhyStats.IS_VERBS[num]+" ");
				id=id.substring(0,x)+middle.toString().trim()+id.substring(y+((""+val).length()));
			}
		}
		x=id.toUpperCase().indexOf("SEN");
		if(x>=0)
		{
			long val=CMParms.getParmPlus(id,"sen");
			int y=id.indexOf(""+val,x);
			if((val!=0)&&(y>x))
			{
				StringBuffer middle=new StringBuffer("");
				for(int num=0;num<PhyStats.CAN_SEE_VERBS.length;num++)
					if(CMath.bset(val,CMath.pow(2,num)))
						middle.append(PhyStats.CAN_SEE_VERBS[num]+" ");
				id=id.substring(0,x)+middle.toString().trim()+id.substring(y+((""+val).length()));
			}
		}
        if(strs[1].length()>0)
            id+="  Restrictions: "+CMLib.masking().maskDesc(strs[1]);
		return id;
	}

	public String accountForYourself()
	{
		return fixAccoutingsWithMask("Affects the owner: "+text());
	}
}
