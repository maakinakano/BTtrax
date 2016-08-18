package jp.ac.titech.itpro.maaki.bttrax;


public class TraxPanelUtil {

    static final TraxPanel allPanel[] = {
            TraxPanel.nonePanel,
            new CrossPanel(Colors.RED, Colors.WHITE, R.drawable.cross1),
            new CrossPanel(Colors.WHITE, Colors.RED, R.drawable.cross2),
            new LUCurvePanel(Colors.RED, Colors.WHITE, R.drawable.lucur1),
            new LUCurvePanel(Colors.WHITE, Colors.RED, R.drawable.lucur2),
            new RUCurvePanel(Colors.RED, Colors.WHITE, R.drawable.rucur1),
            new RUCurvePanel(Colors.WHITE, Colors.RED, R.drawable.rucur2)
    };

    public static TraxPanel of(int id) {
        if(id < allPanel.length)
            return allPanel[id];
        return TraxPanel.nonePanel;
    }
}
