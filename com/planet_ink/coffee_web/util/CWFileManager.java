package com.planet_ink.coffee_web.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import com.planet_ink.coffee_web.interfaces.FileManager;
/*
   Copyright 2012-2018 Bo Zimmerman

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

public class CWFileManager implements FileManager
{

	@Override
	public char getFileSeparator()
	{
		return File.separatorChar;
	}

	@Override
	public File createFileFromPath(String localPath)
	{
		return new File(localPath);
	}
	@Override
	public File createFileFromPath(File parent, String localPath)
	{
		return new File(parent, localPath);
	}
	@Override
	public InputStream getFileStream(File file) throws IOException, FileNotFoundException
	{
		return new BufferedInputStream(new FileInputStream(file));
	}
	@Override
	public RandomAccessFile getRandomAccessFile(File file) throws IOException, FileNotFoundException
	{
		return new RandomAccessFile(file,"r");
	}
	@Override
	public byte[] readFile(File file) throws IOException, FileNotFoundException 
	{
		BufferedInputStream bs = null;
		final byte[] fileBuf = new byte[(int)file.length()];
		try
		{
			bs=new BufferedInputStream(new FileInputStream(file));
			bs.read(fileBuf);
		}
		finally
		{
			if(bs!=null)
				bs.close();
		}
		return fileBuf;
	}

	@Override
	public boolean supportsRandomAccess(File file)
	{
		return true;
	}

	@Override
	public boolean allowedToReadData(File file)
	{
		return file.exists() && file.canRead();
	}
}
