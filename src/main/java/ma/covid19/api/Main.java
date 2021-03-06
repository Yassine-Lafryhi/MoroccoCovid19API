package ma.covid19.api;

import com.google.gson.Gson;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

import static spark.Spark.*;

public class Main {
    public static void main(String[] args) {
        port(80);
        Database.connect();

        notFound((req, res) -> {
            res.type("application/json");
            Gson gson = new Gson();
            HashMap<String, String> data = new HashMap<>();
            data.put("message", "Not Found");
            data.put("documentation_url", "https://github.com/Yassine-Lafryhi/MoroccoCovid19API");
            return gson.toJson(data);
        });

        get("/cases/:type/:day/:month/:year", (req, res) ->
        {
            res.type("application/json");
            String date = req.params(":day") + "-" + req.params(":month") + "-" + req.params(":year");
            String type = req.params(":type");
            Gson gson = new Gson();
            Case caseByDate = null;
            if (type.equals("confirmed") || type.equals("recovered") || type.equals("deaths")) {
                caseByDate = select(type, date);
            }
            return gson.toJson(caseByDate);
        });


        get("/cases/:type/:day/:month/:year/daily", (req, res) ->
        {
            res.type("application/json");
            String date = req.params(":day") + "-" + req.params(":month") + "-" + req.params(":year");
            String type = req.params(":type");
            Gson gson = new Gson();
            Case caseByDate = null;
            if (type.equals("confirmed") || type.equals("recovered") || type.equals("deaths")) {
                caseByDate = selectDaily(type, date);
            }
            return gson.toJson(caseByDate);
        });


        get("/cases/:type", (req, res) ->
        {
            res.type("application/json");
            String type = req.params(":type");
            Gson gson = new Gson();
            ArrayList<Case> cases = new ArrayList<>();
            if (type.equals("confirmed") || type.equals("recovered") || type.equals("deaths")) {
                cases = select(type);
            }
            return gson.toJson(cases);
        });


        get("/cases/:type/daily", (req, res) ->
        {
            res.type("application/json");
            String type = req.params(":type");
            Gson gson = new Gson();
            ArrayList<Case> cases = new ArrayList<>();
            if (type.equals("confirmed") || type.equals("recovered") || type.equals("deaths")) {
                cases = selectDaily(type);
            }
            return gson.toJson(cases);
        });


        get("/cases", (req, res) ->
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


    private static Case select(String type, String date) {
        Case caseByDate = new Case();
        try {
            String query = "SELECT Number FROM Cases WHERE Type = '" + type + "' AND Date = '" + date + "'";
            System.out.println(query);
            Statement statement = Database.connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            resultSet.next();
            caseByDate.setDate(date);
            caseByDate.setType(type);
            caseByDate.setNumber(resultSet.getInt(1));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return caseByDate;
    }

    private static Case selectDaily(String type, String date) {
        Case dailyCase = new Case();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate localDate = LocalDate.parse(date, dateTimeFormatter);
        String yesterday = localDate.minusDays(1).format(dateTimeFormatter);
        try {
            String query = "SELECT Number FROM Cases WHERE Type = '" + type + "' AND Date = '" + date + "'";
            Statement statement = Database.connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            resultSet.next();
            int todayCases = resultSet.getInt(1);

            query = "SELECT Number FROM Cases WHERE Type = '" + type + "' AND Date = '" + yesterday + "'";
            statement = Database.connection.createStatement();
            resultSet = statement.executeQuery(query);
            resultSet.next();
            int yesterdayCases = resultSet.getInt(1);

            dailyCase.setDate(date);
            dailyCase.setType(type);
            dailyCase.setNumber(todayCases - yesterdayCases);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dailyCase;
    }

    private static ArrayList<Case> selectDaily(String type) {
        ArrayList<Case> cases = new ArrayList<>();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        try {
            String query = "SELECT Date FROM Cases WHERE Type = '" + type + "'";
            System.out.println(query);
            Statement statement = Database.connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                String date = resultSet.getString(1);
                LocalDate localDate = LocalDate.parse(date, dateTimeFormatter);
                String yesterday = localDate.minusDays(1).format(dateTimeFormatter);

                query = "SELECT Number FROM Cases WHERE Type = '" + type + "' AND Date = '" + date + "'";
                statement = Database.connection.createStatement();
                ResultSet resultSet2 = statement.executeQuery(query);
                resultSet2.next();
                int todayCases = resultSet2.getInt(1);

                query = "SELECT Number FROM Cases WHERE Type = '" + type + "' AND Date = '" + yesterday + "'";
                statement = Database.connection.createStatement();
                ResultSet resultSet3 = statement.executeQuery(query);

                int yesterdayCases = 0;
                if (resultSet3.next()) {
                    yesterdayCases = resultSet3.getInt(1);
                }

                Case data = new Case(type, date, todayCases - yesterdayCases);
                cases.add(data);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cases;
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
