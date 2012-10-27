
class PreviewPoint {
	int x;
	int y;
	ScreenElement screenElement;
	float angle;
	boolean found;

	public PreviewPoint(int _x, int _y, float _angle){
		x = _x;
		y = _y;
		angle = _angle;
	}

	public void setScreenElement(ScreenElement _se){
		screenElement = _se;
	}
	
	public void setLoc(int _x, int _y, float _angle){
		x = _x;
		y = _y;
		angle = _angle;
		found = true;
	}
	
	public ScreenElement getScreenElement(){
		return screenElement;
	}
	
	public void improvise(){
		found = false;
	}

}
