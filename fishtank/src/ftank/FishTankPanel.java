package ftank;
 
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.event.*;
import java.awt.image.*;
import java.io.File;
import java.net.URL;
import javax.imageio.ImageIO;
import java.util.*;
import java.text.NumberFormat;
import javax.swing.text.NumberFormatter;
import java.beans.*; //property change stuff
import java.lang.*;
import static java.lang.System.err;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;

public class FishTankPanel extends JPanel implements ActionListener{
    //Specify the look and feel to use.  Valid values:
    //null (use the default), "Metal", "System", "Motif", "GTK+"
    private final static String LOOKANDFEEL = "Metal";
    
    //components referenced by methods
    private FishTank ft;  //animation area
    private JButton stopB;  //animation on/off toggle
    private JLabel stopLabel;  //displays animation status
    private JTextArea textArea;  //analysis output
    private SalinitySlide sSlide;  //JPanel for user input of salinity
    private SliderPair tempSlider;  //JPanel for user input of temperature
    
    public FishTankPanel() {
        /*** initialize left panel layout
        **start by creating components, ft is included because it is needed
        **as an argument for a constructor***/
        LeftBoxPanel leftP = new LeftBoxPanel();
        StockTable st = new StockTable();
        ft = new FishTank(st);
        AddFishPanel afp = new AddFishPanel(ft);
        AddPlantPanel app = new AddPlantPanel(ft);
        AdjustmentSlide aSlide = new AdjustmentSlide(ft);
        JButton removeB = new JButton("Remove Item");
        JButton clearB = new JButton("Empty Tank");
        
        removeB.setActionCommand("rem");
        clearB.setActionCommand("clr");
        removeB.addActionListener(this);
        clearB.addActionListener(this);
        
        leftP.setLayout(new BoxLayout(leftP, BoxLayout.Y_AXIS));
        leftP.addBoxElement(afp);
        leftP.addBoxElement(app);
        leftP.addBoxElement(aSlide);
        leftP.addBoxElement(st);
        leftP.addBoxElement(removeB);
        leftP.addBoxElement(clearB);
        
        /***set up fishtank***/
        FishTankFrame ftf = new FishTankFrame();
        ftf.add(ft,BorderLayout.CENTER);
        
        /***set up bottom panel layout***/
        JPanel bottomP = new JPanel();
        tempSlider = new SliderPair();
        sSlide = new SalinitySlide();
        JPanel stopPanel = new JPanel();
        stopB = new JButton("Stop Animation");
        stopLabel = new JLabel("Animation is On");
        JButton analyzeB = new JButton("Analyze Viability");
        textArea = new JTextArea(6, 40);
        JScrollPane scrollPane = new JScrollPane(textArea); 
        
        stopPanel.setLayout(new BoxLayout(stopPanel, BoxLayout.Y_AXIS));
        stopB.setActionCommand("stop");
        stopB.addActionListener(this);
        analyzeB.setActionCommand("analyze");
        analyzeB.addActionListener(this);
        textArea.setEditable(false);

        stopB.setAlignmentX(Component.CENTER_ALIGNMENT);
        stopLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        analyzeB.setAlignmentX(Component.CENTER_ALIGNMENT);
        stopPanel.add(stopLabel);
        stopPanel.add(stopB);
        stopPanel.add(analyzeB);

        bottomP.setLayout(new FlowLayout());
        bottomP.add(tempSlider);
        bottomP.add(sSlide);
        bottomP.add(stopPanel);
        bottomP.add(scrollPane);
        
        /********  set up main Layout **********/
        setLayout(new BorderLayout(10,10));
        setOpaque(true);
        setBackground(new Color(200, 200, 200));
        setBorder(BorderFactory.createTitledBorder("Virtual Aquarium"));
        
        add(leftP,BorderLayout.LINE_START);
        add(ftf,BorderLayout.LINE_END);
        add(bottomP,BorderLayout.PAGE_END);
    }
    private class LeftBoxPanel extends JPanel{
        private Dimension d = new Dimension(150, 600);
        void addBoxElement(JComponent jc){
            jc.setAlignmentX(Component.CENTER_ALIGNMENT);
            jc.setMaximumSize(d);
            add(jc);
        }
        public Dimension getPreferredSize(){
            return new Dimension(160, 650);
        }
    }
    /***these two methods are invoked by javascript based on document events
    concerning the visibility of the applet on the users screen***/
    public void setHidden() {
        ft.setHidden();
    }
    public void setVisible() {
        ft.setVisible();
    }
    public void actionPerformed(ActionEvent e) {
        //stop animation
        if ("stop".equals(e.getActionCommand())){
            ft.setOff();
            stopLabel.setText("Animation is Off");
            stopB.setText("Start Animation");
            stopB.setActionCommand("start");
        }
        //start animation
        else if ("start".equals(e.getActionCommand())){
            ft.setOn();
            stopLabel.setText("Animation is On");
            stopB.setText("Stop Animation");
            stopB.setActionCommand("stop");
        }
        //remove an item from the tank
        else if ("rem".equals(e.getActionCommand()))
            ft.deduct();
        //clear the tank of items
        else if ("clr".equals(e.getActionCommand()))
            ft.clear();
        //run the analysis
        else if ("analyze".equals(e.getActionCommand())){
            textArea.setText(ft.analyze(sSlide.getSalinity(), tempSlider.getTemperature()));
        }
    }
    private static void initLookAndFeel() {
        String lookAndFeel = null;
 
        if (LOOKANDFEEL != null) {
            if (LOOKANDFEEL.equals("Metal")) {
                lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
            } else if (LOOKANDFEEL.equals("System")) {
                lookAndFeel = UIManager.getSystemLookAndFeelClassName();
            } else if (LOOKANDFEEL.equals("Motif")) {
                lookAndFeel = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
            } else if (LOOKANDFEEL.equals("GTK+")) { //new in 1.4.2
                lookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
            } else {
                System.err.println("Unexpected value of LOOKANDFEEL specified: "
                                   + LOOKANDFEEL);
                lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
            }
 
            try {
                UIManager.setLookAndFeel(lookAndFeel);
            } catch (ClassNotFoundException e) {
                System.err.println("Couldn't find class for specified look and feel:"
                                   + lookAndFeel);
                System.err.println("Did you include the L&F library in the class path?");
                System.err.println("Using the default look and feel.");
            } catch (UnsupportedLookAndFeelException e) {
                System.err.println("Can't use the specified look and feel ("
                                   + lookAndFeel
                                   + ") on this platform.");
                System.err.println("Using the default look and feel.");
            } catch (Exception e) {
                System.err.println("Couldn't get specified look and feel ("
                                   + lookAndFeel
                                   + "), for some reason.");
                System.err.println("Using the default look and feel.");
                e.printStackTrace();
            }
        }
    }
    /*** Create the GUI and show it invoking method from the
     * event-dispatching thread for thread safety.*/
    private static void createAndShowGUI() {
        //Set the look and feel.
        initLookAndFeel();
 
        //Create and set up the window.
        JFrame frame = new JFrame("Fish Tank");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Create and set up the content pane.
        FishTankPanel ftp = new FishTankPanel();
//        ftp.setOpaque(true); //content panes must be opaque
        frame.setContentPane(ftp);
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}

class FishTankFrame extends JPanel{  //border around the animation area
    private static BufferedImage img = null;
    private static URL url;
    int width; int height;
    
