package pt.isec.pd.ticketline.src.model.data;

import java.util.ArrayList;

public class Show {
    private int id;
    private String title;
    private String genre;
    private String date;
    private String hour;
    private int duration;
    private String place;
    private String local;
    private String country;
    private int age;
    private ArrayList<Seat> seats;

    public Show(String title, String genre, String date,
                String hour, int duration, String place,
                String local, String country, int age)
                {
                    this.title = title;
                    this.genre = genre;
                    this.date = date;
                    this.hour = hour;
                    this.duration = duration;
                    this.place = place;
                    this.local = local;
                    this.country = country;
                    this.age = age;
                    seats = new ArrayList<>();
                }

    public void setTitle(String title){this.title = title;}

    public String getTitle(){return this.title;}

    public void setGenre(String genre){this.genre = genre;}

    public String getGenre(){return this.genre;}

    public void setDate(String date){this.date = date;}

    public String getDate(){return this.date;}

    public void setHour(String hour){this.hour = hour;}

    public String getHour(){return this.hour;}

    public void setDuration(int duration){this.duration = duration;}

    public int getDuration(){return this.duration;}

    public void setPlace(String place){this.place = place;}

    public String getPlace(){return this.place;}

    public void setLocal(String local){this.local = local;}

    public String getLocal(){return this.local;}

    public void setCountry(String country){this.country = country;}

    public String getCountry(){return this.country;}

    public void setAge(int age){this.age = age;}

    public int getAge(){return this.age;}
}
