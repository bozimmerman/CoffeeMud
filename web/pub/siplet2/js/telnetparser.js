var TELOPT =
{
	ESC: 27,
	IAC: 0xFF,
	BINARY: 0,
	ECHO: 1,
	LOGOUT: 18,
	SUPRESS_GO_AHEAD: 3,
	TERMTYPE: 24,
	NAWS: 31,
	TOGGLE_FLOW_CONTROL: 33,
	LINEMODE: 34,
	MSDP: 69,
	MSSP: 70,
	COMPRESS: 85,
	COMPRESS2: 86,
	MSP: 90,
	MXP: 91,
	AARD: 102,
	SE: 0xF0,
	AYT: 0xF6,
	EC: 0xF7,
	ATCP: 0xC8,
	GMCP: 0xC9,
	SB: 0xFA,
	WILL: 0xFB,
	WONT: 0xFC,
	ANSI16: 0xFC,
	DO: 0xFD,
	ANSI: 0xFD,
	DONT: 0xFE,
	GA: 0xF9,
	NOP: 0xF1,
	BINARY: 0,
	EOR: 25,
	ECHO: 1,
	LOGOUT: 18,
	TTYPE: 24
};

var StringToAsciiArray = function(str) {
	var arr = [];
	for(var i=0;i<str.length;i++)
		arr.push(str.charCodeAt(i));
	return arr;
};

