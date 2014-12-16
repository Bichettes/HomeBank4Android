package com.fowlcorp.homebank4android.gui;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fowlcorp.homebank4android.R;

public class AccountCardView extends CardView{
	

	public AccountCardView(Context context, ViewGroup parent ) {
		super(context);
		
		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
		View view = inflater.inflate(R.layout.card_layout,parent);
		
		this.addView(view);
		
	}

}
