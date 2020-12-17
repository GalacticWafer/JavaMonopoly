package monopoly;

public class BoardSpace {
	private int location;
	public String name;
	SpaceKind kind;
	Estate cp;
	RailRoad rr;
	
	public BoardSpace(int location, String name, SpaceKind kind) {
		this.location = location;
		this.name = name;
		this.kind = kind;
	}
	@Override public  String toString() {
		return name;
	}
}
