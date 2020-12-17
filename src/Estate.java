import jdk.jfr.Unsigned;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
	
	public boolean addHouse(){
		int groupedSiblingsCount = Main.countGroupedSiblings(this, this.owner);
		int maxGroupSize = (int)Main.board.stream()
										.filter(space -> space instanceof Estate && ((Estate)space).color == color)
										.count() - 1;
		if(groupedSiblingsCount != maxGroupSize) {
			Main.input("You have to own all properties of this color to modify housing.");
			return false;
		}
		var groupedSiblings = Main.ownerMap.entrySet()
							.stream()
							.filter(entry -> entry.getValue() == owner
											 && entry.getKey().kind == kind
											 && entry.getKey() != this).collect(Collectors.toList());
		int minHouses = MAX_HOUSES;
		int maxHouses = 0;
			String s = "estate | houses";
		for(Map.Entry<Property, Player> groupedSibling: groupedSiblings) {
			Estate estate = (Estate)groupedSibling.getKey();
			int otherHouses = estate.numHouses;
			s += estate.toString() + " | " + otherHouses + "\n"; 
			minHouses = Math.min(minHouses, otherHouses);
			maxHouses = Math.max(maxHouses, otherHouses);
		}
		if(maxHouses - minHouses > 1) {
			if (minHouses != 4) {
				Main.input("You must build up houses evenly.\n" + s);
				return false;
			}
			if(!hasHotel && houseCost <= owner.money) {
				Main.input("putting a hotel on " + this);
				owner.setBalance(-houseCost);
				Main.input(owner.printBalance());
				numHouses = 0;
				hasHotel = true;
				return true;
			}
			if(hasHotel) {
				Main.input("You are already maxed out with a Hotel Here!");
			} else {
				System.out.println("You don't have enough money!\n" + owner.printBalance());
			}
			return false;
		}
		if((minHouses == maxHouses || numHouses < maxHouses) 
		   && owner.money >= houseCost){
			if(numHouses < MAX_HOUSES) {
				numHouses++;
				Main.input(this + " now has " + numHouses + " houses");
				return true;
			}
			if(Main.hotels == 0) {
				Main.input("There are no hotels available!");
				return false;
			} else {
				Main.hotels -= 1;
				Main.input(this + " now has a hotel!");
				owner.setBalance(-houseCost);
				return true;
			}
		}
		return false;
	}
	
	public int getNumHouses(){
		return numHouses;
	}
	
	public boolean hasHotel() {
		return numHouses == MAX_HOUSES + 1;
	}
	
	public int hotelCount(){
		return hasHotel ? 1 : 0;
	}
	public Color getColor() { return color;}
	
}

			
		
