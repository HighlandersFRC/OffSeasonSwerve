package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;
import com.ctre.phoenix.sensors.CANCoder;

import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import edu.wpi.first.wpilibj.kinematics.SwerveDriveKinematics;
import edu.wpi.first.wpilibj.kinematics.SwerveDriveOdometry;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.OI;
import frc.robot.commands.defaults.DriveDefault;
import frc.robot.tools.math.Vector;

public class Drive extends SubsystemBase {
    private final OI OI = new OI();

    // creating all the falcons
    private final WPI_TalonFX leftForwardMotor = new WPI_TalonFX(6);
    private final WPI_TalonFX leftForwardAngleMotor = new WPI_TalonFX(1);
    private final WPI_TalonFX leftBackMotor = new WPI_TalonFX(4);
    private final WPI_TalonFX leftBackAngleMotor = new WPI_TalonFX(3);
    private final WPI_TalonFX rightForwardMotor = new WPI_TalonFX(2);
    private final WPI_TalonFX rightForwardAngleMotor = new WPI_TalonFX(5);
    private final WPI_TalonFX rightBackMotor = new WPI_TalonFX(8);
    private final WPI_TalonFX rightBackAngleMotor = new WPI_TalonFX(7);

    private Peripherals peripherals;

    private double adjustedX = 0.0;
    private double adjustedY = 0.0;

    private final double moduleXY = ((Constants.ROBOT_WIDTH)/2) - Constants.MODULE_OFFSET;

    // creating all the external encoders
    private CANCoder backRightAbsoluteEncoder = new CANCoder(2);
    private CANCoder frontLeftAbsoluteEncoder = new CANCoder(1);
    private CANCoder frontRightAbsoluteEncoder = new CANCoder(5);
    private CANCoder backLeftAbsoluteEncoder = new CANCoder(3);

    // creating each swerve module with angle and drive motor, module number(relation to robot), and external encoder
    private final SwerveModule leftFront = new SwerveModule(2, leftForwardAngleMotor, leftForwardMotor, 0, frontLeftAbsoluteEncoder);
    private final SwerveModule leftBack = new SwerveModule(3, leftBackAngleMotor, leftBackMotor, 0, backLeftAbsoluteEncoder);
    private final SwerveModule rightFront = new SwerveModule(1, rightForwardAngleMotor, rightForwardMotor, 0, frontRightAbsoluteEncoder);
    private final SwerveModule rightBack = new SwerveModule(4, rightBackAngleMotor, rightBackMotor, 0, backRightAbsoluteEncoder);

    // Locations for the swerve drive modules relative to the robot center.
    Translation2d m_frontLeftLocation = new Translation2d(moduleXY, moduleXY);
    Translation2d m_frontRightLocation = new Translation2d(moduleXY, -moduleXY);
    Translation2d m_backLeftLocation = new Translation2d(-moduleXY, moduleXY);
    Translation2d m_backRightLocation = new Translation2d(-moduleXY, -moduleXY);

    // Creating my kinematics object using the module locations
    SwerveDriveKinematics m_kinematics = new SwerveDriveKinematics(
    m_frontLeftLocation, m_frontRightLocation, m_backLeftLocation, m_backRightLocation
    );

    // Creating my odometry object from the kinematics object. Here,
    // our starting pose is 5 meters along the long end of the field and in the
    // center of the field along the short end, facing forward.
    SwerveDriveOdometry m_odometry; 
    Pose2d m_pose;

    double initAngle;
    double setAngle;
    double diffAngle;

    public Drive(Peripherals peripherals) {
        this.peripherals = peripherals;

        m_odometry = new SwerveDriveOdometry(m_kinematics, new Rotation2d(Math.toRadians(peripherals.getNavxAngle())));
    }

    // get each external encoder
    public double getLeftForwardEncoder() {
        return leftFront.getAbsolutePosition();
    }

    public double getLeftBackEncoder() {
        return leftBack.getAbsolutePosition();
    }

    public double getRightForwardEncoder() {
        return rightFront.getAbsolutePosition();
    }

    public double getRightBackEncoder() {
        return rightBack.getAbsolutePosition();
    }

