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
				scanner.nextLine();
				while(scanner.hasNextLine()){ 
					String[] inner_str = scanner.nextLine().split(",");
					unused.push(new Card(inner_str[0],Arrays.stream(inner_str[1].split(" "))
					 .map(Integer::valueOf).mapToInt(i->i).toArray()));                                          
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
