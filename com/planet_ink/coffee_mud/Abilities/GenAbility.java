package com.planet_ink.coffee_mud.Abilities;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;

/*
   Copyright 2000-2010 Bo Zimmerman

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
public class GenAbility extends StdAbility
{
    // data should be stored in a common instance object .. something common to all genability of same id,
    // but diff to others.n  I'm thinking like a DVector, and just have
    private String ID="GenAbility";
    public String ID() { return ID; }
    private static final Hashtable vars=new Hashtable();
    private static final int V_NAME=0;//S
    private static final int V_DISP=1;//S
    private static final int V_TRIG=2;//S[]
    private static final int V_MAXR=3;//I
    private static final int V_MINR=4;//I
    private static final int V_AUTO=5;//B
    private static final int V_FLAG=6;//I
    private static final int V_CLAS=7;//I
    private static final int V_OMAN=8;//I
    private static final int V_USAG=9;//I
    private static final int V_CAFF=10;//I
    private static final int V_CTAR=11;//I
    private static final int V_QUAL=12;//I
    private static final int V_HERE=13;//A
    private static final int V_CMSK=14;//S
    private static final int V_SCRP=15;//S
    private static final int V_TMSK=16;//S
    private static final int V_FZZL=17;//S
    private static final int V_ACST=18;//S
    private static final int V_CAST=19;//S
    private static final int V_PCST=20;//S
    private static final int V_ATT2=21;//I
    private static final int V_PAFF=22;//S
    private static final int V_PABL=23;//S
    private static final int V_PDMG=24;//S
    private static final int V_HELP=25;//S
    private static final int V_TKBC=26;//I
    private static final int NUM_VS=27;//S
    private static final Object[] makeEmpty()
    {
        Object[] O=new Object[NUM_VS];
        O[V_NAME]="an ability";
        O[V_DISP]="(An Affect)";
        O[V_TRIG]=new String[]{"CAST","CA","C"};
        O[V_MAXR]=Integer.valueOf(0);
        O[V_MINR]=Integer.valueOf(0);
        O[V_AUTO]=Boolean.FALSE;
        O[V_FLAG]=Integer.valueOf(0);
        O[V_CLAS]=Integer.valueOf(Ability.ACODE_SPELL|Ability.DOMAIN_ABJURATION);
        O[V_OMAN]=Integer.valueOf(-1);
        O[V_USAG]=Integer.valueOf(Ability.USAGE_MANA);
        O[V_CAFF]=Integer.valueOf(Ability.CAN_MOBS);
        O[V_CTAR]=Integer.valueOf(Ability.CAN_MOBS);
        O[V_QUAL]=Integer.valueOf(Ability.QUALITY_BENEFICIAL_OTHERS);
        O[V_HERE]=CMClass.getAbility("Prop_HereAdjuster");
        O[V_SCRP]="";
        O[V_CMSK]="";
        O[V_TMSK]="";
        O[V_FZZL]="<S-NAME> attempts to use this ability against <T-NAME>, and fails";
        O[V_ACST]="An amazing thing happens to <T-NAME>!";
        O[V_CAST]="<S-NAME> uses an ability against <T-NAME>";
        O[V_PCST]="<T-NAME> is <DAMAGE> by an ability from <S-NAME>!";
        O[V_ATT2]=Integer.valueOf(0);
        O[V_PAFF]="";
        O[V_PABL]="";
        O[V_PDMG]="1";
        O[V_HELP]="<ABILITY>This ability is not yet documented.";
        O[V_TKBC]=Integer.valueOf(0);
        return O;
    }
    private static final Object V(String ID, int varNum)
    {
        if(vars.containsKey(ID)) return ((Object[])vars.get(ID))[varNum];
        Object[] O=makeEmpty();
        vars.put(ID,O);
        return O[varNum];
    }
    private static final void SV(String ID,int varNum,Object O)
    {
        if(vars.containsKey(ID))
        	((Object[])vars.get(ID))[varNum]=O;
        else
        {
	        Object[] O2=makeEmpty();
	        vars.put(ID,O2);
	        O2[varNum]=O;
        }
    }
    private ScriptingEngine scriptObj=null;
    private long scriptParmHash=0;
    public ScriptingEngine getScripter(){
        if(((String)V(ID,V_SCRP)).hashCode()!=scriptParmHash)
        {
            String parm=(String)V(ID,V_SCRP);
            scriptParmHash=parm.hashCode();
            if(parm.trim().length()==0)
                scriptObj=null;
            else
            {
                scriptObj=(ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
                if(scriptObj!=null)
                	scriptObj.setScript(parm);
                else
                	scriptParmHash=-1;
            }
        }
        return scriptObj;
    }


    public String Name(){return name();}
    public String name(){ return (String)V(ID,V_NAME);}
    public String description(){return "&";}
    public String displayText(){return (String)V(ID,V_DISP);}
    public String[] triggerStrings(){return (String[])V(ID,V_TRIG);}
    public int maxRange(){return adjustedMaxInvokerRange(((Integer)V(ID,V_MAXR)).intValue());}
    public int minRange(){return ((Integer)V(ID,V_MINR)).intValue();}
    public boolean isAutoInvoked(){return ((Boolean)V(ID,V_AUTO)).booleanValue();}
    public long flags(){return ((Integer)V(ID,V_FLAG)).intValue();}
    public int usageType(){return ((Integer)V(ID,V_USAG)).intValue();}
    protected int overrideMana(){return ((Integer)V(ID,V_OMAN)).intValue();} //-1=normal, Integer.MAX_VALUE=all, Integer.MAX_VALUE-100
    public int classificationCode(){ return ((Integer)V(ID,V_CLAS)).intValue(); }
    protected int canAffectCode(){return ((Integer)V(ID,V_CAFF)).intValue(); }
    protected int canTargetCode(){return ((Integer)V(ID,V_CTAR)).intValue(); }
    public int abstractQuality(){return ((Integer)V(ID,V_QUAL)).intValue();}
    protected long timeToNextCast = 0;
	public int ticksBetweenCasts() { return ((Integer)V(ID,V_TKBC)).intValue();}
	public long getTimeOfNextCast(){ return timeToNextCast; }
	public void setTimeOfNextCast(long absoluteTime) { timeToNextCast=absoluteTime;}


    public CMObject newInstance()
    {
        try
        {
            GenAbility A=(GenAbility)this.getClass().newInstance();
            A.ID=ID;
            getScripter();
            A.scriptParmHash=scriptParmHash;
            if(scriptObj!=null){
                A.scriptObj=(ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
                A.scriptObj.setScript(scriptObj.getScript());
            }
            else
                A.scriptObj=null;
            return A;
        }
        catch(Exception e)
        {
            Log.errOut(ID(),e);
        }
        return new GenAbility();
    }

    protected void cloneFix(Ability E)
    {
        if(E instanceof GenAbility)
        {
            GenAbility A=(GenAbility)E;
            A.scriptParmHash=scriptParmHash;
            if(A.scriptObj!=null){
                scriptObj=(ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
                scriptObj.setScript(A.scriptObj.getScript());
            }
            else
                scriptObj=null;
        }
    }

    public boolean isGeneric(){return true;}

    public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
    {
    	if((!auto)
    	&&(((String)V(ID,V_CMSK)).length()>0)
    	&&(!CMLib.masking().maskCheck((String)V(ID,V_CMSK), mob,true)))
    	{
    		mob.tell("You do not meet the requirements: "+CMLib.masking().maskDesc((String)V(ID,V_CMSK)));
    		return false;
    	}
        // dont forget to allow super. calls to Spell.invoke, Chant.invoke, etc.. based on classification?
    	Environmental target=givenTarget;
    	if((this.classificationCode()==Ability.QUALITY_BENEFICIAL_SELF)
    	||(this.classificationCode()==Ability.QUALITY_OK_SELF))
    	{
    		target=mob;
    		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
    			target=(MOB)givenTarget;
    		if(target.fetchEffect(this.ID())!=null)
    		{
    			mob.tell((MOB)target,null,null,"<S-NAME> <S-IS-ARE> already affected by "+name()+".");
    			return false;
    		}
    	}
    	else
    	{
	    	switch(canTargetCode())
	    	{
	    	case Ability.CAN_MOBS:
	    		target=super.getTarget(mob, commands, givenTarget);
	    		if(target==null) return false;
	    		break;
	    	case Ability.CAN_ITEMS:
	    		target=super.getTarget(mob, mob.location(), givenTarget, commands, Wearable.FILTER_ANY);
	    		if(target==null) return false;
	    		break;
	    	case Ability.CAN_ROOMS:
	    		target=mob;
	    		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof Room))
	    			target=(Room)givenTarget;
	    		if(target.fetchEffect(this.ID())!=null)
	    		{
	    			mob.tell("This place is already affected by "+name()+".");
	    			return false;
	    		}
	    		break;
	    	case Ability.CAN_EXITS:
	    	{
	    		String whatToOpen=CMParms.combine(commands,0);
	    		Environmental openThis=null;
	    		int dirCode=Directions.getGoodDirectionCode(whatToOpen);
	    		if(dirCode>=0)
	    			openThis=mob.location().getExitInDir(dirCode);
	    		if(openThis==null)
	    			openThis=mob.location().fetchFromRoomFavorItems(null, whatToOpen, Wearable.FILTER_ANY);
	    		if((openThis==null)||(!(openThis instanceof Exit))) return false;
	    		break;
	    	}
	    	case 0:
	    		break;
	    	default:
	    		target=super.getAnyTarget(mob,commands, givenTarget, Wearable.FILTER_ANY);
	    		if(target==null) return false;
	    		break;
	    	}
    	}
    	if((!auto)
    	&&(target!=null)
    	&&(((String)V(ID,V_TMSK)).length()>0)
    	&&(!CMLib.masking().maskCheck((String)V(ID,V_TMSK), target,true)))
    	{
    		mob.tell("The target is invalid: "+CMLib.masking().maskDesc((String)V(ID,V_TMSK)));
    		return false;
    	}

    	int armorCheck=0;
    	switch(classificationCode()&Ability.ALL_ACODES)
    	{
    	case Ability.ACODE_CHANT: armorCheck=CharClass.ARMOR_LEATHER; break;
    	case Ability.ACODE_COMMON_SKILL: break;
    	case Ability.ACODE_DISEASE: break;
    	case Ability.ACODE_LANGUAGE: break;
    	case Ability.ACODE_POISON: break;
    	case Ability.ACODE_PRAYER: break;
    	case Ability.ACODE_PROPERTY: break;
    	case Ability.ACODE_SKILL: break;
    	case Ability.ACODE_SPELL: armorCheck=CharClass.ARMOR_CLOTH; break;
    	case Ability.ACODE_SUPERPOWER: break;
    	case Ability.ACODE_THIEF_SKILL: armorCheck=CharClass.ARMOR_LEATHER; break;
    	case Ability.ACODE_TRAP: break;
    	default:
    		break;
    	}
    	if((armorCheck>0)
		&&(!auto)
		&&(!mob.isMonster())
		&&(!disregardsArmorCheck(mob))
		&&(!CMLib.utensils().armorCheck(mob,armorCheck))
		&&(mob.isMine(this))
		&&(mob.location()!=null)
		&&(CMLib.dice().rollPercentage()<50))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> fumble(s) "+name()+" due to <S-HIS-HER> armor!");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget, auto, asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			int castCode=0;
	    	switch(classificationCode()&Ability.ALL_ACODES)
	    	{
	    	case Ability.ACODE_CHANT: castCode=CMMsg.MSG_CAST_VERBAL_SPELL; break;
	    	case Ability.ACODE_COMMON_SKILL: castCode=CMMsg.MSG_NOISYMOVEMENT; break;
	    	case Ability.ACODE_DISEASE: castCode=CMMsg.MSG_NOISYMOVEMENT; break;
	    	case Ability.ACODE_LANGUAGE: castCode=CMMsg.MSG_OK_VISUAL; break;
	    	case Ability.ACODE_POISON: castCode=CMMsg.MSG_NOISYMOVEMENT; break;
	    	case Ability.ACODE_PRAYER: castCode=CMMsg.MSG_CAST_SOMANTIC_SPELL; break;
	    	case Ability.ACODE_PROPERTY: castCode=CMMsg.MSG_OK_VISUAL; break;
	    	case Ability.ACODE_SKILL: castCode=CMMsg.MSG_NOISYMOVEMENT; break;
	    	case Ability.ACODE_SPELL: castCode=CMMsg.MSG_CAST_SOMANTIC_SPELL; break;
	    	case Ability.ACODE_SUPERPOWER: castCode=CMMsg.MSG_CAST_SOMANTIC_SPELL; break;
	    	case Ability.ACODE_THIEF_SKILL: castCode=CMMsg.MSG_THIEF_ACT; break;
	    	case Ability.ACODE_TRAP: castCode=CMMsg.MSG_NOISYMOVEMENT; break;
	    	default:
	    		castCode=CMMsg.MSG_CAST_SOMANTIC_SPELL;
	    		break;
	    	}
			if(castingQuality(mob,target)==Ability.QUALITY_MALICIOUS)
				castCode|=CMMsg.MASK_MALICIOUS;
			if(auto) castCode|=CMMsg.MASK_ALWAYS;

			CMMsg msg = CMClass.getMsg(mob, target, this, castCode, (auto?(String)V(ID,V_ACST):(String)V(ID,V_CAST)));
			CMMsg msg2= null;
			Integer OTH=(Integer)V(ID,V_ATT2);
			if(OTH.intValue()>0)
				msg2=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS|OTH.intValue()|(auto?CMMsg.MASK_ALWAYS:0),null);
			if(mob.location().okMessage(mob,msg)&&(this.okMessage(mob, msg))
			&&((msg2==null)||(mob.location().okMessage(mob,msg2)&&(this.okMessage(mob, msg2)))))
			{
				mob.location().send(mob,msg);
				this.executeMsg(mob, msg);
				if(msg2!=null){ mob.location().send(mob,msg2); this.executeMsg(mob, msg2);}
                if((msg.value()<=0)&&((msg2==null)||(msg2.value()<=0)))
                {
                    if((canAffectCode()!=0)&&(target!=null))
                    {
                        if(abstractQuality()==Ability.QUALITY_MALICIOUS)
                            success=maliciousAffect(mob,target,asLevel,0,-1);
                        else
                            success=beneficialAffect(mob,target,asLevel,0);
                    }
                    setTimeOfNextCast(mob);
                    String afterAffect=(String)V(ID,V_PAFF);
                    if((afterAffect.length()>0)&&(success))
                    {
                        Ability P=CMClass.getAbility("Prop_SpellAdder");
                        if(P!=null)
                        {
                            Vector V=CMParms.makeVector(afterAffect);
                            P.invoke(mob,V,null,true,asLevel); // spell adder will return addable affects
                            Ability A=null;
                            if(target!=null)
                            for(int v=0;v<V.size();v+=2)
                            {
                                A=(Ability)V.elementAt(v);
                                if(target.fetchEffect(A.ID())==null)
                                {
                                    A=(Ability)A.copyOf();
                                    int tickDown=(abstractQuality()==Ability.QUALITY_MALICIOUS)?
                                            getMaliciousTickdownTime(mob,target,0,asLevel):
                                            getBeneficialTickdownTime(mob,target,0,asLevel);
                                    A.startTickDown(mob,target,tickDown);
                                }
                            }
                        }
                    }
                }
                String DMG=(String)V(ID,V_PDMG);
                int dmg=0;
                if(DMG.trim().length()>0)
                    dmg=CMath.parseIntExpression(DMG,
                            new double[]{mob.envStats().level(),
                            (target==null)?mob.envStats().level():target.envStats().level()});
                if(((msg.value()<=0)&&((msg2==null)||(msg2.value()<=0)))
                ||(dmg>0))
                {
                    if((msg.value()<=0)&&((msg2==null)||(msg2.value()<=0)))
                        dmg=dmg/2;
                    if((success)&&(((String)V(ID,V_PCST)).length()>0))
                        if((target==null)||(target instanceof Exit)||(target instanceof Area)
                        ||(mob.location()==CMLib.map().roomLocation(target)))
                        {
                            if(dmg>0)
                            {
                                if(!(target instanceof MOB)) target=mob;
                                CMLib.combat().postDamage(mob,(MOB)target,this,dmg,CMMsg.MASK_ALWAYS|((OTH.intValue()<=0)?castCode:OTH.intValue()),Weapon.TYPE_BURSTING,(String)V(ID,V_PCST));
                                dmg=0;
                            }
                            else
                            if(dmg<0)
                            {
                                if(!(target instanceof MOB)) target=mob;
                                CMLib.combat().postHealing(mob,(MOB)target,this,-dmg,CMMsg.MASK_ALWAYS|((OTH.intValue()<=0)?castCode:OTH.intValue()),(String)V(ID,V_PCST));
                                dmg=0;
                            }
                            else
                                CMLib.map().roomLocation(target).show(mob,target,CMMsg.MSG_OK_ACTION,(String)V(ID,V_PCST));
                        }
                    if(dmg>0)
                    {
                        if(!(target instanceof MOB)) target=mob;
                        CMLib.combat().postDamage(mob,(MOB)target,this,dmg,CMMsg.MASK_ALWAYS|((OTH.intValue()<=0)?castCode:OTH.intValue()),Weapon.TYPE_BURSTING,null);
                    }
                    else
                    if(dmg<0)
                    {
                        if(!(target instanceof MOB)) target=mob;
                        CMLib.combat().postHealing(mob,(MOB)target,this,dmg,CMMsg.MASK_ALWAYS|((OTH.intValue()<=0)?castCode:OTH.intValue()),null);
                    }
                    if(CMLib.flags().isInTheGame(mob,true)&&((target==null)||CMLib.flags().isInTheGame(target,true)))
                    {
                        ScriptingEngine S=getScripter();
                        if((success)&&(S!=null))
                        {
                            msg2=CMClass.getMsg(mob,target,this,CMMsg.MSG_OK_VISUAL,null,null,ID);
                            S.executeMsg(mob, msg2);
                            S.dequeResponses();
                        }
                        mob.location().recoverRoomStats();
                    }

				}
                if(((msg.value()<=0)&&((msg2==null)||(msg2.value()<=0)))
                &&(CMLib.flags().isInTheGame(mob,true)&&((target==null)||CMLib.flags().isInTheGame(target,true))))
                {
                    String afterCast=(String)V(ID,V_PABL);
                    if(afterCast.length()>0)
                    {
                        Ability P=CMClass.getAbility("Prop_SpellAdder");
                        if(P!=null) P.invoke(mob,CMParms.makeVector(afterCast),target,true,asLevel);
                    }
                }
			}

		}
		else
		if(abstractQuality()==Ability.QUALITY_MALICIOUS)
			return maliciousFizzle(mob,target,(String)V(ID,V_FZZL));
		else
			return beneficialVisualFizzle(mob,target,(String)V(ID,V_FZZL));

        return true;
    }

    public boolean preInvoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel, int secondsElapsed, double actionsRemaining)
    {
        return true;
    }

    public void executeMsg(Environmental myHost, CMMsg msg)
    {
        ScriptingEngine S=getScripter();
        if(S!=null)
            S.executeMsg(myHost,msg);
        return;
    }

    public void affectEnvStats(Environmental affectedEnv, EnvStats affectableStats)
    {
        if((Ability)V(ID,V_HERE)!=null)
            ((Ability)V(ID,V_HERE)).affectEnvStats(affectedEnv,affectableStats);
    }
    public void affectCharStats(MOB affectedMob, CharStats affectableStats)
    {
        if(((Ability)V(ID,V_HERE))!=null)
            ((Ability)V(ID,V_HERE)).affectCharStats(affectedMob,affectableStats);

    }
    public void affectCharState(MOB affectedMob, CharState affectableMaxState)
    {
        if(((Ability)V(ID,V_HERE))!=null)
            ((Ability)V(ID,V_HERE)).affectCharState(affectedMob,affectableMaxState);
    }

    public boolean okMessage(Environmental myHost, CMMsg msg)
    {
        ScriptingEngine S=getScripter();
        if(S!=null)
            if(!S.okMessage(myHost,msg))
                return false;
        return true;
    }

    public boolean tick(Tickable ticking, int tickID)
    {
        if((unInvoked)&&(canBeUninvoked()))
            return false;
        if(!super.tick(ticking,tickID))
            return false;
        ScriptingEngine S=getScripter();
        if(S!=null)
            if(!S.tick(ticking,tickID))
                return false;
        return true;
    }

    // lots of work to be done here
    public int getSaveStatIndex(){return getStatCodes().length;}
    private static final String[] CODES={"CLASS",//0
                                         "TEXT",//1
                                         "NAME",//2S
                                         "DISPLAY",//3S
                                         "TRIGSTR",//4S[]
                                         "MAXRANGE",//5I
                                         "MINRANGE",//6I
                                         "AUTOINVOKE",//7B
                                         "FLAGS",//8I
                                         "CLASSIFICATION",//9I
                                         "OVERRIDEMANA",//10I
                                         "USAGEMASK",//11I
                                         "CANAFFECTMASK",//12I
                                         "CANTARGETMASK",//13I
                                         "QUALITY",//14I
                                         "HERESTATS",//15A
                                         "CASTMASK",//16S
                                         "SCRIPT",//17S
                                         "TARGETMASK", //18S
                                         "FIZZLEMSG", //19S
                                         "AUTOCASTMSG", //20S
                                         "CASTMSG",//21S
                                         "POSTCASTMSG",//22S
                                         "ATTACKCODE",//23I
                                         "POSTCASTAFFECT",//24S
                                         "POSTCASTABILITY",//25S
                                         "POSTCASTDAMAGE",//26I
                                         "HELP",//27I
                                         "TICKSBETWEENCASTS"//28I
                                        };
    public String[] getStatCodes(){return CODES;}
    protected int getCodeNum(String code){
        for(int i=0;i<CODES.length;i++)
            if(code.equalsIgnoreCase(CODES[i])) return i;
        return -1;
    }
    public String getStat(String code){
        switch(getCodeNum(code))
        {
        case 0: return ID();
        case 1: return text();
        case 2: return (String)V(ID,V_NAME);
        case 3: return (String)V(ID,V_DISP);
        case 4: return CMParms.toStringList((String[])V(ID,V_TRIG));
        case 5: return convert(Ability.RANGE_CHOICES,((Integer)V(ID,V_MAXR)).intValue(),false);
        case 6: return convert(Ability.RANGE_CHOICES,((Integer)V(ID,V_MINR)).intValue(),false);
        case 7: return ((Boolean)V(ID,V_AUTO)).toString();
        case 8: return convert(Ability.FLAG_DESCS,((Integer)V(ID,V_FLAG)).intValue(),true);
        case 9: return convertClassAndDomain(((Integer)V(ID,V_CLAS)).intValue());
        case 10: return ((Integer)V(ID,V_OMAN)).toString();
        case 11: return convert(Ability.USAGE_DESCS,((Integer)V(ID,V_USAG)).intValue(),true);
        case 12: return convert(Ability.CAN_DESCS,((Integer)V(ID,V_CAFF)).intValue(),true);
        case 13: return convert(Ability.CAN_DESCS,((Integer)V(ID,V_CTAR)).intValue(),true);
        case 14: return convert(Ability.QUALITY_DESCS,((Integer)V(ID,V_QUAL)).intValue(),false);
        case 15: return ((Ability)V(ID,V_HERE)).text();
        case 16: return (String)V(ID,V_CMSK);
        case 17: return (String)V(ID,V_SCRP);
        case 18: return (String)V(ID,V_TMSK);
        case 19: return (String)V(ID,V_FZZL);
        case 20: return (String)V(ID,V_ACST);
        case 21: return (String)V(ID,V_CAST);
        case 22: return (String)V(ID,V_PCST);
        case 23: return convert(CMMsg.TYPE_DESCS,((Integer)V(ID,V_ATT2)).intValue(),false);
        case 24: return (String)V(ID,V_PAFF);
        case 25: return (String)V(ID,V_PABL);
        case 26: return (String)V(ID,V_PDMG);
        case 27: return (String)V(ID,V_HELP);
        case 28: return ((Integer)V(ID,V_TKBC)).toString();
        default:
        	if(code.equalsIgnoreCase("allxml")) return getAllXML();
        	break;
        }
        return "";
    }
    public void setStat(String code, String val)
    {
        int num=0;
        int numDex=code.length();
        while((numDex>0)&&(Character.isDigit(code.charAt(numDex-1)))) numDex--;
        if(numDex<code.length())
        {
            num=CMath.s_int(code.substring(numDex));
            code=code.substring(0,numDex);
        }
        switch(getCodeNum(code))
        {
        case 0:
        if(val.trim().length()>0)
        {
        	V(ID,V_NAME); // force creation, if necc
        	Object[] O=(Object[])vars.get(ID);
        	vars.remove(ID);
        	vars.put(val,O);
        	if(num!=9)
        	    CMClass.delClass("ABILITY",this);
        	ID=val;
            if(num!=9)
            	CMClass.addClass("ABILITY",this);
        }
    	break;
        case 1: setMiscText(val); break;
        case 2: SV(ID,V_NAME,val);
                if(ID.equalsIgnoreCase("GenAbility"))
                    break;
                break;
        case 3: SV(ID,V_DISP,val); break;
        case 4: SV(ID,V_TRIG,CMParms.toStringArray(CMParms.parseCommas(val.toUpperCase(),true))); break;
        case 5: SV(ID,V_MAXR,Integer.valueOf(convert(Ability.RANGE_CHOICES,val,false))); break;
        case 6: SV(ID,V_MINR,Integer.valueOf(convert(Ability.RANGE_CHOICES,val,false))); break;
        case 7: SV(ID,V_AUTO,Boolean.valueOf(CMath.s_bool(val))); break;
        case 8: SV(ID,V_FLAG,Integer.valueOf(convert(Ability.FLAG_DESCS,val,true))); break;
        case 9: SV(ID,V_CLAS,Integer.valueOf(convertClassAndDomain(val))); break;
        case 10: SV(ID,V_OMAN,Integer.valueOf(CMath.s_parseIntExpression(val))); break;
        case 11: SV(ID,V_USAG,Integer.valueOf(convert(Ability.USAGE_DESCS,val,true))); break;
        case 12: SV(ID,V_CAFF,Integer.valueOf(convert(Ability.CAN_DESCS,val,true))); break;
        case 13: SV(ID,V_CTAR,Integer.valueOf(convert(Ability.CAN_DESCS,val,true))); break;
        case 14: SV(ID,V_QUAL,Integer.valueOf(convert(Ability.QUALITY_DESCS,val,false))); break;
        case 15: ((Ability)V(ID,V_HERE)).setMiscText(val); break;
        case 16: SV(ID,V_CMSK,val); break;
        case 17: SV(ID,V_SCRP,val); break;
        case 18: SV(ID,V_TMSK,val); break;
        case 19: SV(ID,V_FZZL,val); break;
        case 20: SV(ID,V_ACST,val); break;
        case 21: SV(ID,V_CAST,val); break;
        case 22: SV(ID,V_PCST,val); break;
        case 23: SV(ID,V_ATT2,Integer.valueOf(convert(CMMsg.TYPE_DESCS,val,false))); break;
        case 24: SV(ID,V_PAFF,val); break;
        case 25: SV(ID,V_PABL,val); break;
        case 26: SV(ID,V_PDMG,val); break;
        case 27: SV(ID,V_HELP,val); break;
        case 28: SV(ID,V_TKBC,Integer.valueOf(CMath.s_int(val))); break;
        default:
        	if(code.equalsIgnoreCase("allxml")&&ID.equalsIgnoreCase("GenAbility")) parseAllXML(val);
        	break;
        }
    }

    private String convert(String[] options, int val, boolean mask)
    {
    	if(mask)
    	{
        	StringBuffer str=new StringBuffer("");
        	for(int i=0;i<options.length;i++)
        		if((val&(1<<i))>0)
        			str.append(options[i]+",");
        	if(str.length()>0)
        	{
	        	String sstr=str.toString();
	        	if(sstr.endsWith(",")) sstr=sstr.substring(0,sstr.length()-1);
	        	return sstr;
        	}
    	}
    	else
    	if((val>=0)&&(val<options.length))
    		return options[val];
    	return ""+val;
    }

    private int convertClassAndDomain(String val)
    {
    	if(CMath.isInteger(val)) return CMath.s_int(val);
    	int dom=0;
    	int acod=Ability.ACODE_SKILL;
    	Vector V=CMParms.parseCommas(val,true);
    	for(int v=0;v<V.size();v++)
    	{
    		val=(String)V.elementAt(v);
    		int tacod=-1;
    		for(int a=0;a<Ability.ACODE_DESCS.length;a++)
    			if(val.equalsIgnoreCase(Ability.ACODE_DESCS[a]))
    				tacod=a;
    		if(tacod<0)
    		{
	        	for(int i=0;i<Ability.ACODE_DESCS.length;i++)
	        		if(Ability.ACODE_DESCS[i].toUpperCase().startsWith(val.toUpperCase()))
	        			tacod=i;
	    		if(tacod<0)
	    		{
		    		int tdom=-1;
		    		for(int a=0;a<Ability.DOMAIN_DESCS.length;a++)
		    			if(val.equalsIgnoreCase(Ability.DOMAIN_DESCS[a]))
		    				tdom=a<<5;
		    		if(tdom<0)
		        	for(int i=0;i<Ability.DOMAIN_DESCS.length;i++)
		        		if(Ability.DOMAIN_DESCS[i].toUpperCase().startsWith(val.toUpperCase())
		        				||Ability.DOMAIN_DESCS[i].toUpperCase().endsWith(val.toUpperCase()))
		        		{ tdom=i<<5; break;}
		        	if(tdom>=0) dom=tdom;
	    		}
    		}
    		else
    			acod=tacod;
    	}
    	return acod|dom;
    }

    private String convertClassAndDomain(int val)
    {
    	int dom=(val&Ability.ALL_DOMAINS)>>5;
    	int acod=val&Ability.ALL_ACODES;
    	if((acod>=0)&&(acod<Ability.ACODE_DESCS.length)
    	&&(dom>=0)&&(dom<Ability.DOMAIN_DESCS.length))
    		return Ability.ACODE_DESCS[acod]+","+Ability.DOMAIN_DESCS[dom];
    	return ""+val;
    }

    private int convert(String[] options, String val, boolean mask)
    {
    	if(CMath.isInteger(val)) return CMath.s_int(val);
    	for(int i=0;i<options.length;i++)
    		if(val.equalsIgnoreCase(options[i]))
    			return mask?(1<<i):i;
    	for(int i=0;i<options.length;i++)
    		if(options[i].toUpperCase().startsWith(val.toUpperCase()))
    			return mask?(1<<i):i;
    	if(mask)
    	{
    		Vector V=CMParms.parseCommas(val,true);
    		int num=0;
    		for(int v=0;v<V.size();v++)
    			num=num|(1<<convert(options,(String)V.elementAt(v),false));
    		return num;
    	}
    	return 0;
    }

    public boolean sameAs(Environmental E)
    {
        if(!(E instanceof GenAbility)) return false;
        if(!((GenAbility)E).ID().equals(ID)) return false;
        if(!((GenAbility)E).text().equals(text())) return false;
        return true;
    }

    private void parseAllXML(String xml)
    {
    	Vector V=CMLib.xml().parseAllXML(xml);
    	if((V==null)||(V.size()==0)) return;
    	for(int c=0;c<getStatCodes().length;c++)
    		if(getStatCodes()[c].equals("CLASS"))
    			ID=CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(V, getStatCodes()[c]));
    		else
    		if(!getStatCodes()[c].equals("TEXT"))
    			setStat(getStatCodes()[c],CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(V, getStatCodes()[c])));
    }
    private String getAllXML()
    {
    	StringBuffer str=new StringBuffer("");
    	for(int c=0;c<getStatCodes().length;c++)
    		if(!getStatCodes()[c].equals("TEXT"))
    			str.append("<"+getStatCodes()[c]+">"
    					+CMLib.xml().parseOutAngleBrackets(getStat(getStatCodes()[c]))
    					+"</"+getStatCodes()[c]+">");
    	return str.toString();
    }
}
