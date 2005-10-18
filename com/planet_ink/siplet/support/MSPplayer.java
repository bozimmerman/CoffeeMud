package com.planet_ink.siplet.support;
import com.planet_ink.siplet.applet.*;
import java.applet.*;
import java.util.*;
import java.net.*;

public class MSPplayer extends Thread
{
    public String key=null;
    public int volume=100;
    public int repeats=1;
    public int iterations=0;
    public int priority=50;
    public int continueValue=1;
    public AudioClip clip=null;
    public String url=null;
    public boolean playing=false;
    public boolean orderedStopped=false;
    private Applet applet=null;
    
    public MSPplayer(Applet theApplet)
    {
        super();
        applet=theApplet;
        // TODO Auto-generated constructor stub
    }
    
    public void stopPlaying()
    {
        if(playing)
        {
            orderedStopped=true;
            if(clip!=null) clip.stop();
            try{Thread.sleep(50);}catch(Exception e){}
            if(playing)
                interrupt();
        }
    }
    
    public void run()
    {
        playing=true;
        orderedStopped=false;
        try
        {
            if(url==null)
                clip=applet.getAudioClip(applet.getCodeBase(),key);
            else
                clip=applet.getAudioClip(new URL(url+key));
        }
        catch(MalformedURLException m)
        {
            playing=false;
            return;
        }
        if(clip!=null)
        {
            // dunno how to set volume, but that should go here.
            while((!orderedStopped)&&(iterations<repeats))
            {
                iterations++;
                clip.play();
            }
        }
        playing=false;
    }
    public void startPlaying()
    {
        this.start();
    }
    
}
