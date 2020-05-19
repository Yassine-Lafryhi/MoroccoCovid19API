package ma.covid19.api;

public class Case {
    private String date;
    private String type;
    private int number;

    public Case() {
    }

    public Case(String type, String date, int number) {
        this.date = date;
        this.type = type;
        this.number = number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }


}
