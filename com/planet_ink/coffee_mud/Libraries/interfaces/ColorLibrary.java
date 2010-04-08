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
public interface ColorLibrary extends CMLibrary
{
    public static final String COLOR_WHITE="\033[1;37m";
    public static final String COLOR_LIGHTGREEN="\033[1;32m";
    public static final String COLOR_LIGHTBLUE="\033[1;34m";
    public static final String COLOR_LIGHTRED="\033[1;31m";
    public static final String COLOR_YELLOW="\033[1;33m";
    public static final String COLOR_LIGHTCYAN="\033[1;36m";
    public static final String COLOR_LIGHTPURPLE="\033[1;35m";
    public static final String COLOR_GREY="\033[0;37m";
    public static final String COLOR_GREEN="\033[0;32m";
    public static final String COLOR_BLUE="\033[0;34m";
    public static final String COLOR_RED="\033[0;31m";
    public static final String COLOR_BROWN="\033[0;33m";
    public static final String COLOR_CYAN="\033[0;36m";
    public static final String COLOR_PURPLE="\033[0;35m";
    public static final String COLOR_DARKGREY="\033[1;30m";
    public static final String COLOR_BLACK="\033[0;30m";
    public static final String COLOR_NONE="\033[0;0m";
    public static final String COLOR_BOLD="\033[1m";
    public static final String COLOR_UNDERLINE="\033[4m";
    public static final String COLOR_BLINK="\033[5m";
    public static final String COLOR_ITALICS="\033[6m";
    public static final String COLOR_BGWHITE="\033[47m";
    public static final String COLOR_BGGREEN="\033[42m";
    public static final String COLOR_BGBLUE="\033[44m";
    public static final String COLOR_BGRED="\033[41m";
    public static final String COLOR_BGYELLOW="\033[43m";
    public static final String COLOR_BGCYAN="\033[46m";
    public static final String COLOR_BGPURPLE="\033[45m";
    public static final String COLOR_BGBLACK="\033[40m";
    public static final String COLOR_BGDEFAULT="\033[49m";
    
    public static final String HTTAG_WHITE="<FONT COLOR=WHITE";
    public static final String HTTAG_LIGHTGREEN="<FONT COLOR=LIGHTGREEN";
    public static final String HTTAG_LIGHTBLUE="<FONT COLOR=BLUE";
    public static final String HTTAG_LIGHTRED="<FONT COLOR=RED";
    public static final String HTTAG_YELLOW="<FONT COLOR=YELLOW";
    public static final String HTTAG_LIGHTCYAN="<FONT COLOR=CYAN";
    public static final String HTTAG_LIGHTPURPLE="<FONT COLOR=VIOLET";
    public static final String HTTAG_GREY="<FONT COLOR=LIGHTGREY";
    public static final String HTTAG_GREEN="<FONT COLOR=GREEN";
    public static final String HTTAG_BLUE="<FONT COLOR=#000099";
    public static final String HTTAG_RED="<FONT COLOR=#993300";
    public static final String HTTAG_BROWN="<FONT COLOR=#999966";
    public static final String HTTAG_CYAN="<FONT COLOR=DARKCYAN";
    public static final String HTTAG_PURPLE="<FONT COLOR=PURPLE";
    public static final String HTTAG_DARKGREY="<FONT COLOR=GRAY";
    public static final String HTTAG_BLACK="<FONT COLOR=BLACK";
    public static final String HTTAG_NONE="</I></U></BLINK></B></FONT";
    public static final String HTTAG_BOLD="<B";
    public static final String HTTAG_UNDERLINE="<U";
    public static final String HTTAG_BLINK="<BLINK";
    public static final String HTTAG_ITALICS="<I";
    public static final String HTTAG_BGWHITE=" style=\"background-color: white\"";
    public static final String HTTAG_BGGREEN=" style=\"background-color: green\"";
    public static final String HTTAG_BGBLUE=" style=\"background-color: #000099\"";
    public static final String HTTAG_BGRED=" style=\"background-color: #993300\"";
    public static final String HTTAG_BGYELLOW=" style=\"background-color: #999966\"";
    public static final String HTTAG_BGCYAN=" style=\"background-color: darkcyan\"";
    public static final String HTTAG_BGPURPLE=" style=\"background-color: purple\"";
    public static final String HTTAG_BGBLACK=" style=\"background-color: black\"";
    public static final String HTTAG_BGDEFAULT=" style=\"background-color: white\"";
    
    public static final String[] COLOR_CODELETTERSINCARDINALORDER={
        "k","r","g","y","b","p","c","w",null,null
    };
    public static final String[] COLOR_ALLCOLORS={
        COLOR_WHITE,COLOR_LIGHTGREEN,COLOR_LIGHTBLUE,COLOR_LIGHTRED,
        COLOR_YELLOW,COLOR_LIGHTCYAN,COLOR_LIGHTPURPLE,COLOR_GREY,
        COLOR_GREEN,COLOR_BLUE,COLOR_RED,COLOR_BROWN,
        COLOR_CYAN,COLOR_PURPLE,COLOR_DARKGREY,COLOR_BLACK,COLOR_NONE,
        COLOR_BOLD,COLOR_UNDERLINE,COLOR_BLINK,COLOR_ITALICS,
        COLOR_BGWHITE,COLOR_BGGREEN,COLOR_BGBLUE,COLOR_BGRED,
        COLOR_BGYELLOW,COLOR_BGCYAN,COLOR_BGPURPLE,COLOR_BGBLACK,
        COLOR_BGDEFAULT,
        
    };
    public static final String[] COLOR_ALLHTTAGS={
        HTTAG_WHITE,HTTAG_LIGHTGREEN,HTTAG_LIGHTBLUE,HTTAG_LIGHTRED,
        HTTAG_YELLOW,HTTAG_LIGHTCYAN,HTTAG_LIGHTPURPLE,HTTAG_GREY,
        HTTAG_GREEN,HTTAG_BLUE,HTTAG_RED,HTTAG_BROWN,
        HTTAG_CYAN,HTTAG_PURPLE,HTTAG_DARKGREY,HTTAG_BLACK,HTTAG_NONE,
        HTTAG_BOLD,HTTAG_UNDERLINE,HTTAG_BLINK,HTTAG_ITALICS,
        HTTAG_BGWHITE,HTTAG_BGGREEN,HTTAG_BGBLUE,HTTAG_BGRED,
        HTTAG_BGYELLOW,HTTAG_BGCYAN,HTTAG_BGPURPLE,HTTAG_BGBLACK,
        HTTAG_BGDEFAULT
    };
    public static final String[] COLOR_ALLCOLORNAMES={
        "WHITE","LIGHTGREEN","LIGHTBLUE","LIGHTRED",
        "YELLOW","LIGHTCYAN","LIGHTPURPLE","GREY",
        "GREEN","BLUE","RED","BROWN",
        "CYAN","PURPLE","DARKGREY","BLACK","NONE",
        "BOLD","UNDERLINE","BLINK","ITALICS",
        "BGWHITE","BGGREEN","BGBLUE","BGRED",
        "BGYELLOW","BGCYAN","BGPURPLE","BGBLACK",
        "BGDEFAULT"
    };
    
