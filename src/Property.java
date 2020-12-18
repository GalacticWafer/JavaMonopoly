import java.util.List;

public class Property extends BoardSpace {
	int purchasePrice;
	int mortgagePrice;
	boolean isMortgaged;
	List<Integer> payouts;
	
	public Property(
	 int location,
	 String name,
	 SpaceKind kind,
	int purchasePrice,
	int mortgagePrice,
	 List<Integer> payouts
	)
	{
		super(name, kind);
		this.purchasePrice = purchasePrice;
		this.mortgagePrice = mortgagePrice;
		isMortgaged = false;
		this.payouts = payouts;
	}
	
	public String printCost() {
		return this + " costs $" + purchasePrice;
	}
}
