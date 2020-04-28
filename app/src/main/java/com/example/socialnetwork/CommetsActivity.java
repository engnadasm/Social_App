package com.example.socialnetwork;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommetsActivity extends AppCompatActivity {

    private ImageButton postCommetButton;
    private EditText  commetInputText;
    private RecyclerView commentsList;

    private String post_Key;
    private DatabaseReference usersRef;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference  PostsRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commets);

        post_Key = getIntent().getExtras().get("postKey").toString();

        postCommetButton = (ImageButton) findViewById(R.id.postComment_button);
        commetInputText = (EditText) findViewById(R.id.commentInput);

        commentsList = (RecyclerView) findViewById(R.id.comments_list);
        commentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        commentsList.setLayoutManager(linearLayoutManager);



        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(post_Key).child("Comments");


        postCommetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String userName = dataSnapshot.child("username").getValue().toString();
                            validateCommet(userName);
                            commetInputText.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Comments> options = new FirebaseRecyclerOptions.Builder<Comments>().setQuery(PostsRef, Comments.class).build();
        FirebaseRecyclerAdapter<Comments, CommentsViewHolder> adapter = new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CommentsViewHolder holder, int position, @NonNull Comments model) {

                holder.username.setText(model.getUsername());
                holder.comment.setText(model.getComment());
                holder.date.setText(model.getDate());
                holder.time.setText(model.getTime());


            }

            @NonNull
            @Override
            public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_comments_layout, viewGroup, false);
                CommentsViewHolder viewHolder = new CommentsViewHolder(view);
                return viewHolder;
            }
        };

        commentsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder {
        TextView comment, date, time, username;


        public CommentsViewHolder(@NonNull View itemView) {


            super(itemView);


            username = itemView.findViewById(R.id.comment_username);
            comment = itemView.findViewById(R.id.comment_text);
            date = itemView.findViewById(R.id.comment_date);
            time = itemView.findViewById(R.id.comment_time);


        }
    }

    private void validateCommet(String userName) {
        String commentText = commetInputText.getText().toString();
        if(TextUtils.isEmpty(commentText)){
            Toast.makeText(this,"please write text to comments....",Toast.LENGTH_SHORT).show();
        } else{
            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            final String saveCurrentDate = currentDate.format(calFordDate.getTime());

            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm a");
            final String saveCurrentTime = currentTime.format(calFordDate.getTime());

            final String RandomKey = currentUserID + saveCurrentDate + saveCurrentTime;
            HashMap commentMap = new HashMap();
            commentMap.put("uid",currentUserID);
            commentMap.put("comment",commentText);
            commentMap.put("date",saveCurrentDate);
            commentMap.put("time",saveCurrentTime);
            commentMap.put("username",userName);

            PostsRef.child(RandomKey).updateChildren(commentMap).addOnCompleteListener(this,new OnCompleteListener(){
                @Override
                public void onComplete(Task task){
                    if(task.isSuccessful()){
                        Toast.makeText(CommetsActivity.this,"you have commeted successful....",Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CommetsActivity.this,"Error ocurred, Try again....",Toast.LENGTH_SHORT).show();
                    }
                }
            });




        }
    }
}
