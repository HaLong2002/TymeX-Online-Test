import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Program {
    private static final List<Product> products = new ArrayList<>() {{
        add(new Product("Laptop", 999.99, 5));
        add(new Product("Smartphone", 499.99, 10));
        add(new Product("Tablet", 299.99, 0));
        add(new Product("Smartwatch", 199.99, 3));
    }};

    private static Double TotalInventoryValue() {
        double result = 0.0;
        for (Product i : products) {
            result = result + (i.getPrice() * i.getQuantity());
        }
        return result;
    }

    private static String ProductWithHighestPrice() {
        Product maxPriceProduct = Collections.max(products, Comparator.comparingDouble(Product::getPrice));
        return maxPriceProduct.getName();
    }

    private static boolean IsInStock(String name) {
        boolean exists = products.stream().anyMatch(product -> product.getName().equalsIgnoreCase(name));
        return exists;
    }

    private static void SortByPrice() {
        // Sort by price in ascending order
        List<Product> products1 = products;
        products1.sort(Comparator.comparingDouble(Product::getPrice));
        System.out.println("Products sorted by price (Ascending):");
        products1.forEach(product -> System.out.println(product.toString()));

        // Sort by price in descending order
        products1.sort((p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
        System.out.println("\nProducts sorted by price (Descending):");
        products1.forEach(product -> System.out.println(product.toString()));
    }

    private static void SortByQuantity() {
        // Sort by price in ascending order
        List<Product> products1 = products;
        products1.sort(Comparator.comparingDouble(Product::getQuantity));
        System.out.println("\nProducts sorted by quantity (Ascending):");
        products1.forEach(product -> System.out.println(product.toString()));

        // Sort by price in descending order
        products1.sort((p1, p2) -> Double.compare(p2.getQuantity(), p1.getQuantity()));
        System.out.println("\nProducts sorted by quantity (Descending):");
        products1.forEach(product -> System.out.println(product.toString()));
    }

    public static void main(String[] args) {
        System.out.printf("%.2f%n", TotalInventoryValue());
        System.out.println(ProductWithHighestPrice());
        System.out.println(IsInStock("Headphones"));
        SortByPrice();
        SortByQuantity();
    }
}
