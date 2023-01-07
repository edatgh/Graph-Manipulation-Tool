
/*
 *   All the stuff linked with GUI resists in this file
 *
 *   Edward Gess
 */

/*
 *   Essential imports
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/*
 *   GraphManipulationTool - Main program class, i.e. class with the
 *                           method called "main"
 */
public class GraphManipulationTool {
	public static void main(String[] args) {
		GMTWindow wnd=new GMTWindow(GMTConfiguration.PROGRAM_VERSION);
	}
}

/*
 *   GMTWindow - class of a main window of the program
 */
class GMTWindow extends JFrame {

	public GMTMainMenu mainMenu; /*   VIOLATION   */
	private GMTWorkspace workspace;
	private GMTAdvanced advanced;

	public GMTInformational informational;

	/*
	 *   GMTWindow - class constructor, creates full-featured window, i.e. constructs it
	 *               from the parts: "Menu", "Workspace", "Advanced" and "Informational"
	 */
	public GMTWindow(String sCaption) {
		super(sCaption); /*   create a frame with the given caption   */
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); /*   what to do on close ?   */

		Container cntPane=this.getContentPane();

		cntPane.setLayout(new FlowLayout(FlowLayout.LEFT,
						 GMTConfiguration.DEFAULT_GAP,
						 GMTConfiguration.DEFAULT_GAP));

		workspace=new GMTWorkspace(this);
		advanced=new GMTAdvanced(this);
		informational=new GMTInformational(this);

		mainMenu=new GMTMainMenu(this,workspace);
		this.setJMenuBar(mainMenu); /*   add menu bar   */

		cntPane.add(workspace);
		cntPane.add(advanced);
		cntPane.add(informational);

		this.setSize(GMTConfiguration.MAIN_WINDOW_WIDTH,
			     GMTConfiguration.MAIN_WINDOW_HEIGHT+
			     GMTConfiguration.DEFAULT_MENU_HEIGHT); /*   set appropriate size of the window   */
		this.show(); /*   display the window on the screen   */
	}
}

/*
 *   GMTMainMenu - class of a main menu of the program
 */
class GMTMainMenu extends JMenuBar implements ActionListener {
	private final JMenu[] menus={ new JMenu("Graph manager"),
				      new JMenu("Graph"),
				      new JMenu("Help") };

	private final JMenuItem[] gmItems={ new JMenuItem("Create graph"),
					    new JMenuItem("Save graph"),
					    new JMenuItem("Load graph"),
					    new JMenuItem("Enum graphs"),
					    new JMenuItem("Exit") };

	private final JMenuItem[] graphItems={ new JMenuItem("foobaz"),
					       new JMenuItem("foobar") };

	private final JMenuItem[] helpItems={ new JMenuItem("About") };

	private GMTWindow wnd;
	private GMTWorkspace ws;

	/*
	 *   Constructor
	 */
	public GMTMainMenu(GMTWindow wnd,GMTWorkspace ws) {
		this.wnd=wnd;
		this.ws=ws;

		/*
		 *   Construct "Graph manager" menu
		 */
		for (int i=0;i<gmItems.length;i++) {

			if (i==4)
				menus[0].addSeparator();

			gmItems[i].addActionListener(this);
			menus[0].add(gmItems[i]);
		}

		/*
		 *   Construct "Graph" menu
		 */
		for (int i=0;i<graphItems.length;i++) {
			graphItems[i].addActionListener(this);
			menus[1].add(graphItems[i]);
			menus[1].setEnabled(false);
		}

		/*
		 *   Construct "Help" menu
		 */
		for (int i=0;i<helpItems.length;i++) {
			helpItems[i].addActionListener(this);
			menus[2].add(helpItems[i]);
		}

		/*
		 *   Create menu bar using above constructed menus
		 */
		for (int i=0;i<menus.length;i++)
			this.add(menus[i]);
	}

	public void enableGraphMenu() {
		menus[1].setEnabled(true);
	}

	public void disableGraphMenu() {
		menus[1].setEnabled(false);
	}

