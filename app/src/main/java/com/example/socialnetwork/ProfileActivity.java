package com.example.socialnetwork;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView userName, userProfName, userStatus, userCountry, userGender, userRlationship, userDOB;
    private CircleImageView userProfImage;
    private DatabaseReference profilesUserRef, FriendRef, postRef;
    private FirebaseAuth mAuth;

    private Button myPosts, myFriends;

    private String currentUserId;
    private int countFriends = 0;
    private int countPosts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userName = (TextView) findViewById(R.id.my_profile_full_name);
        userProfName = (TextView) findViewById(R.id.my_username);
        userStatus = (TextView) findViewById(R.id.my_profile_status);
        userCountry = (TextView) findViewById(R.id.my_country);
        userGender = (TextView) findViewById(R.id.my_gender);
        userRlationship = (TextView) findViewById(R.id.my_relationship_status);
        userDOB = (TextView) findViewById(R.id.my_dob);
        userProfImage = (CircleImageView) findViewById(R.id.myProfile_pic);
        myFriends = (Button) findViewById(R.id.my_friends_button);
        myPosts = (Button) findViewById(R.id.my_post_button);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        profilesUserRef  = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        FriendRef = FirebaseDatabase.getInstance().getReference().child("friends");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        postRef.orderByChild("uid").startAt(currentUserId).endAt(currentUserId + "\uf8ff")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    countPosts = (int) dataSnapshot.getChildrenCount();
                    myPosts.setText(Integer.toString(countPosts) +" Posts");

                } else {
                    myPosts.setText("0 Posts");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FriendRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    countFriends = (int) dataSnapshot.getChildrenCount();
                    myFriends.setText(Integer.toString(countFriends) +" Friends");

                } else {
                    myFriends.setText("0 Friends");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        myFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToFriendseActivity();
            }
        });

        myPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToMyPostsActivity();
            }
        });

        profilesUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String myUserName = dataSnapshot.child("username").getValue().toString();
                    String myUserProfiilrName = dataSnapshot.child("fullname").getValue().toString();
                    String myProfileStatus = dataSnapshot.child("status").getValue().toString();
                    String myDob = dataSnapshot.child("dob").getValue().toString();
                    String myCountry = dataSnapshot.child("country").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myRelationshipStatus = dataSnapshot.child("relationshipstatus").getValue().toString();


                    Picasso.with(ProfileActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(userProfImage);

                    userName.setText("@" + myUserName);
                    userProfName.setText(myUserProfiilrName);
                    userStatus.setText(myProfileStatus);
                    userDOB.setText("DOB" + myDob);
                    userCountry.setText("Country" + myCountry);
                    userGender.setText("Gender" + myGender);
                    userRlationship.setText("relationship" + myRelationshipStatus);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private  void SendUserToFriendseActivity(){
        Intent addNewPostIntent = new Intent(ProfileActivity.this, FriendsActivity.class);
        startActivity(addNewPostIntent);
    }

    private  void SendUserToMyPostsActivity(){
        Intent addNewPostIntent = new Intent(ProfileActivity.this, MyPostsActivity.class);
        startActivity(addNewPostIntent);
    }
}
