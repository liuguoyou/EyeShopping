package com.team.formal.eyeshopping;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import static com.team.formal.eyeshopping.MainActivity.DBInstance;
import static com.team.formal.eyeshopping.MainActivity.DBName;
import static com.team.formal.eyeshopping.R.drawable.marmont_bag;

/**
 * Created by NaJM on 2017. 6. 4..
 */

public class ActivityRecentSearch extends Activity {

    // Grid View 전역 변수
    GridView gridView;
    GridViewAdapter gridViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_search);

        DBInstance = new DBHelper(this, DBName, null, 1);
        Cursor c = DBInstance.getTuples("searched_product");
        while(c.moveToNext()){
            Log.i("combination keykord",c.getString(0));
            Log.i("selected_image_url",c.getString(1));
        }


        Drawable im = getDrawable(marmont_bag);
        String str = "Marmont";

        gridView = (GridView)findViewById(R.id.recent_grid_view);
        ArrayList<Results_GridItem> items = new ArrayList<>();

        for(int i=0; i<20; i++) {
            items.add(new Results_GridItem(im,str));
        }
        gridViewAdapter = new GridViewAdapter(getApplicationContext(), items);
        gridView.setAdapter(gridViewAdapter);

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
        private Drawable im;
        private String str;
        public Results_GridItem(Drawable im, String str) {
            this.im=im;
            this.str=str;
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
            thumbView.setImageDrawable(aItem.im);
            strView = (TextView) findViewById(R.id.recent_str);
            strView.setText(String.valueOf(aItem.str));

        }
    }

}
