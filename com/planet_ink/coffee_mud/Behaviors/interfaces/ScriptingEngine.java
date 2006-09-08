package com.planet_ink.coffee_mud.Behaviors.interfaces;
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

/* 
   Copyright 2000-2006 Bo Zimmerman

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
public interface ScriptingEngine extends Behavior
{
    public String execute(Environmental scripted,
                          MOB source,
                          Environmental target,
                          MOB monster,
                          Item primaryItem,
                          Item secondaryItem,
                          Vector script,
                          String msg,
                          Object[] tmp);
    
    public boolean eval(Environmental scripted,
                        MOB source,
                        Environmental target,
                        MOB monster,
                        Item primaryItem,
                        Item secondaryItem,
                        String msg,
                        Object[] tmp,
                        String evaluable);
    public boolean endQuest(Environmental hostObj, MOB mob, String quest);
    public static class ScriptableResponse
    {
        private int tickDelay=0;
        public Environmental h=null;
        public MOB s=null;
        public Environmental t=null;
        public MOB m=null;
        public Item pi=null;
        public Item si=null;
        public Vector scr;
        public String message=null;

        public ScriptableResponse(Environmental host,
                                  MOB source,
                                  Environmental target,
                                  MOB monster,
                                  Item primaryItem,
                                  Item secondaryItem,
                                  Vector script,
                                  int ticks,
                                  String msg)
        {
            h=host;
            s=source;
            t=target;
            m=monster;
            pi=primaryItem;
            si=secondaryItem;
            scr=script;
            tickDelay=ticks;
            message=msg;
        }

        public boolean checkTimeToExecute() { return ((--tickDelay)<=0); }
    }
    
    public static final String[] progs={
        "GREET_PROG", //1
        "ALL_GREET_PROG", //2
        "SPEECH_PROG", //3
        "GIVE_PROG", //4
        "RAND_PROG", //5
        "ONCE_PROG", //6
        "FIGHT_PROG", //7
        "ENTRY_PROG", //8
        "EXIT_PROG", //9
        "DEATH_PROG", //10
        "HITPRCNT_PROG", //11
        "MASK_PROG", //12
        "QUEST_TIME_PROG", // 13
        "TIME_PROG", // 14
        "DAY_PROG", // 15
        "DELAY_PROG", // 16
        "FUNCTION_PROG", // 17
        "ACT_PROG", // 18
        "BRIBE_PROG", // 19
        "GET_PROG", // 20
        "PUT_PROG", // 21
        "DROP_PROG", // 22
        "WEAR_PROG", // 23
        "REMOVE_PROG", // 24
        "CONSUME_PROG", // 25
        "DAMAGE_PROG", // 26
        "BUY_PROG", // 27
        "SELL_PROG", // 28
        "LOGIN_PROG", // 29
        "LOGOFF_PROG", // 30
        "REGMASK_PROG", // 31
        "LEVEL_PROG", // 32
        "CHANNEL_PROG", // 33
        "OPEN_PROG", // 34
        "CLOSE_PROG", // 35
        "LOCK_PROG", // 36
        "UNLOCK_PROG", // 37
        "SOCIAL_PROG", // 38
        "LOOK_PROG", // 39
        "LLOOK_PROG", // 40
        "EXECMSG_PROG", // 41
        "CNCLMSG_PROG", // 42
    };
    
    public static final String[] funcs={
        "RAND", //1
        "HAS", //2
        "WORN", //3
        "ISNPC", //4
        "ISPC", //5
        "ISGOOD", //6
        "ISNAME", //7
        "ISEVIL", //8
        "ISNEUTRAL", //9
        "ISFIGHT", //10
        "ISIMMORT", //11
        "ISCHARMED", //12
        "STAT", //13
        "AFFECTED", //14
        "ISFOLLOW", //15
        "HITPRCNT", //16
        "INROOM", //17
        "SEX", //18
        "POSITION", //19
        "LEVEL", //20
        "CLASS", //21
        "BASECLASS", //22
        "RACE", //23
        "RACECAT", //24
        "GOLDAMT", //25
        "OBJTYPE", // 26
        "VAR", // 27
        "QUESTWINNER", //28
        "QUESTMOB", // 29
        "QUESTOBJ", // 30
        "ISQUESTMOBALIVE", // 31
        "NUMMOBSINAREA", // 32
        "NUMMOBS", // 33
        "NUMRACESINAREA", // 34
        "NUMRACES", // 35
        "ISHERE", // 36
        "INLOCALE", // 37
        "ISTIME", // 38
        "ISDAY", // 39
        "NUMBER", // 40
        "EVAL", // 41
        "RANDNUM", // 42
        "ROOMMOB", // 43
        "ROOMITEM", // 44
        "NUMMOBSROOM", // 45
        "NUMITEMSROOM", // 46
        "MOBITEM", // 47
        "NUMITEMSMOB", // 48
        "HASTATTOO", // 49
        "ISSEASON", // 50
        "ISWEATHER", // 51
        "GSTAT", // 52
        "INCONTAINER", //53
        "ISALIVE", // 54
        "ISPKILL", // 55
        "NAME", // 56
        "ISMOON", // 57
        "ISABLE", // 58
        "ISOPEN", // 59
        "ISLOCKED", // 60
        "STRIN", // 61 
        "CALLFUNC", // 62
        "NUMPCSROOM", // 63
        "DEITY", // 64
        "CLAN", // 65
        "CLANRANK", // 66
        "HASTITLE", // 67
        "CLANDATA", // 68
        "ISBEHAVE", // 69
        "IPADDRESS", // 70
        "RAND0NUM", // 71
        "FACTION", //72
        "ISSERVANT", // 73
        "HASNUM", // 74
        "CURRENCY", // 75
        "VALUE", // 76
        "EXPLORED", // 77
        "EXP", // 78
        "NUMPCSAREA", // 79
        "QUESTPOINTS", // 80
        "TRAINS", // 81
        "PRACS", // 82
        "QVAR", // 83
        "MATH", // 84
    };
    public static final String[] methods={
        "MPASOUND", //1
        "MPECHO", //2
        "MPSLAY", //3
        "MPJUNK", //4
        "MPMLOAD", //5
        "MPOLOAD", //6
        "MPECHOAT", //7
        "MPECHOAROUND", //8
        "MPCAST", //9
        "MPKILL", //10
        "MPEXP", //11
        "MPPURGE", //12
        "MPUNAFFECT", //13
        "MPGOTO", //14
        "MPAT", //15
        "MPSET", //16
        "MPTRANSFER", //17
        "MPFORCE", //18
        "IF", //19
        "MPSETVAR", //20
        "MPENDQUEST",//21
        "MPQUESTWIN", //22
        "MPSTARTQUEST", //23
        "MPCALLFUNC", // 24
        "MPBEACON", // 25
        "MPALARM", // 26
        "MPWHILE", // 27
        "MPDAMAGE", // 28
        "MPTRACKTO", // 29
        "MPAFFECT", // 30
        "MPBEHAVE", // 31
        "MPUNBEHAVE",  //32
        "MPTATTOO", // 33
        "BREAK", // 34
        "MPGSET", // 35
        "MPSAVEVAR", // 36
        "MPENABLE", // 37
        "MPDISABLE", // 38
        "MPLOADVAR", // 39
        "MPM2I2M", // 40
        "MPOLOADROOM", // 41
        "MPHIDE", // 42
        "MPUNHIDE", // 43
        "MPOPEN", // 44
        "MPCLOSE", // 45
        "MPLOCK", // 46
        "MPUNLOCK", // 47
        "RETURN", // 48
        "MPTITLE", // 49
        "BREAK", // 50
        "MPSETCLANDATA", // 51
        "MPPLAYERCLASS", // 52
        "MPWALKTO", // 53
        "MPFACTION", //54 
        "MPNOTRIGGER", // 55
        "MPSTOP", // 56
        "<SCRIPT>", // 57
        "MPRESET", // 58
        "MPQUESTPOINTS", // 59
        "MPTRAINS", // 60
        "MPPRACS", // 61
        "FOR", // 62
        "MPARGSET", // 63
        "MPLOADQUESTOBJ", // 64
        "MPQSET" // 65
    };

    public final static String[] clanVars={
        "ACCEPTANCE", // 0
        "DETAIL", // 1
        "DONATEROOM", // 2
        "EXP", // 3
        "GOVT", // 4
        "MORGUE", // 5
        "POLITICS", // 6
        "PREMISE", // 7
        "RECALL", // 8
        "SIZE", // 9
        "STATUS", // 10
        "TAXES", // 11
        "TROPHIES", // 12
        "TYPE", // 13
        "AREAS", // 14
        "MEMBERLIST", // 15
        "TOPMEMBER" // 16
    };


}
