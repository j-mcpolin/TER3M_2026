package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

@Autonomous
public class Auto extends LinearOpMode {
    DcMotor frontLeftDrive;
    DcMotor frontRightDrive;
    DcMotor backLeftDrive;
    DcMotor backRightDrive;

    @Override
    public void runOpMode() {
        frontLeftDrive = hardwareMap.get(DcMotor.class,  "frontLeft");
        frontRightDrive = hardwareMap.get(DcMotor.class, "frontRight");
        backLeftDrive = hardwareMap.get(DcMotor.class, "backLeft");
        backRightDrive = hardwareMap.get(DcMotor.class, "backRight");

        backLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        frontLeftDrive.setDirection(DcMotor.Direction.REVERSE);

        waitForStart();

        frontLeftDrive.setPower(1.0);
        frontRightDrive.setPower(1.0);
        backLeftDrive.setPower(1.0);
        backLeftDrive.setPower(1.0);

        sleep(2000);

        frontLeftDrive.setPower(0.0);
        frontRightDrive.setPower(0.0);
        backLeftDrive.setPower(0.0);
        backLeftDrive.setPower(0.0);
    }
}
