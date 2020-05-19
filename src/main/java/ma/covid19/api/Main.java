package ma.covid19.api;

import com.google.gson.Gson;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import static spark.Spark.*;

public class Main {
    public static void main(String[] args) {
        port(80);
        Database.connect();

        get("/cases/:type/", (req, res) ->
        {
            res.type("application/json");
            String type = req.params(":type");
            Gson gson = new Gson();
            ArrayList<Case> cases = new ArrayList<>();
            if (type.equals("confirmed") || type.equals("recovered") || type.equals("died")) {
                cases = select(type);
            }
            return gson.toJson(cases);
        });


        get("/cases/", (req, res) ->
        {
            res.type("application/json");
            Gson gson = new Gson();
            ArrayList<Case> cases = selectAllCases();
            return gson.toJson(cases);
        });


        options("/*",
                (request, response) -> {
                    String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
                    if (accessControlRequestHeaders != null) {
                        response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
                    }
                    String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
                    if (accessControlRequestMethod != null) {
                        response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
                    }
                    return "OK";
                });
        before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));


    }

    private static ArrayList<Case> select(String type) {
        ArrayList<Case> cases = new ArrayList<>();
        try {
            String query = "SELECT Number,Date FROM Cases WHERE Type = '" + type + "'";
            System.out.println(query);
            Statement statement = Database.connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                int number = resultSet.getInt(1);
                String date = resultSet.getString(2);
                Case data = new Case(type, date, number);
                cases.add(data);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cases;
    }

    private static ArrayList<Case> selectAllCases() {
        ArrayList<Case> cases = new ArrayList<>();
        try {
            String query = "SELECT Number,Date,Type FROM Cases";
            System.out.println(query);
            Statement statement = Database.connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                int number = resultSet.getInt(1);
                String date = resultSet.getString(2);
                String type = resultSet.getString(3);
                Case data = new Case(type, date, number);
                cases.add(data);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cases;
    }

}
