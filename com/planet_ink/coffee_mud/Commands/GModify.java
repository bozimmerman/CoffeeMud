package com.planet_ink.coffee_mud.Commands;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.util.regex.*;

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
public class GModify extends StdCommand
{
    public GModify(){}

    private String[] access={"GMODIFY"};
    public String[] getAccessWords(){return access;}
    
    private static final int FLAG_CASESENSITIVE=1;
    private static final int FLAG_SUBSTRING=2;
    private static final int FLAG_OR=4;
    private static final int FLAG_AND=8;
    private static final int EQUATOR_$=0;
    private static final int EQUATOR_EQ=1;
    private static final int EQUATOR_NEQ=2;
    private static final int EQUATOR_GT=3;
    private static final int EQUATOR_LT=4;
    private static final int EQUATOR_LTEQ=5;
    private static final int EQUATOR_GTEQ=6;
    private static final Hashtable EQUATORS=CMStrings.makeNumericHash(new String[]{"$","=","!=",">","<","<=",">="});

    public static String getStat(Environmental E, String stat)
    {
        if((stat!=null)&&(stat.length()>0)&&(stat.equalsIgnoreCase("REJUV")))
        {
            if(E.baseEnvStats().rejuv()==Integer.MAX_VALUE)
                return "0";
            return ""+E.baseEnvStats().rejuv();
        }
        return E.getStat(stat);
    }

    public static void setStat(Environmental E, String stat, String value)
    {
        if((stat!=null)&&(stat.length()>0)&&(stat.equalsIgnoreCase("REJUV")))
            E.baseEnvStats().setRejuv(CMath.s_int(value));
        else
            E.setStat(stat,value);
    }

    public static void gmodifydebugtell(MOB mob, String msg)
    {
        if(mob!=null) mob.tell(msg);
        Log.sysOut("GMODIFY",msg);
    }

