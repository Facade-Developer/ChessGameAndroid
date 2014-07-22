package com.example.chessgameandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class ChessMain extends Activity
{
	public static boolean landscape = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.i("info","ChessMain Activity creation");
		setContentView(R.layout.activity_chess_main);
		
		GridView grid = (GridView) this.findViewById(R.id.gridView);
		TextView bt = (TextView) findViewById(R.id.black_text), wt = (TextView) findViewById(R.id.white_text),
			     br = (TextView) findViewById(R.id.br_check),   tl = (TextView) findViewById(R.id.tl_check);
		

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int width = metrics.widthPixels, height = metrics.heightPixels, dim = width;
		
		bt.setText("Black Turn");
		wt.setText("White Turn");
		tl.setText("");
		br.setText("");

		landscape = height < width;
		if (landscape)
		{
			dim = height;
			bt.setText("Black\nTurn");
			wt.setText("White\nTurn");
		}
		dim&=~7;
		bt.setTextSize(TypedValue.COMPLEX_UNIT_SP, Math.abs(height-width)/20f);
		wt.setTextSize(TypedValue.COMPLEX_UNIT_SP, Math.abs(height-width)/20f);
		
		LayoutParams lays = new LayoutParams(dim, dim);
		lays.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		grid.setLayoutParams(lays);
		grid.setNumColumns(8);
		grid.setAdapter(ChessBoard.boardDraw);
		ChessBoard.boardDraw.connect(this, dim/8);

		grid.setOnItemClickListener(ChessBoard.listen);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.restart_menu, menu);
	    return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		  switch (item.getItemId())
		  {
		    case R.id.restart:
		    	ChessBoard.restart();
		    	return true;
		    default:
		      return super.onOptionsItemSelected(item);
		  }
		}
	
	public void setCheck(int white, int black) // 1 - nothing   0 - Check
	{
		for(int val : new int[]{white,black|4})
		{
			int id = R.id.br_check;
			if(landscape ^ (val > 3))
				id = R.id.tl_check;
			TextView txt = (TextView) this.findViewById(id);
			String mess = "";
			switch(val & 3)
			{
			case 0:
				mess = "Check";
				if(ChessBoard.isMate(val == white))
				{
					mess += "mate";
					playerWin(val==white);
				}
			}
			txt.setText(mess);
		}
	}

	public void playerWin(boolean b)
	{
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		String message = " wins!";
		if(b)
			message = "Black" + message;
		else
			message = "White" + message;
		builder.setTitle(message);
		builder.setMessage("Restart the Game?");
		builder.setPositiveButton("Yes", new OnClickListener() 
		{ public void onClick(DialogInterface dialog, int which) { ChessBoard.restart(); } });

		builder.setNegativeButton("Cancel", null);
		builder.show();
	}

	public void setTurn(boolean white)
	{
		TextView bt = (TextView) findViewById(R.id.black_text),
				wt = (TextView) findViewById(R.id.white_text);
		if(white)
		{
			bt.setTextColor(Color.TRANSPARENT);
			wt.setTextColor(Color.WHITE);
		} else
		{
			wt.setTextColor(Color.TRANSPARENT);
			bt.setTextColor(Color.WHITE);
		}
		
	}
}
