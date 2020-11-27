package ru.justtry.database.sort;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.springframework.stereotype.Component;

import ru.justtry.database.sort.SortInfo.Direction;
import ru.justtry.shared.NoteConstants;

@Component
public class Sort
{
    private final static Logger logger = LogManager.getLogger(Sort.class);
    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    class NaturalComparator<K, V extends Comparable<V>> implements Comparator<Map.Entry<K, V>> {

        @Override
        public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2)
        {
            if (o1.getValue() == null && o2.getValue() == null)
                return 0;
            if (o1.getValue() == null)
                return 1;
            if (o2.getValue() == null)
                return -1;
            return o1.getValue().compareTo(o2.getValue());
        }
    }


    class NaturalStringComparator<K, String> implements Comparator<Map.Entry<K, String>> {

        @Override
        public int compare(Map.Entry<K, String> o1, Map.Entry<K, String> o2)
        {
            if (o1.getValue() == null && o2.getValue() == null)
                return 0;
            if (o1.getValue() == null)
                return 1;
            if (o2.getValue() == null)
                return -1;
            return o1.getValue().toString().compareToIgnoreCase(o2.getValue().toString());
        }
    }


    class ReverseComparator<K, V extends Comparable<V>> implements Comparator<Map.Entry<K, V>> {

        @Override
        public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2)
        {
            if (o1.getValue() == null && o2.getValue() == null)
                return 0;
            if (o1.getValue() == null)
                return 1;
            if (o2.getValue() == null)
                return -1;
            return o2.getValue().compareTo(o1.getValue());
        }
    }


    class ReverseStringComparator<K, String> implements Comparator<Map.Entry<K, String>> {

        @Override
        public int compare(Map.Entry<K, String> o1, Map.Entry<K, String> o2)
        {
            if (o1.getValue() == null && o2.getValue() == null)
                return 0;
            if (o1.getValue() == null)
                return 1;
            if (o2.getValue() == null)
                return -1;
            return o2.getValue().toString().compareToIgnoreCase(o1.getValue().toString());
        }
    }


    public void run(List<Document> documents, SortInfo sortInfo)
    {
        if (sortInfo == null)
            return;

        String attrName = sortInfo.getAttribute().getName();
        switch (sortInfo.getAttribute().getTypeAsEnum())
        {
        case TEXT:
        case TEXT_AREA:
        case DELIMITED_TEXT:
        case SELECT:
            HashMap<Document, String> stringMap = new HashMap<>();
            for (Document d : documents)
            {
                Object noteAttr = getNoteAttr(d, attrName);
                String defaultValue = sortInfo.getAttribute().getDefaultValue();

                if (noteAttr == null)
                    stringMap.put(d, defaultValue);
                else
                    stringMap.put(d, noteAttr.toString());
            }
            documents.clear();

            stringMap.entrySet().stream()
                    .sorted(sortInfo.getDirection() == Direction.DESCENDING ? new ReverseStringComparator<>()
                            : new NaturalStringComparator<>())
                    .forEachOrdered(x -> documents.add(x.getKey()));
            break;
        case INC:
        case NUMBER:
            HashMap<Document, Double> doubleMap = new HashMap<>();
            for (Document d : documents)
            {
                Object noteAttr = getNoteAttr(d, attrName);
                Double defaultValue = null;
                if (sortInfo.getAttribute().getDefaultValue() != null)
                    defaultValue = Double.parseDouble(sortInfo.getAttribute().getDefaultValue());

                // In some reason expression like map,put(d, noteAttr == null ? defaultValue : Double.parse...)
                // throws NPE because execution comes to Double.parse even when noteAttr is null.
                if (noteAttr == null)
                    doubleMap.put(d, defaultValue);
                else
                    doubleMap.put(d, Double.parseDouble(noteAttr.toString()));
            }
            documents.clear();

            doubleMap.entrySet().stream()
                    .sorted(sortInfo.getDirection() == Direction.DESCENDING ?
                            new ReverseComparator<>() : new NaturalComparator<>())
                    .forEachOrdered(x -> documents.add(x.getKey()));
            break;
        case CHECKBOX:
            HashMap<Document, Boolean> booleanMap = new HashMap<>();
            for (Document d : documents)
            {
                Object noteAttr = getNoteAttr(d, attrName);
                String defaultValueStr = sortInfo.getAttribute().getDefaultValue();
                Boolean defaultValue = defaultValueStr == null ? null : Boolean.parseBoolean(defaultValueStr);

                if (noteAttr == null)
                    booleanMap.put(d, defaultValue);
                else
                    booleanMap.put(d, (Boolean)noteAttr);
            }
            documents.clear();

            booleanMap.entrySet().stream()
                    .sorted(sortInfo.getDirection() == Direction.DESCENDING ?
                            new ReverseComparator<>() : new NaturalComparator<>())
                    .forEachOrdered(x -> documents.add(x.getKey()));
            break;
        case USER_DATE:
            try
            {
                HashMap<Document, Date> dateMap = new HashMap<>();
                for (Document d : documents)
                {
                    Object noteAttr = getNoteAttr(d, attrName);
                    String defaultValueStr = sortInfo.getAttribute().getDefaultValue();
                    Date defaultValue = defaultValueStr == null ? null : DATE_FORMAT.parse(defaultValueStr);

                    if (noteAttr == null)
                        dateMap.put(d, defaultValue);
                    else
                        dateMap.put(d, DATE_FORMAT.parse(noteAttr.toString()));
                }
                documents.clear();

                dateMap.entrySet().stream()
                        .sorted(sortInfo.getDirection() == Direction.DESCENDING ?
                                new ReverseComparator<>() : new NaturalComparator<>())
                        .forEachOrdered(x -> documents.add(x.getKey()));
            }
            catch (Exception e)
            {
                logger.error(e);
            }
            break;
        case USER_TIME:
            try
            {
                HashMap<Document, LocalTime> dateMap = new HashMap<>();
                for (Document d : documents)
                {
                    Object noteAttr = getNoteAttr(d, attrName);
                    String defaultValueStr = sortInfo.getAttribute().getDefaultValue();
                    LocalTime defaultValue = defaultValueStr == null ? null : LocalTime.parse(defaultValueStr, TIME_FORMAT);

                    if (noteAttr == null)
                        dateMap.put(d, defaultValue);
                    else
                        dateMap.put(d, LocalTime.parse(noteAttr.toString(), TIME_FORMAT));
                }
                documents.clear();

                dateMap.entrySet().stream()
                        .sorted(sortInfo.getDirection() == Direction.DESCENDING
                                ? new ReverseComparator<>() : new NaturalComparator<>())
                        .forEachOrdered(x -> documents.add(x.getKey()));
            }
            catch (Exception e)
            {
                logger.error(e);
            }
                break;
        default:
            break;
        }
    }


    private Object getNoteAttr(Document document, String attributeName)
    {
        Document attributes = (Document)document.get(NoteConstants.ATTRIBUTES);
        return attributes.get(attributeName);
    }
}