	public void actionPerformed(ActionEvent e) {

		if (((JMenuItem)e.getSource()).getText().equals("Create graph")) {
			String sName=JOptionPane.showInputDialog("Enter a name of a graph");
			if (!GMTUtil.filled1(sName)) {
			        wnd.informational.printError("You must specify a name!");

				return;
			}
			ws.createGraph(sName);
			wnd.informational.printInfo("Graph ["+sName+"] was created successfully.");

			return;
		}

		if (((JMenuItem)e.getSource()).getText().equals("Save graph")) {
			ws.saveGraph();
		}

		if (((JMenuItem)e.getSource()).getText().equals("Load graph")) {
			ws.loadGraph();
		}

		if (((JMenuItem)e.getSource()).getText().equals("Enum graphs")) {
			wnd.informational.printInfo("--- List of graphs available in the Graph Manager ---");
			ws.graphManager.graphs.enum(new GraphEnumerator() {
					public void enum(String sName) {
						wnd.informational.printInfo("["+sName+"]");
					}
				});
			wnd.informational.printInfo("-----------------------------------------------------");
		}

		if (((JMenuItem)e.getSource()).getText().equals("Exit")) {
			wnd.dispose(); /*   Destroy the main window   */
			System.exit(0); /*   exit...   */
		}
	}
}

/*
 *   GMTWorkspace - class of a graph representation area
 */
class GMTWorkspaceMenu extends JPopupMenu implements ActionListener {
	private final JMenuItem[] wsmItems={ new JMenuItem("Add vertex") };
	private GMTWorkspace ws;
	private int x;
	private int y;

	public GMTWorkspaceMenu(GMTWorkspace invoker,int x,int y) {
		this.x=x;
		this.y=y;

		for (int i=0;i<wsmItems.length;i++) {
			wsmItems[i].addActionListener(this);
			this.add(wsmItems[i]);
		}
		ws=invoker;
		this.show(invoker,x,y);
	}

	public void actionPerformed(ActionEvent e) {
		if (((JMenuItem)e.getSource()).getText().equals("Add vertex")) {
			ws.addVertex(x,y);
			return;
		}
	}
}

class GMTWorkspaceMouseListener extends MouseAdapter {

	private GMTWorkspace ws;

	public GMTWorkspaceMouseListener(GMTWorkspace invoker) {
		ws=invoker;
	}

	public void mouseClicked(MouseEvent e) {

		ws.awaitsVFL(false,null);
		ws.awaitsLFR(false,null); /*   reset the awaitings...   */

		if (e.getButton()!=MouseEvent.BUTTON1)
			return;

		new GMTWorkspaceMenu(ws,e.getX(),e.getY());
	}
}

/*
 * ############################################## Workspace ################################################
 */

class GMTWorkspace extends JPanel {

	public GMTWindow parent;

	public GraphManager graphManager; /*   VIOLATION   */
	public Graph currentGraph;
	public GMTEdges edges;

	private GMTWorkspaceMouseListener wsml;
	private boolean awaitsVertexForLink;
	private boolean awaitsLinkForRemove;
	private String vflNameSrc;
	private String lfrNameSrc;

	private String activeVertexName;
	private GMTActiveVertexCircle activeVertexCircle;

