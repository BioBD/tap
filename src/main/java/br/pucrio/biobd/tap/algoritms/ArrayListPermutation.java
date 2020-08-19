/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.algoritms;

import br.pucrio.biobd.tap.agents.libraries.Config;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Rafael
 */
public class ArrayListPermutation {

    public static ArrayList<ArrayList<Object>> generateSimplePermutation(ArrayList<Object> itemList) {

        ArrayList<Object> ini = new ArrayList<>();
        ArrayList<ArrayList<Object>> result = new ArrayList<>();
        simplePermutation(ini, itemList, result);
        return result;
    }

    public static ArrayList<ArrayList<Object>> generateCompletePermutation(ArrayList<Object> itemList) {

        ArrayList<Object> ini = new ArrayList<>();
        ArrayList<ArrayList<Object>> result = new ArrayList<>();
        completePermutation(ini, itemList, result);
        return result;
    }

    private static void simplePermutation(ArrayList<Object> sub, ArrayList<Object> a, ArrayList<ArrayList<Object>> result) {
        result.add(new ArrayList<>(sub));
        int L = a.size();
        if (L != 0) {
            for (int i = 0; i < L; i++) {
                sub.add(a.get(i));
                a.remove(i);
                simplePermutation(sub, a, result);
                L = a.size();
            }
        }
    }

    public static ArrayList<ArrayList<Object>> rafaelsPermutationLimited(ArrayList<Object> all) {
        int sizeList = Integer.valueOf(Config.getProperty("sizeMaxIndex"));
        ArrayList<ArrayList<Object>> listRestrictions = ArrayListPermutation.rafaelsPermutation(all, sizeList);
        if (Config.getProperty("turbo").equals("0")) {
            while (listRestrictions.size() > Integer.valueOf(Config.getProperty("numberMaxIndex"))) {
                listRestrictions = ArrayListPermutation.rafaelsPermutation(all, --sizeList);
            }
        }
        return listRestrictions;
    }

    private static ArrayList<ArrayList<Object>> rafaelsPermutation(ArrayList<Object> all, int maxSizeList) {
        ArrayList<ArrayList<Object>> result = new ArrayList<>();
        ArrayList<Object> round;
        for (int i = 0; i < all.size(); i++) {
            round = new ArrayList<>();
            round.add(all.get(i));
            result.add(round);
            for (int j = i + 1; j < all.size(); j++) {
                round = new ArrayList<>();
                round.add(all.get(i));
                round.add(all.get(j));
                result.add(round);
                if (maxSizeList >= 2) {
                    for (int k = j + 1; k < all.size(); k++) {
                        round = new ArrayList<>();
                        round.add(all.get(i));
                        round.add(all.get(j));
                        round.add(all.get(k));
                        result.add(round);
                        if (maxSizeList >= 3) {
                            for (int l = k + 1; l < all.size(); l++) {
                                round = new ArrayList<>();
                                round.add(all.get(i));
                                round.add(all.get(j));
                                round.add(all.get(k));
                                round.add(all.get(l));
                                result.add(round);
                                if (maxSizeList >= 4) {
                                    for (int m = l + 1; m < all.size(); m++) {
                                        round = new ArrayList<>();
                                        round.add(all.get(i));
                                        round.add(all.get(j));
                                        round.add(all.get(k));
                                        round.add(all.get(l));
                                        round.add(all.get(m));
                                        result.add(round);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private static void completePermutation(ArrayList<Object> sub, ArrayList<Object> a, ArrayList<ArrayList<Object>> result) {
        boolean has = false;
        for (ArrayList<Object> itemList : result) {
            if (haveSameElements(itemList, sub)) {
                has = true;
                break;
            }
        }
        if (!has && !sub.isEmpty()) {
            result.add(new ArrayList<>(sub));
        }
        int L = a.size();
        if (L != 0) {
            for (int i = 0; i < L; i++) {
                ArrayList<Object> ab = new ArrayList<>(sub);
                ab.add(a.get(i));
                ArrayList<Object> bc = new ArrayList<>(a);
                bc.remove(i);
                completePermutation(ab, bc, result);
            }
        }
    }

    private static class Count {

        public int count = 0;
    }

    public static boolean haveSameElements(ArrayList<Object> list1, ArrayList<Object> list2) {
        // (list1, list1) is always true
        if (list1 == list2) {
            return true;
        }

        // If either list is null, or the lengths are not equal, they can't possibly match
        if (list1 == null || list2 == null || list1.size() != list2.size()) {
            return false;
        }

        // (switch the two checks above if (null, null) should return false)
        HashMap<String, Count> counts = new HashMap<String, Count>();

        // Count the items in list1
        for (Object item : list1) {
            if (!counts.containsKey(String.valueOf(item.hashCode()))) {
                counts.put(String.valueOf(item.hashCode()), new Count());
            }
            counts.get(String.valueOf(item.hashCode())).count += 1;
        }

        // Subtract the count of items in list2
        for (Object item : list2) {
            // If the map doesn't contain the item here, then this item wasn't in list1
            if (!counts.containsKey(String.valueOf(item.hashCode()))) {
                return false;
            }
            counts.get(String.valueOf(item.hashCode())).count -= 1;
        }

        // If any count is nonzero at this point, then the two lists don't match
        for (Count key : counts.values()) {
            if (key.count != 0) {
                return false;
            }
        }
        return true;
    }

}
