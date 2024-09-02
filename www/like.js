var disliked = false;


function like() {
	disliked = false;
	player.pauseVideo();
	document.getElementById('likeYesResponse').removeAttribute('hidden');
	document.getElementById('likeNoResponse').setAttribute('hidden', '');
}


function dislike() {
	disliked = true;
	document.getElementById('likeNoResponse').removeAttribute('hidden');
	document.getElementById('likeYesResponse').setAttribute('hidden', '');
	player.playVideo();
	updateVolume();
	
	document.addEventListener('fullscreenchange', event => {
		if(!document.fullscreenElement) {
			document.getElementById('player').requestFullscreen();
		}
	});
	document.getElementById('player').requestFullscreen();
}

var player;
function onYouTubeIframeAPIReady() {
	const availableWidth = document?.getElementById('likeForm')?.clientWidth;
	const width = Math.max(availableWidth ? availableWidth - 40 : 854, 200);
	const height = Math.max(Math.floor(width / 16 * 9), 200);
	
	player = new YT.Player(
		'player', 
		{
			height: height, 
			width: width, 
			videoId: 'dQw4w9WgXcQ', 
			playerVars: {
				controls: '0', 
				loop: '1'
			}, 
			events: {
				'onStateChange': videoStateChange
			}
		}
	);
}

function updateVolume() {
	player.unMute();
	player.setVolume(100);
	setTimeout(updateVolume, 100);
}

function videoStateChange(event) {
	if((event.data == YT.PlayerState.PAUSED) && disliked) {
		player.playVideo();
	}
}