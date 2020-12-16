import java.util.ArrayList;
import java.util.Map;

public class Card {
	private static final int ADVANCE = 10;
	private static final int COLLECT = 8;
	private static final int COLLECT_EACH = 9;
	private static final int GO = 4;
	private static final int JAIL = 3;
	private static final int JAIL_FREE = 11;
	private static final int MOVE_X = 12;
	private static final int PAY = 6;
	private static final int PAY_EACH = 7;
	private static final int RAILROAD = 2;
	private static final int REPAIR = 5;
	private static final int UTILITY = 1;
	int[] instructions;
	String text;
	
	public Card(String text, int[] instructions) {
		this.instructions = instructions;
		this.text = text;
	}
	
	public void check(Player currentPlayer, ArrayList<Player> players,
					  Player banker,
					  ArrayList<BoardSpace> board) {
		Main.input(text + "\n" + instructions[0]);
		switch(instructions[0]) {
			case UTILITY:
			case RAILROAD:
			case JAIL:
			case GO:
			case REPAIR:
				
				/*List<BoardSpace> colorGroups
				 = Main.ownerMap.entrySet()
								.stream()
								.filter(entry -> 
								 	entry.getValue() == currentPlayer
								 	&& entry instanceof Estate)
								.map(Map.Entry::getKey)
								.collect(Collectors.groupingBy(Estate::getColor));*/
				
				
				ArrayList<ArrayList<Estate>> colorGroups = new ArrayList<>();
				for(Map.Entry<Property, Player> entry: Main.ownerMap.entrySet()) {
					boolean foundGroup = false;
					if(entry.getKey() instanceof Estate estate) {
						if(estate.owner != currentPlayer) { continue; }
						for(ArrayList<Estate> colorGroup: colorGroups) {
							if(colorGroup.get(0).color == estate.color){
								colorGroup.add(estate);
								foundGroup = true;
							}
						}
						if(!foundGroup) {
							ArrayList<Estate> colorGroup = new ArrayList<>();
							colorGroup.add(estate);
							colorGroups.add(colorGroup);
						}
					}
				}
				for(ArrayList<Estate> cList: colorGroups) {
					Color color = cList.get(0).color;
					int payment = 
					 cList.stream()
						  .filter(thing -> thing.getColor() == color)
						  .mapToInt(Estate::getNumHouses)
						  .sum() * 25 
					 + cList.stream()
							.filter(thing -> thing.getColor() == color)
							.mapToInt(Estate::hotelCount)
							.sum() * 100;
					currentPlayer.setBalance(-payment);
					if(currentPlayer.money < 0) {
						System.out.println("you're broke! You better start selling things...");
					}
				}
			
			case PAY:
				currentPlayer.money -= instructions[1];
			case PAY_EACH:
				for(Player p: players) {
					if(currentPlayer != p) {
						p.money += instructions[1];
						currentPlayer.money -= instructions[1];
					}
				}
			case COLLECT:
				currentPlayer.money += instructions[1];
			case COLLECT_EACH:
				for(Player p: players) {
					if(currentPlayer != p) {
						p.money -= instructions[1];
						currentPlayer.money += instructions[1];
					}
				}
			case ADVANCE:
				currentPlayer.location = instructions[1];
			case JAIL_FREE:
				currentPlayer.jailCards.push(this);
			case MOVE_X:
				currentPlayer.location =
				 (currentPlayer.location + board.size() + instructions[1]) %
				 board.size();
		}
	}
}


/*
--chance--
Bank pays you dividend of $50,8 50, 
Pay poor tax of $15,6 15, 
Your building and loan matures—Collect $150,8 150, 
You have won a crossword competition—Collect $100,8 100, 
Advance to Illinois Ave—If you pass Go collect $200,10 24, 
Advance to St. Charles Place – If you pass Go collect $200,10 11, 
Take a trip to Reading Railroad–If you pass Go collect $200,10 5, 
Take a walk on the Boardwalk–Advance token to Boardwalk,10 39, 
Advance to Go (Collect $200),4, 
Make general repairs on all your property–For each house pay $25–For each 
hotel $100,5 25 100, 
Get Out of Jail Free,11, 
Go to Jail–Go directly to Jail–Do not pass Go. do not collect $200,3, 
Advance token to nearest Utility. If unowned you may buy it from the Bank. If
 owned throw dice and pay owner a total ten times the amount thrown.,1 10, 
You have been elected Chairman of the Board–Pay each player $50,7 50, 
Go Back 3 Spaces,12,
Advance token to the nearest Railroad and pay owner twice the rental to which
 he/she {he} is otherwise entitled. If Railroad is unowned you may buy it 
 from the Bank.,2 2,
--chest--
Bank pays you dividend of $50,6 50,
Doctor's fee—Pay $50,6 -50,
From sale of stock you get $50,8 50,
Pay hospital fees of $100,6 -100,
Holiday Fund matures—Receive $100,8 100
It is your birthday—Collect $10,8 10,
Life insurance matures–Collect $100,8 100,
Pay school fees of $150,6 -150,
Receive $25 consultancy fee,8 25,
Income tax refund–Collect $20,8 20,
You have won second prize in a beauty contest–Collect $10,8 10,
You inherit $100,8 100,
Advance to Go (Collect $200),4, 
You are assessed for street repairs–$40 per house–$115 per hotel,5 40 115, 
Get Out of Jail Free,11,
Go to Jail–Go directly to Jail–Do not pass Go. do not collect $200,3, 
Grand Opera Night—Collect $50 from every player for opening night seats,9 50,
*/
	