var TELNET = function(siplet)
{
	this.neverSupportMSP = false;
	this.neverSupportMXP = true; //TODO: soon!
	this.neverSupportMSDP= true; //TODO: soon!
	this.neverSupportGMCP = true; //TODO: soon!
	this.neverSupportMCCP = true; //TODO: maybe add pako later?
	this.MSPsupport = false;
	this.MSDPsupport = false;
	this.GMCPsupport = false;
	this.MXPsupport = false;
	this.MCCPsupport = false;
	this.msdpInforms = "";
	this.gmcpInforms = "";
	
	this.process = function(dat)
	{
		var response = [];
		switch (dat[0])
		{
		case TELOPT.SB:
		{
			var subOptionData = [];
			var subOptionCode = dat[1];
			var last = 0;
			var i=1;
			while ((i < (dat.length - 1)) && ((last = dat[++i]) != -1))
			{
				if ((last == TELOPT.IAC) && (i < (dat.length - 1)))
				{
					last = dat[++i];
					if (last == TELOPT.IAC)
						subOptionData.push(TELOPT.IAC);
					else
					if (last == TELOPT.SE)
						break;
				}
				else
					subOptionData.push(last);
			}
			if (subOptionCode == TELOPT.TTYPE)
			{
				response = response.concat([TELOPT.IAC, TELOPT.SB, TELOPT.TTYPE, 0]);
				response = response.concat(StringToAsciiArray("siplet"));
				response = response.concat([TELOPT.IAC, TELOPT.SE]);
			}
			else
			if (subOptionCode == TELOPT.NAWS)
			{
				//TODO: store screen info somewhere else
				response = response.concat([
					TELOPT.IAC, TELOPT.SB, TELOPT.NAWS,
						0,siplet.ScreenData.width,0,siplet.ScreenData.height,
					TELOPT.IAC, TELOPT.SE]);
			}
			else
			if (subOptionCode == TELOPT.COMPRESS2)
			{
				// probably need to handle this earlier
			}
			else
			if (subOptionCode == TELOPT.MSDP)
			{
				//TODO: !!!
				/*
				final String received = this.msdpModule.msdpReceive(subOptionData.toByteArray());
				synchronized (msdpInforms)
				{
					msdpInforms.append(received);
				}
				if (debugTelnetCodes)
					debugStream.println("Got MSDP: " + received);
				*/
			}
			else
			if (subOptionCode == TELOPT.GMCP)
			{
				//TODO: !!!
				/*
					final String received = this.gmcpModule.gmcpReceive(subOptionData.toByteArray());
					synchronized (gmcpInforms)
					{
						gmcpInforms.append(received + "\n");
					}
					if (debugTelnetCodes)
						debugStream.println("Got GMCP: " + received);
				*/
			}
			break;
		}
		case TELOPT.WILL:
			switch(dat[1])
			{
			case TELOPT.NAWS:
				response = response.concat([
					TELOPT.IAC, TELOPT.SB, TELOPT.NAWS,
						0,siplet.ScreenData.width,0,siplet.ScreenData.height,
					TELOPT.IAC, TELOPT.SE]);
				break;
			case TELOPT.MSP:
				if (this.neverSupportMSP)
				{
					if (this.MSPsupport)
					{
						response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.MSP]);
						this.MSPsupport = false;
					}
				}
				else
				if (!this.MSPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.DO, TELOPT.MSP]);
					this.MSPsupport = true;
				}
				break;
			case TELOPT.MSDP:
				if (this.neverSupportMSDP)
				{
					if (this.MSDPsupport)
					{
						response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.MSDP]);
						this.MSDPsupport = false;
					}
				}
				else
				if (!this.MSDPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.DO, TELOPT.MSDP]);
					this.MSDPsupport = true;
				}
				break;
			case TELOPT.GMCP:
				if (this.neverSupportGMCP)
				{
					if (this.GMCPsupport)
					{
						response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.GMCP]);
						this.GMCPsupport = false;
					}
				}
				else
				if (!this.GMCPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.DO, TELOPT.GMCP]);
					this.GMCPsupport =true;
					response = response.concat([TELOPT.IAC, TELOPT.SB,TELOPT.GMCP]);
					var gmcpMsg = "core.hello {\"client\":\"siplet\",\"version\":" + siplet.VERSION_MAJOR + "}";
					response = response.concat(StringToAsciiArray(gmcpMsg));
					response = response.concat([TELOPT.IAC, TELOPT.SE]);
				}
				break;
			case TELOPT.MXP:
				if (this.neverSupportMXP)
				{
					if (this.MXPsupport)
					{
						response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.MXP]);
						this.MXPsupport = false;
					}
				}
				else
				if (!this.MXPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.DO, TELOPT.MXP]);
					this.MXPsupport = true;
				}
				break;
			case TELOPT.COMPRESS2:
				if (this.neverSupportMCCP)
				{
					if (this.MCCPsupport)
					{
						response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.MCCP]);
						this.MCCPsupport = false;
					}
				}
				else
				if (!this.MCCPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.DO, TELOPT.MCCP]);
					this.MCCPsupport = false;
					//TODO: it cant get here, but when it does, fix it.
				}
				break;
			case TELOPT.LOGOUT:
				// goot for you serverdude
				break;
			case TELOPT.BINARY:
				response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.BINARY]);
				break;
			};
			break;
		case TELOPT.WONT:
			switch(dat[1])
			{
			case TELOPT.MSP:
				if (this.MSPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.MSP]);
					this.MSPsupport = false;
				}
				break;
			case TELOPT.MSDP:
				if (this.MSDPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.MSDP]);
					this.MSDPsupport = false;
				}
				break;
			case TELOPT.GMCP:
				if (this.GMCPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.GMCP]);
					this.GMCPsupport = false;
				}
				break;
			case TELOPT.MXP:
				if (this.MXPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.MXP]);
					this.MXPsupport = false;
					//TODO: if (mxpModule != null) mxpModule.shutdownMXP();
				}
				break;
			};
			break;
		case TELOPT.DO:
			switch(dat[1])
			{
			case TELOPT.TERMTYPE:
				break;
			case TELOPT.MSP:
				if (this.neverSupportMSP)
				{
					if (this.MSPsupport)
					{
						response = response.concat([TELOPT.IAC, TELOPT.WONT, TELOPT.MSP]);
						this.MSPsupport = false;
					}
				}
				else
				if (!this.MSPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.WILL, TELOPT.MSP]);
					this.MSPsupport = true;
				}
				break;
			case TELOPT.GMCP:
				if (this.neverSupportGMCP)
				{
					if (this.GMCPsupport)
					{
						response = response.concat([TELOPT.IAC, TELOPT.WONT, TELOPT.GMCP]);
						this.GMCPsupport = false;
					}
				}
				else
				if (!this.GMCPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.WILL, TELOPT.GMCP]);
					this.GMCPsupport = true;
				}
				break;
			case TELOPT.LOGOUT:
				// good for you serverdude
				break;
			case TELOPT.MXP:
				if (this.neverSupportMXP)
				{
					if (this.MXPsupport)
					{
						response = response.concat([TELOPT.IAC, TELOPT.WONT, TELOPT.MXP]);
						this.MXPsupport = false;
					}
				}
				else
				if (!this.MXPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.WILL, TELOPT.MXP]);
					this.MXPsupport = true;
				}
				break;
			default:
				if (dat[1] != TELOPT.BINARY)
					response = response.concat([TELOPT.IAC, TELOPT.WONT, dat[1]]);
				break;
			}
			break;
		case TELOPT.DONT:
			switch(dat[1])
			{
			case TELOPT.MSP:
				if (this.MSPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.WONT, TELOPT.MSP]);
					this.MSPsupport = false;
				}
				break;
			case TELOPT.MXP:
				if (this.MXPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.WONT, TELOPT.MXP]);
					this.MXPsupport = false;
					//TODO: if (mxpModule != null) mxpModule.shutdownMXP();
				}
			break;
			case TELOPT.GA:
			case TELOPT.NOP:
				break;
			}
			break;
		default:
			break;
		}
		return new Uint8Array(response).buffer;
	}
}
