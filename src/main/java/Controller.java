import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import java.io.*;
import java.util.List;


public class Controller {
    private S3Connector s3Connector;
    private String currentBucket;
    @FXML
    private Label statusLabel;
    @FXML
    private Label header;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private ChoiceBox<String> choiceBox;
    @FXML
    private ListView<String> listView;

    @FXML
    private Button downloadButton;


    @FXML
    public void downloadFile() {
        ObservableList selectedIndices = listView.getSelectionModel().getSelectedIndices();
        String filename;
        for(Object obj : selectedIndices){
            System.out.println(this.listView.getItems().get((Integer)obj));
            filename = this.listView.getItems().get((int)obj);

            // download this file from S3 bucket
            this.s3Connector.downloadFile(filename);
            this.statusLabel.setText("File "+ filename+ " has been downloaded");
        }
    }

    // initialize S3 add event listiners to ChoiceBox
    @FXML
    public void initialize() {
        this.s3Connector = new S3Connector();
        s3Connector.listBuckets();

        String firstBucket = s3Connector.buckets().get(0).getName();
        for (Bucket bucket : s3Connector.buckets()) {
            System.out.println(bucket.getName());
            this.choiceBox.getItems().add(bucket.getName());
        }
        //this.choiceBox.setValue(firstBucket);

        // add event listener via Lambda
        this.choiceBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) ->
                        getBucketName(newValue));
    }

    @FXML
    public void fileDragOver(DragEvent de) {
        //System.out.println("in imageDragOver");
        Dragboard board = de.getDragboard();
        if (board.hasFiles()) {
            //System.out.println("has file");
            de.acceptTransferModes(TransferMode.ANY);
        }
    }

    @FXML
    public void fileDropped(DragEvent de) throws IOException {
       // System.out.println("in image drop");
        try {
            Dragboard board = de.getDragboard();
            List<File> phil = board.getFiles();
            FileInputStream fis;
            fis = new FileInputStream(phil.get(0));
            String filename = phil.get(0).getName();
            byte[] fileBuffer = new byte[fis.available()];
            fis.read(fileBuffer);

            File targetFile = new File(filename);
            OutputStream outputStream = new FileOutputStream(targetFile);
            outputStream.write(fileBuffer);

            this.s3Connector.uploadFile(targetFile);
            this.statusLabel.setText("File "+ filename+ " has been uploaded to your selected bucket");

            // delete file after it has been uploaded to S3
            targetFile.delete();

            listBucketContents(this.s3Connector.getCurrentBucket());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void getBucketName(String bucketName) {
        listBucketContents(bucketName);
        //System.out.println(bucketName);
    }

    private void listBucketContents(String bucketName) {
        this.listView.getItems().clear();
        System.out.println("Listing objects for "+ bucketName);
        ListObjectsV2Result result = this.s3Connector.getS3().listObjectsV2(bucketName);
        List<S3ObjectSummary> objects = result.getObjectSummaries();
        for (S3ObjectSummary os: objects) {
            if(!os.getKey().equals("MyObjectKey")) {
                this.statusLabel.setText("Listing Contents");
                this.listView.getItems().add(os.getKey());
                this.s3Connector.setCurrentBucket(bucketName);
            }
            else {
                this.statusLabel.setText("Empty Bucket");
                this.s3Connector.setCurrentBucket(bucketName);
            }
        }
    }

}
