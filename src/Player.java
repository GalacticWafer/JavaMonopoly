import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

public class Player {
	public String name;
	public Integer i;
	public int inJail = -1;
	public Stack<Card> jailCards;
	int location;
	int money;
	//private HashMap<SpaceKind, ArrayList<Property>> propertyGroups;
	
	// Constructor
	public Player(String _s, Integer _i) {
		name = _s;
		i = _i;
		//propertyGroups = new HashMap<>();
		location = 0;
		money = 1500;
		jailCards = new Stack<>();
	}
	
	public void developmentCheck() {
		if(Main.ownerMap.values().stream().noneMatch(x -> x == this)) { return; }
		var modifiableEstates = canModify(true);
		String s = 
		 "[choice]   property   |   house cost   |  current houses\nCan Build:\n";
		Iterator<Estate> it = modifiableEstates.iterator();
		int i1 = 0;
		for(; i1 < modifiableEstates.size(); i1++) {
			Estate estate = it.next();
			s += "[" + i1 + "]:      " + estate + "(" + estate.color + ")       $" + estate.houseCost + "       "
					 + (estate.hasHotel ? "HOTEL" : estate.numHouses) + "\n";
		}
		s += "Can Demolish:\n";
		var canDemolish = canModify(false);
		it = canDemolish.iterator();
		for(int j = 0; j < canDemolish.size(); j++) {
			Estate estate = it.next();
			s += "[" + (i1+ j) + "]:      " + estate + "(" + estate.color + ")       $" + estate.houseCost + "       "
					 + (estate.hasHotel ? "HOTEL" : estate.numHouses) + "\n";
		}
		modifiableEstates.addAll(canDemolish);
		if(!Main.inputBool("Do you want to modify a property?")){ return; }
		do{
			int index = -1;
			index = Main.inputInt(s + "\nModify which property?");
			while(index < 0 || index >= modifiableEstates.size()) {
				if(!Main.inputBool("That's not a valid answer. Do you still want to add/remove houses?")) {
					return;
				}
				index = Main.inputInt("Modify which property?\n" + s);
			}
			Estate estate = modifiableEstates.get(index);
			if(estate.hasHotel) {
				System.out.println(estate + " already has a hotel!");
				continue;
			}
			int minHouses = 5;
			int maxHouses = 0;
			for(BoardSpace space: Main.colorGroups.get(estate.color)){
				Estate siblingEstate = (Estate)space;
				minHouses = Math.min(minHouses, siblingEstate.numHouses);
				maxHouses = Math.max(maxHouses, siblingEstate.numHouses);
			}
			var buildUp = Main.inputBool("Build up or demolish? (type 0 for Demolish)");
			if(minHouses == estate.numHouses && buildUp
			   || maxHouses == estate.numHouses && !buildUp) {
				// build a hotel or a house
				if(buildUp && estate.numHouses < 5) {
					if(estate.numHouses == 4) {
						// try to build a hotel
						if(Main.inputBool("Build a hotel on " + estate + "?")) {
							if(Main.hotels == 0) {
								Main.input("There are no houses left on the market to build with. Try again later, or sell a house to put it somewhere else.");
							} else {
								// build a hotel
								if(Main.inputBool("Build a hotel on " + estate + "?")) {
									estate.numHouses += 1;
									money -=  estate.houseCost;
									Main.hotels -= 1;
								}
							}
						}
					}else {
						// try to build a house
						if(Main.houses == 0) {
							Main.input("There are no houses left on the market to build with. Try again later, or sell a house to put it somewhere else.");
						} else {
							if(Main.inputBool("Build a house on " + estate + "?")) {
								// build a house
								estate.numHouses += 1;
								money -=  estate.houseCost;
								Main.houses -= 1;
							}
						}
					}
				} else if(!buildUp) {
					if(estate.numHouses == 5) {
						// try to demolish a hotel
						if(Main.inputBool("Demolish a hotel on " + estate + "?")) {
							if(Main.houses < 4) {
								Main.input("There are not enough houses left on the market to build with, because you would need 4 houses to to break down evenly.  Try again later, or sell a house to use it.");
							} else {
								// demolish a hotel
								if(Main.inputBool("Demolish a hotel on " + estate + "?")) {
									estate.numHouses = 4;
									money +=  estate.houseCost / 2;
									Main.hotels += 1;
								}
							}
						}
					}else {
						// try to demolish a house
						if(estate.numHouses == 0) {
							Main.input("There's nothing to demolish here!");
						} else {
							// demolish a house
							if(Main.inputBool("Demolish a house on " + estate + "?")) {
								estate.numHouses -= 1;
								money +=  estate.houseCost / 2;
								Main.houses += 1;
							}
						}
					}
				}
			} else {
				Main.input("You have to build up and break down properties evenly...");
			}
			modifiableEstates = canModify(true);
			s =
			 "[choice]   property   |   house cost   |  current houses\nCan Build:\n";
			it = modifiableEstates.iterator();
			i1 = 0;
			for(; i1 < modifiableEstates.size(); i1++) {
				estate = it.next();
				s += "[" + i1 + "]:      " + estate + "(" + estate.color + ")       $" + estate.houseCost + "       "
					 + (estate.hasHotel ? "HOTEL" : estate.numHouses) + "\n";
			}
			s += "Can Demolish:\n";
			canDemolish = canModify(false);
			it = canDemolish.iterator();
			for(int j = 0; j < canDemolish.size(); j++) {
				estate = it.next();
				s += "[" + (i1+ j) + "]:      " + estate + "(" + estate.color + ")       $" + estate.houseCost + "       "
					 + (estate.hasHotel ? "HOTEL" : estate.numHouses) + "\n";
			}
			modifiableEstates.addAll(canDemolish);
		} while(modifiableEstates.size() > 0 && Main.inputBool("would you like to modify any of your properties?\n" + s));
	}
	
	public ArrayList<Estate> canModify(boolean developing) {
		ArrayList<Estate> options = new ArrayList<>();
		for(ArrayList<Estate> group: Main.colorGroups.values()) {
			if(Main.ownsAll(group.get(0), this)) {
				int target = developing ? 5 : 0;
				for(Property property: group) {
					Estate cp = (Estate)property;
					target = developing ? Math.min(cp.numHouses, target) : Math.max(cp.numHouses, target);
				}
				for(Property property: group) {
					Estate cp = (Estate)property;
					if(cp.numHouses == target) {
						options.add(cp);
					}
				}
			}
		}
		return options;
	}
	// Jail
	public Card goToJail() { 
		inJail = 0;
		if(jailCards.size() > 0){
			if(Main.inputBool(name + ", do you want to use a" +
			 "get-out-of-jail-free card?")){
				Card card = jailCards.pop();
			System.out.println("You have " + (jailCards.isEmpty() ?
			 "no" : "one") + " card left.");
			return card;
			}
		}
		if(Main.inputBool("Do you want to try to roll doubles to get out of jail?")){
			int[] roll = Main.roll();
			if(roll[0] == roll[1]) {
				Main.println("Nice roll, you're off the hook this time.");
				inJail = -1;
			}
			return null;
		}
		if(money >= 50 && Main.inputBool("Do you want to pay $50 to get out of jail," +
		 " or spend time in the slammer?", "pay $50", "stay in jail")){
			money -= 50;
			Main.println("You have $" + money + " left.");
			inJail = -1;
			return null;
		}
		Main.println("Looks like you're stuck in jail for one round");
		return null;
	}
	
	public String printBalance() {
		return name + " has $" + money;
	}
	
	public void setBalance(int i) {
		money += i;
	}
	
	@Override public String toString() {
		return name;
	}
}
