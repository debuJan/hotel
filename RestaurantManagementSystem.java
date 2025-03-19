import java.sql.*;  
import java.util.ArrayList;  
import java.util.List;  
import java.util.Scanner;  

public class RestaurantManagementSystem {  
    public static void main(String[] args) {  
        String databaseName = "restaurant";  
        String menuTableName = "menu";  
        String orderTableName = "orders";  

        try {  
            String url = "jdbc:mysql://localhost:3306/";  
            String userName = "root";  
            String password = "1234@@*";  
            Connection connection = DriverManager.getConnection(url, userName, password);  
            Statement statement = connection.createStatement();  

            String checkDatabaseExistenceSQL = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '" + databaseName + "';";  
            boolean databaseExists = statement.executeQuery(checkDatabaseExistenceSQL).next();  

            if (!databaseExists) {  
                String createDatabaseSQL = "CREATE DATABASE " + databaseName;  
                statement.executeUpdate(createDatabaseSQL);  
            }  

            String useDatabaseSQL = "USE " + databaseName;  
            statement.executeUpdate(useDatabaseSQL);  

            String createMenuTableSQL = "CREATE TABLE IF NOT EXISTS " + menuTableName + " (" +  
                    "ItemName VARCHAR(255) PRIMARY KEY, " +  
                    "Price DECIMAL(10,2));";  

            statement.executeUpdate(createMenuTableSQL);  

            addMenuItems(connection, menuTableName);  

            String createOrderTableSQL = "CREATE TABLE IF NOT EXISTS " + orderTableName + " (" +  
                    "CustomerName VARCHAR(255)," +  
                    "ItemName VARCHAR(255)," +  
                    "Quantity INT," +  
                    "TableNumber INT);";  

            statement.executeUpdate(createOrderTableSQL);  

            boolean exit = false;  
            Scanner scanner = new Scanner(System.in);  
            while (!exit) {  
                System.out.println("Restaurant Management System\n" +  
                        "1. View Menu\n" +  
                        "2. Place Order\n" +  
                        "3. View Orders\n" +  
                        "4. Generate Bill\n" +  
                        "5. Exit\n" +  
                        "Enter your choice (1-5):");  
                String choice = scanner.nextLine();  
                switch (choice) {  
                    case "1":  
                        viewMenu(connection, menuTableName);  
                        break;  
                    case "2":  
                        placeOrder(connection, menuTableName, orderTableName, scanner);  
                        break;  
                    case "3":  
                        viewOrders(connection, orderTableName);  
                        break;  
                    case "4":  
                        generateBill(connection, orderTableName, scanner);  
                        break;  
                    case "5":  
                        exit = true;  
                        break;  
                    default:  
                        System.out.println("Invalid choice. Please enter a number between 1 and 5.");  
                        break;  
                }  
            }  

            statement.close();  
            connection.close();  
            System.out.println("Exiting Restaurant Management System.");  
        } catch (SQLException e) {  
            e.printStackTrace();  
        }  
    }  

    private static void addMenuItems(Connection connection, String tableName) throws SQLException {  
        String[] menuItems = {"Pizza", "Burger", "Pasta", "Salad", "Soda"};  
        double[] prices = {12.99, 8.50, 10.75, 6.99, 2.50};  
        for (int i = 0; i < menuItems.length; i++) {  
            ResultSet resultSet = connection.createStatement().executeQuery(  
                "SELECT * FROM " + tableName + " WHERE ItemName = '" + menuItems[i] + "'");  
            if (!resultSet.next()) {  
                String insertMenuItemSQL = "INSERT INTO " + tableName + " VALUES ('" +  
                    menuItems[i] + "','" +  
                    prices[i] + "')";  
                connection.createStatement().executeUpdate(insertMenuItemSQL);  
            }  
        }  
    }  

    private static void viewMenu(Connection connection, String tableName) throws SQLException {  
        ResultSet resultSet = connection.createStatement().executeQuery(  
            "SELECT * FROM " + tableName);  
        System.out.println("Menu:");  
        while (resultSet.next()) {  
            String itemName = resultSet.getString("ItemName");  
            double price = resultSet.getDouble("Price");  
            System.out.println(itemName + " - $" + price);  
        }  
        System.out.println();  
    }  

    private static void placeOrder(Connection connection, String menuTableName, String orderTableName, Scanner scanner) throws SQLException {  
        System.out.println("Enter Customer Name:");  
        String customerName = scanner.nextLine();  

        List<String> items = new ArrayList<>();  
        List<Integer> quantities = new ArrayList<>();  

        while (true) {  
            System.out.println("Enter Item Name (or type 'done' to finish):");  
            String itemName = scanner.nextLine();  
            if (itemName.equalsIgnoreCase("done")) {  
                break;  
            }  
            items.add(itemName);  
            System.out.println("Enter Quantity:");  
            int quantity = Integer.parseInt(scanner.nextLine());  
            quantities.add(quantity);  
        }  

        System.out.println("Enter Table Number:");  
        int tableNumber = Integer.parseInt(scanner.nextLine());  

        for (int i = 0; i < items.size(); i++) {  
            String itemName = items.get(i);  
            ResultSet resultSet = connection.createStatement().executeQuery(  
                "SELECT * FROM " + menuTableName + " WHERE ItemName = '" + itemName + "'");  
            if (resultSet.next()) {  
                String insertOrderSQL = "INSERT INTO " + orderTableName + " VALUES ('" +  
                    customerName + "','" +  
                    itemName + "','" +  
                    quantities.get(i) + "'," +  
                    tableNumber + ")";  
                connection.createStatement().executeUpdate(insertOrderSQL);  
            } else {  
                System.out.println("Item " + itemName + " not found in the menu. Skipping...");  
                continue;  
            }  
        }  

        System.out.println("Orders placed successfully.");  
    }  

    private static void viewOrders(Connection connection, String tableName) throws SQLException {  
        ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM " + tableName);  
        System.out.println("Orders:");  
        while (resultSet.next()) {  
            String customerName = resultSet.getString("CustomerName");  
            String itemName = resultSet.getString("ItemName");  
            int quantity = resultSet.getInt("Quantity");  
            int tableNumber = resultSet.getInt("TableNumber");  
            System.out.println("Customer: " + customerName + ", Item: " + itemName + ", Quantity: " + quantity + ", Table: " + tableNumber);  
        }  
        System.out.println();  
    }  

    private static void generateBill(Connection connection, String tableName, Scanner scanner) throws SQLException {  
        System.out.println("Enter Customer Name to generate bill:");  
        String customerName = scanner.nextLine();  
        ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM " + tableName + " WHERE CustomerName = '" + customerName + "'");  

        double totalBill = 0;  
        System.out.println("Bill for " + customerName + ":");  
        while (resultSet.next()) {  
            String itemName = resultSet.getString("ItemName");  
            int quantity = resultSet.getInt("Quantity");  
            ResultSet itemResultSet = connection.createStatement().executeQuery("SELECT Price FROM menu WHERE ItemName = '" + itemName + "'");  
            if (itemResultSet.next()) {  
                double price = itemResultSet.getDouble("Price");  
                double subtotal = price * quantity;  
                System.out.println(itemName + " (" + quantity + "): $" + subtotal);  
                totalBill += subtotal;  
            }  
        }  
        System.out.println("Total Bill: $" + totalBill);  
    }  
}