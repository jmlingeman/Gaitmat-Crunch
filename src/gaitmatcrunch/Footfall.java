/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gaitmatcrunch;

import java.io.Serializable;
import java.util.Collections;
import java.util.Vector;

/**
 *
 * @author j4lingeman
 */
public class Footfall implements Comparable, Serializable {
    int objNum;
    Vector<Point> footfall;
    Point heel, toe;
    double onset, offset;
    double initialDoubleSupport, terminalDoubleSupport;
    double singleSupport;
    double cycleDuration = 999;
    double stepWidth = 999, strideLength = 999, stepLength = 999;
    double dynbase = 999;
    int LeftOrRight;

    public int getNumPoints() {
        return footfall.size();
    }

    public int getLeftOrRight() {
        return LeftOrRight;
    }

    public void setLeftOrRight(int LeftOrRight) {
        this.LeftOrRight = LeftOrRight;
    }

    

    public double getStepLength() {
        return stepLength;
    }

    public void setStepLength(double stepLength) {
        this.stepLength = stepLength;
    }

    public double getStrideLength() {
        return strideLength;
    }

    public void setStrideLength(double strideLen) {
        this.strideLength = strideLen;
    }

    public double getStepWidth() {
        return stepWidth;
    }

    public void setStepWidth(double stepWidth) {
        this.stepWidth = stepWidth;
    }

    public double getSingleSupport() {
        return singleSupport;
    }

    public void setSingleSupport(double singleSupport) {
        this.singleSupport = singleSupport;
    }

    public double getInitialDoubleSupport() {
        return initialDoubleSupport;
    }

    public void setInitialDoubleSupport(double initialDoubleSupport) {
        this.initialDoubleSupport = initialDoubleSupport;
    }

    public double getTerminalDoubleSupport() {
        return terminalDoubleSupport;
    }

    public void setTerminalDoubleSupport(double terminalDoubleSupport) {
        this.terminalDoubleSupport = terminalDoubleSupport;
    }
    
    public double getCycleDuration(){
        return cycleDuration;
    }
    public void setCycleDuration(double dur){
        cycleDuration = dur;
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

    public double getOnset() {
        return onset;
    }

    public void setOnset(double onset) {
        this.onset = onset;
    }

    /* Base constructor.*/
    public Footfall(int objNum, Vector<Point> points){
        this.objNum = objNum;
        this.footfall = points;
    }
    
    /* No params constructor.  Calls 1-param with default value -1.
     */
    public Footfall() {
        this(-1);
    }

    /* Single int constructor.  Assigns empty vector of points for footfall. */
    public Footfall(int objNum) {
        this(objNum, new Vector<Point>());
    }

    public void addPoint(Point p) {
        this.footfall.add(p);
    }

    public Point getPoint(int i) {
        return footfall.get(i);
    }

    public void setFootfall(Vector<Point> footfall) {
        this.footfall = footfall;
    }

    public int getObjNum() {
        return objNum;
    }

    public void setObjNum(int objNum) {
        this.objNum = objNum;
    }

    Point findHeel() {
        Vector<Point> heelCandidates = new Vector<Point>();
        double biggestX = 0;

        //Find the largest X
        for(int i = 0; i < footfall.size(); i++) {
            if(footfall.get(i).x > biggestX) {
                biggestX = footfall.get(i).x;
            }
        }

        //Gather all points that have that X in this footfall
        for(int i = 0; i < footfall.size(); i++) {
            if(footfall.get(i).x == biggestX) {
                heelCandidates.add(footfall.get(i));
            }
        }

        //Find the middle heel point.  If even number of points, average
        //the values of the two center-most points.
        heel = new Point();
        if(heelCandidates.size() % 2 == 0) {
            Point q = heelCandidates.get(heelCandidates.size() / 2 - 1);
            Point w = heelCandidates.get(heelCandidates.size() / 2);
            heel.time = (q.time + w.time) / 2.0;
            heel.x = (q.x + w.x) / 2.0;
            heel.y = (q.y + w.y) / 2.0;
            heel.pressure = (q.pressure + w.pressure) / 2;
            heel.objNum = q.objNum;
            heel.leftOrRight = q.leftOrRight;
            heel.foot = q.foot;
        }
        else if(heelCandidates.size() == 1){
            heel = heelCandidates.get(0);
        }
        else {
            heel = heelCandidates.get(heelCandidates.size() / 2 + 1);
        }

        return this.heel;
    }

    Point findToe() {
        Vector<Point> toeCandidates = new Vector<Point>();
        double smallestX = 99999;

        //Find the largest X
        for(int i = 0; i < footfall.size(); i++) {
            if(footfall.get(i).x < smallestX) {
                smallestX = footfall.get(i).x;
            }
        }

        //Gather all points that have that X in this footfall
        for(int i = 0; i < footfall.size(); i++) {
            if(footfall.get(i).x == smallestX) {
                toeCandidates.add(footfall.get(i));
            }
        }

        //Find the middle toe point.  If even number of points, average
        //the values of the two center-most points.
        toe = new Point();
        if(toeCandidates.size() % 2 == 0) {
            Point q = toeCandidates.get(toeCandidates.size() / 2 - 1);
            Point w = toeCandidates.get(toeCandidates.size() / 2);
            toe.time = (q.time + w.time) / 2.0;
            toe.x = (q.x + w.x) / 2.0;
            toe.y = (q.y + w.y) / 2.0;
            toe.pressure = (q.pressure + w.pressure) / 2;
            toe.objNum = q.objNum;
            toe.leftOrRight = q.leftOrRight;
            toe.foot = q.foot;
        }
        else if ( toeCandidates.size() == 1 ){
            toe = toeCandidates.get(0);
        }
        else {
            toe = toeCandidates.get(toeCandidates.size() / 2 + 1);
        }

        return this.toe;
    }

//    double findOnset() {
//        double curOnset = 99999;
//        for(int i = 0; i < footfall.size(); i++) {
//            if(curOnset > footfall.get(i).time) {
//                curOnset = footfall.get(i).time;
//            }
//        }
//        this.onset = curOnset;
//        return this.onset;
//    }
//
//    double findOffset() {
//        double curOffset = 0;
//        for(int i = 0; i < footfall.size(); i++) {
//            if(curOffset < footfall.get(i).time) {
//                curOffset = footfall.get(i).time;
//            }
//        }
//        this.offset = curOffset;
//        return this.offset;
//    }

    double findOnset() {
        double curOnset = 99999;
        for(int i = 0; i < footfall.size(); i++) {
            if(curOnset > footfall.get(i).onset) {
                curOnset = footfall.get(i).onset;
            }
        }
        this.onset = curOnset;
        return this.onset;
    }

    double findOffset() {
        double curOffset = 0;
        for(int i = 0; i < footfall.size(); i++) {
            if(curOffset < footfall.get(i).offset) {
                curOffset = footfall.get(i).offset;
            }
        }
        this.offset = curOffset;
        return this.offset;
    }

    //Goes through the private functions to set up the footfall for processing
    public void findStepParams() {
        Collections.sort(footfall);
        findToe();
        findHeel();
        findOnset();
        findOffset();
    }

    public int compareTo(java.lang.Object o) {
        Footfall tmp = (Footfall) o;

        if (this.onset > tmp.onset) {
            return 1;
        }
        else if(this.onset < tmp.onset) {
            return -1;
        }
        else {
            return 0;
        }
    }

}
