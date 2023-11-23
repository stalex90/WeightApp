package com.stalex.weightapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Utils {

    public static String getDayAddition(String lastDate) {
        int num = 0;
        SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-yyyy");
        Date myLastdate = null;
        Date current = null;
        try {
            myLastdate = fmt.parse(lastDate);
            current = fmt.parse(new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }


        long diffInMillies = current.getTime() - myLastdate.getTime();
        num = (int) TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        int preLastDigit = num % 100 / 10;

        if (num == 0) {
            return "Сегодня";
        }

        if (num == 1) {
            return "Вчера";
        }

        if (preLastDigit == 1) {
            return num + " дней назад";
        }

        switch (num % 10) {
            case 1:
                return num + " день назад";
            case 2:
            case 3:
            case 4:
                return num + " дня назад";
            default:
                return num + " дней назад";
        }
    }

    public static void sortListByDate(List<WeightItem> list) {
        list.sort(Comparator.comparing(item -> {
            try {
                return new SimpleDateFormat("dd-MM-yyyy").parse(item.getDate());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }));
    }
}
