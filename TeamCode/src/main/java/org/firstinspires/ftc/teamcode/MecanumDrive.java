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
    // controls how fast the robot spins when auto-aligning to AprilTag
    public static final double PROPORTIONAL_GAIN = -0.002;

    // AprilTag detection stuff
    AprilTagProcessor aprilTag;
    VisionPortal visionPortal;

    // the four drive motors for mecanum wheels
    DcMotor frontLeftDrive;
    DcMotor frontRightDrive;
    DcMotor backLeftDrive;
    DcMotor backRightDrive;

    // other motors
    DcMotor intake;
    DcMotor shooter;

    // gyro sensor to know which way the robot is facing
    IMU imu;

    // booleans to keep track of what mode we're in
    boolean isFieldOriented = true;
    boolean isAutoAligning = false;

    // sets up the AprilTag camera
    private void initApriltag() {
        aprilTag = new AprilTagProcessor.Builder()
                .build();

        VisionPortal.Builder builder = new VisionPortal.Builder();
        builder.setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"));

        builder.enableLiveView(true);

        builder.addProcessor(aprilTag);

        visionPortal = builder.build();
    }

    // color sensor to detect element colors
    NormalizedColorSensor colorSensor;
    final float[] hsvValues = new float[3];

    @Override
    public void runOpMode() {
        // initialize all the hardware when the opmode starts
        initApriltag();

        // get all the motor names from the hardware map
        frontLeftDrive = hardwareMap.get(DcMotor.class,  "frontLeft");
        frontRightDrive = hardwareMap.get(DcMotor.class, "frontRight");
        backLeftDrive = hardwareMap.get(DcMotor.class, "backLeft");
        backRightDrive = hardwareMap.get(DcMotor.class, "backRight");
        intake = hardwareMap.get(DcMotor.class, "intake");
        shooter = hardwareMap.get(DcMotor.class, "shooter");

        // reverse the left side motors so the robot moves straight
        backLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        frontLeftDrive.setDirection(DcMotor.Direction.REVERSE);

        // get the color sensor
        colorSensor = hardwareMap.get(NormalizedColorSensor.class, "sensor_color");

        // set up the gyro sensor
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

        // main loop - runs while the opmode is active
        while(opModeIsActive()) {
            // check which obelisk pattern we see
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

            // press A to toggle between field oriented and robot centric
            if (gamepad1.aWasPressed()){
                isFieldOriented = !isFieldOriented;
            }

            // press B to toggle auto-align mode
            if (gamepad1.bWasPressed()) {
                isAutoAligning = !isAutoAligning;
            }

            // if auto-align is on, align to the AprilTag
            if (isAutoAligning) {
                alignToAprilTag(20, 25);
            } else {
                // otherwise, let player control robot
                if (isFieldOriented) {
                    driveFieldOriented(gamepad1.left_stick_y, -gamepad1.left_stick_x, -gamepad1.right_stick_x);
                }
                else{
                    drive(gamepad1.left_stick_y, -gamepad1.left_stick_x, -gamepad1.right_stick_x);
                }
            }

            // press Y to reset the gyro
            if (gamepad1.yWasPressed()) {
                imu.resetYaw();
            }

            // left trigger spins the intake
            if (gamepad1.left_trigger > 0.1) {
                intake.setPower(1.0);
            } else {
                intake.setPower(0.0);
            }

            // right trigger spins the shooter (more trigger = faster spin)
            if (gamepad1.right_trigger > 0.1) {
                shooter.setPower(gamepad1.right_trigger);
            } else {
                shooter.setPower(0.0);
            }

            // update the color sensor reading
            updateColorSensor();

            // display info on the driver hub
            telemetry.addData("Drive Mode", isFieldOriented ? "Field Oriented" : "Robot Centric");
            telemetry.addData("Auto-Align", isAutoAligning ? "ON (Tag 20)" : "OFF");
            telemetry.update();
        };
    }

    // checks if we see a specific obelisk pattern and returns it
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

    // drives the robot in robot centric mode
    public void drive(double forward, double right, double rotate) {
        // calculate the power needed for each motor based on joystick input
        double frontLeftPower = forward + right + rotate;
        double frontRightPower = forward - right - rotate;
        double backRightPower = forward + right - rotate;
        double backLeftPower = forward - right + rotate;

        double maxPower = 1.0;
        double maxSpeed = 1.0;

        // makes sure no motor gets more than 100% power
        maxPower = Math.max(maxPower, Math.abs(frontLeftPower));
        maxPower = Math.max(maxPower, Math.abs(frontRightPower));
        maxPower = Math.max(maxPower, Math.abs(backRightPower));
        maxPower = Math.max(maxPower, Math.abs(backLeftPower));

        // set the power for each motor
        frontLeftDrive.setPower(maxSpeed * (frontLeftPower / maxPower));
        frontRightDrive.setPower(maxSpeed * (frontRightPower / maxPower));
        backLeftDrive.setPower(maxSpeed * (backLeftPower / maxPower));
        backRightDrive.setPower(maxSpeed * (backRightPower / maxPower));
    }

    // drives the robot in field oriented mode
    public void driveFieldOriented(double forward, double right, double rotate){
        // calculate the angle using math
        double theta = Math.atan2(forward, right);
        // calculate distance from center
        double r = Math.hypot(right, forward);
        // subtract the robot's rotation to make it field oriented
        theta = AngleUnit.normalizeRadians(theta - imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS));
        // calculate the new forward and right values
        double newForward = r * Math.sin(theta);
        double newRight = r * Math.cos(theta);
        // drive using the new values
        drive(newForward, newRight, rotate);
    }

    // reads the color sensor and prints what color it sees
    private void updateColorSensor() {
        // get the color from the sensor
        NormalizedRGBA colors = colorSensor.getNormalizedColors();

        // convert RGB to HSV (this makes it easier to detect colors)
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

        // check if the color is saturated enough to be an actual color, then check if it's green or purple
        if (saturation > 0.3) {
            if (hue >= 145 && hue <= 180) {
                detectedColor = "Green";
            } else if (hue >= 225 && hue <= 250) {
                detectedColor = "Purple";
            }
        }

        // print the color info to the driver hub screen
        telemetry.addData("--- Sensor Output ---", "");
        telemetry.addData("Detected Element", detectedColor);
        telemetry.addData("Hue Angle (Deg)", "%.1f", hue);
        telemetry.addData("Saturation/Intensity", "%.2f", saturation);
    }

    // auto-align the robot to an AprilTag
    private void alignToAprilTag(int targetTagId, int tolerance) {
        // look for the AprilTag with the target ID
        AprilTagDetection targetTag = null;

        for (AprilTagDetection tag : aprilTag.getDetections()) {
            if (tag.id == targetTagId) {
                targetTag = tag;
                break;
            }
        }

        // if we don't see the tag, stop and tell the user
        if (targetTag == null) {
            telemetry.addData("Align Status", "ion see shi cuhh");
            return;
        }

        // calculate how far off center the tag is
        double frameCenterX = 320;
        double tagCenterX = targetTag.center.x;
        double errorX = tagCenterX - frameCenterX;

        // if the tag is close enough to center, we're done
        if (Math.abs(errorX) < tolerance) {
            telemetry.addData("Align Status", "shi aligned asf!");
            drive(0, 0, 0);
            return;
        }

        // calculate how much to rotate based on the error
        double rotationPower = errorX * PROPORTIONAL_GAIN;
        // make sure the rotation isn't too fast or too slow
        rotationPower = Math.max(-1.0, Math.min(1.0, rotationPower));

        // shows status information
        telemetry.addData("Align Status", "Aligning");
        telemetry.addData("Tag Center X", "%.0f", tagCenterX);
        telemetry.addData("Error", "%.0f", errorX);
        telemetry.addData("Rotation Power", "%.2f", rotationPower);

        drive(0, 0, rotationPower);
    }
}