	public GMTWorkspace(GMTWindow parent) {
		this.parent=parent;

		/*
		 *   GUI
		 */
		this.setLayout(null); /*   no layout needed here   */
		this.setBackground(Color.white);
		this.setPreferredSize(new Dimension(GMTConfiguration.WORKSPACE_PAN_WIDTH,
						    GMTConfiguration.WORKSPACE_PAN_HEIGHT));
		this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.gray),
								"Workspace"));

		/*
		 *   Graph
		 */
		graphManager=new GraphManager();
		edges=new GMTEdges();
	}

	public void enableMenu() {
		if (wsml!=null)
			return; /*   prevent re-enabling of menu   */

		wsml=new GMTWorkspaceMouseListener(this);
		this.addMouseListener(wsml);
		parent.mainMenu.enableGraphMenu();
	}

	public void disableMenu() {
		if (wsml==null)
			return; /*   prevent re-disabling of menu   */

		this.removeMouseListener(wsml);
		wsml=null;
		parent.mainMenu.disableGraphMenu();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		for (GMTEdge edge=edges.getFirst();edge!=null;edge=edge.getNext()) {
			if (edge.dead)
				edge.erase(g);
			else
				edge.draw(g);
		}

		if (activeVertexCircle!=null)
			activeVertexCircle.draw(g);
	}

	/*
	 *   Set of methods for controlling currently loaded graph
	 */
	public void loadGraph() {

		if (currentGraph!=null) {
			int r=JOptionPane.showConfirmDialog(null,
							    "Do you want to save the current graph?",
							    "Save",
							    JOptionPane.YES_NO_OPTION,
							    JOptionPane.QUESTION_MESSAGE);
			if (r==JOptionPane.YES_OPTION) {
				saveGraph();
			}
		}

		String sName=JOptionPane.showInputDialog("Enter name of a graph");
		if (!GMTUtil.filled1(sName)) {
			parent.informational.printError("You must specify a name of a graph to load!");
			return;
		}



		unloadGraph(); /*   unload the current graph   */

		currentGraph=graphManager.findGraph(sName); /*   try to find the graph in the graph manager   */

		if (currentGraph==null) { /*   graph was not found in the GM, load it from file...   */
			parent.informational.printInfo("The graph ["+
						    sName+
						    "] was not found in the graph manager, trying to load it from a file...");
			currentGraph=graphManager.load(sName);
			if (currentGraph==null) {
				parent.informational.printError("Unable to load graph: ["+sName+"]!");
				return;
			}
		}

		/*
		 *   Now do the visual part of loading a graph
		 */
		final Graph g=currentGraph;
		currentGraph.enumVertices(new VertexEnumerator() {
				public void enum(String sName) {
					Vertex v=g.getVertex(sName);
					guiAddVertex(sName,
						     v.getData().getX(),
						     v.getData().getY(),
						     v.getData().getCaption(),
						     v.getData().getDescription());
				}
			});
		currentGraph.enumVertices(new VertexEnumerator() {
				public void enum(String sName) {
					Vertex v=g.getVertex(sName);
					final String ln=sName;
					v.enumLinks(new LinkEnumerator() {
							public void enum(String sName) {
								guiAddEdge(ln,sName);
							}
						});
				}
			});
		parent.informational.printInfo("Graph ["+sName+"] was loaded successfully.");
		graphManager.addGraph(currentGraph);
		enableMenu();
		this.repaint();
	}

	public void unloadGraph() {
		this.removeAll(); /*   remove the graph from the workspace   */
		edges=new GMTEdges();
		this.repaint();
		disableMenu();
	}

	public void createGraph(String sName) {
		unloadGraph();
		/*
		 *   Graph
		 */
		currentGraph=graphManager.createGraph(sName);
		enableMenu();
	}

	public void saveGraph() {
		if (currentGraph==null) {
			parent.informational.printError("There is no graph to save!");
			return;
		}

		if (!graphManager.save(currentGraph.getName(),currentGraph.getName()))
			parent.informational.printError("Unable to save graph ["+currentGraph.getName()+"]!");
		else
			parent.informational.printInfo("Graph ["+currentGraph.getName()+"] was saved successfully.");
	}

	public void destroyGraph(String sName) {
		/*
		 *   Graph
		 */
		graphManager.destroyGraph(sName);
		disableMenu();
	}

	public void addVertex(int x,int y) {

		String sVN=JOptionPane.showInputDialog("Enter name of the vertex");
		String sVC=JOptionPane.showInputDialog("Enter caption of the vertex");
		String sVD=JOptionPane.showInputDialog("Enter description of the vertex");

		if (!GMTUtil.filled3(sVN,sVC,sVD)) {
			parent.informational.printError("You must specify all info about the vertex!");
			return;
		}

		/*
		 *  Play with the particular graph
		 */
		boolean r=currentGraph.addVertex(sVN,x,y,sVC,sVD);
		if (r)
			parent.informational.printInfo("Vertex ["+sVC+"] was added successfully.");
		else {
			parent.informational.printError("Unable to add the vertex!");
			parent.informational.printHint("Please check the name of the vertex...");
			return;
		}

		/*
		 *   Do the visual stuff
		 */
		GMTVertex vert=new GMTVertex(this,sVN,x,y,sVC,sVD);
		this.add(vert);
		this.repaint();
	}

	public void guiAddVertex(String sName,int x,int y,String sCaption,String sDescription) {
		/*
		 *   Do the visual stuff
		 */
		GMTVertex vert=new GMTVertex(this,sName,x,y,sCaption,sDescription);
		this.add(vert);

		parent.informational.printInfo("Added vertex: ["+sName+"]");
	}

	public void setActiveVertex(String sName,String sCaption) {
		activeVertexName=sName;
		activeVertexCircle=new GMTActiveVertexCircle(this,
							     currentGraph.getVertex(sName).getData().getX(),
							     currentGraph.getVertex(sName).getData().getY(),
							     sCaption);
		repaint();
	}

	public void removeVertex(final GMTVertex v) {
		final GMTNames n=new GMTNames();
		currentGraph.getVertex(v.getName()).enumLinks(new LinkEnumerator() {
				public void enum(String sName) {
					n.add(sName);
				}
			});
		n.enum(new GMTNameEnumerator() {
				public void enum(String sName) {
					removeEdge(v.getName(),sName);
				}
			});

		currentGraph.enumVertices(new VertexEnumerator() {
				public void enum(String sName) {
					if (sName.equals(v.getName()))
						return;

					removeEdge(sName,v.getName());
				}
			});

		if (!currentGraph.removeVertex(v.getName())) {
			parent.informational.printError("Unable to remove vertex: ["+v.getName()+"]");
			return;
		}

		if (activeVertexName!=null)
			if (activeVertexName.equals(v.getName())) {
				activeVertexName=null; /*   active vertex will be removed, so there will be no active vertex   */
				activeVertexCircle=null; /*   remove the circle   */
			}

		this.remove(v);
		this.repaint();
	}

	public void addEdge(String nameSrc,String nameDest) {
		vflNameSrc=nameSrc;
		addEdge(nameDest);
		vflNameSrc=null;
	}

	public void guiAddEdge(String nameSrc,String nameDest) {
		Vertex v1=currentGraph.getVertex(nameSrc);
		Vertex v2=currentGraph.getVertex(nameDest);

		if (v1==null||v2==null) {
			parent.informational.printError("guiAddEdge(): Unable to get one or more vertices!");
			return;
		}

		int x1=v1.getData().getX();
		int y1=v1.getData().getY();
		int x2=v2.getData().getX();
		int y2=v2.getData().getY();

		edges.add(x1,y1,x2,y2);

		parent.informational.printInfo("Added edge: ["+nameSrc+"]--->["+nameDest+"]");

	}

	public void removeEdge(String nameSrc,String nameDest) {
		lfrNameSrc=nameSrc;
		removeEdge(nameDest);
		lfrNameSrc=null;
	}

	public void addEdge(String vflNameDest) {
		Vertex v1=currentGraph.getVertex(vflNameSrc);
		Vertex v2=currentGraph.getVertex(vflNameDest);

		if (!currentGraph.linkVertices(vflNameSrc,vflNameDest)) {
			parent.informational.printError("Unable to add path: ["+vflNameSrc+"]--->["+vflNameDest+"]");
			return;
		}

		if (v1==null||v2==null) {
			parent.informational.printError("Unable to add path: ["+vflNameSrc+"]--->["+vflNameDest+"]");
			return;
		}

		int x1=v1.getData().getX();
		int y1=v1.getData().getY();
		int x2=v2.getData().getX();
		int y2=v2.getData().getY();

		edges.add(x1,y1,x2,y2);
		parent.informational.printInfo("Added path: ["+vflNameSrc+"]--->["+vflNameDest+"]");

		repaint();
	}

	public void removeEdge(String lfrNameDest) {
		Vertex v1=currentGraph.getVertex(lfrNameSrc);
		Vertex v2=currentGraph.getVertex(lfrNameDest);

		if (!v1.isLinked(v2.getName())) {
			parent.informational.printError("The path ["+lfrNameSrc+"]--->["+lfrNameDest+"] doesn't exists!");
			return;
		}

		if (v1==null||v2==null) {
			parent.informational.printError("Unable to remove path: ["+lfrNameSrc+"]--->["+lfrNameDest+"]");
			return;
		}

		int x1=v1.getData().getX();
		int y1=v1.getData().getY();
		int x2=v2.getData().getX();
		int y2=v2.getData().getY();

		GMTEdge e=edges.find(x1,y1,x2,y2);
		e.dead=true;
		this.repaint();

		edges.remove(x1,y1,x2,y2);
		parent.informational.printInfo("Removed path: ["+lfrNameSrc+"]-x->["+lfrNameDest+"]");
	}

	public boolean awaitsVFL() { return awaitsVertexForLink; }
	public void awaitsVFL(boolean bVal,String sName) {
		awaitsVertexForLink=bVal;
		vflNameSrc=sName;
	}
	public boolean awaitsLFR() { return awaitsLinkForRemove; }
	public void awaitsLFR(boolean bVal,String sName) {
		awaitsLinkForRemove=bVal;
		lfrNameSrc=sName;
	}

}

