package pt.isec.pd.phase2.api_rest.model;

import jakarta.persistence.*;

@Entity
class Seat
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String row;

    private String seat;

    private double price;
    @ManyToOne
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRow() {
        return row;
    }

    public void setRow(String row) {
        this.row = row;
    }

    public String getSeat() {
        return seat;
    }

    public void setSeat(String seat) {
        this.seat = seat;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Show getShow() {
        return show;
    }

    public void setShow(Show show) {this.show = show;}
}

