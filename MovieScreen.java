import java.awt.Component;
import java.awt.Frame;
import java.awt.Panel;

import quicktime.QTException;
import quicktime.QTSession;
import quicktime.app.view.MoviePlayer;
import quicktime.app.view.QTFactory;
import quicktime.io.OpenMovieFile;
import quicktime.io.QTFile;
import quicktime.std.StdQTConstants;
import quicktime.std.StdQTConstants4;
import quicktime.std.StdQTException;
import quicktime.std.movies.Movie;
import quicktime.std.movies.media.DataRef;
import quicktime.util.QTHandle;


public class MovieScreen {

	//JFrame videoFrame;
	MoviePlayer qtPlayer;
	Movie qtMovie;
	Component qtCanvas = null;
	Frame myFrame;
	Panel myPApplet;
	int vidW ;
	int vidH ;
	int locx;
	int locy;
	public MovieScreen(Frame _panel, Panel _cover){
		myFrame = _panel;
		myPApplet = _cover;
	    vidW = myFrame.getWidth();
		vidH = myFrame.getHeight();
		try {
			QTSession.open();

			//System.out.println("QTSession Open");

		} catch (QTException qte) {
			System.out.println("Sorry NOT: QTSession Open");
		}
		//videoFrame = DisplayApp.myFrame;
		//videoFrame = new JFrame();
		//videoFrame.setBackground(new Color(DisplayApp.bgColor));
/*
		videoFrame.addMouseListener(new MouseAdapter() {
			// Register a mouse listener that is defined by an anonymous
			// subclass
			// of MouseAdapter. This replaces the RepaintOnClick class that
			// was
			// used in the original version.
			public void mousePressed(MouseEvent evt) {
				Component source = (Component) evt.getSource();
				source.setVisible(false);
				DisplayApp.myApplet.setVisible(true);

			}
		});
		*/
		//videoFrame.getContentPane().setBackground(new Color(DisplayApp.bgColor));
		//videoFrame.setUndecorated(true);
	
	}
	
	public void jiggle(){
		if (qtCanvas != null) {
			//qtCanvas.setVisible(true);
			
			qtCanvas.setLocation(locx, locy);
			//System.out.println("refresh");
		}
	}
	
	public void setMovie(Movie _qtMovie,boolean _fullScreen){
		//System.out.println("Set Movie" + _qtMovie.toString());
		qtMovie = _qtMovie;
		
		if (qtCanvas != null) myFrame.remove(qtCanvas);
		
		if(!_fullScreen){
			try{
			 vidW = qtMovie.getNaturalBoundsRect().getWidth();
			 vidH = qtMovie.getNaturalBoundsRect().getHeight();
			} catch (Exception e) {
				System.out.println("Couldn't get natural sizee" + e);
			}
			
		}

		//videoFrame.setSize(vidW,vidH);
		try {

			qtCanvas = (QTFactory.makeQTComponent(qtMovie)).asComponent();
		} catch (Exception e) {
			System.out.println("Trouble Making Canvas" + e);
		}
	
		qtCanvas.setSize(vidW, vidH);
		locx = (DisplayApp.width - vidW) / 2;
		locy = (DisplayApp.height - vidH) / 2;
		//videoFrame.getContentPane().add(qtCanvas);
		qtCanvas.setLocation(locx,locy) ;
		myFrame.add(qtCanvas);
		//videoFrame.setLocation((DisplayApp.width - videoFrame.getWidth()) / 2, (DisplayApp.height - videoFrame.getHeight()) / 2);

	}
	public void hideMovie(){
		//System.out.println("Hide Movie" + qtMovie.toString());
		//DisplayApp.myApplet.setVisible(true);
		//DisplayApp.setFullScreen(null, videoFrame, vidW, vidH);
		myPApplet.setVisible(true);
		try {
			qtMovie.stop();
		} catch (StdQTException e) {
			System.out.println("Can't Stop Movie");
		}
		
		//videoFrame.setVisible(false);
		//videoFrame.setVisible(true);
		//DisplayApp.setFullScreen();
		

		//videoFrame.dispose();

		 //DisplayApp.myApplet.remove(qtCanvas);
		 qtCanvas = null;
	}

	public void showMovie(){
		
		//System.out.println("Show Movie" + qtMovie.toString());
		try {
			qtMovie.setTimeValue(0);
			qtMovie.start();
		
			} catch (StdQTException e) {
				System.out.println("Can't Start Movie");
			}
		//videoFrame.setVisible(true);
	//	try {
		//	Thread.sleep(2);
		//} catch (InterruptedException e1) {
	//	}
		
	
		myPApplet.setVisible(false);
		//DisplayApp.myApplet.setVisible(false);
		


		
		
	}
	public Movie getMovie(String _mediaFileName){
		
		//System.out.println("Get Movie" + _mediaFileName);
		if (_mediaFileName.startsWith("http")) {
			byte data[] = DisplayApp.myApplet.loadBytes(_mediaFileName);
			try {
				
				qtMovie = fromDataRef(new DataRef(new QTHandle(data)));
			} catch (QTException e) {
				e.printStackTrace();
			}
		} else {
			try {
				OpenMovieFile omf = OpenMovieFile.asRead(new QTFile(_mediaFileName));
				qtMovie = Movie.fromFile(omf);	
			} catch (Exception e) {
				System.out.println("Trouble General" + _mediaFileName);
			}
		}
		//System.out.println("Get Movie" + _mediaFileName + qtMovie);
		return qtMovie;
	}
	private quicktime.std.movies.Movie fromDataRef(DataRef ref) throws QTException {

		return quicktime.std.movies.Movie.fromDataRef(ref, StdQTConstants4.newMovieAsyncOK | StdQTConstants.newMovieActive);
	}
	
	public int getDuration(Movie _qtMovie){
		int videoDuration;
		try {
			videoDuration = 1000 * _qtMovie.getDuration() / _qtMovie.getTimeScale();
		} catch (StdQTException e) {
			e.printStackTrace();
			videoDuration = 3000;
		}
		return videoDuration;
	}

	
}
