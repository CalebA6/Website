function like() {
	rickroll.stop();
	document.getElementById('likeYesResponse').removeAttribute('hidden');
	document.getElementById('likeNoResponse').setAttribute('hidden', '');
}

function dislike() {
	document.getElementById('likeNoResponse').removeAttribute('hidden');
	document.getElementById('likeYesResponse').setAttribute('hidden', '');
	rickroll.play();
}

class UnstoppableRickroll {
	playing = false;
	
	constructor() {
		const availableWidth = document?.getElementById('likeForm')?.clientWidth;
		const width = Math.max(availableWidth ? availableWidth - 40 : 854, 200);
		const height = Math.max(Math.floor(width / 16 * 9), 200);
		
		this.player = new YT.Player(
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
					'onStateChange': event => {this.stateChange(event); }
				}
			}
		);
	}
	
	play() {
		this.playing = true;
		this.player.playVideo();
		this.fullscreen();
		this.maxVolume();
	}
	
	stop() {
		this.playing = false;
		this.player.pauseVideo();
	}
	
	fullscreen() {
		document.addEventListener('fullscreenchange', event => {
			if(!document.fullscreenElement) {
				document.getElementById('player').requestFullscreen();
			}
		});
		
		document.getElementById('player').requestFullscreen();
	}
	
	maxVolume() {
		this.player.unMute();
		this.player.setVolume(100);
		
		if(this.playing) {
			setTimeout(() => {this.maxVolume()}, 100);
		}
	}
	
	stateChange(event) {
		if((event.data == YT.PlayerState.PAUSED) && this.playing) {
			this.player.playVideo();
		}
	}
}

var rickroll;
function onYouTubeIframeAPIReady() {
	rickroll = new UnstoppableRickroll();
}