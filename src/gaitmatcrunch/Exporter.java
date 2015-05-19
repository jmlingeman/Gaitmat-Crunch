/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gaitmatcrunch;

import java.io.File;
import java.io.FileWriter;

/**
 *
 * @author j4lingeman
 */
public class Exporter {
    /* Methods to create:
     * Double Support (and % of stride)
     * Single Support (and % of stride)
     * Velocity
     * Step length
     * Step width
     * Stride length
     * Swing time
     * Dynamic Base (angle between steps)
     * Cycle Duration
     */

    public Exporter (Walk w) {
        setWalkStats(w);
    }

    public Exporter (Subject s) {
        for(int i = 0; i < s.walks.size(); i++) {
            setWalkStats(s.walks.get(i));
        }
    }

    private void calculateTotalTime(Walk w) {
        int verbose = 1;
        if(verbose>0){
            System.out.println("Calculating total time for walk: " + w);
        }
        if(verbose>1){
            System.out.printf("Time 1: %f, Time 2: %f, Diff: %f\n",
                w.walk.get(w.walk.size()-1).onset,
                w.walk.get(0).onset,
                w.walk.get(w.walk.size()-1).onset - w.walk.get(0).onset);
        }
        w.time = w.walk.get(w.walk.size()-1).offset - w.walk.get(0).onset;
        if(verbose>0){
            System.out.println("Finished calculating total time.");
        }
    }

    /* This function will calculate the double support for each footfall
     * as well as calculate averages and total double support times.

        Double support for the first step is always 0.
        Calculate the initial and temrinal seperately.*/
    private void calculateDoubleSupport(Walk w) {
        int verbose = 1;
        if(verbose>0){
            System.out.println("Calculating double support for walk: " + w);
        }
        
        //TODO: Do percentages as well as avgs
        
        for(int i = 1; i < w.walk.size(); i++) {
            Footfall currentStep = w.walk.get(i);
            Footfall prevStep = w.walk.get(i-1);
            if(verbose>1){
                System.out.println(i + " " + prevStep.offset+ " "+ currentStep.onset + " "+ (prevStep.offset - currentStep.onset));
            }
            currentStep.setInitialDoubleSupport(prevStep.offset - currentStep.onset);
            prevStep.setTerminalDoubleSupport(prevStep.offset - currentStep.onset);
        }
        //Calculate the avg Double support and avg flight time.
        //Pos values are DS and neg values are flight.
        double culmDS = 0;
        double culmFlight = 0;
        for(int i = 1; i < w.walk.size(); i++) {
            if(w.walk.get(i).getInitialDoubleSupport() >= 0) {
                culmDS += w.walk.get(i).getInitialDoubleSupport();
            }
            else {
                culmFlight += -w.walk.get(i).getInitialDoubleSupport();
            }
        }
//        w.time += culmDS + culmFlight;
        w.totalDoubleSupport = culmDS;
        w.totalFlight = culmFlight;
        if(verbose>0){
            System.err.println("Finished calculating double support.");
        }
    }

    private void calculateSingleSupport(Walk w) {
        int verbose = 1;
        if(verbose > 0){
            System.out.println("Calculating single support for walk: " + w);
        }
        double sumSingle = 0, cTime, initDS, termDS;
        for(int i = 1; i < w.walk.size() - 1; i++) {
            Footfall currentStep = w.walk.get(i);
            Footfall prevStep = w.walk.get(i-1);
            Footfall nextStep = w.walk.get(i+1);
            cTime = currentStep.getOffset() - currentStep.getOnset();
//
//            initDS = currentStep.getInitialDoubleSupport();
//            termDS = currentStep.getTerminalDoubleSupport();
//            if(initDS < 0)
//                initDS = 0;
//            if(termDS < 0)
//                termDS = 0;
//
//            currentStep.setSingleSupport(cTime - initDS - termDS);
            double prev = prevStep.offset - currentStep.onset;
            double next = currentStep.offset - nextStep.onset;

            if (prev < 0)
                prev = 0;
            if (next < 0)
                next = 0;

            currentStep.setSingleSupport(cTime - prev - next);

            if(verbose > 1){
                System.out.println("Time: " + cTime + " InitDS: " + currentStep.getInitialDoubleSupport() + " TermDS: "
                    + currentStep.getTerminalDoubleSupport() + " SS: " + currentStep.getSingleSupport());
            }
            sumSingle += currentStep.getSingleSupport();
        }
        w.totalSingleSupport = sumSingle;
//        w.time += sumSingle;
        if(verbose > 0){
            System.out.println("Finished calculating single support.");
        }
    }
    
