
import java.util.Random;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import processing.video.Movie;

class ScreenElement {

	Screen screen;
	private Movie qtMovie;
	

	int w = 0;

	int center_x;

	int center_y;

	int center_z;

	int origin_x;

	int origin_y;

	int origin_z;

	int end_x;

	int end_y;

	int end_z;

	float z = 0;

	float y = 0;

	float x = 0;

	float xr = 0.0f;

	float yr = 0.0f;

	float zr = 0.0f;

	float vx = 0;

	float vy = 0;

	float vz = 0;

	float vxr = 0;

	float vyr = 0;

	float vzr = 0;

	float va = 0;

	float alpha = 0;

	float startAlpha = 0;

	float endAlpha = 240;

	int myColor;

	String mediaFileName = null;

	PImage pimg;

	String theString;

	int depth = -1000;

	int videoDuration = 0;

	int midddleOffset;

	PFont myFont;

	Random myRandom = new Random();

	int red, green, blue;

	// boolean inPreview;

	int start_y = 0;

	public ScreenElement(Screen _s, String _st, PFont _font, int _x, int _y, int _color) {
		screen = _s;
		center_x = _x;
		start_y = _y;
		theString = _st;
		pimg = null;
		myFont = _font;
		midddleOffset = DisplayApp.contentMetrics.stringWidth(_st) / 2;
		red = _color & 0xFF0000;
		red = red >> 16;
		green = _color & 0x00FF00;
		green = green >> 8;
		blue = _color & 0x0000FF;
		setEndPoints();
	}

	public ScreenElement(Screen _s, String _mediaFileName, int _x, int _y) {
		screen = _s;
		center_x = _x;
		center_y = _y; // + screen.getTopSpacing();
		start_y = _y;
		theString = null;
		//_mediaFileName = "/Users/dano/Documents/workspace/PS3 Display/cache/3FUND7.mov";
		mediaFileName = _mediaFileName;
		
		  System.out.println("media" + _mediaFileName);
		if (_mediaFileName.endsWith(".mov") || _mediaFileName.endsWith(".mpg")) {
		   // _mediaFileName = "/Users/dano/Documents/workspace/PS3 Display/cache/my.mov";
			qtMovie = new Movie(DisplayApp.myApplet, _mediaFileName) ; // DisplayApp.movieScreen.getMovie(_mediaFileName);
			 //System.out.println(qtMovie + " " + _mediaFileName);
			qtMovie.pause();
			if (qtMovie != null) {
				videoDuration = 1000* (int) qtMovie.duration();

				
			}
		}
		setEndPoints();
	}

	public void bumpDown(int _amount) {
		start_y = start_y + _amount;
		center_y = start_y;
		setEndPoints();

	}
	

	public void unload() {
		pimg = null;
		
		//qtMovie = null;
		//qtPlayer = null;
		//qtCanvas = null;
		//QTSession.close();
		// inPreview = true;
	}

	
	public void load() {
		//if (qtMovie != null){
		if ((mediaFileName != null) && qtMovie == null && (mediaFileName.endsWith(".mov") || mediaFileName.endsWith(".mpg"))) {
			//DisplayApp.movieScreen.setMovie(qtMovie);
		} else if (mediaFileName != null && pimg == null && (mediaFileName.endsWith(".gif") || mediaFileName.endsWith(".jpg") || mediaFileName.endsWith(".png"))) {
			if (DisplayApp.isApplication) {
				pimg = DisplayApp.myApplet.loadImage(mediaFileName);
			} else {
				pimg = DisplayApp.myApplet.loadImage(mediaFileName);
				Screen.resize(pimg, screen.spaceForImage);
			}
		}
	}

	public void setElementEndPoints(int _ox, int _oy, int _oz, int _dx, int _dy, int _dz, boolean _addrandom) {
		origin_x = _ox;
		origin_y = _oy;
		origin_z = _oz;

		end_x = _dx;
		end_y = _dy;
		end_z = _dz;
	}

	public void setEndPoints() {
		// myFont = DisplayApp.contentFont;

		center_z = 1; // 1 instead of 0 seems to avoid a flicker issue when
						// going between images.

		// myRandom.setSeed(System.currentTimeMillis());
		if (mediaFileName != null) {
			// System.out.println("setEndPoints" + mediaFileName);
			origin_x = center_x; // DisplayApp.width + myRandom.nextInt(3 *
			// DisplayApp.width);
			origin_y = center_y; // myRandom.nextInt(3 * DisplayApp.height);
			origin_z = 0; // depth;
			// origin_z = zDepth;
			end_z = 0; // depth;
			end_x = center_x; // -myRandom.nextInt(3 * DisplayApp.width);
			end_y = center_y; // -myRandom.nextInt(3 * DisplayApp.height);
		} else {
			origin_x = DisplayApp.width + myRandom.nextInt(3 * DisplayApp.width);
			origin_y = myRandom.nextInt(3 * DisplayApp.height);
			origin_z = depth;
			// origin_z = zDepth;
			end_z = depth;
			end_x = -myRandom.nextInt(3 * DisplayApp.width);
			end_y = -myRandom.nextInt(3 * DisplayApp.height);
		}
	}

