public enum Color { Blue,  Green, LightBlue, Orange, Pink, Purple, Red, Yellow;
	public static Color parseKind(String string) {
		for(Color color: Color.values()){
			if((color.toString()).equals(string)) {
				return color;
			}
		}
		return null;
	}
}