    public FishTankFrame(){
        url = FishTankPanel.class.getResource("wallpanel.png");
        setLayout(new BorderLayout(0,0));
        setBorder(BorderFactory.createEmptyBorder(50,50,50,50));
        try {
            img =  ImageIO.read(url);
        } catch (Exception e) {img = null;}
        width = img.getWidth(null);
        height = img.getHeight(null);
    }
    public Dimension getPreferredSize(){
        return new Dimension(1025, 650);
    }
    public void paintComponent(Graphics g){
        Dimension size = getSize();
        g.drawImage(img, 0, 0, size.width, size.height, 0, 0, width, height, null);
    }
}

class FishTank extends Component implements ActionListener{  //animation area
    private final int FPS_INIT = 60;  //frames per second
    private final int delay = 1000 / FPS_INIT;
    private boolean moving = true;  //user determined animation status
    private javax.swing.Timer timer;
    private java.util.List<TankItem> items = new LinkedList<TankItem>();
    private java.util.List<StaticItem> accessories = new LinkedList<StaticItem>();
    private Iterator<StaticItem> accessIter = null;
    private StaticItem highlighted = null;  //accessory selected for relocation
    private StockTable stockRef;  //table displaying contents
    
    FishTank(StockTable stockArg){
        stockRef = stockArg;
        timer = new javax.swing.Timer(delay, this);
        timer.setCoalesce(true);
    }
    public String analyze(int saline, int temperature){  //display viability of tank
        /*** cycle through the tank contents and for each fish get its LifeTime
         * object.  Then display results ***/
        String result = "";
        java.util.List<LifeTime> lives = new ArrayList<LifeTime>();
        Iterator<TankItem> e = items.iterator();
        
        while(e.hasNext()) {
            TankItem nf = e.next();
            if (nf instanceof DynamicItem)
                lives.add(((DynamicItem)nf).getLifeTime(saline, temperature));
        }
        Collections.sort(lives);
        Iterator<LifeTime> lti = lives.iterator();
        while(lti.hasNext()) {
            LifeTime lt = lti.next();
            result += "a " + lt.species + " lived for " + lt.duration + " days\n";
        }
        return result;
    }
    public Dimension getPreferredSize(){
        return new Dimension(910, 550);
    }
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Dimension size = getSize();
        g2.setPaint(Color.BLUE);
        g2.fill(new Rectangle2D.Float(0,0,size.width,size.height));
        
