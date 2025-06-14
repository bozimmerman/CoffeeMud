var inputAreaHeight = 70;
var inputSpanArea = null;
var inputTextArea = null;
var inputbacklog = [];
var inputbacklogindex = -1;
var inputcursor = null;

var AsciiKeyMap = 
{
	Tab: 9,
	Enter: 13,
	Escape: 27,
	ArrowUp: "\x1B[A",
	ArrowDown: "\x1B[B",
	ArrowLeft: "\x1B[D",
	ArrowRight: "\x1B[C",
	Home: "\x1B[H",
	End: "\x1B[F",
	Insert: "\x1B[2~"
};

function ConfigureInput(obj, ta)
{
	obj.style.position='fixed';
	obj.style.bottom=0;
	obj.style.width='100%';
	obj.style.height=inputAreaHeight+'px';
	obj.style.background='$f0f0f0';
	inputSpanArea = obj;
	inputTextArea = ta;
	inputTextArea.style.width='calc(100vw - 10px)';
	inputTextArea.style.margin='5px';
	inputTextArea.style.padding='5px';
	inputTextArea.onkeypress=inputKeyPress;
	inputTextArea.onkeydown=inputKeyDown;
	inputTextArea.style.resize='none';
}

function setInputVisibility(yn)
{
	if(yn)
	{
		inputSpanArea.style.visibility = 'visible';
		window.windowArea.style.height=window.winAreaHeight;
	}
	else
	{
		inputSpanArea.style.visibility = 'hidden';
		var winAreaHeight = 'calc(100% - '+(window.winAreaTop)+'px)';
		window.windowArea.style.height=winAreaHeight;
	}
}

function isInputVisible()
{
	return inputSpanArea.style.visibility == 'visible';
}

function inputSubmit(x)
{
	var max = getConfig('window/input_buffer',500);
	while(inputbacklog.length>max)
		inputbacklog.splice(0,1);
	if(x.length>0)
	{
		inputbacklog.push(x);
		inputbacklogindex=inputbacklog.length;
	}
	if(x && window.currWin != null)
	{
		x = window.currWin.aliasProcess(x);
		if(!x)
			return;
	}
	if(window.currWin)
		window.currWin.submitInput(x);
	else
		window.alert("You need to connect first!");
}

function inputKeyPress(e)
{
	if(!isInputVisible())
		return;
	if(e.keyCode == 13)
	{
		e.preventDefault();
		inputSubmit(inputTextArea.value);
		inputTextArea.value='';
	}
}

function sendOneLine(txt)
{
	if(txt != null)
	{
		txt = txt.replaceAll('\n','%0D').replaceAll('\r','');
		window.currWin.submitInput(txt);
	}
}

function inputKeyDown(e)
{
	if(!isInputVisible())
		return;
	var x = e.keyCode;
	if(x == 38) // up
	{
		if(inputbacklogindex>0)
		{
			if((e.shiftKey)&&(inputbacklogindex>=0)&&(inputbacklogindex<inputbacklog.length))
				inputTextArea.value=inputbacklog[inputbacklogindex];
			inputbacklogindex--;
			if((inputTextArea.value=='')
			||((inputbacklogindex<inputbacklog.length-1)
				&&inputTextArea.value==inputbacklog[inputbacklogindex+1]))
				inputTextArea.value=inputbacklog[inputbacklogindex];
		}
	}
	else
	if(x == 40) // down
	{
		if(inputbacklogindex<inputbacklog.length-1)
		{
			if((e.shiftKey)&&(inputbacklogindex>=0)&&(inputbacklogindex<inputbacklog.length))
				inputTextArea.value=inputbacklog[inputbacklogindex];
			inputbacklogindex++;
			if((inputTextArea.value=='')
			||((inputbacklogindex>0)
				&&inputTextArea.value==inputbacklog[inputbacklogindex-1]))
				inputTextArea.value=inputbacklog[inputbacklogindex];
		}
	}
	else
	if((x == 13) && (e.shiftKey))
	{
		inputTextArea.value += '\n';
		e.preventDefault();
	}
	else
	if(x == 10)
		e.preventDefault();
}