    // get Joystick adjusted y-value
    public double getAdjustedY(double originalX, double originalY){
        double adjustedY = originalY * Math.sqrt((1-(Math.pow(originalX, 2))/2));
        return adjustedY;
    }

    // get Joystick adjusted x-value
    public double getAdjustedX(double originalX, double originalY){
        double adjustedX = originalX * Math.sqrt((1-(Math.pow(originalY, 2))/2));
        return adjustedX;
    }

    // method run on robot initialization
    public void init() {
        leftFront.init();
        leftBack.init();
        rightBack.init();
        rightFront.init();
        peripherals.zeroNavx();

        setDefaultCommand(new DriveDefault(this));
    }

    public double getOdometryX() {
        return m_odometry.getPoseMeters().getX();
    }

    public double getOdometryY() {
        return m_odometry.getPoseMeters().getY();
    }

    public double getOdometryAngle() {
        return m_odometry.getPoseMeters().getRotation().getRadians();
    }

    // method to actually run swerve code
    public void teleopDrive() {
        double turnLimit = 0.75;
        double originalX = OI.getDriverLeftX();
        double originalY = -OI.getDriverLeftY();

        if(Math.abs(originalX) < 0.05) {
            originalX = 0;
        }
        if(Math.abs(originalY) < 0.05) {
            originalY = 0;
        }

        double turn = OI.getDriverRightX() * turnLimit * (Constants.TOP_SPEED)/(Constants.ROBOT_RADIUS);
        double navxOffset = peripherals.getNavxAngle();
        double xPower = getAdjustedX(originalX, originalY);
        double yPower = getAdjustedY(originalX, originalY);

        double xSpeed = xPower * Constants.TOP_SPEED;
        double ySpeed = yPower * Constants.TOP_SPEED;

        Vector controllerVector = new Vector(xSpeed, ySpeed);

        m_pose = m_odometry.update(new Rotation2d(Math.toRadians(navxOffset)), leftFront.getState(Math.toRadians(navxOffset)), rightFront.getState(Math.toRadians(navxOffset)), leftBack.getState(Math.toRadians(navxOffset)), rightBack.getState(Math.toRadians(navxOffset)));

        leftFront.velocityDrive(controllerVector, turn, navxOffset);
        rightFront.velocityDrive(controllerVector, turn, navxOffset);
        leftBack.velocityDrive(controllerVector, turn, navxOffset);
        rightBack.velocityDrive(controllerVector, turn, navxOffset);

        // leftForwardMotor.set(ControlMode.Velocity, 2500);
        // rightForwardMotor.set(ControlMode.Velocity, 2500);
        // leftBackMotor.set(ControlMode.Velocity, 2500);
        // rightBackMotor.set(ControlMode.Velocity, 2500);


        rightFront.postDriveMotorTics();

        System.out.println(m_odometry.getPoseMeters());
        // System.out.println("Rate: " + peripherals.getNavxRate());
    }

    public void autoDrive(Vector velocityVector, double turnDegPerSec) {
        double navxOffset = peripherals.getNavxAngle();

        double turnRadiansPerSec = Math.toRadians(turnDegPerSec);
        // inputVector.scalarMultiple(motorPercent);

        // double turnLimit = 1;

        m_pose = m_odometry.update(new Rotation2d(Math.toRadians(navxOffset)), leftFront.getState(Math.toRadians(navxOffset)), rightFront.getState(Math.toRadians(navxOffset)), leftBack.getState(Math.toRadians(navxOffset)), rightBack.getState(Math.toRadians(navxOffset)));

        System.out.println(m_odometry.getPoseMeters());

        leftFront.velocityDrive(velocityVector, turnRadiansPerSec, navxOffset);
        rightFront.velocityDrive(velocityVector, turnRadiansPerSec, navxOffset);
        leftBack.velocityDrive(velocityVector, turnRadiansPerSec, navxOffset);
        rightBack.velocityDrive(velocityVector, turnRadiansPerSec, navxOffset);
    }

    private Command DriveDefault() {
        return null;
    }
 
    @Override
    public void periodic() {
    }

    @Override
    public void simulationPeriodic() {
    }
}