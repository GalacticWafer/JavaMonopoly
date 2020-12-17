package monopoly;

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
	public static final int CHANCE_DECK = 0;
	public static final int CHEST_DECK = 1;
	int instructions;
	String text;
	int[] args;
	public Card(String text, int instructions, int[] args) {
		this.instructions = instructions;
		this.text = text;
		this.args = args;
	}
	
	public void check(Player currentPlayer, ArrayList<Player> players, ArrayList<BoardSpace> board) {
		int oldLocation = currentPlayer.location;
		System.out.println(currentPlayer.printBalance());
		Main.input(text);
		switch(instructions) {
			case UTILITY -> {
				while(Main.board.get(currentPlayer.location).kind !=
					  SpaceKind.Utility) {
					currentPlayer.location =
					 (currentPlayer.location + 1) % board.size();
				}
				Main.utilityCheck(currentPlayer, (Utility)Main.board
				 .get(currentPlayer.location));
				return;
			}
			case RAILROAD -> {
				while(Main.board.get(currentPlayer.location).kind !=
					  SpaceKind.Railroad) {
					currentPlayer.location =
					 (currentPlayer.location + 1) % board.size();
				}
				Main.railRoadCheck(currentPlayer, (RailRoad)Main.board
				 .get(currentPlayer.location));
				return;
			}
			case JAIL -> {
				currentPlayer.location = Main.JAIL;
				currentPlayer.inJail = 3;
				return;
			}
			case GO -> {
				currentPlayer.location = 0;
				currentPlayer.money += 200;
			}
			case REPAIR -> {
				ArrayList<ArrayList<Estate>> colorGroups = new ArrayList<>();
				for(Map.Entry<Property, Player> entry: Main.ownerMap
				 .entrySet()) {
					boolean foundGroup = false;
					if(entry.getKey() instanceof Estate estate) {
						if(estate.owner != currentPlayer) { continue; }
						for(ArrayList<Estate> colorGroup: colorGroups) {
							if(colorGroup.get(0).color == estate.color) {
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
				int payment =0;
				for(ArrayList<Estate> cList: colorGroups) {
					for(Estate estate: cList) {
						if(estate.hasHotel()) {
							payment += 100;
						} else {
							payment += 25 * estate.numHouses;
						}
					}
				}
				if(!(currentPlayer.canPayDebt(payment) >= payment)) {
					Main.removePlayer(currentPlayer);
				} else {
					currentPlayer.money -= payment;
				}
			}
			case PAY -> {
				int collectedDebt = currentPlayer.canPayDebt(args[0]);
				if(!(collectedDebt >= args[0])) {
					Main.removePlayer(currentPlayer);
				} else {
					currentPlayer.money -= args[0];
				}
			}
			case PAY_EACH -> {
				for(Player p: players) {
					if(currentPlayer != p) {
						int collectedDebt = currentPlayer.canPayDebt(args[0]);
						if(!(collectedDebt >= args[0])) {
							Main.removePlayer(currentPlayer);
							break;
						} else {
							p.money += args[0];
						}
						p.money += collectedDebt;
					}
				}
			}
			case COLLECT -> currentPlayer.money += args[0];
			case COLLECT_EACH -> {
				for(Player p: players) {
					if(currentPlayer != p) {
						int collectedDebt = p.canPayDebt(args[0]);
						if(!(collectedDebt >= args[0])) {
							Main.removePlayer(p);
							break;
						} else {
							p.money -= args[0];
						}
						currentPlayer.money += collectedDebt;
					}
				}
			}
			case ADVANCE -> {
				oldLocation = currentPlayer.location;
				currentPlayer.location = args[0];
				move(currentPlayer, board, oldLocation);
				return;
			}
			case JAIL_FREE -> {
				currentPlayer.jailCards.push(this);
				return;
			}
			case MOVE_X -> {
				args[0] = args[0] * (args[1] == 1 ? 1 : -1);
				move(currentPlayer, board, oldLocation);
			}
		}
		System.out.println(currentPlayer.printBalance());
	}
	
	private void move(Player currentPlayer, ArrayList<BoardSpace> board, int oldLocation) {
		int move;
		move = oldLocation > args[0] ? (board.size() - oldLocation + args[0]) : (args[0] - oldLocation);
		Main.movePlayer(currentPlayer, board.get(currentPlayer.location), oldLocation, move);
	}
}
