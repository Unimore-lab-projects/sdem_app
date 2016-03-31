#if !defined DETECTION_H
#define DETECTION_H

#include <opencv2/aruco.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <iostream>
#include <opencv2/core/cvstd.hpp>
#include <vector>


using namespace cv;
using namespace aruco;
using  namespace std;


class detection_marker {

    Ptr<Dictionary> _dictionary;
    // use: (DICT_6X6_250);
    vector<int> _markerIds;
    vector<vector<Point2f>> _markerCorners;
    Mat inImage;

public:

    detection_marker(PREDEFINED_DICTIONARY_NAME name);

    const vector<int> &ids() const;

    const vector<vector<Point2f>> &corners() const;

    const Mat &img() const;

    void detectAndDraw(Mat &inImage);

    void detect(Mat &inImage);

};

#endif //DETECTION_H