package main;


public class GW {

	public static int SX(int value) {
        return GameWindow.instance.scaleX(value); // Asumiendo que scaleX es static
    }

    public static int SY(int value) {
        return GameWindow.instance.scaleY(value);
    }
    
    public static int SQ(int value) {
    	return GameWindow.instance.scaleSquare(value);
    }
	
    public static double DSY(double value) {
        return GameWindow.instance.DscaleY(value);
    }
}
