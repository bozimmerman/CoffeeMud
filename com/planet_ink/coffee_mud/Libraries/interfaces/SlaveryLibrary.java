package com.planet_ink.coffee_mud.Libraries.interfaces;
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
import com.planet_ink.coffee_mud.Libraries.CommonMsgs;
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
public interface SlaveryLibrary extends CMLibrary
{
    public final static int STEP_EVAL=0;
    public final static int STEP_INT1=1;
    public final static int STEP_INT2=2;
    public final static int STEP_INT3=3;
    public final static int STEP_INT4=4;
    public final static int STEP_INT5=5;
    public final static int STEP_ALLDONE=-999;

    public Vector findMatch(MOB mob, Vector prereq);
    public String cleanWord(String s);
    public geasSteps processRequest(MOB you, MOB me, String req);


    public static class geasSteps extends Vector
    {
        public static final long serialVersionUID=Long.MAX_VALUE;
        public Vector bothered=new Vector();
        public boolean done=false;
        public MOB you=null;
        public MOB me=null;

        public geasSteps(MOB you1, MOB me1)
        {
            you=you1;
            me=me1;
        }

        public void step()
        {
            String say=null;
            boolean moveFlag=false;
            boolean holdFlag=false;
            String ss=null;
            geasStep sg=null;

            if(!done)
            for(int s=0;s<size();s++)
            {
                geasStep G=(geasStep)elementAt(s);
                ss=G.step();
                if(ss.equalsIgnoreCase("DONE"))
                {
                    done=true;
                    break;
                }
                if(ss.equalsIgnoreCase("HOLD"))
                {
                    removeElementAt(s);
                    insertElementAt(G,0);
                    holdFlag=true;
                    break;
                }
                else
                if(ss.equalsIgnoreCase("MOVE"))
                    moveFlag=true;
                else
                if(ss.startsWith("1"))
                {
                    say=ss;
                    sg=G;
                }
                else
                if(ss.startsWith("0"))
                {
                    if(say==null)
                    {
                        say=ss;
                        sg=G;
                    }
                }

            }
            if(!holdFlag)
            {
                if((say!=null)&&(sg!=null)&&(ss!=null))
                {
                    if(!sg.botherIfAble(ss.substring(1)))
                    {
                        sg.step=STEP_EVAL;
                        move(CMath.s_int(""+ss.charAt(0)));
                    }
                    else
                        sg.step=STEP_INT1;
                }
                else
                if(moveFlag)
                    move(0);
            }
        }
        public void move(int moveCode)
        {
            if(!bothered.contains(me.location()))
                bothered.addElement(me.location());
            if(CMSecurity.isDebugging("GEAS"))
                Log.debugOut("GEAS","BEINGMOBILE: "+moveCode);
            if(moveCode==0)
            {
                if(!CMLib.tracking().beMobile(me,true,true,false,true,null,bothered))
                    CMLib.tracking().beMobile(me,true,true,false,false,null,null);
            }
            else
            {
                if(!CMLib.tracking().beMobile(me,true,true,true,false,null,bothered))
                    CMLib.tracking().beMobile(me,true,true,false,false,null,null);
            }
        }

        public boolean sayResponse(MOB speaker, MOB target, String response)
        {
            for(int s=0;s<size();s++)
            {
                geasStep G=(geasStep)elementAt(s);
                if(G.bothering!=null)
                    return G.sayResponse(speaker,target,response);
            }
            return false;
        }
    }

    public static class geasStep
    {
        public Vector que=new Vector();
        public int step=STEP_EVAL;
        public MOB bothering=null;
        public geasSteps mySteps=null;
        public MOB you=null;

        public geasStep(geasSteps gs)
        {
            mySteps=gs;
        }

