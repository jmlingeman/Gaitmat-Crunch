/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gaitmatcrunch;

import java.io.Serializable;
import java.util.Vector;

/**
 *
 * @author j4lingeman
 */
public class Walk implements Serializable {
    Vector<Footfall> walk;
    String trialNum;
    double totalDoubleSupport, totalSingleSupport;
    double avgDoubleSupport, avgSingleSupport, totalFlight;
    double velocity, time, avgStepWid, avgStrideLen, avgStepLen;

    public double getOnset() {
        return walk.get(0).getPoint(0).onset;
    }

    public double getOffset() {
        return walk.get(walk.size() - 1).getPoint(walk.get(walk.size() - 1).getNumPoints() - 1).offset;
    }

    public double getTime() {
        return time;
    }

    public int getLength() {
        return walk.size();
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double getTotalDoubleSupport() {
        return totalDoubleSupport;
    }

    public void setTotalDoubleSupport(double totalDoubleSupport) {
        this.totalDoubleSupport = totalDoubleSupport;
    }

    public double getTotalSingleSupport() {
        return totalSingleSupport;
    }

    public void setTotalSingleSupport(double totalSingleSupport) {
        this.totalSingleSupport = totalSingleSupport;
    }

    public String getTrialNum() {
        return trialNum;
    }

    public void setTrialNum(String trialNum) {
        this.trialNum = trialNum;
    }

    

    public double getVelocity() {
        return velocity;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public Walk() {
        trialNum = "-1";
        walk = new Vector<Footfall>();
    }

    public Walk(String t) {
        trialNum = t;
        walk = new Vector<Footfall>();
    }

    public void addStep(Footfall f) {
        walk.add(f);
    }

    public Footfall getStep(int i) {
        return walk.get(i);
    }

    public String toString() {
        return String.valueOf(trialNum);
    }

    


}
