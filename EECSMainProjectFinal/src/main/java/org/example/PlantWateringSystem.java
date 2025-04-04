package org.example;

import org.firmata4j.IODevice;
import org.firmata4j.I2CDevice;
import org.firmata4j.Pin;
import org.firmata4j.firmata.FirmataDevice;
import org.firmata4j.ssd1306.SSD1306;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * EECS1021 Main Project - Automated Plant Watering System
 * Controls a water pump based on soil moisture levels using Java and Arduino (Firmata4j).
 * Displays sensor readings in real time using JFreeChart and OLED.
 */
public class PlantWateringSystem {
    private static final String PORT = "/dev/cu.usbserial-0001"; // Change to match your port
    private static final int MOISTURE_SENSOR_PIN = 14; // Analog pin A0
    private static final int PUMP_PIN = 2; // Digital pin D2
    private static final int MOISTURE_THRESHOLD = 800;

    private static final TimeSeries moistureSeries = new TimeSeries("Moisture Level");
    private static final ArrayList<Integer> moistureReadings = new ArrayList<>();

    private static void showGraphWindow() {
        TimeSeriesCollection dataset = new TimeSeriesCollection(moistureSeries);
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Soil Moisture Over Time",
                "Time",
                "Moisture Level",
                dataset,
                true,
                true,
                false
        );
        JFrame frame = new JFrame("Moisture Sensor Chart");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ChartPanel(chart));
        frame.pack();
        frame.setVisible(true);
    }

    public static int normalizeYValue(int originalValue) {
        final float MAXVALUE = 1023.0F;
        final float MINVALUE = 0.0F;
        final int ERRORVALUE = -1000;

        if (originalValue > MAXVALUE || originalValue < MINVALUE) {
            return ERRORVALUE;
        } else {
            return (int) (100.0 * (originalValue * (1.0 / (MAXVALUE - MINVALUE))));
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("Connecting to Arduino...");
            IODevice arduino = new FirmataDevice(PORT);
            I2CDevice oledDevice = arduino.getI2CDevice((byte) 0x3C);
            SSD1306 oled = new SSD1306(oledDevice, 128, 64, false); // ✅ pass width & height directly

            arduino.start();
            Thread.sleep(5000); // Allow Arduino to reset
            arduino.ensureInitializationIsDone();

            oled.init();
            oled.clear();
            oled.display();

            ShapeRenderer renderer = new ShapeRenderer(oled, 128, 64);

            System.out.println("Arduino connected.");

            Pin moistureSensor = arduino.getPin(MOISTURE_SENSOR_PIN);
            Pin pump = arduino.getPin(PUMP_PIN);
            pump.setMode(Pin.Mode.OUTPUT);

            SwingUtilities.invokeLater(PlantWateringSystem::showGraphWindow);

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        int rawValue = moistureSensor.getValue().intValue();
                        int normalized = normalizeYValue(rawValue);

                        if (normalized == -1000) {
                            System.out.println("Invalid sensor reading.");
                            renderer.drawTextStatus("Sensor Error", "");
                            return;
                        }

                        moistureReadings.add(normalized);
                        moistureSeries.addOrUpdate(new Second(), rawValue);

                        System.out.println("Moisture Level: " + rawValue + " (" + normalized + "%)");

                        if (rawValue < MOISTURE_THRESHOLD) {
                            System.out.println("Dry soil detected. Pump ON.");
                            pump.setValue(1);
                            renderer.drawTextStatus("Moisture: " + normalized + "%", "Pump: ON");
                            Thread.sleep(5000);
                            pump.setValue(0);
                            System.out.println("Pump OFF.");
                        } else {
                            renderer.drawTextStatus("Moisture: " + normalized + "%", "Pump: OFF");
                            System.out.println("Soil is moist. Pump remains OFF.");
                            pump.setValue(0);
                        }
                    } catch (Exception e) {
                        System.err.println("Sensor error: " + e.getMessage());
                    }
                }
            }, 0, 5000);

        } catch (Exception e) {
            System.err.println("Failed to connect: " + e.getMessage());
        }
    }
}
