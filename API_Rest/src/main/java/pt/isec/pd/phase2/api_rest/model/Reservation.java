package pt.isec.pd.phase2.api_rest.model;

import jakarta.persistence.*;

@Entity
@Table(name = "reserva")
public class Reservation
{
    @Id
    private Integer id;

    @Column(name = "data_hora")
    private String date;

    @Column(name = "pago")
    private int payed;

    @OneToOne
    @JoinColumn(name = "id_espetaculo")
    private Show show;

    @OneToOne
    @JoinColumn(name = "id_utilizador")
    private User user;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getPayed() {
        return payed;
    }

    public void setPayed(int payed) {
        this.payed = payed;
    }

    public Show getShow() {
        return show;
    }

    public void setShow(Show show) {
        this.show = show;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
