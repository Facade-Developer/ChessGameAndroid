package com.example.chessgameandroid;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.chessgameandroid.ChessBoard.Line;

@SuppressLint("InflateParams")
public class ChessAdapter extends BaseAdapter
{
	String[] chessPieces = new String[13];
	// K=1 Q=2 R=3 B=4 N=5 P=6
	// Black = white + 6
	// Blank = 0
	int dim,whiteColor;
	TextView black, white;
	
	ChessMain ctx;
	
	public ChessAdapter() 
	{
		chessPieces[0] = " ";
		for(int i = 1; i < 13; i++)
			chessPieces[i] = String.valueOf(Character.toChars(9811+i));
	}
	
	public void connect(ChessMain context, int dimensions)
	{
		ctx = context; 
		dim = dimensions;
		black = (TextView)ctx.findViewById(R.id.black_text);
		black.setTextColor(Color.TRANSPARENT);
		white = (TextView)ctx.findViewById(R.id.white_text);
		whiteColor = ctx.getResources().getColor(R.color.white_square);
		reset();
	}

	public int getCount() {return 64;}

	public long getItemId(int position) {return 0;}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		SquareStruct square;
		if (convertView == null)
		{
			square = new SquareStruct();
			convertView = inflater.inflate(R.layout.chess_square, null);
			
			square.text = (TextView) convertView.findViewById(R.id.text);
			square.relay = (RelativeLayout) convertView.findViewById(R.id.relay);
			convertView.setTag(square);
		} else
			square = (SquareStruct) convertView.getTag();
		
		Line loc = ChessBoard.convert(position);
		int[] clr = getColors(loc);
		square.relay.setBackgroundColor(clr[0]);
		square.text.setText(chessPieces[getPiece(loc,clr[1])]);
		square.text.setTextSize(TypedValue.COMPLEX_UNIT_SP, dim/4f);
		square.text.setTextColor(clr[1]);
		
		android.widget.AbsListView.LayoutParams parms = new android.widget.AbsListView.LayoutParams(dim,dim);
		square.relay.setLayoutParams(parms);

		return convertView;
	}
	
	public int getPiece(Line p, int color)
	{
		int piece = ChessBoard.getPiece(p);
		if(piece != 0 && color == whiteColor)
		{
			if(ChessBoard.isWhite(piece))
				piece += 6;
			else
				piece -= 6;
		}
		return piece;
	}

	public int[] getColors(Line p)
	{
		int[] ret = new int[2];
		boolean white = (p.y&1) != (p.x&1);
		int hl = ChessBoard.getHighlight(p);
		if(hl == 1)
		{
			ret[0] = R.color.select_square;
			ret[1] = R.color.select_piece;
		} else if(ChessBoard.canProtect(p) || hl == 3)
		{
			if(white)
			{
				ret[0] = R.color.threat_white_square;
				ret[1] = R.color.threat_white_piece;
			} else
			{
				ret[0] = R.color.threat_black_square;
				ret[1] = R.color.threat_black_piece;
			}
		} else if(hl == 2)
		{
			if(white)
			{
				ret[0] = R.color.poss_white_square;
				ret[1] = R.color.poss_white_piece;
			} else
			{
				ret[0] = R.color.poss_black_square;
				ret[1] = R.color.poss_black_piece;
			}
		} else if(white)
		{
			ret[0] = R.color.white_square;
			ret[1] = R.color.white_piece;
		} else
		{
			ret[0] = R.color.black_square;
			ret[1] = R.color.black_piece;
		}
		
		ret[0] = ctx.getResources().getColor(ret[0]);
		ret[1] = ctx.getResources().getColor(ret[1]);
		return ret;
	}

	public Object getItem(int position) 
	{
		return null;
	}
	
	public class SquareStruct
	{
		RelativeLayout relay;
		TextView text;
	}
	
	public void updateBoard(boolean whiteTurn)
	{
		ctx.setTurn(whiteTurn);
	}

	public void clickAt(int loc, View v)
	{
		ChessBoard.sendClick(ChessBoard.convert(loc));
		this.notifyDataSetChanged();
	}

	public void updateCheck(int white, int black)
	{
		ctx.setCheck(white, black);
	}

	public void reset()
	{
		ctx.setTurn(ChessBoard.whiteTurn);
		ctx.setCheck(ChessBoard.threatLevel(true), ChessBoard.threatLevel(false));
		this.notifyDataSetChanged();
	}
}
