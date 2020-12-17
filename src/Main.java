import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
	public static final Player BANKER = new Player("Banker", -1);
	public static final String CHANCE_STRING = "ChanceCards.csv";
	public static final String CHEST_STRING = "CommunityChestCards.csv";
	private static final int GO_TO_JAIL = 30;
	public static final Integer JAIL = 10;
	private static final int MAX_HOUSES = 4;
	public static ArrayList<Player> PLAYERS;
	public static final String SPACES_STRING = "board_spaces.csv";
	public static final int TOO_MANY_DOUBLES = 3;
	public static ArrayList<BoardSpace> board;
	public static HashMap<Color, ArrayList<BoardSpace>> colorGroups;
	public static HashMap<SpaceKind, Integer> groupCounts;
	public static int hotels = 12;
	public static int houses = 30;
	public static HashMap<Integer, BoardSpace> locationMap;
	public static HashMap<Property, Player> ownerMap;
	
	private static void auction(ArrayList<Player> bidders,
								Property property) {
		Player player = bidders.get(0);
		int highestBid = property.purchasePrice;
		for(int i = 0; bidders.size() > 1; i++) {
			if(i == bidders.size()) { i = 0; }
			player = bidders.get(i);
			boolean quit = false;
			if(player.money >= highestBid &&
			   inputBool("Does " + player + " want to continue to bid?" +
						 "\nhighest bid: " + highestBid + "\nBalance: " +
						 player.money + "?")) {
				int bid;
				while(true) {
					bid = inputInt("How much do you want to pay?");
					if(bid > highestBid) {
						highestBid = bid;
						break;
					} else {
						if(inputBool(bid + " is not higher than " + highestBid +
									 ". Quit bidding?")) {
							quit = true;
							break;
						}
					}
				}
				if(quit) { bidders.remove(i--); }
			} else { bidders.remove(i--); }
		}
		if(inputBool("All other bidders have been eliminated." + player + "," +
					 " do you want to buy " + property + " for $" + highestBid +
					 "?")) {
			input(player + " has won the auction for " + property + " at $" +
				  highestBid + "!");
			player.money -= highestBid;
			ownerMap.put(property, player);
		}
	}
	
	public boolean ownsAll(SpaceKind kind, Player player){
		int playerOwnedCount = 0;
		for(BoardSpace boardSpace: colorGroups.get(kind)) {
			//Todo some logic to count how many player owns.
		}
		return groupCounts.get(kind) == playerOwnedCount;
	}
	// initialization
	private static ArrayList<BoardSpace> boardInit(Deck chance, Deck chest)
	throws FileNotFoundException {
		locationMap = new HashMap<>();
		ownerMap = new HashMap<>();
		colorGroups = new HashMap<>();
		ArrayList<BoardSpace> board = new ArrayList<>(40);
		Scanner scanner = new Scanner(new File(Main.SPACES_STRING));
		for(int i = 0; scanner.hasNextLine(); i++) {
			String[] space = scanner.nextLine().split(",");
			switch(space[6]) {
				case "Color": {
					Estate cp = new Estate(
					 i, // location
					 space[1],    // String name    	          
					 SpaceKind.Color, // SpaceKind kind
					 Integer.parseInt(space[2]),  // int purchasePrice
					 Integer.parseInt(space[5]),   // int mortgagePrice
					 Arrays.stream(space[4]
					  .split(" "))  // List<Integer> payouts     	        
						   .map(Integer::valueOf).collect(Collectors.toList()),
					 Integer.parseInt(space[3]), // int houseCost
					 Color.parseKind(space[0])); // color
					if(!colorGroups.containsKey(cp.color)) {
						colorGroups.put(cp.color, new ArrayList<>());
					}
					colorGroups.get(cp.color).add(cp);
					ownerMap.put(cp, BANKER);
					board.add(cp);
					continue;
				}
				case "Railroad": {
					board.add(new RailRoad(
					 i, // int location,
					 space[1], // String name 
					 SpaceKind.parseKind(space[0]),  //	SpaceKind kind,
					 Integer.parseInt(space[3]), // int purchasePrice
					 Integer.parseInt(space[5]), // int mortgagePrice
					 Arrays.stream(space[4].split(" ")).map(Integer::valueOf)
						   .collect(Collectors
							.toList()) // List<Integer> payouts,
					));
					continue;
				}
				case "Utility": {
					board.add(new Utility(
					 i, // int location,
					 space[1], // String name 
					 SpaceKind.parseKind(space[0]),  //	SpaceKind kind,
					 Integer.parseInt(space[3]), // int purchasePrice
					 Integer.parseInt(space[5]), // int mortgagePrice
					 Arrays.stream(space[4].split(" ")).map(Integer::valueOf)
						   .collect(Collectors
							.toList()) // List<Integer> payouts,
					));
					continue;
				}
				case "Chance": {
					board.add(new CardSpace(i, "Chance", chance, SpaceKind.Chance));
					continue;
				}
				case "Chest": {
					board.add(new CardSpace(i, "Chest", chest, SpaceKind.Chest));
					continue;
				}
				default:
					board.add(new BoardSpace(i, space[1], SpaceKind.parseKind(space[6])));
			}
		}
		for(BoardSpace boardSpace: board) {
			if(boardSpace instanceof Property property) {
				ownerMap.put(property, BANKER);
			}
		}
		return board;
	}
	
	private static boolean canBuy(Player player, Property property) {
		if(property.owner == player) {
			input(player + " already owns " + property);
			return false;
		}
		return ownerMap.get(property) == BANKER
			   && player.money >= property.purchasePrice;
	}
	
	private static void estateCheck(Player player, Estate estate) {
		if(!canBuy(player, estate)) {
			if(player != estate.owner) {
				int groupedSiblings = countGroupedSiblings(estate, estate.owner);
				int maxGroupSize = (int)board.stream()
										.filter(space -> space instanceof Estate && ((Estate)space).color == estate.color)
										.count() - 1;
				int rent = estate.getRent(groupedSiblings, maxGroupSize);
				player.money -= rent;
				estate.owner.money += rent;
				input("You had to pay " + estate.owner + " $" + rent + ".");
				checkForBroke(player, rent);
				return;
			}
		}
		if(inputBool("do you want to buy this property?")) {
			player.money -= estate.purchasePrice;
			ownerMap.put(estate, player);
		} else {
			ArrayList<Player> players = new ArrayList<>();
			for(Player p: PLAYERS) {
				if(p != player) {
					players.add(p);
				}
			}
			auction(players, estate);
		}
	}
	
	private static ArrayList<Player> getPlayers() {
		ArrayList<Player> players = new ArrayList<>();
		//int numPlayers = inputInt("How many players?");
		for(int i = 0; i < 3 /*numPlayers*/; i++) {
			//String name = input("What's player " + (i+1) + "'s name?");
			players.add(new Player(/*name*/"Player" + (i + 1), i));
		}
		return players;
	}
	
	private static void incomeTax(Player player) {
		println("you landed on income tax... do you want to pay" +
				" 10%, or $200?");
		if(inputBool("Which option do you want to take?", "10%", "$200")) {
			player.money -= player.money / 10;
		} else {
			player.money -= 200;
		}
	}
	
	static String input(String prompt) {
		println(prompt);
		return new Scanner(System.in).nextLine().trim();
	}
	
	static boolean inputBool(String s) {
		while(true) {
			int i = inputInt(s + "\n1 for yes, 0 for no");
			if(i != 1 && i != 0) {
				println("\"" + i + "\" is not a valid choice.");
			} else { return i == 1; }
		}
	}
	
	static boolean inputBool(String s, String yes, String no) {
		while(true) {
			int i = inputInt(s + "\n1 for " + yes + ", 0 for " + no);
			if(i != 1 && i != 0) {
				println("\"" + i + "\" is not a valid choice.");
			} else { return i == 1; }
		}
	}
	
	static int inputInt(String s) {
		int i;
		while(true) {
			String input = input(s);
			try {
				i = Integer.parseInt(input);
				return i;
			} catch(Exception e) { println("\"" + input + "\" is not valid."); }
		}
	}
	
	private static void jailCheck(Player player) {
	}
	
	// taxes
	private static void luxuryTax(Player player) {
		input("you landed on luxury tax. Pay $75");
		player.money -= 75;
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		PLAYERS = getPlayers();
		Deck chance = new Deck(CHANCE_STRING);
		Deck chest = new Deck(CHEST_STRING);
		board = boardInit(chance, chest);
		
		((Estate)board.get(6)).owner = PLAYERS.get(0);
		((Estate)board.get(8)).owner = PLAYERS.get(0);
		((Estate)board.get(9)).owner = PLAYERS.get(0);
		((Estate)board.get(6)).numHouses = 3;
		((Estate)board.get(8)).numHouses = 3;
		((Estate)board.get(9)).numHouses = 3;
		ownerMap.put((Estate)board.get(6), PLAYERS.get(0));
		ownerMap.put((Estate)board.get(8), PLAYERS.get(0));
		ownerMap.put((Estate)board.get(9), PLAYERS.get(0));
		for(int i = 1; PLAYERS.size() > 1; i++) {
			Player player = PLAYERS.get(i % PLAYERS.size());
			jailCheck(player);
			player.developmentCheck();
			input("it's player " + player + "'s turn.");
			int numRolls = 0;
			boolean doubles;
			while(numRolls < 3) {
				numRolls++;
				int[] roll = /*roll();*/ new int[] {1, 5};
				if(roll[0] == roll[1]) {
					doubles = true;
					System.out.println("doubles!");
					if(numRolls == 2) {
						System.out.println("You have to go to jail!");
						player.inJail = 0;
						player.location = 10;
						jailCheck(player);
						break;
					}
				} else { doubles = false; }
				player.location =
				 (roll[0] + roll[1] + board.size() + player.location) %
				 board.size();
				BoardSpace space = board.get(player.location);
				println("You landed on " + space.name + ".");
				if(player.location == GO_TO_JAIL) {
					System.out.println("You have to go to jail!");
					player.inJail = 0;
					player.location = 10;
					jailCheck(player);
					break;
				} else if(space instanceof RailRoad railRoad) {
					railRoadCheck(player, railRoad);
				} else if(space instanceof Utility utility) {
					utilityCheck(player, utility);
				} else if(space instanceof Estate estate) {
					estateCheck(player, estate);
				}
				else {
					switch(space.kind) {
						case Chance:
							chance.draw().check(player, PLAYERS, BANKER, board);
							break;
						case Chest:
							chest.draw().check(player, PLAYERS, BANKER, board);
							break;
						case IncomeTax:
							incomeTax(player);
							break;
						case LuxuryTax:
							luxuryTax(player);
							break;
						case Corner:
							switch(player.location) {
								case 0:
									System.out
									 .println("You're on GO, collect $200!!");
									break;
								case 10:
									System.out.println("Just visiting...");
									break;
								case 20:
									System.out.println("Free parking...");
									break;
							}
					}
				}
				if(!doubles) {
					break;
				}
				input("Roll again!");
			}
		}
	}
	
	// Helper methods to simplify code for printing/getting input
	public static List<Integer> parseIntArray(String s) {
		return Arrays.stream(s.split(" ")).map(Integer::valueOf)
					 .collect(Collectors.toList());
	}
	
	static void print(String s) { System.out.print(s); }
	
	static void println(String s) { print(s + "\n"); }
	
	// properties
	private static void railRoadCheck(Player player, RailRoad railRoad) {
		if(!canBuy(player, railRoad)) { 
			if(railRoad.owner != BANKER) {
				int groupedSiblings = countGroupedSiblings(railRoad, player);
				int rent = railRoad.getRent(groupedSiblings);
				player.money -= rent;
				railRoad.owner.money += rent;
				input("You had to pay " + railRoad.owner + " $" + rent + ".");
				checkForBroke(player, rent);
				return;
			}
		}
		if(inputBool(player + ", do you want to buy " + railRoad + "?")) {
			player.money -= railRoad.purchasePrice;
			ownerMap.put(railRoad, player);
			railRoad.owner = player;
			input(player + " just purchased " + railRoad + 
				  ".\nBalance remaining: $" + player.money);
			return;
		} else {
			ArrayList<Player> players = new ArrayList<>();
			for(Player p: PLAYERS) {
				if(p != player) {
					players.add(p);
				}
				auction(players, railRoad);
			}
		}
	}
	
	private static void checkForBroke(Player player, int rent) {
		while(player.money < rent) {
			List<Property> properties =
			 ownerMap
			  .keySet()
			  .stream()
			  .filter(e -> e.owner == player)
			  .collect(Collectors.toList());
			if(properties.size() == 0) {
				input(player + ", you cannot pay your rent. You're out of the game!");
				PLAYERS.remove(player);
				return;
			}
			String s = "";
			Iterator<Property> it = properties.iterator();
			for(int i = 0; i < properties.size(); i++) {
				s += (i + 1) + ": " + it.next() + "\n";
			}
			Integer selection = null;
			while(selection == null || selection < 0 || selection > properties.toArray().length) {
				selection = inputInt(
				 "You have $" + player.money + "but owe $" + rent +"." +
				 " what else do you want to sell?" +
				 " (select the number of the property to sell)\n" + s);
			}
			Property sellingProperty = properties.get(selection);
			if(inputBool("You are about to sell " + sellingProperty
						 + " for $" + sellingProperty.mortgagePrice
						 + ". Are you sure?")){
				sellingProperty.owner = BANKER;
				ownerMap.put(sellingProperty, BANKER);
				player.setBalance(sellingProperty.mortgagePrice);
			}
		}
	}
	
	public static int countGroupedSiblings(Property property, Player currentPlayer) {
		return (int)ownerMap.entrySet()
							.stream()
							.filter(entry -> entry.getValue() == currentPlayer
											 && entry.getKey().kind == property.kind
											 && entry.getKey() != property)
							.count();
	}
	
	// other
	public static int[] roll() {
		return new int[] {
		 new Random().nextInt(6) + 1,
		 new Random().nextInt(6) + 1,
		 };
	}
	
	public static Object tryParseInt(String s) {
		try { return Integer.parseInt(s); } catch(Exception ignore) {
			return 0;
		}
	}
	
	public static Object tryParseIntArray(String s) {
		try { return Main.parseIntArray(s); } catch(Exception ignore) {
			return null;
		}
	}
	
	private static void utilityCheck(Player player, Utility utility) {
		if(!canBuy(player, utility)) { 
			if(utility.owner != BANKER) {
				int groupedSiblings = countGroupedSiblings(utility, player);
				int rent = utility.getRent(groupedSiblings);
				player.money -= rent;
				utility.owner.money += rent;
				input("You had to pay " + utility.owner + " $" + rent + ".");
				checkForBroke(player, rent);
				return;
			}
		}
		if(inputBool(player + ", do you want to buy " + utility + "?")) {
			player.money -= utility.purchasePrice;
			ownerMap.put(utility, player);
			utility.owner = player;
			input(player + " just purchased " + utility + 
				  ".\nBalance remaining: $" + player.money);
			return;
		} else {
			ArrayList<Player> players = new ArrayList<>();
			for(Player p: PLAYERS) {
				if(p != player) {
					players.add(p);
				}
				auction(players, utility);
			}
		}
	}
}