function addToPrompt(x,att)
{
	inputTextArea.value='';
	if(att)
		inputSubmit(x);
	else
	if(!x)
		return;
	else
	if(!isInputVisible())
	{
		if(x) x += ' ';
		for(var i=0;i<x.length;i++)
		{
			var fakeE = {
				ctrlKey: false,
				altKey: false,
				key: x[i]
			};
			document.onkeydown(fakeE);
		}
	}
	else
		inputTextArea.value=x+" ";
}

function setInputBoxFocus() 
{
	setTimeout(function() {
		if(!isInputVisible())
		{
			DisplayFakeInput(null);
			return;
		}
		inputTextArea.focus();
	}, 100);
}

function DisplayFakeInput(key)
{
	if((!window.currWin) || (!window.currWin.window))
		return;
	if(isInputVisible())
		return;
	if(inputcursor != null)
	{
		if(inputcursor.parentNode != null)
			inputcursor.parentNode.removeChild(inputcursor);
	}
	if(key != null)
	{
		if(key == 'Backspace')
		{
			var node = window.currWin.window.lastChild;
			while(node)
			{
				if((node.nodeName === 'SPAN')
				&& (node.name === 'INPUT'))
				{
					if(node.innerHTML.toLowerCase() != '<br>')
						window.currWin.window.removeChild(node);
					break; // we are done
				}
				node = node.previousSibling;
			}
		}
		else
		{
			var span = window.currWin.displayBit(key);
			span.name = 'INPUT';
			span.style.color = "white";
			span.style.backgroundColor = 'black';
			span.style.visibility = 'visible';
		}
	}
	if(inputcursor == null)
	{
		const cursor = document.createElement("span");
		cursor.id = "INPUTCURSOR";
		cursor.textContent = "|";
		cursor.style.display = "inline";
		cursor.style.fontFamily = "monospace";
		cursor.style.fontSize = "16px";
		cursor.style.color = "white";
		cursor.style.visibility = 'visible';
		inputcursor = cursor;
		setInterval(() => {
			var isCursorVisible = cursor.style.visibility == 'visible';
			cursor.style.visibility = isCursorVisible ? "hidden" : "visible";
		}, 500);
	} 
	window.currWin.window.appendChild(inputcursor);
}

document.onkeydown = function(e) {
	if(e.altKey)
	{
		if(e.key == 'o')
		{
			var win = window.currWin;
			if(window.isElectron && win)
			{
				if(win.logStream != null)
					win.closeLog();
				else
				{
					var captureFilename;
					if(win.pb && win.pb.capture)
						captureFilename = win.pb.capture;
					else
					{
						var os = require('os');
						var osPath = require('path');
						captureFilename = osPath.join(os.tmpdir(),Siplet.NAME.toLowerCase()+'.log');
					}
					win.openLog(captureFilename);
				}
			}
		}
	}
	else
	if(!isInputVisible() && !isDialogOpen())
	{
		if((e.key === 'Backspace')
		&&(window.currWin.pb)
		&&(window.currWin.pb.bsCode))
		{
			DisplayFakeInput(e.key);
			window.currWin.sendRaw([window.currWin.pb.bsCode]);
		}
		else
		if(e.key.length == 1)
		{
			if(e.ctrlKey)
			{
				var ctrlCode = e.key.toLowerCase().charCodeAt(0)-96;
				if((ctrlCode>0)&&(ctrlCode<27))
				{
					if(ctrlCode == 7)
					{
						var audio = new Audio('images/ding.wav');
						audio.play();
					}
					window.currWin.sendRaw([ctrlCode]);
				}
			}
			else
			{
				DisplayFakeInput(e.key);
				window.currWin.sendRaw([e.key.charCodeAt(0)]);
			}
		}
		else
		if(AsciiKeyMap[e.key])
		{
			var keyCode = AsciiKeyMap[e.key];
			if(typeof keyCode === 'string')
			{
				window.currWin.sendStr(keyCode);
				e.preventDefault();
			}
			else
			if(keyCode == 13)
			{
				window.currWin.sendRaw([keyCode]);
				DisplayFakeInput('\n');
			}
			else
			if(keyCode == 9)
			{
				window.currWin.sendRaw([keyCode]);
				DisplayFakeInput('\t');
			}
			//TODO bell? 7?
		}
	}
};
