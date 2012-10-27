import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import processing.core.PApplet;
import processing.core.PImage;

class Screen {

	//int topSpacing = 0; // DisplayApp.margin;

	ArrayList<ScreenElement> previewElements = new ArrayList<ScreenElement>();

	public static int MOVININ = 1;

	static int STAYING = 2;

	static int STARTOUT = 3;

	static int MOVINOUT = 4;

	static int GONE = 5;

	ArrayList<ScreenElement> screenElements = new ArrayList<ScreenElement>();

	int stayingTime = 3000;

	long started = 0;

	int numOfTransitionframes = DisplayApp.numOfTransitionFrames;

	int currentTransitionFrame = 0;

	// int descriptionXOffset;

	int mode = MOVININ;

	String headline = "";

	String date = "";

	int descriptionXOffset;

	boolean unloaded = true;
	
	int charsActuallyDisplayed =0;

	Feed feed;
	
	int spaceForHeadlines;
	int spaceForDescription;
	int spaceForImage;
	boolean emptyDescription;
	String uniqueID;
	Screen(String _uniqueID, String _headline, String _content, String _date, String _desc, String _pictureLocator, String _videoLocator, Feed _feed) {
		uniqueID = _uniqueID;
		feed = _feed;
		date = _date;
		
		_content = _content.trim();
	//	String originalContent = new String(_content);
		
		//System.out.println("Pictures" + _pictureLocator);
		
		//System.out.println("Videos" +_videoLocator);
		///Eventually I should have functions which parse words and get inventory of whole screen
		//then have placement functions which use style sheets to place things including multiple pictures etc.
      // System.out.println(_headline);
		int topOfPictures = 0; //DisplayApp.topMargin;
		//spaceForImage = DisplayApp.height;
		spaceForImage = 0; //just as a spacer for between headline and body
		ArrayList<ScreenElement> headlineElements = dealWithHeadlines(_headline);
		if (spaceForHeadlines != 0){
			bumbDownElements(headlineElements, DisplayApp.topMargin);
			topOfPictures = spaceForHeadlines + DisplayApp.topMargin;
			//spaceForImage = spaceForImage  - (2*DisplayApp.margin + spaceForHeadlines);
		}
		
		ArrayList<ScreenElement> descriptionElements  = dealWithDescriptions(_content,0);
	
		if (emptyDescription & spaceForHeadlines == 0){
			topOfPictures = 0;
			spaceForImage = DisplayApp.height;
			//if (descriptionElements.size() == 1){  //get rid of feed advertizement
			//	screenElements.remove(descriptionElements.get(0));
			//}
		}
		stayingTime = Math.max(2000, charsActuallyDisplayed * DisplayApp.millisPerLetter);
		//put spacer in under headline
		if (_videoLocator != null) {
			dealWithVideo(_videoLocator, topOfPictures, spaceForImage);
			//System.out.println(_videoLocator);
		} else if (_pictureLocator != null) {
			ArrayList<ScreenElement> imageElements = dealWithPictures(_pictureLocator,topOfPictures, spaceForImage);
			charsActuallyDisplayed = charsActuallyDisplayed + spaceForImage/15;
			if (emptyDescription & spaceForHeadlines == 0){
				//System.out.println((DisplayApp.height - spaceForImage) + "Bump down image" +  _pictureLocator);
				bumbDownElements(imageElements, (DisplayApp.height - spaceForImage)/2 );
			}
			stayingTime = Math.max(2000, charsActuallyDisplayed * DisplayApp.millisPerLetter);
		}
		//should be 2* int paddingBeforeDescription = (DisplayApp.height - (DisplayApp.topMargin*2 + spaceForHeadlines +  spaceForImage + spaceForDescription))/2;
		
		
		
		int paddingBeforeDescription = (DisplayApp.height - (DisplayApp.topMargin + spaceForHeadlines +  spaceForImage + spaceForDescription))/2;
		//System.out.println(_headline + "marg " +DisplayApp.topMargin + " head " + spaceForHeadlines  + " top of image" + topOfPictures + " img " + spaceForImage + " sDesc" + spaceForDescription + " pad"+ paddingBeforeDescription+ " h" + DisplayApp.height);
		bumbDownElements(descriptionElements, spaceForHeadlines + spaceForImage + DisplayApp.topMargin + paddingBeforeDescription  );
		//if (_pictureLocator == null && _videoLocator == null) bottomOfHeadline = bottomOfHeadline + DisplayApp.headLineSize / DisplayApp.scaler;
		//ArrayList<ScreenElement> descriptionElements  = dealWithDescriptions(_content,bottomOfHeadline);
		//System.out.println("Staying for this latere" + stayingTime);
		//System.out.println(DisplayApp.millisPerLetter + " " +  charsActuallyDisplayed  + "Staying time" + stayingTime);
		start();
		

	}
	String decodeIt(String _content) {
		// our own display tags
		int lastBeginBad = _content.lastIndexOf("<nod>");
		int lastEndBad = _content.lastIndexOf("<yod>");

		if (lastEndBad < lastBeginBad)
			_content = _content.substring(0, lastBeginBad);
		_content = _content.replaceAll("\\<nod\\>.*\\<yod\\>", "");
		// _content = _content.replaceAll("\\<dr\\>", "\n");
		// _content = _content.replaceAll(":", "\n");
		_content = _content.replaceAll("\\[\\.\\.\\.\\]", "...");
		_content = _content.replaceAll("&#8220;", "\"");
		_content = _content.replaceAll("&#8221;", "\"");
		_content = _content.replaceAll("&#8216;", "'");
		_content = _content.replaceAll("&#8217;", "'");
		_content = _content.replaceAll("&#8218;", "'");
		_content = _content.replaceAll("&#038;", "&");
		_content = _content.replaceAll("&#8230;", "...");
		_content = _content.replaceAll("&#8212;", "--");
		_content = _content.replaceAll("&#8211;", "-");
		_content = _content.replaceAll("&#8242;", "'");
		_content = _content.replaceAll("&amp;", "&");
		
		
		
		_content = _content.replaceAll("&#33;", "!");
		_content = _content.replaceAll("&#34;", "\"");
		_content = _content.replaceAll("&#35;", "#");
		_content = _content.replaceAll("&#36;", "$");
		_content = _content.replaceAll("&#37;", "%");
		_content = _content.replaceAll("&#39;", "'");

		//( &#40;
		//) &#41;
		//* &#42;
		//+ &#43;
		//, &#44;
		//- &#45;
		//. &#46;
		/// &#47;
		//@ &#64;


		_content = _content.replaceAll("&#8222;", "�");
		_content = _content.replaceAll("&#8224;", "x");
		_content = _content.replaceAll("&#8225;", "�");
		_content = _content.replaceAll("&#8226;", "�");
		
		_content = _content.replaceAll("&#8240;", "�");
		_content = _content.replaceAll("&#8364;", "�");
		_content = _content.replaceAll("&#8482;", "�");
		
		//_content = _content.replaceAll("///[/^/(/\x20-\x7F/)/]*/","");

		//_content = _content.replaceAll([^\\p{ASCII}], "");
		_content = _content.replaceAll("[^\\p{ASCII}]", "");

		
			



	

		// _content = _content.replaceAll("\\</p\\>", "--");
		_content = _content.replaceAll("</p>", "\n");
		_content = _content.replaceAll("\\</strong\\>", "--");
		_content = _content.replaceAll("\\<strong\\>", "--");
		_content = _content.replaceAll("\\</b\\>", "--");
		_content = _content.replaceAll("\\<b\\>", "--");
		_content = _content.replaceAll("\\<br\\>", "\n");

		_content = _content.replaceAll("\\<br /\\>", "\n");

		_content = _content.replaceAll("\\<.*\\>", "");

		return _content;
	}

