package com.example.locale.fragments;

import static com.example.locale.models.Constants.POSTS_FRAGMENT_TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.locale.R;
import com.example.locale.adapters.PostAdapter;
import com.example.locale.models.Post;
import com.example.locale.models.User;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class PostFragment extends Fragment {
    private User mUser;
    private RecyclerView mRvPosts;
    private ArrayList<Post> mPosts;
    private PostAdapter mPostAdapter;

    public PostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_posts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the list of posts and adapter
        mPosts = new ArrayList<>();
        mPostAdapter = new PostAdapter(getContext(), mPosts);

        // Recycler view set up: layout manager and the adapter
        mRvPosts = view.findViewById(R.id.rvPosts);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mRvPosts.setLayoutManager(linearLayoutManager);
        mRvPosts.setAdapter(mPostAdapter);
        queryPosts();
    }

    private void queryPosts() {
        // Specify what type of data we want to query
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // Limit query to latest 20 items
        query.setLimit(20);
        // Order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");
        // Start an asynchronous call for posts
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                // Check for errors
                if (e != null) {
                    Log.e(POSTS_FRAGMENT_TAG, "Issue with getting posts", e);
                    return;
                }

                // Save received posts to list and notify adapter of new data
                mPosts.addAll(posts);
                mPostAdapter.notifyDataSetChanged();
            }
        });
    }
}