    /* Calculates the cycle duration for each foot.  Consulted DKL on definition:
     * The cycle duration for each footfall is the duration between the onset of
     * the current step and the onset of two steps prior.
     */
    private void calculateCycleDuration(Walk w){   
        int verbose = 1;
        if(verbose>0){
            System.out.println("Calculating cycle duration for walk: " + w);
        }
        // iterate from 3rd footfall to last footfall to calculate cycle durations
        for(int i = 2; i<w.walk.size()-1; i++){
            Footfall curr = w.getStep(i);
            Footfall prev = w.getStep(i-2);
            double cycleDuration = curr.onset-prev.onset;
            curr.setCycleDuration(cycleDuration);
        }
        
        if(verbose>0){
            System.out.println("Finished calculating cycle duration.");
        }
    }

   /* CalculateVelocity():
    * Calculates the average speed of the run in cm/s according to the time
    * and distance between the first sensor contact and the last sensor contact.
    */
   private void calculateVelocity(Walk w) {
       int verbose = 1;
       if(verbose>0){
           System.out.println("Calculating velocity for walk: " + w);
       }
       
       Footfall first = w.walk.get(0);
       Footfall last = w.walk.get(w.walk.size()-1);

//       w.velocity = ((first.heel.x - last.toe.x) * 1.27)
//               / (last.onset - first.onset);
       w.velocity = ((first.footfall.get(0).x - last.footfall.get(0).x) * 1.27)
               / (last.onset - first.onset);
       
       if(verbose>0){
           System.out.println("Finished calculating velocity");
       }
   }

	/* Calculate velocity by dividing the sum of step lengths by the duration from first footfall onset to last footfall onset.
	 */
   private void calculateVelocity2(Walk w) {
	   Footfall first = w.walk.get(0);
	   Footfall last = w.walk.get(w.walk.size()-1);

           double dist = 0;
	   for(int i=0; i<w.walk.size()-1; i++){
		   dist+= w.walk.get(i).getStepLength();
	   }
	   w.velocity = dist/(last.onset-first.onset);
   }

   /* CalculateStepWidAndLen:
    * This function will calculate the step width and length
    * of each step in the walk by first generating a point on the
    * line perpendicular to the line of progression, then calculating
    * the intersection between the line of progression and the perpendicular
    * line.  The lenghts of the lines describing the length and width of the
    * step step are then calculate.  An average is kept for reporting later.
    */
   private void calculateStepWidAndLen(Walk w) {
       int verbose = 1;
       if(verbose>0){
           System.out.println("Calculating step width and length for walk: " + w);
       }
       Footfall oppPrev, oppNext, current;
       double sumStepLen = 0, sumStepWid = 0;
       for(int i = 1; i < w.walk.size() - 1; i++) {
           oppPrev = w.walk.get(i-1);
           current = w.walk.get(i);
           oppNext = w.walk.get(i+1);

           Point perpLine = calculatePerpLinePoint(oppPrev.heel,
                    oppNext.heel, current.heel);
           Point L = calculateLineIntersect(oppPrev.heel, oppNext.heel,
                    current.heel, perpLine);

           // Quickfix for L==null
           if(L==null)
               continue;
           
           int crossStep = 1;
           if(current.LeftOrRight == 1 && L.y < current.heel.y) {
                crossStep = -1;
           }
           else if(current.LeftOrRight == 0 && L.y > current.heel.y) {
                crossStep = -1;
           }

           // This direction is because of the way we position our runners
           int negLen = 1;
           if(oppPrev.heel.x < current.heel.x) {
               negLen = -1;
           }

           current.setStepWidth(crossStep * calculateLineLength(current.heel, L));
           current.setStepLength(negLen * calculateLineLength(oppPrev.heel, L));
           sumStepWid += current.getStepWidth();
           sumStepLen += current.getStepLength();
       }
       w.avgStepWid = sumStepWid / w.walk.size();
       w.avgStepLen = sumStepLen / w.walk.size();
       
       if(verbose>0){
           System.out.println("Finished calculating step width and step length.");
       }
   }

   /* CalculateStrideLengths:
    * This function will calculate the stride lengths of each step
    * to the next step of that same foot.  It is calculated by the length
    * of the line of progression between heel point and heel point.
    */
   private void calculateStrideLengths(Walk w) {
       int verbose = 1;
       if(verbose>0){
           System.out.println("Calculating stride length for walk: " + w);
       }
       
       Footfall oppPrev, current, oppNext;
       double sumStrideLen = 0;
       for(int i = 1; i < w.walk.size() - 1; i++) {
           current = w.walk.get(i);
           oppPrev = w.walk.get(i - 1);
           oppNext = w.walk.get(i + 1);
           current.setStrideLength(
                   calculateLineLength(oppPrev.heel, oppNext.heel));
           sumStrideLen += current.getStrideLength();
       }
       w.avgStrideLen = sumStrideLen / w.walk.size();
       
       if(verbose>0){
           System.out.println("Finished calculating stide length.");
       }
   }

