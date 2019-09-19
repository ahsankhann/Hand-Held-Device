package com.ahsanshamim.novatexfgs;

import android.content.Context;
import android.database.Cursor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class LoginCheck {
    DatabaseHelper adptr;
    Context context;
    String datetime = "";

    public LoginCheck(Context context) {
        this.context = context;
    }

    public int CheckLogin() throws ParseException {
        adptr = new DatabaseHelper(context);

        Cursor cursor = adptr.getDataUser();

        if (cursor.moveToLast()) {
            datetime = cursor.getString(3);
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/mm/yyyy hh:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -30);


        String date = simpleDateFormat.format(calendar.getTime());
        try {
            if (datetime.equals("")) {
                return 4;
            }
            Date date1 = simpleDateFormat.parse(datetime);
            Date date2 = simpleDateFormat.parse(date);

            if (date2.after(date1)) {
                return 1;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}