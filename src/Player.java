import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
public class Player {
	public String name;
	public Integer i;
	public int inJail = -1;
	public Stack<Card> jailCards;
	int location;
	int money;
	private HashMap<SpaceKind, ArrayList<Property>> propertyGroups;
	
	// Constructor
	public Player(String _s, Integer _i) {
		name = _s;
		i = _i;
		propertyGroups = new HashMap<>();
		location = 0;
		money = 1500;
		jailCards = new Stack<>();
	}
	
	public void developmentCheck() {
		for(Map.Entry<Color, ArrayList<BoardSpace>> colorArrayListEntry:
		 Main.colorGroups.entrySet()) {
			if(owsAll(propertyGroups.get(kind).size() ==))
		}
	}
	
	// Property management
	public boolean owsAll(SpaceKind kind) {
		return propertyGroups.get(kind).size() == Main.groupCounts.get(kind);
	}
	public ArrayList<Estate> canModify(boolean developing) {
		ArrayList<Estate> options = new ArrayList<>();
		for(ArrayList<Property> group: propertyGroups.values()) {
			if(owsAll(group.get(0).kind) && group.get(0) instanceof Estate) {
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
	
	public void printBalance() {
		Main.input(name + " has $" + money);
	}
	
	public void setBalance(int i) {
		money += i;
	}
	
	@Override public String toString() {
		return name;
	}
}