	public void load() {
		if (unloaded) {
			for (int i = 0; i < screenElements.size(); i++) {
				ScreenElement s = (ScreenElement) screenElements.get(i);
				s.load();
			}
			unloaded = false;
		}

	}

	public void unload() {
		unloaded = true;
		for (int i = 0; i < screenElements.size(); i++) {
			ScreenElement s = (ScreenElement) screenElements.get(i);
			s.unload();
		}

	}

	public static BufferedImage resize(BufferedImage img, float maxHeight) {
		if (img != null) {
			// System.out.println(DisplayApp.height + " resize " + maxHeight);
			float maxWidth = (float) DisplayApp.width;
			// float maxHeight = (float) DisplayApp.height;
			// float maxWidth = (float) DisplayApp.width * 2 / 3;
			// float maxHeight = (float) DisplayApp.height * 2 / 3;
			float widthRatio = img.getWidth() / maxWidth;
			float heightRatio = img.getHeight() / maxHeight;
			int newWidth = -1;
			int newHeight = -1;
			if (widthRatio > 1.0f && widthRatio >= heightRatio) {
				newWidth = (int) (img.getWidth() / widthRatio);
				newHeight = (int) (img.getHeight() / widthRatio);
			}
			if (heightRatio > 1.0f && heightRatio > widthRatio) {
				newWidth = (int) (img.getWidth() / heightRatio);
				newHeight = (int) (img.getHeight() / heightRatio);
			}
			if (newWidth != -1) {
				// System.out.println("Scaled");
				BufferedImage scaledBI = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
				Graphics2D scaledBIGraphics = scaledBI.createGraphics();
				scaledBIGraphics.drawImage(img, 0, 0, newWidth, newHeight, null);
				img = scaledBI;
			}
		}
		return img;
	}

