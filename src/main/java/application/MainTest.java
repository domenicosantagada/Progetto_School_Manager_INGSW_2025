package application;

import application.persistence.Database;

public class MainTest {
    public static void main(String[] args) {
        Database db = Database.getInstance();

        System.out.println(db.getAllMaterieIstituto());

//        System.out.println(db.deleteElaboratiByStudente("stud4"));

    }
}
