package com.example.socialnetwork;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {
    private TextView userName, userProfName, userStatus, userCountry, userGender, userRlationship, userDOB;
    private CircleImageView userProfImage;
    private Button sendFrindRequestbutton, declineFriendRequest;

    private DatabaseReference FriendRequestRef, UserRef, FriendRef;
    private FirebaseAuth mAuth;
    private String senderUserId, receiveUserId, current_state;
    private String saveCurrentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth = FirebaseAuth.getInstance();

        receiveUserId = getIntent().getExtras().get("visit_user_id").toString();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        senderUserId = mAuth.getCurrentUser().getUid();
        FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("friendRequests");
        FriendRef = FirebaseDatabase.getInstance().getReference().child("friends");

        intializeFields();

        UserRef.child(receiveUserId).addValueEventListener(new ValueEventListener() {
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


                    Picasso.with(PersonProfileActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(userProfImage);

                    userName.setText("@" + myUserName);
                    userProfName.setText(myUserProfiilrName);
                    userStatus.setText(myProfileStatus);
                    userDOB.setText("DOB" + myDob);
                    userCountry.setText("Country" + myCountry);
                    userGender.setText("Gender" + myGender);
                    userRlationship.setText("relationship" + myRelationshipStatus);

                    MaintananceofButton();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        declineFriendRequest.setVisibility(View.INVISIBLE);
        declineFriendRequest.setEnabled(false);

        if(!senderUserId.equals(receiveUserId)){

            sendFrindRequestbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendFrindRequestbutton.setEnabled(false);

                    if(current_state.equals("not_friends")){
                        sendFrindRequesttoPearson();
                    }
                    if(current_state.equals("request_sent")){
                        cancelFriendRequest();
                    }
                    if(current_state.equals("request_recieved")){
                        acceptFriendRequest();
                    }
                    if(current_state.equals("friends")){
                        unFriendExistindFriend();
                    }
                }
            });

        } else{
            declineFriendRequest.setVisibility(View.INVISIBLE);
            sendFrindRequestbutton.setVisibility(View.INVISIBLE);
        }

    }

    private void unFriendExistindFriend(){
        FriendRef.child(senderUserId).child(receiveUserId)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    FriendRef.child(receiveUserId).child(senderUserId)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                sendFrindRequestbutton.setEnabled(true);
                                current_state = "not_friends";
                                sendFrindRequestbutton.setText("send Friend Request");

                                declineFriendRequest.setEnabled(false);
                                declineFriendRequest.setVisibility(View.INVISIBLE);

                            }
                        }
                    });
                }
            }
        });
    }

    private void sendFrindRequesttoPearson(){
        FriendRequestRef.child(senderUserId).child(receiveUserId)
                .child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    FriendRequestRef.child(receiveUserId).child(senderUserId)
                            .child("request_type").setValue("recieved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                sendFrindRequestbutton.setEnabled(true);
                                current_state = "request_sent";
                                sendFrindRequestbutton.setText("Cancel Friend Request");
                                declineFriendRequest.setEnabled(false);
                                declineFriendRequest.setVisibility(View.INVISIBLE);

                            }
                        }
                    });
                }
            }
        });

    }

    private void intializeFields(){

        userName = (TextView) findViewById(R.id.person_full_name);
        userProfName = (TextView) findViewById(R.id.person_username);
        userStatus = (TextView) findViewById(R.id.person_status);
        userCountry = (TextView) findViewById(R.id.person_country);
        userGender = (TextView) findViewById(R.id.person_gender);
        userRlationship = (TextView) findViewById(R.id.person_relationship_status);
        userDOB = (TextView) findViewById(R.id.person_dob);
        userProfImage = (CircleImageView) findViewById(R.id.person_profile_pic);

        sendFrindRequestbutton = (Button) findViewById(R.id.person_sendFrindRequest_button);
        declineFriendRequest = (Button) findViewById(R.id.person_decline_friend_regest);

        current_state = "not_friends";
    }

    private void MaintananceofButton(){
        FriendRequestRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(receiveUserId)){
                    String request_Type = dataSnapshot.child(receiveUserId).child("request_type").getValue().toString();
                    if(request_Type.equals("sent")){
                        current_state = "request_sent";
                        sendFrindRequestbutton.setText("Cancel Friend request");
                        declineFriendRequest.setEnabled(false);
                        declineFriendRequest.setVisibility(View.INVISIBLE);

                    } else if(request_Type.equals("recieved")){
                        current_state = "request_recieved";
                        sendFrindRequestbutton.setText("accept Friend request");

                        declineFriendRequest.setEnabled(true);
                        declineFriendRequest.setVisibility(View.VISIBLE);

                        declineFriendRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelFriendRequest();
                            }
                        });
                    }
                } else {
                    FriendRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(receiveUserId)){
                                current_state = "friends";
                                sendFrindRequestbutton.setText("unFriend this person");

                                declineFriendRequest.setVisibility(View.INVISIBLE);
                                declineFriendRequest.setEnabled(false);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void acceptFriendRequest(){
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        FriendRef.child(senderUserId).child(receiveUserId).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            FriendRef.child(receiveUserId).child(senderUserId).child("date").setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                FriendRequestRef.child(senderUserId).child(receiveUserId)
                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            FriendRequestRef.child(receiveUserId).child(senderUserId)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        sendFrindRequestbutton.setEnabled(true);
                                                                        current_state = "friends";
                                                                        sendFrindRequestbutton.setText("unFriend this person");

                                                                        declineFriendRequest.setEnabled(false);
                                                                        declineFriendRequest.setVisibility(View.INVISIBLE);

                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void cancelFriendRequest(){
        FriendRequestRef.child(senderUserId).child(receiveUserId)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    FriendRequestRef.child(receiveUserId).child(senderUserId)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                sendFrindRequestbutton.setEnabled(true);
                                current_state = "not_friends";
                                sendFrindRequestbutton.setText("send Friend Request");
                                declineFriendRequest.setEnabled(false);
                                declineFriendRequest.setVisibility(View.INVISIBLE);

                            }
                        }
                    });
                }
            }
        });

    }
}
