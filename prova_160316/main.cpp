#include <aruco.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <iostream>
//#include <aruco\dictionary.hpp>
#include <opencv2\core\cvstd.hpp>
#include <vector>
#include <aruco\charuco.hpp>

using namespace cv;
using namespace aruco;
using  namespace std;

bool readCameraParameters(string filename, Mat &camMatrix, Mat &distCoeffs) {
	FileStorage fs(filename, FileStorage::READ);
	if (!fs.isOpened())
		return false;
	fs["camera_matrix"] >> camMatrix;
	fs["distortion_coefficients"] >> distCoeffs;
	return true;
}

int main(int argc, char* argv[]){
	
	// creo i marker
	
	/*
	Mat markerImage;
	
	Ptr<Dictionary> dictionary = getPredefinedDictionary(DICT_5X5_50);
	
	drawMarker(dictionary, 0, 500, markerImage, 1);

	imwrite("C:\\Users\\Marcella\\Pictures\\prova.jpg", markerImage);
	namedWindow("MARKER", WINDOW_AUTOSIZE);

	imshow("MARKER", markerImage);
	waitKey(0);
	*/

	// Marker detection 

	VideoCapture inVideo;
	inVideo.open(0); //apre fotocamera principale

	if (!inVideo.isOpened())
		return -1;

	/*double dWidth = inVideo.get(CV_CAP_PROP_FRAME_WIDTH); //get the width of frames of the video
	double dHeight = inVideo.get(CV_CAP_PROP_FRAME_HEIGHT); //get the height of frames of the video

	cout << "Frame size : " << dWidth << " x " << dHeight << endl;

	namedWindow("DetectMarker", CV_WINDOW_AUTOSIZE);
	*/

	
	//fintantochè si riesce ad acquisire una immagine
	while (inVideo.grab())
    {
        Mat inImage, imageCopy;


		//inVideo.retrieve(inImage);
		// double tick = (double)getTickCount();

		bool bSuccess = inVideo.read(inImage); // leggo una nuova immagine da video

         if (!bSuccess) 
        {
             cout << "Cannot read a frame from video stream" << endl;
             break;
        }

		 Ptr<Dictionary> dictionary = getPredefinedDictionary(DICT_6X6_250);
		 vector<int> markerIds;
		 vector<vector<Point2f>> markerCorners, rejectCanditates;
		 Ptr<DetectorParameters> parameters; //????

		 
		
		 //acquisisco i marker da inImage
		 detectMarkers(inImage, dictionary, markerCorners, markerIds);

		 inImage.copyTo(imageCopy);
		 
		 //per ogni marker id >0 (trovato) 
		 if (markerIds.size() > 0) 
			drawDetectedMarkers(imageCopy, markerCorners, markerIds);

		
        imshow("DetectMarker", imageCopy); //mostra inImage nella finestra

        if (waitKey(30) == 27) //se viene premuto il tasto 'esc' si interrompe
       {
            cout << "esc key is pressed by user" << endl;
            break; 
       }

		
    }
    return 0;


}