    private static boolean tryModfy(MOB mob,
                                    Room room,
                                    Environmental E,
                                    DVector changes,
                                    DVector onfields,
                                    boolean noisy)
    {
        if((mob.session()==null)||(mob.session().killFlag()))
        	return false;
        try{Thread.sleep(1);}catch(Exception e){mob.session().kill(false,false,false);return false;}
        boolean didAnything=false;
        if(noisy) gmodifydebugtell(mob,E.name()+"/"+CMClass.classID(E));
        String field=null;
        String value=null;
        String equator=null;
        String stat=null;
        int codes=-1;
        Pattern pattern=null;
        boolean checkedOut=true;
        Matcher M=null;
        DVector matches=new DVector(3);
        int lastCode=FLAG_AND;
        for(int i=0;i<onfields.size();i++)
        {
            field=(String)onfields.elementAt(i,1);
            equator=(String)onfields.elementAt(i,2);
            value=(String)onfields.elementAt(i,3);
            codes=((Integer)onfields.elementAt(i,4)).intValue();
            pattern=(Pattern)onfields.elementAt(i,5);
            if(noisy) gmodifydebugtell(mob,field+"/"+getStat(E,field)+"/"+value+"/"+getStat(E,field).equals(value));
            int matchStart=-1;
            int matchEnd=-1;
            stat=getStat(E,field);
            Integer EQ=(Integer)EQUATORS.get(equator);
            if(EQ!=null)
            switch(EQ.intValue())
            {
            case EQUATOR_$:
                if(pattern!=null)
                {
                    if(!CMath.bset(codes,FLAG_SUBSTRING))
                    {
                        if(stat.matches(value))
                        {
                            matchStart=0;
                            matchEnd=stat.length();
                        }
                    }
                    else
                    {
                        M=pattern.matcher(stat);
                        M.reset();
                        if(M.find())
                        {
                            matchStart=M.start();
                            matchEnd=M.end();
                        }
                    }
                }
                break;
            case EQUATOR_EQ:
            {
                if(!CMath.bset(codes,FLAG_CASESENSITIVE))
                {
                    stat=stat.toLowerCase();
                    value=value.toLowerCase();
                }
                if(CMath.bset(codes,FLAG_SUBSTRING))
                {
                    matchStart=stat.indexOf(value);
                    matchEnd=matchStart+value.length();
                }
                else
                if(stat.equals(value))
                {
                    matchStart=0;
                    matchEnd=stat.length();
                }
                break;
            }
            case EQUATOR_NEQ:
            {
                if(!CMath.bset(codes,FLAG_CASESENSITIVE))
                {
                    stat=stat.toLowerCase();
                    value=value.toLowerCase();
                }
                if(CMath.bset(codes,FLAG_SUBSTRING))
                {
                    if(stat.indexOf(value)<0)
                    {
                        matchStart=0;
                        matchEnd=stat.length();
                    }
                }
                else
                if(!stat.equals(value))
                {
                    matchStart=0;
                    matchEnd=stat.length();
                }
                break;
            }
            case EQUATOR_GT:
            {
                if(!CMath.bset(codes,FLAG_CASESENSITIVE))
                {
                    stat=stat.toLowerCase();
                    value=value.toLowerCase();
                }
                if(CMath.isNumber(stat)&&CMath.isNumber(value))
                    matchStart=(CMath.s_long(stat)>CMath.s_long(value))?0:-1;
                else
                    matchStart=(stat.compareTo(value)>0)?0:-1;
                break;
            }
            case EQUATOR_LT:
            {
                if(!CMath.bset(codes,FLAG_CASESENSITIVE))
                {
                    stat=stat.toLowerCase();
                    value=value.toLowerCase();
                }
                if(CMath.isNumber(stat)&&CMath.isNumber(value))
                    matchStart=(CMath.s_long(stat)<CMath.s_long(value))?0:-1;
                else
                    matchStart=(stat.compareTo(value)<0)?0:-1;
                break;
            }
            case EQUATOR_LTEQ:
            {
                if(!CMath.bset(codes,FLAG_CASESENSITIVE))
                {
                    stat=stat.toLowerCase();
                    value=value.toLowerCase();
                }
                if(CMath.isNumber(stat)&&CMath.isNumber(value))
                    matchStart=(CMath.s_long(stat)<=CMath.s_long(value))?0:-1;
                else
                    matchStart=(stat.compareTo(value)<=0)?0:-1;
                break;
            }
            case EQUATOR_GTEQ:
            {
                if(!CMath.bset(codes,FLAG_CASESENSITIVE))
                {
                    stat=stat.toLowerCase();
                    value=value.toLowerCase();
                }
                if(CMath.isNumber(stat)&&CMath.isNumber(value))
                    matchStart=(CMath.s_long(stat)>=CMath.s_long(value))?0:-1;
                else
                    matchStart=(stat.compareTo(value)>=0)?0:-1;
                break;
            }
            }
            if(matchStart>=0)
                matches.addElement(field,Integer.valueOf(matchStart),Integer.valueOf(matchEnd));
            if(CMath.bset(lastCode,FLAG_AND))
                checkedOut=checkedOut&&(matchStart>=0);
            else
            if(CMath.bset(lastCode,FLAG_OR))
                checkedOut=checkedOut||(matchStart>=0);
            else
                checkedOut=(matchStart>=0);
            lastCode=codes;
        }
        if(checkedOut)
        {
            if(changes.size()==0)
                mob.tell("Matched on "+E.name()+" from "+CMLib.map().getExtendedRoomID(room)+".");
            else
            for(int i=0;i<changes.size();i++)
            {
                field=(String)changes.elementAt(i,1);
                value=(String)changes.elementAt(i,3);
                codes=((Integer)changes.elementAt(i,4)).intValue();
                if(noisy) gmodifydebugtell(mob,E.name()+" wants to change "+field+" value "+getStat(E,field)+" to "+value+"/"+(!getStat(E,field).equals(value)));
                if(CMath.bset(codes,FLAG_SUBSTRING))
                {
                    int matchStart=-1;
                    int matchEnd=-1;
                    for(int m=0;m<matches.size();m++)
                        if(((String)matches.elementAt(m,1)).equals(field))
                        {
                            matchStart=((Integer)matches.elementAt(m,2)).intValue();
                            matchEnd=((Integer)matches.elementAt(m,3)).intValue();
                        }
                    if(matchStart>=0)
                    {
                        stat=getStat(E,field);
                        value=stat.substring(0,matchStart)+value+stat.substring(matchEnd);
                    }
                }
                if(!getStat(E,field).equals(value))
                {
                    Log.sysOut("GMODIFY","The "+CMStrings.capitalizeAndLower(field)+" field on "+E.Name()+" in "+room.roomID()+" was changed from "+getStat(E,field)+" to "+value+".");
                    setStat(E,field,value);
                    didAnything=true;
                }
            }
        }
        if(didAnything)
        {
            E.recoverEnvStats();
            if(E instanceof MOB)
            {
                ((MOB)E).recoverCharStats();
                ((MOB)E).recoverMaxState();
            }
            E.text();
        	if(CMLib.flags().isCataloged(E))
        		CMLib.catalog().updateCatalog(E);
        }
        return didAnything;
    }
    
