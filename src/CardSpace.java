public class CardSpace extends BoardSpace {
	private  Deck deck;
	
	public CardSpace(int location, String name, Deck deck, SpaceKind kind) {
		super(name,kind);
		this.deck = deck;
	}
}
