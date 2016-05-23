
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


/**
 * Classe di gestione dei marker aruco.
 */

class detection_marker {

    Ptr<Dictionary> _dictionary;// use: (DICT_6X6_250);
    vector<int> _markerIds;
    vector<vector<Point2f>> _markerCorners;
    Mat inImage;

public:
    /**
      * Costruttore dell'oggetto detection marker. Prende come parametro il nome del dizionario dei marker aruco che si vogliono visualizzare.
      *
      * @param name nome del dizionario aruco.
      */
    detection_marker(PREDEFINED_DICTIONARY_NAME name);


    /**
     * Restituisce gli identificatori dei marker che sono stati riconosciuti, nell'ultima chiamata, da uno dei metodi detectAndDraw o detect.
     *
     * @return reference a vettore di interi contenente gli identificatori dei marker riconosciuti.
     *
     */
    const vector<int>& ids() const;

    /**
     * Restituisce le coordinate dei vertici dei marker che sono stati riconosciuti, nell'ultima chiamata, da uno dei metodi detectAndDraw o detect.
     *
     * @return reference a vettore di Point2f contenente i punti che rappresentano la posizione a schermo dei vertici dei marker riconosciuti.
     *
     */
    const vector<vector<Point2f>>& corners() const;

    /**
     * Restituisce l'oggetto Mat corrispondente al frame corrente su cui è stato invocato uno dei metodi detectAndDraw o detect.
     *
     * @return reference a Mat.
     *
     */
    const Mat& img() const;


    /**
     * Esegue la procedura di detection su inImage. Disegna direttamente sul frame il contorno dei marker individuati correttamente.
     *
     * @param inImage oggetto Mat che rappresenta il frame su cui eseguire la detection.
     *
     */
    void detectAndDraw(Mat& inImage);

    /**
     * Esegue la procedura di detection su inImage.
     *
     * @param inImage oggetto Mat che rappresenta il frame su cui eseguire la detection.
     *
     */
    void detect(Mat& inImage);

};

#endif //DETECTION_H