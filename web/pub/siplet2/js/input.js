var inputAreaHeight = 70;
var inputTextArea = null;
function configureInput(obj, ta)
{
	obj.style.position='fixed';
	obj.style.bottom=0;
	obj.style.width='100%';
	obj.style.height=inputAreaHeight+'px';
	obj.style.background='$f0f0f0';
	inputTextArea = ta;
	inputTextArea.style.width='calc(100vw - 10px)';
	inputTextArea.style.margin='5px';
	inputTextArea.style.padding='5px';
}

function inputKeyPress(i,e)
{
    if(e.keyCode == 13)
    {
        e.preventDefault();
        window.currentSiplet.submitInput(i.value);
        i.value='';
    }
}

function addToPrompt(x,att)
{
    inputTextArea.value='';
    if(att)
        window.currentSiplet.submitInput(x);
    else
        inputTextArea.value=x+" ";
}

function boxFocus() 
{
    setTimeout(function() {
        inputTextArea.focus();
    }, 100);
}
