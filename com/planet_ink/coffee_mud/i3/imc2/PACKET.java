
package com.planet_ink.coffee_mud.i3.imc2;

import java.lang.String;

/* an IMC packet, as seen by the high-level code */
public final class PACKET {
    
    /* max number of data keys in a packet */
    public final static int IMC_MAX_KEYS = 20;
    
    String to = ""; /* destination of packet */
    String from = ""; /* source of packet      */
    String type = ""; /* type of packet        */
    String[] key = new String[IMC_MAX_KEYS];
    String[] value = new String[IMC_MAX_KEYS];

    /* internal things which only the low-level code needs to know about */
    class ii {
        String to = "";
        String from = "";
        String path = "";

        long sequence;
        int stamp;
    }
    ii i = new ii();
}
