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
            "(?<width>\\d+)[xх](?<height>\\d+)[xх](?<dpi>\\d+)(?<compressionType>[a-zA-z0-9+]+)?(?<extension>\\.[a-zA-z]+?$)";


    @FXML
    private Button button;

    @FXML
    private ListView<Label> listView;

    private final FileChooser fileChooser;

    {
        fileChooser = new FileChooser();
        File userDirectory = new File(System.getProperty("user.dir"));
        fileChooser.setInitialDirectory(userDirectory);
    }

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
            throw new RuntimeException("incorrect file");
        }
        return String.format("%s | %s | %s | %s %s",
                file.getName(),
                matcher.group("width") + "x" + matcher.group("height"),
                matcher.group("dpi"),
                file.length() * 8 / Integer.parseInt(matcher.group("width")) / Integer.parseInt(matcher.group("height")),
                (matcher.group("compressionType") != null) ? "| " + matcher.group("compressionType") : ""

        );
    }
}
