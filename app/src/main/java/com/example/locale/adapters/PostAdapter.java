/*
Creates the item views for each post stored in the Parse database. Each post has the user's first
name, last name, username, the photo the user uploaded with their review (if they uploaded a photo),
and the review the user left for the location.
 */

package com.example.locale.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.locale.R;
import com.example.locale.models.Post;

import java.util.ArrayList;
import java.util.Base64;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder>{
    private Context mContext;
    private ArrayList<Post> mPosts;
    private Post mPost;

    private TextView mTvFirstNameLastName;
    private TextView mTvUsername;
    private TextView mTvVisitedLocationName;
    private TextView mTvReview;
    private ImageView ivUIploadedImage;

    public PostAdapter(Context context, ArrayList<Post> posts){
        this.mContext = context;
        this.mPosts = posts;
    }

    @NonNull
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.ViewHolder holder, int position) {
        mPost = mPosts.get(position);
        holder.bind(mPost);
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ViewHolder(View itemView) {
            super(itemView);
        }

        public void bind(Post post) {
            // Find IDs
            mTvFirstNameLastName = itemView.findViewById(R.id.tvFirstLastNamePost);
            mTvUsername = itemView.findViewById(R.id.tvUsernamePost);
            mTvVisitedLocationName = itemView.findViewById(R.id.tvVisitedLocationPost);
            ivUIploadedImage = itemView.findViewById(R.id.ivUploadedImagePost);
            mTvReview = itemView.findViewById(R.id.tvReviewPost);

            // Set text and image views
            mTvFirstNameLastName.setText(post.getFirstName() + " " + post.getLastName());
            mTvUsername.setText("@" + post.getUsername());
            mTvVisitedLocationName.setText(post.getFirstName() + " visited " + post.getPlaceName() + "!");
            mTvReview.setText(post.getReview());

            String byteArrayString = post.getPhoto();

            // If the post has no photo, do not set one
            if (byteArrayString != null){
                byte[] byteArray = byteArrayString.getBytes();
                if (byteArray.length != 0){
                    byte[] bitmapData = Base64.getDecoder().decode(byteArray);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);

                    RequestOptions requestOptions = new RequestOptions();
                    requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(10));
                    Glide.with(mContext).asBitmap().load(bitmap).apply(requestOptions.override(600, 300)).into(ivUIploadedImage);
                }
            }
        }
    }

    public void clear() {
        mPosts.clear();
        notifyDataSetChanged();
    }
}
