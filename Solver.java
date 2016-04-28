import java.io.*;
import java.util.*;

public class Solver {
	private static Block[][] grid;
	private static int maxi;
	private static int maxj;
	private static Block[] blocks;
	private static Block[] goal;
	private static HashSet<Configuration> seen;
	private static BlocksComparator comparator;
	private static Stack<Move> solution;
	
	private static Stack<Move> history;
	private static Stack<Move> moves;
	
	private static final int UP = 1;
	private static final int DOWN = -1;
	private static final int LEFT = 2;
	private static final int RIGHT = -2;
	private static final boolean PRINT = true;
	
	static class Block implements Comparable<Block> {
		int id;
		int i, j; // coords of top left corner
		int i2, j2; // i2 = i + len; j2 = j + width
		public String toString() {
			//return String.format("%d: (%d,%d at %d,%d -> %d,%d)", id, len, width, i, j, i2, j2);
			return String.format("%d: (%d %d %d %d)", id, i, j, i2, j2);
			//return "" + id;
		}
		public Block clone() {
			Block b = new Block();
			b.id = id; b.i = i; b.j = j; b.i2 = i2; b.j2 = j2;
			return b;
		}
		public int compareTo(Block b) {
			if(i == b.i)
				if(j == b.j)
					if(i2 == b.i2)
						if(j2 == b.j2)
							return 0;
						else
							return j2 - b.j2;
					else
						return i2 - b.i2;
				else
					return j - b.j;
			else
				return i - b.i;
		}
		public boolean equals(Block b) {
			if(i == b.i)
				if(j == b.j)
					if(i2 == b.i2)
						if(j2 == b.j2)
							return true;
			return false;
		}
	}
	
	static class Configuration {
		int hash;
		Block[] blocks;
		
		public Configuration() {
			hash = 0;
			int k = 1;
			
			for(int x = 0; x < maxi; x++) {
				for(int y = 0; y < maxj; y++) {
					if(grid[x][y] == null) {
						hash = hash ^ k;
					}
					k = k << 1;
					if(k == 0) ++k;
				}
			}
			
			blocks = new Block[Solver.blocks.length];
			for(int x = 0; x < blocks.length; x++) {
				blocks[x] = Solver.blocks[x].clone();
			}
		}

		public int hashCode() {
			return hash;
		}
		
		public boolean equals(Object c) {
			for(int x = 0; x < blocks.length; x++) {
				if(!blocks[x].equals(((Configuration) c).blocks[x])) {
					return false;
				}
			}
			return true;
		}
	}
	
	class BlocksComparator implements Comparator<Block> {
		public int compare(Block b, Block c) {
			if(b.i == c.i)
				if(b.j == c.j)
					if(b.i2 == c.i2)
						if(b.j2 == c.j2)
							return 0;
						else
							return b.j2 - c.j2;
					else
						return b.i2 - c.i2;
				else
					return b.j - c.j;
			else
				return b.i - c.i;
		}
	}
	
	static class Move {
		Block block;
		int direction;
		public String toString() {
			String s = "";
			switch(direction) {
			case UP:
				s = "up";
				break;
			case DOWN:
				s = "down";
				break;
			case LEFT:
				s = "left";
				break;
			case RIGHT:
				s = "right";
				break;
			}
			return block.toString() + " " + s;
		}
		public Move clone() {
			Move m = new Move(); m.block = block; m.direction = direction; return m;
		}
		public Move deepClone() {
			Move m = new Move(); m.block = block.clone(); m.direction = direction; return m;
		}
	}
	
	private boolean solve(Move lastMove) {
		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Arrays.sort(blocks, comparator);
		Configuration config = new Configuration();
		
		print("---");
		show();
		
		if(!seen.add(config)) {
			print("seen");
			return false;
		}
		

		if(check()) {
			print("FOUND!!");
			return true;
		}
		
		if(lastMove != null) {
			history.push(lastMove.deepClone());
		}
		
		print("History: ", history);
		
		List<Move> newMoves = findMoves();
		print("Classic moves", newMoves);
		
		for(Move m : newMoves) {
			print("Moves", moves);
			Move nextMove = moves.pop();
			if(nextMove == null) {
				print("returning");
				history.pop();
			}
			print("\nPopped", m, nextMove);
			makeMove(m);
			//if(1+1==2)break;
			if(solve(m)) {
				m.direction = -m.direction;
				solution.push(m.deepClone());
				makeMove(m);
				m.direction = -m.direction;
				solution.push(m.deepClone());
				return true;
			}
			m.direction = -m.direction;
			makeMove(m);
			print("Returning...");
			show();
		}
		print(moves.pop());
		
		history.pop();
		print("out of moves");
		
		show();
		print(history);
		print(moves);
		//throw new RuntimeException(history.toString() + "\n" + moves);
		
		return false;
	}
	
	private void makeMove(Move m) {
		Block b = m.block;
		int x, y, xtop, xbot, yleft, yright;
		switch(m.direction) {
		case UP:
			xtop = b.i - 1;
			xbot = b.i2 - 1;
			for(y = b.j; y < b.j2; y++) {
				grid[xtop][y] = b;
				grid[xbot][y] = null;
			}
			b.i--;
			b.i2--;
			break;

		case DOWN:
			xtop = b.i;
			xbot = b.i2;
			for(y = b.j; y < b.j2; y++) {
				grid[xtop][y] = null;
				grid[xbot][y] = b;
			}
			b.i++;
			b.i2++;
			break;
			
		case LEFT:
			yleft = b.j - 1;
			yright = b.j2 - 1;
			for(x = b.i; x < b.i2; x++) {
				grid[x][yleft] = b;
				grid[x][yright] = null;
			}
			b.j--;
			b.j2--;
			break;
			
		case RIGHT:
			yleft = b.j;
			yright = b.j2;
			for(x = b.i; x < b.i2; x++) {
				grid[x][yleft] = null;
				grid[x][yright] = b;
			}
			b.j++;
			b.j2++;
			break;
		}
	}

