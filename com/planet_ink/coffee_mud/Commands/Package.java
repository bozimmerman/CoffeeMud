package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Package extends BaseItemParser
{
    public Package(){}

    private String[] access={"PACKAGE"};
    public String[] getAccessWords(){return access;}

    public boolean execute(MOB mob, Vector commands)
        throws java.io.IOException
    {
        if(commands.size()<2)
        {
            mob.tell("Package what?");
            return false;
        }
        commands.removeElementAt(0);
        String whatName="";
        if(commands.size()>0)
            whatName=(String)commands.lastElement();
        int maxToGet=Integer.MAX_VALUE;
        if((commands.size()>1)
        &&(Util.s_int((String)commands.firstElement())>0)
        &&(EnglishParser.numPossibleGold(null,Util.combine(commands,0))==0))
        {
            maxToGet=Util.s_int((String)commands.firstElement());
            commands.setElementAt("all",0);
        }
        String whatToGet=Util.combine(commands,0);
        boolean allFlag=(commands.size()>0)?((String)commands.elementAt(0)).equalsIgnoreCase("all"):false;
        if(whatToGet.toUpperCase().startsWith("ALL.")){ allFlag=true; whatToGet="ALL "+whatToGet.substring(4);}
        if(whatToGet.toUpperCase().endsWith(".ALL")){ allFlag=true; whatToGet="ALL "+whatToGet.substring(0,whatToGet.length()-4);}
        Vector V=new Vector();
        int addendum=1;
        String addendumStr="";
        do
        {
            Environmental getThis=null;
            if(getThis==null)
                getThis=mob.location().fetchFromRoomFavorItems(null,whatToGet+addendumStr,Item.WORN_REQ_UNWORNONLY);
            if(getThis==null) break;
            if((getThis instanceof Item)
            &&(Sense.canBeSeenBy(getThis,mob))
            &&((!allFlag)||Sense.isGettable(((Item)getThis))||(getThis.displayText().length()>0))
            &&(!V.contains(getThis)))
                V.addElement(getThis);
            addendumStr="."+(++addendum);
        }
        while((allFlag)&&(addendum<=maxToGet));

        if(V.size()==0)
        {
            mob.tell("You don't see '"+whatName+"' here.");
            return false;
        }
        
        for(int i=0;i<V.size();i++)
            if((V.elementAt(i) instanceof Coins)
            ||(!(V.elementAt(i) instanceof Item)))
            {
                mob.tell("Items such as "+((Item)V.elementAt(i)).name()+" may not be packaged.");
                return false;
            }
        PackagedItems thePackage=(PackagedItems)CMClass.getItem("GenPackagedItems");
        if(thePackage==null) return false;
        if(!thePackage.isPackagable(V))
        {
            mob.tell("All items in a package must be absolutely identical.  Some here are not.");
            return false;
        }
        Item getThis=null;
        for(int i=0;i<V.size();i++)
        {
            getThis=(Item)V.elementAt(i);
            if((!mob.isMine(getThis))&&(!Get.get(mob,null,getThis,true,"get",true)))
                return false;
        }
        String name=EnglishParser.cleanArticles(getThis.name());
        FullMsg msg=new FullMsg(mob,getThis,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> package(s) up "+V.size()+" <T-NAMENOART>(s).");
        if(mob.location().okMessage(mob,msg))
        {
            mob.location().send(mob,msg);
            thePackage.setName(name);
            thePackage.packageMe(getThis,V.size());
            for(int i=0;i<V.size();i++)
                ((Item)V.elementAt(i)).destroy();
            mob.location().addItemRefuse(thePackage,Item.REFUSE_PLAYER_DROP);
            mob.location().recoverRoomStats();
            mob.location().recoverRoomStats();
        }
        return false;
    }
    public int ticksToExecute(){return 1;}
    public boolean canBeOrdered(){return true;}

    public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}