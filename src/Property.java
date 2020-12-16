import java.util.List;

public class Property extends BoardSpace {
	int purchasePrice;
	int mortgagePrice;
	boolean isMortgaged;
	List<Integer> payouts;
	Player owner;
	
	public Property(
	 int location,
	 String name,
	 SpaceKind kind,
	int purchasePrice,
	int mortgagePrice,
	 List<Integer> payouts
	)
	{
		super(location, name, kind);
		this.purchasePrice = purchasePrice;
		this.mortgagePrice = mortgagePrice;
		isMortgaged = false;
		this.payouts = payouts;
		owner = Main.BANKER;
	}
}
