package org.techtown.diary;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.fragment.app.Fragment;

public class Fragment3 extends Fragment {

 private static final String TAG = "Fragment3";
 Context context;

    PieChart chart;
    BarChart chart2;
    LineChart chart3;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment3, container, false);

     initUI(rootView);

     loadStatData();

     return rootView;
 }

 //세 개의 그래프를 위해 데이터베이스로부터 기분 데이터를 조회한다.
    private void loadStatData() {
        NoteDatabase database = NoteDatabase.getInstance(context);

        //기분별 비율

        String sql = "select mood " +
                "  , count(mood) " +
                "from " + NoteDatabase.TABLE_NOTE + " " +
                "where create_date > '" + getMonthBefore(1) + "' " +
                "  and create_date < '" + getTomorrow() + "' " +
                "group by mood";

        Cursor cursor = database.rawQuery(sql);
       int recordCount = cursor.getCount();
       AppConstants.println("recordCount : " + recordCount);

        HashMap<String, Integer> dataHash1 = new HashMap<String, Integer>();
        for (int i = 0; i < recordCount; i++) {
            cursor.moveToNext();

            String moodName = cursor.getString(0);
            int moodCount = cursor.getInt(1);

            AppConstants.println("#" + i + "->" + moodName + "," + moodCount);
            dataHash1.put(moodName, moodCount);
        }

        setData1(dataHash1);

        //요일별 비율

        sql = "select strftime('%w', create_date) " +
                "  , avg(mood) " +
                "from " + NoteDatabase.TABLE_NOTE + " " +
                "where create_date > '" + getMonthBefore(1) + "' " +
                "  and create_date < '" + getTomorrow() + "' " +
                "group by strftime('%w', create_date)";

        cursor = database.rawQuery(sql);
        recordCount = cursor.getCount();
        AppConstants.println("recordCount : " + recordCount);

        HashMap<String,Integer> dataHash2 = new HashMap<String,Integer>();
        for (int i = 0; i < recordCount; i++) {
            cursor.moveToNext();

            String weekDay = cursor.getString(0);
            int moodCount = cursor.getInt(1);

            AppConstants.println("#" + i + " -> " + weekDay + ", " + moodCount);
            dataHash2.put(weekDay, moodCount);
        }

        setData2(dataHash2);

        //기분 변화

        sql = "select strftime('%Y-%m-%d', create_date) " +
                "  , avg(cast(mood as real)) " +
                "from " + NoteDatabase.TABLE_NOTE + " " +
                "where create_date > '" + getDayBefore(7) + "' " +
                "  and create_date < '" + getTomorrow() + "' " +
                "group by strftime('%Y-%m-%d', create_date)";

        cursor = database.rawQuery(sql);
        recordCount = cursor.getCount();
        AppConstants.println("recordCount : " + recordCount);

        HashMap<String,Integer> recordsHash = new HashMap<String,Integer>();
        for (int i = 0; i < recordCount; i++) {
            cursor.moveToNext();

            String monthDate = cursor.getString(0);
            int moodCount = cursor.getInt(1);

            AppConstants.println("#" + i + " -> " + monthDate + ", " + moodCount);
            recordsHash.put(monthDate, moodCount);
        }

        ArrayList<Float> dataKeys3 = new ArrayList<Float>();
        ArrayList<Integer> dataValues3 = new ArrayList<Integer>();

        Date todayDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(todayDate);
        cal.add(java.util.Calendar.DAY_OF_MONTH, -7);

        for (int i = 0; i < 7; i++) {
            cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
            String monthDate = AppConstants.dateFormat5.format(cal.getTime());
            Object moodCount = recordsHash.get(monthDate);

            dataKeys3.add((i-6) * 24.0f);
            if (moodCount == null) {
                dataValues3.add(0);
            } else {
                dataValues3.add((Integer)moodCount);
            }

            AppConstants.println("#" + i + " -> " + monthDate + ", " + moodCount);
        }

        setData3(dataKeys3, dataValues3);

    }

    private String getDayBefore(int amount) {
        Date todayDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(todayDate);
        cal.add(Calendar.MONTH, (amount * -1));
        return AppConstants.dateFormat5.format(cal.getTime());
    }

    private String getTomorrow() {

        Date todayDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(todayDate);
        cal.add(Calendar.DAY_OF_MONTH, 1);

        return AppConstants.dateFormat5.format(cal.getTime());
    }

    private String getMonthBefore(int amount) {
        Date todayDate  = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(todayDate);
        cal.add(Calendar.MONTH, (amount * -1));

        return AppConstants.dateFormat5.format(cal.getTime());
    }

    private void initUI(ViewGroup rootView) {

        chart = rootView.findViewById(R.id.chart1);
        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);

        chart.setCenterText(getResources().getString(R.string.graph1_title));

        chart.setTransparentCircleColor(Color.WHITE);
        chart.setTransparentCircleAlpha(110);

        chart.setHoleRadius(58f);
        chart.setTransparentCircleRadius(61f);

        chart.setDrawCenterText(true);

        chart.setHighlightPerTapEnabled(true);

        Legend legend1 = chart.getLegend();
        legend1.setEnabled(false);

        chart.setEntryLabelColor(Color.WHITE);
        chart.setEntryLabelTextSize(12f);




        chart2 = rootView.findViewById(R.id.chart2);
        chart2.setDrawValueAboveBar(true);

        chart2.getDescription().setEnabled(false);
        chart2.setDrawGridBackground(false);

        XAxis xAxis = chart2.getXAxis();
        xAxis.setEnabled(false);

        YAxis leftAxis = chart2.getAxisLeft();
        leftAxis.setLabelCount(6, false);
        leftAxis.setAxisMinimum(0.0f);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setGranularity(1f);

        YAxis rightAxis = chart2.getAxisRight();
        rightAxis.setEnabled(false);

        Legend legend2 = chart2.getLegend();
        legend2.setEnabled(false);

        chart2.animateXY(1500, 1500);




        chart3 = rootView.findViewById(R.id.chart3);

        chart3.getDescription().setEnabled(false);
        chart3.setDrawGridBackground(false);

        chart3.setBackgroundColor(Color.WHITE);
        chart3.setViewPortOffsets(0, 0, 0, 0);

        Legend legend3 = chart3.getLegend();
        legend3.setEnabled(false);

        XAxis xAxis3 = chart3.getXAxis();
        xAxis3.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        xAxis3.setTextSize(10f);
        xAxis3.setTextColor(Color.WHITE);
        xAxis3.setDrawAxisLine(false);
        xAxis3.setDrawGridLines(true);
        xAxis3.setTextColor(Color.rgb(255, 192, 56));
        xAxis3.setCenterAxisLabels(true);
        xAxis3.setGranularity(1f);



        xAxis3.setValueFormatter(new ValueFormatter() {

            private final SimpleDateFormat mFormat = new SimpleDateFormat("MM-DD", Locale.KOREA);

            @Override
            public String getFormattedValue(float value) {

                long millis = TimeUnit.HOURS.toMillis((long) value);
                return mFormat.format(new Date(millis));
            }
        });

        YAxis leftAxis3 = chart3.getAxisLeft();
        leftAxis3.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        leftAxis3.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis3.setDrawGridLines(true);
        leftAxis3.setGranularityEnabled(true);
        leftAxis3.setAxisMinimum(0f);
        leftAxis3.setAxisMaximum(170f);
        leftAxis3.setYOffset(-9f);
        leftAxis3.setTextColor(Color.rgb(255, 192, 56));

        YAxis rightAxis3 = chart3.getAxisRight();
        rightAxis3.setEnabled(false);



    }

    private void setData1(HashMap<String, Integer> dataHash1) {

        ArrayList<PieEntry> entries = new ArrayList<>();

        String [] keys = {"0","1","2","3","4"};
        int [] icons = {R.drawable.smile1_24, R.drawable.smile2_24,R.drawable.smile3_24,R.drawable.smile4_24,R.drawable.smile5_24};

        for (int i = 0; i<keys.length; i++){
            int value = 0;
            Integer outValue = dataHash1.get(keys[i]);
            if(outValue !=null){
                value = outValue.intValue();
            }

            if(value >0){
                entries.add(new PieEntry(value,"",getResources().getDrawable(icons[i])));
            }
        }

      /*  entries.add(new PieEntry(20.0f, "",
                getResources().getDrawable(R.drawable.smile1_24)));
        entries.add(new PieEntry(20.0f, "",
                getResources().getDrawable(R.drawable.smile2_24)));
        entries.add(new PieEntry(20.0f, "",
                getResources().getDrawable(R.drawable.smile3_24)));
        entries.add(new PieEntry(20f, "",
                getResources().getDrawable(R.drawable.smile4_24)));
        entries.add(new PieEntry(20.0f, "",
                getResources().getDrawable(R.drawable.smile5_24)));
*/
        PieDataSet dataSet = new PieDataSet(entries, getResources().getString(R.string.graph1_title));

        dataSet.setDrawIcons(true);

        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, -40));
        dataSet.setSelectionShift(5f);

        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.JOYFUL_COLORS) {
            colors.add(c);
        }
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(22.0f);
        data.setValueTextColor(Color.WHITE);

        chart.setData(data);
        chart.invalidate();
    }


    private void setData2(HashMap<String, Integer> dataHash2) {

        ArrayList<BarEntry> entries = new ArrayList<>();

        entries.add(new BarEntry(1.0f, 20.0f,
                getResources().getDrawable(R.drawable.smile1_24)));
        entries.add(new BarEntry(2.0f, 40.0f,
                getResources().getDrawable(R.drawable.smile2_24)));
        entries.add(new BarEntry(3.0f, 60.0f,
                getResources().getDrawable(R.drawable.smile3_24)));
        entries.add(new BarEntry(4.0f, 30.0f,
                getResources().getDrawable(R.drawable.smile4_24)));
        entries.add(new BarEntry(5.0f, 90.0f,
                getResources().getDrawable(R.drawable.smile5_24)));

        BarDataSet dataSet2 = new BarDataSet(entries, "Sinus Function");
        dataSet2.setColor(Color.rgb(240, 120, 124));

        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.JOYFUL_COLORS) {
            colors.add(c);
        }
        dataSet2.setColors(colors);
        dataSet2.setIconsOffset(new MPPointF(0, -10));

        BarData data = new BarData(dataSet2);
        data.setValueTextSize(10f);
        data.setDrawValues(false);
        data.setBarWidth(0.8f);

        chart2.setData(data);
        chart2.invalidate();

    }

    private void setData3(ArrayList<Float> dataKeys3, ArrayList<Integer> dataValues3) {

        ArrayList<Entry> values = new ArrayList<>();
        values.add(new Entry(24f, 20.0f));
        values.add(new Entry(48f, 50.0f));
        values.add(new Entry(72f, 30.0f));
        values.add(new Entry(96f, 70.0f));
        values.add(new Entry(120f, 90.0f));

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(values, "DataSet 1");
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setColor(ColorTemplate.getHoloBlue());
        set1.setValueTextColor(ColorTemplate.getHoloBlue());
        set1.setLineWidth(1.5f);
        set1.setDrawCircles(true);
        set1.setDrawValues(false);
        set1.setFillAlpha(65);
        set1.setFillColor(ColorTemplate.getHoloBlue());
        set1.setHighLightColor(Color.rgb(244, 117, 117));
        set1.setDrawCircleHole(false);

        // create a data object with the data sets
        LineData data = new LineData(set1);
        data.setValueTextColor(Color.WHITE);
        data.setValueTextSize(9f);

        // set data
        chart3.setData(data);
        chart3.invalidate();


    }


}


