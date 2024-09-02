function like() {
	document.getElementById('likeYesResponse').removeAttribute('hidden');
	document.getElementById('likeNoResponse').setAttribute('hidden', '');
}

function dislike() {
	document.getElementById('likeNoResponse').removeAttribute('hidden');
	document.getElementById('likeYesResponse').setAttribute('hidden', '');
}