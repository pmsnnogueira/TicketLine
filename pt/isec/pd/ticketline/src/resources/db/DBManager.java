package pt.isec.pd.ticketline.src.resources.db;

import pt.isec.pd.ticketline.src.model.server.heartbeat.HeartBeat;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
public class DBManager {

    private Connection dbConn;
    private Connection defaultDB;

    private HeartBeat serverHB;

    private String addShowQuery;

    public DBManager() throws SQLException {
        this.dbConn = DriverManager.getConnection("jdbc:sqlite:pt/isec/pd/ticketline/src/resources/db/PD-2022-23-TP.db");
        this.defaultDB = DriverManager.getConnection("jdbc:sqlite:pt/isec/pd/ticketline/src/resources/db/PD-2022-23-TP.db");
        this.addShowQuery = null;
    }

    public void close() throws SQLException
    {
        if (dbConn != null && defaultDB != null){
            dbConn.close();
            defaultDB.close();
        }
    }

    public void setServerHB(HeartBeat serverHB) {
        this.serverHB = serverHB;
    }

    public boolean connectToDB(int port, String DBDirectory){
        //if the database already exists, there is no need to duplicate it
        if((new File(DBDirectory + "/PD-2022-23-TP-" + port + ".db")).exists()){
            try{
                this.dbConn = DriverManager.getConnection("jdbc:sqlite:" + DBDirectory + "/PD-2022-23-TP-" + port + ".db");
            }catch (SQLException e){
                return false;
            }
            return true;
        }

        File file = new File("pt/isec/pd/ticketline/src/resources/db/PD-2022-23-TP.db");

        FileInputStream fis;
        try{
            fis = new FileInputStream(file);
        }catch (FileNotFoundException e){
            return false;
        }


        FileOutputStream fos;
        try{
            fos = new FileOutputStream(DBDirectory + "/PD-2022-23-TP-" + port + ".db");
        }catch (FileNotFoundException e){
            return false;
        }

        byte[] buffer = new byte[1024];
        int bytesRead = 0;

        while(bytesRead >= 0){
            try{
                bytesRead = fis.read(buffer);
                fos.write(buffer);
            }catch (IOException e){
                return false;
            }
        }

        try {
            fos.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            this.dbConn = DriverManager.getConnection("jdbc:sqlite:" + DBDirectory + "/PD-2022-23-TP-" + port + ".db");
        } catch (SQLException e) {
            return false;
        }
        insertVersion();
        return true;
    }

    public int testDatabaseVersion(String DBDirectory, int tcpPort){
        Connection testConnection;
        try {
            testConnection = DriverManager.getConnection("jdbc:sqlite:" + DBDirectory + "/PD-2022-23-TP-" + tcpPort + ".db");
        } catch (SQLException e) {
            return -1;
        }
        int versao=0;
        Statement statement = null;
        ResultSet resultSet = null;
        try
        {
            statement = testConnection.createStatement();
            String sqlQuery = "SELECT versao FROM configuracoes";
            resultSet = statement.executeQuery(sqlQuery);
            versao = resultSet.getInt("versao");
        }catch(SQLException sqle){
            sqle.printStackTrace();
        }
        finally {
            try{
                if(statement != null){
                    statement.close();
                }
                resultSet.close();
            }catch (SQLException e){

            }
        }
        return versao;
    }

