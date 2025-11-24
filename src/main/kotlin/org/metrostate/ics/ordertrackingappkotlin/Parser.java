package org.metrostate.ics.ordertrackingappkotlin;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser adapter class that reads a JSON file and creates a corresponding Order object.
 * Populates the Order with data from the JSON file.
 * Created for ICS 372-01
 * @author Joseph Murtha hw4546dw
 */
public class Parser {
    private static int nextOrderNumber = 1;

    /**
     * set method that should only be called on when recalling current state
     * from a failed closure, and adds 1.
     * @param orderID   The maximum OrderID from current state
     */
    public static void setNextOrderNumber(int orderID) {
        Parser.nextOrderNumber = orderID + 1;
    }

    /**
     * Parser method creates order object with data populated from given JSON file.
     * Generates a random orderID for the Order.
     *
     * @param file              JSON file to be read
     * @return                  Order object populated with data from the JSON file
     * @throws IOException      if the file cannot be read
     */
	public static Order parseJSONOrder(File file) throws IOException {
        long orderDate;
        Type orderType;
        List<FoodItem> foodItemList = new ArrayList<>();

        // use try-with-resources to ensure the FileReader is closed and the file isn't left locked
        try (FileReader fr = new FileReader(file)) {
            JSONObject jsonObject = new JSONObject(new JSONTokener(fr));
            JSONObject orderJson = jsonObject.getJSONObject("order");
            Object orderDateObj = orderJson.get("order_date");
            if (orderDateObj instanceof Number) orderDate = ((Number) orderDateObj).longValue();
            else orderDate = Long.parseLong(String.valueOf(orderDateObj));


            String typeStr = orderJson.getString("type");
            orderType = Type.valueOf(typeStr.toLowerCase());

            JSONArray itemArray = orderJson.getJSONArray("items");
            for (Object o : itemArray) {
                JSONObject item = (JSONObject) o;
                int quantity = ((Number) item.get("quantity")).intValue();
                double price = ((Number) item.get("price")).doubleValue();
                String name = item.getString("name");
                foodItemList.add(new FoodItem(name, quantity, price));

            }
        }

        Order order = new Order(getNextOrderNumber(),orderType,orderDate,foodItemList);
        order.setCompany("FoodHub (JSON)");
        return order;
    }

    /**
     * parser method creates order object with data populated from given XML file.
     * Generates a random orderID for the Order using getNextOrder() method
     *
     * @param file              XML file to be read
     * @return                  Order object populated with data from the XML file
     * @throws IOException      if file can't be read
     */
    public static Order parseXMLOrder(File file) throws IOException {
        long orderDate = 0;
        Type orderType = null;
        List<FoodItem> foodItemList = new ArrayList<>();

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();

            //Reads and grabs order Date and Type
            NodeList nList = doc.getElementsByTagName("Order");
            for (int i = 0; i < nList.getLength(); i++) {

                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) node;
                    orderDate = Long.parseLong(eElement.getAttribute("id"));
                    String typeStr = eElement.getElementsByTagName("OrderType").item(0).getTextContent();
                    orderType = Type.valueOf(typeStr.toLowerCase());
                }
            }

            //Reads and creates FoodItems for foodItemList
            NodeList nodeList = doc.getElementsByTagName("Item");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                String name;
                int quantity;
                double price;
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) node;
                    name = eElement.getAttribute("type");
                    quantity = Integer.parseInt(eElement.getElementsByTagName("Quantity").item(0).getTextContent());
                    price = Double.parseDouble(eElement.getElementsByTagName("Price").item(0).getTextContent());

                    foodItemList.add(new FoodItem(name, quantity, price));
                }
            }


        } catch (ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }


        Order order = new Order (getNextOrderNumber(), orderType, orderDate, foodItemList);
        order.setCompany("GrubStop (XML)");
        return order;
    }

    /**
     * Parses a saved JSON order file that includes OrderID and Status.
     * This is a separate method from parseJSONOrder to handle extra saved fields such as OrderID, Status, and Company.
     * This is used to restore application state on startup.
     *
     * @param file              JSON file to be read
     * @return                  Order object with restored state
     * @throws IOException      if file can't be read
     */
    public static Order parseSavedJSONOrder(File file) throws IOException {
        int orderId;
        long date;
        double totalPrice;
        Type orderType;
        Status orderStatus;
        String originalCompany = null;
        List<FoodItem> foodItemList = new ArrayList<>();

        try (FileReader reader = new FileReader(file)) {
            JSONObject jsonObject = new JSONObject(new JSONTokener(reader));

            orderId = jsonObject.getInt("orderID");
            date = jsonObject.getLong("date");
            totalPrice = jsonObject.getDouble("totalPrice");

            String typeStr = jsonObject.getString("type");
            orderType = Type.valueOf(typeStr.toLowerCase());

            String statusStr = jsonObject.getString("status");
            orderStatus = Status.valueOf(statusStr);

            if (jsonObject.has("company") && !jsonObject.isNull("company")) {
                originalCompany = jsonObject.getString("company");
            }

            JSONArray itemArray = jsonObject.getJSONArray("foodList");
            for (Object o : itemArray) {
                JSONObject item = (JSONObject) o;
                int quantity = item.getInt("quantity");
                double price = item.getDouble("price");
                String name = item.getString("name");
                foodItemList.add(new FoodItem(name, quantity, price));
            }
        }

        Order order = new Order(orderId, date, totalPrice, orderType, orderStatus, foodItemList);

        if (originalCompany != null && !originalCompany.isEmpty()) {
            if (originalCompany.startsWith("Restored - ")) {
                order.setCompany(originalCompany);
            } else {
                order.setCompany("Restored - " + originalCompany);
            }
        } else {
            order.setCompany("Restored - Unknown");
        }
        
        return order;
    }

    /**
     * Static helper method
     * returns next order number and increments the counter
     * @return int, next Order ID number
     */
    private static int getNextOrderNumber(){
        return nextOrderNumber++;
    }

    /**
     * Main test method for the Parser class.
     * Uses a hardcoded JSON file to test the parser method.
     * Prints to console.
     */
    public static void main(String[] args) throws IOException {
        File file = new File("src/main/orderFiles/testOrders/order_09-16-2025_10-00.json");
        Order myOrder = Parser.parseJSONOrder(file);
        System.out.println(myOrder);
    }
}
