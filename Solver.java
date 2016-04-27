import java.io.*;
import java.util.*;

public class Solver {
	private static Block[][] grid;
	private static int maxi;
	private static int maxj;
	private static Block[] blocks;
	private static Block[] goal;
	private static HashSet<Block[]> seen;
	
	private static final int UP = 1;
	private static final int DOWN = -1;
	private static final int LEFT = 2;
	private static final int RIGHT = -2;
	
	static class Block implements Comparable<Block> {
		int id;
		int i, j; // coords of top left corner
		int i2, j2; // i2 = i + len; j2 = j + width
		int len, width;
		public String toString() {
			return String.format("%d: (%d,%d at %d,%d -> %d,%d)", id, len, width, i, j, i2, j2);
			//return String.format("%d: (%d,%d at %d,%d)", id, len, width, i, j);
			//return "" + id;
		}
		public Block clone() {
			Block b = new Block();
			b.i = i; b.j = j; b.len = len; b.width = width;
			return b;
		}
		public int compareTo(Block b) {
			return 0;
		}
	}
	
	static class Configuration {
		Block[][] grid;
		Block[] blocks;
		
		public Configuration() {
			grid = new Block[maxi][maxj];
			for(int x = 0; x < maxi; x++) {
				for(int y = 0; y < maxj; y++) {
					if(Solver.grid[x][y] == null) {
						grid[x][y] = null;
					} else {
						grid[x][y] = Solver.grid[x][y].clone();
					}
				}
			}
			blocks = new Block[Solver.blocks.length];
			for(int x = 0; x < blocks.length; x++) {
				blocks[x] = Solver.blocks[x].clone();
			}
		}
		
		public void show() {
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
		public int hashCode() {
			int hash = 0;
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
			return hash;
		}
		
		public boolean equals(Configuration c) {
			for(Block cBlock : c.blocks) {
				Block b = grid[cBlock.i][cBlock.j];
				System.out.println(b + "; " + cBlock);
				if(b != null && b.i == cBlock.i && b.j == cBlock.j && b.len == cBlock.len && b.width == cBlock.width) {
					continue;
				}
				return false;
			}
			return true;
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
	}
	
	private void solve() {
		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		show();
		Configuration config = new Configuration();
		config.show();
		print(config.hashCode());
		if(!seen.add(blocks)) {
			return;
		}
		if(check()) {
			print("FOUND!!");
			System.exit(0);
		}
		
		List<Move> moves = findMoves();
		print(moves);
		
		for(Move m : moves) {
			makeMove(m);
			//if(1+1==2)break;
			solve();
			m.direction = -m.direction;
			makeMove(m);
			Configuration c2 = new Configuration();
			print(config.equals(c2));
			config.blocks[0].i = -1;
			for(Block b : config.blocks) {
				print(b);
			}
			print();
			for(Block b : c2.blocks) {
				print(b);
			}
			print("Equals:");
			print(config.equals(c2));
			print(c2.equals(config));
		}
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
		List<Move> moves = new ArrayList<>();
		Move m = new Move();
		int x, y;

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
					moves.add(m.clone());
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
					moves.add(m.clone());
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
					moves.add(m.clone());
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
					moves.add(m.clone());
				}
			}
		}
		return moves;
	}

	private boolean check() {
		for(Block goalBlock : goal) {
			Block b = grid[goalBlock.i][goalBlock.j];
			if(b != null && b.i == goalBlock.i && b.j == goalBlock.j && b.len == goalBlock.len && b.width == goalBlock.width) {
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
		
		int id = 0;
		List<Block> blocksList = new ArrayList<>();
		while(init.hasNextInt()) {
			Block b = new Block();
			b.id = ++id;
			b.len = init.nextInt();
			b.width = init.nextInt();
			b.i = init.nextInt();
			b.j = init.nextInt();
			b.i2 = b.i + b.len;
			b.j2 = b.j + b.width;
			
			for(int x = b.i; x < b.i + b.len; x++) {
				for(int y = b.j; y < b.j + b.width; y++) {
					grid[x][y] = b;
				}
			}
			blocksList.add(b);
		}
		this.blocks = (Block[]) blocksList.toArray(new Block[blocksList.size()]);
		
		blocksList.clear();
		while(goal.hasNextInt()) {
			Block b = new Block();
			b.len = goal.nextInt();
			b.width = goal.nextInt();
			b.i = goal.nextInt();
			b.j = goal.nextInt();
			blocksList.add(b);
		}
		this.goal = (Block[]) blocksList.toArray(new Block[blocksList.size()]);
	}

	public Solver(Scanner init, Scanner goal) {
		parse(init, goal);
		seen = new HashSet<>();
		solve();
	}

	public void print(Object ... args) {
		for(Object arg : args) {
			System.out.print(arg + "; ");
		}
		System.out.println();
	}
	
	public void show() {
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
	
	public static void main(String args[]) {
		Scanner init = null, goal = null;
		try {
			init = new Scanner(new File("init.txt"));
			goal = new Scanner(new File("goal.txt"));
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		Solver solver = new Solver(init, goal);
	}
}
