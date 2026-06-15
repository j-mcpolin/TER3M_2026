package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

// this class handles all the drive motor stuff for the mecanum robot
public class DriveTrain {
    // the four drive motors (one for each wheel)
    private DcMotor frontLeftDrive;
    private DcMotor frontRightDrive;
    private DcMotor backLeftDrive;
    private DcMotor backRightDrive;

    // gyro sensor to know which way the robot is facing
    private IMU imu;

    // constructor - gets called when we create a new DriveTrain
    public DriveTrain(HardwareMap hardwareMap) {
        // get all four drive motors from the hardware map, and adds name for us to use on driver hub
        frontLeftDrive = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRightDrive = hardwareMap.get(DcMotor.class, "frontRight");
        backLeftDrive = hardwareMap.get(DcMotor.class, "backLeft");
        backRightDrive = hardwareMap.get(DcMotor.class, "backRight");

        // reverse the left side motors so the robot drives straight
        backLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        frontLeftDrive.setDirection(DcMotor.Direction.REVERSE);

        // set up the gyro sensor
        imu = hardwareMap.get(IMU.class, "imu");

        // tell the gyro which way the hub is facing on the robot
        RevHubOrientationOnRobot.LogoFacingDirection logoDirection =
                RevHubOrientationOnRobot.LogoFacingDirection.UP;
        RevHubOrientationOnRobot.UsbFacingDirection usbDirection =
                RevHubOrientationOnRobot.UsbFacingDirection.BACKWARD;

        // create the orientation object and initialize the gyro with it
        RevHubOrientationOnRobot orientationOnRobot = new
                RevHubOrientationOnRobot(logoDirection, usbDirection);
        imu.initialize(new IMU.Parameters(orientationOnRobot));
    }

    // drives the robot in robot oriented mode
    public void drive(double forward, double right, double rotate) {
        // calculate the power needed for each motor based on joystick input
        double frontLeftPower = forward + right + rotate;
        double frontRightPower = forward - right - rotate;
        double backRightPower = forward + right - rotate;
        double backLeftPower = forward - right + rotate;

        double maxPower = 1.0;
        double maxSpeed = 1.0;

        // find the biggest motor power so we can scale everything down
        // this makes sure no motor tries to go over 100% power
        maxPower = Math.max(maxPower, Math.abs(frontLeftPower));
        maxPower = Math.max(maxPower, Math.abs(frontRightPower));
        maxPower = Math.max(maxPower, Math.abs(backRightPower));
        maxPower = Math.max(maxPower, Math.abs(backLeftPower));

        // set the power for each motor (scaled so nothing goes over 100%)
        frontLeftDrive.setPower(maxSpeed * (frontLeftPower / maxPower));
        frontRightDrive.setPower(maxSpeed * (frontRightPower / maxPower));
        backLeftDrive.setPower(maxSpeed * (backLeftPower / maxPower));
        backRightDrive.setPower(maxSpeed * (backRightPower / maxPower));
    }

    // drives the robot in field oriented mode
    public void driveFieldOriented(double forward, double right, double rotate) {
        // calculate the angle of the joystick
        double theta = Math.atan2(forward, right);
        // calculate distance from center
        double r = Math.hypot(right, forward);
        // subtract the robot's rotation from the angle to make it field oriented
        // gyro tells us how much the robot is rotated
        theta = AngleUnit.normalizeRadians(theta - imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS));
        // calculate the new forward and right values
        double newForward = r * Math.sin(theta);
        double newRight = r * Math.cos(theta);
        // now drive using the new values
        drive(newForward, newRight, rotate);
    }

    // resets the gyro angle to zero
    public void resetYaw() {
        imu.resetYaw();
    }
}
