package monopoly;

import java.util.List;

public class RailRoad extends Property {
	public RailRoad(
	 int location,
	 String name,
	 SpaceKind kind,
	 int purchasePrice,
	 int mortgagePrice,
	 List<Integer> payouts
	)
	{
		super(location, name, kind, purchasePrice, mortgagePrice, payouts);
	}
	public int getRent(int otherSiblings) {
		return payouts.get(otherSiblings);
	}
}





