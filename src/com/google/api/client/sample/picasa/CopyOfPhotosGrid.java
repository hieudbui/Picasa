package com.google.api.client.sample.picasa;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.sample.picasa.model.AlbumFeed;
import com.google.api.client.sample.picasa.model.PhotoEntry;
import com.google.api.client.sample.picasa.model.PicasaUrl;
import com.google.api.client.sample.picasa.model.Util;
import com.google.api.client.xml.atom.AtomParser;

/**
 * A grid that displays a set of framed photos.
 * 
 */
public class CopyOfPhotosGrid extends Activity
{
    private static final String TAG = "PhotosGrid";

    private static final int DIALOG_PROGRESS = 1;

    private static HttpTransport transport;

    private String authToken;
    private String albumLink;

    private GridView gridView;
    List<PhotoEntry> photos;
    final Handler getPhotos = new Handler();

    final Runnable updateGrid = new Runnable()
    {
        public void run()
        {
            updateGridInUI();
            dismissDialog(DIALOG_PROGRESS);
        }
    };

    public CopyOfPhotosGrid()
    {
        transport = GoogleTransport.create();
        GoogleHeaders headers = (GoogleHeaders) transport.defaultHeaders;
        headers.setApplicationName("Google-PicasaAndroidAample/1.0");
        headers.gdataVersion = "2";
        AtomParser parser = new AtomParser();
        parser.namespaceDictionary = Util.NAMESPACE_DICTIONARY;
        transport.addParser(parser);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "on create called " + this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.photos_grid);
        gridView = (GridView) findViewById(R.id.myGrid);

        Intent intent = getIntent();
        authToken = intent.getStringExtra("authToken");
        albumLink = intent.getStringExtra("albumLink");

