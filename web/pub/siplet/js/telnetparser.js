var TELOPT =
{
	IAC: 0xFF,
	WILL: 0xFB,
	WONT: 0xFC,
	DO: 0xFD,
	DONT: 0xFE,
	ESC: 27,
	ECHO: 1,
	LOGOUT: 18,
	CHARSETS: 42,
	SUPRESS_GO_AHEAD: 3,
	TTYPE: 24,
	NAWS: 31,
	TOGGLE_FLOW_CONTROL: 33,
	NEWENVIRON: 39,
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
	ANSI16: 0xFC,
	ANSI: 0xFD,
	GA: 0xF9,
	NOP: 0xF1,
	BINARY: 0,
	ECHO: 1,
	LOGOUT: 18,
	ZMP: 0x5D
};

var MSDPOP =
{
	MSDP_VAR: 1,
	MSDP_VAL: 2,
	MSDP_TABLE_OPEN: 3,
	MSDP_TABLE_CLOSE: 4,
	MSDP_ARRAY_OPEN: 5,
	MSDP_ARRAY_CLOSE: 6
};

var NEWENV =
{
	IS: 0,
	SEND: 1,
	INFO: 2,
	VAR: 0,
	VALUE: 1,
	ESC: 2,
	USERVAR: 3
};

var StringToAsciiArray = function(str) {
	var arr = [];
	for(var i=0;i<str.length;i++)
		arr.push(str.charCodeAt(i));
	return arr;
};

