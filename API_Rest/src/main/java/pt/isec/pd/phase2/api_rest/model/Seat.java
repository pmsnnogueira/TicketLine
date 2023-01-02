package pt.isec.pd.phase2.api_rest.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "lugar")
class Seat
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "fila")
    private String row;

    @Column(name = "assento")
    private String seat;

    @Column(name = "preco")
    private double price;
    @ManyToOne
    @JoinColumn(name = "espetaculo_id")
    private Show espetaculo;

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

    @JsonBackReference
    public Show getShow() {
        return espetaculo;
    }

    public void setShow(Show show) {this.espetaculo = show;}
}