   private double dotProduct(Point A, Point B) {
       return A.x * B.x + A.y * B.y;
   }

   private double vectorLength(Point A) {
       return Math.sqrt(dotProduct(A, A));
   }

   private void vectorNormalize(Point A) {
       double temp;
       temp = 1.0 / vectorLength(A);

       A.x *= temp;
       A.y *= temp;
   }

   private double angleDegree(Point A, Point B) {
       vectorNormalize(A);
       vectorNormalize(B);

       return Math.toDegrees(Math.acos(dotProduct(A,B)));
   }

   private void calculateDynamicBase(Walk w) {
       int verbose = 1;
       if(verbose>0){
           System.out.println("Calculating dynamic base angle for walk : " + w);
       }
       Footfall oppPrev, current, oppNext;
       Point anchor, prev, next;
       double angle = 0, len1 = 0, len2 = 0, dotproduct = 0;
       for(int i = 1; i < w.walk.size() - 1; i++) {
           current = w.walk.get(i);
           oppPrev = w.walk.get(i - 1);
           oppNext = w.walk.get(i + 1);

           anchor = current.heel;
           prev = oppPrev.heel;
           next = oppNext.heel;

           Point v1 = new Point();
           Point v2 = new Point();
           v1.x = anchor.x - prev.x;
           v1.y = anchor.y - prev.y;
           
           v2.x = anchor.x - next.x;
           v2.y = anchor.y - next.y;
           
           current.dynbase = angleDegree(v1, v2);
       }
       
       if(verbose>0){
           System.out.println("Finished calculating dynamic base angle.");
       }
   }


//   private Point calculateLineIntersect(Point q1, Point q2, Point w1, Point w2) {
//        Point intersect = new Point();
//        double slope1, offset1, slope2, offset2;
//
//        if(w2.x == -1 && w2.y == -1) {
//            //This is the handler for 0 slope-same-foot heel points.
//            intersect.x = w1.x;
//            intersect.y = q1.y;
//        }
//        else{
//            // Find the slope and offset of line 1
//            slope1 = (q2.y - q1.y) / (q2.x - q1.x);
//            offset1 = q1.y - slope1 * q1.x;
//
//            // Find the slope and offset of line 2
//            slope2 = (w2.y - w1.y) / (w2.x - w1.x);
//            offset2 = w2.y - slope2 * w2.x;
//
//            if(slope1 - slope2 != 0) {
//                intersect.x = (offset2 - offset1) / (slope1 - slope2);
//            }
//            intersect.y = slope1 * intersect.x + offset1;
//        }
//
//        return intersect;
//   }

   private Point calculateLineIntersect(Point q1, Point q2, Point w1, Point w2) {
    //if (! line1.intersectsLine(line2) ) return null;
      double px = q1.x,
             py = q1.y,
             rx = q2.x-px,
             ry = q2.y-py;
      double qx = w1.x,
            qy = w1.y,
            sx = w2.x-qx,
            sy = w2.y-qy;

      double det = sx*ry - sy*rx;
      if (det == 0) {
        return null;
      } else {
        double z = (sx*(qy-py)+sy*(px-qx))/det;
        if (z==0 || z==1) return null;  // intersection at end point!
        Point i = new Point();
        i.x = (double)(px+z*rx);
        i.y = (double)(py+z*ry);
        return i;
      }
 } // end intersection line-line

   private Point calculatePerpLinePoint(Point line1, Point line2, Point perpPoint) {
        double slope, inverseSlope, offset;
        Point perpPoint2  = new Point();
        
        //Handler if the slope is 0 to avoid div-by-0
        if(line2.x - line1.x == 0) {
            line2.x = line2.x + 0.0001;
        }
        if(line2.y - line1.y == 0) {
            line2.y = line2.y + 0.0001;
        }
        slope = (line2.y - line1.y) / (line2.x - line1.x);
        inverseSlope = (-1.0) * Math.pow(slope, -1.0);
        offset = perpPoint.y - inverseSlope * perpPoint.x;

        //Now find another point on the line so we can find the intersect point above

        perpPoint2.x = perpPoint.x + 1;
        perpPoint2.y = inverseSlope * perpPoint2.x + offset;

        return perpPoint2;
   }



   /* calculateLineLength:
    * Returns the length, in cm, of the line between the two sensors.
    * Each sensor is 1.27cm apart.
    */
   private double calculateLineLength(Point p, Point q) {
        return 1.27 * Math.sqrt(Math.pow(q.x - p.x, 2) + Math.pow(q.y - p.y, 2));
   }

