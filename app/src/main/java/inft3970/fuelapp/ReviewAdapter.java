package inft3970.fuelapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class: ReviewAdapter
 * Author: Shane
 * Purpose: This class handles the Recycler View for displaying each instance of the Review object.
 * Creation Date: 02-Nov-17
 * Modification Date: 05-Nov-17
 */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private static final String TAG = ReviewAdapter.class.getSimpleName();
    ArrayList<HashMap<String, String>> reviewList;
    Context context = App.getContext();

    /**
     * Method: ReviewAdapter
     * Purpose: Acts as the constructor of this class. Points the Review List object to the list
     * created in StationActivity which is filled out.
     * Returns: None
     */
    ReviewAdapter(ArrayList reviewList) {
        this.reviewList = reviewList;
    }

    /**
     * Method: getItemCount
     * Purpose: Retrieves the length of the list being used. This is called by the Recycler View itself
     * Returns: The size of the list as an integer
     */
    @Override
    public int getItemCount() { return reviewList.size();}

    /**
     * Method: onCreateViewHolder
     * Purpose: Creates the layout for the recycler to populate with data, and calls the method to create each Holder
     * Returns: The constructed holder
     */
    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.review_card, viewGroup, false);
        ReviewViewHolder rvh = new ReviewViewHolder(v);
        return rvh;
    }

    /**
     * Class: ReviewViewHolder
     * Author: Shane
     * Purpose: This class creates each individual instance of the data container for display, and initialises the fields
     * Creation Date: 02-Nov-17
     * Modification Date: 05-Nov-17
     */
    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePhoto;
        TextView authorName;
        TextView datePosted;
        TextView reviewText;
        ImageView[] ratingStars;

        ReviewViewHolder(View itemView) {
            super(itemView);
            profilePhoto = (ImageView)itemView.findViewById(R.id.profile_img);
            authorName = (TextView)itemView.findViewById(R.id.author_name_txt);
            datePosted = (TextView)itemView.findViewById(R.id.posted_date_txt);
            reviewText = (TextView)itemView.findViewById(R.id.review_txt);
            ratingStars = new ImageView[5];
            ratingStars[0] = (ImageView)itemView.findViewById(R.id.rating_star_1);
            ratingStars[1] = (ImageView)itemView.findViewById(R.id.rating_star_2);
            ratingStars[2] = (ImageView)itemView.findViewById(R.id.rating_star_3);
            ratingStars[3] = (ImageView)itemView.findViewById(R.id.rating_star_4);
            ratingStars[4] = (ImageView)itemView.findViewById(R.id.rating_star_5);
        }
    }

    /**
     * Method: onBindViewHolder
     * Purpose: Takes in the Holder object and populates it with data based on the integer input as
     * a reference to the particular series of data.
     * Returns: None
     */
    @Override
    public void onBindViewHolder(ReviewViewHolder reviewViewHolder, int i) {
        for (ImageView star:reviewViewHolder.ratingStars
             ) {
            star.setImageDrawable(context.getResources().getDrawable(R.drawable.star_outline));
        }
        if(reviewList.get(i).get("photoUrl") != null) {  //Handles the url of the user's profile photo who posted the review
            LoadImage(reviewList.get(i).get("photoUrl"), reviewViewHolder.profilePhoto);
        }
        if(reviewList.get(i).get("authorName") != null) {  //Handles the name of the user who posted the review
            reviewViewHolder.authorName.setText(reviewList.get(i).get("authorName"));
        }
        if(reviewList.get(i).get("time") != null) {  //Handles the time of the posted review
            reviewViewHolder.datePosted.setText(reviewList.get(i).get("time"));
        }
        if(reviewList.get(i).get("reviewText") != null) {  //Handles the text of the review
            reviewViewHolder.reviewText.setText(reviewList.get(i).get("reviewText"));
        }
        if(reviewList.get(i).get("rating") != null) {  //Handles the rating of the review, and draws the appropriate number of stars
            Drawable fullStar = context.getResources().getDrawable(R.drawable.star_full);
            Drawable halfStar = context.getResources().getDrawable(R.drawable.star_half);
            Double ratingValue = Double.parseDouble(reviewList.get(i).get("rating"));
            if(ratingValue != Math.floor(ratingValue)) {
                reviewViewHolder.ratingStars[(int)Math.floor(ratingValue)].setImageDrawable(halfStar);
            }
            for(int count = 0; count < Math.floor(ratingValue);count++) {
                reviewViewHolder.ratingStars[count].setImageDrawable(fullStar);
            }
        }
    }

    /**
     * Method: onAttachedToRecyclerView
     * Purpose: Automatically generated method which is called on by the Recycler as it starts observing this Adapter
     * Returns: None
     */
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    /**
     * Method: loadImage
     * Purpose: Loads the image of the user profile
     * Returns: None
     */
    @SuppressLint("StaticFieldLeak")
    public void LoadImage(final String url, final ImageView image) {
        final Bitmap[] imageBitmap = new Bitmap[1];
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void...params) {
                try {
                    InputStream is = new URL(url).openStream();
                    imageBitmap[0] = BitmapFactory.decodeStream(is);
                } catch (Exception e) {

                }
                return null;
            }

            protected void onPostExecute(Void result) {
                if(imageBitmap[0] != null) {
                    image.setImageBitmap(imageBitmap[0]);
                }
            }
        }.execute();
    }
}