    public static final String[] COLOR_ALLNORMALCOLORCODELETTERS={
        "w","g","b","r",
        "y","c","p","W",
        "G","B","R","Y",
        "C","P","k"
    };
    public static final String[] COLOR_ALLEXTENDEDCOLORCODELETTERS={
        "w","g","b","r",
        "y","c","p","W",
        "G","B","R","Y",
        "C","P","k","K"
    };
    //remaining=aijlnoszAJV
    public static final char COLORCODE_YOU_FIGHT='f';
    public static final char COLORCODE_FIGHT_YOU='e';
    public static final char COLORCODE_FIGHT='F';
    public static final char COLORCODE_SPELL='S';
    public static final char COLORCODE_EMOTE='E';
    public static final char COLORCODE_WEATHER='J';
    public static final char COLORCODE_TALK='T';
    public static final char COLORCODE_TELL='t';
    public static final char COLORCODE_CHANNEL='Q';
    public static final char COLORCODE_CHANNELFORE='q';
    public static final char COLORCODE_IMPORTANT1='x';
    public static final char COLORCODE_IMPORTANT2='X';
    public static final char COLORCODE_IMPORTANT3='Z';
    public static final char COLORCODE_ROOMTITLE='O';
    public static final char COLORCODE_ROOMDESC='L';
    public static final char COLORCODE_DIRECTION='D';
    public static final char COLORCODE_DOORDESC='d';
    public static final char COLORCODE_ITEM='I';
    public static final char COLORCODE_MOB='M';
    public static final char COLORCODE_HITPOINTS='h';
    public static final char COLORCODE_MANA='m';
    public static final char COLORCODE_MOVES='v';
    public static final char COLORCODE_NORMAL='N';
    public static final char COLORCODE_HIGHLIGHT='H';
    public static final char COLORCODE_UNEXPDIRECTION='U';
    public static final char COLORCODE_UNEXPDOORDESC='u';
    public static final char[] COLORCODE_ALLCODES={
        COLORCODE_YOU_FIGHT,COLORCODE_FIGHT_YOU,COLORCODE_FIGHT,COLORCODE_SPELL,
        COLORCODE_EMOTE,COLORCODE_TALK,COLORCODE_TELL,COLORCODE_CHANNEL,
        COLORCODE_CHANNELFORE,COLORCODE_IMPORTANT1,COLORCODE_IMPORTANT2,
        COLORCODE_IMPORTANT3,COLORCODE_ROOMTITLE,COLORCODE_ROOMDESC,
        COLORCODE_DIRECTION,COLORCODE_DOORDESC,COLORCODE_ITEM,COLORCODE_MOB,
        COLORCODE_HITPOINTS,COLORCODE_MANA,COLORCODE_MOVES,COLORCODE_NORMAL,
        COLORCODE_HIGHLIGHT,COLORCODE_UNEXPDIRECTION,COLORCODE_UNEXPDOORDESC,
        COLORCODE_WEATHER
    };
    public static final String[] COLORCODE_ALLCODENAMES={
        "YOU-FIGHT","FIGHT-YOU","FIGHT","SPELL","EMOTE","TALK",
        "TELL","CHANNEL","CHANNELFORE","IMPORTANT1",
        "IMPORTANT2","IMPORTANT3","ROOMTITLE","ROOMDESC",
        "DIRECTION","DOORDESC","ITEM","MOB",
        "HITPOINTS","MANA","MOVES","NORMAL",
        "HIGHLIGHT","UNEXPDIRECTION","UNEXPDOORDESC","WEATHER"
    };