        Iterator<TankItem> e = items.iterator();
        while(e.hasNext()) {
            TankItem ti = e.next();
            ti.show(g2);
        }
    }
    public void addFish(String s, int qt){  //add fish to the collection
        try{
            Class<?> theClass = Class.forName("ftank." + s);
            Class[] ctorArgs = {ftank.FishTank.class};
            Constructor ctor = theClass.getDeclaredConstructor(ctorArgs);
            for(int i=0;i<qt;i++){items.add((TankItem)ctor.newInstance(this));}
        } catch (ClassNotFoundException e) {
	    e.printStackTrace();
	} catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            err.format("%n%nCaught exception: %s%n", e.getCause());
        } catch (Exception e){
            System.out.println("Some other failure");
        }
//            stock.add(Fish.Factory.createFish("ClownFish", this, x, y));
        if ((! timer.isRunning()) && moving) timer.start();
        else if (! timer.isRunning()) repaint();
        loadTable();
    }  
    public void addPlant(String s, int qt){  //add plants to collection
        try{
            Class<?> theClass = Class.forName("ftank." + s);
            Class[] ctorArgs = {ftank.FishTank.class};
            Constructor ctor = theClass.getDeclaredConstructor(ctorArgs);
            for(int i=0;i<qt;i++){
                StaticItem si = (StaticItem)ctor.newInstance(this);
                items.add(si);
                accessories.add(si);
            }
        } catch (ClassNotFoundException e) {
	    e.printStackTrace();
	} catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            err.format("%n%nCaught exception: %s%n", e.getCause());
        } catch (Exception e){
            System.out.println("Some other failure");
        }
        accessIter = null;
        if (! timer.isRunning()) repaint();
        loadTable();
    }
    public void clear(){  //empty tank of contents
        items.clear();
        unSelect();
        accessories.clear();
        loadTable();
        timer.stop();
        repaint();
    }
    public void deduct(){  //remove most recently added item
        if (items.size() > 0){
            TankItem ti = items.remove(items.size()-1);
            if (ti instanceof StaticItem){
                accessories.remove(accessories.size() -1);
            }
            loadTable();
        }
        if (items.size() == accessories.size()) timer.stop();
        if (! timer.isRunning()) repaint();
    } 
    public int select(){  //highlight and get reference of accessory to be relocated
        if ((accessIter == null) || (! accessIter.hasNext()))
            accessIter = accessories.iterator();
        if (accessIter.hasNext()){
            if (highlighted != null) highlighted.setUnselected();
            highlighted = accessIter.next();
            highlighted.setSelected();
        } else highlighted = null;
        if (! timer.isRunning()) repaint();
        if (highlighted != null)
            return highlighted.getPos();
        else return 200;
    }
    public void unSelect(){  //unselect accessory for relocation
        accessIter = null;
        if (highlighted != null) highlighted.setUnselected();
        highlighted = null;
        if (! timer.isRunning()) repaint();
    }
    public void moveSelected(int newX){  //move selected accessory
        if (highlighted != null) {
            highlighted.setX(newX);
        if (! timer.isRunning()) repaint();
        }
    }
    public void loadTable(){  //display the contents of the fish tank
        /*** here we create a Tally object for each type of item. Tally stores the quantity
         * of that type present in the tank ***/
        stockRef.clearTable();
        java.util.List<Tally> tallies = new Vector<Tally>();
        Iterator<TankItem> e = items.iterator();
        while(e.hasNext()) {
            TankItem nf = e.next();
            String s = nf.getClass().getSimpleName();
            Boolean hit = false;
            Iterator<Tally> ef = tallies.iterator();
            while(ef.hasNext() && ! hit) {
                Tally nex = ef.next();
                if (nex.getName().equals(s)){
                    nex.increment();
                    hit = true;
                }
            }
            if(! hit) tallies.add(new Tally(s));
        }
        Iterator<Tally> ef = tallies.iterator();
        while(ef.hasNext()) {
            stockRef.addRow(ef.next().getTotal());
        }
        stockRef.fireTableDataChanged();
    }
    private class Tally {
        private String s;
        private int tal = 1;
        Tally (String sArg){s = sArg;}
        public String getName(){return s;}
        public void increment(){tal++;}
        public String[] getTotal(){
            String intS = "" + tal;
            String[] finalTally = {intS, s};
            return finalTally;
        }
    }
    public void actionPerformed(ActionEvent e) {  //triggered by timer
        repaint();
    }
    public void setHidden(){timer.stop();}  //triggered by browser
    public void setOn(){  //user selected
        moving = true;
        if (items.size() > accessories.size()) timer.start();
    }
    public void setOff(){  //user selected
        moving = false;
        timer.stop();
    }
    public void setVisible(){  //triggered by browser
        if (moving) timer.start();
    }
}

