package pt.isec.pd.ticketline.src.model.data;

public class Seat {
     private final int id;
     private char row;
     private int seat;
     private float price;
     private boolean available;

     public Seat(int id, char row, int seat, float price)
     {
        this.id = id;
        this.row = row;
        this.seat = seat;
        this.price = price;
        this.available = false;
     }

     public void setRow(char row){this.row = row;}

     public char getRow(){return this.row;}

     public void setSeat(int seat){this.seat = seat;}

     public int getSeat(){return this.seat;}

     public void setPrice(float price){this.price = price;}

     public float getPrice(){return this.price;}

     public void setAvailable(boolean available){this.available = available;}

     public boolean getAvailable(){return this.available;}

     public int getId(){return this.id;}
}
