/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gaitmatcrunch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 *
 * @author j4lingeman
 */
public class Subject implements Serializable {
    String tdate, bdate, id, study;
    Vector<Walk> walks;


    public Subject(String id) {
        this.id = id;
        walks = new Vector<Walk>();
    }

    public Subject(){
        this.id = "Error: Unassigned";
        walks = new Vector<Walk>();
    }

    public void addWalk(Walk w) {
        walks.add(w);
    }

    public Walk getWalk(int i) {
        return walks.get(i);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBdate() {
        return bdate;
    }

    public void setBdate(String bdate) {
        this.bdate = bdate;
    }

    public String getStudy() {
        return study;
    }

    public void setStudy(String study) {
        this.study = study;
    }

    public String getTdate() {
        return tdate;
    }

    public void setTdate(String tdate) {
        this.tdate = tdate;
    }

    public void addWalkFromFile(String filename, String walkid) {
        File file = new File(filename);
        try{
            String line = null;
            Point p;
            Walk w = new Walk(walkid);
            Footfall f;
            int footNum;

            BufferedReader br = new BufferedReader(new FileReader(file));
            
            while((line = br.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line,",");
                p = new Point();
                p.setTime(Double.valueOf(st.nextToken()));
                p.setX(Double.valueOf(st.nextToken()));
                p.setY(Double.valueOf(st.nextToken()));
                p.setPressure(Integer.valueOf(st.nextToken()));
                footNum = Integer.valueOf(st.nextToken());
                p.setLeftOrRight(Integer.valueOf(st.nextToken()));
                p.setFoot(Integer.valueOf(st.nextToken()));
                
                p.setOnset(p.time);
                p.setOffset(p.time);
                

                if(findObjectNum(footNum, w) != -1) {
                    int found = 0;
                    for(int i = 0; i < w.walk.get(findObjectNum(footNum, w)).getNumPoints(); i++) {
                        if( p.x == w.walk.get(findObjectNum(footNum, w)).getPoint(i).x &&
                                p.y == w.walk.get(findObjectNum(footNum, w)).getPoint(i).y ) {
                            found = 1;
                            Point matchpoint = w.walk.get(findObjectNum(footNum, w)).getPoint(i);
                            if(p.time < matchpoint.onset) {
                                matchpoint.onset = p.time;
                            }
                            if(p.time > matchpoint.offset) {
                                matchpoint.offset = p.time;
                            }
//                            System.out.println("We found a matching point.  New pt: " + " " + matchpoint.x + " " + matchpoint.y + " " + matchpoint.onset + " " + matchpoint.offset + " " + p.time);
                        }
                    }
                    if( found == 0 ) {
                        w.walk.get(findObjectNum(footNum, w)).addPoint(p);
                    }
                    else
                        found = 0;

                }
                else {
                    f = new Footfall();
                    f.setObjNum(footNum);
                    f.setLeftOrRight(p.leftOrRight);
                    f.addPoint(p);
                    w.addStep(f);
                }
                    
            }
            

            // Check to see if this walk is backwards, if so, flip the X coords
            Footfall first = w.getStep(0);
            Footfall last = w.getStep(w.walk.size() - 1);

            if( (last.footfall.get(0).x - first.footfall.get(0).x) > 0 )
            {
                System.out.println("Flipping walk...");
                // Then this walk went in the opposite direction, so flip it
                for(Footfall s : w.walk) {
//                    s.heel.x = Math.abs(400 - s.heel.x);
//                    s.heel.y = Math.abs(30 - s.heel.y);
//
//                    s.toe.x = Math.abs(400 - s.toe.x);
//                    s.toe.y = Math.abs(30 - s.toe.y);
                    
                    for(Point po : s.footfall) {
                        po.x = Math.abs(400 - po.x);
                        po.y = Math.abs(30 - po.y);


                    }
                }
            }

            for(int i = 0; i < w.walk.size(); i++) {
                w.walk.get(i).findStepParams();
            }

            this.addWalk(w);

        }
        catch(IOException e) {
            System.err.println("Error: File not found.");
        }
    }

    public String toString() {
        return "ID: " + this.id + " | Study: " + this.study;
    }

    private int findObjectNum(int objNum, Walk w) {
        for(int i = 0; i < w.walk.size(); i++) {
            if(w.walk.get(i).objNum == objNum) {
                return i;
            }
        }
        return -1;
    }

}