abstract class TankItem{  // all tank contents inherit from this
    protected BufferedImage img = null;
    protected int width;
    protected int height;
    protected float px, py;
    protected FishTank ft = null;
    
    protected TankItem(FishTank fftt){
        ft = fftt;
    }
    public abstract void show(Graphics2D g2);
    protected static BufferedImage stringToImg(String s){
        URL url = FishTankPanel.class.getResource(s);
        try {return ImageIO.read(url);}
        catch (Exception e) {return null;}
    }
}

abstract class DynamicItem extends TankItem{  // tank items which move themselves
    protected int speed;
    protected boolean direction;
    protected int vertMode = 1;
    
    protected DynamicItem(FishTank fftt){
        super(fftt);
        Random rand = new Random();
        if (rand.nextInt(2) == 1) direction = true;
        else direction = false;
    }
    /*********** Factory Strategy Technique **********
    static class Factory {
        private static final Map<String, Strategy> STRATEGY_MAP = new HashMap<String, Strategy>();
        static void addStrategy(String identifier, Strategy strategy) {
            STRATEGY_MAP.put(identifier, strategy);
        }
        public static Fish createFish(String identifier, FishTank fftt, int x, int y) {
            return STRATEGY_MAP.get(identifier).createFish(fftt, x, y);
        }
        interface Strategy<F extends Fish> {
            F createFish(FishTank fftt, int x, int y);
        }
    }
    **********************/
    public void show(Graphics2D g2){  //display to screen
        int x = Math.round(px);
        int y = Math.round(py);
        try {if(direction) 
            g2.drawImage(img,
                x, y, width + x, height + y,
                0, 0, width, height, null); else
            g2.drawImage(img,
                x, y, width + x, height + y,
                width, 0, 0, height, null);
        } catch (Exception e) {}
        advance();
    }
    private void advance(){  //  generic motion
        if (px < 10) direction = false;
        if (ft.getSize().width - px - width < 10) direction = true;
        
        if (direction) px = px - speed;
        else px = px + speed;
        
        if (vertMode == 2)
            if (py > 10) py = py - (float)speed/2;
            else vertMode = 1;
        if (vertMode == 3)
            if((py + height + 10 ) < ft.getHeight()) py = py + (float)speed/2;
            else vertMode = 1;

        idioMovement();  // motion peculiar to each class
    }
    protected abstract void idioMovement();
    public abstract LifeTime getLifeTime(int saline, int temperature);
}