        showDialog(DIALOG_PROGRESS);

        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    executeRefreshPhotos();
                    getPhotos.post(updateGrid);
                }
                catch (Exception e)
                {
                    handleException(e);
                }
            }
        }.start();
    }

    public void onStart()
    {
        super.onStart();
        Log.d(TAG, "on start called");
    }

    public void onRestart()
    {
        super.onRestart();
        Log.d(TAG, "on restart called");
    }

    public void onResume()
    {
        super.onResume();
        Log.d(TAG, "on resumes called");
    }

    public void onPause()
    {
        super.onResume();
        Log.d(TAG, "on pause called");
    }

    private void handleException(Exception e)
    {
        // TODO
        // handle exception
        e.printStackTrace();
    }

    private void updateGridInUI()
    {
        ImageAdapter adapter = new ImageAdapter(this, photos);
        // ImageAdapter2 adapter = new ImageAdapter2(this);
        gridView.setAdapter(adapter);
    }

    private void executeRefreshPhotos() throws IOException
    {
        ((GoogleHeaders) transport.defaultHeaders).setGoogleLogin(authToken);
        AlbumFeed albumFeed = AlbumFeed.executeGet(transport, new PicasaUrl(albumLink));
        photos = albumFeed.photos;

        // Log.d(TAG, "photos size=" + photos.size());
        // TODO
        // implement image processing
        final AtomicInteger counter = new AtomicInteger(0);

        for (final PhotoEntry photoEntry : photos)
        {
            String url = photoEntry.mediaGroup.getSmallThumbNail().url;
            new Thread(url)
            {
                @Override
                public void run()
                {
                    Bitmap bitmap = getImageBitmap(this.getName());
                    photoEntry.addBitmap(this.getName(), bitmap);
                    // Log.d(TAG, Calendar.getInstance().getTimeInMillis() + " photoEntry: "
                    // + photoEntry.getFeedLink() + " bitmap name: " + this.getName());
                    // Log.d(TAG, "count is at " + counter.get());
                    counter.incrementAndGet();
                }
            }.start();

        }

        while (true)
        {
            if (counter.get() == (photos.size()))
            {
                break;
            }
            // Log.d(TAG, "current count is at " + counter.get());
        }
        // Log.d(TAG, Calendar.getInstance().getTimeInMillis()
        // + " executeRefreshPhotos where final counter=" + counter.get());
    }

    private Bitmap getImageBitmap(String url)
    {
        Bitmap bm = null;
        InputStream is = null;
        BufferedInputStream bis = null;
        try
        {
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.setConnectTimeout(1000);
            conn.connect();
            is = conn.getInputStream();
            bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            // if (bm == null)
            // {
            // Log.d(TAG, "unable to get bitmap for url: " + url);
            // }
            // else
            // {
            // Log.d(TAG, Calendar.getInstance().getTimeInMillis() + " url: " + url + " bitmap: "
            // + bm.toString());
            // }
        }
        catch (IOException e)
        {
            // TODO
            // handle this
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (bis != null)
                {
                    bis.close();
                }

                if (is != null)
                {
                    is.close();
                }
            }
            catch (IOException e)
            {
                // TODO
                // handle this
                e.printStackTrace();
            }
        }

        return bm;
    }

    @Override
    protected Dialog onCreateDialog(int id)
    {
        Dialog progressDialog;
        switch (id)
        {
        case DIALOG_PROGRESS:
            progressDialog = new ProgressDialog(CopyOfPhotosGrid.this);
            progressDialog.setTitle(R.string.loading);
            ((ProgressDialog) progressDialog).setProgressStyle(ProgressDialog.STYLE_SPINNER);
            break;
        default:
            progressDialog = null;

        }
        return progressDialog;
    }

    public class ImageAdapter extends BaseAdapter
    {
        private Context mContext;
        private List<PhotoEntry> photos;

        public ImageAdapter(Context c, List<PhotoEntry> photos)
        {
            mContext = c;
            this.photos = photos;
        }

        public int getCount()
        {
            return photos.size();
        }

        public Object getItem(int position)
        {
            return position;
        }

        public long getItemId(int position)
        {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            Log.d(TAG,"position: " + position + " view: " + convertView);
            ImageView imageView;
            if (convertView == null)
            {
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(60, 60));
                imageView.setAdjustViewBounds(false);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(4, 4, 4, 4);
            }
            else
            {
                imageView = (ImageView) convertView;
            }

            PhotoEntry photoEntry = photos.get(position);
            String url = photoEntry.mediaGroup.getSmallThumbNail().url;
            Bitmap bitmap = photoEntry.getBitmap(url);
            if (bitmap == null)
            {
                Log.d(TAG, Calendar.getInstance().getTimeInMillis() + " position: " + position
                        + " url: " + url);
            }
            // Log.d(TAG, Calendar.getInstance().getTimeInMillis() + " position: " + position
            // + " url: " + url + " bitmap: " + bitmap.describeContents());
            imageView.setImageBitmap(bitmap);
            imageView.setTag(photoEntry.mediaGroup.getLargeThumbNail().url);
            imageView.setOnClickListener(new View.OnClickListener()
            {

                public void onClick(View view)
                {
                    Log.d(TAG, "view id=" + view.getId());
                    String url = (String) view.getTag();
                    Intent intent = new Intent(CopyOfPhotosGrid.this, PhotoView.class);
                    // TODO
                    // can we pass the photos list?
                    //we could serialized it to json
                    //and deserialized it?
                    intent.putExtra("url", url);
                    startActivity(intent);
                }
            });
            return imageView;
        }
    }

    public class ImageAdapter2 extends BaseAdapter
    {
        public ImageAdapter2(Context c)
        {
            mContext = c;
        }

        public int getCount()
        {
            return mThumbIds.length;
        }

        public Object getItem(int position)
        {
            return position;
        }

        public long getItemId(int position)
        {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            ImageView imageView;
            if (convertView == null)
            {
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(60, 60));
                imageView.setAdjustViewBounds(false);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(4, 4, 4, 4);
            }
            else
            {
                imageView = (ImageView) convertView;
            }

            imageView.setImageResource(mThumbIds[position]);
            imageView.setOnClickListener(new View.OnClickListener()
            {

                public void onClick(View view)
                {
                    Log.d(TAG, "view id=" + view.getId());

                }
            });
            return imageView;
        }

        private Context mContext;

        private Integer[] mThumbIds = {R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
                R.drawable.sample_thumb_2, R.drawable.sample_thumb_3, R.drawable.sample_thumb_4,
                R.drawable.sample_thumb_5, R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
                R.drawable.sample_thumb_0, R.drawable.sample_thumb_1, R.drawable.sample_thumb_2,
                R.drawable.sample_thumb_3, R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
                R.drawable.sample_thumb_6, R.drawable.sample_thumb_7, R.drawable.sample_thumb_0,
                R.drawable.sample_thumb_1, R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
                R.drawable.sample_thumb_4, R.drawable.sample_thumb_5, R.drawable.sample_thumb_6,
                R.drawable.sample_thumb_7, R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
                R.drawable.sample_thumb_2, R.drawable.sample_thumb_3, R.drawable.sample_thumb_4,
                R.drawable.sample_thumb_5, R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
                R.drawable.sample_thumb_0, R.drawable.sample_thumb_1, R.drawable.sample_thumb_2,
                R.drawable.sample_thumb_3, R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
                R.drawable.sample_thumb_6, R.drawable.sample_thumb_7, R.drawable.sample_thumb_0,
                R.drawable.sample_thumb_1, R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
                R.drawable.sample_thumb_4, R.drawable.sample_thumb_5, R.drawable.sample_thumb_6,
                R.drawable.sample_thumb_7, R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
                R.drawable.sample_thumb_2, R.drawable.sample_thumb_3, R.drawable.sample_thumb_4,
                R.drawable.sample_thumb_5, R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
                R.drawable.sample_thumb_0, R.drawable.sample_thumb_1, R.drawable.sample_thumb_2,
                R.drawable.sample_thumb_3, R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
                R.drawable.sample_thumb_6, R.drawable.sample_thumb_7, R.drawable.sample_thumb_0,
                R.drawable.sample_thumb_1, R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
                R.drawable.sample_thumb_4, R.drawable.sample_thumb_5, R.drawable.sample_thumb_6,
                R.drawable.sample_thumb_7, R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
                R.drawable.sample_thumb_2, R.drawable.sample_thumb_3, R.drawable.sample_thumb_4,
                R.drawable.sample_thumb_5, R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
                R.drawable.sample_thumb_0, R.drawable.sample_thumb_1, R.drawable.sample_thumb_2,
                R.drawable.sample_thumb_3, R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
                R.drawable.sample_thumb_6, R.drawable.sample_thumb_7, R.drawable.sample_thumb_0,
                R.drawable.sample_thumb_1, R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
                R.drawable.sample_thumb_4, R.drawable.sample_thumb_5, R.drawable.sample_thumb_6,
                R.drawable.sample_thumb_7, R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
                R.drawable.sample_thumb_2, R.drawable.sample_thumb_3, R.drawable.sample_thumb_4,
                R.drawable.sample_thumb_5, R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
                R.drawable.sample_thumb_0, R.drawable.sample_thumb_1, R.drawable.sample_thumb_2,
                R.drawable.sample_thumb_3, R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
                R.drawable.sample_thumb_6, R.drawable.sample_thumb_7, R.drawable.sample_thumb_0,
                R.drawable.sample_thumb_1, R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
                R.drawable.sample_thumb_4, R.drawable.sample_thumb_5, R.drawable.sample_thumb_6,
                R.drawable.sample_thumb_7, R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
                R.drawable.sample_thumb_2, R.drawable.sample_thumb_3, R.drawable.sample_thumb_4,
                R.drawable.sample_thumb_5, R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
                R.drawable.sample_thumb_0, R.drawable.sample_thumb_1, R.drawable.sample_thumb_2,
                R.drawable.sample_thumb_3, R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
                R.drawable.sample_thumb_6, R.drawable.sample_thumb_7, R.drawable.sample_thumb_0,
                R.drawable.sample_thumb_1, R.drawable.sample_thumb_2, R.drawable.sample_thumb_3,
                R.drawable.sample_thumb_4, R.drawable.sample_thumb_5, R.drawable.sample_thumb_6,
                R.drawable.sample_thumb_7, R.drawable.sample_thumb_0, R.drawable.sample_thumb_1,
                R.drawable.sample_thumb_2, R.drawable.sample_thumb_3, R.drawable.sample_thumb_4,
                R.drawable.sample_thumb_5, R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,
                R.drawable.sample_thumb_0, R.drawable.sample_thumb_1, R.drawable.sample_thumb_2,
                R.drawable.sample_thumb_3, R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
                R.drawable.sample_thumb_6, R.drawable.sample_thumb_7,};
    }

}
