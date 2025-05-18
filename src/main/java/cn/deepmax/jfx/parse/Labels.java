package cn.deepmax.jfx.parse;

public class Labels {

    private static int id = 0;

    Labels parent;

    String labelValue;

    boolean noLabel() {
        return labelValue == null;
    }

    public Labels makeTempLabel() {
        String l = "Loop." + id++;
        Labels labels = new Labels();
        labels.labelValue = l;
        labels.parent = this;
        return labels;
    }
}
