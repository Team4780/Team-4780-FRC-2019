/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;
import frc.robot.Gamepad;


import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.cscore.VideoSource;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.Compressor; //(COMPRESSER COMMENTED OUT 3/20/19 AT PITTSBURGH)
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.cscore.VideoMode.PixelFormat;
import edu.wpi.first.wpilibj.Encoder;
//no jag import -- (used as a free line/spacer)

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
//  public static Compressor compressor;
  public static DoubleSolenoid habClimbPistons;
//


// smartdashboard auto choices dropdown
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
// --end


//port instantiation
private static final SPI.Port kGyroPort = SPI.Port.kOnboardCS0;
private static final int leftVictorPort= 0;
private static final int rightVictorPort = 1;
private static final int kElevatorPort = 2;
private static final int kCargoIntakePortLeft = 3;
private static final int kCargoIntakePortRight = 4;
private static final int kHabClimbElevatorPort = 5;
// --end

//Button set-up
private static final int bCargoIntake = 1;
private static final int bCargoExpel = 2;
private static final int bHatchLevel2 = 7;
private static final int bHatchLevel3 = 5;
private static final int bDriveLevel = 2;
private static final int bHomeLevel = 8;
//end

//Encoder Level Set up
boolean elevatorButtonPressed = false;
double targetDistance = 0;
 double homeLevel = 1.335;
 double hatchLevel2 = 12; 
 double hatchLevel3 = 25.5;
 double driveLevel = 3;
//Encoder Sensor end


//testing Elevator Speed start
double elevatorSpeedFast = -0.75;  //"fast" elevator speed (In the middle)
double elevatorSpeedSlow = -0.4; //"slow" elevator speed (approaching hard stops)
double elevatorSpeedStop = -0.25;
double elevatorSpeedAct = 0; //speed elevator is actually set to (either the fast or slow) 
//testing Elevator Speed end


//joystick ports
private static final int kJoystickPort = 0;
private static final int kJoystick2Port = 1;


// new gyro instantiation 
int P, I, D = 1;
private static final double kP = 0.005; // propotional turning constant
double angle;
boolean turned = true;
int mustTurnDegree = 0;
// --end


// drive victorsp's
  VictorSP leftVictorSP = new VictorSP(leftVictorPort);
  VictorSP rightVictorSP = new VictorSP(rightVictorPort);
// --end


//auxillary sparks
Spark elevatorSpark = new Spark(kElevatorPort); //single - one controller
Spark cargoIntakeSparkLeft = new Spark(kCargoIntakePortLeft); //double - two controllers - one signal
Spark cargoIntakeSparkRight = new Spark(kCargoIntakePortRight); //double - two controllers - one signal
Spark habClimbElevator = new Spark(kHabClimbElevatorPort);
// --end


//hall effect
DigitalInput habElevatorHallTop = new DigitalInput(0); // top sensor init
DigitalInput habElevatorHallBottom = new DigitalInput(1); // bottom sensor init
DigitalInput habElevatorLimitSwitch = new DigitalInput(2);
DigitalInput habClimbPistonLimitSwitch = new DigitalInput(3);
// --end


// drivetrain (with victorsp's)
DifferentialDrive m_myRobot = new DifferentialDrive(leftVictorSP, rightVictorSP);
	private ADXRS450_Gyro m_gyro = new ADXRS450_Gyro(kGyroPort);
  private Joystick m_joystick = new Joystick(kJoystickPort);
  private Joystick m_joystick2 = new Joystick(kJoystick2Port);
// --end