	public static PImage resize(PImage img, float maxHeight) {
		if (img != null) {
			// System.out.println(DisplayApp.height + " resize " + maxHeight);
			float maxWidth = (float) DisplayApp.width;
			// float maxHeight = (float) DisplayApp.height;
			// float maxWidth = (float) DisplayApp.width * 2 / 3;
			// float maxHeight = (float) DisplayApp.height * 2 / 3;
			float widthRatio = img.width / maxWidth;
			float heightRatio = img.height / maxHeight;
			int newWidth = -1;
			int newHeight = -1;
			if (widthRatio > 1.0f && widthRatio >= heightRatio) {
				newWidth = (int) (img.width / widthRatio);
				newHeight = (int) (img.height / widthRatio);
			}
			if (heightRatio > 1.0f && heightRatio > widthRatio) {
				newWidth = (int) (img.width / heightRatio);
				newHeight = (int) (img.height / heightRatio);
			}
			if (newWidth != -1) {
				img.resize(newWidth, newHeight);

			}
		}
		return img;
	}

	public ArrayList<ScreenElement> dealWithHeadlines(String _headline){
		// DEAL WITH HEADLINES
		//ArrayList<ArrayList> linesList = new ArrayList<ArrayList>();
		ArrayList<ScreenElement> elements = new ArrayList<ScreenElement>();
		_headline = decodeIt(_headline.trim());
		_headline = _headline.replaceAll("   ", "\n").trim();
		ArrayList<String> headLines = new ArrayList<String>();
		int bottomOfHeadlines = 0;
		//int bottomOfHeadlines = DisplayApp.margin; // ; // DisplayApp.height / 5;
		breakIntoLines(_headline, headLines, DisplayApp.headlineMetrics);
		
		
		for (int i = 0; i < headLines.size(); i++) {
			//  System.out.println(i + headLines.get(i));
			String thisHeadLine =  headLines.get(i).trim();
			if (thisHeadLine.length() == 0)
				continue;
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int halfWidth = DisplayApp.headlineMetrics.stringWidth(thisHeadLine) / 2;

			String[] words = thisHeadLine.split(" ");
			int cumPosition = (DisplayApp.width ) / 2 - halfWidth;
			//ArrayList<ScreenElement> wordsList = new ArrayList<ScreenElement>();
			for (int j = 0; j < words.length; j++) {
				
				String thisWord = words[j];

				ScreenElement se = new ScreenElement(this, thisWord, DisplayApp.headlineFont, cumPosition, bottomOfHeadlines, DisplayApp.headColor);

				cumPosition = (int) (cumPosition + DisplayApp.headlineMetrics.stringWidth(thisWord + " "));
				elements.add(se);
				previewElements.add(se);
				screenElements.add(se);
				
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//linesList.add(wordsList);
			bottomOfHeadlines = bottomOfHeadlines + (DisplayApp.headLineSize) / DisplayApp.scaler;

		}
		spaceForHeadlines = bottomOfHeadlines;
		return elements;
	}
	public ArrayList<ScreenElement> dealWithDescriptions(String _content,int _bottomOfDescription){
		// System.out.println(_content);
	//	ArrayList<ArrayList> linesList = new ArrayList<ArrayList>();
		ArrayList<ScreenElement> elements = new ArrayList<ScreenElement>();
		//int descriptionSpace = 0;
		boolean centerMode = (_content.indexOf("<cod>") != -1);

		_content = decodeIt(_content);

		if (_content.trim().equals("")) {
			emptyDescription = true;
			centerMode = true;
			if (spaceForHeadlines != 0)  //don't do this if it is going to be a picture only
			_content = feed.shortenAddress() ;
		}
		ArrayList<String> descriptionLines = new ArrayList<String>();
		breakIntoLines(_content, descriptionLines, DisplayApp.contentMetrics);
		// descriptionX = DisplayApp.width;
		int longestLine = 0;
		for (int i = 0; i < Math.min(DisplayApp.maxNumberOfContentLines, descriptionLines.size()); i++) {
			String line = descriptionLines.get(i).trim();
			int len = (int) DisplayApp.contentMetrics.stringWidth(line);

			// int x = (center_x - len/2) ;
			if (len > longestLine)
				longestLine = len;
			// text(line, , i*40 + headLines.size()*60);
		}
	
		// center_y = (DisplayApp.height - (headLines.size() *
		// DisplayApp.headLineSize/DisplayApp.scaler +
		// descriptionLines.size() *
		// DisplayApp.descriptionSize/DisplayApp.scaler )) / 2;
	
		descriptionXOffset = DisplayApp.width / 2 - longestLine / 2;
		for (int i = 0; i < Math.min(DisplayApp.maxNumberOfContentLines, descriptionLines.size()); i++) {
			String line = (String) descriptionLines.get(i);
			charsActuallyDisplayed = charsActuallyDisplayed + line.length();
			//int dy = (i) * DisplayApp.descriptionSize / DisplayApp.scaler ;

			String[] words = line.split(" ");

			// if center mode make cumPositon the center minus half the
			// width of the line
			int cumPosition = descriptionXOffset;
			if (centerMode)
				cumPosition = DisplayApp.width / 2 - DisplayApp.contentMetrics.stringWidth(line) / 2;
			//ArrayList<ScreenElement> wordsList = new ArrayList<ScreenElement>();
			for (int j = 0; j < words.length; j++) {
				String thisWord = words[j];

				ScreenElement se = new ScreenElement(this, thisWord, DisplayApp.contentFont, cumPosition, _bottomOfDescription, DisplayApp.contentColor);
				cumPosition = (int) (cumPosition + DisplayApp.contentMetrics.stringWidth(thisWord + " "));
				screenElements.add(se);
				elements.add(se);
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			 _bottomOfDescription =  _bottomOfDescription+DisplayApp.descriptionSize / DisplayApp.scaler ;
			//linesList.add(wordsList);
		}
		spaceForDescription =  _bottomOfDescription;
		//return linesList;
		return elements;
	}
	
	public ArrayList<ScreenElement> dealWithPictures(String _pictureLocator, int _topOfPicture, int _allowedSpace){
		URL url = null;
		File file = null;
		String filename = "";
		String[] urlParts = _pictureLocator.split("/");
		filename = urlParts[urlParts.length - 1];
		BufferedImage img = null;
		ArrayList<ScreenElement> elements = new ArrayList<ScreenElement>();
		int h = 0;
		if (_pictureLocator.startsWith("http")) {
			try {
				url = new URL(_pictureLocator);
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			}
		} else {
			file = new File(_pictureLocator);
		}
		try {
			// check the date of the url, check if it exists in cache and if
			// date is same as cache.

			if (DisplayApp.isApplication) {
			//	System.out.println("Picture Locator" + _pictureLocator);
			 //System.out.println(file.getName());
				if (file != null) {
					// filename = _pictureLocator;
					//System.out.println("Using local picture file " + _pictureLocator);
					img = ImageIO.read(file);
					img = resize(img, _allowedSpace);
					// transfer them over to the cache so they can retain
					// their size and only resize in the cache
					filename = "./cache/" + file.getName();
					String filetype = "jpg";
					if (filename.endsWith(".png")) filetype = "png";
					ImageIO.write(img,  filetype, new File(filename));

				} else if (upToDateInCache(url, filename) == false) {

					System.out.println("Not in cache, loading " + url);
					img = ImageIO.read(url);

					img = resize(img, _allowedSpace);
					filename = "./cache/" + filename;
					String filetype = "jpg";
					if (filename.endsWith(".png")) filetype = "png";
					ImageIO.write(img, filetype, new File(filename));
				} else {
					filename = "./cache/" + filename;
					// System.out.println("Using Cached " + filename);
					img = ImageIO.read(new File(filename));
				}
				//topSpacing = (DisplayApp.height - (bottomOfHeadlines + img.getHeight())) / 4;
				ScreenElement se = new ScreenElement(this, filename, DisplayApp.width / 2 - img.getWidth() / 2, _topOfPicture);
				elements.add(se);
				h = img.getHeight();
				//ScreenElement se = new ScreenElement(this, filename, DisplayApp.width / 2 - img.getWidth() / 2, (DisplayApp.height - (bottomOfHeadlines + img.getHeight())) / 4);
				screenElements.add(se);
				// previewElements.add(se);
			} else {
				if (file == null) {
					img = ImageIO.read(url);
					// }else{
					// img = ImageIO.read(url);
				}
				// img = ImageIO.read(url);

				img = resize(img, _allowedSpace);
				//topSpacing = (DisplayApp.height - (bottomOfHeadlines + img.getHeight())) / 2;
				screenElements.add(new ScreenElement(this, _pictureLocator, DisplayApp.width / 2 - img.getWidth() / 2, _topOfPicture));
				h = img.getHeight();
			}
			// topSpacing = (DisplayApp.height - (bottomOfHeadlines +
			// descriptionLines.size() * DisplayApp.descriptionSize /
			// DisplayApp.scaler)) / 2;

		} catch (Exception e) {
			System.out.println("Probem getting image" + e.getMessage());
		}
		spaceForImage = h;
		return elements;

	}
	
	public void dealWithVideo(String _videoLocator , int bottomOfHeadlines, int _spaceAllowed){
		// screenElements.add(new ScreenElement(this, _videoLocator,
		// DisplayApp.width / 2 - im, 
		URL url = null;
		File file = null;
		String filename = "";
		String[] urlParts = _videoLocator.split("/");
		filename = urlParts[urlParts.length - 1];

		ArrayList<ScreenElement> elements = new ArrayList<ScreenElement>();
	
		if (_videoLocator.startsWith("http")) {
			try {
				url = new URL(_videoLocator);
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			}
		} else {
			file = new File(_videoLocator);
		}
	//	try {
			// check the date of the url, check if it exists in cache and if
			// date is same as cache.

			if (DisplayApp.isApplication) {
				//System.out.println("Picture Locator" + _pictureLocator);
		
				if (file != null) {
					//System.out.println("Using local video file " + _videoLocator);
					filename = _videoLocator;
				} else if (upToDateInCache(url, filename) == false) {
					System.out.println("Not in cache, loading " + url);
					byte[] inBytes = DisplayApp.myApplet.loadBytes(_videoLocator);
					filename = "./cache/" + filename;
					DisplayApp.myApplet.saveBytes(filename,inBytes);
				} else {
					filename = "./cache/" + filename;
				}
				ScreenElement se = new ScreenElement(this, filename, 0, bottomOfHeadlines);
				stayingTime = se.getVideoDuration();
				//Hack to make all the other elements dissappear
				screenElements = new ArrayList<ScreenElement>();
				elements.add(se);
		screenElements.add(se);
		
			} else {
				ScreenElement se = new ScreenElement(this, _videoLocator, 0, bottomOfHeadlines);
				stayingTime = se.getVideoDuration();
				screenElements.add(se);
			
			}

		//} catch (Exception e) {
		//	System.out.println("Probem getting video" + e.getMessage());
		//}
		//System.out.println("Staying for this movie" + stayingTime);
		//ScreenElement se = new ScreenElement(this, _videoLocator, 0, bottomOfHeadlines);
		//screenElements.add(se);


	}

	
	public void bumbDownElements(ArrayList<ScreenElement> _elements, int _howmuch){
		for (int i = 0; i < _elements.size(); i++){
			ScreenElement thisElement = _elements.get(i);
			thisElement.bumpDown(_howmuch);
		}
	}

	public boolean upToDateInCache(URL url, String filename) {
		long fileDate = 0;
		try {

			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			fileDate = urlConnection.getLastModified();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			System.out.println("Couldn't info on" + url.toString());

		} catch (IOException e) {
			System.out.println("Couldn't info on" + url.toString());

		}
		File cacheDirectory = new File("./cache/");
		File[] files = cacheDirectory.listFiles();
		File cachedFile = null;
		for (int f = 0; f < files.length; f++) {
			File thisFile = files[f];
			if (thisFile.getName().equals(filename)) {
				cachedFile = thisFile;
				break;
			}
		}
		/*
		 * if (cachedFile == null){ System.out.println(" no cached version of " +
		 * filename); }else{ System.out.println(cachedFile + " " +
		 * cachedFile.lastModified() + " " + fileDate); }
		 */
		return (cachedFile != null && (cachedFile.lastModified() > fileDate));
	}

	public void start() {
		setMode(Screen.MOVININ);
		// reset for next time? do this in start instead
		currentTransitionFrame = 0;
		for (int i = 0; i < screenElements.size(); i++) {
			ScreenElement s = (ScreenElement) screenElements.get(i);
			s.startElement();
		}
	}

	/*
	 * public void setCenterOffset(int _mx, int _my, int _mz){ for (int i = 0; i <
	 * screenElements.size(); i++) { ScreenElement s = (ScreenElement)
	 * screenElements.get(i); //int dy = (i) * DisplayApp.descriptionSize /
	 * DisplayApp.scaler + bottomOfHeadlines; s.setElementCenterOffset( _mx,
	 * _my+i*40, _mz); } }
	 */
	public void setEndPoints(int _ox, int _oy, int _oz, int _dx, int _dy, int _dz, boolean _addRandom) {
		// setMode(Screen.MOVININ);
		// reset for next time? do this in start instead
		currentTransitionFrame = 0;
		for (int i = 0; i < screenElements.size(); i++) {
			ScreenElement s = (ScreenElement) screenElements.get(i);
			// int dy = (i) * DisplayApp.descriptionSize / DisplayApp.scaler +
			// bottomOfHeadlines;
			s.setElementEndPoints(_ox, _oy, _oz, _dx, _dy, _dz, _addRandom);
		}
	}

	public ArrayList getPreviewElements() {
		return previewElements;
	}

	public String getTitle() {
		return headline;
	}

	public String getDate() {
		return date;
	}
	public String getUniqueID() {
		return uniqueID;
	}


	void breakIntoLines(String _content, ArrayList _lines, FontMetrics _fontMetrics) {
		String[] arrayOfLines = _content.split("\n");
		for (int i = 0; i < arrayOfLines.length; i++) {
			String thisLine = arrayOfLines[i];
			// if (thisLine.trim().length() == 0)
			// continue;
			float headlineWidth = _fontMetrics.stringWidth(thisLine);

			String[] words = thisLine.split(" ");
			String subLine = "";

			for (int j = 0; j < words.length; j++) {
				subLine = subLine + " " + words[j];
				headlineWidth = _fontMetrics.stringWidth(subLine);

				if ((headlineWidth > DisplayApp.width - DisplayApp.sideMargin*2 ) || (j == words.length - 1)) {

					_lines.add(subLine);
					subLine = "";

				}
			}

		}
	}

	void setMode(int _mode) {
		mode = _mode;
	}

	int move(PApplet g) {

		for (int i = 0; i < screenElements.size(); i++) {
			ScreenElement s = (ScreenElement) screenElements.get(i);
			s.display(g);
		}
		// alpha = alpha + 1;
		// if ((z == destination_z) || (z == origin_z)){

		if (mode == MOVININ) {
			for (int i = 0; i < screenElements.size(); i++) {
				ScreenElement s = (ScreenElement) screenElements.get(i);
				s.incrementPosition();
			}
			currentTransitionFrame++;
			if (currentTransitionFrame == numOfTransitionframes) {
				currentTransitionFrame = 0;

				started = System.currentTimeMillis();

				for (int i = 0; i < screenElements.size(); i++) {
					ScreenElement s = (ScreenElement) screenElements.get(i);
					s.arrived();
				}

				mode = STAYING;
			}

		} else if (mode == MOVINOUT) {
			for (int i = 0; i < screenElements.size(); i++) {
				ScreenElement s = (ScreenElement) screenElements.get(i);
				s.incrementPosition();
			}
			currentTransitionFrame++;
			if (currentTransitionFrame == numOfTransitionframes) {
				mode = GONE;
				for (int i = 0; i < screenElements.size(); i++) {
					ScreenElement s = (ScreenElement) screenElements.get(i);

				}
			}

		} else if (mode == STAYING) {
			/*
			 * displayDescription(g); if (movie != null) { if (movie.getTime() ==
			 * 0) { movie.play(); System.out.println("Play movie"); }
			 * movie.grabFrame(); // g.drawImage(movie.getImage(), x -
			 * movie.getVideoWidth() / 2, // y + headLines.size() *
			 * DisplayApp.headLineSize / // DisplayApp.scaler, null); }
			 */
			if (System.currentTimeMillis() - started > stayingTime) {
				mode = STARTOUT;
				for (int i = 0; i < screenElements.size(); i++) {
					ScreenElement s = (ScreenElement) screenElements.get(i);
					s.startOut();
				}

			}

		} else if (mode == STARTOUT) {

			// if (movie != null) {
			// movie.stop();
			// movie.setTime(0);
			// }
			mode = MOVINOUT;

		}

		return mode;

	}

	//public void setTopSpacing(int i) {
	//	topSpacing = i;

	//}

	//public int getTopSpacing() {
	//	return topSpacing;

	//}

	/*
	 * void flipEndPoints(){ int temp_x = destination_x; int temp_y =
	 * destination_y; int temp_z = destination_z; destination_x = origin_x;
	 * destination_y = origin_y; destination_z = origin_z; origin_x = temp_x;
	 * origin_y = temp_y; origin_z = temp_z; setVectors(); }
	 */
	/*
	 * void displayTitle(PApplet g) {
	 * 
	 * g.fill(200, 255, 200); // g.setColor(new Color(200, 255, 200)); //
	 * fill(255,255,255,alpha); // g.setFont(DisplayApp.headlineFont);
	 * g.textFont(DisplayApp.headlineFont);
	 * 
	 * g.pushMatrix(); g.translate(x, y,z); g.rotateX(xr); g.rotateY(yr);
	 * g.rotateZ(zr);
	 * 
	 * if (img != null) {
	 * 
	 * g.image(pimg, -pimg.width/2,0); //g.image(pimg, x - img.getWidth() / 2, y +
	 * headLines.size() * // DisplayApp.headLineSize / DisplayApp.scaler); //
	 * g.drawImage(img, x - img.getWidth() / 2, y + headLines.size() * //
	 * DisplayApp.headLineSize / DisplayApp.scaler, null); } if (movie != null) {
	 * movie.grabFrame(); PImage myImage = new PImage(movie.getImage());
	 * g.image(myImage,-myImage.width/2,0); //g.image( myImage, x -
	 * movie.getVideoWidth() / 2, y + // headLines.size() *
	 * DisplayApp.headLineSize / DisplayApp.scaler); } for (int i = 0; i <
	 * headLines.size(); i++) { String headline = (String) headLines.get(i); int
	 * len = DisplayApp.headlineMetrics.stringWidth(headline);
	 * 
	 * g.text(headline, -len / 2, i * DisplayApp.headLineSize /
	 * DisplayApp.scaler); // g.text(headline, x - len / 2, y + i *
	 * DisplayApp.headLineSize / // DisplayApp.scaler); //
	 * g.drawString(headline, x - len / 2, y + i * // DisplayApp.headLineSize /
	 * DisplayApp.scaler); } g.popMatrix(); }
	 */
	/*
	 * void openMovie(String _myFileName, boolean _start, boolean _loop, boolean
	 * _isFile) {
	 * 
	 * MoviePlayer myMoviePlayer;
	 * 
	 * int duration;
	 * 
	 * float rate = 1.0f;
	 * 
	 * Movie myMovie = null; try { // open up the video QTSession.open(); }
	 * catch (Exception ee) { System.out.println("QuickTime For Java Not
	 * Installed in the current Java jre/lib/ext folder"); ee.printStackTrace();
	 * QTSession.close(); } DataRef fileRef = null;
	 * 
	 * try {
	 * 
	 * if (_isFile) { fileRef = new DataRef(_myFileName); } else { QTFile myFile =
	 * new QTFile(_myFileName); fileRef = new DataRef(myFile); } myMovie =
	 * Movie.fromDataRef(fileRef, StdQTConstants4.newMovieAsyncOK |
	 * StdQTConstants.newMovieActive); myMoviePlayer = new MoviePlayer(myMovie);
	 * System.out.println("Got Movie " + myMovie + " and player " +
	 * myMoviePlayer);
	 * 
	 * while (myMovie.maxLoadedTimeInMovie() == 0) { myMovie.task(100); }
	 * 
	 * if (_loop) { myMovie.getTimeBase().setFlags(StdQTConstants.loopTimeBase); } //
	 * QDRect r = myMoviePlayer.getDisplayBounds(); // kWidth = r.getWidth();
	 * //set this in the interhited class // kHeight = r.getHeight(); // create
	 * offscreen space and link movie to it
	 * 
	 * if (_start) { myMoviePlayer.setRate(rate); } else {
	 * myMoviePlayer.setRate(0.0f); } myMovie.prePreroll(0, 1.0f);
	 * 
	 * try { myMovie.preroll(0, 1.0f); } catch (QTException ee) {
	 * System.out.println("Problem with preroll" + ee); } try { duration =
	 * myMoviePlayer.getDuration(); } catch (StdQTException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } } catch (QTException
	 * ee) { System.out.println("Problem with getting movie and player" + ee); } }
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * //setVectors(center_x, center_y, center_z, 2 * Math.PI, 2 * Math.PI, 2 *
	 * Math.PI); // destination_x = center_x; // destination_y = center_y; //
	 * destination_z = 0; // destination_z = _z; // z = _z; // video = null; /*
	 * if (img != null) { float maxWidth = (float) DisplayApp.width * 2 / 3;
	 * float maxHeight = (float) DisplayApp.height * 2 / 3; float widthRatio =
	 * img.getWidth() / maxWidth; float heightRatio = img.getHeight() /
	 * maxHeight; int newWidth = -1; int newHeight = -1; if (widthRatio > 1.0f &&
	 * widthRatio > heightRatio) { newWidth = (int) (img.getWidth() /
	 * widthRatio); newHeight = (int) (img.getHeight() / widthRatio); } if
	 * (heightRatio > 1.0f && heightRatio > widthRatio) { newWidth = (int)
	 * (img.getWidth() / heightRatio); newHeight = (int) (img.getHeight() /
	 * heightRatio); } if (newWidth != -1) { // System.out.println("Scaled");
	 * BufferedImage scaledBI = new BufferedImage(newWidth, newHeight,
	 * BufferedImage.TYPE_INT_RGB); Graphics2D scaledBIGraphics =
	 * scaledBI.createGraphics(); scaledBIGraphics.drawImage(img, 0, 0,
	 * newWidth, newHeight, null); img = scaledBI; } }
	 */
	/*
	 * 
	 * void displayDescription(PApplet g) { // fill(255,255,255,alpha); //
	 * g.setFont(DisplayApp.contentFont); g.textFont(DisplayApp.contentFont);
	 * g.fill(255, 255, 255); // g.setColor(new Color(255, 255, 255)); for (int
	 * i = 0; i < descriptionLines.size(); i++) { String line = (String)
	 * descriptionLines.get(i); g.text(line, x - descriptionXOffset, y + i *
	 * DisplayApp.descriptionSize / DisplayApp.scaler + headLines.size() *
	 * DisplayApp.headLineSize / DisplayApp.scaler); // g.drawString(line, x -
	 * descriptionXOffset, y + i * // DisplayApp.descriptionSize /
	 * DisplayApp.scaler + headLines.size() // * DisplayApp.headLineSize /
	 * DisplayApp.scaler); } // text(description, -40, headLines.size()*60); }
	 */
	// } else if (end != -1) {
	/*
	 * int start = originalContent.lastIndexOf("href=", end);
	 * 
	 * String urlString = originalContent.substring(start + 6, end + 4); //
	 * Authenticator.setDefault(new DisplayApp.MyAuthenticator());
	 * 
	 * QTMoviePixelSource movie = new QTMoviePixelSource(urlString, true, true,
	 * true, 30); if (movie != null) { stayingTime = movie.getDuration(); }
	 * ScreenElement se = new ScreenElement(this, movie, DisplayApp.width / 2 -
	 * movie.getVideoWidth() / 2, bottomOfHeadlines); screenElements.add(se);
	 */
}
