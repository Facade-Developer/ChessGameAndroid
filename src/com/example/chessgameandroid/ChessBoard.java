package com.example.chessgameandroid;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.graphics.Point;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class ChessBoard extends Application
{
	public static int[][] board = new int[][]
			{new int[8],new int[8],new int[8],new int[8],
			 new int[8],new int[8],new int[8],new int[8]};
	public static List<List<List<Line>>> possible = null;;
	public static final int[] boardInit = new int[]{3,5,4,1,2,4,5,3}, util01  = new int[]{-1,1};

	public static ChessAdapter boardDraw;
	public static OnItemClickListener listen;
	
	public static Line prev = new Line(-1,-1), bKing, wKing;
	public static boolean whiteTurn = true;
	public static int whiteCheck = 2, blackCheck = 2;
	public static ArrayList<Line> whiteKing, blackKing;

	public static int debug = 0;
	public static String info = "info";
	
	public static int turn = 0, checkTurn = -1;
	public static List<Line> blackThreat, whiteThreat, blackMate, whiteMate;
	
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		possible  = new ArrayList<List<List<Line>>>(8);
		boardDraw = new ChessAdapter();
		listen = new OnItemClickListener() 
		{
			public void onItemClick(AdapterView<?> parent, View v,
				int position, long id) 
			{ boardDraw.clickAt(position,v); }
		};
		
		reset();
	}
	
	public static List<Line> getXzibit(List<List<List<Line>>> xzibit, Line p)
	{
		if(isValid(p,8))
			return xzibit.get(p.x).get(p.y);
		return null;
	}
	
	public static void addXzibit(List<List<List<Line>>> xzibit, Line r, Line p)
	{ 
		if(isValid(r,8))
			xzibit.get(r.x).get(r.y).add(p); 
	}
	
	public static void removeXzibit(List<List<List<Line>>> xzibit, Line r, Line p)
	{
		if(isValid(r,8))
			xzibit.get(r.x).get(r.y).remove(p);
	}
	
	public static List<Line> getXzibit(List<List<List<Line>>> xzibit, int x, int y)
	{
		if(isValid(x,8) && isValid(y,8))
			return xzibit.get(x).get(y);
		return null;
	}
	
	public static void addXzibit(List<List<List<Line>>> xzibit, int x, int y, Line p)
	{
		if(isValid(x,8) && isValid(y,8))
			xzibit.get(x).get(y).add(p);
	}
	
	public static void removeXzibit(List<List<List<Line>>> xzibit, int x, int y, Line p)
	{
		if(isValid(x,8) && isValid(y,8))
			xzibit.get(x).get(y).remove(p);
	}

	private static void reset()
	{
		possible.clear();
		for(int i = 0; i < 8; i++)
		{
			possible.add(new ArrayList<List<Line>>(8));
			for(int k = 0; k < 8; k++)
			{
				if(i == 0 || i == 7)
					board[i][k] = (boardInit[k]+(i==0 ? 6 : 0));
				else if(i == 1 || i == 6)
					board[i][k] = (6+(i==1 ? 6 : 0));
				else
					board[i][k] = 0;
				possible.get(i).add(new ArrayList<Line>());
			}
		}
		turn = 0; checkTurn = -1;
		whiteCheck = 2; blackCheck = 2; 
		whiteKing = null; blackKing = null;
		whiteTurn = true; prev.set(-1, -1);
		for(int x = 0; x < 8; x++)
			for(int y = 0; y < 8; y++)
				propagatePossible(new Line(x,y));
		updateKing(false,new Line(0,3));
		updateKing(true,new Line(7,3));
		updateThreats();
	}
	
	public static void restart()
	{
		reset();
		boardDraw.reset();
	}
	
	public static int threatLevel(boolean white)
	{
		if(turn != checkTurn)
			updateThreats();
		return (white ? whiteCheck : blackCheck);
	}
	
	private static void updateThreats()
	{
		if(turn != checkTurn)
		{
			blackThreat = updateThreat(false);
			whiteThreat = updateThreat(true);
			checkTurn = turn;
			blackMate = getMovers(false);
			whiteMate = getMovers(true);
		}
		debugOn("King Checks");
		print("White: " + whiteCheck + ", Black: " + blackCheck);
		debugOff();
	}
	
	public static boolean canProtect(Line ln)
	{
		if(isWhite(ln))
			return whiteMate.contains(ln);
		return blackMate.contains(ln);
	}
	
	public static List<Line> getMovers(boolean b)
	{
		List<Line> lines = new ArrayList<Line>();
		for(int x = 0; x < 8; x++)
			for(int y = 0; y < 8; y++)
			{
				Line ln = new Line(x,y);
				if(getPiece(ln) != 0)
					if(canMove(ln))
						lines.add(ln);
			}
		return lines;
	}

	private static List<Line> updateThreat(boolean white)
	{
		Line king = (white ? wKing : bKing);
		List<Line> lns = new ArrayList<Line>(), all = getXzibit(possible,king);
		int threat = 2;
		for(Line ln : all)
		{
			if(ln.isThreat(king))
			{
				ArrayList<Line> line = new ArrayList<Line>();
				int type = 0;
				line.add(ln);
				if(ln.line != null)
				{
					Line temp = null;
					int val = ln.line.size();
					for(int i = 0; i < val; i++)
					{
						temp = ln.line.get(i);
						if(king.equals(temp))
							i = val;
						else
						{
							if(getPiece(temp) != 0)
								type++;
							line.add(temp);
						}
					}
				}
				if(type < 2)
				{
					if(type < threat)
						threat = type;
					Line vector = new Line(king,line);
					vector.type = type; // Warning level: 0-check X-X blockers
					lns.add(vector);
				}
			}
		}
		if(white)
			whiteCheck = threat;
		else
			blackCheck = threat;
		return lns;
	}

	private static void updateKing(boolean white, Line t)
	{
		print((white ? "White" : "Black") + " King: " + t);
		if(white)
			wKing = t;
		else
			bKing = t;
	}
	
	private static boolean canMove(Line p)
	{
		int val = getPiece(p);
		if(val == 0)
			return false;
		boolean white = isWhite(val), king = (val == 1 || val == 7);
		for(Line ln : getThreats(white))
		{
			if(ln.type == 0)
			{
				for(int i = 0; i < ln.line.size(); i++)
				{
					Line loc = ln.line.get(i);
					List<Line> temp = getXzibit(possible,loc);
					for(Line m : temp)
					{
						if(m.equals(p))
						{
							if(m.isVisible(loc))
							{
								if(king)
									return isDangerous(loc, white);
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	public static boolean isDangerous(Line loc, boolean white)
	{
		List<Line> temp = getXzibit(possible,loc);
		for(Line m : temp)
			if(isWhite(m) != white)
				if(isVisible(m,loc,white) && m.isThreat(null))
					return false;
		return true;
	}
	
	public static boolean isVisible(Line ori, Line loc, boolean white)
	{
		if(ori.type == -1)
		{
			for(Line ln : ori.line)
			{
				if(ln.equals(loc))
					return true;
				if(getPiece(ln) != 0)
					return false;
			}
		} 
		return true;
	}
	
	public static List<Line> getThreats(boolean white)
	{
		if(turn != checkTurn)
			updateThreats();
		return (white ? whiteThreat : blackThreat);
	}
	
	public static int getPiece(Line p)
	{
		if(isValid(p,8))
			return board[p.x][p.y]&15;
		return -1;
	}
	
	public static void setPiece(Line p, int val)
	{
		removePossible(p);
		if(isValid(p,8))
			board[p.x][p.y] = val;
		propagatePossible(p);
	}

	public static void movePiece(Line p, Line t)
	{
		boolean white = isWhite(p);
		Line king = (white ? wKing : bKing);
		setPiece(t,getPiece(p));
		setPiece(p,0);
		if(p.equals(king))
			updateKing(white,t);
	}

	private static boolean isWhite(Line p)
	{ return isWhite(getPiece(p)); }
	public static boolean isWhite(int piece) 
	{ return piece < 7; }
	public static boolean isValid(int x, int val) 
	{ return (x > -1 && x < val); }
	public static boolean isValid(Line p, int val)
	{ return isValid(p.x,val) && isValid(p.y,val); }

	public static boolean empty(Line p)
	{
		if(isValid(p.x,8) && isValid(p.y,8) && getPiece(p) == 0)
			return true;
		return false;
	}
	
	public static int check(Line p, boolean white)
	{
		if(isValid(p.x,8) && isValid(p.y,8))
		{
			int piece = getPiece(p);
			if(piece == 0)
				return 0;
			if(isWhite(piece) != white)
				return 1;
		}
		return -1;
	}
	
	private static void removePossible(Line p)
	{
		for(int x = 0; x < 8; x++)
			for(int y = 0; y < 8; y++)
			{
				removeXzibit(possible,x,y,p);
			}
	}

	public static void propagatePossible(Line loc)
	{
		int piece = getPiece(loc);
		boolean white = true;
		if(piece > 6) 
		{
			piece -= 6;
			white = false;
		}
		if(piece == 1)
			getKingMoves(loc,white);
		else if(piece == 2)
			getQueenMoves(loc,white);
		else if(piece == 3)
			getRookMoves(loc,white);
		else if(piece == 4)
			getBishopMoves(loc,white);
		else if(piece == 5)
			getKnightMoves(loc,white);
		else if(piece == 6)
			getPawnMoves(loc,white);
	}
	
	public static void getPawnMoves(Line loc, boolean white)
	{
		Line p = new Line(loc), pawn = new Line(loc, new ArrayList<Line>());
		int val = (white ? -1 : 1);
		p.x+=val;
		if(isValid(p,8))
		{
			loc = new Line(pawn,1);
			pawn.add(loc);
			addXzibit(possible,p,loc);
			if(pawn.x == (white ? 6 : 1))
			{
				Line temp = new Line(p);
				p.x+=val;
				if(isValid(p,8))
				{
					loc = new Line(pawn,3,temp);
					pawn.add(loc);
					addXzibit(possible,p,loc);
				}
				p.x-=val;
			}
		}
		p.y--;
		for(int i = 1; i < 3; p.y+=(++i))
		{
			if(isValid(p,8))
			{
				loc = new Line(pawn,2);
				pawn.add(loc);
				addXzibit(possible,p,loc);
			}
		}
	}

	public static void getKnightMoves(Line loc, boolean white)
	{
		Line temp = new Line(loc), knight = new Line(loc);
		knight.setList(new ArrayList<Line>());
		for(int i = 1; i > -2; i-=2)
			for(int k = 2; k > -3; k-=4)
			{
				temp.x = knight.x + i; temp.y = knight.y + k;
				loc = new Line(knight,4);
				addXzibit(possible,temp,loc);
				knight.add(loc);
				temp.x = knight.x + k; temp.y = knight.y + i;
				loc = new Line(knight,4);
				addXzibit(possible,temp,loc);
				knight.add(loc);
			}
	}

	public static void getBishopMoves(Line loc, boolean white)
	{
		propagate(loc,white,new Line(1,1),new Line(1,-1));
	}

	public static void getRookMoves(Line loc, boolean white)
	{
		propagate(loc,white,new Line(1,0),new Line(0,1));
	}

	public static void getQueenMoves(Line loc, boolean white)
	{
		propagate(loc,white,new Line(1,0),new Line(0,1),new Line(1,1),new Line(1,-1));
	}
	
	public static void propagate(Line loc, boolean white, Line... mod)
	{
		Line link = new Line(loc,new ArrayList<Line>());
		for(Line ln : mod)
		{
			linePropagate(link,ln,white);
			ln.set(-ln.x, -ln.y);
			linePropagate(link,ln,white);
		}
	}
	
	public static void linePropagate(Line piece, Line mod, boolean white)
	{
		print("PROPAGATE",mod.toString());
		Line l = new Line(piece), temp = new Line(piece,-1);
		temp.setList(new ArrayList<Line>());
		while(isValid(add(l,mod),8))
		{
			addXzibit(possible,l,temp);
			temp.add(new Line(l));
		}
		piece.add(temp);
	}

	public static void getKingMoves(Line loc, boolean white)
	{
		debugOn("King Create");
		Line temp = new Line(), king = new Line(loc);
		king.setList(new ArrayList<Line>());
		String val = "";
		for(int i = -1; i < 2; i++)
			for(int k = -1; k < 2; k++)
				if(i!=0 || k!=0)
				{
					temp.set(i+loc.x,k+loc.y);
					loc = new Line(king,5);
					king.add(loc);
					val += temp.toString() + " ";
					addXzibit(possible,temp,loc);
				}
		print(val);
		debugOff();
	}

	public static int getHighlight(Line p)
	{
		if(isValid(prev,8))
		{
			if(prev.equals(p))
			{
				return 1;
			}
			if(isPossibleMove(p))
			{
				return 2;
			}
		}
		return 0;
	}
	
	public static boolean isPossibleMove(Line p)
	{
		List<Line> arr = possible.get(p.x).get(p.y);
		if(arr.contains(prev))
		{
			int loc = arr.indexOf(prev);
			Line ln = arr.get(loc);
			if(ln.isVisible(p))
			{
				int temp = getPiece(p);
				if(temp == 0)
					return true;
				return isWhite(temp) != isWhite(prev);
			}
		}
		return false;
	}

	public static boolean protectingKing()
	{ return false; }

	public static void sendClick(Line p)
	{
		int check = check(p,whiteTurn);
		if(isValid(prev,8) && isPossibleMove(p))
		{
			movePiece(prev,p);
			prev.set(-1, -1);
			turn++;
			whiteTurn = !whiteTurn;
			updateThreats();
			boardDraw.updateBoard(whiteTurn);
			boardDraw.updateCheck(whiteCheck,blackCheck);
		} else if(check == -1)
		{
			prev.set(p.x, p.y);
			//updateCheck(whiteTurn);
		}
	}
	
	public static Line add(Line p, int x, int y)
	{
		p.x+=x;p.y+=y;return p;
	}

	public static Line add(Line p, Line v)
	{
		p.x+=v.x;p.y+=v.y;return p;
	}

	public static Line sub(Line p, Line v)
	{
		p.x-=v.x;p.y-=v.y;return p;
	}
	
	public static Line mod(Line p, Line v)
	{
		return new Line(p.x+v.x,p.y+v.y);
	}
	
	public static int[] combine(int[]... arrs)
	{
		int size = 0, i = 0;
		for(int[] arr : arrs)
		{
			size += arr.length;
		}
		int[] ret = new int[size];
		size = 0;
		for(int[] arr : arrs)
		{
			i = 0;
			while(i < arr.length && arr[i] != -1)
			{
				ret[size++] = arr[i++];
			}
		}
		return fill(ret, size);
	}

	public static boolean contains(int[] arr, int loc)
	{
		if(arr == null)
			return false;
		for(int i = 0; i < arr.length; i++)
			if(arr[i] == loc) return true;
		return false;
	}

	public static int[] fill(int[] arr, int filled)
	{
		for(; filled < arr.length; filled++)
			arr[filled] = -1;
		return arr;
	}
	
	public static Line convert(int loc)
	{
		if(!isValid(loc,64))
			return null;
		return new Line(loc/8,loc%8);
	}
	
	public static void print(String info, String str)
	{
		Log.i(info,str);
	}
	
	public static boolean isMate(boolean b)
	{
		if(whiteTurn == b)
			if((b ? whiteMate : blackMate).size() == 0)
			{
				Line ln = (b ? wKing : bKing);
				for(int i = -1; i < 2; i++)
					for(int j = -1; j < 2; j++)
						if(i!=0 || j!=0)
							if(isPossibleMove(ln))
								return false;
				
			} else
                return false;
            
		return true;
	}
	
	public static void print(String str)
	{ if(debug > 0) Log.i(info + "(" + debug + ")",str); }
	public static void debugOn()
	{ debug++; }
	public static void debugOn(String str)
	{ debug++; setInfo(str); }
	public static void debugOn(String str, int i)
	{ setInfo(str); debug += i; }
	public static void debugOff()
	{ debug--; }
	public static void setInfo(String str)
	{ info = str; }
	
	public static class Line extends Point
	{
		ArrayList<Line> line;
		int type = 0, turnCheck = 0; // 1-3 pawn: pawn move, pawn attack, pawn double move
					  // -1: line not a point
		Line extra, parent, king;
		
		public Line(Point p) 
		{ super(p); }
		public Line(int i, int j) 
		{ super(i,j); }
		public Line() 
		{ super(); }
		public Line(Line loc, ArrayList<Line> arrayList) 
		{ super(loc); line = arrayList; }
		public Line(Line p, int i) 
		{ super(p); type = i; }
		public Line(Line p, int i, Line x) 
		{ super(p); type = i; extra = x; }
		public Line(ArrayList<Line> arrayList) 
		{ line = arrayList; }
		public void setList(ArrayList<Line> ls)
		{ line = ls; }

		
		public void add(Line ln)
		{
			ln.parent = this;
			line.add(ln);
		}
		
		public boolean isThreat(Line loc)
		{
			if(loc == null)
				return  type!=3 && type!=1;
			return type!=3 && type!=1 && isWhite(loc) != isWhite(this);
		}
		
		public boolean isVisible(Line loc)
		{
			boolean thisWhite = isWhite(this);
			List<Line> threats = (thisWhite ? whiteThreat : blackThreat);
			int val = 0;
			for(Line threat : threats)
			{
				if(threat.type == 1)
				{
					val = 0;
					for(Line thr : threat.line)
					{
						if(thr.equals(this))
							val |= 1;
						if(thr.equals(loc))
							val |= 2;
					}
					if(val == 1)
						return false;
				}
				if(threat.type == 0)
				{
                    if (type == 5)
                        return isDangerous(loc,isWhite(this));
					val = 0;
					for(Line thr : threat.line)
					{
						if(thr.equals(loc))
							val |= 4;
					}
					if((val&4) == 0)
						return false;
				}
			}
			if(type == 5)
			{
				List<Line> lst = getXzibit(possible,loc);
				for(Line ln : lst)
				{
					if(ln.isThreat(this))
					{
						if(ln.type == -1)
						{
							if(ChessBoard.isVisible(ln,loc,thisWhite))
							{
								return false;
							}
						} else
						{
							return false;
						}
					}
				}
				return true;
			}
			if(line == null)
			{
				int c = getPiece(loc);
				boolean bool = thisWhite != isWhite(c);
				switch(type)
				{
				case 4:
				case 0:
					if(bool)
						return true;
				case 1:
					return c == 0;
				case 3:
					return getPiece(extra) == 0 && c == 0;
				case 2:
					return c != 0 && bool;
				}
			}
			for(Line ln : line)
			{
				int piece = getPiece(ln);
				if(ln.equals(loc))
				{
					if(piece == 0)
						return true;
					return isWhite(piece) != thisWhite;
				}
				if(piece != 0)
					return false;
			}
			return false;
		}
		public Line protectKing(Line loc, Line threats)
		{
			if(turnCheck == turn)
				return king;
			if(parent != null)
				return parent.protectKing(loc, threats);
			for(Line ln : line)
			{
				if(ln.line.contains(loc))
					return ln;
			}
			return null;
		}
		
		public boolean equals(Object obj)
		{
			if(obj instanceof Point || obj instanceof Line)
			{
				Point temp = (Point)obj;
				return temp.x == this.x && temp.y == this.y;
			}
			return obj.equals(this);
		}
		
		public String toString()
		{
			return "Line(" + x + ", " + y + ", " + type + ")";
		}
	}


}