    public static final String COLOR_FR0G0B0="\033[38;5;"+(16+(0*36)+(0*6)+0)+"m";
    public static final String COLOR_FR1G0B0="\033[38;5;"+(16+(1*36)+(0*6)+0)+"m";
    public static final String COLOR_FR2G0B0="\033[38;5;"+(16+(2*36)+(0*6)+0)+"m";
    public static final String COLOR_FR3G0B0="\033[38;5;"+(16+(3*36)+(0*6)+0)+"m";
    public static final String COLOR_FR4G0B0="\033[38;5;"+(16+(4*36)+(0*6)+0)+"m";
    public static final String COLOR_FR5G0B0="\033[38;5;"+(16+(5*36)+(0*6)+0)+"m";
    public static final String COLOR_FR0G1B0="\033[38;5;"+(16+(0*36)+(1*6)+0)+"m";
    public static final String COLOR_FR1G1B0="\033[38;5;"+(16+(1*36)+(1*6)+0)+"m";
    public static final String COLOR_FR2G1B0="\033[38;5;"+(16+(2*36)+(1*6)+0)+"m";
    public static final String COLOR_FR3G1B0="\033[38;5;"+(16+(3*36)+(1*6)+0)+"m";
    public static final String COLOR_FR4G1B0="\033[38;5;"+(16+(4*36)+(1*6)+0)+"m";
    public static final String COLOR_FR5G1B0="\033[38;5;"+(16+(5*36)+(1*6)+0)+"m";
    public static final String COLOR_FR0G2B0="\033[38;5;"+(16+(0*36)+(2*6)+0)+"m";
    public static final String COLOR_FR1G2B0="\033[38;5;"+(16+(1*36)+(2*6)+0)+"m";
    public static final String COLOR_FR2G2B0="\033[38;5;"+(16+(2*36)+(2*6)+0)+"m";
    public static final String COLOR_FR3G2B0="\033[38;5;"+(16+(3*36)+(2*6)+0)+"m";
    public static final String COLOR_FR4G2B0="\033[38;5;"+(16+(4*36)+(2*6)+0)+"m";
    public static final String COLOR_FR5G2B0="\033[38;5;"+(16+(5*36)+(2*6)+0)+"m";
    public static final String COLOR_FR0G3B0="\033[38;5;"+(16+(0*36)+(3*6)+0)+"m";
    public static final String COLOR_FR1G3B0="\033[38;5;"+(16+(1*36)+(3*6)+0)+"m";
    public static final String COLOR_FR2G3B0="\033[38;5;"+(16+(2*36)+(3*6)+0)+"m";
    public static final String COLOR_FR3G3B0="\033[38;5;"+(16+(3*36)+(3*6)+0)+"m";
    public static final String COLOR_FR4G3B0="\033[38;5;"+(16+(4*36)+(3*6)+0)+"m";
    public static final String COLOR_FR5G3B0="\033[38;5;"+(16+(5*36)+(3*6)+0)+"m";
    public static final String COLOR_FR0G4B0="\033[38;5;"+(16+(0*36)+(4*6)+0)+"m";
    public static final String COLOR_FR1G4B0="\033[38;5;"+(16+(1*36)+(4*6)+0)+"m";
    public static final String COLOR_FR2G4B0="\033[38;5;"+(16+(2*36)+(4*6)+0)+"m";
    public static final String COLOR_FR3G4B0="\033[38;5;"+(16+(3*36)+(4*6)+0)+"m";
    public static final String COLOR_FR4G4B0="\033[38;5;"+(16+(4*36)+(4*6)+0)+"m";
    public static final String COLOR_FR5G4B0="\033[38;5;"+(16+(5*36)+(4*6)+0)+"m";
    public static final String COLOR_FR0G5B0="\033[38;5;"+(16+(0*36)+(5*6)+0)+"m";
    public static final String COLOR_FR1G5B0="\033[38;5;"+(16+(1*36)+(5*6)+0)+"m";
    public static final String COLOR_FR2G5B0="\033[38;5;"+(16+(2*36)+(5*6)+0)+"m";
    public static final String COLOR_FR3G5B0="\033[38;5;"+(16+(3*36)+(5*6)+0)+"m";
    public static final String COLOR_FR4G5B0="\033[38;5;"+(16+(4*36)+(5*6)+0)+"m";
    public static final String COLOR_FR5G5B0="\033[38;5;"+(16+(4*36)+(5*6)+0)+"m";
    public static final String COLOR_FR0G0B1="\033[38;5;"+(16+(0*36)+(0*6)+1)+"m";
    public static final String COLOR_FR1G0B1="\033[38;5;"+(16+(1*36)+(0*6)+1)+"m";
    public static final String COLOR_FR2G0B1="\033[38;5;"+(16+(2*36)+(0*6)+1)+"m";
    public static final String COLOR_FR3G0B1="\033[38;5;"+(16+(3*36)+(0*6)+1)+"m";
    public static final String COLOR_FR4G0B1="\033[38;5;"+(16+(4*36)+(0*6)+1)+"m";
    public static final String COLOR_FR5G0B1="\033[38;5;"+(16+(5*36)+(0*6)+1)+"m";
    public static final String COLOR_FR0G1B1="\033[38;5;"+(16+(0*36)+(1*6)+1)+"m";
    public static final String COLOR_FR1G1B1="\033[38;5;"+(16+(1*36)+(1*6)+1)+"m";
    public static final String COLOR_FR2G1B1="\033[38;5;"+(16+(2*36)+(1*6)+1)+"m";
    public static final String COLOR_FR3G1B1="\033[38;5;"+(16+(3*36)+(1*6)+1)+"m";
    public static final String COLOR_FR4G1B1="\033[38;5;"+(16+(4*36)+(1*6)+1)+"m";
    public static final String COLOR_FR5G1B1="\033[38;5;"+(16+(5*36)+(1*6)+1)+"m";
    public static final String COLOR_FR0G2B1="\033[38;5;"+(16+(0*36)+(2*6)+1)+"m";
    public static final String COLOR_FR1G2B1="\033[38;5;"+(16+(1*36)+(2*6)+1)+"m";
    public static final String COLOR_FR2G2B1="\033[38;5;"+(16+(2*36)+(2*6)+1)+"m";
    public static final String COLOR_FR3G2B1="\033[38;5;"+(16+(3*36)+(2*6)+1)+"m";
    public static final String COLOR_FR4G2B1="\033[38;5;"+(16+(4*36)+(2*6)+1)+"m";
    public static final String COLOR_FR5G2B1="\033[38;5;"+(16+(5*36)+(2*6)+1)+"m";
    public static final String COLOR_FR0G3B1="\033[38;5;"+(16+(0*36)+(3*6)+1)+"m";
    public static final String COLOR_FR1G3B1="\033[38;5;"+(16+(1*36)+(3*6)+1)+"m";
    public static final String COLOR_FR2G3B1="\033[38;5;"+(16+(2*36)+(3*6)+1)+"m";
    public static final String COLOR_FR3G3B1="\033[38;5;"+(16+(3*36)+(3*6)+1)+"m";
    public static final String COLOR_FR4G3B1="\033[38;5;"+(16+(4*36)+(3*6)+1)+"m";
    public static final String COLOR_FR5G3B1="\033[38;5;"+(16+(5*36)+(3*6)+1)+"m";
    public static final String COLOR_FR0G4B1="\033[38;5;"+(16+(0*36)+(4*6)+1)+"m";
    public static final String COLOR_FR1G4B1="\033[38;5;"+(16+(1*36)+(4*6)+1)+"m";
    public static final String COLOR_FR2G4B1="\033[38;5;"+(16+(2*36)+(4*6)+1)+"m";
    public static final String COLOR_FR3G4B1="\033[38;5;"+(16+(3*36)+(4*6)+1)+"m";
    public static final String COLOR_FR4G4B1="\033[38;5;"+(16+(4*36)+(4*6)+1)+"m";
    public static final String COLOR_FR5G4B1="\033[38;5;"+(16+(5*36)+(4*6)+1)+"m";
    public static final String COLOR_FR0G5B1="\033[38;5;"+(16+(0*36)+(5*6)+1)+"m";
    public static final String COLOR_FR1G5B1="\033[38;5;"+(16+(1*36)+(5*6)+1)+"m";
    public static final String COLOR_FR2G5B1="\033[38;5;"+(16+(2*36)+(5*6)+1)+"m";
    public static final String COLOR_FR3G5B1="\033[38;5;"+(16+(3*36)+(5*6)+1)+"m";
    public static final String COLOR_FR4G5B1="\033[38;5;"+(16+(4*36)+(5*6)+1)+"m";
    public static final String COLOR_FR5G5B1="\033[38;5;"+(16+(4*36)+(5*6)+1)+"m";
    public static final String COLOR_FR0G0B2="\033[38;5;"+(16+(0*36)+(0*6)+2)+"m";
    public static final String COLOR_FR1G0B2="\033[38;5;"+(16+(1*36)+(0*6)+2)+"m";
    public static final String COLOR_FR2G0B2="\033[38;5;"+(16+(2*36)+(0*6)+2)+"m";
    public static final String COLOR_FR3G0B2="\033[38;5;"+(16+(3*36)+(0*6)+2)+"m";
    public static final String COLOR_FR4G0B2="\033[38;5;"+(16+(4*36)+(0*6)+2)+"m";
    public static final String COLOR_FR5G0B2="\033[38;5;"+(16+(5*36)+(0*6)+2)+"m";
    public static final String COLOR_FR0G1B2="\033[38;5;"+(16+(0*36)+(1*6)+2)+"m";
    public static final String COLOR_FR1G1B2="\033[38;5;"+(16+(1*36)+(1*6)+2)+"m";
    public static final String COLOR_FR2G1B2="\033[38;5;"+(16+(2*36)+(1*6)+2)+"m";
    public static final String COLOR_FR3G1B2="\033[38;5;"+(16+(3*36)+(1*6)+2)+"m";
    public static final String COLOR_FR4G1B2="\033[38;5;"+(16+(4*36)+(1*6)+2)+"m";
    public static final String COLOR_FR5G1B2="\033[38;5;"+(16+(5*36)+(1*6)+2)+"m";
    public static final String COLOR_FR0G2B2="\033[38;5;"+(16+(0*36)+(2*6)+2)+"m";
    public static final String COLOR_FR1G2B2="\033[38;5;"+(16+(1*36)+(2*6)+2)+"m";
    public static final String COLOR_FR2G2B2="\033[38;5;"+(16+(2*36)+(2*6)+2)+"m";
    public static final String COLOR_FR3G2B2="\033[38;5;"+(16+(3*36)+(2*6)+2)+"m";
    public static final String COLOR_FR4G2B2="\033[38;5;"+(16+(4*36)+(2*6)+2)+"m";
    public static final String COLOR_FR5G2B2="\033[38;5;"+(16+(5*36)+(2*6)+2)+"m";
    public static final String COLOR_FR0G3B2="\033[38;5;"+(16+(0*36)+(3*6)+2)+"m";
    public static final String COLOR_FR1G3B2="\033[38;5;"+(16+(1*36)+(3*6)+2)+"m";
    public static final String COLOR_FR2G3B2="\033[38;5;"+(16+(2*36)+(3*6)+2)+"m";
    public static final String COLOR_FR3G3B2="\033[38;5;"+(16+(3*36)+(3*6)+2)+"m";
    public static final String COLOR_FR4G3B2="\033[38;5;"+(16+(4*36)+(3*6)+2)+"m";
    public static final String COLOR_FR5G3B2="\033[38;5;"+(16+(5*36)+(3*6)+2)+"m";
    public static final String COLOR_FR0G4B2="\033[38;5;"+(16+(0*36)+(4*6)+2)+"m";
    public static final String COLOR_FR1G4B2="\033[38;5;"+(16+(1*36)+(4*6)+2)+"m";
    public static final String COLOR_FR2G4B2="\033[38;5;"+(16+(2*36)+(4*6)+2)+"m";
    public static final String COLOR_FR3G4B2="\033[38;5;"+(16+(3*36)+(4*6)+2)+"m";
    public static final String COLOR_FR4G4B2="\033[38;5;"+(16+(4*36)+(4*6)+2)+"m";
    public static final String COLOR_FR5G4B2="\033[38;5;"+(16+(5*36)+(4*6)+2)+"m";
    public static final String COLOR_FR0G5B2="\033[38;5;"+(16+(0*36)+(5*6)+2)+"m";
    public static final String COLOR_FR1G5B2="\033[38;5;"+(16+(1*36)+(5*6)+2)+"m";
    public static final String COLOR_FR2G5B2="\033[38;5;"+(16+(2*36)+(5*6)+2)+"m";
    public static final String COLOR_FR3G5B2="\033[38;5;"+(16+(3*36)+(5*6)+2)+"m";
    public static final String COLOR_FR4G5B2="\033[38;5;"+(16+(4*36)+(5*6)+2)+"m";
    public static final String COLOR_FR5G5B2="\033[38;5;"+(16+(4*36)+(5*6)+2)+"m";
    public static final String COLOR_FR0G0B3="\033[38;5;"+(16+(0*36)+(0*6)+3)+"m";
    public static final String COLOR_FR1G0B3="\033[38;5;"+(16+(1*36)+(0*6)+3)+"m";
    public static final String COLOR_FR2G0B3="\033[38;5;"+(16+(2*36)+(0*6)+3)+"m";
    public static final String COLOR_FR3G0B3="\033[38;5;"+(16+(3*36)+(0*6)+3)+"m";
    public static final String COLOR_FR4G0B3="\033[38;5;"+(16+(4*36)+(0*6)+3)+"m";
    public static final String COLOR_FR5G0B3="\033[38;5;"+(16+(5*36)+(0*6)+3)+"m";
    public static final String COLOR_FR0G1B3="\033[38;5;"+(16+(0*36)+(1*6)+3)+"m";
    public static final String COLOR_FR1G1B3="\033[38;5;"+(16+(1*36)+(1*6)+3)+"m";
    public static final String COLOR_FR2G1B3="\033[38;5;"+(16+(2*36)+(1*6)+3)+"m";
    public static final String COLOR_FR3G1B3="\033[38;5;"+(16+(3*36)+(1*6)+3)+"m";
    public static final String COLOR_FR4G1B3="\033[38;5;"+(16+(4*36)+(1*6)+3)+"m";
    public static final String COLOR_FR5G1B3="\033[38;5;"+(16+(5*36)+(1*6)+3)+"m";
    public static final String COLOR_FR0G2B3="\033[38;5;"+(16+(0*36)+(2*6)+3)+"m";
    public static final String COLOR_FR1G2B3="\033[38;5;"+(16+(1*36)+(2*6)+3)+"m";
    public static final String COLOR_FR2G2B3="\033[38;5;"+(16+(2*36)+(2*6)+3)+"m";
    public static final String COLOR_FR3G2B3="\033[38;5;"+(16+(3*36)+(2*6)+3)+"m";
    public static final String COLOR_FR4G2B3="\033[38;5;"+(16+(4*36)+(2*6)+3)+"m";
    public static final String COLOR_FR5G2B3="\033[38;5;"+(16+(5*36)+(2*6)+3)+"m";
    public static final String COLOR_FR0G3B3="\033[38;5;"+(16+(0*36)+(3*6)+3)+"m";
    public static final String COLOR_FR1G3B3="\033[38;5;"+(16+(1*36)+(3*6)+3)+"m";
    public static final String COLOR_FR2G3B3="\033[38;5;"+(16+(2*36)+(3*6)+3)+"m";
    public static final String COLOR_FR3G3B3="\033[38;5;"+(16+(3*36)+(3*6)+3)+"m";
    public static final String COLOR_FR4G3B3="\033[38;5;"+(16+(4*36)+(3*6)+3)+"m";
    public static final String COLOR_FR5G3B3="\033[38;5;"+(16+(5*36)+(3*6)+3)+"m";
    public static final String COLOR_FR0G4B3="\033[38;5;"+(16+(0*36)+(4*6)+3)+"m";
    public static final String COLOR_FR1G4B3="\033[38;5;"+(16+(1*36)+(4*6)+3)+"m";
    public static final String COLOR_FR2G4B3="\033[38;5;"+(16+(2*36)+(4*6)+3)+"m";
    public static final String COLOR_FR3G4B3="\033[38;5;"+(16+(3*36)+(4*6)+3)+"m";
    public static final String COLOR_FR4G4B3="\033[38;5;"+(16+(4*36)+(4*6)+3)+"m";
    public static final String COLOR_FR5G4B3="\033[38;5;"+(16+(5*36)+(4*6)+3)+"m";
    public static final String COLOR_FR0G5B3="\033[38;5;"+(16+(0*36)+(5*6)+3)+"m";
    public static final String COLOR_FR1G5B3="\033[38;5;"+(16+(1*36)+(5*6)+3)+"m";
    public static final String COLOR_FR2G5B3="\033[38;5;"+(16+(2*36)+(5*6)+3)+"m";
    public static final String COLOR_FR3G5B3="\033[38;5;"+(16+(3*36)+(5*6)+3)+"m";
    public static final String COLOR_FR4G5B3="\033[38;5;"+(16+(4*36)+(5*6)+3)+"m";
    public static final String COLOR_FR5G5B3="\033[38;5;"+(16+(4*36)+(5*6)+3)+"m";
    public static final String COLOR_FR0G0B4="\033[38;5;"+(16+(0*36)+(0*6)+4)+"m";
    public static final String COLOR_FR1G0B4="\033[38;5;"+(16+(1*36)+(0*6)+4)+"m";
    public static final String COLOR_FR2G0B4="\033[38;5;"+(16+(2*36)+(0*6)+4)+"m";
    public static final String COLOR_FR3G0B4="\033[38;5;"+(16+(3*36)+(0*6)+4)+"m";
    public static final String COLOR_FR4G0B4="\033[38;5;"+(16+(4*36)+(0*6)+4)+"m";
    public static final String COLOR_FR5G0B4="\033[38;5;"+(16+(5*36)+(0*6)+4)+"m";
    public static final String COLOR_FR0G1B4="\033[38;5;"+(16+(0*36)+(1*6)+4)+"m";
    public static final String COLOR_FR1G1B4="\033[38;5;"+(16+(1*36)+(1*6)+4)+"m";
    public static final String COLOR_FR2G1B4="\033[38;5;"+(16+(2*36)+(1*6)+4)+"m";
    public static final String COLOR_FR3G1B4="\033[38;5;"+(16+(3*36)+(1*6)+4)+"m";
    public static final String COLOR_FR4G1B4="\033[38;5;"+(16+(4*36)+(1*6)+4)+"m";
    public static final String COLOR_FR5G1B4="\033[38;5;"+(16+(5*36)+(1*6)+4)+"m";
    public static final String COLOR_FR0G2B4="\033[38;5;"+(16+(0*36)+(2*6)+4)+"m";
    public static final String COLOR_FR1G2B4="\033[38;5;"+(16+(1*36)+(2*6)+4)+"m";
    public static final String COLOR_FR2G2B4="\033[38;5;"+(16+(2*36)+(2*6)+4)+"m";
    public static final String COLOR_FR3G2B4="\033[38;5;"+(16+(3*36)+(2*6)+4)+"m";
    public static final String COLOR_FR4G2B4="\033[38;5;"+(16+(4*36)+(2*6)+4)+"m";
    public static final String COLOR_FR5G2B4="\033[38;5;"+(16+(5*36)+(2*6)+4)+"m";
    public static final String COLOR_FR0G3B4="\033[38;5;"+(16+(0*36)+(3*6)+4)+"m";
    public static final String COLOR_FR1G3B4="\033[38;5;"+(16+(1*36)+(3*6)+4)+"m";
    public static final String COLOR_FR2G3B4="\033[38;5;"+(16+(2*36)+(3*6)+4)+"m";
    public static final String COLOR_FR3G3B4="\033[38;5;"+(16+(3*36)+(3*6)+4)+"m";
    public static final String COLOR_FR4G3B4="\033[38;5;"+(16+(4*36)+(3*6)+4)+"m";
    public static final String COLOR_FR5G3B4="\033[38;5;"+(16+(5*36)+(3*6)+4)+"m";
    public static final String COLOR_FR0G4B4="\033[38;5;"+(16+(0*36)+(4*6)+4)+"m";
    public static final String COLOR_FR1G4B4="\033[38;5;"+(16+(1*36)+(4*6)+4)+"m";
    public static final String COLOR_FR2G4B4="\033[38;5;"+(16+(2*36)+(4*6)+4)+"m";
    public static final String COLOR_FR3G4B4="\033[38;5;"+(16+(3*36)+(4*6)+4)+"m";
    public static final String COLOR_FR4G4B4="\033[38;5;"+(16+(4*36)+(4*6)+4)+"m";
    public static final String COLOR_FR5G4B4="\033[38;5;"+(16+(5*36)+(4*6)+4)+"m";
    public static final String COLOR_FR0G5B4="\033[38;5;"+(16+(0*36)+(5*6)+4)+"m";
    public static final String COLOR_FR1G5B4="\033[38;5;"+(16+(1*36)+(5*6)+4)+"m";
    public static final String COLOR_FR2G5B4="\033[38;5;"+(16+(2*36)+(5*6)+4)+"m";
    public static final String COLOR_FR3G5B4="\033[38;5;"+(16+(3*36)+(5*6)+4)+"m";
    public static final String COLOR_FR4G5B4="\033[38;5;"+(16+(4*36)+(5*6)+4)+"m";
    public static final String COLOR_FR5G5B4="\033[38;5;"+(16+(4*36)+(5*6)+4)+"m";
    public static final String COLOR_FR0G0B5="\033[38;5;"+(16+(0*36)+(0*6)+5)+"m";
    public static final String COLOR_FR1G0B5="\033[38;5;"+(16+(1*36)+(0*6)+5)+"m";
    public static final String COLOR_FR2G0B5="\033[38;5;"+(16+(2*36)+(0*6)+5)+"m";
    public static final String COLOR_FR3G0B5="\033[38;5;"+(16+(3*36)+(0*6)+5)+"m";
    public static final String COLOR_FR4G0B5="\033[38;5;"+(16+(4*36)+(0*6)+5)+"m";
    public static final String COLOR_FR5G0B5="\033[38;5;"+(16+(5*36)+(0*6)+5)+"m";
    public static final String COLOR_FR0G1B5="\033[38;5;"+(16+(0*36)+(1*6)+5)+"m";
    public static final String COLOR_FR1G1B5="\033[38;5;"+(16+(1*36)+(1*6)+5)+"m";
    public static final String COLOR_FR2G1B5="\033[38;5;"+(16+(2*36)+(1*6)+5)+"m";
    public static final String COLOR_FR3G1B5="\033[38;5;"+(16+(3*36)+(1*6)+5)+"m";
    public static final String COLOR_FR4G1B5="\033[38;5;"+(16+(4*36)+(1*6)+5)+"m";
    public static final String COLOR_FR5G1B5="\033[38;5;"+(16+(5*36)+(1*6)+5)+"m";
    public static final String COLOR_FR0G2B5="\033[38;5;"+(16+(0*36)+(2*6)+5)+"m";
    public static final String COLOR_FR1G2B5="\033[38;5;"+(16+(1*36)+(2*6)+5)+"m";
    public static final String COLOR_FR2G2B5="\033[38;5;"+(16+(2*36)+(2*6)+5)+"m";
    public static final String COLOR_FR3G2B5="\033[38;5;"+(16+(3*36)+(2*6)+5)+"m";
    public static final String COLOR_FR4G2B5="\033[38;5;"+(16+(4*36)+(2*6)+5)+"m";
    public static final String COLOR_FR5G2B5="\033[38;5;"+(16+(5*36)+(2*6)+5)+"m";
    public static final String COLOR_FR0G3B5="\033[38;5;"+(16+(0*36)+(3*6)+5)+"m";
    public static final String COLOR_FR1G3B5="\033[38;5;"+(16+(1*36)+(3*6)+5)+"m";
    public static final String COLOR_FR2G3B5="\033[38;5;"+(16+(2*36)+(3*6)+5)+"m";
    public static final String COLOR_FR3G3B5="\033[38;5;"+(16+(3*36)+(3*6)+5)+"m";
    public static final String COLOR_FR4G3B5="\033[38;5;"+(16+(4*36)+(3*6)+5)+"m";
    public static final String COLOR_FR5G3B5="\033[38;5;"+(16+(5*36)+(3*6)+5)+"m";
    public static final String COLOR_FR0G4B5="\033[38;5;"+(16+(0*36)+(4*6)+5)+"m";
    public static final String COLOR_FR1G4B5="\033[38;5;"+(16+(1*36)+(4*6)+5)+"m";
    public static final String COLOR_FR2G4B5="\033[38;5;"+(16+(2*36)+(4*6)+5)+"m";
    public static final String COLOR_FR3G4B5="\033[38;5;"+(16+(3*36)+(4*6)+5)+"m";
    public static final String COLOR_FR4G4B5="\033[38;5;"+(16+(4*36)+(4*6)+5)+"m";
    public static final String COLOR_FR5G4B5="\033[38;5;"+(16+(5*36)+(4*6)+5)+"m";
    public static final String COLOR_FR0G5B5="\033[38;5;"+(16+(0*36)+(5*6)+5)+"m";
    public static final String COLOR_FR1G5B5="\033[38;5;"+(16+(1*36)+(5*6)+5)+"m";
    public static final String COLOR_FR2G5B5="\033[38;5;"+(16+(2*36)+(5*6)+5)+"m";
    public static final String COLOR_FR3G5B5="\033[38;5;"+(16+(3*36)+(5*6)+5)+"m";
    public static final String COLOR_FR4G5B5="\033[38;5;"+(16+(4*36)+(5*6)+5)+"m";
    public static final String COLOR_FR5G5B5="\033[38;5;"+(16+(4*36)+(5*6)+5)+"m";
    public static final String COLOR_BR0G0B0="\033[48;5;"+(16+(0*36)+(0*6)+0)+"m";
    public static final String COLOR_BR1G0B0="\033[48;5;"+(16+(1*36)+(0*6)+0)+"m";
    public static final String COLOR_BR2G0B0="\033[48;5;"+(16+(2*36)+(0*6)+0)+"m";
    public static final String COLOR_BR3G0B0="\033[48;5;"+(16+(3*36)+(0*6)+0)+"m";
    public static final String COLOR_BR4G0B0="\033[48;5;"+(16+(4*36)+(0*6)+0)+"m";
    public static final String COLOR_BR5G0B0="\033[48;5;"+(16+(5*36)+(0*6)+0)+"m";
    public static final String COLOR_BR0G1B0="\033[48;5;"+(16+(0*36)+(1*6)+0)+"m";
    public static final String COLOR_BR1G1B0="\033[48;5;"+(16+(1*36)+(1*6)+0)+"m";
    public static final String COLOR_BR2G1B0="\033[48;5;"+(16+(2*36)+(1*6)+0)+"m";
    public static final String COLOR_BR3G1B0="\033[48;5;"+(16+(3*36)+(1*6)+0)+"m";
    public static final String COLOR_BR4G1B0="\033[48;5;"+(16+(4*36)+(1*6)+0)+"m";
    public static final String COLOR_BR5G1B0="\033[48;5;"+(16+(5*36)+(1*6)+0)+"m";
    public static final String COLOR_BR0G2B0="\033[48;5;"+(16+(0*36)+(2*6)+0)+"m";
    public static final String COLOR_BR1G2B0="\033[48;5;"+(16+(1*36)+(2*6)+0)+"m";
    public static final String COLOR_BR2G2B0="\033[48;5;"+(16+(2*36)+(2*6)+0)+"m";
    public static final String COLOR_BR3G2B0="\033[48;5;"+(16+(3*36)+(2*6)+0)+"m";
    public static final String COLOR_BR4G2B0="\033[48;5;"+(16+(4*36)+(2*6)+0)+"m";
    public static final String COLOR_BR5G2B0="\033[48;5;"+(16+(5*36)+(2*6)+0)+"m";
    public static final String COLOR_BR0G3B0="\033[48;5;"+(16+(0*36)+(3*6)+0)+"m";
    public static final String COLOR_BR1G3B0="\033[48;5;"+(16+(1*36)+(3*6)+0)+"m";
    public static final String COLOR_BR2G3B0="\033[48;5;"+(16+(2*36)+(3*6)+0)+"m";
    public static final String COLOR_BR3G3B0="\033[48;5;"+(16+(3*36)+(3*6)+0)+"m";
    public static final String COLOR_BR4G3B0="\033[48;5;"+(16+(4*36)+(3*6)+0)+"m";
    public static final String COLOR_BR5G3B0="\033[48;5;"+(16+(5*36)+(3*6)+0)+"m";
    public static final String COLOR_BR0G4B0="\033[48;5;"+(16+(0*36)+(4*6)+0)+"m";
    public static final String COLOR_BR1G4B0="\033[48;5;"+(16+(1*36)+(4*6)+0)+"m";
    public static final String COLOR_BR2G4B0="\033[48;5;"+(16+(2*36)+(4*6)+0)+"m";
    public static final String COLOR_BR3G4B0="\033[48;5;"+(16+(3*36)+(4*6)+0)+"m";
    public static final String COLOR_BR4G4B0="\033[48;5;"+(16+(4*36)+(4*6)+0)+"m";
    public static final String COLOR_BR5G4B0="\033[48;5;"+(16+(5*36)+(4*6)+0)+"m";
    public static final String COLOR_BR0G5B0="\033[48;5;"+(16+(0*36)+(5*6)+0)+"m";
    public static final String COLOR_BR1G5B0="\033[48;5;"+(16+(1*36)+(5*6)+0)+"m";
    public static final String COLOR_BR2G5B0="\033[48;5;"+(16+(2*36)+(5*6)+0)+"m";
    public static final String COLOR_BR3G5B0="\033[48;5;"+(16+(3*36)+(5*6)+0)+"m";
    public static final String COLOR_BR4G5B0="\033[48;5;"+(16+(4*36)+(5*6)+0)+"m";
    public static final String COLOR_BR5G5B0="\033[48;5;"+(16+(4*36)+(5*6)+0)+"m";
    public static final String COLOR_BR0G0B1="\033[48;5;"+(16+(0*36)+(0*6)+1)+"m";
    public static final String COLOR_BR1G0B1="\033[48;5;"+(16+(1*36)+(0*6)+1)+"m";
    public static final String COLOR_BR2G0B1="\033[48;5;"+(16+(2*36)+(0*6)+1)+"m";
    public static final String COLOR_BR3G0B1="\033[48;5;"+(16+(3*36)+(0*6)+1)+"m";
    public static final String COLOR_BR4G0B1="\033[48;5;"+(16+(4*36)+(0*6)+1)+"m";
    public static final String COLOR_BR5G0B1="\033[48;5;"+(16+(5*36)+(0*6)+1)+"m";
    public static final String COLOR_BR0G1B1="\033[48;5;"+(16+(0*36)+(1*6)+1)+"m";
    public static final String COLOR_BR1G1B1="\033[48;5;"+(16+(1*36)+(1*6)+1)+"m";
    public static final String COLOR_BR2G1B1="\033[48;5;"+(16+(2*36)+(1*6)+1)+"m";
    public static final String COLOR_BR3G1B1="\033[48;5;"+(16+(3*36)+(1*6)+1)+"m";
    public static final String COLOR_BR4G1B1="\033[48;5;"+(16+(4*36)+(1*6)+1)+"m";
    public static final String COLOR_BR5G1B1="\033[48;5;"+(16+(5*36)+(1*6)+1)+"m";
    public static final String COLOR_BR0G2B1="\033[48;5;"+(16+(0*36)+(2*6)+1)+"m";
    public static final String COLOR_BR1G2B1="\033[48;5;"+(16+(1*36)+(2*6)+1)+"m";
    public static final String COLOR_BR2G2B1="\033[48;5;"+(16+(2*36)+(2*6)+1)+"m";
    public static final String COLOR_BR3G2B1="\033[48;5;"+(16+(3*36)+(2*6)+1)+"m";
    public static final String COLOR_BR4G2B1="\033[48;5;"+(16+(4*36)+(2*6)+1)+"m";
    public static final String COLOR_BR5G2B1="\033[48;5;"+(16+(5*36)+(2*6)+1)+"m";
    public static final String COLOR_BR0G3B1="\033[48;5;"+(16+(0*36)+(3*6)+1)+"m";
    public static final String COLOR_BR1G3B1="\033[48;5;"+(16+(1*36)+(3*6)+1)+"m";
    public static final String COLOR_BR2G3B1="\033[48;5;"+(16+(2*36)+(3*6)+1)+"m";
    public static final String COLOR_BR3G3B1="\033[48;5;"+(16+(3*36)+(3*6)+1)+"m";
    public static final String COLOR_BR4G3B1="\033[48;5;"+(16+(4*36)+(3*6)+1)+"m";
    public static final String COLOR_BR5G3B1="\033[48;5;"+(16+(5*36)+(3*6)+1)+"m";
    public static final String COLOR_BR0G4B1="\033[48;5;"+(16+(0*36)+(4*6)+1)+"m";
    public static final String COLOR_BR1G4B1="\033[48;5;"+(16+(1*36)+(4*6)+1)+"m";
    public static final String COLOR_BR2G4B1="\033[48;5;"+(16+(2*36)+(4*6)+1)+"m";
    public static final String COLOR_BR3G4B1="\033[48;5;"+(16+(3*36)+(4*6)+1)+"m";
    public static final String COLOR_BR4G4B1="\033[48;5;"+(16+(4*36)+(4*6)+1)+"m";
    public static final String COLOR_BR5G4B1="\033[48;5;"+(16+(5*36)+(4*6)+1)+"m";
    public static final String COLOR_BR0G5B1="\033[48;5;"+(16+(0*36)+(5*6)+1)+"m";
    public static final String COLOR_BR1G5B1="\033[48;5;"+(16+(1*36)+(5*6)+1)+"m";
    public static final String COLOR_BR2G5B1="\033[48;5;"+(16+(2*36)+(5*6)+1)+"m";
    public static final String COLOR_BR3G5B1="\033[48;5;"+(16+(3*36)+(5*6)+1)+"m";
    public static final String COLOR_BR4G5B1="\033[48;5;"+(16+(4*36)+(5*6)+1)+"m";
    public static final String COLOR_BR5G5B1="\033[48;5;"+(16+(4*36)+(5*6)+1)+"m";
    public static final String COLOR_BR0G0B2="\033[48;5;"+(16+(0*36)+(0*6)+2)+"m";
    public static final String COLOR_BR1G0B2="\033[48;5;"+(16+(1*36)+(0*6)+2)+"m";
    public static final String COLOR_BR2G0B2="\033[48;5;"+(16+(2*36)+(0*6)+2)+"m";
    public static final String COLOR_BR3G0B2="\033[48;5;"+(16+(3*36)+(0*6)+2)+"m";
    public static final String COLOR_BR4G0B2="\033[48;5;"+(16+(4*36)+(0*6)+2)+"m";
    public static final String COLOR_BR5G0B2="\033[48;5;"+(16+(5*36)+(0*6)+2)+"m";
    public static final String COLOR_BR0G1B2="\033[48;5;"+(16+(0*36)+(1*6)+2)+"m";
    public static final String COLOR_BR1G1B2="\033[48;5;"+(16+(1*36)+(1*6)+2)+"m";
    public static final String COLOR_BR2G1B2="\033[48;5;"+(16+(2*36)+(1*6)+2)+"m";
    public static final String COLOR_BR3G1B2="\033[48;5;"+(16+(3*36)+(1*6)+2)+"m";
    public static final String COLOR_BR4G1B2="\033[48;5;"+(16+(4*36)+(1*6)+2)+"m";
    public static final String COLOR_BR5G1B2="\033[48;5;"+(16+(5*36)+(1*6)+2)+"m";
    public static final String COLOR_BR0G2B2="\033[48;5;"+(16+(0*36)+(2*6)+2)+"m";
    public static final String COLOR_BR1G2B2="\033[48;5;"+(16+(1*36)+(2*6)+2)+"m";
    public static final String COLOR_BR2G2B2="\033[48;5;"+(16+(2*36)+(2*6)+2)+"m";
    public static final String COLOR_BR3G2B2="\033[48;5;"+(16+(3*36)+(2*6)+2)+"m";
    public static final String COLOR_BR4G2B2="\033[48;5;"+(16+(4*36)+(2*6)+2)+"m";
    public static final String COLOR_BR5G2B2="\033[48;5;"+(16+(5*36)+(2*6)+2)+"m";
    public static final String COLOR_BR0G3B2="\033[48;5;"+(16+(0*36)+(3*6)+2)+"m";
    public static final String COLOR_BR1G3B2="\033[48;5;"+(16+(1*36)+(3*6)+2)+"m";
    public static final String COLOR_BR2G3B2="\033[48;5;"+(16+(2*36)+(3*6)+2)+"m";
    public static final String COLOR_BR3G3B2="\033[48;5;"+(16+(3*36)+(3*6)+2)+"m";
    public static final String COLOR_BR4G3B2="\033[48;5;"+(16+(4*36)+(3*6)+2)+"m";
    public static final String COLOR_BR5G3B2="\033[48;5;"+(16+(5*36)+(3*6)+2)+"m";
    public static final String COLOR_BR0G4B2="\033[48;5;"+(16+(0*36)+(4*6)+2)+"m";
    public static final String COLOR_BR1G4B2="\033[48;5;"+(16+(1*36)+(4*6)+2)+"m";
    public static final String COLOR_BR2G4B2="\033[48;5;"+(16+(2*36)+(4*6)+2)+"m";
    public static final String COLOR_BR3G4B2="\033[48;5;"+(16+(3*36)+(4*6)+2)+"m";
    public static final String COLOR_BR4G4B2="\033[48;5;"+(16+(4*36)+(4*6)+2)+"m";
    public static final String COLOR_BR5G4B2="\033[48;5;"+(16+(5*36)+(4*6)+2)+"m";
    public static final String COLOR_BR0G5B2="\033[48;5;"+(16+(0*36)+(5*6)+2)+"m";
    public static final String COLOR_BR1G5B2="\033[48;5;"+(16+(1*36)+(5*6)+2)+"m";
    public static final String COLOR_BR2G5B2="\033[48;5;"+(16+(2*36)+(5*6)+2)+"m";
    public static final String COLOR_BR3G5B2="\033[48;5;"+(16+(3*36)+(5*6)+2)+"m";
    public static final String COLOR_BR4G5B2="\033[48;5;"+(16+(4*36)+(5*6)+2)+"m";
    public static final String COLOR_BR5G5B2="\033[48;5;"+(16+(4*36)+(5*6)+2)+"m";
    public static final String COLOR_BR0G0B3="\033[48;5;"+(16+(0*36)+(0*6)+3)+"m";
    public static final String COLOR_BR1G0B3="\033[48;5;"+(16+(1*36)+(0*6)+3)+"m";
    public static final String COLOR_BR2G0B3="\033[48;5;"+(16+(2*36)+(0*6)+3)+"m";
    public static final String COLOR_BR3G0B3="\033[48;5;"+(16+(3*36)+(0*6)+3)+"m";
    public static final String COLOR_BR4G0B3="\033[48;5;"+(16+(4*36)+(0*6)+3)+"m";
    public static final String COLOR_BR5G0B3="\033[48;5;"+(16+(5*36)+(0*6)+3)+"m";
    public static final String COLOR_BR0G1B3="\033[48;5;"+(16+(0*36)+(1*6)+3)+"m";
    public static final String COLOR_BR1G1B3="\033[48;5;"+(16+(1*36)+(1*6)+3)+"m";
    public static final String COLOR_BR2G1B3="\033[48;5;"+(16+(2*36)+(1*6)+3)+"m";
    public static final String COLOR_BR3G1B3="\033[48;5;"+(16+(3*36)+(1*6)+3)+"m";
    public static final String COLOR_BR4G1B3="\033[48;5;"+(16+(4*36)+(1*6)+3)+"m";
    public static final String COLOR_BR5G1B3="\033[48;5;"+(16+(5*36)+(1*6)+3)+"m";
    public static final String COLOR_BR0G2B3="\033[48;5;"+(16+(0*36)+(2*6)+3)+"m";
    public static final String COLOR_BR1G2B3="\033[48;5;"+(16+(1*36)+(2*6)+3)+"m";
    public static final String COLOR_BR2G2B3="\033[48;5;"+(16+(2*36)+(2*6)+3)+"m";
    public static final String COLOR_BR3G2B3="\033[48;5;"+(16+(3*36)+(2*6)+3)+"m";
    public static final String COLOR_BR4G2B3="\033[48;5;"+(16+(4*36)+(2*6)+3)+"m";
    public static final String COLOR_BR5G2B3="\033[48;5;"+(16+(5*36)+(2*6)+3)+"m";
    public static final String COLOR_BR0G3B3="\033[48;5;"+(16+(0*36)+(3*6)+3)+"m";
    public static final String COLOR_BR1G3B3="\033[48;5;"+(16+(1*36)+(3*6)+3)+"m";
    public static final String COLOR_BR2G3B3="\033[48;5;"+(16+(2*36)+(3*6)+3)+"m";
    public static final String COLOR_BR3G3B3="\033[48;5;"+(16+(3*36)+(3*6)+3)+"m";
    public static final String COLOR_BR4G3B3="\033[48;5;"+(16+(4*36)+(3*6)+3)+"m";
    public static final String COLOR_BR5G3B3="\033[48;5;"+(16+(5*36)+(3*6)+3)+"m";
    public static final String COLOR_BR0G4B3="\033[48;5;"+(16+(0*36)+(4*6)+3)+"m";
    public static final String COLOR_BR1G4B3="\033[48;5;"+(16+(1*36)+(4*6)+3)+"m";
    public static final String COLOR_BR2G4B3="\033[48;5;"+(16+(2*36)+(4*6)+3)+"m";
    public static final String COLOR_BR3G4B3="\033[48;5;"+(16+(3*36)+(4*6)+3)+"m";
    public static final String COLOR_BR4G4B3="\033[48;5;"+(16+(4*36)+(4*6)+3)+"m";
    public static final String COLOR_BR5G4B3="\033[48;5;"+(16+(5*36)+(4*6)+3)+"m";
    public static final String COLOR_BR0G5B3="\033[48;5;"+(16+(0*36)+(5*6)+3)+"m";
    public static final String COLOR_BR1G5B3="\033[48;5;"+(16+(1*36)+(5*6)+3)+"m";
    public static final String COLOR_BR2G5B3="\033[48;5;"+(16+(2*36)+(5*6)+3)+"m";
    public static final String COLOR_BR3G5B3="\033[48;5;"+(16+(3*36)+(5*6)+3)+"m";
    public static final String COLOR_BR4G5B3="\033[48;5;"+(16+(4*36)+(5*6)+3)+"m";
    public static final String COLOR_BR5G5B3="\033[48;5;"+(16+(4*36)+(5*6)+3)+"m";
    public static final String COLOR_BR0G0B4="\033[48;5;"+(16+(0*36)+(0*6)+4)+"m";
    public static final String COLOR_BR1G0B4="\033[48;5;"+(16+(1*36)+(0*6)+4)+"m";
    public static final String COLOR_BR2G0B4="\033[48;5;"+(16+(2*36)+(0*6)+4)+"m";
    public static final String COLOR_BR3G0B4="\033[48;5;"+(16+(3*36)+(0*6)+4)+"m";
    public static final String COLOR_BR4G0B4="\033[48;5;"+(16+(4*36)+(0*6)+4)+"m";
    public static final String COLOR_BR5G0B4="\033[48;5;"+(16+(5*36)+(0*6)+4)+"m";
    public static final String COLOR_BR0G1B4="\033[48;5;"+(16+(0*36)+(1*6)+4)+"m";
    public static final String COLOR_BR1G1B4="\033[48;5;"+(16+(1*36)+(1*6)+4)+"m";
    public static final String COLOR_BR2G1B4="\033[48;5;"+(16+(2*36)+(1*6)+4)+"m";
    public static final String COLOR_BR3G1B4="\033[48;5;"+(16+(3*36)+(1*6)+4)+"m";
    public static final String COLOR_BR4G1B4="\033[48;5;"+(16+(4*36)+(1*6)+4)+"m";
    public static final String COLOR_BR5G1B4="\033[48;5;"+(16+(5*36)+(1*6)+4)+"m";
    public static final String COLOR_BR0G2B4="\033[48;5;"+(16+(0*36)+(2*6)+4)+"m";
    public static final String COLOR_BR1G2B4="\033[48;5;"+(16+(1*36)+(2*6)+4)+"m";
    public static final String COLOR_BR2G2B4="\033[48;5;"+(16+(2*36)+(2*6)+4)+"m";
    public static final String COLOR_BR3G2B4="\033[48;5;"+(16+(3*36)+(2*6)+4)+"m";
    public static final String COLOR_BR4G2B4="\033[48;5;"+(16+(4*36)+(2*6)+4)+"m";
    public static final String COLOR_BR5G2B4="\033[48;5;"+(16+(5*36)+(2*6)+4)+"m";
    public static final String COLOR_BR0G3B4="\033[48;5;"+(16+(0*36)+(3*6)+4)+"m";
    public static final String COLOR_BR1G3B4="\033[48;5;"+(16+(1*36)+(3*6)+4)+"m";
    public static final String COLOR_BR2G3B4="\033[48;5;"+(16+(2*36)+(3*6)+4)+"m";
    public static final String COLOR_BR3G3B4="\033[48;5;"+(16+(3*36)+(3*6)+4)+"m";
    public static final String COLOR_BR4G3B4="\033[48;5;"+(16+(4*36)+(3*6)+4)+"m";
    public static final String COLOR_BR5G3B4="\033[48;5;"+(16+(5*36)+(3*6)+4)+"m";
    public static final String COLOR_BR0G4B4="\033[48;5;"+(16+(0*36)+(4*6)+4)+"m";
    public static final String COLOR_BR1G4B4="\033[48;5;"+(16+(1*36)+(4*6)+4)+"m";
    public static final String COLOR_BR2G4B4="\033[48;5;"+(16+(2*36)+(4*6)+4)+"m";
    public static final String COLOR_BR3G4B4="\033[48;5;"+(16+(3*36)+(4*6)+4)+"m";
    public static final String COLOR_BR4G4B4="\033[48;5;"+(16+(4*36)+(4*6)+4)+"m";
    public static final String COLOR_BR5G4B4="\033[48;5;"+(16+(5*36)+(4*6)+4)+"m";
    public static final String COLOR_BR0G5B4="\033[48;5;"+(16+(0*36)+(5*6)+4)+"m";
    public static final String COLOR_BR1G5B4="\033[48;5;"+(16+(1*36)+(5*6)+4)+"m";
    public static final String COLOR_BR2G5B4="\033[48;5;"+(16+(2*36)+(5*6)+4)+"m";
    public static final String COLOR_BR3G5B4="\033[48;5;"+(16+(3*36)+(5*6)+4)+"m";
    public static final String COLOR_BR4G5B4="\033[48;5;"+(16+(4*36)+(5*6)+4)+"m";
    public static final String COLOR_BR5G5B4="\033[48;5;"+(16+(4*36)+(5*6)+4)+"m";
    public static final String COLOR_BR0G0B5="\033[48;5;"+(16+(0*36)+(0*6)+5)+"m";
    public static final String COLOR_BR1G0B5="\033[48;5;"+(16+(1*36)+(0*6)+5)+"m";
    public static final String COLOR_BR2G0B5="\033[48;5;"+(16+(2*36)+(0*6)+5)+"m";
    public static final String COLOR_BR3G0B5="\033[48;5;"+(16+(3*36)+(0*6)+5)+"m";
    public static final String COLOR_BR4G0B5="\033[48;5;"+(16+(4*36)+(0*6)+5)+"m";
    public static final String COLOR_BR5G0B5="\033[48;5;"+(16+(5*36)+(0*6)+5)+"m";
    public static final String COLOR_BR0G1B5="\033[48;5;"+(16+(0*36)+(1*6)+5)+"m";
    public static final String COLOR_BR1G1B5="\033[48;5;"+(16+(1*36)+(1*6)+5)+"m";
    public static final String COLOR_BR2G1B5="\033[48;5;"+(16+(2*36)+(1*6)+5)+"m";
    public static final String COLOR_BR3G1B5="\033[48;5;"+(16+(3*36)+(1*6)+5)+"m";
    public static final String COLOR_BR4G1B5="\033[48;5;"+(16+(4*36)+(1*6)+5)+"m";
    public static final String COLOR_BR5G1B5="\033[48;5;"+(16+(5*36)+(1*6)+5)+"m";
    public static final String COLOR_BR0G2B5="\033[48;5;"+(16+(0*36)+(2*6)+5)+"m";
    public static final String COLOR_BR1G2B5="\033[48;5;"+(16+(1*36)+(2*6)+5)+"m";
    public static final String COLOR_BR2G2B5="\033[48;5;"+(16+(2*36)+(2*6)+5)+"m";
    public static final String COLOR_BR3G2B5="\033[48;5;"+(16+(3*36)+(2*6)+5)+"m";
    public static final String COLOR_BR4G2B5="\033[48;5;"+(16+(4*36)+(2*6)+5)+"m";
    public static final String COLOR_BR5G2B5="\033[48;5;"+(16+(5*36)+(2*6)+5)+"m";
    public static final String COLOR_BR0G3B5="\033[48;5;"+(16+(0*36)+(3*6)+5)+"m";
    public static final String COLOR_BR1G3B5="\033[48;5;"+(16+(1*36)+(3*6)+5)+"m";
    public static final String COLOR_BR2G3B5="\033[48;5;"+(16+(2*36)+(3*6)+5)+"m";
    public static final String COLOR_BR3G3B5="\033[48;5;"+(16+(3*36)+(3*6)+5)+"m";
    public static final String COLOR_BR4G3B5="\033[48;5;"+(16+(4*36)+(3*6)+5)+"m";
    public static final String COLOR_BR5G3B5="\033[48;5;"+(16+(5*36)+(3*6)+5)+"m";
    public static final String COLOR_BR0G4B5="\033[48;5;"+(16+(0*36)+(4*6)+5)+"m";
    public static final String COLOR_BR1G4B5="\033[48;5;"+(16+(1*36)+(4*6)+5)+"m";
    public static final String COLOR_BR2G4B5="\033[48;5;"+(16+(2*36)+(4*6)+5)+"m";
    public static final String COLOR_BR3G4B5="\033[48;5;"+(16+(3*36)+(4*6)+5)+"m";
    public static final String COLOR_BR4G4B5="\033[48;5;"+(16+(4*36)+(4*6)+5)+"m";
    public static final String COLOR_BR5G4B5="\033[48;5;"+(16+(5*36)+(4*6)+5)+"m";
    public static final String COLOR_BR0G5B5="\033[48;5;"+(16+(0*36)+(5*6)+5)+"m";
    public static final String COLOR_BR1G5B5="\033[48;5;"+(16+(1*36)+(5*6)+5)+"m";
    public static final String COLOR_BR2G5B5="\033[48;5;"+(16+(2*36)+(5*6)+5)+"m";
    public static final String COLOR_BR3G5B5="\033[48;5;"+(16+(3*36)+(5*6)+5)+"m";
    public static final String COLOR_BR4G5B5="\033[48;5;"+(16+(4*36)+(5*6)+5)+"m";
    public static final String COLOR_BR5G5B5="\033[48;5;"+(16+(4*36)+(5*6)+5)+"m";
    
    public void clearLookups();
    public int translateSingleCMCodeToANSIOffSet(String code);
    public String translateCMCodeToANSI(String code);
    public String translateANSItoCMCode(String code);
    public String mixHTMLCodes(String code1, String code2);
    public String mixColorCodes(String code1, String code2);
    public CMMsg fixSourceFightColor(CMMsg msg);
    public String[] standardHTMLlookups();
    public String[] standardColorLookups();
    
}
