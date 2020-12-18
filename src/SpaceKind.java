public enum SpaceKind { 
	Chance,
	Chest,
	Corner,
	IncomeTax,
	LuxuryTax,
	Railroad,
	Utility,
	Blue,
	Green,
	LightBlue,
	Orange,
	Pink,
	Purple, 
	Red,
	Yellow;
	public static SpaceKind parseKind(String string) {
		for(SpaceKind kind: SpaceKind.values()){
			if((kind.toString()).equals(string)) {
				return kind;
			}
		}
		return Corner; // This should never happen...
	}
	
	public int count() {
		switch(this) {
			case Blue, Purple -> {return 2;}
			case Green, Red, Pink, Orange, LightBlue, Yellow -> { return 3;}
		}
		throw new IllegalArgumentException();
	}
}
