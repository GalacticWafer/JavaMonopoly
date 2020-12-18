package monopoly;

public class BoardSpace {
	public String name;
	SpaceKind kind;
	
	public BoardSpace(String name, SpaceKind kind) {
		this.name = name;
		this.kind = kind;
	}
	@Override public  String toString() {
		return name;
	}
}
