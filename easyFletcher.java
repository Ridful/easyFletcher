import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.ItemWidget;
import com.epicbot.api.shared.methods.IBankAPI;
import com.epicbot.api.shared.methods.IInventoryAPI;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.paint.frame.PaintFrame;
import com.epicbot.api.shared.util.time.Time;
import com.sun.webkit.Timer;

import java.awt.*;
import java.util.Date;

/* quick and janky script for now but does the job */

@ScriptManifest(name = "Simple Fletching Script", gameType = GameType.OS)
public class easyFletcher extends LoopScript {

    /*** BEGIN VARIABLES ***/
    
    public String status = "On";
    public IInventoryAPI myInventory() { return getAPIContext().inventory(); }
    public IBankAPI myBank() { return getAPIContext().bank(); }

    public double totalExperienceGained = 0;
    public long startTime = System.currentTimeMillis();
    public long currentTime;
    public double totalTimeElapsed = startTime - currentTime;
    public int totalRunningProfit = 0;

    /*** END VARIABLES ***/

    public boolean myInventoryFull() {
        return myInventory().isFull();
    }

    public void sendContinue() {
        System.out.println("sendtext <num> false");
        getAPIContext().keyboard().sendText("3", false);
    }

    private enum Fletch {
        MAPLE_LONGBOW("Maple longbow", 55, 58.5F, new String[]{"Maple longbow (u)", "Bow string"});

        private final int requiredLevel;
        private final float experienceDrop;
        private final String[] itemsRequired;
        private final String outputName;

        Fletch(String outputName, int requiredLevel, float experienceDrop, String[] itemsRequired) {
            this.outputName = outputName;
            this.requiredLevel = requiredLevel;
            this.experienceDrop = experienceDrop;
            this.itemsRequired = itemsRequired;
        }

        public int getRequiredLevel() { return requiredLevel; }

        public String getOutputName() { return outputName; }

        public float getExperienceDrop() { return experienceDrop; }

        public String[] getItemsRequired() { return itemsRequired; }

    }

    /*** UNDER ***/

    public void openBank() {
        if (!myBank().isOpen()) {
            myBank().open();
        }
    }

    public int itemAmtInInventory(String itemName) {
        return myInventory().getCount(itemName);
    }

    public void withdrawXItems(int amount, String itemName) {
        myBank().withdraw(amount, itemName);
    }

    public void depositBows() {
        myBank().depositAllExcept("Knife");
    }

    public void depositAll() {
        myBank().depositInventory();
    }


    public void closeBank() {
        if (myBank().isOpen()) {
            myBank().close();
        }
    }

    public void doMsg(String message) {
        System.out.println(message);
    }

    public boolean shouldBank() {
        return ((itemAmtInInventory("Maple longbow") == 27) || (itemAmtInInventory("Maple logs") == 0));
    }

    public boolean shouldFletch() {
        return (((itemAmtInInventory("Knife")) == 1) && ((itemAmtInInventory("Maple logs")) == 27));
    }

    public void doFletching() {

        doMsg("Clicking on Knife");
        ItemWidget myKnife = getAPIContext().inventory().getItem("Knife");

        doMsg("Clicking on Maple logs");
        ItemWidget myMaples = getAPIContext().inventory().getItem("Maple logs");

        if ((myKnife != null) && (myMaples != null)) {
            myKnife.interact();
            myMaples.interact();

            Time.sleep(800);

            doMsg("Sending Keyboard Option 3");
            getAPIContext().keyboard().sendText("3", false);
        }

    }

    public void addStats() {
        totalExperienceGained += 58.5;
        totalRunningProfit += 75;
        totalTimeElapsed = (System.currentTimeMillis() - startTime);
    }

    @Override
    public boolean onStart(String... strings) {
        System.out.println("Starting program");
        status = "Running";
        return true;
    }

    @Override
    protected int loop() {

        boolean prepped = false;

        if (shouldBank()) {

            openBank();

            if ((itemAmtInInventory("Knife")) == 1) {
                depositBows();
                withdrawXItems(27, "Maple logs");
                closeBank();
                prepped = true;
            } else {
                depositAll();
                withdrawXItems(1, "Knife");
                withdrawXItems(27, "Maple logs");
                closeBank();
                prepped = true;
            }
        }

        if ((prepped) && (shouldFletch())) {
            prepped = false;
            closeBank();

            doFletching();

            int fletchTime = 48600;
            int timeElapsed = 0;
            int oneFletchTime = 1800;

            while (timeElapsed < fletchTime) {
                timeElapsed++;
                if ((timeElapsed % 1800) == 0) {
                    Time.sleep(1800);
                    doMsg("timeElapsed: " + String.valueOf(timeElapsed) + "ms slept.");
                    addStats();
                }
            }
            //FIN
        }

        return 300;
    }

    @Override
    protected void onPaint(Graphics2D g, APIContext ctx) {
        PaintFrame frame = new PaintFrame("Fletcher Test Script");
        frame.addLine("Status", status);
        frame.addLine("Profit", String.valueOf(totalRunningProfit) + " gp");
        frame.addLine("Experience", String.valueOf(totalExperienceGained) + " xp");
        frame.addLine("Time Running", String.valueOf((totalTimeElapsed)/1000) + " sec");
        frame.draw(g, 0, 170, ctx);
    }
}