	public void startElement() {
		center_y = start_y; // + screen.getTopSpacing();
		x = origin_x;
		y = origin_y;
		z = origin_z;
		if (pimg == null) {
			xr = myRandom.nextFloat() * 6.24f;
			yr = myRandom.nextFloat() * 6.24f;
			zr = myRandom.nextFloat() * 6.24f;
		} else {
			xr = 0.0f;
			yr = 0.0f;
			zr = 0.0f;
		}

		if (pimg == null) {
			setVectors(center_x, center_y, center_z, 2 * Math.PI, 2 * Math.PI, 2 * Math.PI);

		} else {
			setVectors(center_x, center_y, center_z, 0, 0, 0);
		}
		// flipEndPoints();
		alpha = startAlpha;
		va = (endAlpha - startAlpha) / screen.numOfTransitionframes;

	}

	public void setFont(PFont _f) {
		myFont = _f;
	}

	public int getVideoDuration() {
		System.out.println("diration" + videoDuration);
		return videoDuration;
	}

	public void startOut() {

		if (pimg == null) {
			setVectors(end_x, end_y, end_z, myRandom.nextFloat() * 6.24f, myRandom.nextFloat() * 6.24f, myRandom.nextFloat() * 6.24f);

		} else {
			setVectors(end_x, end_y, end_z, 0, 0, 0);
		}
		va = -va;
		//if (qtMovie != null) {
		//	DisplayApp.movieScreen.hideMovie();
		//}
	}

	public void arrived() {
		// System.out.println("Arrived" + qtMovie );
		if (qtMovie != null) {
			qtMovie.jump(0);
			qtMovie.play();
			//DisplayApp.movieScreen.setMovie(qtMovie,true);
			//DisplayApp.movieScreen.showMovie();
		}

	}

	void setVectors(int _destination_x, int _destination_y, int _destination_z, double _destination_xr, double _destination_yr, double _destination_zr) {
		vx = (_destination_x - x) / screen.numOfTransitionframes;
		vy = (_destination_y - y) / screen.numOfTransitionframes;
		vz = (_destination_z - z) / screen.numOfTransitionframes;
		vxr = (float) ((_destination_xr - xr) / screen.numOfTransitionframes);
		vyr = (float) ((_destination_yr - yr) / screen.numOfTransitionframes);
		vzr = (float) ((_destination_zr - zr) / screen.numOfTransitionframes);
	}

	void incrementPosition() {
		x = x + vx;
		y = y + vy;
		z = z + vz;
		xr = xr + vxr;
		yr = yr + vyr;
		zr = zr + vzr;
		alpha = alpha + va;
	}

	void preview(PApplet _pApplet, int _x, int _y, float _angle) {

		// if (inPreview == false)
		// return;

		// z = -10;
		z = -700;
		x = PApplet.lerp(x, _x, .18f);
		// x = _x;
		y = PApplet.lerp(y, _y, .18f);
		// y = _y;
		zr = PApplet.lerp(zr, _angle, .02f);

		if (qtMovie != null) {
			// DisplayApp.myApplet.add(qtCanvas);
			// videoFrame.repaint();
			//_pApplet.image(qtMovie,0,0);

		} else if (theString != null) {
			_pApplet.pushMatrix();

			_pApplet.translate(x + midddleOffset, y, z);
			_pApplet.translate(-midddleOffset, 0, 0);
			_pApplet.rotateX(xr);
			_pApplet.rotateY(yr);
			_pApplet.rotateZ(zr);

			// _pApplet.scale(.4f);
			// _pApplet.fill(red, green, blue, alpha);
			_pApplet.fill(0, 127);
			_pApplet.textFont(myFont);
			_pApplet.text(theString, 0, 0);
			// System.out.println("preview" + x + " " + y + " " + z + " " + zr +
			// theString);
			_pApplet.popMatrix();
		} else if (pimg != null) {
			_pApplet.pushMatrix();

			_pApplet.translate(x, y, z);
			// _pApplet.scale(.1f);
			// _pApplet.rotateX(xr);
			// _pApplet.rotateY(yr);
			_pApplet.rotateZ(zr);
			try {
				_pApplet.tint(255, 255, 255, 100);
				_pApplet.image(pimg, 0, 0);
			} catch (Exception e) {
				System.out.println("Problem drawing image" + e);
			}
			// System.out.println("preview img" + x);
			_pApplet.popMatrix();

		} 
	}

	void display(PApplet _pApplet) {
		// if (true) return;

		 if (qtMovie != null){
			// qtMovie.read();
			 _pApplet.tint(255, 255, 255, 255);
			_pApplet.image(qtMovie,0,0);
			//System.out.println(" show movie" + qtMovie);
		}else if (theString != null) {
			_pApplet.pushMatrix();
			_pApplet.translate(x + midddleOffset, y, z);
			_pApplet.translate(-midddleOffset, 0, 0);
			// _pApplet.translate(x, y , z);
			_pApplet.rotateX(xr);
			_pApplet.rotateY(yr);
			_pApplet.rotateZ(zr);
			_pApplet.fill(red, green, blue, alpha);
			_pApplet.textFont(myFont);
			_pApplet.text(theString, 0, 0);
			_pApplet.popMatrix();
		} else if (pimg != null) {
			_pApplet.pushMatrix();
			_pApplet.translate(x, y, z);
			_pApplet.rotateX(xr);
			_pApplet.rotateY(yr);
			_pApplet.rotateZ(zr);
			try {
				_pApplet.tint(255, 255, 255, alpha);
				_pApplet.image(pimg, 0, 0);
			} catch (Exception e) {
				System.out.println("Problem drawing image" + e);
			}
			_pApplet.popMatrix();
		} 
	}

}
