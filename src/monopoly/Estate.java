package monopoly;

import java.util.List;

public class Estate extends Property {
	private static final int MAX_HOUSES = 4;
	int houseCost;	
	Color color;
	int numHouses;
	boolean hasHotel;
	
	public Estate(
	 int location,
	 String name,
	 SpaceKind kind,
	 int purchasePrice,
	 int mortgagePrice,
	 List<Integer> payouts,
	 int houseCost,
	 Color color
	)
	{
		super(location, name, kind, purchasePrice, mortgagePrice, payouts);
		this.purchasePrice = purchasePrice;
		this.color = color;
		this.houseCost = houseCost;
		this.payouts = payouts;
		this.name = name;
		numHouses = 0;
		isMortgaged = false;
		hasHotel = false;
	}
	
	public int getRent(int groupedSiblings, int maxGroupSize) {
		if(groupedSiblings != maxGroupSize) {
			return payouts.get(0);
		} else if(numHouses > 0) {
			return payouts.get(numHouses);
		}
		Main.input("You pay twice as much since " + owner + " owns all the "
				   + color.toString().toLowerCase() + " properties");
		return payouts.get(0) * 2;
	}
	
	public boolean hasHotel() {
		return numHouses == MAX_HOUSES + 1;
	}
	
}

			
		