var TELNET = function(sipwin)
{
	this.debug = false;
	this.mttsBitmap = 1 | 4 | 8 | 256;
	this.termType = "ANSI-TRUECOLOR";
	this.ttypeCount = 0;
	this.localEcho = true;

	this.reset = function()
	{
		this.neverSupportMSP = !(getConfig("window/term/msp",'true') === 'true');
		this.neverSupportMXP = !(getConfig("window/term/mxp",'true') === 'true');
		this.neverSupportMSDP= !(getConfig("window/term/msdp",'true') === 'true');
		this.neverSupportGMCP = !(getConfig("window/term/gmcp",'true') === 'true');
		this.neverSupportMCCP = !(getConfig("window/term/mccp",'true') === 'true');
		this.sentNaws = false;
		this.ttypeCount = 0;
		this.localEcho = true;
	};
	this.reset();
	
	this.process = function(dat)
	{
		if(this.debug) logDat('teln', dat, TELOPT);
		var response = [];
		//var s='>' + new Date().getTime()+'>';for(var i=0;i<dat.length;i++)s+=dat[i]+',';console.log(s);
		switch (dat[0])
		{
		case TELOPT.SB:
		{
			var subOptionData = [];
			var subOptionCode = dat[1];
			var c = 0;
			var i=1;
			while ((i < (dat.length - 1)) && ((c = dat[++i]) != -1))
			{
				if ((c == TELOPT.IAC) && (i < (dat.length - 1)))
				{
					c = dat[++i];
					if (c == TELOPT.IAC)
						subOptionData.push(TELOPT.IAC);
					else
					if (c == TELOPT.SE)
						break;
				}
				else
					subOptionData.push(c);
			}
			if (subOptionCode == TELOPT.TTYPE)
			{
				if(subOptionData.length > 1)
				{
					if(subOptionData[0] == 0) // illegal!
						break;
					if(subOptionData[0] == 1)
					{
						var d = subOptionData;
						d.splice(0,1);
						if(d.length && (d[d.length-1]==255))
							d.splice(d.length-1,1);
						if(d.length)
						{
							var decoder = new TextDecoder("utf-8");
							var ds = '';
							try {
								ds=decoder.decode(d);
							} catch(e) {
							}
							if(this.debug && ds) console.log('TTYPE SEND: ' + ds);
						}
					}
				}
				response = response.concat(this.buildTType());
			}
			else
			if (subOptionCode == TELOPT.NAWS)
				response = response.concat(this.getNaws());
			else
			if((subOptionCode == TELOPT.COMPRESS2)||(subOptionCode == TELOPT.COMPRESS))
			{
				if(sipwin.MCCPsupport)
					sipwin.decompressor = new pako.Inflate({raw:false});
			}
			else
			if (subOptionCode == TELOPT.MSDP)
			{
				if(subOptionData.length > 1)
				{
					if(subOptionData[subOptionData.length-1] == 255)
						subOptionData.splice(subOptionData.length-1);
					var received = this.msdpReceive(subOptionData);
					sipwin.dispatchEvent({type: 'msdp',data:received});
				}
			}
			else
			if (subOptionCode == TELOPT.NEWENVIRON)
			{
				var vartypes = [NEWENV.VAR, NEWENV.USERVAR];
				if(subOptionData.length > 1)
				{
					var k = subOptionData.shift();
					if(k == NEWENV.SEND)
					{
						var minLength = 0;
						while(subOptionData.length>=minLength)
						{
							minLength = 2;
							var varName = '';
							var c;
							var typ;
							if(subOptionData.length>0)
							{
								if((subOptionData[0]==NEWENV.IS)&&(subOptionData.length>1))
									subOptionData.shift();
								if((vartypes.indexOf(typ=subOptionData.shift())) < 0)
									break; // exit and do nothing
								while(subOptionData.length>0)
								{
									if(vartypes.indexOf(subOptionData[0])>=0)
										break; // done, so process it
									c=subOptionData.shift();
									if((c == NEWENV.ESC) && (subOptionData.length>0))
										varName += String.fromCharCode(subOptionData.shift()).toUpperCase();
									else
									if(c>=32)
										varName += String.fromCharCode(c).toUpperCase();
								}
							}
							response = response.concat([TELOPT.IAC, TELOPT.SB, TELOPT.NEWENVIRON, NEWENV.IS]);
							if((varName == 'USER')||(varName.length == 0))
								response = response.concat(this.newEnvironVar(typ,"USER",(sipwin.pb && sipwin.pb.user)?sipwin.pb.user:''));
							if((varName == 'ACCT')||(varName.length == 0))
								response = response.concat(this.newEnvironVar(typ,"ACCT",(sipwin.pb && sipwin.pb.accountName)?sipwin.pb.accountName:''));
							if((varName == 'JOB')||(varName.length == 0))
							{
								if(!sipwin.jobid)
								{
									sipwin.jobid = '';
									for(var i=0;i<16;i++)
										sipwin.jobid += String.fromCharCode(Math.floor(Math.random()*94)+33);
								}
								response = response.concat(this.newEnvironVar(typ,"JOB",sipwin.jobid));
							}
							if((varName == 'CHARSET')||(varName.length == 0))
								response = response.concat('UTF-8');
							if((varName == 'CLIENT_NAME')||(varName.length == 0))
								response = response.concat(this.newEnvironVar(typ,"CLIENT_NAME",Siplet.NAME));
							if((varName == 'CLIENT_VERSION')||(varName.length == 0))
								response = response.concat(this.newEnvironVar(typ,"CLIENT_VERSION",''+Siplet.VERSION_MAJOR));
							if((varName == 'MTTS')||(varName.length == 0))
								response = response.concat(this.newEnvironVar(typ,"MTTS",''+this.mttsBitmap));
							if((varName == 'TERMINAL_TYPE')||(varName.length == 0))
								response = response.concat(this.newEnvironVar(typ,"TERMINAL_TYPE",this.termType));
							response = response.concat([TELOPT.IAC, TELOPT.SE]);
						} // get the value
					}
					else
					if((k == NEWENV.IS)||(k==NEWENV.INFO))
					{
						if(vartypes.indexOf(subOptionData.shift) < 0)
							break;
						while(subOptionData.length > 1)
						{
							while((subOptionData.length>0)
							&&(subOptionData.shift != NEWENV.VALUE))
							{}; // get the var name
							while((subOptionData.length>0)
							&&(vartypes.indexOf(subOptionData.shift) < 0))
							{}; // get the value
						}
					}
				}
			}
			else
			if (subOptionCode == TELOPT.GMCP)
			{
				if(subOptionData.length > 1)
				{
					if(subOptionData[subOptionData.length-1] == 255)
						subOptionData.splice(subOptionData.length-1);
					var received = this.gmcpReceive(subOptionData);
					var x=received.indexOf(' ');
					var e = {type: 'gmcp', command:'?', data:received};
					if(x>0)
					{
						var cmd = received.substring(0,x);
						var jsonStr = received.substring(x+1).trim();
						e = {type: 'gmcp', command:cmd, data:jsonStr};
						try { e.data = JSON.parse(jsonStr); } catch(ee) {}
						InvokeGMCP(sipwin, cmd, e.data);
					}
					sipwin.dispatchEvent(e);
				}
			}
			break;
		}
		case TELOPT.WILL:
			switch(dat[1])
			{
			case TELOPT.ZMP:
				response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.ZMP]);
				break;
			case TELOPT.EOR:
				response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.EOR]);
				break;
			case TELOPT.AARD:
				response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.AARD]);
				break;
			case TELOPT.MSSP:
				response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.MSSP]);
				break;
			case TELOPT.CHARSETS:
				response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.CHARSETS]);
				break;
			case TELOPT.NAWS:
				response = response.concat(this.getNaws());
				break;
			case TELOPT.ECHO:
				response = response.concat([TELOPT.IAC, TELOPT.DO, TELOPT.ECHO]);
				this.localEcho = false;
				break;
			case TELOPT.MSP:
				if (this.neverSupportMSP)
				{
					if (sipwin.MSPsupport)
					{
						response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.MSP]);
						sipwin.MSPsupport = false;
					}
				}
				else
				if (!sipwin.MSPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.DO, TELOPT.MSP]);
					sipwin.MSPsupport = true;
				}
				break;
			case TELOPT.MSDP:
				if (this.neverSupportMSDP)
				{
					if (sipwin.MSDPsupport)
					{
						response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.MSDP]);
						sipwin.MSDPsupport = false;
					}
				}
				else
				if (!sipwin.MSDPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.DO, TELOPT.MSDP]);
					sipwin.MSDPsupport = true;
				}
				break;
			case TELOPT.GMCP:
				if (this.neverSupportGMCP)
				{
					if (sipwin.GMCPsupport)
					{
						response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.GMCP]);
						sipwin.GMCPsupport = false;
					}
				}
				else
				if (!sipwin.GMCPsupport)
				{
					sipwin.GMCPsupport =true;
					response = response.concat([TELOPT.IAC, TELOPT.DO, TELOPT.GMCP]);
					response = response.concat(BuildGMCPHello(sipwin));
				}
				break;
			case TELOPT.COMPRESS:
			case TELOPT.COMPRESS2:
				if (this.neverSupportMCCP)
				{
					if (sipwin.MCCPsupport)
					{
						response = response.concat([TELOPT.IAC, TELOPT.DONT, dat[1]]);
						sipwin.MCCPsupport = false;
					}
				}
				else
				if (!sipwin.MCCPsupport)
				{
					sipwin.MCCPsupport =true;
					response = response.concat([TELOPT.IAC, TELOPT.DO, dat[1]]);
				}
				break;
			case TELOPT.MXP:
				if (this.neverSupportMXP)
				{
					if (sipwin.MXPsupport)
					{
						response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.MXP]);
						sipwin.MXPsupport = false;
					}
				}
				else
				if (!sipwin.MXPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.DO, TELOPT.MXP]);
					sipwin.MXPsupport = true;
				}
				break;
			case TELOPT.COMPRESS2:
				if (this.neverSupportMCCP)
				{
					if (sipwin.MCCPsupport)
					{
						response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.MCCP]);
						sipwin.MCCPsupport = false;
					}
				}
				else
				if (!sipwin.MCCPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.DO, TELOPT.MCCP]);
					sipwin.MCCPsupport = false;
					//TODO: it cant get here, but when it does, fix it.
				}
				break;
			case TELOPT.LOGOUT:
				// goot for you serverdude
				break;
			case TELOPT.BINARY:
				response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.BINARY]);
				break;
			default:
				response = response.concat([TELOPT.IAC, TELOPT.DONT, dat[1]]);
				break;
			};
			break;
		case TELOPT.WONT:
			switch(dat[1])
			{
			case TELOPT.MSP:
				if (sipwin.MSPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.MSP]);
					sipwin.MSPsupport = false;
				}
				break;
			case TELOPT.MSDP:
				if (sipwin.MSDPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.MSDP]);
					sipwin.MSDPsupport = false;
				}
				break;
			case TELOPT.GMCP:
				if (sipwin.GMCPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.GMCP]);
					sipwin.GMCPsupport = false;
				}
				break;
			case TELOPT.MXP:
				if (sipwin.MXPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.MXP]);
					sipwin.MXPsupport = false;
					//if (mxpModule != null) mxpModule.shutdownMXP();
				}
				break;
			case TELOPT.ECHO:
				response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.ECHO]);
				this.localEcho = true;
				break;
			};
			break;
		case TELOPT.DO:
			switch(dat[1])
			{
			case TELOPT.LINEMODE:
			{
				var inputVis = getConfig('window/disableInput','')==''?true:false;
				if(sipwin.pb)
					inputVis = !sipwin.pb.disableInput;
				if(inputVis)
					response = response.concat([TELOPT.IAC, TELOPT.WILL, TELOPT.LINEMODE]);
				else
					response = response.concat([TELOPT.IAC, TELOPT.WONT, TELOPT.LINEMODE]);
				break;
			}
			case TELOPT.NAWS:
				response = response.concat(this.getNaws());
				break;
			case TELOPT.SUPRESS_GO_AHEAD:
				response = response.concat([TELOPT.IAC, TELOPT.WILL, TELOPT.SUPRESS_GO_AHEAD]);
				break;
			case TELOPT.TTYPE:
				response = response.concat([TELOPT.IAC, TELOPT.WILL, TELOPT.TTYPE]);
				response = response.concat(this.buildTType());
				break;
			case TELOPT.MSP:
				if (this.neverSupportMSP)
				{
					if (sipwin.MSPsupport)
					{
						response = response.concat([TELOPT.IAC, TELOPT.WONT, TELOPT.MSP]);
						sipwin.MSPsupport = false;
					}
				}
				else
				if (!sipwin.MSPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.WILL, TELOPT.MSP]);
					sipwin.MSPsupport = true;
				}
				break;
			case TELOPT.GMCP:
				if (this.neverSupportGMCP)
				{
					if (sipwin.GMCPsupport)
					{
						response = response.concat([TELOPT.IAC, TELOPT.WONT, TELOPT.GMCP]);
						sipwin.GMCPsupport = false;
					}
				}
				else
				if (!sipwin.GMCPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.WILL, TELOPT.GMCP]);
					sipwin.GMCPsupport = true;
					response = response.concat(BuildGMCPHello(sipwin));
				}
				break;
			case TELOPT.COMPRESS:
			case TELOPT.COMPRESS2:
				if (this.neverSupportMCCP)
				{
					if (sipwin.MCCPsupport)
					{
						response = response.concat([TELOPT.IAC, TELOPT.WONT, dat[1]]);
						sipwin.MCCPsupport = false;
					}
				}
				else
				if (!sipwin.MCCPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.WILL, dat[1]]);
					sipwin.MCCPsupport = true;
				}
				break;
			case TELOPT.LOGOUT:
				// good for you serverdude
				break;
			case TELOPT.MXP:
				if (this.neverSupportMXP)
				{
					if (sipwin.MXPsupport)
					{
						response = response.concat([TELOPT.IAC, TELOPT.WONT, TELOPT.MXP]);
						sipwin.MXPsupport = false;
					}
				}
				else
				if (!sipwin.MXPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.WILL, TELOPT.MXP]);
					sipwin.MXPsupport = true;
				}
				break;
			case TELOPT.NEWENVIRON:
				response = response.concat([TELOPT.IAC, TELOPT.WILL, TELOPT.NEWENVIRON]);
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
				if (sipwin.MSPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.WONT, TELOPT.MSP]);
					sipwin.MSPsupport = false;
				}
				break;
			case TELOPT.MXP:
				if (sipwin.MXPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.WONT, TELOPT.MXP]);
					sipwin.MXPsupport = false;
					//if (mxpModule != null) mxpModule.shutdownMXP();
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
		//var s='<' + new Date().getTime()+'<';for(var i=0;i<response.length;i++)s+=response[i]+',';console.log(s);
		if(this.debug && response.length)
			logDat('resp', response, TELOPT);
		return new Uint8Array(response).buffer;
	};

	this.getNaws = function()
	{
		this.sentNaws = true;
		return [
			TELOPT.IAC, TELOPT.SB, TELOPT.NAWS,
				0,sipwin.width,0,sipwin.height,
			TELOPT.IAC, TELOPT.SE
		];
	};
	
	this.sendNaws = function(resend)
	{
		if(resend && !this.sentNaws)
			return;
		sipwin.sendRaw(this.getNaws());
	};

	this.newEnvironVar =function(typ, key, value)
	{
		var response = [];
		if(value)
		{
			response = response.concat([typ]);
			response = response.concat(StringToAsciiArray(key));
			response = response.concat([NEWENV.VALUE]);
			response = response.concat(StringToAsciiArray(value));
		}
		return response;
	};
	
	this.gmcpReceive = function(buffer)
	{
		var s = '';
		for (var i=0; i <buffer.length; i++)
			s += String.fromCharCode( buffer[i]);
		return s;
	};
	
	this.msdpReceive = function(buffer)
	{
		var map = {};
		var stack = [];
		stack.push({});
		var str = null;
		var v = null;
		var valVar = null;
		var x = -1;
		var M = null;
		while (++x < dataSize)
		{
			switch (buffer[x])
			{
			case MSDPOP.MSDP_VAR: // start a string
				str = '';
				v = str;
				if (Array.isArray(stack[stack.length-1]))
					stack[stack.length-1].push(str);
				else
					stack[stack.length-1][str] = '';
				break;
			case MSDPOP.MSDP_VAL:
				valVar = v;
				v = null;
				str = '';
				break;
			case MSDPOP.MSDP_TABLE_OPEN: // open a table
				M = {};
				if (Array.isArray(stack[stack.length-1]))
					stack[stack.length-1].push(M);
				else
				if(varVal != null)
					stack[stack.length-1][valVar] = M;
				valVar = null;
				stack.push(M);
				break;
			case MSDPOP.MSDP_TABLE_CLOSE: // done with table
				if((stack.length > 1)&&(!Array.isArray(stack[stack.length-1])))
					stack.pop();
				break;
			case MSDPOP.MSDP_ARRAY_OPEN: // open an array
				M = [];
				if (Array.isArray(stack[stack.length-1]))
					stack[stack.length-1].push(M);
				else
				if (valVar != null)
					stack[stack.length-1][valVar]= M;
				valVar = null;
				stack.push(M);
				break;
			case MSDPOP.MSDP_ARRAY_CLOSE: // close an array
				if((stack.length > 1)&&(Array.isArray(stack[stack.length-1])))
					stack.pop();
				break;
			default:
				if (Array.isArray(stack[stack.length-1]))
				{
					if(!In(str, stack[stack.length-1]))
						stack[stack.length-1].push(str);
				}
				else
					stack[stack.length-1][valVar]= str;
				valVar = null;
				if (str != null)
					str += String.fromCharCode(buffer[x]);
				break;
			}
		}
		return JSON.stringify(stack[0]);
	};
	
	this.buildTType = function()
	{
		var response = [TELOPT.IAC, TELOPT.SB, TELOPT.TTYPE, 0];
		if(this.ttypeCount == 0)
			response = response.concat(StringToAsciiArray(Siplet.NAME.toUpperCase()));
		else
		if(this.ttypeCount == 1)
			response = response.concat(StringToAsciiArray(this.termType));
		else
		if(this.ttypeCount > 1)
		{
			response = response.concat(StringToAsciiArray("MTTS "+this.mttsBitmap));
		}
		this.ttypeCount++;
		response = response.concat([TELOPT.IAC, TELOPT.SE]);
		return response;
	}
};

var updateTelnetOptions = function() {
	for(var i=0;i<window.siplets.length;i++)
	{
		var siplet = window.siplets[i];
		siplet.telnet.neverSupportMSP = !(getConfig("window/term/msp",'true') === 'true');
		siplet.telnet.neverSupportMXP = !(getConfig("window/term/mxp",'true') === 'true');
		siplet.telnet.neverSupportMSDP= !(getConfig("window/term/msdp",'true') === 'true');
		siplet.telnet.neverSupportGMCP = !(getConfig("window/term/gmcp",'true') === 'true');
		siplet.telnet.neverSupportMCCP = !(getConfig("window/term/mccp",'true') === 'true');
    }
}
addConfigListener('window/term/msp', updateTelnetOptions);
addConfigListener('window/term/mxp', updateTelnetOptions);
addConfigListener('window/term/msdp', updateTelnetOptions);
addConfigListener('window/term/gmcp', updateTelnetOptions);
addConfigListener('window/term/mccp', updateTelnetOptions);
