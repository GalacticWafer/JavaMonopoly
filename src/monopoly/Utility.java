package monopoly;

import java.util.List;

public class Utility extends Property {
	public Utility(
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
	
	public int getRent(int groupedSiblings) {
		int multiplier = payouts.get(groupedSiblings);
		Game.input("Roll and pay " + multiplier + " times the amount shown on the dice.");
		var roll =  Game.roll();
		Game.input("you rolled " + roll[0] + " & " + roll[1]);
		return (roll[0] + roll[1]) * multiplier;
	}
}
