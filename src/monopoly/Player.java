package monopoly;

import java.util.*;

public class Player {
	public static final int MODIFY = 1;
	public static final int NO_JUST_ROLL = 0;
	public static final int SELL = 1;
	public String name;
	public Integer i;
	public int inJail = -1;
	public Stack<Card> jailCards;
	int location;
	int money;
	
	// Constructor
	public Player(String _s, Integer _i) {
		name = _s;
		i = _i;
		//propertyGroups = new HashMap<>();
		location = 0;
		money = 1500;
		jailCards = new Stack<>();
	}
	
	public int canPayDebt(int owedAmount) {
		if(money >= owedAmount) {
			return owedAmount;
		}
		int total = 0;
		System.out.println("you're broke! You better start selling things...");
		do {
			var modifiableEstates = canModify(false);
			var sellableProperties = Main.ownerMap.keySet().stream().filter(x -> x.owner == this && (!(x instanceof Estate) || ((Estate)x).numHouses == 0)).toArray();
			if(modifiableEstates.size() == 0 && sellableProperties.length == 0 && money < owedAmount) {
				System.out.println(this + " is completely broke and has nothing left to sell... You lose!");
				return total;
			}
			String[] options = new String[0];
			if(modifiableEstates.size() != 0 && sellableProperties.length != 0) {
				options = new String[] {"Sell Houses", "Sell Properties"};
			} else if(modifiableEstates.size() != 0) {
				options = new String[] {"Sell Houses"};
			} else if(sellableProperties.length != 0){
				options = new String[] {"Sell Properties"};
			}
			int choice = Main.inputOption("What do you want to do?", options);
			if(choice == 1) {
				String s;
				s =  printBalance() + "\n[choice]   property   |   house cost   |  current houses\n";
				s = getModifiables(modifiableEstates, modifiableEstates.size(), s, modifiableEstates.iterator());
				Integer index = selectProperty(modifiableEstates, s);
				Estate estate = modifiableEstates.get(index);
				int sum = modifyProperty(estate, false);
				money += sum;
				total += sum;
			} else {
				String[] saleOptions = new String[sellableProperties.length];
				for(int i1 = 0; i1 < sellableProperties.length; i1++) {
						saleOptions[i1] = sellableProperties[i1].toString();
				}
				int index = Main.inputOption("Sell which property?", saleOptions);
				Property sellableProperty = (Property)sellableProperties[index];
				int sum = sellableProperty.purchasePrice / 2;
				if(Main.inputBool(this + " is about to sell " + sellableProperty + " for $" + sum + ". Are you sure?")) {
					sellableProperty.owner = Main.BANKER;
					Main.ownerMap.put(sellableProperty, Main.BANKER);
					money += sum;
					total += sum;
				}
			}
			printBalance();
		} while(money < owedAmount);
		return total;
	}
	
	public void developmentCheck() {
			if(Main.ownerMap.values().stream().noneMatch(x -> x == this)) { return; }
			var modifiableEstates = canModify(true);
			int modifiablesCount = 0;
			ArrayList<Estate> canDemolish;
			String s =  printBalance() + "\n[choice]   property   |   house cost   |  current houses\nCan Build:\n";
			Iterator<Estate> modifiablesItr = modifiableEstates.iterator();
			Estate estate;
		s = getModifiables(modifiableEstates, modifiablesCount, s, modifiablesItr);
		int option = Main.inputOption("Do you want to modify/sell properties?", new String[] {"No (Roll)", "Modify Properties", "Sell Properties"});
			if(modifiableEstates.size() == 0 || option == NO_JUST_ROLL){return;}
			do{
				if(option == SELL) {
					sellProperty();
				} else if(option == MODIFY) {
					do{
						Integer index = selectProperty(modifiableEstates, s);
						int minHouses = 5;
						int maxHouses = 0;
						if(index == null) { return; }
						estate = modifiableEstates.get(index);
						if(estate.hasHotel) {
							System.out.println(estate + " already has a hotel!");
							continue;
						}
						for(BoardSpace space: Main.colorGroups.get(estate.color)){
							Estate siblingEstate = (Estate)space;
							minHouses = Math.min(minHouses, siblingEstate.numHouses);
							maxHouses = Math.max(maxHouses, siblingEstate.numHouses);
						}
						var buildUp = Main.inputBool("Build up or demolish?", "Build", "Demolish");
						if(minHouses == estate.numHouses && buildUp
						   || maxHouses == estate.numHouses && !buildUp) {
							modifyProperty(estate, buildUp);
						} else {
							Main.input("You have to build up and break down properties evenly...");
						}
						modifiableEstates = canModify(true);
						s = printBalance() + "\n[choice]   property   |   house cost   |  current houses\nCan Build:\n";
						modifiablesItr = modifiableEstates.iterator();
						modifiablesCount = 0;
						s = getModifiables(modifiableEstates, modifiablesCount, s, modifiablesItr);
					} while(modifiableEstates.size() > 0 && Main.inputBool("would " + this + " you like to modify any more properties?\n"));
				}
			}
			while(Main.inputOption("Do you want to modify/sell properties?", new String[] {"Modify Properties", "Sell Properties", "No"}) !=
				  NO_JUST_ROLL);
		}
	
