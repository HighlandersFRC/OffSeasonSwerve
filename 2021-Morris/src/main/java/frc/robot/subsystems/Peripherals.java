// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.SPI.Port;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.sensors.Navx;

public class Peripherals extends SubsystemBase {
  private final AHRS ahrs = new AHRS(Port.kMXP);

  private final Navx navx = new Navx(ahrs);
  /** Creates a new Peripherals. */
  public Peripherals() {
   
  }

  public double getNavxAngle() {
    return navx.currentAngle();
}

  public void zeroNavx() {
    navx.softResetAngle();
}

  public double getNavxYaw(){
    return navx.currentYaw();
  }

  public double getNavxPitch(){
    return navx.currentPitch();
  }

  public double getNavxRoll(){
    return navx.currentRoll();
  }


  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }
}
