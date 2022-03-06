package com.example.tracker1.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tracker1.Interface.IRecyclerItemClickListener;
import com.example.tracker1.R;

public class BuddyRequestViewHolder extends RecyclerView.ViewHolder {
    public TextView txt_user_email;
    public ImageView btn_accept, btn_decline;



    public BuddyRequestViewHolder(@NonNull View itemView) {
        super(itemView);
        txt_user_email = (TextView) itemView.findViewById(R.id.txt_user_email);
        btn_accept = (ImageView) itemView.findViewById(R.id.btn_accept);
        btn_decline = (ImageView) itemView.findViewById(R.id.btn_decline);
    }

}