	private String getModifiables(ArrayList<Estate> modifiableEstates, int modifiablesCount, String s, Iterator<Estate> modifiablesItr) {
		Estate estate;
		ArrayList<Estate> canDemolish;
		for(; modifiablesCount < modifiableEstates.size(); modifiablesCount++) {
			estate = modifiablesItr.next();
			s += "[" + (modifiablesCount + 1) + "]:      " + estate + "(" + estate.color + ")       $" + estate.houseCost + "       " + (estate.hasHotel ? "HOTEL" : estate.numHouses) + "\n";
		}
		canDemolish = canModify(false);
		if(canDemolish.size() > 0) {
			s += "Can Demolish:\n";
			modifiablesItr = canDemolish.iterator();
			for(int j = 0; j < canDemolish.size(); j++) {
				estate = modifiablesItr.next();
				s += "[" + (modifiablesCount + j + 1) + "]:      " + estate + "(" + estate.color + ")       $" + estate.houseCost + "       " + (estate.hasHotel ? "HOTEL" : estate.numHouses) + "\n";
			}
			modifiableEstates.addAll(canDemolish);
		}
		return s;
	}
	
	private int modifyProperty(Estate estate, boolean buildUp) {
		if(buildUp && estate.numHouses < 5) {
			return tryBuild(estate);
		} else if(!buildUp) {
			return tryDemolish(estate);
		}
		return 0;
	}
	
	private int tryDemolish(Estate estate) {
		if(estate.numHouses == 5) {
			// try to demolish a hotel
			if(Main.inputBool("Demolish a hotel on " + estate + "?")) {
				if(Main.houses < 4) {
					Main.input("There are not enough houses left on the market to build with, because you would need 4 houses to to break down evenly.  Try again later, or sell a house to use it.");
				} else {
					// demolish a hotel
					if(Main.inputBool("Demolish a hotel on " + estate + "?")) {
						estate.numHouses = 4;
						int sum = estate.houseCost / 2;
						money += sum;
						Main.hotels += 1;
						return sum;
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
					int sum = estate.houseCost / 2;
					money += sum;
					Main.houses += 1;
					return sum;
				}
			}
		}
		return 0;
	}
	
	private int tryBuild(Estate estate) {
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
		} else {
			// try to build a house
			if(Main.houses == 0) {
				Main.input("There are no houses left on the market to build with. Try again later, or sell a house to put it somewhere else.");
			} else {
				if(Main.inputBool("Build a house on " + estate + " for $" + estate.houseCost + "?")) {
					// build a house
					estate.numHouses += 1;
					money -=  estate.houseCost;
					Main.houses -= 1;
				}
			}
		}
		return 0;
	}
	
	// @org.jetbrains.annotations.Nullable
	private Integer selectProperty(ArrayList<Estate> modifiableEstates,
								   String s) {
		int index = -1;
		index = Main.inputInt(s + "\nModify which property?");
		while(index < 1 || index > modifiableEstates.size()) {
			if(!Main.inputBool("That's not a valid answer. Do you still want to add/remove houses?")) {
				return null;
			}
			index = Main.inputInt("Modify which property?\n" + s);
		}
		return index - 1;
	}
	
	private void sellProperty() {
		var sellableProperties = Main.ownerMap.keySet().stream().filter(x -> x.owner == this && (x instanceof Utility || x instanceof RailRoad || x instanceof Estate estate && estate.numHouses == 0)).toArray();
		var itr = Arrays.stream(sellableProperties).iterator();
		int choice = Main.inputOption("Which property would you like to sell?",itr, sellableProperties.length);
		Property property = (Property)sellableProperties[choice];
		if(Main.inputBool("Are you sure you want to sell " + property + " for $" + property.mortgagePrice + "?")) {
			money += property.mortgagePrice;
			Main.ownerMap.put(property, Main.BANKER);
			property.owner = Main.BANKER;
			Main.inputBool(printBalance());
		}
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
					if(cp.numHouses == target && (!developing && cp.numHouses > 0 || developing && cp.numHouses < 5)) {
						options.add(cp);
					}
				}
			}
		}
		return options;
	}
	// Jail
	
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