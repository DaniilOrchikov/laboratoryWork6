package ticket;

import utility.CSVReaderAndWriter;

import java.io.Serializable;

/**
 * Класс адреса с полями <b>street</b> и <b>zipCode</b>
 */
public class Address implements Serializable {
    /**
     * Поле улица.
     * Не может быть пустой, Поле не может быть null
     */
    private String street;
    /**
     * Поле индекс.
     * Не может быть null
     */
    private String zipCode;
    public Address(String street, String zipCode){
        this.street = street;
        this.zipCode = zipCode;
    }
    @Override
    public String toString(){
        return String.format("{street:%s, zipCode:%s}", street, zipCode);
    }

    /**
     * @param separator символ разделения колонок в csv файле. {@link CSVReaderAndWriter#separator}
     * @return возвращает строку в формате для записи в csv файл
     */
    public String toCSVFormat(String separator){
        return street + separator + zipCode;
    }
}