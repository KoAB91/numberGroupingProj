import java.io.*;
import java.util.*;


public class Main {
    public static void main(String[] args) {

        //long startTime = System.currentTimeMillis();

        Set<String> lines = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Ошибка поиска файла: \n" + e.getMessage());
        } catch (IOException e) {
            System.out.println("Ошибка чтения файла: \n" + e.getMessage());
        }

        //Хранит значения по принципу <Элемент строки-Позиция в строке>, <Id группы>>
        HashMap<String, Integer> keys = new HashMap<>();
        //Хранит значения по принципу <Id группы, Группа>>
        HashMap<Integer, StringBuilder> groups = new HashMap<>();
        int counter = 0;
        int groupId;

        Iterator<String> iterator = lines.iterator();
        outer:
        while (iterator.hasNext()) {
            String line = iterator.next();
            String[] elems = line.split(";");
            if (elems.length == 0) {
                continue;
            }
            for(String elem : elems){
                if(elem == null || elem.isEmpty() || elem.equals("\"\"")) continue;
                if (!isNumeric(elem)) {
                    iterator.remove();
                    continue outer;
                }
            }

            // Проверяем, соответствует ли строка каким-либо группам
            Set<Integer> intersections = new HashSet<>();
            for(int i = 0; i < elems.length; i++){
                String elem = elems[i];
                if (!elem.equals("\"\"") & !elem.isEmpty()) {
                    elem += "-" + i;
                    Integer id = keys.get(elem);
                    if (id != null) {
                        intersections.add(id);
                    }
                }
            }

            StringBuilder group;
            //Если группа не найдена или найдено более одной - создаем новую
            if(intersections.size() != 1){
                counter++;
                groupId = counter;
                group = new StringBuilder();
                group.append(String.join(";", elems));
                //Если групп больше чем одна - объединяем с новой
                if(intersections.size() > 1){
                    for(Integer id : intersections){
                        StringBuilder groupToJoin = groups.get(id);
                        group.append("\n").append(groupToJoin);
                        String[] groupLines = groupToJoin.toString().split("\n");
                        for (String groupLine : groupLines){
                            String[] lineElements = groupLine.split(";");
                            for(int i = 0; i < lineElements.length; i++){
                                String lineElem = lineElements[i];
                                if (!lineElem.equals("\"\"") & !lineElem.isEmpty()) {
                                    keys.put(lineElem + '-' + i, groupId);
                                }
                            }
                        }
                        groups.remove(id);
                    }
                }
                groups.put(groupId, group);
            // Если найдена одна - добавляем в нее строку
            } else {
                groupId = intersections.iterator().next();
                group = groups.get(groupId);
                group.append("\n").append(String.join(";", elems));
            }
            addNewKeys(elems, keys, groupId);

            iterator.remove();
        }

        keys.clear();

        List<StringBuilder> groupList = new ArrayList<>(groups.values());
        groups.clear();
        groupList.sort(compareByLines);


        StringBuilder listToOutput = new StringBuilder();
        counter = 0;
        for(StringBuilder group : groupList){
            if(group.chars().filter(c -> c == '\n').count() > 0) {
                counter++;
            }
        }
        listToOutput.append("Количество групп более чем с одним элементом - ").append(counter).append("\n");

        counter = 0;
        for(StringBuilder group : groupList){
            counter++;
            listToOutput.append("Группа ").append(counter).append("\n").append(group).append("\n");
        }

        //long endTime = System.currentTimeMillis();
        //long timeElapsed = endTime - startTime;
        //listToOutput.append("Время выполнения - ").append(timeElapsed/1000).append(" сек.");

        try {
            FileWriter writer = new FileWriter("grouped_lines.txt");
            writer.write(listToOutput.toString());
            writer.close();
        } catch (IOException e) {
            System.out.println("Ошибка при записи в файл");
            e.printStackTrace();
        }

    }

    public static void addNewKeys(String[] elements,
                                  HashMap<String, Integer> keys,
                                  int groupId){
        for(int i = 0; i < elements.length; i++){
            String element = elements[i];
            if (!element.equals("\"\"") & !element.isEmpty()) {
                element += "-" + i;
                keys.putIfAbsent(element, groupId);
            }
        }
    }

    public static boolean isNumeric(String str) {
        return str.matches("\"?-?\\d+[\\.\\,]?\\d+\"?");
    }

    static Comparator<StringBuilder> compareByLines = new Comparator<>() {
        @Override
        public int compare(StringBuilder o1, StringBuilder o2) {
            return (int) (o2.chars().filter(c -> c == '\n').count() - o1.chars().filter(c -> c == '\n').count());
        }
    };
}
