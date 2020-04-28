package com.example.socialnetwork;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
    private List<Messages> userMessageList ;
    private FirebaseAuth mAuth;
    private DatabaseReference usersDatabaseRef;

    public MessagesAdapter(List<Messages> userMessageList){
        this.userMessageList = userMessageList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView senderMessgaeText, recieverMessageText;
        public CircleImageView recieverProfileImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessgaeText = (TextView) itemView.findViewById(R.id.sender_message_text);
            recieverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            recieverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);

        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_layout_of_users, parent, false);

        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessageList.get(position);

        String fromUserId = messages.getFrom();
        String fromMessageType = messages.getType();

        usersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);
        usersDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String image = dataSnapshot.child("profileimage").getValue().toString();

                    Picasso.with(holder.recieverProfileImage.getContext()).load(image).placeholder(
                            R.drawable.profile).into(holder.recieverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(fromMessageType.equals("text")){
            holder.recieverMessageText.setVisibility(View.INVISIBLE);
            holder.recieverProfileImage.setVisibility(View.INVISIBLE);

            if(fromUserId.equals(messageSenderId)){
                holder.senderMessgaeText.setBackgroundResource(R.drawable.sender_message_text_background);
                holder.senderMessgaeText.setTextColor(Color.WHITE);
                holder.senderMessgaeText.setGravity(Gravity.LEFT);
                holder.senderMessgaeText.setText(messages.getMessage());
            } else {
                holder.senderMessgaeText.setVisibility(View.INVISIBLE);

                holder.recieverProfileImage.setVisibility(View.VISIBLE);
                holder.recieverMessageText.setVisibility(View.VISIBLE);

                holder.recieverMessageText.setBackgroundResource(R.drawable.receiver_message_text_background);
                holder.recieverMessageText.setTextColor(Color.WHITE);
                holder.recieverMessageText.setGravity(Gravity.LEFT);
                holder.recieverMessageText.setText(messages.getMessage());

            }

        }

    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }
}