abstract class StaticItem extends TankItem {  // tank items which are stationary
    protected boolean selected = false;
    
    protected StaticItem(FishTank fftt){
        super(fftt);
    }
    public void show(Graphics2D g2){
        int x = Math.round(px);
        int y = Math.round(py);
        if (selected){
            g2.setPaint(Color.WHITE);
            g2.fill(new Rectangle2D.Float(x, y, width, height));
        }
        try {
            g2.drawImage(img,
                x, y, width + x, height + y,
                0, 0, width, height,null);
        } catch (Exception e) {}
    }
    
    /*** these four methods are used for graphical relocation of the object in the tank ***/
    public void setX(int newX){
        px = ((float)newX * (float)((float)(ft.getWidth() - (float)width) / (float)100));
    }
    public int getPos(){
        return Math.round((px / (float)((float)ft.getWidth()- (float)width) * (float)100));
    }
    public void setSelected(){selected = true;}
    public void setUnselected(){selected = false;}
}

class Ambulia extends StaticItem {  // a plant
    private static BufferedImage localImage;
    static {localImage =  stringToImg("Ambulia.png");}
    
    public Ambulia(FishTank fftt){
        super(fftt);
        img = localImage;
        if (img != null) {
            width = img.getWidth();
            height = img.getHeight();
            Dimension size = fftt.getSize();
            py = size.height - height;
            Random rand = new Random();
            px = rand.nextInt(size.width - width - 20) + 10;
        }
    }
}

class Cabomba extends StaticItem {  // a plant
    private static BufferedImage localImage;
    static {localImage =  stringToImg("Cabomba.png");}
    
    public Cabomba(FishTank fftt){
        super(fftt);
        img = localImage;
        if (img != null) {
            width = img.getWidth();
            height = img.getHeight();
            Dimension size = fftt.getSize();
            py = size.height - height;
            Random rand = new Random();
            px = rand.nextInt(size.width - width - 20) + 10;
        }
    }
}

class ClownFish extends DynamicItem {  // a fish
    private static BufferedImage localImage;
    static {localImage =  stringToImg("ClownFish.png");}
    
    public ClownFish(FishTank fftt){
        super(fftt);
        img = localImage;
        speed = 2;
        if (img != null) {
            width = img.getWidth();
            height = img.getHeight();
            Dimension size = ft.getSize();
            Random rand = new Random();
            px = rand.nextInt(size.width - width - 20) + 10;
            py = rand.nextInt(size.height - height - 20) + 10;
        }
    }
    protected void idioMovement(){
        Random rand = new Random();
        int r = rand.nextInt(500);
        if (r == 250) direction = ! direction;
        
        r = rand.nextInt(1000);
        if (vertMode == 1){
            if (r == 999) vertMode = 2;
            else if (r == 998) vertMode = 3;
        } else if (r < 5) vertMode = 1;

    }
    @Override
    public LifeTime getLifeTime(int saline, int temperature){
        double averageLife = 1500;
        double deviation = 300;
        int maxTemp = 80;
        int minTemp = 75;
        int maxSal = 32;
        int minSal = 27;
        int thisLife;
        int maxLife;
        Random rand = new Random();
        thisLife = Math.round(Math.round(rand.nextGaussian() * deviation + averageLife));
        maxLife = thisLife;
        
        if (temperature > maxTemp){
            int dif = temperature - maxTemp;
            int deduct = dif * dif * 40;
            if (deduct < maxLife) maxLife = maxLife - deduct;
            else maxLife = 1;
        }
        else if (temperature < minTemp){
            int dif = minTemp - temperature;
            int deduct = dif * dif * 40;
            if (deduct < maxLife) maxLife = maxLife - deduct;
            else maxLife = 1;
        }
        
        if (saline > maxSal){
            int dif = saline - maxSal;
            int deduct = dif * dif * 40;
            if (deduct < maxLife) maxLife = maxLife - deduct;
            else maxLife = 1;
        }
        else if (saline < minSal){
            int dif = minSal - saline;
            int deduct = dif * dif * 40;
            if (deduct < maxLife) maxLife = maxLife - deduct;
            else maxLife = 1;
        }
        
        return new LifeTime(maxLife, "ClownFish");
    }
    /*********** Factory Strategy Technique **********    
    static {
        Factory.addStrategy("ClownFish", new Fish.Factory.Strategy<ClownFish>(){
            public ClownFish createFish(FishTank fftt, int x, int y) {
                return new ClownFish(fftt, x, y);
            }
        });
    }
    **********************/
}

