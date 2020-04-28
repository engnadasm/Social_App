package com.example.socialnetwork;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView myPostsList;
    private FirebaseAuth mAuth;
    private DatabaseReference PostsRef ;
    private String currentUserID;
    private DatabaseReference UsersRef, likesRef;
    Boolean likeChecker = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        mToolbar = (Toolbar) findViewById(R.id.my_posts_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Posts");
        myPostsList = (RecyclerView) findViewById(R.id.my_all_posts_list);
        myPostsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myPostsList.setLayoutManager(linearLayoutManager);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        DisplayMyAllPosts();
    }

    private void DisplayMyAllPosts(){
        Query myPostsQuery = PostsRef.orderByChild("uid").startAt(currentUserID).endAt(currentUserID + "\uf8ff");

        FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>().setQuery(myPostsQuery, Posts.class).build();
        FirebaseRecyclerAdapter<Posts, MyPostsViewHolder> adapter = new FirebaseRecyclerAdapter<Posts, MyPostsViewHolder>(options) {

            @NonNull
            @Override
            public MyPostsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_posts_layout, viewGroup, false);
                MyPostsViewHolder viewHolder = new MyPostsViewHolder(view);
                return viewHolder;
            }

            @Override
            protected void onBindViewHolder(@NonNull MyPostsViewHolder holder, int position, @NonNull Posts model) {
                final String postKek = getRef(position).getKey();

                holder.username.setText(model.getFullname());
                holder.time.setText(" " +model.getTime());
                holder.date.setText(" "+model.getDate());
                holder.description.setText(model.getDescription());
                holder.setLikeButtonStatus(postKek);

                Picasso.with(MyPostsActivity.this).load(model.getProfileimage()).into(holder.user_post_image);
                Picasso.with(MyPostsActivity.this).load(model.getPostimage()).into(holder.postImage);

                holder.itemView.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        Intent clickPostIntent = new Intent(MyPostsActivity.this, ClickPostActivity.class);
                        clickPostIntent.putExtra("postKey", postKek);
                        startActivity(clickPostIntent);

                    }
                });

                holder.commetPostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentsIntent = new Intent(MyPostsActivity.this, CommetsActivity.class);
                        commentsIntent.putExtra("postKey", postKek);
                        startActivity(commentsIntent);
                    }
                });

                holder.likePostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        likeChecker = true;
                        likesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(likeChecker.equals(true)){
                                    if(dataSnapshot.child(postKek).hasChild(currentUserID)){
                                        likesRef.child(postKek).child(currentUserID).removeValue();
                                        likeChecker = false;

                                    } else {
                                        likesRef.child(postKek).child(currentUserID).setValue(true);
                                        likeChecker = false;

                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }
        };

        myPostsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class MyPostsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        TextView username, date, time, description;
        CircleImageView user_post_image;
        ImageView postImage;

        ImageButton likePostButton, commetPostButton;
        TextView displayNumerOfLikes;
        int countLikes = 0;
        String currentUserId;
        DatabaseReference likeRef;

        public MyPostsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            username = itemView.findViewById(R.id.post_user_name);
            date = itemView.findViewById(R.id.post_date);
            time = itemView.findViewById(R.id.post_time);
            description = itemView.findViewById(R.id.post_description);
            postImage = itemView.findViewById(R.id.post_image);
            user_post_image = itemView.findViewById(R.id.post_profile_image);
            likePostButton = itemView.findViewById(R.id.like_button);
            commetPostButton = itemView.findViewById(R.id.comment_buuton);
            displayNumerOfLikes = itemView.findViewById(R.id.display_number_likes);
            likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        }
        public void setLikeButtonStatus(final String postKek) {
            likeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(postKek).hasChild(currentUserId)){
                        countLikes = (int) dataSnapshot.child(postKek).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.like);
                        displayNumerOfLikes.setText(Integer.toString(countLikes));

                    } else {
                        countLikes = (int) dataSnapshot.child(postKek).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.dislike);
                        displayNumerOfLikes.setText(Integer.toString(countLikes));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
    }
}
