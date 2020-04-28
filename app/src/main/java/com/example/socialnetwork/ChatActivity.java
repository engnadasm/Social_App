package com.example.socialnetwork;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar chateToolbar;
    private ImageButton sendMessagebutton, sendImageFileButton;
    private EditText message;
    private RecyclerView usersMessagesList;

    private String messageRecieverId, messageRecieverName, messageSenderId;

    private TextView recieverName, userLastSeen;
    private CircleImageView receiverProfileImage;

    private DatabaseReference RootRef;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;

    private String saveCurrentDate, saveCurrentTime;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        RootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        messageRecieverId = getIntent().getExtras().get("visit_user_id").toString();
        messageRecieverName = getIntent().getExtras().get("userName").toString();

        inialization();

        DisplayRecieverInfo();

        sendMessagebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });

        fetchMessages();
    }

    private void fetchMessages(){
        RootRef.child("Messages").child(messageSenderId).child(messageRecieverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if(dataSnapshot.exists()){
                            Messages messages2 = dataSnapshot.getValue(Messages.class);
                            messagesList.add(messages2);
                            messagesAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void SendMessage(){
        updateUserStatus("online");
        String messageText = message.getText().toString();

        if(TextUtils.isEmpty(messageText)){
            Toast.makeText(this,"please write a message first......",Toast.LENGTH_SHORT).show();
        } else {
            String message_sender_ref = "Messages/" + messageSenderId + "/" + messageRecieverId;
            String message_reciever_ref = "Messages/" + messageRecieverId + "/" + messageSenderId;

            DatabaseReference user_message_key = RootRef.child("Messages").child(messageSenderId).child(messageRecieverId)
                    .push();
            String message_push_id = user_message_key.getKey();

            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            saveCurrentDate = currentDate.format(calFordDate.getTime());

            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm a");
            saveCurrentTime = currentTime.format(calFordDate.getTime());

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderId);

            Map messageBodyDetaile = new HashMap();
            messageBodyDetaile.put(message_sender_ref + "/"+ message_push_id , messageTextBody);
            messageBodyDetaile.put(message_reciever_ref + "/"+ message_push_id , messageTextBody);

            RootRef.updateChildren(messageBodyDetaile).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ChatActivity.this,"Message send Successfully",Toast.LENGTH_SHORT).show();
                        message.setText("");

                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(ChatActivity.this,"Error " + error ,Toast.LENGTH_SHORT).show();
                        message.setText("");
                    }
                }
            });

        }

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

        UsersRef.child(messageSenderId).child("userstate").updateChildren(currentStateMap);

    }

    private void DisplayRecieverInfo(){
        recieverName.setText(messageRecieverName);

        RootRef.child("Users").child(messageRecieverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){
                    final String profileImage = dataSnapshot.child("profileimage").getValue().toString();
                    final  String type = dataSnapshot.child("userstate").child("type").getValue().toString();
                    final  String lastDate = dataSnapshot.child("userstate").child("date").getValue().toString();
                    final  String lastTime = dataSnapshot.child("userstate").child("time").getValue().toString();

                    if(type.equals("online")){
                        userLastSeen.setText("online");
                    }
                    else {
                        userLastSeen.setText("last seen: " + lastTime + "  " + lastDate);
                    }
                    Picasso.with(ChatActivity.this).load(profileImage).placeholder(R.drawable.profile).into(receiverProfileImage);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void inialization(){
        chateToolbar = (Toolbar) findViewById(R.id.chat_bar_layout);
        setSupportActionBar(chateToolbar);

        ActionBar actipnBar = getSupportActionBar();
        actipnBar.setDisplayHomeAsUpEnabled(true);
        actipnBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater =  (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar,null);
        actipnBar.setCustomView(action_bar_view);

        recieverName = (TextView) findViewById(R.id.custom_profile_name);
        receiverProfileImage = (CircleImageView) findViewById(R.id.custom_profile_image);

        sendMessagebutton = (ImageButton) findViewById(R.id.send_image_button);
        sendImageFileButton = (ImageButton) findViewById(R.id.send_image_file_button);
        message = (EditText) findViewById(R.id.input_message);
        usersMessagesList = (RecyclerView) findViewById(R.id.masseges_list_users);
        userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);

        messagesAdapter = new MessagesAdapter(messagesList);
        linearLayoutManager = new LinearLayoutManager(this);
        usersMessagesList.setHasFixedSize(true);
        usersMessagesList.setLayoutManager(linearLayoutManager);
        usersMessagesList.setAdapter(messagesAdapter);

    }
}
