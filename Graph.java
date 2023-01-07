
/*
 *   This file is a part of GMT project
 *
 *   Edward Gess
 */



/*
 *   Imports
 */
import java.util.*;
import java.io.*;

/*
 *   Graph class definition
 */
class Graph {
	/*
	 *   PRIVATE FIELDS
	 */
	private String name;
	private Vertices vertices; /* list of vertices available in the graph */

	/*
	 *   PUBLIC METHODS
	 */
	public Graph(String sName) {
		vertices=new Vertices();
		setName(sName);
	}

	public void setName(String sName) { name=sName; }
	public String getName() { return name; }

	public boolean addVertex(String sName,int iX,int iY,String sCaption,String sDescription) {

		/*
		 *   Create new data, which is used by the vertex
		 */
		VertexData vdNew=new VertexData();
		vdNew.setX(iX);
		vdNew.setY(iY);
		vdNew.setCaption(sCaption);
		vdNew.setDescription(sDescription);

		/*
		 *   Create new vertex with the specified data
		 */
		Vertex vNew=new Vertex(sName,vdNew);

		/*
		 *   Add the new vertex to the list of vertices
		 */
		return vertices.add(vNew);
	}

	/*
	 *   NOTE: This method is for digraph!
	 */
	public boolean linkVertices(String nameSrc,String nameDest) {
		Vertex srcV=vertices.find(nameSrc);
		Vertex destV=vertices.find(nameDest);
		
		if (srcV==null||destV==null)
			return false;

		return srcV.addLink(destV);
	}

	public boolean unlinkVertices(String nameSrc,String nameDest) {
		Vertex srcV=vertices.find(nameSrc);
		Vertex destV=vertices.find(nameDest);

		if (srcV==null||destV==null)
			return false;

		return srcV.removeLink(destV);
	}

	public boolean pathExists(String nameSrc,String nameDest) {
		Vertex v=vertices.find(nameSrc);

		if (v==null)
			return false;

		return v.isLinked(nameDest);
	}

	public boolean removeVertex(String sName) {
		return vertices.remove(sName);
	}

	public Vertex getVertex(String sName) {
		return vertices.find(sName);
	}

	public void enumVertices(VertexEnumerator ve) {
		Vertex v=vertices.getFirst();
		if (v==null)
			return;

		do {
			ve.enum(v.getName());
		} while ((v=vertices.getNext(v))!=null);
	}
}

interface VertexEnumerator {
	public void enum(String sName);
}

/*
 *   Graph applied class(es) definition
 */

class Vertices {
	private int cnt; /* counter of vertices */
	private VerticesItem firstItem;

	public void setCounter(int iCount) { cnt=iCount; }
	public int getCounter() { return cnt; }

	public boolean add(Vertex vVert) {
		if (find(vVert.getName())!=null)
			return false;
		cnt++;
		VerticesItem vi=new VerticesItem();

		vi.setVert(vVert);
		vi.setPrev(null);
		vi.setNext(firstItem);

		if (firstItem!=null)
			firstItem.setPrev(vi);

		firstItem=vi;

		return true;
	}

	public boolean remove(String sName) {
		VerticesItem deadItem=firstItem;

		while (deadItem!=null) {
			if (deadItem.getVert().getName().equals(sName)) {
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
				cnt--;
				return true;
			} /* name */
			deadItem=deadItem.getNext();
		} /* while */
		return false;
	}

	public Vertex find(String sName) {
		VerticesItem vi=firstItem;

		while (vi!=null) {
			if (vi.getVert().getName().equals(sName))
				return vi.getVert();

			vi=vi.getNext();
		}
		return null;
	}

	public Vertex getFirst() { return firstItem==null?null:firstItem.getVert(); }
	public Vertex getNext(Vertex vStart) {
		VerticesItem vi=firstItem;
		if (vStart==null)
			return null;
		while (vi!=null) {
			if (vi.getVert()==vStart)
				return vi.getNext()==null?null:vi.getNext().getVert();

			vi=vi.getNext();
		}
		return null;
	}
}

class VerticesItem {
	private Vertex vert;
	private VerticesItem prev;
	private VerticesItem next;

	public void setVert(Vertex vVert) { vert=vVert; }
	public Vertex getVert() { return vert; }
	public void setPrev(VerticesItem viPrev) { prev=viPrev; }
	public VerticesItem getPrev() { return prev; }
	public void setNext(VerticesItem viNext) { next=viNext; }
	public VerticesItem getNext() { return next; }
}

class VertexData {
	private int x;
	private int y;
	private String caption;
	private String description;

	public void setX(int iX) { x=iX; }
	public int getX() { return x; }
	public void setY(int iY) { y=iY; }
	public int getY() { return y; }
	public void setCaption(String sCaption) { caption=sCaption; }
	public String getCaption() { return caption; }
	public void setDescription(String sDescription) { description=sDescription; }
	public String getDescription() { return description; }
}

class Vertex {
	/*
	 *   PRIVATE FIELDS
	 */
	private String name; /* name of the vertex (unique for each vertex) */
	private String caption; /* caption of the vertex (may not be unique) */
	private VertexData data; /* data of the vertex (it's context may change with time) */
	private Vertices links; /* list of vertices reachable from this vertex */

	/*
	 *   PRIVATE METHODS
	 */

	/*
	 *   PUBLIC METHODS
	 */
	public Vertex(String sName,VertexData vd) {
		name=sName;
		data=vd;
		links=new Vertices();
	}

	public void setName(String sName) { name=sName; }
	public String getName() { return name; }

	public void setCaption(String sCaption) { caption=sCaption; }
	public String getCaption() { return caption; }

	public VertexData getData() { return data; }

	public boolean addLink(Vertex vDest) {
		return links.add(vDest);
	}

	public boolean removeLink(Vertex vDest) {
		return links.remove(vDest.getName());
	}

	public boolean isLinked(String sName) {
		return links.find(sName)!=null;
	}
	public void enumLinks(LinkEnumerator le) {
		Vertex l=links.getFirst();

		if (l==null)
			return;
		do {
			le.enum(l.getName());
		} while ((l=links.getNext(l))!=null);
	}
}

interface LinkEnumerator {
	public void enum(String sName);
}
