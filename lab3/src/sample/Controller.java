package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Function;

public class Controller {

    private final FileChooser fileChooser = new FileChooser();

    @FXML
    private Button button;

    @FXML
    private ListView<VBox> listView;

    public void onClick() {
        File file = fileChooser.showOpenDialog(button.getScene().getWindow());
        BufferedImage image;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "invalid file " + file.getName()).show();
            throw new RuntimeException();
        }

        VBox vBox = new VBox();
        vBox.getChildren().add(new Label("исходное изображение"));
        vBox.getChildren().add(new ImageView(SwingFXUtils.toFXImage(image, null)));

        listView.getItems().add(vBox);

        BufferedImage result = applyLinearContrast(image);

        vBox = new VBox();
        vBox.getChildren().add(new Label("линейное контрастирование"));
        vBox.getChildren().add(new ImageView(SwingFXUtils.toFXImage(result, null)));
        listView.getItems().add(vBox);

        result = applyHistogramEqualizationRGB(image);

        vBox = new VBox();
        vBox.getChildren().add(new Label("эквализация гистограммы по компонентам RGB"));
        vBox.getChildren().add(new ImageView(SwingFXUtils.toFXImage(result, null)));
        listView.getItems().add(vBox);

        result = applyHistogramEqualizationHSV(image);

        vBox = new VBox();
        vBox.getChildren().add(new Label("эквализация гистограммы по яркости HSV"));
        vBox.getChildren().add(new ImageView(SwingFXUtils.toFXImage(result, null)));
        listView.getItems().add(vBox);

        result = applyMedianFilter(image);

        vBox = new VBox();
        vBox.getChildren().add(new Label("медианный фильтр"));
        vBox.getChildren().add(new ImageView(SwingFXUtils.toFXImage(result, null)));
        listView.getItems().add(vBox);

        result = applyMinimumFilter(image);

        vBox = new VBox();
        vBox.getChildren().add(new Label("фильтр минимума"));
        vBox.getChildren().add(new ImageView(SwingFXUtils.toFXImage(result, null)));
        listView.getItems().add(vBox);

        result = applyMaximumFilter(image);

        vBox = new VBox();
        vBox.getChildren().add(new Label("фильтр максимума"));
        vBox.getChildren().add(new ImageView(SwingFXUtils.toFXImage(result, null)));
        listView.getItems().add(vBox);
    }

    private BufferedImage applyHistogramEqualizationRGB(BufferedImage image) {
        int[] rHistogram = new int[256];
        int[] gHistogram = new int[256];
        int[] bHistogram = new int[256];

        for (int y = 0; y < image.getHeight(); ++y) {
            for (int x = 0; x < image.getWidth(); ++x) {
                Color color = new Color(image.getRGB(x, y));
                rHistogram[color.getRed()]++;
                gHistogram[color.getGreen()]++;
                bHistogram[color.getBlue()]++;
            }
        }

        Function<int[], double[]> normalizeHistogram = histogram -> {
            long H = Arrays.stream(histogram).reduce(0, Integer::sum);
            return Arrays.stream(histogram).mapToDouble(value -> (double) value / H).toArray();
        };

        double[] rNormalizedHistogram = normalizeHistogram.apply(rHistogram);
        double[] gNormalizedHistogram = normalizeHistogram.apply(gHistogram);
        double[] bNormalizedHistogram = normalizeHistogram.apply(bHistogram);

        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

        for (int y = 0; y < image.getHeight(); ++y) {
            for (int x = 0; x < image.getWidth(); ++x) {
                Color color = new Color(image.getRGB(x, y));

                int r = (int) (255 * Arrays.stream(rNormalizedHistogram, 0, color.getRed()).reduce(0, Double::sum));
                int g = (int) (255 * Arrays.stream(gNormalizedHistogram, 0, color.getGreen()).reduce(0, Double::sum));
                int b = (int) (255 * Arrays.stream(bNormalizedHistogram, 0, color.getBlue()).reduce(0, Double::sum));

                result.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }

        return result;
    }

    private BufferedImage applyHistogramEqualizationHSV(BufferedImage image) {
        int[] histogram = new int[100];

        for (int y = 0; y < image.getHeight(); ++y) {
            for (int x = 0; x < image.getWidth(); ++x) {
                Color color = new Color(image.getRGB(x, y));
                int brightness = (int) (100 * Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null)[2]);
                histogram[brightness]++;
            }
        }

        long H = Arrays.stream(histogram).reduce(0, Integer::sum);
        double[] normalizedHistogram = Arrays.stream(histogram).mapToDouble(value -> (double) value / H).toArray();

        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

        for (int y = 0; y < image.getHeight(); ++y) {
            for (int x = 0; x < image.getWidth(); ++x) {
                Color color = new Color(image.getRGB(x, y));

                float[] hsv = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);

                int currentBrightness = (int) (100 * hsv[2]);
                int newBrightness = (int) (100 * Arrays.stream(normalizedHistogram, 0, currentBrightness).reduce(0, Double::sum));

                result.setRGB(x, y, Color.HSBtoRGB(hsv[0], hsv[1], (float) newBrightness / 100));
            }
        }

        return result;
    }

    private BufferedImage applyLinearContrast(BufferedImage image) {
        Color firstPixelColor = new Color(image.getRGB(0, 0));

        int rMin = firstPixelColor.getRed();
        int rMax = firstPixelColor.getRed();

        int gMin = firstPixelColor.getGreen();
        int gMax = firstPixelColor.getGreen();

        int bMin = firstPixelColor.getBlue();
        int bMax = firstPixelColor.getBlue();

        for (int y = 0; y < image.getHeight(); ++y) {
            for (int x = 0; x < image.getWidth(); ++x) {
                Color color = new Color(image.getRGB(x, y));

                if (color.getRed() < rMin) {
                    rMin = color.getRed();
                } else if (color.getRed() > rMax) {
                    rMax = color.getRed();
                }

                if (color.getGreen() < gMin) {
                    gMin = color.getGreen();
                } else if (color.getGreen() > gMax) {
                    gMax = color.getGreen();
                }

                if (color.getBlue() < bMin) {
                    bMin = color.getBlue();
                } else if (color.getBlue() > bMax) {
                    bMax = color.getBlue();
                }
            }
        }

        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

        for (int y = 0; y < image.getHeight(); ++y) {
            for (int x = 0; x < image.getWidth(); ++x) {
                Color color = new Color(image.getRGB(x, y));

                int r = 255 * (color.getRed() - rMin) / (rMax - rMin);
                int g = 255 * (color.getGreen() - gMin) / (gMax - gMin);
                int b = 255 * (color.getBlue() - bMin) / (bMax - bMin);

                result.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }

        return result;
    }

    private BufferedImage applyMedianFilter(BufferedImage image) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

        for (int y = 1; y < image.getHeight() - 1; ++y) {
            for (int x = 1; x < image.getWidth() - 1; ++x) {
                int[] rValues = new int[8];
                int[] gValues = new int[8];
                int[] bValues = new int[8];

                int counter = 0;
                for(int i = y - 1; i < y + 2; i++) {
                    for(int j = x - 1; j < x + 2; j++) {
                        if(i == y && j == x) {
                            continue;
                        }
                        Color color = new Color(image.getRGB(j, i));
                        rValues[counter] = color.getRed();
                        gValues[counter] = color.getGreen();
                        bValues[counter] = color.getBlue();
                        counter++;
                    }
                }

                Arrays.sort(rValues);
                Arrays.sort(gValues);
                Arrays.sort(bValues);

                int r = (rValues[3] + rValues[4]) / 2;
                int g = (gValues[3] + gValues[4]) / 2;
                int b = (bValues[3] + bValues[4]) / 2;

                result.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }

        return result;
    }

    private BufferedImage applyMinimumFilter(BufferedImage image) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

        for (int y = 1; y < image.getHeight() - 1; ++y) {
            for (int x = 1; x < image.getWidth() - 1; ++x) {
                int[] rValues = new int[8];
                int[] gValues = new int[8];
                int[] bValues = new int[8];

                int counter = 0;
                for(int i = y - 1; i < y + 2; i++) {
                    for(int j = x - 1; j < x + 2; j++) {
                        if(i == y && j == x) {
                            continue;
                        }
                        Color color = new Color(image.getRGB(j, i));
                        rValues[counter] = color.getRed();
                        gValues[counter] = color.getGreen();
                        bValues[counter] = color.getBlue();
                        counter++;
                    }
                }

                int r = Arrays.stream(rValues).min().getAsInt();
                int g = Arrays.stream(gValues).min().getAsInt();
                int b = Arrays.stream(bValues).min().getAsInt();

                result.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }

        return result;
    }

    private BufferedImage applyMaximumFilter(BufferedImage image) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

        for (int y = 1; y < image.getHeight() - 1; ++y) {
            for (int x = 1; x < image.getWidth() - 1; ++x) {
                int[] rValues = new int[8];
                int[] gValues = new int[8];
                int[] bValues = new int[8];

                int counter = 0;
                for(int i = y - 1; i < y + 2; i++) {
                    for(int j = x - 1; j < x + 2; j++) {
                        if(i == y && j == x) {
                            continue;
                        }
                        Color color = new Color(image.getRGB(j, i));
                        rValues[counter] = color.getRed();
                        gValues[counter] = color.getGreen();
                        bValues[counter] = color.getBlue();
                        counter++;
                    }
                }

                int r = Arrays.stream(rValues).max().getAsInt();
                int g = Arrays.stream(gValues).max().getAsInt();
                int b = Arrays.stream(bValues).max().getAsInt();

                result.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }

        return result;
    }

}
