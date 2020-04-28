package com.example.socialnetwork;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {

    private RecyclerView myFriendList;
    private DatabaseReference FriendsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String online_user_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("friends").child(online_user_id);


        myFriendList = (RecyclerView) findViewById(R.id.friend_list);
        myFriendList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendList.setLayoutManager(linearLayoutManager);

        DisplayAllFriends();
    }

    public void updateUserStatus(String state){
        String saveCurrentDate, saveCurrentTime;

        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        Calendar calFordTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm a");
        saveCurrentTime = currentTime.format(calFordDate.getTime());

        Map currentStateMap = new HashMap();
        currentStateMap.put("time" , saveCurrentTime);
        currentStateMap.put("date" , saveCurrentDate);
        currentStateMap.put("type" , state);

        UsersRef.child(online_user_id).child("userstate").updateChildren(currentStateMap);

    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUserStatus("online");
    }

    @Override
    protected void onStop() {
        super.onStop();
        updateUserStatus("offline");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateUserStatus("offline");
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;
        ImageView onlineStateView;

        public FriendsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            onlineStateView = (ImageView) itemView.findViewById(R.id.all_user_online_icon);


        }

        public void setProfileimage(Context ctx, String profileimage) {
            CircleImageView myImage = (CircleImageView) mView.findViewById(R.id.all_users_profile_imagge);
            Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(myImage);
        }

        public void setFullname(String fullname) {
            TextView myName  = (TextView) mView.findViewById(R.id.all_users_profile_name);
            myName.setText(fullname);
        }

        public void setDate(String date)  {
            TextView friendsDate  = (TextView) mView.findViewById(R.id.all_users_status);
            friendsDate.setText("Friends since: " + date);
        }
    }

    private void DisplayAllFriends() {
        FirebaseRecyclerOptions<Friends> options = new FirebaseRecyclerOptions.Builder<Friends>().setQuery(FriendsRef, Friends.class).build();
        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull Friends model) {
                        final String usersIDs = getRef(position).getKey();
                        holder.setDate(model.getDate());

                        UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    final String userName = dataSnapshot.child("fullname").getValue().toString();
                                    final String profileImage = dataSnapshot.child("profileimage").getValue().toString();
                                    final String type;

                                    if(dataSnapshot.hasChild("userstate")){
                                        type = dataSnapshot.child("userstate").child("type").getValue().toString();

                                        if(type.equals("online")){
                                            holder.onlineStateView.setVisibility(View.VISIBLE);
                                        } else{
                                            holder.onlineStateView.setVisibility(View.INVISIBLE);
                                        }
                                    }

                                    holder.setFullname(userName);
                                    holder.setProfileimage(getApplicationContext(),profileImage);

                                    holder.mView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            CharSequence options[] = new CharSequence[]{
                                                    userName + "'s profile",
                                                    "Send massege",

                                            };
                                            AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                                            builder.setTitle("Select Option");

                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if(which == 0){
                                                        Intent profileIntant = new Intent(FriendsActivity.this, PersonProfileActivity.class);
                                                        profileIntant.putExtra("visit_user_id", usersIDs);
                                                        startActivity(profileIntant);

                                                    }
                                                    if(which == 1){
                                                        Intent chatIntant = new Intent(FriendsActivity.this, ChatActivity.class);
                                                        chatIntant.putExtra("visit_user_id", usersIDs);
                                                        chatIntant.putExtra("userName", userName);

                                                        startActivity(chatIntant);
                                                    }

                                                }
                                            });
                                            builder.show();
                                        }
                                    });

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_users_display_layout, viewGroup, false);
                        FriendsViewHolder viewHolder = new FriendsViewHolder(view);
                        return viewHolder;
                    }
                };

        myFriendList.setAdapter(adapter);
        adapter.startListening();
    }
}