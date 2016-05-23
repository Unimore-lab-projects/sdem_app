----------------------- Page 1-----------------------

/*! \mainpage Introduzione
 *
 * \section intro_sec Caratteristiche Generali
 *
 *



Marker Finder è una applicazione sviluppata per gli smartphone dotati di sistema operativo Android. Essa si  

propone come un rilevatore di specifici Aruco Marker ed è basata principalmente sull'utilizzo di OpenCv, una  

libreria  open  source  orientata  alla  Computer  Vision  e  ArUco  Marker  library,  una  libreria  per  la  realtà  

aumentata che prevede l'impiego di un sistema di markers di riferimento (ArUco Markers).   



Marker Finder è stata sviluppata mediante  l'IDE Android Studio, utilizzando i linguaggi di programmazione  

Java e C++ opportunamente combinati attraverso il framework JNI (Java Native Interface).  



\section rilevamento Rilevamento Marker



Un  ArUco  marker  è  un  semplice  marker  di  forma  quadrata,  che  presenta  un  bordo  nero  ai  lati,  mentre  

all’interno si configura una matrice binaria che determina il suo identificatore.  Il bordo consente  una veloce  

e  facile  rilevazione,  la  codifica  binaria  permette  la  sua  identificazione  e  l’applicazione  di  tecniche  di  

rilevazione e  di correzione  degli  errori. La dimensione del marker definisce  quella della sua matrice interna  

(per esempio, un marker di dimensione 4x4 è composto da 16 bits).   



Un marker può trovarsi in qualsiasi posizione nell’ambiente; tuttavia,  il processo di rilevazione è in grado di  

determinare la sua rotazione originale, identificando in maniera univoca i suoi 4 angoli, basandosi sulla sua  

codifica binaria.   



Ogni marker appartiene ad uno specifico dizionario  predefinito. Esso è semplicemente una lista di codifiche  

binarie di ognuno dei marker che contiene ed è identificato da due parametri principali: la dimensione, ossia  

il numero di marker che contiene e la dimensione dei marker, ovvero il numero di bits da cui sono composti.  

L’identificatore di un marker è, dunque, l’indice di riferimento del dizionario a cui appartiene (per esempio, i  

primi 5 markers di un dizionario hanno i seguenti ids: 0,1,2,3,4).   



Il dizionario utilizzato da Marker Finder è il seguente: DICT_6X6_250.  



  
 *
 * \section  funzionamento Funzionamento
 * 



Durante la fase di avvio, ArUcoTetsApp accede direttamente alla fotocamera dello smartphone. Posizionando  

il  dispositivo  in  direzione dell'ArUco Marker,  l'applicazione  attua  la  rilevazione,  restituendo  direttamente  

sullo schermo l'identificatore del marker.   



È  possibile utilizzare l’applicazione per rilevare un numero arbitrario di ArUco Markers. L’obiettivo è quello  

di   effettuare   la   rilevazione   di   ognuno,       in   una   precisa   sequenza.       In   seguito   ad   ogni   rilevazione,  

opportunamente  verificata  mediante  l’apparizione  dell’identificatore  sullo  schermo,  viene visualizzato  un  

messaggio di approvazione insieme ad un indizio sul prossimo marker da rilevare.
 *  
 * 
 */
