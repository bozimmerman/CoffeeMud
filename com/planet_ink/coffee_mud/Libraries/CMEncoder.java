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
   Copyright 2005-2018 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "CMEncoder";
	}

	private byte[]			encodeBuffer	= new byte[65536];
	private final Deflater	compresser		= new Deflater(Deflater.BEST_COMPRESSION);
	private final Inflater	decompresser	= new Inflater();

	public CMEncoder()
	{
		super();
	}

	@Override
	public synchronized String decompressString(byte[] b)
	{
		try
		{
			if ((b == null)||(b.length==0)) return "";

			decompresser.reset();
			decompresser.setInput(b);

			synchronized (encodeBuffer)
			{
				final int len = decompresser.inflate(encodeBuffer);
				return new String(encodeBuffer, 0, len, CMProps.getVar(CMProps.Str.CHARSETINPUT));
			}
		}
		catch (final Exception ex)
		{
			Log.errOut(Thread.currentThread().getName(), "Error occurred during decompression: "+ex.getMessage());
			encodeBuffer=new byte[65536];
			return "";
		}
	}

	@Override
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

				final int len = compresser.deflate(encodeBuffer);
				result = new byte[len];
				System.arraycopy(encodeBuffer, 0, result, 0, len);
			}
		}
		catch (final Exception ex)
		{
			Log.errOut("MUD", "Error occurred during compression: "+ex.getMessage());
			encodeBuffer=new byte[65536];
		}

		return result;
	}

	@Override
	public String makeRandomHashString(final String password)
	{
		final int salt=(int)Math.round(CMath.random() * Integer.MAX_VALUE);
		final int passHash=(password+salt).toLowerCase().hashCode();
		return "|"+B64Encoder.B64encodeBytes(ByteBuffer.allocate(4).putInt(salt).array())
			  +"|"+B64Encoder.B64encodeBytes(ByteBuffer.allocate(4).putInt(passHash).array());
	}

	@Override
	public boolean isARandomHashString(final String password)
	{
		return ((password.length()>2) && (password.startsWith("|")) && (password.indexOf('|',1)>1));
	}

	@Override
	public boolean checkPasswordAgainstRandomHashString(final String passwordString, final String hashString)
	{
		final int hashDex=hashString.indexOf('|',1);
		final int salt=ByteBuffer.wrap(B64Encoder.B64decode(hashString.substring(1,hashDex))).getInt();
		final int hash=ByteBuffer.wrap(B64Encoder.B64decode(hashString.substring(hashDex+1))).getInt();
		return hash==(passwordString+salt).toLowerCase().hashCode();
	}

	@Override
	public boolean checkHashStringPairs(final String hashString1, final String hashString2)
	{
		final int hashDex1=hashString1.indexOf('|',1);
		final int salt1=ByteBuffer.wrap(B64Encoder.B64decode(hashString1.substring(1,hashDex1))).getInt();
		final int hash1=ByteBuffer.wrap(B64Encoder.B64decode(hashString1.substring(hashDex1+1))).getInt();
		
		final int hashDex2=hashString2.indexOf('|',1);
		final int salt2=ByteBuffer.wrap(B64Encoder.B64decode(hashString2.substring(1,hashDex2))).getInt();
		final int hash2=ByteBuffer.wrap(B64Encoder.B64decode(hashString2.substring(hashDex2+1))).getInt();
		
		return (hash1==(hashString2+salt1).toLowerCase().hashCode()) || (hash2==(hashString1+salt2).toLowerCase().hashCode());
	}

	@Override
	public String generateRandomPassword()
	{
		final StringBuilder str=new StringBuilder("");
		for(int i=0;i<10;i++)
		{
			if((i%2)==0)
				str.append(CMLib.dice().roll(1, 10, -1));
			else
				str.append((char)('a'+CMLib.dice().roll(1, 26, -1)));
		}
		return str.toString();
	}

	@Override
	public boolean passwordCheck(String pass1, String pass2)
	{
		if(pass1 == null)
		{
			return (pass2==null);
		}
		else
		if(pass2 == null)
			return false;
		
		if(pass1.equalsIgnoreCase(pass2))
			return true;
		if(CMLib.encoder().isARandomHashString(pass2))
		{
			if(CMLib.encoder().isARandomHashString(pass1))
				return checkHashStringPairs(pass1,pass2);
			else
				return CMLib.encoder().checkPasswordAgainstRandomHashString(pass1, pass2);
		}
		else
		{
			if(CMLib.encoder().isARandomHashString(pass1))
				return CMLib.encoder().checkPasswordAgainstRandomHashString(pass2, pass1);
			return pass1.equalsIgnoreCase(pass2);
		}
	}

}
