package org.firstinspires.ftc.teamcode.subsystems;

import android.graphics.Color;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;

// this class handles all the color sensor stuff so we don't have to clutter the main opmode
public class ColorSensorSubsystem {
    // the actual color sensor hardware
    private NormalizedColorSensor colorSensor;
    // array to store HSV values (hue, saturation, value)
    private final float[] hsvValues = new float[3];

    // constructor - gets called when we create a new ColorSensorSubsystem
    public ColorSensorSubsystem(HardwareMap hardwareMap) {
        // find the color sensor in the hardware map
        colorSensor = hardwareMap.get(NormalizedColorSensor.class, "sensor_color");
    }

    // returns what color the sensor sees as a string
    public String getDetectedColor() {
        // get the raw RGB color from the sensor
        NormalizedRGBA colors = colorSensor.getNormalizedColors();

        // convert RGB to HSV (easier for detecting specific colors)
        Color.RGBToHSV(
                (int) (colors.red * 255),
                (int) (colors.green * 255),
                (int) (colors.blue * 255),
                hsvValues
        );

        // grab the hue and saturation values
        float hue = hsvValues[0];
        float saturation = hsvValues[1];

        String detectedColor = "idk bruh";

        // if the color is bright/saturated enough, check what color it actually is
        if (saturation > 0.3) {
            // hue 145-180 is green
            if (hue >= 145 && hue <= 180) {
                detectedColor = "Green";
            }
            // hue 225-250 is purple
            else if (hue >= 225 && hue <= 250) { //also made sure that it wasn't too low of a number that it detected it as blue because of frame color
                detectedColor = "Purple";
            }
        }

        return detectedColor;
    }

    // returns just the hue value (angle of the color on the color wheel)
    public float getHue() {
        return hsvValues[0];
    }

    // returns just the saturation value
    public float getSaturation() {
        return hsvValues[1];
    }

    // updates the hsvValues array with the latest sensor reading
    public void update() {
        // get the raw RGB color from the sensor
        NormalizedRGBA colors = colorSensor.getNormalizedColors();

        // convert RGB to HSV and store it in hsvValues
        Color.RGBToHSV(
                (int) (colors.red * 255),
                (int) (colors.green * 255),
                (int) (colors.blue * 255),
                hsvValues
        );
    }
}
