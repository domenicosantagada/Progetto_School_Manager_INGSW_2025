package application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneHandler {

    private static SceneHandler instance = null;

    private Scene scene;
    private Stage stage;
    private String username;

    private SceneHandler() {
    }

    public static SceneHandler getInstance() {
        if (instance == null)
            instance = new SceneHandler();
        return instance;
    }

    public void init(Stage stage) throws Exception {
        this.stage = stage;
        setLoginPage();
    }

    public String getUsername() {
        return username;
    }

    private void loadPage(FXMLLoader loader) throws IOException {
        Parent root = loader.load();
        this.stage = stage;
        scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("MyClassRoom");
        stage.setFullScreen(false);
        stage.setResizable(true);
        stage.setWidth(1050);
        stage.setHeight(760);
        stage.show();
    }

    public void setLoginPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
        loadPage(loader);
    }

    public void setProfessorHomePage(String username) throws IOException {
        this.username = username;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/prof/ProfHomePage.fxml"));
        loadPage(loader);
    }

    public void setStudentHomePage(String username) throws IOException {
        this.username = username;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/studente/StudentHomePage.fxml"));
        loadPage(loader);
    }

    public void setRegistrationPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Registration.fxml"));
        loadPage(loader);
    }

    public void setStudentsListPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/prof/StudentiProfPage.fxml"));
        loadPage(loader);
    }

    private void loadPopUp(Alert alert, String title, String message) {
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText("");
        alert.setContentText(message);
        DialogPane dialog = alert.getDialogPane();
        alert.showAndWait();
    }

    public void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        loadPopUp(alert, "Attenzione", message);
    }

    public void showInformation(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        loadPopUp(alert, "Informazione", message);
    }

    public void setAssignmentPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/prof/CompitiProfPage.fxml"));
        loadPage(loader);
    }

    public void setVotesPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/prof/AssenzeProfPage.fxml"));
        loadPage(loader);
    }

    public void setAndamentoPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/studente/AndamentoStudentPage.fxml"));
        loadPage(loader);
    }

    public void setCompitiPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/studente/CompitiStudentPage.fxml"));
        loadPage(loader);
    }

    public void setNotePage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/studente/NoteStudentPage.fxml"));
        loadPage(loader);
    }
}