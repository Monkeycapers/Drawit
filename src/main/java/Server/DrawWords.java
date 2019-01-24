package Server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//Load and provide words for DrawLobbys
public class DrawWords {

    private static List<String> wordsList;

    public static void loadFromFile(File file) throws IOException {
        String in = "";
        wordsList = getNewList();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        while ((in = bufferedReader.readLine()) != null) {
            //total.append(in);
            wordsList.add(in);
        }
        bufferedReader.close();
        System.out.println("Loaded " + wordsList.size() + " words.");
    }

    private static List<String> getNewList() {
        return new ArrayList<>();
    }

    public static List<String> getWords() {
        return new ArrayList<>(wordsList);
    }

    private static List<String> getRandomWords(int amount) {
        throw new UnsupportedOperationException();
    }

}
