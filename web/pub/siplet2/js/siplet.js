var Siplet =
{
	VERSION_MAJOR: 3.0,
	VERSION_MINOR: 0
};

function SipletWindow(windowName)
{
	this.siplet = Siplet;
	this.width = 80;
	this.height = 25;
	this.maxLines = 5000;
	this.MSPsupport = false;
	this.MSDPsupport = false;
	this.GMCPsupport = false;
	this.MXPsupport = false;
	this.MCCPsupport = false;
	this.wsopened = false;
	this.windowName = windowName;
	this.bin = new BPPARSE(false);
	this.ansi = new ANSISTACK();
	this.telnet = new TELNET(this);
	this.msp = new MSP(this);
	this.mxp = new MXP(this);
	this.text = new TEXT([this.mxp,this.msp]);
	this.window = document.getElementById(windowName);
	this.wsocket = null;
	
	
	this.connect = function(url)
	{
		this.wsocket = new WebSocket(url);
		this.wsocket.binaryType = "arraybuffer";
	};
}

var nextSipletSpanId = 0;

