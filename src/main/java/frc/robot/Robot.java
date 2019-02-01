/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;



import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.cscore.VideoSource;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.DigitalInput;
//import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
// auto choices in smartdashbord/shuffleboard  
  private static final String kAutoLine = "Drive Straight - Auto Line";
  private static final String kAutoLineRight = "Drive Straight - Turn Right";
  private static final String kAutoLineLeft = "Drive Straight - Turn Left";
//

// essential instatiation
  public static Compressor compressor;
  public static DoubleSolenoid doubleSolenoid;
//

// smartdashboard auto choices dropdown
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
// --end

// gyro instantiation and angle setpoints/error margin

//old gyro instantiation  
private static final double kAngleSetpoint = 0.0;
private static final double kP = 0.005; // propotional turning constant
// --end

//port instantiation
private static final SPI.Port kGyroPort = SPI.Port.kOnboardCS0;
private static final int leftVictorPort= 0;
private static final int rightVictorPort = 1;
private static final int kJoystickPort = 0;
private static final int kJoystick2Port = 1;
private static final int kElevatorPort = 2;
private static final int kCargoIntakePort = 3;
private static final int kHabClimbPort = 4;
private static final int kHatchReleasePort = 5;
// --end

// new gyro instantiation 
  int P, I, D = 1;

//int integral, previous_error, setpoint = 0;
  int integral, previous_error = 0;
  int setpoint = 45; //attempting to set the setpoint as a different number to see if it turns to that angle or not
// --end

// drive victorsp's
  VictorSP leftVictorSP = new VictorSP(leftVictorPort);
  VictorSP rightVictorSP = new VictorSP(rightVictorPort);
// --end

//auxillary sparks
Spark elevatorSpark = new Spark(kElevatorPort); //single - one controller
Spark cargoIntakeSpark = new Spark(kCargoIntakePort); //double - two controllers - one sign
Spark habClimbSpark = new Spark(kHabClimbPort); //double - two controllers - one sign
Spark hatchReleaseSpark = new Spark(kHatchReleasePort); //single - one controller -- *tenative*
// --end

//hall effect
DigitalInput hallEffectSensorTop = new DigitalInput(0);
DigitalInput hallEffectSensorBottom = new DigitalInput(1);
// --end

// drivetrain (with victorsp's)
DifferentialDrive m_myRobot = new DifferentialDrive(leftVictorSP, rightVictorSP);
	private ADXRS450_Gyro m_gyro = new ADXRS450_Gyro(kGyroPort);
  private Joystick m_joystick = new Joystick(kJoystickPort);
// --end

//second joystick instantiation
private Joystick m_joystick2 = new Joystick(kJoystick2Port);
// --end

  @Override
  public void robotInit() {
  // compressor = new Compressor(1);
  //  doubleSolenoid = new DoubleSolenoid(4, 5);
  m_joystick2 = new Joystick(1);

  // smartdashboard option adding  
    m_chooser.setDefaultOption("Drive Straight - Auto Line", kAutoLine);
    m_chooser.addOption("Drive Straight - Turn Right", kAutoLineRight);
    m_chooser.addOption("Drive Straight - Turn Left", kAutoLineLeft);
    SmartDashboard.putData("Auto choices", m_chooser);
  //   --end

  // gyro calibrate at robot turn on  
    m_gyro.calibrate();
  // --end

  // camera instatiation
  CameraServer camera = CameraServer.getInstance();
  VideoSource usbCam = camera.startAutomaticCapture("cam0", 0);
  usbCam.setResolution(320,240); 
  usbCam.setFPS(30);
  // --end
  }

  /**
   * This function is called every robot packet, no matter the mode. Use
   * this for items like diagnostics that you want ran during disabled,
   * autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before
   * LiveWindow and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
  // unsure whether functioning but supposed to put angle of gyro in smartdashboard
    SmartDashboard.putNumber("Gyro Angle", m_gyro.getAngle());
  // --end
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable
   * chooser code works with the Java SmartDashboard. If you prefer the
   * LabVIEW Dashboard, remove all of the chooser code and uncomment the
   * getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to
   * the switch structure below with additional strings. If using the
   * SendableChooser make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    m_autoSelected = SmartDashboard.getString("Drive Straight - Auto Line", kAutoLine);
    System.out.println("Auto selected: " + m_autoSelected);
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
      case kAutoLine:
      default:
      //DO NOT USE UNTIL FIXED -- DRIVES FORWARDS AT FULL SPEED INFINITELY -- USE ONLY ON ROBOT CART AND BE EXTRA CAUTIOUS
      double error = setpoint - m_gyro.getAngle(); // Error = Target - Actual
      this.integral += (error*.02); // Integral is increased by the error*time (which is .02 seconds using normal IterativeRobot)
      double derivative = (error - this.previous_error) / .02;
      double finalangle = P * error + I * this.integral + D * derivative;
      m_myRobot.arcadeDrive(0, finalangle);
      break;
      case kAutoLineRight:
      //code goes here
        break;
      case kAutoLineLeft:
      //code goes here  
        break;
    }
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {
  
  //Gyro Old (1/21/19)

    // adds the joystick y variable 
    boolean joystickYaxis = m_joystick.getY() > 1 || m_joystick.getY() < -1;

    if (joystickYaxis) {
    double turningValue = (kAngleSetpoint - m_gyro.getAngle()) * kP;
		// Invert the direction of the turn if we are going backwards
    turningValue = Math.copySign(turningValue, m_joystick.getY());
    m_myRobot.arcadeDrive(m_joystick.getY(), turningValue);
    }
    else{      
      //allow normal robot control if not y axis is not changing
      m_myRobot.arcadeDrive(m_joystick.getY(), m_joystick.getX());
    }
    // --end
    
    //elevator y-axis backup control
    elevatorSpark.set(m_joystick2.getY());
    // --end



    //hall effect for elevator
    boolean upTriggered = hallEffectSensorTop.get() == false;
    boolean downTriggered = hallEffectSensorBottom.get() == false;		
		double joystickYAxis = m_joystick2.getY();    
   
    if (joystickYAxis > 0 && upTriggered || joystickYAxis < 0 && downTriggered)
      {
      	elevatorSpark.set(0);
      }
    else
      {
      	elevatorSpark.set(m_joystick2.getY());
      }
    // --end
//
//


// Pneumatics

compressor.setClosedLoopControl(false);
  
  boolean whileHeld = m_joystick2.getRawButton(8);

  // if-else piston control statement
  if (whileHeld) {
    doubleSolenoid.set(DoubleSolenoid.Value.kForward);
  } 
  else{
    doubleSolenoid.set(DoubleSolenoid.Value.kReverse);
  }	
  
  if (m_joystick2.getRawButton(7)) {
    doubleSolenoid.set(DoubleSolenoid.Value.kOff);
  }
  else {
  
  }
  // --end

  // pressure switch boolean's
  boolean pressureSwitchOn = compressor.getPressureSwitchValue() == true;
  boolean pressureSwitchOff = compressor.getPressureSwitchValue() == false;
  // --end

  // compressor pressure regulation
  if(pressureSwitchOff) {
    compressor.enabled();
  }
  else if (pressureSwitchOn) {
    compressor.setClosedLoopControl(true);
  }
}
// --end
  

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
  }
}