        public boolean botherIfAble(String msgOrQ)
        {
            MOB me=mySteps.me;
            bothering=null;
            if((me==null)||(me.location()==null))
                return false;
            if((msgOrQ!=null)&&(!CMLib.flags().isAnimalIntelligence(me)))
                for(int m=0;m<me.location().numInhabitants();m++)
                {
                    MOB M=me.location().fetchInhabitant(m);
                    if((M!=null)
                    &&(M!=me)
                    &&(!CMLib.flags().isAnimalIntelligence(M))
                    &&(!mySteps.bothered.contains(M)))
                    {
                        CMLib.commands().postSay(me,M,msgOrQ,false,false);
                        bothering=M;
                        mySteps.bothered.addElement(M);
                        if(CMSecurity.isDebugging("GEAS"))
                            Log.debugOut("GEAS","BOTHERING: "+bothering.name());
                        return true;
                    }
                }
            return false;
        }

        public boolean sayResponse(MOB speaker, MOB target, String response)
        {
            MOB me=mySteps.me;
            if((speaker!=null)
            &&(speaker!=me)
            &&(bothering!=null)
            &&(speaker==bothering)
            &&(step!=STEP_EVAL)
            &&((target==null)||(target==me)))
            {
                for(int s=0;s<universalRejections.length;s++)
                {
                    if(CMLib.english().containsString(response,universalRejections[s]))
                    {
                        CMLib.commands().postSay(me,speaker,"Ok, thanks anyway.",false,false);
                        return true;
                    }
                }
                boolean starterFound=false;
                response=response.toLowerCase().trim();
                for(int s=0;s<responseStarters.length;s++)
                {
                    if(response.startsWith(responseStarters[s]))
                    {
                        starterFound=true;
                        response=response.substring(responseStarters[s].length()).trim();
                    }
                }
                if((!starterFound)&&(speaker.isMonster())&&(CMLib.dice().rollPercentage()<10))
                    return false;
                if(response.trim().length()==0)
                    return false;
                bothering=null;
                que.insertElementAt(CMParms.parse("find150 \""+response+"\""),0);
                step=STEP_EVAL;
                return true;
            }
            return false;
        }

