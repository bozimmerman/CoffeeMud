package com.planet_ink.siplet.support;
import java.applet.*;
import java.net.*;
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
public class MXP
{

    public MXP(){super();}
    
    private int defaultMode=0;
    public static final int MODE_LINE_OPEN=0;
    public static final int MODE_LINE_SECURE=1;
    public static final int MODE_LINE_LOCKED=2;
    public static final int MODE_RESET=3;
    public static final int MODE_TEMP_SECURE=4;
    public static final int MODE_LOCK_OPEN=5;
    public static final int MODE_LOCK_SECURE=6;
    public static final int MODE_LOCK_LOCKED=7;
    public static final int MODE_LINE_ROOMNAME=10;
    public static final int MODE_LINE_ROOMDESC=11;
    public static final int MODE_LINE_ROOMEXITS=12;
    public static final int MODE_LINE_WELCOME=19;
    
    private int mode=0;
    public int mode(){return mode;}
    public void setMode(int newMode){mode=newMode;}

    public void executeMode()
    {
        switch(mode)
        {
        case MODE_RESET:
            break;
        }
    }
    public String escapeTranslate(String escapeString)
    {
        if(escapeString.endsWith("z"))
        {
            int code=Util.s0_int(escapeString.substring(0,escapeString.length()-1));
            if(code<20)
            {
                mode=code;
                executeMode();
            }
            else
            if(code<100)
            {
                
            }
            return "";
        }
        return escapeString;
    }
    
    public void newlineDetected()
    {
        
    }
    
    public int processTag(StringBuffer buf, int i)
    {
        return 0;
    }
    public int processEntity(StringBuffer buf, int i)
    {
        boolean convertIt=false;
        int x=i;
        if((buf.charAt(i+1)=='#')&&(Character.isDigit(buf.charAt(i+2))))
        {
            x++; // skip to the hash, the next line will skip to the digit
            while((++x)<buf.length())
            {
                if(buf.charAt(x)==';')
                {
                    convertIt=false;
                    break;
                }
                else
                if(!Character.isDigit(buf.charAt(x)))
                {
                    convertIt=true;
                    break;
                }
            }
        }
        else
        while((++x)<buf.length())
        {
            if(buf.charAt(x)==';')
            {
                convertIt=false;
                break;
            }
            else
            if(!Character.isLetter(buf.charAt(x)))
            {
                convertIt=true;
                break;
            }
        }
        if(!convertIt) return 0;
        buf.insert(i+1,"amp;");
        return 4;
    }
    
}
