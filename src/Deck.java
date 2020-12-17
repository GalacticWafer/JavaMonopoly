import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

public class Deck  {
			BoardSpace boardSpace;
			Stack<Card> used;
			Stack<Card> unused;
			
			public Deck(String fileName) throws FileNotFoundException {
				unused = new Stack<>();
				used = new Stack<>();
				Scanner scanner = new Scanner(new File(fileName));
				while(scanner.hasNextLine()){
					String[] array = scanner.nextLine().split(",");
					int[] s;
					if(array.length == 3) {
						s = Arrays.stream(array[2].split(" "))
								  .map(Integer::valueOf).mapToInt(i -> i)
								  .toArray();
					} else {
						s = new int[0];
					}
					unused.push(new Card(array[0], Integer.parseInt(array[1]), s));                                          
				}
			} 
			public Card draw(){
				if(unused.isEmpty()) {
					var temp = unused;
					unused = used;
					used = temp;
				}
				int rand = /*new Random().nextInt(unused.size())*/0;
				Card card = unused.get(rand);
				used.push(card);
				unused.remove(rand);
				return card;
			}
}
