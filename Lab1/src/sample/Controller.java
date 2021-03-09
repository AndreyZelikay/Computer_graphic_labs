package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.ResourceBundle;

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

    @FXML
    private ColorPicker colorPicker;

    private final Color defaultColor = Color.rgb(255, 255, 255);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colorPicker.setValue(defaultColor);
        circle.setFill(defaultColor);

        onCYMKEdit();

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
        double[] cmyk = new double[]{
                cyanSlider.getValue(),
                magentaSlider.getValue(),
                yellowSlider.getValue(),
                keySlider.getValue()
        };

        double[] lab = Converter.CMYK_LAB(cmyk[0], cmyk[1], cmyk[2], cmyk[3]);

        lSlider.setValue(lab[0]);
        aSlider.setValue(lab[1]);
        bSlider.setValue(lab[2]);

        double[] hsv = Converter.CMYK_HSV(cmyk[0], cmyk[1], cmyk[2], cmyk[3]);

        hueSlider.setValue(hsv[0]);
        saturationSlider.setValue(hsv[1]);
        valueSlider.setValue(hsv[2]);

        resetCircleColor(Converter.CMYK_RGB(cmyk[0], cmyk[1], cmyk[2], cmyk[3]));
    }

    private void onLABEdit() {
        double[] lab = new double[]{
                lSlider.getValue(),
                aSlider.getValue(),
                bSlider.getValue()
        };

        double[] hsv;

        try {
            hsv = Converter.LAB_HSV(lab[0], lab[1], lab[2]);
        } catch (RuntimeException e) {
            lab = Converter.CMYK_LAB(
                    cyanSlider.getValue(),
                    magentaSlider.getValue(),
                    yellowSlider.getValue(),
                    keySlider.getValue()
            );

            lSlider.setValue(lab[0]);
            aSlider.setValue(lab[1]);
            bSlider.setValue(lab[2]);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Lab conversion");
            alert.setContentText(e.getMessage());
            alert.show();

            return;
        }

        hueSlider.setValue(hsv[0]);
        saturationSlider.setValue(hsv[1]);
        valueSlider.setValue(hsv[2]);

        double[] cmyk = Converter.HSV_CMYK(hsv[0], hsv[1], hsv[2]);

        cyanSlider.setValue(cmyk[0]);
        magentaSlider.setValue(cmyk[1]);
        yellowSlider.setValue(cmyk[2]);
        keySlider.setValue(cmyk[3]);

        resetCircleColor(Converter.LAB_RGB(lab[0], lab[1], lab[2]));
    }

    private void onHSVEdit() {
        double[] hsv = new double[]{
                hueSlider.getValue(),
                saturationSlider.getValue(),
                valueSlider.getValue()
        };

        double[] cmyk = Converter.HSV_CMYK(hsv[0], hsv[1], hsv[2]);

        cyanSlider.setValue(cmyk[0]);
        magentaSlider.setValue(cmyk[1]);
        yellowSlider.setValue(cmyk[2]);
        keySlider.setValue(cmyk[3]);

        double[] lab = Converter.CMYK_LAB(cmyk[0], cmyk[1], cmyk[2], cmyk[3]);

        lSlider.setValue(lab[0]);
        aSlider.setValue(lab[1]);
        bSlider.setValue(lab[2]);

        resetCircleColor(Converter.HSV_RGB(hsv[0], hsv[1], hsv[2]));
    }

    private void setupColorSelector(Slider slider, TextField textField, Runnable onColorModelEdit) {
        slider.setValue(0);
        textField.setText(Integer.toString(0));

        textField.focusedProperty().addListener((obs, oldval, newval) -> {
            if (newval) {
                textField.setText("");
                return;
            }

            if (!textField.getText().matches("-?\\d+(\\.(\\d+)?)?")) {
                textField.setText(String.valueOf(0));
                return;
            }

            double value = Double.parseDouble(textField.getText());

            if (value >= slider.getMin() && value <= slider.getMax()) {
                slider.setValue(value);
                onColorModelEdit.run();
            } else {
                textField.setText(String.valueOf(0));
            }
        });

        slider.valueProperty().addListener((obs, oldval, newval) -> {
            if (slider.getMax() - slider.getMin() > 1) {
                textField.setText(String.valueOf(newval.intValue()));
            } else {
                textField.setText(String.valueOf(newval.doubleValue()));
            }
        });

        slider.setOnMouseReleased(e -> onColorModelEdit.run());
    }

    public void onColorPicker() {
        hueSlider.setValue(colorPicker.getValue().getHue() / 360);
        saturationSlider.setValue(colorPicker.getValue().getSaturation());
        valueSlider.setValue(colorPicker.getValue().getBrightness());
        onHSVEdit();
    }

    private void resetCircleColor(int[] rgb) {
        colorPicker.setValue(Color.rgb(rgb[0], rgb[1], rgb[2]));
        circle.setFill(Color.rgb(rgb[0], rgb[1], rgb[2]));
    }
}
