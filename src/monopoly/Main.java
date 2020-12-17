package monopoly;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
	public Main(){
	}
	public static final Player BANKER = new Player("Banker", -1);
	public static final String CHANCE_STRING = "ChanceCards.csv";
	public static final String CHEST_STRING = "CommunityChestCards.csv";
	private static final int GO_TO_JAIL = 30;
	public static final Integer JAIL = 10;
	public static ArrayList<Player> PLAYERS;
	public static final String SPACES_STRING = "board_spaces.csv";
	public static final int TOO_MANY_DOUBLES = 3;
	public static ArrayList<BoardSpace> board;
	private static Deck chance;
	private static Deck chest;
	public static HashMap<Color, ArrayList<Estate>> colorGroups;
	public static HashMap<Integer, Color> locationColorMap;
	public static HashMap<SpaceKind, ArrayList<BoardSpace>> spaceGroups;
	public static int hotels = 12;
	public static int houses = 30;
	public static HashMap<Integer, BoardSpace> locationMap;
	public static HashMap<Property, Player> ownerMap;
	private static int turnCounter;
	
	private static void auction(List<Player> bidders, Property property) {
		println("\n\nAUCTION TIME! The bank is selling " + property + "\n\n");
		Player player = bidders.get(0);
		int highestBid = property.purchasePrice;
		int i = 0;
		int originalSize = bidders.size();
		for(; bidders.size() > 1; i++) {
			player = bidders.get(i % bidders.size());
			boolean quit = false;
			if(player.money >= highestBid &&
			   inputBool("Does " + player + " want to continue to bid?\nhighest bid: " + highestBid + "\nBalance: " +
						 player.money + "?")) { 
				int bid;
				while(true) {
					bid = inputInt("How much do you want to pay?");
					if(bid > highestBid) {
						highestBid = bid;
						break;
					} else {
						if(inputBool(bid + " is not higher than " + highestBid + ". Quit bidding?")) {
							quit = true;
							break;
						}
					}
				}
				if(quit) {
					int x = i % bidders.size();
					bidders.remove(x);
					i--; 
				}
			} else { 
				int x = i%bidders.size();
				bidders.remove(x);
				i--; 
			}
		}
		if(i >= originalSize || player.money >= highestBid && inputBool("All other bidders have been eliminated." + player + ", do you want to buy " + property + " for $" + highestBid + "?")) { 
			input(player + " has won the auction for " + property + " at $" + highestBid + "!");
			player.money -= highestBid;
			ownerMap.put(property, player);
		}
	}
	
	public static boolean ownsAll(Estate estate, Player player){
		var group = colorGroups.get(estate.color);
		for(BoardSpace boardSpace: group) {
			if(ownerMap.get(boardSpace) != player) {
				return false;
			}
		}
		return true;
	}
	
	// initialization
	public static ArrayList<BoardSpace> boardInit(Deck chance, Deck chest)
	throws FileNotFoundException {
		locationMap = new HashMap<>();
		locationColorMap = new HashMap<>();
		spaceGroups = new HashMap<>();
		ownerMap = new HashMap<>();
		colorGroups = new HashMap<>();
		ArrayList<BoardSpace> board = new ArrayList<>(40);
		Scanner scanner = new Scanner(new File(Main.SPACES_STRING));
		for(int i = 0; scanner.hasNextLine(); i++) {
			String[] space = scanner.nextLine().split(",");
			switch(space[6]) {
				case "monopoly.Color" -> {
					Estate cp = new Estate(
					 i, // location
					 space[1],    // String name    	          
					 SpaceKind.Color, // monopoly.SpaceKind kind
					 Integer.parseInt(space[2]),  // int purchasePrice
					 Integer.parseInt(space[5]),   // int mortgagePrice
					 Arrays.stream(space[4].split(" "))  // List<Integer> payouts     	        
						   .map(Integer::valueOf).collect(Collectors.toList()),
					 Integer.parseInt(space[3]), // int houseCost
					 Color.parseKind(space[0])); // color
					if(!colorGroups.containsKey(cp.color)) {
						colorGroups.put(cp.color, new ArrayList<>());
					}
					colorGroups.get(cp.color).add(cp);
					ownerMap.put(cp, BANKER);
					board.add(cp);
				}
				case "Railroad" -> board.add(new RailRoad(
				 i, // int location,
				 space[1], // String name 
				 SpaceKind.parseKind(space[0]),  //	monopoly.SpaceKind kind,
				 Integer.parseInt(space[2]), // int purchasePrice
				 Integer.parseInt(space[5]), // int mortgagePrice
				 Arrays.stream(space[4].split(" ")).map(Integer::valueOf)
					   .collect(Collectors
						.toList()) // List<Integer> payouts,
				));
				case "Utility" -> board.add(new Utility(
				 i, // int location,
				 space[1], // String name 
				 SpaceKind.parseKind(space[0]),  //	monopoly.SpaceKind kind,
				 Integer.parseInt(space[3]), // int purchasePrice
				 Integer.parseInt(space[5]), // int mortgagePrice
				 Arrays.stream(space[4].split(" ")).map(Integer::valueOf)
					   .collect(Collectors
						.toList()) // List<Integer> payouts,
				));
				case "Chance" -> board
				 .add(new CardSpace(i, "Chance", chance,
				  SpaceKind.Chance));
				case "Chest" -> board.add(new CardSpace(i, "Chest", chest, SpaceKind.Chest));default -> board.add(new BoardSpace(i, space[1], SpaceKind.parseKind(space[6])));
			}
		}
		Iterator<BoardSpace> it = board.iterator();
		for(int i = 0; i < board.size(); i++) {
			BoardSpace boardSpace = it.next();
			if(!spaceGroups.containsKey(boardSpace.kind)) {
						spaceGroups.put(boardSpace.kind, new ArrayList<>());
			}
			spaceGroups.get(boardSpace.kind).add(boardSpace);
			if(boardSpace instanceof Property property) {
				ownerMap.put(property, BANKER);
				if(boardSpace instanceof Estate estate) {
					locationColorMap.put(i, estate.color);
					//DUPLICATE, already happens when parsing spaces 
					//if(!colorGroups.containsKey(estate.color)) {
					//	colorGroups.put(estate.color, new ArrayList<>());
					//}
					//colorGroups.get(estate.color).add(estate);
				}
			}
		}
		return board;
	}
	
	private static boolean canBuy(Player player, Property property) {
		if(property.owner == player) {
			input(player + " already owns " + property + ".");
			return false;
		}
		return property.owner == BANKER && player.money >= property.purchasePrice;
	}
	
	private static void estateCheck(Player player, Estate estate) {
		if(!canBuy(player, estate)) {
			if(player == estate.owner) { return; }
			if(estate.owner != BANKER) {
				int groupedSiblings = (int)ownerMap.keySet().stream().filter(e -> e instanceof Estate es && es.owner == estate.owner).count();
				int maxGroupSize = estate.color.count();
				int rent = estate.getRent(groupedSiblings, maxGroupSize);
				if(!(player.canPayDebt(rent) >= rent)) {
					Main.removePlayer(player);
				} else {
					player.money -= rent;
					estate.owner.money += rent;
				}
				input(player + " has to pay " + estate.owner + " $" + rent + ".");
				checkForBroke(player, rent);
				return;
			}
		} else if(inputBool(player.printBalance() + ".\n" + estate.printCost() +  ".\n" + player + ", do you want to buy " + estate + "?")) {
			player.money -= estate.purchasePrice;
			ownerMap.put(estate, player);
			input(player + " now owns " + estate + ".\n" + player.printBalance());
		} else {
			auction(PLAYERS.stream().filter(x -> x != player).collect(Collectors.toList()),estate);
		}
	}
	
	private static ArrayList<Player> getPlayers() {
		ArrayList<Player> players = new ArrayList<>();
		//int numPlayers = inputInt("How many players?");
		for(int i = 0; i < 30 /*numPlayers*/; i++) {
			//String name = input("What's player " + (i+1) + "'s name?");
			players.add(new Player(/*name*/"monopoly.Player" + (i + 1), i));
		}
		return players;
	}
	
	private static void incomeTax(Player player) {
		println("you landed on income tax... " + player.printBalance() + ". Do you want to pay 10% ($" + (player.money / 10) + "), or $200?");
		if(inputBool("Which option do you want to take?", "10% ($" + (player.money / 10) + ")", "$200")) {
			player.money -= player.money / 10;
		} else {
			player.money -= 200;
		}
		println(player.printBalance());
	}
	
	static String input(String prompt) {
		println(prompt);
		return new Scanner(System.in).nextLine().trim();
	}
	
	static boolean inputBool(String s) {
		while(true) {
			int i = inputInt(s + "\t(1 for yes, 2 for no)");
			if(i != 1 && i != 2) {
				println("\"" + i + "\" is not a valid choice.");
			} else { return i == 1; }
		}
	}
	
	static int inputOption(String prompt, String[] options){
		prompt  += "\npick from the following options:\n";
		for(int i = 0; i < options.length; i++) {
			prompt += "[" + (i + 1) + "] " +  options[i] + "\n";
		}
		int i = -1;
		do {
			String input = input(prompt);
			try {
				i = Integer.parseInt(input);
			} catch(Exception e) { println("\"" + input + "\" is not valid."); }
			if(i < 1 || i > options.length) {
				input(i + " is not a valid option.");
			}
		} while(i < 1 || i > options.length);
		return i - 1;
	}
	
	static int inputOption(String prompt, Iterator<Object> options, int length){
		String[] array = new String[length];
		for(int i = 0; options.hasNext(); i++) {
			array[i] = options.next().toString();
		}
		return inputOption(prompt, array);
	}
	
	static boolean inputBool(String s, String yes, String no) {
		while(true) {
			int i = inputInt(s + "\t(1 for " + yes + ", 2 for " + no + ")");
			if(i != 1 && i != 2) {
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
	
	private static boolean jailCheck(Player player) {
		String s = "";
		if(player.inJail > -1) {
			println("\n\nIt's " + player + "'s turn.");
			if(player.jailCards.size() > 0 && inputBool("Do you want to use a get out of jail free card?")){
				Card jailCard = player.jailCards.pop();
				if(jailCard.args[0] == Card.CHANCE_DECK) {
					chance.used.push(jailCard);
				} else {
					chest.used.push(jailCard);
				}
				player.inJail = -1;
				println(player + " is out of jail!");
				return false;
			} else if (inputBool("Does " + player + " want to try to roll doubles to get out of jail?")) {
				int[] roll = roll();
				if(roll[0] == roll[1]) {
					input(player + " rolled double " + roll[0] + "'s and got out of jail!");
					player.inJail = -1;	
					//return true;
				} else {
					s += player + " rolled " + roll[0] + " and " + roll[1];
				}
			} else if(player.money >= 50 && inputBool("Do you want to pay $50 to get out of jail?")) {
				player.money -= 50;
				player.inJail = -1;	
				//return true;
			}
			input(s + "\n" + player + " is still in jail for this round.");
			player.inJail--;
			return false;
		}
		return true;
	}
	
	// taxes
	private static void luxuryTax(Player player) {
		input("you landed on luxury tax. Pay $75");
		player.money -= 75;
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		PLAYERS = getPlayers();
		chance = new Deck(CHANCE_STRING);
		chest = new Deck(CHEST_STRING);
		board = boardInit(chance, chest);
		
		turnCounter = 0;
		for(; PLAYERS.size() > 1; turnCounter++) {
			Player player = PLAYERS.get(turnCounter % PLAYERS.size());
			if(!jailCheck(player)) {
				continue;
			}
			input("\nIt's " + player + "'s turn.");
			player.developmentCheck();
			int numRolls = 0;
			boolean doubles;
			while(numRolls < 4) {
				numRolls++;
				//int[] roll = roll();
				int[] roll = {3,4};
				if(roll[0] == roll[1]) {
					doubles = true;
					println("doubles!");
					if(numRolls == TOO_MANY_DOUBLES) {
						println("Three doubles in a row, " + player + " has to go to jail!");
						player.inJail = 3;
						player.location = 10;
						break;
					}
				} else { doubles = false; }
				player.location = (roll[0] + roll[1] + board.size() + player.location) % board.size();
				int oldLocation = player.location;
				BoardSpace space = board.get(player.location);
				println(player + " rolled " + roll[0] + " & " + roll[1] + ", and landed on " + space.name + ".");
				boolean nowInJail = movePlayer(player, space, oldLocation, roll[0] + roll[1]);
				if(nowInJail) { break; }
				if(!doubles) {
					break;
				}
				input("Roll again!");
			}
		}
	}
	
	public static boolean movePlayer(Player player, BoardSpace space,
									 int oldLocation, int move) {
		if(oldLocation + move > board.size()) {
			player.money += 200;
			System.out.println(player + " passed go, and now has $" + player.money);
		}
		if(player.location == GO_TO_JAIL) {
			println("You have to go to jail!");
			player.inJail = 0;
			player.location = 10;
			return true;
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
					chance.draw().check(player, PLAYERS, board);
					break;
				case Chest:
					chest.draw().check(player, PLAYERS, board);
					break;
				case IncomeTax:
					incomeTax(player);
					break;
				case LuxuryTax:
					luxuryTax(player);
					break;
				case Corner: {
					switch(player.location) {
						case 0:
							System.out
							 .println("You're on GO, collect $200!!");
							break;
						case 10:
							println("Just visiting...");
							break;
						case 20:
							println("Free parking...");
							break;
					}
				}
			}
		}
		return false;
	}
	
	static void print(String s) { System.out.print(s); }
	
	static void println(String s) { print(s + "\n"); }
	
	// properties
	static void railRoadCheck(Player player, RailRoad railRoad) {
		boolean b = canBuy(player, railRoad);
		if(!b) { 
			if(railRoad.owner != BANKER) {
				int groupedSiblings = (int)ownerMap.keySet().stream().filter(e -> e instanceof RailRoad rr && railRoad.owner == rr.owner).count() - 1;
				int rent = railRoad.getRent(groupedSiblings);
				player.money -= rent;
				railRoad.owner.money += rent;
				input("You had to pay " + railRoad.owner + " $" + rent + ".");
				checkForBroke(player, rent);
				return;
			}
		} 
		/*if(railRoad.owner != BANKER) {
			
		}*/
		if(inputBool(player.printBalance() + ".\n" + railRoad.printCost() +  ".\n" + player + ", do you want to buy " + railRoad + "?")) {
			player.money -= railRoad.purchasePrice;
			ownerMap.put(railRoad, player);
			railRoad.owner = player;
			input(player + " now owns " + railRoad + ".\n" + player.printBalance());
		} else {
			auction(PLAYERS.stream().filter(x -> x != player).collect(Collectors.toList()), railRoad);
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
		if(property instanceof RailRoad) {
			return (int) ownerMap.entrySet().stream().filter(e -> e.getKey() instanceof RailRoad rr && rr.owner == currentPlayer).count();
		}
		if(property instanceof Utility) {
			return (int) ownerMap.entrySet().stream().filter(e -> e.getKey() instanceof Utility ut && ut.owner == currentPlayer).count();
		}
		if(property instanceof Estate estate) {
			return (int) ownerMap.entrySet().stream().filter(e -> e.getKey() instanceof Estate es && es.owner == currentPlayer && es.color == estate.color).count();
		}
		throw new InputMismatchException();
	}
	
	public static void removePlayer(Player player) {
		PLAYERS.remove(player);
		turnCounter = (turnCounter - 1 + PLAYERS.size()) % PLAYERS.size();
	}
	
	// other
	public static int[] roll() {
		return new int[] {
		 new Random().nextInt(6) + 1,
		 new Random().nextInt(6) + 1,
		 };
	}
	
	static void utilityCheck(Player player, Utility utility) {
		if(!canBuy(player, utility)) { 
			if(utility.owner != BANKER) {
				int groupedSiblings = (int)ownerMap.keySet().stream().filter(e -> e instanceof Utility ut && ut.owner == utility.owner).count() - 1;
				int rent = utility.getRent(groupedSiblings);
				player.money -= rent;
				utility.owner.money += rent;
				input("You had to pay " + utility.owner + " $" + rent + ".");
				checkForBroke(player, rent);
				return;
			}
		}
		if(inputBool(player.printBalance() + ".\n" + utility.printCost() +  ".\n"+ player + ", do you want to buy " + utility + "?")) {
			player.money -= utility.purchasePrice;
			ownerMap.put(utility, player);
			utility.owner = player;
			input(player + " now owns " + utility + ".\n" + player.printBalance());
		} else {
			auction(PLAYERS.stream().filter(x -> x != player).collect(Collectors.toList()),utility);
		}
	}
}
