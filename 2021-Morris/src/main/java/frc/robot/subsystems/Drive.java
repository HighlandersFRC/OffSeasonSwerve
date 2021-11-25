package frc.robot.subsystems;

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
    Translation2d m_frontLeftLocation = new Translation2d(0.2921, 0.2921);
    Translation2d m_frontRightLocation = new Translation2d(0.2921, -0.2921);
    Translation2d m_backLeftLocation = new Translation2d(-0.2921, 0.2921);
    Translation2d m_backRightLocation = new Translation2d(-0.2921, -0.2921);

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

    // method to actually run swerve code
    public void swerveDrive() {
        double originalX = OI.getDriverLeftX();
        double originalY = -OI.getDriverLeftY();
        double turn = OI.getDriverRightX();
        double navxOffset = peripherals.getNavxAngle();
        double xPower = getAdjustedX(originalX, originalY);
        double yPower = getAdjustedY(originalX, originalY);

        Vector controllerVector = new Vector(xPower, yPower);

        m_pose = m_odometry.update(new Rotation2d(Math.toRadians(-navxOffset)), leftFront.getState(), rightFront.getState(), leftBack.getState(), rightBack.getState());

        leftFront.moduleDrive(controllerVector, turn, navxOffset);
        rightFront.moduleDrive(controllerVector, turn, navxOffset);
        leftBack.moduleDrive(controllerVector, turn, navxOffset);
        rightBack.moduleDrive(controllerVector, turn, navxOffset);

        rightFront.postDriveMotorTics();

        System.out.println(m_odometry.getPoseMeters());
    }

    public void autoDrive(double angle, double turnDegPerSec, double driveSpeed) {
        double navxOffset = peripherals.getNavxAngle();

        double turn = turnDegPerSec/360;
        double motorPercent = driveSpeed/4.96824;

        double radiansAngle = Math.toRadians(angle);
        Vector inputVector = new Vector(Math.cos(radiansAngle), Math.sin(radiansAngle));
        inputVector.scalarMultiple(motorPercent);

        leftFront.moduleDrive(inputVector, turn, navxOffset);
        rightFront.moduleDrive(inputVector, turn, navxOffset);
        leftBack.moduleDrive(inputVector, turn, navxOffset);
        rightBack.moduleDrive(inputVector, turn, navxOffset);
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