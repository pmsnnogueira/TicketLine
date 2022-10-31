package pt.isec.pd.ticketline.src.resources.db;

import java.sql.*;
import java.util.ArrayList;

public class DBManager {
    private final Connection dbConn;

    public DBManager() throws SQLException {
        this.dbConn = DriverManager.getConnection("jdbc:sqlite:pt/isec/pd/ticketline/src/resources/db/PD-2022-23-TP.db");
    }

    public void close() throws SQLException
    {
        if (dbConn != null)
            dbConn.close();
    }

    public String listShows(Integer showID) throws SQLException{
        Statement statement = dbConn.createStatement();

        String sqlQuery = "SELECT id, descricao, tipo, data_hora, duracao, local, localidade, " +
                "pais, classificacao_etaria FROM espetaculo";

        if (showID != null)
            sqlQuery += " WHERE id like '%" + showID + "%'";

        ResultSet resultSet = statement.executeQuery(sqlQuery);

        StringBuilder str = new StringBuilder();
        str.append("ID\tDescricao\tTipo\tData_Hora\tDuracao\tLocal\tLocalidade\tPais\tClass_Etaria\n");

        while(resultSet.next()){
            int id = resultSet.getInt("id");
            String descricao = resultSet.getString("descricao");
            String tipo = resultSet.getString("tipo");
            String data_hora = resultSet.getString("data_hora");
            int duracao = resultSet.getInt("duracao");
            String local = resultSet.getString("local");
            String localidade = resultSet.getString("localidade");
            String pais = resultSet.getString("pais");
            String classificacao_etaria = resultSet.getString("classificacao_etaria");

            str.append(id).append("\t").append(descricao).append("\t").append(tipo).append("\t");
            str.append(data_hora).append("\t").append(duracao).append("\t").append(local);
            str.append("\t").append(localidade).append("\t").append(pais).append(classificacao_etaria).append("\n");
        }

        resultSet.close();
        statement.close();

        return str.toString();
    }

    public String listSeats(Integer placeID) throws SQLException{
        Statement statement = dbConn.createStatement();

        String sqlQuery = "SELECT id, fila, assento, preco, espetaculo_id FROM lugar";

        if (placeID != null)
            sqlQuery += " WHERE id like '%" + placeID + "%'";

        ResultSet resultSet = statement.executeQuery(sqlQuery);

        StringBuilder str = new StringBuilder();
        str.append("ID\tFila\tAssento\tPreco\tEspetaculo_ID\n");

        while(resultSet.next()){
            int id = resultSet.getInt("id");
            String fila = resultSet.getString("fila");
            String assento = resultSet.getString("assento");
            double preco = resultSet.getDouble("preco");
            int espetaculo_id = resultSet.getInt("espetaculo_id");

            str.append(id).append("\t").append(fila).append("\t").append(assento);
            str.append("\t").append(preco).append("\t").append(espetaculo_id).append("\n");
        }
        resultSet.close();
        statement.close();

        return str.toString();
    }

    public String listReservations(Integer reservationID) throws SQLException{
        Statement statement = dbConn.createStatement();

        String sqlQuery = "SELECT id, data_hora, pago, id_utilizador, id_espetaculo FROM reserva";

        if (reservationID != null)
            sqlQuery += " WHERE id like '%" + reservationID + "%'";

        ResultSet resultSet = statement.executeQuery(sqlQuery);

        StringBuilder str = new StringBuilder();
        str.append("ID\tData_Hora\tPago\tID_Utilizador\tID_Espetaculo\n");

        while(resultSet.next()){
            int id = resultSet.getInt("id");
            String data_hora = resultSet.getString("data_hora");
            int pago = resultSet.getInt("pago");
            int id_utilizador = resultSet.getInt("id_utilizador");
            int id_espetaculo = resultSet.getInt("id_espetaculo");

            str.append(id).append("\t").append(data_hora).append("\t").append(pago);
            str.append("\t").append(id_utilizador).append("\t").append(id_espetaculo).append("\n");
        }
        resultSet.close();
        statement.close();

        return str.toString();
    }

    public String listUsers(Integer userID) throws SQLException{
        Statement statement = dbConn.createStatement();

        String sqlQuery = "SELECT id, username, nome, administrador, autenticado FROM utilizador";

        if (userID != null)
            sqlQuery += " WHERE id like '%" + userID + "%'";

        ResultSet resultSet = statement.executeQuery(sqlQuery);

        StringBuilder str = new StringBuilder();
        str.append("ID\tUsername\tNome\tAdministrador\tAutenticado\n");

        while(resultSet.next()){
            int id = resultSet.getInt("id");
            String username = resultSet.getString("username");
            String nome = resultSet.getString("nome");
            int administrador = resultSet.getInt("administrador");
            int autenticado = resultSet.getInt("autenticado");

            str.append(id).append("\t").append(username).append("\t").append(nome);
            str.append("\t").append(administrador).append("\t").append(autenticado).append("\n");
        }
        resultSet.close();
        statement.close();

        return str.toString();
    }

    public boolean insertShow(ArrayList<String> parameters){
        Statement statement;
        try{
            statement = dbConn.createStatement();
        }catch (SQLException e){
            return false;
        }

        int i = 0;
        String sqlQuery = "INSERT INTO espetaculo VALUES (NULL, '" + parameters.get(i++) + "' , '" +
                            parameters.get(i++) + "' , '" + parameters.get(i++) + "' , '" +
                            parameters.get(i++) + "' , '" + parameters.get(i++) + "' , '" +
                            parameters.get(i++) + "' , '" + parameters.get(i++) + "' , '" +
                            parameters.get(i++) + "' , '" + "0" + "')";

        try{
            statement.executeUpdate(sqlQuery);
            statement.close();
        }catch (SQLException e){
            return false;
        }

        return true;
    }

    public boolean insertSeat(ArrayList<String> parameters){
        Statement statement;
        try{
            statement = dbConn.createStatement();
        }catch (SQLException e){
            return false;
        }

        int i = 0;
        String sqlQuery = "INSERT INTO espetaculo VALUES (NULL, '" + parameters.get(i++) + "' , '" +
                parameters.get(i++) + "' , '" + parameters.get(i++) + "' , '" +
                parameters.get(i) + "')";

        try{
            statement.executeUpdate(sqlQuery);
            statement.close();
        }catch (SQLException e){
            return false;
        }

        return true;
    }

    public boolean insertReservation(ArrayList<String> parameters){
        Statement statement;
        try{
            statement = dbConn.createStatement();
        }catch (SQLException e){
            return false;
        }

        int i = 0;
        String sqlQuery = "INSERT INTO espetaculo VALUES (NULL, '" + parameters.get(i++) + "' , '" +
                parameters.get(i++) + "' , '" + parameters.get(i++) + "' , '" +
                parameters.get(i) + "')";

        try{
            statement.executeUpdate(sqlQuery);
            statement.close();
        }catch (SQLException e){
            return false;
        }

        return true;
    }
    public boolean insertUser(ArrayList<String> parameters){
        Statement statement;
        try{
            statement = dbConn.createStatement();
        }catch (SQLException e){
            return false;
        }

        int i = 0;
        String sqlQuery = "INSERT INTO espetaculo VALUES (NULL, '" + parameters.get(i++) + "' , '" +
                parameters.get(i++) + "' , '" + parameters.get(i++) + "' , '" +
                parameters.get(i++) + "' , '" + parameters.get(i++) + "')";

        try{
            statement.executeUpdate(sqlQuery);
            statement.close();
        }catch (SQLException e){
            return false;
        }

        return true;
    }

}
