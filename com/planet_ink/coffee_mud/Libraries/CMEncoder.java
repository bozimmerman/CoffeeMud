package com.planet_ink.coffee_mud.Libraries;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import com.planet_ink.coffee_mud.Libraries.interfaces.TextEncoders;
import com.planet_ink.coffee_mud.core.CMProps;
import com.planet_ink.coffee_mud.core.Log;

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
public class CMEncoder extends StdLibrary implements TextEncoders
{
    public String ID(){return "CMEncoder";}
    private byte[] encodeBuffer = new byte[65536];
    private Deflater compresser = new Deflater(Deflater.BEST_COMPRESSION);
    private Inflater decompresser = new Inflater();
    
    public CMEncoder()
    {
        super();
    }

    public synchronized String decompressString(byte[] b)
    {
        try
        {
            if ((b == null)||(b.length==0)) return "";

            decompresser.reset();
            decompresser.setInput(b);

            synchronized (encodeBuffer)
            {
                int len = decompresser.inflate(encodeBuffer);
                return new String(encodeBuffer, 0, len, CMProps.getVar(CMProps.SYSTEM_CHARSETINPUT));
            }
        }
        catch (Exception ex)
        {
            Log.errOut(Thread.currentThread().getName(), "Error occurred during decompression: "+ex.getMessage());
            encodeBuffer=new byte[65536];
            return "";
        }
    }

    public synchronized byte[] compressString(String s)
    {
        byte[] result = null;

        try
        {
            compresser.reset();
            compresser.setInput(s.getBytes(CMProps.getVar(CMProps.SYSTEM_CHARSETINPUT)));
            compresser.finish();
            
            synchronized (encodeBuffer)
            {
                if(s.length()>encodeBuffer.length)
                    encodeBuffer=new byte[s.length()];
                encodeBuffer[0]=0;

                int len = compresser.deflate(encodeBuffer);
                result = new byte[len];
                System.arraycopy(encodeBuffer, 0, result, 0, len);
            }
        }
        catch (Exception ex)
        {
            Log.errOut("MUD", "Error occurred during compression: "+ex.getMessage());
            encodeBuffer=new byte[65536];
        }

        return result;
    }
    
}
