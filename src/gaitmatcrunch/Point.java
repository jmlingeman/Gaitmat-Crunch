/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gaitmatcrunch;

import java.io.Serializable;

/**
 *
 * @author j4lingeman
 */
public class Point implements Comparable, Serializable {
    double time, x, y, onset, offset;
    int pressure, leftOrRight, foot, objNum;

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

    

    public int getFoot() {
        return foot;
    }

    public void setFoot(int foot) {
        this.foot = foot;
    }

    public int getLeftOrRight() {
        return leftOrRight;
    }

    public void setLeftOrRight(int leftOrRight) {
        this.leftOrRight = leftOrRight;
    }

    public int getObjNum() {
        return objNum;
    }

    public void setObjNum(int objNum) {
        this.objNum = objNum;
    }

    public int getPressure() {
        return pressure;
    }

    public void setPressure(int pressure) {
        this.pressure = pressure;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public Point() {
        time = -1;
        x = -1;
        y = -1;
        pressure = -1;
        leftOrRight = -1;
        foot = -1;
        objNum = -1;
        onset = 99999;
        offset = -99999;
    }

    public Point(double ntime, double nx, double ny,
            int npressure, int nleftOrRight, int nfoot, int nobjNum) {
        time = ntime;
        x = nx;
        y = ny;
        pressure = npressure;
        leftOrRight = nleftOrRight;
        foot = nfoot;
        objNum = nobjNum;
        onset = ntime;
        offset = ntime;
    }

    public int compareTo(java.lang.Object o) {
        Point tmp = (Point) o;

        if (this.time > tmp.time) {
            return 1;
        }
        else if(this.time < tmp.time) {
            return -1;
        }
        else {
            return 0;
        }
    }

    
}