/*
 *   GMTAdvanced - class of an advanced options window
 */
class GMTAdvanced extends JPanel {

	private GMTWindow parent;

	public GMTAdvanced(GMTWindow parent) {
		this.parent=parent;

		this.setBackground(Color.white);
		this.setPreferredSize(new Dimension(GMTConfiguration.ADVANCED_PAN_WIDTH,
						    GMTConfiguration.ADVANCED_PAN_HEIGHT));
		this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.gray),
								"Advanced"));
	}
}

/*
 *   GMTInformational - class of an informational window of the
 *                      program
 */
class GMTInformational extends JPanel {

	private GMTWindow parent;
	private JTextArea textOut;

	public GMTInformational(GMTWindow parent) {
		this.parent=parent;

		this.setBackground(Color.white);
		this.setPreferredSize(new Dimension(GMTConfiguration.INFORMATIONAL_PAN_WIDTH,
						    GMTConfiguration.INFORMATIONAL_PAN_HEIGHT));
		this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.gray),
								"Informational window"));
		textOut=new JTextArea();
		textOut.setEditable(false); /*   this area is read-only   */

		JScrollPane sp=new JScrollPane(textOut);

		sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		this.setLayout(new GridLayout(1,1));
		this.add(sp);
	}

	public void printError(String sMsg) {
		textOut.setDisabledTextColor(Color.red);
		textOut.append("ERROR: "+sMsg+"\n");
	}

	public void printInfo(String sMsg) {
		textOut.setDisabledTextColor(Color.blue);
		textOut.append("INFORMATION: "+sMsg+"\n");
	}

	public void printHint(String sMsg) {
		textOut.setDisabledTextColor(Color.green);
		textOut.append("HINT: "+sMsg+"\n");
	}
}

