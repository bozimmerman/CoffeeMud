package com.planet_ink.coffee_mud.Libraries;
import java.nio.ByteBuffer;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import com.planet_ink.coffee_mud.Libraries.interfaces.TextEncoders;
import com.planet_ink.coffee_mud.core.B64Encoder;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMProps;
import com.planet_ink.coffee_mud.core.CMath;
import com.planet_ink.coffee_mud.core.Log;

/* 
   Copyright 2000-2013 Bo Zimmerman

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
				return new String(encodeBuffer, 0, len, CMProps.getVar(CMProps.Str.CHARSETINPUT));
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
			compresser.setInput(s.getBytes(CMProps.getVar(CMProps.Str.CHARSETINPUT)));
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

	public String makeRandomHashString(final String password)
	{
		int salt=(int)Math.round(CMath.random() * Integer.MAX_VALUE);
		int passHash=(password+salt).toLowerCase().hashCode();
		return "|"+B64Encoder.B64encodeBytes(ByteBuffer.allocate(4).putInt(salt).array())
			  +"|"+B64Encoder.B64encodeBytes(ByteBuffer.allocate(4).putInt(passHash).array());
	}
	
	public boolean isARandomHashString(final String password)
	{
		return ((password.length()>2) && (password.startsWith("|")) && (password.indexOf('|',1)>1));
	}
	
	public boolean checkAgainstRandomHashString(final String checkString, final String hashString)
	{
		int hashDex=hashString.indexOf('|',1);
		int salt=ByteBuffer.wrap(B64Encoder.B64decode(hashString.substring(1,hashDex))).getInt();
		int hash=ByteBuffer.wrap(B64Encoder.B64decode(hashString.substring(hashDex+1))).getInt();
		return hash==(checkString+salt).toLowerCase().hashCode();
	}
	
	public String generateRandomPassword()
	{
		StringBuilder str=new StringBuilder("");
		for(int i=0;i<10;i++)
		{
			if((i%2)==0)
				str.append(CMLib.dice().roll(1, 10, -1));
			else
				str.append((char)('a'+CMLib.dice().roll(1, 26, -1)));
		}
		return str.toString();
	}
	
}