class GoldenBarb extends DynamicItem {  // a fish
    private static BufferedImage localImage;
    static {localImage =  stringToImg("GoldenBarb.png");}
    
    public GoldenBarb(FishTank fftt){
        super(fftt);
        img = localImage;
        speed = 1;
        if (img != null) {
            width = img.getWidth();
            height = img.getHeight();
            Dimension size = ft.getSize();
            Random rand = new Random();
            px = rand.nextInt(size.width - width - 20) + 10;
            py = rand.nextInt(size.height - height - 20) + 10;
        }
    }
    protected void idioMovement(){
        Random rand = new Random();
        int r = rand.nextInt(1000);
        if (r == 250) direction = ! direction;
        
        r = rand.nextInt(2000);
        if (vertMode == 1){
            if (r == 999) vertMode = 2;
            else if (r == 998) vertMode = 3;
        } else if (r < 5) vertMode = 1;
    }
    @Override
    public LifeTime getLifeTime(int saline, int temperature){
        double averageLife = 1825;
        double deviation = 350;
        int maxTemp = 75;
        int minTemp = 64;
        int maxSal = 4;
        int minSal = 0;
        int thisLife;
        int maxLife;
        Random rand = new Random();
        thisLife = Math.round(Math.round(rand.nextGaussian() * deviation + averageLife));
        maxLife = thisLife;
        
        if (temperature > maxTemp){
            int dif = temperature - maxTemp;
            int deduct = dif * dif * 40;
            if (deduct < maxLife) maxLife = maxLife - deduct;
            else maxLife = 1;
        }
        else if (temperature < minTemp){
            int dif = minTemp - temperature;
            int deduct = dif * dif * 40;
            if (deduct < maxLife) maxLife = maxLife - deduct;
            else maxLife = 1;
        }
        
        if (saline > maxSal){
            int dif = saline - maxSal;
            int deduct = dif * dif * 40;
            if (deduct < maxLife) maxLife = maxLife - deduct;
            else maxLife = 1;
        }
        else if (saline < minSal){
            int dif = minSal - saline;
            int deduct = dif * dif * 40;
            if (deduct < maxLife) maxLife = maxLife - deduct;
            else maxLife = 1;
        }
        
        return new LifeTime(maxLife, "GoldenBarb");
    }
}

class SliderPair extends JPanel implements ChangeListener, ActionListener{  //user input of water temperature
    JLabel label;
    JSlider slider;
    JComboBox<String> unitChooser;
    private static final int FMAX = 104, FMIN = 32, FVAL = 68, CMAX = 40, CMIN = 0, CVAL = 20;
    String[] tempUnits = {"Fahrenheit", "Celsius"};
    
