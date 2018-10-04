/* this class is the entry point for the program and sets up javafx */
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("gui-styles.css").toExternalForm());

        primaryStage.setTitle("S3 - Dropbox");
        primaryStage.setScene(scene);

        Image icon = new Image(getClass().getResourceAsStream("bucketwithobjects.png"));
        primaryStage.getIcons().add(icon);

        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