	private List<Move> findMoves() {
		List<Move> newMoves = new LinkedList<>();
		Move m = new Move();
		int x, y;
		
		moves.push(null);

		for(Block b : blocks) {
			m.block = b;
			
			m.direction = UP;
			//print(m);
			if(b.i > 0) {
				x = b.i - 1;
				for(y = b.j; y < b.j2; y++) {
					if(grid[x][y] != null) {
						break;
					}
				}
				if(y == b.j2) {
					//System.out.println("OK");
					newMoves.add(0, m.clone());
					moves.push(m.clone());
					//moves.push(m.clone());
				}
			}

			m.direction = DOWN;
			//print(m);
			if(b.i2 < maxi) {
				x = b.i2;
				for(y = b.j; y < b.j2; y++) {
					if(grid[x][y] != null) {
						break;
					}
				}
				if(y == b.j2) {
					//System.out.println("OK");
					newMoves.add(0, m.clone());
					moves.push(m.clone());
				}
			}
			
			m.direction = LEFT;
			//print(m);
			if(b.j > 0) {
				y = b.j-1;
				for(x = b.i; x < b.i2; x++) {
					if(grid[x][y] != null) {
						break;
					}
				}
				if(x == b.i2) {
					//System.out.println("OK");
					newMoves.add(0, m.clone());
					moves.push(m.clone());
				}
			}
			
			
			m.direction = RIGHT;
			//print(m);
			if(b.j2 < maxj) {
				y = b.j2;
				for(x = b.i; x < b.i2; x++) {
					if(grid[x][y] != null) {
						break;
					}
				}
				if(x == b.i2) {
					//System.out.println("OK");
					newMoves.add(0, m.clone());
					moves.push(m.clone());
				}
			}
		}
		return newMoves;
	}

	private boolean check() {
		for(Block goalBlock : goal) {
			Block b = grid[goalBlock.i][goalBlock.j];
			if(b != null && b.i == goalBlock.i && b.j == goalBlock.j && b.i2 == goalBlock.i2 && b.j2 == goalBlock.j2) {
				continue;
			}
			return false;
		}
		return true;
	}

	private void parse(Scanner init, Scanner goal) {
		maxi = init.nextInt();
		maxj = init.nextInt();
		grid = new Block[maxi][maxj];
		
		int id = 0, len, width;
		List<Block> blocksList = new ArrayList<>();
		while(init.hasNextInt()) {
			Block b = new Block();
			b.id = ++id;
			len = init.nextInt();
			width = init.nextInt();
			b.i = init.nextInt();
			b.j = init.nextInt();
			b.i2 = b.i + len;
			b.j2 = b.j + width;
			
			for(int x = b.i; x < b.i + len; x++) {
				for(int y = b.j; y < b.j + width; y++) {
					grid[x][y] = b;
				}
			}
			blocksList.add(b);
		}
		this.blocks = (Block[]) blocksList.toArray(new Block[blocksList.size()]);
		
		blocksList.clear();
		while(goal.hasNextInt()) {
			Block b = new Block();
			len = goal.nextInt();
			width = goal.nextInt();
			b.i = goal.nextInt();
			b.j = goal.nextInt();
			b.i2 = b.i + len;
			b.j2 = b.j + width;
			blocksList.add(b);
		}
		this.goal = (Block[]) blocksList.toArray(new Block[blocksList.size()]);
	}

	public Solver(Scanner init, Scanner goal) {
		parse(init, goal);
		seen = new HashSet<>();
		comparator = new BlocksComparator();
		solution = new Stack<Move>();
		history = new Stack<Move>();
		moves = new Stack<Move>();
		
		
		
		if(solve(null)) {
			while(!solution.isEmpty()) {
				Move m1 = solution.pop();
				Move m2 = solution.pop();
				System.out.print(m1.block.i + " " + m1.block.j + " ");
				System.out.println(m2.block.i + " " + m2.block.j);
			}
			System.exit(1);
		}
		System.exit(1);
	}

	public void print(Object ... args) {
		if(PRINT) {
			for(int x = 0; x < args.length; x++) {
				if(args[x] instanceof Stack) {
					Stack s = (Stack) args[x];
					System.out.print(s.size() > 0 ? "{" : "empty");
					for(int i = s.size()-1; i >= 0; i--) {
						System.out.print(s.get(i) + (i == 0 ? "}" :", "));
					}
				} else {
					System.out.print(args[x] + (x == args.length - 1 ? "" : "; "));
				}
			}
			System.out.println();
		}
	}
	
	public void show() {
		if(PRINT) {
			for(int x = 0; x < maxi; x++) {
				for(int y = 0; y < maxj; y++) {
					//System.out.println(grid[x][y]);
					//if(1+1==2) continue;
					if(grid[x][y] == null) {
						System.out.print(". ");
					} else {
						System.out.print(grid[x][y].id + " ");
					}
				}
				System.out.println();
			}
			System.out.println();
		}
	}
	
	public static void main(String args[]) {
		Scanner init = null, goal = null;
		String is = "dads+90.txt";
		String gs = "dads+90.goal.txt";
		try {
			if(args.length != 2) {
				init = new Scanner(new File(is));
				goal = new Scanner(new File(gs));
			} else {
				init = new Scanner(new File(args[0]));
				goal = new Scanner(new File(args[1]));
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		Solver solver = new Solver(init, goal);
	}
}
