#include "detection.h"

detection_marker::detection_marker(PREDEFINED_DICTIONARY_NAME name) { _dictionary = getPredefinedDictionary(name); }

const vector<int>& detection_marker::ids() const { return _markerIds; }
const vector<vector<Point2f>>& detection_marker::corners() const { return _markerCorners; }
const Mat& detection_marker::img() const { return inImage; }

void detection_marker::detectAndDraw(Mat& inImage) {

	detectMarkers(inImage, _dictionary, _markerCorners, _markerIds);
	if (_markerIds.size() > 0)
		drawDetectedMarkers(inImage, _markerCorners, _markerIds);

}
void detection_marker::detect(Mat& inImage) {

	detectMarkers(inImage, _dictionary, _markerCorners, _markerIds);

}