        public String step()
        {
            MOB me=mySteps.me;
            if(me==null) return "DONE";
            Room R=me.location();
            if(R==null) return "HOLD";
            if(que.size()==0)
            {
                step=STEP_ALLDONE;
                return "DONE";
            }
            Vector cur=(Vector)que.firstElement();
            if(cur.size()==0)
            {
                step=STEP_EVAL;
                que.removeElementAt(0);
                return "HOLD";
            }
            String s=(String)cur.firstElement();
            if(CMSecurity.isDebugging("GEAS"))
                Log.debugOut("GEAS","STEP-"+s);
            if(s.equalsIgnoreCase("itemfind"))
            {
                String item=CMParms.combine(cur,1);
                if(CMSecurity.isDebugging("GEAS"))
                    Log.debugOut("GEAS","ITEMFIND: "+item);
                if((CMath.isNumber(item)&&(CMath.s_int(item)>0)))
                {
                    if(CMLib.beanCounter().getTotalAbsoluteNativeValue(me)>=((double)CMath.s_int(item)))
                    {
                        step=STEP_EVAL;
                        que.removeElementAt(0);
                        CMLib.commands().postSay(me,null,"I got the money!",false,false);
                        return "HOLD";
                    }
                    item="coins";
                }

                // do I already have it?
                Item I=me.fetchInventory(item);
                if((I!=null)&&(CMLib.flags().canBeSeenBy(I,me)))
                {
                    step=STEP_EVAL;
                    if(!I.amWearingAt(Wearable.IN_INVENTORY))
                    {
                        CMLib.commands().postRemove(me,I,false);
                        return "HOLD";
                    }
                    if(I.container()!=null)
                    {
                        CMLib.commands().postGet(me,I.container(),I,false);
                        return "HOLD";
                    }
                    que.removeElementAt(0);
                    CMLib.commands().postSay(me,null,"I got "+I.name()+"!",false,false);
                    return "HOLD";
                }
                // is it just sitting around?
                I=R.fetchItem(null,item);
                if((I!=null)&&(CMLib.flags().canBeSeenBy(I,me)))
                {
                    step=STEP_EVAL;
                    CMLib.commands().postGet(me,null,I,false);
                    return "HOLD";
                }
                // is it in a container?
                I=R.fetchAnyItem(item);
                if((I!=null)&&(I.container()!=null)
                   &&(I.container() instanceof Container)
                   &&(((Container)I.container()).isOpen()))
                {
                    step=STEP_EVAL;
                    CMLib.commands().postGet(me,I.container(),I,false);
                    return "HOLD";
                }
                // is it up for sale?
                for(int m=0;m<R.numInhabitants();m++)
                {
                    MOB M=R.fetchInhabitant(m);
                    if((M!=null)&&(M!=me)&&(CMLib.flags().canBeSeenBy(M,me)))
                    {
                        I=M.fetchInventory(null,item);
                        if((I!=null)&&(!I.amWearingAt(Wearable.IN_INVENTORY)))
                        {
                            if(step==STEP_EVAL)
                            {
                                CMLib.commands().postSay(me,M,"I must have '"+I.name()+".  Give it to me now.",false,false);
                                step=STEP_INT1;
                                return "HOLD";
                            }
                            else
                            if(step==STEP_INT1)
                            {
                                step=STEP_INT2;
                                return "HOLD";
                            }
                            else
                            if(step==STEP_INT2)
                            {
                                CMLib.commands().postSay(me,M,"I MUST HAVE '"+I.name().toUpperCase()+".  GIVE IT TO ME NOW!!!!",false,false);
                                step=STEP_INT3;
                                return "HOLD";
                            }
                            else
                            if(step==STEP_INT3)
                            {
                                step=STEP_INT4;
                                return "HOLD";
                            }
                            else
                            if(step==STEP_INT4)
                            {
                                CMLib.combat().postAttack(me,M,me.fetchWieldedItem());
                                step=STEP_EVAL;
                                return "HOLD";
                            }
                        }
                        ShopKeeper sk=CMLib.coffeeShops().getShopKeeper(M);
                        if((!item.equals("coins"))&&(sk!=null)&&(sk.getShop().getStock(item,me)!=null))
                        {
                            Environmental E=sk.getShop().getStock(item,me);
                            if((E!=null)&&(E instanceof Item))
                            {
                                double price=CMLib.coffeeShops().sellingPrice(M,me,E,sk,true).absoluteGoldPrice;
                                if(price<=CMLib.beanCounter().getTotalAbsoluteShopKeepersValue(me,M))
                                {
                                    me.enqueCommand(CMParms.parse("BUY \""+E.name()+"\""),Command.METAFLAG_FORCED|Command.METAFLAG_ORDER,0);
                                    step=STEP_EVAL;
                                    return "HOLD";
                                }
                                price=price-CMLib.beanCounter().getTotalAbsoluteShopKeepersValue(me,M);
                                que.insertElementAt(CMParms.parse("itemfind "+CMLib.beanCounter().nameCurrencyShort(M,price)),0);
                                CMLib.commands().postSay(me,null,"Damn, I need "+CMLib.beanCounter().nameCurrencyShort(M,price)+".",false,false);
                                step=STEP_EVAL;
                                return "HOLD";
                            }
                        }
                    }
                }
                // if asked someone something, give them time to respond.
                if((bothering!=null)&&(step>STEP_EVAL)&&(step<=STEP_INT4)&&(!bothering.isMonster()))
                {   step++; return "HOLD";}
                step=STEP_EVAL;
                return "0Can you tell me where to find "+CMParms.combine(cur,1)+"?";
            }
            else
            if(s.equalsIgnoreCase("mobfind"))
            {
                String name=CMParms.combine(cur,1);
                if(CMSecurity.isDebugging("GEAS"))
                    Log.debugOut("GEAS","MOBFIND: "+name);
                if(name.equalsIgnoreCase("you")) name=me.name();
                if(name.equalsIgnoreCase("yourself")) name=me.name();
                if(you!=null)
                {
                    if(name.equals("me")) name=you.name();
                    if(name.equals("myself")) name=you.name();
                    if(name.equals("my")) name=you.name();
                }

                MOB M=R.fetchInhabitant(name);
                if(M==me) M=R.fetchInhabitant(name+".2");
                if((M!=null)&&(M!=me)&&(CMLib.flags().canBeSeenBy(M,me)))
                {
                    if(CMSecurity.isDebugging("GEAS"))
                        Log.debugOut("GEAS","MOBFIND-FOUND: "+name);
                    step=STEP_EVAL;
                    que.removeElementAt(0);
                    return "HOLD";
                }

                // if asked someone something, give them time to respond.
                if((bothering!=null)&&(step>STEP_EVAL)&&(step<=STEP_INT4)&&(!bothering.isMonster()))
                {
                    if(CMSecurity.isDebugging("GEAS"))
                        Log.debugOut("GEAS","MOBFIND-RESPONSEWAIT: "+bothering.name());
                    step++;
                    return "HOLD";
                }
                step=STEP_EVAL;
                int code=0;
                if((you!=null)&&(you.name().equalsIgnoreCase(name)))
                    code=1;
                return code+"Can you tell me where to find "+name+"?";
            }
            else
            if(s.toLowerCase().startsWith("find"))
            {
                String name=CMParms.combine(cur,1);
                if(CMSecurity.isDebugging("GEAS"))
                    Log.debugOut("GEAS","FIND: "+name);
                if(name.equalsIgnoreCase("you")) name=me.name();
                if(name.equalsIgnoreCase("yourself")) name=me.name();
                if(you!=null)
                {
                    if(name.equals("me")) name=you.name();
                    if(name.equals("myself")) name=you.name();
                    if(name.equals("my")) name=you.name();
                }
                int dirCode=Directions.getGoodDirectionCode((String)CMParms.parse(name).firstElement());
                if((dirCode>=0)&&(R!=null)&&(R.getRoomInDir(dirCode)!=null))
                {
                    if(CMParms.parse(name).size()>1)
                        cur.setElementAt(CMParms.combine(CMParms.parse(name),1),1);
                    step=STEP_EVAL;
                    CMLib.tracking().move(me,dirCode,false,false);
                    return "HOLD";
                }

                if(CMLib.english().containsString(R.name(),name)
                   ||CMLib.english().containsString(R.displayText(),name)
                   ||CMLib.english().containsString(R.description(),name))
                {
                    step=STEP_EVAL;
                    que.removeElementAt(0);
                    return "HOLD";
                }
                MOB M=R.fetchInhabitant(name);
                if((M!=null)&&(M!=me)&&(CMLib.flags().canBeSeenBy(M,me)))
                {
                    step=STEP_EVAL;
                    que.removeElementAt(0);
                    return "HOLD";
                }
                // is it just sitting around?
                Item I=R.fetchItem(null,name);
                if((I!=null)&&(CMLib.flags().canBeSeenBy(I,me)))
                {
                    step=STEP_EVAL;
                    CMLib.commands().postGet(me,null,I,false);
                    return "HOLD";
                }
                if((s.length()>4)&&(CMath.isNumber(s.substring(4))))
                {
                    int x=CMath.s_int(s.substring(4));
                    if((--x)<0)
                    {
                        que.removeElementAt(0);
                        step=STEP_EVAL;
                        return "HOLD";
                    }
                    cur.setElementAt("find"+x,0);
                }

                // if asked someone something, give them time to respond.
                if((bothering!=null)&&(step>STEP_EVAL)&&(step<=STEP_INT4)&&(!bothering.isMonster()))
                {   step++; return "HOLD";}
                step=STEP_EVAL;
                if(s.length()>4)
                    return "0Can you tell me where to find "+name+"?";
                return "MOVE";
            }
            else
            if(s.equalsIgnoreCase("wanderquery"))
            {
                if(CMSecurity.isDebugging("GEAS"))
                    Log.debugOut("GEAS","WANDERQUERY: "+CMParms.combine(cur,1));
                // if asked someone something, give them time to respond.
                if((bothering!=null)&&(step>STEP_EVAL)&&(step<=STEP_INT4)&&(!bothering.isMonster()))
                {   step++; return "HOLD";}
                step=STEP_EVAL;
                return "Can you help me "+CMParms.combine(cur,1)+"?";
            }
            else
            {
                step=STEP_EVAL;
                que.removeElementAt(0);
                me.enqueCommand(cur,Command.METAFLAG_FORCED|Command.METAFLAG_ORDER,0);
                return "HOLD";
            }
        }
    }