    SliderPair(){
        setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Water Temperature"),
                        BorderFactory.createEmptyBorder(0,0,0,0)));
        label = new JLabel(FVAL + " degrees");
        slider = new JSlider(FMIN, FMAX, FVAL);
        slider.addChangeListener(this);
        
        unitChooser = new JComboBox<String>(tempUnits);
        unitChooser.setSelectedIndex(0);
        unitChooser.addActionListener(this);
        
        JPanel top = new JPanel();
        top.setLayout(new FlowLayout());
        top.add(label);
        top.add(unitChooser);
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(top);
        add(slider);
    }
    public void stateChanged(ChangeEvent e) {
        JSlider js = (JSlider)e.getSource();
        label.setText(js.getValue() + " degrees");
    }
    public void actionPerformed(ActionEvent e) {
        //Combo box event. Set new maximums for the sliders.
        int i = unitChooser.getSelectedIndex();
        if(i == 0){
            slider.setMaximum(FMAX);
            slider.setMinimum(FMIN);
            slider.setValue(FVAL);
        } else {
            slider.setMaximum(CMAX);
            slider.setMinimum(CMIN);
            slider.setValue(CVAL);
        }
        label.setText(slider.getValue() + " degrees");
    }
    public int getTemperature(){
        if ("Fahrenheit".equals(unitChooser.getSelectedItem()))
            return slider.getValue();
        else return Math.round((float)slider.getValue() * (float)1.8 + (float)32);
            }
}

class SalinitySlide extends JPanel implements ChangeListener{  //user input of salinity
    JLabel label;
    JSlider slider;
    private static final int SMAX = 50, SMIN = 0, SVAL = 25;
    
    SalinitySlide(){
        setBorder(BorderFactory.createTitledBorder("Salinity"));
        label = new JLabel(SVAL + " ppt");
        slider = new JSlider(SMIN, SMAX, SVAL);
        slider.addChangeListener(this);
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(label);
        add(slider);
    }
    public void stateChanged(ChangeEvent e) {
        JSlider js = (JSlider)e.getSource();
        label.setText(js.getValue() + " ppt");
    }
    public int getSalinity(){return slider.getValue();}
}

abstract class ChooserPanel extends JPanel implements ActionListener{  //model for the add plant and fish panels
    JComboBox<String> fishChooser;
    private JLabel picture;
    protected FishTank ftref;
    private JSpinner spinner;
    
