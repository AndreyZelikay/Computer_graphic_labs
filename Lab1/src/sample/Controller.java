package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.text.NumberFormat;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Controller implements Initializable {

    //----CMYK-----

    @FXML
    private Slider cyanSlider;
    @FXML
    private TextField cyanTextField;
    @FXML
    private Slider magentaSlider;
    @FXML
    private TextField magentaTextField;
    @FXML
    private Slider yellowSlider;
    @FXML
    private TextField yellowTextField;
    @FXML
    private Slider keySlider;
    @FXML
    private TextField keyTextField;

    //----LAB-----

    @FXML
    private Slider lSlider;
    @FXML
    private TextField lTextField;
    @FXML
    private Slider aSlider;
    @FXML
    private TextField aTextField;
    @FXML
    private Slider bSlider;
    @FXML
    private TextField bTextField;

    //----HSV-----

    @FXML
    private Slider hueSlider;
    @FXML
    private TextField hueTextField;
    @FXML
    private Slider saturationSlider;
    @FXML
    private TextField saturationTextField;
    @FXML
    private Slider valueSlider;
    @FXML
    private TextField valueTextField;

    @FXML
    private Circle circle;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupColorSelector(cyanSlider, cyanTextField, this::onCYMKEdit);
        setupColorSelector(magentaSlider, magentaTextField, this::onCYMKEdit);
        setupColorSelector(yellowSlider, yellowTextField, this::onCYMKEdit);
        setupColorSelector(keySlider, keyTextField, this::onCYMKEdit);

        setupColorSelector(lSlider, lTextField, this::onLABEdit);
        setupColorSelector(aSlider, aTextField, this::onLABEdit);
        setupColorSelector(bSlider, bTextField, this::onLABEdit);

        setupColorSelector(hueSlider, hueTextField, this::onHSVEdit);
        setupColorSelector(saturationSlider, saturationTextField, this::onHSVEdit);
        setupColorSelector(valueSlider, valueTextField, this::onHSVEdit);
    }

    private void onCYMKEdit() {
        double[] lab = Converter.CMYK_LAB(
                (int) cyanSlider.getValue(),
                (int) magentaSlider.getValue(),
                (int) yellowSlider.getValue(),
                (int) keySlider.getValue()
        );

        lTextField.setText(Double.toString(lab[0]));
        aTextField.setText(Double.toString(lab[1]));
        bTextField.setText(Double.toString(lab[2]));

        double[] hsv = Converter.LAB_HSV(lab[0], lab[1], lab[2]);

        hueTextField.setText(Double.toString(hsv[0]));
        saturationTextField.setText(Double.toString(hsv[1]));
        valueTextField.setText(Double.toString(hsv[2]));

        resetCircleColor();
    }

    private void onLABEdit() {
        double[] hsv = Converter.LAB_HSV(
                lSlider.getValue(),
                aSlider.getValue(),
                bSlider.getValue()
        );

        hueTextField.setText(Double.toString(hsv[0]));
        saturationTextField.setText(Double.toString(hsv[1]));
        valueTextField.setText(Double.toString(hsv[2]));

        double[] cmyk = Converter.HSV_CMYK(hsv[0], hsv[1], hsv[2]);

        cyanTextField.setText(Double.toString(cmyk[0]));
        magentaTextField.setText(Double.toString(cmyk[0]));
        yellowTextField.setText(Double.toString(cmyk[0]));
        keyTextField.setText(Double.toString(cmyk[0]));

        resetCircleColor();
    }

    private void onHSVEdit() {
        double[] cmyk = Converter.HSV_CMYK(
                hueSlider.getValue(),
                saturationSlider.getValue(),
                valueSlider.getValue()
        );

        cyanTextField.setText(Double.toString(cmyk[0]));
        magentaTextField.setText(Double.toString(cmyk[0]));
        yellowTextField.setText(Double.toString(cmyk[0]));
        keyTextField.setText(Double.toString(cmyk[0]));

        double[] lab = Converter.CMYK_LAB(cmyk[0], cmyk[1], cmyk[2], cmyk[3]);

        lTextField.setText(Double.toString(lab[0]));
        aTextField.setText(Double.toString(lab[1]));
        bTextField.setText(Double.toString(lab[2]));

        resetCircleColor();
    }

    private void setupColorSelector(Slider slider, TextField textField, Runnable onColorModelEdit) {
        slider.setValue(0);
        textField.setText(Integer.toString(0));
        textField.textProperty().addListener((obs, oldval, newVal) -> {
            newVal = newVal.replace("0", "");
            if (!newVal.matches("\\d+")) {
                textField.setText(oldval);
            } else {
                textField.setText(newVal);
            }
        });
        textField.textProperty().bindBidirectional(slider.valueProperty(), NumberFormat.getIntegerInstance());
        slider.setOnMouseReleased(e -> onColorModelEdit.run());
    }

    private void resetCircleColor() {
        circle.setFill(Color.hsb(hueSlider.getValue(), saturationSlider.getValue(), valueSlider.getValue()));
    }
}