/*
 *   Classes of graphical objects which will appear on the workspace
 */

class GMTVMenu extends JPopupMenu implements ActionListener {
	private final JMenuItem[] vmItems={ new JMenuItem("Information"),
					    new JMenuItem("Enum links"),
					    new JMenuItem("Set active"),
					    new JMenuItem("Add path"),
					    new JMenuItem("Remove path"),
					    new JMenuItem("Remove") };

	private GMTWorkspace ws;
	private GMTVertex v;

	public GMTVMenu(GMTWorkspace parent,GMTVertex invoker,int x,int y) {
		ws=parent;
		v=invoker;

		for (int i=0;i<vmItems.length;i++) {
			vmItems[i].addActionListener(this);
			this.add(vmItems[i]);
		}
		this.show(ws,x,y);
	}

	public void actionPerformed(ActionEvent e) {
		if (((JMenuItem)e.getSource()).getText().equals("Information")) {

			return;
		}

		if (((JMenuItem)e.getSource()).getText().equals("Enum links")) {
			ws.parent.informational.printInfo("--------------------[Paths]-------------------");
			ws.currentGraph.getVertex(v.getName()).enumLinks(new LinkEnumerator() {
					public void enum(String sName) {
						ws.parent.informational.printInfo("["+v.getCaption()+"]--->["+sName+"]");
					}
				});
			ws.parent.informational.printInfo("----------------------------------------------");
		}

		if (((JMenuItem)e.getSource()).getText().equals("Set active")) {
			ws.setActiveVertex(v.getName(),v.getCaption());
			ws.parent.informational.printInfo("Vertex ["+v.getCaption()+"] was set as an active");
		}

		if (((JMenuItem)e.getSource()).getText().equals("Add path")) {
			ws.awaitsVFL(true,v.getName());
			ws.parent.informational.printHint("Click on a vertex");
			return;
		}

		if (((JMenuItem)e.getSource()).getText().equals("Remove path")) {
			ws.awaitsLFR(true,v.getName());
			ws.parent.informational.printHint("Click on a vertex");
			return;
		}

		if (((JMenuItem)e.getSource()).getText().equals("Remove")) {
			ws.removeVertex(v);
			return;
		}
	}
}

