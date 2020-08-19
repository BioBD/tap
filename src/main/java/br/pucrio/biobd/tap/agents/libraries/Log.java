/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.libraries;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Log {

    protected static String debug;
    private static ResourceBundle bundle;
    private static Locale locale;
    private static String lastDebug = "";
    private static int difTime = 0;
    protected static Gson gson;
    protected static String nameFile;

    public static String getNameFileLog(String complement) {
        if (nameFile == null) {
            nameFile = getDateTime("dd-MM-yyyy-'at'-hh-mm-ss-a");
        }
        return complement + "_" + nameFile;

    }

    public static String readMvQuery(String query) {
        return readFile("config/mv_query/" + query + "_mv_query.sql");
    }

    public static String readFile(String path) {
        String everything = null;
        try {
            FileReader fileToRead = new FileReader(path);
            BufferedReader br = new BufferedReader(fileToRead);
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            everything = sb.toString();
            br.close();
            fileToRead.close();
        } catch (IOException ex) {
            Log.error(ex);
        }
        return everything;
    }

    public final void createBundle() {
        if (useBundle()) {
            try {
                if (bundle == null) {
                    locale = new Locale(Config.getProperty("language"), Config.getProperty("country"));
                    bundle = ResourceBundle.getBundle("language/messages", locale);
                }
            } catch (Error err) {
                this.error(err);
            }
        }
    }

    private boolean useBundle() {
        return Config.containsKey("language")
                && Config.containsKey("country");
    }

    public Log() {
        readDebug();
        createBundle();
        gson = new Gson();
        try {
            if ((bundle == null) && (useBundle())) {
                locale = new Locale(Config.getProperty("language"), Config.getProperty("country"));
                bundle = ResourceBundle.getBundle("base/messages", locale);
            }
        } catch (Error err) {
            error(err);
        }
    }

    protected static final void readDebug() {
        if (Log.debug == null) {
            Log.debug = String.valueOf(Config.getProperty("debug"));
        }
    }

    protected static void print(Object msg) {
        String textToPrint = getDateTime("hh:mm:ss") + getDifTime(getDateTime("hh:mm:ss")) + " = " + msg;
        if (isPrint(0)) {
            System.out.println(textToPrint);
        }
        if (isPrint(1)) {
            writeFile("log", textToPrint);
        }
    }

    protected static boolean isPrint(int pos) {
        if (debug == null) {
            readDebug();
        }
        return Log.debug.substring(pos, pos + 1).equals("1");
    }

    public static String removeNl(String frase) {
        String padrao = "\\s{2,}";
        Pattern regPat = Pattern.compile(padrao);
        Matcher matcher = regPat.matcher(frase);
        String res = matcher.replaceAll(" ").trim();
        return res.replaceAll("(\n|\r)+", " ").replaceAll(" +", " ").trim();
    }

    public static final String getDateTime(String format) {
        SimpleDateFormat ft = new SimpleDateFormat(format);
        Date today = new Date();
        return ft.format(today);
    }

    public static void title(String msg) {
        if (hasBundle(msg)) {
            msg = bundle.getString(msg);
        }
        if (isPrint(1)) {
            int size = 80 - msg.length();
            StringBuilder buf = new StringBuilder();
            buf.append("==");
            for (int i = 0; i < size / 2; ++i) {
                buf.append("=");
            }
            print(buf.toString() + " " + msg + " " + buf.toString());
        }
    }

    public static void endTitle() {
        title("fim");
    }

    public static void msg(Object msg) {
        if (hasBundle(msg.toString())) {
            print(bundle.getString(msg.toString()));
        } else {
            print(msg);
        }
    }

    public void msg(String msg, String bundleMsg) {
        this.print(bundle.getString(bundleMsg) + msg);
    }

    public static void error(Object error) {
        if (hasBundle(error.toString())) {
            errorPrint(bundle.getString(error.toString()));
        } else {
            errorPrint(error);
        }
    }

    private static void errorPrint(Object e) {
        print(e);
        throw new UnsupportedOperationException(e.toString());
    }

    public static String getDifTime(String now) {
        if (!now.isEmpty() && !lastDebug.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
                Date nowDate = sdf.parse(now);
                Date lastDate = sdf.parse(lastDebug);
                long diff = nowDate.getTime() - lastDate.getTime();
                difTime = (int) (diff / 1000);
            } catch (ParseException ex) {
                Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        setLastDebug(now);
        String result = "= + " + difTime + "s";
        if (difTime < 9) {
            result += "  ";
        } else {
            result += " ";
        }
        return result;
    }

    public static void setLastDebug(String last) {
        lastDebug = last;
    }

    public static String getLastDebug() {
        return lastDebug;
    }

    public static void setDifTime(int difTime) {
        Log.difTime = difTime;
    }

    public static void writeFile(String nameFile, String content) {
        try {
            OutputStreamWriter writer;
            boolean append = false;
            switch (nameFile) {
                case "pid":
                    nameFile = nameFile + ".txt";
                    break;
                case "reportexcel":
                    nameFile = Config.getProperty("folderLog") + File.separatorChar + getNameFileLog(nameFile) + ".csv";
                    break;
                case "reportexcelappend":
                    nameFile = Config.getProperty("folderLog") + File.separatorChar + getNameFileLog(nameFile) + ".csv";
                    append = true;
                    break;
                case "log":
                    nameFile = Config.getProperty("folderLog") + File.separatorChar + getNameFileLog(nameFile) + ".txt";
                    append = true;
                    break;
                default:
                    nameFile = Config.getProperty("folderLog") + File.separatorChar + nameFile + ".txt";
                    append = true;
            }
            File file = new File(Config.getProperty("folderLog"));
            if (!file.exists()) {
                file.mkdir();
            }
            writer = new OutputStreamWriter(new FileOutputStream(nameFile, append), "UTF-8");

            BufferedWriter fbw = new BufferedWriter(writer);
            fbw.write(content);
            fbw.newLine();
            fbw.close();
        } catch (IOException ex) {
            errorPrint(ex);
        }
    }

    private static boolean hasBundle(String msg) {
        if (bundle != null) {
            Enumeration<String> keys = bundle.getKeys();
            while (keys.hasMoreElements()) {
                if (keys.nextElement().equals(msg)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void writePID(long pid) {
        msg("PID: " + pid);
        writeFile("pid", String.valueOf(pid));
    }

    public void archiveLogFiles() {
        File folder = new File(System.getProperty("user.dir") + File.separatorChar + Config.getProperty("folderLog") + File.separatorChar);
        System.out.println(System.getProperty("user.dir") + File.separatorChar + Config.getProperty("folderLog") + File.separatorChar);
        if (!folder.exists()) {
            folder.mkdir();
        }
        File afile[] = folder.listFiles();
        int i = 0;
        for (int j = afile.length; i < j; i++) {
            File arquivos = afile[i];
            String nameFile = arquivos.getName();
            if (nameFile.contains(".txt") || nameFile.contains(".csv") || (nameFile.equals("blackboard.properties") && Config.containsKey("blackboard") && Config.getProperty("blackboard").equals("1"))) {
                String nameFolder = "talk/";
                if (nameFile.contains("_")) {
                    nameFolder += nameFile.substring(nameFile.indexOf("_") + 1, nameFile.indexOf("."));
                } else {
                    nameFolder += nameFile.substring(0, nameFile.indexOf("."));
                }
                new File(nameFolder).mkdir();
                File diretorio = new File(nameFolder);
                File destiny = new File(diretorio, arquivos.getName());
                if (destiny.exists()) {
                    destiny.delete();
                }
                arquivos.renameTo(new File(diretorio, arquivos.getName()));
            }
        }
    }

    public static String getJson(Object obj) {
        return gson.toJson(obj);
    }

    public static void clearFolder(String path) {
        File folderGna = new File(path);
        File[] contents = folderGna.listFiles();
        if (contents != null) {
            for (File f : contents) {
                f.delete();
            }
        }

    }

}
