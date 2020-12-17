import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Game {
	public static final Player BANKER = new Player("Banker", -1);
	public static final String CHANCE_STRING = "ChanceCards.csv";
	public static final String CHEST_STRING = "CommunityChestCards.csv";
	public static final Integer JAIL = 10;
	public static ArrayList<Player> PLAYERS;
	public static final String SPACES_STRING = "board_spaces.csv";
	public static final int TOO_MANY_DOUBLES = 3;
	public static ArrayList<BoardSpace> board;
	private static Deck chance;
	private static Deck chest;
	public static HashMap<Color, ArrayList<Estate>> colorGroups;
	public static int hotels = 12;
	public static int houses = 30;
	public static HashMap<Integer, BoardSpace> locationMap;
	public static HashMap<Property, Player> ownerMap;
	private static int turnCounter;
	
	// Will refactor to get rid of these
	public static HashMap<Integer, Color> locationColorMap;
	public static HashMap<SpaceKind, ArrayList<BoardSpace>> spaceGroups;
	
	public Game() {
	}
	
	private void activateGame() throws FileNotFoundException {
		PLAYERS = getPlayers();
		chance = new Deck(CHANCE_STRING);
		chest = new Deck(CHEST_STRING);
		board = boardInit(chance, chest);
		/*
		// use these for testing what happens at particular types of BoardSpaces
		// Estate
		ownerMap.put((Property)board.get(24), PLAYERS.get(1));
		
		// Utility
		ownerMap.put((Property)board.get(12), PLAYERS.get(1));
		
		// RailRoad
		ownerMap.put((Property)board.get(15), PLAYERS.get(1));
		*/
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
				int[] roll = roll();
				/*
				// use this for testing, to make players always land on a specific BoardSpace
				int[] roll = {3, 4};
				*/
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
				int x = i % bidders.size();
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
	
	// initialization
	public static ArrayList<BoardSpace> boardInit(Deck chance, Deck chest)
	throws FileNotFoundException {
		locationMap = new HashMap<>();
		locationColorMap = new HashMap<>();
		spaceGroups = new HashMap<>();
		ownerMap = new HashMap<>();
		colorGroups = new HashMap<>();
		ArrayList<BoardSpace> board = new ArrayList<>(40);
		Scanner scanner = new Scanner(new File(Game.SPACES_STRING));
		for(int i = 0; scanner.hasNextLine(); i++) {
			String[] space = scanner.nextLine().split(",");
			SpaceKind kind = SpaceKind.parseKind(space[5]);
			switch(kind) {
				case Chance -> board.add(new CardSpace(i, "Chance", chance, SpaceKind.Chance));
				case Chest -> board.add(new CardSpace(i, "Chest", chest, SpaceKind.Chest));
				case Corner, IncomeTax, LuxuryTax -> board.add(new BoardSpace(space[0], kind));
				case Railroad -> board.add(new RailRoad(
				 i, // int location,
				 space[0], // String name 
				 kind,  //	SpaceKind kind,
				 Integer.parseInt(space[1]), // int purchasePrice
				 Integer.parseInt(space[4]), // int mortgagePrice
				 Arrays.stream(space[3].split(" ")).map(Integer::valueOf)
					   .collect(Collectors
						.toList()) // List<Integer> payouts,
				));
				case Utility -> board.add(new Utility(
				 i, // int location,
				 space[0], // String name 
				 kind,  //	SpaceKind kind,
				 Integer.parseInt(space[2]), // int purchasePrice
				 Integer.parseInt(space[4]), // int mortgagePrice
				 Arrays.stream(space[3].split(" ")).map(Integer::valueOf)
					   .collect(Collectors
						.toList()) // List<Integer> payouts,
				));
				default -> {
					Estate es = new Estate(
					 i, // location
					 space[0],    // String name    	          
					 kind, // SpaceKind kind
					 Integer.parseInt(space[1]),  // int purchasePrice
					 Integer.parseInt(space[4]),   // int mortgagePrice
					 Arrays.stream(space[3].split(" "))  // List<Integer> payouts     	        
						   .map(Integer::valueOf).collect(Collectors.toList()),
					 Integer.parseInt(space[2]), // int houseCost
					 Color.parseKind(space[5])); // color
					if(!colorGroups.containsKey(es.color)) {
						colorGroups.put(es.color, new ArrayList<>());
					}
					colorGroups.get(es.color).add(es);
					ownerMap.put(es, BANKER);
					board.add(es);
				}
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
				}
			}
		}
		return board;
	}
	
	private static void buyOrAuction(Player player, Property property) {
		if(inputBool(
		 player.printBalance() + ".\n" + property.printCost() + ".\n" + player + ", do you want to buy " +
		 property + "?")) {
			player.money -= property.purchasePrice;
			ownerMap.put(property, player);
			input(player + " now owns " + property + ".\n" + player.printBalance());
		} else {
			auction(PLAYERS.stream().filter(x -> x != player).collect(Collectors.toList()), property);
		}
	}
	
	private static boolean canBuy(Player player, Property property) {
		if(ownerMap.get(property) == player) {
			input(player + " already owns " + property + ".");
			return false;
		}
		return ownerMap.get(property) == BANKER && player.money >= property.purchasePrice;
	}
	
	public static void debtCheck(Player currentPlayer, int payment, int i) {
		if(!(i >= payment)) {
			Game.removePlayer(currentPlayer);
		} else {
			currentPlayer.money -= payment;
		}
	}
	
	private static ArrayList<Player> getPlayers() {
		ArrayList<Player> players = new ArrayList<>();
/*		int numPlayers = inputInt("WELCOME TO MONOPOLY!\n\nHow many players (2 - 5)?");
		while(numPlayers < 2 || numPlayers > 5) {
			numPlayers = inputInt("This game is for 2 - 5 players.\nPick a reasonable number of players to start the game.\nHow many players (2 - 5)?");
		}
		for(int i = 0; i <  numPlayers; i++) {
			String name = input("What's player " + (i + 1) + "'s name?");
			players.add(new Player(name *//*"Player" + (i + 1)*//*, i));
		}*/
		for(int i = 0; i < 3; i++) {
			players.add(new Player("Player" + (i + 1), i));
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
	
	static int inputOption(String prompt, String[] options) {
		prompt += "\npick from the following options:\n";
		for(int i = 0; i < options.length; i++) {
			prompt += "[" + (i + 1) + "] " + options[i] + "\n";
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
	
	static int inputOption(String prompt, Iterator<Object> options, int length) {
		String[] array = new String[length];
		for(int i = 0; options.hasNext(); i++) {
			array[i] = options.next().toString();
		}
		return inputOption(prompt, array);
	}
	
	private static boolean jailCheck(Player player) {
		String s = "";
		if(player.inJail > -1) {
			println("\n\nIt's " + player + "'s turn.");
			if(player.jailCards.size() > 0 && inputBool("Do you want to use a get out of jail free card?")) {
				Card jailCard = player.jailCards.pop();
				if(jailCard.args[0] == Card.CHANCE_DECK) {
					chance.used.push(jailCard);
				} else {
					chest.used.push(jailCard);
				}
				player.inJail = -1;
				println(player + " is out of jail!");
				return false;
			} else if(inputBool("Does " + player + " want to try to roll doubles to get out of jail?")) {
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
	
	private static void luxuryTax(Player player) {
		input("you landed on luxury tax. Pay $75");
		player.money -= 75;
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		new Game().activateGame();
	}
	
	public static boolean movePlayer(Player player, BoardSpace space,
									 int oldLocation, int move) {
		if(oldLocation + move > board.size()) {
			player.money += 200;
			System.out.println(player + " passed go, and now has $" + player.money);
		}
		if(space instanceof Property property) {
			propertyCheck(player, property);
		} else {
			switch(space.kind) {
				case Chance -> chance.draw().check(player, PLAYERS, board);
				case Chest -> chest.draw().check(player, PLAYERS, board);
				case IncomeTax -> incomeTax(player);
				case LuxuryTax -> luxuryTax(player);
				case Corner -> {
					switch(player.location) {
						case 0 -> System.out.println("You're on GO, collect $200!!");
						case 10 -> println("Just visiting...");
						case 20 -> println("Free parking...");
						case 30 -> {
							println("You have to go to jail!");
							player.inJail = 0;
							player.location = 10;
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	public static boolean ownsAll(Estate estate, Player player) {
		var group = colorGroups.get(estate.color);
		for(BoardSpace boardSpace: group) {
			if(ownerMap.get(boardSpace) != player) {
				return false;
			}
		}
		return true;
	}
	
	static void print(String s) { System.out.print(s); }
	
	static void println(String s) { print(s + "\n"); }
	
	static void propertyCheck(Player player, Property property) {
		if(!canBuy(player, property)) {
			Player otherPlayer = ownerMap.get(property);
			if(otherPlayer != BANKER) {
				int groupedSiblings = (int)ownerMap.keySet().stream().filter(e -> e instanceof RailRoad rr && otherPlayer == ownerMap.get(rr)).count() - 1;
				int rent;
				if(property instanceof RailRoad railRoad) {
					rent = railRoad.getRent(groupedSiblings);
				} else if(property instanceof Utility utility) {
					rent = utility.getRent(groupedSiblings);
				} else if(property instanceof Estate estate) {
					groupedSiblings = (int)ownerMap.keySet().stream().filter(e -> e.kind == estate.kind && ownerMap.get(e) == ownerMap.get(estate)).count();
					int maxGroupSize = estate.kind.count();
					rent = estate.getRent(groupedSiblings, maxGroupSize);
				} else { throw new InputMismatchException(); }
				System.out.println(player + " has to pay " + otherPlayer + " $" + rent + ".");
				player.payPlayer(otherPlayer, rent);
			}
		} else {
			buyOrAuction(player, property);
		}
	}
	
	public static void removePlayer(Player player) {
		PLAYERS.remove(player);
		turnCounter = (turnCounter - 1 + PLAYERS.size()) % PLAYERS.size();
	}
	
	public static int[] roll() {
		return new int[] {
		 new Random().nextInt(6) + 1,
		 new Random().nextInt(6) + 1,
		 };
	}
}