   public void exportToMacshapa(Subject s, File fileName, Boolean header) {
       try {
           FileWriter write = new FileWriter(fileName, true);
           Walk curWalk;
           Footfall curFoot;
           String temp;
           if(header) {
               temp = "#ID,Study,tDate,bDate,trialNum,objNum,onset,offset,LorR,heel x,"
                       + "heel y,toe x,toe y,initDoubleSup,termDoubleSup,"
                       + "singleSupport,stepLen,stepWidth,strideLen,walkVelocity,dynamicBase,cycleDuration\n";
               write.append(temp);
           }
           for(int i = 0; i < s.walks.size(); i++) {
               curWalk = s.walks.get(i);
               for(int j = 0; j < curWalk.walk.size(); j++) {
                   curFoot = curWalk.walk.get(j);
                   temp = s.id + ',' + s.study + ',' + s.tdate + ',' 
                           + s.bdate + ','
                       + curWalk.getTrialNum() + ','
                       + String.valueOf(curFoot.objNum) + ','
                       + String.valueOf(curFoot.onset) + ','
                       + String.valueOf(curFoot.offset) + ','
                       + String.valueOf(curFoot.LeftOrRight) + ','
                       + String.valueOf(curFoot.heel.x) + ','
                       + String.valueOf(curFoot.heel.y) + ','
                       + String.valueOf(curFoot.toe.x) + ','
                       + String.valueOf(curFoot.toe.y) + ','
                       + String.valueOf(curFoot.initialDoubleSupport) + ','
                       + String.valueOf(curFoot.terminalDoubleSupport) + ','
                       + String.valueOf(curFoot.singleSupport) + ','
                       + String.valueOf(curFoot.stepLength) + ','
                       + String.valueOf(curFoot.stepWidth) + ','
                       + String.valueOf(curFoot.strideLength) + ','
                       + String.valueOf(curWalk.velocity) + ','
                       + String.valueOf(curFoot.dynbase)
                       + curFoot.cycleDuration + '\n';
                   write.append(temp);
                   write.flush();
               }

           }
           write.close();
       }
       catch (Exception e) {
           e.printStackTrace();
       }
   }

   
    public void exportToSingleFile(Subject s, FileWriter write, Boolean header) {
       try {
           //FileWriter write = new FileWriter(fileName, true);
           Walk curWalk;
           Footfall curFoot;
           String temp;
           if(header) {
               temp = "#ID,Study,tDate,bDate,trialNum,objNum,onset,offset,LorR,heel x,"
                       + "heel y,toe x,toe y,initDoubleSup,termDoubleSup,"
                       + "singleSupport,stepLen,stepWidth,strideLen,walkVelocity,dynamicBase,cycleDuration\n";
               write.append(temp);
           }
           for(int i = 0; i < s.walks.size(); i++) {
               curWalk = s.walks.get(i);
               for(int j = 0; j < curWalk.walk.size(); j++) {
                   curFoot = curWalk.walk.get(j);
                   temp = s.id + ',' + s.study + ',' + s.tdate + ',' 
                           + s.bdate + ','
                       + curWalk.getTrialNum() + ','
                       + String.valueOf(curFoot.objNum) + ','
                       + String.valueOf(curFoot.onset) + ','
                       + String.valueOf(curFoot.offset) + ','
                       + String.valueOf(curFoot.LeftOrRight) + ','
                       + String.valueOf(curFoot.heel.x) + ','
                       + String.valueOf(curFoot.heel.y) + ','
                       + String.valueOf(curFoot.toe.x) + ','
                       + String.valueOf(curFoot.toe.y) + ','
                       + String.valueOf(curFoot.initialDoubleSupport) + ','
                       + String.valueOf(curFoot.terminalDoubleSupport) + ','
                       + String.valueOf(curFoot.singleSupport) + ','
                       + String.valueOf(curFoot.stepLength) + ','
                       + String.valueOf(curFoot.stepWidth) + ','
                       + String.valueOf(curFoot.strideLength) + ','
                       + String.valueOf(curWalk.velocity) + ','
                       + String.valueOf(curFoot.dynbase) + ','
                       + curFoot.cycleDuration + "\n";
                   write.append(temp);
                   write.flush();
               }

           }
           //write.close();
       }
       catch (Exception e) {
           e.printStackTrace();
       }
   }
      
      
   public void setWalkStats(Walk w) {
       w.time = 0;
       calculateTotalTime(w);
       calculateDoubleSupport(w);
       calculateSingleSupport(w);
       calculateVelocity(w);
       calculateStepWidAndLen(w);
       calculateStrideLengths(w);
       calculateDynamicBase(w);
       calculateCycleDuration(w);
   }
}
