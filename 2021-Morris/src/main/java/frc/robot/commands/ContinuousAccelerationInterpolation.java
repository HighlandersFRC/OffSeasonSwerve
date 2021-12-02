package frc.robot.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.Drive;
import frc.robot.tools.math.Vector;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

public class ContinuousAccelerationInterpolation extends CommandBase {
    /** Creates a new ContinuousAccelerationInterpolation. */

    private Drive drive;

    private static Vector velocityVector = new Vector();
    private static Vector adjustedVelocityVector = new Vector();

    private static double angleChangeRate = 0;

    private static Boolean needCurve = false;

    private static File jsonFile;

    private static StringBuilder pathPointsString;

    private static double initTime;
    private static double currentTime;
    private static double lastTime;

    private static JSONArray pathPoints;

    private static JSONParser parser = new JSONParser();

    private static double turnTimePercent = 1;

    public ContinuousAccelerationInterpolation(Drive drive, double turnTimePercent) {
      this.turnTimePercent = turnTimePercent;
      this.drive = drive;
      addRequirements(drive);
        // Use addRequirements() here to declare subsystem dependencies.
    }

    // Called when the command is initially scheduled.
    @Override
    public void initialize() {
        try {
            jsonFile = new File("filePath");
            BufferedReader br = new BufferedReader(new FileReader(jsonFile));

            String line;
            while ((line = br.readLine()) != null) {
                pathPointsString.append(line);
            }
            pathPoints = (JSONArray) parser.parse(pathPointsString.toString());
        } catch (Exception e) {
            System.out.println("Could not open pathing file");
        }

        initTime = Timer.getFPGATimestamp();

        JSONObject lastPoint = (JSONObject) pathPoints.get(pathPoints.size() - 1);
        lastTime = (double) lastPoint.get("Time");
      
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    currentTime = Timer.getFPGATimestamp() - initTime;
    int i = 0;
    JSONObject currentPoint = (JSONObject) pathPoints.get(i);
    JSONObject hookPoint = (JSONObject) pathPoints.get(i + 1);
    JSONObject thirdPoint = (JSONObject) pathPoints.get(i + 2);
    if(currentTime < lastTime) {
      int currentPointTime = (int) currentPoint.get("Time");
      int hookPointTime = (int) hookPoint.get("Time");
      int thirdPointTime = (int) thirdPoint.get("Time");

      int currentPointTheta = (int) currentPoint.get("Theta");
      int hookPointTheta = (int) hookPoint.get("Theta");
      int thirdPointTheta = (int) thirdPoint.get("Theta");

      int currentPointX = (int) currentPoint.get("X");
      int hookPointX = (int) hookPoint.get("X");
      int thirdPointX = (int) thirdPoint.get("X");

      int currentPointY = (int) currentPoint.get("Y");
      int hookPointY = (int) hookPoint.get("Y");
      int thirdPointY = (int) thirdPoint.get("Y");

      int timeDiffT1 = (hookPointTime - currentPointTime);
      double t1 = (timeDiffT1 * turnTimePercent) + currentPointTime;

      int timeDiffT2 = (thirdPointTime - hookPointTime);
      double t2 = (timeDiffT2 * (1-turnTimePercent)) + hookPointTime;

      Vector t1Vector = new Vector(hookPointX - currentPointX, hookPointY - currentPointY);
      Vector t2Vector = new Vector(thirdPointX - hookPointX, thirdPointY - hookPointY);

      if(t1Vector.getI() == t2Vector.getI() && t1Vector.getJ() == t2Vector.getJ()) {
        needCurve = false;
      }
      else {
        needCurve = true;
      }

      if(needCurve == false || currentTime < t1) {
        velocityVector = new Vector(hookPointX - drive.getOdometryX(), hookPointY - drive.getOdometryY());

        adjustedVelocityVector = velocityVector;
        
        angleChangeRate = (hookPointTheta - drive.getOdometryAngle());
      }
      else {
        needCurve = true;

        double t1Theta = ((hookPointTheta  - currentPointTheta) * ((t1 - 1)%timeDiffT1)/timeDiffT1) + currentPointTheta;
        double t2Theta = ((thirdPointTheta - hookPointTheta) * ((t2 -1)%timeDiffT2)/timeDiffT2) + hookPointTheta;

        angleChangeRate = (t2Theta - t1Theta)/(t2 - t1);

        Vector accelVector = new Vector((t2Vector.getI() - t1Vector.getI())/(t2 - t1), (t2Vector.getJ() - t1Vector.getJ())/(t2 - t1));

        adjustedVelocityVector = new Vector((accelVector.getI() * (currentTime - t1)) + velocityVector.getI(),(accelVector.getJ() * (currentTime - t1)) + velocityVector.getJ());
      
        if((Math.abs(adjustedVelocityVector.getI() - t2Vector.getI()) < 0.2) && Math.abs(adjustedVelocityVector.getJ() - t2Vector.getJ()) < 0.2) {
          System.out.println("Within correct velocity");
          needCurve = false;
          i++;
        }
      }

    }

    // drive.autoDrive(adjustedVelocityVector, angleChangeRate);

  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {}

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }
}
