package utility;

public class ConsoleWriter {
    private int inputStatus = 0;

    public void setInputStatus(int inputStatus) {
        this.inputStatus = inputStatus;
    }

    public int getInputStatus() {
        return inputStatus;
    }

    /**
     * При условии, что {@link ConsoleWriter#inputStatus} != 1 печатает переданную строку с помощью System.out.println(String)
     *
     * @param str строка, которую необходимо напечатать
     */
    public void println(String str) {
        if (inputStatus != 1) System.out.println(str);
    }

    /**
     * При условии, что {@link ConsoleWriter#inputStatus} != 1 печатает переданную строку с помощью System.out.print(String)
     *
     * @param str строка, которую необходимо напечатать
     */
    public void print(String str) {
        if (inputStatus != 1) System.out.print(str);
    }

    /**
     * Печатает переданную строку с помощью System.out.println(String)
     *
     * @param str строка, которую необходимо напечатать
     */
    public void printIgnoringPrintStatus(String str) {
        System.out.println(str);
    }
}
