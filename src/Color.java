public enum Color { Blue,  Green, LightBlue, Orange, Pink, Purple, Red, Yellow;
		public static Color parseKind(String string) {
			for(Color color: Color.values()){
				if((color.toString()).equals(string)) {
					return color;
				}
			}
			return null;
		}
		
		public int count() {
			switch(this) {
				case Blue, Purple: { return 2;}  
				case Green, Red, Pink, Orange, LightBlue, Yellow: { return 3;}
			}
			throw new IllegalArgumentException();
		}
	}
