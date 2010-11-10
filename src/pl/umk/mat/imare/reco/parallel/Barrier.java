/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.umk.mat.imare.reco.parallel;

import java.util.concurrent.Semaphore;

/**
 *
 * @author Maciek
 */
public class Barrier {
    public Semaphore sem;
    private int count=0;

    public Barrier()
    {
      sem=new Semaphore(0);
    }

    public synchronized void add(int max)
    {
      if(++count >= max)
      {
        count=0;
        sem.release(max);
      }
    }
}
