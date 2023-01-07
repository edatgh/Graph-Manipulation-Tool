class GMTName {
	private String name;
	private GMTName next;

	public GMTName(String sName) { name=sName; }

	public void setName(String sName) { name=sName; }
	public String getName() { return name; }

	public void setNext(GMTName nNext) { next=nNext; }
	public GMTName getNext() { return next; }
}

class GMTNames {
	private GMTName first;

	public void add(String sName) {
		GMTName nn=new GMTName(sName);
		nn.setNext(first);
		first=nn;
	}

	public void enum(GMTNameEnumerator ne) {
		GMTName n=first;

		while (n!=null) {
			ne.enum(n.getName());
			n=n.getNext();
		}
	}

	public boolean isEmpty() { return first==null; }
}

interface GMTNameEnumerator {
	public void enum(String sName);
}