        // these should be checked after pmap prelim check.
        public static final String[] universalStarters={
            "go ",
            "go and ",
            "i want you to ",
            "i command you to ",
            "i order you to ",
            "you are commanded to ",
            "please ",
            "to ",
            "you are ordered to "};

        public static final String[] responseStarters={
            "try at the",
            "its at the",
            "it`s at the",
            "over at the",
            "at the",
            "go to",
            "go to the",
            "find the",
            "go ",
            "try to the",
            "over to the",
            "you`ll have to go",
            "you`ll have to find the",
            "you`ll have to find",
            "look at the",
            "look at",
            "look to",
            "look to the",
            "look for",
            "look for the",
            "search at the",
            "search at",
            "search to the",
            "look",
            "i saw one at",
            "he`s",
            "it`s",
            "she`s",
            "hes",
            "shes",
            "its",
        };
        public static String[] universalRejections={
            "dunno",
            "nope",
            "not",
            "never",
            "nowhere",
            "noone",
            "can`t",
            "cant",
            "don`t",
            "dont",
            "no"
        };

        //codes:
        //%m mob name (anyone)
        //%i item name (anything)
        //%g misc parms
        //%c casters name
        //%s social name
        //%k skill command word
        //%r room name
        // * match anything
        public static final String[][] pmap={
            // below is killing
            {"kill %m","mobfind %m;kill %m"},
            {"find and kill %m","mobfind %m;kill %m"},
            {"murder %m","mobfind %m;kill %m"},
            {"find and murder %m","mobfind %m;kill %m"},
            {"find and destroy %m","mobfind %m;kill %m"},
            {"search and destroy %m","mobfind %m;kill %m"},
            {"destroy %i","itemfind %i;recall"},
            {"find and destroy %i","mobfind %i;recall"},
            {"search and destroy %i","mobfind %i;recall"},
            {"destroy %m","mobfind %m;kill %m"},
            {"assassinate %m","mobfind %m; kill %m"},
            {"find and assassinate %m","mobfind %m; kill %m"},
            // below is socials
            {"find and %s %m","mobfind %m;%s %m"},
            {"%s %m","mobfind %m;%s %m"},
            // below is item fetching
//    DROWN, DROWN YOURSELF, DROWN IN A LAKE, SWIM, SWIM AN OCEAN, CLIMB A MOUNTAIN, CLIMB A TREE, CLIMB <X>, SWIM <x>, HANG YOURSELF, CRAWL <x>
//    BLOW YOUR NOSE, VOMIT, PUKE, THROW UP, KISS MY ASS, KISS <CHAR> <Body part>
            {"bring %i","itemfind %i;mobfind %c;give %i %c"},
            {"bring %m %i","itemfind %i;mobfind %m;give %i %m"},
            {"bring %i to %m","itemfind %i;mobfind %m;give %i %m"},
            {"bring me %i","itemfind %i;mobfind %c;give %i %c"},
            {"bring my %i","itemfind %i;mobfind %c;give %i %c"},
            {"make %i","itemfind %i;mobfind %c;give %i %c"},
            {"make %m %i","itemfind %i;mobfind %m;give %i %m"},
            {"make %i for %m","itemfind %i;mobfind %m;give %i %m"},
            {"make me %i","itemfind %i;mobfind %c;give %i %c"},
            {"give %i","itemfind %i;mobfind %c;give %i %c"},
            {"give %m %i","itemfind %i;mobfind %m;give %i %m"},
            {"give %i to %m","itemfind %i;mobfind %m;give %i %m"},
            {"give me %i","itemfind %i;mobfind %c;give %i %c"},
            {"give your %i","itemfind %i;mobfind %c;give %i %c"},
            {"give %m your %i","itemfind %i;mobfind %m;give %i %m"},
            {"give your %i to %m","itemfind %i;mobfind %m;give %i %m"},
            {"give me your %i","itemfind %i;mobfind %c;give %i %c"},
            {"buy %i","itemfind %i;mobfind %c;give %i %c"},
            {"buy %m %i","itemfind %i;mobfind %m;give %i %m"},
            {"buy %i to %m","itemfind %i;mobfind %m;give %i %m"},
            {"buy me %i","itemfind %i;mobfind %c;give %i %c"},
            {"find me %i","itemfind %i;mobfind %c;give %i %c"},
            {"find my %i","itemfind %i;mobfind %c;give %i %c"},
            {"find %i for %m","itemfind %i;mobfind %m;give %i %m"},
            {"find %m %i","itemfind %i;mobfind %m;give %i %m"},
            {"fetch me %i","itemfind %i;mobfind %c;give %i %c"},
            {"fetch my %i","itemfind %i;mobfind %c;give %i %c"},
            {"fetch %i for %m","itemfind %i;mobfind %m;give %i %m"},
            {"fetch %m %i","itemfind %i;mobfind %m;give %i %m"},
            {"get me %i","itemfind %i;mobfind %c;give %i %c"},
            {"get my %i","itemfind %i;mobfind %c;give %i %c"},
            {"get %i for %m","itemfind %i;mobfind %m;give %i %m"},
            {"get %m %i","itemfind %i;mobfind %m;give %i %m"},
            {"get %i","itemfind %i"},
            {"deliver %i to %m","itemfind %i;mobfind %m;give %i %m"},
            {"deliver my %i to %m","itemfind %i;mobfind %m;give %i %m"},
            // below are eats, drinks
            {"eat %i","itemfind %i;eat %i"},
            {"consume %i","itemfind %i;eat %i"},
            {"stuff yourself with %i","itemfind %i;eat %i"},
            {"drink %i","itemfind %i;drink %i"},
            // below are gos, and find someone (and report back where), take me to, show me
            {"go to %r","find %r;sit"},
            {"report to %r","find %r;sit"},
            {"walk to %r","find %r;sit"},
            {"find %r","find %r;"},
            {"find %r","find %r;"},
            {"show me the way to %r","say follow me;find %r;"},
            {"show me how to get to %r","say follow me;find %r;"},
            {"show me how to get %r","say follow me;find %r;"},
            {"take me to %r","say follow me;find %r;"},
            // follow someone around (but not FOLLOW)
            // simple commands: hold, lock, unlock, read, channel
            {"hold %i","itemfind %i;hold %i"},
            {"lock %i","itemfind %i;lock %i"},
            {"unlock %i","itemfind %i;unlock %i"},
            {"read %i","itemfind %i;read %i"},
            {"gossip %g","gossip %g"},
            // more simpletons: say sit sleep stand wear x, wield x
            {"sleep","sleep"},
            {"sit","sit"},
            {"stand","stand"},
            {"sit down","sit"},
            {"stand up","stand"},
            {"wear %i","itemfind %i;wear %i"},
            {"wield %i","itemfind %i;wield %i"},
            // below are sit x sleep x mount x enter x
            {"sit %i","itemfind %i;sit %i"},
            {"sleep %i","itemfind %i;sleep %i"},
            {"mount %i","itemfind %i;mount %i"},
            {"mount %m","mobfind %m;mount %m"},
            // below are learns, practices, teaches, etc..
            // below are tells, say tos, report tos,
            {"tell %m %g","mobfind %m;sayto %m %g"},
            {"say %g to %m","mobfind %m;sayto %m %g"},
            {"tell %g to %m","mobfind %m;sayto %m %g"},
            // below are skill usages
            {"%k %i","itemfind %i;%k %i"},
            {"%k %m","mobfind %m;%k %m"},
            {"%k %g %i","itemfind %i;%k %g %i"},
            {"%k %g %m","mobfind %m;%k %g %m"},
            // below are silly questions
            {"where %*","say You want me to answer where? I don't know where!"},
            {"who %*","say You want me to answer who? I don't know who!"},
            {"when %*","say You want me to answer when? I don't know when!"},
            {"what %*","say You want me to answer what? I don't know what!"},
            {"why %*","say You want me to answer why? I don't know why!"}
        };
}