class GMTActiveVertexCircle {
	private GMTWorkspace ws;
	private String caption;

	public int x;
	public int y;
	public boolean active;

	public GMTActiveVertexCircle(GMTWorkspace ws,int x,int y,String sCaption) {
		this.ws=ws;
		this.x=x;
		this.y=y;
		active=true;
		caption=sCaption;
	}

	public void draw(Graphics g) {
		if (active)
			g.setColor(Color.red);
		else
			g.setColor(Color.white);

		int w=GMTConfiguration.GMTVERTEX_WIDTH;
		int h=GMTConfiguration.GMTVERTEX_HEIGHT;

		g.drawOval(x-w-1,y-h-1,w*2,h*2);

		/*
		 *   I think the caption of an active vertex may be useful
		 */
		g.drawChars(caption.toCharArray(),0,caption.length(),x+w+1,y);
	}
}

class GMTVMouseMotionListener extends MouseMotionAdapter {
	private GMTVertex v;
	private GMTWorkspace ws;
	private int ox;
	private int oy;

	public GMTVMouseMotionListener(GMTWorkspace ws,GMTVertex v,int x,int y) {
		this.v=v;
		this.ws=ws;
		ox=x;
		oy=y;
	}

	public void mouseDragged(MouseEvent e) {
		final int dx=e.getX()-ox;
		final int dy=e.getY()-oy;
		final Vertex v1=ws.currentGraph.getVertex(v.getName());
		final int vx=v1.getData().getX();
		final int vy=v1.getData().getY();
		final GMTWorkspace lws=ws;
		final int nx=vx+dx;
		final int ny=vy+dy;



		v1.enumLinks(new LinkEnumerator() {
				public void enum(String sName) {
					Vertex v2=lws.currentGraph.getVertex(sName);
					GMTEdge edge=lws.edges.find(v1.getData().getX(),v1.getData().getY(),
								    v2.getData().getX(),v2.getData().getY());
					if (edge==null)
						return;

					edge.x1=nx;
					edge.y1=ny;
					edge.redraw(lws.getGraphics());
					lws.repaint();
				}
			});

		lws.currentGraph.enumVertices(new VertexEnumerator() {
				private String destName=v1.getName();
				public void enum(String sName) {
					if (lws.currentGraph.pathExists(sName,destName)) {
						Vertex v2=lws.currentGraph.getVertex(sName);
						GMTEdge edge=lws.edges.find(v2.getData().getX(),v2.getData().getY(),
									    v1.getData().getX(),v1.getData().getY());

						if (edge==null)
							return;

						edge.x2=nx;
						edge.y2=ny;
						edge.redraw(lws.getGraphics());
						lws.repaint();
					}
				}
			});
		v.setLocation(nx,ny);
	}
}


class GMTVMouseListener extends MouseAdapter {
	private GMTVertex v;
	private GMTWorkspace ws;
	private GMTVMouseMotionListener vMML;

	public GMTVMouseListener(GMTWorkspace ws,GMTVertex v) {
		this.v=v;
		this.ws=ws;
	}

	public void mousePressed(MouseEvent e) {
		if (e.getButton()!=e.BUTTON2)
			return;

		vMML=new GMTVMouseMotionListener(ws,v,e.getX(),e.getY());
		v.addMouseMotionListener(vMML);
	}

