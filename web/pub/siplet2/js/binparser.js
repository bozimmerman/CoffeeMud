var BPTYPE =
{
	TEXT: 0,
	ANSI: 1,
	TELNET: 2
};

var BPSTATE = 
{
	OUTER: 0,
	ANSI: 1,
	ANSI2: 2,
	ANSI3: 3,
	TELNET: 4,
	TELNET2: 5,
	TELNETSB: 6,
};

var BPCODE =
{
	ESC: 27,
	IAC: 0xFF,
	TELNET_BINARY: 0,
	TELNET_ECHO: 1,
	TELNET_LOGOUT: 18,
	TELNET_SUPRESS_GO_AHEAD: 3,
	TELNET_TERMTYPE: 24,
	TELNET_NAWS: 31,
	TELNET_TOGGLE_FLOW_CONTROL: 33,
	TELNET_LINEMODE: 34,
	TELNET_MSDP: 69,
	TELNET_MSSP: 70,
	TELNET_COMPRESS: 85,
	TELNET_COMPRESS2: 86,
	TELNET_MSP: 90,
	TELNET_MXP: 91,
	TELNET_AARD: 102,
	TELNET_SE: 0xF0,
	TELNET_AYT: 0xF6,
	TELNET_EC: 0xF7,
	TELNET_ATCP: 0xC8,
	TELNET_GMCP: 0xC9,
	TELNET_SB: 0xFA,
	TELNET_WILL: 0xFB,
	TELNET_WONT: 0xFC,
	TELNET_ANSI16: 0xFC,
	TELNET_DO: 0xFD,
	TELNET_ANSI: 0xFD,
	TELNET_DONT: 0xFE,
	TELNET_GA: 0xF9,
	TELNET_NOP: 0xF1
};

var BPENTRY = function()
{
	this.type = BPTYPE.TEXT;
	this.data = [];
	this.done = false;
};

var BPPARSE = function()
{
	this.entries = [];
	this.data = [];
	this.prev = '\0';
	this.state = BPSTATE.OUTER;
};

function binparse(block)
{
	var entries = block.entries;
	if (entries.length == 0)
		entries.push(new BPENTRY());
	var curr = entries[entries.length-1];
	var i=-1;
	var c=block.prev;
	while(++i < block.data.length)
	{
		prev = c;
		c = block.data[i];
		switch(block.state)
		{
		case BPSTATE.OUTER:
			if(c==BPCODE.ESC)
				block.state = BPSTATE.ANSI;
			else if(c==BPCODE.IAC)
				block.state = BPSTATE.TELNET;
			else
			if((c & 0xFF00) > 0)
			{
				curr.data.push((c & 0xFF00)>>8);
				curr.data.push(c & 0xFF);
			}
			else
				curr.data.push(c);
			break;
		case BPSTATE.ANSI:
			if(c=='[')
			{
				block.state = BPSTATE.ANSI2;
				curr.done = true;
				curr = new BPENTRY();
				block.entries.push(curr);
				curr.type = BPTYPE.ANSI;
			}
			else
			{
				block.state = BPSTATE.OUTER;
				curr.data.push(27);
				curr.data.push(c);
			}
			break;
		case BPSTATE.ANSI2:
			curr.data.push(c);
			if(c=='"')
				block.state = BPSTATE.ANSI3;
			else
			if((c=='m') || (c=='z'))
			{
				block.state = BPSTATE.OUTER;
				curr.done=true;
				curr = new BPENTRY();
				block.entries.push(curr);
			}
			break;
		case BPSTATE.ANSI3:
			curr.data.push(c);
			if(c=='"')
				block.state = BPSTATE.ANSI2;
			else
			if((curr.data.length>128)
			||(!(((c>='a')&&(c<='z'))||((c>='A')&&(c<='Z')))))
			{
				//something went very very wrong.  kill with fire.
				block.state = BPSTATE.OUTER;
				curr.type=BPTYPE.TEXT;
				curr.data=[];
				i--;
			}
			break;
		case BPSTATE.TELNET:
			switch(c)
			{
			case BPCODE.IAC: // double escape?  Just stay here..
				break;
			case BPCODE.TELNET_SB:
				curr.done = true;
				curr = new BPENTRY();
				block.entries.push(curr);
				curr.type = BPTYPE.TELNET;
				curr.data.push(c);
				block.state = BPSTATE.TELNETSB;
				break;
			case BPCODE.TELNET_WILL:
			case BPCODE.TELNET_WONT:
			case BPCODE.TELNET_DO:
			case BPCODE.TELNET_DONT:
				curr.done = true;
				curr = new BPENTRY();
				block.entries.push(curr);
				curr.type = BPTYPE.TELNET;
				curr.data.push(c);
				block.state = BPSTATE.TELNET2;
				break;
			case BPCODE.TELNET_GA:
			case BPCODE.TELNET_NOP:
				// just eat it
				block.state = BPSTATE.OUTER;
				break;
			default:
				curr.data.push(BPCODE.IAC);
				curr.data.push(c);
				block.state = BPSTATE.OUTER;
				break;
			}
			break;
		case BPSTATE.TELNET2:
			curr.data.push(c);
			curr.done = true;
			curr = new BPENTRY();
			block.entries.push(curr);
			block.state = BPSTATE.OUTER;
			break;
		case BPSTATE.TELNETSB:
			if(block.prev == BPCODE.IAC)
			{
				if(c == BPCODE.IAC)
					curr.data.push(c);
				else
				if(c == BPCODE.TELNET_EC)
				{
					curr.done = true;
					curr = new BPENTRY();
					block.entries.push(curr);
					block.state = BPSTATE.OUTER;
				}
			}
			else
				curr.data.push(c);
			break;
		}
		
	}
	if((curr.type == BPTYPE.TEXT) && (curr.data.length > 0))
		curr.done = true;
	return bits;
}