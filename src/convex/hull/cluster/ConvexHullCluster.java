package convex.hull.cluster;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.Stack;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author tom
 */

    /**
     * @param args the command line arguments
     */
    
class Point2D implements Comparable<Point2D>
{
    public static final Comparator<Point2D> X_ORDER = new XOrder();
    public static final Comparator<Point2D> Y_ORDER = new YOrder();
    public static final Comparator<Point2D> R_ORDER = new ROrder();
    public final Comparator<Point2D> POLAR_ORDER = new PolarOrder();
    public final Comparator<Point2D> ATAN2_ORDER = new Atan2Order();
    public final Comparator<Point2D> DISTANCE_TO_ORDER = new DistanceToOrder();
 
    private final double x; 
    private final double y;
 
    public Point2D(double x, double y)
    {
        if (Double.isInfinite(x) || Double.isInfinite(y))
            throw new IllegalArgumentException("Coordinates must be finite");
        if (Double.isNaN(x) || Double.isNaN(y))
            throw new IllegalArgumentException("Coordinates cannot be NaN");
        if (x == 0.0)
            x = 0.0; 
        if (y == 0.0)
            y = 0.0; 
        this.x = x;
        this.y = y;
    }
 
    public double x()
    {
        return x;
    }
 
    public double y()
    {
        return y;
    }
 
    public double r()
    {
        return Math.sqrt(x * x + y * y);
    }
 
    public double theta()
    {
        return Math.atan2(y, x);
    }
 
    private double angleTo(Point2D that)
    {
        double dx = that.x - this.x;
        double dy = that.y - this.y;
        return Math.atan2(dy, dx);
    }
 
