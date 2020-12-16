public enum SpaceKind { Chance, Chest, Corner, IncomeTax, LuxuryTax, Color, Railroad, Utility;
	public static SpaceKind parseKind(String string) {
		for(SpaceKind kind: SpaceKind.values()){
			if((kind.toString()).equals(string)) {
				return kind;
			}
		}
		return Corner; // This should never happen...
	}
}
