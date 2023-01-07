
/*
 *   This class is used for keeping program configuration in a single place
 *   reacheable from any point in the program
 */

public class GMTConfiguration {
	public static final String PROGRAM_VERSION="GMT-20031203, v0.9";

	public static final int DEFAULT_GAP=1;
	public static final int DEFAULT_MENU_HEIGHT=29;

	public static final int WORKSPACE_PAN_WIDTH=800;
	public static final int WORKSPACE_PAN_HEIGHT=600;

	public static final int ADVANCED_PAN_WIDTH=120;
	public static final int ADVANCED_PAN_HEIGHT=WORKSPACE_PAN_HEIGHT;

	public static final int INFORMATIONAL_PAN_WIDTH=WORKSPACE_PAN_WIDTH+ADVANCED_PAN_WIDTH+DEFAULT_GAP;
	public static final int INFORMATIONAL_PAN_HEIGHT=100;

	public static final int MAIN_WINDOW_WIDTH=WORKSPACE_PAN_WIDTH+ADVANCED_PAN_WIDTH+DEFAULT_GAP*3;
	public static final int MAIN_WINDOW_HEIGHT=WORKSPACE_PAN_HEIGHT+INFORMATIONAL_PAN_HEIGHT+DEFAULT_GAP*3;

	public static final int GMTVERTEX_WIDTH=10;
	public static final int GMTVERTEX_HEIGHT=GMTVERTEX_WIDTH;
}
