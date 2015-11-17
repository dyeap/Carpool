package com.parse.starter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;


public class EventActivity extends ActionBarActivity {
    public int increment;
    private Context context = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

          /* Displays username */
        String username = DataHolder.getInstance().getUsername();
        TextView textElement = (TextView) findViewById(R.id.username);
        textElement.setText(username);
        updateTable(null);
        

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void AddPassenger(View view){
        Intent intent = new Intent(this, AccountActivity.class);
        final TableLayout table_layout = (TableLayout) findViewById(R.id.tableLayout);
        final String username = DataHolder.getInstance().getUsername();
        EditText text_passenger = (EditText) findViewById(R.id.enter_passenger);
        String passenger_username = text_passenger.getText().toString();
        //ParseObject testObject = new ParseObject("events");
        ParseObject testObject = DataHolder.getInstance().getEvent();
        testObject.put("organizer", username);
        testObject.put("guest", passenger_username);
        testObject.put("attending", false);
        try {
            testObject.saveInBackground();
        }
        catch(Exception e){
            System.err.print("error");
        }

        table_layout.removeAllViews();
        updateTable(null);
    }

    /* Called when user clicks "Update" button */
    public int flag;
    public void Update(View view) {

        Intent intent = new Intent(this, AccountActivity.class);

        final String username = DataHolder.getInstance().getUsername();

        EditText text_passenger = (EditText) findViewById(R.id.enter_passenger);
        String passenger_username = text_passenger.getText().toString();

        EditText text_time = (EditText) findViewById(R.id.enter_time);
        final int time = Integer.parseInt(text_time.getText().toString());
        final ArrayList<String> passengers = new ArrayList<String>();
        ParseQuery<ParseObject> passengerQuery = ParseQuery.getQuery("EventsTable");
        passengerQuery.whereEqualTo("organizer", username);
        passengerQuery.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> object, ParseException e) {
                try{
                    for (int i = 0; i < object.size(); i++) {
                        if (object.get(i).getBoolean("attending") == true){
                            passengers.add(object.get(i).getString("guest"));
                            String test = object.get(i).getObjectId();
                        }
                        else
                            passengers.add(null);
                    }
                    for (int i =0 ; i < passengers.size(); i++) {
                        if(passengers.get(i) == null){
                            continue;
                        }
                        ParseQuery<ParseObject> query1 = ParseQuery.getQuery("DrivingTable");
                        query1.whereEqualTo("user1", username);
                        query1.whereEqualTo("user2", passengers.get(i));
                        // user1 = b && user2 = a
                        ParseQuery<ParseObject> query2 = ParseQuery.getQuery("DrivingTable");
                        query2.whereEqualTo("user2", username);
                        query2.whereEqualTo("user1", passengers.get(i));
                        // Store first two queries in a list
                        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
                        queries.add(query1);
                        queries.add(query2);
                        // Perform multiple queries
                        ParseQuery<ParseObject> mainQuery = ParseQuery.or(queries);
                        mainQuery.findInBackground(new FindCallback<ParseObject>() {
                            public void done(List<ParseObject> object, ParseException e) {
                                if (e == null)//That means there's something in the query
                                {
                                    String userCheck = object.get(0).getString("user1");
                                    if (userCheck.equals(username)) {
                                        //Driver is user1
                                        int prev_time = object.get(0).getInt("time");
                                        object.get(0).put("time", prev_time + time);
                                        object.get(0).saveInBackground();
                                    } else {
                                        int prev_time = object.get(0).getInt("time");
                                        object.get(0).put("time", prev_time - time);
                                        object.get(0).saveInBackground();
                                    }
                                } else {
                                    System.out.printf("user not found");
                                }
                            }
                        });
                    }
                }
                catch(Exception l){
                    System.out.println("noo");
                }
            }
        });

        // Query database
        // user1 = a && user2 = b


        startActivity(intent);
    }
    public void updateTable(View view){
        System.out.printf("UPDATE\n");
        final String username = DataHolder.getInstance().getUsername();
        final TableLayout table_layout = (TableLayout) findViewById(R.id.tableLayout);
        ParseQuery<ParseObject> queryGuests = ParseQuery.getQuery("EventsTable");
        queryGuests.whereEqualTo("organizer", username);
        System.out.printf("username: %s\n", username);
        queryGuests.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    System.out.printf("Querying Success\n");
                    System.out.printf("how many queries: %d\n", objects.size());
                    int i = 0;
                    int cols = 3;
                    while (i < objects.size()) {
                        System.out.printf("number of elements %d\n", objects.size());
                        TableRow row = new TableRow(context);
                        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                                TableRow.LayoutParams.WRAP_CONTENT));
                        for (int j = 0; j < cols; j++) {
                            /* Create column for each row (information for each row) */
                            TextView tv = new TextView(context);
                            tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT));
                            tv.setPadding(25, 25, 25, 25);
                            if (j == 0) {
                                /* Number each entry */
                                tv.setText(Integer.toString(i + 1));
                                tv.setTextColor(Color.WHITE);
                                row.addView(tv);
                            } else if (j == 1) {
                                /* Username of friend */
                                String friend_username = objects.get(i).getString("guest");
                                tv.setText(friend_username);
                                tv.setTextColor(Color.WHITE);
                                row.addView(tv);
                            } else if (j == 2) {
                                /* Time owed */
                                Boolean isAttend = objects.get(i).getBoolean("attendance");
                                if (isAttend == true)
                                    tv.setText("Attending");
                                else
                                    tv.setText("Not Attending");
                                tv.setTextColor(Color.WHITE);
                                row.addView(tv);
                            }
                        }
                        final Button removeButton = new Button(context);
                        removeButton.setText("uninvite");
                        removeButton.setContentDescription(objects.get(i).getObjectId());
                        removeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    ParseObject.createWithoutData("EventsTable", (String) removeButton.getContentDescription()).delete();
                                } catch (Exception e) {
                                    System.err.println("cant find");
                                }
                                table_layout.removeAllViews();
                                updateTable(null);
                            }
                        });
                        i++;
                        increment = i;


                        row.addView(removeButton);
                        table_layout.addView(row);
                    }
                } else {
                    System.out.printf("TOO BAD\n");
                }
            }
        });
    }

}

