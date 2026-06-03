package org.firstinspires.ftc.teamcode;

import android.graphics.Color;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.hardware.DistanceSensor;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;
import java.util.Optional;

@TeleOp
public class MecanumDrive extends LinearOpMode {
    AprilTagProcessor aprilTag;
    VisionPortal visionPortal;
    DcMotor frontLeftDrive;
    DcMotor frontRightDrive;
    DcMotor backLeftDrive;
    DcMotor backRightDrive;

    IMU imu;
    boolean isFieldOriented = true;
    boolean isAutoAligning = false;

    private void initApriltag() {
        aprilTag = new AprilTagProcessor.Builder()
                .build();

        VisionPortal.Builder builder = new VisionPortal.Builder();
        builder.setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"));

        builder.enableLiveView(true);

        builder.addProcessor(aprilTag);

        visionPortal = builder.build();
    }

    NormalizedColorSensor colorSensor;
    final float[] hsvValues = new float[3];
    @Override
    public void runOpMode() {
        initApriltag();

        frontLeftDrive = hardwareMap.get(DcMotor.class,  "frontLeft");
        frontRightDrive = hardwareMap.get(DcMotor.class, "frontRight");
        backLeftDrive = hardwareMap.get(DcMotor.class, "backLeft");
        backRightDrive = hardwareMap.get(DcMotor.class, "backRight");

        backLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        frontLeftDrive.setDirection(DcMotor.Direction.REVERSE);


        colorSensor = hardwareMap.get(NormalizedColorSensor.class, "sensor_color");

        imu = hardwareMap.get(IMU.class, "imu");
        RevHubOrientationOnRobot.LogoFacingDirection logoDirection =
                RevHubOrientationOnRobot.LogoFacingDirection.UP;
        RevHubOrientationOnRobot.UsbFacingDirection usbDirection =
                RevHubOrientationOnRobot.UsbFacingDirection.BACKWARD;

        RevHubOrientationOnRobot orientationOnRobot = new
                RevHubOrientationOnRobot(logoDirection, usbDirection);
        imu.initialize(new IMU.Parameters(orientationOnRobot));

        waitForStart();
        telemetry.setAutoClear(true);
        while(opModeIsActive()) {
            String obelisk = "Idk";
            for (AprilTagDetection tag : aprilTag.getDetections()) {

                if (tag.id == 21) {
                    obelisk = "GPP";
                } else if (tag.id == 22) {
                    obelisk = "PGP";
                } else if (tag.id == 23) {
                    obelisk = "PPG";
                }
            }
            telemetry.addData("Obelisk Pattern", obelisk);

            if (gamepad1.aWasPressed()){
                isFieldOriented = !isFieldOriented;
            }


            if (gamepad1.bWasPressed()) {
                isAutoAligning = !isAutoAligning;
            }


            if (isAutoAligning) {
                alignToAprilTag(20, 25);
            } else {

                if (isFieldOriented) {
                    driveFieldOriented(gamepad1.left_stick_y, -gamepad1.left_stick_x, -gamepad1.right_stick_x);
                }
                else{
                    drive(gamepad1.left_stick_y, -gamepad1.left_stick_x, -gamepad1.right_stick_x);
                }
            }

            if (gamepad1.yWasPressed()) {
                imu.resetYaw();
            }


            updateColorSensor();
            telemetry.addData("Drive Mode", isFieldOriented ? "Field Oriented" : "Robot Centric");
            telemetry.addData("Auto-Align", isAutoAligning ? "ON (Tag 20)" : "OFF");
            telemetry.update();
        };
    }

    private Optional<String> checkObelisk(List<AprilTagDetection> tagDetections) {
        for (AprilTagDetection tag : tagDetections) {
            switch (tag.id) {
                case 21:
                    return Optional.of("GPP");
                case 22:
                    return Optional.of("PGP");
                case 23:
                    return Optional.of("PPG");
            }
        }

        return Optional.empty();
    }

    public void drive(double forward, double right, double rotate) {
        double frontLeftPower = forward + right + rotate;
        double frontRightPower = forward - right - rotate;
        double backRightPower = forward + right - rotate;
        double backLeftPower = forward - right + rotate;

        double maxPower = 1.0;
        double maxSpeed = 1.0;

        maxPower = Math.max(maxPower, Math.abs(frontLeftPower));
        maxPower = Math.max(maxPower, Math.abs(frontRightPower));
        maxPower = Math.max(maxPower, Math.abs(backRightPower));
        maxPower = Math.max(maxPower, Math.abs(backLeftPower));

        frontLeftDrive.setPower(maxSpeed * (frontLeftPower / maxPower));
        frontRightDrive.setPower(maxSpeed * (frontRightPower / maxPower));
        backLeftDrive.setPower(maxSpeed * (backLeftPower / maxPower));
        backRightDrive.setPower(maxSpeed * (backRightPower / maxPower));
    }

    public void driveFieldOriented(double forward, double right, double rotate){
        double theta = Math.atan2(forward, right);
        double r = Math.hypot(right, forward);
        theta = AngleUnit.normalizeRadians(theta - imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS));
        double newForward = r * Math.sin(theta);
        double newRight = r * Math.cos(theta);
        drive(newForward, newRight, rotate);
    }

    private void updateColorSensor() {
        NormalizedRGBA colors = colorSensor.getNormalizedColors();

        Color.RGBToHSV(
                (int) (colors.red * 255),
                (int) (colors.green * 255),
                (int) (colors.blue * 255),
                hsvValues
        );

        float hue = hsvValues[0];
        float saturation = hsvValues[1];
        float value = hsvValues[2];

        String detectedColor = "idk bruh";


        if (saturation > 0.3) {
            if (hue >= 145 && hue <= 180) {
                detectedColor = "Green";
            } else if (hue >= 225 && hue <= 250) {
                detectedColor = "Purple";
            }
        }

        telemetry.addData("--- Sensor Output ---", "");
        telemetry.addData("Detected Element", detectedColor);
        telemetry.addData("Hue Angle (Deg)", "%.1f", hue);
        telemetry.addData("Saturation/Intensity", "%.2f", saturation);
    }



    private void alignToAprilTag(int targetTagId, int tolerance) {
        AprilTagDetection targetTag = null;


        for (AprilTagDetection tag : aprilTag.getDetections()) {
            if (tag.id == targetTagId) {
                targetTag = tag;
                break;
            }
        }

        if (targetTag == null) {
            telemetry.addData("Align Status", "ion see shi cuhh");
            return;
        }


        double frameCenterX = 320;
        double tagCenterX = targetTag.center.x;
        double errorX = tagCenterX - frameCenterX;


        if (Math.abs(errorX) < tolerance) {
            telemetry.addData("Align Status", "shi aligned asf!");
            drive(0, 0, 0);
            return;
        }


        double rotationPower = errorX * -0.002;
        rotationPower = Math.max(-1.0, Math.min(1.0, rotationPower));

        telemetry.addData("Align Status", "Aligning");
        telemetry.addData("Tag Center X", "%.0f", tagCenterX);
        telemetry.addData("Error", "%.0f", errorX);
        telemetry.addData("Rotation Power", "%.2f", rotationPower);

        drive(0, 0, rotationPower);
    }
}