    public static int ccw(Point2D a, Point2D b, Point2D c)
    {
        double area2 = (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
        if (area2 < 0)
            return -1;
        else if (area2 > 0)
            return 1;
        else
            return 0;
    }
 
    public static double area2(Point2D a, Point2D b, Point2D c)
    {
        return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
    }
 
    public double distanceTo(Point2D that)
    {
        double dx = this.x - that.x;
        double dy = this.y - that.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
 
    public double distanceSquaredTo(Point2D that)
    {
        double dx = this.x - that.x;
        double dy = this.y - that.y;
        return dx * dx + dy * dy;
    }
 
    public int compareTo(Point2D that)
    {
        if (this.y < that.y)
            return -1;
        if (this.y > that.y)
            return 1;
        if (this.x < that.x)
            return -1;
        if (this.x > that.x)
            return 1;
        return 0;
    }
 
    private static class XOrder implements Comparator<Point2D>
    {
        public int compare(Point2D p, Point2D q)
        {
            if (p.x < q.x)
                return -1;
            if (p.x > q.x)
                return 1;
            return 0;
        }
    }
 
    private static class YOrder implements Comparator<Point2D>
    {
        public int compare(Point2D p, Point2D q)
        {
            if (p.y < q.y)
                return -1;
            if (p.y > q.y)
                return 1;
            return 0;
        }
    }
 
    private static class ROrder implements Comparator<Point2D>
    {
        public int compare(Point2D p, Point2D q)
        {
            double delta = (p.x * p.x + p.y * p.y) - (q.x * q.x + q.y * q.y);
            if (delta < 0)
                return -1;
            if (delta > 0)
                return 1;
            return 0;
        }
    }
 
    private class Atan2Order implements Comparator<Point2D>
    {
        public int compare(Point2D q1, Point2D q2)
        {
            double angle1 = angleTo(q1);
            double angle2 = angleTo(q2);
            if (angle1 < angle2)
                return -1;
            else if (angle1 > angle2)
                return 1;
            else
                return 0;
        }
    }
 
    private class PolarOrder implements Comparator<Point2D>
    {
        public int compare(Point2D q1, Point2D q2)
        {
            double dx1 = q1.x - x;
            double dy1 = q1.y - y;
            double dx2 = q2.x - x;
            double dy2 = q2.y - y;
 
            if (dy1 >= 0 && dy2 < 0)
                return -1; 
            else if (dy2 >= 0 && dy1 < 0)
                return +1; 
            else if (dy1 == 0 && dy2 == 0)
            { 
                if (dx1 >= 0 && dx2 < 0)
                    return -1;
                else if (dx2 >= 0 && dx1 < 0)
                    return +1;
                else
                    return 0;
            } else
                return -ccw(Point2D.this, q1, q2); 
        }
    }
 
    private class DistanceToOrder implements Comparator<Point2D>
    {
        public int compare(Point2D p, Point2D q)
        {
            double dist1 = distanceSquaredTo(p);
            double dist2 = distanceSquaredTo(q);
            if (dist1 < dist2)
                return -1;
            else if (dist1 > dist2)
                return +1;
            else
                return 0;
        }
    }
 
    public boolean equals(Object other)
    {
        if (other == this)
            return true;
        if (other == null)
            return false;
        if (other.getClass() != this.getClass())
            return false;
        Point2D that = (Point2D) other;
        return this.x == that.x && this.y == that.y;
    }
 
    public String toString()
    {
        return "(" + x + ", " + y + ")";
    }
 
    public int hashCode()
    {
        int hashX = ((Double) x).hashCode();
        int hashY = ((Double) y).hashCode();
        return 31 * hashX + hashY;
    }
 
}
 
class GrahamScan
{
    private Stack<Point2D> hull = new Stack<Point2D>();
 
    public GrahamScan(Point2D[] pts)
    {
 
        int N = pts.length;
        Point2D[] points = new Point2D[N];
        for (int i = 0; i < N; i++)
            points[i] = pts[i];
        Arrays.sort(points);
 
        Arrays.sort(points, 1, N, points[0].POLAR_ORDER);
 
        hull.push(points[0]);
        int k1;
        for (k1 = 1; k1 < N; k1++)
            if (!points[0].equals(points[k1]))
                break;
        if (k1 == N)
            return; 
 
        int k2;
        for (k2 = k1 + 1; k2 < N; k2++)
            if (Point2D.ccw(points[0], points[k1], points[k2]) != 0)
                break;
        hull.push(points[k2 - 1]); 
 
        for (int i = k2; i < N; i++)
        {
            Point2D top = hull.pop();
            while (Point2D.ccw(hull.peek(), top, points[i]) <= 0)
            {
                top = hull.pop();
            }
            hull.push(top);
            hull.push(points[i]);
        }
 
        assert isConvex();
    }
 
    public Iterable<Point2D> hull()
    {
        Stack<Point2D> s = new Stack<Point2D>();
        for (Point2D p : hull)
            s.push(p);
        return s;
    }
 
    private boolean isConvex()
    {
        int N = hull.size();
        if (N <= 2)
            return true;
 
        Point2D[] points = new Point2D[N];
        int n = 0;
        for (Point2D p : hull())
        {
            points[n++] = p;
        }
 
        for (int i = 0; i < N; i++)
        {
            if (Point2D
                    .ccw(points[i], points[(i + 1) % N], points[(i + 2) % N]) <= 0)
            {
                return false;
            }
        }
        return true;
    }
}
public class ConvexHullCluster extends JPanel{

    /**
     * @param args the command line arguments
     */

    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Random rand = new Random();
        int m = 1000;
        int points=1000000;
        int ppc = points/m;
        Dimension size = getSize();
        int w = size.width ;
        int h = size.height;
        
        for(int k=0;k<m;k++)
        {
            int x = Math.abs(rand.nextInt())%w;
            int y = Math.abs(rand.nextInt())%h;
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(new Color(rand.nextInt(255),rand.nextInt(255),rand.nextInt(255)));
            Point2D p2d[]= new Point2D[ppc];
            p2d[0] = new Point2D(x,y);
            for (int i=1;i<p2d.length;i++) 
            {
              x = Math.abs(rand.nextInt())%w;
              y = Math.abs(rand.nextInt())%h;
              if(p2d[i-1].distanceTo(new Point2D(x,y))<=rand.nextInt(100))
              {
               p2d[i]= new Point2D(x,y);   
              }
              else 
              {
                  i--;
              }
            }
            for(int i=0;i<p2d.length;i++)
            {
                g2d.fillOval((int)p2d[i].x(),(int)p2d[i].y(),10,10);
                g2d.drawString(""+i, (float)p2d[i].x(),(float)p2d[i].y());

            }
            GrahamScan graham = new GrahamScan(p2d);
            Point2D t[] = new Point2D[p2d.length];
            int i=0;
            for (Point2D p : graham.hull())
            {
                t[i]=new Point2D(p.x(),p.y());
                i++;
            }
            int j=0;
            for(j=0;j<(i-1);j++)
            {
                g2d.drawLine((int)t[j].x(),(int)t[j].y(),(int)t[j+1].x(),(int)t[j+1].y());
            }
            g2d.drawLine((int)t[j].x(),(int)t[j].y(),(int)t[0].x(),(int)t[0].y());
        }
    }
    public static void main(String[] args) {
        // TODO code application logic here
          long wc =System.currentTimeMillis();
        long cpu=System.nanoTime();
        
        ConvexHullCluster ch = new ConvexHullCluster();
        JFrame frame = new JFrame("Graham Scan");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(ch);
        frame.setSize(720, 560);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        long wc1 =System.currentTimeMillis();
        long cpu1=System.nanoTime();
        System.out.println("\n\nCPU Time : "+(cpu1-cpu)+"\n\nWall-Clock Time : "+(wc1-wc));
    }
    
}
