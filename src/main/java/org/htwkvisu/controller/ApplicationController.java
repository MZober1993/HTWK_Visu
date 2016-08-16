package org.htwkvisu.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.htwkvisu.gui.MapCanvas;
import org.htwkvisu.gui.NumericTextField;
import org.htwkvisu.gui.ScoringConfig;
import org.htwkvisu.model.ScoreTableModel;
import org.htwkvisu.org.pois.Category;
import org.htwkvisu.org.pois.ScoreType;
import org.htwkvisu.org.pois.ScoringCalculator;
import org.htwkvisu.scoring.IFallOf;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Control for GUI changes for App
 */
public class ApplicationController implements Initializable {


    @FXML
    private TableColumn<Double, Double> weightColumn;
    @FXML
    private TableColumn<Double, Double> paramOneColumn;
    @FXML
    private TableColumn<Double, Double> paramTwoColumn;
    @FXML
    private TableColumn<Double, Double> paramThreeColumn;
    /*@FXML
            private ComboBox categoryBox;
            @FXML
            private ComboBox scoreTypeBox;*/
    @FXML
    private TableColumn<Boolean, Boolean> enabled;
    @FXML
    private TableColumn<Category, String> categoryColumn;
    @FXML
    private TableColumn<ScoreType, String> scoreColumn;
    @FXML
    public TableColumn<IFallOf, String> fallOfColumn;
    @FXML
    private NumericTextField pixelDensityTextField;

    @FXML
    private TableView<ScoreTableModel> tableView;

    private static final int DEFAULT_PIXEL_DENSITY = 30;
    private static final int MAX_NUMERIC_FIELD_LENGTH = 6;

    @FXML
    private NumericTextField minScoringTextField;
    private static final int DEFAULT_MIN_SCORING_VALUE = 0;

    @FXML
    private NumericTextField maxScoringTextField;
    private static final int DEFAULT_MAX_SCORING_VALUE = 100000;

    @FXML
    private Button resetViewButton;

    @FXML
    private Label messageLabel;

    @FXML
    private Pane canvasPane;

    private MapCanvas canvas;
    private ScoringConfig config;

    /**
     * Write message to status bar.
     *
     * @param message            Message to show
     * @param isImportantMessage Show the message in red if it's important
     */
    public void writeStatusMessage(String message, boolean isImportantMessage) {
        if (isImportantMessage) {
            messageLabel.setTextFill(Color.RED);
        } else {
            messageLabel.setTextFill(Color.GRAY);
        }

        messageLabel.setText(message);
    }

    /**
     * Default JavaFX initilization method for controllers.
     * Implemented from Initializable-interface.
     *
     * @param location  Location
     * @param resources Resources
     */
    public void initialize(URL location, ResourceBundle resources) {

        initCanvas();
        initNumericTextFields(pixelDensityTextField, DEFAULT_PIXEL_DENSITY);
        initNumericTextFields(minScoringTextField, DEFAULT_MIN_SCORING_VALUE);
        initNumericTextFields(maxScoringTextField, DEFAULT_MAX_SCORING_VALUE);

        initTable();
        // initComboBox();

        Logger.getGlobal().log(Level.INFO, "ApplicationController initialized!");

    }

   /* private void initComboBox() {
        categoryBox.setItems(FXCollections.observableList(Arrays.asList(Category.values())));
        categoryBox.setValue(categoryBox.getItems().get(0));

        Category category = (Category) categoryBox.getItems().get(0);

        scoreTypeBox.setItems(FXCollections.observableList(category.getTypes()));
        scoreTypeBox.setValue(scoreTypeBox.getItems().get(0));

        categoryBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            Category newCat = (Category) newValue;
            scoreTypeBox.setItems(FXCollections.observableList(newCat.getTypes()));
            scoreTypeBox.setValue(scoreTypeBox.getItems().get(0));
        });

    } */

    private void initTable() {
        tableView.setEditable(true);
        onDoubleTableColumnKeyPressed();
        enabled.setCellValueFactory(new PropertyValueFactory<>("enabled"));
        enabled.setCellFactory(CheckBoxTableCell.forTableColumn(enabled));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        fallOfColumn.setCellValueFactory(new PropertyValueFactory<>("fallOf"));
        paramOneColumn.setCellValueFactory(new PropertyValueFactory<>("paramOne"));
        paramTwoColumn.setCellValueFactory(new PropertyValueFactory<>("paramTwo"));
        paramThreeColumn.setCellValueFactory(new PropertyValueFactory<>("paramThree"));
        weightColumn.setCellValueFactory(new PropertyValueFactory<>("weight"));
        paramOneColumn.setEditable(true);

        //TODO: please remove this - read property-file and set value from them
        Category.EDUCATION.setEnabledForCategory(true);

        List<ScoreTableModel> tableModels = ScoringCalculator.calcAllTableModels();
        tableView.setItems(FXCollections.observableList(tableModels));
    }

    private void onDoubleTableColumnKeyPressed() {
        paramOneColumn.setOnEditCommit(event -> {
            long value = event.getNewValue().longValue();
            Logger.getGlobal().info("radius pressed");
            if (value > 0) {
                ScoreTableModel scoreTableModel = tableView.getItems().get(event.getTablePosition().getRow());
                scoreTableModel.setParamOne(value);
                Logger.getGlobal().info("set radius of type:" + scoreTableModel.getType().name() + ", value: " + value);
            }
        });
        paramTwoColumn.setOnEditCommit(event -> {
            long value = event.getNewValue().longValue();
            if (value > 0) {
                tableView.getItems().get(event.getTablePosition().getRow()).setParamTwo(value);
            }
        });
        paramThreeColumn.setOnEditCommit(event -> {
            long value = event.getNewValue().longValue();
            if (value > 0) {
                tableView.getItems().get(event.getTablePosition().getRow()).setParamThree(value);
            }
        });
        weightColumn.setOnEditCommit(event -> {
            long value = event.getNewValue().longValue();
            if (value > 0) {
                tableView.getItems().get(event.getTablePosition().getRow()).setWeight(value);
            }
        });
    }

    private void initNumericTextFields(NumericTextField numericTextField, final int value) {
        numericTextField.setMaxlength(MAX_NUMERIC_FIELD_LENGTH);
        numericTextField.setDefaultValue(value);
        numericTextField.setText(Integer.toString(value));
    }

    private void initCanvas() {
        config = new ScoringConfig(DEFAULT_PIXEL_DENSITY, DEFAULT_MIN_SCORING_VALUE, DEFAULT_MAX_SCORING_VALUE);
        canvas = new MapCanvas(config);
        canvasPane.getChildren().add(canvas);
        canvasPane.widthProperty().addListener((observable, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        canvasPane.heightProperty().addListener((observable, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));
    }


    /**
     * Center to view to default point.
     *
     * @param ev Mouse click event
     */
    @FXML
    public void onResetViewClicked(MouseEvent ev) {
        canvas.centerView(new Point2D(51, 13)); // test value as an example!
    }

    /**
     * Refreshes Values for textFields
     *
     * @param ev Key Event
     */
    @FXML
    public void onEnterPressed(KeyEvent ev) {
        if (ev.getCode().equals(KeyCode.ENTER)) {
            config.changeValueOfNumericScoringTextField(
                    (NumericTextField) ev.getSource(),
                    pixelDensityTextField, minScoringTextField, maxScoringTextField);
        }
    }

}
