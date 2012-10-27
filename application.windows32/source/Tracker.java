import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;

import processing.core.PApplet;
import processing.core.PImage;


 class Tracker {
	

//	PImage backgroundImage;
	PImage[] backgroundImages = new PImage[2];
	int whichBackgroundImage = 0;
	
	PImage video;
	PApplet parent;
	public Tracker(PApplet _parent, PImage _video){
		parent = _parent;
		video = _video;
		//backgroundImage = new PImage(video.width, video.height, parent.RGB);
		backgroundImages[0] = new PImage(video.width, video.height, parent.RGB);
		backgroundImages[1] = new PImage(video.width, video.height, parent.RGB);
	}
	public ArrayList getChangeEdges(ArrayList expectedPoints){
		//loadPixels();
		whichBackgroundImage++;
		PImage backgroundImage = backgroundImages[whichBackgroundImage % 2];
	//	ArrayList myPolys = new ArrayList();
		ArrayList myRects = new ArrayList();
		ArrayList newPoints = new ArrayList();
		//int debugColor = color(255, 0, 0);
		//fill(color(255,255,0));
		int spacing = 30;

		int threshold = 40;
		int minSize = 200;
		int[][] directions = { { 0, 1 }, { 1, 1 }, { 1, 0 }, { 1, -1 }, { 0, -1 }, { -1, -1 }, { -1, 0 }, { -1, 1 } };
		float videoRatio = parent.width/(float)video.width;
		float circle8th = (float)(2*Math.PI/8);
		float[] angles = {0.0f,circle8th,2*circle8th,3*circle8th,4*circle8th,5*circle8th,6*circle8th,7*circle8th};
		Rectangle videoBounds = new Rectangle(1, 1, video.width - 2, video.height - 2);
	
		for (int row = spacing; row < video.height; row = row + spacing) {
			for (int col = spacing; col < video.width; col = col + spacing) {
				int fillTestColor = 0;
				


				// make a hypothetical Polygon
				//Polygon thisPoly = new Polygon();
				Rectangle thisRect = null;
				for (int direction = 0; direction < directions.length; direction++) {
					// pull out the directions
					int[] thisDirection = directions[direction];
					// take a step in that direction
					int testx = col + thisDirection[0];
					int testy = row + thisDirection[1];
					
					boolean thisPointInsideExistingBlob = false;
					//for (int i = 0; i < myPolys.size(); i++) {
						for (int i = 0; i < myRects.size(); i++) {
						Rectangle otherRect = (Rectangle) myRects.get(i);
						//Polygon otherPoly = (Polygon) myRects.get(i);
						//if (otherPoly.contains(testx, testy)) {
							if (otherRect.contains(testx, testy)) {
							thisPointInsideExistingBlob = true;
							//println("bail");
							break;
						}
					}
					
					// if already in a another skip this iteration of the loop
					if (thisPointInsideExistingBlob) continue;
					
					
					boolean foundSomething = false;
					// as long you don't go out of bounds of your video go in a
					// direction
					while (videoBounds.contains(testx, testy)) {
						int offset = testy * video.width + testx;
						int thisPixel = video.pixels[offset];

						//pixels[offset] =0xff00ff00; //debugColor;

						float r = parent.red(thisPixel);
						float g = parent.green(thisPixel);
						float b = parent.blue(thisPixel);
						
						int bgColor = backgroundImage.pixels[offset];
						float bgR = parent.red(bgColor);
						float bgG = parent.green(bgColor);
						float bgB = parent.blue(bgColor);
						float bgdiff = parent.dist(r, g, b, bgR, bgG, bgB);
					
						if (bgdiff > threshold) {
							int previousPixel = video.pixels[offset - 1];
							float prevR = parent.red(previousPixel);
							float prevG = parent.green(previousPixel);
							float prevB = parent.blue(previousPixel);
							float hdiff = parent.dist(r, g, b, prevR, prevG, prevB);
							if (hdiff > threshold) {
								foundSomething = true;
								break;
							}
						
						
						}
						// take a step in this direction
						testx = testx + thisDirection[0];
						testy = testy + +thisDirection[1];
					}
					// add as far as you got in this direction to the rect
					if (foundSomething){
						if (thisRect == null)
						 thisRect = new Rectangle(testx,testy,1,1);
						else thisRect.add(testx,testy);
						//thisPoly.addPoint(testx, testy);
				
						newPoints.add(new PreviewPoint((int) (testx * videoRatio),(int) (testy * videoRatio),(float)(angles[direction]-Math.PI) ));

						//newPoints.add(new int[] {(int)(testx * videoRatio), (int)(testy * videoRatio)});
					
					}
				}
				// if the rect added up to anything
				if (thisRect != null && thisRect.getBounds().width * thisRect.getBounds().height > minSize) 
					myRects.add(thisRect);
				//myPolys.add(thisPoly);
				//myPolys.add(thisRect);
			
			}
	
		}
		

	  	int[] distances = new int[expectedPoints.size()*newPoints.size()];
	  	int[][] pairings = new int[expectedPoints.size()*newPoints.size()][3]; //dist,expectedIndex,newIndex
		for (int i = 0; i < expectedPoints.size(); i++) {
			PreviewPoint expectedPoint = (PreviewPoint) expectedPoints.get(i);
			float expectedX = expectedPoint.x;
			float expectedY = expectedPoint.y ;

			for (int j = 0; j < newPoints.size(); j++) {
				int placeInArray = i*newPoints.size() + j;
				//int[] newRect = (int[]) newPoints.get(j);
				PreviewPoint newPoint = (PreviewPoint) newPoints.get(j);
				int dist = (int) (parent.dist(expectedX, expectedY,newPoint.x, newPoint.y  ));
				distances[placeInArray] = dist;
				pairings[placeInArray] = new int[] {dist,i,j};
		
			}
		}

		// sort the distances
		Arrays.sort(distances);
		//keep track if these things have been spoken for already 
		boolean[] checkListForExpected = new boolean[expectedPoints.size()];
		boolean[] checkListForNew = new boolean[newPoints.size()];
		//keep a variable that knows if you you have found enough
		int found = 0;
		//go down the distances in order and find the pairing with the least distance
		for (int i = 0; i < distances.length; i++) {
			int closeOne = distances[i];
			for (int j = 0; j < pairings.length; j++){
				int[] thisPairing = pairings[j];
				int dist = thisPairing[0];
				int expectedIndex = thisPairing[1];
				int newIndex = thisPairing[2];
				//if this pairing has the distance you are after and it's parts are not spoken for
				if (dist == closeOne &&  checkListForNew[newIndex] == false && checkListForExpected[expectedIndex] == false) { // not spoken for
					PreviewPoint expectedPoint = (PreviewPoint) expectedPoints.get(expectedIndex);
					//int[] newPoint = (int[]) newPoints.get(newIndex);
					PreviewPoint newPoint = (PreviewPoint) newPoints.get(newIndex);

					checkListForExpected[expectedIndex] = true; // mark this found as used
					checkListForNew[newIndex] = true;
					expectedPoint.setLoc(newPoint.x, newPoint.y, newPoint.angle);

					found++;
			
				}
				if (found >= newPoints.size()) break;
			}
			if (found >= newPoints.size()) break;
		}

		for (int i = 0; i < checkListForExpected.length; i++) {
			if (checkListForExpected[i] == false){
				PreviewPoint expectedPoint = (PreviewPoint) expectedPoints.get(i);
				expectedPoint.improvise();
			}
		}
		
		backgroundImage.copy(video, 0, 0, video.width, video.height, 0, 0, video.width, video.height);
		backgroundImage.updatePixels();

		return newPoints;

	}
	
	
	


}


