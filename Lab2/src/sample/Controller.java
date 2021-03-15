package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Controller {

    private static final String fileNameRegex =
            "(?<imageSize>\\d+x\\d+)x(?<dpi>\\d+)?(?<compressionType>[a-zA-z]+)?(?<extension>\\.[a-zA-z]+)";


    @FXML
    private Button button;

    @FXML
    private ListView<Label> listView;

    private final FileChooser fileChooser = new FileChooser();

    public void onClick() {
        listView.getItems().clear();
        listView.getItems()
                .addAll(fileChooser.showOpenMultipleDialog(button.getScene().getWindow())
                        .stream()
                        .map(this::getString)
                        .map(Label::new)
                        .collect(Collectors.toList())
                );
    }

    private String getString(File file) {
        Matcher matcher = Pattern.compile(fileNameRegex).matcher(file.getName());
        if(!matcher.matches()) {
            throw new RuntimeException("incorrect file name");
        }
        return String.format("%s/ %s", matcher.group("imageSize"), matcher.group("dpi"));
    }
}
