package org.firstinspires.ftc.teamcode.subsystems;

import android.graphics.Color;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;

public class ColorSensorSubsystem {
    private NormalizedColorSensor colorSensor;
    private final float[] hsvValues = new float[3];

    public ColorSensorSubsystem(HardwareMap hardwareMap) {
        colorSensor = hardwareMap.get(NormalizedColorSensor.class, "sensor_color");
    }

    public String getDetectedColor() {
        NormalizedRGBA colors = colorSensor.getNormalizedColors();

        Color.RGBToHSV(
                (int) (colors.red * 255),
                (int) (colors.green * 255),
                (int) (colors.blue * 255),
                hsvValues
        );

        float hue = hsvValues[0];
        float saturation = hsvValues[1];

        String detectedColor = "idk bruh";

        if (saturation > 0.3) {
            if (hue >= 145 && hue <= 180) {
                detectedColor = "Green";
            } else if (hue >= 225 && hue <= 250) {
                detectedColor = "Purple";
            }
        }

        return detectedColor;
    }

    public float getHue() {
        return hsvValues[0];
    }

    public float getSaturation() {
        return hsvValues[1];
    }

    public void update() {
        NormalizedRGBA colors = colorSensor.getNormalizedColors();

        Color.RGBToHSV(
                (int) (colors.red * 255),
                (int) (colors.green * 255),
                (int) (colors.blue * 255),
                hsvValues
        );
    }
}