    public void processNewQuerie(String newQuerie){
        String[] queries = newQuerie.split("\\|");
        Statement statement = null;
        try{
            for (String str : queries){
                statement = this.dbConn.createStatement();
                statement.executeUpdate(str);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        finally {
            try{
                if(statement != null){
                    statement.close();
                }
            }catch (SQLException e){

            }
        }
        updateVersion();
    }

    public void saveQuery(String newQuerie){
        this.serverHB.setQueries(newQuerie);
    }

    //Parameters 0 -> ID            1 -> visivel
    public String listShows(ArrayList<String> parameters){
        Statement statement = null;
        ResultSet resultSet = null;
        try{
            statement = dbConn.createStatement();

            String sqlQuery = null;

            if(parameters.get(0).equals("-2")){
                sqlQuery = "SELECT distinct e.id FROM espetaculo e ,reserva r WHERE e.id = r.id_espetaculo and e.id" +
                        " NOT IN (SELECT  id_espetaculo FROM reserva r WHERE pago = 1) OR e.id NOT IN(SELECT id_espetaculo FROM reserva)";
            }else{
                sqlQuery = "SELECT id, descricao, tipo, data_hora, duracao, local, localidade, " +
                        "pais, classificacao_etaria FROM espetaculo";

                if (parameters != null && !parameters.get(1).equals("0")) {
                    sqlQuery += " WHERE visivel = " + parameters.get(1) + "";
                    if (!parameters.get(0).equals("-1"))
                        sqlQuery += " and id like '%" + parameters.get(0) + "%'";
                }
            }

            resultSet = statement.executeQuery(sqlQuery);

            StringBuilder str = new StringBuilder();

            if(parameters.get(0).equals("-2")){
                str.append("ID: ").append(resultSet.getInt("id"));
            }else{
                str.append("\n-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------\n");
                str.append(String.format("|%-4s|%-40s|%-12s|%-17s|%-7s|%-55s|%-11s|%-10s|%-13s|", "ID", "Descricao", "Tipo", "Data_Hora", "Duracao","Local","Localidade","Pais","Classe_Etaria"));
                str.append("\n-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------\n");

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

                    str.append(String.format("|%-4s|%-40s|%-12s|%-17s|%-7s|%-55s|%-11s|%-10s|%-13s|", id , descricao, tipo, data_hora, duracao,local,localidade,pais,classificacao_etaria));
                    str.append("\n-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------\n");
                }
            }

            return str.toString();
        }catch(SQLException e){
            return "Could not list shows";
        }
        finally {
            try{
                if(statement != null){
                    statement.close();
                }
                resultSet.close();
            }catch (SQLException e){

            }
        }
    }

    public String listSeats(Integer showID){
        Statement statement = null;
        ResultSet resultSet = null;
        try{
            statement = dbConn.createStatement();

            String sqlQuery = "SELECT id, fila, assento, preco, espetaculo_id FROM lugar";

            if (showID != null)
                sqlQuery += " WHERE espetaculo_id like '%" + showID + "%'";

            resultSet = statement.executeQuery(sqlQuery);

            StringBuilder str = new StringBuilder();
            str.append("\n---------------------------------------------------------\n");
            str.append(String.format("|%-4s|%-10s|%-10s|%-13s|%-14s|", "ID", "Fila", "Assento", "Preco", "Espetaculo_ID"));
            str.append("\n---------------------------------------------------------\n");


            while(resultSet.next()){
                int id = resultSet.getInt("id");
                String fila = resultSet.getString("fila");
                String assento = resultSet.getString("assento");
                double preco = resultSet.getDouble("preco");
                int espetaculo_id = resultSet.getInt("espetaculo_id");

                str.append(String.format("|%-4s|%-10s|%-10s|%-13s|%-14s|", id , fila, assento, preco, espetaculo_id));
                str.append("\n---------------------------------------------------------\n");
            }

            return str.toString();
        }catch (SQLException e){
            return "Could not list seats";
        }
        finally {
            try{
                if(statement != null){
                    statement.close();
                }
                resultSet.close();
            }catch (SQLException e){

            }
        }
        
    }

    public String listReservations(Integer reservationID){
        Statement statement = null;
        ResultSet resultSet = null;
        try{
            statement = dbConn.createStatement();

            String sqlQuery = "SELECT id, data_hora, pago, id_utilizador, id_espetaculo FROM reserva";

            if (reservationID != null)
                sqlQuery += " WHERE id like '%" + reservationID + "%'";

            resultSet = statement.executeQuery(sqlQuery);

            StringBuilder str = new StringBuilder();
            str.append("\n-----------------------------------------------------------------\n");
            str.append(String.format("|%-4s|%-17s|%-10s|%-14s|%-14s|", "ID", "Data_Hora", "Pago", "ID_Utilizador", "ID_Espetaculo"));
            str.append("\n-----------------------------------------------------------------\n");

            while(resultSet.next()){
                int id = resultSet.getInt("id");
                String data_hora = resultSet.getString("data_hora");
                int pago = resultSet.getInt("pago");
                int id_utilizador = resultSet.getInt("id_utilizador");
                int id_espetaculo = resultSet.getInt("id_espetaculo");

                str.append(String.format("|%-4s|%-17s|%-10s|%-14s|%-14s|", id , data_hora, pago, id_utilizador, id_espetaculo));
                str.append("\n-----------------------------------------------------------------\n");
            }

            return str.toString();
        }catch (SQLException e){
            return "Could not list reservations";
        }
        finally {
            try{
                if(statement != null){
                    statement.close();
                }
                resultSet.close();
            }catch (SQLException e){

            }
        }
    }

    public String listUsers(Integer userID){
        Statement statement = null;
        ResultSet resultSet = null;
        try{
            statement = dbConn.createStatement();

            String sqlQuery = "SELECT * FROM utilizador";

            if (userID != null)
                sqlQuery += " WHERE id like '%" + userID + "%'";

            resultSet = statement.executeQuery(sqlQuery);

            StringBuilder str = new StringBuilder();
            str.append("\n---------------------------------------------------------------------------------------------\n");
            str.append(String.format("|%-4s|%-19s|%-30s|%-22s|%-12s|", "ID", "Username", "Nome", "Administrador", "Autenticado"));
            str.append("\n---------------------------------------------------------------------------------------------\n");


            while(resultSet.next()){
                int id = resultSet.getInt("id");
                String username = resultSet.getString("username");
                String nome = resultSet.getString("nome");
                int administrador = resultSet.getInt("administrador");
                int autenticado = resultSet.getInt("autenticado");

                str.append(String.format("|%-4s|%-19s|%-30s|%-22s|%-12s|", id , username, nome, administrador, autenticado));
                str.append("\n---------------------------------------------------------------------------------------------\n");       
            }

            return str.toString();
        }catch (SQLException e){
            return "Could not list user";
        }
        finally {
            try{
                if(statement != null){
                    statement.close();
                }
                resultSet.close();
            }catch (SQLException e){

            }
        }
    }

    //Parameters -> 0(Pago)
    public String listNotOrPaidReservations(Integer userID,  ArrayList<String>parameters){
        Statement statement = null;
        ResultSet resultSet = null;
        try{
            statement = dbConn.createStatement();

            String sqlQuery = "SELECT id , data_hora , id_espetaculo FROM reserva WHERE id_utilizador = '" + userID + "'";

            if(parameters != null && !parameters.get(0).equals("-1"))
                sqlQuery += "and pago = "+ parameters.get(0) +"";

            resultSet = statement.executeQuery(sqlQuery);
            StringBuilder str = new StringBuilder();

            str.append("\n--------------------------------------------------\n");
            str.append(String.format("|%-11s|%-20s|%15s|", "Id_Reserva", "Data_Hora", "Id_Espetaculo"));
            str.append("\n--------------------------------------------------\n");
            while(resultSet.next()){
                int id = resultSet.getInt("id");
                String dataHora = resultSet.getString("data_hora");
                int id_espetaculo = resultSet.getInt("id_espetaculo");
                str.append(String.format("|%-11s|%-20s|%-15s|", id , dataHora, id_espetaculo));
                str.append("\n--------------------------------------------------\n");
            }

            return str.toString();
        }catch (SQLException e){
            e.printStackTrace();
            return "Could not list any reservation";
        }
        finally {
            try{
                if(statement != null){
                    statement.close();
                }
                resultSet.close();
            }catch (SQLException e){

            }
        }

    }

    public String listEmptySeatsDayBefore(Integer showID){
        Statement statement = null;
        ResultSet resultSet = null;
        try{
            statement = dbConn.createStatement();

            String sqlQuery = "SELECT l.id as id_bilhete, l.fila as fila, l.assento as assento, l.preco as preco, e.id as id_espetaculo, e.descricao as descricao, e.data_hora as datahora FROM lugar l ,espetaculo  e " +
                    "WHERE e.data_hora <= datetime('now','-1 day') and l.id NOT IN (SELECT id_lugar FROM reserva_lugar) and l.espetaculo_id = e.id";

            /*if (showID != null)
                sqlQuery += " and id like '%" + showID + "%'";*/

            resultSet = statement.executeQuery(sqlQuery);
            StringBuilder str = new StringBuilder();
            str.append("\n-----------------------------------------------------------------------------------------------\n");
            str.append(String.format("|%12s|%-5s|%-8s|%-7s|%-15s|%-40s|", "id_bilhete", "fila", "assento", "preco", "id_espetaculo" , "descricao" , "dataHora"));
            str.append("\n-----------------------------------------------------------------------------------------------\n");


            while(resultSet.next()){
                int id = resultSet.getInt("id_bilhete");
                String username = resultSet.getString("fila");
                String nome = resultSet.getString("assento");
                int administrador = resultSet.getInt("preco");
                int id_espetaculo = resultSet.getInt("id_espetaculo");
                String descricao = resultSet.getString( "descricao");
                String dataHora = resultSet.getString("dataHora");

                str.append(String.format("|%-12s|%-5s|%-8s|%-7s|%-15s|%-40s|", id , username, nome, administrador, id_espetaculo , descricao , dataHora));
                str.append("\n-----------------------------------------------------------------------------------------------\n");
            }

            return str.toString();
        }catch (SQLException e){
            e.printStackTrace();
            return "Could not list any empty seats for a show";
        }
        finally {
            try{
                if(statement != null){
                    statement.close();
                }
                resultSet.close();
            }catch (SQLException e){

            }
        }

    }

    public boolean deleteUnPaidReservation(int idReservation , ArrayList<String> parameters){
        Statement statement = null;
        try{
            statement = dbConn.createStatement();
            String sqlQuery = "DELETE FROM reserva WHERE id = " + idReservation + " and pago = 0 and id_utilizador = " + parameters.get(0) +"";
            statement.executeUpdate(sqlQuery);
            saveQuery(sqlQuery);
        }catch(SQLException e){
            return false;
        }
        finally {
            try{
                if(statement != null){
                    statement.close();
                }
            }catch (SQLException e){

            }
        }
        updateVersion();
        return true;
    }

    public String verifyUserLogin(ArrayList<String> parameters){
        Statement statement = null;
        ResultSet resultSet = null;
        try{
            statement = dbConn.createStatement();

            String sqlQuery = "SELECT id,username,nome,password,administrador,autenticado FROM utilizador WHERE lower(username) = lower('" + parameters.get(0) + "') and password = '" + parameters.get(1) + "'";

            resultSet = statement.executeQuery(sqlQuery);
            StringBuilder str = new StringBuilder();

            while(resultSet.next()){
                str.append("ID: " + resultSet.getInt("id"))
                        .append("\nUsername:" + resultSet.getString("username"))
                        .append("\nName:" + resultSet.getString("nome"))
                        .append("\nPassword:" + resultSet.getString("password"))
                        .append("\nAdmin:" + resultSet.getInt("administrador"))
                        .append("\nAuthenthicated:" + resultSet.getInt("autenticado"));
            }

            if(str.toString().isEmpty() || str.toString().isBlank())
                return "User doesnt exist!";

            return str.toString();
        }catch (SQLException e){
            e.printStackTrace();
            return "User doesnt exist!";
        }
        finally {
            try{
                if(statement != null){
                    statement.close();
                }
                resultSet.close();
            }catch (SQLException e){

            }
        }

    }

    //Vai procurar em todos os assentos se há filas repetidas, se houver vai procurar nessas filas se há cadeiras repetidas, se houver repetidas retorna TRUE
    public boolean verificarRepetidos(ArrayList<ArrayList<String>> parameters){

        for(int i = 0 ; i < parameters.size() ; i++){
            for(int j = i + 1 ; j < parameters.size(); j++){
                if(parameters.get(i).get(0).equals(parameters.get(j).get(0))){       //Aqui vai encontrar duas filas iguais
                    if(parameters.get(i).get(1).equals(parameters.get(j).get(1))){    //Comparar se o Assento é o mesmo na mesma fila
                        return (true);                                                 //Se houver Filas iguais com assentos iguais ele não vai deixar criar
                    }
                }
            }
        }

        return (false);
    }

    //Verifica se é há elementos repetidos no ficheiro de texto, se não houver adiciona e retorna true
    public boolean insertShowSeatFile(ArrayList<String> parametersShow , ArrayList<ArrayList<String>> parametersSeats){
        if(!verificarRepetidos(parametersSeats)){
            int numShow = insertShow(parametersShow);
            if(numShow > 0) {     //Se retornar -1 é porque houve algum erro na insercao
                insertSeat(parametersSeats, numShow);
                return (true);
            }
        }

        return (false);
    }


    //Insert Show   ERROR: Return (-1)   else   return(show)
    public int insertShow(ArrayList<String> parameters){
        Statement statement;
        try{
            statement = dbConn.createStatement();
        }catch (SQLException e){
            return (-1);
        }

        int i = 0;
        String sqlQuery = "INSERT INTO espetaculo VALUES (NULL, '" + parameters.get(i++) + "' , '" +
                parameters.get(i++) + "' , '" + parameters.get(i++) + "' , '" +
                parameters.get(i++) + "' , '" + parameters.get(i++) + "' , '" +
                parameters.get(i++) + "' , '" + parameters.get(i++) + "' , '" +
                parameters.get(i) + "' , '" + "0" + "')";

        try{
            statement.executeUpdate(sqlQuery , statement.RETURN_GENERATED_KEYS);
            ResultSet rs = statement.getGeneratedKeys();
            addShowQuery = sqlQuery;
            updateVersion();
            return (rs.getInt(1));
        }catch (SQLException e){
            return (-1);
        }finally {
            try{
                if(statement != null){
                    statement.close();
                }
            }catch (SQLException e){

            }
        }

    }

    public boolean insertSeat(ArrayList<ArrayList<String>> parameters , int numShow){
        Statement statement;
        int contador = 0;
        try{
            statement = dbConn.createStatement();
        }catch (SQLException e){
            return false;
        }

        StringBuilder sb = new StringBuilder();

        if(addShowQuery != null){
            sb.append(addShowQuery).append("|");
            addShowQuery = null;
        }

        try{
            int i = 0;
            while(i < parameters.size()){
                String sqlQuery = "INSERT INTO lugar VALUES (NULL, '" + parameters.get(i).get(contador++) + "' , '" +
                        parameters.get(i).get(contador++) + "' , '" + parameters.get(i).get(contador) + "' , '" +
                        numShow + "')";
                statement.executeUpdate(sqlQuery);
                sb.append(sqlQuery).append("|");
                i++;
                contador = 0;
            }
            saveQuery(sb.toString());
        }catch (SQLException e){
            return false;
        }finally {
            try{
                if(statement != null){
                    statement.close();
                }
            }catch (SQLException e){

            }
        }
        return true;
    }

    public String insertReservation(ArrayList<String> parameters){
        Statement statement;
        try{
            statement = dbConn.createStatement();
        }catch (SQLException e){
            return null;
        }

        int i = 0;
        String sqlQuery = "INSERT INTO reserva VALUES (NULL, '" + parameters.get(i++) + "' , '" +
                parameters.get(i++) + "' , '" + parameters.get(i++) + "' , '" +
                parameters.get(i) + "')";

        try{
            statement.executeUpdate(sqlQuery, statement.RETURN_GENERATED_KEYS);
            ResultSet rs = statement.getGeneratedKeys();
            saveQuery(sqlQuery);
            updateVersion();
            return Integer.toString(rs.getInt(1));
        }catch (SQLException e){
            e.printStackTrace();
            return null;
        }finally {
            try{
                if(statement != null){
                    statement.close();
                }
            }catch (SQLException e){

            }
        }

    }


    public String insertReservationSeat(ArrayList<String> parameters) {
        Statement statement = null;
        try{
            statement = dbConn.createStatement();
        }catch (SQLException e){
            return null;
        }

        int i = 0;
        //RECEBIDO -> clientID , datas_hora, id_show, lugar escolhido
        String sqlQuery = "INSERT INTO reserva VALUES (NULL, '" + parameters.get(1) + "' , 0 ,'" + parameters.get(0) + "', '" + parameters.get(2) + "')";

        try{
            statement.executeUpdate(sqlQuery, statement.RETURN_GENERATED_KEYS);
            ResultSet rs = statement.getGeneratedKeys();
            int idReserva = rs.getInt(1);


            String seatSqlQuery = "INSERT INTO reserva_lugar VALUES (" + idReserva + " , " + parameters.get(3) + ")";
            statement.executeUpdate(seatSqlQuery);

            saveQuery(sqlQuery + "|" + seatSqlQuery);
            updateVersion();
            return "Reserva de lugar realizada";
        }catch (SQLException e){
            e.printStackTrace();
            return "Reserva lugar nao realizada";
        }finally {
            try{
                if(statement != null){
                    statement.close();
                }
            }catch (SQLException e){

            }
        }

    }


    public int getDatabaseVersion(){
        int versao=0;
        Statement statement = null;
        try
        {
            statement = dbConn.createStatement();
            String sqlQuery = "SELECT versao FROM configuracoes";
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            versao = resultSet.getInt("versao");
        }catch(SQLException sqle){
            sqle.printStackTrace();
        }
        finally {
            try{
                if(statement != null){
                    statement.close();
                }
                }catch (SQLException e){

                }
        }
        return versao;
    }

    public boolean insertVersion(){
        Statement statement = null;
        try
        {
            statement = this.dbConn.createStatement();
        }
        catch(SQLException sqle){
            return false;
        }

        String sqlQuery = "INSERT INTO configuracoes VALUES(NULL, '" + 1 + "')";
        try{
            statement.executeUpdate(sqlQuery);
        }
        catch(SQLException sqle){
            return false;
        }
        finally {
            try{
                statement.close();
            }catch (SQLException e){

            }
        }
        return true;
    }

    public boolean updateVersion()
    {
        Statement statement = null;
        int version = getDatabaseVersion();
        try{
            statement = dbConn.createStatement();
            String sqlQuery = "UPDATE configuracoes SET versao='" + ++version + "'WHERE id=" + 1;
            statement.executeUpdate(sqlQuery);
        }catch(SQLException e){
            return false;
        }
        finally {
            try{
                statement.close();
            }catch (SQLException e){

            }
        }
        return true;
    }

    public boolean insertUser(ArrayList<String> parameters){
        Statement statement = null;
        try{
            statement = dbConn.createStatement();
        }catch (SQLException e){
            return false;
        }

        int i = 0;
        //Verificar se há algum com nome ou utilizador igual
        String verificar = "SELECT Count(*) AS contador FROM utilizador WHERE lower(username)=lower('" + parameters.get(0) + "') or lower(nome)=lower('" + parameters.get(1)+"')";
        try {
            ResultSet resultSet = statement.executeQuery(verificar);

            int contador = resultSet.getInt("contador");
            if(contador > 0){
                return false;
            }

        }catch (SQLException e){
            e.printStackTrace();
        }
        finally {
            try{
                statement.close();
            }catch (SQLException e){

            }
        }


        String sqlQuery = "INSERT INTO utilizador VALUES (NULL, '" + parameters.get(i++) + "' , '" +
                parameters.get(i++) + "' , '" + parameters.get(i++) + "' , '" +
                parameters.get(i++) + "' , '" + parameters.get(i++) + "')";

        try{
            statement.executeUpdate(sqlQuery);
            saveQuery(sqlQuery);
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }finally {
            try{
                statement.close();
            }catch (SQLException e){

            }
        }
        updateVersion();
        return true;
    }

    public boolean deleteShow(int id)
    {
        Statement statement = null;
        try{
            statement = dbConn.createStatement();

            String sqlQuery = "DELETE FROM espetaculo WHERE id=" + id;
            statement.executeUpdate(sqlQuery);
            saveQuery(sqlQuery);
        }catch(SQLException e){
            return false;
        }
        finally {
            try{
                if(statement != null){
                    statement.close();
                }
                }catch (SQLException e){

                }
        }
        updateVersion();
        return true;
    }

    public boolean deleteSeat(int id)
    {
        Statement statement = null;
        try{
            statement = dbConn.createStatement();

            String sqlQuery = "DELETE FROM lugar WHERE id=" + id;
            statement.executeUpdate(sqlQuery);
            saveQuery(sqlQuery);
        }catch(SQLException e){
            return false;
        }
        finally {
            try{
                if(statement != null){
                    statement.close();
                }
                }catch (SQLException e){

                }
        }
        updateVersion();
        return true;
    }

    public boolean deleteReservations(int id)
    {
        Statement statement = null;
        try{
            statement = dbConn.createStatement();
            String sqlQuery = "DELETE FROM reserva WHERE id=" + id;
            statement.executeUpdate(sqlQuery);
            saveQuery(sqlQuery);
        }catch(SQLException e){
            return false;
        }
        finally {
            try{
                if(statement != null){
                    statement.close();
                }
                }catch (SQLException e){

                }
        }
        updateVersion();
        return true;
    }

    public boolean deleteUsers(int id)
    {
        Statement statement = null;
        try{
            statement = dbConn.createStatement();

            String sqlQuery = "DELETE FROM utilizador WHERE id=" + id;
            statement.executeUpdate(sqlQuery);
            saveQuery(sqlQuery);
        }catch(SQLException e){
            return false;
        }
        finally {
            try{
                if(statement != null){
                    statement.close();
                }
                }catch (SQLException e){

                }
        }
        updateVersion();
        return true;
    }

    public boolean updateShows(int id, HashMap<String, String> newData){
        Statement statement = null;
        try{
            statement = dbConn.createStatement();
            String sqlQuery = new String();

            for(Map.Entry<String, String> entry : newData.entrySet()){
                switch (entry.getKey()){
                    case "descricao" ->{
                        sqlQuery = "UPDATE espetaculo SET descricao='" + entry.getValue() + "' WHERE id=" + id;
                    }
                    case "tipo" ->{
                        sqlQuery = "UPDATE espetaculo SET tipo='" + entry.getValue() + "' WHERE id=" + id;
                    }
                    case "data_hora" ->{
                        sqlQuery = "UPDATE espetaculo SET data_hora='" + entry.getValue() + "' WHERE id=" + id;
                    }
                    case "local" ->{
                        sqlQuery = "UPDATE espetaculo SET local='" + entry.getValue() + "' WHERE id=" + id;
                    }
                    case "localidade" ->{
                        sqlQuery = "UPDATE espetaculo SET localidade='" + entry.getValue() + "' WHERE id=" + id;
                    }
                    case "pais" ->{
                        sqlQuery = "UPDATE espetaculo SET pais='" + entry.getValue() + "' WHERE id=" + id;
                    }
                    case "classificacao_etaria" ->{
                        sqlQuery = "UPDATE espetaculo SET classificacao_etaria='" + entry.getValue() + "' WHERE id=" + id;
                    }
                    case "visivel" ->{
                        sqlQuery = "UPDATE espetaculo SET visivel='" + entry.getValue() + "' WHERE id=" + id;
                    }
                }
                statement.executeUpdate(sqlQuery);
                saveQuery(sqlQuery);
            }
        }catch(SQLException e){
            return false;
        }
        finally {
            try{
                if(statement != null){
                    statement.close();
                }
                }catch (SQLException e){

                }
        }
        updateVersion();
        return true;
    }

    public boolean updateSeats(int id, HashMap<String, String> newData){
        Statement statement = null;
        try{
            statement = dbConn.createStatement();
            String sqlQuery = new String();

            for(Map.Entry<String, String> entry : newData.entrySet()){
                switch (entry.getKey()){
                    case "fila" ->{
                        sqlQuery = "UPDATE lugar SET fila='" + entry.getValue() + "' WHERE id=" + id;
                    }
                    case "assento" ->{
                        sqlQuery = "UPDATE lugar SET assento='" + entry.getValue() + "' WHERE id=" + id;
                    }
                    case "preco" ->{
                        sqlQuery = "UPDATE lugar SET preco='" + entry.getValue() + "' WHERE id=" + id;
                    }
                    case "espetaculo_id" -> {
                        sqlQuery = "UPDATE lugar SET espetaculo_id='" + entry.getValue() + "' WHERE id=" + id;
                    }
                }
                statement.executeUpdate(sqlQuery);
                saveQuery(sqlQuery);
            }
        }catch(SQLException e){
            return false;
        }
        finally {
            try{
                if(statement != null){
                    statement.close();
                }
                }catch (SQLException e){

                }
        }
        updateVersion();
        return true;
    }


                                                        //ACABAR ESTE POIS SO O PROPRIO USER É QUE PODE MUDAR OS SEUS PAGAMENTOS
    public boolean updateReservation(int id, HashMap<String, String> newData){
        Statement statement = null;
        try{
            statement = dbConn.createStatement();
            String sqlQuery = new String();

            for(Map.Entry<String, String> entry : newData.entrySet()){
                switch (entry.getKey()){
                    case "data_hora" ->{
                        sqlQuery = "UPDATE reserva SET data_hora='" + entry.getValue() + "' WHERE id=" + id;
                    }
                    case "pago" ->{
                        sqlQuery = "UPDATE reserva SET pago='" + entry.getValue() + "' WHERE id=" + id;
                    }
                    case "id_utilizador" ->{
                        sqlQuery = "UPDATE reserva SET id_utilizador='" + entry.getValue() + "' WHERE id=" + id;
                    }
                    case "id_espetaculo" -> {
                        sqlQuery = "UPDATE reserva SET id_espetaculo='" + entry.getValue() + "' WHERE id=" + id;
                    }
                }
                statement.executeUpdate(sqlQuery);
                saveQuery(sqlQuery);
            }
        }catch(SQLException e){
            return false;
        }
        finally {
            try{
                if(statement != null){
                    statement.close();
                }
                }catch (SQLException e){

                }
        }
        updateVersion();
        return true;
    }

    public boolean updateUser(int id, HashMap<String, String> newData){
        Statement statement = null;
        try{
            statement = dbConn.createStatement();
            String sqlQuery = new String();

            for(Map.Entry<String, String> entry : newData.entrySet()){
                switch (entry.getKey()){
                    case "username" ->{
                        sqlQuery = "UPDATE utilizador SET username='" + entry.getValue() + "' WHERE id=" + id;
                    }
                    case "nome" ->{
                        sqlQuery = "UPDATE utilizador SET nome='" + entry.getValue() + "' WHERE id=" + id;
                    }
                    case "password" ->{
                        sqlQuery = "UPDATE utilizador SET password='" + entry.getValue() + "' WHERE id=" + id;
                    }
                    case "administrador" -> {
                        sqlQuery = "UPDATE utilizador SET administrador='" + entry.getValue() + "' WHERE id=" + id;
                    }
                    case "autenticado" -> {
                        sqlQuery = "UPDATE utilizador SET autenticado='" + entry.getValue() + "' WHERE id=" + id;
                    }
                }
                statement.executeUpdate(sqlQuery);
                saveQuery(sqlQuery);
            }
        }catch(SQLException e){
            return false;
        }
        finally {
            try{
                if(statement != null){
                    statement.close();
                }
                }catch (SQLException e){

                }
        }
        updateVersion();
        return true;
    }




}
