package pt.isec.pd.ticketline.src.resources.db;

import pt.isec.pd.ticketline.src.model.server.MULTICAST;
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
    private MulticastSocket mcs;
    private HeartBeat serverHB;

    private String addShowQuery;

    public DBManager(MulticastSocket mcs) throws SQLException {
        this.dbConn = DriverManager.getConnection("jdbc:sqlite:pt/isec/pd/ticketline/src/resources/db/PD-2022-23-TP.db");
        this.defaultDB = DriverManager.getConnection("jdbc:sqlite:pt/isec/pd/ticketline/src/resources/db/PD-2022-23-TP.db");
        this.mcs = mcs;
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
        try
        {
            Statement statement = testConnection.createStatement();
            String sqlQuery = "SELECT versao FROM configuracoes";
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            versao = resultSet.getInt("versao");
        }catch(SQLException sqle){
            sqle.printStackTrace();
        }
        return versao;
    }

    public void processNewQuerie(String newQuerie){
        String[] queries = newQuerie.split("\\|");
        try{
            for (String str : queries){
                Statement statement = this.dbConn.createStatement();
                statement.executeUpdate(str);
                statement.close();
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        updateVersion();
    }

    public void multicastQuery(String newQuerie){
        this.serverHB.setQueries(newQuerie);
    }

    public String listShows(Integer showID){
        try{
            Statement statement = dbConn.createStatement();

            String sqlQuery = "SELECT id, descricao, tipo, data_hora, duracao, local, localidade, " +
                    "pais, classificacao_etaria FROM espetaculo";

            if (showID != null)
                sqlQuery += " WHERE id like '%" + showID + "%'";

            ResultSet resultSet = statement.executeQuery(sqlQuery);

            StringBuilder str = new StringBuilder();
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

            resultSet.close();
            statement.close();

            return str.toString();
        }catch(SQLException e){
            return "Could not list shows";
        }
    }

    public String listSeats(Integer placeID){
        try{
            Statement statement = dbConn.createStatement();

            String sqlQuery = "SELECT id, fila, assento, preco, espetaculo_id FROM lugar";

            if (placeID != null)
                sqlQuery += " WHERE id like '%" + placeID + "%'";

            ResultSet resultSet = statement.executeQuery(sqlQuery);

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
            resultSet.close();
            statement.close();

            return str.toString();
        }catch (SQLException e){
            return "Could not list seats";
        }
    }

    public String listReservations(Integer reservationID){
        try{
            Statement statement = dbConn.createStatement();

            String sqlQuery = "SELECT id, data_hora, pago, id_utilizador, id_espetaculo FROM reserva";

            if (reservationID != null)
                sqlQuery += " WHERE id like '%" + reservationID + "%'";

            ResultSet resultSet = statement.executeQuery(sqlQuery);

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
            resultSet.close();
            statement.close();

            return str.toString();
        }catch (SQLException e){
            return "Could not list reservations";
        }
    }

    public String listUsers(Integer userID){
        try{
            Statement statement = dbConn.createStatement();

            String sqlQuery = "SELECT * FROM utilizador";

            if (userID != null)
                sqlQuery += " WHERE id like '%" + userID + "%'";

            ResultSet resultSet = statement.executeQuery(sqlQuery);

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
                str.append("\n---------------------------------------------------------------------------------------------\n");       }
            resultSet.close();
            statement.close();
            return str.toString();
        }catch (SQLException e){
            return "Could not list user";
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
            statement.close();
            addShowQuery = sqlQuery;
            updateVersion();
            return (rs.getInt(1));
        }catch (SQLException e){
            return (-1);
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
            statement.close();
            multicastQuery(sb.toString());
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
        String sqlQuery = "INSERT INTO reserva VALUES (NULL, '" + parameters.get(i++) + "' , '" +
                parameters.get(i++) + "' , '" + parameters.get(i++) + "' , '" +
                parameters.get(i) + "')";

        try{
            statement.executeUpdate(sqlQuery);
            multicastQuery(sqlQuery);
            statement.close();
        }catch (SQLException e){
            return false;
        }
        updateVersion();
        return true;
    }

    public int getDatabaseVersion(){
        int versao=0;
        try
        {
            Statement statement = dbConn.createStatement();
            String sqlQuery = "SELECT versao FROM configuracoes";
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            versao = resultSet.getInt("versao");
        }catch(SQLException sqle){
            sqle.printStackTrace();
        }
        return versao;
    }

    public boolean insertVersion(){
        Statement statement;
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
            statement.close();
        }
        catch(SQLException sqle){
            return false;
        }
        return true;
    }

    public boolean updateVersion()
    {
        int version = getDatabaseVersion();
        try{
            Statement statement = dbConn.createStatement();
            String sqlQuery = "UPDATE configuracoes SET versao='" + ++version + "'WHERE id=" + 1;

            statement.executeUpdate(sqlQuery);

            statement.close();
        }catch(SQLException e){
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
        String sqlQuery = "INSERT INTO utilizador VALUES (NULL, '" + parameters.get(i++) + "' , '" +
                parameters.get(i++) + "' , '" + parameters.get(i++) + "' , '" +
                parameters.get(i++) + "' , '" + parameters.get(i++) + "')";
            
        try{
            statement.executeUpdate(sqlQuery);
            multicastQuery(sqlQuery);
            statement.close();
        }catch (SQLException e){
            return false;
        }
        updateVersion();
        return true;
    }

    public boolean deleteShow(int id)
    {
        try{
            Statement statement = dbConn.createStatement();

            String sqlQuery = "DELETE FROM espetaculo WHERE id=" + id;
            statement.executeUpdate(sqlQuery);
            statement.close();
            multicastQuery(sqlQuery);
        }catch(SQLException e){
            return false;
        }
        updateVersion();
        return true;
    }

    public boolean deleteSeat(int id)
    {
        try{
            Statement statement = dbConn.createStatement();

            String sqlQuery = "DELETE FROM lugar WHERE id=" + id;
            statement.executeUpdate(sqlQuery);
            statement.close();
            multicastQuery(sqlQuery);
        }catch(SQLException e){
            return false;
        }
        updateVersion();
        return true;
    }

    public boolean deleteReservations(int id)
    {
        try{
            Statement statement = dbConn.createStatement();

            String sqlQuery = "DELETE FROM reserva WHERE id=" + id;
            statement.executeUpdate(sqlQuery);
            statement.close();
            multicastQuery(sqlQuery);
        }catch(SQLException e){
            return false;
        }
        updateVersion();
        return true;
    }

    public boolean deleteUsers(int id)
    {
        try{
            Statement statement = dbConn.createStatement();

            String sqlQuery = "DELETE FROM utilizador WHERE id=" + id;
            statement.executeUpdate(sqlQuery);
            statement.close();
            multicastQuery(sqlQuery);
        }catch(SQLException e){
            return false;
        }
        updateVersion();
        return true;
    }

    public boolean updateShows(int id, HashMap<String, String> newData){
        try{
            Statement statement = dbConn.createStatement();
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
                multicastQuery(sqlQuery);
            }

            statement.close();
        }catch(SQLException e){
            return false;
        }
        updateVersion();
        return true;
    }

    public boolean updateSeats(int id, HashMap<String, String> newData){
        try{
            Statement statement = dbConn.createStatement();
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
                multicastQuery(sqlQuery);
            }

            statement.close();
        }catch(SQLException e){
            return false;
        }
        updateVersion();
        return true;
    }

    public boolean updateReservation(int id, HashMap<String, String> newData){
        try{
            Statement statement = dbConn.createStatement();
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
                multicastQuery(sqlQuery);
            }

            statement.close();
        }catch(SQLException e){
            return false;
        }
        updateVersion();
        return true;
    }

    public boolean updateUser(int id, HashMap<String, String> newData){
        try{
            Statement statement = dbConn.createStatement();
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
                multicastQuery(sqlQuery);
            }

            statement.close();
        }catch(SQLException e){
            return false;
        }
        updateVersion();
        return true;
    }
}
