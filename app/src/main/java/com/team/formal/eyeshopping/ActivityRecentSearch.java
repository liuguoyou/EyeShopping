package com.team.formal.eyeshopping;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static com.team.formal.eyeshopping.MainActivity.DBInstance;
import static com.team.formal.eyeshopping.MainActivity.DBName;

/**
 * Created by NaJM on 2017. 6. 4..
 */

public class ActivityRecentSearch extends AppCompatActivity {

    // Grid View 전역 변수
    GridView gridView;
    GridViewAdapter gridViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        DBInstance = new DBHelper(this, DBName, null, 1);

        DBInstance.insertSearchedProduct("asd", 123, 1, "http://shopping.phinf.naver.net/main_1144437/11444373299.jpg?type=f140");
        DBInstance.insertSearchedProduct("asd", 123, 1, "http://shopping.phinf.naver.net/main_1144437/11444373299.jpg?type=f140");


        final Cursor c = DBInstance.getTuples("searched_product");
        c.moveToNext();
        final int count = c.getCount();

        new AsyncTask<Object, Void, ArrayList<ActivityRecentSearch.Results_GridItem>>() {
            @Override
            protected ArrayList<ActivityRecentSearch.Results_GridItem> doInBackground(Object... params) {
                ArrayList<ActivityRecentSearch.Results_GridItem> gridItems = new ArrayList<>();
                HttpURLConnection connection = null;
                for(int i=0; i<count; i++) {
                    String keyword;
                    Bitmap bitmap;
                    try {
                        URL url = new URL(c.getString(4));
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.connect();
                        InputStream input = connection.getInputStream();
                        BitmapFactory.Options op = new BitmapFactory.Options();
                        op.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        bitmap = BitmapFactory.decodeStream(input, null, op);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    } finally {
                        if (connection != null) connection.disconnect();
                    }
                    keyword = c.getString(1);
                    gridItems.add(new Results_GridItem(bitmap,keyword));
                    if( c.isLast()) { break; }
                    else { c.moveToNext(); }
                }

                return gridItems;
            }

            protected void onPostExecute(ArrayList<ActivityRecentSearch.Results_GridItem> gridItems)
            {
                gridView = (GridView)findViewById(R.id.recent_grid_view);
                gridViewAdapter = new GridViewAdapter(getApplicationContext(), gridItems);
                gridView.setAdapter(gridViewAdapter);
            }
        }.execute();
    }

    /*
        그리드뷰 어댑터, 그리드 뷰를 inflate하여 객체화 한다
     */
    private class GridViewAdapter extends BaseAdapter {
        Context context;
        ArrayList<ActivityRecentSearch.Results_GridItem> gridItems;

        private GridViewAdapter(Context context, ArrayList<ActivityRecentSearch.Results_GridItem> gridItems) {
            this.context = context;
            this.gridItems = gridItems;
        }

        public int getCount() {
            return gridItems.size();
        }

        public ActivityRecentSearch.Results_GridItem getItem(int position) {
            return gridItems.get(position);
        }

        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ActivityRecentSearch.Results_GridView gridView;

            if(convertView == null) {
                gridView = new ActivityRecentSearch.Results_GridView(this.context, this.gridItems.get(position));
            } else {
                gridView = (ActivityRecentSearch.Results_GridView) convertView;
            }

            return gridView;
        }
    }

    /*
        그리드뷰 아이템 그리드 뷰에 들어갈 정보를 담고 있다
     */
    private class Results_GridItem {
        Bitmap bitmap;
        String keyword;
        public Results_GridItem(Bitmap bitmap, String keyword) {
            this.bitmap=bitmap;
            this.keyword=keyword;
        }
    }

    /*
        그리드뷰 뷰, xml과 연결된 레이아웃 클래스
     */
    private class Results_GridView extends LinearLayout {
        private ViewGroup vGroup;
        private ImageView thumbView;
        private TextView strView;

        public Results_GridView(Context context, final ActivityRecentSearch.Results_GridItem aItem) {
            super(context);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.grid_recent_item, this, true);

            vGroup = (ViewGroup) findViewById(R.id.grid_recent_item_list_view);
            thumbView = (ImageView) findViewById(R.id.recent_product_thumbnail);
            thumbView.setImageBitmap(aItem.bitmap);
            strView = (TextView) findViewById(R.id.recent_str);
            strView.setText(String.valueOf(aItem.keyword));

        }
    }

}