public static Encoder m_encoder;


  @Override
  public void robotInit() {
 // compressor = new Compressor(1);
  habClimbPistons= new DoubleSolenoid(4, 5);
  m_joystick2 = new Joystick(1);
  m_encoder = new Encoder(4, 5, true, Encoder.EncodingType.k4X);
  m_encoder.setDistancePerPulse((Math.PI * 1.804) / 192);

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
  usbCam.setVideoMode(PixelFormat.kMJPEG, 640, 480, 30);
  CameraServer camera2 = CameraServer.getInstance();
  VideoSource usbCam2 = camera2.startAutomaticCapture("cam1", 1);
  usbCam2.setVideoMode(PixelFormat.kMJPEG, 640, 480, 30);
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
    SmartDashboard.putNumber("Encoder Distance", m_encoder.getDistance());
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
    
    //teleop in auto for sandstorm - Rishikesh 3/20/19
    teleopPeriodic();

    switch (m_autoSelected) {
      case kAutoLine:
      default:
      //code goes here
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
  public void turnDegrees(int degree) {
    if(turned)return;
    angle = m_gyro.getAngle() % 360;
    if(angle-10 > degree)m_myRobot.arcadeDrive(0.8, (angle - degree)*kP);
    else if(angle+10 < degree)m_myRobot.arcadeDrive(0.8, (angle + degree)*kP);
    else turned = true;
    }

  @Override
  public void teleopPeriodic() {
  //encoder top and bottom limits for elevator (tested & working as of 3/22/19)
 // boolean upTriggered = hallEffectSensorTop.get() == false;
  //  boolean downTriggered = hallEffectSensorBottom.get() == false;		
	  double joystickYAxis = m_joystick2.getY();    
   
     if (joystickYAxis>0 && m_encoder.getDistance() < 1 || joystickYAxis<0 && m_encoder.getDistance() > 26)
      {
        elevatorSpark.set(0); 
      }
    else
      {
        if (m_joystick2.getRawButton(5) || m_joystick2.getRawButton(6) || m_joystick2.getRawButton(7) || m_joystick2.getRawButton(8)  || m_joystick.getRawButton(2)) 
        {
          
        }
      	else{
          elevatorSpark.set(m_joystick2.getY()*0.5);
        }
      }
    // --end


//cargo intake if-else control
if (m_joystick2.getRawButton(bCargoIntake)) {
  cargoIntakeSparkLeft.set(0.5);
  cargoIntakeSparkRight.set(-0.5);
} 
else if (m_joystick2.getRawButton(bCargoExpel)) {
  cargoIntakeSparkLeft.set(-1);
  cargoIntakeSparkRight.set(1);
}
else{
  cargoIntakeSparkLeft.set(0);
  cargoIntakeSparkRight.set(0);
}
// --end


//NEW (and working 100%) Gyro Math (tested & working as of 2/9/19)

m_myRobot.arcadeDrive(m_joystick.getY()*0.8, m_joystick.getX()*0.8);

if(m_joystick.getRawButton(1))turned = true;
if(m_joystick.getPOV() != -1){
  turned = false;
  mustTurnDegree = m_joystick.getPOV();
}
if(!turned)turnDegrees(mustTurnDegree);
// --end


//Encoded Elevator "Final" Code - Rishikesh & Kyle 3/21/19 (PA Day 1) - [WORKING 3/21/19]
if(Math.abs(m_encoder.getDistance()-targetDistance) < 2){
  elevatorSpeedAct = elevatorSpeedSlow;
}
else{
  elevatorSpeedAct = elevatorSpeedFast;
}

boolean elevatorButtonPressed = (m_joystick2.getRawButton(bHomeLevel)) || (m_joystick2.getRawButton(bHatchLevel2)) || (m_joystick2.getRawButton(bHatchLevel3)) || (m_joystick.getRawButton(bDriveLevel));

if(m_joystick2.getRawButton(bHomeLevel)){
  targetDistance = homeLevel;
}
if(m_joystick2.getRawButton(bHatchLevel2)){
  targetDistance = hatchLevel2;
}
if(m_joystick2.getRawButton(bHatchLevel3)){
  targetDistance = hatchLevel3;
}
if(m_joystick.getRawButton(bDriveLevel)){
  targetDistance = driveLevel;
}


if(elevatorButtonPressed){

boolean TooLow = (m_encoder.getDistance()-targetDistance) < -0.2;
boolean TooHigh = (m_encoder.getDistance()-targetDistance) > 0.2;

  if (TooLow) {
    elevatorSpark.set(elevatorSpeedAct);
  }
  else if (TooHigh){
    elevatorSpark.set(-elevatorSpeedAct*0.5);
  }
  else {
    elevatorSpark.set(elevatorSpeedStop);
  }
}
else if (m_joystick2.getY() == 0){
  elevatorSpark.set(0);
}
// --end


// //HAB Level 2 Climb Command Chain 3/27/19 ~ Rishikesh

// //step 2 (deploy front pistons as soon as robot is on HAB 1 platform)
// if (m_joystick.getRawButton(5)) {
// habClimbPistons.set(DoubleSolenoid.Value.kForward);  
// }
// //step 4 (HAB elevator goes down until it hits bottom hall sensor)
// else if (m_joystick.getRawButton(6)){
// habClimbElevator.set(-0.5); 
// }
// //step 5 (meghan drives forward till pistons hit front face of HAB)
// else if (habClimbPistonLimitSwitch.get() == false){
// DriverStation.reportWarning("PISTON HIT PISTON HIT PISTON HIT PISTON HIT PISTON HIT PISTON HIT PISTON HIT", false);
// }
// //step 6 (meghan checks whether AT LEAST two front wheels are on HAB base and then retracts piston)
// else if (m_joystick.getRawButton(7)){
// habClimbPistons.set(DoubleSolenoid.Value.kReverse);
// }
// //step 7 (meghan drives forward till HAB elevator hits front face of HAB (75% of robot up on platform))
// else if (habElevatorLimitSwitch.get() == false){
// DriverStation.reportWarning("ELEV HIT ELEV HIT ELEV HIT ELEV HIT ELEV HIT ELEV HIT ELEV HIT ELEV HIT ELEV HIT", false);
// }
// //step 8 (pull HAB elevator up and drive forward again to get robot on platform)
// else if (m_joystick.getRawButton(8)){
// habClimbElevator.set(0.5);
// }
// //step 4 and step 8 hall stop for HAB Elevator
// else if (habElevatorHallBottom.get() == false && m_joystick.getRawButton(6)|| habElevatorHallTop.get() == false && m_joystick.getRawButton(8)) {
//   habClimbElevator.set(0);
//   }
// // --end

// //temp button
// if (m_joystick2.getRawButton(9)) {
//   habClimbPistons.set(DoubleSolenoid.Value.kForward);
// }
// else if (m_joystick2.getRawButton(10)){
//   habClimbPistons.set(DoubleSolenoid.Value.kReverse);
// }
}
/**
   * This function is called periodically during test mode.
   */  
  @Override
  public void testPeriodic() {
  }
}