package org.firstinspires.ftc.teamcode.subsystems;

import android.util.Size;

import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.ArrayList;
import java.util.List;

// this class handles all the AprilTag camera stuff
public class AprilTagWebCam {
    // the thing that processes images and finds AprilTags
    private AprilTagProcessor aprilTagProcessor;
    // the camera system itself
    private VisionPortal visionPortal;

    // list to store all the tags we see
    private List<AprilTagDetection> detectedTags = new ArrayList<>();

    // for printing debug info to the phone screen
    private Telemetry telemetry;

    // set up the camera and AprilTag processor
    public void init(HardwareMap hwMap, Telemetry telemetry) {
        this.telemetry = telemetry;

        // create the AprilTag processor with settings
        aprilTagProcessor = new AprilTagProcessor.Builder()
                .setDrawTagID(true)               // draw the tag ID on the camera feed
                .setDrawTagOutline(true)          // draw a box around each tag
                .setDrawAxes(true)                // draw XYZ axes on each tag
                .setDrawCubeProjection(true)      // draw a 3D cube on each tag
                .setOutputUnits(DistanceUnit.CM, AngleUnit.DEGREES)  // use CM and degrees for measurements
                .build();

        // create the camera system
        VisionPortal.Builder builder = new VisionPortal.Builder();
        builder.setCamera(hwMap.get(WebcamName.class, "Webcam 1"));  // grab the camera
        builder.setCameraResolution(new Size(640, 480));              // set resolution to 640x480
        builder.addProcessor(aprilTagProcessor);                      // add the AprilTag processor

        visionPortal = builder.build();  // build the whole system
    }

    // gets the latest detections from the camera and stores them
    public void update() {
        detectedTags = aprilTagProcessor.getDetections();
    }

    // returns the list of all tags we see right now
    public List<AprilTagDetection> getDetectedTags() {
        return detectedTags;
    }

    // prints out all the info about a specific detected tag (position, rotation, distance, etc)
    public void displayDetectionTelemetry(AprilTagDetection detectedId) {
        if (detectedId == null) {return;}  // if there's no tag, don't do anything

        // if the tag has metadata (name and location info)
        if (detectedId.metadata != null) {
            // print the tag ID and name
            telemetry.addLine(String.format("\n==== (ID %d) %s", detectedId.id, detectedId.metadata.name));
            // print the X, Y, Z position in inches
            telemetry.addLine(String.format("XYZ %6.1f %6.1f %6.1f  (inch)", detectedId.ftcPose.x, detectedId.ftcPose.y, detectedId.ftcPose.z));
            // print pitch, roll, yaw (which way the tag is rotated) in degrees
            telemetry.addLine(String.format("PRY %6.1f %6.1f %6.1f  (deg)", detectedId.ftcPose.pitch, detectedId.ftcPose.roll, detectedId.ftcPose.yaw));
            // print range (distance to tag), bearing (angle to tag), elevation (angle up/down to tag)
            telemetry.addLine(String.format("RBE %6.1f %6.1f %6.1f  (inch, deg, deg)", detectedId.ftcPose.range, detectedId.ftcPose.bearing, detectedId.ftcPose.elevation));
        } else {
            // if the tag doesn't have metadata, just print its ID and center position
            telemetry.addLine(String.format("\n==== (ID %d) Unknown", detectedId.id));
            telemetry.addLine(String.format("Center %6.0f %6.0f   (pixels)", detectedId.center.x, detectedId.center.y));
        }
    }

    // searches through all detected tags and returns the one with the matching ID
    public AprilTagDetection getTagBySpecificId(int id) {
        for (AprilTagDetection detection : detectedTags) {
            if (detection.id == id) {
                return detection;  // found it, return it
            }
        }
        return null;  // didn't find it, return null
    }

    // shuts down the camera when you're done
    public void stop() {
        if (visionPortal != null) {
            visionPortal.close();
        }
    }
}