    public void sortEnumeratedList(Enumeration e, Vector allKnownFields, StringBuffer allFieldsMsg)
    {
        for(;e.hasMoreElements();)
        {
            Environmental E=(Environmental)e.nextElement();
            String[] fields=E.getStatCodes();
            for(int x=0;x<fields.length;x++)
                if(!allKnownFields.contains(fields[x]))
                {
                    allKnownFields.addElement(fields[x]);
                    allFieldsMsg.append(fields[x]+" ");
                }
        }
    }

    public boolean execute(MOB mob, Vector commands, int metaFlags)
        throws java.io.IOException
    {
        boolean noisy=CMSecurity.isDebugging("GMODIFY");
        Vector placesToDo=new Vector();
        String whole=CMParms.combine(commands,0);
        commands.removeElementAt(0);
        if(commands.size()==0)
        {
            mob.tell("GModify what?");
            return false;
        }
        if(mob.isMonster())
        {
            mob.tell("No can do.");
            return false;
        }
        if((commands.size()>0)&&
           ((String)commands.elementAt(0)).equalsIgnoreCase("?"))
        {
            StringBuffer allFieldsMsg=new StringBuffer("");
            Vector allKnownFields=new Vector();
            sortEnumeratedList(CMClass.mobTypes(),allKnownFields,allFieldsMsg);
            sortEnumeratedList(CMClass.basicItems(),allKnownFields,allFieldsMsg);
            sortEnumeratedList(CMClass.weapons(),allKnownFields,allFieldsMsg);
            sortEnumeratedList(CMClass.armor(),allKnownFields,allFieldsMsg);
            sortEnumeratedList(CMClass.clanItems(),allKnownFields,allFieldsMsg);
            sortEnumeratedList(CMClass.miscMagic(),allKnownFields,allFieldsMsg);
            sortEnumeratedList(CMClass.miscTech(),allKnownFields,allFieldsMsg);
            mob.tell("Valid field names are "+allFieldsMsg.toString());
            return false;
        }
        if((commands.size()>0)&&
           ((String)commands.elementAt(0)).equalsIgnoreCase("room"))
        {
            if(!CMSecurity.isAllowed(mob,mob.location(),"GMODIFY"))
            {
                mob.tell("You are not allowed to do that here.");
                return false;
            }
            commands.removeElementAt(0);
            placesToDo.addElement(mob.location());
        }
        if((commands.size()>0)&&
           ((String)commands.elementAt(0)).equalsIgnoreCase("area"))
        {
            if(!CMSecurity.isAllowed(mob,mob.location(),"GMODIFY"))
            {
                mob.tell("You are not allowed to do that here.");
                return false;
            }
            commands.removeElementAt(0);
            placesToDo.addElement(mob.location().getArea());
        }
        if((commands.size()>0)&&
           ((String)commands.elementAt(0)).equalsIgnoreCase("world"))
        {
            if(!CMSecurity.isAllowedEverywhere(mob,"GMODIFY"))
            {
                mob.tell("You are not allowed to do that.");
                return false;
            }
            commands.removeElementAt(0);
            placesToDo=new Vector();
        }
        DVector changes=new DVector(5);
        DVector onfields=new DVector(5);
        DVector use=null;
        Vector allKnownFields=new Vector();
        StringBuffer allFieldsMsg=new StringBuffer("");
        sortEnumeratedList(CMClass.mobTypes(),allKnownFields,allFieldsMsg);
        sortEnumeratedList(CMClass.basicItems(),allKnownFields,allFieldsMsg);
        sortEnumeratedList(CMClass.weapons(),allKnownFields,allFieldsMsg);
        sortEnumeratedList(CMClass.armor(),allKnownFields,allFieldsMsg);
        sortEnumeratedList(CMClass.clanItems(),allKnownFields,allFieldsMsg);
        sortEnumeratedList(CMClass.miscMagic(),allKnownFields,allFieldsMsg);
        sortEnumeratedList(CMClass.miscTech(),allKnownFields,allFieldsMsg);

        allKnownFields.addElement("REJUV");
        allFieldsMsg.append("REJUV ");
        use=onfields;
        Vector newSet=new Vector();
        StringBuffer s=new StringBuffer("");
        for(int i=0;i<commands.size();i++)
        {
            String str=((String)commands.elementAt(i));
            if((str.toUpperCase().startsWith("CHANGE="))
            ||(str.toUpperCase().startsWith("WHEN=")))
            {
                if(s.length()>0)
                    newSet.addElement(s.toString());
                s=new StringBuffer("");
            }
            if(str.indexOf(" ")>=0)
                str="\""+str+"\"";
            if(s.length()>0)
                s.append(" "+str);
            else
                s.append(str);
        }
        if(s.length()>0)
            newSet.addElement(s.toString());
        for(int i=0;i<newSet.size();i++)
        {
            String str=((String)newSet.elementAt(i));
            if(str.toUpperCase().startsWith("CHANGE="))
            {
                use=changes;
                str=str.substring(7).trim();
            }
            if(str.toUpperCase().startsWith("WHEN="))
            {
                str=str.substring(5).trim();
                use=onfields;
            }
            while(str.trim().length()>0)
            {
                int eq=-1;
                int divLen=0;
                Integer code=Integer.valueOf(0);
                while((divLen==0)&&((++eq)<str.length()))
                    switch(str.charAt(eq))
                    {
                    case '!':
                        if((eq<(str.length()-1))&&(str.charAt(eq+1)=='='))
                        {
                            divLen=2;
                            break;
                        }
                        break;
                    case '=':
                    case '$':
                        divLen=1;
                        break;
                    case '<':
                    case '>':
                        divLen=1;
                        if((eq<(str.length()-1))&&(str.charAt(eq+1)=='='))
                        {
                            divLen=2;
                            break;
                        }
                        break;
                    }
                if(divLen==0)
                {
                    mob.tell("String '"+str+"' does not contain an equation divider.  Even CHANGE needs at least an = sign!");
                    return false;
                }
                String equator=str.substring(eq,eq+divLen);
                String val=str.substring(eq+divLen);
                String key=str.substring(0,eq).trim();
                
                int divBackLen=0;
                eq=-1;
                while((divBackLen==0)&&((++eq)<val.length()))
                    switch(val.charAt(eq))
                    {
                    case '&':
                        if((eq<(val.length()-1))&&(val.charAt(eq+1)=='&'))
                        {
                            divBackLen=2;
                            break;
                        }
                        break;
                    case '|':
                        if((eq<(val.length()-1))&&(val.charAt(eq+1)=='&'))
                        {
                            divBackLen=2;
                            break;
                        }
                        break;
                    }
                if(divBackLen==0)
                    str="";
                else
                {
                    String attach=val.substring(eq,eq+divBackLen).trim();
                    if(attach.equals("&&"))
                        code=Integer.valueOf(code.intValue()|FLAG_AND);
                    else
                    if(attach.equals("||"))
                        code=Integer.valueOf(code.intValue()|FLAG_OR);
                    str=val.substring(eq+divBackLen);
                    val=val.substring(0,eq);
                }
                Pattern P=null;
                if(use==null)
                {
                    mob.tell("'"+((String)commands.elementAt(i))+"' goes to an unknown parameter!");
                    return false;
                }
                while(val.trim().startsWith("["))
                {
                    int x2=val.indexOf("]");
                    if(x2<0) break;
                    String cd=val.trim().substring(1,x2);
                    if(cd.length()!=2)
                        break;
                    if(cd.equalsIgnoreCase("ss"))
                        code=Integer.valueOf(code.intValue()|FLAG_SUBSTRING);
                    if(cd.equalsIgnoreCase("cs"))
                        code=Integer.valueOf(code.intValue()|FLAG_CASESENSITIVE);
                    val=val.substring(x2+1);
                }
                if(equator.equals("$"))
                {
                    int patCodes=Pattern.DOTALL;
                    if(!CMath.bset(code.intValue(),FLAG_CASESENSITIVE))
                        patCodes=patCodes|Pattern.CASE_INSENSITIVE;
                    P=Pattern.compile(val,patCodes);
                }
                key=key.toUpperCase().trim();
                if(allKnownFields.contains(key))
                    use.addElement(key,equator,val,code,P);
                else
                {
                    mob.tell("'"+key+"' is an unknown field name.  Valid fields include: "+allFieldsMsg.toString());
                    return false;
                }
            }
        }
        if((onfields.size()==0)&&(changes.size()==0))
        {
            mob.tell("You must specify either WHEN, or CHANGES parameters for valid matches to be made.");
            return false;
        }
        if(placesToDo.size()==0)
        for(Enumeration a=CMLib.map().areas();a.hasMoreElements();)
        {
            Area A=(Area)a.nextElement();
            if(A.getCompleteMap().hasMoreElements()
            &&CMSecurity.isAllowed(mob,((Room)A.getCompleteMap().nextElement()),"GMODIFY"))
                placesToDo.addElement(A);
        }
        if(placesToDo.size()==0)
        {
            mob.tell("There are no rooms with data to gmodify!");
            return false;
        }
        for(int i=placesToDo.size()-1;i>=0;i--)
        {
            if(placesToDo.elementAt(i) instanceof Area)
            {
                Area A=(Area)placesToDo.elementAt(i);
                placesToDo.removeElement(A);
                for(Enumeration r=A.getCompleteMap();r.hasMoreElements();)
                {
                    Room R=(Room)r.nextElement();
                    if(CMSecurity.isAllowed(mob,R,"GMODIFY"))
                        placesToDo.addElement(R);
                }
            }
            else
            if(placesToDo.elementAt(i) instanceof Room)
                if(mob.session()!=null) mob.session().rawPrint(".");
            else
                return false;
        }
        // now do the modification...
        if(mob.session()!=null)
        {
            if(changes.size()==0)
                mob.session().rawPrintln("Searching...");
            else
                mob.session().rawPrint("Searching, modifying and saving...");
        }
        if(noisy) gmodifydebugtell(mob,"Rooms to do: "+placesToDo.size());
        if(noisy) gmodifydebugtell(mob,"When fields="+CMParms.toStringList(onfields.getDimensionVector(1)));
        if(noisy) gmodifydebugtell(mob,"Change fields="+CMParms.toStringList(changes.getDimensionVector(1)));
        Log.sysOut("GModify",mob.Name()+" "+whole+".");
        for(int r=0;r<placesToDo.size();r++)
        {
            Room R=(Room)placesToDo.elementAt(r);
            if(!CMSecurity.isAllowed(mob,R,"GMODIFY"))
                continue;
            if((R==null)||(R.roomID()==null)||(R.roomID().length()==0)) continue;
	    	synchronized(("SYNC"+R.roomID()).intern())
	    	{
	    		R=CMLib.map().getRoom(R);
	            if((mob.session()==null)||(mob.session().killFlag())||(R.getArea()==null))
	            	return false;
	            Area A=R.getArea();
	            int oldFlag=A.getAreaState();
	            if(changes.size()==0)
	            {
	                R=CMLib.coffeeMaker().makeNewRoomContent(R);
		            if(R!=null) A.setAreaState(Area.STATE_FROZEN);
	            }
	            else
	            {
		            A.setAreaState(Area.STATE_FROZEN);
	                CMLib.map().resetRoom(R);
	            }
	            if(R==null) continue;
	            boolean savemobs=false;
	            boolean saveitems=false;
	            for(int i=0;i<R.numItems();i++)
	            {
	                Item I=R.fetchItem(i);
	                if((I!=null)&&(tryModfy(mob,R,I,changes,onfields,noisy)))
	                    saveitems=true;
	            }
	            for(int m=0;m<R.numInhabitants();m++)
	            {
	                MOB M=R.fetchInhabitant(m);
	                if((M!=null)&&(M.savable()))
	                {
	                    if(tryModfy(mob,R,M,changes,onfields,noisy))
	                        savemobs=true;
		                for(int i=0;i<M.inventorySize();i++)
		                {
		                    Item I=M.fetchInventory(i);
		                    if((I!=null)&&(tryModfy(mob,R,I,changes,onfields,noisy)))
		                        savemobs=true;
		                }
		                if(CMLib.coffeeShops().getShopKeeper(M)!=null)
		                {
		                    Vector V=CMLib.coffeeShops().getShopKeeper(M).getShop().getStoreInventory();
		                    for(int i=0;i<V.size();i++)
		                    {
		                        if(V.elementAt(i) instanceof Item)
		                        {
		                            Item I=(Item)V.elementAt(i);
		                            if((I!=null)&&(tryModfy(mob,R,I,changes,onfields,noisy)))
		                                savemobs=true;
		                        }
		                    }
		                }
	                }
	            }
	            if(saveitems) CMLib.database().DBUpdateItems(R);
	            if(savemobs) CMLib.database().DBUpdateMOBs(R);
	            if((mob.session()!=null)&&(changes.size()>0)) 
	                mob.session().rawPrint(".");
	            A.setAreaState(oldFlag);
	            if(changes.size()==0) R.destroy();
	    	}
        }

        if(mob.session()!=null) mob.session().rawPrintln("!\n\rDone!");
        Area A=null;
        for(int i=0;i<placesToDo.size();i++)
        {
            A=((Room)placesToDo.elementAt(i)).getArea();
            if((A!=null)&&(A.getAreaState()>Area.STATE_ACTIVE)) 
                A.setAreaState(Area.STATE_ACTIVE);
        }
        return false;
    }
    
    public boolean canBeOrdered(){return true;}
    public boolean securityCheck(MOB mob){return CMSecurity.isAllowedAnywhere(mob,"GMODIFY");}

    
}
