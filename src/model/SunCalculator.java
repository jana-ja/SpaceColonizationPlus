package model;

import java.util.ArrayList;
import java.util.List;

public class SunCalculator {

    private static final double LAT = 51.51494;

    private static double dayAngle(int day) {
        return (2.0 * Math.PI * day) / 365.25;
    }

    private static double hourAngle(double t) {
        return 0.2617993878 * (t - 12.0);
    }


    private static double declination(double dayangle) {
        return Math.asin(0.3978 * Math.sin(dayangle - 1.4 + 0.0355 * Math.sin(dayangle - 0.0489)));
    }


    private static double sunrise(double lat, double declination) {
        double tmp = -Math.tan(lat) * Math.tan(declination);
        if (Math.abs(tmp) <= 1.0) {
            return 12.0 - Math.acos(tmp) / 0.2617993878;
        } else {
            // polar winter > 1 or polar night < -1
            return -1.0;
        }
    }


    private static double sunset(double lat, double declination) {
        double tmp = -Math.tan(lat) * Math.tan(declination);
        if (Math.abs(tmp) <= 1.0) {
            return 12.0 + Math.acos(tmp) / 0.2617993878;
        } else {
            // polar winter > 1 or polar night < -1
            return -1.0;
        }
    }

    private static double elevationAngle(double hourangle, double lat, double declination) {
        return Math.asin(Math.sin(lat) * Math.sin(declination) + Math.cos(lat) * Math.cos(declination) * Math.cos(hourangle));
    }


    private static double azimuth(double decl, double lat, double elev, double hourangle) {
        double a = Math.sin(decl) * Math.cos(lat) - Math.cos(decl) * Math.sin(lat) * Math.cos(hourangle);
        a /= Math.cos(elev);

        a = Math.acos(a);

        if (hourangle < 0) {
            return a;
        } else {
            return 2.0 * Math.PI - a;
        }

    }

    public static List<SunPosition> positionsForDay(int day, double frequency){
        List<SunPosition> sunPositions = new ArrayList<>();

        double lat = Math.toRadians(LAT);

        double da = dayAngle(day);//winkel der schräge zwischen himmelshorizont und ekliptik an diesem tag? oder zu beginn dieses tages?
        double decl = declination(da);

        double s = sunset(lat, decl);
        double sr = sunrise(lat, decl);
//        System.out.println("day: " + day);
        for (double t = sr + 0.5; t < s; t += frequency)
        {
            double ha = hourAngle(t);
            double e = elevationAngle(ha, lat, decl); //Höhenwinkel der Sonne
            double a = azimuth(decl, lat, e, ha); //Azimuth

            sunPositions.add(new SunPosition(a,e));

//            System.out.println("\t" + t + ": " + Math.toDegrees(e) + ", " + Math.toDegrees(a));
        }

        return sunPositions;
    }

    public static SunPosition midde(int day){

        double lat = Math.toRadians(LAT);

        double da = dayAngle(day);//winkel der schräge zwischen himmelshorizont und ekliptik an diesem tag? oder zu beginn dieses tages?
        double decl = declination(da);

        double s = sunset(lat, decl);
        double sr = sunrise(lat, decl);

        double t = (s+sr)/2.0;

            double ha = hourAngle(t);
            double e = elevationAngle(ha, lat, decl); //Höhenwinkel der Sonne
            double a = azimuth(decl, lat, e, ha); //Azimuth



        return new SunPosition(a,e);
    }

//    public static void test() {
//        double lat = Math.toRadians(LAT);
//
//        for (int day = 1; day <= 365; day++) {
//            double da = dayAngle(day);
//            double decl = declination(da);
//
//            double s = sunset(lat, decl);
//            double sr = sunrise(lat, decl);
//            System.out.println("day: " + day);
//            for (double t = sr + 0.5; t < s; t += 1.0) //once an hour
//            {
//                double ha = hourAngle(t);
//                double e = elevationAngle(ha, lat, decl); //Höhenwinkel der Sonne
//                double a = azimuth(decl, lat, e, ha); //Azimuth
//
//                System.out.println("\t" + t + ": " + Math.toDegrees(e) + ", " + Math.toDegrees(a));
//            }
//        }
//
//    }
}