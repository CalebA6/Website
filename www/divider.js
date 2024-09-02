let basePath = window.location.pathname;
if(basePath.length == 0) {
	basePath = '/';
} else if(basePath[basePath.length - 1] != '/') {
	basePath += '/';
}
const mainPath = basePath + 'main.html';

document.write('<iframe src="' + mainPath + '" class="main">');