    ChooserPanel(FishTank ft, String[] options, String tWord, String bWord){
        ftref = ft;
        /********* initialize combo box **********/
        fishChooser = new JComboBox<String>(options);
        fishChooser.setSelectedIndex(0);
        Dimension d = new Dimension(120, 25);
        fishChooser.setMaximumSize(d);
        fishChooser.addActionListener(this);
        
        /********* initialize icon **********/
        picture = new JLabel();
        picture.setIcon(createImageIcon((String)fishChooser.getSelectedItem() + ".png"));
        
        /********* initialize add button **********/
        JButton addF = new JButton(bWord);
        addF.setActionCommand("add");
        addF.addActionListener(this);
        
        /********* initialize spinner **********/
        SpinnerModel quantityModel = new SpinnerNumberModel(1, 1, 10, 1);
        spinner = new JSpinner(quantityModel);
        spinner.setMaximumSize(d);
        
        /********* initialize layout **********/
        setBorder(BorderFactory.createTitledBorder(tWord));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        addElement(addF);
        addElement(spinner);
        addElement(picture);
        fishChooser.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(fishChooser);
        
    }
    public void actionPerformed(ActionEvent e) {
        if ("add".equals(e.getActionCommand())){
            String s = (String)fishChooser.getSelectedItem();
            int qt = (int)spinner.getValue();
            makeCall(s,qt);
        }
        else {
            String s = (String)fishChooser.getSelectedItem();
            ImageIcon icon = createImageIcon(s + ".png");
            picture.setIcon(icon);
        }
    }
    abstract void makeCall(String s, int qt);
    protected static ImageIcon createImageIcon(String path) {
        int target = 45;
        URL imgURL = FishTankPanel.class.getResource(path);
        if (imgURL != null) {
            try {
                Image img =  ImageIO.read(imgURL);
                int height = img.getHeight(null);
                int width = img.getWidth(null);
                width = (int)((float)width / (float)height * target);
                Image scaledImage = img.getScaledInstance(width, target, java.awt.Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            } catch (Exception e) {return null;}
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
    private void addElement(JComponent jc){
        jc.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(jc);
        add(Box.createRigidArea(new Dimension(0,5)));
    }
}

class AddPlantPanel extends ChooserPanel{
    private static String[] choices = {"Cabomba", "Ambulia"};
    private static String borderTitle = "Add Accessory";
    private static String buttonLabel = "Place Item";
    
    AddPlantPanel(FishTank ft){
        super(ft, choices, borderTitle, buttonLabel);
    }
    void makeCall(String s, int qt){
        ftref.addPlant(s,qt);
    }
}

class AddFishPanel extends ChooserPanel{
    private static String[] choices = {"ClownFish", "GoldenBarb"};
    private static String borderTitle = "Add Stock";
    private static String buttonLabel = "Add Fish";
    
    AddFishPanel(FishTank ft){
        super(ft, choices, borderTitle, buttonLabel);
    }
    void makeCall(String s, int qt){
        ftref.addFish(s,qt);
    }
}

class StockTable extends JPanel{  //displays contents of the tank
    private static JTable table;
    private SimpleTableModel tm = new SimpleTableModel();
    
    StockTable(){
        /********* initialize layout **********/
        setBorder(BorderFactory.createTitledBorder("Tank Contents"));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        /********* initialize table **********/
        table = new JTable(tm);
        table.getColumnModel().getColumn(0).setPreferredWidth(30);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.setRowSelectionAllowed(false);
        
        table.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(table);
        add(Box.createRigidArea(new Dimension(0,5)));
    }
    public void clearTable(){tm.clearTable();}
    public void addRow(String[] inRow){tm.addRow(inRow);}
    public void fireTableDataChanged(){
        tm.fireTableDataChanged();
    }
    class SimpleTableModel extends AbstractTableModel {
        private java.util.List<String> columnNames = new Vector<String>();
        private java.util.List<String[]> data = new Vector<String[]>();
        SimpleTableModel(){
            columnNames.add("qty");
            columnNames.add("species");
        }
        public String getValueAt(int row, int col) {
            return data.get(row)[col];
        }
        public void removeRow(int row) {
            data.remove(row);
        }
        public boolean isCellEditable(int row, int column){
            return false;
        }
        public int getColumnCount() {
            return columnNames.size();
        }
        public int getRowCount() {
            return data.size();
        }
        public void addRow(String[] inRow){
            data.add(inRow);
        }
        public void clearTable(){
            data.clear();
        }
    }
}

class AdjustmentSlide extends JPanel implements ChangeListener, ActionListener{  //for relocation of static items
    private static final int SMAX = 100, SMIN = 0, SVAL = 50;
    private FishTank ft;
    private JSlider slider;
    
    AdjustmentSlide(FishTank fftt){
        ft = fftt;
        setBorder(BorderFactory.createTitledBorder("Relocate Contents"));
        slider = new JSlider(SMIN, SMAX, SVAL);
        Dimension d = new Dimension(120, 20);
        slider.setMaximumSize(d);
        slider.addChangeListener(this);
        
        JButton selectB = new JButton("Select Item");
        JButton unSelectB = new JButton("Unselect");
        selectB.setActionCommand("select");
        unSelectB.setActionCommand("unselect");
        selectB.addActionListener(this);
        unSelectB.addActionListener(this);
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        addElement(selectB);
        addElement(unSelectB);
        addElement(slider);
    }
    public void stateChanged(ChangeEvent e) {
        JSlider js = (JSlider)e.getSource();
        ft.moveSelected(js.getValue());
    }
    public void actionPerformed(ActionEvent e) {
        if ("select".equals(e.getActionCommand())){
            int x = ft.select();
            if (x != 200) slider.setValue(x);
        } else
        if ("unselect".equals(e.getActionCommand()))
            ft.unSelect();
    }
    private void addElement(JComponent jc){
        jc.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(jc);
        add(Box.createRigidArea(new Dimension(0,5)));
    }
}

class LifeTime implements Comparable<LifeTime>{  // class which represents life of a fish for analysis results
    int duration;
    String species;
    public LifeTime(int d, String s){
        duration = d;
        species = s;
    }
    @Override
    public int compareTo(LifeTime lt){
        return duration < lt.duration ? -1 : duration < lt.duration ? 1 : 0;
    }
}