	public void mouseReleased(MouseEvent e) {
		if (e.getButton()!=e.BUTTON2)
			return;

		v.removeMouseMotionListener(vMML);
	}
}

class GMTVertex extends JButton implements ActionListener {
	private GMTWorkspace parent;
	private String name; /*   this is a hidden field, i.e. it won't be displayed for the user   */
	private int x;
	private int y;
	private String caption;
	private String description;
	private int w;
	private int h;

	public GMTVertex(GMTWorkspace parent,String sName,int x,int y,String sCaption,String sDescription) {
		this.parent=parent;

		this.name=sName;
		this.x=x;
		this.y=y;
		this.caption=sCaption;
		this.description=sDescription;

		w=GMTConfiguration.GMTVERTEX_WIDTH;
		h=GMTConfiguration.GMTVERTEX_HEIGHT;

		this.setBounds(x-w/2,y-h/2,w,h);

		this.addActionListener(this);
		this.addMouseListener(new GMTVMouseListener(parent,this));
		this.setToolTipText("Name of the vertex: ["+name+"], caption: ["+caption+"]");
	}

	public void setLocation(int x,int y) {
		super.setLocation(x-w/2,y-h/2);
		parent.currentGraph.getVertex(name).getData().setX(x);
		parent.currentGraph.getVertex(name).getData().setY(y);
		this.x=x;
		this.y=y;
	}

	public String getName() { return name; }
	public int myGetX() { return x; }
	public int myGetY() { return y; }
	public String getCaption() { return caption; }
	public String getDescription() { return description; }

	public void actionPerformed(ActionEvent e) {
		if (parent.awaitsVFL()) {
			parent.addEdge(name);
			parent.awaitsVFL(false,null);
			return;
		}

		if (parent.awaitsLFR()) {
			parent.removeEdge(name);
			parent.awaitsLFR(false,null);
			return;
		}
		new GMTVMenu(parent,this,x-w/2,y-h/2); /*   make menu appear to cover the vertex   */
	}
}

class GMTEdge {
	public int x1;
	public int y1;
	public int x2;
	public int y2;
	public boolean dead;

	private GMTEdge nextEdge;

	public void setNext(GMTEdge geNext) { nextEdge=geNext; }
	public GMTEdge getNext() { return nextEdge; }

	public void draw(Graphics g) {
		g.setColor(Color.red);
		g.drawLine(x1,y1,x2,y2);
	}

	public void erase(Graphics g) {
		g.setColor(Color.white);
		g.drawLine(x1,y1,x2,y2);
	}

	public void redraw(Graphics g) {
		erase(g);
		draw(g);
	}
}

class GMTEdges {
	private GMTEdge firstEdge;

	public GMTEdge getFirst() { return firstEdge; }

	public void add(int x1,int y1,int x2,int y2) {
		GMTEdge ge=new GMTEdge();

		ge.x1=x1;
		ge.y1=y1;
		ge.x2=x2;
		ge.y2=y2;

		ge.setNext(firstEdge);
		firstEdge=ge;
	}

	public boolean remove(int x1,int y1,int x2,int y2) {
		GMTEdge ge;
		if ((ge=find(x1,y1,x2,y2))!=null) {
			if (ge==firstEdge) {
				firstEdge=ge.getNext();
				return true;
			}
			if (ge.getNext()==null) {
				GMTEdge pe=firstEdge;
				for (;pe.getNext()!=ge;pe=pe.getNext());
				pe.setNext(null);
				return true;
			}
			GMTEdge pe=firstEdge;
			for (;pe.getNext()!=ge;pe=pe.getNext());
			pe.setNext(ge.getNext());
			return true;
		}
		return false;
	}

	public GMTEdge find(int x1,int y1,int x2,int y2) {
		GMTEdge ge=firstEdge;

		while (ge!=null) {
			if (ge.x1==x1&&ge.y1==y1&&ge.x2==x2&&ge.y2==y2)
				return ge;
			ge=ge.getNext(); /* shift forward */
		}
		return null;
	}
}
