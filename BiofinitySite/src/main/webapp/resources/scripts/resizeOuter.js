// for cross-domain resizing
function resizeFrame(frameId, newHeight) {
	document.getElementById(frameId).height = (parseInt(newHeight) + 10) + 'px';
}

// for same-domain resizing
function resizeLocalFrame(frameId) {
	var h = document.getElementById(frameId).contentWindow.document.body.scrollHeight + 10;
	document.getElementById(frameId).height = h + 'px';
}
