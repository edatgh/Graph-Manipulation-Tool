
/*
 *   This file is a part of GMT project
 *
 *   Edward Gess
 */



import java.io.*;
import javax.swing.*;

/*
 *   GraphManager class definition (experimental)
 */

/*
 *   Auxiliary class (experimental)
 */
class GraphsItem {
	private Graph graph;
	private GraphsItem prev;
	private GraphsItem next;

	public void setGraph(Graph gGraph) { graph=gGraph; }
	public Graph getGraph() { return graph; }
	public void setPrev(GraphsItem gGraphs) { prev=gGraphs; }
	public GraphsItem getPrev() { return prev; }
	public void setNext(GraphsItem gGraphs) { next=gGraphs; }
	public GraphsItem getNext() { return next; }
}

class Graphs {
	private GraphsItem firstItem;

	public boolean add(Graph graph) {
		if (find(graph.getName())!=null) {
			remove(graph.getName()); /*   if exists, then remove   */
		}

		GraphsItem gi=new GraphsItem();

		gi.setGraph(graph);
		gi.setPrev(null);
		gi.setNext(firstItem);

		if (firstItem!=null)
			firstItem.setPrev(gi);

		firstItem=gi;

		return true;
	}

	public boolean remove(String sName) {
		GraphsItem deadItem=firstItem;

		while (deadItem!=null) {
			if (deadItem.getGraph().getName().equals(sName)) {
				/*
				 *   1st case: deadItem is the only item of the list
				 */
				if (deadItem.getPrev()==null&&deadItem.getNext()==null) {
					firstItem=null; /* now the list is empty */
				} else {
					/*
					 *   2nd case: deadItem is on the first position of the list
					 */
					if (deadItem.getPrev()==null) {
						deadItem.getNext().setPrev(null);
						firstItem=firstItem.getNext(); /* shift forward */
					} else {
						/*
						 *   3rd case: deadItem is on the last position of the list
						 */
						if (deadItem.getNext()==null) {
							deadItem.getPrev().setNext(null);
							deadItem.setPrev(null);
							deadItem.setNext(null);
						/*
						 *   4th case: deadItem is in the middle of the list
						 */
						} else {
							deadItem.getPrev().setNext(deadItem.getNext());
							deadItem.getNext().setPrev(deadItem.getPrev());
						} /* 3rd else */
					} /* 2nd else */
				} /* 1st else */
				return true;
			} /* name */
			deadItem=deadItem.getNext();
		} /* while */
		return false;
	}

	public Graph find(String sName) {
		GraphsItem gi=firstItem;

		while (gi!=null) {
			if (gi.getGraph().getName().equals(sName))
				return gi.getGraph();

			gi=gi.getNext();
		}
		return null;
	}

	public Graph getFirst() { return firstItem==null?null:firstItem.getGraph(); }
	public Graph getNext(Graph gStart) {
		GraphsItem gi=firstItem;

		if (gStart==null)
			return null;

		while (gi!=null) {
			if (gi.getGraph()==gStart)
				return gi.getNext()==null?null:gi.getNext().getGraph();

			gi=gi.getNext();
		}
		return null;
	}

	public void enum(GraphEnumerator ge) {
		for (Graph g=getFirst();g!=null;g=getNext(g))
			ge.enum(g.getName());
	}
}

interface GraphEnumerator {
	public void enum(String sName);
}

public class GraphManager {
	private final int V_FLAG=0;
        private final int L_FLAG=1;
	public Graphs graphs; /*   VIOLATION   */

	public Graph findGraph(String sName) { return graphs.find(sName); }

	public GraphManager() {
		graphs=new Graphs();
	}

	public Graph createGraph(String sName) {
		Graph graph=new Graph(sName);

		graphs.add(graph);

		return graph;
	}

	public void addGraph(Graph graph) {
		graphs.add(graph);
	}

	public boolean save(String fileName,String graphName) {

		final Graph graph=graphs.find(graphName);

		if (graph==null)
			return false; /*   graph was not found   */

		try {
			final DataOutputStream dos=new DataOutputStream(new FileOutputStream(fileName));
			graph.enumVertices(new VertexEnumerator() {
					public void enum(String sName) {

						Vertex v=graph.getVertex(sName);

						try {
							dos.writeInt(V_FLAG);

							dos.writeInt(v.getName().length());
							dos.writeBytes(v.getName());

							dos.writeInt(v.getData().getX());
							dos.writeInt(v.getData().getY());

							dos.writeInt(v.getData().getCaption().length());
							dos.writeBytes(v.getData().getCaption());

							dos.writeInt(v.getData().getDescription().length());
							dos.writeBytes(v.getData().getDescription());
						} catch(IOException e) {
							return;
						}
					}
				});
			graph.enumVertices(new VertexEnumerator() {
					public void enum(String sName) {
						final Vertex v=graph.getVertex(sName);
						v.enumLinks(new LinkEnumerator() {
								public void enum(String sName) {
									try {
										dos.writeInt(L_FLAG);

										dos.writeInt(v.getName().length());
										dos.writeBytes(v.getName());

										dos.writeInt(sName.length());
										dos.writeBytes(sName);

									} catch(IOException e) {

									}
								}
							});
					}
				});
			try {
				dos.close();
			} catch(IOException e) {}
		} catch(FileNotFoundException e) {
			return false;
		}

		return true;
	}
	
	public Graph load(String fileName) {
		final Graph lgraph=new Graph(fileName);
		try {
			DataInputStream dis=new DataInputStream(new FileInputStream(fileName));
			try {
				for (;;) {
				        int b=dis.readInt();

					if (b==V_FLAG) {
						int nLen=dis.readInt();
						byte[] name=new byte[nLen];
						dis.readFully(name);
						String vName=new String(name);

						int x=dis.readInt();
						int y=dis.readInt();

						int cLen=dis.readInt();
						byte[] capt=new byte[cLen];
						dis.readFully(capt);
						String vCapt=new String(capt);

						int dLen=dis.readInt();
						byte[] desc=new byte[dLen];
						dis.readFully(desc);
						String vDesc=new String(desc);

						lgraph.addVertex(vName,x,y,vCapt,vDesc);
					}

					if (b==L_FLAG) {
						int snLen=dis.readInt();
						byte[] sn=new byte[snLen];
						dis.readFully(sn);
						String srcName=new String(sn);

						int dnLen=dis.readInt();
						byte[] dn=new byte[dnLen];
						dis.readFully(dn);
						String destName=new String(dn);

						lgraph.linkVertices(srcName,destName);
					}
				} /* for */
			} catch(EOFException e) {

			} catch(IOException e) {
				return null;
			}
			try {
				dis.close();
			} catch (IOException e) {/*   do nothing   */}
		} catch(FileNotFoundException e) {
			return null;
		}
		return lgraph;
	}

	public void destroyGraph(String sName) {
		graphs.remove(sName);
	}
}
